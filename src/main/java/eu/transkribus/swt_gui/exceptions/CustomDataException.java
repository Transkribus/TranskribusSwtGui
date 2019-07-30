package eu.transkribus.swt_gui.exceptions;

public class CustomDataException extends Exception {
	private static final long serialVersionUID = -7074844950046778755L;
	Object data;
	
	public CustomDataException(String message, Object data) {
		super(message);
		this.data = data;
	}
	
	public Object getData() {
		return data;
	}

}
