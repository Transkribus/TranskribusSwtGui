package eu.transkribus.swt_gui.mainwidget.listener;

import java.net.URI;
import java.net.URL;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.swt_canvas.canvas.CanvasScene;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_canvas.util.DialogUtil;
import eu.transkribus.swt_canvas.util.DropDownToolItem;
import eu.transkribus.swt_canvas.util.SWTUtil;
import eu.transkribus.swt_canvas.xmlviewer.XmlViewer;
import eu.transkribus.swt_gui.canvas.TrpSWTCanvas;
import eu.transkribus.swt_gui.dialogs.PAGEXmlViewer;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidgetView;
import eu.transkribus.swt_gui.menubar.TrpMenuBar;
import eu.transkribus.swt_gui.transcription.ATranscriptionWidget;
import eu.transkribus.util.DesktopApi;

public class TrpMainWidgetListener extends SelectionAdapter {
	private final static Logger logger = LoggerFactory.getLogger(TrpMainWidgetListener.class);
	
	TrpMainWidget mainWidget;
	TrpMainWidgetView ui;
	TrpMenuBar menuBar;
	TrpSWTCanvas canvas;
	
	public TrpMainWidgetListener(TrpMainWidget mainWidget) {
		this.mainWidget = mainWidget;
		ui = mainWidget.getUi();
		menuBar = mainWidget.getUi().getTrpMenuBar();
		canvas = ui.getCanvas();
		
		addListener();
	}

	private void addListener() {
		menuBar.getLoadTestsetMenuItem().addSelectionListener(this);
		menuBar.getOpenMenuItem().addSelectionListener(this);
		menuBar.getOpenLocalPageFileItem().addSelectionListener(this);
		menuBar.getUploadImagesFromPdfFileItem().addSelectionListener(this);
		menuBar.getManageCollectionsMenuItem().addSelectionListener(this);
		menuBar.getSyncWordsWithLinesMenuItem().addSelectionListener(this);
		menuBar.getSaveTranscriptionToNewFileMenuItem().addSelectionListener(this);
		menuBar.getSaveTranscriptionMenuItem().addSelectionListener(this);
		menuBar.getAboutMenuItem().addSelectionListener(this);
		menuBar.getUpdateMenuItem().addSelectionListener(this);
		menuBar.getInstallMenuItem().addSelectionListener(this);
		menuBar.getTipsOfTheDayMenuItem().addSelectionListener(this);
		menuBar.getAnalyzeStructureItem().addSelectionListener(this);
		
		menuBar.getDeletePageMenuItem().addSelectionListener(this);
		
		// update IDs of segmentation:
		ui.getStructureTreeWidget().getUpdateIDsItem().addSelectionListener(this);
		ui.getStructureTreeWidget().getClearPageItem().addSelectionListener(this);
		
		ui.getStructureTreeWidget().getSetReadingOrderRegions().addSelectionListener(this);
//		ui.getStructureTreeWidget().getDeleteReadingOrderRegions().addSelectionListener(this);
		
		SWTUtil.addSelectionListener(ui.getReloadDocumentButton(), this);
		SWTUtil.addSelectionListener(ui.getExportDocumentButton(), this);
		SWTUtil.addSelectionListener(ui.getReplacePageImgButton(), this);
		//SWTUtil.addToolItemSelectionListener(ui.getDeletePageButton(), this);
		
		SWTUtil.addSelectionListener(ui.getExportPdfButton(), this);
		SWTUtil.addSelectionListener(ui.getExportTeiButton(), this);
		SWTUtil.addSelectionListener(ui.getExportRtfButton(), this);
		
		ui.getSaveTranscriptButton().addSelectionListener(this);
		ui.getSaveTranscriptWithMessageButton().addSelectionListener(this);
		ui.getOpenLocalFolderButton().addSelectionListener(this);
		ui.getCloseDocBtn().addSelectionListener(this);
		ui.getLoadTranscriptInTextEditor().addSelectionListener(this);
		ui.getSendBugReportButton().addSelectionListener(this);
		ui.getLoginToggle().addSelectionListener(this);
		
		ui.getVkeyboards().getVirtualKeyboardsTabWidget().addKeySelectionListener(this);
		
//		SWTUtil.addToolItemSelectionListener(ui.getShowReadingOrderToolItem().ti, this);
		SWTUtil.addSelectionListener(ui.getProfilesToolItem().ti, this);
		
//		SWTUtil.addToolItemSelectionListener(ui.getLanguageDropDown().ti, this);
		
		// listener for tagging:
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		Object s = e.getSource();
				
		// MENU BUTTONS:
		if (s == ui.getLoginToggle()) {
//			ui.getLoginToggle().setSelection(false);
			if (!mainWidget.getStorage().isLoggedIn())
				mainWidget.loginDialog("Please give me your credentials!");
			else
				mainWidget.logout(false, true);
		}
		if (s == menuBar.getLoadTestsetMenuItem()) {
			mainWidget.loadLocalTestset();
		}
		else if (s == menuBar.getOpenMenuItem() || s == ui.getOpenLocalFolderButton()) {
			mainWidget.loadLocalFolder();
		}
		else if (s == menuBar.getOpenLocalPageFileItem()) {
			mainWidget.loadLocalPageXmlFile();
		}
		else if (s == menuBar.getDeletePageMenuItem()){
			mainWidget.deletePage();
		}
		else if (s == menuBar.getUploadImagesFromPdfFileItem()) {
			mainWidget.uploadDocuments();
		}
		
		else if (s == menuBar.getManageCollectionsMenuItem()) {
			mainWidget.getUi().getDocOverviewWidget().openCollectionsManagerWidget();
		}
		else if (s == ui.getCloseDocBtn()) {
			mainWidget.closeCurrentDocument(false);
		}
		
	
//		else if (s == ui.getShowReadingOrderToolItem().ti && e.detail != SWT.ARROW && e.detail == DropDownToolItem.IS_DROP_DOWN_ITEM_DETAIL) {
//			logger.debug("tool item choice is: " + ui.getShowReadingOrderToolItem().getLastSelectedIndex());
//			
//			boolean showR = ui.getShowReadingOrderToolItem().getSelectedIndices().contains(0);
//			boolean showL = ui.getShowReadingOrderToolItem().getSelectedIndices().contains(1);
//			boolean showW = ui.getShowReadingOrderToolItem().getSelectedIndices().contains(2);
//
//			canvas.getScene().setRegionsRO(showR);
//			canvas.getScene().setLinesRO(showL);
//			canvas.getScene().setWordsRO(showW);
//			
//			mainWidget.updateReadingOrderVisibility(ui.getShowReadingOrderToolItem().getSelectedIndices());
//			
//			canvas.redraw();
//		}
		else if (s == ui.getProfilesToolItem().ti && e.detail != SWT.ARROW && e.detail == DropDownToolItem.IS_DROP_DOWN_ITEM_DETAIL) {			
			int i = ui.getProfilesToolItem().getLastSelectedIndex();
			logger.debug("i = "+i+" detail = "+e.detail);
			
			if (i>=0 && i < ui.getProfilesToolItem().getItemCount()-1) { // profile selected
				if (!SWTUtil.isDisposed(ui.getProfilesToolItem().getSelected()) && ui.getProfilesToolItem().getSelected().getData() instanceof String) {				
					String name = (String) ui.getProfilesToolItem().getSelected().getData();
					logger.info("selecting profile: "+name);
					mainWidget.selectProfile(name);
					
					//select the first shape if nothing is selected
//					if(canvas.getNSelected() == 0){
//						TrpTextRegionType ttrt = Storage.getInstance().getCurrentRegionObject();
//						if (ttrt == null){
//							logger.info("current region object is null");
//							ttrt = Storage.getInstance().getTextRegions().get(0);
//							mainWidget.getScene().selectObject((ICanvasShape) ttrt.getData(), true, false);
//							logger.info("current regions length " + Storage.getInstance().getTextRegions().size());
//						}
//						
//						if (ttrt != null){
//							if (!ttrt.getChildren(false).isEmpty())
//								mainWidget.getScene().selectObject((ICanvasShape) ttrt.getChildren(false).get(0).getData(), true, false);
//						}
//					}
					
					boolean mode = (name.contains("Transcription")? true : false);
					canvas.getScene().setTranscriptionMode(mode);
				}
			} else if (i == ui.getProfilesToolItem().getItemCount()-1) {
				logger.info("opening save profile dialog...");
				mainWidget.saveNewProfile();
				canvas.getScene().setTranscriptionMode(false);
			}
		}
		else if (s == menuBar.getSyncWordsWithLinesMenuItem()) {
			mainWidget.syncTextOfDocFromWordsToLinesAndRegions();

			// TODO sync words with lines
//			logger.debug("syncing words to lines!");
//			if (Storage.getInstance().hasCurrentTranscript()) {
//				for (TrpTextRegionType tr : Storage.getInstance().getCurrentJAXBTranscriptObject().getPage().getRegions()) {
//					// FIXME: apply text from words to all lines!
//				}
//				
//			}
			
		}
		else if (s == menuBar.getSaveTranscriptionToNewFileMenuItem()) {
			mainWidget.saveTranscriptionToNewFile();
		}
		else if (s == menuBar.getSaveTranscriptionMenuItem() || s == ui.getSaveTranscriptButton()) {
			mainWidget.saveTranscription(false);
		}
		else if (s == ui.getSaveTranscriptWithMessageButton()) {
			mainWidget.saveTranscription(true);
		}
		else if (s == menuBar.getUpdateMenuItem()) {
			mainWidget.checkForUpdates();
		}
		else if (s == menuBar.getInstallMenuItem()) {
			mainWidget.installSpecificVersion();
		}
		else if (s == menuBar.getTipsOfTheDayMenuItem()) {
			mainWidget.showTipsOfTheDay();
		}
		else if (s == menuBar.getAnalyzeStructureItem()) {
			mainWidget.analyzePageStructure(true, true, true);
		}
		else if (s == menuBar.getAboutMenuItem()) {
//			DialogUtil.showInfoMessageBox(mainWidget.getShell(), ui.APP_NAME
//					, ui.HELP_TEXT);
			int res = DialogUtil.showMessageDialog(mainWidget.getShell(), ui.APP_NAME, ui.HELP_TEXT, null, MessageDialog.INFORMATION, 
					new String[] {"OK", "Report bug / feature request"}, 0);
			if (res == 1) {
				ui.getSendBugReportButton().notifyListeners(SWT.Selection, new Event());
			}
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
//		else if (s == ui.getStructureTreeWidget().getDeleteReadingOrderRegions()) {
//			mainWidget.updateReadingOrderAccordingToCoordinates(true);
//		}
		else if (s == ui.getReloadDocumentButton()) {
			mainWidget.reloadCurrentDocument();
		}
		else if (s == ui.getReplacePageImgButton()) {
			mainWidget.replacePageImg();
		}
//		else if (s == ui.getDeletePageButton()) {
//			mainWidget.deletePage();
//		}
		else if (s == ui.getExportDocumentButton()) {
			//mainWidget.exportDocument();
			mainWidget.unifiedExport();
		}
//		else if (s == ui.getExportPdfButton()) {
//			mainWidget.exportPdf();
//		}
//		else if (s == ui.getExportRtfButton()) {
//			mainWidget.exportRtf();
//		}
//		else if (s == ui.getExportTeiButton()) {
//			mainWidget.exportTei();
//		}
		else if (s == ui.getSendBugReportButton()) {
			mainWidget.sendBugReport();
		}
//		else if (s == ui.getReloadTranscriptsButton()) {
//			mainWidget.reloadCurrentTranscript(false);
//		}
//		else if (s == ui.getAddShapeActionCombo()) {
//			mainWidget.updateAddShapeActionButton();
//		}
		
		// VISUAL KEYBOARD:
		else if (s == ui.getVkeyboards().getVirtualKeyboardsTabWidget()) {
			Character c = (char) e.detail;
			logger.debug("key pressed: "+c+", name: "+e.text);
			
			ATranscriptionWidget tw = ui.getSelectedTranscriptionWidget();
			if (tw != null) {
				tw.insertTextIfFocused(""+c);
			}
		}
		
		else if (s == ui.getLoadTranscriptInTextEditor()) {
			logger.debug("loading transcript source");
			if (Storage.getInstance().isPageLoaded() && Storage.getInstance().getTranscriptMetadata()!=null) {
				URL url = Storage.getInstance().getTranscriptMetadata().getUrl();
				
				try {
					if (true) {
//						XmlViewer xmlviewer = new XmlViewer(ui.getShell(), SWT.MODELESS);
						PAGEXmlViewer xmlviewer = new PAGEXmlViewer(ui.getShell(), SWT.MODELESS);
						xmlviewer.open(url);
					} else {
						DesktopApi.browse(new URI(url.toString().replaceAll(" ", "%20")));		
					}
				} catch (Exception e1) {
					mainWidget.onError("Could not open XML", "Could not open XML", e1);
				}
				
			}
		}
//		else if (s == ui.getLanguageDropDown().ti && e.detail != SWT.ARROW) {
//			mainWidget.setLocale((Locale) ui.getLanguageDropDown().getSelected().getData());
//		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		
	}
	
}
