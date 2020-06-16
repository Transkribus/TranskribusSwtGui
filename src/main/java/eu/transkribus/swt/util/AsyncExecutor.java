package eu.transkribus.swt.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncExecutor {
	private static final Logger logger = LoggerFactory.getLogger(AsyncExecutor.class);
	
	public interface AsyncCallback<R> {
		void onError(Throwable error);
		void onSuccess(R result);
	}
	
	public static final int DEFAULT_N_THREADS = 2;
	
	ThreadPoolExecutor executor;
	boolean purgeBeforeInsertion=true;
	boolean runCallbackOnUiThread=true;
	
	List<Future<?>> futures=new ArrayList<>();
	
	private static AsyncExecutor defaultExecutor;
	
	public AsyncExecutor() {
		this(DEFAULT_N_THREADS, true, true);
	}
	
	public AsyncExecutor(int nThreads, boolean purgeBeforeInsertion, boolean runCallbackOnUiThread) {
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(nThreads);
		this.purgeBeforeInsertion = purgeBeforeInsertion;
		this.runCallbackOnUiThread = runCallbackOnUiThread;
	}
	
	public static AsyncExecutor get() {
		if (defaultExecutor==null) {
			defaultExecutor = new AsyncExecutor();
		}
		return defaultExecutor;
	}	
	
	public <R> Future<R> runAsync(String taskName, Callable<R> callable, AsyncCallback<R> callback) {
		printInfo("", taskName);
		if (purgeBeforeInsertion) { // try to stop all existing threads
			cancelFutures();
			executor.purge();
			printInfo("after purge - ", taskName);
		}
		
		Future<R> future = executor.submit(() -> {
			try {
				R result = callable.call();
				
				if (runCallbackOnUiThread) {
					Display.getDefault().syncExec(() -> {
						callback.onSuccess(result);	
					});					
				}
				else {
					callback.onSuccess(result);					
				}
				return result;
			} catch (Throwable e) {
				if (runCallbackOnUiThread) {
					Display.getDefault().syncExec(() -> {
						callback.onError(e);
					});					
				}
				else {
					callback.onError(e);					
				}
				throw e;
			}
		});
		futures.add(future);
		removeFinishedOrCancelledFutures();
		return future;
	}
	
	private void printInfo(String prefix, String taskName) {
		logger.debug(prefix+taskName+" nr-running tasks = "+executor.getActiveCount()+", n-futures = "+futures.size()+", poolsize = "+executor.getPoolSize()+", max-pool-size = "+executor.getMaximumPoolSize()+" queue-size = "+executor.getQueue().size());
	}
	
	private void removeFinishedOrCancelledFutures() {
		Iterator<Future<?>> it = futures.iterator();
		while (it.hasNext()) {
			Future<?> future = it.next();
			if (future.isDone() || future.isCancelled()) {
				it.remove();
			}
		}
	}
	
	private void cancelFutures() {
		Iterator<Future<?>> it = futures.iterator();
		while (it.hasNext()) {
			Future<?> future = it.next();
			if (!future.isCancelled()) {
				future.cancel(false);
			}
		}
	}	
	
	public static <R> void onError(AsyncCallback<R> callback, Throwable error) {
		if (callback != null) {
			callback.onError(error);
		}
	}
	
	public static <R> void onSuccess(AsyncCallback<R> callback, R result) {
		if (callback != null) {
			callback.onSuccess(result);
		}
	}	
}


