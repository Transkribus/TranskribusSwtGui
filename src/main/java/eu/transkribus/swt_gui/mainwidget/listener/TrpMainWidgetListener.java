package eu.transkribus.swt_gui.mainwidget.listener;

import java.net.URI;
import java.net.URL;
import java.util.Locale;

import org.dea.swt.util.DialogUtil;
import org.dea.swt.util.SWTUtil;
import org.dea.swt.util.XmlViewer;
import org.dea.util.DesktopApi;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_gui.Msgs;
import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.canvas.TrpSWTCanvas;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidgetView;
import eu.transkribus.swt_gui.mainwidget.TrpSettings;
import eu.transkribus.swt_gui.menubar.TrpMenuBar;
import eu.transkribus.swt_gui.transcription.ATranscriptionWidget;

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
		menuBar.getManageCollectionsMenuItem().addSelectionListener(this);
		menuBar.getSyncWordsWithLinesMenuItem().addSelectionListener(this);
		menuBar.getSaveTranscriptionToNewFileMenuItem().addSelectionListener(this);
		menuBar.getSaveTranscriptionMenuItem().addSelectionListener(this);
		menuBar.getAboutMenuItem().addSelectionListener(this);
		menuBar.getUpdateMenuItem().addSelectionListener(this);
		menuBar.getInstallMenuItem().addSelectionListener(this);
		menuBar.getTipsOfTheDayMenuItem().addSelectionListener(this);
		menuBar.getAnalyzeStructureItem().addSelectionListener(this);
		
		// update IDs of segmentation:
		ui.getStructureTreeWidget().getUpdateIDsItem().addSelectionListener(this);
		ui.getStructureTreeWidget().getClearPageItem().addSelectionListener(this);
		
		ui.getStructureTreeWidget().getSetReadingOrderRegions().addSelectionListener(this);
//		ui.getStructureTreeWidget().getDeleteReadingOrderRegions().addSelectionListener(this);
		
		SWTUtil.addToolItemSelectionListener(ui.getReloadDocumentButton(), this);
		SWTUtil.addToolItemSelectionListener(ui.getExportDocumentButton(), this);
		SWTUtil.addToolItemSelectionListener(ui.getReplacePageImgButton(), this);
		SWTUtil.addToolItemSelectionListener(ui.getDeletePageButton(), this);
		
		SWTUtil.addToolItemSelectionListener(ui.getExportPdfButton(), this);
		SWTUtil.addToolItemSelectionListener(ui.getExportTeiButton(), this);
		SWTUtil.addToolItemSelectionListener(ui.getExportRtfButton(), this);
		
		ui.getSaveTranscriptButton().addSelectionListener(this);
		ui.getSaveTranscriptWithMessageButton().addSelectionListener(this);
		ui.getOpenLocalFolderButton().addSelectionListener(this);
		ui.getCloseDocBtn().addSelectionListener(this);
		ui.getLoadTranscriptInTextEditor().addSelectionListener(this);
		ui.getSendBugReportButton().addSelectionListener(this);
		ui.getLoginToggle().addSelectionListener(this);
		
		ui.getVkeyboards().addSelectionListener(this);
		
		SWTUtil.addToolItemSelectionListener(ui.getShowReadingOrderToolItem().ti, this);
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
		
		else if (s == menuBar.getManageCollectionsMenuItem()) {
			mainWidget.getUi().getDocOverviewWidget().openCollectionsManagerWidget();
		}
		else if (s == ui.getCloseDocBtn()) {
			mainWidget.closeCurrentDocument(false);
		}
	
		else if (s == ui.getShowReadingOrderToolItem().ti && e.detail != SWT.ARROW) {
			logger.debug("tool item choice is: " + ui.getShowReadingOrderToolItem().getLastSelectedIndex());
			
			boolean showR = ui.getShowReadingOrderToolItem().getSelectedIndices().contains(0);
			boolean showL = ui.getShowReadingOrderToolItem().getSelectedIndices().contains(1);
			boolean showW = ui.getShowReadingOrderToolItem().getSelectedIndices().contains(2);
			
			
			canvas.getScene().setRegionsRO(showR);
			canvas.getScene().setLinesRO(showL);
			canvas.getScene().setWordsRO(showW);
			
			mainWidget.updateReadingOrderVisibility(ui.getShowReadingOrderToolItem().getSelectedIndices());
			
			canvas.redraw();
		
//		canvas.getScene().setShowReadingOrderForShapes();
			
//			switch (ui.getShowReadingOrderToolItem().getLastSelectedIndex()) {
//			case 0:
//				//if (previously clicked the subsequent click means show no reading order
//				if (canvas.getScene().isRegionsRO()){
////					ui.getShowReadingOrderToolItem().ti.setImage(Images.getOrLoad("/icons/reading_order_r_hide.png"));
//					canvas.getScene().setRegionsRO(false);
//				}
//				else{
////					ui.getShowReadingOrderToolItem().ti.setImage(Images.getOrLoad("/icons/reading_order_r.png"));
//					canvas.getScene().setRegionsRO(true);
//				}
//				canvas.getScene().setLinesRO(false);
//				canvas.getScene().setWordsRO(false);
//				canvas.getScene().setShowReadingOrderForShapes();
//				break;
//			case 1:
//				if (canvas.getScene().isLinesRO()){
//					ui.getShowReadingOrderToolItem().ti.setImage(Images.getOrLoad("/icons/reading_order_l_hide.png"));
//					canvas.getScene().setLinesRO(false);
//				}
//				else{
//					ui.getShowReadingOrderToolItem().ti.setImage(Images.getOrLoad("/icons/reading_order_l.png"));
//					canvas.getScene().setLinesRO(true);
//				}
//				canvas.getScene().setRegionsRO(false);
//				canvas.getScene().setWordsRO(false);
//				canvas.getScene().setShowReadingOrderForShapes();
//				break;
//			case 2:
//				if (canvas.getScene().isWordsRO()){
//					ui.getShowReadingOrderToolItem().ti.setImage(Images.getOrLoad("/icons/reading_order_w_hide.png"));
//					canvas.getScene().setWordsRO(false);
//				}
//				else{
//					ui.getShowReadingOrderToolItem().ti.setImage(Images.getOrLoad("/icons/reading_order_w.png"));
//					canvas.getScene().setWordsRO(true);
//				}
//				canvas.getScene().setRegionsRO(false);
//				canvas.getScene().setLinesRO(false);
//				canvas.getScene().setShowReadingOrderForShapes();
//				break;
//			case 3:
//				if (canvas.getScene().isAllRO()){
//					ui.getShowReadingOrderToolItem().ti.setImage(Images.getOrLoad("/icons/readingOrder_hide.png"));
//					canvas.getScene().setAllRO(false);
//					canvas.getScene().setRegionsRO(false);
//					canvas.getScene().setLinesRO(false);
//					canvas.getScene().setWordsRO(false);
//				}
//				else{
//					ui.getShowReadingOrderToolItem().ti.setImage(Images.getOrLoad("/icons/readingOrder.png"));
//					canvas.getScene().setAllRO(true);
//					canvas.getScene().setRegionsRO(true);
//					canvas.getScene().setLinesRO(true);
//					canvas.getScene().setWordsRO(true);
//				}
//				canvas.getScene().setShowReadingOrderForShapes();
//				break;
//			}
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
		else if (s == ui.getDeletePageButton()) {
			mainWidget.deletePage();
		}
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
		else if (s == ui.getVkeyboards()) {
			Character c = (char) e.detail;
			logger.debug("key pressed: "+c+", name: "+e.text);
			
			ATranscriptionWidget tw = ui.getSelectedTranscriptionWidget();
			if (tw != null) {
				tw.insertTextIfFocused(""+c);
			}
		}
		
		else if (s == ui.getLoadTranscriptInTextEditor()) {
			logger.debug("loading transcript source");
			if (Storage.getInstance().isPageLoaded()) {
				URL url = Storage.getInstance().getTranscriptMetadata().getUrl();
				
				try {
					if (true) {
						XmlViewer xmlviewer = new XmlViewer(ui.getShell(), SWT.MODELESS);
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
