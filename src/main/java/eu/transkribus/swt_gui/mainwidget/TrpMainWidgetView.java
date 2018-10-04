package eu.transkribus.swt_gui.mainwidget;

import java.util.HashMap;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.TextToolItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.auth.TrpRole;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.model.beans.enums.TranscriptionLevel;
import eu.transkribus.core.util.EnumUtils;
import eu.transkribus.swt.pagingtoolbar.PagingToolBar;
import eu.transkribus.swt.portal.PortalWidget;
import eu.transkribus.swt.portal.PortalWidget.Docking;
import eu.transkribus.swt.portal.PortalWidget.Position;
import eu.transkribus.swt.portal.PortalWidget.PositionDocking;
import eu.transkribus.swt.util.DropDownToolItem;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.ThumbnailManagerVirtual;
import eu.transkribus.swt.util.ThumbnailWidget;
import eu.transkribus.swt.util.ThumbnailWidgetVirtual;
import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.canvas.CanvasMode;
import eu.transkribus.swt_gui.canvas.CanvasWidget;
import eu.transkribus.swt_gui.canvas.SWTCanvas;
import eu.transkribus.swt_gui.canvas.shapes.CanvasShapeType;
import eu.transkribus.swt_gui.comments_widget.CommentsWidget;
import eu.transkribus.swt_gui.doc_overview.DocInfoWidget;
import eu.transkribus.swt_gui.doc_overview.DocMetadataEditor;
import eu.transkribus.swt_gui.doc_overview.ServerWidget;
import eu.transkribus.swt_gui.mainwidget.menubar.TrpMenuBar;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.metadata.StructuralMetadataWidget;
import eu.transkribus.swt_gui.metadata.TaggingWidget;
import eu.transkribus.swt_gui.metadata.TextStyleTypeWidget;
import eu.transkribus.swt_gui.structure_tree.StructureTreeWidget;
import eu.transkribus.swt_gui.tools.ToolsWidget;
import eu.transkribus.swt_gui.transcription.ATranscriptionWidget;
import eu.transkribus.swt_gui.transcription.LineTranscriptionWidget;
import eu.transkribus.swt_gui.transcription.WordTranscriptionWidget;
import eu.transkribus.swt_gui.util.DropDownToolItemSimple;

public class TrpMainWidgetView extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(TrpMainWidgetView.class);
		
	public final String APP_NAME;
	public final String HELP_TEXT;

	// ##### Widgets and other stuff: #####
	CanvasWidget canvasWidget;
	PagingToolBar pagesPagingToolBar;
	GridData toolBarGridData;
	LineTranscriptionWidget lineTranscriptionWidget;
	WordTranscriptionWidget wordTranscriptionWidget;
	StructureTreeWidget structureTreeWidget;
	ServerWidget serverWidget;
	DocInfoWidget docInfoWidget;
	DocMetadataEditor docMetadataEditor;
	
//	JobTableWidgetPagination jobOverviewWidget;
//	TranscriptsTableWidgetPagination versionsWidget;
	StructuralMetadataWidget structuralMdWidget;
	TextStyleTypeWidget textStyleWidget;
	
//	public static boolean SHOW_NEW_TW = true;
	TaggingWidget taggingWidget;
	ToolsWidget toolsWidget;
	CommentsWidget commentsWidget;
	//ThumbnailWidget thumbnailWidget;

	TrpSettings trpSets;
	PortalWidget portalWidget;
	TrpMenuBar menu;
	ToolBar toolBar;
	// ##########
	
	// ##### Toolbar stuff: #####
	ToolItem menuButton, /*loginToggle,*/ reloadDocumentButton, exportDocumentButton, openLocalFolderButton, closeDocBtn;
	ToolItem uploadDocsItem, searchBtn;

	DropDownToolItem saveDrowDown;
	MenuItem saveTranscriptMenuItem, saveTranscriptWithMessageMenuItem;
	
	ToolItem saveTranscriptToolItem, saveTranscriptWithMessageToolItem;
	
	DropDownToolItemSimple saveOptionsToolItem;
	Menu autoSaveListMenu;
	MenuItem autoSaveListMenuItem, autoSaveSettingsMenuItem;
	
	MenuItem createDefaultLineItem;
	MenuItem createImageSizeTextRegionItem;
	
	
	ToolItem versionsButton;
	ToolItem jobsButton;
	
//	DropDownToolItem visibilityItem;
//	MenuItem showRegionsItem;
//	MenuItem showLinesItem;
//	MenuItem showBaselinesItem;
//	MenuItem showWordsItem;
//	MenuItem showPrintspaceItem;
//	MenuItem renderBlackeningsItem;
//	MenuItem showReadingOrderRegionsMenuItem;
//	MenuItem showReadingOrderLinesMenuItem;
//	MenuItem showReadingOrderWordsMenuItem;	
		
	// dock state buttons
//	DropDownToolItem viewLeftDockingDropItem, viewBottomDockingDropitem;
//	DropDownToolItem viewDockingDropItem;
	
	HashMap<PositionDocking, MenuItem>  dockingMenuItems = new HashMap<>();
	
	DropDownToolItem profilesToolItem;
	
	ToolItem showLineEditorToggle;
	ToolItem loadTranscriptInTextEditor;
//	ToolItem helpItem;
	ToolItem bugReportItem;
	
	Combo statusCombo;
	
	//	****Search integration stuff****
	ToolItem quickSearchButton;
	TextToolItem quickSearchText;
//	********************************
	
	// ##########
		
	TrpTabWidget tabWidget;
	Composite transcriptionWidgetContainer;

	private TrpMainWidget mainWidget;

	public TrpMainWidgetView(Composite parent, TrpMainWidget mainWidget) {
		super(parent, SWT.NONE);
		getShell().setMinimumSize(new Point(0, 0));
		APP_NAME = mainWidget.NAME+" v"+mainWidget.VERSION+" ("+mainWidget.info.getTimestampString()+")";
		HELP_TEXT = mainWidget.info.getHelptext();
				
		init(mainWidget);
		initSize();
	}
	
//	public CTabFolder getRightTabFolder() { return rightTabFolder; }
	
	private void initSize() {
		Rectangle b = getShell().getDisplay().getPrimaryMonitor().getBounds();
		
		float frac = 0.90f;
		
		getShell().setSize((int)(b.width*frac), (int)(b.height*frac));
		center();
	}
	
	public TreeViewer getStructureTreeViewer() { return structureTreeWidget.getTreeViewer(); }
	
	private void init(TrpMainWidget mainWidget) {
		this.mainWidget = mainWidget;
		trpSets = TrpConfig.getTrpSettings();
		
//		initSettings();
		
//		progressDialog = new ProgressMonitorDialog(getShell());
		
		setToolTipText("An interactive adaptive transcription platform");
		getShell().setText(APP_NAME);
		//getShell().setImage(Images.getOrLoad("/icons/pencil.png"));
		getShell().setImage(Images.getOrLoad("/icons/ticon6.png"));
//		getShell().setImage(Images.getOrLoad("/wolpertinger_small_64.png"));
//		setSize(1200, 850);
//		setLayout(new FillLayout());
		setLayout(new GridLayout(2, false));
		
//		setBackground(Colors.getSystemColor(SWT.COLOR_BLUE));
		
//		menu = new TrpMenuBar(this); // currently used when clicked on "burger button"
		menu = new TrpMenuBar(getShell()); // currently used when clicked on "burger button"
		menu.getMenuBar().addMenuListener(new MenuListener() {
			@Override
			public void menuShown(MenuEvent e) {
				menuButton.setSelection(true);
			}
			
			@Override
			public void menuHidden(MenuEvent e) {
				menuButton.setSelection(false);
			}
		});
		
		
//		getShell().setMenuBar(menu.getMenuBar());

		initToolBar();

		canvasWidget = new CanvasWidget(SWTUtil.dummyShell, SWT.NONE, getPagesPagingToolBar().getToolBar(), this);
		
		initToolBar2();

		// NEW: only one tab widget
		tabWidget = new TrpTabWidget(this, 0);
		
//		serverDocsWidget = new ServerDocsWidget(tabWidget.serverTf);
//		tabWidget.docListItem.setControl(serverDocsWidget);
		
		serverWidget = new ServerWidget(tabWidget.mainTf);
		tabWidget.serverItem.setControl(serverWidget);
		
//		docInfoWidget = new DocInfoWidget(tabWidget.documentTf, 0);
//		tabWidget.docoverviewItem.setControl(docInfoWidget);
		docInfoWidget = new DocInfoWidget(tabWidget.mainTf, 0, mainWidget);
		tabWidget.documentItem.setControl(docInfoWidget);
		
		structureTreeWidget = new StructureTreeWidget(tabWidget.mainTf);
		tabWidget.structureItem.setControl(structureTreeWidget);
		
//		versionsWidget = new TranscriptsTableWidgetPagination(tabWidget.documentTf, SWT.NONE, 25);
//		tabWidget.versionsItem.setControl(versionsWidget);
//		versionsWidget = new TranscriptsTableWidgetPagination(SWTUtil.dummyShell, SWT.NONE, 25);
//		tabWidget.versionsItem.setControl(versionsWidget);		
		
//		thumbnailWidget = new ThumbnailWidget(tabWidget.documentTf, SWT.NONE);
//		tabWidget.thumbnailItem.setControl(thumbnailWidget);
					
//		jobOverviewWidget = new JobTableWidgetPagination(tabWidget.serverTf, SWT.NONE, 50);
//		tabWidget.jobsItem.setControl(jobOverviewWidget);
//		jobOverviewWidget = new JobTableWidgetPagination(SWTUtil.dummyShell, SWT.NONE, 50);
//		tabWidget.jobsItem.setControl(jobOverviewWidget);
		
		docMetadataEditor = new DocMetadataEditor(tabWidget.metadataTf, 0);
		tabWidget.docMdItem.setControl(docMetadataEditor);

		structuralMdWidget = new StructuralMetadataWidget(tabWidget.metadataTf, SWT.TOP);
		tabWidget.structuralMdItem.setControl(structuralMdWidget);
		
		taggingWidget = new TaggingWidget(tabWidget.metadataTf, SWT.TOP);
		taggingWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tabWidget.textTaggingItem.setControl(taggingWidget);
		
		commentsWidget = new CommentsWidget(tabWidget.metadataTf, SWT.TOP);
		tabWidget.commentsItem.setControl(commentsWidget);
		
		if (tabWidget.textStyleMdItem != null) { // outdated and removed
		textStyleWidget = new TextStyleTypeWidget(tabWidget.metadataTf, SWT.TOP);
		tabWidget.textStyleMdItem.setControl(textStyleWidget);
		}
		
		toolsWidget = new ToolsWidget(tabWidget.mainTf, SWT.TOP);
		toolsWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tabWidget.toolsItem.setControl(toolsWidget);
			
		// the bottom widget (transcription):
		if (true) {
//			transcriptionWidgetContainer = new TabFolder(SWTUtil.dummyShell, SWT.NONE);
		transcriptionWidgetContainer = new Composite(SWTUtil.dummyShell, SWT.NONE);
		GridLayout gl = new GridLayout(1, true);
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		transcriptionWidgetContainer.setLayout(gl);
		
//		transcriptionWidgetContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		lineTranscriptionWidget = new LineTranscriptionWidget(SWTUtil.dummyShell, SWT.NONE, trpSets, this);
		lineTranscriptionWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		wordTranscriptionWidget = new WordTranscriptionWidget(SWTUtil.dummyShell, SWT.NONE, trpSets, this);
		wordTranscriptionWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				
//		lineTranscriptionItem = createTabItem(transcriptionTabFolder, lineTranscriptionWidget, "Line based correction");
//		wordTranscriptionItem = createTabItem(transcriptionTabFolder, wordTranscriptionWidget, "Word based correction");
		}
		changeToTranscriptionWidget(TranscriptionLevel.LINE_BASED);
		
//		Composite child = new Composite(SWTUtil.dummyShell, SWT.NONE);
//	    child.setLayout(new FillLayout());
//
//	    // Create the buttons
//	    new Button(child, SWT.PUSH).setText("One");
//	    new Button(child, SWT.PUSH).setText("Two");
				
		// init portal widget:
//		portalWidget = new PortalWidget(this, SWT.NONE, null, canvasWidget, tabWidget, transcriptionWidgetContainer, rightTabFolder);
		portalWidget = new PortalWidget(this, SWT.NONE, null, canvasWidget, tabWidget, transcriptionWidgetContainer, null);
		portalWidget.setMinWidth(Position.LEFT, 200);
		portalWidget.setMinWidth(Position.CENTER, 400);
		portalWidget.setMinWidth(Position.BOTTOM, 400);
		// if 'selection' event in PortalWidget, then some dock status has changed -> adjust button selection!
		portalWidget.addListener(SWT.Selection, new Listener() {
			@Override public void handleEvent(Event event) {
				updateDockingStateButtons();
			}
		});			
		
		
//		portalWidget.setMinWidth(Position.RIGHT, 300);

//		portalWidget.setMinHeight(Position.RIGHT, rightTabFolder.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		
		logger.debug("left view docking state: "+getTrpSets().getLeftViewDockingState());
		
		portalWidget.setWidgetDockingType(Position.LEFT, getTrpSets().getLeftViewDockingState());
//		portalWidget.setWidgetDockingType(Position.RIGHT, getTrpSets().getRightViewDockingState());
		portalWidget.setWidgetDockingType(Position.BOTTOM, getTrpSets().getBottomViewDockingState());
		
//		addInternalListener();
//		addBindings();
//		canvasWidget.getToolbar().addBindings(getTrpSets());
		updateLoginInfo(false, "", "");
		updateDockingStateButtons();
		
//		canvasWidget.getToolbar().setParent(this);
		
//		for (ToolItem ti : canvasWidget.getToolbar().getItems()) {
//			ti.set
			
//		}
		
//		canvasWidget.getToolBar().setParent(parent)
		
		pack();
	}
	
	private void updateDockingStateButtons() {
		logger.debug("updateDockingStateButtons");
		for (MenuItem mi : dockingMenuItems.values()) {
			PositionDocking pd = (PositionDocking) mi.getData();
			
			Docking currentDocking = portalWidget.getDocking(pd.pos);
			mi.setSelection(pd.docking.equals(currentDocking)); // set selection depending on docking state!
		}
	}
	
	public void changeToTranscriptionWidget(TranscriptionLevel type) {
		logger.debug("changing to tr-widget: "+type);
		boolean changed=false;
		
		if (type == TranscriptionLevel.LINE_BASED) {
			changed=true;
			lineTranscriptionWidget.getTranscriptionTypeLineBasedItem().setSelection(true);
			lineTranscriptionWidget.getTranscriptionTypeWordBasedItem().setSelection(false);
//			lineTranscriptionWidget.getTranscriptionTypeItem().selectItem(0, false);
			
			lineTranscriptionWidget.setParent(transcriptionWidgetContainer);
			wordTranscriptionWidget.setParent(SWTUtil.dummyShell);
			
//			lineTranscriptionWidget.updateToolbarSize();
		} else if (type == TranscriptionLevel.WORD_BASED) {
			changed=true;
			wordTranscriptionWidget.getTranscriptionTypeLineBasedItem().setSelection(false);
			wordTranscriptionWidget.getTranscriptionTypeWordBasedItem().setSelection(true);
//			wordTranscriptionWidget.getTranscriptionTypeItem().selectItem(1, false);
			
			lineTranscriptionWidget.setParent(SWTUtil.dummyShell);
			wordTranscriptionWidget.setParent(transcriptionWidgetContainer);
			
//			wordTranscriptionWidget.updateToolbarSize();
		}
		
		if (changed) {
//			lineTranscriptionWidget.pack();
//			wordTranscriptionWidget.pack();
			transcriptionWidgetContainer.layout(true);
			mainWidget.updateSelectedTranscriptionWidgetData();
		}
		
	}
		
	public void selectLeftTab(int idx) {
		// TODO: adapt for new tab widget TrpTabWidget
//		if (idx < 0 || idx >= leftTabFolder.getItemCount()){
//			idx = 0;
//		}
//		leftTabFolder.setSelection(idx);
//		if (leftTabFolder.getSelection().equals(thumbnailItem)){
//			thumbnailWidget.reload();
//		}
	}

	public void selectRightTab(int idx) {
		// TODO: remove in future versions as no right tab area exists anymore
//		if (idx < 0 || idx >= rightTabFolder.getItemCount()){
//			idx = 0;
//		}
//		rightTabFolder.setSelection(idx);
	}

//	private void initSettings() {
//		trpSets = new TrpSettings();
//		TrpConfig.registerBean(trpSets, true);
//	}

	/**
	 * Initialize toolbar and add toolbar buttons that come *before* the canvas toolbar
	 */
	private void initToolBar() {
		toolBar = new ToolBar(this, /*SWT.FLAT |*/ SWT.WRAP | SWT.RIGHT);
		toolBarGridData = new GridData(SWT.FILL, SWT.TOP, true, true);
						
		// open menu button:
		menuButton = new ToolItem(toolBar, SWT.CHECK);
//		menuButton.setImage(Images.getOrLoad("/icons/house.png"));
		menuButton.setImage(Images.BURGER);
		menuButton.setText("");
		menuButton.setToolTipText("Main Menu");
		menuButton.addSelectionListener(new SelectionAdapter() {
	        @Override
	        public void widgetSelected(SelectionEvent e) {
	                Point point = toolBar.toDisplay(new Point(menuButton.getBounds().x, menuButton.getBounds().y+menuButton.getBounds().height));
	                menu.getMenuBar().setLocation(point.x, point.y);
	                menu.getMenuBar().setVisible(true);
	        }
		});
		
//		menu.getMenuBar().addMenuListener(new org.eclipse.swt.events.MenuListener() {
//			@Override public void menuShown(MenuEvent e) {
//				menuButton.setSelection(true);
//			}
//			@Override public void menuHidden(MenuEvent e) {
//				menuButton.setSelection(false);
//			}
//		});
		
		/*
		loginToggle = new ToolItem(toolBar, SWT.PUSH, preInsertIndex++);
		loginToggle.setToolTipText("Login");
		loginToggle.setImage(Images.getOrLoad("/icons/disconnect.png"));
		*/
		
//		ToolItem leftViewButton = new ToolItem(toolBar, SWT.CHECK);
//		leftViewButton.setImage(Images.getOrLoad("/icons/three_dots_16.png"));
////		leftViewButton.setText("yeah!");
//		leftViewButton.setToolTipText("Show / hide menu view");
//		leftViewButton.setSelection(trpSets.getLeftViewDockingState()==Docking.DOCKED);
//		SWTUtil.onSelectionEvent(leftViewButton, e -> {
//			if (!leftViewButton.getSelection()) {
//				portalWidget.setWidgetDockingType(Position.LEFT, Docking.INVISIBLE);
//			} else {
//				portalWidget.setWidgetDockingType(Position.LEFT, Docking.DOCKED);
//			}
//		});
		
//		initDockingToolItems();
		initDockingToolItems2();

		profilesToolItem = new DropDownToolItem(toolBar, false, false, true, SWT.NONE);
		profilesToolItem.ti.setImage(Images.CONTROL_EQUALIZER);
		profilesToolItem.ti.setToolTipText("Profiles");
		updateProfiles(true);
		
		new ToolItem(toolBar, SWT.SEPARATOR);
		
		openLocalFolderButton = new ToolItem(toolBar, SWT.PUSH);
		openLocalFolderButton.setToolTipText("Open local document");
		openLocalFolderButton.setImage(Images.FOLDER);
		
		uploadDocsItem = new ToolItem(toolBar, SWT.PUSH);
		uploadDocsItem.setToolTipText("Import document(s)");
//		uploadFromPrivateFtpItem.setImage(Images.getOrLoad("/icons/weather_clouds.png"));
		uploadDocsItem.setImage(Images.FOLDER_IMPORT);
		
		exportDocumentButton = new ToolItem(toolBar, SWT.PUSH);
		exportDocumentButton.setToolTipText("Export document");
		exportDocumentButton.setImage(Images.FOLDER_GO);
		exportDocumentButton.setEnabled(false);
		
		reloadDocumentButton = new ToolItem(toolBar, SWT.PUSH);
		reloadDocumentButton.setToolTipText("Reload document");
		reloadDocumentButton.setImage(Images.REFRESH);
		reloadDocumentButton.setEnabled(false);
								
		searchBtn = new ToolItem(toolBar, SWT.PUSH);
		searchBtn.setToolTipText("Search for documents, keywords etc.");
		searchBtn.setImage(Images.getOrLoad("/icons/find.png"));
		
		jobsButton = new ToolItem(toolBar, SWT.PUSH);
		jobsButton.setToolTipText("Show jobs");
		jobsButton.setImage(Images.CUP);
		jobsButton.setEnabled(false);
		
		// *** quicksearch
		ToolItem offset = new ToolItem(toolBar, SWT.SEPARATOR);
		//offset.setWidth(20);
		
		quickSearchText = new TextToolItem(toolBar, SWT.NONE);
		quickSearchText.setAutoSelectTextOnFocus();
		quickSearchText.setMessage("Search current document...");
		quickSearchText.resizeToMessage();
		
		quickSearchButton = new ToolItem(toolBar, SWT.NONE);
		quickSearchButton.setImage(Images.getOrLoad("/icons/quickfind.png"));
		// ****
		
		new ToolItem(toolBar, SWT.SEPARATOR);
		
		pagesPagingToolBar = new PagingToolBar("Page: ", false, false, true, this, SWT.NONE, toolBar);
		pagesPagingToolBar.getReloadBtn().setToolTipText("Reload page");
		
		boolean USE_SAVE_DROP_DOWN=false;
		
		if (USE_SAVE_DROP_DOWN) {
			saveDrowDown = new DropDownToolItem(toolBar, false, true, false, SWT.RADIO);
			saveTranscriptMenuItem = saveDrowDown.addItem("Save", Images.DISK, "", true);
			saveTranscriptWithMessageMenuItem = saveDrowDown.addItem("Save with message", Images.DISK_MESSAGE, "", false);
		} else {
			
			if (false) {
			saveTranscriptToolItem = new ToolItem(toolBar, SWT.PUSH);
			saveTranscriptToolItem.setImage(Images.DISK);
			saveTranscriptToolItem.setToolTipText("Save");
			}
			
			if (false) {
			saveTranscriptWithMessageToolItem = new ToolItem(toolBar, SWT.PUSH);
			saveTranscriptWithMessageToolItem.setImage(Images.DISK_MESSAGE);
			saveTranscriptWithMessageToolItem.setToolTipText("Save with commit message");
			}
			
//			saveOptionsToolItem = new DropDownToolItemSimple(toolBar, SWT.PUSH, "", Images.DISK_WRENCH, "Save options");
			saveOptionsToolItem = new DropDownToolItemSimple(toolBar, SWT.DROP_DOWN, null, Images.DISK, "Save transcription...");
			saveOptionsToolItem.setOpenMenuOnlyOnArrowBtn(true);
			
//			DropDownToo
			
			saveTranscriptMenuItem = saveOptionsToolItem.addItem("Save", Images.DISK, SWT.PUSH);
			saveTranscriptWithMessageMenuItem = saveOptionsToolItem.addItem("Save with commit message", Images.DISK_MESSAGE, SWT.PUSH);
			
			autoSaveListMenuItem = saveOptionsToolItem.addItem("Load autosaved transcript", null, SWT.CASCADE);
			autoSaveListMenu = new Menu(saveOptionsToolItem.getMenu());
			autoSaveListMenuItem.setMenu(autoSaveListMenu);
			
			autoSaveSettingsMenuItem = saveOptionsToolItem.addItem("Autosave settings", null, SWT.PUSH);

//			createImageSizeTextRegionItem = otherSaveOptionstoolItem.addItem("Create top level text region with size of image", null, null);
//			createDefaultLineItem = otherSaveOptionstoolItem.addItem("Create default line for selected line / baseline", null, null);
		}

		/*ToolItem label = new ToolItem(toolBar, SWT.LEFT);
		label.setText("Page status:");*/
		
		ToolItem sep = new ToolItem(toolBar, SWT.SEPARATOR);
	
		statusCombo = new Combo(toolBar, SWT.DROP_DOWN | SWT.READ_ONLY);
		statusCombo.setToolTipText("Page status");
		//statusCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		statusCombo.setItems(EnumUtils.stringsArray(EditStatus.class));
		//statusCombo.setItems(EditStatus.getStatusListWithoutNew());
		statusCombo.pack();
	    sep.setWidth(statusCombo.getSize().x);
	    sep.setControl(statusCombo);
		
		// page status:
		if (Storage.getInstance().getTranscriptMetadata() != null){
			SWTUtil.select(statusCombo, EnumUtils.indexOf(Storage.getInstance().getTranscriptMetadata().getStatus()));
		}
		statusCombo.setEnabled(false);

		versionsButton = new ToolItem(toolBar, SWT.PUSH);
		versionsButton.setToolTipText("Show versions");
		versionsButton.setImage(Images.PAGE_WHITE_STACK);
		versionsButton.setEnabled(false);
				
		loadTranscriptInTextEditor = new ToolItem(toolBar, SWT.PUSH);
		loadTranscriptInTextEditor.setToolTipText("Open transcript source");
		loadTranscriptInTextEditor.setImage(Images.getOrLoad("/icons/script.png"));
				
		new ToolItem(toolBar, SWT.SEPARATOR);
								
		if (TrpSettings.ENABLE_LINE_EDITOR) {
			new ToolItem(toolBar, SWT.SEPARATOR);			
			showLineEditorToggle = new ToolItem(toolBar, SWT.CHECK);
			showLineEditorToggle.setImage(Images.getOrLoad("/icons/pencil.png"));
			showLineEditorToggle.setToolTipText("Show line transcription editor");
		}
		
		toolBar.pack();
		
		getShell().addListener(SWT.Resize, new Listener() {
			@Override public void handleEvent(Event e) {
				updateToolBarSize();
			}
		});
		
		//updateToolBarSize();
	}
	
	private void initDockingToolItems2() {
		DropDownToolItemSimple viewLeftDockingDropItem = new DropDownToolItemSimple(toolBar, SWT.DROP_DOWN, null, Images.APPLICATION_SIDE_CONTRACT, "Menu view docking state");
		viewLeftDockingDropItem.setOpenMenuOnlyOnArrowBtn(true);
			
		DropDownToolItemSimple viewBottomDockingDropitem = new DropDownToolItemSimple(toolBar, SWT.DROP_DOWN, null, Images.APPLICATION_SIDE_PUT, "Transcription view docking state");
		viewBottomDockingDropitem.setOpenMenuOnlyOnArrowBtn(true);
		
		class DockingStateMainBtnListener extends SelectionAdapter {
			Position p;
			
			public DockingStateMainBtnListener(Position p) {
				this.p = p;
			}
			
			@Override public void widgetSelected(SelectionEvent e) {
				if (e.detail == SWT.ARROW) { // don't react if user has pressed the arrow button!
					return;
				}
				
				Docking d = portalWidget.getDocking(p);
				if (d == Docking.DOCKED) {
					portalWidget.setWidgetDockingType(p, Docking.INVISIBLE);
				} else if (d == Docking.INVISIBLE) {
					portalWidget.setWidgetDockingType(p, Docking.DOCKED);
				} else if (d == Docking.UNDOCKED) {
					portalWidget.setWidgetDockingType(p, Docking.DOCKED);
				}
			}
		}
		viewLeftDockingDropItem.getToolItem().addSelectionListener(new DockingStateMainBtnListener(Position.LEFT));
		viewBottomDockingDropitem.getToolItem().addSelectionListener(new DockingStateMainBtnListener(Position.BOTTOM));

		SelectionListener dockingStateSl = new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				if (!(e.getSource() instanceof MenuItem)) {
					return;
				}
				
				MenuItem mi = (MenuItem) e.getSource();
				if (!(mi.getData() instanceof PositionDocking))
					return;
				
				portalWidget.setWidgetDockingType((PositionDocking) mi.getData());
			}
		};
		
		Position[] positions = { Position.LEFT, /*Position.RIGHT,*/ Position.BOTTOM };
		Docking[] dockings = { Docking.DOCKED, Docking.UNDOCKED, Docking.INVISIBLE };
//		String[] cascadeLabels = { "Left view", /*"Right view",*/ "Bottom view" };
		String[] dockingsLabels = { "Docked", "Undocked", "Invisible" };	

		int i=0;
		for (Position p : positions) {
			DropDownToolItemSimple ti = (p == Position.LEFT) ? viewLeftDockingDropItem : viewBottomDockingDropitem;
			// create sub-menu items
			int j=0;
			for (Docking d : dockings) {
				MenuItem dockItem = ti.addItem(dockingsLabels[j], null, SWT.RADIO);
//				MenuItem dockItem = new MenuItem(cmiMenu, SWT.RADIO);
				PositionDocking pd = new PositionDocking(p, d);
				dockItem.setData(new PositionDocking(p, d));
//				dockItem.setText(dockingsLabels[j]);
				dockItem.addSelectionListener(dockingStateSl);
				dockingMenuItems.put(pd, dockItem);
				++j;
			}

			++i;
		}
	}
	
	private void initDockingToolItems() {
		boolean USE_TWO_DOCKING_ICONS=true;
		DropDownToolItem viewLeftDockingDropItem=null, viewBottomDockingDropitem=null, viewDockingDropItem=null;
		if (USE_TWO_DOCKING_ICONS) {
			viewLeftDockingDropItem = new DropDownToolItem(toolBar, false, false, true, SWT.RADIO);
			viewLeftDockingDropItem.ti.setImage(Images.APPLICATION_SIDE_CONTRACT);
			viewLeftDockingDropItem.ti.setToolTipText("Left view docking state...");
			
			
			viewBottomDockingDropitem = new DropDownToolItem(toolBar, false, false, true, SWT.RADIO);
			viewBottomDockingDropitem.ti.setImage(Images.APPLICATION_SIDE_PUT);
			viewBottomDockingDropitem.ti.setToolTipText("Bottom view docking state...");
		} else {
			viewDockingDropItem = new DropDownToolItem(toolBar, false, false, true, SWT.CASCADE);
			viewDockingDropItem.ti.setImage(Images.APPLICATION);
			viewDockingDropItem.ti.setToolTipText("Change docking state...");
		}

		SelectionListener dockingStateSl = new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				if (!(e.getSource() instanceof MenuItem))
					return;
				
				MenuItem mi = (MenuItem) e.getSource();
				if (!(mi.getData() instanceof PositionDocking))
					return;
				
				portalWidget.setWidgetDockingType((PositionDocking) mi.getData());
			}
		};
		
		Position[] positions = { Position.LEFT, /*Position.RIGHT,*/ Position.BOTTOM };
		Docking[] dockings = { Docking.DOCKED, Docking.UNDOCKED, Docking.INVISIBLE };
		String[] cascadeLabels = { "Left view", /*"Right view",*/ "Bottom view" };
		String[] dockingsLabels = { "Docked", "Undocked", "Invisible" };	

		int i=0;
		for (Position p : positions) {
			DropDownToolItem ti = USE_TWO_DOCKING_ICONS ? ( p == Position.LEFT ? viewLeftDockingDropItem : viewBottomDockingDropitem) : viewDockingDropItem;
						
			Menu cmiMenu = null;
			if (!USE_TWO_DOCKING_ICONS) {
				// create the cascade menu
				MenuItem cmi = ti.addItem(cascadeLabels[i], Images.APPLICATION, "Change docking states of the different views");
				
				// create sub-menu and attach it
				cmiMenu = new Menu(ti.getMenu());
				cmi.setMenu(cmiMenu);
			} else {
				cmiMenu = ti.getMenu();
			}

			// create sub-menu items
			int j=0;
			for (Docking d : dockings) {
				MenuItem dockItem = new MenuItem(cmiMenu, SWT.RADIO);
				PositionDocking pd = new PositionDocking(p, d);
				dockItem.setData(new PositionDocking(p, d));
				dockItem.setText(dockingsLabels[j]);
				dockItem.addSelectionListener(dockingStateSl);
				
				dockingMenuItems.put(pd, dockItem);
				++j;
			}

			++i;
		}	
	}

	/**
	 * add all toolbar buttons that come *after* the canvas toolbar
	 */
	private void initToolBar2() {
		
		new ToolItem(toolBar, SWT.SEPARATOR);
		
		bugReportItem = new ToolItem(toolBar, SWT.PUSH);
		bugReportItem.setToolTipText("Send a bug report or feature request");
		bugReportItem.setImage(Images.BUG);
		
		//new ToolItem(toolBar, SWT.SEPARATOR);
		
		toolBar.pack();
		updateToolBarSize();
	}
	
	public void updateProfiles(boolean canTranscribe) {
		profilesToolItem.removeAll();
		
		//reader don't need profiles
		if (!canTranscribe){
			return;
		}
		
		for (String name : TrpConfig.getPredefinedProfiles()) {
			MenuItem i = profilesToolItem.addItem(name, null, null);
			i.setData(name);			
		}
		profilesToolItem.addSeparator();
		for (String name : TrpConfig.getCustomProfiles()) {
			MenuItem i = profilesToolItem.addItem(name, null, null);
			i.setData(name);
		}
		if (!TrpConfig.getCustomProfiles().isEmpty())
			profilesToolItem.addSeparator();
		
		profilesToolItem.addItem("Save current as new profile...", null, null);
	}
	
	void updateToolBarSize() {
		Rectangle rect = getShell().getClientArea();
		toolBarGridData.widthHint = rect.width;
		Point size = pagesPagingToolBar.getToolBar().computeSize(rect.width, SWT.DEFAULT);
		pagesPagingToolBar.getToolBar().setSize(size);
	}
	
	public CanvasShapeType getShapeTypeToDraw() {
		CanvasMode m = getCanvas().getSettings().getMode();
		if (m != CanvasMode.ADD_BASELINE) {
			if (trpSets.getRectMode())
				return CanvasShapeType.RECTANGLE;
			else
				return CanvasShapeType.POLYGON;
			
		}
		else		
			return CanvasShapeType.POLYLINE;
	}
	
	public TrpMenuBar getTrpMenuBar() { return menu; }
	public StructureTreeWidget getStructureTreeWidget() { return structureTreeWidget; }
	public ServerWidget getServerWidget() { return serverWidget; }
//	public JobTableWidgetPagination getJobOverviewWidget() { return jobOverviewWidget; }
	
//	public TranscriptsTableWidgetPagination getVersionsWidget() { return versionsWidget; };
	
	public CanvasWidget getCanvasWidget() { return canvasWidget; }
//	public CanvasToolBar getCanvasToolBar() { return canvasWidget.getToolbar(); }
	
	public SWTCanvas getCanvas() {
		return (canvasWidget != null && canvasWidget.getCanvas()!=null) ? canvasWidget.getCanvas() : null;
	}
	
	public PagingToolBar getPagesPagingToolBar() { return this.pagesPagingToolBar; }
	public ToolItem getReloadDocumentButton() { return reloadDocumentButton; }
	public ToolItem getExportDocumentButton() { return exportDocumentButton; }
	public ToolItem getVersionsButton() { return versionsButton; }
	public ToolItem getJobsButton() { return jobsButton; }
	
	public MenuItem getSaveTranscriptMenuItem() { return saveTranscriptMenuItem; }
	public MenuItem getSaveTranscriptWithMessageMenuItem() { return saveTranscriptWithMessageMenuItem; }
	
	public ToolItem getSaveTranscriptToolItem() { return saveTranscriptToolItem; }
	public ToolItem getSaveTranscriptWithMessageToolItem() { return saveTranscriptWithMessageToolItem; }	
	
	public ToolItem getOpenLocalFolderButton() { return openLocalFolderButton; }
	public ToolItem getCloseDocBtn() { return closeDocBtn; }
	public ToolItem getLoadTranscriptInTextEditor() { return loadTranscriptInTextEditor; }
	public ToolItem getSearchBtn() { return searchBtn; }
	public ToolItem getUploadDocsItem() { return uploadDocsItem; }
	public Combo getStatusCombo() {	return statusCombo;	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public LineTranscriptionWidget getLineTranscriptionWidget() {
		return lineTranscriptionWidget;
	}
	
	public WordTranscriptionWidget getWordTranscriptionWidget() {
		return wordTranscriptionWidget;
	}
	
	public Composite getTranscriptionComposite() { return transcriptionWidgetContainer; }
	
//	public TabFolder getLeftTabFolder() { return this.leftTabFolder; }
//	public DocMetadataEditor getDocMetadataEditor() { return docOverviewWidget.getDocMetadataEditor(); }
	
	public TranscriptionLevel getSelectedTranscriptionType() {
		ATranscriptionWidget widget = getSelectedTranscriptionWidget();
		if (widget!=null) {
			return widget.getType();
		}
		return null;
	}
	
	public ATranscriptionWidget getSelectedTranscriptionWidget() {
		if (transcriptionWidgetContainer.getChildren().length>0 && transcriptionWidgetContainer.getChildren()[0] instanceof ATranscriptionWidget) {
			return (ATranscriptionWidget) transcriptionWidgetContainer.getChildren()[0];	
		}
		else
			return null;
	}

	public TrpSettings getTrpSets() {
		return trpSets;
	}
	
	//public ToolItem getLoginToggle() { return loginToggle; }
	
	public StructuralMetadataWidget getStructuralMetadataWidget() { return structuralMdWidget; }
	public ToolsWidget getToolsWidget() { return toolsWidget; }
		
	public void updateLoginInfo(boolean loggedIn, String username, String server) {
		if (loggedIn) {
			final String suffix;
			if(server.contains("Testing")) {
				suffix = " (TEST SERVER)";
			} else {
				suffix = "";
			}
			serverWidget.getLoginBtn().setText("Logout " + username + suffix);
			serverWidget.getLoginBtn().setImage(Images.CONNECT);
			serverWidget.getLoginBtn().setToolTipText("Server: "+server);
		} else {
			serverWidget.getLoginBtn().setText("Login");
			serverWidget.getLoginBtn().setImage(Images.DISCONNECT);
			serverWidget.getLoginBtn().setToolTipText("");
		}
		
		uploadDocsItem.setEnabled(loggedIn);
		searchBtn.setEnabled(loggedIn);
	}
	
	public void updateVisibility(){
		//show only if role > transcriber (=editor, owner, admin)
		boolean canManage = Storage.getInstance().getRoleOfUserInCurrentCollection().canManage();
		boolean canTranscribe = Storage.getInstance().getRoleOfUserInCurrentCollection().canTranscribe();
		
//		logger.debug("can transcribe " + canTranscribe);
//		logger.debug("can manage " + canManage);		
		
		if (tabWidget.isEnabled() && !tabWidget.toolsItem.isDisposed()){
			if (!canTranscribe){
				tabWidget.toolsItem.setControl(null);
				tabWidget.textTaggingItem.setControl(null);
				tabWidget.structuralMdItem.setControl(null);
				tabWidget.commentsItem.setControl(null);
				tabWidget.docMdItem.setControl(null);
				tabWidget.structureItem.setControl(null);
				lineTranscriptionWidget.setParent(SWTUtil.dummyShell);
				wordTranscriptionWidget.setParent(SWTUtil.dummyShell);				
			}
			else{
				tabWidget.toolsItem.setControl(toolsWidget);
				tabWidget.textTaggingItem.setControl(taggingWidget);
				tabWidget.structuralMdItem.setControl(structuralMdWidget);
				tabWidget.commentsItem.setControl(commentsWidget);
				tabWidget.docMdItem.setControl(docMetadataEditor);
				tabWidget.structureItem.setControl(structureTreeWidget);
				changeToTranscriptionWidget(TranscriptionLevel.LINE_BASED);
			}
			
		}
		
		uploadDocsItem.setEnabled(canManage);
		
		updateProfiles(canTranscribe);
		
		//see also updateToolBars() in TrpMainWidget
		
//		saveOptionsToolItem.setEnabled(canTranscribe);
//		statusCombo.setEnabled(canTranscribe);
		
		//burger menu
		menu.updateVisibility(canManage);
		
		//doc overview tool bar
		serverWidget.updateBtnVisibility(canManage);
		
		//canvas toolbar
		canvasWidget.getCanvas().getSettings().setEditingEnabled(canTranscribe);
		
	}

	public void center() {
		Monitor primary = Display.getCurrent().getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = getShell().getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;

		getShell().setLocation(x, y);
	}
	
	public TrpTabWidget getTabWidget() {
		return tabWidget;
	}

	public TaggingWidget getTaggingWidget() {
		return taggingWidget;
	}

	public CommentsWidget getCommentsWidget() {
		return commentsWidget;
	}

	public DropDownToolItem getProfilesToolItem() {
		return profilesToolItem;
	}
	
	public PortalWidget getPortalWidget() {
		return portalWidget;
	}
	
	public ThumbnailWidget getThumbnailWidget() {
		return docInfoWidget.getThumbnailWidget();
	}

	public DropDownToolItem getSaveDropDown() {
		return saveDrowDown;
	}

	public DocInfoWidget getDocInfoWidget() {
		return docInfoWidget;
	}
	
	public ToolItem getQuickSearchButton() {
		return quickSearchButton;
	}
	
	public TextToolItem getQuickSearchText() {
		return quickSearchText;
	}
	
	public TextStyleTypeWidget getTextStyleWidget() {
		return textStyleWidget;
	}

}
