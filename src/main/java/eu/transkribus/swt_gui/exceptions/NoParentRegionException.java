package eu.transkribus.swt_gui.exceptions;

public class NoParentRegionException extends Exception {
	private static final long serialVersionUID = -2796098568286337926L;

	public NoParentRegionException() {
		super();
	}

	public NoParentRegionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NoParentRegionException(String message) {
		super(message);
	}

	public NoParentRegionException(Throwable cause) {
		super(cause);
	}

}
