package eu.transkribus.swt_gui.mainwidget;

import java.util.HashMap;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.pagingtoolbar.PagingToolBar;
import eu.transkribus.swt.portal.PortalWidget;
import eu.transkribus.swt.portal.PortalWidget.Docking;
import eu.transkribus.swt.portal.PortalWidget.Position;
import eu.transkribus.swt.portal.PortalWidget.PositionDocking;
import eu.transkribus.swt.util.DropDownToolItem;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.ThumbnailWidget;
import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.canvas.CanvasMode;
import eu.transkribus.swt_gui.canvas.CanvasToolBar;
import eu.transkribus.swt_gui.canvas.CanvasWidget;
import eu.transkribus.swt_gui.canvas.SWTCanvas;
import eu.transkribus.swt_gui.canvas.shapes.CanvasShapeType;
import eu.transkribus.swt_gui.comments_widget.CommentsWidget;
import eu.transkribus.swt_gui.doc_overview.DocInfoWidget;
import eu.transkribus.swt_gui.doc_overview.DocMetadataEditor;
import eu.transkribus.swt_gui.doc_overview.ServerWidget;
import eu.transkribus.swt_gui.mainwidget.menubar.TrpMenuBar;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import eu.transkribus.swt_gui.page_metadata.PageMetadataWidget;
import eu.transkribus.swt_gui.page_metadata.TaggingWidget;
import eu.transkribus.swt_gui.structure_tree.StructureTreeWidget;
import eu.transkribus.swt_gui.tools.ToolsWidget;
import eu.transkribus.swt_gui.transcription.ATranscriptionWidget;
import eu.transkribus.swt_gui.transcription.ATranscriptionWidget.Type;
import eu.transkribus.swt_gui.transcription.LineTranscriptionWidget;
import eu.transkribus.swt_gui.transcription.WordTranscriptionWidget;

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
	PageMetadataWidget structuralMdWidget;
	
//	public static boolean SHOW_NEW_TW = true;
	TaggingWidget taggingWidget;
	ToolsWidget toolsWidget;
	CommentsWidget commentsWidget;
	ThumbnailWidget thumbnailWidget;

	TrpSettings trpSets;
	PortalWidget portalWidget;
	TrpMenuBar menu;
	// ##########
	
	// ##### Toolbar stuff: #####
	ToolItem menuButton, /*loginToggle,*/ reloadDocumentButton, exportDocumentButton, openLocalFolderButton, closeDocBtn;
	ToolItem uploadDocsItem, searchBtn;

	DropDownToolItem saveDrowDown;
	MenuItem saveTranscriptButton, saveTranscriptWithMessageButton;
	ToolItem versionsButton;
	
	DropDownToolItem visibilityItem;
	MenuItem showRegionsItem;
	MenuItem showLinesItem;
	MenuItem showBaselinesItem;
	MenuItem showWordsItem;
	MenuItem showPrintspaceItem;
	MenuItem renderBlackeningsItem;
	MenuItem showReadingOrderRegionsMenuItem;
	MenuItem showReadingOrderLinesMenuItem;
	MenuItem showReadingOrderWordsMenuItem;	
		
	// dock state buttons
	DropDownToolItem viewDockingDropItem;
	HashMap<PositionDocking, MenuItem>  dockingMenuItems = new HashMap<>();
	
	DropDownToolItem profilesToolItem;
	
	ToolItem showLineEditorToggle;
	ToolItem loadTranscriptInTextEditor;
	ToolItem helpItem;
	// ##########
		
	TrpTabWidget tabWidget;
	Composite transcriptionWidgetContainer;

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
		initSettings();
		
//		progressDialog = new ProgressMonitorDialog(getShell());
		
		setToolTipText("An interactive adaptive transcription platform");
		getShell().setText(APP_NAME);
		getShell().setImage(Images.getOrLoad("/icons/pencil.png"));
//		getShell().setImage(Images.getOrLoad("/wolpertinger_small_64.png"));
//		setSize(1200, 850);
//		setLayout(new FillLayout());
		setLayout(new GridLayout(2, false));
		
//		setBackground(Colors.getSystemColor(SWT.COLOR_BLUE));
		
//		menu = new TrpMenuBar(this); // currently used when clicked on "burger button"
		menu = new TrpMenuBar(getShell()); // currently used when clicked on "burger button"
//		getShell().setMenuBar(menu.getMenuBar());

		initToolBar();

		canvasWidget = new CanvasWidget(SWTUtil.dummyShell, mainWidget, SWT.NONE, getPagesPagingToolBar().getToolBar());

		// NEW: only one tab widget
		tabWidget = new TrpTabWidget(this, 0);
		
//		serverDocsWidget = new ServerDocsWidget(tabWidget.serverTf);
//		tabWidget.docListItem.setControl(serverDocsWidget);
		
		serverWidget = new ServerWidget(tabWidget.mainTf);
		tabWidget.serverItem.setControl(serverWidget);
		
//		docInfoWidget = new DocInfoWidget(tabWidget.documentTf, 0);
//		tabWidget.docoverviewItem.setControl(docInfoWidget);
		docInfoWidget = new DocInfoWidget(tabWidget.mainTf, 0);
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

		structuralMdWidget = new PageMetadataWidget(tabWidget.metadataTf, SWT.TOP);
		tabWidget.structuralMdItem.setControl(structuralMdWidget);
		
		taggingWidget = new TaggingWidget(tabWidget.metadataTf, SWT.TOP, 2, true);
		taggingWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tabWidget.textTaggingItem.setControl(taggingWidget);
		
		commentsWidget = new CommentsWidget(tabWidget.metadataTf, SWT.TOP);
		tabWidget.commentsItem.setControl(commentsWidget);
		
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
		changeToTranscriptionWidget(Type.LINE_BASED);
		
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
	
	public void changeToTranscriptionWidget(ATranscriptionWidget.Type type) {
		logger.debug("changing to tr-widget: "+type);
		boolean changed=false;
		
		if (type == ATranscriptionWidget.Type.LINE_BASED) {
			changed=true;
//			lineTranscriptionWidget.getTranscriptionTypeItem().clearSelections();
			lineTranscriptionWidget.getTranscriptionTypeItem().selectItem(0, false);
			
			lineTranscriptionWidget.setParent(transcriptionWidgetContainer);
			wordTranscriptionWidget.setParent(SWTUtil.dummyShell);
			
//			lineTranscriptionWidget.updateToolbarSize();
			
		} else if (type == ATranscriptionWidget.Type.WORD_BASED) {
			changed=true;
//			wordTranscriptionWidget.getTranscriptionTypeItem().clearSelections();
			wordTranscriptionWidget.getTranscriptionTypeItem().selectItem(1, false);
			
			lineTranscriptionWidget.setParent(SWTUtil.dummyShell);
			wordTranscriptionWidget.setParent(transcriptionWidgetContainer);
			
//			wordTranscriptionWidget.updateToolbarSize();
		}
		
		if (changed) {
//			lineTranscriptionWidget.pack();
//			wordTranscriptionWidget.pack();
			transcriptionWidgetContainer.layout(true);
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

	private void initSettings() {
		trpSets = new TrpSettings();
		TrpConfig.registerBean(trpSets, true);
	}

	private void initToolBar() {
		pagesPagingToolBar = new PagingToolBar("Page: ", false, false, this, SWT.NONE);
		
		toolBarGridData = new GridData(SWT.FILL, SWT.TOP, true, true);
//		pagesPagingToolBar.setLayoutData(toolBarGridData);
		pagesPagingToolBar.getReloadBtn().setToolTipText("Reload page");
		
		// retrieve toolbar from pagesPagingToolBar -> this will be the main toolbar where all other items are prepended / appended
		ToolBar toolBar = pagesPagingToolBar.getToolBar();
		
		saveDrowDown = new DropDownToolItem(toolBar, false, true, SWT.RADIO);
		
		saveTranscriptButton = saveDrowDown.addItem("Save", Images.DISK, "", true);
		saveTranscriptWithMessageButton = saveDrowDown.addItem("Save with message", Images.DISK_MESSAGE, "", false);
				
		versionsButton = new ToolItem(toolBar, SWT.PUSH);
		versionsButton.setToolTipText("Show versions");
		versionsButton.setImage(Images.PAGE_WHITE_STACK);
		versionsButton.setEnabled(false);
		
		loadTranscriptInTextEditor = new ToolItem(toolBar, SWT.PUSH);
		loadTranscriptInTextEditor.setToolTipText("Open transcript source");
		loadTranscriptInTextEditor.setImage(Images.getOrLoad("/icons/script.png"));

		new ToolItem(toolBar, SWT.SEPARATOR);
				
		int preInsertIndex=0;
		
		// open menu button:
		menuButton = new ToolItem(toolBar, SWT.CHECK, preInsertIndex++);
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

		viewDockingDropItem = new DropDownToolItem(toolBar, false, true, SWT.CASCADE, preInsertIndex++);

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
			// create the cascade menu
			MenuItem cmi = viewDockingDropItem.addItem(cascadeLabels[i], Images.APPLICATION, "Change docking states of the different views");
			
			// create sub-menu and attach it
			Menu cmiMenu = new Menu(viewDockingDropItem.getMenu());
			cmi.setMenu(cmiMenu);
			
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

		profilesToolItem = new DropDownToolItem(toolBar, false, false, SWT.NONE, preInsertIndex++);
		profilesToolItem.ti.setImage(Images.CONTROL_EQUALIZER);
		profilesToolItem.ti.setToolTipText("Profiles");
		updateProfiles();
		
		new ToolItem(toolBar, SWT.SEPARATOR, preInsertIndex++);
		
		openLocalFolderButton = new ToolItem(toolBar, SWT.PUSH, preInsertIndex++);
		openLocalFolderButton.setToolTipText("Open local document");
		openLocalFolderButton.setImage(Images.getOrLoad("/icons/folder.png"));
		
		uploadDocsItem = new ToolItem(toolBar, SWT.PUSH, preInsertIndex++);
		uploadDocsItem.setToolTipText("Import document(s)");
//		uploadFromPrivateFtpItem.setImage(Images.getOrLoad("/icons/weather_clouds.png"));
		uploadDocsItem.setImage(Images.FOLDER_IMPORT);
		
		exportDocumentButton = new ToolItem(toolBar, SWT.PUSH, preInsertIndex++);
		exportDocumentButton.setToolTipText("Export document");
		exportDocumentButton.setImage(Images.FOLDER_GO);
		exportDocumentButton.setEnabled(false);
		
		reloadDocumentButton = new ToolItem(toolBar, SWT.PUSH, preInsertIndex++);
		reloadDocumentButton.setToolTipText("Reload document");
		reloadDocumentButton.setImage(Images.REFRESH);
		reloadDocumentButton.setEnabled(false);
								
		searchBtn = new ToolItem(toolBar, SWT.PUSH, preInsertIndex++);
		searchBtn.setToolTipText("Search for documents, keywords etc.");
		searchBtn.setImage(Images.getOrLoad("/icons/find.png"));
	
		// view item:
		visibilityItem = new DropDownToolItem(toolBar, false, true, SWT.CHECK);
		visibilityItem.ti.setImage(Images.EYE);
		String vtt = "Visibility of items on canvas"; 
		
		showRegionsItem = visibilityItem.addItem("Show Regions", Images.EYE, vtt);
		showLinesItem = visibilityItem.addItem("Show Lines", Images.EYE, vtt);
		showBaselinesItem = visibilityItem.addItem("Show Baselines", Images.EYE, vtt);
		showWordsItem = visibilityItem.addItem("Show Words", Images.EYE, vtt);
		showPrintspaceItem = visibilityItem.addItem("Show Printspace", Images.EYE, vtt);
		renderBlackeningsItem = visibilityItem.addItem("Render Blackenings", Images.EYE, vtt);
		showReadingOrderRegionsMenuItem = visibilityItem.addItem("Show regions reading order", Images.EYE, vtt);
		showReadingOrderLinesMenuItem = visibilityItem.addItem("Show lines reading order", Images.EYE, vtt);
		showReadingOrderWordsMenuItem = visibilityItem.addItem("Show words reading order", Images.EYE, vtt);
		
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
		
		updateToolBarSize();
	}
	
	public void updateProfiles() {
		profilesToolItem.removeAll();
		
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
//		getPagesPagingToolBar().pack(true);
		Rectangle rect = getShell().getClientArea();
		logger.debug("client area width: "+rect.width);
		toolBarGridData.widthHint = rect.width;
		Point size = pagesPagingToolBar.getToolBar().computeSize(rect.width, SWT.DEFAULT);
//		Point size = pagesPagingToolBar.getToolBar().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		logger.debug("tb size: "+size);
		pagesPagingToolBar.getToolBar().setSize(size);
		pagesPagingToolBar.getToolBar().pack();
//		pagesPagingToolBar.getToolBar().pack();
//		cbItem.setSize(size);
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
	public CanvasToolBar getCanvasToolBar() { return canvasWidget.getToolbar(); }
	
	public SWTCanvas getCanvas() {
		return (canvasWidget != null && canvasWidget.getCanvas()!=null) ? canvasWidget.getCanvas() : null;
	}
	
	public PagingToolBar getPagesPagingToolBar() { return this.pagesPagingToolBar; }
	public ToolItem getReloadDocumentButton() { return reloadDocumentButton; }
	public ToolItem getExportDocumentButton() { return exportDocumentButton; }
	public ToolItem getVersionsButton() { return versionsButton; }
	public MenuItem getSaveTranscriptButton() { return saveTranscriptButton; }
	public MenuItem getSaveTranscriptWithMessageButton() { return saveTranscriptWithMessageButton; }
	public ToolItem getOpenLocalFolderButton() { return openLocalFolderButton; }
	public ToolItem getCloseDocBtn() { return closeDocBtn; }
	public ToolItem getLoadTranscriptInTextEditor() { return loadTranscriptInTextEditor; }
	public ToolItem getSearchBtn() { return searchBtn; }
	public ToolItem getUploadDocsItem() { return uploadDocsItem; }
	
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
	
	public ATranscriptionWidget.Type getSelectedTranscriptionType() {
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
	
	public PageMetadataWidget getStructuralMetadataWidget() { return structuralMdWidget; }
	public ToolsWidget getToolsWidget() { return toolsWidget; }
		
	public void updateLoginInfo(boolean loggedIn, String username, String server) {
		if (loggedIn) {
			serverWidget.getLoginBtn().setText("Logout "+username);
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

	public TaggingWidget getTaggingWidgetNew() {
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

}
