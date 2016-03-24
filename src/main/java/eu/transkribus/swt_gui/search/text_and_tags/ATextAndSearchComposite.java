package eu.transkribus.swt_gui.search.text_and_tags;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.search.SearchFacets;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.swt_canvas.progress.ProgressBarDialog;
import eu.transkribus.swt_canvas.util.DialogUtil;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.page_metadata.CustomTagSearcher;
import eu.transkribus.swt_gui.transcription.ATranscriptionWidget.Type;

public abstract class ATextAndSearchComposite extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(ATextAndSearchComposite.class);
	
	static final String SCOPE_DOC = "Current document";
	static final String SCOPE_PAGE = "Current page";
	static final String SCOPE_REGION = "Current region";
	static final String SCOPE_COLL = "Current collection";
	
	String[] SCOPES = new String[] { SCOPE_COLL, SCOPE_DOC, SCOPE_PAGE, SCOPE_REGION };
		
	protected List<CustomTag> foundTags = new ArrayList<>();
	
	public ATextAndSearchComposite(Composite parent, int style) {
		super(parent, style);
	}

	public abstract String getScope();
	public abstract void updateResults();
	
	public abstract SearchFacets getFacets() throws IOException;
	
	protected void findNextTag(final boolean previous) {
		
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
			
			final List<CustomTag> tag = new ArrayList<>();
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
					CustomTagSearcher.searchOnDoc_WithoutIndex(tag, s.getDoc(), facets, startPageIndex, startRegionIndex, startLineIndex, true, currentOffset, previous, monitor, false);
				}
			}, "Searching", true);		
			
			if (!tag.isEmpty()) {
				logger.info("found a tag - displaying: "+tag.get(0));
				TrpLocation l = new TrpLocation(tag.get(0));
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
		final int currentCollID = mw.getUi().getDocOverviewWidget().getSelectedCollectionId();
		
		String scope = getScope();
		logger.debug("searching on scope: "+scope+ " currentCollID = "+currentCollID);
		
		foundTags.clear();
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
				pd.open(new IRunnableWithProgress() {
					@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							CustomTagSearcher.searchOnCollection_WithoutIndex(currentCollID, foundTags, facets, monitor);
							
						} catch (SessionExpiredException | IllegalArgumentException | NoConnectionException e) {
							throw new InvocationTargetException(e);
						}
					}
				}, "Searching", true);
			}
			else if (scope.equals(SCOPE_DOC)) {
				if (!s.isDocLoaded()) {
					DialogUtil.showErrorMessageBox(getShell(), "Error", "No document loaded!");
					return;
				}
				pd.open(new IRunnableWithProgress() {
					@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						CustomTagSearcher.searchOnDoc_WithoutIndex(foundTags, s.getDoc(), facets, 0, 0, 0, false, 0, false, monitor, false);
					}
				}, "Searching", true);
			}
			else if (scope.equals(SCOPE_PAGE)) {
	//			if (s.getTranscript()==null || s.getTranscript().getPage() == null) {
				if (!s.isPageLoaded() || s.getTranscript().getPageData() == null) {
					DialogUtil.showErrorMessageBox(getShell(), "Error", "No page loaded!");
					return;
				}
				TrpPageType p = s.getTranscript().getPage();
				
				CustomTagSearcher.searchOnPage(foundTags, p, facets, 0, 0, false, 0, false);
			} else if (scope.equals(SCOPE_REGION)) {
				TrpTextRegionType r = s.getCurrentRegionObject();
				if (r==null) {
					DialogUtil.showErrorMessageBox(getShell(), "Error", "No region selected!");
					return;
				}
					
				CustomTagSearcher.searchOnRegion(foundTags, r, facets, 0, false, 0, false);
			}
		}
		catch (Throwable e) {
			mw.onError("Error in tag search", e.getMessage(), e);
			return;
		}
		
		logger.debug("setting item count to "+foundTags.size());
		
		updateResults();
	}

}
