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

public class TrpMainWidgetViewListener extends SelectionAdapter implements ITrpVirtualKeyboardsTabWidgetListener {
	private final static Logger logger = LoggerFactory.getLogger(TrpMainWidgetViewListener.class);
	
	TrpMainWidget mainWidget;
	TrpMainWidgetView ui;
	SWTCanvas canvas;
	
	public TrpMainWidgetViewListener(TrpMainWidget mainWidget) {
		this.mainWidget = mainWidget;
		ui = mainWidget.getUi();
		canvas = ui.getCanvas();
		
		addListener();
	}

	private void addListener() {		
		ui.getStructureTreeWidget().getUpdateIDsItem().addSelectionListener(this);
		ui.getStructureTreeWidget().getClearPageItem().addSelectionListener(this);
		
		ui.getStructureTreeWidget().getSetReadingOrderRegions().addSelectionListener(this);
		
		SWTUtil.addSelectionListener(ui.getReloadDocumentButton(), this);
		SWTUtil.addSelectionListener(ui.getExportDocumentButton(), this);
		SWTUtil.addSelectionListener(ui.getVersionsButton(), this);
		
		SWTUtil.addSelectionListener(ui.getExportPdfButton(), this);
		SWTUtil.addSelectionListener(ui.getExportTeiButton(), this);
		SWTUtil.addSelectionListener(ui.getExportRtfButton(), this);
		
		SWTUtil.addSelectionListener(ui.getSaveDropDown(), this);
//		SWTUtil.addSelectionListener(ui.getSaveTranscriptButton(), this);
//		SWTUtil.addSelectionListener(ui.getSaveTranscriptWithMessageButton(), this);
		
		SWTUtil.addSelectionListener(ui.getCloseDocBtn(), this);
		
		SWTUtil.addSelectionListener(ui.getOpenLocalFolderButton(), this);
		SWTUtil.addSelectionListener(ui.getLoadTranscriptInTextEditor(), this);
		SWTUtil.addSelectionListener(ui.getLoginToggle(), this);
		
		SWTUtil.addSelectionListener(ui.getUploadDocsItem(), this);
		SWTUtil.addSelectionListener(ui.getSearchBtn(), this);
						
		SWTUtil.addSelectionListener(ui.getProfilesToolItem().ti, this);
		
		SWTUtil.addSelectionListener(ui.getThumbnailWidget().getCreateThumbs(), this);
		
		SWTUtil.addSelectionListener(ui.getServerWidget().getShowJobsBtn(), this);
		
//		DataBinder db = DataBinder.get();
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		Object s = e.getSource();
		Storage storage = Storage.getInstance();
		
		logger.debug("source = "+s);
				
		if (s == ui.getLoginToggle()) {
			if (!mainWidget.getStorage().isLoggedIn())
				mainWidget.loginDialog("Please give me your credentials!");
			else
				mainWidget.logout(false, true);
		}
		if (s == ui.getOpenLocalFolderButton()) {
			mainWidget.loadLocalFolder();
		}
		else if (s == ui.getCloseDocBtn()) {
			mainWidget.closeCurrentDocument(false);
		}
		else if (s == ui.getUploadDocsItem()) {
			mainWidget.uploadDocuments();
		} 
		else if (s == ui.getSearchBtn()) {
			mainWidget.openSearchDialog();
		}
		// 
		else if (s == ui.getProfilesToolItem().ti && e.detail != SWT.ARROW && e.detail == DropDownToolItem.IS_DROP_DOWN_ITEM_DETAIL) {
			int i = ui.getProfilesToolItem().getLastSelectedIndex();
			logger.debug("i = "+i+" detail = "+e.detail);
			
			if (i>=0 && i < ui.getProfilesToolItem().getItemCount()-1) { // profile selected
				if (!SWTUtil.isDisposed(ui.getProfilesToolItem().getSelected()) && ui.getProfilesToolItem().getSelected().getData() instanceof String) {				
					String name = (String) ui.getProfilesToolItem().getSelected().getData();
					logger.info("selecting profile: "+name);
					mainWidget.selectProfile(name);
										
					boolean mode = (name.contains("Transcription")? true : false);
					canvas.getScene().setTranscriptionMode(mode);
				}
			} else if (i == ui.getProfilesToolItem().getItemCount()-1) {
				logger.info("opening save profile dialog...");
				mainWidget.saveNewProfile();
				canvas.getScene().setTranscriptionMode(false);
			}
		}
		
		if (s == ui.getSaveDropDown().ti && e.detail != SWT.ARROW) {
			boolean withMessage = ui.getSaveDropDown().getSelected()==ui.getSaveTranscriptWithMessageButton();
			mainWidget.saveTranscription(withMessage);
		}
				
		// UI BUTTONS:
		else if (s == ui.getStructureTreeWidget().getUpdateIDsItem()) {			
			mainWidget.updateIDs();
		}
		else if (s == ui.getStructureTreeWidget().getClearPageItem()) {
			if (DialogUtil.showYesNoDialog(ui.getShell(), "Really?", "Do you really want to clear the whole page content?")==SWT.YES) {
				ui.getCanvas().getShapeEditor().removeAll();
			}
		}
		else if (s == ui.getStructureTreeWidget().getSetReadingOrderRegions()) {
			mainWidget.updateReadingOrderAccordingToCoordinates(false);
			canvas.redraw();
		}
		else if (s == ui.getReloadDocumentButton()) {
			mainWidget.reloadCurrentDocument();
		}
		else if (s == ui.getVersionsButton()) {
			mainWidget.openVersionsDialog();
		}
		else if (s == ui.getServerWidget().getShowJobsBtn()) {
			mainWidget.openJobsDialog();
		}
		else if (s == ui.getExportDocumentButton()) {
			mainWidget.unifiedExport();
		}
		else if (s == ui.getLoadTranscriptInTextEditor()) {
			mainWidget.openPAGEXmlViewer();
		}
		else if (s == ui.getThumbnailWidget().getCreateThumbs()) {
			mainWidget.createThumbs(storage.getDoc());
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		
	}

	@Override public void onVirtualKeyPressed(TrpVirtualKeyboardsTabWidget w, char c, String description) {
		mainWidget.insertTextOnSelectedTranscriptionWidget(c);
	}
	
}
