package eu.transkribus.swt_gui.util;

import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.swt.util.SWTUtil;

public class TestHandles {
	
	public static void main(String[] args) {
		DeviceData data = new DeviceData();
		data.tracking = true;

		Display display = new Display(data);
		Shell shell = new Shell(display);
		shell.open();
		
//		String fn = Display.getCurrent().getSystemFont().getFontData()[0].getName();
//		System.out.println(fn);
//		if (true)
//			return;

		// run the event loop as long as the window is open
		while (!shell.isDisposed()) {
		    // read the next OS event queue and transfer it to a SWT event
		        if (!display.readAndDispatch())
		         {
		        	
		        	try {
						SWTUtil.testHandleLimit((int)1e6);
					} catch (Exception e) {
						e.printStackTrace();
					}
		        	
		        	if (true)
		        		break;

		        // if there are currently no other OS event to process
		        // sleep until the next OS event is available
		                display.sleep();
		         }
		}

		// disposes all associated windows and their components
		display.dispose();		

		System.out.println("--- DONE ---");
	}

}
