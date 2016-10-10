package eu.transkribus.swt_gui.mainwidget;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.job.TrpJobStatus;

public abstract class DocJobUpdater {
	private final static Logger logger = LoggerFactory.getLogger(DocJobUpdater.class);
	
	Runnable r;
	Thread t;
	Storage store = Storage.getInstance();
	boolean started=false;
	boolean stop=false;
	
	final public int UPDATE_TIME_MS = 3000;
	
//	Object monitor = new Object();
	private final ReentrantLock lock = new ReentrantLock();
	final Condition condition = lock.newCondition();
	Map<String, TrpJobStatus> unfinished = new HashMap<>();
	
	int nExc=0;
	
	public DocJobUpdater() {
		r = new Runnable() {
			@Override public void run() {
				started = true;
				
//				synchronized (monitor) {
					lock.lock();
					try {
					while(true) {
//						TrpMainWidget mw = TrpMainWidget.getInstance();
												
						try {
							Thread.sleep(UPDATE_TIME_MS);

							if (stop) {
								logger.info("Doc update thread stopped");
								break;
							}
							
							// remove finished jobs from unfinished map:
							for (Iterator<Map.Entry<String, TrpJobStatus>> it = unfinished.entrySet().iterator(); it.hasNext();) {
								Map.Entry<String, TrpJobStatus> entry = it.next();
								if (entry.getValue().isFinished())
									unfinished.remove(entry.getKey());
							}

							// get new unfinished jobs from database and merge them into the unfinished map:
							List<TrpJobStatus> newUnfinishedJobs = store.getUnfinishedJobs(true);
							for (TrpJobStatus j : newUnfinishedJobs) {
								TrpJobStatus existing = unfinished.get(j.getJobId());
								if (existing != null) {
									existing.setState(j.getState());
								} else {
									unfinished.put(j.getJobId(), j);
								}
							}

							// update unfinished jobs (that may also include some finished jobs at this time!)
							logger.debug("n unfinished jobs 1: "+unfinished.size());
							for (TrpJobStatus j : unfinished.values()) {
								updateJob(j);
							}
							
							// check if unfinished map is empty and send thread to sleep if so:
							if (unfinished.size() == 0) {
								logger.debug("sending thread to sleep");
								condition.await();
								logger.debug("DocJobUpdater just woke up");
							}
							
							nExc=0;
						}
						catch (SessionExpiredException | NoConnectionException ex) {
							logger.warn("Session expired or no connection - stopping job update thread!");
							stopJobThread();
						}
						catch(Exception ex) {
							logger.error(ex.getMessage(), ex);
							nExc++;
							logger.debug("nr of subsequent exceptions: "+nExc);
//							if (nExc > 3) {
//								stopJobThread();
//							}
						}
//					}
				}
					} finally {
						lock.unlock();
					}
			}
		};
		
		t = new Thread(r);
	}
	
	public boolean isThisDocOpen(TrpJobStatus job) {
		return store.isDocLoaded() && store.getDoc().getId()==job.getDocId();
	}
	
	private void updateJob(final TrpJobStatus job) throws Exception {
		logger.trace("Updating job: "+job);
		
		final TrpJobStatus jobUpdated = store.loadJob(job.getJobId());
		if (jobUpdated==null) {
			logger.error("Could not update job with id = "+job.getJobId()+" - the return value was null! Check your code!");
			return;
		}
		
		// merge that also into the unfinished jobs:
		TrpJobStatus existing = unfinished.get(jobUpdated.getJobId());
		if (existing != null) { // should always be != null here
			existing.setState(jobUpdated.getState());
		}
		
		if (jobUpdated != null)
		Display.getDefault().asyncExec(new Runnable() {
			@Override public void run() {
				onUpdate(jobUpdated);
			}
		});
	}
	
	public synchronized void startOrResumeJobThread() throws IllegalMonitorStateException {
		if (!started) {
			t.start();
		} else {
			if (lock.isLocked()) // thread already running
				return;
			else { // thread is sleeping -> wake up!
				lock.lock();
				try {
					condition.signalAll();
				} finally {
					lock.unlock();
				}
			}
		}
	}
	
	public synchronized void stopJobThread() throws IllegalMonitorStateException {
		stop = true;
		if (started) {
			startOrResumeJobThread();
		}
	}
	
//	public abstract void onFinished(TrpJobStatus job);
	public abstract void onUpdate(TrpJobStatus job);
}
