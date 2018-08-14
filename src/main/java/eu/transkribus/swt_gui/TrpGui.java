package eu.transkribus.swt_gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.SebisStopWatch;
import eu.transkribus.core.util.SysUtils;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.util.Utils;

public class TrpGui {
	private final static Logger logger = LoggerFactory.getLogger(TrpGui.class);
	
//	static final String LIBS_FOLDER = TrpConfig.getTrpSettings().getLibDir();
	static final String LIBS_FOLDER = "libs";
	
	private static String stripSwtVersion(String swtJarFileName) {
		return swtJarFileName.split("-")[1];
	}
	
	public static File getLatestSWTJarFile() {
		File libsFolder = new File(LIBS_FOLDER);
		logger.debug(""+libsFolder.exists());
		
		File[] swtJars = libsFolder.listFiles(new FilenameFilter() {
			@Override public boolean accept(File dir, String name) {
				return name.startsWith("swt-") && name.endsWith(SysUtils.getOSName()+ SysUtils.getArchName()+".jar");
			}
		});
		
		logger.info("N swtJars = "+swtJars.length);
		
		if (swtJars.length==0)
			throw new RuntimeException("No SWT lib found in lib-folder "+LIBS_FOLDER+" for arch="+SysUtils.getArchName()+" os="+SysUtils.getOSName());
		
		Arrays.sort(swtJars);
		for (File f : swtJars) {
			logger.debug("swt: "+f.getName());
		}
		
		File swtFile = swtJars[swtJars.length-1];
		return swtFile;		
	}
	
	public static String getArchFilename(String prefix) {
		return prefix + SysUtils.getOSName() + SysUtils.getArchName() + ".jar";
	}
	
	public static File[] listLibs() {
		File libsFolder = new File(LIBS_FOLDER);
		return libsFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".jar");
			}
		});
	}
	
	public static String computeMD5(File f) throws IOException {
		SebisStopWatch sw = new SebisStopWatch();
		logger.info("computing md5 sum of file: "+f.getName());
		
		sw.start();
		FileInputStream fis = new FileInputStream(f);
		String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
		fis.close();
		sw.stop(true);
		
		logger.info("MD5: "+md5);
		
		return md5;
	}
	
	public static Map<File, String> getMD5sOfLibs() throws IOException {
		Map<File, String> m = new HashMap<>();
		for (File l : listLibs()) {
			m.put(l, computeMD5(l));
		}
		
		return m;
	}
	
	public static void main(String [] args) {
		File swtJarFile = getLatestSWTJarFile();
		logger.debug("SWT version is: "+stripSwtVersion(swtJarFile.getName()));
		
		// Java >= 9 does not allow this anymore #198
//		logger.info("swt jar: "+swtJarFile.getName());
//		Utils.addJarToClasspath(swtJarFile);
		
		TrpMainWidget.show();
	}

}
