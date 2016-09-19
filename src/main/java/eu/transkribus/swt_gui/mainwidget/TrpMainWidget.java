package eu.transkribus.swt_gui.mainwidget;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.security.auth.login.LoginException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.ServerErrorException;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.dea.fimgstoreclient.beans.FimgStoreImgMd;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.mihalis.opal.tipOfTheDay.TipOfTheDay;
import org.mihalis.opal.tipOfTheDay.TipOfTheDay.TipStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.OAuthTokenRevokedException;
import eu.transkribus.core.io.LocalDocReader;
import eu.transkribus.core.io.util.ImgFileFilter;
import eu.transkribus.core.model.beans.JAXBPageTranscript;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpDocDir;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpEvent;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory;
import eu.transkribus.core.model.beans.customtags.TextStyleTag;
import eu.transkribus.core.model.beans.enums.OAuthProvider;
import eu.transkribus.core.model.beans.enums.ScriptType;
import eu.transkribus.core.model.beans.pagecontent.PcGtsType;
import eu.transkribus.core.model.beans.pagecontent.TextStyleType;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpShapeTypeUtils;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.core.model.builder.ExportUtils;
import eu.transkribus.core.model.builder.docx.DocxBuilder;
import eu.transkribus.core.model.builder.ms.TrpXlsxBuilder;
import eu.transkribus.core.model.builder.rtf.TrpRtfBuilder;
import eu.transkribus.core.model.builder.tei.TeiExportPars;
import eu.transkribus.core.program_updater.ProgramPackageFile;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.PageXmlUtils;
import eu.transkribus.core.util.SysUtils;
import eu.transkribus.swt_canvas.canvas.CanvasMode;
import eu.transkribus.swt_canvas.canvas.CanvasSettings;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_canvas.progress.ProgressBarDialog;
import eu.transkribus.swt_canvas.util.CreateThumbsService;
import eu.transkribus.swt_canvas.util.DialogUtil;
import eu.transkribus.swt_canvas.util.Images;
import eu.transkribus.swt_canvas.util.LoginDialog;
import eu.transkribus.swt_canvas.util.SWTLog;
import eu.transkribus.swt_canvas.util.SplashWindow;
import eu.transkribus.swt_canvas.util.databinding.DataBinder;
import eu.transkribus.swt_gui.Msgs;
import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.TrpGuiPrefs;
import eu.transkribus.swt_gui.TrpGuiPrefs.OAuthCreds;
import eu.transkribus.swt_gui.canvas.CanvasSettingsPropertyChangeListener;
import eu.transkribus.swt_gui.canvas.CanvasShapeObserver;
import eu.transkribus.swt_gui.canvas.TrpCanvasContextMenuListener;
import eu.transkribus.swt_gui.canvas.TrpCanvasScene;
import eu.transkribus.swt_gui.canvas.TrpCanvasSceneListener;
import eu.transkribus.swt_gui.canvas.TrpCanvasWidget;
import eu.transkribus.swt_gui.canvas.TrpSWTCanvas;
import eu.transkribus.swt_gui.collection_manager.CollectionManagerListener;
import eu.transkribus.swt_gui.dialogs.AffineTransformDialog;
import eu.transkribus.swt_gui.dialogs.BatchImageReplaceDialog;
import eu.transkribus.swt_gui.dialogs.BugDialog;
import eu.transkribus.swt_gui.dialogs.CommonExportDialog;
import eu.transkribus.swt_gui.dialogs.DebuggerDialog;
import eu.transkribus.swt_gui.dialogs.DocSyncDialog;
import eu.transkribus.swt_gui.dialogs.InstallSpecificVersionDialog;
import eu.transkribus.swt_gui.dialogs.ProgramUpdaterDialog;
import eu.transkribus.swt_gui.doc_overview.DocMetadataEditor;
import eu.transkribus.swt_gui.doc_overview.DocOverviewListener;
import eu.transkribus.swt_gui.factory.TrpShapeElementFactory;
import eu.transkribus.swt_gui.mainwidget.listener.PagesPagingToolBarListener;
import eu.transkribus.swt_gui.mainwidget.listener.RegionsPagingToolBarListener;
import eu.transkribus.swt_gui.mainwidget.listener.StorageObserver;
import eu.transkribus.swt_gui.mainwidget.listener.TranscriptObserver;
import eu.transkribus.swt_gui.mainwidget.listener.TrpMainWidgetKeyListener;
import eu.transkribus.swt_gui.mainwidget.listener.TrpMainWidgetListener;
import eu.transkribus.swt_gui.mainwidget.listener.TrpSettingsPropertyChangeListener;
import eu.transkribus.swt_gui.page_metadata.PageMetadataWidgetListener;
import eu.transkribus.swt_gui.page_metadata.TaggingWidgetListener;
import eu.transkribus.swt_gui.pagination_tables.JobTableWidgetListener;
import eu.transkribus.swt_gui.pagination_tables.TranscriptsTableWidgetListener;
import eu.transkribus.swt_gui.search.SearchDialog;
import eu.transkribus.swt_gui.structure_tree.StructureTreeListener;
import eu.transkribus.swt_gui.tools.ToolsWidgetListener;
import eu.transkribus.swt_gui.transcription.ATranscriptionWidget;
import eu.transkribus.swt_gui.transcription.LineTranscriptionWidget;
import eu.transkribus.swt_gui.transcription.listener.LineEditorListener;
import eu.transkribus.swt_gui.transcription.listener.LineTranscriptionWidgetListener;
import eu.transkribus.swt_gui.transcription.listener.WordTranscriptionWidgetListener;
import eu.transkribus.swt_gui.upload.UploadDialog;
import eu.transkribus.swt_gui.upload.UploadDialogUltimate;
import eu.transkribus.swt_gui.util.GuiUtil;
import eu.transkribus.swt_gui.util.OAuthGuiUtil;
import eu.transkribus.util.RecentDocsPreferences;

public class TrpMainWidget {
	private final static boolean USE_SPLASH = true;
	private final static Logger logger = LoggerFactory.getLogger(TrpMainWidget.class);

	private static Shell mainShell;
	// Ui stuff:
	// Display display = Display.getDefault();
	static Display display;
	TrpSWTCanvas canvas;
	TrpMainWidgetView ui;
	LoginDialog loginDialog;
	// LineTranscriptionWidget transcriptionWidget;
	HashSet<String> userCache = new HashSet<String>();
	
//	static Preferences prefNode = Preferences.userRoot().node( "/trp/recent_docs" );
//	private RecentDocsPreferences userPrefs = new RecentDocsPreferences(5, prefNode);

	public ProgramInfo info;
	public final String VERSION;
	public final String NAME;

	// Listener:
	// CanvasGlobalEventsFilter globalEventsListener;
	TrpMainWidgetKeyListener keyListener;
	PagesPagingToolBarListener pagesPagingToolBarListener;
	RegionsPagingToolBarListener lineTrRegionsPagingToolBarListener;
	RegionsPagingToolBarListener wordTrRegionsPagingToolBarListener;
	// TranscriptsPagingToolBarListener transcriptsPagingToolBarListener;
	TrpCanvasSceneListener canvasSceneListener;
	LineTranscriptionWidgetListener lineTranscriptionWidgetListener;
	WordTranscriptionWidgetListener wordTranscriptionWidgetListener;
	TrpShapeElementFactory shapeFactory;
	LineEditorListener lineEditorListener;
	StructureTreeListener structTreeListener;
	DocOverviewListener docOverviewListener;
	TrpMainWidgetListener mainWidgetListener;
	TrpCanvasContextMenuListener canvasContextMenuListener;
	TranscriptObserver transcriptObserver;
	CanvasShapeObserver canvasShapeObserver;
	PageMetadataWidgetListener metadataWidgetListener;
	TaggingWidgetListener taggingWidgetListener;
	ToolsWidgetListener laWidgetListener;
	JobTableWidgetListener jobOverviewWidgetListener;
	TranscriptsTableWidgetListener versionsWidgetListener;
	CollectionManagerListener collectionsManagerListener;

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
	
//	DebuggerDialog debugDiag;
	public static DocMetadataEditor docMetadataEditor;
	
	private Runnable updateThumbsRunnable = new Runnable() {
		@Override public void run() {
			ui.thumbnailWidget.reload();
		}
	};
	
	private TrpMainWidget(Composite parent) {
		// GlobalResourceManager.init();

		info = new ProgramInfo();
		VERSION = info.getVersion();
		NAME = info.getName();
		
		Display.setAppName(NAME+"asdf");
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
		enableAutocomplete();
		updateToolBars();

	}

	public static TrpMainWidget getInstance() {
		return mw;
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

	public void showTipsOfTheDay() {
		Collection<Object> tips = TrpConfig.getTipsOfTheDay().values();

		final TipOfTheDay tip = new TipOfTheDay();
		tip.setShowOnStartup(getTrpSets().isShowTipOfTheDay());

		for (Object tipStr : tips) {
			tip.addTip((String) tipStr);
		}

		if (tips.isEmpty())
			tip.addTip("No tip found... check your configuration!");

		tip.setStyle(TipStyle.TWO_COLUMNS);
		
		tip.open(getShell());

		getTrpSets().setShowTipOfTheDay(tip.isShowOnStartup());
//		TrpConfig.save(TrpSettings.SHOW_TIP_OF_THE_DAY_PROPERTY);
	}

	/**
	 * This method gets called in the {@link #show()} method after the UI is
	 * inited and dispayed
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
		if (tagNamesProp != null)
			CustomTagFactory.addCustomDefinedTagsToRegistry(tagNamesProp);
		
		// check for updates:
		if (getTrpSets().isCheckForUpdates()) {
			ProgramUpdaterDialog.showTrayNotificationOnAvailableUpdateAsync(ui.getShell(), VERSION, info.getTimestamp());
		}
		
		boolean TESTTABLES=false; // test-hook for sebi's table editor
		if (getTrpSets().isAutoLogin() && !TESTTABLES) {
			String lastAccount = TrpGuiPrefs.getLastLoginAccountType();
			
			if(OAuthGuiUtil.TRANSKRIBUS_ACCOUNT_TYPE.equals(lastAccount)) {
				Pair<String, String> lastLogin = TrpGuiPrefs.getLastStoredCredentials();
				if (lastLogin != null) {
					// TODO: also remember server in TrpGuiPrefs, for now: logon to prod server
					login(TrpServerConn.PROD_SERVER_URI, lastLogin.getLeft(), lastLogin.getRight(), true);
				}
			} else {
				OAuthProvider prov;
				try {
					prov = OAuthProvider.valueOf(lastAccount);
				} catch(Exception e){
					prov = null;
				}
				if(prov != null) {
					//TODO get state token from server
					final String state = "test";
					OAuthCreds creds = TrpGuiPrefs.getOAuthCreds(prov);
					try {
						loginOAuth(TrpServerConn.PROD_SERVER_URI, creds.getRefreshToken(), 
								state, OAuthGuiUtil.REDIRECT_URI, prov);
					} catch (OAuthTokenRevokedException e) {
						logger.error("OAuth token was revoked!", e);
					}
				}
			}
		}

		final boolean DISABLE_TIPS_OF_THE_DAY = true;
		if (getTrpSets().isShowTipOfTheDay() && !DISABLE_TIPS_OF_THE_DAY) {
			showTipsOfTheDay();
		}
		
		if (TESTTABLES) {
			loadLocalTestset();
		}
		
//		SWTUtil.mask2(ui.getStructureTreeWidget()); // TESt
//		MyInfiniteProgressPanel p = MyInfiniteProgressPanel.getInfiniteProgressPanelFor(ui.getStructureTreeWidget());
//		p.start();
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

	public void reloadDocList(final TrpCollection coll) {
		try {
			if (!storage.isLoggedIn())
				return;

			canvas.getScene().selectObject(null, true, false); // security
																// measure due
																// to mysterious
																// bug leading
																// to freeze of
																// progress
																// dialog
			
			getUi().getDocOverviewWidget().refreshDocList();
			
//			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
//				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
//					monitor.beginTask("Reloading doclist", IProgressMonitor.UNKNOWN);
//					try {
////						int colId = storage.getCollectionId(collectionIndex);
////						if (colId == -1)
////							return;
//						logger.debug("reloading doclist for collection "+coll.getColId());
//						storage.reloadDocList(coll.getColId());
//					} catch (Throwable e) {
//						throw new InvocationTargetException(e, e.getMessage());
//					}
//				}
//			}, "Updating documents", false);

			// update ui:
			ui.selectDocListTab();
			updatePageInfo();
			
			if (ui.getDocOverviewWidget().isCollectionManagerOpen())
				ui.getDocOverviewWidget().getCollectionManagerDialog().updateCollections();
			
//			if (storage.getRemoteDocList() != null) {
//				getUi().getDocOverviewWidget().setInput(storage.getRemoteDocList());
//				ui.selectDocListTab();
//				logger.debug("Loaded " + storage.getRemoteDocList().size() + " docs from " + storage.getServerUri());
//				updatePageInfo();
//			} else {
//				logger.debug("Failed to load doc list");
//			}
		} catch (Throwable e) {
			onError("Cannot load document list", "Could not connect to " + ui.getTrpSets().getTrpServer(), e);
		}
	}
	
//	public int getSelectedCollectionId() {
//		return ui.getDocOverviewWidget().getSelectedCollectionId();
//	}

	public void reloadJobList() {
		try {
			ui.getJobOverviewWidget().refreshPage(true);
			storage.startOrResumeJobThread();
			
//			storage.reloadJobs(!ui.getJobOverviewWidget().getShowAllJobsBtn().getSelection()); // should
																								// trigger
																								// event
																								// that
																								// updates
																								// gui!
		} catch (Exception ex) {
			onError("Error", "Error during update of jobs", ex);
		}
	}

	public void cancelJob(final String jobId) {
		try {
			storage.cancelJob(jobId);
		} catch (Exception ex) {
			onError("Error", "Error while canceling a job", ex);
		}
	}

	public void clearDocList() {
		String title = ui.APP_NAME;

		getUi().getDocOverviewWidget().clearDocList();
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
					currentCollectionStr = c.getColName()+", ID: "+c.getColId();
			}
		}

		ui.getDocOverviewWidget().setAdminAreaVisible(storage.isAdminLoggedIn());
		ui.getDocOverviewWidget().getLoadedDocText().setText(loadedDocStr);
		ui.getDocOverviewWidget().getCurrentCollectionText().setText(currentCollectionStr);
		ui.getDocOverviewWidget().updateHighlightedRow(docId);

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
		String imgUrl="", transcriptUrl="";

		int docId = -1;

		if (storage.getDoc() != null) {
			docId = storage.getDoc().getId();

			if (storage.getPage() != null) {
				fn = storage.getPage().getImgFileName() != null ? storage.getPage().getImgFileName() : "";
				key = storage.getPage().getKey() != null ? storage.getPage().getKey() : "";
				
//				imgUrl = CoreUtils.urlToString(storage.getPage().getUrl());
				if (storage.getCurrentImage()!=null)
					imgUrl = CoreUtils.urlToString(storage.getCurrentImage().url);

				pageNr = storage.getPage().getPageNr();
				
				if (storage.getTranscriptMetadata() != null && storage.getTranscriptMetadata().getUrl()!=null) {
					transcriptUrl = CoreUtils.urlToString(storage.getTranscriptMetadata().getUrl());
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
					title += " [Image Meta Info: (Resolution:"+ imgMd.getXResolution() +", w*h: " + imgMd.getWidth() + " * " + imgMd.getHeight() + ") ]";
			}
			
			TrpTextRegionType currRegion = storage.getCurrentRegionObject();
			TrpTextLineType currLine = storage.getCurrentLineObject();
			TrpWordType currWord = storage.getCurrentWordObject();
			if (currWord != null){
				java.awt.Rectangle boundingRect = PageXmlUtils.buildPolygon(currWord.getCoords().getPoints()).getBounds();
				title += " [ current word: w*h: " + boundingRect.width + " * " + boundingRect.height + " ]";
			}
			else if (currLine != null){
				java.awt.Rectangle boundingRect = PageXmlUtils.buildPolygon(currLine.getCoords().getPoints()).getBounds();
				title += " [ current line: w*h: " + boundingRect.width + " * " + boundingRect.height + " ]";
			}
			else if (currRegion != null){
				java.awt.Rectangle boundingRect = PageXmlUtils.buildPolygon(currRegion.getCoords().getPoints()).getBounds();
				title += " [ current region: w*h: " + boundingRect.width + " * " + boundingRect.height + " ]";
			}
			
		}

		ui.getDocOverviewWidget().getLoadedDocText().setText(loadedDocStr);
		// if (pageNr != -1) {
		ui.getDocOverviewWidget().getLoadedPageText().setText(fn);
//		ui.getDocOverviewWidget().getLoadedPageKey().setText(key);
		ui.getDocOverviewWidget().getLoadedImageUrl().setText(imgUrl);
		ui.getDocOverviewWidget().getLoadedTranscriptUrl().setText(transcriptUrl);
		
		// }
		ui.getDocOverviewWidget().updateHighlightedRow(docId);
		ui.getShell().setText(title);
		// updateDocMetadata();
	}

	private void addListener() {
		ui.getShell().addListener(SWT.Close, new Listener() {
			@Override public void handleEvent(Event event) {
				logger.debug("close event!");
				if (!saveTranscriptDialogOrAutosave()) {
					event.doit = false;
					return;
				}
				
				logger.debug("stopping CreateThumbsService");
				CreateThumbsService.stop(true);
				
				System.exit(0);
//				storage.finalize();
			}
		});

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
		mainWidgetListener = new TrpMainWidgetListener(this);
		canvasContextMenuListener = new TrpCanvasContextMenuListener(this);

		// pages paging toolbar listener:
		pagesPagingToolBarListener = new PagesPagingToolBarListener(ui.getPagesPagingToolBar(), this);
		// transcripts paging toolbar listener:
		// transcriptsPagingToolBarListener = new
		// TranscriptsPagingToolBarListener(ui.getTranscriptsPagingToolBar(),
		// this);
		// CanvasSceneListener acts on add / remove shape and selection change:
		canvasSceneListener = new TrpCanvasSceneListener(this);
		// add toolbar listener for transcription widgets:
		lineTrRegionsPagingToolBarListener = new RegionsPagingToolBarListener(ui.getLineTranscriptionWidget().getRegionsPagingToolBar(), this);
		wordTrRegionsPagingToolBarListener = new RegionsPagingToolBarListener(ui.getWordTranscriptionWidget().getRegionsPagingToolBar(), this);
		// act on transcription changes:
		lineTranscriptionWidgetListener = new LineTranscriptionWidgetListener(this, ui.getLineTranscriptionWidget());
		wordTranscriptionWidgetListener = new WordTranscriptionWidgetListener(this, ui.getWordTranscriptionWidget());

		// line editor listener (modify and enter pressed)
		// lineEditorListener = new LineEditorListener(this);
		// struct tree listener:
		structTreeListener = new StructureTreeListener(this);
		// doc overview listener:
		docOverviewListener = new DocOverviewListener(this);
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

		ui.thumbnailWidget.addListener(SWT.Selection, new Listener() {
			@Override public void handleEvent(Event event) {
				logger.debug("loading page " + event.index);
				jumpToPage(event.index);
			}
		});

		metadataWidgetListener = new PageMetadataWidgetListener(this);

		taggingWidgetListener = new TaggingWidgetListener(this);

		laWidgetListener = new ToolsWidgetListener(this);
		jobOverviewWidgetListener = new JobTableWidgetListener(this);
		versionsWidgetListener = new TranscriptsTableWidgetListener(this);

		// storage observer:
		storage.addObserver(new StorageObserver(this));
	}
	
	public TaggingWidgetListener getTaggingWidgetListener() { return taggingWidgetListener; }

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

			loginDialog = new LoginDialog(getShell(), message, storedUsers.toArray(new String[0]), TrpServerConn.SERVER_URIS, TrpServerConn.DEFAULT_URI_INDEX) {
				@Override protected void okPressed() {
					String server = getServerCombo().getText();
					String accType = getAccountType();
					
					boolean success;
					switch (accType){
					case "Google":
						final String state = "test";
						OAuthCreds creds = TrpGuiPrefs.getOAuthCreds(OAuthProvider.Google);
						if(creds == null) {
							success = false;
						} else {
							try {
								success = loginOAuth(server, creds.getRefreshToken(), state, OAuthGuiUtil.REDIRECT_URI, OAuthProvider.Google);
							} catch (OAuthTokenRevokedException oau) {
								// get new consent
								TrpGuiPrefs.clearOAuthToken(OAuthProvider.Google);
								String code;
								try {
									code = OAuthGuiUtil.getUserConsent(this.getShell(), state, OAuthProvider.Google);
									if(code == null) {
										success = false;
									} else {
										success = OAuthGuiUtil.authorizeOAuth(server, code, state, OAuthProvider.Google);
									}
								} catch (IOException e) {
									success = false;
								}
							}
						}
						break;
					default: //Transkribus
						String user = getUser();
						char[] pw = getPassword();
						boolean rememberCreds = isRememberCredentials();
						success = login(server, user, String.valueOf(pw), rememberCreds);
						break;
					}
					
					if (success) {
						close();
						onSuccessfullLoginAndDialogIsClosed();
					} else {
						setInfo("Login failed!");
					}
				}

				@Override protected void postInit() {
					DataBinder db = DataBinder.get();
					
					db.bindBeanToWidgetSelection(TrpSettings.AUTO_LOGIN_PROPERTY, getTrpSets(), autoLogin);
					
				}
			};
			loginDialog.open();

			// attachListener();
		} catch (Throwable e) {
			onError("Error during login", "Unable to login to server", e);
			ui.updateLoginInfo(false, "", "");
		}
	}

	/**
	 * Gets called when the login dialog is closed by a successful login attempt.<br>
	 * It's a verbose method name, I know ;-)
	 */
	protected void onSuccessfullLoginAndDialogIsClosed() {
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
		if (!RecentDocsPreferences.getItems().isEmpty()){
			if (RecentDocsPreferences.isShowOnStartup()){
				String docToLoad = RecentDocsPreferences.getItems().get(0);
				loadRecentDoc(docToLoad);
			}
		}
		else{
			//if no recent docs are available -> load the example doc
			loadRemoteDoc(5014, 4);
			getUi().getDocOverviewWidget().setSelectedCollection(4, true);
			getUi().getDocOverviewWidget().getDocTableWidget().loadPage("docId", 5014, true);
		}
		
		reloadJobList();
//		reloadDocList(ui.getDocOverviewWidget().getSelectedCollection());
//		reloadHtrModels();
		// reloadJobListForDocument();
	}

	public boolean login(String server, String user, String pw, boolean rememberCredentials) {
		try {
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
			return true;
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
	
	public void loadRecentDoc(String docToLoad) {
		String[] tmp = docToLoad.split(";;;");
		if (tmp.length == 1){
			if (new File(tmp[0]).exists()){
				loadLocalDoc(tmp[0]);
			}
			else{
				DialogUtil.createAndShowBalloonToolTip(getShell(), SWT.ICON_ERROR, "Loading Error", "Local folder does not exist anymore", 2, true);
			}
		}
		else if (tmp.length == 3){
//			for (int i = 0; i < tmp.length; i++){
//				logger.debug(" split : " + tmp[i]);
//			}
			int docid = Integer.valueOf(tmp[1]);
			int colid = Integer.valueOf(tmp[2]); 
			
			List<TrpDocMetadata> docList;
			try {
				docList = storage.getConnection().findDocuments(colid, docid, "", "", "", "", true, false, 0, 0, null, null);
				if (docList != null && docList.size() > 0){
					if (loadRemoteDoc(docid, colid)){
						getUi().getDocOverviewWidget().setSelectedCollection(colid, true);
						getUi().getDocOverviewWidget().getDocTableWidget().loadPage("docId", docid, true);
					}
				}
				else{
					//DialogUtil.createAndShowBalloonToolTip(getShell(), SWT.ICON_ERROR, "Loading Error", "Last used document is not on this server", 2, true);
				}
			} catch (SessionExpiredException | ServerErrorException | ClientErrorException
					| IllegalArgumentException e) {
				// DO NOTHING
			}

		}
		
		
	}

	public boolean loginOAuth(final String server, final String refreshToken, final String state, final String redirectUri, final OAuthProvider prov) throws OAuthTokenRevokedException {
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

			reloadJobList();
//			reloadDocList(ui.getDocOverviewWidget().getSelectedCollection());
//			reloadHtrModels();
			// reloadJobListForDocument();
			sessionExpired = false;
			lastLoginServer = server;
			return true;
		} catch(OAuthTokenRevokedException oau){
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

		clearDocList();
//		clearHtrModelList();
		ui.getVersionsWidget().refreshPage(true);
		ui.getJobOverviewWidget().refreshPage(true);
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
			storage.updateDocMd(colId);
			// DialogUtil.createAndShowBalloonToolTip(getShell(),
			// SWT.ICON_ERROR, "Success saving doc-metadata", "", 2, true);
		} catch (Exception e) {
			onError("Error saving doc-metadata", e.getMessage(), e, true, true);
			// DialogUtil.createAndShowBalloonToolTip(getShell(),
			// SWT.ICON_ERROR, "Error saving doc-metadata", e.getMessage(), 2,
			// true);
		}

		// // update doc metadata on doc and reload doc list:
		// try {
		// final int colId = storage.getCurrentCollectionId();
		// ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
		// @Override public void run(IProgressMonitor monitor) throws
		// InvocationTargetException, InterruptedException {
		// monitor.beginTask("Saving metadata", IProgressMonitor.UNKNOWN);
		// logger.debug("applying metadata...");
		// try {
		// storage.updateDocMd(colId);
		// } catch (Exception e) {
		// throw new InvocationTargetException(e, e.getMessage());
		// }
		// }
		// }, "Saving metadata", false);
		// reloadDocList(ui.getDocOverviewWidget().getSelectedCollectionIndex());
		// } catch (Throwable e) {
		// onError("Saving Error", "Error while storing metadata on server", e);
		// }
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
			logger.debug("commitMessage = "+commitMessage);

			final int colId = storage.getCurrentDocumentCollectionId();
			// canvas.getScene().selectObject(null, true, false); // security
			// measure due to mysterious bug leading to freeze of progress
			// dialog
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						logger.debug("saving transcription, commitMessage = "+commitMessage);
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
			ui.setStatusMessage("Successfully saved data!", 5000);
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

	public void updateTranscriptionWidget(ATranscriptionWidget.Type type) {
		ATranscriptionWidget aw = null;
		if (type == ATranscriptionWidget.Type.WORD_BASED)
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
		updateTranscriptionWidget(ATranscriptionWidget.Type.WORD_BASED);
	}

	public void updateLineTranscriptionWidgetData() {
		updateTranscriptionWidget(ATranscriptionWidget.Type.LINE_BASED);
	}

	public void jumpToPage(int index) {
		if (saveTranscriptDialogOrAutosave()) {
			if (storage.setCurrentPage(index)) {
				reloadCurrentPage(true);
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

			ui.getCanvasWidget().getToolBar().getEditingEnabledToolItem().setEnabled(!isPageLocked);
			TrpConfig.getCanvasSettings().setEditingEnabled(!isPageLocked);
			ui.getTranscriptionComposite().setEnabled(!isPageLocked);
			ui.getRightTabFolder().setEnabled(!isPageLocked);
			ui.getSaveTranscriptButton().setEnabled(!isPageLocked);
			ui.getSaveTranscriptWithMessageButton().setEnabled(!isPageLocked);

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
			ui.taggingWidget.setSelectedTags(null);
			ui.getMetadataWidget().updateData(null, null, nSel, null, null, new ArrayList<CustomTag>());
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

		// get text style(s) for selection:
		boolean isSelectedInTranscriptionWidget = isTextSelectedInTranscriptionWidget();
		logger.debug("isSelectedInTranscriptionWidget = " + isSelectedInTranscriptionWidget);
		TextStyleType textStyle = new TextStyleType();

		if (st != null) {
			if (!getTrpSets().isEnableIndexedStyles()) { // OUTDATED
				textStyle = canvas.getScene().getCommonTextStyleOfSelected();
			} else { // get common TextStyleType for selection
				ATranscriptionWidget aw = ui.getSelectedTranscriptionWidget();
				if (aw != null) {
					TextStyleTag tst = aw.getCommonIndexedCustomTagForCurrentSelection(TextStyleTag.TAG_NAME);
					if (tst != null)
						textStyle = tst.getTextStyle();
				}
			}
		}

		ui.taggingWidget.setSelectedTags(selectedTags);
		ui.getMetadataWidget().updateData(storage.getTranscript(), st, nSel, structureType, textStyle, selectedTags);
		

	}

	public void updateTreeSelectionFromCanvas() {
		if (structTreeListener.isInsideTreeSelectionEvent) {
			// logger.debug("not updating tree!!");
			return;
		}

		List<Object> selData = canvas.getScene().getSelectedData();
		
		// select lines for baselines in struct view if lines not visible: 
		if (!getTrpSets().isShowLines()) {
			for (int i=0; i<selData.size(); ++i) {
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
			logger.info("loading page: "+storage.getPage());
			clearCurrentPage();

			final int colId = storage.getCurrentDocumentCollectionId();
			final String fileType = mw.getSelectedImageFileType();
			logger.debug("selected img filetype = "+fileType);
			
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
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
			CreateThumbsService.createThumbs(storage.getPage(), storage.getCurrentImage().img, false, updateThumbsRunnable);
		}
	}

	public void updateThumbs() {
		logger.trace("updating thumbs");
		
		Display.getDefault().asyncExec(updateThumbsRunnable); // asyncExec needed??

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

			ui.taggingWidget.updateAvailableTags();
			updateTranscriptionWidgetsData();
			canvas.getScene().updateSegmentationViewSettings();

			logger.debug("loaded transcript - edited = " + storage.isTranscriptEdited());
		} catch (Throwable th) {
			String msg = "Could not load transcript for page " + (storage.getPageIndex() + 1);
			onError("Error loading transcript", msg, th);
			clearTranscriptFromView();
		}
	}
	
	public void showLocation(CustomTag t) {
		showLocation(new TrpLocation(t));
	}

	public void showLocation(TrpLocation l) {
		// if (l.md == null) {
		// DialogUtil.showErrorMessageBox(getShell(),
		// "Error showing custom tag",
		// "Cannot open custom tag - no related metadata found!");
		// return;
		// }

		logger.info("showing loation: " + l);

		// 1st: load doc & page
		if (!l.hasDoc()) {
			logger.info("location has no doc specified!");
			return;
		}
		int pageIndex = l.hasPage() ? l.pageNr -1 : 0;

		boolean wasDocLoaded=false;
		if (!storage.isThisDocLoaded(l.docId, l.localFolder)) {
			wasDocLoaded = true;
			if (l.docId == -1) {
				if (!loadLocalDoc(l.localFolder.getAbsolutePath(), pageIndex))
					return;
			} else {
				if (!loadRemoteDoc(l.docId, l.collectionId, pageIndex))
					return;
			}
		}
		
		// 2nd: load page if not loaded by doc anyway:
		if (!l.hasPage()) {
			logger.info("location has no page specified!");
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
		
		if (s == null)
			return;
		
		canvas.focusShape(s);

		// 4th: select tag in transcription widget; TODO: select word!?
		if (l.t == null) {
			logger.info("location has no tag specified!");
			return;
		}

		boolean isLine = l.getLowestShapeType() instanceof TrpTextLineType;
		boolean isWord = l.getLowestShapeType() instanceof TrpWordType;
		ATranscriptionWidget tw = ui.getSelectedTranscriptionWidget();
		if (isLine && tw instanceof LineTranscriptionWidget) {
			tw.selectCustomTag(l.t);
		}

	}

	private void clearTranscriptFromView() {
		getUi().getStructureTreeViewer().setInput(null);
		getCanvas().getScene().clearShapes();
		getCanvas().redraw();
	}

	// @SuppressWarnings("rawtypes")
	private void loadJAXBTranscriptIntoView(JAXBPageTranscript transcript) throws Exception {

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

	public TrpCanvasWidget getCanvasWidget() {
		return ui.getCanvasWidget();
	}

	public TrpSWTCanvas getCanvas() {
		return canvas;
	}

	public TrpCanvasScene getScene() {
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
		if (ui.getPagesPagingToolBar().getLabelItem() != null) {
			ui.getPagesPagingToolBar().getLabelItem().setImage(isPageLocked ? Images.LOCK : null);
			ui.getPagesPagingToolBar().getLabelItem().setToolTipText(isPageLocked ? "Page locked" : "");
		}

		ui.getCloseDocBtn().setEnabled(isDocLoaded);
		ui.getSaveTranscriptButton().setEnabled(isDocLoaded);
		ui.getSaveTranscriptWithMessageButton().setEnabled(isDocLoaded);
		ui.getReloadDocumentButton().setEnabled(isDocLoaded);
		ui.getLoadTranscriptInTextEditor().setEnabled(isDocLoaded);

		ui.updateToolBarSize();
	}

	public void loadLocalTestset() {
		String localTestdoc = "";
		
		if (SysUtils.isWin()) {
			localTestdoc = "C:/Schauplatz_small";
		}
		else if (SysUtils.isOsx()) {
			localTestdoc = "/Users/hansm/Documents/testDocs/Bentham_box_035/";
		}
		else {
//			localTestdoc = System.getProperty( "user.home" )+"/Transkribus_TestDoc";
			localTestdoc = "/mnt/dea_scratch/TRP/Transkribus_TestDoc";
//			localTestdoc = System.getProperty( "user.home" )+"/testdocmanybl";
		}
		
		File f = new File(localTestdoc);
		if (!f.isDirectory()) {
			DialogUtil.showErrorMessageBox(getShell(), "Error loading local testset", "The local testset directory does not exist:\n "+f.getAbsolutePath());
			return;
		}

		loadLocalDoc(localTestdoc, 0);
	}
	
	public void loadRemoteTestset() {
		// TODO
		
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
			storage.loadLocalDoc(folder);

			if (getTrpSets().isCreateThumbs()) {
				CreateThumbsService.createThumbs(storage.getDoc(), false, updateThumbsRunnable);
			}

			storage.setCurrentPage(pageIndex);
			reloadCurrentPage(true);
			
			//store the path for the local doc
			RecentDocsPreferences.push(folder);
			ui.getDocOverviewWidget().updateRecentDocs();
			
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
		String fn = DialogUtil.showOpenFolderDialog(getShell(), "Choose a folder with images and page files", lastLocalDocFolder);
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
	 * @param docId The id of the document to load
	 * @param colId The id of the collection to load the document from. 
	 * A colId <= 0 means, the currently selected collection from the DocOverViewWidget is taken (if one is selected!)
	 * @return True for success, false otherwise
	 */
	public boolean loadRemoteDoc(final int docId, int colId, int pageIndex) {
		if (!saveTranscriptDialogOrAutosave()) {
			return false;
		}

		try {
			canvas.getScene().selectObject(null, true, false); // security
																// measure due
																// to mysterios
																// bug leading
																// to freeze of
																// progress
																// dialog
			
			
			if (colId <= 0) {
				colId = ui.getDocOverviewWidget().getSelectedCollectionId();
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
			
			//store the recent doc info to the preferences
			RecentDocsPreferences.push(Storage.getInstance().getDoc().getMd().getTitle() + ";;;" + docId + ";;;" + colIdFinal);
			ui.getDocOverviewWidget().updateRecentDocs();
				
			updateThumbs();
			getCanvas().fitWidth();
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

	public static void show() {
		ProgramInfo info = new ProgramInfo();
		Display.setAppName(info.getName());
		Display.setAppVersion(info.getVersion());
		
		show(null);
	}

	public static void show(Display givenDisplay) {
		GuiUtil.initLogger();
		try {
			// final Display display = Display.getDefault();

			if (givenDisplay != null)
				display = givenDisplay;
			else
				display = new Display();

			final Shell shell = new Shell(display, SWT.SHELL_TRIM);
			setMainShell(shell);
			// final Shell shell = new Shell(display, SWT.NO_TRIM | SWT.ON_TOP);

			Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
				@Override public void run() {
					if (USE_SPLASH) {
						final SplashWindow sw = new SplashWindow(display);
						sw.start(new Runnable() {
							@Override public void run() {
								sw.setProgress(10);
								shell.setLayout(new FillLayout());
								mw = new TrpMainWidget(shell);
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
						shell.open();
						shell.layout();
						mw.postInit();
					}

					// the main display loop:
					logger.debug("entering main event loop");
					// while((Display.getCurrent().getShells().length != 0)
					// && !Display.getCurrent().getShells()[0].isDisposed()) {
					while (!shell.isDisposed()) {
						if (!Display.getCurrent().readAndDispatch()) {
							Display.getCurrent().sleep();
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

	public void enableAutocomplete() {
		ui.lineTranscriptionWidget.getAutoComplete().getAdapter().setEnabled(getTrpSets().isAutocomplete());
		ui.wordTranscriptionWidget.getAutoComplete().getAdapter().setEnabled(getTrpSets().isAutocomplete());
		canvas.getLineEditor().getAutoComplete().getAdapter().setEnabled(getTrpSets().isAutocomplete());

		// trpTranscriptionWidget.getAutoComplete().getAdapter().setEnabled(selection);
	}

	/** 
	 * replaced by {@link #uploadDocuments()}
	 */
	@Deprecated
	public void uploadSingleDocument() {
		try {
			if (!storage.isLoggedIn()) {
				DialogUtil.showErrorMessageBox(getShell(), "Not logged in!", "You have to be logged in to upload a document!");
				return;
			}

			final UploadDialog ud = new UploadDialog(getShell(), ui.getDocOverviewWidget().getSelectedCollection());
			int ret = ud.open();
			

			if (ret == IDialogConstants.OK_ID) {
				final TrpCollection c = ud.getCollection();
				final int cId = (c == null) ? -1 : c.getColId();
				if (c == null || (c.getRole() != null && !c.getRole().canManage())) {
					throw new Exception("Cannot upload to specified collection: " + cId);
				}

				logger.debug("uploading to directory: " + ud.getFolder() + ", title: '" + ud.getTitle() + " collection: " + cId+" viaFtp: "+ud.isUploadViaFtp());
				String type = ud.isUploadViaFtp() ? "FTP" : "HTTP";

				// final int colId =
				// storage.getCollectionId(ui.getDocOverviewWidget().getSelectedCollectionIndex());
				ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
					@Override public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							// storage.uploadDocument(4, ud.getFolder(),
							// ud.getTitle(), monitor);// TEST
							boolean uploadViaFTP = ud.isUploadViaFtp();
							logger.debug("uploadViaFTP = "+uploadViaFTP);
							storage.uploadDocument(cId, ud.getFolder(), ud.getTitle(), monitor);
							if (!monitor.isCanceled())
								displaySuccessMessage("Uploaded document!\nNote: the document will be ready after document processing on the server is finished - reload the document list occasionally");
						} catch (Exception e) {
							throw new InvocationTargetException(e);
						}
					}
				}, "Uploading via "+type, true);
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

//			final UploadFromFtpDialog ud = new UploadFromFtpDialog(getShell(), ui.getDocOverviewWidget().getSelectedCollection());
			final UploadDialogUltimate ud = new UploadDialogUltimate(getShell(), ui.getDocOverviewWidget().getSelectedCollection());
			if (ud.open() != IDialogConstants.OK_ID)
				return;

			final TrpCollection c = ud.getCollection();
			final int cId = (c == null) ? -1 : c.getColId();
			if (c == null || (c.getRole() != null && !c.getRole().canManage())) {
				throw new Exception("Cannot upload to specified collection: " + cId);
			}
			
			if (ud.isSingleDocUpload()) { // single doc upload
				logger.debug("uploading to directory: " + ud.getFolder() + ", title: '" + ud.getTitle() + " collection: " + cId+" viaFtp: "+ud.isSingleUploadViaFtp());
				String type = ud.isSingleUploadViaFtp() ? "FTP" : "HTTP";

				// final int colId =
				// storage.getCollectionId(ui.getDocOverviewWidget().getSelectedCollectionIndex());
				ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
					@Override public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							// storage.uploadDocument(4, ud.getFolder(),
							// ud.getTitle(), monitor);// TEST
							boolean uploadViaFTP = ud.isSingleUploadViaFtp();
							logger.debug("uploadViaFTP = "+uploadViaFTP);
							storage.uploadDocument(cId, ud.getFolder(), ud.getTitle(), monitor);
							if (!monitor.isCanceled())
								displaySuccessMessage("Uploaded document!\nNote: the document will be ready after document processing on the server is finished - reload the document list occasionally");
						} catch (Exception e) {
							throw new InvocationTargetException(e);
						}
					}
				}, "Uploading via "+type, true);
			} else if (ud.isMetsUrlUpload()){
				logger.debug("uploading title: " + ud.getTitle() + " to collection: " +cId);
				//test url: http://rosdok.uni-rostock.de/file/rosdok_document_0000007322/rosdok_derivate_0000026952/ppn778418405.dv.mets.xml
				int h = DialogUtil.showInfoMessageBox(getShell(), "Upload Information", "Upload document!\nNote: the document will be ready after document processing on the server is finished - this takes a while - reload the document list occasionally");
				try {
					storage.uploadDocumentFromMetsUrl(cId, ud.getMetsUrl());
				}catch (ClientErrorException e){
					if(e.getMessage().contains("DFG-Viewer Standard")){
						onError("Error during uploading from Mets URL - reason: ", e.getMessage(), e);
					}
					else{
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
				logger.debug("ingest into collection: " + cId+" viaFtp: "+ud.isSingleUploadViaFtp());
				String type = ud.isSingleUploadViaFtp() ? "FTP" : "HTTP";

				// final int colId =
				// storage.getCollectionId(ui.getDocOverviewWidget().getSelectedCollectionIndex());
				ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
					@Override public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							// storage.uploadDocument(4, ud.getFolder(),
							// ud.getTitle(), monitor);// TEST
							boolean uploadViaFTP = ud.isSingleUploadViaFtp();
							logger.debug("uploadViaFTP = "+uploadViaFTP);
							storage.uploadDocumentFromPdf(cId, ud.getFile(), ud.getPdfFolder(), monitor);
							if (!monitor.isCanceled())
								displaySuccessMessage("Uploaded document!\nNote: the document will be ready after document processing on the server is finished - reload the document list occasionally");
						} catch (Exception e) {
							throw new InvocationTargetException(e);
						}
					}
				}, "Uploading via "+type, true);
				
			} else { // private ftp ingest
				final List<TrpDocDir> dirs = ud.getDocDirs();
				if(dirs == null || dirs.isEmpty()){
					//should not happen. check is already done in Dialog...
					throw new Exception("DocDir list is empty!");
				}
				
				DialogUtil.createAndShowBalloonToolTip(getShell(), SWT.ICON_INFORMATION, "FTP Upload", "The FTP upload runs as background process and last for some time.\n"
					+ "Look into Jobs tab!\nReloading the collection shows the already uploaded documents.", 2, true);
							
				for(final TrpDocDir d : dirs) {
//					String docTitle = d.getMetadata()==null ? d.getName() : d.getMetadata().getTitle();
					try {
						storage.uploadDocumentFromPrivateFtp(cId, d.getName(), true);
					} catch (final ClientErrorException ie) {
						
						if (ie.getResponse().getStatus() == 409) { // conflict! (= duplicate name)
							if (DialogUtil.showYesNoDialog(getShell(), "Duplicate title", ie.getMessage()+"\n\nIngest anyway?") == SWT.YES) {
								storage.uploadDocumentFromPrivateFtp(cId, d.getName(), false);
							}
						}
					}
				}
				
				ui.selectJobListTab();
				ui.getJobOverviewWidget().refreshPage(false);

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
		ui.getRightTabFolder().setEnabled(value);
	}

	public static void main(String[] args) throws IOException {
		// TEST:
		// TrpGui.getMD5sOfLibs();

		// Dynamically load the correct swt jar depending on OS:

		TrpMainWidget.show();
		// TrpMainWidget mainWidget = new TrpMainWidget();
	}

	@Deprecated public void deleteSelectedDocument() {
		final TrpDocMetadata doc = ui.getDocOverviewWidget().getSelectedDocument();
		try {
			if (doc == null || !storage.isLoggedIn()) {
				return;
			}

			if (DialogUtil.showYesNoDialog(getShell(), "Are you sure?", "Do you really want to delete document " + doc.getDocId()) != SWT.YES) {
				return;
			}

			canvas.getScene().selectObject(null, true, false); // security
																// measure due
																// to mysterios
																// bug leading
																// to freeze of
																// progress
																// dialog
			final int colId = storage.getCurrentDocumentCollectionId();
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						logger.debug("deleting document...");
						monitor.beginTask("Deleting document ", IProgressMonitor.UNKNOWN);
						logger.debug("Deleting selected document: " + doc);
						storage.deleteDocument(colId, doc.getDocId());
						displaySuccessMessage("Deleted document " + doc.getDocId());
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Exporting", false);

			reloadDocList(ui.getDocOverviewWidget().getSelectedCollection());
		} catch (Throwable e) {
			onError("Error deleting document", "Could not delete document " + doc.getDocId(), e);
		}
	}

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
			if (storage.isTranscriptEdited()){
				storage.getTranscript().getPage().setEdited(false);
			}
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						logger.debug("Delete page: " + storage.getPage().getPageNr());
						
						monitor.beginTask("Deleting page "+storage.getPage().getPageNr(), 1);
						
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
		final String[] extArr = new String[] { "*.jpg", "*.jpeg", "*.tiff", "*.tif", "*.TIF", "*.TIFF", "*.png"};
		final String fn = DialogUtil.showOpenFileDialog(getShell(), "Select image file", null, extArr);
		if (fn == null)
			return;

		try {
			//check img file
			final File imgFile = new File(fn);
			if(!imgFile.canRead())
				throw new Exception("Can't read file at: " + fn);
			
			
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						logger.debug("Replacing file: " + fn);
						monitor.beginTask("Uploading image file", 120);
						//replace on server
						storage.replacePageImgFile(imgFile, monitor);

						for(int i = 1; i <= 2; i++){
							Thread.sleep(1000);
							monitor.worked(100+(i*10));
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
	
	public void unifiedExport() {
		File dir = null;
		try {

			if (!storage.isDocLoaded()) {
				DialogUtil.showErrorMessageBox(getShell(), "No document loaded", "You first have to open a document that shall be exported!");
				return;
			}

			/*
			 * preselect document title for export folder name filter out all
			 * unwanted chars
			 */
			String adjTitle = getAdjustedDocTitle();

			saveTranscriptDialogOrAutosave();
				
			final CommonExportDialog exportDiag = new CommonExportDialog(getShell(), SWT.NONE, lastExportFolder, adjTitle, storage.getDoc().getPages());

			dir = exportDiag.open();
			if (dir == null)
				return;
			
			if (!dir.exists()){
				dir.mkdir();
			}
			
			String exportFormats = "";
			String exportFileOrDir = dir.getAbsolutePath();
			Set<Integer> pageIndices = null;
			
			boolean doZipExport = false;
			
			boolean doMetsExport = false;
			boolean doPdfExport = false;
			boolean doDocxExport = false;
			boolean doTeiExport = false;
			boolean doXlsxExport = false;
			
			String tempDir = null;

			String metsExportDirString = dir.getAbsolutePath() + "/" + dir.getName();
			File metsExportDir = new File(metsExportDirString);
			
			String pdfExportFileOrDir = dir.getAbsolutePath() + "/" + dir.getName() + ".pdf";
			File pdfExportFile = new File(pdfExportFileOrDir);	
			
			String teiExportFileOrDir = dir.getAbsolutePath() + "/" + dir.getName() + "_tei.xml";
			File teiExportFile = new File(teiExportFileOrDir);
			
			String docxExportFileOrDir = dir.getAbsolutePath() + "/" + dir.getName() + ".docx";
			File docxExportFile = new File(docxExportFileOrDir);
			
			String xlsxExportFileOrDir = dir.getAbsolutePath() + "/" + dir.getName() + ".xlsx";
			File xlsxExportFile = new File(xlsxExportFileOrDir);
			
			String zipExportFileOrDir = dir.getAbsolutePath() + "/" + dir.getName() + ".zip";
			File zipExportFile = new File(zipExportFileOrDir);
			
			/*
			 * only check export path if it is not ZIP because than we check just the ZIP location
			 */
			if (!exportDiag.isZipExport()){
				doMetsExport = (exportDiag.isMetsExport() && exportDiag.getExportPathComp().checkExportFile(metsExportDir, null, getShell()));
			
				doPdfExport = (exportDiag.isPdfExport() && exportDiag.getExportPathComp().checkExportFile(pdfExportFile, ".pdf", getShell()));	
			
				doTeiExport = (exportDiag.isTeiExport() && exportDiag.getExportPathComp().checkExportFile(teiExportFile, ".xml", getShell()));

				doDocxExport = (exportDiag.isDocxExport() && exportDiag.getExportPathComp().checkExportFile(docxExportFile, ".docx", getShell()));

				doXlsxExport = (exportDiag.isXlsxExport() && exportDiag.getExportPathComp().checkExportFile(xlsxExportFile, ".xlsx", getShell()));
			}
			
			doZipExport = (exportDiag.isZipExport() && exportDiag.getExportPathComp().checkExportFile(zipExportFile, ".zip", getShell()));
			
			if (doZipExport){
				tempDir = System.getProperty("java.io.tmpdir");
				//logger.debug("temp dir is ..." + tempDir);
			}
			
			final String fileNamePattern = exportDiag.getFileNamePattern();
			
			if (!doMetsExport && !doPdfExport && !doTeiExport && !doDocxExport && !doXlsxExport && !doZipExport){
				/*
				 * if the export file exists and the user wants not to overwrite it then the 
				 * export dialog shows up again with the possibility to choose another location
				 * --> comment out if export should close instead
				 */
				unifiedExport();
				return;
			}
			

			if (exportDiag.isPageableExport()) {
				pageIndices = exportDiag.getSelectedPages();
				if (pageIndices == null) {
					DialogUtil.showErrorMessageBox(getShell(), "Error parsing page ranges", "Error parsing page ranges");
					return;
				}
			}
			
			final Set<Integer> copyOfPageIndices = pageIndices;
			Set<String> selectedTags = null;
			
			logger.debug("loading transcripts..." + copyOfPageIndices.size());
			
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						//logger.debug("loading transcripts...");
						monitor.beginTask("Loading transcripts...", copyOfPageIndices.size());
						//unmarshal the page transcript only once
						ExportUtils.storePageTranscripts4Export(storage.getDoc(), copyOfPageIndices, monitor, exportDiag.getVersionStatus(), storage.getPageIndex(), storage.getTranscript().getMd());

						monitor.done();
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Loading of transcripts: ", true);
			
			logger.debug("transcripts loaded");
			
			
			
			if (exportDiag.isTagableExportChosen()) {
							
				selectedTags = exportDiag.getSelectedTagsList();
				
				logger.debug("loading tags..." + selectedTags.size());
				
				final Set<String> copyOfSelectedTags = selectedTags;

				ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
					@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							//logger.debug("loading transcripts...");
							monitor.beginTask("Loading tags...", copyOfPageIndices.size());
							ExportUtils.storeCustomTagMapForDoc(storage.getDoc(), exportDiag.isWordBased(), copyOfPageIndices, monitor, exportDiag.isDoBlackening());
							if (copyOfSelectedTags == null) {
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
			
			boolean wordBased = exportDiag.isWordBased();
			boolean doBlackening = exportDiag.isDoBlackening();
			boolean createTitle = exportDiag.isCreateTitlePage();
			
			if (doZipExport){
				
				if (tempDir == null)
					return;
				
				String tempZipDirParent = tempDir + "/" + dir.getName();
				File tempZipDirParentFile = new File(tempZipDirParent);
				
				if (tempZipDirParentFile.exists()){
				    Random randomGenerator = new Random();
				    int randomInt = randomGenerator.nextInt(1000);
				    tempZipDirParent = tempZipDirParent.concat(Integer.toString(randomInt));
					tempZipDirParentFile = new File(tempZipDirParent);
				}
				
				String tempZipDir = tempZipDirParent + "/" + dir.getName();
				File tempZipFileDir = new File(tempZipDir);
				FileUtils.forceMkdir(tempZipFileDir);
				
				if (exportDiag.isMetsExport())
					exportDocument(tempZipFileDir, pageIndices, exportDiag.isImgExport(), exportDiag.isPageExport(), exportDiag.isAltoExport(), fileNamePattern);
				if (exportDiag.isPdfExport())
					exportPdf(new File(tempZipDirParent + "/" + dir.getName() + ".pdf"), pageIndices, exportDiag.isAddExtraTextPages2PDF(), exportDiag.isExportImagesOnly(), selectedTags, exportDiag.isHighlightTags(), wordBased, doBlackening, createTitle);
				if (exportDiag.isTeiExport())
					exportTei(new File(tempZipDirParent + "/" + dir.getName() + ".xml"), exportDiag);
				if (exportDiag.isDocxExport())
					exportDocx(new File(tempZipDirParent + "/" + dir.getName() + ".docx"), pageIndices, wordBased, exportDiag.isTagExport(), doBlackening, selectedTags, createTitle, exportDiag.isMarkUnclearWords(), exportDiag.isExpandAbbrevs(), exportDiag.isSubstituteAbbreviations(), exportDiag.isPreserveLinebreaks());
				if (exportDiag.isXlsxExport())
					exportXlsx(new File(tempZipDirParent + "/" + dir.getName() + ".xlsx"), pageIndices, exportDiag.isWordBased(), exportDiag.isTagExport(), selectedTags);
				
				//createZipFromFolder(tempZipDirParentFile.getAbsolutePath(), dir.getParentFile().getAbsolutePath() + "/" + dir.getName() + ".zip");
				createZipFromFolder(tempZipDirParentFile.getAbsolutePath(), zipExportFile.getAbsolutePath());
				
				if (exportFormats != "") {
					exportFormats += " and ";
				}
				exportFormats += "ZIP";
				
				for (File f : tempZipDirParentFile.listFiles()){
					f.delete();
				}
				
				lastExportFolder = dir.getParentFile().getAbsolutePath();
				logger.debug("last export folder: " + lastExportFolder);

				//delete the temp folder for making the ZIP
				FileDeleteStrategy.FORCE.delete(tempZipDirParentFile);
				
				if (exportFormats != "") {
					displaySuccessMessage("Sucessfully written " + exportFormats + " to " + exportFileOrDir);
				}
				
				//export was done via ZIP and is completed now
				return;
				
			}

			if (doMetsExport) {

				exportDocument(metsExportDir, pageIndices, exportDiag.isImgExport(), exportDiag.isPageExport(), exportDiag.isAltoExport(), fileNamePattern);
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
				
			}

			if (doPdfExport) {

				exportPdf(pdfExportFile, pageIndices, exportDiag.isAddExtraTextPages2PDF(), exportDiag.isExportImagesOnly(), selectedTags, exportDiag.isHighlightTags(), wordBased, doBlackening, createTitle);
				if (exportFormats != "") {
					exportFormats += " and ";
				}
				exportFormats += "PDF";
				
			}

			if (doTeiExport) {
				
				exportTei(teiExportFile, exportDiag);
				if (exportFormats != "") {
					exportFormats += " and ";
				}
				exportFormats += "TEI";
				
			}

			if (doDocxExport) {

				exportDocx(docxExportFile, pageIndices, wordBased, exportDiag.isTagExport(), doBlackening, selectedTags, createTitle, exportDiag.isMarkUnclearWords(), exportDiag.isExpandAbbrevs(), exportDiag.isSubstituteAbbreviations(), exportDiag.isPreserveLinebreaks());
				if (exportFormats != "") {
					exportFormats += " and ";
				}
				exportFormats += "DOCX";
				
			}

			if (doXlsxExport) {
				
				exportXlsx(xlsxExportFile, pageIndices, exportDiag.isWordBased(), exportDiag.isTagExport(), selectedTags);
				if (exportFormats != "") {
					exportFormats += " and ";
				}
				exportFormats += "Xlsx";
				
			}

			if (exportFormats != "") {
				displaySuccessMessage("Sucessfully written " + exportFormats + " to " + exportFileOrDir);
			}

		} catch (Throwable e) {
			if (e instanceof InterruptedException){
				DialogUtil.showInfoMessageBox(getShell(), "Export canceled", "Export was canceled");
			}
			else{
				logger.error(e.getMessage(), e);
			}
			// onError("Export error", "Error during export of document", e);
		}
		finally{
			if(dir != null){
				lastExportFolder = dir.getParentFile().getAbsolutePath();
			}
		}

	}

	// public void exportDocument() {
	// try {
	// if (!storage.isDocLoaded()) {
	// DialogUtil.showErrorMessageBox(getShell(), "No document loaded",
	// "You first have to open a document that shall be exported!");
	// return;
	// }
	//
	// /*
	// * preselect document title for export folder name
	// * filter out all unwanted chars
	// *
	// * */
	// String title = storage.getDoc().getMd().getTitle();
	// title = title.replaceAll("([/\\?%*:| \"<>. ])", "_");
	// final DocExportDialog exportDiag = new DocExportDialog(getShell(),
	// SWT.NONE, lastExportFolder, title);
	// final File dir = exportDiag.open();
	//
	// if (dir == null)
	// return;
	//
	// lastExportFolder = dir.getParent();
	// ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
	// @Override public void run(IProgressMonitor monitor) throws
	// InvocationTargetException, InterruptedException {
	// try {
	// logger.debug("exporting document...");
	// monitor.beginTask("Exporting document", storage.getNPages());
	// final String path = storage.exportDocument(dir, true, false, monitor);
	// monitor.done();
	// displaySuccessMessage("Written export to "+path);
	// } catch (Exception e) {
	// throw new InvocationTargetException(e, e.getMessage());
	// }
	// }
	// }, "Exporting", false);
	// } catch (Throwable e) {
	// onError("Export error", "Error during export of document", e);
	// }
	// }

	private void createZipFromFolder(String srcFolder, String destZipFile) throws IOException{
	    ZipOutputStream zip = null;
	    FileOutputStream fileWriter = null;

	    try {
			fileWriter = new FileOutputStream(destZipFile);

	    zip = new ZipOutputStream(fileWriter);

	    addFolderToZip("", srcFolder, zip);
	    zip.flush();
	    zip.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			throw e;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw e;
		}finally{
        if (zip != null) {
            zip.close();
        }
    }
	  } 
	
	  static private void addFileToZip(String path, String srcFile, ZipOutputStream zip) throws IOException
	     {

	    File folder = new File(srcFile);
	    if (folder.isDirectory()) {
	      addFolderToZip(path, srcFile, zip);
	    } else {
	      byte[] buf = new byte[1024];
	      int len;
	      FileInputStream in = new FileInputStream(srcFile);
	      zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
	      while ((len = in.read(buf)) > 0) {
	        zip.write(buf, 0, len);
	      }
	      in.close();
	    }
	  }

	  static private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws IOException
	     {
	    File folder = new File(srcFolder);

	    for (String fileName : folder.list()) {
	      if (path.equals("")) {
	        addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
	      } else {
	        addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip);
	      }
	    }
	  }

	public void exportDocument(final File dir, final Set<Integer> pageIndices, final boolean exportImg, final boolean exportPage, final boolean exportAlto, final String fileNamePattern) throws Throwable {
		try {

			if (dir == null)
				return;

			String what = "Images" + (exportPage ? ", PAGE" : "") + (exportAlto ? ", ALTO" : "");
			lastExportFolder = dir.getParentFile().getAbsolutePath();
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						logger.debug("exporting document...");
						final String path = storage.exportDocument(dir, pageIndices, exportImg, exportPage, exportAlto, fileNamePattern, monitor);
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

	public void exportRtf(final File file, final Set<Integer> pageIndices, final boolean isWordBased, final boolean isTagExport, final boolean doBlackening, final Set<String> selectedTags)
			throws Throwable {
		try {

			if (file == null)
				return;

			logger.info("RTF export. pages " + pageIndices + ", isWordBased: " + isWordBased);

			lastExportFolder = file.getParentFile().getAbsolutePath();
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						logger.debug("creating RTF document...");
						TrpRtfBuilder.writeRtfForDoc(storage.getDoc(), isWordBased, isTagExport, doBlackening, file, pageIndices, monitor, selectedTags);
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
	
	public void exportDocx(final File file, final Set<Integer> pageIndices, final boolean isWordBased, final boolean isTagExport, final boolean doBlackening, final Set<String> selectedTags, final boolean createTitle, final boolean markUnclearWords, final boolean expandAbbrevs, final boolean substituteAbbrevs, final boolean preserveLineBreaks)
			throws Throwable {
		try {

			if (file == null)
				return;

			logger.info("Docx export. pages " + pageIndices + ", isWordBased: " + isWordBased);

			lastExportFolder = file.getParentFile().getAbsolutePath();
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						logger.debug("creating Docx document...");
						DocxBuilder.writeDocxForDoc(storage.getDoc(), isWordBased, isTagExport, doBlackening, file, pageIndices, monitor, selectedTags, createTitle, markUnclearWords, expandAbbrevs, substituteAbbrevs, preserveLineBreaks);
						monitor.done();
					} catch (InterruptedException ie){
						throw ie;
					}
					catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Exporting", true);
		} catch (Throwable e) {
			if (!(e instanceof InterruptedException)){
				onError("Export error", "Error during Docx export of document", e);
			}
			throw e;
		}
	}

	public void exportXlsx(final File file, final Set<Integer> pageIndices, final boolean isWordBased, final boolean isTagExport, final Set<String> selectedTags)
			throws Throwable {
		try {
			
			if (ExportUtils.getCustomTagMapForDoc().isEmpty()){
				logger.info("No tags to store -> Xlsx export cancelled");
				displayCancelMessage("No custom tags in document to store -> Xlsx export cancelled");
				throw new Exception("No tags to store -> Xlsx export cancelled");
			}

			//logger.debug("lastExportXlsxFn = " + lastExportXlsxFn);

			if (file == null)
				return;

			logger.info("Excel export. pages " + pageIndices + ", isWordBased: " + isWordBased);

			lastExportFolder = file.getParentFile().getAbsolutePath();
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						logger.debug("creating Excel document...");
						TrpXlsxBuilder.writeXlsxForDoc(storage.getDoc(), isWordBased, isTagExport, file, pageIndices, monitor, selectedTags);
						monitor.done();
					} catch (InterruptedException ie){
						throw ie;
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Exporting", true);
		} catch (Throwable e) {
			if(!(e instanceof InterruptedException))
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

	public void exportPdf(final File dir, final Set<Integer> pageIndices, final boolean extraTextPages, final boolean imagesOnly, final Set<String> selectedTags, final boolean highlightTags, final boolean wordBased, final boolean doBlackening, final boolean createTitle) throws Throwable {
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
						logger.debug("creating PDF document...");
						storage.exportPdf(dir, pageIndices, monitor, extraTextPages, imagesOnly, selectedTags, highlightTags, wordBased, doBlackening, createTitle);
						monitor.done();
					} catch (InterruptedException ie){
							throw ie;
					}
					catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Exporting", true);
		} catch (Throwable e) {
			if(!(e instanceof InterruptedException))
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

	public void exportTei(final File file, final CommonExportDialog exportDiag) throws Throwable {
		try {
			
			final TeiExportPars pars = new TeiExportPars();
			pars.mode = exportDiag.getTeiExportMode();
			pars.linebreakMode = exportDiag.getTeiLinebreakMode();
			pars.writeTextOnWordLevel = exportDiag.isWordBased();
			pars.doBlackening = exportDiag.isDoBlackening();
			pars.pageIndices = exportDiag.getSelectedPages();
			pars.selectedTags = exportDiag.getSelectedTagsList();

			if (file == null)
				return;

			logger.info("TEI export. Mode = " + pars.mode);

			lastExportFolder = file.getParentFile().getAbsolutePath();
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						logger.debug("creating TEI document, pars: "+pars);

						storage.exportTei(file, pars, monitor);
						monitor.done();
					} catch (InterruptedException ie){
						throw ie;
					} catch (Exception e) {
							throw new InvocationTargetException(e, e.getMessage());
						}
				}
			}, "Exporting", true);
		} catch (Throwable e) {
			if(!(e instanceof InterruptedException))
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
			final BugDialog bugDialog = new BugDialog(getShell(), SWT.NONE);
			bugDialog.open();
		} catch (Throwable e) {
			onError("Fatal bug report error", "Fatal error sending bug report / feature request", e);
		}
	}

	public void selectTranscriptionWidgetOnSelectedShape(ICanvasShape selected) {
		if (selected == null || selected.getData() == null)
			return;

		if (selected.getData() instanceof TrpWordType) {
			ui.changeToTranscriptionWidget(ATranscriptionWidget.Type.WORD_BASED);
		} else if (selected.getData() instanceof TrpTextLineType || selected.getData() instanceof TrpBaselineType) {
			ui.changeToTranscriptionWidget(ATranscriptionWidget.Type.LINE_BASED);
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

			ui.selectStructureTab();
			updatePageInfo();
		} catch (Throwable e) {
			onError("Analyze Page Structure", e.getMessage(), e);
		}

	}

	public void updateReadingOrderAccordingToCoordinates(boolean deleteReadingOrder) {
		if (!storage.hasTranscript())
			return;

		JAXBPageTranscript tr = storage.getTranscript();

		logger.debug("applying reading order according to coordinates");
		IStructuredSelection sel = (IStructuredSelection) ui.getStructureTreeViewer().getSelection();
		Iterator<?> it = sel.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof TrpPageType) {
				TrpShapeTypeUtils
						.applyReadingOrderFromCoordinates(((TrpPageType) o).getTextRegionOrImageRegionOrLineDrawingRegion(), false, deleteReadingOrder);
			} else if (o instanceof TrpTextRegionType) {
				TrpShapeTypeUtils.applyReadingOrderFromCoordinates(((TrpTextRegionType) o).getTextLine(), false, deleteReadingOrder);
			} else if (o instanceof TrpTextLineType) {
				TrpShapeTypeUtils.applyReadingOrderFromCoordinates(((TrpTextLineType) o).getWord(), false, deleteReadingOrder);
			}
		}
		tr.getPage().sortContent();
		ui.getStructureTreeViewer().refresh();
	}

	public DebuggerDialog showDebugDialog() {
		DebuggerDialog debugDiag = new DebuggerDialog(getShell(), 0);
		debugDiag.open();
		
		return debugDiag;
		
//		logger.debug("showing debug dialog!");
//		if (debugDiag == null || debugDiag.shell == null || debugDiag.shell.isDisposed()) {
//			debugDiag = new DebuggerDialog(getShell(), 0);
//			debugDiag.open();
//		} else
//			debugDiag.shell.setActive();
//		
//		return debugDiag;
	}
	
//	public void appendDebugLog(final String text) {
//		Display.getDefault().asyncExec(new Runnable() {
//			@Override public void run() {
//				if (SWTUtil.isOpen(debugDiag)) {
//					debugDiag.debugText.append(text+"\n");
//				}
//				
//			}
//		});
//	}
		
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

	private String getAdjustedDocTitle() {
		String title = storage.getDoc().getMd().getTitle();
		return (title.replaceAll("([/\\?%*:| \"<>. ])", "_"));
	}
	
	public String getSelectedImageFileType() {
		String fileType = getCanvasWidget().getToolBar().getImageVersionItem().ti.getText();
		if (!fileType.equals("orig") && fileType.equals("view") && !fileType.equals("bin"))
			return "view";
		else
			return fileType;
		
//		return getCanvasWidget().getToolBar().getImageVersionItem().ti.getText();
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

			if (!storage.isLoggedIn() || !storage.isRemoteDoc())
				throw new IOException("No remote document loaded!");

			String fn = DialogUtil.showOpenFolderDialog(getShell(), "Choose a folder with images and page files", lastLocalDocFolder);
			if (fn == null)
				return;

			TrpDoc localDoc = LocalDocReader.load(fn);
			// create thumbs for this doc:			
			CreateThumbsService.createThumbs(localDoc, false, updateThumbsRunnable);

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
		SearchDialog d = new SearchDialog(getShell());
		d.open();
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
			for(TrpEvent ev : events){
				final String msg = CoreUtils.DATE_FORMAT_USER_FRIENDLY.format(ev.getDate()) +  ": " 
						+ ev.getTitle() + "\n\n" + ev.getMessage();					
				Pair<Integer, Boolean> ret = DialogUtil.showMessageDialogWithToggle(getShell(), 
					"Notification", msg, 
					"Do not show this message again", false, SWT.NONE, "OK");
				boolean doNotShowAgain = ret.getRight();
				logger.debug("Do not show again = " + doNotShowAgain);
				if(doNotShowAgain) {
					storage.markEventAsRead(ev.getId());
				}
			}
		} catch(IOException ioe){
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
		logger.debug("setting new locale: "+l);
		
		if (l==null || Msgs.getLocale().equals(l))
			return;
		
		Msgs.setLocale(l);
		TrpConfig.getTrpSettings().setLocale(l);
//		TrpConfig.save(TrpSettings.LOCALE_PROPERTY);
		
		DialogUtil.showInfoMessageBox(ui.getShell(), 
				 Msgs.get2("language_changed")+": "+l.getDisplayName(), Msgs.get2("restart"));
	}

	public void batchReplaceImagesForDoc() {
		try {
			logger.debug("batch replacing images!");

			if (!storage.isLoggedIn() || !storage.isRemoteDoc())
				throw new IOException("No remote document loaded!");

			String fn = DialogUtil.showOpenFolderDialog(getShell(), "Choose a folder with image files", lastLocalDocFolder);
			if (fn == null)
				return;
			
			File inputDir = new File(fn);
			List<File> imgFiles = Arrays.asList(inputDir.listFiles(new ImgFileFilter()));
			Collections.sort(imgFiles);

			final BatchImageReplaceDialog d = new BatchImageReplaceDialog(getShell(), storage.getDoc(), imgFiles);
			if (d.open() != Dialog.OK) {
				return;
			}
			
			logger.debug("checked pages: ");
			for (TrpPage p : d.getCheckedPages()) {
				logger.debug(""+p);
			}
			logger.debug("checkesd urls: ");
			for (URL u : d.getCheckedUrls()) {
				logger.debug(""+u);
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
			logger.debug("profileName = "+profileName);
			
			File profileFile=null;
			try {
				profileFile=TrpConfig.saveProfile(profileName, false);
				ui.updateProfiles();
			} catch (FileExistsException e ) {
				if (DialogUtil.showYesNoDialog(getShell(), "Profile already exists!", "Do want to overwrite the existing one?") == SWT.YES) {
					profileFile=TrpConfig.saveProfile(profileName, true);
				}				
			}
			if (profileFile!=null)
				DialogUtil.showMessageBox(getShell(), "Success", "Written profile to: \n\n"+profileFile.getAbsolutePath(), SWT.ICON_INFORMATION);
			
		} catch (Exception e) {
			onError("Error saving profile!", e.getMessage(), e, true, false);
		}
		
	}	
}
