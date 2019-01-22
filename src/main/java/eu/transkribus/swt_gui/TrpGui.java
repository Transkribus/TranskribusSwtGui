package eu.transkribus.swt_gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.SebisStopWatch;
import eu.transkribus.core.util.SysUtils;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.util.Utils;

public class TrpGui {
	private final static Logger logger = LoggerFactory.getLogger(TrpGui.class);
	
//	static final String LIBS_FOLDER = TrpConfig.getTrpSettings().getLibDir();
	static final String LIBS_FOLDER = new File("libs").exists() ? "libs" : "target/libs";
	
	public static final int EXIT_CODE_WORKDIR_NOT_WRITABLE = 100;
	public static final int EXIT_CODE_COULD_NOT_COPY_SWT = 101;
	public static final int EXIT_CODE_SWT_CONFIGURED = 200;
	
	private static String stripSwtVersion(String swtJarFileName) {
		return swtJarFileName.split("-")[1];
	}
	
	/**
	 * @deprecated the app has to be restarted for java to find the swt lib in this way... 
	 */
	public static boolean copyLatestSWTJarFileToClasspathFromManifest(boolean force) throws IOException {
		File latestSwt = getLatestSWTJarFile();
		String latestSwtPath = latestSwt.getAbsolutePath();
		logger.info("latestSwt: "+latestSwtPath);
		String swtLibNameFromManifest = latestSwtPath.substring(0, latestSwtPath.lastIndexOf("-")) + ".jar";
		File swtLibFromManifest = new File(swtLibNameFromManifest);
		logger.info("swtLibNameFromManifest = "+swtLibNameFromManifest+", exists: "+swtLibFromManifest.exists());
		
		if (force || !swtLibFromManifest.exists()) {
			try {
				FileUtils.copyFile(latestSwt, swtLibFromManifest);
				return true;
			} catch (IOException e) {
				logger.error("Could not copy swt file to classpath destination - do you have write access in the Transkribus folder?");
				throw new IOException(e.getMessage(), e);
			}
		}
		
		return false;
	}
	
	public static File getLatestSWTJarFile() {
		File libsFolder = new File(LIBS_FOLDER);
//		logger.debug(""+libsFolder.exists());
//		if (!libsFolder.exists()) {
//			logger.warn("libs folder does not exist - now assuming you are starting this class directly from Eclipse...");
//			libsFolder = new File("target/"+LIBS_FOLDER);
//		}
		
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
	
	public static void checkWorkingDirectoryWriteable() {
		String workDir = System.getProperty("user.dir");
		logger.info("Working directory: "+workDir);
		File f = new File(workDir);
		if (!f.canWrite()) {
			String msg = "Your installation directory is not writable!\n"
					+ "Please install into another directory or check if the program has admin rights!";
			JOptionPane.showMessageDialog(null, msg, "Error starting Transkribus", JOptionPane.ERROR_MESSAGE);
			System.exit(EXIT_CODE_WORKDIR_NOT_WRITABLE);
		}
	}
	
	public static void configureSwtLib() {
		File swtJarFile = getLatestSWTJarFile();
		logger.info("Latest SWT version is: "+stripSwtVersion(swtJarFile.getName()));
		logger.info("swt jar: "+swtJarFile.getName());
		
		if (SysUtils.getJavaVersion().startsWith("1.")) { // java version <= 8 --> jump out
			logger.info("Java version <= 8 --> including swt lib to classpath via reflection!");
			Utils.addJarToClasspath(swtJarFile);
			return;
		}
		else {
			try {
				if (copyLatestSWTJarFileToClasspathFromManifest(false)) {
					String msg = "Performed initial SWT re-configuration for java version > 8\n";
					msg += "Now trying to restart the program - restart manually if nothing happens!";
					logger.info(msg);
					JOptionPane.showMessageDialog(null, msg, "New SWT version configured", JOptionPane.INFORMATION_MESSAGE);
					
					// try to restart automatically here!
//					System.exit(EXIT_CODE_SWT_CONFIGURED);
					try {
						Utils.restartApplication(EXIT_CODE_SWT_CONFIGURED);
					} catch (URISyntaxException e) {
						logger.error("Error restarting: "+e.getMessage(), e);
					}
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), "Error starting Transkribus", JOptionPane.ERROR_MESSAGE);
				System.exit(EXIT_CODE_COULD_NOT_COPY_SWT);
			}
		}
	}
	
	public static void main(String [] args) throws Exception {
		checkWorkingDirectoryWriteable();
		configureSwtLib();
		
		TrpMainWidget.show();
	}

}
