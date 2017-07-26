package eu.transkribus.swt_gui.exceptions;

public class NoParentLineException extends Exception {
	private static final long serialVersionUID = 6638935219474283001L;

	public NoParentLineException() {
	}

	public NoParentLineException(String message) {
		super(message);
	}

	public NoParentLineException(Throwable cause) {
		super(cause);
	}

	public NoParentLineException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoParentLineException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
