package eu.transkribus.swt_gui.mainwidget.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.DropDownToolItem;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.databinding.DataBinder;
import eu.transkribus.swt_gui.canvas.SWTCanvas;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidgetView;
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
				
		db.runOnSelection(ui.getStructureTreeWidget().getUpdateIDsItem(), (e) -> { mw.updateIDs(); } );
		
		db.runOnSelection(ui.getStructureTreeWidget().getClearPageItem(), (e) -> { 			
			if (DialogUtil.showYesNoDialog(ui.getShell(), "Really?", "Do you really want to clear the whole page content?")==SWT.YES) {
			ui.getCanvas().getShapeEditor().removeAll();
			} 
		});
		
		db.runOnSelection(ui.getStructureTreeWidget().getSetReadingOrderRegions(), (e) -> { 
			mw.updateReadingOrderAccordingToCoordinates(false);
			canvas.redraw();
		} );
		
//		db.runOnSelection(ui.getReloadDocumentButton(), (e) -> { mw.reloadCurrentDocument(); } );
		
		db.runOnSelection(ui.getExportDocumentButton(), (e) -> { mw.unifiedExport(); } );
						
		db.runOnSelection(ui.getVersionsButton(), (e) -> { mw.openVersionsDialog(); } );
				
		db.runOnSelection(ui.getSaveDropDown(), (e) -> {
			boolean withMessage = ui.getSaveDropDown().getSelected()==ui.getSaveTranscriptWithMessageButton();
			mw.saveTranscription(withMessage);
		});
//		SWTUtil.addSelectionListener(ui.getSaveTranscriptButton(), this);
//		SWTUtil.addSelectionListener(ui.getSaveTranscriptWithMessageButton(), this);
		
		db.runOnSelection(ui.getCloseDocBtn(), (e) -> { mw.closeCurrentDocument(false); } );
		
		db.runOnSelection(ui.getOpenLocalFolderButton(), (e) -> { mw.loadLocalFolder(); } );
				
		db.runOnSelection(ui.getLoadTranscriptInTextEditor(), (e) -> { mw.openPAGEXmlViewer(); } );
		
		db.runOnSelection(ui.getLoginToggle(), (e) -> {
			if (!mw.getStorage().isLoggedIn())
				mw.loginDialog("Please enter email and password!");
			else
				mw.logout(false, true);
		});
		
		db.runOnSelection(ui.getUploadDocsItem(), (e) -> { mw.uploadDocuments(); } );
		
		db.runOnSelection(ui.getSearchBtn(), (e) -> { mw.openSearchDialog(); } );
								
		SWTUtil.addSelectionListener(ui.getProfilesToolItem().ti, this);
		
		db.runOnSelection(ui.getProfilesToolItem().ti, (e) -> {
			if (e.detail != SWT.ARROW && e.detail == DropDownToolItem.IS_DROP_DOWN_ITEM_DETAIL) {
				mw.changeProfileFromUi();
			}
		});
				
		db.runOnSelection(ui.getThumbnailWidget().getCreateThumbs(), (e) -> { mw.createThumbs(storage.getDoc()); } );
				
		db.runOnSelection(ui.getServerWidget().getShowJobsBtn(), (e) -> { mw.openJobsDialog(); } );
		
		db.runOnSelection(ui.getServerWidget().getShowVersionsBtn(), (e) -> { mw.openVersionsDialog(); } );
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
