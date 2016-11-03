package eu.transkribus.swt_gui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.swt.util.Fonts;

public class TestHandles {

	static int testHandleLimit() throws Exception {
		if (Display.getCurrent()==null)
			throw new Exception("No current display found!");
		if (!Display.getCurrent().getDeviceData().tracking)
			throw new Exception("Display not tracking!");
		
		int N = (int) 1e6; // nr of object that are tried to be created
		Object[] objs = new Object[N];
		boolean stopOnFirstError = true;
		
		int i=0;
		int firstHandleErrorIndex=-1;
		
		for (i=0; i<N; ++i) {
			System.out.println("i = "+(i+1)+" / "+N);
			
			try {
//				objs[i] = Images.getOrLoad("src/main/resources/NCSR_icon.png");
				objs[i] = Fonts.createFont("Segoe UI", (i+1), SWT.NORMAL); // create fonts with different sizes, s.t. every time a new font gets stored!
				
//				System.out.println("current display: "+Display.getCurrent().getDeviceData().tracking);
				
			} catch (Throwable e) {
				e.printStackTrace();
				if (stopOnFirstError) {
					firstHandleErrorIndex = i+1;
					break;
				}
			}
			
			if (Display.getCurrent().getDeviceData().objects != null)
				System.out.println("nr of objects: "+Display.getCurrent().getDeviceData().objects.length);
		}
		System.out.println("firstHandleErrorIndex = "+i);
		
		return firstHandleErrorIndex;
	}
	
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
						testHandleLimit();
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
