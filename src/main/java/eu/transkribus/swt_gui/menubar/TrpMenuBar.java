package eu.transkribus.swt_gui.menubar;

import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.databinding.DataBinder;
import eu.transkribus.swt_gui.Msgs;
import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.canvas.CanvasSettings;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidgetView;
import eu.transkribus.swt_gui.mainwidget.TrpSettings;

public class TrpMenuBar {
	private Menu menuBar;
	
	TrpMainWidgetView mainWidgetView;
	
	// file menu:
	MenuItem fileMenuItem;
	Menu fileMenu;
	MenuItem saveTranscriptionToNewFileMenuItem;
	MenuItem saveTranscriptionMenuItem;
	MenuItem openMenuItem;
	MenuItem uploadServerDocsItem;

	MenuItem openLocalPageFileItem;
	MenuItem uploadImagesFromPdfFileItem;
	MenuItem manageCollectionsMenuItem;
	MenuItem syncWordsWithLinesMenuItem;
	MenuItem proxySettingsMenuItem;
	MenuItem createThumbsMenuItem;
	
	MenuItem deletePageMenuItem;
	
//	Menu testMenu;
	MenuItem testMenuItem;
	MenuItem sortBaselinePtsItem;
	
	Menu languageMenu;
	MenuItem languageMenuItem;
	
	// view menu:
	Menu viewMenu;
	MenuItem viewMenuItem;
	MenuItem viewSettingsMenuItem;
//	MenuItem showLeftViewMenuItem;
//	MenuItem showRightViewMenuItem;
//	MenuItem showBottomViewMenuItem;
	
	// segmentation menu:
	Menu segmentationMenu;
	MenuItem segmentationMenuItem;
	MenuItem showAllMenuItem;
	MenuItem hideAllMenuItem;
	MenuItem showPrintspaceMenuItem;
	MenuItem showRegionsMenuItem;
	MenuItem showLinesMenuItem;
	MenuItem showBaselinesMenuItem;
	MenuItem showWordsMenuItem;

	
//	private DeaSWTCanvasWidget deaSWTCanvasUI;
//	Menu helpMenu;
//	MenuItem mntmhelp;
	MenuItem aboutMenuIItem;
	MenuItem updateMenuItem;
	
	MenuItem loadTestsetMenuItem;
	
	TrpSettings viewSets;
	CanvasSettings canvasSets;

	MenuItem analyzeStructureItem;
	MenuItem installMenuItem;
	MenuItem tipsOfTheDayMenuItem;
	MenuItem bugReportItem;

	

	public TrpMenuBar(TrpMainWidgetView mainWidgetView) {
//		menuBar = new Menu(mainWidgetView.getShell(), SWT.BAR);
		menuBar = new Menu(mainWidgetView.getShell(), SWT.POP_UP);
		
//		super(mainWidgetView, SWT.BAR);
		this.mainWidgetView = mainWidgetView;
		this.viewSets = mainWidgetView.getTrpSets();
		
		if (mainWidgetView.getCanvas()!=null)
			this.canvasSets = mainWidgetView.getCanvas().getSettings();
		else
			this.canvasSets = new CanvasSettings();
		
		initMenu();
	}
	
//	@Override
	// Must be overridden in order to extend Menu. By default this method would throw an exception!
//	public void checkSubclass() { return; }
	
	private void initMenu() {
//		Menu menuBar = new Menu(this, SWT.BAR);
//		this.mainWidgetView.getShell().setMenuBar(menuBar);
//		setMenuBar(menuBar);
		
		// FILE MENU:
		fileMenuItem = new MenuItem(menuBar, SWT.CASCADE);
		fileMenuItem.setText("&File");
		
		fileMenu = new Menu(fileMenuItem);
		fileMenuItem.setMenu(fileMenu);
		
		saveTranscriptionToNewFileMenuItem = new MenuItem(fileMenu, SWT.NONE);
		saveTranscriptionToNewFileMenuItem.setImage(Images.getOrLoad("/icons/page_save.png"));
//		saveTranscriptionToNewFileMenuItem.setImage(Images.getOrLoad("/icons/scissor_200.png"));
		saveTranscriptionToNewFileMenuItem.setText("&Save transcription to a new file");
		
		saveTranscriptionMenuItem = new MenuItem(fileMenu, SWT.NONE);
		saveTranscriptionMenuItem.setImage(Images.getOrLoad("/icons/disk.png"));
		saveTranscriptionMenuItem.setText("&Save transcription");
		
		uploadServerDocsItem = new MenuItem(fileMenu, SWT.NONE);
		uploadServerDocsItem.setImage(Images.getOrLoad("/icons/folder_add.png"));
		uploadServerDocsItem.setText("Upload documents");
		
		openMenuItem = new MenuItem(fileMenu, SWT.NONE);
		openMenuItem.setImage(Images.getOrLoad("/icons/folder.png"));
		openMenuItem.setText("&Open local folder");
		
		openLocalPageFileItem = new MenuItem(fileMenu, 0);
		openLocalPageFileItem.setImage(null);
		openLocalPageFileItem.setText("Open local page file for current page");
		
		deletePageMenuItem = new MenuItem(fileMenu, 0);
		deletePageMenuItem.setImage(null);
		deletePageMenuItem.setText("Delete current page from server");
		
		createThumbsMenuItem = new MenuItem(fileMenu, SWT.CHECK);
		createThumbsMenuItem.setImage(null);
		createThumbsMenuItem.setText("Create thumbs when opening local folder");
			
		uploadImagesFromPdfFileItem = new MenuItem(fileMenu, 0);
		uploadImagesFromPdfFileItem.setText("Upload images from pdf file");
		
		syncWordsWithLinesMenuItem = new MenuItem(fileMenu, SWT.NONE);
		syncWordsWithLinesMenuItem.setText("Sync word transcription with text in lines");
		
		loadTestsetMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		loadTestsetMenuItem.setText("Load &testset");
		
//		testMenuItem = new MenuItem(fileMenu, SWT.CASCADE);
//		sortBaselinePtsItem = new MenuItem(testMenuItem, 0);
//		sortBaselinePtsItem.setText("Sort baseline pts of selected );
		
		
		
		// VIEW MENU:
//		viewMenuItem = new MenuItem(menuBar, SWT.CASCADE);
//		viewMenuItem.setText("&View");
//		viewMenu = new Menu(viewMenuItem);
//		viewMenuItem.setMenu(viewMenu);
		
//		showLeftViewMenuItem = new MenuItem(viewMenu, SWT.CHECK);
//		showLeftViewMenuItem.setText("Show left view");
//		showLeftViewMenuItem.setSelection(true);
//		
//		showRightViewMenuItem = new MenuItem(viewMenu, SWT.CHECK);
//		showRightViewMenuItem.setText("Show right view");
//		showRightViewMenuItem.setSelection(true);		
//		
//		showBottomViewMenuItem = new MenuItem(viewMenu, SWT.CHECK);
//		showBottomViewMenuItem.setText("Show bottom view");
//		showBottomViewMenuItem.setSelection(true);
		
		// SEGMENTATION MENU:
		segmentationMenuItem = new MenuItem(menuBar, SWT.CASCADE);
		segmentationMenuItem.setText("&Segmentation");
		segmentationMenu = new Menu(segmentationMenuItem);
		segmentationMenuItem.setMenu(segmentationMenu);
		
		showAllMenuItem = new MenuItem(segmentationMenu, SWT.PUSH);
		showAllMenuItem.setText("Show all");
		
		hideAllMenuItem = new MenuItem(segmentationMenu, SWT.PUSH);
		hideAllMenuItem.setText("Hide all");		
		
		showPrintspaceMenuItem = new MenuItem(segmentationMenu, SWT.CHECK);
		showPrintspaceMenuItem.setText("Show printspace");
		
		showRegionsMenuItem = new MenuItem(segmentationMenu, SWT.CHECK);
		showRegionsMenuItem.setText("Show text regions");
		showLinesMenuItem = new MenuItem(segmentationMenu, SWT.CHECK);
		showLinesMenuItem.setText("Show lines");
		showBaselinesMenuItem = new MenuItem(segmentationMenu, SWT.CHECK);
		showBaselinesMenuItem.setText("Show baselines");
	
		showWordsMenuItem = new MenuItem(segmentationMenu, SWT.CHECK);
		showWordsMenuItem.setText("Show words");	
		
		// HELP MENU:
//		mntmhelp = new MenuItem(menuBar, SWT.CASCADE);
//		mntmhelp.setText("&Help");
//		
//		helpMenu = new Menu(mntmhelp);
//		mntmhelp.setMenu(helpMenu);
		
		analyzeStructureItem = new MenuItem(menuBar, SWT.PUSH);
		analyzeStructureItem.setText("Analyze structure for this page");
		
		viewSettingsMenuItem = new MenuItem(menuBar, SWT.PUSH);
		viewSettingsMenuItem.setText("Change &viewing settings...");
		viewSettingsMenuItem.setImage(Images.getOrLoad("/icons/palette.png"));
		
		proxySettingsMenuItem = new MenuItem(menuBar, SWT.PUSH);
		proxySettingsMenuItem.setText("&Proxy settings...");
		proxySettingsMenuItem.setImage(Images.getOrLoad("/icons/server_connect.png"));
		
		if (false) {
		languageMenuItem = new MenuItem(menuBar, SWT.CASCADE);
		languageMenuItem.setText("&Language (todo...)");
		languageMenuItem.setImage(Images.getOrLoad("/icons/server_connect.png"));
		
		languageMenu = new Menu(languageMenuItem);
		languageMenuItem.setMenu(languageMenu);		
		
		for (Locale l : Msgs.LOCALES) {
			MenuItem li = new MenuItem(languageMenu, SWT.RADIO);
			li.setText(l.getDisplayName());
			li.setData(l);
			if (l.equals(TrpConfig.getTrpSettings().getLocale()))
				li.setSelection(true);
		}
		}
		
		manageCollectionsMenuItem = new MenuItem(menuBar, SWT.NONE);
		manageCollectionsMenuItem.setText("Manage collections...");
		
		tipsOfTheDayMenuItem = new MenuItem(menuBar, SWT.PUSH);
		tipsOfTheDayMenuItem.setText("Show &tips of the day...");
		
		updateMenuItem = new MenuItem(menuBar, SWT.NONE);
		updateMenuItem.setText("Check for &updates...");
		updateMenuItem.setImage(Images.getOrLoad("/icons/update_wiz.gif"));	
		
		installMenuItem = new MenuItem(menuBar, SWT.NONE);
		installMenuItem.setText("&Install a specific version...");
		installMenuItem.setImage(Images.getOrLoad("/icons/install_wiz.gif"));
		
		bugReportItem = new MenuItem(menuBar, SWT.NONE);
		bugReportItem.setText("Send a bug report or feature request");
		bugReportItem.setImage(Images.BUG);
		
		aboutMenuIItem = new MenuItem(menuBar, SWT.NONE);
		aboutMenuIItem.setText("&About");
		aboutMenuIItem.setImage(Images.getOrLoad("/icons/information.png"));
		
		updatMenuFromSettings();
		addBindings();
	}
	
	private void updatMenuFromSettings() {
		// viewing sets:
//		showLeftViewMenuItem.setSelection(viewSets.isShowLeftView());
//		showRightViewMenuItem.setSelection(viewSets.isShowRightView());
//		showBottomViewMenuItem.setSelection(viewSets.isShowLeftView());
		showPrintspaceMenuItem.setSelection(viewSets.isShowPrintSpace());
		showRegionsMenuItem.setSelection(viewSets.isShowTextRegions());
		showLinesMenuItem.setSelection(viewSets.isShowLines());
		showBaselinesMenuItem.setSelection(viewSets.isShowBaselines());
		showWordsMenuItem.setSelection(viewSets.isShowWords());
	}
	
	private void addBindings() {
		// bind viewsets:
//		DataBinder.get().bindBeanToWidgetSelection(TrpSettings.SHOW_LEFT_VIEW_PROPERTY, viewSets, showLeftViewMenuItem);
//		DataBinder.get().bindBeanToWidgetSelection(TrpSettings.SHOW_RIGHT_VIEW_PROPERTY, viewSets, showRightViewMenuItem);
		DataBinder.get().bindBeanToWidgetSelection(TrpSettings.SHOW_PRINTSPACE_PROPERTY, viewSets, showPrintspaceMenuItem);
		DataBinder.get().bindBeanToWidgetSelection(TrpSettings.SHOW_TEXT_REGIONS_PROPERTY, viewSets, showRegionsMenuItem);
		DataBinder.get().bindBeanToWidgetSelection(TrpSettings.SHOW_LINES_PROPERTY, viewSets, showLinesMenuItem);
		DataBinder.get().bindBeanToWidgetSelection(TrpSettings.SHOW_BASELINES_PROPERTY, viewSets, showBaselinesMenuItem);
		DataBinder.get().bindBeanToWidgetSelection(TrpSettings.SHOW_WORDS_PROPERTY, viewSets, showWordsMenuItem);
		DataBinder.get().bindBeanToWidgetSelection(TrpSettings.CREATE_THUMBS_PROPERTY, viewSets, createThumbsMenuItem);
	}
	
//	public MenuItem getDrawSelectedCornerNumbersMenuItem() {
//		return drawSelectedCornerNumbersMenuItem;
//	}
	
//	public MenuItem getShowLeftViewMenuItem() { 
//		return this.showLeftViewMenuItem;
//	}
//	
//	public MenuItem getShowBottomViewMenuItem() { 
//		return this.showBottomViewMenuItem;
//	}	

	public Menu getViewMenu() {
		return viewMenu;
	}

	public MenuItem getViewMenuItem() {
		return viewMenuItem;
	}
	
//	public MenuItem getViewSettingsMenuItem() {
//		return this.viewSettingsMenuItem;
//	}
	
	public MenuItem getAnalyzeStructureItem() {
		return analyzeStructureItem;
	}
		
	public Menu getMenuBar() { return menuBar; }

	public TrpMainWidgetView getMainWidgetView() {
		return mainWidgetView;
	}

	public MenuItem getFileMenuItem() {
		return fileMenuItem;
	}

	public Menu getFileMenu() {
		return fileMenu;
	}

	public MenuItem getOpenMenuItem() {
		return openMenuItem;
	}
	
	public MenuItem getUploadServerDocsItem() {
		return uploadServerDocsItem;
	}
	
	public MenuItem getOpenLocalPageFileItem() {
		return openLocalPageFileItem;
	}
	
	public MenuItem getUploadImagesFromPdfFileItem() {
		return uploadImagesFromPdfFileItem;
	}
	
	public MenuItem getManageCollectionsMenuItem() {
		return manageCollectionsMenuItem;
	}
	
	public MenuItem getSyncWordsWithLinesMenuItem() {
		return syncWordsWithLinesMenuItem;
	}
	
	public MenuItem getSaveTranscriptionToNewFileMenuItem() {
		return saveTranscriptionToNewFileMenuItem;
	}
	
	public MenuItem getSaveTranscriptionMenuItem() {
		return saveTranscriptionMenuItem;
	}

	public Menu getSegmentationMenu() {
		return segmentationMenu;
	}

	public MenuItem getSegmentationMenuItem() {
		return segmentationMenuItem;
	}
	
	public MenuItem getShowAllMenuItem() { return showAllMenuItem; }
	public MenuItem getHideAllMenuItem() { return hideAllMenuItem; }

//	public MenuItem getMntmhelp() {
//		return mntmhelp;
//	}
//
//	public Menu getHelpMenu() {
//		return helpMenu;
//	}

	public MenuItem getAboutMenuItem() {
		return aboutMenuIItem;
	}
	
	public MenuItem getUpdateMenuItem() { return updateMenuItem; }
	public MenuItem getInstallMenuItem() { return installMenuItem; }
	public MenuItem getTipsOfTheDayMenuItem() { return tipsOfTheDayMenuItem; }
	public MenuItem getBugReportItem() { return bugReportItem; }

	public MenuItem getLoadTestsetMenuItem() {
		return loadTestsetMenuItem;
	}

	public MenuItem getDeletePageMenuItem() {
		return deletePageMenuItem;
	}

	public void setDeletePageMenuItem(MenuItem deletePageMenuItem) {
		this.deletePageMenuItem = deletePageMenuItem;
	}

//	public void showAll(boolean value) {
//		this.viewSets.setShowAll(value);
//	}

}
