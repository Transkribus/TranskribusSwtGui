package eu.transkribus.swt_gui.mainwidget.listener;

import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.swt_canvas.util.DialogUtil;
import eu.transkribus.swt_canvas.util.SWTUtil;
import eu.transkribus.swt_gui.canvas.CanvasMode;
import eu.transkribus.swt_gui.canvas.SWTCanvas;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.Storage.CollectionsLoadEvent;
import eu.transkribus.swt_gui.mainwidget.Storage.DocLoadEvent;
import eu.transkribus.swt_gui.mainwidget.Storage.DocMetadataUpdateEvent;
import eu.transkribus.swt_gui.mainwidget.Storage.JobUpdateEvent;
import eu.transkribus.swt_gui.mainwidget.Storage.LoginOrLogoutEvent;
import eu.transkribus.swt_gui.mainwidget.Storage.MainImageLoadEvent;
import eu.transkribus.swt_gui.mainwidget.Storage.PageLoadEvent;
import eu.transkribus.swt_gui.mainwidget.Storage.TranscriptListLoadEvent;
import eu.transkribus.swt_gui.mainwidget.Storage.TranscriptLoadEvent;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidgetView;

public class StorageObserver extends AStorageObserver {
	private static final Logger logger = LoggerFactory.getLogger(StorageObserver.class);
	
	Storage storage = Storage.getInstance();
	TrpMainWidget mw;
	TrpMainWidgetView ui;
	SWTCanvas canvas;
	
	public StorageObserver(TrpMainWidget mainWidget) {
		this.mw = mainWidget;
		this.ui = mw.getUi();
		this.canvas = ui.getCanvas();
	}
	
	@Override protected void handleMainImageLoadEvent(MainImageLoadEvent mile) {
		if (storage.isPageLoaded() && storage.getCurrentImage() != null) {
			canvas.getScene().setMainImage(storage.getCurrentImage());
			canvas.redraw();
		}
	}
	
	@Override protected void handleCollectionsLoadEvent(CollectionsLoadEvent cle) {
		
		
		// OLD stuff - update of available collections and reloading doc list now gets handled via CollectionComboViewer!
//		logger.debug("collections loaded!");
//		ui.getDocOverviewWidget().setAvailableCollections(cle.collections);
				
//		logger.debug("now also reloading docs for collection");
//		if (!cle.collections.isEmpty())
//			mw.reloadDocList(cle.collections.get(0));
	}
	
	@Override protected void handleJobUpdate(JobUpdateEvent jue) {
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
	
	@Override protected void handleDocLoadEvent(DocLoadEvent dle) {
		logger.debug("document loaded event: "+dle.doc);
		canvas.setMode(CanvasMode.SELECTION);
		SWTUtil.setEnabled(mw.getUi().getExportDocumentButton(), dle.doc!=null);
		
		SWTUtil.setEnabled(mw.getUi().getReplacePageImgButton(), dle.doc!=null && dle.doc.isRemoteDoc());
		//SWTUtil.setEnabled(mw.getUi().getDeletePageButton(), dle.doc!=null && dle.doc.isRemoteDoc());
		
		SWTUtil.setEnabled(mw.getUi().getExportPdfButton(), dle.doc!=null);
		SWTUtil.setEnabled(mw.getUi().getExportTeiButton(), dle.doc!=null);
		SWTUtil.setEnabled(mw.getUi().getExportRtfButton(), dle.doc!=null);
		
		mw.updateDocumentInfo();
	}
	
	@Override protected void handleTranscriptListLoadEvent(TranscriptListLoadEvent arg) {
		logger.debug("setting transcripts list: "+arg.transcripts);
		ui.getVersionsWidget().refreshPage(true);
		
//		ui.getToolsWidget().updateVersions(arg.transcripts);
	}

	@Override protected void handleTranscriptLoadEvent(TranscriptLoadEvent arg) {
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
	
	@Override protected void handleLoginOrLogout(LoginOrLogoutEvent arg) {
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

	@Override protected void handlePageLoadEvent(PageLoadEvent arg) {
		canvas.setMode(CanvasMode.SELECTION);
		
		// generate thumb for loaded page if local doc:
		mw.createThumbForCurrentPage();
	}
	
	@Override protected void handleDocMetadataUpdateEvent(DocMetadataUpdateEvent e) {
		mw.updateDocumentInfo();
	}

}
