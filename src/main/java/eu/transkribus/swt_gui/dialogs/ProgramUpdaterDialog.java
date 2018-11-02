package eu.transkribus.swt_gui.dialogs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.exec.OS;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.mihalis.opal.notify.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.program_updater.ProgramPackageFile;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.progress.ProgressBarDialog;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.ProgramInfo;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.util.HttpProgramUpdater;
import eu.transkribus.swt_gui.util.ProgramUpdater;
import eu.transkribus.util.SebisZipUtils;
import eu.transkribus.util.SebisZipUtils.UnzipListener;
import eu.transkribus.util.SebisZipUtils.ZipEvent;

public class ProgramUpdaterDialog {
	private final static Logger logger = LoggerFactory.getLogger(ProgramUpdaterDialog.class);
	
	final static String UPDATE_ZIP_FN = "./update.zip";
	final static boolean ASK_FOR_SNAPSHOTS_ON_UPDATE = false;
	/** A list of files that are not overwritten during the update process */
	final static List<String> CONFIG_FILES = new ArrayList<>();
	
	public static final boolean TEST_ONLY_DOWNLOAD = false; // set to true to test only the download process!
	
	static {
		CONFIG_FILES.add("config.properties");
		CONFIG_FILES.add("virtualKeyboards.xml");
	}
	
//	public static ProgramUpdater PROGRAM_UPDATER = new FTPProgramUpdater();
	public static ProgramUpdater PROGRAM_UPDATER;
	static {
//		if (TrpConfig.getTrpSettings().isUseFtpProgramUpdater()) {
//			PROGRAM_UPDATER = new FTPProgramUpdater();
//			logger.info("using FTP program updater");
//		} else {
			PROGRAM_UPDATER = new HttpProgramUpdater();
			logger.info("using HTTP program updater");
//		}
	}
	
	private static String getZipBaseDirName() throws IOException {
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(UPDATE_ZIP_FN))) {			
			ZipEntry ze = zis.getNextEntry();
			String basedirname = null;
			while (ze!=null) {
				if (ze.isDirectory()) {
					basedirname = ze.getName();
					break;
				}
				
			}
			if (basedirname == null)
				throw new IOException("Could not find base directory!");
			
			return basedirname;
		} catch (IOException e) {
			throw e;
		}
	}
	
//	@Deprecated
//	public static File getCurrentJar() throws URISyntaxException {		
//		URI jarUri = TrpGui.class.getProtectionDomain().getCodeSource().getLocation().toURI();
//		System.out.println("jar uri = "+jarUri);
//		System.out.println("authority = "+jarUri.getAuthority());
//		
//		File f = new File(jarUri);
//		
//		return f;
//	}
	
	public static File getCurrentJar() throws IOException {
		File f = new File(new ProgramInfo().getJarName());
		if (!f.exists())
			throw new IOException("Could not find current jar file: "+f.getName());
		
		return f;
	}
	
	public static void removeUnusedJarFiles() throws Exception {
		File jarFiles[] = new File(".").listFiles(new FilenameFilter() {
			@Override public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		});
		if (jarFiles.length<=1) {
			logger.debug("no jar files to remove, N = "+jarFiles.length);
			return;
		}
				
		String currentJarName = getCurrentJar().getName();
		
		logger.info("current jar: "+currentJarName);
		if (!currentJarName.endsWith(".jar")) { // means, that the program is not started via jar -> do nothing!
			logger.info("program not started from jar - skipping deletion of unused jar files!");
			return;
		}
				
		for (File f : jarFiles) {
			if (!f.getName().equals(currentJarName)) {
				logger.info("removing old jar file: "+f.getName());
				 f.delete();
			}
		}
	}
		
	public static String getStartScriptName() throws URISyntaxException {
		String base="Transkribus.";
		if (OS.isFamilyWindows()) {
			String exe = base+"exe";
			String cmd = "cmd /c start "+exe;
//			cmd += "& del "+getCurrentJar().getName(); // this cleans the old version in windows after the new version has started --> should work, as current JVM should exit sooner than new program has started! 
			return cmd;
		} else if (OS.isFamilyMac()) {
			return "./"+base+"command";
		} else {
			return "./"+base+"sh";
		}
	}

	private static void installZipFile(Shell parent, final String zipFile, final String installFolder, final boolean keepConfigFiles) throws InterruptedException, Throwable {
		final ProgressBarDialog pbd = new ProgressBarDialog(parent);
		pbd.open(new IRunnableWithProgress() {
			@Override public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					monitor.beginTask("Unzipping", IProgressMonitor.UNKNOWN);

					String basedirname = getZipBaseDirName();
					SebisZipUtils.unzip(zipFile, installFolder, basedirname, true, new UnzipListener() {
						@Override public void beforeUnzip(ZipEvent event) {
							String fn = event.file.getName();
							if ( keepConfigFiles && CONFIG_FILES.contains(fn) && event.file.exists() ) {
								event.doit = false;
							}
						}				
						
						@Override public void afterUnzip(ZipEvent e) {
							monitor.subTask("Extracted "+e.entry.getName());
							if (e.file.getName().endsWith(".command") || e.file.getName().endsWith(".sh")|| e.file.getName().endsWith(".bat") ) {
								logger.debug("making script file executable: "+e.file.getName());
								e.file.setExecutable(true);
							}
						}
					});
										
					if (pbd.isCanceled())
						throw new InterruptedException("Unzip cancelled!");
					
				} catch (IOException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, "", true);
	}

	private static boolean downloadUpdate(Shell parent, final ProgramPackageFile f, final boolean downloadAll) throws InterruptedException, Throwable {
		final File downloadFile = new File(UPDATE_ZIP_FN);
		logger.info("downloading update to: "+downloadFile.getAbsolutePath());
		final ProgressBarDialog pbd = new ProgressBarDialog(parent);
			pbd.open(new IRunnableWithProgress() {
				@Override public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						PROGRAM_UPDATER.downloadUpdate(downloadFile, f, monitor, downloadAll);
					} catch (Exception e) {
						throw new InvocationTargetException(e);
					}
				}
			}, "", true);
			logger.debug("was canceled: "+pbd.isCanceled());
			return pbd.isCanceled();
	}
	
	public static void restartApplication() throws URISyntaxException, IOException {
//		  final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
//		  final File currentJar = new File(TrpGui.class.getProtectionDomain().getCodeSource().getLocation().toURI());

		  /* is it a jar file? */
//		  if(!currentJar.getName().endsWith(".jar"))
//		    return;

		  /* Build command: java -jar application.jar */
		  final ArrayList<String> command = new ArrayList<String>();
		  
		  logger.debug("restarting: "+getStartScriptName());
		  command.addAll(Arrays.asList(getStartScriptName().split(" ")));
		  
//		  command.add(javaBin);
//		  command.add("-jar");
//		  command.add(currentJar.getPath());

		  final ProcessBuilder builder = new ProcessBuilder(command);
		  builder.start();
		  
		  System.exit(0);
	}
	
	
		public static void showTrayNotificationOnAvailableUpdateAsync(final Shell shell, final String version, 
				final Date timestamp, boolean forceShow) {
			logger.info("Checking for updates, current version: " + version + ", timestamp: " + timestamp);

			final Point p = TrpMainWidget.getInstance() == null ? shell.getLocation() : TrpMainWidget.getInstance().getLocationOnTitleBarAfterMenuButton();	
	
			// get new version:
			new Thread(new Runnable() {
				@Override public void run() {
					try {
						String newV = null;
						
						final boolean TEST = false;
						Pair<ProgramPackageFile, Date> f = PROGRAM_UPDATER.checkForUpdates(version, timestamp, false);
						if (f != null) {
							String fn = TEST ? "TrpGui-0.4.8-package.zip" : f.getLeft().getName();
							newV = ProgramPackageFile.stripVersion(fn);
						}
						logger.info("newest version on server: " + newV);
	
						if (forceShow || TEST || newV != null) {
							final String newV2 = newV;
							Display.getDefault().asyncExec(new Runnable() {
								@Override public void run() {
									String title = "Update available";
									String msg = "A new version of the tool (" + newV2 + ") is available!";
									if (false) {
										Notifier.notify(title, msg); // ugly... and
																		// not
																		// closeable
									} else {
										ToolTip tip = DialogUtil.createBallonToolTip(shell, SWT.ICON_INFORMATION, msg, title, p.x, p.y);
										tip.addSelectionListener(new SelectionAdapter() {
											@Override public void widgetSelected(SelectionEvent e) {
												checkForUpdatesDialog(shell, version, timestamp, false, false);
											}
										});
	
										tip.setAutoHide(true);
										tip.setVisible(true);
									}
								}
	
							});
	
						}
					} catch (final Throwable e) {
						Display.getDefault().asyncExec(new Runnable() {
							@Override public void run() {
								if (false) {
									ToolTip tip = DialogUtil.createBallonToolTip(shell, SWT.ICON_INFORMATION, "Check your internet connection",
											"Could not connect to update server", p.x, p.y);
									tip.setAutoHide(true);
									tip.setVisible(true);
								}
								logger.error("Could not connect to update server: "+e.getMessage());
							}
						});
					}
				}
			}).start();
		}

		public static void showTrayNotificationOnAvailableUpdate(final Shell shell, final String version, final Date timestamp) {
			logger.info("Checking for updates, current version: "+version+", timestamp: "+timestamp);
			
			Rectangle b = shell.getBounds();
			try {
				String newV=null;
				boolean test=false;
				
				// get new version from ftp:
				Pair<ProgramPackageFile, Date> f = PROGRAM_UPDATER.checkForUpdates(version, timestamp, false);
				if (f != null) {
					String fn = test ? "TrpGui-0.4.8-package.zip" : f.getLeft().getName();
					newV = ProgramPackageFile.stripVersion(fn);
				}
				logger.info("newest version on server: "+newV);
				
				if (test || newV!=null) {
					String title = "Update available";
					String msg = "A new version of the tool (" + newV
							+ ") is available!";
					
					if (false) {
					Notifier.notify(title, msg); // ugly... and not closeable
					} else {
					ToolTip tip = DialogUtil.createBallonToolTip(shell, SWT.ICON_INFORMATION, msg, title, b.x + b.width, b.y);
					tip.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							checkForUpdatesDialog(shell, version, timestamp, false, false);
						}
					});
					
					tip.setAutoHide(true);
					tip.setVisible(true);
					}
				}
			} catch (Throwable e) {
				ToolTip tip = DialogUtil.createBallonToolTip(shell, SWT.ICON_INFORMATION, "Check your internet connection", 
						"Could not connect to update server", b.x + b.width, b.y);
				tip.setAutoHide(true);
				tip.setVisible(true);
				logger.error(e.getMessage());
			}
		}
		
		public static void checkForUpdatesDialog(Shell parent, String version, Date timestamp, boolean withSnapshots, boolean downloadAll) {
			final Pair<ProgramPackageFile, Date> f;
			logger.debug("check for update dialog!");
			
//			final File downloadFile = new File("./update.zip");
			try {
				f = PROGRAM_UPDATER.checkForUpdates(version, timestamp, withSnapshots);
				if (f==null) {
					if (withSnapshots || !ASK_FOR_SNAPSHOTS_ON_UPDATE )
						DialogUtil.showInfoMessageBox(parent, "Version up to date", "Your version of the program is up to date!");
					else {
						int res = 
							DialogUtil.showMessageDialog(parent, "Version up to date", "Your version of the program is up to date - do you want to check the snapshot releases?\nNote: snapshot releases are experimental and shall be used with care.", null, MessageDialog.INFORMATION, new String[] {"Yes", "No"}, 0 );
						logger.debug("res = "+res);
						if (res == 0)
							checkForUpdatesDialog(parent, version, timestamp, true, downloadAll);
						return;
					}
				}
								
				if (f!=null) {
					String newV = f.getLeft().getVersion();
					
					String versionPlusTimestamp = new String(newV);
					logger.info("newV = "+newV+" version = "+version);
					if (f.getRight()!=null) {
						String date = CoreUtils.DATE_FORMAT.format(f.getRight());
						versionPlusTimestamp+=" ("+date+")";
					}
					
					String msg = "New version: "+versionPlusTimestamp;
					msg += " - download and install?";
					
					ProgramUpdateDialog pud = new ProgramUpdateDialog(parent, 0, msg);
					int res = pud.open();
//						logger.debug("result = "+res+" download all: "+pud.isDownloadAll()+ " replace config files: "+pud.isReplaceConfigFiles());
					if (res > 0) {
						boolean keepConfigFiles = !pud.isReplaceConfigFiles();
						downloadAll = pud.isDownloadAll();
						
						boolean isNewVersion = !newV.equals(version);
						logger.debug("now downloading and installing new version: isNewVersion = "+isNewVersion+" keepConfigFiles = "+keepConfigFiles+" downloadAll = "+downloadAll);
						downloadAndInstall(parent, f.getLeft(), isNewVersion, keepConfigFiles, downloadAll);
					}
				}
			}
			catch (InterruptedException ie) {
				logger.debug("Interrupted: "+ie.getMessage());
			}
			catch (IOException e) {
				if (!e.getMessage().equals("stream is closed")) {
					TrpMainWidget.getInstance().onError("IO-Error during update", "Error during update: \n\n"+e.getMessage(), e);	
				}	
			}
			catch (Throwable e) {
				TrpMainWidget.getInstance().onError("Error during update", "Error during update: \n\n"+e.getMessage(), e);
			} finally {
				removeUpdateZip();
			}
		}
		
		public static void downloadAndInstall(Shell shell, ProgramPackageFile f, boolean isNewVersion, boolean keepConfigFiles, boolean downloadAll) throws InterruptedException, Throwable {
			logger.debug("downloadAndInstall, keepConfigFiles = "+keepConfigFiles+" isNewVersion = "+isNewVersion);
//			if (true)
//				return;
			
			if (!isNewVersion) {
				DialogUtil.showErrorMessageBox(shell, "Error updating", "Cannot update to the same version - please choose another version!");
				return;
			}
			
			boolean canceled = downloadUpdate(shell, f, downloadAll);
			logger.debug("downloaded update file, canceled = "+canceled);
				
			if (canceled || TEST_ONLY_DOWNLOAD)
				return;
								
			installZipFile(shell, UPDATE_ZIP_FN, ".", keepConfigFiles);
			
			if (TrpMainWidget.getInstance()!=null && TrpMainWidget.getTrpSettings()!=null) {
				logger.debug("setting showChangeLog=true for next program startup!");
				TrpMainWidget.getTrpSettings().setShowChangeLog(true);	
			}
			
			removeUpdateZip();
			
//			if (isNewVersion) { // if it is a new version (and not only a newer version with the same version string!), then remove old jar file!
//				removeCurrentJarFile();
//			}
			
			if (DialogUtil.showYesNoDialog(shell, "Restart?", 
					"Program restart required for update to take effect") == SWT.YES) {
				restartApplication();
			}
		}
		
		public static void removeUpdateZip() {
			new File(UPDATE_ZIP_FN).delete();
		}
		
		public static String getProgramFolder() {
			return ProgramUpdaterDialog.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		}
		
		// TEST STUFF:
		static class UpdaterDemoShell extends Shell {
			public UpdaterDemoShell(Display display, int style) {
				super(display, style);
				createContents();
			}

			protected void createContents() {			
				setText("Updater Demo");
				setSize(218, 98);
				setLayout(new FillLayout());

				final Button openButton = new Button(this, SWT.NONE);
				openButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						checkForUpdatesDialog(UpdaterDemoShell.this, "0.1.1", new Date(), false, false);
					}
				});
				openButton.setText("Open Updater Dialog");

			}

			@Override
			protected void checkSubclass() {
			}
		}

		public static void testDemoShell() {
			try {
				Display display = Display.getDefault();
				UpdaterDemoShell shell = new UpdaterDemoShell(display, SWT.SHELL_TRIM);
				SWTUtil.centerShell(shell);
				shell.open();
				shell.layout();
				while (!shell.isDisposed()) {
					if (!display.readAndDispatch())
						display.sleep();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
		
		public static void main(String[] args) {
			System.out.println(getProgramFolder());
			testDemoShell();
			
//			String path = snapshotsPath;
//			boolean withSnapshots = false;
//			try {
//				Pair<FTPFile, Date> f = checkForUpdates("0.4.6", new Date(1413907080000l), withSnapshots);
//				if (f!=null) {
//					logger.info("update available: "+f.getLeft()+" / "+f.getRight().getTime());
//				} else {
//					logger.info("No updates available!");
//				}
//				
////				List<FTPFile> files = getAllVersions(withSnapshots);
////				for (FTPFile f : files) {
////					logger.info("file: "+f);
////				}
////				logger.info("current version and timestamp: "+getLatestVersionAndTimeStamp(withSnapshots));
//			} catch (Exception e) {
//				logger.error(e.getMessage(), e);
//			}
			
					
//			FTPFile f = checkForUpdates("0.1.1")C;
//			logger.debug("update = "+f);
			
//			if (f!=null)
//				FTPUtils.downloadFile(ftpServer, ftpPort, ftpUser, ftpPw, f, new File("./testDownload.zip"));
//			
			
//			List<String> version = new ArrayList<String>();
//			
//			String[] strs = new String[] {
//					"TrpGui-0.11.10-SNAPSHOT-package.zip",
//					"TrpGui-0.2.10-package.zip",
//					"TrpGui-0.1.11-SNA-package.zip",
//					"",
//					"asdfasdf",
//			};
//			
//			for (String str : strs) {
//				String vs = stripVersion(str);
//				if (vs!=null)
//					version.add(vs);
//			}
//			
//			Collections.sort(version, new NaturalOrderComparator());
//			for (String v : version)
//				System.out.println(v);
			
//			System.out.println(stripVersion("TrpGui-0.1.10-SNAPSHOT-package.zip"));
//			System.out.println(stripVersion("TrpGui-0.1.10-SNAPSHOTpackage.zip"));
//			System.out.println(stripVersion("TrpGui-0.1.11SNAPSHOTpackage.zip"));
//			System.out.println(stripVersion(""));
//			System.out.println(stripVersion("asdfasdf"));
			
		}

}
