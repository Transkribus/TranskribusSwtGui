package eu.transkribus.swt_canvas.util;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;

public class GlobalResourceManager {
	
	public static LocalResourceManager resManager;
	
	/** Call this from the GUI thread. */
//	public static void init() {
//		resManager = new LocalResourceManager(JFaceResources.getResources());
//	}
	
	public static LocalResourceManager getResourceManager() {
		if (resManager == null)
			resManager = new LocalResourceManager(JFaceResources.getResources());
		return resManager;
	}

}
