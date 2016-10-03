package eu.transkribus.swt.util;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;

public class Colors {
//	public final static LocalResourceManager resManager = new LocalResourceManager(JFaceResources.getResources());
	
	public static Color getSystemColor(int id) { 
		return Display.getDefault().getSystemColor(id);
	}
	
	public static Color createColor(RGB rgb) {
		return SWTResourceManager.getColor(rgb);
//		return resManager.createColor(rgb);
	}
	
	public static Color decode(String code) throws NumberFormatException {
		java.awt.Color c = java.awt.Color.decode(code);
		RGB rgb = new RGB(c.getRed(), c.getGreen(), c.getBlue());
		return createColor(rgb);
	}
	
	public static Color decode2(String code) throws NumberFormatException {
		try {
			if (code == null)
				return null;
			
			return decode(code);
		} catch (Exception e) {
			return null;
		}
	}	
	
	public static String toHex(int r, int g, int b) {
	    return "#" + toBrowserHexValue(r) + toBrowserHexValue(g) + toBrowserHexValue(b);
	  }

	  private static String toBrowserHexValue(int number) {
	    StringBuilder builder = new StringBuilder(Integer.toHexString(number & 0xff));
	    while (builder.length() < 2) {
	      builder.append("0");
	    }
	    return builder.toString().toUpperCase();
	  }
 
}
