package eu.transkribus.swt_gui.mainwidget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.DropDownToolItem;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.databinding.DataBinder;
import eu.transkribus.swt_gui.canvas.CanvasToolBarNew;
import eu.transkribus.swt_gui.canvas.SWTCanvas;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.vkeyboards.ITrpVirtualKeyboardsTabWidgetListener;
import eu.transkribus.swt_gui.vkeyboards.TrpVirtualKeyboardsTabWidget;

public class TrpMainWidgetViewListener extends SelectionAdapter implements ITrpVirtualKeyboardsTabWidgetListener, IStorageListener {
	private final static Logger logger = LoggerFactory.getLogger(TrpMainWidgetViewListener.class);
	
	TrpMainWidget mw;
	TrpMainWidgetView ui;
	SWTCanvas canvas;
	
	public TrpMainWidgetViewListener(TrpMainWidget mainWidget) {
		this.mw = mainWidget;
		ui = mainWidget.getUi();
		canvas = ui.getCanvas();
		
		addListener();
	}

	private void addListener() {
		DataBinder db = DataBinder.get();
		Storage storage = Storage.getInstance();
				
		SWTUtil.onSelectionEvent(ui.getStructureTreeWidget().getUpdateIDsItem(), (e) -> { mw.updateIDs(); } );
		
		SWTUtil.onSelectionEvent(ui.getStructureTreeWidget().getClearPageItem(), (e) -> { 			
			if (DialogUtil.showYesNoDialog(ui.getShell(), "Really?", "Do you really want to clear the whole page content?")==SWT.YES) {
			ui.getCanvas().getShapeEditor().removeAll();
			} 
		});
		
		SWTUtil.onSelectionEvent(ui.getStructureTreeWidget().getSetReadingOrderRegions(), (e) -> { 
			mw.updateReadingOrderAccordingToCoordinates(false);
			canvas.redraw();
		} );
		
//		db.runOnSelection(ui.getReloadDocumentButton(), (e) -> { mw.reloadCurrentDocument(); } );
		
		SWTUtil.onSelectionEvent(ui.exportDocumentButton, (e) -> { mw.unifiedExport(); } );
		
		SWTUtil.onSelectionEvent(ui.reloadDocumentButton, (e) -> { mw.reloadCurrentDocument(); } );
						
		SWTUtil.onSelectionEvent(ui.versionsButton, (e) -> { mw.openVersionsDialog(); } );
				
		SWTUtil.onSelectionEvent(ui.saveDrowDown, (e) -> {
			if (e.detail != SWT.ARROW) {
				boolean withMessage = ui.saveDrowDown.getSelected()==ui.saveTranscriptWithMessageMenuItem;
				mw.saveTranscription(withMessage);
			}
		});
		
		SWTUtil.onSelectionEvent(ui.saveTranscriptToolItem, (e) -> { mw.saveTranscription(false); } );
		SWTUtil.onSelectionEvent(ui.saveTranscriptWithMessageToolItem, (e) -> { mw.saveTranscription(true); } );
		
		
//		SWTUtil.addSelectionListener(ui.getSaveTranscriptButton(), this);
//		SWTUtil.addSelectionListener(ui.getSaveTranscriptWithMessageButton(), this);
		
		SWTUtil.onSelectionEvent(ui.getCloseDocBtn(), (e) -> { mw.closeCurrentDocument(false); } );
		
		SWTUtil.onSelectionEvent(ui.getOpenLocalFolderButton(), (e) -> { mw.loadLocalFolder(); } );
				
		SWTUtil.onSelectionEvent(ui.getLoadTranscriptInTextEditor(), (e) -> { mw.openPAGEXmlViewer(); } );
		
		SWTUtil.onSelectionEvent(ui.getServerWidget().getLoginBtn(), (e) -> {
			if (!mw.getStorage().isLoggedIn())
				mw.loginDialog("Enter your email and password");
			else
				mw.logout(false, true);
		});
		
		SWTUtil.onSelectionEvent(ui.getUploadDocsItem(), (e) -> { mw.uploadDocuments(); } );
		
		SWTUtil.onSelectionEvent(ui.getSearchBtn(), (e) -> { mw.openSearchDialog(); } );
								
		SWTUtil.addSelectionListener(ui.getProfilesToolItem().ti, this);
		
		SWTUtil.onSelectionEvent(ui.getProfilesToolItem().ti, (e) -> {
			if (e.detail != SWT.ARROW && e.detail == DropDownToolItem.IS_DROP_DOWN_ITEM_DETAIL) {
				mw.changeProfileFromUi();
			}
		});
				
		SWTUtil.onSelectionEvent(ui.getThumbnailWidget().getCreateThumbs(), (e) -> { mw.createThumbs(storage.getDoc()); } );
				
		SWTUtil.onSelectionEvent(ui.getServerWidget().getShowJobsBtn(), (e) -> { mw.openJobsDialog(); } );
		
		SWTUtil.onSelectionEvent(ui.getServerWidget().getShowVersionsBtn(), (e) -> { mw.openVersionsDialog(); } );
				
//		SWTUtil.onSelectionEvent(ui.helpItem, (e) -> { mw.openCanvasHelpDialog(); } );
		
		SWTUtil.onSelectionEvent(ui.bugReportItem, (e) -> { mw.sendBugReport(); } );
		
		CanvasToolBarNew tb = ui.getCanvasWidget().getToolbar();
		
		SWTUtil.onSelectionEvent(tb.rotateLeftBtn, (e) -> { canvas.rotateLeft(); });
		SWTUtil.onSelectionEvent(tb.rotateRightBtn, (e) -> { canvas.rotateRight(); });
		SWTUtil.onSelectionEvent(tb.rotateLeft90Btn, (e) -> { canvas.rotate90Left(); });
		SWTUtil.onSelectionEvent(tb.rotateRight90Btn, (e) -> { canvas.rotate90Right(); });
		
		SWTUtil.onSelectionEvent(tb.translateLeftBtn, (e) -> { canvas.translateLeft(); });
		SWTUtil.onSelectionEvent(tb.translateRightBtn, (e) -> { canvas.translateRight(); });
		SWTUtil.onSelectionEvent(tb.translateUpBtn, (e) -> { canvas.translateUp(); });
		SWTUtil.onSelectionEvent(tb.translateDownBtn, (e) -> { canvas.translateDown(); });
		
		SWTUtil.onSelectionEvent(tb.fitPageItem, (e) -> { canvas.fitToPage(); });
		SWTUtil.onSelectionEvent(tb.fitWidthItem, (e) -> { canvas.fitWidth();; });
		SWTUtil.onSelectionEvent(tb.origSizeItem, (e) -> { canvas.resetTransformation(); });
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	@Override public void onVirtualKeyPressed(TrpVirtualKeyboardsTabWidget w, char c, String description) {
		mw.insertTextOnSelectedTranscriptionWidget(c);
	}
	
}
