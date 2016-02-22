package eu.transkribus.swt_gui.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.program_updater.FTPProgramPackageFile;
import eu.transkribus.core.program_updater.ProgramPackageFile;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.FTPUtils;
import eu.transkribus.core.util.FTPUtils.FTPTransferListener;
import eu.transkribus.core.util.FTPUtils.MyFTPClient;

@Deprecated
public class FTPProgramUpdater extends ProgramUpdater<FTPProgramPackageFile> {
	private final static Logger logger = LoggerFactory.getLogger(FTPProgramUpdater.class);
	
//	public final static String ftpServer = "dea-gulliver.uibk.ac.at";
	public final static String ftpServer = "dbis-faxe.uibk.ac.at";
	public final static int ftpPort = 21;
	public final static String ftpUser = "trp_user";
	public final static String ftpPw = "trp$user";
	final static String releasesPath = "releases/";
	final static String snapshotsPath = "snapshots/";	
	
	@Override
	public List<FTPProgramPackageFile> getAllSnapshots() throws Exception {
		return getAllVersionsInPathSortedByVersion(snapshotsPath);
	}
	
	@Override
	public List<FTPProgramPackageFile> getAllReleases() throws Exception {
		return getAllVersionsInPathSortedByVersion(releasesPath);
	}
	
	public Pair<String, Date> getLatestVersionAndTimeStamp(boolean withSnapshots) throws SocketException, IOException, InterruptedException, ParseException {
		Pair<String, Date> vt = getVersionAndTimestamp(releasesPath);
		if (withSnapshots) {
			Pair<String, Date> vtS = getVersionAndTimestamp(snapshotsPath);
			if (vtS.getRight().compareTo(vt.getRight()) > 0)
				return vtS;
		}
		return vt;
	}
	
	private List<FTPProgramPackageFile> getAllVersionsInPathSortedByVersion(String path) throws SocketException, IOException {
		List<FTPProgramPackageFile> files = new ArrayList<>();
		
		try (MyFTPClient ftp = new MyFTPClient(ftpServer, ftpPort, ftpUser, ftpPw)) {			
			for (FTPFile f : ftp.listFiles(path)) {
				if (!f.getName().equals(ProgramPackageFile.BUILD_FN))
					files.add(new FTPProgramPackageFile(f, path));
			}
		}
		for (FTPProgramPackageFile f : files) {
			System.out.println(f.toString());
		}
		
		
		Collections.sort(files, programFileComp);
		return files;
	}
	
	private Pair<String, Date> getVersionAndTimestamp(String path) throws SocketException, IOException, InterruptedException, ParseException {
		try (MyFTPClient ftp = new MyFTPClient(ftpServer, ftpPort, ftpUser, ftpPw)) {		
			for (FTPFile f : ftp.listFiles(path)) {
				if (f.getName().equals(ProgramPackageFile.BUILD_FN)) {
					File tsF = new File("./updateTimestamp.txt");
					ftp.downloadFile(path, f, tsF, FTP.ASCII_FILE_TYPE);
					Properties p = new Properties();
					FileInputStream fis = new FileInputStream(tsF);
					p.load(fis);
					fis.close();
					tsF.delete();

					logger.debug("remote timestamp string: "+p.getProperty("Date"));
					return Pair.of(p.getProperty("Version"), CoreUtils.DATE_FORMAT.parse(p.getProperty("Date")));
				}
			}
		}
		
		return null;
	}
	
//	@Override
//	public Pair<FTPProgramPackageFile, Date> checkForUpdates(String currentVersion, Date localTimestamp, boolean withSnapshots) throws Exception {		
//		List<FTPProgramPackageFile> versions = getAllVersions(withSnapshots);
//		Pair<String, Date> vt = getLatestVersionAndTimeStamp(withSnapshots);
//		
//		if (versions.isEmpty())
//			return null;
//		
//		FTPProgramPackageFile lastFile = versions.get(versions.size()-1);
//		String lastVersion = vt.getLeft();
//		Date remoteTimestamp = vt.getRight();
//		
//		// check if this is a snapshot version and the latest (release) version on the server equals the snapshot version:
//		if (currentVersion.endsWith(ProgramPackageFile.SNAPSHOT_SUFFIX)) {
//			if (currentVersion.replaceFirst(ProgramPackageFile.SNAPSHOT_SUFFIX, "").equals(lastVersion)) {
//				return Pair.of(lastFile, remoteTimestamp);
//			}
//		}
//
//		if (noc.compare(currentVersion, lastVersion) < 0) { // version smaller -> get newest
//			return Pair.of(lastFile, remoteTimestamp);
//		} else if (noc.compare(currentVersion, lastVersion) == 0) { // version equal -> check timestamp
//			if (vt.getRight() != null && localTimestamp.compareTo(remoteTimestamp)<0) {
//				return Pair.of(lastFile, remoteTimestamp);
//			} else
//				return null;
//		}
//		else {
//			return null;
//		}
//	}

	@Override
	public void downloadUpdate(final File downloadFile, FTPProgramPackageFile f, final IProgressMonitor monitor, boolean downloadAll) throws Exception {
		monitor.beginTask("Downloading "+f.f.getName(), 100);
		// TODO: use FTPUtils.downloadFile method...
		FTPTransferListener transferL = new FTPTransferListener(f.f) {
			@Override public void downloaded(int percent) {
				monitor.worked(percent);
				monitor.subTask(percent+"%");											
				if (monitor.isCanceled()) {
					boolean success = abort();
				}
			}
		};
		
		try {
			FTPUtils.downloadFile(ftpServer, ftpPort, ftpUser, ftpPw, 
					f.path, FTP.BINARY_FILE_TYPE, f.f, downloadFile, transferL);
		} catch (IOException e) {
			throw new InvocationTargetException(e, e.getMessage());
		}
		
		monitor.done();
	}
	
	public static void main(String[] args) throws Exception {
		
		FTPProgramUpdater u = new FTPProgramUpdater();
		for (FTPProgramPackageFile f : u.getAllReleases()) {
			System.out.println(f.toString());
		}
		
//		List<FTPProgramPackageFile> files = new ArrayList<>();
//		Collections.sort(files, programFileComp);
		System.out.println("DONE");
		
	}
			
}
