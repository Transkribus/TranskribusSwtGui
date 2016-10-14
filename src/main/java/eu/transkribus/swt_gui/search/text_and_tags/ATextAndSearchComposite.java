package eu.transkribus.swt_gui.search.text_and_tags;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.ServerErrorException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.search.SearchFacets;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.swt.progress.ProgressBarDialog;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.page_metadata.CustomTagSearcher;
import eu.transkribus.swt_gui.transcription.ATranscriptionWidget.Type;

public abstract class ATextAndSearchComposite extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(ATextAndSearchComposite.class);
	
	static final String SCOPE_DOC = "Current document";
	static final String SCOPE_PAGE = "Current page";
	static final String SCOPE_REGION = "Current region";
	static final String SCOPE_COLL = "Current collection";
	
	String[] SCOPES = new String[] { SCOPE_COLL, SCOPE_DOC, SCOPE_PAGE, SCOPE_REGION };
	
	public static class SearchResult {		
		public int collId;
		public List<CustomTag> foundTags;
		
		public SearchResult() {
			clear();
		}
		
		public int size() { return foundTags.size(); }
		public CustomTag get(int index) { return foundTags.get(index); }
		
		public void clear() {
			collId = -1;
			foundTags = new ArrayList<>();
		}
	}
	
	protected SearchResult searchResult = new SearchResult();
		
//	protected List<CustomTag> foundTags = new ArrayList<>();
	
	public ATextAndSearchComposite(Composite parent, int style) {
		super(parent, style);
	}

	public abstract String getScope();
	public abstract void updateResults();
	
	public abstract SearchFacets getFacets() throws IOException;
	
	protected void findNextTagOnCurrentDocument(final boolean previous) {
		
		final SearchFacets facets;
		try {
			facets = getFacets();
		} catch (IOException e1) {
			DialogUtil.showErrorMessageBox(getShell(), "Error", e1.getMessage());
			return;
		}
		
		logger.debug("searching for next tag, previous = "+previous);
		
		final Storage s = Storage.getInstance();
		try {
			if (!s.isDocLoaded()) {
				DialogUtil.showErrorMessageBox(getShell(), "Error", "No document loaded!");
				return;
			}
			
			final SearchResult sr = new SearchResult();
			sr.collId = s.getCurrentDocumentCollectionId();
			
//			final List<CustomTag> tag = new ArrayList<>();
			// TODO: specify real current position here, and display a wait dialog
			final int startPageIndex = s.getPageIndex();
			final int startRegionIndex = s.getCurrentRegion()==-1 ? 0 : s.getCurrentRegion();
			final int startLineIndex = s.getCurrentLineObject()==null ? 0 : s.getCurrentLineObject().getIndex();
			int currentOffsetTmp = 0;
			TrpMainWidget mw = TrpMainWidget.getInstance();
			if (mw.getUi().getSelectedTranscriptionType() == Type.LINE_BASED) {
				int o = mw.getUi().getSelectedTranscriptionWidget().getText().getCaretOffset();
				int lo = mw.getUi().getSelectedTranscriptionWidget().getText().getOffsetAtLine(startLineIndex);
				logger.debug("o = "+o+" lo = "+lo);
				currentOffsetTmp = o-lo;
			}
			final int currentOffset = currentOffsetTmp;
			logger.info("searching for next tag, startPageIndex= "+startPageIndex+", startRegionIndex= "+startRegionIndex+", startLindex= "+startLineIndex+" currentOffset= "+currentOffset+", previous= "+previous);			
			
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					CustomTagSearcher.searchOnDoc_WithoutIndex(sr, s.getCurrentDocumentCollectionId(), s.getDoc(), facets, startPageIndex, startRegionIndex, startLineIndex, true, currentOffset, previous, monitor, false);
				}
			}, "Searching", true);		
			
			if (!sr.foundTags.isEmpty()) {
				logger.info("found a tag - displaying: "+sr.foundTags.get(0));
				TrpLocation l = new TrpLocation(sr.foundTags.get(0));
				TrpMainWidget.getInstance().showLocation(l);
			} else {
				DialogUtil.showInfoMessageBox(getShell(), "No match", "No match found!");
			}
		} catch (Throwable e) {
			DialogUtil.showErrorMessageBox(getShell(), "Error in tag search", e.getMessage());
			logger.error(e.getMessage(), e);
			return;
		}
	}
		
	protected void findTags() {
		final SearchFacets facets;
		try {
			facets = getFacets();
		} catch (IOException e1) {
			DialogUtil.showErrorMessageBox(getShell(), "Error", e1.getMessage());
			return;
		}
				
		logger.debug("searching for "+facets.getType());
		logger.debug("facets = "+facets);

		final Storage s = Storage.getInstance();
		final TrpMainWidget mw = TrpMainWidget.getInstance();
				
		String scope = getScope();
		logger.debug("searching on scope: "+scope);
		
		searchResult.clear();
				
		updateResults();

		ProgressBarDialog pd = new ProgressBarDialog(getShell()) {
			@Override public void subTask(final String name) {
				super.subTask(name);
				Display.getDefault().syncExec(new Runnable() {
					@Override public void run() {
						Shell s = ATextAndSearchComposite.this.getShell();
						if (!s.isDisposed()) {
							updateResults();
						}
					}
				});
			}
		};
		
		
		try {
			if (scope.equals(SCOPE_COLL)) {
				final TrpCollection currCol =  mw.getUi().getServerWidget().getSelectedCollection();
				final int currentCollID = currCol == null ? -1 : currCol.getColId();

				if (currCol == null) {
					DialogUtil.showErrorMessageBox(getShell(), "Error", "No collection selected!");
					return;
				}
				
				searchResult.collId = currentCollID;
				
				pd.open(new IRunnableWithProgress() {
					@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							CustomTagSearcher.searchOnCollection_WithoutIndex(currentCollID, searchResult, facets, monitor);
							
						} catch (SessionExpiredException | IllegalArgumentException | NoConnectionException e) {
							throw new InvocationTargetException(e);
						}
					}
				}, "Searching in collection "+currCol.getColName(), true);
			}
			else if (scope.equals(SCOPE_DOC)) {
				if (!s.isDocLoaded()) {
					DialogUtil.showErrorMessageBox(getShell(), "Error", "No document loaded!");
					return;
				}
				String docTitle = s.getDoc().getMd() != null ? s.getDoc().getMd().getTitle() : "NA";
				searchResult.collId = s.getCurrentDocumentCollectionId();
				
				pd.open(new IRunnableWithProgress() {
					@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						CustomTagSearcher.searchOnDoc_WithoutIndex(searchResult, s.getCurrentDocumentCollectionId(), s.getDoc(), facets, 0, 0, 0, false, 0, false, monitor, false);
					}
				}, "Searching in document "+docTitle, true);
			}
			else if (scope.equals(SCOPE_PAGE)) {
	//			if (s.getTranscript()==null || s.getTranscript().getPage() == null) {
				if (!s.isPageLoaded() || s.getTranscript().getPageData() == null) {
					DialogUtil.showErrorMessageBox(getShell(), "Error", "No page loaded!");
					return;
				}
				TrpPageType p = s.getTranscript().getPage();
				searchResult.collId = s.getCurrentDocumentCollectionId();
				
				CustomTagSearcher.searchOnPage(searchResult, s.getCurrentDocumentCollectionId(), p, facets, 0, 0, false, 0, false);
			} else if (scope.equals(SCOPE_REGION)) {
				TrpTextRegionType r = s.getCurrentRegionObject();
				if (r==null) {
					DialogUtil.showErrorMessageBox(getShell(), "Error", "No region selected!");
					return;
				}
				searchResult.collId = s.getCurrentDocumentCollectionId();
					
				CustomTagSearcher.searchOnRegion(searchResult, s.getCurrentDocumentCollectionId(), r, facets, 0, false, 0, false);
			}
		}
		catch (Throwable e) {
			mw.onError("Error in tag search", e.getMessage(), e);
			return;
		}
		
		logger.debug("setting item count to "+searchResult.foundTags.size());
		
		updateResults();
	}
	
	protected static void saveAffectedPages(IProgressMonitor monitor, int collId, Collection<TrpPageType> affectedPages)
			throws SessionExpiredException, ServerErrorException, IllegalArgumentException, Exception {
		if (monitor != null)
			monitor.beginTask("Saving affected transcripts", affectedPages.size());

		Storage s = Storage.getInstance();
		int c = 0;
		for (TrpPageType pt : affectedPages) {
			if (monitor != null && monitor.isCanceled())
				return;

			s.saveTranscript(s.getCurrentDocumentCollectionId(), pt, null, pt.getMd().getTsId(), "Tagged from text");

			if (monitor != null)
				monitor.worked(c++);

			++c;
		}
	}

}
