package org.dea.swt.canvas;

public class CanvasException extends Exception {
	private static final long serialVersionUID = 3367790045463712845L;
	
	public CanvasException() {}
	public CanvasException(String message) { super(message); }
	public CanvasException(String message, Throwable cause) { super(message, cause); }
}
