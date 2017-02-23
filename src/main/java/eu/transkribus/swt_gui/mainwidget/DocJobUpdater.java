package eu.transkribus.swt_gui.mainwidget;

import java.awt.Desktop;
import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.FtpConsts;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.model.beans.job.enums.JobImpl;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt_gui.dialogs.ActivityDialog;
import eu.transkribus.swt_gui.dialogs.ShowServerExportLinkDialog;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

/**
 * Starts a thread that periodically updates jobs registered via the {@link DocJobUpdater#registerJobToUpdate(String)} method
 * @author sebastian
 */
public class DocJobUpdater {
	private final static Logger logger = LoggerFactory.getLogger(DocJobUpdater.class);
	
	Runnable r;
	Thread t;
	Storage store = Storage.getInstance();
	boolean started=false;
	boolean stop=false;
	
	final public int UPDATE_TIME_MS = 3000;
	
	int nExc=0;
	
	Set<String> jobsToUpdate = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());	
	TrpMainWidget mw;
		
	public DocJobUpdater(TrpMainWidget mw) {
		this.mw = mw;
				
		r = new Runnable() {
			@Override
			public void run() {
				started = true;
				logger.debug("starting DocJobUpdater");

				while (true) {
					try {
						Thread.sleep(UPDATE_TIME_MS);
						
						if (stop) {
							logger.debug("stopping DocJobUpdater");
							break;
						}

						if (jobsToUpdate.isEmpty())
							continue;

						logger.trace("jobs to update: " + jobsToUpdate.size());

						store.checkConnection(true);

						// update jobs and remove from list if necessary:
						for (Iterator<String> it = jobsToUpdate.iterator(); it.hasNext();) {
							String jobId = it.next();
							TrpJobStatus job = store.getConnection().getJob(jobId);

							store.sendJobUpdateEvent(job);

							if (job.isFinished()) {
								logger.debug("removing finished job " + jobId + ", nr of unfinished jobs: "
										+ jobsToUpdate.size());
								jobsToUpdate.remove(jobId);
								checkIfFinishedJobAffectsOpenedPage(job);
							}
						}

						nExc = 0;
					} catch (SessionExpiredException | NoConnectionException ex) {
						logger.debug("Session expired or no connection - skipping job update");
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
						nExc++;
						logger.debug("nr of subsequent exceptions: " + nExc);
					}
				}
			}
		};
		
		t = new Thread(r);
		t.start();
	}
	
	private void checkIfFinishedJobAffectsOpenedPage(TrpJobStatus job) {
		
		boolean isThisDocOpen = store.isDocLoaded() && store.getDoc().getId()==job.getDocId();
		// reload current page if page job for this page is finished:
		// (only ask question to reload page!!)
		if (isThisDocOpen && job.isFinished()) {
			Display.getDefault().asyncExec(() -> {
				if (!job.isSuccess()) {
					logger.error("a job for the current page failed: "+job);
					DialogUtil.showErrorMessageBox(mw.getShell(), "A job for this page failed", job.getDescription());
				}
				else if (store.getPageIndex() == (job.getPageNr()-1) || job.getPageNr()==-1) {
					if (job.getJobImpl().equals(JobImpl.DocExportJob.toString())) {
						ShowServerExportLinkDialog linkDiag = new ShowServerExportLinkDialog(mw.getShell(), job.getResult());
						linkDiag.open();
						//DialogUtil.showDownloadLinkDialog(mw.getShell(), "A job for this page finished", "A job for this page just finished - do you want to reload the current page?"); 
						return;
					}
					// reload page if doc and page is open:					
					else if (DialogUtil.showYesNoDialog(mw.getShell(), "A job for this page finished", "A job for this page just finished - do you want to reload the current page?") == SWT.YES) {
						logger.debug("reloading page!");
						mw.reloadCurrentPage(true);						
					}
				}
			});
		}
	}
	
	public void registerJobToUpdate(String jobId) {
		jobsToUpdate.add(jobId);
	}

}
