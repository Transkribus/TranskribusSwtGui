package eu.transkribus.swt_gui.mainwidget;

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
	
	@Override public void handleCollectionsLoadEvent(CollectionsLoadEvent cle) {
	}
	
	@Override public void handleDocListLoadEvent(DocListLoadEvent e) {
		ui.getServerWidget().refreshDocList();
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
