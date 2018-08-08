package eu.transkribus.swt_gui.mainwidget;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Future;

import javax.security.auth.login.LoginException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.ServerErrorException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.dea.fimgstoreclient.beans.FimgStoreImgMd;
import org.dea.fimgstoreclient.beans.ImgType;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.BidiUtil;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.exceptions.ClientVersionNotSupportedException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.exceptions.OAuthTokenRevokedException;
import eu.transkribus.core.io.LocalDocReader;
import eu.transkribus.core.io.LocalDocReader.DocLoadConfig;
import eu.transkribus.core.io.util.ImgFileFilter;
import eu.transkribus.core.model.beans.JAXBPageTranscript;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpCrowdProjectMessage;
import eu.transkribus.core.model.beans.TrpCrowdProjectMilestone;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpDocDir;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpEvent;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.TrpUpload;
import eu.transkribus.core.model.beans.auth.TrpRole;
import eu.transkribus.core.model.beans.auth.TrpUserLogin;
import eu.transkribus.core.model.beans.customtags.CommentTag;
import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory.TagRegistryChangeEvent;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.model.beans.enums.OAuthProvider;
import eu.transkribus.core.model.beans.enums.ScriptType;
import eu.transkribus.core.model.beans.enums.TranscriptionLevel;
import eu.transkribus.core.model.beans.pagecontent.PcGtsType;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpShapeTypeUtils;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.core.model.builder.CommonExportPars;
import eu.transkribus.core.model.builder.ExportCache;
import eu.transkribus.core.model.builder.ExportUtils;
import eu.transkribus.core.model.builder.alto.AltoExportPars;
import eu.transkribus.core.model.builder.docx.DocxBuilder;
import eu.transkribus.core.model.builder.docx.DocxExportPars;
import eu.transkribus.core.model.builder.ms.TrpXlsxBuilder;
import eu.transkribus.core.model.builder.ms.TrpXlsxTableBuilder;
import eu.transkribus.core.model.builder.pdf.PdfExportPars;
import eu.transkribus.core.model.builder.rtf.TrpRtfBuilder;
import eu.transkribus.core.model.builder.tei.TeiExportPars;
import eu.transkribus.core.model.builder.txt.TrpTxtBuilder;
import eu.transkribus.core.program_updater.ProgramPackageFile;
import eu.transkribus.core.util.AuthUtils;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.IntRange;
import eu.transkribus.core.util.PageXmlUtils;
import eu.transkribus.core.util.SysUtils;
import eu.transkribus.core.util.ZipUtils;
import eu.transkribus.swt.portal.PortalWidget.Position;
import eu.transkribus.swt.progress.ProgressBarDialog;
import eu.transkribus.swt.util.CreateThumbsService;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.LoginDialog;
import eu.transkribus.swt.util.SWTLog;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.SplashWindow;
import eu.transkribus.swt.util.databinding.DataBinder;
import eu.transkribus.swt_gui.Msgs;
import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.TrpGuiPrefs;
import eu.transkribus.swt_gui.TrpGuiPrefs.OAuthCreds;
import eu.transkribus.swt_gui.canvas.CanvasContextMenuListener;
import eu.transkribus.swt_gui.canvas.CanvasMode;
import eu.transkribus.swt_gui.canvas.CanvasScene;
import eu.transkribus.swt_gui.canvas.CanvasSettings;
import eu.transkribus.swt_gui.canvas.CanvasSettingsPropertyChangeListener;
import eu.transkribus.swt_gui.canvas.CanvasShapeObserver;
import eu.transkribus.swt_gui.canvas.CanvasWidget;
import eu.transkribus.swt_gui.canvas.SWTCanvas;
import eu.transkribus.swt_gui.canvas.editing.ShapeEditOperation;
import eu.transkribus.swt_gui.canvas.listener.CanvasSceneListener;
import eu.transkribus.swt_gui.canvas.listener.ICanvasSceneListener;
import eu.transkribus.swt_gui.canvas.shapes.CanvasPolygon;
import eu.transkribus.swt_gui.canvas.shapes.CanvasPolyline;
import eu.transkribus.swt_gui.canvas.shapes.CanvasShapeUtil;
import eu.transkribus.swt_gui.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.collection_manager.CollectionEditorDialog;
import eu.transkribus.swt_gui.collection_manager.CollectionManagerDialog;
import eu.transkribus.swt_gui.collection_manager.CollectionUsersDialog;
import eu.transkribus.swt_gui.dialogs.ActivityDialog;
import eu.transkribus.swt_gui.dialogs.AffineTransformDialog;
import eu.transkribus.swt_gui.dialogs.AutoSaveDialog;
import eu.transkribus.swt_gui.dialogs.BatchImageReplaceDialog;
import eu.transkribus.swt_gui.dialogs.BugDialog;
import eu.transkribus.swt_gui.dialogs.ChangeLogDialog;
import eu.transkribus.swt_gui.dialogs.ChooseCollectionDialog;
import eu.transkribus.swt_gui.dialogs.CommonExportDialog;
import eu.transkribus.swt_gui.dialogs.DebuggerDialog;
import eu.transkribus.swt_gui.dialogs.DocSyncDialog;
import eu.transkribus.swt_gui.dialogs.InstallSpecificVersionDialog;
import eu.transkribus.swt_gui.dialogs.JavaVersionDialog;
import eu.transkribus.swt_gui.dialogs.PAGEXmlViewer;
import eu.transkribus.swt_gui.dialogs.ProgramUpdaterDialog;
import eu.transkribus.swt_gui.dialogs.ProxySettingsDialog;
import eu.transkribus.swt_gui.dialogs.SettingsDialog;
import eu.transkribus.swt_gui.dialogs.TrpLoginDialog;
import eu.transkribus.swt_gui.dialogs.VersionsDiffBrowserDialog;
import eu.transkribus.swt_gui.edit_decl_manager.EditDeclManagerDialog;
import eu.transkribus.swt_gui.edit_decl_manager.EditDeclViewerDialog;
import eu.transkribus.swt_gui.factory.TrpShapeElementFactory;
import eu.transkribus.swt_gui.mainwidget.menubar.TrpMenuBarListener;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettingsPropertyChangeListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.mainwidget.storage.StorageUtil;
import eu.transkribus.swt_gui.metadata.PageMetadataWidgetListener;
import eu.transkribus.swt_gui.metadata.TaggingWidgetUtils;
import eu.transkribus.swt_gui.metadata.TextStyleTypeWidgetListener;
import eu.transkribus.swt_gui.pagination_tables.JobsDialog;
import eu.transkribus.swt_gui.pagination_tables.TranscriptsDialog;
import eu.transkribus.swt_gui.search.SearchDialog;
import eu.transkribus.swt_gui.search.SearchDialog.SearchType;
import eu.transkribus.swt_gui.search.fulltext.FullTextSearchComposite;
import eu.transkribus.swt_gui.structure_tree.StructureTreeListener;
import eu.transkribus.swt_gui.table_editor.TableUtils;
import eu.transkribus.swt_gui.tools.ToolsWidgetListener;
import eu.transkribus.swt_gui.transcription.ATranscriptionWidget;
import eu.transkribus.swt_gui.transcription.LineEditorListener;
import eu.transkribus.swt_gui.transcription.LineTranscriptionWidget;
import eu.transkribus.swt_gui.transcription.LineTranscriptionWidgetListener;
import eu.transkribus.swt_gui.transcription.WordTranscriptionWidget;
import eu.transkribus.swt_gui.transcription.WordTranscriptionWidgetListener;
import eu.transkribus.swt_gui.upload.UploadDialog;
import eu.transkribus.swt_gui.upload.UploadDialogUltimate;
import eu.transkribus.swt_gui.util.GuiUtil;
import eu.transkribus.swt_gui.util.OAuthGuiUtil;
import eu.transkribus.swt_gui.vkeyboards.ITrpVirtualKeyboardsTabWidgetListener;
import eu.transkribus.swt_gui.vkeyboards.TrpVirtualKeyboardsDialog;
import eu.transkribus.swt_gui.vkeyboards.TrpVirtualKeyboardsTabWidget;
import eu.transkribus.util.RecentDocsPreferences;

public class TrpMainWidget {
	private final static boolean USE_SPLASH = true;
	private final static Logger logger = LoggerFactory.getLogger(TrpMainWidget.class);

	private static Shell mainShell;
	// Ui stuff:
	// Display display = Display.getDefault();
	static Display display;	
	SWTCanvas canvas;
	TrpMainWidgetView ui;
	LoginDialog loginDialog;
	// LineTranscriptionWidget transcriptionWidget;
	HashSet<String> userCache = new HashSet<String>();

//	static Preferences prefNode = Preferences.userRoot().node( "/trp/recent_docs" );
//	private RecentDocsPreferences userPrefs = new RecentDocsPreferences(5, prefNode);

	public ProgramInfo info;
	public final String VERSION;
	public final String NAME;
	
	private final double readingOrderCircleInitWidth = 90;

	// Listener:
	// CanvasGlobalEventsFilter globalEventsListener;
	TrpMainWidgetKeyListener keyListener;
	PagesPagingToolBarListener pagesPagingToolBarListener;
	RegionsPagingToolBarListener lineTrRegionsPagingToolBarListener;
	RegionsPagingToolBarListener wordTrRegionsPagingToolBarListener;
	// TranscriptsPagingToolBarListener transcriptsPagingToolBarListener;
	ICanvasSceneListener canvasSceneListener;
	LineTranscriptionWidgetListener lineTranscriptionWidgetListener;
	WordTranscriptionWidgetListener wordTranscriptionWidgetListener;
	TrpShapeElementFactory shapeFactory;
	LineEditorListener lineEditorListener;
	StructureTreeListener structTreeListener;
	TrpMainWidgetViewListener mainWidgetViewListener;
	CanvasContextMenuListener canvasContextMenuListener;
	TranscriptObserver transcriptObserver;
	CanvasShapeObserver canvasShapeObserver;
	PageMetadataWidgetListener pageMetadataWidgetListener;
	TextStyleTypeWidgetListener textStyleWidgetListener;
//	TaggingWidgetOldListener taggingWidgetListener;
	ToolsWidgetListener laWidgetListener;
//	JobTableWidgetListener jobOverviewWidgetListener;
//	TranscriptsTableWidgetListener versionsWidgetListener;
	TrpMainWidgetStorageListener mainWidgetStorageListener;
//	CollectionManagerListener collectionsManagerListener;
	TrpMenuBarListener menuListener;
	
	// Dialogs
	SearchDialog searchDiag;
	TrpVirtualKeyboardsDialog vkDiag;
	TranscriptsDialog versionsDiag;
	SettingsDialog viewSetsDiag;
	ProxySettingsDialog proxyDiag;
	AutoSaveDialog autoSaveDiag;
	DebuggerDialog debugDiag;
	VersionsDiffBrowserDialog browserDiag;
	BugDialog bugDialog;
	ChangeLogDialog changelogDialog;
	JavaVersionDialog javaVersionDialog;
	
	JobsDialog jobsDiag;
	CollectionManagerDialog cm;
	CollectionUsersDialog collUsersDiag;
	
	EditDeclManagerDialog edDiag;
	ActivityDialog ad;
	Shell sleakDiag;

	Storage storage; // the data
	boolean isPageLocked = false;

	String lastExportFolder = System.getProperty("user.home");
//	String lastExportPdfFn = System.getProperty("user.home");
//	String lastExportTeiFn = System.getProperty("user.home");
//	String lastExportDocxFn = System.getProperty("user.home");
//	String lastExportXlsxFn = System.getProperty("user.home");

	String lastLocalDocFolder = null;
	boolean sessionExpired = false;
	String lastLoginServer = "";

	static TrpMainWidget mw;

	static int tmpCount = 0;
	
	static Thread asyncSaveThread;
	static DocJobUpdater docJobUpdater;
	
	AutoSaveController autoSaveController;
//	TaggingController taggingController;

	private Runnable updateThumbsWidgetRunnable = new Runnable() {
		@Override public void run() {
			ui.getThumbnailWidget().reload();
		}
	};

	private TrpMainWidget(Composite parent) {
		// GlobalResourceManager.init();

		info = new ProgramInfo();
		VERSION = info.getVersion();
		NAME = info.getName();

		Display.setAppName(NAME);
		Display.setAppVersion(VERSION);

		// String time = info.getTimestampString();
		RecentDocsPreferences.init();

		// Display display = Display.getDefault();
		// canvas = new TrpSWTCanvas(SWTUtil.dummyShell, SWT.NONE, this);
		ui = new TrpMainWidgetView(parent, this);
		canvas = ui.getCanvas();
	
		// transcriptionWidget = ui.getLineTranscriptionWidget();
		shapeFactory = new TrpShapeElementFactory(this);

		storage = Storage.getInstance();

		addListener();
		addUiBindings();
		
		autoSaveController = new AutoSaveController(this);
//		taggingController = new TaggingController(this);
		
		updateToolBars();
		if(getTrpSets().getAutoSaveFolder().trim().isEmpty()){
			getTrpSets().setAutoSaveFolder(TrpSettings.getDefaultAutoSaveFolder());
		}
		beginAutoSaveThread();
		
		docJobUpdater = new DocJobUpdater(this);
	}

	public static TrpMainWidget getInstance() {
		return mw;
	}
	
	public String registerJobsToUpdate(Collection<String> jobIds) {
		if (jobIds == null)
			return "no jobs started";
		
		return registerJobsToUpdate(jobIds.toArray(new String[0]));
	}
	
	public String registerJobsToUpdate(String... jobIds) {
		if (jobIds == null)
			return "no jobs started";
		
		String jobIdsStr = "";
		for (String jobId : jobIds) {
			docJobUpdater.registerJobToUpdate(jobId);
			jobIdsStr += jobId+"\n";
		}
		
		return jobIdsStr;
	}

	public Storage getStorage() {
		return storage;
	}

	public ProgramInfo getInfo() {
		return info;
	}

	public static TrpSettings getTrpSettings() {
		return TrpConfig.getTrpSettings();
	}
	
	public AutoSaveController getAutoSaveController() {
		return autoSaveController;
	}
	
//	public TaggingController getTaggingController() {
//		return taggingController;
//	}


	/**
	 * This method gets called in the {@link #show()} method after the UI is
	 * inited and displayed
	 */
	public void postInit() {
		// remove unused old jar files: (maybe there from program update):
		try {
			ProgramUpdaterDialog.removeUnusedJarFiles();
		} catch (Exception e) {
			logger.error("Error removing old jar files: " + e.getMessage());
		}

		//read and set proxy settings
		storage.updateProxySettings();

		// init predifined tags:
		String tagNamesProp = TrpConfig.getTrpSettings().getTagNames();
		if (tagNamesProp != null) {
			CustomTagFactory.addLocalUserDefinedTagsToRegistry(tagNamesProp);
		}

		// check for updates:
		if (getTrpSets().isCheckForUpdates()) {
			ProgramUpdaterDialog.showTrayNotificationOnAvailableUpdateAsync(ui.getShell(), VERSION, info.getTimestamp());
		}

		boolean FORCE_AUTO_LOGIN = true;
		if (FORCE_AUTO_LOGIN && getTrpSets().isAutoLogin()) {
			String lastAccount = TrpGuiPrefs.getLastLoginAccountType();

			try {
				if (OAuthGuiUtil.TRANSKRIBUS_ACCOUNT_TYPE.equals(lastAccount)) {
					Pair<String, String> lastLogin = TrpGuiPrefs.getLastStoredCredentials();
					if (lastLogin != null) {
						// TODO: also remember server in TrpGuiPrefs, for now: logon to prod server
						login(TrpServerConn.PROD_SERVER_URI, lastLogin.getLeft(), lastLogin.getRight(), true);
					}
				} else {
					OAuthProvider prov;
					try {
						prov = OAuthProvider.valueOf(lastAccount);
					} catch (Exception e) {
						prov = null;
					}
					if (prov != null) {
						//TODO get state token from server
						final String state = "test";
						OAuthCreds creds = TrpGuiPrefs.getOAuthCreds(prov);
						loginOAuth(TrpServerConn.PROD_SERVER_URI, creds.getRefreshToken(), state, OAuthGuiUtil.REDIRECT_URI, prov);
					}
				}
			
			} catch (OAuthTokenRevokedException e) {
				logger.error("OAuth token was revoked!", e);
			} catch (Exception e) {
				logger.error("Error during login in postInit: "+e.getMessage(), e);
			}
		}

		loadTestDocSpecifiedInLocalFile();
		

		
		// TEST:
//		if (TESTTABLES) {
//			loadTestDocSpecifiedInLocalFile();
//		}		
//		jumpToPage(1);

//		SWTUtil.mask2(ui.getStructureTreeWidget()); // TESt
//		MyInfiniteProgressPanel p = MyInfiniteProgressPanel.getInfiniteProgressPanelFor(ui.getStructureTreeWidget());
//		p.start();

//		final boolean DISABLE_CHANGELOG = true;
//		openChangeLogDialog(getTrpSets().isShowChangeLog() && !DISABLE_CHANGELOG);
	}
	
	/** Tries to read the local file "loadThisDocOnStartup.txt" and load the specified document.<br/>
     * To auto load a remote document: specify the first line as: "colId docId".<br/>
	 * To auto load a local document: specify the path of the document (without spaces!) in the first line.<br/>
	 * Comment out a line using a # sign at the start.
	 */
	public boolean loadTestDocSpecifiedInLocalFile() {
		try {
			final String TEST_DOC_FN = "./loadThisDocOnStartup.txt";
			logger.debug("loading test doc from file: "+new File(TEST_DOC_FN).getAbsolutePath());
			List<String> lines = Files.readAllLines(Paths.get(TEST_DOC_FN));
			
			for (int i=0; i<lines.size(); ++i) {
				String docStr = lines.get(i);
				if (!docStr.startsWith("#")) {
					logger.debug("found docStr: "+docStr);
					String[] splits = docStr.split(" ");
					if (splits.length == 2) { // remote doc
						try {
							int colid = Integer.parseInt(splits[0]);
							int docid = Integer.parseInt(splits[1]);
							loadRemoteDoc(docid, colid);
							return true;
						} catch (NumberFormatException e) {
							// ignore parsing errors and do nothing...
						}
					} else { // local doc
						loadLocalDoc(docStr);
						return true;
					}
				}
			}
		} catch (IOException e) {
			// no file found -> ignore and to not load anything
		}
		
		return false;
	}

	public void syncTextOfDocFromWordsToLinesAndRegions() {
		try {
			if (!storage.isDocLoaded())
				return;

			final int colId = storage.getCurrentDocumentCollectionId();
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						storage.syncTextOfDocFromWordsToLinesAndRegions(colId, monitor);
					} catch (InterruptedException ie) {
						throw ie;
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Applying text from words", true);
		} catch (InterruptedException ie) {
		} catch (Throwable e) {
			onError("Could not apply text", "Could not apply text", e);
		} finally {
			// reloadCurrentDocument(true);
			reloadCurrentPage(true);
		}
	}

	public Future<List<TrpDocMetadata>> reloadDocList(int colId) {
		try {
			
			if (colId == 0)
				return null;
			
			try {
				storage.checkConnection(true);
			} catch (NoConnectionException e1) {
				// TODO Auto-generated catch block
				loginDialog("No conection to server!");
			}
			
			if (!storage.isLoggedIn())
				return null;

			canvas.getScene().selectObject(null, true, false); // security measure due to mysterious bug leading to freeze of progress dialog

			ui.getServerWidget().setSelectedCollection(storage.getCollection(colId));

			Future<List<TrpDocMetadata>> doclist;
			try {
				doclist = storage.reloadDocList(colId);
			} catch (SessionExpiredException | ServerErrorException | ClientErrorException | IllegalArgumentException e) {
				// TODO Auto-generated catch block
				if(e instanceof SessionExpiredException){
					loginDialog("Session Expired!");
					//retry
					ui.getServerWidget().setSelectedCollection(storage.getCollection(colId));
					doclist = storage.reloadDocList(colId);
				}
				throw e;
			}
			
			return doclist;
			
//			updatePageInfo();
		} catch (Throwable e) {
			onError("Cannot load document list", "Could not connect to " + ui.getTrpSets().getTrpServer(), e);
			return null;
		}
	}

//	public int getSelectedCollectionId() {
//		return ui.getDocOverviewWidget().getSelectedCollectionId();
//	}

//	public void reloadJobList() {
//		try {
//			ui.getJobOverviewWidget().refreshPage(true);
//			storage.startOrResumeJobThread();
//
////			storage.reloadJobs(!ui.getJobOverviewWidget().getShowAllJobsBtn().getSelection()); // should
//			// trigger
//			// event
//			// that
//			// updates
//			// gui!
//		} catch (Exception ex) {
//			onError("Error", "Error during update of jobs", ex);
//		}
//	}

	public void cancelJob(final String jobId) {
		try {
			storage.cancelJob(jobId);
		} catch (Exception ex) {
			onError("Error", "Error while canceling a job", ex);
		}
	}

	public void clearDocList() {
		getUi().getServerWidget().clearDocList();
	}

//	public void reloadHtrModels() {
//		try {
//			storage.reloadHtrModelsStr();
//			ui.getToolsWidget().setHtrModelList(storage.getHtrModelsStr());
//		} catch (Exception e) {
//			onError("Error", "Error during update of HTR models", e);
//		}
//	}

//	public void clearHtrModelList() {
//		storage.clearHtrModels();
//		getUi().getToolsWidget().clearHtrModelList();
//	}

	public String updateDocumentInfo() {
		String loadedDocStr = "", currentCollectionStr = "";
		int docId = -1;
		ScriptType st = null;
		String language = null;
		TrpCollection c = null;

		if (storage.getDoc() != null) {
			docId = storage.getDoc().getId();
			TrpDocMetadata md = storage.getDoc().getMd();
			st = md.getScriptType();
			language = md.getLanguage();

			if (storage.isLocalDoc()) {
				loadedDocStr = md.getLocalFolder().getAbsolutePath();
			} else {
				if (md.getTitle() != null && !md.getTitle().isEmpty())
					loadedDocStr = md.getTitle() + ", ID: " + md.getDocId();

				c = storage.getDoc().getCollection();
				if (c != null)
					currentCollectionStr = c.getColName() + ", ID: " + c.getColId();
			}
		}

//		ui.getServerWidget().setAdminAreaVisible(storage.isAdminLoggedIn());
		ui.getDocInfoWidget().getLoadedDocText().setText(loadedDocStr);
		ui.getDocInfoWidget().getCurrentCollectionText().setText(currentCollectionStr);
		ui.getServerWidget().updateHighlightedRow(docId);

//		ui.toolsWidget.updateParameter(st, language);

		return loadedDocStr;
	}

	// update title:
	public void updatePageInfo() {
		String title = ui.APP_NAME;

		String loadedDocStr = updateDocumentInfo();

		String loadedPageStr = "";
		String fn = "";
		String key = "";
		int pageNr = -1;
		int pageId=-1, tsid=-1;
		String imgUrl = "", transcriptUrl = "";
		
		int collId = storage.getCurrentDocumentCollectionId();
		int docId = -1;

		if (storage.getDoc() != null) {
			docId = storage.getDoc().getId();

			if (storage.getPage() != null) {
				fn = storage.getPage().getImgFileName() != null ? storage.getPage().getImgFileName() : "";
//				key = storage.getPage().getKey() != null ? storage.getPage().getKey() : "";

//				imgUrl = CoreUtils.urlToString(storage.getPage().getUrl());
				if (storage.getCurrentImage() != null && !storage.getPage().hasImgError()) {
					imgUrl = CoreUtils.urlToString(storage.getCurrentImage().url);
				}
				pageNr = storage.getPage().getPageNr();
				pageId = storage.getPage().getPageId();

				if (storage.getTranscriptMetadata() != null 
						&& storage.getTranscriptMetadata().getUrl() != null 
						&& !storage.getPage().hasImgError()) {
					transcriptUrl = CoreUtils.urlToString(storage.getTranscriptMetadata().getUrl());
					tsid = storage.getTranscriptMetadata().getTsId();
				}

				loadedPageStr = "Page " + pageNr + ", file: " + fn;
				if (storage.isPageLocked()) {
					loadedPageStr += " (LOCKED)";
				}
			}

			title += ", Loaded doc: " + loadedDocStr + ", " + loadedPageStr;
			if (storage.isTranscriptEdited())
				title += "*";

//			if (shellInfoText.contains("Img Meta Info")){
//				shellInfoText = shellInfoText.substring(0, shellInfoText.indexOf("Img Meta Info")).concat("Img Meta Info: ("+ storage.getImageMetadata().getXResolution() +")");
//			}

			if (storage.getDoc().isRemoteDoc()) {
				FimgStoreImgMd imgMd = storage.getCurrentImageMetadata();
				if (imgMd != null)
					title += " [Image Meta Info: (Resolution:" + imgMd.getXResolution() + ", w*h: " + imgMd.getWidth() + " * " + imgMd.getHeight() + ") ]";
			}

			TrpTextRegionType currRegion = storage.getCurrentRegionObject();
			TrpTextLineType currLine = storage.getCurrentLineObject();
			TrpWordType currWord = storage.getCurrentWordObject();
			if (currWord != null) {
				java.awt.Rectangle boundingRect = PageXmlUtils.buildPolygon(currWord.getCoords().getPoints()).getBounds();
				title += " [ current word: w*h: " + boundingRect.width + " * " + boundingRect.height + " ]";
			} else if (currLine != null) {
				java.awt.Rectangle boundingRect = PageXmlUtils.buildPolygon(currLine.getCoords().getPoints()).getBounds();
				title += " [ current line: w*h: " + boundingRect.width + " * " + boundingRect.height + " ]";
			} else if (currRegion != null) {
				java.awt.Rectangle boundingRect = PageXmlUtils.buildPolygon(currRegion.getCoords().getPoints()).getBounds();
				title += " [ current region: w*h: " + boundingRect.width + " * " + boundingRect.height + " ]";
			}

		}

		ui.getDocInfoWidget().getLoadedDocText().setText(loadedDocStr);
		ui.getDocInfoWidget().getLoadedPageText().setText(fn);
		
		ui.getDocInfoWidget().getLoadedImageUrl().setText(imgUrl);
		ui.getDocInfoWidget().getLoadedTranscriptUrl().setText(transcriptUrl);
		
		if (!storage.isDocLoaded() || storage.isLocalDoc())
			ui.getDocInfoWidget().getIdsText().setText("NA");
		else
			ui.getDocInfoWidget().getIdsText().setText(pageId+"/"+tsid);

		ui.getServerWidget().updateHighlightedRow(docId);
		ui.getShell().setText(title);
		// updateDocMetadata();
	}

	private void addListener() {
		Listener closeListener = new Listener() {
			@Override public void handleEvent(Event event) {
				logger.debug("close event!");
				if (!saveTranscriptDialogOrAutosave()) {
					event.doit = false;
					return;
				}
				docJobUpdater.stop = true;

				logger.debug("stopping CreateThumbsService");
				CreateThumbsService.stop(true);

				System.exit(0);
//				storage.finalize();
			}
		};
		ui.getShell().addListener(SWT.Close, closeListener);

		// add global filter for key listening:
		keyListener = new TrpMainWidgetKeyListener(this);
		getUi().getDisplay().addFilter(SWT.KeyDown, keyListener);

		// dispose listener
		getUi().addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(DisposeEvent e) {
				storage.logout();
				// remove key listener:
				getUi().getDisplay().removeFilter(SWT.KeyDown, keyListener);
			}
		});
		mainWidgetViewListener = new TrpMainWidgetViewListener(this);
		menuListener = new TrpMenuBarListener(this);
		
		canvasContextMenuListener = new CanvasContextMenuListener(this);

		// pages paging toolbar listener:
		pagesPagingToolBarListener = new PagesPagingToolBarListener(ui.getPagesPagingToolBar(), this);
		// transcripts paging toolbar listener:
		// transcriptsPagingToolBarListener = new
		// TranscriptsPagingToolBarListener(ui.getTranscriptsPagingToolBar(),
		// this);
		// CanvasSceneListener acts on add / remove shape and selection change:
		canvasSceneListener = new CanvasSceneListener(this);
		// add toolbar listener for transcription widgets:
		lineTrRegionsPagingToolBarListener = new RegionsPagingToolBarListener(ui.getLineTranscriptionWidget().getRegionsPagingToolBar(), this);
		wordTrRegionsPagingToolBarListener = new RegionsPagingToolBarListener(ui.getWordTranscriptionWidget().getRegionsPagingToolBar(), this);
		// act on transcription changes:
		lineTranscriptionWidgetListener = new LineTranscriptionWidgetListener(this, ui.getLineTranscriptionWidget());
		wordTranscriptionWidgetListener = new WordTranscriptionWidgetListener(this, ui.getWordTranscriptionWidget());

		// line editor listener (modify and enter pressed)
		// lineEditorListener = new LineEditorListener(this);
		// struct tree listener:
		structTreeListener = new StructureTreeListener(ui.getStructureTreeWidget().getTreeViewer(), true);
		// transcription observer:
		transcriptObserver = new TranscriptObserver(this);
		// shape observer:
		canvasShapeObserver = new CanvasShapeObserver(this);

		// listen for changes in canvas settings:
		getCanvas().getSettings().addPropertyChangeListener(new CanvasSettingsPropertyChangeListener(this));
		// listen for changes in trp settings:
		getTrpSets().addPropertyChangeListener(new TrpSettingsPropertyChangeListener(this));

		// resize listener (for debug output):
		ui.addListener(SWT.Resize, new Listener() {
			@Override public void handleEvent(Event event) {
				Rectangle rect = ui.getClientArea();
				logger.debug("shell: " + rect + ", canvas: " + getCanvas().getClientArea() + " canvasWidget: " + getCanvasWidget().getClientArea());
			}
		});

		ui.getThumbnailWidget().addListener(SWT.Selection, new Listener() {
			@Override public void handleEvent(Event event) {
				logger.debug("loading page " + event.index);
				jumpToPage(event.index);
			}
		});
		
		pageMetadataWidgetListener = new PageMetadataWidgetListener(this);
		
		if (ui.getTextStyleWidget()!=null) {
			textStyleWidgetListener = new TextStyleTypeWidgetListener(ui.getTextStyleWidget());
		}

//		taggingWidgetListener = new TaggingWidgetOldListener(this);

		laWidgetListener = new ToolsWidgetListener(this);
		
//		jobOverviewWidgetListener = new JobTableWidgetListener(this);
//		versionsWidgetListener = new TranscriptsTableWidgetListener(this);

		// storage observer:
		mainWidgetStorageListener = new TrpMainWidgetStorageListener(this);

//		ui.getServerWidget().getShowJobsBtn().addSelectionListener(new SelectionListener() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				openJobsDialog();
//			}
//			@Override public void widgetDefaultSelected(SelectionEvent e) {}
//		});
		
		CustomTagFactory.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				Display.getDefault().asyncExec(() -> {
					if (arg instanceof TagRegistryChangeEvent) {
						logger.debug("registry has changed ");
						TagRegistryChangeEvent trce = (TagRegistryChangeEvent) arg;
						if (trce.type.equals(TagRegistryChangeEvent.CHANGED_TAG_COLOR) && getUi()!=null && getUi().getSelectedTranscriptionWidget()!=null) {
							TrpMainWidget.getInstance().getUi().getSelectedTranscriptionWidget().redrawText(true);
						}
												
						//if tag registry has changed and user is logged in -> store into DB for the current user
						if (storage.isLoggedIn()){
							Storage.getInstance().updateCustomTagSpecsForUserInDB();
						}
					}
				});
			}
		});
	}
	
	/**
	 * Add a comment tag for the current selection in the transcription widget.
	 * If commentText is empty or null, the user is prompted to input a comment.
	 */
	public void addCommentForSelection(String commentText) {
		try {
			// show dialog if commentText parameter is empty!
			if (StringUtils.isEmpty(commentText)) {
				InputDialog id = new InputDialog(getShell(), "Comment", "Please enter a comment: ", "", null);
				id.setBlockOnOpen(true);
				if (id.open() != Window.OK) {
					return;
				}
				commentText = id.getValue();
			}
			
			if (StringUtils.isEmpty(commentText)) {
				DialogUtil.showErrorMessageBox(getShell(), "Error", "Cannot add an empty comment!");
				return;
			}
				    			
			Map<String, Object> atts = new HashMap<>();
			atts.put(CommentTag.COMMENT_PROPERTY_NAME, commentText);
			addTagForSelection(CommentTag.TAG_NAME, atts, null);
			getUi().getCommentsWidget().reloadComments();
		} catch (Exception e) {
			onError("Error adding comment", e.getMessage(), e);
		}
		
		
	}
	
	private void addUiBindings() {
		DataBinder db = DataBinder.get();
		TrpSettings trpSets = getTrpSets();
		
		CanvasWidget cw = ui.canvasWidget;
		
		logger.debug("cw = "+cw);
		
		CanvasSettings canvasSet = cw.getCanvas().getSettings();
				
		db.bindBeanPropertyToObservableValue(TrpSettings.LEFT_VIEW_DOCKING_STATE_PROPERTY, trpSets, 
												Observables.observeMapEntry(ui.portalWidget.getDockingMap(), Position.LEFT));
		db.bindBeanPropertyToObservableValue(TrpSettings.BOTTOM_VIEW_DOCKING_STATE_PROPERTY, trpSets, 
												Observables.observeMapEntry(ui.portalWidget.getDockingMap(), Position.BOTTOM));
		
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_PRINTSPACE_PROPERTY, trpSets, cw.getShowPrintspaceItem());
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_TEXT_REGIONS_PROPERTY, trpSets, cw.getShowRegionsItem());
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_LINES_PROPERTY, trpSets, cw.getShowLinesItem());
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_BASELINES_PROPERTY, trpSets, cw.getShowBaselinesItem());
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_WORDS_PROPERTY, trpSets, cw.getShowWordsItem());
		
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_TEXT_REGIONS_PROPERTY, trpSets, cw.getToolbar().showRegionsButton);
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_LINES_PROPERTY, trpSets, cw.getToolbar().showLinesButton);
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_BASELINES_PROPERTY, trpSets, cw.getToolbar().showBaselinesButton);
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_WORDS_PROPERTY, trpSets, cw.getToolbar().showWordsButton);	
		db.bindBeanToWidgetSelection(TrpSettings.RENDER_BLACKENINGS_PROPERTY, trpSets, cw.getToolbar().renderBlackeningsButton);
		
		
				
//		DataBinder.get().bindBoolBeanValueToToolItemSelection("editingEnabled", canvasSet, cw.getEditingEnabledToolItem());
		
		if (TrpSettings.ENABLE_LINE_EDITOR)
			db.bindBoolBeanValueToToolItemSelection(TrpSettings.SHOW_LINE_EDITOR_PROPERTY, trpSets, ui.showLineEditorToggle);
		
		db.bindBeanToWidgetSelection(TrpSettings.RECT_MODE_PROPERTY, trpSets, ui.canvasWidget.getToolbar().getRectangleModeItem());
		db.bindBeanToWidgetSelection(TrpSettings.AUTO_CREATE_PARENT_PROPERTY, trpSets, ui.canvasWidget.getToolbar().getAutoCreateParentItem());
		
		db.bindBeanToWidgetSelection(TrpSettings.ADD_LINES_TO_OVERLAPPING_REGIONS_PROPERTY, trpSets, ui.canvasWidget.getToolbar().getAddLineToOverlappingRegionItem());
		db.bindBeanToWidgetSelection(TrpSettings.ADD_BASELINES_TO_OVERLAPPING_LINES_PROPERTY, trpSets, ui.canvasWidget.getToolbar().getAddBaselineToOverlappingLineItem());
		db.bindBeanToWidgetSelection(TrpSettings.ADD_WORDS_TO_OVERLAPPING_LINES_PROPERTY, trpSets, ui.canvasWidget.getToolbar().getAddWordsToOverlappingLineItem());
		
		db.bindBeanToWidgetSelection(CanvasSettings.LOCK_ZOOM_ON_FOCUS_PROPERTY, TrpConfig.getCanvasSettings(), ui.canvasWidget.getToolbar().getLockZoomOnFocusItem());
		
		db.bindBeanToWidgetSelection(TrpSettings.DELETE_LINE_IF_BASELINE_DELETED_PROPERTY, trpSets, ui.canvasWidget.getToolbar().getDeleteLineIfBaselineDeletedItem());
		
		db.bindBeanToWidgetSelection(TrpSettings.SELECT_NEWLY_CREATED_SHAPE_PROPERTY, trpSets, ui.canvasWidget.getToolbar().getSelectNewlyCreatedShapeItem());
		
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_READING_ORDER_REGIONS_PROPERTY, trpSets, cw.getShowReadingOrderRegionsMenuItem());
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_READING_ORDER_LINES_PROPERTY, trpSets, cw.getShowReadingOrderLinesMenuItem());
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_READING_ORDER_WORDS_PROPERTY, trpSets, cw.getShowReadingOrderWordsMenuItem());
		
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_READING_ORDER_REGIONS_PROPERTY, trpSets, cw.getToolbar().showReadingOrderRegionsButton);
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_READING_ORDER_LINES_PROPERTY, trpSets, cw.getToolbar().showReadingOrderLinesButton);
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_READING_ORDER_WORDS_PROPERTY, trpSets, cw.getToolbar().showReadingOrderWordsButton);
	}
	
//	public TaggingWidgetOldListener getTaggingWidgetListener() {
//		return taggingWidgetListener;
//	}

	// boolean isThisDocOpen(TrpJobStatus job) {
	// return storage.isDocLoaded() && storage.getDoc().getId()==job.getDocId();
	// }

	// public void detachListener() {
	// globalEventsListener.detach();
	// }
	//
	// public void attachListener() {
	// globalEventsListener.attach();
	// }

	public void loginDialog(String message) {
		try {
			if (!getTrpSets().isServerSideActivated()) {
				throw new NotSupportedException("Connecting to the server not supported yet!");
			}

			// detachListener();

			if (loginDialog != null && !loginDialog.isDisposed()) {
				loginDialog.close();
			}

			List<String> storedUsers = TrpGuiPrefs.getUsers();
			
			loginDialog = new TrpLoginDialog(getShell(), this, message, storedUsers.toArray(new String[0]), TrpServerConn.SERVER_URIS, TrpServerConn.DEFAULT_URI_INDEX);
			loginDialog.open();

			// attachListener();
		} catch (Throwable e) {
			onError("Error during login", "Unable to login to server", e);
			ui.updateLoginInfo(false, "", "");
		}
	}

	/**
	 * Gets called when the login dialog is closed by a successful login
	 * attempt.<br>
	 * It's a verbose method name, I know ;-)
	 */
	public void onSuccessfullLoginAndDialogIsClosed() {
		logger.debug("onSuccessfullLoginAndDialogIsClosed");

		/*
		 * during login we want to load the last loaded doc from the previous logout
		 */
		//getTrpSets().getLastDocId();
//		if (getTrpSets().getLastDocId() != -1 && getTrpSets().getLastColId() != -1){
//			int colId = getTrpSets().getLastColId();
//			int docId = getTrpSets().getLastDocId();
//			loadRemoteDoc(docId, colId, 0);
//			getUi().getDocOverviewWidget().setSelectedCollection(colId, true);
//			getUi().getDocOverviewWidget().getDocTableWidget().loadPage("docId", docId, true);
//		}

		//section to load the last used document for each user - either local or remote doc
		if (false) {
			if (!RecentDocsPreferences.getItems().isEmpty()) {
				if (RecentDocsPreferences.isShowOnStartup()) {
					String docToLoad = RecentDocsPreferences.getItems().get(0);
					loadRecentDoc(docToLoad);
				}
			} else {
				//if no recent docs are available -> load the example doc
				if (false) {
					loadRemoteDoc(5014, 4);
//					getUi().getServerWidget().setSelectedCollection(4, true);
//					getUi().getServerWidget().getDocTableWidget().loadPage("docId", 5014, true);
				}
			}
		}

//		reloadDocList(ui.getDocOverviewWidget().getSelectedCollection());
//		reloadHtrModels();
		// reloadJobListForDocument();
	}

	public boolean login(String server, String user, String pw, boolean rememberCredentials) throws ClientVersionNotSupportedException, LoginException, Exception {
//		try {
			if (!getTrpSets().isServerSideActivated()) {
				throw new NotSupportedException("Connecting to the server not supported yet!");
			}

			storage.login(server, user, pw);
			if (rememberCredentials) { // store credentials on successful login
				logger.debug("storing credentials for user: " + user);
				TrpGuiPrefs.storeCredentials(user, pw);
			}
			TrpGuiPrefs.storeLastLogin(user);
			TrpGuiPrefs.storeLastAccountType(OAuthGuiUtil.TRANSKRIBUS_ACCOUNT_TYPE);

			storage.reloadCollections();

			userCache.add(user);
			


			if (sessionExpired && !lastLoginServer.equals(server)) {
				closeCurrentDocument(true);
			}

			sessionExpired = false;
			lastLoginServer = server;
			
			/*
			 * when user is logged in we can store the tag definitions into the DB
			 * later on the are stored each time they change
			 */
			try {
				storage.updateCustomTagSpecsForUserInDB();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return true;
//		}
//		catch (ClientVersionNotSupportedException e) {
//			DialogUtil.showErrorMessageBox(getShell(), "Version not supported anymore!", e.getMessage());
//			logger.error(e.getMessage(), e);
//			return false;
//		}
//		catch (LoginException e) {
//			logout(true, false);
//			logger.error(e.getMessage(), e);
//			return false;
//		}
//		catch (Exception e) {
//			logout(true, false);
//			logger.error(e.getMessage(), e);
//			return false;
//		}

		// finally {
		// ui.updateLoginInfo(storage.isLoggedIn(), getCurrentUserName(),
		// storage.getCurrentServer());
		// }
	}

	public void loadRecentDoc(String docToLoad) {
		if (docToLoad == null)
			return;
		
		try {
			storage.checkConnection(true);
		} catch (NoConnectionException e1) {
			// TODO Auto-generated catch block
			loginDialog("No connection to server!");
		}
				
		String[] tmp = docToLoad.split(";;;");
		if (tmp.length == 1) {
			if (new File(tmp[0]).exists()) {
				loadLocalDoc(tmp[0]);
			} else {
				DialogUtil.createAndShowBalloonToolTip(getShell(), SWT.ICON_ERROR, "Loading Error", "Local folder does not exist anymore", 2, true);
			}
		} else if (tmp.length == 3 || tmp.length == 4) {
			boolean loadPage = (tmp.length == 4 ? true : false);
//			for (int i = 0; i < tmp.length; i++){
//				logger.debug(" split : " + tmp[i]);
//			}
			int docid = Integer.valueOf(tmp[1]);
			int colid = Integer.valueOf(tmp[2]);

			List<TrpDocMetadata> docList;
			try {
				docList = storage.getConnection().findDocuments(colid, docid, "", "", "", "", true, false, 0, 0, null, null);
				if (docList != null && docList.size() > 0) {
					if (loadPage){
						int pageId = Integer.valueOf(tmp[3]);
						loadRemoteDoc(docid, colid, (pageId-1));
					}
					else{
						loadRemoteDoc(docid, colid);
					}
				} else {
					//DialogUtil.createAndShowBalloonToolTip(getShell(), SWT.ICON_ERROR, "Loading Error", "Last used document is not on this server", 2, true);
				}
			} catch (SessionExpiredException | ServerErrorException | ClientErrorException | IllegalArgumentException e) {
				//logger.debug(" exception message " + e.toString() +  " -> " + e.getMessage());
				if(e instanceof SessionExpiredException){
					logger.debug("Exception message " + e.toString() +  " -> " + e.getMessage());
					loginDialog("Session Expired!");
					//retry
					loadRecentDoc(docToLoad);
				}
			}

		}

	}

	public boolean loginOAuth(final String server, final String refreshToken, final String state, final String redirectUri, final OAuthProvider prov)
			throws OAuthTokenRevokedException {
		final String grantType = "refresh_token";
		try {
			if (!getTrpSets().isServerSideActivated()) {
				throw new NotSupportedException("Connecting to the server not supported yet!");
			}
			storage.loginOAuth(server, refreshToken, state, grantType, redirectUri, prov);
			TrpGuiPrefs.storeLastAccountType(prov.toString());

			storage.reloadCollections();

			if (sessionExpired && !lastLoginServer.equals(server)) {
				closeCurrentDocument(true);
			}

//			reloadDocList(ui.getDocOverviewWidget().getSelectedCollection());
//			reloadHtrModels();
			// reloadJobListForDocument();
			sessionExpired = false;
			lastLoginServer = server;
			return true;
		} catch (OAuthTokenRevokedException oau) {
			logout(true, false);
			logger.error("The OAuth token seems to have been revoked!");
			throw oau;
		} catch (LoginException e) {
			logout(true, false);
			logger.error(e.getMessage(), e);
			return false;
		} catch (Exception e) {
			logout(true, false);
			logger.error(e.getMessage(), e);
			return false;
		}

		// finally {
		// ui.updateLoginInfo(storage.isLoggedIn(), getCurrentUserName(),
		// storage.getCurrentServer());
		// }
	}

//	public int getSelectedCollectionIndex() {
//		return ui.getDocOverviewWidget().getSelectedCollectionIndex();
//	}

	public void logout(boolean force, boolean closeOpenDoc) {
		if (!force && !saveTranscriptDialogOrAutosave())
			return;

		logger.debug("Logging out " + storage.getUser());
		storage.logout();

		if (closeOpenDoc && !storage.isLocalDoc()) {
			closeCurrentDocument(true);
		}

		ui.serverWidget.setSelectedCollection(null);
		clearDocList();
		
//		clearHtrModelList();
//		ui.getJobOverviewWidget().refreshPage(true);
		updateThumbs();

		// reloadJobListForDocument();
		// ui.updateLoginInfo(false, getCurrentUserName(), "");
	}

	public String getCurrentUserName() {
		if (storage.getUser() != null)
			return storage.getUser().getUserName();
		else
			return "";
	}

	public void saveDocMetadata() {
		if (!storage.isDocLoaded())
			return;

		final int colId = storage.getCurrentDocumentCollectionId();
		try {
			storage.saveDocMd(colId);
			logger.debug("saved doc-md to collection "+colId);

			// DialogUtil.createAndShowBalloonToolTip(getShell(),
			// SWT.ICON_ERROR, "Success saving doc-metadata", "", 2, true);
//			DialogUtil.showInfoMessageBox(shell, "Success", message);
			
//			DialogUtil.createAndShowBalloonToolTip(getShell(), SWT.ICON_INFORMATION, "Saved document metadata!", "Success", 2, true);
		} catch (Exception e) {
			Display.getDefault().asyncExec(() -> {
				onError("Error saving doc-metadata", e.getMessage(), e, true, true);	
			});
		}
	}

	/** Reassigns unique id's to the current page file */
	public void updateIDs() {
		try {
			if (!storage.hasTranscript())
				return;

			storage.getTranscript().getPage().updateIDsAccordingToCurrentSorting();
			updatePageRelatedMetadata();

			// reload tree with new IDs:
			refreshStructureView();
		} catch (Throwable th) {
			onError("Could not update IDs", "Unable to update IDs - see error log for more details", th);
		}
	}

	public void saveTranscriptionToNewFile() {
		if (!storage.hasTranscript())
			return;

		logger.debug("saving transcription to file...");
		String fn = DialogUtil.showSaveDialog(getShell(), "Choose a file", null, new String[] { "*.xml" });
		if (fn == null)
			return;
		File f = new File(fn);
		try {
			PageXmlUtils.marshalToFile(storage.getTranscript().getPageData(), f);
		} catch (Exception e1) {
			onError("Saving Error", "Error while saving transcription to " + f.getAbsolutePath(), e1);
		}
		logger.debug("finished writing xml output to " + f.getAbsolutePath());
	}
	
	

	Thread autoSaveThread;
	
	Runnable saveTask = new Runnable(){			
		int autoSaveInterval;
		String autoSavePath;
		
		@Override
		public void run() {
			while(true){
				try{	
					autoSaveInterval = getTrpSets().getAutoSaveInterval();
					Thread.sleep(autoSaveInterval * 1000);					
					autoSavePath = getTrpSets().getAutoSaveFolder();
					
					Display.getDefault().asyncExec(() -> {
						localAutoSave(autoSavePath);	
					});

				} catch(Exception e){
					logger.error("Exception " + e, e);
				}
			}
		}
	};
	
	public void beginAutoSaveThread(){
		
		if(autoSaveThread != null){
			if(autoSaveThread.isAlive()){
				autoSaveThread.interrupt();
				logger.debug("AutoSave Thread interrupted");
			}
		}		
		if(getTrpSets().getAutoSaveEnabled()){
			autoSaveThread = new Thread(saveTask, "AutoSaveThread");
			autoSaveThread.start();
			logger.debug("AutoSave Thread started");
		}
	}
	
	
	public boolean localAutosaveEnabled = true;
	
	public void localAutoSave(String path){
		if(!storage.isPageLoaded()){
			return;
		}
		if (storage.getTranscript() == null || storage.getTranscript().getMd() == null)
			return;
		
		if(!localAutosaveEnabled){
			return;
		}
		
		if(!storage.isTranscriptEdited()){
			return;
		}
		
		File f = null;
		try {
			PcGtsType currentPage = storage.getTranscript().getPageData();
			if(currentPage == null){
				return;
			}
			Date datenow = new Date();
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTime(datenow);
			XMLGregorianCalendar xc = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
			currentPage.getMetadata().setLastChange(xc);
			String tempDir = path;
			tempDir += File.separator + storage.getTranscript().getMd().getPageId()+".xml";
//			tempDir += File.separator + "p" + storage.getTranscript().getMd().getPageId()+"_autoSave.xml";
			f = new File(tempDir);
			
			byte[] bytes = PageXmlUtils.marshalToBytes(currentPage);
//			PageXmlUtils.marshalToFile(storage.getTranscript().getPageData(), f);
			FileUtils.writeByteArrayToFile(f, bytes);
			logger.trace("Auto-saved current transcript to " + f.getAbsolutePath());
		} catch (Exception e1) {
//			onError("Saving Error", "Error while saving transcription to " + f.getAbsolutePath(), e1);
			String fn = f==null ? "NA" : f.getAbsolutePath();
			logger.error("Error while autosaving transcription to " + fn, e1);
		}
	}
	
//	public boolean checkLocalSaves(TrpPage page) {
//		List<File> files = autoSaveController.getAutoSavesFiles(page);
//		
//	    if (CoreUtils.isEmpty(files)) {
//	    	logger.debug("No local autosave files found.");
//	    	return false;
//	    }
//	    
//	    logger.debug("Local autosave files found! Comparing timestamps...");	    
//	    File localTranscript = files.get(0);
//	    	    
//	    try {
//	    	/*
//	    	 * getLastChange() Doesn't return correct metadata xml element ???
//	    	 * (getCreator/creationDate work fine)
//	    	 * --> use actual file lastmodified time instead for now	    	
//	    	 */
////			localTimestamp = pcLocal.getMetadata().getLastChange();	    	
//	    	
//			long lLocalTimestamp = localTranscript.lastModified();
//		    GregorianCalendar gc = new GregorianCalendar();
//		    gc.setTimeInMillis(lLocalTimestamp);
//		    XMLGregorianCalendar localTimestamp = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);	
//	    
//			logger.debug("local timestamp: "
//	    		+localTimestamp.getMonth()
//			    + "/" + localTimestamp.getDay()
//			    +"h" + localTimestamp.getHour() 
//			    + "m" + localTimestamp.getMinute() 
//			    + "s" + localTimestamp.getSecond());
//	    
//		    long lRemoteTimestamp = page.getCurrentTranscript().getTimestamp();
//		    gc.setTimeInMillis(lRemoteTimestamp);
//		    XMLGregorianCalendar remoteTimeStamp = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);	  
////		    XMLGregorianCalendar remoteTimeStamp = storage.getTranscript().getPage().getPcGtsType().getMetadata().getLastChange();
//
//		    logger.debug("remote timestamp: "
//	    		+remoteTimeStamp.getMonth()
//			    + "/" + remoteTimeStamp.getDay()
//			    +"h" + remoteTimeStamp.getHour() 
//			    + "m" + remoteTimeStamp.getMinute() 
//			    + "s" + remoteTimeStamp.getSecond());
//	    
//		    //Return false if local autosave transcript is older
//		    if(localTimestamp.compare(remoteTimeStamp)==DatatypeConstants.LESSER 
//		    		||localTimestamp.compare(remoteTimeStamp)==DatatypeConstants.EQUAL ){
//		    	logger.debug("No newer autosave transcript found.");
//		    	return false;
//		    }
//	
//		    logger.debug("Newer autosave transcript found.");
//		    Display.getDefault().syncExec(new Runnable() {
//		        public void run() {
//		        	String diagText = "A newer transcript of this page exists on your computer. Do you want to load it?";
//		        	if(DialogUtil.showYesNoCancelDialog(getShell(),"Newer version found in autosaves",diagText) == SWT.YES){
//		        		logger.debug("loading local transcript into view");	        		
//
//		    			try {
//			        		PcGtsType pcLocal = PageXmlUtils.unmarshal(localTranscript);
//			        		JAXBPageTranscript jxtr = new JAXBPageTranscript();
//			        		jxtr.setPageData(pcLocal);
//			        		storage.getTranscript().setPageData(pcLocal);
//			        		storage.getTranscript().setMd(jxtr.getMd());
//			        		storage.setLatestTranscriptAsCurrent();      		    				
//							loadJAXBTranscriptIntoView(storage.getTranscript());
//						} catch (Exception e) {
//							TrpMainWidget.getInstance().onError("Error when loading transcript into view.", e.getMessage(), e.getCause());
//							e.printStackTrace();
//						}
//		    			ui.taggingWidget.updateAvailableTags();
//		    			updateTranscriptionWidgetsData();
//		    			canvas.getScene().updateSegmentationViewSettings();
//		    			canvas.update();
//		    			
////	    				reloadCurrentPage(true);	        		
//		        	}
//		        }
//		    });
//	    
//	    }catch (Exception e){
//	    	e.printStackTrace();
//	    }
//	    
//		return true;
//	}
	
	
	public boolean saveTranscriptionSilent() {
		try {
			if (!storage.isPageLoaded()) {
//				DialogUtil.showErrorMessageBox(getShell(), "Saving page", "No page loaded!");
				return false;
			}

			final String commitMessage = "";
			logger.debug("commitMessage = " + commitMessage);

			final int colId = storage.getCurrentDocumentCollectionId();

			Runnable saveTask = new Runnable() {
				
				@Override public void run() {
					try {
						storage.saveTranscript(colId, commitMessage);					

						storage.setLatestTranscriptAsCurrent();
					} catch (Exception e) {
						//throw new InvocationTargetException(e, e.getMessage());
					}
					logger.debug("Async save completed.");
				}
			};
			if(asyncSaveThread != null){
				if(asyncSaveThread.isAlive()){
					asyncSaveThread.interrupt();
				}
			}
			asyncSaveThread = new Thread(saveTask, "Async Save Thread");
			asyncSaveThread.start();
			updateToolBars();
			return true;
			
		} catch (Throwable e) {
			onError("Saving Error", "Error while saving transcription", e);
			return false;
		} finally {
			updatePageInfo();
		}
	}

	public boolean saveTranscription(boolean isCommit) {
		// final List<TrpTranscriptMetadata> newTransList = new ArrayList<>();

		try {
			if (!storage.isPageLoaded()) {
				DialogUtil.showErrorMessageBox(getShell(), "Saving page", "No page loaded!");
				return false;
			}

			String commitMessageTmp = null;
			if (isCommit) {
				InputDialog id = new InputDialog(getShell(), "Commit message", "Please enter a commit message: ", "", null);
				id.setBlockOnOpen(true);
				if (id.open() != Window.OK) {
					return false;
				}
				commitMessageTmp = id.getValue();
			}
			final String commitMessage = commitMessageTmp;
			logger.debug("commitMessage = " + commitMessage);

			final int colId = storage.getCurrentDocumentCollectionId();
			
			updateRecentDocItems(Storage.getInstance().getDoc().getMd());			

			RecentDocsPreferences.push(Storage.getInstance().getDoc().getMd().getTitle() + ";;;" + storage.getDocId() + ";;;" + colId + ";;;" + (storage.getPageIndex()+1));
			ui.getServerWidget().updateRecentDocs();
			
			
			// canvas.getScene().selectObject(null, true, false); // security
			// measure due to mysterious bug leading to freeze of progress
			// dialog
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						logger.debug("saving transcription, commitMessage = " + commitMessage);
						monitor.beginTask("Saving transcription", IProgressMonitor.UNKNOWN);
						storage.saveTranscript(colId, commitMessage);
						// set new transcription list and reload locally:
						logger.debug("Saved file - reloading transcript");

						storage.setLatestTranscriptAsCurrent();
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Saving", false);

//			reloadCurrentTranscript(true, true);
			updateToolBars();
//			updateSelectedTranscription();
			return true;
		} catch (Throwable e) {
			onError("Saving Error", "Error while saving transcription", e);
			return false;
		} finally {
			updatePageInfo();
		}
	}

	private void updateRecentDocItems(TrpDocMetadata md) {
		ListIterator<String> it = RecentDocsPreferences.getItems().listIterator();
		
		//delete all entries starting with title;;;docID;;;colID
		while (it.hasNext()) {
			if (it.next().startsWith(md.getTitle() + ";;;" + md.getDocId() + ";;;" + Storage.getInstance().getCollId())){
				it.remove();
			}
		}
		
	}

	public void updateSegmentationEditStatus() {
		boolean isEditOn = getCanvas().getSettings().isEditingEnabled();

		// ui.getUpdateIDsItem().setEnabled(isEditOn);
		if (isEditOn) {
			canvas.getScene().updateSegmentationViewSettings();
		}

		// updateAddShapeActionButton();
		ui.redraw();
	}

	// /**
	// * Updates enable state of the add shape button in the canvas widget
	// * depending on the selected action and the currently selected element
	// */
	// public void updateAddShapeActionButton() {
	// CanvasToolBar toolbar = ui.getCanvasToolBar();
	// String currentAction = ui.getSelectedAddShapeActionText();
	// ICanvasShape selected = getCanvas().getFirstSelected();
	//
	// if (storage.currentJAXBTranscript == null ||
	// storage.currentJAXBTranscript.getPage() == null)
	// return;
	//
	// if (toolbar.getAddShape()== null || toolbar.getAddShape().isDisposed())
	// return;
	//
	// toolbar.getAddShape().setEnabled(false);
	// if (!getCanvas().getSettings().isEditingEnabled())
	// return;
	//
	// Object data = null;
	// if (selected != null) {
	// data = selected.getData();
	// }
	//
	// boolean enable = false;
	// if (currentAction.equals(SegmentationTypes.TYPE_REGION)) {
	// enable = true;
	// } else if (currentAction.equals(SegmentationTypes.TYPE_PRINTSPACE)) {
	// enable =
	// storage.currentJAXBTranscript.getPageData().getPage().getPrintSpace() ==
	// null;
	// } else if (currentAction.equals(SegmentationTypes.TYPE_LINE)) {
	// enable = (data != null && data instanceof TextRegionType);
	// } else if (currentAction.equals(SegmentationTypes.TYPE_WORD)) {
	// enable = (data != null && data instanceof TextLineType);
	// } else if (currentAction.equals(SegmentationTypes.TYPE_BASELINE)) {
	// enable = data != null && data instanceof TextLineType && ((TextLineType)
	// data).getBaseline() == null;
	// }
	//
	// toolbar.getAddShape().setEnabled(enable);
	// }

	public void updateTranscriptionWidget(TranscriptionLevel type) {
		ATranscriptionWidget aw = null;
		if (type == TranscriptionLevel.WORD_BASED)
			aw = ui.getWordTranscriptionWidget();
		else
			aw = ui.getLineTranscriptionWidget();

		try {
			// update storage data:
			ICanvasShape shape = getCanvas().getFirstSelected();
			storage.updateDataForSelectedShape(shape);

			aw.updateData(storage.getCurrentRegionObject(), storage.getCurrentLineObject(), storage.getCurrentWordObject());
		} catch (Throwable th) {
			onError("Error updating transcription", "Error during the update of the transcription widget (" + type + ")", th);
		}

		logger.debug("finished updating " + type + " based trancription widget");
	}

	public void updateTranscriptionWidgetsData() {
		updateLineTranscriptionWidgetData();
		updateWordTranscriptionWidgetData();
	}

	public void updateWordTranscriptionWidgetData() {
		updateTranscriptionWidget(TranscriptionLevel.WORD_BASED);
	}

	public void updateLineTranscriptionWidgetData() {
		updateTranscriptionWidget(TranscriptionLevel.LINE_BASED);
	}

	public void jumpToPage(int index) {
		if (saveTranscriptDialogOrAutosave()) {
			if (storage.setCurrentPage(index)) {
				reloadCurrentPage(true);
				if (getTrpSets().getAutoSaveEnabled() && getTrpSets().isCheckForNewerAutosaveFile()) {
					autoSaveController.checkForNewerAutoSavedPage(storage.getPage());
				}
			}
		}
	}

	public void jumpToTranscript(TrpTranscriptMetadata md, boolean reloadSamePage) {
		if (saveTranscriptDialogOrAutosave()) {
			boolean changed = storage.setCurrentTranscript(md);

			if (reloadSamePage || changed) {
				reloadCurrentTranscript(false, true);
			}
		}
	}

	public void jumpToNextRegion() {
		jumpToRegion(Storage.getInstance().getCurrentRegion() + 1);
	}

	public void jumpToPreviousRegion() {
		jumpToRegion(Storage.getInstance().getCurrentRegion() - 1);
	}

	public void jumpToNextCell(int keycode) {
		TrpTableCellType currentCell = TableUtils.getTableCell(GuiUtil.getCanvasShape(Storage.getInstance().getCurrentRegionObject())); 
		if (currentCell == null) {
			logger.debug("No table found in transcript");
			return;
		}
		TableUtils.selectNeighborCell(getCanvas(), 
				currentCell, 
				TableUtils.parsePositionFromArrowKeyCode(keycode));
	}
	
	public void jumpToRegion(int index) {
		if (storage.jumpToRegion(index)) {
			// get item and select it in canvas, then it will automatically be
			// shown in the transcription widget:
			selectObjectWithData(storage.getCurrentRegionObject(), true, false);
			getCanvas().focusFirstSelected();
		}
	}

	// public ICanvasShape selectObjectWithData(ITrpShapeType trpShape) {
	// return selectObjectWithData(trpShape, true);
	// }

	public ICanvasShape selectObjectWithData(ITrpShapeType trpShape, boolean sendSignal, boolean multiselect) {
		ICanvasShape shape = null;
		if (trpShape != null && trpShape.getData() != null) {
			shape = (ICanvasShape) trpShape.getData();
			getScene().selectObject(shape, sendSignal, multiselect);
		}
		return shape;
	}

	public void nextPage() {
		jumpToPage(storage.getPageIndex() + 1);
	}

	public void prevPage() {
		jumpToPage(storage.getPageIndex() - 1);
	}

	public void firstPage() {
		jumpToPage(0);
	}

	public void lastPage() {
		jumpToPage(storage.getNPages() - 1);
	}

	public void reloadCurrentDocument() {
		if (!storage.isDocLoaded())
			return;

		if (storage.getDoc().isRemoteDoc())
			loadRemoteDoc(storage.getDoc().getId(), storage.getDoc().getCollection().getColId());
		else
			loadLocalDoc(storage.getDoc().getMd().getLocalFolder().getAbsolutePath());
	}

	/** Returns false if user presses cancel */
	public boolean saveTranscriptDialogOrAutosave() {
		if (!storage.isPageLocked() && storage.isTranscriptEdited()) {
			int r = DialogUtil.showYesNoCancelDialog(getShell(), "Unsaved changes",
					"There are unsaved changes in the transcript - do you want to save them first?");
			if (r == SWT.CANCEL)
				return false;
			if (r == SWT.YES) {
				return this.saveTranscription(false);
			}
		}
		return true;
	}

	public void updatePageLock() {
		if (storage.isPageLocked() != isPageLocked) { // page locking changed
			isPageLocked = storage.isPageLocked();
			TrpConfig.getCanvasSettings().setEditingEnabled(!isPageLocked);

			SWTUtil.setEnabled(ui.getCanvasWidget().getEditingEnabledToolItem(), !isPageLocked);
			
			SWTUtil.setEnabled(ui.getTranscriptionComposite(), !isPageLocked);
			SWTUtil.setEnabled(ui.getSaveDropDown(), !isPageLocked);

			updatePageInfo();
			updateToolBars();
		}
	}

	public void closeCurrentDocument(boolean force) {
		if (force || saveTranscriptDialogOrAutosave()) {
			storage.closeCurrentDocument();

			reloadCurrentPage(false);
			updatePageInfo();
		}
	}

	/**
	 * (Tries) to determine whether the selection in the current transcription
	 * widget corresponds to the current selection in the canvas. This method is
	 * used e.g. in the class {@link PageMetadataWidgetListener} to distinguish
	 * between tagging based on the transcriptin widget selection and the canvas
	 * selection.
	 */
	public boolean isTextSelectedInTranscriptionWidget() {
		ATranscriptionWidget aw = ui.getSelectedTranscriptionWidget();
		// if (aw == null)
		// return false;

		return aw != null && !aw.isSingleSelection();

		// this was all bullshit:
		// Class<? extends ITrpShapeType> clazz =
		// aw.getTranscriptionUnitClass();
		// // get selected shape data in canvas:
		// List<Object> selectedData = canvas.getScene().getSelectedData();
		// int nSelectedInCanvas = selectedData.size();
		//
		// // if selection range in transcription widget is empty and multiple
		// elements are selected in the canvas, return false:
		// boolean isSelectionEmpty = aw.getText().getSelectionText().isEmpty();
		// if (isSelectionEmpty /*&& nSelectedInCanvas>1*/)
		// return false;

		// // get selected shapes in transcription widget:
		// List<ITrpShapeType> selectedInTw = aw.getSelectedShapes();
		// int nSelectedInTw = selectedInTw.size();
		//
		// // nr of selections in canvas != number of selection in transcription
		// widget -> return false (should be covered by above if however I
		// guess...)
		// if (nSelectedInCanvas != nSelectedInTw)
		// return false;
		//
		// // now check if all elements selected in the canvas correspond with
		// the elements selected in the tw:
		// for (int i=0; i<selectedData.size(); ++i) {
		// Object o = selectedData.get(i);
		// if (!selectedInTw.contains(o))
		// return false;
		// }
		// return true;
	}

	public void updatePageRelatedMetadata() {
		logger.debug("updating page related metadata!");

		int nSel = canvas.getNSelected();
		List<ICanvasShape> selected = canvas.getScene().getSelectedAsNewArray();

		if (!storage.hasTranscript()) {
//			ui.taggingWidget.setSelectedTags(null);
			ui.getStructuralMetadataWidget().updateData(null, null, nSel, null, new ArrayList<CustomTag>());
			return;
		}

		// TEST: update tagging widget:
		// ui.tw3.setInput(storage.getTranscript().getPage());

		// storage.getTranscript().getPage().getTagsMap();

		// get structure type:
		// boolean hasStructure = nSel>=1;
		String structureType = null;
		for (ICanvasShape s : selected) {
			ITrpShapeType st = GuiUtil.getTrpShape(s);
			if (structureType == null) {
				structureType = st.getStructure();
			} else if (!structureType.equals(st.getStructure())) {
				structureType = null;
				break;
			}
		}

		ITrpShapeType st = GuiUtil.getTrpShape(canvas.getFirstSelected());
		if (nSel == 1) {
			structureType = st.getStructure();
		}

		// get tag(s) under cursor:
		List<CustomTag> selectedTags = new ArrayList<>();
		if (getUi().getSelectedTranscriptionWidget() != null) {
			selectedTags = getUi().getSelectedTranscriptionWidget().getCustomTagsForCurrentOffset();
		}

		// List<CustomTag> selectedTags =
		// getUi().getSelectedTranscriptionWidget().getSelectedCommonCustomTags();
		// logger.debug("update metadata, nr of tags = "+selectedTags.size());

		// for (CustomTag t : selectedTags) {
		// if (!(t instanceof TextStyleTag))
		// selectedTagNames.add(t.getTagName());
		// }

//		ui.taggingWidget.setSelectedTags(selectedTags);
		ui.getStructuralMetadataWidget().updateData(storage.getTranscript(), st, nSel, structureType, selectedTags);
		
		if (ui.getTextStyleWidget()!=null) {
			ui.getTextStyleWidget().updateData();	
		}
	}

	public void updateTreeSelectionFromCanvas() {
		if (structTreeListener.isInsideTreeSelectionEvent) {
			// logger.debug("not updating tree!!");
			return;
		}

		List<Object> selData = canvas.getScene().getSelectedData();

		// select lines for baselines in struct view if lines not visible: 
		if (!getTrpSets().isShowLines()) {
			for (int i = 0; i < selData.size(); ++i) {
				Object o = selData.get(i);
				if (o instanceof TrpBaselineType) {
					TrpBaselineType bl = (TrpBaselineType) o;
					selData.set(i, bl.getLine());
				}
			}
		}

		// logger.debug("selected data size = "+selData.size());

		StructuredSelection sel = new StructuredSelection(selData);

		// if (!selData.isEmpty()) {
		// //
		// logger.debug("selected data = "+canvas.getScene().getSelectedData());
		// sel = new StructuredSelection(canvas.getScene().getSelectedData());
		// }

		getTreeListener().detach();
		ui.getStructureTreeViewer().setSelection(sel, true);
		getTreeListener().attach();
		
		ui.getStructuralMetadataWidget().getStructTagListWidget().updateTreeSelectionFromCanvas(selData);
	}

	// public void updateDocMetadata() {
	// if (storage.getDoc() != null)
	// ui.getDocMetadataEditor().setMetadata(storage.getDoc().getMd());
	// else
	// ui.getDocMetadataEditor().setMetadata(null);
	// }

	public void reloadCurrentImage() {
		try {
			Storage.getInstance().reloadCurrentImage(TrpMainWidget.getInstance().getSelectedImageFileType());
			updatePageInfo();
		} catch (Throwable e) {
			onError("Image load error", "Error loading main image", e);
		}
	}

	public boolean reloadCurrentPage(boolean force) {
		return reloadCurrentPage(force, true);
	}

	public boolean reloadCurrentPage(boolean force, boolean reloadTranscript) {
		if (!force && !saveTranscriptDialogOrAutosave())
			return false;

		try {
			logger.info("loading page: " + storage.getPage());
			clearCurrentPage();

			final int colId = storage.getCurrentDocumentCollectionId();
			final String fileType = mw.getSelectedImageFileType();
			logger.debug("selected img filetype = " + fileType);

			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						logger.debug("Runnable reloads page with index = " + (storage.getPageIndex() + 1));
						monitor.beginTask("Loading page " + (storage.getPageIndex() + 1), IProgressMonitor.UNKNOWN);
						storage.reloadCurrentPage(colId, fileType);
					} catch (Exception e) {
						throw new InvocationTargetException(e);
					}
				}
			}, "Loading page", false);

			if (storage.isPageLoaded() && storage.getCurrentImage() != null) {
				getScene().setMainImage(storage.getCurrentImage());
			}

			if (reloadTranscript && storage.getNTranscripts() > 0) {
				storage.setLatestTranscriptAsCurrent();
				reloadCurrentTranscript(false, true);
				updateVersionStatus();
			}

			return true;
		} catch (Throwable th) {
			String msg = "Could not load page " + (storage.getPageIndex() + 1);
			onError("Error loading page", msg, th);

			return false;
		} finally {
			updatePageLock();
			ui.getCanvasWidget().updateUiStuff();
			updateSegmentationEditStatus();
			getCanvas().updateEditors();
			updatePageRelatedMetadata();
			updateToolBars();
			updatePageInfo();
		}
	}

	public void createThumbForCurrentPage() {
		// generate thumb for loaded page if local doc:
		if (storage.isLocalDoc() && storage.getPage() != null && storage.getCurrentImage() != null) {
			CreateThumbsService.createThumbForPage(storage.getPage(), storage.getCurrentImage().img, false, null);
		}
	}

	public void updateThumbs() {
		logger.trace("updating thumbs");

		Display.getDefault().asyncExec(updateThumbsWidgetRunnable); // asyncExec needed??

		// try {
		// ui.thumbnailWidget.setUrls(storage.getDoc().getThumbUrls(),
		// storage.getDoc().getPageImgNames());
		// } catch (Exception e) {
		// onError("Error loading thumbnails", e.getMessage(), e);
		// }
	}

	private void clearCurrentPage() {
		getScene().clear();
		// getScene().selectObject(null);
		ui.getStructureTreeViewer().setInput(null);
		// getTreeViewer().refresh();

		updateToolBars();
	}

//	public void reloadTranscriptsList() {
//		try {
//			int colId = storage.getCurrentDocumentCollectionId();
//			storage.reloadTranscriptsList(colId);
//		} catch (Throwable e) {
//			onError("Error updating transcripts", "Error updating transcripts", e);
//		}
//		updateToolBars();
//	}

	/**
	 * Reloads the current transcrition
	 * 
	 * @param tryLocalReload
	 *            If true, the transcription is reloaded from the locally stored
	 *            object (if it has been loaded already!)
	 */
	public void reloadCurrentTranscript(boolean tryLocalReload, boolean force) {
		if (!force && !saveTranscriptDialogOrAutosave()) {
			return;
		}

		// LOAD STRUCT ELEMENTS FROM TRANSCRIPTS
		try {
			// save transcript if edited:
			// clearTranscriptFromView();
			logger.info("loading transcript: " + storage.getTranscript().getMd() + " tryLocalReload: " + tryLocalReload);
			canvas.getScene().selectObject(null, true, false); // security
																// measure due
																// to mysterious
																// bug leading
																// to freeze of
																// progress
																// dialog
			if (!tryLocalReload || !storage.hasTranscript()) {
				ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
					@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							monitor.beginTask("Loading transcription", IProgressMonitor.UNKNOWN);
							storage.reloadTranscript();
						} catch (Exception e) {
							throw new InvocationTargetException(e);
						}
					}
				}, "Loading transcription", false);
				// storage.reloadTranscript();
				// add observers for transcript:
				// addTranscriptObserver();
			}
			// logger.debug("CHANGED: "+storage.getTranscript().getPage().isEdited());
			loadJAXBTranscriptIntoView(storage.getTranscript());

//			ui.taggingWidget.updateAvailableTags();
			updateTranscriptionWidgetsData();
			canvas.getScene().updateSegmentationViewSettings();

			logger.debug("loaded transcript - edited = " + storage.isTranscriptEdited());
		} catch (Throwable th) {
			String msg = "Could not load transcript for page " + (storage.getPageIndex() + 1);
			onError("Error loading transcript", msg, th);
			clearTranscriptFromView();
		}
	}

	public void showLocation(TrpLocation l) {
		// if (l.md == null) {
		// DialogUtil.showErrorMessageBox(getShell(),
		// "Error showing custom tag",
		// "Cannot open custom tag - no related metadata found!");
		// return;
		// }

		logger.debug("showing loation: " + l);

		// 1st: load doc & page
		if (!l.hasDoc()) {
			logger.debug("location has no doc specified!");
			return;
		}
		int pageIndex = l.hasPage() ? l.pageNr - 1 : 0;

		boolean wasDocLoaded = false;
		if (!storage.isThisDocLoaded(l.docId, l.localFolder)) {
			wasDocLoaded = true;
			if (l.docId == -1) {
				if (!loadLocalDoc(l.localFolder.getAbsolutePath(), pageIndex))
					return;
			} else {
				if (!loadRemoteDoc(l.docId, l.collId, pageIndex))
					return;
			}
		}

		// 2nd: load page if not loaded by doc anyway:
		if (!l.hasPage()) {
			logger.debug("location has no page specified!");
			return;
		}

		if (!wasDocLoaded && storage.getPageIndex() != l.pageNr - 1) {
			if (!storage.setCurrentPage(l.pageNr - 1))
				return;
			if (!reloadCurrentPage(true))
				return;
		}

		// 3rd: select region / line / word:
		logger.debug("loading shape region: " + l.shapeId);
		if (l.shapeId == null) {
			logger.info("location has no region / line / word specified!");
			return;
		}
		ICanvasShape s = canvas.getScene().selectObjectWithId(l.shapeId, true, false);
		if (s == null) {
			logger.debug("shape is null!");
			return;
		}
		
		canvas.focusShape(s);
		
		ITrpShapeType st = canvas.getFirstSelectedSt();
		// 4th: select tag in transcription widget; TODO: select word!?
		if (l.t == null) {
			logger.debug("location has no tag specified!");
			return;
		} else {
			// reinforce focus on canvas
			canvas.focusShape(s, true);
		}
		if (st == null) {
			logger.warn("shape type could not be retrieved - should not happen here!");
		}
		
		ATranscriptionWidget tw = ui.getSelectedTranscriptionWidget();
		if (st instanceof TrpTextLineType && tw instanceof LineTranscriptionWidget) {
			logger.debug("selecting custom tag: "+l.t);
			tw.selectCustomTag(l.t);			
		}
		if (st instanceof TrpWordType && tw instanceof WordTranscriptionWidget) {
			// TODO
		} 
		
//		boolean isLine = l.getLowestShapeType() instanceof TrpTextLineType;
//		logger.debug("isLine: "+isLine);
//		boolean isWord = l.getLowestShapeType() instanceof TrpWordType;
//		
//		logger.debug("is lwtw: "+(tw instanceof LineTranscriptionWidget));
//		if (isLine && tw instanceof LineTranscriptionWidget) {
//
//		}
	}

	private void clearTranscriptFromView() {
		getUi().getStructureTreeViewer().setInput(null);
		getCanvas().getScene().clearShapes();
		getCanvas().redraw();
	}

	// @SuppressWarnings("rawtypes")
	void loadJAXBTranscriptIntoView(JAXBPageTranscript transcript) throws Exception {

		// add shapes to canvas:
		getCanvas().getScene().clearShapes();
		for (ITrpShapeType s : transcript.getPage().getAllShapes(false)) {
			shapeFactory.addAllCanvasShapes(s);
		}

		// set input to tree:
		// getUi().getStructureTreeViewer().setInput(transcript.getPage());
		getUi().getStructureTreeViewer().setInput(transcript.getPageData());

		getCanvas().redraw();
		// ui.updateTreeColumnSize();
		ui.getStructureTreeViewer().expandToLevel(3);
		// ui.getTreeViewer().expandToLevel(TreeViewer.ALL_LEVELS);
	}

	// public void updateAvailableTagNamesFromCurrentPage() {
	// ui.taggingWidget.updateAvailableTags();

	// if (true)
	// return;

	// if (!storage.hasTranscript())
	// return;

	// for (String tn : storage.getTranscript().getPage().getTagNames()) {
	// try {
	// logger.debug("1 adding tag: "+tn);
	// CustomTagFactory.addToRegistry(CustomTagFactory.create(tn));
	// } catch (Exception e) {
	// logger.warn(e.getMessage());
	// }
	// }

	// ui.taggingWidget.updateAvailableTags(); // still needed ... why???
	// }

	public CanvasShapeObserver getCanvasShapeObserver() {
		return canvasShapeObserver;
	}

	public CanvasWidget getCanvasWidget() {
		return ui.getCanvasWidget();
	}

	public SWTCanvas getCanvas() {
		return canvas;
	}

	public CanvasScene getScene() {
		return ui.getCanvas().getScene();
	}

//	public void updateSelectedTranscription() {
//		ui.versionsWidget.updateSelectedVersion(storage.getTranscriptMetadata());
//	}

	public void updateToolBars() {
		boolean isDocLoaded = storage.isDocLoaded();
		int nNPages = storage.getNPages();
		boolean isPageLocked = storage.isPageLocked();

		ui.getPagesPagingToolBar().setToolbarEnabled(nNPages > 0);
		ui.getPagesPagingToolBar().setValues(storage.getPageIndex() + 1, nNPages);

		if (!SWTUtil.isDisposed(ui.getPagesPagingToolBar().getLabelItem())) {
			ui.getPagesPagingToolBar().getLabelItem().setImage(isPageLocked ? Images.LOCK : null);
			ui.getPagesPagingToolBar().getLabelItem().setToolTipText(isPageLocked ? "Page locked" : "");
		}

		SWTUtil.setEnabled(ui.getCloseDocBtn(), isDocLoaded);
		SWTUtil.setEnabled(ui.getSaveDropDown(), isDocLoaded);
		if (ui.saveOptionsToolItem != null)
			SWTUtil.setEnabled(ui.saveOptionsToolItem.getToolItem(), isDocLoaded);

		SWTUtil.setEnabled(ui.getReloadDocumentButton(), isDocLoaded);
		SWTUtil.setEnabled(ui.getLoadTranscriptInTextEditor(), isDocLoaded);
		SWTUtil.setEnabled(ui.getStatusCombo(), isDocLoaded);
		
		if (Storage.getInstance().getTranscript() != null && Storage.getInstance().getTranscript().getMd() != null){
			ui.getStatusCombo().setText(Storage.getInstance().getTranscript().getMd().getStatus().getStr());
		}
		
		ui.updateToolBarSize();
	}

	public void loginAsTestUser() {

	}

	public boolean loadLocalDoc(String folder) {
		return loadLocalDoc(folder, 0);
	}

	public boolean loadLocalDoc(String folder, int pageIndex) {
		if (!saveTranscriptDialogOrAutosave()) {
			return false;
		}

		try {
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Loading local document from "+folder, IProgressMonitor.UNKNOWN);
					try {
						storage.loadLocalDoc(folder, monitor);
						logger.debug("loaded local doc "+folder);
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
//					} finally {
//						monitor.done();
//					}
				}
			}, "Loading local document", false);

			final boolean DISABLE_THUMB_CREATION_ON_LOAD = true;
			if (!DISABLE_THUMB_CREATION_ON_LOAD && getTrpSets().isCreateThumbs()) {
				//CreateThumbsService.createThumbForDoc(storage.getDoc(), false, updateThumbsWidgetRunnable);
			}

			storage.setCurrentPage(pageIndex);
			reloadCurrentPage(true);
			
			//store the path for the local doc
			RecentDocsPreferences.push(folder);
			ui.getServerWidget().updateRecentDocs();
			
			updateThumbs();
			getCanvas().fitWidth();
			return true;
		} catch (Throwable th) {
			onError("Error loading local document", "Could not load document: " + th.getMessage(), th);
			return false;
		}
	}

	public void loadLocalFolder() {
		logger.debug("loading a local folder...");
		String fn = DialogUtil.showOpenFolderDialog(getShell(), "Choose a folder with images and (optional) PAGE XML files", lastLocalDocFolder);
		if (fn == null)
			return;

		lastLocalDocFolder = fn;
		loadLocalDoc(fn);
	}

	// public void loadTestDocFromServer() {
	// reloadCurrentDocument();
	// }

//	public boolean loadRemoteDoc(final int docId) {
//		return loadRemoteDoc(docId, 0);
//	}

	public boolean loadRemoteDoc(final int docId, int colId) {
		return loadRemoteDoc(docId, colId, 0);
	}

	/**
	 * Loads a document from the remote server
	 * 
	 * @param docId
	 *            The id of the document to load
	 * @param colId
	 *            The id of the collection to load the document from.
	 *            A colId <= 0 means, the currently selected collection from the
	 *            DocOverViewWidget is taken (if one is selected!)
	 * @return True for success, false otherwise
	 */
	public boolean loadRemoteDoc(final int docId, int colId, int pageIndex) {
		if (!saveTranscriptDialogOrAutosave()) {
			return false;
		}

		try {
			boolean collectionChanged = colId != ui.serverWidget.getSelectedCollectionId();
			if (collectionChanged) {
				Future<List<TrpDocMetadata>> fut = reloadDocList(colId);
				if (fut == null)
					return false;
				
				fut.get(); // wait for doclist to be loaded!
			}
			
			canvas.getScene().selectObject(null, true, false); // security measure due to mysterious bug leading to freeze of progress dialog

			if (colId <= 0) {
				colId = ui.getServerWidget().getSelectedCollectionId();
				if (colId <= 0)
					throw new Exception("No collection specified to load document!");
			}

			final int colIdFinal = colId;

			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Loading remote document " + docId, IProgressMonitor.UNKNOWN);
					try {
						// if (true) throw new SessionExpiredException("Yo!");
						storage.loadRemoteDoc(colIdFinal, docId);
						logger.debug("loaded remote doc, colIdFinal = " + colIdFinal);
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Loading document from server", false);

			storage.setCurrentPage(pageIndex);
			reloadCurrentPage(true);
			if (getTrpSets().getAutoSaveEnabled() && getTrpSets().isCheckForNewerAutosaveFile()) {
				autoSaveController.checkForNewerAutoSavedPage(storage.getPage());
			}
			
			//store the recent doc info to the preferences
			if (pageIndex == 0){
				RecentDocsPreferences.push(Storage.getInstance().getDoc().getMd().getTitle() + ";;;" + docId + ";;;" + colIdFinal);
			}
			else if (pageIndex > 0){
				RecentDocsPreferences.push(Storage.getInstance().getDoc().getMd().getTitle() + ";;;" + docId + ";;;" + colIdFinal + ";;;" + (pageIndex+1));
			}
			ui.getServerWidget().updateRecentDocs();
									
//			getUi().getServerWidget().setSelectedCollection(colId);
			getUi().getServerWidget().getDocTableWidget().loadPage("docId", docId, true);

			updateThumbs();
			getCanvas().fitWidth();
			
			//change reading order circle width according to image resolution
			FimgStoreImgMd imgMd = storage.getCurrentImageMetadata();
			if (imgMd != null){
				double initWidth = readingOrderCircleInitWidth;
				logger.debug("initWidth " + initWidth);
				
				double resizeFactor = 1.0;
				if (imgMd.getXResolution() < 210){
					resizeFactor = 0.5;
				}
				else if(imgMd.getXResolution() > 210 && imgMd.getXResolution() < 390){
					resizeFactor = 0.75;
				}
				double tmpWith = initWidth*resizeFactor;
				logger.debug("set ro in settings " + tmpWith);
				
				canvas.getSettings().setReadingOrderCircleWidth((int) tmpWith);
			}			
			
			tmpCount++;
			return true;
		} catch (Throwable e) {
			onError("Error loading remote document", "Could not load document with id  " + docId, e);
			return false;
		}
		// finally {
		// updatePageInfo();
		// }
	}

	public void center() {
		ui.center();
	}
	
	public void onError(String title, String message, Throwable th, boolean logStackTrace, boolean showBalloonTooltip) {
		canvas.getMouseListener().reset();
		canvas.setMode(CanvasMode.SELECTION);
		canvas.layout();

		if (th instanceof SessionExpiredException) {
			sessionExpired = true;
			logout(true, false);
			logger.warn("Session expired!");
			loginDialog("Session expired!");
		} else {
			SWTLog.logError(logger, getShell(), title, message, th, logStackTrace);

			if (!showBalloonTooltip) {
				if (SWTLog.showError(logger, getShell(), title, message, th) == IDialogConstants.HELP_ID) {
					sendBugReport();
				}
			} else
				DialogUtil.createAndShowBalloonToolTip(getShell(), SWT.ICON_ERROR, title, message, 2, true);
		}
	}

	/**
	 * Prints an error message and the stack trace of the given throwable to the
	 * error log and pops up an error message box. Also, resets data in some
	 * listeners to recover from the error.
	 */
	public void onError(String title, String message, Throwable th) {
		onError(title, message, th, true, false);
	}

	public void onInterruption(String title, String message, Throwable th) {
		onError(title, message, th, true, true);
	}
	
	private static boolean shouldITrack() {
		
		try {
			try (FileInputStream fis = new FileInputStream(new File("config.properties"))) {
				Properties p = new Properties();
				p.load(fis);
				
				Object tracking = p.get("tracking");
				if (tracking!=null && ((String)tracking).equals("true"))
					return true;
			}
		} catch (Exception e) {
			logger.warn("Could not determine tracking property: "+e.getMessage());
		}
		return false;
		
	}
	

	public static void show() {
		ProgramInfo info = new ProgramInfo();
		Display.setAppName(info.getName());
		Display.setAppVersion(info.getVersion());
		
		DeviceData data = new DeviceData();

		data.tracking = shouldITrack();
		logger.info("resource tracking = "+data.tracking);
		
		Display display = new Display(data);

		show(display);
	}

	public static void show(Display givenDisplay) {
		BidiUtils.setBidiSupport(true);
		logger.debug("bidiSupport: "+BidiUtils.getBidiSupport()+ " isBidiPlatform: "+BidiUtil.isBidiPlatform());
		
		GuiUtil.initLogger();
		try {
			// final Display display = Display.getDefault();

			if (givenDisplay != null)
				display = givenDisplay;
			else
				display = new Display();

			final Shell shell = new Shell(display, SWT.SHELL_TRIM);
			setMainShell(shell);

			Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
				@Override public void run() {
					if (USE_SPLASH) {
						final SplashWindow sw = new SplashWindow(display);
						sw.start(new Runnable() {
							@Override public void run() {
								sw.setProgress(10);
								shell.setLayout(new FillLayout());
								mw = new TrpMainWidget(shell);
								shell.setMaximized(true);
								shell.open();
								shell.layout();
								sw.setProgress(50);
								// sw.setProgress(66);
								mw.postInit();
								// if (true) throw new
								// NullPointerException("ajdflkasjdf");

								sw.setProgress(100);
								sw.stop();

							}
						});
					} else {
						shell.setLayout(new FillLayout());
						mw = new TrpMainWidget(shell);
						// TrpMainWidgetView ui = mw.ui;

						// shell.setSize(1400, 1000);
						// mw.center();
						shell.setMaximized(true);
						shell.open();
						shell.layout();
						mw.postInit();
					}

					// the main display loop:
					logger.debug("entering main event loop");
					
					
					mw.openChangeLogDialog(getTrpSettings().isShowChangeLog());
					mw.openJavaVersionDialog();
					
					// while((Display.getCurrent().getShells().length != 0)
					// && !Display.getCurrent().getShells()[0].isDisposed()) {
					while (!shell.isDisposed()) {
						try {
							if (!Display.getCurrent().readAndDispatch()) {
								Display.getCurrent().sleep();
							}
						} catch (Throwable th) {
							logger.error("Unexpected error occured: "+th.getMessage(), th);
						}
					}

				}
			});

			// Display.getCurrent().dispose();
			logger.debug("Program end");

			// while (!ui.isDisposed()) {
			// if (!display.readAndDispatch()) {
			// display.sleep();
			// }
			// }
		} catch (Throwable e) {
			// Display.getCurrent().dispose();
			logger.error("PROGRAM EXIT WITH FATAL ERROR: " + e.getMessage(), e);
		}
	}

	// public void show() {
	// try {
	// Realm.runWithDefault(SWTObservables.getRealm(display),
	// new Runnable() {
	// public void run() {
	// ui.setSize(1400, 1000);
	// center();
	// ui.open();
	// ui.layout();
	//
	// postInit();
	// while (!ui.isDisposed()) {
	// if (!display.readAndDispatch()) {
	// display.sleep();
	// }
	// }
	// }
	// });
	//
	// // while (!ui.isDisposed()) {
	// // if (!display.readAndDispatch()) {
	// // display.sleep();
	// // }
	// // }
	// } catch (Exception e) {
	// logger.error(e);
	// }
	// }

	private static void setMainShell(Shell shell) {
		mainShell = shell;

	}

	public TrpMainWidgetView getUi() {
		return ui;
	}

	public Shell getShell() {
		return ui.getShell();
	}

	public CanvasSettings getCanvasSettings() {
		return ui.getCanvas().getSettings();
	}

	public TrpSettings getTrpSets() {
		return ui.getTrpSets();
	}

	public void redrawCanvas() {
		getCanvas().redraw();
	}

	public void refreshStructureView() {
		ui.getStructureTreeViewer().refresh();
		ui.getStructuralMetadataWidget().getStructTagListWidget().getTreeViewer().refresh();
	}

	// public TreeViewer getStructureTreeViewer() {
	// return ui.getStructureTreeViewer();
	// }

	public StructureTreeListener getTreeListener() {
		return structTreeListener;
	}

	public TrpShapeElementFactory getShapeFactory() {
		return shapeFactory;
	}

	public TranscriptObserver getTranscriptObserver() {
		return transcriptObserver;
	}

	/**
	 * replaced by {@link #uploadDocuments()}
	 */
	@Deprecated public void uploadSingleDocument() {
		try {
			if (!storage.isLoggedIn()) {
				DialogUtil.showErrorMessageBox(getShell(), "Not logged in!", "You have to be logged in to upload a document!");
				return;
			}

			final UploadDialog ud = new UploadDialog(getShell(), ui.getServerWidget().getSelectedCollection());
			int ret = ud.open();

			if (ret == IDialogConstants.OK_ID) {
				final TrpCollection c = ud.getCollection();
				final int cId = (c == null) ? -1 : c.getColId();
				if (c == null || (c.getRole() != null && !c.getRole().canManage())) {
					throw new Exception("Cannot upload to specified collection: " + cId);
				}

				logger.debug(
						"uploading to directory: " + ud.getFolder() + ", title: '" + ud.getTitle() + " collection: " + cId + " viaFtp: " + ud.isUploadViaFtp());
				String type = ud.isUploadViaFtp() ? "FTP" : "HTTP";

				// final int colId =
				// storage.getCollectionId(ui.getDocOverviewWidget().getSelectedCollectionIndex());
				ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
					@Override public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							// storage.uploadDocument(4, ud.getFolder(),
							// ud.getTitle(), monitor);// TEST
							boolean uploadViaFTP = ud.isUploadViaFtp();
							logger.debug("uploadViaFTP = " + uploadViaFTP);
							storage.uploadDocument(cId, ud.getFolder(), ud.getTitle(), monitor);
							if (!monitor.isCanceled())
								displaySuccessMessage(
										"Uploaded document!\nNote: the document will be ready after document processing on the server is finished - reload the document list occasionally");
						} catch (Exception e) {
							throw new InvocationTargetException(e);
						}
					}
				}, "Uploading via " + type, true);
			}
		} catch (Throwable e) {
			onError("Error loading uploading document", "Could not upload document", e);
		}
	}

	public void uploadDocuments() {
		try {
			if (!storage.isLoggedIn()) {
				DialogUtil.showErrorMessageBox(getShell(), "Not logged in!", "You have to be logged in to upload a document!");
				return;
			}

//			final UploadFromFtpDialog ud = new UploadFromFtpDialog(getShell(), ui.getServerWidget().getSelectedCollection());
			final UploadDialogUltimate ud = new UploadDialogUltimate(getShell(), ui.getServerWidget().getSelectedCollection());
			if (ud.open() != IDialogConstants.OK_ID)
				return;

			final TrpCollection c = ud.getCollection();
			final int cId = (c == null) ? -1 : c.getColId();
			if (c == null || (c.getRole() != null && !c.getRole().canManage())) {
				throw new Exception("You must be at least an editor to upload to this collection ("+ cId+")");
			}

			if (ud.isSingleDocUpload()) { // single doc upload
				logger.debug("uploading to directory: " + ud.getFolder() + ", title: '" + ud.getTitle() + " collection: " + cId);
				ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
					@Override public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						TrpUpload upload = null;
						try {
							upload = storage.uploadDocument(cId, ud.getFolder(), ud.getTitle(), monitor);
							if (!monitor.isCanceled()) {
								displaySuccessMessage(
										"Uploaded document!\nNote: the document will be ready after document processing on the server is finished - reload the document list occasionally");
							}
						} catch (Exception e) {
							throw new InvocationTargetException(e);
						}
					}
				}, "Uploading via HTTPS", true);
			} else if (ud.isMetsUrlUpload()) {
				logger.debug("uploading title: " + ud.getTitle() + " to collection: " + cId);
				//test url: http://rosdok.uni-rostock.de/file/rosdok_document_0000007322/rosdok_derivate_0000026952/ppn778418405.dv.mets.xml
				int h = DialogUtil.showInfoMessageBox(getShell(), "Upload Information",
						"Upload document!\nNote: the document will be ready after document processing on the server is finished - this takes a while - reload the document list occasionally");
				try {
					storage.uploadDocumentFromMetsUrl(cId, ud.getMetsUrl());
				} catch (ClientErrorException e) {
					if (e.getMessage().contains("DFG-Viewer Standard")) {
						onError("Error during uploading from Mets URL - reason: ", e.getMessage(), e);
					} else {
						throw e;
					}

				}
//				catch (SessionExpiredException | ServerErrorException eo) {
//				// TODO Auto-generated catch block
//				throw eo;
//			}
				// extract images from pdf and upload extracted images
			} else if (ud.isUploadFromPdf()) {
				logger.debug("extracting images from pdf " + ud.getFile() + " to local folder " + ud.getPdfFolder());
				logger.debug("ingest into collection: " + cId);
				
				ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
					@Override public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							storage.uploadDocumentFromPdf(cId, ud.getFile(), ud.getPdfFolder(), monitor);
							if (!monitor.isCanceled())
								displaySuccessMessage(
										"Uploaded document!\nNote: the document will be ready after document processing on the server is finished"
										+ " - reload the document list occasionally");
						} catch (Exception e) {
							throw new InvocationTargetException(e);
						}
					}
				}, "Uploading PDF via HTTPS", true);

			} else { // private ftp ingest
				final List<TrpDocDir> dirs = ud.getDocDirs();
				if (dirs == null || dirs.isEmpty()) {
					//should not happen. check is already done in Dialog...
					throw new Exception("DocDir list is empty!");
				}

				DialogUtil.createAndShowBalloonToolTip(getShell(), SWT.ICON_INFORMATION, "FTP Upload",
						"The FTP upload runs as background process and takes a while.\n"
								+ "Look into Jobs tab!\n"
								+ "Reloading the collection shows the already uploaded documents.",
						2, true);
				for (final TrpDocDir d : dirs) {
					try {
						storage.uploadDocumentFromPrivateFtp(cId, d.getName(), true);
					} catch (final ClientErrorException ie) {

						if (ie.getResponse().getStatus() == 409) { // conflict! (= duplicate name)
							if (DialogUtil.showYesNoDialog(getShell(), "Duplicate title", ie.getMessage() + "\n\nIngest anyway?") == SWT.YES) {
								storage.uploadDocumentFromPrivateFtp(cId, d.getName(), false);
							}
						}
					}
				}

				storage.sendJobListUpdateEvent();

				
//				ui.selectJobListTab();

//				ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
//					@Override public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
//						try {
//							monitor.beginTask("Starting jobs", dirs.size());
//							int i = 0;
//							for(final TrpDocDir d : dirs) {
//								if(monitor.isCanceled())
//									break;
//								
////								String docTitle = d.getMetadata()==null ? d.getName() : d.getMetadata().getTitle();
//								try {
//									storage.uploadDocumentFromPrivateFtp(cId, d.getName(), true);
//								} catch (final ClientErrorException ie) {
//									
////									if (ie.getResponse().getStatus() == 409) { // conflict! (= duplicate name)
////										Display.getDefault().syncExec(new Runnable() {
////											@Override public void run() {
////												if (DialogUtil.showYesNoDialog(getShell(), "Duplicate title", ie.getMessage()+"\n\nIngest anyway?") == SWT.YES) {
////													storage.uploadDocumentFromPrivateFtp(cId, d.getName(), false);
////												}												
////											}
////										});
////										
////
////									}
//								}
//
//								monitor.worked(++i);
//							}
//							
//							if (!monitor.isCanceled()) {
//								displaySuccessMessage("Ingest jobs started!\nNote: the documents will be ready after document processing on the server is finished - reload the document list occasionally");
//							}
//							monitor.done();
//						} catch (Exception e) {
//							throw new InvocationTargetException(e);
//						}
//					}
//				}, "Ingesting", true);
			}
		} catch (Throwable e) {
			onError("Error loading uploading document", "Could not upload document", e);
		}
	}

	public void enable(boolean value) {
		canvas.setEnabled(value);
		ui.getTranscriptionComposite().setEnabled(value);
//		ui.getRightTabFolder().setEnabled(value);
	}

	public static void main(String[] args) throws IOException {
		// TEST:
		// TrpGui.getMD5sOfLibs();

		// Dynamically load the correct swt jar depending on OS:

		TrpMainWidget.show();
		
		// TrpMainWidget mainWidget = new TrpMainWidget();
	}

//	@Deprecated public void deleteSelectedDocument() {
//		final TrpDocMetadata doc = ui.getServerWidget().getSelectedDocument();
//		try {
//			if (doc == null || !storage.isLoggedIn()) {
//				return;
//			}
//
//			if (DialogUtil.showYesNoDialog(getShell(), "Are you sure?", "Do you really want to delete document " + doc.getDocId()) != SWT.YES) {
//				return;
//			}
//
//			canvas.getScene().selectObject(null, true, false); // security
//																// measure due
//																// to mysterios
//																// bug leading
//																// to freeze of
//																// progress
//																// dialog
//			final int colId = storage.getCurrentDocumentCollectionId();
//			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
//				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
//					try {
//						logger.debug("deleting document...");
//						monitor.beginTask("Deleting document ", IProgressMonitor.UNKNOWN);
//						logger.debug("Deleting selected document: " + doc);
//						storage.deleteDocument(colId, doc.getDocId());
//						displaySuccessMessage("Deleted document " + doc.getDocId());
//					} catch (Exception e) {
//						throw new InvocationTargetException(e, e.getMessage());
//					}
//				}
//			}, "Exporting", false);
//
//			reloadDocList(ui.getServerWidget().getSelectedCollection());
//		} catch (Throwable e) {
//			onError("Error deleting document", "Could not delete document " + doc.getDocId(), e);
//		}
//	}

	public void displaySuccessMessage(final String message) {
		display.syncExec(new Runnable() {
			@Override public void run() {
				DialogUtil.showInfoMessageBox(getShell(), "Success", message);
			}
		});
	}

	public void displayCancelMessage(final String message) {
		display.syncExec(new Runnable() {
			@Override public void run() {
				DialogUtil.showInfoMessageBox(getShell(), "Cancel", message);
			}
		});
	}
	
	public void addPage(){		
		
		
		if(storage.getDoc() == null){
			DialogUtil.showErrorMessageBox(getShell(), "No remote document loaded", "No remote document loaded");
			return;
		}
		
		//FIXME where to handle which file extensions are allowed?
		final String[] extArr = new String[] { "*.jpg", "*.jpeg", "*.tiff", "*.tif", "*.TIF", "*.TIFF", "*.png" };
		
		String filePath = DialogUtil.showOpenFileDialog(mw.getShell(), "Add page", null, extArr);
		logger.debug("Uploading new page from: " + filePath);
		if(filePath == null){
			logger.error("ERROR: Bad filepath");
			return;
		}
		File imgFile = new File(filePath);
		
		logger.debug(Long.toString(imgFile.length()));
		//Set new pageNr
		int pageNr = storage.getNPages()+1;
		int docId = storage.getDocId();
		int colId = storage.getCollId();	
		
		
		try {			
			if (!imgFile.canRead())
				throw new Exception("Can't read file at: " + filePath);
			ProgressBarDialog.open(mw.getShell(), new IRunnableWithProgress(){

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					
					try {
						monitor.beginTask("Uploading image file...colId " + colId + " docId " + docId + " pageNr " + pageNr, 120);
						Storage.getInstance().addPage(colId, docId, pageNr, imgFile, monitor);
					} catch (NoConnectionException e) {
						logger.error(e.toString());
					}					
				}				
			}, "Upload", false);			


			reloadCurrentDocument();

			
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		

	}
	
	public void addSeveralPages2Doc() {
		logger.debug("Open Dialog for adding images");

		//FIXME where to handle which file extensions are allowed?
		final String[] extArr = new String[] { "*.jpg", "*.jpeg", "*.tiff", "*.tif", "*.TIF", "*.TIFF", "*.png" };
		final ArrayList<String> imgNames = DialogUtil.showOpenFilesDialog(getShell(), "Select image files to add", null, extArr);
		if (imgNames == null)
			return;

		try {
			int pageNr = storage.getNPages();
			//check img file
			for (String img : imgNames){
				final File imgFile = new File(img);
				
				pageNr += 1;
				int pageNumber = pageNr;
				int docId = storage.getDocId();
				int colId = storage.getCollId();
				
				if (!imgFile.canRead())
					throw new Exception("Can't read file at: " + img);
				
				ProgressBarDialog.open(mw.getShell(), new IRunnableWithProgress(){

					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						
						try {
							monitor.beginTask("Uploading image file...colId " + colId + " docId " + docId + " pageNr " + pageNumber, 120);
							Storage.getInstance().addPage(colId, docId, pageNumber, imgFile, monitor);
						} catch (NoConnectionException e) {
							logger.error(e.toString());
						}					
					}				
				}, "Upload", false);			


				reloadCurrentDocument();
			}
			
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	public void deletePage() {
		logger.debug("Open Dialog for deleting page");
		if (!storage.isPageLoaded() || !storage.isRemoteDoc()) {
			DialogUtil.showErrorMessageBox(getShell(), "No remote page loaded", "No remote page loaded");
			return;
		}

		if (DialogUtil.showYesNoDialog(getShell(), "", "Do you really want to delete the current page?") != SWT.YES)
			return;

		try {
			//during deleting a page we don't care if it was edited before
			if (storage.isTranscriptEdited()) {
				storage.getTranscript().getPage().setEdited(false);
			}
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						logger.debug("Delete page: " + storage.getPage().getPageNr());

						monitor.beginTask("Deleting page " + storage.getPage().getPageNr(), 1);

						//replace on server
						storage.deleteCurrentPage();

						monitor.worked(1);
						monitor.done();
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Replacing...", true);
			//reload page
			this.reloadCurrentDocument();
		} catch (Throwable e) {
			onError("Error replacing page image", e.getMessage(), e);
		}

	}

	public void replacePageImg() {
		logger.debug("Open Dialog for replacing image");
		if (!storage.isPageLoaded() || !storage.isRemoteDoc()) {
			DialogUtil.showErrorMessageBox(getShell(), "No document loaded", "No document loaded!");
			return;
		}

		//FIXME where to handle which file extensions are allowed?
		final String[] extArr = new String[] { "*.jpg", "*.jpeg", "*.tiff", "*.tif", "*.TIF", "*.TIFF", "*.png" };
		final String fn = DialogUtil.showOpenFileDialog(getShell(), "Select image file", null, extArr);
		if (fn == null)
			return;

		try {
			//check img file
			final File imgFile = new File(fn);
			if (!imgFile.canRead())
				throw new Exception("Can't read file at: " + fn);

			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						logger.debug("Replacing file: " + fn);
						monitor.beginTask("Uploading image file", 120);
						//replace on server
						storage.replacePageImgFile(imgFile, monitor);

						for (int i = 1; i <= 2; i++) {
							Thread.sleep(1000);
							monitor.worked(100 + (i * 10));
						}
						monitor.done();
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Replacing...", true);
			//reload page
			this.reloadCurrentPage(false);
		} catch (Throwable e) {
			onError("Error replacing page image", e.getMessage(), e);
		}
	}

	/**
	 * FIXME <br/>
	 * this is one monster method!<br/>
	 * export-parameter-objects can be used instead of single parameters<br/>
	 * progress bar does not work after transcripts are loaded
	 * 
	 */
	public void unifiedExport() {
		File dir = null;
		String exportFileOrDir = "";
		String exportFormats = "";
		try {

			if (!storage.isDocLoaded()) {
				DialogUtil.showErrorMessageBox(getShell(), "No document loaded", "You first have to open a document that shall be exported!");
				return;
			}

			/*
			 * preselect document title for export folder name filter out all
			 * unwanted chars
			 */
			boolean isLocalDoc = storage.isLocalDoc();
			String title = isLocalDoc ? storage.getDoc().getMd().getLocalFolder().getName() : storage.getDoc().getMd().getTitle();
			String adjTitle = ExportUtils.getAdjustedDocTitle(title);

			saveTranscriptDialogOrAutosave();

			String lastExportFolderTmp = TrpGuiPrefs.getLastExportFolder();
			if (lastExportFolderTmp != null && !lastExportFolderTmp.equals("")) {
				lastExportFolder = lastExportFolderTmp;
			}
			final CommonExportDialog exportDiag = new CommonExportDialog(getShell(), SWT.NONE, lastExportFolder, adjTitle, storage.getDoc().getPages());
			
			dir = exportDiag.open();
			if (dir == null){
				return;
			}
			
			/*
			 * if we do not export the latest version
			 * -> reload the doc with all available transcripts to allow export of specific versions
			 * param -1
			 */
			if (!exportDiag.getVersionStatus().contains("Latest")){
				storage.reloadDocWithAllTranscripts();
			}
			
			String pages = exportDiag.getPagesStr();
			Set<Integer> pageIndices = exportDiag.getPageIndices();
			
			CommonExportPars commonPars = exportDiag.getCommonExportPars();
			TeiExportPars teiPars =  exportDiag.getTeiExportPars();
			PdfExportPars pdfPars = exportDiag.getPdfPars();
			DocxExportPars docxPars = exportDiag.getDocxPars();
			AltoExportPars altoPars = exportDiag.getAltoPars();

			if (exportDiag.isDoServerExport()) {
				String jobId;
				if (exportDiag.isExportCurrentDocOnServer()) {
					logger.debug("server export, collId = "+storage.getCollId()+", docId = "+storage.getDocId()+", commonPars = "+commonPars+", teiPars = "+teiPars+", pdfPars = "+pdfPars+", docxPars = "+docxPars+", altoPars = "+altoPars);
					jobId = storage.getConnection().exportDocument(storage.getCollId(), storage.getDocId(), 
												commonPars, altoPars, pdfPars, teiPars, docxPars);
				} else {
					commonPars.setPages(null); // delete pagesStr for multiple document export!
					
					logger.debug("server collection export, collId = "+storage.getCollId()+" dsds = "+CoreUtils.toListString(exportDiag.getDocumentsToExportOnServer()));
					logger.debug("commonPars = "+commonPars+", teiPars = "+teiPars+", pdfPars = "+pdfPars+", docxPars = "+docxPars+", altoPars = "+altoPars);
					
					jobId = storage.getConnection().exportDocuments(storage.getCollId(), exportDiag.getDocumentsToExportOnServer(), 
							commonPars, altoPars, pdfPars, teiPars, docxPars);
				}

				if (jobId != null) {
					logger.debug("started job with id = "+jobId);
								
//					mw.registerJobToUpdate(jobId); // do not register job as you get an email anyway...
					
					storage.sendJobListUpdateEvent();
					mw.updatePageLock();
					
					DialogUtil.showInfoMessageBox(mw.getShell(), "Export Job started", "Started export job with id = "+jobId+"\n After it is finished, you will receive a download link via mail");
				}
				return;
			}
			
			logger.debug("after server export");

			if (!dir.exists()) {
				dir.mkdir();
			}
			
			exportFileOrDir = dir.getAbsolutePath();
			boolean doZipExport = false;

			boolean doMetsExport = false;
			boolean doPdfExport = false;
			boolean doDocxExport = false;
			boolean doTxtExport = false;
			/*
			 * tei export only available as server export because it is implemented as xslt transformation page -> tei
			 */
			//boolean doTeiExport = false;
			boolean doXlsxExport = false;
			boolean doTableExport = false;

			String tempDir = null;

			String metsExportDirString = dir.getAbsolutePath() + "/" + dir.getName();
			File metsExportDir = new File(metsExportDirString);

			String pdfExportFileOrDir = dir.getAbsolutePath() + "/" + dir.getName() + ".pdf";
			File pdfExportFile = new File(pdfExportFileOrDir);

//			String teiExportFileOrDir = dir.getAbsolutePath() + "/" + dir.getName() + "_tei.xml";
//			File teiExportFile = new File(teiExportFileOrDir);

			String docxExportFileOrDir = dir.getAbsolutePath() + "/" + dir.getName() + ".docx";
			File docxExportFile = new File(docxExportFileOrDir);
			
			String txtExportFileOrDir = dir.getAbsolutePath() + "/" + dir.getName() + ".txt";
			File txtExportFile = new File(txtExportFileOrDir);

			String xlsxExportFileOrDir = dir.getAbsolutePath() + "/" + dir.getName() + ".xlsx";
			File xlsxExportFile = new File(xlsxExportFileOrDir);
			
			String tableExportFileOrDir = dir.getAbsolutePath() + "/" + dir.getName() + "_tables.xlsx";
			File tableExportFile = new File(tableExportFileOrDir);

			String zipExportFileOrDir = dir.getAbsolutePath() + "/" + dir.getName() + ".zip";
			File zipExportFile = new File(zipExportFileOrDir);

			/*
			 * only check export path if it is not ZIP because than we check just the ZIP location
			 */
			if (!exportDiag.isZipExport()) {
				doMetsExport = (exportDiag.isMetsExport() && exportDiag.getExportPathComp().checkExportFile(metsExportDir, null, getShell()));

				doPdfExport = (exportDiag.isPdfExport() && exportDiag.getExportPathComp().checkExportFile(pdfExportFile, ".pdf", getShell()));

				//doTeiExport = (exportDiag.isTeiExport() && exportDiag.getExportPathComp().checkExportFile(teiExportFile, ".xml", getShell()));

				doDocxExport = (exportDiag.isDocxExport() && exportDiag.getExportPathComp().checkExportFile(docxExportFile, ".docx", getShell()));
				
				doTxtExport = (exportDiag.isTxtExport() && exportDiag.getExportPathComp().checkExportFile(txtExportFile, ".txt", getShell()));

				doXlsxExport = (exportDiag.isTagXlsxExport() && exportDiag.getExportPathComp().checkExportFile(xlsxExportFile, ".xlsx", getShell()));
				
				doTableExport = (exportDiag.isTableXlsxExport() && exportDiag.getExportPathComp().checkExportFile(tableExportFile, ".xlsx", getShell()));
			}

			doZipExport = (exportDiag.isZipExport() && exportDiag.getExportPathComp().checkExportFile(zipExportFile, ".zip", getShell()));

			if (doZipExport) {
				tempDir = System.getProperty("java.io.tmpdir");
				//logger.debug("temp dir is ..." + tempDir);
			}

			if (!doMetsExport && !doPdfExport && !doDocxExport && !doTxtExport && !doXlsxExport && !doZipExport && !doTableExport) {
				/*
				 * if the export file exists and the user wants not to overwrite it then the 
				 * export dialog shows up again with the possibility to choose another location
				 * --> comment out if export should close instead
				 */
				unifiedExport();
				return;
			}

			if (exportDiag.isPageableExport() && pageIndices == null) {
				DialogUtil.showErrorMessageBox(getShell(), "Error parsing page ranges", "Error parsing page ranges");
				return;
			}

//			logger.debug("loading transcripts..." + copyOfPageIndices.size());
			
			ExportCache cache = new ExportCache();
			cache.setSelectedTags(exportDiag.getSelectedTagsList());
			
			if (!commonPars.exportImagesOnly()){

				ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
					@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
	
							//logger.debug("loading transcripts...");
							monitor.beginTask("Loading transcripts...", pageIndices.size());						
						
							//unmarshal the page transcript only once for all different export, don't do this if only images are exported
							cache.storePageTranscripts4Export(storage.getDoc(), pageIndices, monitor, exportDiag.getVersionStatus(),
									storage.getPageIndex(), storage.getTranscript().getMd());
							
							monitor.done();
	
						} catch (Exception e) {
							throw new InvocationTargetException(e, e.getMessage());
						}
					}
				}, "Loading of transcripts: ", true);
	
				logger.debug("transcripts loaded");
	
				if (exportDiag.isTagableExportChosen()) {	
					logger.debug("loading tags..." + cache.getSelectedTags().size());
	
					ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
						@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							try {
								//logger.debug("loading transcripts...");
								monitor.beginTask("Loading tags...", pageIndices.size());
								cache.storeCustomTagMapForDoc(storage.getDoc(), exportDiag.isWordBased(), pageIndices, monitor,
										exportDiag.isDoBlackening());
								
								if (cache.getSelectedTags() == null) {
									DialogUtil.showErrorMessageBox(getShell(), "Error while reading selected tag names", "Error while reading selected tag names");
									return;
								}
								monitor.done();
							} catch (Exception e) {
								throw new InvocationTargetException(e, e.getMessage());
							}
						}
					}, "Loading of tags: ", true);
	
					logger.debug("tags loaded");
	
				}
			
			}

			boolean wordBased = exportDiag.isWordBased();
			boolean doBlackening = exportDiag.isDoBlackening();
			boolean createTitle = exportDiag.isCreateTitlePage();
			
			if (doZipExport) {

				if (tempDir == null)
					return;

				String tempZipDirParent = tempDir + "/" + dir.getName();
				File tempZipDirParentFile = new File(tempZipDirParent);

				if (tempZipDirParentFile.exists()) {
					Random randomGenerator = new Random();
					int randomInt = randomGenerator.nextInt(1000);
					tempZipDirParent = tempZipDirParent.concat(Integer.toString(randomInt));
					tempZipDirParentFile = new File(tempZipDirParent);
				}

				String tempZipDir = tempZipDirParent + "/" + dir.getName();
				File tempZipFileDir = new File(tempZipDir);
				FileUtils.forceMkdir(tempZipFileDir);

				if (exportDiag.isMetsExport())
					exportDocument(tempZipFileDir, pageIndices, exportDiag.isImgExport(), exportDiag.isPageExport(), exportDiag.isAltoExport(),
							exportDiag.isSplitUpWords(), commonPars.getFileNamePattern(), commonPars.getRemoteImgQuality(), cache);
				if (exportDiag.isPdfExport())
					exportPdf(new File(tempZipDirParent + "/" + dir.getName() + ".pdf"), pageIndices, exportDiag.isAddExtraTextPages2PDF(),
							exportDiag.isExportImagesOnly(), exportDiag.isHighlightTags(), wordBased, doBlackening, createTitle, cache, exportDiag.getFont(), pdfPars.getPdfImgQuality());
				if (exportDiag.isTeiExport())
					exportTei(new File(tempZipDirParent + "/" + dir.getName() + ".xml"), exportDiag, cache);
				if (exportDiag.isDocxExport())
					exportDocx(new File(tempZipDirParent + "/" + dir.getName() + ".docx"), pageIndices, wordBased, exportDiag.isDocxTagExport(), doBlackening,
							createTitle, exportDiag.isMarkUnclearWords(), exportDiag.isExpandAbbrevs(), exportDiag.isSubstituteAbbreviations(),
							exportDiag.isPreserveLinebreaks(), exportDiag.isForcePagebreaks(), exportDiag.isShowSuppliedWithBrackets(), exportDiag.isIgnoreSupplied(), cache);
				if (exportDiag.isTxtExport())
					exportTxt(new File(tempZipDirParent + "/" + dir.getName() + ".txt"), pageIndices, createTitle, exportDiag.isWordBased(), true, cache);
				if (exportDiag.isTagXlsxExport())
					exportXlsx(new File(tempZipDirParent + "/" + dir.getName() + ".xlsx"), pageIndices, exportDiag.isWordBased(), exportDiag.isDocxTagExport(), cache);
				if (exportDiag.isTableXlsxExport())
					exportTableXlsx(new File(tempZipDirParent + "/" + dir.getName() + "_tables.xlsx"), pageIndices, cache);

				//createZipFromFolder(tempZipDirParentFile.getAbsolutePath(), dir.getParentFile().getAbsolutePath() + "/" + dir.getName() + ".zip");
				ZipUtils.createZipFromFolder(tempZipDirParentFile.getAbsolutePath(), zipExportFile.getAbsolutePath(), false);

				if (exportFormats != "") {
					exportFormats += " and ";
				}
				exportFormats += "ZIP";

				for (File f : tempZipDirParentFile.listFiles()) {
					f.delete();
				}

				lastExportFolder = dir.getParentFile().getAbsolutePath();
				logger.debug("last export folder: " + lastExportFolder);

				TrpGuiPrefs.storeLastExportFolder(lastExportFolder);

				//delete the temp folder for making the ZIP
				FileDeleteStrategy.FORCE.delete(tempZipDirParentFile);

				if (exportFormats != "") {
					displaySuccessMessage("Sucessfully written " + exportFormats + " to " + exportFileOrDir);
				}

				//export was done via ZIP and is completed now
				return;

			}

			if (doMetsExport) {

				exportDocument(metsExportDir, pageIndices, exportDiag.isImgExport(), exportDiag.isPageExport(), exportDiag.isAltoExport(),
						exportDiag.isSplitUpWords(), commonPars.getFileNamePattern(), commonPars.getRemoteImgQuality(), cache);
				if (exportDiag.isPageExport()) {
					if (exportFormats != "") {
						exportFormats += " and ";
					}
					exportFormats += "METS/PAGE";
				}

				if (exportDiag.isAltoExport()) {
					if (exportFormats != "") {
						exportFormats += " and ";
					}
					exportFormats += "METS/ALTO";
				}
				
				if (exportDiag.isImgExport()){
					if (exportFormats != "") {
						exportFormats += " and ";
					}
					exportFormats += "IMAGES";
				}

			}

			if (doPdfExport) {

				exportPdf(pdfExportFile, pageIndices, exportDiag.isAddExtraTextPages2PDF(), exportDiag.isExportImagesOnly(), 
						exportDiag.isHighlightTags(), wordBased, doBlackening, createTitle, cache, exportDiag.getFont(), pdfPars.getPdfImgQuality());
				if (exportFormats != "") {
					exportFormats += " and ";
				}
				exportFormats += "PDF";

			}

//			if (doTeiExport) {
//
//				exportTei(teiExportFile, exportDiag, cache);
//				if (exportFormats != "") {
//					exportFormats += " and ";
//				}
//				exportFormats += "TEI";
//
//			}

			if (doDocxExport) {

				exportDocx(docxExportFile, pageIndices, wordBased, exportDiag.isDocxTagExport(), doBlackening, createTitle,
						exportDiag.isMarkUnclearWords(), exportDiag.isExpandAbbrevs(), exportDiag.isSubstituteAbbreviations(),
						exportDiag.isPreserveLinebreaks(), exportDiag.isForcePagebreaks(), exportDiag.isShowSuppliedWithBrackets(), 
						exportDiag.isIgnoreSupplied(), cache);
				if (exportFormats != "") {
					exportFormats += " and ";
				}
				exportFormats += "DOCX";

			}
			
			if (doTxtExport) {

				//last param keeps the line breaks by default 
				exportTxt(txtExportFile, pageIndices, createTitle, wordBased, true, cache);
				if (exportFormats != "") {
					exportFormats += " and ";
				}
				exportFormats += "TXT";

			}

			if (doXlsxExport) {

				if (exportXlsx(xlsxExportFile, pageIndices, exportDiag.isWordBased(), exportDiag.isDocxTagExport(), cache)){
					if (exportFormats != "") {
						exportFormats += " and ";
					}
					exportFormats += "XLSX";
				}

			}
			
			if (doTableExport) {

				exportTableXlsx(tableExportFile, pageIndices, cache);
				if (exportFormats != "") {
					exportFormats += " and ";
				}
				exportFormats += "TABLES_XLSX";

			}



		} catch (Throwable e) {
			if (e instanceof InterruptedException) {
				DialogUtil.showInfoMessageBox(getShell(), "Export canceled", "Export was canceled");
			}
			else if (e instanceof SessionExpiredException) {
				sessionExpired = true;
				logout(true, false);
				logger.warn("Session expired!");
				loginDialog("Session expired!");
				//unifiedExport();
			} else {
				onError("Export error", e.getMessage(), e);
			}
		} finally {
			
			if (exportFormats != "") {
				displaySuccessMessage("Sucessfully written " + exportFormats + " to " + exportFileOrDir);
			}
						
			if (dir != null) {
				lastExportFolder = dir.getParentFile().getAbsolutePath();
				TrpGuiPrefs.storeLastExportFolder(lastExportFolder);
			}
		}

	}

	public void exportDocument(final File dir, final Set<Integer> pageIndices, final boolean exportImg, final boolean exportPage, final boolean exportAlto,
			final boolean splitIntoWordsInAlto, final String fileNamePattern, final ImgType imgType, ExportCache cache) throws Throwable {
		try {

			if (dir == null)
				return;

			String what = "Images" + (exportPage ? ", PAGE" : "") + (exportAlto ? ", ALTO" : "");
			lastExportFolder = dir.getParentFile().getAbsolutePath();
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						logger.debug("exporting document...");
						final String path = storage.exportDocument(dir, pageIndices, exportImg, exportPage, exportAlto, splitIntoWordsInAlto, fileNamePattern,
								imgType, monitor, cache);
						monitor.done();
						// displaySuccessMessage("Written export to "+path);
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Exporting document files: " + what, false);
		} catch (Throwable e) {
			onError("Export error", "Error during export of document", e);
			throw e;
		}
	}

	// public void exportRtf() {
	// try {
	// if (!storage.isDocLoaded()) {
	// DialogUtil.showErrorMessageBox(getShell(), "No document loaded",
	// "You first have to open a document!");
	// return;
	// }
	//
	// String adjTitle = getAdjustedDocTitle();
	//
	// logger.debug("lastExportRtfFn = "+lastExportRtfFn);
	// final RtfExportDialog exportDiag = new RtfExportDialog(
	// getShell(), SWT.NONE, lastExportRtfFn, storage.getDoc().getNPages(),
	// adjTitle
	// );
	// final File file = exportDiag.open();
	// if (file == null)
	// return;
	// final Integer startPage = exportDiag.getStartPage();
	// final Integer endPage = exportDiag.getEndPage();
	// final boolean isWordBased = exportDiag.isWordBased();
	//
	// logger.info("PDF export. pages " + startPage + "-" +
	// endPage+", isWordBased: "+isWordBased);
	//
	// lastExportRtfFn = file.getAbsolutePath();
	// ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
	// @Override public void run(IProgressMonitor monitor) throws
	// InvocationTargetException, InterruptedException {
	// try {
	// logger.debug("creating RTF document...");
	// TrpRtfBuilder.writeRtfForDoc(storage.getDoc(), isWordBased, file,
	// startPage, endPage, monitor);
	// monitor.done();
	// displaySuccessMessage("Written RTF file to "+lastExportRtfFn);
	// } catch (Exception e) {
	// throw new InvocationTargetException(e, e.getMessage());
	// }
	// }
	// }, "Exporting", true);
	// } catch (Throwable e) {
	// onError("Export error", "Error during RTF export of document", e);
	// }
	// }

	public void exportRtf(final File file, final Set<Integer> pageIndices, final boolean isWordBased, final boolean isTagExport, final boolean doBlackening,
			ExportCache cache) throws Throwable {
		try {

			if (file == null)
				return;

			logger.info("RTF export. pages " + pageIndices + ", isWordBased: " + isWordBased);

			lastExportFolder = file.getParentFile().getAbsolutePath();
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						logger.debug("creating RTF document...");
						TrpRtfBuilder.writeRtfForDoc(storage.getDoc(), isWordBased, isTagExport, doBlackening, file, pageIndices, monitor, cache);
						monitor.done();
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Exporting", true);
		} catch (Throwable e) {
			onError("Export error", "Error during RTF export of document", e);
			throw e;
		}
	}

	public void exportTxt(final File file, final Set<Integer> pageIndices, final boolean createTitle, final boolean isWordBased, final boolean preserveLineBreaks, ExportCache cache) throws Throwable {
		try {

			if (file == null)
				return;

			logger.info("Txt export. pages " + pageIndices + ", isWordBased: " + isWordBased);

			lastExportFolder = file.getParentFile().getAbsolutePath();
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						logger.debug("creating txt file...");
						TrpTxtBuilder txtBuilder = new TrpTxtBuilder();
						txtBuilder.writeTxtForDoc(storage.getDoc(), createTitle, isWordBased, preserveLineBreaks, file, pageIndices, monitor, cache);
						monitor.done();
					} catch (InterruptedException ie) {
						throw ie;
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Exporting", true);
		} catch (Throwable e) {
			if (!(e instanceof InterruptedException)) {
				onError("Export error", "Error during Txt export of document", e);
			}
			throw e;
		}
	}
	
	public void exportDocx(final File file, final Set<Integer> pageIndices, final boolean isWordBased, final boolean isTagExport, final boolean doBlackening,
			final boolean createTitle, final boolean markUnclearWords, final boolean expandAbbrevs,
			final boolean substituteAbbrevs, final boolean preserveLineBreaks, final boolean forcePageBreaks, final boolean suppliedWithBrackets, final boolean ignoreSupplied, ExportCache cache) throws Throwable {
		try {

			if (file == null)
				return;

			logger.info("Docx export. pages " + pageIndices + ", isWordBased: " + isWordBased);

			lastExportFolder = file.getParentFile().getAbsolutePath();
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						logger.debug("creating Docx document...");
						DocxBuilder docxBuilder = new DocxBuilder();
						docxBuilder.writeDocxForDoc(storage.getDoc(), isWordBased, isTagExport, doBlackening, file, pageIndices, monitor,
								createTitle, markUnclearWords, expandAbbrevs, substituteAbbrevs, preserveLineBreaks, forcePageBreaks, suppliedWithBrackets, ignoreSupplied, cache);
						monitor.done();
					} catch (InterruptedException ie) {
						throw ie;
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Exporting", true);
		} catch (Throwable e) {
			if (!(e instanceof InterruptedException)) {
				onError("Export error", "Error during Docx export of document", e);
			}
			throw e;
		}
	}

	public boolean exportXlsx(final File file, final Set<Integer> pageIndices, final boolean isWordBased, final boolean isTagExport, ExportCache cache) throws Throwable {
		try {

			if (cache.getCustomTagMapForDoc().isEmpty()) {
				logger.info("No tags to store -> Xlsx export cancelled");
				displayCancelMessage("No custom tags in document to store -> Xlsx export cancelled");
				return false;
				//throw new Exception("No tags to store -> Xlsx export cancelled");
			}
			
			logger.debug("tags " + cache.getCustomTagMapForDoc().size());
			
			//logger.debug("lastExportXlsxFn = " + lastExportXlsxFn);

			if (file == null)
				return false;

			logger.info("Excel export. pages " + pageIndices + ", isWordBased: " + isWordBased);

			lastExportFolder = file.getParentFile().getAbsolutePath();
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						logger.debug("creating Excel document...");
						TrpXlsxBuilder xlsxBuilder = new TrpXlsxBuilder();
						xlsxBuilder.writeXlsxForDoc(storage.getDoc(), isWordBased, file, pageIndices, monitor, cache);
						monitor.done();
					} catch (InterruptedException ie) {
						throw ie;
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Exporting", true);
		} catch (Throwable e) {
			if (!(e instanceof InterruptedException))
				onError("Export error", "Error during Xlsx export of document", e);
			throw e;
		}
		return true;
	}
	
	public void exportTableXlsx(final File file, final Set<Integer> pageIndices, ExportCache cache) throws Throwable {
		try {

			if (file == null)
				return;

			logger.info("Excel table export. pages " + pageIndices);

			lastExportFolder = file.getParentFile().getAbsolutePath();
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						logger.debug("creating Excel document...");
						TrpXlsxTableBuilder xlsxTableBuilder = new TrpXlsxTableBuilder();
						xlsxTableBuilder.writeXlsxForTables(storage.getDoc(), file, pageIndices, monitor, cache);
						monitor.done();
					} catch (InterruptedException ie) {
						throw ie;
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Exporting", true);
		} catch (Throwable e) {
			if (!(e instanceof InterruptedException))
				onError("Export error", "Error during Xlsx export of document", e);
			throw e;
		}
	}

	// public void exportPdf() {
	// try {
	// if (!storage.isDocLoaded()) {
	// DialogUtil.showErrorMessageBox(getShell(), "No document loaded",
	// "You first have to open a document that shall be exported as PDF!");
	// return;
	// }
	//
	// String adjTitle = getAdjustedDocTitle();
	//
	// final PdfExportDialog exportDiag = new PdfExportDialog(
	// getShell(), SWT.NONE, lastExportPdfFn, storage.getDoc().getNPages(),
	// adjTitle
	// );
	// final File dir = exportDiag.open();
	// if (dir == null)
	// return;
	// final Integer startPage = exportDiag.getStartPage();
	// final Integer endPage = exportDiag.getEndPage();
	//
	// logger.info("PDF export. pages " + startPage + "-" + endPage);
	//
	// lastExportPdfFn = dir.getAbsolutePath();
	// ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
	// @Override public void run(IProgressMonitor monitor) throws
	// InvocationTargetException, InterruptedException {
	// try {
	// logger.debug("creating PDF document...");
	// int totalWork = endPage+1 - startPage;
	// monitor.beginTask("Creating PDF document" , totalWork);
	//
	// final String path = storage.exportPdf(dir, startPage, endPage, monitor);
	// monitor.done();
	// displaySuccessMessage("Written PDF file to "+path);
	// } catch (Exception e) {
	// throw new InvocationTargetException(e, e.getMessage());
	// }
	// }
	// }, "Exporting", false);
	// } catch (Throwable e) {
	// onError("Export error", "Error during export of document", e);
	// }
	// }

	public void exportPdf(final File dir, final Set<Integer> pageIndices, final boolean extraTextPages, final boolean imagesOnly,
			final boolean highlightTags, final boolean wordBased, final boolean doBlackening, final boolean createTitle, ExportCache cache, final String exportFontname, final ImgType imgType)
			throws Throwable {
		try {
			if (dir == null)
				return;

			// logger.info("PDF export. pages " + startPage + "-" + endPage);
			// logger.info("PDF dir " + dir.getAbsolutePath());
			// logger.info("PDF parent dir " +
			// dir.getParentFile().getAbsolutePath());

			lastExportFolder = dir.getParentFile().getAbsolutePath();
			final Shell shell = getShell();
			ProgressBarDialog.open(shell, new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						storage.exportPdf(dir, pageIndices, monitor, extraTextPages, imagesOnly, cache.getSelectedTags(), highlightTags, wordBased, doBlackening, createTitle, cache, exportFontname, imgType);
						monitor.done();
					} catch (InterruptedException ie) {
						throw ie;
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Exporting", true);
		} catch (Throwable e) {
			if (!(e instanceof InterruptedException))
				onError("Export error", "Error during export of document", e);
			throw e;
		}
	}

	// public void exportTei() {
	// try {
	// if (!storage.isDocLoaded()) {
	// DialogUtil.showErrorMessageBox(getShell(), "No document loaded",
	// "You first have to open a document that shall be exported as PDF!");
	// return;
	// }
	//
	// String adjTitle = getAdjustedDocTitle();
	//
	// final TeiExportDialog exportDiag = new TeiExportDialog(
	// getShell(), SWT.NONE, lastExportTeiFn, storage.getDoc().getNPages(),
	// adjTitle
	// );
	// final File dir = exportDiag.open();
	// if (dir == null)
	// return;
	// final int mode = exportDiag.getMode();
	// logger.info("TEI export. Mode = " + mode);
	//
	// lastExportTeiFn = dir.getAbsolutePath();
	// ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
	// @Override public void run(IProgressMonitor monitor) throws
	// InvocationTargetException, InterruptedException {
	// try {
	// logger.debug("creating TEI document...");
	// monitor.beginTask("Creating TEI document" , IProgressMonitor.UNKNOWN);
	//
	// final String path = storage.exportTei(dir, mode);
	// monitor.done();
	// displaySuccessMessage("Written TEI file to "+path);
	// } catch (Exception e) {
	// throw new InvocationTargetException(e, e.getMessage());
	// }
	// }
	// }, "Exporting", false);
	// } catch (Throwable e) {
	// onError("Export error", "Error during export of document", e);
	// }
	// }

	public void exportTei(final File file, final CommonExportDialog exportDiag, ExportCache cache) throws Throwable {
		try {
			TeiExportPars pars = exportDiag.getTeiExportPars();
			CommonExportPars commonPars = exportDiag.getCommonExportPars();

			if (file == null)
				return;

			logger.info("TEI export, pars = "+pars+" commonPars = "+commonPars);

			lastExportFolder = file.getParentFile().getAbsolutePath();
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						logger.debug("creating TEI document, pars: " + pars);

						storage.exportTei(file, commonPars, pars, monitor, cache);
						monitor.done();
					} catch (InterruptedException ie) {
						throw ie;
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Exporting", true);
		} catch (Throwable e) {
			if (!(e instanceof InterruptedException))
				onError("Export error", "Error during export of TEI", e);
			throw e;
		}
	}

	public void exportAlto(final File file) throws Throwable {
		try {

			if (file == null)
				return;

			logger.info("Alto export.");

			lastExportFolder = file.getParentFile().getAbsolutePath();
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						logger.debug("creating Alto document...");
						monitor.beginTask("Creating Alto document", IProgressMonitor.UNKNOWN);

						storage.exportAlto(file, monitor);
						monitor.done();
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Exporting", false);
		} catch (Throwable e) {
			onError("Export error", "Error during export of Alto", e);
			throw e;
		}
	}

	public void sendBugReport() {
		try {

			if(bugDialog == null){
				bugDialog = new BugDialog(getShell(), SWT.NONE);
				bugDialog.open();
			}else{
				bugDialog.setActive();
			}
			

		} catch (Throwable e) {
			onError("Fatal bug report error", "Fatal error sending bug report / feature request", e);
		}
	}

	public void selectTranscriptionWidgetOnSelectedShape(ICanvasShape selected) {
		if (selected == null || selected.getData() == null)
			return;

		if (selected.getData() instanceof TrpWordType) {
			ui.changeToTranscriptionWidget(TranscriptionLevel.WORD_BASED);
		} else if (selected.getData() instanceof TrpTextLineType || selected.getData() instanceof TrpBaselineType) {
			ui.changeToTranscriptionWidget(TranscriptionLevel.LINE_BASED);
		}
	}

	public void checkForUpdates() {
		ProgramUpdaterDialog.checkForUpdatesDialog(ui.getShell(), VERSION, info.getTimestamp(), false, false);
	}

	public void installSpecificVersion() {
		InstallSpecificVersionDialog d = new InstallSpecificVersionDialog(ui.getShell(), SWT.NONE);
		int answer = d.open();
		if (answer == 0 || answer == 1) {
			ProgramPackageFile f = d.getSelectedFile();
			boolean downloadAll = d.isDownloadAll();
			logger.debug("installing selected file: " + f + " downloadAll: " + downloadAll);
			if (f == null)
				return;

			boolean keepConfigFiles = answer == 0;
			boolean isNewVersion = !f.getVersion().equals(VERSION);

			try {
				ProgramUpdaterDialog.downloadAndInstall(ui.getShell(), f, isNewVersion, keepConfigFiles, downloadAll);
			} catch (InterruptedException ie) {
				logger.debug("Interrupted: " + ie.getMessage());
			} catch (IOException e) {
				if (!e.getMessage().equals("stream is closed")) {
					TrpMainWidget.getInstance().onError("IO-Error during update", "Error during update: \n\n" + e.getMessage(), e);
				}
			} catch (Throwable e) {
				TrpMainWidget.getInstance().onError("Error during update", "Error during update: \n\n" + e.getMessage(), e);
			} finally {
				if (!ProgramUpdaterDialog.TEST_ONLY_DOWNLOAD)
					ProgramUpdaterDialog.removeUpdateZip();
			}
		}

	}

	public void analyzePageStructure(final boolean detectPageNumbers, final boolean detectRunningTitles, final boolean detectFootnotes) {
		try {
			if (!storage.isPageLoaded()) {
				DialogUtil.showErrorMessageBox(getShell(), "Analyze Page Structure", "No page loaded!");
				return;
			}
			if (!storage.isLoggedIn()) {
				DialogUtil.showErrorMessageBox(getShell(), "Analyze Page Structure", "You are not logged in!");
				return;
			}

			final int colId = storage.getCurrentDocumentCollectionId();
			final int docId = storage.getDoc().getId();
			final int pageNr = storage.getPage().getPageNr();

			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						logger.debug("analyzing structure...");
						monitor.beginTask("Analyzing structure", IProgressMonitor.UNKNOWN);
						storage.analyzeStructure(colId, docId, pageNr, detectPageNumbers, detectRunningTitles, detectFootnotes);
						monitor.done();
						// displaySuccessMessage("Written TEI file to "+path);
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Exporting", false);

			reloadCurrentTranscript(true, true);
			storage.getTranscript().getPage().setEdited(true);

//			ui.selectStructureTab();
			updatePageInfo();
		} catch (Throwable e) {
			onError("Analyze Page Structure", e.getMessage(), e);
		}

	}
	
	public void updateParentRelationshipAccordingToGeometricOverlap() {
		if (!storage.hasTranscript())
			return;

		try {
			logger.debug("updating parent relationship according to geometric overlap");
			IStructuredSelection sel = (IStructuredSelection) ui.getStructureTreeViewer().getSelection();
			Iterator<?> it = sel.iterator();
			
			int cTotal=0;
			while (it.hasNext()) {
				Object o = it.next();
				int c=0;
				
				if (o instanceof TrpPageType) {
					TrpPageType page = (TrpPageType) o;
					c = CanvasShapeUtil.assignToShapesGeometrically(page.getTextRegions(false), page.getLines());
					if (c > 0) {
						TrpShapeTypeUtils.applyReadingOrderFromCoordinates(page.getTextRegionOrImageRegionOrLineDrawingRegion(), false, true, false);
					}
				} else if (o instanceof TrpTextRegionType) {
					TrpTextRegionType textRegion = (TrpTextRegionType) o;
					c = CanvasShapeUtil.assignToParentIfOverlapping(textRegion, textRegion.getPage().getLines(), 0.9d);
					if (c > 0) {
						TrpShapeTypeUtils.applyReadingOrderFromCoordinates(textRegion.getTrpTextLine(), false, false, false);
					}
				} else if (o instanceof TrpTextLineType) {
					TrpTextLineType textLine = (TrpTextLineType) o;
					c = CanvasShapeUtil.assignToParentIfOverlapping(textLine, textLine.getPage().getLines(), 0.9d);
					if (c > 0) {
						TrpShapeTypeUtils.applyReadingOrderFromCoordinates(textLine.getTrpWord(), false, false, false);
					}
				}
				// TODO: tables???? --> most probably not relevant for this functionality...
				cTotal += c;
			}
			logger.debug("reassigned nr of shapes: "+cTotal);
			
			if (cTotal > 0) {
				JAXBPageTranscript tr = storage.getTranscript();
				if (tr != null) {
					tr.getPage().sortContent();
				}
				ui.getStructureTreeViewer().refresh();
			}
		} catch (Throwable e) {
			onError("Error updating parent relationship", e.getMessage(), e);
		}
	}

	public void updateReadingOrderAccordingToCoordinates(boolean deleteReadingOrder, boolean recursive) {
		if (!storage.hasTranscript())
			return;

		logger.debug("applying reading order according to coordinates");
		IStructuredSelection sel = (IStructuredSelection) ui.getStructureTreeViewer().getSelection();
		Iterator<?> it = sel.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof TrpPageType) {
				TrpShapeTypeUtils.applyReadingOrderFromCoordinates(((TrpPageType) o).getTextRegionOrImageRegionOrLineDrawingRegion(), false,
						true, recursive);
			} else if (o instanceof TrpTextRegionType) {
				TrpShapeTypeUtils.applyReadingOrderFromCoordinates(((TrpTextRegionType) o).getTrpTextLine(), false, deleteReadingOrder, recursive);
			} else if (o instanceof TrpTextLineType) {
				TrpShapeTypeUtils.applyReadingOrderFromCoordinates(((TrpTextLineType) o).getTrpWord(), false, deleteReadingOrder, recursive);
			} else if (o instanceof TrpTableRegionType) {
				TrpShapeTypeUtils.applyReadingOrderFromCoordinates(((TrpTableRegionType) o).getTrpTableCell(), false, true, recursive);
			}
		}

		JAXBPageTranscript tr = storage.getTranscript();

		tr.getPage().sortContent();
		ui.getStructureTreeViewer().refresh();
	}

	public void reloadCollections() {
		try {
			logger.debug("reloading collections!");
			if (!storage.isLoggedIn()) {
				// DialogUtil.showErrorMessageBox(getShell(), "Not logged in",
				// "You have to log in to reload the collections list");
				return;
			}

			storage.reloadCollections();
		} catch (Throwable e) {
			onError("Error", "Error reload of collections: " + e.getMessage(), e);
		}
	}

	public String getSelectedImageFileType() {
		String fileType = "view";
		MenuItem mi = getCanvasWidget().getToolbar().getImageVersionDropdown().getSelected();
		if (mi != null) {
			fileType = (String) mi.getData();
		}
		
		return fileType;
	}

	private boolean checkExportFile(File file, String extension) {

		String fTxt = file.getAbsolutePath();
		if (extension != null && !fTxt.toLowerCase().endsWith(extension)) {
			fTxt = fTxt + extension;
			file = new File(fTxt);
		}

		if (!file.getParentFile().exists()) {
			DialogUtil.showErrorMessageBox(getShell(), "Error trying to export",
					"The export destination folder does not exist - select an existing base folder!");
			return false;
		}

		if (file.exists() && extension != null) {
			int a = DialogUtil.showYesNoDialog(getShell(), "File exists", "The specified file " + file.getAbsolutePath() + " exists - overwrite?");
			if (a == SWT.YES)
				return true;
			else
				return false;
		} else if (file.exists()) {
			int a = DialogUtil.showYesNoDialog(getShell(), "Folder exists", "The specified document folder " + file.getAbsolutePath() + " exists - overwrite?");
			FileUtils.deleteQuietly(file);
			if (a == SWT.YES)
				return true;
			else
				return false;
		}

		return true;

	}

	public void loadLocalPageXmlFile() {
		if (!storage.isPageLoaded()) {
			onError("No page loaded", "No page is loaded currently!", null);
			return;
		}

		logger.debug("loading a local page xml file...");
		String fn = DialogUtil.showOpenFileDialog(getShell(), "Select xml file to load", null, new String[] { "*.xml" });
		if (fn == null)
			return;

		try {
			PcGtsType p = PageXmlUtils.unmarshal(new File(fn));
			storage.getTranscript().setPageData(p);
			storage.getTranscript().getPage().setEdited(true);

			reloadCurrentTranscript(true, false);
		} catch (Exception e) {
			onError("Error loading page XML", e.getMessage(), e);
		}
	}

	public void syncWithLocalDoc() {
		try {
			logger.debug("syncing with local doc!");

			if (!storage.isLoggedIn() || !storage.isRemoteDoc()) {
				DialogUtil.showErrorMessageBox(getShell(), "Error", "No remote document loaded!");
				return;
			}

			String fn = DialogUtil.showOpenFolderDialog(getShell(), "Choose a folder with images and page files", lastLocalDocFolder);
			if (fn == null)
				return;

			// store current location 
			lastLocalDocFolder = fn;
			
			// enable sync mode to allow for local docs without images
			DocLoadConfig config = new DocLoadConfig();
			config.setEnableSyncWithoutImages(true);
			config.setDimensionMapFromDoc(storage.getDoc());
			TrpDoc localDoc = LocalDocReader.load(fn, config);

			final DocSyncDialog d = new DocSyncDialog(getShell(), storage.getDoc(), localDoc);
			if (d.open() != Dialog.OK) {
				return;
			}

			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						storage.syncDocPages(d.getSourcePages(), d.getChecked(), monitor);
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Syncing", true);

			reloadCurrentDocument();
		} catch (Throwable e) {
			onError("Sync error", "Error during sync of remote document", e);
		}

	}

	public void openSearchDialog() {
		if (searchDiag != null) {
			if(searchDiag.getShell() != null ){

				if(searchDiag.getShell().getMinimized()){
					searchDiag.getShell().setMinimized(false);
					searchDiag.getShell().forceActive();
				}else{
					searchDiag.getShell().forceActive();
				}
			}else{
				searchDiag.open();
			}
		} 
		else{		
			searchDiag = new SearchDialog(getShell());
			searchDiag.open();
		}
	}
	
	public SearchDialog getSearchDialog(){
		return searchDiag;
	}


//	//update visibility of reading order
//	public void updateReadingOrderVisibility() {
//
//		for (ICanvasShape s : getScene().getShapes()) {
//
//			if (s.hasDataType(TrpTextRegionType.class)) {
//				s.showReadingOrder(getTrpSets().isShowReadingOrderRegions());
//			}
//			if (s.hasDataType(TrpTextLineType.class)) {
//				s.showReadingOrder(getTrpSets().isShowReadingOrderLines());
//			}
//			if (s.hasDataType(TrpWordType.class)) {
//				s.showReadingOrder(getTrpSets().isShowReadingOrderWords());
//			}
//		}
//	}

	public void showEventMessages() {
		try {
			List<TrpEvent> events = storage.getEvents();
			for (TrpEvent ev : events) {
				final String msg = CoreUtils.DATE_FORMAT_USER_FRIENDLY.format(ev.getDate()) + ": " + ev.getTitle() + "\n\n" + ev.getMessage();
				Pair<Integer, Boolean> ret = DialogUtil.showMessageDialogWithToggle(getShell(), "Notification", msg, "Do not show this message again", false,
						SWT.NONE, "OK");
				boolean doNotShowAgain = ret.getRight();
				logger.debug("Do not show again = " + doNotShowAgain);
				if (doNotShowAgain) {
					storage.markEventAsRead(ev.getId());
				}
			}
		} catch (IOException ioe) {
			logger.info("Could not write events.txt file!", ioe);
		} catch (Exception e) {
			logger.info("Could not load events from server!", e);
		}
	}

	public void applyAffineTransformToDoc() {
		try {
			logger.debug("applying affine transformation!");

			if (!storage.isDocLoaded())
				throw new IOException("No document loaded!");

			final AffineTransformDialog d = new AffineTransformDialog(getShell(), storage.getDoc().getPages());
			if (d.open() != Dialog.OK) {
				logger.debug("cancelled");
				return;
			}

			if (!d.hasTransform()) {
				logger.debug("no transform specified");
				return;
			}

			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						logger.debug("applying transformation!");
						storage.applyAffineTransformation(d.getSelectedPages(), d.getTx(), d.getTy(), d.getSx(), d.getSy(), d.getRot(), monitor);
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Transforming coordinates", true);

			if (d.getSelectedPages().contains(storage.getPageIndex())) {
				reloadCurrentPage(true);
			}
		} catch (Throwable e) {
			onError("Affine transformation error", "Error during affine transformation of document", e);
		}
	}

	public void setLocale(Locale l) {
		logger.debug("setting new locale: " + l);

		if (l == null || Msgs.getLocale().equals(l))
			return;

		Msgs.setLocale(l);
		TrpConfig.getTrpSettings().setLocale(l);
//		TrpConfig.save(TrpSettings.LOCALE_PROPERTY);

		DialogUtil.showInfoMessageBox(ui.getShell(), Msgs.get2("language_changed") + ": " + l.getDisplayName(), Msgs.get2("restart"));
	}

	public void batchReplaceImagesForDoc() {
		try {
			logger.debug("batch replacing images!");

			if (!storage.isLoggedIn() || !storage.isRemoteDoc())
				throw new IOException("No remote document loaded!");

			String fn = DialogUtil.showOpenFolderDialog(getShell(), "Choose a folder with image files", lastLocalDocFolder);
			if (fn == null)
				return;

			// store current location 
			lastLocalDocFolder = fn;
			
			File inputDir = new File(fn);
			List<File> imgFiles = Arrays.asList(inputDir.listFiles(new ImgFileFilter()));
			Collections.sort(imgFiles);

			final BatchImageReplaceDialog d = new BatchImageReplaceDialog(getShell(), storage.getDoc(), imgFiles);
			if (d.open() != Dialog.OK) {
				return;
			}

			logger.debug("checked pages: ");
			for (TrpPage p : d.getCheckedPages()) {
				logger.debug("" + p);
			}
			logger.debug("checkesd urls: ");
			for (URL u : d.getCheckedUrls()) {
				logger.debug("" + u);
			}

			if (d.getCheckedPages().size() != d.getCheckedUrls().size()) {
				throw new Exception("The nr. of checked pages must equals nr. of checked images!");
			}

			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						storage.batchReplaceImages(d.getCheckedPages(), d.getCheckedUrls(), monitor);
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Batch replacing images", true);

			if (d.getCheckedPages().contains(storage.getPage())) {
				reloadCurrentPage(false);
			}

		} catch (Throwable e) {
			onError("Error", "Error during batch replace of images", e);
		}
	}

	public void selectProfile(String name) {
		try {
			TrpConfig.loadProfile(name);
//			ui.updateProfiles();
		} catch (Exception e) {
			onError("Error loading profile!", e.getMessage(), e, true, false);
		}
	}

	public void saveNewProfile() {

		try {
			InputDialog dlg = new InputDialog(getShell(), "Save current settings as profile", "Profile name: ", "", new IInputValidator() {
				@Override public String isValid(String newText) {
					if (StringUtils.isEmpty(newText) || !newText.matches(TrpConfig.PROFILE_NAME_REGEX))
						return "Invalid profile name - only alphanumeric characters and underscores allowed!";

					return null;
				}
			});
			if (dlg.open() != Window.OK)
				return;

			String profileName = dlg.getValue();
			logger.debug("profileName = " + profileName);

			File profileFile = null;
			try {
				profileFile = TrpConfig.saveProfile(profileName, false);
				ui.updateProfiles();
			} catch (FileExistsException e) {
				if (DialogUtil.showYesNoDialog(getShell(), "Profile already exists!", "Do want to overwrite the existing one?") == SWT.YES) {
					profileFile = TrpConfig.saveProfile(profileName, true);
				}
			}
			if (profileFile != null)
				DialogUtil.showMessageBox(getShell(), "Success", "Written profile to: \n\n" + profileFile.getAbsolutePath(), SWT.ICON_INFORMATION);

		} catch (Exception e) {
			onError("Error saving profile!", e.getMessage(), e, true, false);
		}

	}

	public void createThumbs(TrpDoc doc) {
		// TODO Auto-generated method stub

		try {
			logger.debug("creating thumbnails for document: " + doc);
			if (doc == null) {
				DialogUtil.showErrorMessageBox(getShell(), "Error", "No document given");
				return;
			}

			if (!doc.isLocalDoc()) {
				DialogUtil.showErrorMessageBox(getShell(), "Error", "This is not a local document");
				return;
			}

			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						/*
						int N = 1000;
						monitor.beginTask("task!", N);
						for (int i=0; i<N; ++i) {
							monitor.worked(i+1);
							monitor.subTask("done: "+(i+1)+"/"+N);
							if (monitor.isCanceled())
								return;
						}
						*/

						//CreateThumbsService.createThumbForDoc(doc, true, null);

						SWTUtil.createThumbsForDoc(doc, false, monitor);
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Creating thumbs for local document", true);
		} catch (Throwable e) {
			onError("Error", "Error during batch replace of images", e);
		}
		updateThumbs();
	}
	
	public void insertTextOnSelectedTranscriptionWidget(Character c) {
		if (c == null)
			return;

		ATranscriptionWidget tw = ui.getSelectedTranscriptionWidget();
		if (tw == null)
			return;

		tw.insertTextIfFocused("" + c);
	}
	
	public void openVkDialog() {
		logger.debug("opening vk dialog");
		
		if (SWTUtil.isOpen(vkDiag)) {
			vkDiag.getShell().setVisible(true);
		} else {
			vkDiag = new TrpVirtualKeyboardsDialog(getShell());
			vkDiag.create();
			vkDiag.getVkTabWidget().addListener(new ITrpVirtualKeyboardsTabWidgetListener() {
				@Override public void onVirtualKeyPressed(TrpVirtualKeyboardsTabWidget w, char c, String description) {
					TrpMainWidget.this.insertTextOnSelectedTranscriptionWidget(c);
				}
			});			
			vkDiag.open();
		}
	}
	
	public void openJobsDialog() {
		logger.debug("opening jobs dialog");
		if (SWTUtil.isOpen(jobsDiag)) {
			jobsDiag.getShell().setVisible(true);
		} else {
			jobsDiag = new JobsDialog(getShell());
			jobsDiag.open();
		}
	}
		
	public void openActivityDialog() {
		logger.debug("opening cm dialog");
		if (SWTUtil.isOpen(ad)) {
			ad.getShell().setVisible(true);
		} else {
			ad = new ActivityDialog(getShell());
			ad.open();
		}
	}
	
	public void openCollectionUsersDialog(TrpCollection c) {
		if (SWTUtil.isOpen(collUsersDiag)) {
			collUsersDiag.getShell().setVisible(true);
		} else {
			collUsersDiag = new CollectionUsersDialog(getShell(), c);
			collUsersDiag.open();
		}
	}
	
	public void openCollectionManagerDialog() {
		logger.debug("opening cm dialog");
		
		if (cm!=null && !SWTUtil.isDisposed(cm.getShell())) {
			cm.getShell().setVisible(true);
		} else {
			cm = new CollectionManagerDialog(getShell(), SWT.NONE, ui.getServerWidget());
			cm.open();
		}
	}
	
	public void openEditDeclManagerDialog() {
		if(!storage.isDocLoaded()) {
			return;
		}
		
		if (edDiag!=null && !SWTUtil.isDisposed(edDiag.getShell())) {
			edDiag.getShell().setVisible(true);
		} else {
			if(storage.getRoleOfUserInCurrentCollection().getValue() < TrpRole.Editor.getValue()){
				edDiag = new EditDeclViewerDialog(getShell(), SWT.NONE);
			} else {
				edDiag = new EditDeclManagerDialog(getShell(), SWT.NONE);
			}
			edDiag.open();
		}
	}
	
//	public boolean isEditDeclManagerOpen() {
//		return edm != null && edm.getShell() != null && !edm.getShell().isDisposed();
//	}
	
	public void openDebugDialog() {
		logger.debug("opening debug dialog");
		if (SWTUtil.isOpen(debugDiag)) {
			debugDiag.getShell().setVisible(true);
		} else {
			debugDiag = new DebuggerDialog(getShell());
			debugDiag.open();
		}
	}
	
	public void openVersionsCompareDialog(String diffText) {
		logger.debug("opening compare dialog");
		if (SWTUtil.isOpen(browserDiag)) {
			browserDiag.getShell().setVisible(true);
		} else {
			browserDiag = new VersionsDiffBrowserDialog(getShell(), diffText);
			browserDiag.open();
		}
	}
	
		
	public void openVersionsDialog() {
		logger.debug("opening versions dialog");
		if (SWTUtil.isOpen(versionsDiag)) {
			versionsDiag.getShell().setVisible(true);
		} else {
			versionsDiag = new TranscriptsDialog(getShell());
			versionsDiag.open();
		}
	}
	
	public void openViewSetsDialog() {
		
		logger.debug("opening view sets dialog");
		if (viewSetsDiag!=null && !SWTUtil.isDisposed(viewSetsDiag.getShell())) {
			viewSetsDiag.getShell().setVisible(true);
		} else {
			viewSetsDiag = new SettingsDialog(getShell(), /*SWT.PRIMARY_MODAL|*/ SWT.DIALOG_TRIM, getCanvas().getSettings(), getTrpSets());
			viewSetsDiag.open();
		}
	}
	
	public void openProxySetsDialog() {
		logger.debug("opening proxy sets dialog");
		if (proxyDiag!=null && !SWTUtil.isDisposed(proxyDiag.getShell())) {
			proxyDiag.getShell().setVisible(true);
		} else {
			proxyDiag = new ProxySettingsDialog(getShell(), /*SWT.PRIMARY_MODAL|*/ SWT.DIALOG_TRIM, TrpGuiPrefs.getProxyPrefs());
			proxyDiag.open();
			Storage.getInstance().updateProxySettings();
		}
	}
	
	public void openAutoSaveSetsDialog() {
		logger.debug("opening autosave sets dialog");
		if (autoSaveDiag!=null && !SWTUtil.isDisposed(autoSaveDiag.getShell())) {
			autoSaveDiag.getShell().setVisible(true);
		} else {
			autoSaveDiag = new AutoSaveDialog(getShell(), /*SWT.PRIMARY_MODAL|*/ SWT.DIALOG_TRIM, getTrpSets());
			autoSaveDiag.open();
		}
	}

	public void openAboutDialog() {
		int res = DialogUtil.showMessageDialog(getShell(), ui.APP_NAME, ui.HELP_TEXT, null, MessageDialog.INFORMATION, 
				new String[] {"OK", "Report bug / feature request"}, 0);
		
		if (res == 1) {
			ui.getTrpMenuBar().getBugReportItem().notifyListeners(SWT.Selection, new Event());
		}		
	}

	public void openChangeLogDialog(boolean show) {
		
		if (changelogDialog == null) {
			changelogDialog = new ChangeLogDialog(getShell(), SWT.NONE);
			changelogDialog.setShowOnStartup(getTrpSets().isShowChangeLog());
		}
		
		if (show) {
			changelogDialog.open();
			getTrpSets().setShowChangeLog(changelogDialog.isShowOnStartup());
		}

	}
	
public void openJavaVersionDialog() {
		
		String javaArch = System.getProperty("sun.arch.data.model");
		String version = System.getProperty("java.version");
		String fileEnc = System.getProperty("file.encoding");
		
		if (SysUtils.isWin()) {
			String arch = System.getenv("PROCESSOR_ARCHITECTURE");
			String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

			String realArch = arch != null && arch.endsWith("64")
			                  || wow64Arch != null && wow64Arch.endsWith("64")
			                      ? "64" : "32";
			if (javaVersionDialog == null && (!realArch.equals(javaArch) || version.startsWith("1.10") || !fileEnc.startsWith("UTF-8"))) {
				javaVersionDialog = new JavaVersionDialog(getShell(), SWT.NONE, realArch,javaArch,version,fileEnc);
				javaVersionDialog.open();
			}
		}
		if(SysUtils.isLinux()) {
			String realArch;
			Process p;
			try {
				p = Runtime.getRuntime().exec("lscpu");
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));	
				realArch = br.readLine().contains("64") ? "64" : "32" ;
				logger.debug("line : "+realArch);
				if (javaVersionDialog == null && (!realArch.equals(javaArch) || version.startsWith("1.10") || !fileEnc.startsWith("UTF-8"))) {
					javaVersionDialog = new JavaVersionDialog(getShell(), SWT.NONE, realArch,javaArch,version,fileEnc);
					javaVersionDialog.open();
				}
				
			}catch (Exception e) {}
		}
	
	}
	
	public void openPAGEXmlViewer() {
		try {
			logger.debug("loading transcript source");
			if (storage.isPageLoaded() && storage.getTranscriptMetadata() != null) {
				URL url = Storage.getInstance().getTranscriptMetadata().getUrl();

				PAGEXmlViewer xmlviewer = new PAGEXmlViewer(ui.getShell(), SWT.MODELESS);
				xmlviewer.open(url);
			}
		} catch (Exception e1) {
			onError("Could not open XML", "Could not open XML", e1);
		}			
	}

	public void changeProfileFromUi() {
		int i = ui.getProfilesToolItem().getLastSelectedIndex();
		logger.debug("changing profile from ui, selected index = "+i);
		
		if (i>=0 && i < ui.getProfilesToolItem().getItemCount()-1) { // profile selected
			if (!SWTUtil.isDisposed(ui.getProfilesToolItem().getSelected()) && ui.getProfilesToolItem().getSelected().getData() instanceof String) {				
				String name = (String) ui.getProfilesToolItem().getSelected().getData();
				logger.info("selecting profile: "+name);
				mw.selectProfile(name);
									
				boolean mode = (name.contains("Transcription")? true : false);
				canvas.getScene().setTranscriptionMode(mode);
			}
		} else if (i == ui.getProfilesToolItem().getItemCount()-1) {
			logger.info("opening save profile dialog...");
			mw.saveNewProfile();
			canvas.getScene().setTranscriptionMode(false);
		}
	}
	
	public int getSelectedCollectionId() {
		return ui.getServerWidget().getSelectedCollectionId();
	}
	
	public TrpCollection getSelectedCollection() {
		return ui.getServerWidget().getSelectedCollection();
	}
	
	public void openHowToGuides() {
		Desktop d = Desktop.getDesktop();
		try {
			d.browse(new URI("https://transkribus.eu/wiki/index.php/How_to_Guides"));
		} catch (IOException | URISyntaxException e) {
			logger.debug(e.getMessage());
		}		
	}

	public void openCanvasHelpDialog() {
		String ht = ""
//				+ "Canvas shortcut operations:\n"
				+ "- esc: set selection mode\n"
				+ "- shift + drag-on-bounding-box: resize shape on bounding box\n"
				+ "- shift + drag shape: move shape including all its child shapes\n"
				+ "- right click on a shape: context menu with additional operations\n"
				+ "  (note: on mac touchpads, right-clicks are performed using two fingers simultaneously)"
				;
		
		
		int res = DialogUtil.showMessageDialog(getShell(), "Canvas shortcut operations", ht, null, MessageDialog.INFORMATION, 
				new String[] {"OK"}, 0);
		
	}
	
	/**
	 * Sleak is a memory tracking utility for SWT
	 */
	public void openSleak() {
//		if (true) // FIXME
//			return;
		
		logger.debug("opening sleak...");

		if (!SWTUtil.isDisposed(sleakDiag)) {
			sleakDiag.getShell().setVisible(true);
		} else {
//			DeviceData data = new DeviceData();
//			data.tracking = true;
//			Display display = new Display(data);
//			
//			display.getDeviceData().tracking = true;
			
			Sleak sleak = new Sleak();
			sleakDiag = new Shell(getShell(), SWT.RESIZE | SWT.CLOSE | SWT.MODELESS);
			sleakDiag.setText("S-Leak");
			Point size = sleakDiag.getSize();
			sleakDiag.setSize(size.x / 2, size.y / 2);
			sleak.create(sleakDiag);
			sleakDiag.open();
		}
	}
	
	public void openSearchForTagsDialog() {
		if(Storage.getInstance().getDoc() == null){
			DialogUtil.showErrorMessageBox(mw.getShell(), "Error", "No document loaded.");
			return;
		}
		
		openSearchDialog();
		getSearchDialog().selectTab(SearchType.TAGS);
	}
	
	public void searchCurrentDoc(){		
		if(Storage.getInstance().getDoc() == null){
			DialogUtil.showErrorMessageBox(mw.getShell(), "Error", "No document loaded.");
			return;
		}
		
		openSearchDialog();
		getSearchDialog().selectTab(SearchType.FULLTEXT);
		
		FullTextSearchComposite ftComp = getSearchDialog().getFulltextComposite();		
		ftComp.searchCurrentDoc(true);							
		ftComp.setSearchText(ui.getQuickSearchText().getText());			
		ftComp.findText();
	}
	
	public boolean duplicateDocuments(int srcColId, List<TrpDocMetadata> docs) {
		if (!storage.isLoggedIn())
			return false;
		
		if (CoreUtils.isEmpty(docs)) {
			DialogUtil.showErrorMessageBox(getShell(), "No document selected", "Please select documents you want to duplicate!");
			return false;
		}
		
		logger.debug("duplicating document, srcColId = "+srcColId+", nDocs = "+docs.size());
		
//		if (srcColId <= 0) {
//			DialogUtil.showErrorMessageBox(getShell(), "Error", "No source collection specified!");
//			return;
//		}
				
		if (!StorageUtil.canDuplicate(srcColId)) {
			DialogUtil.showErrorMessageBox(getShell(), "Insufficient rights", "You must be either at least editor of the collection!");
			return false;
		}
								
		ChooseCollectionDialog diag = new ChooseCollectionDialog(getShell(), "Choose a destination collection", storage.getCollection(srcColId));
		if (diag.open() != Dialog.OK)
			return false;
		
		TrpCollection c = diag.getSelectedCollection();
		if (c==null) {
			DialogUtil.showErrorMessageBox(getShell(), "No collection selected", "Please select a collection to duplicate the document to!");
			return false;
		}
		int toColId = c.getColId();
		
		final String title_suffix = "_duplicated";
		
		String nameTmp = null;
		if (docs.size() == 1) {
			InputDialog dlg = new InputDialog(getShell(), "New name", "Enter the new name of the document", docs.get(0).getTitle()+title_suffix, null);
			if (dlg.open() != Window.OK)
				return false;
		
			nameTmp = dlg.getValue();
		}
		final String newNameForSingleDoc = nameTmp;
		
		final List<String> error = new ArrayList<>();
		try {
		ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
			@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				
				try {
					monitor.beginTask("Duplicating documents", docs.size());
//						TrpUserLogin user = storage.getUser();
					int i = 0;
					for (TrpDocMetadata d : docs) {
						if (monitor.isCanceled())
							throw new InterruptedException();

						try {
							// the name of the duplicated document is either the name the user has input into the dialog for a single document, or, for multiple
							// documents, the document title + title_suffix
							String name = docs.size()==1 && !StringUtils.isEmpty(newNameForSingleDoc) ? newNameForSingleDoc : d.getTitle()+title_suffix;
							logger.debug("duplicating document: "+d+" name: "+name+", toColId: "+toColId);
							
							storage.duplicateDocument(srcColId, d.getDocId(), name, toColId <= 0 ? null : toColId);
//							storage.duplicateDocument(srcColId, d.getDocId(), null, toColId <= 0 ? null : toColId); // TEST: null as name, did formerly result in NPE on server
						} catch (Throwable e) {
							logger.warn("Could not duplicate document: "+d);
							error.add(d.getTitle()+", ID = "+d.getDocId()+", Reason = "+e.getMessage());
						}
													
						monitor.worked(++i);
					}
				}
				catch (InterruptedException ie) {
					throw ie;
				} catch (Throwable e) {
					throw new InvocationTargetException(e, e.getMessage());
				}
			}
		}, "Duplicating documents", true);
		}
		catch (InterruptedException e) {}
		catch (Throwable e) {
			onError("Unexpected error", e.getMessage(), e);
		}
		
		if (!error.isEmpty()) {
			String msg = "Could not duplicate the following documents:\n";
			for (String u : error) {
				msg += u + "\n";
			}
			
			mw.onError("Error duplicating documents", msg, null);
			return false;
		} else {
			DialogUtil.showInfoMessageBox(getShell(), "Success", "Successfully duplicated "+docs.size()+" documents\nGo to the jobs view to check the status of duplication!");
			return true;
		}
	}
	
	public boolean deleteDocuments(List<TrpDocMetadata> docs) {
		if (!storage.isLoggedIn()) {
			return false;
		}
		
		if (CoreUtils.isEmpty(docs)) {
			DialogUtil.showErrorMessageBox(getShell(), "No document selected", "Please select a document you want to delete!");
			return false;
		}
		
		int N = docs.size();
		
		if (N > 1) {
			if (DialogUtil.showYesNoDialog(getShell(), "Delete Documents", "Do you really want to delete " + N + " selected documents ")!=SWT.YES) {
				return false;
			}
		}
		else{
			if (DialogUtil.showYesNoDialog(getShell(), "Delete Document", "Do you really want to delete document "+docs.get(0).getTitle())!=SWT.YES) {
				return false;
			}
		}
		
		final List<String> error = new ArrayList<>();
		try {
		ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
			@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				
				try {
					monitor.beginTask("Deleting documents", docs.size());
					TrpUserLogin user = storage.getUser();
					int i = 0;
					for (TrpDocMetadata d : docs) {
						if (monitor.isCanceled())
							throw new InterruptedException();

						logger.debug("deleting document: "+d);
						
						if (!user.isAdmin() && user.getUserId()!=d.getUploaderId()) {
//							DialogUtil.showErrorMessageBox(getShell(), "Unauthorized", "You are not the uploader of this document. " + md.getTitle());
//							return false;
							String errorMsg = "Unauthorized - you are not the owner of this document: "+d.getTitle()+", id: "+d.getDocId();
							logger.warn(errorMsg);
							error.add(errorMsg);
						} else {
							try {
								storage.deleteDocument(storage.getCollId(), d.getDocId());
								logger.info("deleted document: "+d);
							} catch (SessionExpiredException | TrpClientErrorException | TrpServerErrorException e) {
								logger.warn("Could not delete document: "+d, e);
								error.add(d.getTitle()+", ID = "+d.getDocId()+", Reason = "+e.getMessageToUser());
							} catch (Throwable e) {
								logger.warn("Could not delete document: "+d, e);
								error.add(d.getTitle()+", ID = "+d.getDocId()+", Reason = "+e.getMessage());
							}
						}
	
						monitor.worked(++i);
					}

				}
				catch (InterruptedException ie) {
					throw ie;
				} catch (Throwable e) {
					throw new InvocationTargetException(e, e.getMessage());
				}
			}
		}, "Deleting documents", true);
		}
		catch (InterruptedException e) {}
		catch (Throwable e) {
			onError("Unexpected error", e.getMessage(), e);
		}
		
		if (!error.isEmpty()) {
			String msg = "Could not delete the following documents:\n";
			for (String u : error) {
				msg += u + "\n";
			}
			mw.onError("Error deleting documents", msg, null);
			try {
				//reload necessary in fact the symbolic image has changed - this is done during the delete job
				storage.reloadCollections();
			} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException
					| NoConnectionException e) {
				// TODO Auto-generated catch block
				logger.error("reloading collections not possible after document(s) deletion");
				e.printStackTrace();
			}
			ui.serverWidget.getDocTableWidget().reloadDocs(false, true);
			return false;
		} else {
			DialogUtil.showInfoMessageBox(getShell(), "Success", "Successfully deleted "+docs.size()+" documents");
			//clean up GUI
			try {
				//reload necessary in fact the symbolic image has changed - this is done during the delete job
				storage.reloadCollections();
			} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException
					| NoConnectionException e) {
				// TODO Auto-generated catch block
				logger.error("reloading collections not possible after documents deletion");
				e.printStackTrace();
			}
			//reload necessary to actualize doc list
			ui.serverWidget.getDocTableWidget().reloadDocs(false, true);
			for (TrpDocMetadata d : docs) {
				if (storage.isThisDocLoaded(d.getDocId(), null)) {
					//if deleted doc was loaded in GUI - close it
					logger.debug("deleted doc loaded in GUI - close it");
					storage.closeCurrentDocument();
					clearCurrentPage();
				}
				//update the recent docs list
				updateRecentDocItems(d);
				ui.getServerWidget().updateRecentDocs();
			}
			
			return true;
		}
	}
	
	public boolean addDocumentsToCollection(int srcColId, Collection<TrpDocMetadata> docs) {
		if (!storage.isLoggedIn() || docs==null || docs.isEmpty()) {
			return false;
		}
		
		TrpCollection coll = storage.getCollection(srcColId);
		if (coll == null) {
			DialogUtil.showErrorMessageBox(getShell(), "Error", "Could not determine collection for selected documents!");
		}
		
		final TrpUserLogin user = storage.getUser();
		
		if (!user.isAdmin() && !StorageUtil.isOwnerOfCollection(coll) && !StorageUtil.isUploader(user, docs)) {
			DialogUtil.showErrorMessageBox(getShell(), "Unauthorized", "You are not the owner in this collection or uploader of the document(s)!");
			return false;
		}
		
		ChooseCollectionDialog diag = new ChooseCollectionDialog(getShell(), "Choose a collection where the documents should be added to", storage.getCurrentDocumentCollection()) {
			@Override protected Control createDialogArea(Composite parent) {
				Composite container = (Composite) super.createDialogArea(parent);
				
				Label infoLabel = new Label(container, 0);
				infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 2, 2));
				infoLabel.setText("Note: documents are only linked into the collection, i.e. a soft copy is created.\nThey also remain linked to the current collection");
				Fonts.setItalicFont(infoLabel);
				
				return container;
			}
			
			@Override protected Point getInitialSize() {
				return new Point(550, 200);
			}
		};
		if (diag.open() != Dialog.OK)
			return false;
		
		TrpCollection c = diag.getSelectedCollection();
		if (c==null) {
			DialogUtil.showErrorMessageBox(getShell(), "No collection selected", "Please select a collection to add the document to!");
			return false;
		}
		logger.debug("selected collection is: "+c);		
		
		TrpServerConn conn = storage.getConnection();
				
		final List<String> error = new ArrayList<>();
		try {
		ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
			@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					monitor.beginTask("Adding documents to collection", docs.size());
					
					int i = 0;
					for (TrpDocMetadata d : docs) {
						if (monitor.isCanceled())
							throw new InterruptedException();
						
						logger.debug("adding document: "+d+" to collection: "+c.getColId());				
						try {					
							conn.addDocToCollection(c.getColId(), d.getDocId());
							logger.info("added document: "+d);
						} catch (Throwable e) {
							logger.warn("Could not add document: "+d);
							error.add(d.getTitle()+", ID = "+d.getDocId()+", Reason = "+e.getMessage());
						}
						
						monitor.worked(++i);
					}
				}
				catch (InterruptedException ie) {
					throw ie;
				} catch (Throwable e) {
					throw new InvocationTargetException(e, e.getMessage());
				}
			}
		}, "Adding documents to collection", true);
		}
		catch (InterruptedException e) {}
		catch (Throwable e) {
			onError("Unexpected error", e.getMessage(), e);
		}
		
		if (!error.isEmpty()) {
			String msg = "Could not add the following documents:\n";
			for (String u : error) {
				msg += u + "\n";
			}
			
			mw.onError("Error adding documents", msg, null);
			return false;
		} else {
			DialogUtil.showInfoMessageBox(getShell(), "Success", "Successfully added "+docs.size()+" documents");
			return true;
		}
	}

	public boolean removeDocumentsFromCollection(int selColId, List<TrpDocMetadata> docs) {
		if (!storage.isLoggedIn() || CoreUtils.isEmpty(docs)) {
			return false;
		}
		
		if (DialogUtil.showYesNoDialog(getShell(), "", "Do you really want to remove "+docs.size()+" documents from this collection?") != SWT.YES)
			return false;
		
		// check rights first:
		TrpCollection coll = storage.getCollection(selColId);
		if (coll == null) {
			DialogUtil.showErrorMessageBox(getShell(), "Error", "Could not determin collection for selected documents!");
		}
		
		TrpUserLogin user = storage.getUser();
		if (!user.isAdmin() && !StorageUtil.isOwnerOfCollection(coll) && !StorageUtil.isUploader(user, docs)) {
			DialogUtil.showErrorMessageBox(getShell(), "Unauthorized", "You are not the owner in this collection or uploader of the document(s)!");
			return false;
		}
		
		logger.debug("selected collection is: "+coll);		
		
		List<String> error = new ArrayList<>();
		try {
		ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
			@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					monitor.beginTask("Removing documents from collection", docs.size());

					TrpServerConn conn = storage.getConnection();
					int i = 0;
					for (TrpDocMetadata d : docs) {
						if (monitor.isCanceled())
							throw new InterruptedException();
						
						logger.debug("removing document: "+d+" from collection: "+coll.getColId());				
						try {
							conn.removeDocFromCollection(coll.getColId(), d.getDocId());
							logger.info("removed document: "+d);
						} catch (Throwable e) {
							logger.warn("Could not remove document: "+d);
							error.add(d.getTitle()+", ID = "+d.getDocId()+", Reason = "+e.getMessage());
						}
						
						monitor.worked(++i);
					}
				}
				catch (InterruptedException ie) {
					throw ie;
				}
				catch (Throwable e) {
					throw new InvocationTargetException(e, e.getMessage());
				}
			}
		}, "Removing documents from collection", true);
		}
		catch (InterruptedException e) {}
		catch (Throwable e) {
			onError("Unexpected error", e.getMessage(), e);
		}	
				
		if (!error.isEmpty()) {
			String msg = "Could not remove the following documents:\n";
			for (String u : error) {
				msg += u + "\n";
			}
			mw.onError("Error removing documents", msg, null);
			//clean up GUI
			try {
				//reload necessary in fact the symbolic image has changed
				storage.reloadCollections();
			} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException
					| NoConnectionException e) {
				// TODO Auto-generated catch block
				logger.error("reloading collections not possible after documents deletion");
				e.printStackTrace();
			}
			ui.serverWidget.getDocTableWidget().reloadDocs(false, true);
			return false;
		} else {
			DialogUtil.showInfoMessageBox(getShell(), "Success", "Successfully removed "+docs.size()+" documents");
			//clean up GUI
			try {
				//reload necessary in fact the symbolic image has changed
				storage.reloadCollections();
			} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException
					| NoConnectionException e) {
				// TODO Auto-generated catch block
				logger.error("reloading collections not possible after documents deletion");
				e.printStackTrace();
			}
			//reload necessary to actualize doc list
			ui.serverWidget.getDocTableWidget().reloadDocs(false, true);
			for (TrpDocMetadata d : docs) {
				if (storage.isThisDocLoaded(d.getDocId(), null)) {
					//if deleted doc was loaded in GUI - close it
					logger.debug("deleted doc loaded in GUI - close it");
					storage.closeCurrentDocument();
					clearCurrentPage();
				}
			}
			
			
			return true;
		}
	}
	
	public int createCollection() {
		logger.debug("creating collection...");
		
		InputDialog dlg = new InputDialog(getShell(),
	            "Create collection", "Enter the name of the new collection (min. 3 characters)", "", new IInputValidator() {
					@Override public String isValid(String newText) {
						if (StringUtils.length(newText) >= 3)
							return null;
						else
							return "Too short";
					}
				});
		if (dlg.open() != Window.OK)
			return 0;
				
		String collName = dlg.getValue();
		try {
			int collId = storage.addCollection(dlg.getValue());
			logger.debug("created new collection '"+collName+"' - now reloading available collections!");
			storage.reloadCollections();
			
			return collId;
		} catch (Throwable th) {
			mw.onError("Error", "Error creating collection '"+collName+"': "+th.getMessage(), th);
			return 0;
		}
	}
	
	public void deleteCollection(TrpCollection c) {
		if (c== null || !storage.isLoggedIn())
			return;
		
		TrpServerConn conn = storage.getConnection();
		logger.debug("deleting collection: "+c.getColId()+" name: "+c.getColName());
					
		if(!storage.getUser().isAdmin() && !AuthUtils.isOwner(c.getRole())) {
			DialogUtil.showErrorMessageBox(getShell(), "Unauthorized", "You are not the owner of this collection.");
			return;
		}
		
		if (DialogUtil.showYesNoDialog(getShell(), "Are you sure?", "Do you really want to delete the collection \"" 
				+ c.getColName() + "\"?\n\n"
				+ "Note: documents are not deleted, only their reference to the collection is removed - "
				+ "use the delete document button to completely remove documents from the server!",
				SWT.ICON_WARNING)!=SWT.YES) {
			return;
		}
		
		try {
			conn.deleteCollection(c.getColId());
//			DialogUtil.showInfoMessageBox(getShell(), "Success", "Successfully deleted collection!");
			
			logger.info("deleted collection "+c.getColId()+" name: "+c.getColName());
			storage.reloadCollections();
		} catch (Throwable th) {
			mw.onError("Error", "Error deleting collection '"+c.getColName()+"': "+th.getMessage(), th);
		}
	}

	public void modifyCollection(TrpCollection c) {
		if (c== null || !storage.isLoggedIn())
			return;
		
		try {
			logger.debug("Role in collection: " + c.getRole());
			if (!AuthUtils.canManage(c.getRole())) {
				DialogUtil.showErrorMessageBox(getShell(), "Unauthorized", "You are not allowed to modify this collection!");
				return;
			}
			
			storage.reloadCollections();
			
			CollectionEditorDialog ced = new CollectionEditorDialog(getShell(), c);
//			if(c.isCrowdsourcing()) {
//				ced.getCollection().setCrowdProject(storage.loadCrowdProject(ced.getCollection().getColId()));
//			}
			if (ced.open() != IDialogConstants.OK_ID) {
				/*
				 * user clicked cancel: milestones and messages without project id (this is how we know the
				 * added milestones and messages from this session) get deleted because this seems to be his 
				 * intention by clicking Cancel 
				 */
				if (ced.isCrowdMdChanged()){
					storage.getConnection().deleteCrowdProjectMilestones(ced.getCollection().getColId());
					storage.getConnection().deleteCrowdProjectMessages(ced.getCollection().getColId());
					storage.reloadCollections();
					
				}
				return;
			}
			
			if(!ced.isMdChanged()) {
				logger.debug("Metadata was not altered.");
				//return;
			}
			else{
				TrpCollection newMd = ced.getCollection();
				storage.getConnection().updateCollectionMd(newMd);
				storage.reloadCollections();
			}
			
			if (ced.isCrowdMdChanged()){
				TrpCollection newMd = ced.getCollection();
				logger.debug("crowd metadata has changed");
				storage.getConnection().postCrowdProject(newMd.getColId(), newMd.getCrowdProject());
				for (TrpCrowdProjectMilestone mst : newMd.getCrowdProject().getCrowdProjectMilestones()){
					storage.getConnection().postCrowdProjectMilestone(newMd.getCrowdProject().getColId(), mst);
				}
				for (TrpCrowdProjectMessage msg : newMd.getCrowdProject().getCrowdProjectMessages()){
					storage.getConnection().postCrowdProjectMessage(newMd.getCrowdProject().getColId(), msg);
				}
				
			}
			
//			DialogUtil.showInfoMessageBox(getShell(), "Success", "Successfully modified the colleciton!");
		} catch (Exception e) {
			mw.onError("Error modifying collection", e.getMessage(), e);
		}
	}
	
	public void createImageSizeTextRegion() {
		try {
			if (!storage.hasTranscript()) {
				return;
			}
			
			canvas.getScene().getMainImage().getBounds();
			Rectangle imgBounds = canvas.getScene().getMainImage().getBounds();
			
			if (CanvasShapeUtil.getFirstTextRegionWithSize(storage.getTranscript().getPage(), 0, 0, imgBounds.width, imgBounds.height, false) != null) {
				DialogUtil.showErrorMessageBox(getShell(), "Error", "Top level region with size of image already exists!");
				return;
			}
			
			CanvasPolygon imgBoundsPoly = new CanvasPolygon(imgBounds);
//			CanvasMode modeBackup = canvas.getMode();
			canvas.setMode(CanvasMode.ADD_TEXTREGION);
			ShapeEditOperation op = canvas.getShapeEditor().addShapeToCanvas(imgBoundsPoly, true);
			canvas.setMode(CanvasMode.SELECTION);
		} catch (Exception e) {
			TrpMainWidget.getInstance().onError("Error", e.getMessage(), e);
		}	
	}

	public void createDefaultLineForSelectedShape() {
		if (canvas.getFirstSelected() == null)
			return;
		
		try {
			logger.debug("creating default line for seected line/baseline!");
			
//			CanvasPolyline baselineShape = (CanvasPolyline) shape;
//			shapeOfParent = baselineShape.getDefaultPolyRectangle();
			
			ICanvasShape shape = canvas.getFirstSelected();
			CanvasPolyline blShape = (CanvasPolyline) CanvasShapeUtil.getBaselineShape(shape);
			if (blShape == null)
				return;
			
			CanvasPolygon pl = blShape.getDefaultPolyRectangle();
			if (pl == null)
				return;
			
			ITrpShapeType st = (ITrpShapeType) shape.getData();
			TrpTextLineType line = TrpShapeTypeUtils.getLine(st);
			if (line != null) {
				ICanvasShape lineShape = (ICanvasShape) line.getData();
				if (lineShape != null) {
					lineShape.setPoints(pl.getPoints());
					
					canvas.redraw();
				}
			}
		} catch (Exception e) {
			TrpMainWidget.getInstance().onError("Error", e.getMessage(), e);
		}	
	}
	
	public void deleteTags(CustomTag... tags) {
		if (tags != null) {
			deleteTags(Arrays.asList(tags));
		}
	}

	public void deleteTags(List<CustomTag> tags) {
		try {
			for (CustomTag t : tags) {
				logger.trace("deleting tag: "+t+" ctl: "+t.getCustomTagList());
				if (t==null || t.getCustomTagList()==null)
					continue;
				
				t.getCustomTagList().deleteTagAndContinuations(t);
			}
	
			updatePageRelatedMetadata();
			getUi().getLineTranscriptionWidget().redrawText(true);
			getUi().getWordTranscriptionWidget().redrawText(true);
			refreshStructureView();
			
//			getUi().getTaggingWidget().getTagListWidget().refreshTable();
		} catch (Exception e) {
			TrpMainWidget.getInstance().onError("Error", e.getMessage(), e);
		}
	}
	
	public void deleteTagsForCurrentSelection() {
		try {
			logger.debug("clearing tags from selection!");
			ATranscriptionWidget aw = ui.getSelectedTranscriptionWidget();
			if (aw==null) {
				logger.debug("no transcription widget selected - doin nothing!");
				return;
			}
			
			List<Pair<ITrpShapeType, IntRange>> ranges = aw.getSelectedShapesAndRanges();
			for (Pair<ITrpShapeType, IntRange> p : ranges) {
				ITrpShapeType s = p.getLeft();
				IntRange r = p.getRight();
				s.getCustomTagList().deleteTagsInRange(r.getOffset(), r.getLength(), true);
				s.setTextStyle(null); // delete also text styles from range!
			}
			
			updatePageRelatedMetadata();
			getUi().getLineTranscriptionWidget().redrawText(true);
			getUi().getWordTranscriptionWidget().redrawText(true);
			refreshStructureView();
		} catch (Exception e) {
			onError("Unexpected error deleting tags", e.getMessage(), e);
		}	
	}
	
	public void addTagForSelection(CustomTag t, String addOnlyThisProperty) {
		addTagForSelection(t.getTagName(), t.getAttributeNamesValuesMap(), addOnlyThisProperty);
	}

	public void addTagForSelection(String tagName, Map<String, Object> attributes, String addOnlyThisProperty) {
		try {
			logger.debug("addTagForSelection, tagName = "+tagName+", attributes = "+attributes+", addOnlyThisProperty = "+addOnlyThisProperty);
			
			boolean isTextSelectedInTranscriptionWidget = isTextSelectedInTranscriptionWidget();
			
	//		ATranscriptionWidget aw = mainWidget.getUi().getSelectedTranscriptionWidget();
	//		boolean isSingleSelection = aw!=null && aw.isSingleSelection();
			
			CustomTag protoTag = CustomTagFactory.getTagObjectFromRegistry(tagName);
			boolean canBeEmpty = protoTag!=null && protoTag.canBeEmpty();
			logger.debug("protoTag = "+protoTag+" canBeEmtpy = "+canBeEmpty);
			logger.debug("isTextSelectedInTranscriptionWidget = "+isTextSelectedInTranscriptionWidget);		
			
			if (!isTextSelectedInTranscriptionWidget && !canBeEmpty) {
				logger.debug("applying tag to all selected in canvas: "+tagName);
				List<? extends ITrpShapeType> selData = canvas.getScene().getSelectedData(ITrpShapeType.class);
				logger.debug("selData = "+selData.size());
				for (ITrpShapeType sel : selData) {
					if (sel instanceof TrpTextLineType || sel instanceof TrpWordType) { // tags only for words and lines!
						try {
							CustomTag t = CustomTagFactory.create(tagName, 0, sel.getUnicodeText().length(), attributes);						
							sel.getCustomTagList().addOrMergeTag(t, addOnlyThisProperty);
							logger.debug("created tag: "+t);
						} catch (Exception e) {
							logger.error("Error creating tag: "+e.getMessage(), e);
						}
					}
				}
			} else {
				logger.debug("applying tag to all selected in transcription widget: "+tagName);
				List<Pair<ITrpShapeType, CustomTag>> tags4Shapes = TaggingWidgetUtils.constructTagsFromSelectionInTranscriptionWidget(ui, tagName, attributes);
	//			List<Pair<ITrpShapeType, CustomTag>> tags4Shapes = TaggingWidgetUtils.constructTagsFromSelectionInTranscriptionWidget(ui, tagName, null);
				for (Pair<ITrpShapeType, CustomTag> p : tags4Shapes) {
					CustomTag tag = p.getRight();
					if (tag != null) {
						tag.setContinued(tags4Shapes.size()>1);
						p.getLeft().getCustomTagList().addOrMergeTag(tag, addOnlyThisProperty);
					}
				}		
			}
			
			updatePageRelatedMetadata();
			getUi().getLineTranscriptionWidget().redrawText(true);
			getUi().getWordTranscriptionWidget().redrawText(true);
			refreshStructureView();
		} catch (Exception e) {
			TrpMainWidget.getInstance().onError("Error", e.getMessage(), e);
		}
	}
	
	public void updateVersionStatus(){
		if (storage.hasTranscript()) {
			ui.getStatusCombo().setText(storage.getTranscriptMetadata().getStatus().getStr());
			ui.getStatusCombo().redraw();			
		}
	}
	
	public void changeVersionStatus(String text, TrpPage page) {
		if (EditStatus.fromString(text).equals(EditStatus.NEW)){
			//New is only allowed for the first transcript
			DialogUtil.showInfoMessageBox(getShell(), "Status 'New' reserved for first transcript", "Only the first transcript can be 'New', all others must be at least 'InProgress'");
			return;
		}
		
		int colId = Storage.getInstance().getCollId();
		//is the page the one currently loaded in Transkribus
		boolean isLoaded = (page.getPageId() == Storage.getInstance().getPage().getPageId());
        //then only the latest transcript can be changed -> page.getCurrentTranscript() gives the latest of this page
		boolean isLatestTranscript = (page.getCurrentTranscript().getTsId() == Storage.getInstance().getTranscriptMetadata().getTsId());
        
		if (isLoaded && !isLatestTranscript){
			DialogUtil.showInfoMessageBox(getShell(), "Status change not allowed", "Status change is only allowed for the latest transcript. Load the latest transcript via the 'Versions' button.");
			return;
			//logger.debug("page is loaded with transcript ID " + Storage.getInstance().getTranscriptMetadata().getTsId());
		}
		
		int pageNr = page.getPageNr();
		int docId = page.getDocId();
		
		int transcriptId = 0;
		if ((pageNr - 1) >= 0) {
			transcriptId = page.getCurrentTranscript().getTsId();
		}
		
		try {
			storage.getConnection().updatePageStatus(colId, docId, pageNr, transcriptId,
					EditStatus.fromString(text), "");

		
			if (isLoaded && isLatestTranscript){
				storage.getTranscript().getMd().setStatus(EditStatus.fromString(text));
				Storage.getInstance().reloadTranscriptsList(colId);
				if (Storage.getInstance().setLatestTranscriptAsCurrent()){
					logger.debug("latest transcript is current");
				}
				else{
					logger.debug("setting of latest transcript to current fails");
				}
				
				TrpTranscriptMetadata trMd = Storage.getInstance().getTranscript().getMd();
				
				if (trMd != null){
					//ui.getStatusCombo().add(storage.getTranscriptMetadata().getStatus().getStr());
	//					ui.getStatusCombo().add(arg0);
	//					ui.getStatusCombo().remove(arg0);
					ui.getStatusCombo().setText(trMd.getStatus().getStr());
					ui.getStatusCombo().redraw();
					logger.debug("Status: " + trMd.getStatus().getStr() + " tsid = " + trMd.getTsId());
					//SWTUtil.select(ui.getStatusCombo(), EnumUtils.indexOf(storage.getTranscriptMetadata().getStatus()));
				}
				
			}
		} catch (SessionExpiredException | ServerErrorException | ClientErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void changeVersionStatus(String text, List<TrpPage> pageList) {
		
		if (EditStatus.fromString(text).equals(EditStatus.NEW)){
			//New is only allowed for the first transcript
			DialogUtil.showInfoMessageBox(getShell(), "Status 'New' reserved for first transcript", "Only the first transcript can be 'New', all others must be at least 'InProgress'");
			return;
		}
		
		Storage storage = Storage.getInstance();
		
		int colId = Storage.getInstance().getCollId();
		if (!pageList.isEmpty()) {
			
			try {
				ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
					@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							try{

								monitor.beginTask("Change status to "+EditStatus.fromString(text), pageList.size());
								int c=0;
								
								for (TrpPage page : pageList) {
									
									if (monitor.isCanceled()){
										storage.reloadDocWithAllTranscripts();
										return;
									}

									int pageNr = page.getPageNr();
									int docId = page.getDocId();
									
									int transcriptId = 0;
									if ((pageNr - 1) >= 0) {
										transcriptId = page.getCurrentTranscript().getTsId();
									}
									
									storage.getConnection().updatePageStatus(colId, docId, pageNr, transcriptId,
											EditStatus.fromString(text), "");
									
									monitor.subTask("Page " + ++c + "/" + pageList.size() );
									monitor.worked(c);
																	
									/*
									 * TODO: we break after first change because otherwise too slow for a batch
									 * Try to fasten this on the server side
									 */
									//break;
									// logger.debug("status is changed to : " +
									// storage.getDoc().getPages().get(pageNr-1).getCurrentTranscript().getStatus());
								}
								
								storage.reloadDocWithAllTranscripts();
								
							} catch (SessionExpiredException | ServerErrorException | ClientErrorException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
//							} catch (NoConnectionException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

					}
				}, "Updating page status", true);
			} catch (Throwable e) {
				TrpMainWidget.getInstance().onError("Error updating page status", e.getMessage(), e, true, false);
			}
			finally{
				for (TrpPage page : pageList) {
					//reload the page in the GUI if status has changed
					if (page.getPageId() == Storage.getInstance().getPage().getPageId()){
						reloadCurrentPage(true);
					}
				}
				updateVersionStatus();
				
			}
		}

	}

	public void revertVersions() {
		
		try {
			//get doc to have all transcripts for all pages available to change
			Storage storage = Storage.getInstance();
			TrpDoc currDoc = Storage.getInstance().getRemoteDoc(storage.getCurrentDocumentCollectionId(), storage.getDocId(), -1);
			for (TrpPage page : currDoc.getPages()) {
				
				TrpTranscriptMetadata currTranscript = page.getCurrentTranscript();
				TrpTranscriptMetadata parentTranscript = page.getTranscriptById(currTranscript.getParentTsId());
				
				/*
				 * workaround as long t2i does not save the parent transcript in the transcript metadata
				 */
				if(currTranscript.getToolName() != null && currTranscript.getToolName().equals("T2I")){
					parentTranscript = page.getTranscriptWithStatus("New");
					logger.debug("parent transcript for T2I found: " + parentTranscript.getTsId());
				}

				//logger.debug("currTranscript.getToolName(): " + currTranscript.getToolName());
				
				//if there is a toolname != null we can revert the job of that tool
				if(parentTranscript != null && currTranscript.getToolName() != null){
					logger.debug("parent exists and transcript stems from tool/job - revert version");
					JAXBPageTranscript tr = new JAXBPageTranscript(parentTranscript);
					tr.build();
					storage.getConnection().updateTranscript(storage.getCurrentDocumentCollectionId(), parentTranscript.getDocId(), parentTranscript.getPageNr(), parentTranscript.getStatus(), tr.getPageData(), parentTranscript.getParentTsId(), "resetted as current");
				}
				
				//for loaded page do a reload: does not work since because it runs in a separate thread and there are some resource conflicts
//				if (storage.getPage().getPageId() == page.getPageId()){
//					logger.debug("page id = " + storage.getPage().getPageId());
//					Storage.getInstance().setLatestTranscriptAsCurrent();
//					mw.reloadCurrentPage(true);
//				}
			}
		} catch (SessionExpiredException | ServerErrorException | ClientErrorException
				| IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setStructureTypeOfSelected(String structType, boolean recursive) {
		List<ICanvasShape> selected = getCanvas().getScene().getSelectedAsNewArray();
		logger.debug("applying structure type to selected, n = "+selected.size()+" structType: "+structType);
//		TextTypeSimpleType struct = EnumUtils.fromValue(TextTypeSimpleType.class, mw.getRegionTypeCombo().getText());
//		String struct = mw.getStructureType();	
		for (ICanvasShape sel : selected) {
			ITrpShapeType st = GuiUtil.getTrpShape(sel);
			logger.debug("updating struct type for " + sel+" type = "+structType+", TrpShapeType = "+st);
			
			if (st != null) {
				st.setStructure(structType, recursive, mw);
			}
		}
		
		refreshStructureView();
		redrawCanvas();
	}



}
