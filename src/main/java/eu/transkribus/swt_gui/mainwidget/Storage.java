package eu.transkribus.swt_gui.mainwidget;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.security.auth.login.LoginException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.dea.fimgstoreclient.FimgStoreGetClient;
import org.dea.fimgstoreclient.beans.FimgStoreImgMd;
import org.dea.fimgstoreclient.beans.FimgStoreTxt;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.DocumentException;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.exceptions.NullValueException;
import eu.transkribus.core.exceptions.OAuthTokenRevokedException;
import eu.transkribus.core.io.DocExporter;
import eu.transkribus.core.io.LocalDocReader;
import eu.transkribus.core.io.LocalDocWriter;
import eu.transkribus.core.io.UnsupportedFormatException;
import eu.transkribus.core.model.beans.EdFeature;
import eu.transkribus.core.model.beans.EdOption;
import eu.transkribus.core.model.beans.HtrModel;
import eu.transkribus.core.model.beans.JAXBPageTranscript;
import eu.transkribus.core.model.beans.PageLock;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpDocDir;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpEvent;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.TrpWordgraph;
import eu.transkribus.core.model.beans.auth.TrpRole;
import eu.transkribus.core.model.beans.auth.TrpUserLogin;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.model.beans.enums.OAuthProvider;
import eu.transkribus.core.model.beans.enums.SearchType;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.model.beans.pagecontent.PcGtsType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageTypeUtils;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.core.model.beans.searchresult.FulltextSearchResult;
import eu.transkribus.core.model.builder.alto.AltoExporter;
import eu.transkribus.core.model.builder.pdf.PdfExporter;
import eu.transkribus.core.model.builder.tei.ATeiBuilder;
import eu.transkribus.core.model.builder.tei.TeiExportPars;
import eu.transkribus.core.model.builder.tei.TrpTeiStringBuilder;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.Event;
import eu.transkribus.core.util.HtrUtils;
import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.TrpGuiPrefs;
import eu.transkribus.swt_gui.canvas.CanvasImage;
import eu.transkribus.swt_gui.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.mainwidget.listener.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.listener.IStorageListener.CollectionsLoadEvent;
import eu.transkribus.swt_gui.mainwidget.listener.IStorageListener.DocListLoadEvent;
import eu.transkribus.swt_gui.mainwidget.listener.IStorageListener.DocLoadEvent;
import eu.transkribus.swt_gui.mainwidget.listener.IStorageListener.DocMetadataUpdateEvent;
import eu.transkribus.swt_gui.mainwidget.listener.IStorageListener.JobUpdateEvent;
import eu.transkribus.swt_gui.mainwidget.listener.IStorageListener.LoginOrLogoutEvent;
import eu.transkribus.swt_gui.mainwidget.listener.IStorageListener.MainImageLoadEvent;
import eu.transkribus.swt_gui.mainwidget.listener.IStorageListener.PageLoadEvent;
import eu.transkribus.swt_gui.mainwidget.listener.IStorageListener.TranscriptListLoadEvent;
import eu.transkribus.swt_gui.mainwidget.listener.IStorageListener.TranscriptLoadEvent;
import eu.transkribus.swt_gui.mainwidget.listener.IStorageListener.TranscriptSaveEvent;
import eu.transkribus.util.DataCache;
import eu.transkribus.util.DataCacheFactory;
import eu.transkribus.util.MathUtil;
import eu.transkribus.util.Utils;

/** Singleton class that contains all data related to loading a transcription */
public class Storage {
	private final static Logger logger = LoggerFactory.getLogger(Storage.class);	

	final int N_IMAGES_TO_PRELOAD_PREVIOUS = 1;
	final int N_IMAGES_TO_PRELOAD_NEXT = 1;

	// public final static String LOGIN_OR_LOGOUT_EVENT =
	// "LOGIN_OR_LOGOUT_EVENT";

	private static Storage storage = null;

	// private int currentTranscriptIndex = 0;

	private List<TrpDocMetadata> docList = Collections.synchronizedList(new ArrayList<>());
	private List<TrpDocMetadata> userDocList = Collections.synchronizedList(new ArrayList<>());
	
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

	private static DocJobUpdater docUpdater;
	private DataCache<URL, CanvasImage> imCache;
	
	public static final boolean USE_TRANSCRIPT_CACHE = false;
	private DataCache<TrpTranscriptMetadata, JAXBPageTranscript> transcriptCache;
	
	FimgStoreImgMd imgMd;
	
//	private int currentColId = -1;
	
	Set<IStorageListener> listener = new HashSet<>();

	private Storage() {
		initImCache();
		initTranscriptCache();
		addInternalListener();
	}
	
	private void addInternalListener() {
		addListener(new IStorageListener() {
			@Override public void handleLoginOrLogout(LoginOrLogoutEvent arg) {
				reloadUserDocs();
			}
		});
	}
	
	public TrpPageType getOrBuildPage(TrpTranscriptMetadata md, boolean keepAlways) throws Exception {
		if (USE_TRANSCRIPT_CACHE) {
			return transcriptCache.getOrPut(md, keepAlways, null).getPage();
		} else {
			JAXBPageTranscript tr = new JAXBPageTranscript(md);
			tr.build();
			return tr.getPage();
//			return TrpPageTranscriptBuilder.build(md);
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

	private static void initDocUpdater() {
		docUpdater = new DocJobUpdater() {
			@Override public void onUpdate(final TrpJobStatus job) {
				// Display.getDefault().asyncExec(new Runnable() {
				// @Override public void run() {
				storage.sendEvent(new JobUpdateEvent(this, job));
				// }
				// });
			}
		};
	}

	@Override public void finalize() {
		logger.debug("Storage finalize - stopping job update thread!");
		docUpdater.stopJobThread();
	}

	public static Storage getInstance() {
		if (storage == null) {
			storage = new Storage();
			initDocUpdater();
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
		
		return conn.getJobs(filterByUser, TrpJobStatus.UNFINISHED, null, 0, 0, null, null);
		
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

	private void sendEvent(final Event event) {
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
	
//	public int getCollectionId(int collectionIndex) /*throws NoConnectionException*/ {		
//		if (user == null)
//			return -1;
//		
//		if (collectionIndex>=0 && collectionIndex<collections.size())
//			return collections.get(collectionIndex).getColId();
//		else
//			return -1;
//	}
	
	public int getCurrentDocumentCollectionId() {
		return isRemoteDoc() ? doc.getCollection().getColId() : 0;
		// TODO: check if collection is null (should never happen with the new server though...)
//		if (isRemoteDoc()) {
//			if (doc.getCollection() != null)
//				return doc.getCollection().getColId();
//			else
//				return TrpMainWidget.getInstance().getUi().getDocOverviewWidget().getSelectedCollection().getColId();
//		}
//		else
//			return 0;
	}
		
	public void reloadUserDocs() {
		logger.debug("reloading docs by user!");
		
		if (user != null) {
			conn.getAllDocsByUserAsync(0, 0, null, null, new InvocationCallback<List<TrpDocMetadata>>() {
				@Override public void failed(Throwable throwable) {
					logger.error("Error loading documents by user "+user+" - "+throwable.getMessage(), throwable);
				}
				
				@Override public void completed(List<TrpDocMetadata> response) {
					logger.debug("loaded docs by user "+user+" - "+response.size()+" thread: "+Thread.currentThread().getName());
					synchronized (this) {
						userDocList.clear();
						userDocList.addAll(response);
						
						sendEvent(new DocListLoadEvent(this, userDocList, true));
					}
				}
			});
		} else {
			synchronized (this) {
				userDocList.clear();				
				sendEvent(new DocListLoadEvent(this, userDocList, true));
			}
		}
	}
	
	public List<TrpDocMetadata> getUserDocList() {
		return userDocList;
	}

	public List<TrpDocMetadata> reloadDocList(int colId) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, NoConnectionException {
		checkConnection(true);
		if (colId == -1)
			return docList;

		logger.debug("reloading doclist for collection: "+colId);

		if (true) {
			synchronized (this) {
				docList.clear();
				docList.addAll(conn.getAllDocs(colId));
			}
		} else {
			docList = conn.getAllDocs(colId);
		}
		
		this.collId = colId;
		
		logger.debug("loaded "+docList.size()+" nr of docs of collection "+collId);
		
//		this.currentColId = colId;
		
		sendEvent(new DocListLoadEvent(this, docList, false));
		
		return docList;
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

	public void login(String serverUri, String username, String password) throws LoginException {
		logger.debug("Logging in as user: " + username);
		if (conn != null)
			conn.close();

		conn = new TrpServerConn(serverUri);
		user = conn.login(username, password);
		logger.debug("Logged in as user: " + user + " connection: " + conn);

		sendEvent(new LoginOrLogoutEvent(this, true, user, conn.getServerUri()));
	}
	
	public void loginOAuth(final String serverUri, final String code, final String state, final String grantType, final String redirectUri, final OAuthProvider prov) throws LoginException, OAuthTokenRevokedException {
		logger.debug("Logging in via OAuth at: " + prov.toString());
		if (conn != null)
			conn.close();

		conn = new TrpServerConn(serverUri);

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
		sendEvent(new LoginOrLogoutEvent(this, true, user, conn.getServerUri()));
	}
	
	public void logout() {
		try {
			if (conn != null)
				conn.close();
		} catch (Throwable th) {
			logger.error("Error logging out: " + th.getMessage(), th);
		} finally {
			clearCollections();
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
	
	public void startOrResumeJobThread() {
		docUpdater.startOrResumeJobThread();
	}

	public void cancelJob(String jobId) throws SessionExpiredException, ServerErrorException, IllegalArgumentException {
		if (conn != null && jobId != null) {
			conn.killJob(jobId);
		}
	}

	public TrpJobStatus loadJob(String jobId) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, NoConnectionException {
		// FIXME: direct access to job table not "clean" here...
		List<TrpJobStatus> jobs = (List<TrpJobStatus>) TrpMainWidget.getInstance().getUi().getJobOverviewWidget().getTableViewer().getInput();
		if (jobs == null) // should not happen!
			return null;
		
		synchronized (jobs) {
			checkConnection(true);
			TrpJobStatus job = conn.getJob(jobId);
			// update job in jobs array if there
			for (int i = 0; i < jobs.size(); ++i) {
				if (jobs.get(i).getJobId().equals(job.getJobId())) {
					//logger.debug("UPDATING JOB: "+job.getJobId()+" new status: "+job.getState());
					jobs.get(i).copy(job); // do not set new instance, s.t. table-viewer does not get confused!
					
					return jobs.get(i);
					
//					jobs.set(i, job);
//					break;
				}
			}
//			return null; // orig
			return job; // return "original" job from connection here if not found in table (can be possible since introduction of paginated widgets!!)
		}
	}

	public void reloadCurrentDocument(int colId) throws SessionExpiredException, IllegalArgumentException, NoConnectionException, UnsupportedFormatException,
			IOException, NullValueException {

		// public void loadLocalDoc(String folder) throws Exception {
		// public void loadRemoteDoc(int docId) throws Exception {

		if (doc != null) {
			if (isLocalDoc()) {
				loadLocalDoc(doc.getMd().getLocalFolder().getAbsolutePath());
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
			logger.debug("this is a remove image file - adding fileType parameter for fileType="+fileType);
			urlStr = UriBuilder.fromUri(urlStr).replaceQueryParam("fileType", fileType).toString();
		}
					
		logger.debug("Loading image from url: " + urlStr);
		final boolean FORCE_RELOAD = false;
		currentImg = imCache.getOrPut(new URL(urlStr), true, fileType, FORCE_RELOAD);
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
		
		transcript.getPage().setEdited(false);
		if (!isLocalDoc()) {
			// FIXME:
			// PcGtsType pc = conn.getTranscript(doc.getId(), trMd.getPageNr());
			// isPageLocked = conn.isPageLocked(doc.getId(), trMd.getPageNr());

			// transcript = new JAXBPageTranscript(trMd, pc);
		}
		// TEST:
		// isPageLocked = true;

		sendEvent(new TranscriptLoadEvent(this, doc, page, transcript));
		logger.debug("loaded JAXB, regions: " + getNTextRegions());
	}
	
	public void analyzeStructure(int colId, int docId, int pageNr, boolean detectPageNumbers, boolean detectRunningTitles, boolean detectFootnotes) throws NoConnectionException, SessionExpiredException, ServerErrorException, IllegalArgumentException, JAXBException {
		checkConnection(true);
		
		PcGtsType pcGts = conn.analyzePageStructure(colId, docId, pageNr, detectPageNumbers, detectRunningTitles, detectFootnotes);
		
		transcript.setPageData(pcGts);
	}

	public void loadLocalDoc(String folder) throws UnsupportedFormatException, IOException, NullValueException {
		tryReleaseLocks();
		
		doc = LocalDocReader.load(folder);
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

		logger.info("loaded remote document, docId = " + doc.getId() + ", title = " + doc.getMd().getTitle() + ", nPages = " + doc.getPages().size());
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
		
		if (status == null)
			status = EditStatus.IN_PROGRESS;
			
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
	
	public void syncDocPages(List<TrpPage> pages, List<Boolean> checked, IProgressMonitor monitor) throws IOException, SessionExpiredException, ServerErrorException, IllegalArgumentException, NoConnectionException, NullValueException, JAXBException {
//		if (!localDoc.isLocalDoc())
//			throw new IOException("No local document given!");
		
		checkConnection(true);
		
		if (checked != null && pages.size() != checked.size()) {
			throw new IOException("Nr of checked list is unequal to nr of pages: "+checked.size()+"/"+pages.size());
		}
		if (!isRemoteDoc())
			throw new IOException("No remote document loaded!");
		
		if (pages.size() > doc.getPages().size()) {
			pages = pages.subList(0, doc.getPages().size());
			if (checked != null)
				checked = checked.subList(0, doc.getPages().size());
		}
		int nToSync = checked == null ? pages.size() : Utils.countTrue(checked);
		
//		int N = Math.min(doc.getPages().size(), nToSync);
		
		if (monitor != null)
			monitor.beginTask("Syncing doc pages with local pages", nToSync);

		int worked=0;
		for (int i=0; i<pages.size(); ++i) {
			if (checked == null || checked.get(i)) {
				TrpTranscriptMetadata tmd = pages.get(i).getCurrentTranscript();
				
				logger.debug("syncing page "+(worked+1));
				
				if (monitor != null)
					monitor.subTask("Syncing page "+(worked+1)+" / "+nToSync);
				
				if (monitor != null && monitor.isCanceled())
					return;
				
				conn.updateTranscript(getCurrentDocumentCollectionId(), doc.getMd().getDocId(), 
						(i+1), EditStatus.IN_PROGRESS,
						tmd.unmarshallTranscript(), tmd.getTsId(), null);
				
				if (monitor != null)
					monitor.worked(++worked);
			}
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

	public void uploadDocument(int colId, String folder, String title, IProgressMonitor monitor) throws IOException, Exception {
		if (!isLoggedIn())
			throw new Exception("Not logged in!");

		TrpDoc doc = LocalDocReader.load(folder);
		if (title != null && !title.isEmpty())
			doc.getMd().setTitle(title);

		conn.postTrpDoc(colId, doc, monitor);
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
	public void uploadDocumentFromPdf(int colId, String file, String dirName, IProgressMonitor monitor) 
			throws IOException, Exception {
		if (!isLoggedIn())
			throw new Exception("Not logged in!");

		// extract images from pdf and load images into Trp document
		TrpDoc doc = LocalDocReader.loadPdf(file, dirName);
		logger.debug("Extracted and loaded pdf " + file);
		
		if (file != null && !file.isEmpty())
			doc.getMd().setTitle(file);

		conn.postTrpDoc(colId, doc, monitor);
	}

	public void uploadDocumentFromPrivateFtp(int cId, String dirName, boolean checkForDuplicateTitle) throws Exception {
		if (!isLoggedIn())
			throw new Exception("Not logged in!");
		
		conn.ingestDocFromFtp(cId, dirName, checkForDuplicateTitle);
	}
	
	public void uploadDocumentFromMetsUrl(int cId, String metsUrl) throws SessionExpiredException, ServerErrorException, ClientErrorException, NoConnectionException{
//		if (!isLoggedIn())
//			throw new Exception("Not logged in!");
		checkConnection(true);
		conn.ingestDocFromUrl(cId, metsUrl);
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
	
	public String analyzeLayout(int colId, int docId, String pageStr, boolean doBlockSeg, boolean doLineSeg) throws SessionExpiredException, ServerErrorException, ClientErrorException, IllegalArgumentException, NoConnectionException {
		checkConnection(true);
		return conn.analyzeLayoutBatch(colId, docId, pageStr, doBlockSeg, doLineSeg);
	}
	
	public String runOcr(int colId, int docId) throws NoConnectionException, SessionExpiredException, ServerErrorException, IllegalArgumentException {
		checkConnection(true);
		return conn.runOcr(colId, docId);
	}
	
	public String runOcr(int colId, int docId, int pageNr) throws NoConnectionException, SessionExpiredException, ServerErrorException, IllegalArgumentException {
		checkConnection(true);
		return conn.runOcr(colId, docId, pageNr);
	}
	
	public String runOcr(int colId, int docId, String pageStr) throws NoConnectionException, SessionExpiredException, ServerErrorException, IllegalArgumentException {
		checkConnection(true);
		return conn.runOcr(colId, docId, pageStr);
	}
	
	public String runHtrOnPage(int colId, int docId, int pageNr, String model) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, NoConnectionException {
		checkConnection(true);
		return conn.runHtr(colId, docId, pageNr, model);
	}
	
	public String runHtr(int colId, int docId, String pageStr, String model) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, NoConnectionException {
		checkConnection(true);
		return conn.runHtr(colId, docId, pageStr, model);
	}

	public void deleteDocument(int colId, int docId) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, NoConnectionException {
		checkConnection(true);

		conn.deleteDoc(colId, docId);
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

	public String exportDocument(File dir, Set<Integer> pageIndices, boolean exportImg, boolean exportPage, boolean exportAlto, boolean splitIntoWordsInAlto, final String fileNamePattern, final IProgressMonitor monitor) throws SessionExpiredException, ServerErrorException, IllegalArgumentException,
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
		DocExporter de = new DocExporter();
		de.addObserver(o);
		de.writeRawDoc(doc, path, true, pageIndices, exportImg, exportPage, exportAlto, splitIntoWordsInAlto, fileNamePattern);

		return path;
	}

	public String exportPdf(File pdf, Set<Integer> pageIndices, final IProgressMonitor monitor, final boolean extraTextPages, final boolean imagesOnly, Set<String> selectedTags, final boolean highlightTags, final boolean wordBased, final boolean doBlackening, boolean createTitle) throws MalformedURLException, DocumentException,
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
		
		
		pdf = pdfExp.export(doc, pdf.getAbsolutePath(), pageIndices, extraTextPages, imagesOnly, selectedTags, highlightTags, wordBased, doBlackening, createTitle);

		return pdf.getAbsolutePath();
	}

	public String exportTei(File tei, TeiExportPars pars, IProgressMonitor monitor) throws IOException, Exception {
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
		ATeiBuilder builder = new TrpTeiStringBuilder(doc, pars, monitor);
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

		List<TrpCollection> collectionsFromServer = conn.getAllCollections(0, 0, null, null);
		
		collections.clear();
		collections.addAll(collectionsFromServer);

		sendEvent(new CollectionsLoadEvent(this, user, collections));
	}
	
	public void addCollection(String name) throws NoConnectionException, SessionExpiredException, ServerErrorException {
		checkConnection(true);
		
		conn.createCollection(name);
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
	
	public List<String> getHtrNets() throws NoConnectionException, SessionExpiredException, ServerErrorException, ClientErrorException, IllegalArgumentException{
		checkConnection(true);
		return conn.getHtrRnnListText();
	}
	
	public List<String> getHtrDicts() throws NoConnectionException, SessionExpiredException, ServerErrorException, ClientErrorException, IllegalArgumentException{
		checkConnection(true);
		return conn.getHtrDictListText();
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
	
	public void storeEdOption(EdOption opt) throws NoConnectionException, SessionExpiredException, ServerErrorException, IllegalArgumentException {
		checkConnection(true);
		conn.postEditDeclOption(getCurrentDocumentCollectionId(), opt);
	}

	public void deleteEdOption(EdOption opt) throws SessionExpiredException, ServerErrorException, IllegalArgumentException, NoConnectionException {
		checkConnection(true);
		conn.deleteEditDeclOption(getCurrentDocumentCollectionId(), opt);
	}

	public TrpRole getRoleOfUserInCurrentCollection() {
		if(!isLoggedIn()){
			return TrpRole.Admin;
		}
		TrpUserLogin userLogin = conn.getUserLogin();
		if(userLogin.isAdmin()){
			return TrpRole.Admin;
		}
		final int currentCol = getCurrentDocumentCollectionId();
		TrpRole role = null;
		for(TrpCollection c : collections){
			if(c.getColId() == currentCol){
				role = c.getRole();
				break;
			}
		}
		if(role == null){
			role = TrpRole.None;
		}
		return role;
	}

	public void deleteTranscript(TrpTranscriptMetadata tMd) throws NoConnectionException, SessionExpiredException, ServerErrorException, IllegalArgumentException {
		checkConnection(true);
		conn.deleteTranscript(this.getCurrentDocumentCollectionId(), this.getDoc().getId(), 
				this.getPage().getPageNr(), tMd.getKey());
	}

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
	
	public FimgStoreImgMd getCurrentImageMetadata() {
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
			imgMd = (FimgStoreImgMd)getter.getFileMd(page.getKey());
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
		final boolean isProxyEnabled = TrpConfig.getTrpSettings().isProxyEnabled();
		if(isProxyEnabled){
			logger.debug("PROXY IS ENABLED");
			final String proxyHost = TrpConfig.getTrpSettings().getProxyHost();
			final String proxyPort = TrpConfig.getTrpSettings().getProxyPort();
			final String proxyUser = TrpConfig.getTrpSettings().getProxyUser();
			final String proxyPassword = TrpConfig.getTrpSettings().getProxyPassword();
			System.setProperty("https.proxyHost", proxyHost);
			System.setProperty("https.proxyPort", proxyPort);
			System.setProperty("https.proxyUser", proxyUser);
			System.setProperty("https.proxyPassword", proxyPassword);
			System.setProperty("https.nonProxyHosts", "localhost|127.0.0.1");
			System.setProperty("http.proxyHost", proxyHost);
			System.setProperty("http.proxyPort", proxyPort);
			System.setProperty("http.proxyUser", proxyUser);
			System.setProperty("http.proxyPassword", proxyPassword);
			System.setProperty("http.nonProxyHosts", "localhost|127.0.0.1");

		} else {
			logger.debug("PROXY IS DISABLED");
			System.setProperty("https.proxyHost", "");
			System.setProperty("https.proxyPort", "");
			System.setProperty("https.proxyUser", "");
			System.setProperty("https.proxyPassword", "");
			System.setProperty("http.proxyHost", "");
			System.setProperty("http.proxyPort", "");
			System.setProperty("http.proxyUser", "");
			System.setProperty("http.proxyPassword", "");
		}
		logger.debug("ProxyHost = " + System.getProperty("https.proxyHost"));
		logger.debug("ProxyPort = " + System.getProperty("https.proxyPort"));
		logger.debug("ProxyUser = " + System.getProperty("https.proxyUser"));
		logger.debug("ProxyPassword = " + System.getProperty("https.proxyPassword"));
		logger.debug("ProxyHost = " + System.getProperty("http.proxyHost"));
		logger.debug("ProxyPort = " + System.getProperty("http.proxyPort"));
		logger.debug("ProxyUser = " + System.getProperty("http.proxyUser"));
		logger.debug("ProxyPassword = " + System.getProperty("http.proxyPassword"));
	}
}
