package eu.transkribus.swt_gui.exceptions;

public class BaselineExistsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1245197955947442370L;

	public BaselineExistsException() {
	}

	public BaselineExistsException(String message) {
		super(message);
	}

	public BaselineExistsException(Throwable cause) {
		super(cause);
	}

	public BaselineExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public BaselineExistsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
