package eu.transkribus.swt_gui.dialogs;

import java.util.concurrent.Future;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class ErrorRateAdvancedTest {
	
	private final static Logger logger = LoggerFactory.getLogger(ErrorRateAdvancedTest.class);	

	public static void main(String[] args) {
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				
				try {
					Storage.i().login(TrpServerConn.TEST_SERVER_URI, args[0], args[1]);
					Future<?> fut = Storage.i().reloadDocList(2875);
					fut.get();
					
//					Storage.i().loadRemoteDoc(1, 7885);
					Storage.i().loadRemoteDoc(2875, 10846);
					logger.info("login success!");
				} catch (Exception e) {
					e.printStackTrace();
				}				
		
				ErrorRateAdvancedDialog eDia = new ErrorRateAdvancedDialog(getShell());
			
				eDia.open();
				

				return parent;
			}
		};
		aw.setBlockOnOpen(true);
		aw.open();
		
		
	}

}
