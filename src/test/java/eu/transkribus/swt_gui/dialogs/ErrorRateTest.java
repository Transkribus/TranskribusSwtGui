package eu.transkribus.swt_gui.dialogs;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import eu.transkribus.core.model.beans.TrpErrorRate;

public class ErrorRateTest {
	
	public static void main(String[] args) {
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
			
				TrpErrorRate rate = new TrpErrorRate();
				rate.setWer("1%");
				rate.setCer("4%");
				rate.setcAcc("12.1%");
				rate.setwAcc("12.34%");
				rate.setBagTokensF("98.2%");
				rate.setBagTokensPrec("13.2%");
				rate.setBagTokensRec("76.0%");
		
				
				ErrorRateDialog eDia = new ErrorRateDialog(getShell(),rate);
				eDia.open();

				return parent;
			}
		};
		aw.setBlockOnOpen(true);
		aw.open();

		Display.getCurrent().dispose();
		
		
	}


}
