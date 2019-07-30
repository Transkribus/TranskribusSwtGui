package eu.transkribus.swt_gui.mainwidget;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.canvas.CanvasMode;
import eu.transkribus.swt_gui.canvas.SWTCanvas;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class TrpMainWidgetStorageListener implements IStorageListener {
	private static final Logger logger = LoggerFactory.getLogger(TrpMainWidgetStorageListener.class);
	
	Storage storage = Storage.getInstance();
	TrpMainWidget mw;
	TrpMainWidgetView ui;
	SWTCanvas canvas;
	
	/**
	 * Counts all DocListLoadEvents until the collection is changed.
	 */
	private int docListLoadEventCounter = 0;
	
	public TrpMainWidgetStorageListener(TrpMainWidget mainWidget) {
		this.mw = mainWidget;
		this.ui = mw.getUi();
		this.canvas = ui.getCanvas();
		
		attach();
		
		this.ui.addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(DisposeEvent e) {
				detach();
			}
		});
	}
	
	void attach() {
		storage.addListener(this);
	}
	
	void detach() {
		storage.removeListener(this);
	}
	
	@Override public void handleMainImageLoadEvent(MainImageLoadEvent mile) {
		if (storage.isPageLoaded() && storage.getCurrentImage() != null) {
			canvas.getScene().setMainImage(storage.getCurrentImage());
			canvas.redraw();
		}
	}
	
	@Override public void handleUserDocListLoadEvent(UserDocListLoadEvent e) {
		if (SWTUtil.isOpen(mw.strayDocsDialog)){
			mw.strayDocsDialog.refreshDocList();
		}
	}
	
	@Override public void handleDocListLoadEvent(DocListLoadEvent e) {
		if(e.isCollectionChange) {
			logger.debug("Collection changed to ID = " + e.collId);
			docListLoadEventCounter = 0;
		}
		logger.debug("Handling DocListLoadEvent #" + ++docListLoadEventCounter + " in collection " + e.collId + " sent by " + e.getSource());
		if (mw.recycleBinDiag != null){
			mw.recycleBinDiag.getDocTableWidget().refreshList(mw.getSelectedCollectionId());
		}
		if(e.isCollectionChange) {
			mw.getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					//force a reload of the HTR model list only if collection has changed
					logger.debug("Async load HTR list");
					storage.reloadHtrs();
					logger.debug("Loading HTR list done.");
				}
			});
		} else {
			logger.debug("Omitting reload of HTR list as collection has not changed.");
		}
	}
	
	@Override public void handleDocLoadEvent(DocLoadEvent dle) {
		logger.debug("document loaded event: "+dle.doc);
		canvas.setMode(CanvasMode.SELECTION);
		
		SWTUtil.setEnabled(mw.getUi().getExportDocumentButton(), dle.doc!=null);
		SWTUtil.setEnabled(mw.getUi().getVersionsButton(), dle.doc!=null);
		SWTUtil.setEnabled(mw.getUi().getLoadTranscriptInTextEditor(), dle.doc!=null);
		
		SWTUtil.setEnabled(mw.getUi().getSaveTranscriptToolItem(), dle.doc!=null);
		SWTUtil.setEnabled(mw.getUi().getSaveTranscriptWithMessageToolItem(), dle.doc!=null);
		
		mw.updateDocumentInfo();
		
		if(dle.doc != null && dle.doc.isLocalDoc()) {
			/*
			 * check if the doc contains known errors and display an error message in case
			 * FIXME when remote doc is loaded this does not work and the GUI is then locked!?!?
			 */
			final String problems = dle.doc.getImageErrors();
			if(!StringUtils.isEmpty(problems)) {
				mw.onError("The document contains faulty pages!", problems, null);
			}
		}
		
		//switch to collection tab if doc is no gt doc and other tab is selected
		mw.getUi().getServerWidget().selectCollectionsTab();		
		mw.getUi().updateVisibility();
	}
	
	@Override public void handleGroundTruthLoadEvent(GroundTruthLoadEvent dle) {
		logger.debug("ground truth loaded event: "+dle.doc);
		canvas.setMode(CanvasMode.SELECTION);
		
		/**
		 * FIXME
		 * those settings are overwritten immediately after loading the document by a call to TrpMainWidget#updateToolBars in TrpMainWidget#reloadCurrentPage
		 */
		SWTUtil.setEnabled(mw.getUi().getExportDocumentButton(), false);
		SWTUtil.setEnabled(mw.getUi().getVersionsButton(), false);
		SWTUtil.setEnabled(mw.getUi().getLoadTranscriptInTextEditor(), dle.doc!=null);
		SWTUtil.setEnabled(mw.getUi().getSaveTranscriptToolItem(), false);
		SWTUtil.setEnabled(mw.getUi().getSaveTranscriptWithMessageToolItem(), false);
		
		mw.updateDocumentInfo();
		
		mw.getUi().updateVisibility();
	}
	
	@Override public void handleTranscriptLoadEvent(TranscriptLoadEvent arg) {
		canvas.setMode(CanvasMode.SELECTION);
		mw.updatePageLock();
		
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
			ui.getTabWidget().selectServerTab();
			ui.updateLoginInfo(arg.login, arg.user.getUserName(), arg.serverUri);
			 
			//load future events from server and show a message box for each
			mw.showEventMessages();
		} else {
			ui.updateLoginInfo(arg.login, "", "");
		}
		
		SWTUtil.setEnabled(mw.getUi().getJobsButton(), arg.login);
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
