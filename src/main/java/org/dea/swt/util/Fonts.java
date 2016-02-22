package org.dea.swt.util;

import java.util.HashMap;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.SysUtils;

public class Fonts {
	private final static Logger logger = LoggerFactory.getLogger(Fonts.class);
	
	public static LocalResourceManager resManager = GlobalResourceManager.getResourceManager();
		
//	static List<FontData> availableFonts;
	static HashMap<String, Font> fontCache = new HashMap<>();
	
	static {
//		availableFonts = new ArrayList<>();
//		for (FontData fd : Display.getDefault().getFontList(null, true)) {
//			availableFonts.add(fd);
//		}
	}
	
	
	
	public static Font createFont(FontData fd) {
		return createFont(fd.getName(), fd.getHeight(), fd.getStyle());
	}
	
	public static Font createBoldFont(Font f) {
		if (f.getFontData().length > 0) {
			FontData fd = f.getFontData()[0];
			fd.setStyle(SWT.BOLD);
			return createFont(fd);
		}
		return null;
	}
	
	public static Font setBoldFont(Control ctrl) {
		Font f = createBoldFont(ctrl.getFont());
		ctrl.setFont(f);
		return f;
	}
	
//	public static Font createRedFont(Font f) {
//		if (f.getFontData().length > 0) {
//			FontData fd = f.getFontData()[0];
//			fd.setStyle(SWT.BOLD);
//			return createFont(fd);
//		}
//		return null;
//	}
	
	public static Font createItalicFont(Font f) {
		if (f.getFontData().length > 0) {
			FontData fd = f.getFontData()[0];
			fd.setStyle(SWT.ITALIC);
			return createFont(fd);
		}
		return null;
	}
	
	public static void setFontHeight(Control ctrl, int height) {
		Font f = createFontWithHeight(ctrl.getFont(), height);
		ctrl.setFont(f);
	}
	
	public static Font createFontWithHeight(Font f, int height) {
		if (f.getFontData().length > 0) {
			FontData fd = f.getFontData()[0];
			fd.setHeight(height);
			return createFont(fd);
		}
		return null;
	}	
	
	public static Font createFont(String name, int height, int style) {
		String id = name+"_"+height+"_"+style;
		
		Font f = fontCache.get(id);
		if (f == null) { // create new font and put it into cache
			f = resManager.createFont(FontDescriptor.createFrom(name, height, style));
			fontCache.put(id, f);
			logger.debug("created font: "+id+" N="+fontCache.size());
		}
		return f;
	}
		
	public static String getSystemFontName(boolean isSerif, boolean isMonospace, boolean isLetterSpaced) {
		boolean w = SysUtils.isWin();
		boolean o = SysUtils.isOsx();
		
		String fontName = w ? "Tahoma" : "Sans";
		
		if (isSerif && isMonospace) {
			fontName = "Courier New";
		}
		else if (isSerif) {
			fontName = w||o ? "Times New Roman" : "Serif";
		}
		else if (isMonospace || isLetterSpaced) {
			fontName = "Monospace";
			if (w)
				fontName = "Lucida Console";
			else if (o)
				fontName = "Monaco";
		}		
		
		return fontName;
		
	}
	
	public static int createStyle(boolean isBold, boolean isItalic) {
		int style = SWT.NORMAL;
		if (isBold && isItalic)
			style = SWT.BOLD | SWT.ITALIC;
		else if (isBold)
			style = SWT.BOLD;
		else if (isItalic)
			style = SWT.ITALIC;
		return style;
	}

	public static int createRise(boolean isSubscript, boolean isSuperscript, Integer height) {
		int rise = 0;
		if (isSubscript) {
			rise -= height / 2;	
		} else if (isSuperscript) {
			rise += height / 2;
		}
		return rise;
	}
	

	
	
	
	
	
	

}
