package eu.transkribus.swt_canvas.util;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

public class Resources {
	private final static LocalResourceManager resManager = new LocalResourceManager(JFaceResources.getResources());
//	final static Bundle bundle = FrameworkUtil.getBundle(Icons.class);
	
//	public final static Image FOLDER_OPEN = createImage("icons/fldr_obj.gif");
//	public final static Image ZOOM = createImage("icons/zoom.gif");
	
	public final static Cursor CURSOR_ZOOM = createCursor("icons/magnifier.png", 0, 0);
	
	static {
//		loggerger.debug("FOLDER_OPEN = "+FOLDER_OPEN.toString()+" bounds = "+FOLDER_OPEN.getBounds());
		
	}
	
	public static Image createImage(String location) {
		return resManager.createImage(ImageDescriptor.createFromURL(Resources.class.getClassLoader().getResource(location)));
	}
	
	public static Cursor createCursor(String location, int hotspotX, int hotspotY) {
		ImageData imData = ImageDescriptor.createFromURL(Resources.class.getClassLoader().getResource(location)).getImageData();
		Cursor curs = new Cursor(Display.getDefault(), imData, hotspotX, hotspotY);
		return curs;
	}

}
