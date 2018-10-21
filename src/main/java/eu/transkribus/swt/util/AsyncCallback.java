package eu.transkribus.swt.util;

public abstract class AsyncCallback<T> {
	
	public abstract void onSuccess(T result);
	public abstract void onError(Throwable error);

}
