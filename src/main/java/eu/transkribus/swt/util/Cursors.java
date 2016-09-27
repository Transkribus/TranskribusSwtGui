package eu.transkribus.swt.util;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class Cursors {

	public final static LocalResourceManager resManager = new LocalResourceManager(JFaceResources.getResources());
	
	public static final Cursor cursorHand = constructCursor(Images.getOrLoad("/icons/cursor_hand.png"), 0, 0);
	public static final Cursor cursorHandDrag = constructCursor(Images.getOrLoad("/icons/cursor_drag_hand.png"), 0, 0);	
	
	public static Color getSystemColor(int id) { return Display.getCurrent().getSystemColor(id); }
	
	private static Cursor constructCursor(Image image, int hx, int hy) {
		Image im = Images.getOrLoad( "/icons/cursor.png");
		
		return new Cursor(Display.getCurrent(), image.getImageData(), hx, hy);
	}
	

}
