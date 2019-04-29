package eu.transkribus.swt_gui.mainwidget.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.json.JsonArray;
import javax.security.auth.login.LoginException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.dea.fimagestore.core.beans.ImageMetadata;
import org.dea.fimgstoreclient.FimgStoreGetClient;
import org.dea.fimgstoreclient.beans.FimgStoreTxt;
import org.dea.fimgstoreclient.beans.ImgType;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.DocumentException;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.exceptions.NullValueException;
import eu.transkribus.core.exceptions.OAuthTokenRevokedException;
import eu.transkribus.core.io.DocExporter;
import eu.transkribus.core.io.LocalDocReader;
import eu.transkribus.core.io.LocalDocReader.DocLoadConfig;
import eu.transkribus.core.io.LocalDocWriter;
import eu.transkribus.core.io.UnsupportedFormatException;
import eu.transkribus.core.io.util.ExtensionFileFilter;
import eu.transkribus.core.model.beans.CitLabHtrTrainConfig;
import eu.transkribus.core.model.beans.CitLabSemiSupervisedHtrTrainConfig;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor.PageDescriptor;
import eu.transkribus.core.model.beans.EdFeature;
import eu.transkribus.core.model.beans.EdOption;
import eu.transkribus.core.model.beans.HtrModel;
import eu.transkribus.core.model.beans.JAXBPageTranscript;
import eu.transkribus.core.model.beans.PageLock;
import eu.transkribus.core.model.beans.TrpAction;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpCrowdProject;
import eu.transkribus.core.model.beans.TrpCrowdProjectMessage;
import eu.transkribus.core.model.beans.TrpCrowdProjectMilestone;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpDocDir;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpErrorRateResult;
import eu.transkribus.core.model.beans.TrpEvent;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.TrpP2PaLAModel;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.TrpUpload;
import eu.transkribus.core.model.beans.TrpWordgraph;
import eu.transkribus.core.model.beans.auth.TrpRole;
import eu.transkribus.core.model.beans.auth.TrpUserLogin;
import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory;
import eu.transkribus.core.model.beans.customtags.CustomTagUtil;
import eu.transkribus.core.model.beans.customtags.StructureTag;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.model.beans.enums.OAuthProvider;
import eu.transkribus.core.model.beans.enums.SearchType;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.model.beans.job.enums.JobImpl;
import eu.transkribus.core.model.beans.job.enums.JobTask;
import eu.transkribus.core.model.beans.pagecontent.PcGtsType;
import eu.transkribus.core.model.beans.pagecontent.TextTypeSimpleType;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageTypeUtils;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.core.model.beans.rest.ParameterMap;
import eu.transkribus.core.model.beans.searchresult.FulltextSearchResult;
import eu.transkribus.core.model.builder.CommonExportPars;
import eu.transkribus.core.model.builder.ExportCache;
import eu.transkribus.core.model.builder.alto.AltoExporter;
import eu.transkribus.core.model.builder.pdf.PdfExporter;
import eu.transkribus.core.model.builder.tei.ATeiBuilder;
import eu.transkribus.core.model.builder.tei.TeiExportPars;
import eu.transkribus.core.model.builder.tei.TrpTeiStringBuilder;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.DescriptorUtils;
import eu.transkribus.core.util.Event;
import eu.transkribus.core.util.HtrUtils;
import eu.transkribus.core.util.PageXmlUtils;
import eu.transkribus.core.util.ProxyUtils;
import eu.transkribus.core.util.SebisStopWatch;
import eu.transkribus.swt.util.AsyncCallback;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.TrpGuiPrefs;
import eu.transkribus.swt_gui.TrpGuiPrefs.ProxyPrefs;
import eu.transkribus.swt_gui.canvas.CanvasImage;
import eu.transkribus.swt_gui.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.mainwidget.ImageDataDacheFactory;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener.CollectionsLoadEvent;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener.DocListLoadEvent;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener.DocLoadEvent;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener.DocMetadataUpdateEvent;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener.JobUpdateEvent;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener.LoginOrLogoutEvent;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener.MainImageLoadEvent;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener.PageLoadEvent;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener.StructTagSpecsChangedEvent;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener.TagSpecsChangedEvent;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener.TranscriptListLoadEvent;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener.TranscriptLoadEvent;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener.TranscriptSaveEvent;
import eu.transkribus.swt_gui.metadata.CustomTagSpec;
import eu.transkribus.swt_gui.metadata.CustomTagSpecDBUtil;
import eu.transkribus.swt_gui.metadata.CustomTagSpecUtil;
import eu.transkribus.swt_gui.metadata.StructCustomTagSpec;
import eu.transkribus.swt_gui.metadata.TaggingWidgetUtils;
import eu.transkribus.util.DataCache;
import eu.transkribus.util.DataCacheFactory;
import eu.transkribus.util.MathUtil;
import eu.transkribus.util.OcrConfig;
import eu.transkribus.util.RecognitionPreferences;
import eu.transkribus.util.TextRecognitionConfig;
import eu.transkribus.util.Utils;

/** Singleton class that contains all data related to loading a transcription */
public class Storage {
	private final static Logger logger = LoggerFactory.getLogger(Storage.class);	

	final int N_IMAGES_TO_PRELOAD_PREVIOUS = 0;
	final int N_IMAGES_TO_PRELOAD_NEXT = 0;
	
//	final int N_IMAGES_TO_PRELOAD_PREVIOUS = 1;
//	final int N_IMAGES_TO_PRELOAD_NEXT = 1;	

	// public final static String LOGIN_OR_LOGOUT_EVENT =
	// "LOGIN_OR_LOGOUT_EVENT";

	private static Storage storage = null;

	// private int currentTranscriptIndex = 0;

	private List<TrpDocMetadata> docList = Collections.synchronizedList(new ArrayList<>());
	private List<TrpDocMetadata> deletedDocList = Collections.synchronizedList(new ArrayList<>());
	private List<TrpDocMetadata> userDocList = Collections.synchronizedList(new ArrayList<>());
	
	private List<CustomTagSpec> customTagSpecs = new ArrayList<>();
	private List<CustomTagSpec> collectionSpecificTagSpecs = new ArrayList<>();
	private List<StructCustomTagSpec> structCustomTagSpecs = new ArrayList<>();
	private Map<String, Pair<Integer, String>> virtualKeysShortCuts = new HashMap<>();
	
	private int collId;
		
	private List<String> htrModelList = new ArrayList<>(0);

	private TrpDoc doc = null;
	private TrpPage page = null;
	private boolean isPageLocked = false;

	private JAXBPageTranscript transcript = new JAXBPageTranscript();
	private TrpTextRegionType regionObject = null;
	private TrpTextLineType lineObject = null;
	private TrpWordType wordObject = null;

	// TextLineType currentLineObject = null;

	// TrpTranscriptMetadata currentTranscriptMetadata = null;

	private CanvasImage currentImg;
	List<TrpWordgraph> wordgraphs = new ArrayList<>();
	String[][] wgMatrix = new String[][] {};

	private TrpServerConn conn = null;
	private TrpUserLogin user = null;
	
	private List<TrpCollection> collections = Collections.synchronizedList(new ArrayList<>());

//	private static DocJobUpdater docUpdater;
	private DataCache<URL, CanvasImage> imCache;
	
	public static final boolean USE_TRANSCRIPT_CACHE = false;
	private DataCache<TrpTranscriptMetadata, JAXBPageTranscript> transcriptCache;
	
	ImageMetadata imgMd;
	
//	private int currentColId = -1;
	
	Set<IStorageListener> listener = new HashSet<>();
	
	// just for debugging purposes:
	private static int reloadDocListCounter=0;
	
	private List<TrpP2PaLAModel> p2palaModels = new ArrayList<>();
	
	public static class StorageException extends Exception {
		private static final long serialVersionUID = -2215354890031208420L;

		public StorageException(String message) {
			super(message);
		}
		
		public StorageException(String message, Exception cause) {
			super(message, cause);
		}
	}
	

	private Storage() {
		initImCache();
		initTranscriptCache();
		addInternalListener();
		readTagSpecsFromLocalSettings();
		readStructTagSpecsFromLocalSettings();
		
		// init some dummy vk shortcuts:
//		setVirtualKeyShortCut("1", Pair.of(1, "hello"));
//		setVirtualKeyShortCut("2", Pair.of(2, "-"));
	}
	
	private void addInternalListener() {
		addListener(new IStorageListener() {
			@Override public void handleLoginOrLogout(LoginOrLogoutEvent arg) {
				//reloadUserDocs();
			}
		});
	}
	
	public TrpPageType getOrBuildPage(TrpTranscriptMetadata md, boolean keepAlways) throws Exception {
		if (USE_TRANSCRIPT_CACHE) {
			return transcriptCache.getOrPut(md, keepAlways, null).getPage();
		} else {
			JAXBPageTranscript tr = new JAXBPageTranscript(md);
			tr.build();
			if(currentImg != null && currentImg.getTransformation() != null) {
				PageXmlUtils.checkAndFixXmlOrientation(currentImg.getTransformation(), tr.getPageData());
				//enable this if user is to be asked to save on orientation fix.
//				setCurrentTranscriptEdited(true);
			}
			return tr.getPage();
		}
		
	}
	
	private void initTranscriptCache() {
		int cacheSize = TrpConfig.getTrpSettings().getImageCacheSize();
		if (cacheSize < 1)
			cacheSize = 1;
		
		logger.info("setting transcript cache size to " + cacheSize);
		transcriptCache = new DataCache<TrpTranscriptMetadata, JAXBPageTranscript>(cacheSize, new DataCacheFactory<TrpTranscriptMetadata, JAXBPageTranscript>() {
			@Override public JAXBPageTranscript createFromKey(TrpTranscriptMetadata key, Object opts) throws Exception {
//				JAXBPageTranscript tr = TrpPageTranscriptBuilder.build(key);
				JAXBPageTranscript tr = new JAXBPageTranscript(key);
				tr.build();
				return tr;
			}
			@Override public void dispose(JAXBPageTranscript element) {
			}
		});
		
	}

	private void initImCache() {
		int imCacheSize = TrpConfig.getTrpSettings().getImageCacheSize();
		if (imCacheSize < 1)
			imCacheSize = 1;

		logger.info("setting image cache size to " + imCacheSize);
		imCache = new DataCache<URL, CanvasImage>(imCacheSize, new ImageDataDacheFactory());
	}

//	private static void initDocUpdater() {
//		docUpdater = new DocJobUpdater() {
//			@Override public void onUpdate(final TrpJobStatus job) {
//				// Display.getDefault().asyncExec(new Runnable() {
//				// @Override public void run() {
//				storage.sendEvent(new JobUpdateEvent(this, job));
//				// }
//				// });
//			}
//		};
//	}
//	
//	public void startOrResumeJobThread() {
//		docUpdater.startOrResumeJobThread();
//	}
//
//	@Override public void finalize() {
//		logger.debug("Storage finalize - stopping job update thread!");
//		docUpdater.stopJobThread();
//	}

	public static Storage getInstance() {
		if (storage == null) {
			storage = new Storage();
//			initDocUpdater();
		}
		return storage;
	}

	private static TrpTranscriptMetadata findTranscriptWithTimeStamp(List<TrpTranscriptMetadata> transcripts, TrpTranscriptMetadata md) {
		// logger.debug("timestamp to find: "+md.getTimestamp());
		for (TrpTranscriptMetadata pmd : transcripts) {
			if (pmd.getTimestamp() == md.getTimestamp()) {
				// logger.debug("returning ts: "+pmd.getTimestamp());
				return pmd;
			}
		}
		return null;
	}

	public List<TrpTranscriptMetadata> getTranscriptsSortedByDate(boolean includeCurrent, int max) {
		if (page == null)
			return new ArrayList<TrpTranscriptMetadata>();

		List<TrpTranscriptMetadata> trlist = page.getTranscripts();

		if (max > 0 && trlist.size() > max) {
			trlist = new ArrayList<>(trlist.subList(0, max));
		}

		if (includeCurrent && hasTranscriptMetadata() && findTranscriptWithTimeStamp(trlist, transcript.getMd()) == null) {
			// logger.debug("adding transcription!!");
			trlist.add(transcript.getMd());
		}

		Collections.sort(trlist, Collections.reverseOrder());

		return trlist;
	}	

	public boolean hasPageIndex(int index) {
		return (doc != null && index >= 0 && index < getNPages());
	}

	// public boolean hasCurrentPage() { return currentPageObject!=null; }

	// public boolean hasTranscript(int index) {
	//
	// return (page != null && index >= 0 && index < getNTranscripts());
	// }

	public boolean hasTranscriptMetadata() {
		return transcript != null && transcript.getMd() != null;
	}

	public boolean hasTranscript() {
		return hasTranscriptMetadata() && transcript.getPageData() != null;
	}

	public int getNTranscripts() {
		if (page != null) {
			return page.getTranscripts().size();
		} else {
			return 0;
		}
	}

	public boolean hasTextRegion(int index) {
		return (hasTranscript() && index >= 0 && index < getNTextRegions());
	}

	public int getNPages() {
		if (doc != null) {
			return doc.getNPages();
		} else
			return 0;
	}

	public int getNTextRegions() {
		return getTextRegions().size();
	}

	public List<TrpTextRegionType> getTextRegions() {
		if (hasTranscript()) {
			return transcript.getPage().getTextRegions(true);
		} else
			return new ArrayList<>();
	}

//	public List<TrpJobStatus> getJobs() {
//		return jobs;
//	}

	// public List<TrpJobStatus> getJobsForCurrentDocument() {
	// // reloadJobs();
	// List<TrpJobStatus> jobs4Doc = new ArrayList<>();
	// if (!isDocLoaded())
	// return jobs4Doc;
	//
	// for (TrpJobStatus j : jobs) {
	// if (j.getDocId() == doc.getMd().getDocId())
	// jobs4Doc.add(j);
	// }
	// return jobs4Doc;
	// }
	
//	public int getNUnfinishedJobs(boolean filterByUser) throws NumberFormatException, SessionExpiredException, ServerErrorException, IllegalArgumentException {
//		if (!isLoggedIn())
//			return 0;
//		
//		return conn.countJobs(filterByUser, TrpJobStatus.UNFINISHED, null);
//	}

	public List<TrpJobStatus> getUnfinishedJobs(boolean filterByUser) throws SessionExpiredException, ServerErrorException, IllegalArgumentException {
		List<TrpJobStatus> unfinished = new ArrayList<>();
		if (!isLoggedIn())
			return unfinished;
		
		return conn.getJobs(filterByUser, TrpJobStatus.UNFINISHED, null, null, 0, 0, null, null);
		
//		for (TrpJobStatus j : getJobs()) {
//			if (!j.isFinished())
//				unfinished.add(j);
//		}
//		return unfinished;
	}

	public List<TrpJobStatus> getUnfinishedJobsForCurrentPage() {
		List<TrpJobStatus> unfinished = new ArrayList<>();
		// TODO
//		if (doc == null || page == null)
//			return unfinished;
//
//		for (TrpJobStatus j : getUnfinishedJobs()) {
//			if (j.getDocId() == doc.getId() && j.getPageNr() == page.getPageNr())
//				unfinished.add(j);
//		}
		return unfinished;
	}

	// public boolean hasUnfinishedJobForCurrentPage() {
	// if (doc==null || page==null)
	// return false;
	//
	// for (TrpJobStatus j : getUnfinishedJobs()) {
	// if (j.getDocId()==doc.getId() && j.getPageNr() == page.getPageNr())
	// return true;
	// }
	// return false;
	// }
	
	public String getUserName() {
		return user == null ? null : user.getUserName();
	}

	public TrpUserLogin getUser() {
		return user;
	}
	
	public int getUserId() {
		return user!=null ? user.getUserId() : -1;
	}

	public boolean isLoggedIn() {
		return (conn != null && user != null);
	}
	
	public boolean isAdminLoggedIn() {
		return (conn != null && user != null && user.isAdmin());
	}

	public boolean isPageLoaded() {
		return (doc != null && page != null);
	}

	public TrpServerConn getConnection() {
		return conn;
	}

	public String getCurrentServer() {
		return isLoggedIn() ? conn.getServerUri().toString() : null;
	}
	
	public boolean isLoggedInAtTestServer() {
		if (getCurrentServer()==null) {
			return false;
		}
		return getCurrentServer().startsWith(TrpServerConn.TEST_SERVER_URI);
	}

	public boolean isLocalDoc() {
		return isLocalDoc(doc);
	}

	public boolean isRemoteDoc() {
		return isRemoteDoc(doc);
	}
	
	public static boolean isLocalDoc(TrpDoc doc) {
		return doc != null && doc.isLocalDoc();
	}
	
	public static boolean isRemoteDoc(TrpDoc doc) {
		return doc != null && doc.isRemoteDoc();
	}
	
	public void closeCurrentDocument() {
		clearDocContent();
		clearPageContent();
		
		tryReleaseLocks();
		
		sendEvent(new DocLoadEvent(this, null));
	}

	private void clearDocContent() {
		doc = null;
		page = null;
	}

	private void clearPageContent() {
		// currentTranscriptIndex = 0;
		currentImg = null;
		wordgraphs = new ArrayList<>();
		clearTranscriptContent();
		regionObject = null;
		lineObject = null;
		wordObject = null;
	}
	
	public void checkDocLoaded() throws StorageException {
		if (!isDocLoaded()) {
			throw new StorageException("No document loaded!");
		}
	}
	
	public void checkRemoteDocLoaded() throws StorageException {
		if (!isRemoteDoc()) {
			throw new StorageException("No remote document loaded!");
		}
	}
	
	public void checkLocalDocLoaded() throws StorageException {
		if (!isRemoteDoc()) {
			throw new StorageException("No local document loaded!");
		}
	}
	
	public void checkLoggedIn() throws StorageException {
		if (!isLoggedIn()) {
			throw new StorageException("You are not logged in!");
		}
	}
	
	public void checkPageLoaded() throws StorageException {
		if (!isPageLoaded()) {
			throw new StorageException("No page loaded!");
		}
	}
	

	private void clearTranscriptContent() {
		transcript.clear();
	}

	private void preloadSurroundingImages(String fileType) {
		if (!isPageLoaded()) {
			return;
		}

		logger.debug("preloading surrounding images - n-previous = " + N_IMAGES_TO_PRELOAD_PREVIOUS + " n-next = " + N_IMAGES_TO_PRELOAD_NEXT);
		ArrayList<URL> preload = new ArrayList<URL>();

		int notLoadedCounter = 0;
		int currentPageIndex = getPageIndex();

		for (int i = 1; i <= N_IMAGES_TO_PRELOAD_PREVIOUS; ++i) {
			
			if (hasPageIndex(currentPageIndex - i)) {
				String urlStr = doc.getPages().get(currentPageIndex - i).getUrl().toString();
				urlStr = UriBuilder.fromUri(urlStr).replaceQueryParam("fileType", fileType).toString();
				
				try {
					preload.add(new URL(urlStr));
				} catch (MalformedURLException e) {
					logger.error(e.getMessage(),e );
				}
			} else
				notLoadedCounter++;
		}
		for (int i = 1; i <= (N_IMAGES_TO_PRELOAD_NEXT + notLoadedCounter); ++i) {
			if (hasPageIndex(currentPageIndex + i)) {
//				preload.add(doc.getPages().get(currentPageIndex + i).getUrl());
				String urlStr = doc.getPages().get(currentPageIndex + i).getUrl().toString();
				urlStr = UriBuilder.fromUri(urlStr).replaceQueryParam("fileType", fileType).toString();				
				
				try {
					preload.add(new URL(urlStr));
				} catch (MalformedURLException e) {
					logger.error(e.getMessage(),e );
				}
			}
		}

		imCache.preload(preload, fileType);
	}

	/**
	 * Sets the current page to page with the given index. Note that for
	 * actually loading the page (which loads the corresponding image and a list
	 * of transcriptions) a call to {@link #reloadCurrentPage()} is needed.
	 * 
	 * @param pageIndex
	 *            The 0 based index of the page
	 * @return True if the page exists and was set, false elsewise
	 */
	public boolean setCurrentPage(int pageIndex) {
		if (hasPageIndex(pageIndex)) {
			page = doc.getPages().get(pageIndex);
			return true;
		}
		return false;
	}

	public boolean setCurrentTranscript(TrpTranscriptMetadata md) {
		if (doc == null || page == null)
			return false;

		if (transcript.getMd()!=null && transcript.getMd().equals(md))
			return false;
		
		transcript.setMd(md);
		return true;
	}

	public boolean setLatestTranscriptAsCurrent() {
		if (doc == null || page == null || page.getTranscripts().isEmpty())
			return false;

		List<TrpTranscriptMetadata> trs = getTranscriptsSortedByDate(false, 0);
		if (!trs.isEmpty())
			return setCurrentTranscript(trs.get(0));
		else
			return false;
	}

	public boolean jumpToRegion(int index) {
		if (doc == null || page == null || !hasTranscript())
			return false;

		logger.debug("Jumping to region " + index);
		if (hasTextRegion(index)) {
			regionObject = getTextRegions().get(index);
			return true;
		} else
			return false;

	}

	// public int getTranscriptIndex() {
	// return currentTranscriptIndex;
	// }

	public JAXBPageTranscript getTranscript() {
		return transcript;
	}

	public TrpTranscriptMetadata getTranscriptMetadata() {
		return transcript.getMd();
	}

	public int getCurrentRegion() {
		if (regionObject != null)
			return regionObject.getIndex();
		else
			return -1;
	}

	public TrpPage getPage() {
		return page;
	}

	public int getPageIndex() {
		return isPageLoaded() ? doc.getPageIndex(page) : -1;
	}

	public boolean isPageLocked() {
		if (page == null)
			return false;

		return isPageLocked || !getUnfinishedJobsForCurrentPage().isEmpty();
	}

	public boolean isDocLoaded() {
		return doc != null;
	}
	
	public boolean isThisDocLoaded(int docId, File localFolder) {
		if (doc == null)
			return false;
		
		if (docId == -1)
			return doc.getMd().getLocalFolder().equals(localFolder);
		else
			return docId == doc.getMd().getDocId();
	}

	public void updateDataForSelectedShape(ICanvasShape shape) {
		// update storage data:
		regionObject = null;
		lineObject = null;
		wordObject = null;

		if (shape != null && shape.getData() != null) {
			if (shape.getData() instanceof TrpTextRegionType) {
				regionObject = (TrpTextRegionType) shape.getData();
			} else if (shape.getData() instanceof TrpTextLineType) {
				TrpTextLineType tl = (TrpTextLineType) shape.getData();
				regionObject = tl.getRegion();
				lineObject = tl;
			} else if (shape.getData() instanceof TrpBaselineType) {
				TrpTextLineType tl = ((TrpBaselineType) shape.getData()).getLine();
				regionObject = tl.getRegion();
				lineObject = tl;
			} else if (shape.getData() instanceof TrpWordType) {
				TrpWordType word = (TrpWordType) shape.getData();
				TrpTextLineType tl = word.getLine();
				regionObject = tl.getRegion();
				lineObject = tl;
				wordObject = word;
			}
		}
	}

	public boolean isTranscriptEdited() {
		return (hasTranscript() && transcript.getPage().isEdited());
	}

	public CanvasImage getCurrentImage() {
		return currentImg;
	}

	public TrpTextRegionType getCurrentRegionObject() {
		return regionObject;
	}

	public TrpTextLineType getCurrentLineObject() {
		return lineObject;
	}

	public TrpWordType getCurrentWordObject() {
		return wordObject;
	}
	
	public String getServerUri() {
		return conn==null ? "" : conn.getServerUri();
	}

	public TrpDoc getDoc() {
		return doc;
	}
	
	/** Returns the current document id. A value of -2 means no doc is loaded, while -1 means a local doc is loaded! */
	public int getDocId() {
		return doc==null ? -2 : doc.getId();
	}
	
	public CanvasImage getCurrentImg() {
		return currentImg;
	}
	
	public boolean addListener(IStorageListener l) {
		return listener.add(l);
	}
	
	public boolean removeListener(IStorageListener l) {
		return listener.remove(l);
	}
	
	public void sendJobListUpdateEvent() {
		sendEvent(new JobUpdateEvent(this, null));
	}
	
	public void sendJobUpdateEvent(TrpJobStatus job) {
		sendEvent(new JobUpdateEvent(this, job));
	}	

	public void sendEvent(final Event event) {
		if (Thread.currentThread() == Display.getDefault().getThread()) {
			for (IStorageListener l : listener) {
				l.handleEvent(event);
			}
		} else {
			Display.getDefault().asyncExec(() -> {
				for (IStorageListener l : listener) {
					l.handleEvent(event);
				}
			});
		}

		// setChanged();
		// notifyObservers(event);
	}

	// //////////// METHODS THAT THROW EXCEPTIONS: /////////////////////////	
	public TrpCollection getCurrentDocumentCollection() {
		return isRemoteDoc() ? doc.getCollection() : null;
	}
	
	public int getCurrentDocumentCollectionId() {
		TrpCollection c = getCurrentDocumentCollection();
		return c==null ? 0 : c.getColId();
	}
		
	public void reloadUserDocs() {
		logger.debug("reloading docs by user!");
		
		if (user != null) {
			conn.getAllStrayDocsByUserAsync(0, 0, null, null, new InvocationCallback<List<TrpDocMetadata>>() {
				@Override public void failed(Throwable throwable) {
					logger.error("Error loading documents by user "+user+" - "+throwable.getMessage(), throwable);
				}
				
				@Override public void completed(List<TrpDocMetadata> response) {
					logger.debug("loaded docs by user "+user+" - "+response.size()+" thread: "+Thread.currentThread().getName());
					synchronized (this) {
						userDocList.clear();
						userDocList.addAll(response);
						
						sendEvent(new DocListLoadEvent(this, 0, userDocList, true));
					}
				}
			});
		} else {
			synchronized (this) {
				userDocList.clear();				
				sendEvent(new DocListLoadEvent(this, 0, userDocList, true));
			}
		}
	}
	
	public List<TrpDocMetadata> getUserDocList() {
		return userDocList;
	}

	public Future<List<TrpDocMetadata>> reloadDocList(int colId) throws NoConnectionException, SessionExpiredException, ServerErrorException, ClientErrorException, IllegalArgumentException{
		checkConnection(true);
		if (colId == 0)
			return null;
		

		//Just as a work around to get SessionExpiredException, which is catched and the login dialog can be called 
		getConnection().checkSession();

		logger.debug("reloading doclist for collection: "+colId+" reloadDocListCounter = "+(++reloadDocListCounter));
		
		SebisStopWatch.SW.start();
		
		Future<List<TrpDocMetadata>> fut = 
//			conn.getAllDocsAsync(colId, 0, 0, null, null, new InvocationCallback<List<TrpDocMetadata>>() {
			conn.getAllDocsAsync(colId, 0, 0, "docId", "desc", false, new InvocationCallback<List<TrpDocMetadata>>() {
			@Override
			public void completed(List<TrpDocMetadata> docs) {				
				synchronized (this) {
					docList.clear();
					docList.addAll(docs);
				}
				
				Storage.this.collId = colId;
				
				logger.debug("async loaded "+docList.size()+" nr of docs of collection "+collId+" thread: "+Thread.currentThread().getName());
				SebisStopWatch.SW.stop(true, "load time: ", logger);
				
				sendEvent(new DocListLoadEvent(this, colId, docList, false));
			}

			@Override public void failed(Throwable throwable) {
				
				
				
//				TrpMainWidget.getInstance().onError(title, message, th);
			}
		});
		
		//load deleted docs list as well
		Future<List<TrpDocMetadata>> fut2 = 
//				conn.getAllDocsAsync(colId, 0, 0, null, null, new InvocationCallback<List<TrpDocMetadata>>() {
				conn.getAllDocsAsync(colId, 0, 0, "docId", "desc", true, new InvocationCallback<List<TrpDocMetadata>>() {
				@Override
				public void completed(List<TrpDocMetadata> docs) {				
					synchronized (this) {
						deletedDocList.clear();
						deletedDocList.addAll(docs);
					}
										
					logger.debug("async loaded "+deletedDocList.size()+" nr of deleted docs in collection "+collId+" thread: "+Thread.currentThread().getName());

				}

				@Override public void failed(Throwable throwable) {

				}
			});
		
		return fut;
	}
	
	public int getCollId() {
		return collId;
	}
		
	public boolean hasRemoteDoc(int index) {
		return (docList != null && index >= 0 && index < docList.size());
	}
	
	public List<TrpDocMetadata> getDocList() {
		return docList;
	}

	public void invalidateSession() throws SessionExpiredException, ServerErrorException, Exception {
		checkConnection(true);
		conn.invalidate();
	}
	
	/**
	 * @deprecated not tested and used yet
	 */
	public void loginAsync(String serverUri, String username, String password, AsyncCallback<Object> callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					login(serverUri, username, password);
					callback.onSuccess(null);
				} catch (Throwable e) {
					callback.onError(e);
				}
			}
		}).start();
	}

	public void login(String serverUri, String username, String password) throws ClientErrorException, LoginException {
		logger.debug("Logging in as user: " + username);
		if (conn != null)
			conn.close();

		conn = new TrpServerConn(serverUri);
		conn.enableDebugLogging(TrpMainWidget.getTrpSettings().isLogHttp());
		user = conn.login(username, password);
		logger.debug("Logged in as user: " + user + " connection: " + conn);
		
		if(user.isAdmin() && TrpMainWidget.getInstance()!=null) {
			logger.info(user + " is admin.");
			TrpMainWidget.getInstance().getTrpSets().setServerSelectionEnabled(user.isAdmin());
		}
		onLogin();
		sendEvent(new LoginOrLogoutEvent(this, true, user, conn.getServerUri()));
	}
	
	public void loginOAuth(final String serverUri, final String code, final String state, final String grantType, final String redirectUri, final OAuthProvider prov) throws LoginException, OAuthTokenRevokedException {
		logger.debug("Logging in via OAuth at: " + prov.toString());
		if (conn != null)
			conn.close();

		conn = new TrpServerConn(serverUri);
		conn.enableDebugLogging(TrpMainWidget.getTrpSettings().isLogHttp());
		user = conn.loginOAuth(code, state, grantType, redirectUri, prov);
		
		logger.debug("Logged in as user: " + user + " connection: " + conn);
		
		if("authorization_code".equals(grantType)){
			final String token = user.getRefreshToken();
			if(token == null){
				throw new LoginException("No token was returned!");
			}
//			logger.debug("THE TOKEN: " + token);
			try {
				TrpGuiPrefs.storeOAuthCreds(prov, user.getEmail(), user.getProfilePicUrl(), token);
			} catch (Exception e) {
				logger.error("Could not store OAuth refresh token!", e);
			}
		}
		onLogin();
		sendEvent(new LoginOrLogoutEvent(this, true, user, conn.getServerUri()));
	}
	
	protected void onLogin() {
		reloadP2PaLAModels();
	}
	
	public void logout() {
		try {
			if (conn != null)
				conn.close();
		} catch (Throwable th) {
			logger.error("Error logging out: " + th.getMessage(), th);
		} finally {
			clearCollections();
			clearP2PaLAModels();
			conn = null;
			user = null;
//			clearDocList();
//			jobs = new ArrayList<>();
			sendEvent(new LoginOrLogoutEvent(this, false, null, null));
		}
	}

//	public void reloadJobs(boolean filterByUser) throws SessionExpiredException, ServerErrorException, IllegalArgumentException {
//		logger.debug("reloading jobs ");
//		if (conn != null && !isLocalDoc()) {
//			jobs = conn.getJobs(filterByUser, null, 0, 0);
//			// sort by creation date:
//			Comparator<TrpJobStatus> comp = new Comparator<TrpJobStatus>() {
//				@Override public int compare(TrpJobStatus o1, TrpJobStatus o2) {
//					return Long.compare(o1.getCreateTime(), o2.getCreateTime());
//				}
//			};
//			Comparator<TrpJobStatus> reverseComp = Collections.reverseOrder(comp);
//			Collections.sort(jobs, reverseComp);
//			startOrResumeJobThread();
//		}
//		if (jobs == null) // ensure that jobs array is never null!
//			jobs = new ArrayList<>();
//
//		sendEvent(new JobUpdateEvent(this, null));
//	}
	
	public void cancelJob(String jobId) throws SessionExpiredException, ServerErrorException, IllegalArgumentException {
		if (conn != null && jobId != null) {
			conn.killJob(jobId);
		}
	}

//	public TrpJobStatus loadJob(String jobId) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, NoConnectionException {
//		// FIXME: direct access to job table not "clean" here...
//		List<TrpJobStatus> jobs = (List<TrpJobStatus>) TrpMainWidget.getInstance().getUi().getJobOverviewWidget().getTableViewer().getInput();
//		if (jobs == null) // should not happen!
//			return null;
//		
//		synchronized (jobs) {
//			checkConnection(true);
//			TrpJobStatus job = conn.getJob(jobId);
//			// update job in jobs array if there
//			for (int i = 0; i < jobs.size(); ++i) {
//				if (jobs.get(i).getJobId().equals(job.getJobId())) {
//					//logger.debug("UPDATING JOB: "+job.getJobId()+" new status: "+job.getState());
//					jobs.get(i).copy(job); // do not set new instance, s.t. table-viewer does not get confused!
//					
//					return jobs.get(i);
//					
////					jobs.set(i, job);
////					break;
//				}
//			}
////			return null; // orig
//			return job; // return "original" job from connection here if not found in table (can be possible since introduction of paginated widgets!!)
//		}
//	}

	public void reloadCurrentDocument(int colId) throws SessionExpiredException, IllegalArgumentException, NoConnectionException, UnsupportedFormatException,
			IOException, NullValueException {

		// public void loadLocalDoc(String folder) throws Exception {
		// public void loadRemoteDoc(int docId) throws Exception {

		if (doc != null) {
			if (isLocalDoc()) {
				loadLocalDoc(doc.getMd().getLocalFolder().getAbsolutePath(), null);
			} else {
				loadRemoteDoc(colId, doc.getMd().getDocId());
			}

			logger.debug("nr of pages: " + getNPages());
			setCurrentPage(0);

			// if (!hasPageIndex(0)) {
			// currentPage = 0;
			// }
		}
	}

	public String[][] getWordgraphMatrix(boolean fromCache, final int docId, final int pageNr, final String lineId) throws IOException {
		if (fromCache)
			return wgMatrix;

		TrpWordgraph wg = getWordGraph(docId, pageNr, lineId);
		wgMatrix = new String[][] {};
		if (wg != null) {
			FimgStoreGetClient getter = new FimgStoreGetClient(wg.getnBestUrl());
			FimgStoreTxt nBest = getter.getTxt(wg.getnBestKey());
			wgMatrix = HtrUtils.getnBestMatrixUpvlc(nBest.getText(), false);
		}
		return wgMatrix;
	}
	
	public TrpWordgraph getWordGraph(final int docId, final int pageNr, final String lineId) {
		TrpWordgraph wg = null;
		if (wordgraphs != null) {
			for (TrpWordgraph w : wordgraphs) {
				if (w.getLineId().equals(lineId)) {
					wg = w;
					break;
				}
			}
		}
		return wg;
	}
	
	public boolean hasWordGraph(final int docId, final int pageNr, final String lineId) {
		return getWordGraph(docId, pageNr, lineId) != null;
	}
	
	public void reloadCurrentImage(final String fileType) throws MalformedURLException, Exception {
		if (!isPageLoaded())
			return;
		
		String urlStr = page.getUrl().toString();
		UriBuilder ub = UriBuilder.fromUri(urlStr);
		
		ub = ub.replaceQueryParam("fileType", null); // remove existing fileType par
		
		logger.debug("img uri: "+ub.toString());
		
		if (ub.toString().startsWith("file:") || new File(ub.toString()).exists()) {
			logger.debug("this is a local image file!");
			urlStr = ub.toString();
		} else {
			logger.debug("this is a remote image file - adding fileType parameter for fileType="+fileType);
			urlStr = UriBuilder.fromUri(urlStr).replaceQueryParam("fileType", fileType).toString();
		}
					
		logger.debug("Loading image from url: " + urlStr);
		final boolean FORCE_RELOAD = false;
		
		// always reload original image if asked for it
		currentImg = imCache.getOrPut(new URL(urlStr), true, fileType, (fileType == "orig") || FORCE_RELOAD);
		logger.trace("loaded image!");
		
		setCurrentImageMetadata();
		
		sendEvent(new MainImageLoadEvent(this, currentImg));
	}

	/**
	 * Reloads the current page, i.e. its corresponding image and a list of
	 * transcription belonging to this page
	 * 
	 * @throws IllegalArgumentException
	 * @throws ServerErrorException
	 * @throws SessionExpiredException
	 * @throws Exception
	 */
	public void reloadCurrentPage(int colId, String fileType) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, Exception {
		if (!isPageLoaded())
			return;
		if (isRemoteDoc())
			checkConnection(true);

		clearPageContent();

		// load image
		reloadCurrentImage(fileType);
		
		// load wordgraphs
		if (isRemoteDoc()) {
			wordgraphs = conn.getWordgraphs(colId, page.getDocId(), page.getPageNr());
			logger.debug("loaded wordgraphs, size = " + wordgraphs.size());
		}

		reloadTranscriptsList(colId);
		setLatestTranscriptAsCurrent();
		logger.debug("nr of transcripts: " + getNTranscripts());
		logger.debug("image filename: " + page.getUrl());
		
		if (isRemoteDoc() && this.getRoleOfUserInCurrentCollection().getValue() > TrpRole.Reader.getValue())
			lockPage(getCurrentDocumentCollectionId(), page);
		
		if (TrpConfig.getTrpSettings().isPreloadImages())
			preloadSurroundingImages(fileType);
		else
			logger.debug("preloading images is turned off!");

		sendEvent(new PageLoadEvent(this, doc, page));
	}
	
	public List<PageLock> listPageLocks(int colId, int docId, int pageNr) throws NoConnectionException, SessionExpiredException, ServerErrorException, IllegalArgumentException {
		checkConnection(true);
		
		return conn.listPageLocks(colId, docId, pageNr);
	}
	
	public List<TrpAction> listAllActions(int colId, int docId, int nValues) throws NoConnectionException, SessionExpiredException, ServerErrorException, IllegalArgumentException {
		checkConnection(true);
		final Integer[] typeIds = {1, 3}; // = Save | Status Change | Access Document = 4 (no need)
		return conn.listActions(typeIds, colId, docId, nValues);
	}
	
	
	
	public void lockPage(int colId, TrpPage page) throws NoConnectionException, SessionExpiredException, ServerErrorException {
		if (page.getDocId() > 0) {
			checkConnection(true);
			logger.debug("locking page: colId = "+colId+" docId = "+page.getDocId()+" pageNr = "+page.getPageNr());
			conn.lockPage(colId, page.getDocId(), page.getPageNr());
		} else {
			logger.debug("This is a local doc... locking no page!");
		}
		
	}
	
	public void releaseLocks() throws NoConnectionException, SessionExpiredException, ServerErrorException {
		checkConnection(true);
		
		conn.unlockPage();
	}
	
	public void tryReleaseLocks() {
		try {
			if (isLoggedIn()) {
				releaseLocks();
			}
		} catch (SessionExpiredException | ServerErrorException | NoConnectionException e) {
			logger.error("Error releasing locks: "+e.getMessage(), e);
		}
	}
	
	public void getLocks() {
		
		
		
		
		
	}

	public void syncTextOfDocFromWordsToLinesAndRegions(final int colId, IProgressMonitor monitor) throws JAXBException, IOException, InterruptedException, Exception {
		monitor.beginTask("Applying text from words", doc.getPages().size());
		int i = 1;
		for (TrpPage p : doc.getPages()) {
			monitor.subTask("Applying text of page " + i);

			List<TrpTranscriptMetadata> trlist = p.getTranscripts();
			Collections.sort(trlist);
			if (!trlist.isEmpty()) {
				TrpTranscriptMetadata md = trlist.get(trlist.size() - 1);
//				JAXBPageTranscript tr = TrpPageTranscriptBuilder.build(md);
				JAXBPageTranscript tr = new JAXBPageTranscript(md);
				tr.build();
				for (TrpTextRegionType region : tr.getPage().getTextRegions(true))
					region.applyTextFromWords();

//				saveTranscript(colId, p, tr, EditStatus.IN_PROGRESS);
				saveTranscript(colId, tr.getPage(), EditStatus.IN_PROGRESS, md.getTsId(), "Synced text from word to line regions");
			}

			if (monitor.isCanceled())
				throw new InterruptedException();

			monitor.worked(i + 1);
			++i;
		}
	}

	public void reloadTranscriptsList(int colId) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, Exception {
		// if (!isPageLoaded())
		// return;

		if (isRemoteDoc()) {
			checkConnection(true);
			
			int nValues = 10; // 0 for all!
			List<TrpTranscriptMetadata> list = conn.getTranscriptMdList(colId, doc.getMd().getDocId(), getPageIndex() + 1, 0, nValues, null, null);
			logger.debug("got transcripts: " + list.size());
			page.setTranscripts(list);
		}

		sendEvent(new TranscriptListLoadEvent(this, doc, page, isPageLoaded() ? page.getTranscripts() : null));
	}

	/**
	 * Reloads the current transcrition
	 * 
	 * @param tryLocalReload
	 *            If true, the transcription is reloaded from the locally stored
	 *            object (if it has been loaded already!)
	 * @throws IOException
	 *             , Exception
	 * @throws JAXBException
	 */
	public void reloadTranscript() throws JAXBException, IOException, Exception {
		if (!isLocalDoc() && !isLoggedIn())
			throw new Exception("No connection");
		else if (!isPageLoaded())
			throw new Exception("No page loaded");
		
		TrpTranscriptMetadata trMd = getTranscriptMetadata();
		if (trMd == null)
			throw new Exception("Transcript metadata is null -> should not happen...");

		logger.debug("reloading transcript 2: " + trMd);
		clearTranscriptContent();
//		transcript = TrpPageTranscriptBuilder.build(trMd); // OLD
		
		// NEW:
		TrpPageType p = getOrBuildPage(trMd, true);
		transcript.setMd(trMd);
		transcript.setPageData(p.getPcGtsType());
		
		setCurrentTranscriptEdited(false);
		if (!isLocalDoc()) {
			// FIXME:
			// PcGtsType pc = conn.getTranscript(doc.getId(), trMd.getPageNr());
			// isPageLocked = conn.isPageLocked(doc.getId(), trMd.getPageNr());

			// transcript = new JAXBPageTranscript(trMd, pc);
		}
		// TEST:
		// isPageLocked = true;
		
		// add foreign tags from this transcript:
		addForeignStructTagSpecsFromTranscript();

		sendEvent(new TranscriptLoadEvent(this, doc, page, transcript));
		logger.debug("loaded JAXB, regions: " + getNTextRegions());
	}
	
	public void analyzeStructure(int colId, int docId, int pageNr, boolean detectPageNumbers, boolean detectRunningTitles, boolean detectFootnotes) throws NoConnectionException, SessionExpiredException, ServerErrorException, IllegalArgumentException, JAXBException {
		checkConnection(true);
		
		PcGtsType pcGts = conn.analyzePageStructure(colId, docId, pageNr, detectPageNumbers, detectRunningTitles, detectFootnotes);
		
		transcript.setPageData(pcGts);
	}

	public void loadLocalDoc(String folder, IProgressMonitor monitor) throws UnsupportedFormatException, IOException, NullValueException {
		tryReleaseLocks();
		
		doc = LocalDocReader.load(folder); // TODO: integrate monitor feedback
		setCurrentPage(0);

		if (doc == null)
			throw new NullValueException(folder + " is null...");

		sendEvent(new DocLoadEvent(this, doc));

		logger.info("loaded local document, path = " + doc.getMd().getLocalFolder().getAbsolutePath() + ", title = " + doc.getMd().getTitle() + ", nPages = "
				+ doc.getPages().size());
	}

	public void loadRemoteDoc(int colId, int docId) throws SessionExpiredException, IllegalArgumentException, NoConnectionException {
		checkConnection(true);

		doc = conn.getTrpDoc(colId, docId, 1);
//		currentColId  = colId;
		setCurrentPage(0);

		sendEvent(new DocLoadEvent(this, doc));

		logger.info("loaded remote document, docId = " + doc.getId() + ", title = " 
				+ doc.getMd().getTitle() + ", nPages = " + doc.getPages().size() + ", pageId = " 
				+ doc.getMd().getPageId());
	}

	public TrpDoc getRemoteDoc(int colId, int docId, int nrOfTranscripts) throws SessionExpiredException, IllegalArgumentException, NoConnectionException {
		checkConnection(true);
		return conn.getTrpDoc(colId, docId, nrOfTranscripts);
	}
	
	/**
	 * Saves all transcripts in transcriptsMap.
	 * @param transcriptsMap A map of the transcripts to save. The map's key is the page-id, its value is a pair of the collection-id and the 
	 * corresponding TrpPageType object to save as newest version.
	 * @param monitor A progress monitor that can also be null is no GUI status update is needed.
	 */
	public void saveTranscriptsMap(Map<Integer, Pair<Integer, TrpPageType>> transcriptsMap, IProgressMonitor monitor)
			throws SessionExpiredException, ServerErrorException, IllegalArgumentException, Exception {
		if (monitor != null)
			monitor.beginTask("Saving affected transcripts", transcriptsMap.size());

		int c = 0;
		for (Pair<Integer, TrpPageType> ptPair : transcriptsMap.values()) {
			if (monitor != null && monitor.isCanceled())
				return;
			
			saveTranscript(ptPair.getLeft(), ptPair.getRight(), null, ptPair.getRight().getMd().getTsId(), "Tagged from text");

			if (monitor != null)
				monitor.worked(c++);

			++c;
		}
	}
	
	public void saveTranscript(int colId, String commitMessage) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, Exception {
		if (!isLocalDoc() && !isLoggedIn())
			throw new Exception("No connection");
		if (!isPageLoaded())
			throw new Exception("No page loaded");
		if (!hasTranscript()) {
			throw new Exception("No transcript loaded");
		}

		transcript.getPage().removeDeadLinks();
		
		logger.debug("saving transcription " + (getPageIndex() + 1) + " for doc " + doc.getMd().getDocId());
//		saveTranscript(colId, page, transcript, EditStatus.IN_PROGRESS);
		saveTranscript(colId, transcript.getPage(), transcript.getMd().getStatus(), transcript.getMd().getTsId(), commitMessage);
	}
	
//	public void saveTranscript(int colId, TrpPageType page) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, Exception {
//		saveTranscript(coldId, page.getMd().getPage(), page.getPcGtsType())
//		
//		
//	}
	
	public void saveTranscript(int colId, TrpPageType page, EditStatus status, int parentId, String commitMessage) 
			throws SessionExpiredException, ServerErrorException, IllegalArgumentException, Exception {
		if (page == null)
			throw new Exception("No page or metadata given");
		if (page.getMd() == null)
			throw new Exception("No page metadata set");
		
//		logger.debug("docId = "+docId);
				
		int docId = page.getMd().getDocId();
		
		if (docId != -1) {
			checkConnection(true);	
		}
		
		if (status == null || status.equals(EditStatus.NEW)){
			status = EditStatus.IN_PROGRESS;
		}
			
		if (docId != -1) {
			page.writeCustomTagsToPage();
			TrpTranscriptMetadata res = conn.updateTranscript(colId, docId, page.getMd().getPageNr(), status, page.getPcGtsType(), parentId, commitMessage);
		} else {
			LocalDocWriter.updateTrpPageXml(new JAXBPageTranscript(page.getMd(), page.getPcGtsType()));
		}
		page.setEdited(false);
		
		sendEvent(new TranscriptSaveEvent(this, colId, page.getMd()));
		reloadTranscriptsList(colId);
	}	

//	@Deprecated
//	// Too much fuzz
//	private void saveTranscript(int colId, TrpPage page, JAXBPageTranscript transcript, EditStatus status) 
//			throws SessionExpiredException, ServerErrorException, IllegalArgumentException, Exception {
//		if (page == null)
//			throw new Exception("No page given");
//		int docId = page.getDocId();
//		
//		if (docId != -1) {
//			checkConnection(true);	
//		}
//		if (transcript == null) {
//			throw new Exception("No transcript given");
//		}
//		
//		if (status == null)
//			status = EditStatus.IN_PROGRESS;
//		
//		if (page == null || transcript == null || status == null)
//			throw new Exception("Null values in saveTransript not allowed!");
//
//		if (docId != -1) {
//			transcript.getPage().writeCustomTagsToPage();
//			TrpTranscriptMetadata res = conn.updateTranscript(colId, docId, page.getPageNr(), EditStatus.IN_PROGRESS,
//					transcript.getPageData());
//		} else {
//			LocalDocWriter.updateTrpPageXml(transcript);
//		}
//		transcript.getPage().setEdited(false);
//		reloadTranscriptsList(colId);
//	}
	
	public void applyAffineTransformation(Collection<Integer> pageIndices, double tx, double ty, double sx, double sy, double rot, IProgressMonitor monitor) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, Exception {
		if (!isDocLoaded())
			throw new IOException("No document loaded!");
		
		int N = pageIndices == null ? doc.getNPages() : pageIndices.size();
		
		if (monitor != null)
			monitor.beginTask("Applying transformation", N);
		
		String trTxt = "tx="+tx+", ty="+ty+", sx="+sx+", sy="+sy+", rot="+rot;
		logger.info("affine transform is: "+trTxt);
		double rotRad = MathUtil.degToRad(rot);
		logger.debug("rotation in radiants = "+rotRad);
		
		int worked=0;
		
		for (int i=0; i<doc.getNPages(); ++i) {
			TrpPage p = doc.getPages().get(i);
			if (pageIndices != null && !pageIndices.contains(i))
				continue;
			
			if (monitor != null)
				monitor.subTask("Processing page "+(worked+1)+" / "+N);
			
			if (monitor != null && monitor.isCanceled())
				return;
			
			// unmarshal page:
			TrpTranscriptMetadata md = p.getCurrentTranscript();
			JAXBPageTranscript tr = new JAXBPageTranscript(md);
			tr.build();
			
			// apply transformation:
			TrpPageTypeUtils.applyAffineTransformation(tr.getPage(), tx, ty, sx, sy, rotRad);
			
			String msg = "Applied affine transformation: "+trTxt;
			
			saveTranscript(getCurrentDocumentCollectionId(), tr.getPage(), md.getStatus(), md.getParentTsId(), msg);
			
			if (monitor != null)
				monitor.worked(++worked);
		}

	}
	
	/**
	 * Synchronize local PAGE xml files for current document on server.
	 * If the number of local and remote pages do not match, file name matching is 
	 * checked and documents are synced according to filename. 
	 * If filenames do not match, the current document page is not touched 
	 * and a warning is passed issued.
	 * 
	 * @param pages local document pages
	 * @param checked indices of pages to sync
	 * @param monitor status of progress
	 * @throws IOException
	 * @throws SessionExpiredException
	 * @throws ServerErrorException
	 * @throws IllegalArgumentException
	 * @throws NoConnectionException
	 * @throws NullValueException
	 * @throws JAXBException
	 */
	public void syncDocPages(List<TrpPage> pages, List<Boolean> checked, IProgressMonitor monitor) 
			throws IOException, SessionExpiredException, ServerErrorException, IllegalArgumentException, NoConnectionException, NullValueException, JAXBException {
//		if (!localDoc.isLocalDoc())
//			throw new IOException("No local document given!");
		
		checkConnection(true);
		
		// alert if number of local and selected pages mismatch
		if (checked != null && pages.size() != checked.size()) {
			throw new IOException("Nr of checked list is unequal to nr of pages: "+checked.size()+"/"+pages.size());
		}
		
		// alert if no remote document (i.e. target doc) is loaded
		if (!isRemoteDoc())
			throw new IOException("No remote document loaded!");
		
		// calculate correct number of pages to sync for progress monitor output
		int nToSync = checked == null ? pages.size() : Utils.countTrue(checked);

		if (monitor != null)
			monitor.beginTask("Syncing doc pages with local pages", nToSync);

		// retrieve names of files in current doc
		List<String> remoteImgNames = doc.getPageImgNames();
		
		List<Integer> remoteIndices = new ArrayList<Integer>();
		List<Integer> syncIndices = new ArrayList<Integer>();
		
		// retrieve matching server page according to filename
		for (int i=0; i<pages.size(); ++i) {
			// loop until match is found in remote doc
			for (int j=0; j<remoteImgNames.size(); j++) {

				// check whether image filenames match (and incoming images are selected)
				if (StringUtils.contains(remoteImgNames.get(j), FilenameUtils.getBaseName(pages.get(i).getImgFileName()))
						&& (checked == null || checked.get(i))) {
					remoteIndices.add(j);
					syncIndices.add(i);
					logger.debug("Found remote match at position" + j + ": "+pages.get(i).getImgFileName());
					continue;
				}
			}
		}	

		// adopt nToSync to actual number
		nToSync = syncIndices.size();
		
		logger.debug("Synching "+nToSync+" pages " + remoteIndices);
		
		// TODO:FIXME decide what to do then !!! Until then: ignore :-) 
		// This case should occur if one or more
		// of the selected local images do not have 
		// matching images on the server 
		if (nToSync != pages.size()) {
			logger.warn("Found " + remoteIndices.size() +" pages on server, you gave me " + pages.size());
		}

		if (monitor != null)
			monitor.subTask("Found "+nToSync+ " images on server, will start syncing these now");
		
		// workflow to sync by filename
		int worked=0;
		for (int i=0; i<nToSync; ++i) {
			// metadata of entry of local document 
			TrpTranscriptMetadata tmd = pages.get(syncIndices.get(i)).getCurrentTranscript();

			logger.debug("syncing page "+(worked+1) + ": " + tmd.getUrl().getFile());
			
			if (monitor != null)
				monitor.subTask("Syncing page "+(worked+1)+" / "+nToSync + ": " + tmd.getUrl().getFile());
			
			if (monitor != null && monitor.isCanceled())
				return;
			
			conn.updateTranscript(getCurrentDocumentCollectionId(), doc.getMd().getDocId(), 
					(remoteIndices.get(i)+1), EditStatus.IN_PROGRESS,
					tmd.unmarshallTranscript(), tmd.getTsId(), "synched from local doc");

			if (monitor != null)
				monitor.worked(++worked);
		}
	}

	public void saveDocMd(int colId) throws SessionExpiredException, IllegalArgumentException, Exception {
		if (!isDocLoaded())
			throw new Exception("No document loaded");
	
		logger.debug("saving metadata for doc " + doc.getMd());
		if (isLocalDoc()) {
			LocalDocWriter.updateTrpDocMetadata(doc);
		} else {
			if (!isLoggedIn())
				throw new Exception("No connection");

			conn.updateDocMd(colId, doc.getMd().getDocId(), doc.getMd());
		}
		sendEvent(new DocMetadataUpdateEvent(this, doc, doc.getMd()));
	}
	
	/*
	 * 
	 */
	public void updateDocMd(int colId, TrpDocMetadata docMd) throws SessionExpiredException, IllegalArgumentException, Exception {
	
		logger.debug("saving metadata " + docMd);

		if (!isLoggedIn())
			throw new Exception("No connection");

		conn.updateDocMd(colId, docMd.getDocId(), docMd);
		
	}
	
	public TrpUpload uploadDocument(int colId, String folder, String title, IProgressMonitor monitor) throws IOException, Exception {
		if (!isLoggedIn())
			throw new Exception("Not logged in!");

		DocLoadConfig config = new DocLoadConfig();
		
		File inputDir = new File(folder);
		LocalDocReader.checkInputDir(inputDir);
		
		/*
		 * Check if an import of non-PAGE files is needed.
		 * Check for existence of a non-empty page dir
		 */
		final File pageDir = LocalDocReader.getPageXmlInputDir(inputDir);
		final boolean hasNonEmptyPageDir = pageDir.isDirectory() 
				&& pageDir.listFiles(ExtensionFileFilter.getXmlFileFilter()).length > 0;
		
		//if there is no page dir with files, then check if other files exist that should be converted
		if(!hasNonEmptyPageDir && 
				(LocalDocReader.getOcrXmlInputDir(inputDir).isDirectory()
				|| LocalDocReader.getAltoXmlInputDir(inputDir).isDirectory()
				|| LocalDocReader.getTxtInputDir(inputDir).isDirectory())) {
			//force page XML creation for importing existing text files
			config.setForceCreatePageXml(true);
		}
		
		TrpDoc doc = LocalDocReader.load(folder, config, monitor);
		if (title != null && !title.isEmpty()) {
			doc.getMd().setTitle(title);
		}

		return conn.uploadTrpDoc(colId, doc, monitor);
	}
	
	/**
	 * Upload pdf by extracting images first and uploading them as a new document
	 * @param colId ID of new collection
	 * @param file path of pdf file
	 * @param dirName name of directory
	 * @param monitor
	 * @throws IOException
	 * @throws Exception
	 */
	public void uploadDocumentFromPdf(int colId, String file, String dirName, final IProgressMonitor monitor) 
			throws IOException, Exception {
		if (!isLoggedIn())
			throw new Exception("Not logged in!");

		Observer o = new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				if(arg instanceof String) {
					monitor.subTask((String) arg);
				}
			}
		};
		
		// extract images from pdf and load images into Trp document
		TrpDoc doc = LocalDocReader.loadPdf(file, dirName, o);
		logger.debug("Extracted and loaded pdf " + file);

		conn.uploadTrpDoc(colId, doc, monitor);
	}
	
	public boolean checkDocumentOnPrivateFtp(String dirName) throws Exception {
		if (!isLoggedIn())
			throw new Exception("Not logged in!");
		
		return conn.checkDirOnFtp(dirName);
	}
	
	public void uploadDocumentFromPrivateFtp(int cId, String dirName, boolean checkForDuplicateTitle) throws Exception {
		if (!isLoggedIn())
			throw new Exception("Not logged in!");
		
		conn.ingestDocFromFtp(cId, dirName, checkForDuplicateTitle);
	}
	
	public void uploadDocumentFromMetsUrl(int cId, String metsUrlStr) throws SessionExpiredException, ServerErrorException, ClientErrorException, NoConnectionException, MalformedURLException, IOException{
//		if (!isLoggedIn())
//			throw new Exception("Not logged in!");
		checkConnection(true);
		URL metsUrl = new URL(metsUrlStr);
		if (metsUrl.getProtocol().startsWith("file")){
			conn.ingestDocFromLocalMetsUrl(cId, metsUrl);
		}
		else{
			conn.ingestDocFromUrl(cId, metsUrl);
		}
	}
	
	public String analyzeBlocks(int colId, int docId, int pageNr, PcGtsType pageData, boolean usePrintspaceOnly) throws SessionExpiredException, ServerErrorException,
			IllegalArgumentException, NoConnectionException {
		checkConnection(true);
		
		return conn.analyzeBlocks(colId, docId, pageNr, pageData, usePrintspaceOnly);
	}

	public String analyzeLines(int colId, int docId, int pageNr, PcGtsType pageData, List<String> regIds) throws SessionExpiredException, ServerErrorException,
			IllegalArgumentException, NoConnectionException {
		checkConnection(true);
		return conn.analyzeLines(colId, docId, pageNr, pageData, regIds);
	}

	public String analyzeWords(int colId, int docId, int pageNr, PcGtsType pageData, List<String> regIds) throws SessionExpiredException, ServerErrorException,
			IllegalArgumentException, NoConnectionException {
		checkConnection(true);
		return conn.analyzeWords(colId, docId, pageNr, pageData, regIds);
	}

	public String addBaselines(int colId, int docId, int pageNr, PcGtsType pageData, List<String> regIds) throws SessionExpiredException, ServerErrorException,
			IllegalArgumentException, NoConnectionException {
		checkConnection(true);
		return conn.addBaselines(colId, docId, pageNr, pageData, regIds);
	}
	
	public List<String> analyzeLayoutOnCurrentTranscript(List<String> regIds, boolean doBlockSeg, boolean doLineSeg, boolean doWordSeg, boolean doPolygonToBaseline, boolean doBaselineToPolygon, String jobImpl, ParameterMap pars) throws SessionExpiredException, ServerErrorException, ClientErrorException, IllegalArgumentException, NoConnectionException, IOException {
		checkConnection(true);
		
		if (!isRemoteDoc()) {
			throw new IOException("No remote doc loaded!");
		}
		int colId = getCurrentDocumentCollectionId();
		
		DocumentSelectionDescriptor dd = new DocumentSelectionDescriptor(getDocId());
		PageDescriptor pd = dd.addPage(getPage().getPageId());
		if (regIds != null && !regIds.isEmpty()) {
			pd.getRegionIds().addAll(regIds);
		}
		pd.setTsId(getTranscriptMetadata().getTsId());
		
		List<DocumentSelectionDescriptor> dsds = new ArrayList<>();
		dsds.add(dd);
		
		List<String> jobids = new ArrayList<>();
		List<TrpJobStatus> jobs = conn.analyzeLayout(colId, dsds, doBlockSeg, doLineSeg, doWordSeg, doPolygonToBaseline, doBaselineToPolygon, jobImpl, pars);
		for (TrpJobStatus j : jobs) {
			jobids.add(j.getJobId());
		}
				
		return jobids;
	}
	
	
	/**
	 * Wrapper method which takes a pages range string of the currently loaded document
	 */
	public List<String> analyzeLayoutOnLatestTranscriptOfPages(String pageStr, boolean doBlockSeg, boolean doLineSeg, boolean doWordSeg, boolean doPolygonToBaseline, boolean doBaselineToPolygon, String jobImpl, ParameterMap pars) throws SessionExpiredException, ServerErrorException, ClientErrorException, IllegalArgumentException, NoConnectionException, IOException {
		checkConnection(true);
		
		if (!isRemoteDoc()) {
			throw new IOException("No remote doc loaded!");
		}
		int colId = getCurrentDocumentCollectionId();
		
		DocumentSelectionDescriptor dd = getDoc().getDocSelectionDescriptorForPagesString(pageStr);
		List<DocumentSelectionDescriptor> dsds = new ArrayList<>();
		dsds.add(dd);
		
		List<String> jobids = new ArrayList<>();
		List<TrpJobStatus> jobs = conn.analyzeLayout(colId, dsds, doBlockSeg, doLineSeg, doWordSeg, doPolygonToBaseline, doBaselineToPolygon, jobImpl, pars);
		for (TrpJobStatus j : jobs) {
			jobids.add(j.getJobId());
		}
				
		return jobids;
	}
	
	public List<TrpJobStatus> analyzeLayout(int colId, List<DocumentSelectionDescriptor> dsds, boolean doBlockSeg, boolean doLineSeg, boolean doWordSeg, boolean doPolygonToBaseline, boolean doBaselineToPolygon, String jobImpl, ParameterMap pars) throws SessionExpiredException, ServerErrorException, ClientErrorException, IllegalArgumentException, NoConnectionException {
		checkConnection(true);
		return conn.analyzeLayout(colId, dsds, doBlockSeg, doLineSeg, doWordSeg, doPolygonToBaseline, doBaselineToPolygon, jobImpl, pars);
	}
	
	public String runOcr(int colId, int docId, String pageStr, OcrConfig config) throws NoConnectionException, SessionExpiredException, ServerErrorException, IllegalArgumentException {
		checkConnection(true);
		return conn.runOcr(colId, docId, pageStr, config.getTypeFace(), config.getLanguageString());
	}

	public void deleteDocument(int colId, int docId, boolean reallyDelete) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, NoConnectionException {
		checkConnection(true);

		conn.deleteDoc(colId, docId, reallyDelete);
	}
	
	public void deleteCurrentPage() throws NoConnectionException, SessionExpiredException, IllegalArgumentException {
		checkConnection(true);
		
		if (!isPageLoaded() || !isRemoteDoc())
			throw new IllegalArgumentException("No remote page loaded!");
		
		
		int colId = storage.getCurrentDocumentCollectionId();
		int docId = getDocId();
		int pageNr = getPage().getPageNr();
		
		deletePage(colId, docId, pageNr);
	}
	
	public void deletePage(int colId, int docId, int pageNr) throws NoConnectionException, SessionExpiredException, IllegalArgumentException {
		checkConnection(true);		
		conn.deletePage(colId, docId, pageNr);
	}
	
	public void addPage(final int colId, final int docId, final int pageNr, File imgFile, IProgressMonitor monitor) throws NoConnectionException{
		checkConnection(true);
		conn.addPage(colId, docId, pageNr, imgFile, monitor);
	}

	public String exportDocument(File dir, Set<Integer> pageIndices, boolean exportImg, 
			boolean exportPage, boolean exportAlto, boolean splitIntoWordsInAlto, 
			final String fileNamePattern, final ImgType imgType, final IProgressMonitor monitor, ExportCache cache) throws SessionExpiredException, ServerErrorException, IllegalArgumentException,
			NoConnectionException, Exception {
		if (!isDocLoaded())
			throw new Exception("No document is loaded!");
		
		FileUtils.forceMkdir(dir);

		// FileUtils.forceMkdir(new File(baseDir));

		// String dirName = isLocalDoc() ?
		// doc.getMd().getLocalFolder().getName() : "trp_doc_"+doc.getId();
		// String path = baseDir + "/" + dirName;
		// File destDir = new File(path);

		// if (destDir.exists())
		// throw new
		// Exception("Export directory already exists: "+destDir.getAbsolutePath());
		
		final int totalWork = pageIndices==null ? doc.getNPages() : pageIndices.size();		
		monitor.beginTask("Exporting document", totalWork);

		String path = dir.getAbsolutePath();
		logger.debug("Trying to export document to " + path);
			
		Observer o = new Observer() {
			int c=0;
			@Override public void update(Observable o, Object arg) {
				if (monitor != null && monitor.isCanceled()) {
					return;
				}
				if (arg instanceof Integer) {
					++c;
					monitor.subTask("Processed page " + c +" / "+totalWork);
					monitor.worked(c);
				}
			}
		};
		DocExporter de = new DocExporter(conn.newFImagestoreGetClient(), cache);
		de.addObserver(o);
		de.writeRawDoc(doc, path, true, pageIndices, exportImg, exportPage, exportAlto, splitIntoWordsInAlto, fileNamePattern, imgType);

		return path;
	}

	public String exportPdf(File pdf, Set<Integer> pageIndices, final IProgressMonitor monitor, final boolean extraTextPages, final boolean imagesOnly, Set<String> selectedTags, final boolean highlightTags, final boolean highlightArticles, final boolean wordBased, final boolean doBlackening, boolean createTitle, ExportCache cache, String exportFontname, ImgType imgType) throws MalformedURLException, DocumentException,
			IOException, JAXBException, InterruptedException, Exception {
		if (!isDocLoaded())
			throw new Exception("No document is loaded!");
		if (pdf.isDirectory()) {
			throw new IOException(pdf.getAbsolutePath() + " is not a valid file name!");
		}
		if (!pdf.getParentFile().exists()) {
			throw new IOException("Directory " + pdf.getParent() + " does not exist!");
		}

		logger.debug("Trying to export PDF file to " + pdf.getAbsolutePath());
		
		final int totalWork = pageIndices==null ? doc.getNPages() : pageIndices.size();
		monitor.beginTask("Creating PDF document" , totalWork);

		final PdfExporter pdfExp = new PdfExporter();
		Observer o = new Observer() {
			int c=0;
			@Override public void update(Observable o, Object arg) {
				if (monitor != null && monitor.isCanceled()) {
					pdfExp.cancel = true;
//					return;
				}
				
				if (arg instanceof Integer) {
					++c;
					monitor.setTaskName("Exported page " + c);
					monitor.worked(c);
					
					
				} else if (arg instanceof String) {
					monitor.setTaskName((String) arg);
				}
			}
		};
		pdfExp.addObserver(o);
		
		pdf = pdfExp.export(doc, pdf.getAbsolutePath(), pageIndices, wordBased, extraTextPages, imagesOnly, highlightTags, highlightArticles, doBlackening, createTitle, cache, exportFontname, imgType);

		return pdf.getAbsolutePath();
	}

	public String exportTei(File tei, CommonExportPars commonPars, TeiExportPars pars, IProgressMonitor monitor, ExportCache cache) throws IOException, Exception {
		if (!isDocLoaded())
			throw new Exception("No document is loaded!");
		if (tei.isDirectory()) {
			throw new IOException(tei.getAbsolutePath() + " is not a valid file name!");
		}
		if (!tei.getParentFile().exists()) {
			throw new IOException("Directory " + tei.getParent() + " does not exist!");
		}

		logger.debug("Trying to export TEI XML file to " + tei.getAbsolutePath());
		
//		TrpTeiDomBuilder builder = new TrpTeiDomBuilder(doc, mode, monitor, pageIndices);
		ATeiBuilder builder = new TrpTeiStringBuilder(doc, commonPars, pars, monitor);
		builder.addTranscriptsFromCache(cache);
		builder.buildTei();
		
		if (monitor != null)
			monitor.setTaskName("Writing TEI XML file");
		
		builder.writeTeiXml(tei);

		return tei.getAbsolutePath();
	}
	
	public String exportAlto(File altoDir, final IProgressMonitor monitor) throws IOException, Exception {
		
		FileUtils.forceMkdir(altoDir);
		
		if (!isDocLoaded())
			throw new Exception("No document is loaded!");

		if (!altoDir.exists()) {
			throw new IOException("Directory " + altoDir + " does not exist!");
		}

		logger.debug("Trying to export Alto XML file to " + altoDir.getAbsolutePath());
		AltoExporter altoExp = new AltoExporter();
		Observer o = new Observer() {
			@Override public void update(Observable o, Object arg) {
				if (arg instanceof Integer) {
					int i = (Integer) arg;
					monitor.setTaskName("Exporting page " + i);
					monitor.worked(i);
				} else if (arg instanceof String) {
					monitor.setTaskName((String) arg);
				}
			}
		};
		altoExp.addObserver(o);
		altoExp.export(doc, altoDir.getAbsolutePath());

		return altoDir.getAbsolutePath();
	}
	
	public List<TrpCollection> getCollections() { return collections; }
	
	public List<TrpDocMetadata> getDeletedDocList() {
		return deletedDocList;
	}

	public TrpCollection getCollection(int colId) {
		for (TrpCollection c : collections) {
			if (c.getColId() == colId) {
				return c;
			}
		}
		return null;
	}
	
	public List<TrpCollection> getCollectionsCanManage() {
		List<TrpCollection> ccm = new ArrayList<>();
		for (TrpCollection c : collections) {
			 if (c.getRole()==null || c.getRole().canManage()) { // if role==null -> admin!
				 ccm.add(c);
			 }
		}
		
		return ccm;
	}
	
	public synchronized void clearCollections() {
		collections.clear();
		sendEvent(new CollectionsLoadEvent(this, user, collections));
	}
	
	public void reloadCollections() throws NoConnectionException, SessionExpiredException, ServerErrorException, IllegalArgumentException {
		checkConnection(true);
		
		logger.debug("loading collections from server");
		List<TrpCollection> collectionsFromServer = conn.getAllCollections(0, 0, null, null);
		logger.debug("got collections: "+(collectionsFromServer==null?"null":collectionsFromServer.size()));
		
		collections.clear();
		collections.addAll(collectionsFromServer);

		sendEvent(new CollectionsLoadEvent(this, user, collections));
	}
	
	public int addCollection(String name) throws NoConnectionException, SessionExpiredException, ServerErrorException {
		checkConnection(true);
		
		return conn.createCollection(name);
	}
	

	public void checkConnection(boolean checkLoggedIn) throws NoConnectionException {
		if (conn == null || (checkLoggedIn && conn.getUserLogin() == null)) {
			throw new NoConnectionException("No connection to the server!");
		}
	}
	
//	public List<EdFeature> getEditDeclFeatures(TrpDoc doc) throws NoConnectionException, SessionExpiredException, ServerErrorException, IllegalArgumentException {
////		checkConnection(true);
//		List<EdFeature> features;
//		if(isLoggedIn() && isRemoteDoc(doc)) {
//			if (doc.getCollection() == null)
//				throw new IllegalArgumentException("Collection not set for remote doc: "+doc);
//			
//			features = conn.getEditDeclByDoc(doc.getCollection().getColId(), doc.getId());
//		} else {
//			File editDecl = new File(doc.getMd().getLocalFolder() + File.separator + "editorialDeclaration.xml");
//			if(editDecl.isFile()){
//				try {
//					JaxbList<EdFeature> list = JaxbUtils.unmarshal(editDecl, JaxbList.class, EdFeature.class, EdOption.class);
//					features = list.getList();
//				} catch (FileNotFoundException | JAXBException e) {
//					features = new ArrayList<EdFeature>(0);
//					e.printStackTrace();
//				}
//			} else {
//				features = new ArrayList<EdFeature>(0);
//			}
//		}
//		return features;
//		
//	}
	
	public List<EdFeature> getAvailFeatures() throws NoConnectionException, SessionExpiredException, ServerErrorException, IllegalArgumentException {
//		checkConnection(true);
		List<EdFeature> features;
		if(isLoggedIn() && isRemoteDoc()){
			features = conn.getEditDeclFeatures(getCurrentDocumentCollectionId());
		} else {
			try {
				features = new TrpServerConn(TrpServerConn.PROD_SERVER_URI).getEditDeclFeatures(0);
			} catch (LoginException e) {
				//is only thrown if uriStr is null or empty
				e.printStackTrace();
				features = new ArrayList<>(0);
			}
		}
		return features;
	}
	
	public List<EdFeature> getEditDeclFeatures() throws NoConnectionException, SessionExpiredException, ServerErrorException, IllegalArgumentException {
//		checkConnection(true);
		List<EdFeature> features;
		if(isLoggedIn() && isRemoteDoc()){
			features = conn.getEditDeclByDoc(getCurrentDocumentCollectionId(), doc.getId());
		} else {
			features = LocalDocReader.readEditDeclFeatures(getDoc().getMd().getLocalFolder());
		}
		return features;
	}
	
	public void saveEditDecl(List<EdFeature> feats) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, NoConnectionException, FileNotFoundException, JAXBException {
		if(isLoggedIn() && isRemoteDoc()){
			saveEditDecl(doc.getId(), feats);
		} else {
			LocalDocWriter.writeEditDeclFeatures(feats, getDoc().getMd().getLocalFolder());
		}
		
		doc.setEdDeclList(feats);
	}
	
	public void saveEditDecl(final int docId, List<EdFeature> feats) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, NoConnectionException {
		checkConnection(true);
		conn.postEditDecl(getCurrentDocumentCollectionId(), docId, feats);
	}

	public void storeEdFeature(EdFeature feat, boolean isCollectionFeature) throws NoConnectionException, SessionExpiredException, ServerErrorException, IllegalArgumentException {
		checkConnection(true);
		if(isCollectionFeature){
			feat.setColId(getCurrentDocumentCollectionId());
		} else {
			feat.setColId(null);
		}
		conn.postEditDeclFeature(getCurrentDocumentCollectionId(), feat);
	}

	public void deleteEdFeature(EdFeature feat) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, NoConnectionException {
		checkConnection(true);
		conn.deleteEditDeclFeature(getCurrentDocumentCollectionId(), feat);
	}
	
	public void storeCrowdProject(TrpCrowdProject project, int collId) throws NoConnectionException, SessionExpiredException, ServerErrorException, IllegalArgumentException {
		checkConnection(true);
		conn.postCrowdProject(collId, project);
	}
	
	public int storeCrowdProjectMilestone(TrpCrowdProjectMilestone milestone, int collId) throws NoConnectionException, SessionExpiredException, ServerErrorException, IllegalArgumentException {
		checkConnection(true);
		return conn.postCrowdProjectMilestone(collId, milestone);
	}
	
	public int storeCrowdProjectMessage(TrpCrowdProjectMessage message, int collId) throws NoConnectionException, SessionExpiredException, ServerErrorException, IllegalArgumentException {
		checkConnection(true);
		return conn.postCrowdProjectMessage(collId, message);
	}
	
	public void storeEdOption(EdOption opt) throws NoConnectionException, SessionExpiredException, ServerErrorException, IllegalArgumentException {
		checkConnection(true);
		conn.postEditDeclOption(getCurrentDocumentCollectionId(), opt);
	}

	public void deleteEdOption(EdOption opt) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, NoConnectionException {
		checkConnection(true);
		conn.deleteEditDeclOption(getCurrentDocumentCollectionId(), opt);
	}

	public void deleteTranscript(TrpTranscriptMetadata tMd) throws NoConnectionException, SessionExpiredException, ServerErrorException, IllegalArgumentException {
		checkConnection(true);
		conn.deleteTranscript(this.getCurrentDocumentCollectionId(), this.getDoc().getId(), 
				this.getPage().getPageNr(), tMd.getKey());
	}
	
	public TrpRole getRoleOfUserInCurrentCollection() {
		return StorageUtil.getRoleOfUserInCurrentCollection();
	}
	
	public TrpJobStatus createSample(Map<TrpDocMetadata, List<TrpPage>> sampleDocMap, int nrOfLines, String sampleName, String sampleDescription) throws SessionExpiredException, ServerErrorException, ClientErrorException, IllegalArgumentException {
		List<DocumentSelectionDescriptor> descList = null;
		try {
			descList = DescriptorUtils.buildCompleteSelectionDescriptorList(sampleDocMap, null);
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not build selection descriptor list");
		}
		conn.createSampleJob(collId,descList,nrOfLines, sampleName, sampleDescription);
		return null;
	}
	
	public TrpJobStatus computeSampleRate(int docId, ParameterMap params) throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		return conn.computeSampleJob(docId,params);
	}

	
	public TrpJobStatus computeErrorRate(int docId, final String pageStr, ParameterMap params) throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		//TODO
		return conn.computeErrorRateWithJob(docId, pageStr, params);
		
	}
	
	public TrpErrorRateResult computeErrorRate(TrpTranscriptMetadata ref, TrpTranscriptMetadata hyp) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, NoConnectionException {
		checkConnection(true);
		if(ref == null || hyp == null){
			throw new IllegalArgumentException("A parameter is null!");
		}	
		return conn.computeErrorRate(ref.getKey(), hyp.getKey());
	}
	
	@Deprecated
	public String computeWer(TrpTranscriptMetadata ref, TrpTranscriptMetadata hyp) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, NoConnectionException {
		checkConnection(true);
		if(ref == null || hyp == null){
			throw new IllegalArgumentException("A parameter is null!");
		}	
		String result = conn.computeWer(ref.getKey(), hyp.getKey());
		result = result.replace("WER", "Word Error Rate:");
		result = result.replace("CER", "Character Error Rate:");
		return result;
	}
	
	public ImageMetadata getCurrentImageMetadata() {
		return imgMd;
	}
	
	private void setCurrentImageMetadata() throws IOException{
		TrpPage page = getPage();
		if (isLocalDoc() || page == null) {
			imgMd = null;
			return;
		}
		
		try {
			FimgStoreGetClient getter = new FimgStoreGetClient(page.getUrl());
			imgMd = (ImageMetadata)getter.getFileMd(page.getKey());
		} catch (Exception e) {
			logger.error("Couldn't read metadata for file: "+page.getUrl());
			imgMd = null;
		}
	}

	public void replacePageImgFile(File imgFile, IProgressMonitor monitor) throws Exception {
		checkConnection(true);
		TrpPage newPage = conn.replacePageImage(this.getCurrentDocumentCollectionId(), this.getDocId(), 
				this.getPage().getPageNr(), imgFile, monitor);
		this.doc.getPages().set(getPageIndex(), newPage);
		this.page = newPage;
	}
	
	public FulltextSearchResult searchFulltext(String query, SearchType type, Integer start, Integer rows, List<String> filters) throws NoConnectionException, SessionExpiredException, ServerErrorException, ClientErrorException {
		checkConnection(true);
		return conn.searchFulltext(query, type, start, rows, filters);
	}
	
	public void movePage(final int colId, final int docId, final int pageNr, final int toPageNr) throws SessionExpiredException, ServerErrorException, ClientErrorException, NoConnectionException{
		checkConnection(true);
		conn.movePage(colId, docId, pageNr, toPageNr);
	}

	public List<TrpDocDir> listDocDirsOnFtp() throws SessionExpiredException, ServerErrorException, IllegalArgumentException, NoConnectionException {
		checkConnection(true);
		return conn.listDocsOnFtp();
	}

	public List<TrpEvent> getEvents() throws SessionExpiredException, ServerErrorException, IllegalArgumentException, NoConnectionException {
		checkConnection(true);
		TrpSettings sets = TrpConfig.getTrpSettings();
		List<TrpEvent> events = conn.getNextEvents(sets.getShowEventsMaxDays());
		Collections.sort(events);
		List<String> lines;
		try{
			lines = FileUtils.readLines(new File(sets.getEventsTxtFileName()));
		} catch (Exception e){
			logger.debug("No " + sets.getEventsTxtFileName() + " found. Show all events...");
			lines = new ArrayList<String>(0);
		}
		for(String l : lines){
			int id;
			try{
				id = Integer.parseInt(l);
			} catch (Exception e){
				continue;
			}
			Iterator<TrpEvent> it = events.iterator();
			while(it.hasNext()){
				TrpEvent ev = it.next();
				if(ev.getId() == id){
					it.remove();
//					break;
				}
			}
		}
		return events;
	}
	public void markEventAsRead(int id) throws IOException{
		TrpSettings sets = TrpConfig.getTrpSettings();
		File eventsTxt = new File(sets.getEventsTxtFileName());
		ArrayList<String> lines = new ArrayList<>(1);
		lines.add(""+id);
		FileUtils.writeLines(eventsTxt, lines, true);
	}

	public String duplicateDocument(int colId, int docId, String newName, Integer toColId) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, NoConnectionException {
		checkConnection(true);
		
		return conn.duplicateDocument(colId, docId, newName, toColId);		
	}

	public void batchReplaceImages(List<TrpPage> checkedPages, List<URL> checkedUrls, IProgressMonitor monitor) throws Exception {
		logger.info("batch replacing "+checkedPages.size()+" images!");

		checkConnection(true);
		
		if (checkedPages.size() != checkedUrls.size()) {
			throw new IOException("Nr of checked pages is unequal to nr of images: "+checkedPages.size()+"/"+checkedUrls.size());
		}
		if (!isRemoteDoc())
			throw new IOException("No remote document loaded!");
		
		int N = checkedPages.size();
		
		if (monitor != null)
			monitor.beginTask("Replacing images", N);

		for (int i=0; i<checkedPages.size(); ++i) {
			if (monitor.isCanceled())
				break;
			
			if (monitor != null)
				monitor.subTask("Replacing page "+(i+1)+" / "+N);			
			
			TrpPage p = checkedPages.get(i);
			int pageIndex = doc.getPageIndex(p);
			
			URL u = checkedUrls.get(i);
			
			if (!CoreUtils.isLocalFile(u))
				throw new Exception("Not a local file: "+u);
			
			File imgFile = FileUtils.toFile(u);
			
			TrpPage newPage = conn.replacePageImage(getCurrentDocumentCollectionId(), getDocId(), p.getPageNr(), imgFile, null);
			doc.getPages().set(pageIndex, newPage);
			
			if (monitor != null)
				monitor.worked(i+1);
		}
	}

	public void updateProxySettings() {
		ProxyPrefs p = TrpGuiPrefs.getProxyPrefs();
		if(p.isEnabled()) {
			logger.debug("PROXY IS ENABLED");
			ProxyUtils.setProxy(p);
		} else {
			logger.debug("PROXY IS DISABLED");
			ProxyUtils.unsetProxy();
		}
		ProxyUtils.logProxySettings();
	}
	
	/*
	 * HTR stuff
	 */
	
	public List<TrpHtr> listHtrs(String provider) throws NoConnectionException, SessionExpiredException, ServerErrorException, ClientErrorException {
		checkConnection(true);
		logger.debug("Listing HTRs: colId = " + this.getCollId() + " - provider = " + provider);
		return conn.getHtrs(this.getCollId(), provider);		
	}
	
	public String runHtr(String pages, TextRecognitionConfig config) throws NoConnectionException, SessionExpiredException, ServerErrorException, ClientErrorException {
		checkConnection(true);
		switch(config.getMode()) {
		case CITlab:
			return conn.runCitLabHtr(getCurrentDocumentCollectionId(), getDocId(), pages, 
					config.getHtrId(), config.getDictionary());
		case UPVLC:
			return null;
		default:
			return null;
		}
	}
	
	public String runHtrTraining(CitLabHtrTrainConfig config) throws SessionExpiredException, ServerErrorException, ClientErrorException, IllegalArgumentException {
		if(config == null) {
			throw new IllegalArgumentException("Config is null!");
		}
		return conn.runCitLabHtrTraining(config);
	}
	
	public String runCitLabText2Image(CitLabSemiSupervisedHtrTrainConfig config) throws SessionExpiredException, ServerErrorException, ClientErrorException, IllegalArgumentException {
		if(config == null) {
			throw new IllegalArgumentException("Config is null!");
		}
		return conn.runCitLabText2Image(config);
	}
	
	public TrpDoc getTestSet(TrpHtr htr) throws SessionExpiredException, ClientErrorException, IllegalArgumentException, NoConnectionException {
		checkConnection(true);
		return conn.getHtrTestDoc(collId, htr.getHtrId(), 1);
	}
	
	public TrpDoc getTrainSet(TrpHtr htr) throws SessionExpiredException, ClientErrorException, IllegalArgumentException, NoConnectionException {
		checkConnection(true);
		return conn.getHtrTrainDoc(collId, htr.getHtrId(), 1);
	}

	public void saveTextRecognitionConfig(TextRecognitionConfig config) {
		RecognitionPreferences.save(collId, this.conn.getServerUri(), config);		
	}
	
	public TextRecognitionConfig loadTextRecognitionConfig() {
		return RecognitionPreferences.getHtrConfig(collId, this.conn.getServerUri());
	}
	
	public OcrConfig loadOcrConfig() {
		return RecognitionPreferences.getOcrConfig(collId,  this.conn.getServerUri());
	}
	
	public void saveOcrConfig(OcrConfig config) {
		RecognitionPreferences.save(collId,  this.conn.getServerUri(), config);
	}
	
	/*
	 * old HTR stuff
	 */
	
	public List<HtrModel> getHtrModels() throws NoConnectionException, SessionExpiredException, ServerErrorException, IllegalArgumentException {
		checkConnection(true);
		List<HtrModel> models = conn.getHtrModelList();
		//remove models that are incompatible with HTR in Transkribus 
		List<HtrModel> out = new LinkedList<>();
		for(HtrModel m : models){
			if(m.getIsUsableInTranskribus() == 1){
				out.add(m);
			}
		}
		return models;
	}
	
	public String[] getHtrModelsStr() {
		return htrModelList.toArray(new String[htrModelList.size()]);
	}
	
	public String runRnnHtr(int colId, int docId, String pageStr, String netName, String dictName) throws SessionExpiredException, ServerErrorException, ClientErrorException, IllegalArgumentException, NoConnectionException {
		checkConnection(true);
		return conn.runRnnHtr(colId, docId, pageStr, netName, dictName);
	}
	
	@Deprecated
	public List<String> getHtrNets() throws NoConnectionException, SessionExpiredException, ServerErrorException, ClientErrorException, IllegalArgumentException{
		checkConnection(true);
		return conn.getHtrRnnListText();
	}
	
	public List<String> getHtrDicts() throws NoConnectionException, SessionExpiredException, ServerErrorException, ClientErrorException, IllegalArgumentException{
		checkConnection(true);
		List<String> sortedDictList = conn.getHtrDictListText();
		Collections.sort(sortedDictList);
		return sortedDictList;
	}

	public void reloadHtrModelsStr() throws SessionExpiredException, ServerErrorException, IllegalArgumentException, NoConnectionException {
		checkConnection(true);
		List<HtrModel> models = conn.getHtrModelList();
		List<String> htrModelsStrArr = new ArrayList<>(models.size());
		for(int i = 0; i < models.size(); i++){
			final HtrModel m = models.get(i);
			if(m.getIsUsableInTranskribus() == 1){
				htrModelsStrArr.add(m.getModelName());
			}
		}
		
		htrModelList = htrModelsStrArr;		
	}
	
	public void clearHtrModels(){
		htrModelList = new ArrayList<>(0);
	}

	public void addHtrToCollection(TrpHtr htr, TrpCollection col) throws SessionExpiredException, ServerErrorException, ClientErrorException, NoConnectionException {
		checkConnection(true);
		conn.addHtrToCollection(htr.getHtrId(), this.collId, col.getColId());		
	}

	public void removeHtrFromCollection(TrpHtr htr) throws SessionExpiredException, ServerErrorException, ClientErrorException, NoConnectionException {
		checkConnection(true);
		conn.removeHtrFromCollection(htr.getHtrId(), this.collId);		
	}

	public TrpCrowdProject loadCrowdProject(int colId) throws NoConnectionException, SessionExpiredException, ServerErrorException, ClientErrorException {
		checkConnection(true);
		return conn.getCrowdProject(colId);		
	}

	public void reloadDocWithAllTranscripts() throws SessionExpiredException, ClientErrorException, IllegalArgumentException {
		doc = conn.getTrpDoc(this.collId, doc.getMd().getDocId(), -1);
		
	}
	
	
	// CUSTOM TAG SPEC STUFF:
	
	public List<CustomTagSpec> getCustomTagSpecs() {
		return customTagSpecs;
	}
	
	public List<StructCustomTagSpec> getStructCustomTagSpecs() {
		return structCustomTagSpecs;
	}
	
	public List<String> getStructCustomTagSpecsTypeStrings() {
		return structCustomTagSpecs.stream().map(t -> t.getCustomTag().getType()).collect(Collectors.toList());
	}
	
	public void addCustomTagSpec(CustomTagSpec tagSpec, boolean collectionSpecific) {
		if (collectionSpecific){
			collectionSpecificTagSpecs.add(tagSpec);
			sendEvent(new TagSpecsChangedEvent(this, collectionSpecificTagSpecs));
			return;
		}
	
		customTagSpecs.add(tagSpec);
		CustomTagSpecUtil.checkTagSpecsConsistency(customTagSpecs);
		sendEvent(new TagSpecsChangedEvent(this, customTagSpecs));
	
		try {
			storeCustomTagSpecsForCurrentCollection();
		} catch (ClientErrorException | IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void removeCustomTagSpec(CustomTagSpec tagDef, boolean collectionSpecific) {
		if (collectionSpecific){
			collectionSpecificTagSpecs.remove(tagDef);
			sendEvent(new TagSpecsChangedEvent(this, collectionSpecificTagSpecs));
			return;
		}
		
		customTagSpecs.remove(tagDef);
		CustomTagSpecUtil.checkTagSpecsConsistency(customTagSpecs);
		sendEvent(new TagSpecsChangedEvent(this, customTagSpecs));
		
		try {
			storeCustomTagSpecsForCurrentCollection();
		} catch (ClientErrorException | IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public void addStructCustomTagSpec(StructCustomTagSpec tagSpec) {
		structCustomTagSpecs.add(tagSpec);
		CustomTagSpecUtil.checkTagSpecsConsistency(structCustomTagSpecs);
		sendEvent(new StructTagSpecsChangedEvent(this, structCustomTagSpecs));
	
		storeStructCustomTagSpecsForCurrentCollection();
	}
	
	public String getNewStructCustomTagColor() {
		return CustomTagSpecUtil.getNewStructSpecColor(structCustomTagSpecs);
	}
	
	public void removeStructCustomTagSpec(CustomTagSpec tagDef) {
		structCustomTagSpecs.remove(tagDef);
		CustomTagSpecUtil.checkTagSpecsConsistency(structCustomTagSpecs);
		sendEvent(new StructTagSpecsChangedEvent(this, structCustomTagSpecs));
		
		storeStructCustomTagSpecsForCurrentCollection();
	}
	
	public void signalCustomTagSpecsChanged() {
		CustomTagSpecUtil.checkTagSpecsConsistency(customTagSpecs);
		sendEvent(new TagSpecsChangedEvent(this, customTagSpecs));
		try {
			storeCustomTagSpecsForCurrentCollection();
		} catch (ClientErrorException | IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public void signalStructCustomTagSpecsChanged() {
		CustomTagSpecUtil.checkTagSpecsConsistency(structCustomTagSpecs);
		sendEvent(new StructTagSpecsChangedEvent(this, structCustomTagSpecs));
		storeStructCustomTagSpecsForCurrentCollection();
	}
	
	public CustomTagSpec getCustomTagSpecWithShortCut(String shortCut) {
		return CustomTagSpecUtil.getCustomTagSpecWithShortCut(customTagSpecs, shortCut);
	}
	
	public StructCustomTagSpec getStructCustomTagSpecWithShortCut(String shortCut) {
		return CustomTagSpecUtil.getCustomTagSpecWithShortCut(structCustomTagSpecs, shortCut);
	}
	
	private void storeCustomTagSpecsForCurrentCollection() {
		logger.debug("updating custom tag specs for local mode, customTagSpecs: "+customTagSpecs);
		CustomTagSpecUtil.writeCustomTagSpecsToSettings(customTagSpecs);
	}
	
	public void storeStructCustomTagSpecsForCurrentCollection() {
		logger.debug("updating struct custom tag specs for local mode, structCustomTagSpecs: "+structCustomTagSpecs);
		CustomTagSpecUtil.writeStructCustomTagSpecsToSettings(structCustomTagSpecs);
	}
	
	private void readTagSpecsFromLocalSettings() {
		customTagSpecs.clear();
		customTagSpecs.addAll(CustomTagSpecUtil.readCustomTagSpecsFromSettings());
		
		sendEvent(new TagSpecsChangedEvent(this, customTagSpecs));
	}
	
	public void readCollectionTagSpecsFromDB() {
		collectionSpecificTagSpecs.clear();
		try {
			if (conn != null){
				logger.debug("tag Defs = " + conn.getTagDefsCollection(collId));
				//List<CustomTagSpec> tagDefs = CustomTagSpecUtil.readCustomTagSpecsFromJsonString(conn.getTagDefsUser());
				
				List<String> tagNames = CustomTagSpecDBUtil.readCustomTagSpecsFromJsonString(conn.getTagDefsCollection(collId));
				List<CustomTagSpec> tagDefs = new ArrayList<CustomTagSpec>();

				for (String tn : tagNames){
					CustomTag ct = CustomTagFactory.getTagObjectFromRegistry(tn);
					//not null if tagname from DB is already known in the client
					if (ct != null){
						CustomTagSpec cts = new CustomTagSpec(ct);
						tagDefs.add(cts);
					}
					else{
						//TODO: add custom tag from DB to object registry because it is it known for this user
					}
				}

				if (tagDefs != null){
					collectionSpecificTagSpecs.addAll(tagDefs);
				}
			}
		} catch (SessionExpiredException | ServerErrorException | ClientErrorException | IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//sendEvent(new TagDefsChangedEvent(this, collectionSpecificTagSpecs));
	}

	private void readStructTagSpecsFromLocalSettings() {
		structCustomTagSpecs.clear();
		structCustomTagSpecs.addAll(CustomTagSpecUtil.readStructCustomTagSpecsFromSettings());
		
		if (structCustomTagSpecs.isEmpty()) {
			structCustomTagSpecs.addAll(getDefaultStructCustomTagSpecs());
		}
		
		sendEvent(new StructTagSpecsChangedEvent(this, structCustomTagSpecs));
	}
	
	private void addForeignStructTagSpecsFromTranscript() {
		if (transcript != null) {
			int sizeBefore = structCustomTagSpecs.size();
			for (ITrpShapeType st : transcript.getPage().getAllShapes(true)) {
				String structType = CustomTagUtil.getStructure(st);	

				if (!StringUtils.isEmpty(structType)) {
					
					/*
					 * for articles the structType could be extended with the id to get differentiation between different articles of a page
					 */
					logger.debug("structType for adding foreign struct tag specs: " + structType);
					if (structType.equals("article")){
						StructureTag stStructTag = CustomTagUtil.getStructureTag(st);
						String id = (String) stStructTag.getAttributeValue("id");
						//logger.debug("attribute id of structure" + id);
						structType = structType.concat("_"+id);
						StructCustomTagSpec spec = getStructCustomTagSpec(structType);	
						
						if (spec == null) { // tag not found --> create new one and add it to the list with a new color!
							StructureTag newStructTag = new StructureTag(structType);
							try {
								newStructTag.setAttribute("id", id, true);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							spec = new StructCustomTagSpec(newStructTag, getNewStructCustomTagColor());
							logger.debug("adding foreign page from transcript: "+spec);
							structCustomTagSpecs.add(spec);
						}

					}
					else{
					
						StructCustomTagSpec spec = getStructCustomTagSpec(structType);		
						if (spec == null) { // tag not found --> create new one and add it to the list with a new color!
							spec = new StructCustomTagSpec(new StructureTag(structType), getNewStructCustomTagColor());
							logger.debug("adding foreign page from transcript: "+spec);
							structCustomTagSpecs.add(spec);
						}
					}
				}
			}
			if (sizeBefore != structCustomTagSpecs.size()) {
				logger.debug("added "+(structCustomTagSpecs.size()-sizeBefore)+" foreign tags!");
				sendEvent(new StructTagSpecsChangedEvent(this, structCustomTagSpecs));
			}
		}
	}
	
	public void restoreDefaultStructCustomTagSpecs() {
		structCustomTagSpecs.clear();
		structCustomTagSpecs.addAll(getDefaultStructCustomTagSpecs());
		
		sendEvent(new StructTagSpecsChangedEvent(this, structCustomTagSpecs));
	}
	
	public static List<StructCustomTagSpec> getDefaultStructCustomTagSpecs() {
		List<StructCustomTagSpec> specs = new ArrayList<>();
		
		int i=0;
		for (TextTypeSimpleType tt : TextTypeSimpleType.values()) {
			
			String colorCode = TaggingWidgetUtils.getColorCodeForIndex(i++);
			RGB rgb = Colors.toRGB(colorCode);
			if (rgb.equals(StructCustomTagSpec.DEFAULT_COLOR)) { // get next color if this was the default color!
				colorCode = TaggingWidgetUtils.getColorCodeForIndex(i++);
			}
			
			StructCustomTagSpec spec = new StructCustomTagSpec(new StructureTag(tt.value()), colorCode);
			specs.add(spec);
		}
		
		return specs;
	}
	
	public boolean hasStructCustomTagSpec(String type) {
		return getStructCustomTagSpec(type) != null;
	}
	
	public StructCustomTagSpec getStructCustomTagSpec(String type) {
//		for (StructCustomTagSpec spec : structCustomTagSpecs){
//			logger.debug("spec " + spec.getCustomTag().getType());
//		}
		return structCustomTagSpecs.stream().filter(c1 -> c1.getCustomTag().getType().equals(type)).findFirst().orElse(null);
	}
	
	public Color getStructureTypeColor(String type) {
		StructCustomTagSpec c = getStructCustomTagSpec(type);
		if (c!=null && c.getRGB()!=null) {
			return Colors.createColor(c.getRGB());
		}
		else {
			return Colors.createColor(StructCustomTagSpec.DEFAULT_COLOR);
		}
	}
	// END OF CUSTOM TAG SPECS STUFF
	
	// virtual keys shortcuts:
	public Pair<Integer, String> getVirtualKeyShortCutValue(String key) {
		return virtualKeysShortCuts.get(key);
	}
	
	public String getVirtualKeyShortCutKey(Pair<Integer, String> vk) {
		for (String key : virtualKeysShortCuts.keySet()) {
			if (virtualKeysShortCuts.get(key).equals(vk)) {
				return key;
			}
		}
		return null;
	}
	
	public Pair<Integer, String> removeVirtualKeyShortCut(String key) {
		return virtualKeysShortCuts.remove(key);
	}
	
	public Pair<Integer, String> setVirtualKeyShortCut(String newKey, Pair<Integer, String> vk) {
		Iterator<String> it = virtualKeysShortCuts.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			Pair<Integer, String> value = getVirtualKeyShortCutValue(key);
			if (value.equals(vk)) {
				logger.debug("removing old shorcut, key = "+key+", value = "+value);
				virtualKeysShortCuts.remove(key);
			}
		}
		
		return virtualKeysShortCuts.put(newKey, vk);
	}
	
	public boolean isValidVirtualKeyShortCutKey(String key) {
		return key.equals("0") || key.equals("1") || key.equals("2") || key.equals("3") || key.equals("4") || key.equals("5") || 
				key.equals("6") || key.equals("7") || key.equals("8") || key.equals("9");
	}
	
	public void clearVirtualKeyShortCuts() {
		virtualKeysShortCuts.clear();
	}
	
	public Map<String, Pair<Integer, String>> getVirtualKeysShortCuts() {
		return virtualKeysShortCuts;
	}
	// END OF CUSTOM TAG SPECS STUFF
	
	// START OF COLLECTION-SPECIFIC TAG STUFF
	
	public List<CustomTagSpec> getCustomTagSpecsForCurrentCollection(){
		
		//TODO: "load custom tag defs from DB for loaded collection"
		logger.debug("load custom tag defs from DB for this collection");
		return collectionSpecificTagSpecs;
		
//		//List<CustomTagSpec> tagSpecs = 
//		try {
//			return CustomTagSpecUtil.readCustomTagSpecsFromJsonString(conn.getTagDefsCollection(collId));
//		} catch (SessionExpiredException | ServerErrorException | ClientErrorException | IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;

//		currList.add(getCustomTagSpecs().get(0));
//		
//		CustomTag tag;
//		try {
//			tag = CustomTagFactory.create("person");
//			CustomTagSpec tagDef = new CustomTagSpec(tag);
//			
//			currList.add(tagDef);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return currList;
		
//		if (!storage.isLoggedIn()) {
//			logger.debug("updating custom tag defs for local mode, customTagDefs: "+customTagDefs);
//			CustomTagDefUtil.writeCustomTagDefsToSettings(customTagDefs);
//		} else {
//			// TODO: write to server for current collection if logged in!
//		}
	}
	
	public void updateCustomTagSpecsForCurrentCollectionInDB(){
		
		// TODO: write to DB on server for current collection if logged in!
		
		/*
		 * add current colors to store in the web interface
		 */
		for (CustomTagSpec ct : collectionSpecificTagSpecs){
			String color = CustomTagFactory.getTagColor(ct.getCustomTag().getTagName());
			ct.setColor(color);
		}

		JsonArray tagSpecString = CustomTagSpecDBUtil.getCollectionTagSpecsAsJsonString(collectionSpecificTagSpecs);
		try {
			checkConnection(true);
			conn.updateTagDefsCollection(collId, tagSpecString.toString());
		} catch (SessionExpiredException | ClientErrorException | IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		if (!storage.isLoggedIn()) {
//			logger.debug("updating custom tag defs for local mode, customTagDefs: "+customTagDefs);
//			CustomTagDefUtil.writeCustomTagDefsToSettings(customTagDefs);
//		} else {
//			// TODO: write to server for current collection if logged in!
//		}
	}
	
	public void updateCustomTagSpecsForUserInDB(){
		
		/*
		 * first of all get the object registry (contains all custom tags )
		 * get tag definitions which are not predefined
		 * get them with tagname from object registry
		 * add them to a customtagspec list and import in DB
		 */
		// init predifined tags:
		//String tagNamesProp = TrpConfig.getTrpSettings().getTagNames();
		List<CustomTagSpec> userTagSpecs = new ArrayList<CustomTagSpec>();

		//List<CustomTag> cts = CustomTagFactory.getCustomTagListFromProperties(tagNamesProp);
		Collection<CustomTag> cts = CustomTagFactory.getRegisteredCustomTags();
		for (CustomTag ct : cts){
			String color = CustomTagFactory.getTagColor(ct.getTagName());
			CustomTagSpec spec = new CustomTagSpec(ct);
			spec.setColor(color);
			userTagSpecs.add(spec);
		}

		JsonArray tagSpecString = CustomTagSpecDBUtil.getCollectionTagSpecsAsJsonString(userTagSpecs);
		logger.debug("user defined tags " + tagSpecString.toString());
		try {
			checkConnection(true);
			String s = (String) tagSpecString.toString();
			conn.updateTagDefsUser(s);
		} catch (SessionExpiredException | ClientErrorException | IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	// END OF COLLECTION-SPECIFIC TAG STUFF
	public void saveTagDefinitions() {
		String tagNamesProp = CustomTagFactory.createTagDefPropertyForConfigFile();
		logger.debug("storing tag defs, tagNamesProp: "+tagNamesProp);
		TrpConfig.getTrpSettings().setTagNames(tagNamesProp);
	}

	public void setCurrentTranscriptEdited(boolean edited) {
		if (transcript==null || transcript.getPage()==null) {
			return;
		}
		
		transcript.getPage().setEdited(edited);
	}

	public JobImpl[] getHtrTrainingJobImpls() throws SessionExpiredException, ServerErrorException, ClientErrorException {
		final Predicate<JobImpl> htrTrainingJobImplFilter = j -> j.getTask().equals(JobTask.HtrTraining);
		List<JobImpl> list = getConnection().getJobImplAcl(htrTrainingJobImplFilter);
		return list.toArray(new JobImpl[list.size()]);
	}
	
	public void reloadP2PaLAModels() {
		if (isLoggedInAtTestServer()) {
			try {
				List<TrpP2PaLAModel> models = conn.getP2PaLAModels(-1);
				if (CoreUtils.size(models)>0) {
					p2palaModels = models;
				}
			} catch (SessionExpiredException | ServerErrorException | ClientErrorException e) {
				logger.error("Error loading P2PaLA models: "+e.getMessage(), e);
			}
			
		}
	}
	
	public void clearP2PaLAModels() {
		p2palaModels = new ArrayList<>();
	}
	
	public List<TrpP2PaLAModel> getP2PaLAModels() {
		return p2palaModels;
	}

	
}
