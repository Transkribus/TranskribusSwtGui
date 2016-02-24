package eu.transkribus.swt_canvas.util;

public enum MouseButtons {
		BUTTON_UNKNOWN(-1),
		BUTTON_LEFT(1),
		BUTTON_MIDDLE(2),
		BUTTON_RIGHT(3);	
		
		int value;
		
		MouseButtons(int value) { this.value = value; }
		public int toInt() { return value; }
		public static MouseButtons fromInt(int value) {
			switch (value) {
				case 1:
					return BUTTON_LEFT;
//					break;
				case 2:
					return BUTTON_MIDDLE;
//					break;
				case 3:
					return BUTTON_RIGHT;
//					break;
				default:
					return BUTTON_UNKNOWN;
			}
		}
		
		public static MouseButtons[] getValidButtons() {
			return new MouseButtons[]{BUTTON_LEFT, BUTTON_MIDDLE, BUTTON_RIGHT};
		}
		
	}