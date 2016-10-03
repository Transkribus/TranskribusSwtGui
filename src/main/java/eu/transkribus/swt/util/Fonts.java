package eu.transkribus.swt.util;

import java.util.HashMap;

import javax.swing.JPanel;

import org.docx4j.model.datastorage.XPathEnhancerParser.main_return;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.SebisStopWatch;
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
	
	private final static String Az = "ABCpqr";
	/** A dummy JPanel used to provide font metrics. */
    protected static final JPanel DUMMY_PANEL = new JPanel();
	
	/**
     * Create a <code>FontData</code> object which encapsulate
     * the essential data to create a swt font. The data is taken
     * from the provided awt Font.
     * <p>Generally speaking, given a font size, the returned swt font
     * will display differently on the screen than the awt one.
     * Because the SWT toolkit use native graphical resources whenever
     * it is possible, this fact is platform dependent. To address
     * this issue, it is possible to enforce the method to return
     * a font with the same size (or at least as close as possible)
     * as the awt one.
     * <p>When the object is no more used, the user must explicitly
     * call the dispose method on the returned font to free the
     * operating system resources (the garbage collector won't do it).
     *
     * @param device The swt device to draw on (display or gc device).
     * @param font The awt font from which to get the data.
     * @param ensureSameSize A boolean used to enforce the same size
     * (in pixels) between the awt font and the newly created swt font.
     * @return a <code>FontData</code> object.
     */
    public static FontData toSwtFontData(Device device, java.awt.Font font,
            boolean ensureSameSize) {
        FontData fontData = new FontData();
        fontData.setName(font.getFamily());
        // SWT and AWT share the same style constants.
        fontData.setStyle(font.getStyle());
        // convert the font size (in pt for awt) to height in pixels for swt
        int height = (int) Math.round(font.getSize() * 72.0
                / device.getDPI().y);
        fontData.setHeight(height);
        // hack to ensure the newly created swt fonts will be rendered with the
        // same height as the awt one
        if (ensureSameSize) {
            GC tmpGC = new GC(device);
            Font tmpFont = new Font(device, fontData);
            tmpGC.setFont(tmpFont);
            if (tmpGC.textExtent(Az).x
                    > DUMMY_PANEL.getFontMetrics(font).stringWidth(Az)) {
                while (tmpGC.textExtent(Az).x
                        > DUMMY_PANEL.getFontMetrics(font).stringWidth(Az)) {
                    tmpFont.dispose();
                    height--;
                    fontData.setHeight(height);
                    tmpFont = new Font(device, fontData);
                    tmpGC.setFont(tmpFont);
                }
            }
            else if (tmpGC.textExtent(Az).x
                    < DUMMY_PANEL.getFontMetrics(font).stringWidth(Az)) {
                while (tmpGC.textExtent(Az).x
                        < DUMMY_PANEL.getFontMetrics(font).stringWidth(Az)) {
                    tmpFont.dispose();
                    height++;
                    fontData.setHeight(height);
                    tmpFont = new Font(device, fontData);
                    tmpGC.setFont(tmpFont);
                }
            }
            tmpFont.dispose();
            tmpGC.dispose();
        }
        return fontData;
    }

    /**
     * Create an awt font by converting as much information
     * as possible from the provided swt <code>FontData</code>.
     * <p>Generally speaking, given a font size, an swt font will
     * display differently on the screen than the corresponding awt
     * one. Because the SWT toolkit use native graphical ressources whenever
     * it is possible, this fact is platform dependent. To address
     * this issue, it is possible to enforce the method to return
     * an awt font with the same height as the swt one.
     *
     * @param device The swt device being drawn on (display or gc device).
     * @param fontData The swt font to convert.
     * @param ensureSameSize A boolean used to enforce the same size
     * (in pixels) between the swt font and the newly created awt font.
     * @return An awt font converted from the provided swt font.
     */
    public static java.awt.Font toAwtFont(Device device, FontData fontData,
            boolean ensureSameSize) {
        int height = (int) Math.round(fontData.getHeight() * device.getDPI().y
                / 72.0);
        // hack to ensure the newly created awt fonts will be rendered with the
        // same height as the swt one
        if (ensureSameSize) {
            GC tmpGC = new GC(device);
            Font tmpFont = new Font(device, fontData);
            tmpGC.setFont(tmpFont);
            JPanel DUMMY_PANEL = new JPanel();
            java.awt.Font tmpAwtFont = new java.awt.Font(fontData.getName(),
                    fontData.getStyle(), height);
            if (DUMMY_PANEL.getFontMetrics(tmpAwtFont).stringWidth(Az)
                    > tmpGC.textExtent(Az).x) {
                while (DUMMY_PANEL.getFontMetrics(tmpAwtFont).stringWidth(Az)
                        > tmpGC.textExtent(Az).x) {
                    height--;
                    tmpAwtFont = new java.awt.Font(fontData.getName(),
                            fontData.getStyle(), height);
                }
            }
            else if (DUMMY_PANEL.getFontMetrics(tmpAwtFont).stringWidth(Az)
                    < tmpGC.textExtent(Az).x) {
                while (DUMMY_PANEL.getFontMetrics(tmpAwtFont).stringWidth(Az)
                        < tmpGC.textExtent(Az).x) {
                    height++;
                    tmpAwtFont = new java.awt.Font(fontData.getName(),
                            fontData.getStyle(), height);
                }
            }
            tmpFont.dispose();
            tmpGC.dispose();
        }
        return new java.awt.Font(fontData.getName(), fontData.getStyle(),
                height);
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
	
	public static Font createNormalFont(Font f) {
		if (f.getFontData().length > 0) {
			FontData fd = f.getFontData()[0];
			fd.setStyle(SWT.NORMAL);
			return createFont(fd);
		}
		return null;
	}
	
	public static Font setBoldFont(Control ctrl) {
		Font f = createBoldFont(ctrl.getFont());
		ctrl.setFont(f);
		return f;
	}
	
	public static Font setNormalFont(Control ctrl) {
		Font f = createNormalFont(ctrl.getFont());
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
	
	public static Font changeStyleBit(Font f, int style, boolean add) {
		if (f.getFontData().length  == 0)
			return null;
		
		FontData fd = f.getFontData()[0];
		if (add)
			fd.setStyle(fd.getStyle() | style);
		else
			fd.setStyle(fd.getStyle()  ^ style);

		return createFont(fd);
	}
	
	public static Font addStyleBit(Font f, int style) {
		if (f.getFontData().length == 0)
			return null;
		
		FontData fd = f.getFontData()[0];
		fd.setStyle(fd.getStyle() | style);
		
		return createFont(fd);
	}
	
	public static Font removeStyleBit(Font f, int style) {
		if (f.getFontData().length == 0)
			return null;
		
		FontData fd = f.getFontData()[0];
		fd.setStyle(fd.getStyle() & ~style);
		
		return createFont(fd);
	}
		
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
	
	public static void main(String[] args) {
		SebisStopWatch sw = new SebisStopWatch();
		
		sw.start();
		Font font = Fonts.createFont("Arial", 14, SWT.NORMAL);
		sw.stop();
		
		
		
		sw.start();
		java.awt.Font awtFont = Fonts.toAwtFont(Display.getDefault(), font.getFontData()[0], false);
		sw.stop();
		
		
		
		sw.start();
		int i = awtFont.canDisplayUpTo("asdkjfla kajdlfj kljalsdf asdf asdfasdf adf asdfasdf asdfasdf asdf asdf"
				+ "asdkjfla kajdlfj kljalsdf asdf asdfasdf adf asdfasdf asdfasdf asdf asdf"
				+ "asdkjfla kajdlfj kljalsdf asdf asdfasdf adf asdfasdf asdfasdf asdf asdf"
				+ "asdkjfla kajdlfj kljalsdf asdf asdfasdf adf asdfasdf asdfasdf asdf asdf"
				+ "asdkjfla kajdlfj kljalsdf asdf asasdfead hjgjkzöäpö#pü# sdf"
				+ "asdkjfla kajdlfj kljalsdf asdf asdfasdf adf asdfasdf asdfasdf asdf asdf"
				+ "asdkjfla kajdlfj kljalsdf asdf asdfasdf adf asdfasdf asdfasdf asdf asdf"
				+ "123ß4ß29ß01.,yxcmv,ycnb,yn,bmynx,cmnß0=)/($!\"§$%&/()=?"
				+ "asdf dd"
				+ "\u10FFFE"
				+ "");
		
		
		
		sw.stop();
		
		System.out.println("i = "+i);
		
//		sw.start();
//		java.awt.Font awtFont1 = Fonts.toAwtFont(Display.getDefault(), font.getFontData()[0], true);
//		sw.stop(true);
		
		
		
	}
	
	
	
	
	
	

}
