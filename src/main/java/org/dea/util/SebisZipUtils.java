package org.dea.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SebisZipUtils {
	private final static Logger logger = LoggerFactory.getLogger(SebisZipUtils.class);
	
	public static class ZipEvent {
		public ZipInputStream zis; 
		public ZipEntry entry; 
		public File file;
		public boolean doit=true;
		
		public ZipEvent(ZipInputStream zis, ZipEntry entry, File file) {
			this.zis = zis;
			this.entry = entry;
			this.file = file;
		}
	}
	
	public interface UnzipListener {
		void beforeUnzip(ZipEvent event);
		void afterUnzip(ZipEvent event);
	}
	
	public static void unzip(String zipFile, String outputFolder, String baseFolder, boolean stripBaseFolder) throws IOException {
		unzip(zipFile, outputFolder, baseFolder, stripBaseFolder);
	}

	public static void unzip(String zipFile, String outputFolder, String baseFolder, boolean stripBaseFolder, UnzipListener listener) throws IOException {
		byte[] buffer = new byte[1024];
		
		if (baseFolder==null || baseFolder.equals(".") || baseFolder.equals("./")) {
			baseFolder = "";
		} else if (baseFolder.startsWith("./")) {
			baseFolder = baseFolder.substring(2);
		}
		if (!baseFolder.endsWith("/")) {
			baseFolder = baseFolder+"/";
		}

		File folder = new File(outputFolder);
		if (!folder.exists()) {
			folder.mkdir();
		}

		// get the zip file content
		ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
		
		// get the zipped file list entry
		ZipEntry ze = zis.getNextEntry();

		while (ze != null) {
			String fileName = ze.getName();
			
			boolean contains=fileName.startsWith(baseFolder);
			logger.debug("zip entry: " + fileName+" contains: "+contains+" baseFolder: "+baseFolder);
			
			if (true)
			if (contains) {
				if (stripBaseFolder)
					fileName = fileName.replaceFirst(baseFolder, "");
				
				if (!fileName.isEmpty()) {
//					logger.debug("extracting to: "+fileName);
					File newFile = new File(outputFolder + File.separator + fileName);
					
					ZipEvent event = new ZipEvent(zis, ze, newFile);
					if (listener!=null)
						listener.beforeUnzip(event);
					boolean parentDirDoesNotExit = newFile.getParent()!=null && !(new File(newFile.getParent()).exists());
					
					if (event.doit && !parentDirDoesNotExit) {
						if (ze.isDirectory()) {
							newFile.mkdirs();
						} else {
							FileOutputStream fos = new FileOutputStream(newFile);
							int len;
							while ((len = zis.read(buffer)) > 0) {
								fos.write(buffer, 0, len);
							}
							fos.close();
						}
						if (listener!=null)
							listener.afterUnzip(event);
					}
				}
									
			}

			ze = zis.getNextEntry();
		}

		zis.closeEntry();
		zis.close();
	}
}
