package eu.transkribus.swt_canvas.util;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.widgets.Display;

public class GlobalResourceManager {
	
	public static LocalResourceManager resManager;
	
	/** Call this from the GUI thread. */
//	public static void init() {
//		resManager = new LocalResourceManager(JFaceResources.getResources());
//	}
	
	static {
		// create a default display if none exists, else JFaceResources.getResources() returns null
		if (Display.getCurrent()==null)
			Display.getDefault();	
	}
	
	public static LocalResourceManager getResourceManager() {
		if (resManager == null)
			resManager = new LocalResourceManager(JFaceResources.getResources());
		return resManager;
	}

}
