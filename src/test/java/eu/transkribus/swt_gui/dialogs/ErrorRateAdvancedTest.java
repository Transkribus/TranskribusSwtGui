package eu.transkribus.swt_gui.dialogs;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorRateAdvancedTest {
	
	private final static Logger logger = LoggerFactory.getLogger(ErrorRateAdvancedTest.class);	

	public static void main(String[] args) {
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
		
				ErrorRateAdvancedDialog eDia = new ErrorRateAdvancedDialog(getShell());
			
				eDia.open();
				

				return parent;
			}
		};
		aw.setBlockOnOpen(true);
		aw.open();
		
		
	}

}
