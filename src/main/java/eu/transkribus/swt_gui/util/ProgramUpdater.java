package eu.transkribus.swt_gui.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.runtime.IProgressMonitor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.program_updater.ProgramPackageFile;
import eu.transkribus.core.util.NaturalOrderComparator;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public abstract class ProgramUpdater<T extends ProgramPackageFile> {
	private final static Logger logger = LoggerFactory.getLogger(ProgramUpdater.class);
	
	final static Comparator<ProgramPackageFile> programFileComp = new Comparator<ProgramPackageFile>() {
		@Override public int compare(ProgramPackageFile o1, ProgramPackageFile o2) {
//			return new NaturalOrderComparator().compare(o1.f.getName(), o2.f.getName());
			return new NaturalOrderComparator().compare(o1.getVersion(), o2.getVersion());
		}
	};
	
	static NaturalOrderComparator noc = new NaturalOrderComparator();
		
	public abstract List<T> getAllSnapshots() throws Exception;
	public abstract List<T> getAllReleases() throws Exception;
//	public abstract Pair<T, Date> checkForUpdates(String currentVersion, Date localTimestamp, boolean withSnapshots) throws Exception;
	public abstract void downloadUpdate(File downloadFile, T f, IProgressMonitor monitor, boolean downloadAll) throws Exception;
	
	public static Map<String, String> getLibs(boolean doLocalTest) throws FileNotFoundException, IOException {
		Map<String, String> libsMap = new HashMap<String, String>();
		File libDirF = new File(System.getProperty("user.home")+"/workspace/TrpGui/target/libs");
		
		if (!doLocalTest && !TrpMainWidget.getTrpSettings().getLibDir().equals("${dependency-dir}")) {
			String libDir = TrpMainWidget.getTrpSettings().getLibDir();
			libDirF = new File("./"+libDir);
		}
		
		FilenameFilter filter = new FilenameFilter() {
			@Override public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		};
		
		File[] libs = libDirF.listFiles(filter);		
//		for (File l : libs) {
		for (int i=0; i<libs.length; ++i) {
			File l = libs[i];
			
			try (FileInputStream fis = new FileInputStream(l)) {
				try {
					String md5Hex = DigestUtils.md5Hex(fis);
					libsMap.put(l.getName(), md5Hex);
					logger.debug("file: "+l.getName()+", md5: "+md5Hex);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return libsMap;
	}
		
	public Pair<String, Date> getLatestVersionAndTimeStamp(boolean withSnapshots) throws Exception {
		List<T> versions = getAllVersions(withSnapshots);
		if (!versions.isEmpty()) {
			T lastV = versions.get(versions.size()-1);
			
			Date ts = ProgramPackageFile.DATE_FORMAT.parse(lastV.getTimestamp());
			
			return Pair.of(lastV.getVersion(), ts);
		} else
			return Pair.of("NA", new Date(0));
	}
		
	public Pair<T, Date> checkForUpdates(String currentVersion, Date localTimestamp, boolean withSnapshots) throws Exception {		
		List<T> versions = getAllVersions(withSnapshots);
		Pair<String, Date> vt = getLatestVersionAndTimeStamp(withSnapshots);
		
		if (versions.isEmpty())
			return null;
		
		T lastFile = versions.get(versions.size()-1);
		String lastVersion = vt.getLeft();
		Date remoteTimestamp = vt.getRight();
		
		// check if this is a snapshot version and the latest (release) version on the server equals the snapshot version:
		if (currentVersion.endsWith(ProgramPackageFile.SNAPSHOT_SUFFIX)) {
			if (currentVersion.replaceFirst(ProgramPackageFile.SNAPSHOT_SUFFIX, "").equals(lastVersion)) {
				return Pair.of(lastFile, remoteTimestamp);
			}
		}

		if (noc.compare(currentVersion, lastVersion) < 0) { // version smaller -> get newest
			return Pair.of(lastFile, remoteTimestamp);
		} else if (noc.compare(currentVersion, lastVersion) == 0) { // version equal -> check timestamp
			if (vt.getRight() != null && localTimestamp.compareTo(remoteTimestamp)<0) {
				return Pair.of(lastFile, remoteTimestamp);
			} else
				return null;
		}
		else {
			return null;
		}
	}
	
	public T getLatestRelease() throws Exception {
		List<T> releases = getAllReleases();
		if (!releases.isEmpty())
			return releases.get(releases.size()-1);
		else
			throw new IOException("No release version found!");
	}

	public List<T> getAllVersions(boolean withSnapshots) throws Exception {
		List<T> files = getAllReleases();
		if (withSnapshots) {
			files.addAll(getAllSnapshots());
		}
		
		Collections.sort(files, programFileComp);
		return files;
	}
	
	// MAX HEADER BUG WORKAROUND:
	
	public static int getLibsSize(Map<String, String> libs) {
		String libsStr = new JSONObject(libs).toString();
		return libsStr.length();
	}
	
	public static boolean isExceedingSize(Map<String, String> libs) {
		return getLibsSize(libs) > 7700;
	}
	
	public static Map<String, String> reduceLibsSize(Map<String, String> libs) {
//		String libsStr = new JSONObject(libs).toString();
//		if (!isExceedingSize(libs))
//			return libs;

		for (Iterator<Map.Entry<String, String>> it = libs.entrySet().iterator(); it.hasNext();) {
			it.next();
			if (!isExceedingSize(libs))
				return libs;

			it.remove();
		}
		
		return libs;
		
//		System.out.println("str size = "+libsStr.length());
		
//		return null;
	}
	
	public static void main(String[] args) throws Exception {
		Map<String, String> libs = getLibs(true);
		System.out.println("got libs = "+libs.size()+ ", str sizie = "+getLibsSize(libs));
		reduceLibsSize(libs);
		System.out.println("reduced libs = "+libs.size()+ ", str sizie = "+getLibsSize(libs));
	}

}