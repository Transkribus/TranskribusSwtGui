package eu.transkribus.swt_gui.mainwidget.listener;

import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.canvas.CanvasMode;
import eu.transkribus.swt_gui.canvas.SWTCanvas;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidgetView;

public class TrpMainWidgetStorageListener implements IStorageListener {
	private static final Logger logger = LoggerFactory.getLogger(TrpMainWidgetStorageListener.class);
	
	Storage storage = Storage.getInstance();
	TrpMainWidget mw;
	TrpMainWidgetView ui;
	SWTCanvas canvas;
	
	public TrpMainWidgetStorageListener(TrpMainWidget mainWidget) {
		this.mw = mainWidget;
		this.ui = mw.getUi();
		this.canvas = ui.getCanvas();
		
		storage.addListener(this);
	}
	
	@Override public void handleMainImageLoadEvent(MainImageLoadEvent mile) {
		if (storage.isPageLoaded() && storage.getCurrentImage() != null) {
			canvas.getScene().setMainImage(storage.getCurrentImage());
			canvas.redraw();
		}
	}
	
	@Override public void handleCollectionsLoadEvent(CollectionsLoadEvent cle) {
		
		
		// OLD stuff - update of available collections and reloading doc list now gets handled via CollectionComboViewer!
//		logger.debug("collections loaded!");
//		ui.getDocOverviewWidget().setAvailableCollections(cle.collections);
				
//		logger.debug("now also reloading docs for collection");
//		if (!cle.collections.isEmpty())
//			mw.reloadDocList(cle.collections.get(0));
	}
	
	@Override public void handleJobUpdate(JobUpdateEvent jue) {
		TrpJobStatus job = jue.job;
		
		boolean isThisDocOpen = true;
		if (job != null) { // specific job was updated
			logger.debug("specific job update: "+job);
//			ISelection sel = mw.getUi().getJobOverviewWidget().getJobTableViewer().getSelection();
//			mw.getUi().getJobOverviewWidget().getJobTableViewer().refresh(true);
//			mw.getUi().getJobOverviewWidget().getJobTableViewer().setSelection(sel);
			
			mw.getUi().getJobOverviewWidget().getTableViewer().update(job, null);
//			mw.getUi().getJobOverviewWidget().getJobTableViewer().update(null, null);

			isThisDocOpen = storage.isDocLoaded() && storage.getDoc().getId()==job.getDocId();	
			
			// reload current page if page job for this page is finished:
			// TODO: only ask question to reload page!!
			if (isThisDocOpen && job.isFinished()) {
				if (!job.isSuccess()) {
					logger.error("a job for the current page failed: "+job);
					DialogUtil.showErrorMessageBox(mw.getShell(), "A job for this page failed", job.getDescription());
				} 
				else if (storage.getPageIndex() == (job.getPageNr()-1) || job.getPageNr()==-1) {
					// reload page if doc and page is open:					
					if (DialogUtil.showYesNoDialog(mw.getShell(), "A job for this page finished", "A job for this page just finished - do you want to reload the current page?") == SWT.YES) {
						logger.debug("reloading page!");
						mw.reloadCurrentPage(true);						
					}
				}
			}
		} else {
//			logger.debug("got "+storage.getJobs().size()+" jobs, thread = "+Thread.currentThread().getName());
//			mw.getUi().getJobOverviewWidget().setInput(new ArrayList<>(storage.getJobs()));
			mw.getUi().getJobOverviewWidget().refreshPage(false);
		}
		
		mw.updatePageLock();
	}
	
	@Override public void handleDocLoadEvent(DocLoadEvent dle) {
		logger.debug("document loaded event: "+dle.doc);
		canvas.setMode(CanvasMode.SELECTION);
		SWTUtil.setEnabled(mw.getUi().getExportDocumentButton(), dle.doc!=null);
		
//		SWTUtil.setEnabled(mw.getUi().getReplacePageImgButton(), dle.doc!=null && dle.doc.isRemoteDoc());
		//SWTUtil.setEnabled(mw.getUi().getDeletePageButton(), dle.doc!=null && dle.doc.isRemoteDoc());
		
		SWTUtil.setEnabled(mw.getUi().getVersionsButton(), dle.doc!=null);
		SWTUtil.setEnabled(mw.getUi().getExportPdfButton(), dle.doc!=null);
		SWTUtil.setEnabled(mw.getUi().getExportTeiButton(), dle.doc!=null);
		SWTUtil.setEnabled(mw.getUi().getExportRtfButton(), dle.doc!=null);
		
		mw.updateDocumentInfo();
	}
	
	@Override public void handleTranscriptListLoadEvent(TranscriptListLoadEvent arg) {
		logger.debug("setting transcripts list: "+arg.transcripts);
		ui.getVersionsWidget().refreshPage(true);
		
//		ui.getToolsWidget().updateVersions(arg.transcripts);
	}

	@Override public void handleTranscriptLoadEvent(TranscriptLoadEvent arg) {
		canvas.setMode(CanvasMode.SELECTION);
		mw.updatePageLock();
		ui.getVersionsWidget().getPageableTable().getViewer().refresh();
//		ui.getVersionsWidget().updateSelectedVersion(arg.transcript==null ? null : arg.transcript.getMd());
		
		ui.getLineTranscriptionWidget().clearAutocompleteProposals();
		ui.getLineTranscriptionWidget().addAutocompleteProposals(arg.transcript);
		
		ui.getWordTranscriptionWidget().clearAutocompleteProposals();
		ui.getWordTranscriptionWidget().addAutocompleteProposals(arg.transcript);
		
		ui.getCommentsWidget().reloadComments();
	}
	
	@Override public void handleLoginOrLogout(LoginOrLogoutEvent arg) {
		logger.debug("handling login event: "+arg);
		canvas.setMode(CanvasMode.SELECTION);
		if (arg.login) {
			ui.updateLoginInfo(arg.login, arg.user.getUserName(), arg.serverUri);
			//load future events from server and show a message box for each
			mw.showEventMessages();
		} else {
			ui.updateLoginInfo(arg.login, "", "");
		}
	}

	@Override public void handlePageLoadEvent(PageLoadEvent arg) {
		canvas.setMode(CanvasMode.SELECTION);
		
		// generate thumb for loaded page if local doc:
		mw.createThumbForCurrentPage();
	}
	
	@Override public void handleDocMetadataUpdateEvent(DocMetadataUpdateEvent e) {
		mw.updateDocumentInfo();
	}

}
