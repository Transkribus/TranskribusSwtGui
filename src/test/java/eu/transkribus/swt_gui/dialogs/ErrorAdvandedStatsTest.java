package eu.transkribus.swt_gui.dialogs;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpErrorRate;

public class ErrorAdvandedStatsTest {
	
	private final static Logger logger = LoggerFactory.getLogger(ErrorRateAdvancedTest.class);	

	public static void main(String[] args) {
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
		
				TrpErrorRate e = new TrpErrorRate();
				e.setWer("12,2%");
				e.setwAcc("6,98%");
				e.setCer("10,66%");
				e.setcAcc("29,38%");
				e.setBagTokensF("18,96%");
				e.setBagTokensPrec("24,91%");
				e.setBagTokensRec("40.21528861154446177");
				
				ErrorRateAdvancedStats eDia = new ErrorRateAdvancedStats(getShell(),e);
			
				eDia.open();
				

				return parent;
			}
		};
		aw.setBlockOnOpen(true);
		aw.open();
		
		
	}

}
