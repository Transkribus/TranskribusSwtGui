package eu.transkribus.swt_gui.util;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.exceptions.NotLoggedInException;
import eu.transkribus.core.program_updater.HttpProgramPackageFile;
import eu.transkribus.core.util.ProgressInputStream.ProgressInputStreamListener;
import eu.transkribus.swt_gui.mainwidget.Storage;

public class HttpProgramUpdater extends ProgramUpdater<HttpProgramPackageFile> {
	
	private final static Logger logger = LoggerFactory.getLogger(HttpProgramUpdater.class);
	
	Storage store = Storage.getInstance();
	TrpServerConn conn;
	
	public HttpProgramUpdater() {
		try {	
			conn = new TrpServerConn(TrpServerConn.PROD_SERVER_URI);
		} catch (LoginException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public TrpServerConn getConnection() {
		if (Storage.getInstance().isLoggedIn()) {
			return Storage.getInstance().getConnection();
		} else
			return conn;
	}
	
	@Override public List<HttpProgramPackageFile> getAllSnapshots() throws Exception {
		TrpServerConn conn = getConnection();
		
		if (conn == null)
			throw new NotLoggedInException("Program updater could not login to server!");
		
		return conn.getAvailableClientFiles(false);
	}

	@Override public List<HttpProgramPackageFile> getAllReleases() throws Exception {
		TrpServerConn conn = getConnection();
		if (conn == null)
			throw new NotLoggedInException("Program updater could not login to server!");
		
		return conn.getAvailableClientFiles(true);
	}

	@Override public void downloadUpdate(File downloadFile, HttpProgramPackageFile f, final IProgressMonitor monitor, boolean downloadAll) throws Exception {
		TrpServerConn conn = getConnection();
		if (conn == null)
			throw new NotLoggedInException("Program updater could not login to server!");		
		
		monitor.beginTask("Downloading "+f.getName(), 100);
		
		logger.debug("downloading update:\n"+f.toString());
		Map<String, String> libs = ProgramUpdater.getLibs(false);
		logger.debug("found "+libs.size()+" nr of libs!");
		// FIXME: workaround for header field limit of 8kb:
//		libs = ProgramUpdater.reduceLibsSize(libs);
//		logger.debug("reduced nr of libs = "+libs.size());
		
//		conn.downloadClientFile(f.isReleaseVersion(), f.getName(), downloadFile, downloadAll ? null : libs, new ProgressInputStreamListener() {
		conn.downloadClientFileNew(f.isReleaseVersion(), f.getName(), downloadFile, downloadAll ? null : libs, new ProgressInputStreamListener() {
			@Override public void progress(long oldBytesRead, long bytesRead, long totalBytes) {
//				logger.debug("progress: oldread: "+oldBytesRead+" newread: "+bytesRead+" total: "+totalBytes);
				int percent = (int) (( (float) bytesRead / (float) totalBytes) * 100.0f);
				
				double readMB = bytesRead / (1048576.0d);
				double totalMB = totalBytes / (1048576.0d);
				
				DecimalFormat df = new DecimalFormat("#.##");
								
				monitor.worked(percent);
				monitor.subTask(percent+"% ("+df.format(readMB)+"/"+df.format(totalMB)+" MB)");											
				if (monitor.isCanceled()) {
					// TODO: cancel download??
					abort();
				}
			}
		});
	}
	
	
//	public static void main(String[] args) throws Exception {
//		HttpProgramUpdater updater = new HttpProgramUpdater();
//		for (HttpProgramPackageFile f : updater.getAllReleases()) {
//			logger.info(f.toString());
//		}
		

//	}

}
