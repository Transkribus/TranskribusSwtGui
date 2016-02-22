package eu.transkribus.swt_gui.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.lang.ObjectUtils;
import org.dea.swt.canvas.shapes.ICanvasShape;
import org.dea.swt.util.Fonts;
import org.dea.util.Utils;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import eu.transkribus.core.model.beans.pagecontent.TextStyleType;
import eu.transkribus.core.model.beans.pagecontent_extension.ITrpShapeType;
import eu.transkribus.swt_gui.mainwidget.TrpSettings;

public class GuiUtil {
	private static Logger logger = LoggerFactory.getLogger(GuiUtil.class);
	public final static int DEFAULT_TEXT_HEIGHT=20;
		
	public static File LOG_FILE = new File("logs/TrpGui.log");
	public static String LOG_FILE_TAIL_FN = "logs/TrpGuiTail.log";
	
	public static File getTailOfLogFile(int nMaxBytes) throws IOException {
		long L = LOG_FILE.length();
		logger.debug("log file size: "+L+" maxBytes: "+nMaxBytes);
		
		long off = 0;
		if (nMaxBytes >= L) {
			return LOG_FILE;
		}
		
		off = L-nMaxBytes;
		RandomAccessFile raf = new RandomAccessFile(LOG_FILE, "r");
		raf.seek(off);
		
		byte[] buff = new byte[nMaxBytes];
		int N = raf.read(buff);
		
		raf.close();
		
//		Path p = Files.write(Paths.get(LOG_FILE_TAIL_FN), buff);
		
	
		FileOutputStream fos = new FileOutputStream(LOG_FILE_TAIL_FN);
		fos.write(buff, 0, N);
		fos.close();
		
		File logFileTail = new File(LOG_FILE_TAIL_FN);
		return logFileTail;
	}
	
	private static void configureLogbackFromLocalFile() {
		boolean localLogFileExists = Files.exists(Paths.get("./logback.xml"));
		if (!localLogFileExists) {
			System.out.println("logback.xml not found in local path - defaulting to packaged one");
			return;
		}
		
		// assume SLF4J is bound to logback in the current environment
	    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
	    
	    try {
	      JoranConfigurator configurator = new JoranConfigurator();
	      configurator.setContext(context);
	      // Call context.reset() to clear any previous configuration, e.g. default 
	      // configuration. For multi-step configuration, omit calling context.reset().
	      context.reset();
	      configurator.doConfigure("./logback.xml");
	    } catch (JoranException je) {
	      // StatusPrinter will handle this
	    }
	    StatusPrinter.printInCaseOfErrorsOrWarnings(context);		
		
	}

	private static void findLogFile() {
//		logger.info("initinddg log file!!!");
//		LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
////		context.reset();
//		logger.info("nLoggers = "+context.getLoggerList().size());
////		for (Logger logger : context.getLoggerList()) {
////			logger.info("here");
//		ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) logger;
//		
//		
//		        for (Iterator<Appender<ILoggingEvent>> index = ((ch.qos.logback.classic.Logger) logger).iteratorForAppenders(); index.hasNext();) {
////		        	logger.info("here");
//		            Appender<ILoggingEvent> appender = index.next();
//		            logger.info("appender: "+appender);
//					if (appender instanceof FileAppender) {
//						LOG_FILE = new File(((FileAppender) appender).getFile());
//						logger.info("log file is: "+LOG_FILE.getAbsolutePath());
//						return;
//					}
//		       }
//		}
		
		
//		System.out.println("searching log file");
//		Enumeration e = LoggerFactory.getLogger("org.dea").getAllAppenders();
//		while (e.hasMoreElements()) {
//			Appender app = (Appender) e.nextElement();
////			logger.info("appender: "+app);
//			if (app instanceof FileAppender) {
//				LOG_FILE = new File(((FileAppender) app).getFile());
//				logger.info("log file is: "+LOG_FILE.getAbsolutePath());
//				return;
//			}
//		}
//		throw new RuntimeException("Could not find log file!");
	}
	
	public static void initLogger() {
		try {
			// FIXME???????????????
			configureLogbackFromLocalFile();
//			PropertyConfigurator.configure("./log4j.properties");
//			findLogFile();
		}
		catch (Throwable t) {
			System.err.println("Error initializing logger: "+t.getMessage());
			t.printStackTrace();
			System.exit(1);
		}
	}

	/** Returns the ITrpShapeType object from the data field of the ICanvasShape or null if not there */
	public static ITrpShapeType getTrpShape(ICanvasShape s) {
		return (s != null && s.getData() != null) ? (ITrpShapeType) s.getData() : null;
	}
	
	/** Returns the ICanvasShape object from the data field of the ITrpShapeType or null if not there */
	public static ICanvasShape getCanvasShape(ITrpShapeType ts) {
		return (ts != null && ts.getData() != null) ? (ICanvasShape) ts.getData() : null;
	}
		
	public static TextStyle getDefaultSWTTextStyle(FontData fd, TrpSettings settings) {
		return getSWTTextStyle(null, fd, settings);
	}
	
	public static TextStyle getSWTTextStyleFromShape(ITrpShapeType st, FontData fd, TrpSettings settings) {
		if (st==null)
			return getSWTTextStyle(null, fd, settings);
		else
			return getSWTTextStyle(st.getTextStyle(), fd, settings);
	}

	/** Converts a TRP {@link TextStyleType} to an SWT {@link TextStyle} object 
	 * @param tst The TextStyleType object to convert to an SWT TextStyle object. If null, a default TextStyle is created 
	 * @param fd Font data currently set in the widget. If null, a default font is created.
	 * @param settings The trp settings from the main widget
	 */
	public static TextStyle getSWTTextStyle(TextStyleType tst, FontData fd, TrpSettings settings) {
		TextStyle swtTs = new TextStyle();
		
		if (tst == null) {
			tst = new TextStyleType(); // if no TextStyleType given, create default
		}
		if (fd == null) fd = Display.getDefault().getSystemFont().getFontData()[0]; // if no font-data given, get default
		
		// return default font if no TextStyleType given:
//		if (tst == null) {
//			FontData fdDefault = fd==null ? Display.getDefault().getSystemFont().getFontData()[0] : fd;
//			swtTs.font = Fonts.createFont(fdDefault.getName(), fdDefault.getHeight(), fdDefault.getStyle());
//			return swtTs;
//		}
		
		int height = fd==null ? DEFAULT_TEXT_HEIGHT : fd.getHeight();
		if (tst.getFontSize()!=null && fd==null) {
			height = tst.getFontSize().intValue();
		}
		
		String fontName="";
		if (settings.isRenderFontStyles()) {
			fontName = Fonts.getSystemFontName(Utils.val(tst.isSerif()), Utils.val(tst.isMonospace()), Utils.val(tst.isLetterSpaced()));
		}
		else {
			fontName = fd.getName();
		}

		logger.trace("fontName = "+fontName);
		
		int style = settings.isRenderTextStyles() ? Fonts.createStyle(Utils.val(tst.isBold()), Utils.val(tst.isItalic())) : fd.getStyle();
		
		if (settings.isRenderOtherStyles()) {
			swtTs.rise = Fonts.createRise(Utils.val(tst.isSubscript()), Utils.val(tst.isSuperscript()), height);
			swtTs.underline = Utils.val(tst.isUnderlined());
			swtTs.strikeout = Utils.val(tst.isStrikethrough());
		}
		
		swtTs.font = Fonts.createFont(fontName, height, style);
				
		return swtTs;
	}
	
	public static boolean isEqualStyle(TextStyleType s1, TextStyleType s2) {
	    return (
	    		ObjectUtils.equals(s1.getFontFamily(), s2.getFontFamily()) &&
	    		ObjectUtils.equals(s1.isSerif(), s2.isSerif()) &&
	    		ObjectUtils.equals(s1.isMonospace(), s2.isMonospace()) &&
	    		ObjectUtils.equals(s1.getFontSize(), s2.getFontSize()) &&
	    		ObjectUtils.equals(s1.getKerning(), s2.getKerning()) &&
	    		ObjectUtils.equals( s1.getTextColour(), s2.getTextColour()) &&
	    		ObjectUtils.equals(s1.getBgColour(), s2.getBgColour()) &&
	    		ObjectUtils.equals(s1.isReverseVideo(), s2.isReverseVideo()) &&
	    		ObjectUtils.equals(s1.isBold(), s2.isBold()) &&
	    		ObjectUtils.equals(s1.isItalic(), s2.isItalic()) &&
	    		ObjectUtils.equals(s1.isUnderlined(), s2.isUnderlined()) &&
	    		ObjectUtils.equals(s1.isSubscript(), s2.isSubscript()) &&
	    		ObjectUtils.equals(s1.isSuperscript(), s2.isSuperscript()) &&
	    		ObjectUtils.equals(s1.isStrikethrough(), s2.isStrikethrough()) &&
	    		ObjectUtils.equals(s1.isSmallCaps(), s2.isSmallCaps()) &&
	    		ObjectUtils.equals(s1.isLetterSpaced(), s2.isLetterSpaced())
	    );
	}
	
	
}
