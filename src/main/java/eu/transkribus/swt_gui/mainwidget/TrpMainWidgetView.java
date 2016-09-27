package eu.transkribus.swt_gui.mainwidget;

import java.util.HashMap;

import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
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

import eu.transkribus.core.model.beans.pagecontent_trp.RegionTypeUtil;
import eu.transkribus.swt_canvas.canvas.CanvasMode;
import eu.transkribus.swt_canvas.canvas.CanvasSettings;
import eu.transkribus.swt_canvas.canvas.CanvasToolBar;
import eu.transkribus.swt_canvas.canvas.CanvasWidget;
import eu.transkribus.swt_canvas.canvas.shapes.CanvasShapeType;
import eu.transkribus.swt_canvas.pagingtoolbar.PagingToolBar;
import eu.transkribus.swt_canvas.portal.PortalWidget;
import eu.transkribus.swt_canvas.portal.PortalWidget.Docking;
import eu.transkribus.swt_canvas.portal.PortalWidget.Position;
import eu.transkribus.swt_canvas.portal.PortalWidget.PositionDocking;
import eu.transkribus.swt_canvas.util.DropDownToolItem;
import eu.transkribus.swt_canvas.util.Images;
import eu.transkribus.swt_canvas.util.SWTUtil;
import eu.transkribus.swt_canvas.util.ThumbnailWidget;
import eu.transkribus.swt_canvas.util.databinding.DataBinder;
import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.canvas.TrpSWTCanvas;
import eu.transkribus.swt_gui.comments_widget.CommentsWidget;
import eu.transkribus.swt_gui.dialogs.ProxySettingsDialog;
import eu.transkribus.swt_gui.dialogs.SettingsDialog;
import eu.transkribus.swt_gui.doc_overview.DocMetadataWidget;
import eu.transkribus.swt_gui.doc_overview.DocOverviewWidget;
import eu.transkribus.swt_gui.menubar.MenuListener;
import eu.transkribus.swt_gui.menubar.TrpMenuBar;
import eu.transkribus.swt_gui.page_metadata.PageMetadataWidget;
import eu.transkribus.swt_gui.page_metadata.TaggingWidget;
import eu.transkribus.swt_gui.pagination_tables.JobTableWidgetPagination;
import eu.transkribus.swt_gui.pagination_tables.TranscriptsTableWidgetPagination;
import eu.transkribus.swt_gui.structure_tree.StructureTreeWidget;
import eu.transkribus.swt_gui.tools.ToolsWidget;
import eu.transkribus.swt_gui.transcription.ATranscriptionWidget;
import eu.transkribus.swt_gui.transcription.ATranscriptionWidget.Type;
import eu.transkribus.swt_gui.transcription.LineTranscriptionWidget;
import eu.transkribus.swt_gui.transcription.WordTranscriptionWidget;
import eu.transkribus.swt_gui.vkeyboards.TrpVirtualKeyboardsWidget;

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
	DocOverviewWidget docOverviewWidget;
	DocMetadataWidget docMetadataWidget;
	
//	JobOverviewWidget jobOverviewWidget;
	JobTableWidgetPagination jobOverviewWidget;
//	VersionsWidget versionsWidget;
	TranscriptsTableWidgetPagination versionsWidget;
//	JobsAndVersionsView jobsAndVersionView;
	PageMetadataWidget metadataWidget;
	
	public static boolean SHOW_NEW_TW = true;
	TaggingWidget taggingWidget;
	TrpVirtualKeyboardsWidget vkeyboards;
	ToolsWidget toolsWidget;
	CommentsWidget commentsWidget;
//	AnalyticsWidget analyticsWidget;
	ThumbnailWidget thumbnailWidget;
//	Label currentUserLabel; // currently unused

	TrpSettings trpSets;
	PortalWidget portalWidget;
	TrpMenuBar menu;
	MenuListener menuListener;	
	// ##########
	
	// ##### Toolbar stuff: #####
	ToolItem menuButton, loginToggle, reloadDocumentButton, exportDocumentButton, exportPdfButton, exportTeiButton, exportRtfButton, openLocalFolderButton, closeDocBtn;
	ToolItem uploadDocsItem, searchBtn;

	DropDownToolItem saveDrowDown;
	MenuItem saveTranscriptButton, saveTranscriptWithMessageButton;
	ToolItem replacePageImgButton;
	
	// old dock state buttons:
	DropDownToolItem leftViewDockingDropItem, rightViewDockingDropItem, bottomViewDockingDropItem;
	HashMap<Position, DropDownToolItem> dockingToolItems = new HashMap<>();
	
	// new dock state buttons:
	DropDownToolItem viewDockingDropItem;
	HashMap<PositionDocking, MenuItem>  dockingMenuItems = new HashMap<>();

	ToolItem leftViewVisibleToggle;
	ToolItem rightViewVisibleToggle;
	ToolItem bottomViewVisibleToggle;
	
	ToolItem showPrintSpaceToggle, showRegionsToggle, showLinesToggle, showBaselinesToggle, showWordsToggle;
	//ToolItem renderBlackeningsToggle;
	
//	DropDownToolItem showReadingOrderToolItem;
//	MenuItem showReadingOrderRegionsItem, showReadingOrderLinesItem, showReadingOrderWordsItem;
	
	ToolItem showReadingOrderRegionsItem, showReadingOrderLinesItem, showReadingOrderWordsItem;
	DropDownToolItem profilesToolItem;
	
	ToolItem showLineEditorToggle;
	ToolItem loadTranscriptInTextEditor;
//	ToolItem sendBugReportButton;
	// ##########
	
	// ##### Tab folders stuff: #####
//	CTabFolder leftTabFolder;
//	CTabFolder rightTabFolder;
	
	TrpTabWidget tabWidget;
	
//	TabFolder transcriptionTabFolder;
	Composite transcriptionWidgetContainer;
	
	// left tab-items:
	CTabItem docoverviewItem, structureItem, jobOverviewItem, versionsItem, thumbnailItem;

	// right tab-items:
	CTabItem metadataItem, vkeyboardsItem, laItem;

	// bottom tab-items:
//	TabItem lineTranscriptionItem;
//	TabItem wordTranscriptionItem;
//	TabItem wordGraphEditorItem;
	// ##########
	
//	CoolBar cb;
//	CoolItem cbItem;
	
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
	
	private final static boolean DISABLE_EXPLICIT_VISIBILITY_BTNS = true;
	private final static boolean DISABLE_OLD_DOCK_STATE_BTNS = true;
		
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
		
		menu = new TrpMenuBar(this); // currently used when clicked on "burger button"
//		getShell().setMenuBar(menu.getMenuBar());		

		initToolBar();

		canvasWidget = new CanvasWidget(SWTUtil.dummyShell, mainWidget, SWT.NONE, getPagesPagingToolBar().getToolBar());

		// NEW: only one tab widget
		tabWidget = new TrpTabWidget(this, 0);
		
		docOverviewWidget = new DocOverviewWidget(tabWidget.serverTf);
		tabWidget.docListItem.setControl(docOverviewWidget);
		
		docMetadataWidget = new DocMetadataWidget(tabWidget.documentTf, 0);
		tabWidget.docoverviewItem.setControl(docMetadataWidget);
		
		structureTreeWidget = new StructureTreeWidget(tabWidget.documentTf);
		tabWidget.structureItem.setControl(structureTreeWidget);
		
		versionsWidget = new TranscriptsTableWidgetPagination(tabWidget.documentTf, SWT.NONE, 25);
		tabWidget.versionsItem.setControl(versionsWidget);
		
		thumbnailWidget = new ThumbnailWidget(tabWidget.documentTf, SWT.NONE);
		tabWidget.thumbnailItem.setControl(thumbnailWidget);
		
		
		// ####### LEFT TAB FOLDER: #######
		if (true) {
//		leftTabFolder = new CTabFolder(SWTUtil.dummyShell, SWT.BORDER | SWT.FLAT);
//		leftTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		leftTabFolder.setMinimizeVisible(true);
//		leftTabFolder.addCTabFolder2Listener(new CTabFolder2Listener() {
//			
//			@Override public void showList(CTabFolderEvent event) {
//			}
//			
//			@Override public void restore(CTabFolderEvent event) {
//			}
//			
//			@Override public void minimize(CTabFolderEvent event) {
//				leftTabFolder.setMinimized(true);
//			}
//			
//			@Override public void maximize(CTabFolderEvent event) {
//			}
//			
//			@Override public void close(CTabFolderEvent event) {
//			}
//		});
		
//		docOverviewWidget = new DocOverviewWidget(leftTabFolder);
//		structureTreeWidget = new StructureTreeWidget(leftTabFolder);
		jobOverviewWidget = new JobTableWidgetPagination(tabWidget.serverTf, SWT.NONE, 50);
		tabWidget.jobsItem.setControl(jobOverviewWidget);
		
		
		
//		versionsWidget = new TranscriptsTableWidgetPagination(leftTabFolder, SWT.NONE, 25);
//		thumbnailWidget = new ThumbnailWidget(leftTabFolder, SWT.NONE);
		
//		docoverviewItem = createCTabItem(leftTabFolder, docOverviewWidget, Msgs.get2("documents"));
//		structureItem = createCTabItem(leftTabFolder, structureTreeWidget, Msgs.get2("layout_tab_title"));
//		jobOverviewItem = createCTabItem(leftTabFolder, jobOverviewWidget, Msgs.get2("jobs"));
//		versionsItem = createCTabItem(leftTabFolder, versionsWidget, Msgs.get2("versions"));
//		thumbnailItem = createCTabItem(leftTabFolder, thumbnailWidget, Msgs.get2("pages"));
		
//		selectStructureTab();
		}
		
		// the right widget (page metadata, virtual keyboard):
		if (true) {
//			rightTabFolder = new CTabFolder(SWTUtil.dummyShell, SWT.TOP | SWT.BORDER | SWT.FLAT );
//			rightTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			
			metadataWidget = new PageMetadataWidget(tabWidget.metadataTf, SWT.TOP);
			tabWidget.structuralMdItem.setControl(metadataWidget);
			
			vkeyboards = new TrpVirtualKeyboardsWidget(tabWidget.tf, SWT.TOP | SWT.BORDER | SWT.FLAT );
			tabWidget.vkItem.setControl(vkeyboards);
			
			toolsWidget = new ToolsWidget(tabWidget.serverTf, SWT.TOP);
			toolsWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			tabWidget.remoteToolsItem.setControl(toolsWidget);
			
			if (SHOW_NEW_TW) {
				taggingWidget = new TaggingWidget(tabWidget.metadataTf, SWT.TOP, 2, true);
				taggingWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				tabWidget.textTaggingItem.setControl(taggingWidget);
				
//				analyticsWidget = new AnalyticsWidget(rightTabFolder, SWT.TOP);
			}

//			metadataItem = createCTabItem(rightTabFolder, metadataWidget, Msgs.get2("metadata"));
//			laItem = createCTabItem(rightTabFolder, toolsWidget, Msgs.get2("tools"));
//			vkeyboardsItem = createCTabItem(rightTabFolder, vkeyboards, Msgs.get2("virt_keyboards"));
			
//			if (SHOW_NEW_TW) {
//				CTabItem twItem = createCTabItem(rightTabFolder, taggingWidget, Msgs.get2("tagging"));
////				CTabItem analyticsItem = createCTabItem(rightTabFolder, analyticsWidget, "Analytics");
//			}
			
			commentsWidget = new CommentsWidget(tabWidget.metadataTf, SWT.TOP);
			tabWidget.commentsItem.setControl(commentsWidget);
			
			
//			CTabItem commentsItem = createCTabItem(rightTabFolder, commentsWidget, Msgs.get2("comments"));
		}

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
//		portalWidget.setMinWidth(Position.RIGHT, 300);

//		portalWidget.setMinHeight(Position.RIGHT, rightTabFolder.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		
		logger.debug("left view docking state: "+getTrpSets().getLeftViewDockingState());
		
		portalWidget.setWidgetDockingType(Position.LEFT, getTrpSets().getLeftViewDockingState());
		portalWidget.setWidgetDockingType(Position.RIGHT, getTrpSets().getRightViewDockingState());
		portalWidget.setWidgetDockingType(Position.BOTTOM, getTrpSets().getBottomViewDockingState());
		
		addInternalListener();
		addBindings();
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
	
	public void openViewSetsDialog() {
		SettingsDialog sd = new SettingsDialog(getShell(), /*SWT.PRIMARY_MODAL|*/ SWT.DIALOG_TRIM, getCanvas().getSettings(), getTrpSets());		
		sd.open();
	}
	
	public void openProxySetsDialog() {
		ProxySettingsDialog psd = new ProxySettingsDialog(getShell(), /*SWT.PRIMARY_MODAL|*/ SWT.DIALOG_TRIM, getTrpSets());		
		psd.open();
		Storage.getInstance().updateProxySettings();
	}
	
	// TABBING STUFF:
//	private TabItem createTabItem(TabFolder tabFolder, Control control, String Text) {
//		TabItem ti = new TabItem(tabFolder, SWT.NONE);
//		ti.setText(Text);
//		ti.setControl(control);
//		return ti;
//	}
//	
//	private CTabItem createCTabItem(CTabFolder tabFolder, Control control, String Text) {
//		CTabItem ti = new CTabItem(tabFolder, SWT.NONE);
//		ti.setText(Text);
//		ti.setControl(control);
//		return ti;
//	}
	
	public void selectDocListTab() {
		tabWidget.selectDocListTab();
	}
	
//	public void selectStructureTab() {
//		tabWidget.tf.setSelection(tabWidget.documentItem);
//		tabWidget.documentTf.setSelection(tabWidget.structureItem);
//		tabWidget.updateAllSelectedTabs();
//	}
	
	public void selectJobListTab() {
		tabWidget.selectJobListTab();		
	}
	
//	public void selectMetadataTab() {
//		tabWidget.tf.setSelection(tabWidget.metadataItem);
//		tabWidget.metadataTf.setSelection(tabWidget.structuralMdItem);
//		tabWidget.updateAllSelectedTabs();		
//	}
	
//	public void selectToolsTab() {
//		tabWidget.tf.setSelection(tabWidget.toolsItem);
//		tabWidget.toolsTf.setSelection(tabWidget.remoteToolsItem);
//		tabWidget.updateAllSelectedTabs();		
//	}
	
	public void selectLeftTab(int idx) {
//		if (idx < 0 || idx >= leftTabFolder.getItemCount()){
//			idx = 0;
//		}
//		leftTabFolder.setSelection(idx);
//		if (leftTabFolder.getSelection().equals(thumbnailItem)){
//			thumbnailWidget.reload();
//		}
	}

	public void selectRightTab(int idx){
//		if (idx < 0 || idx >= rightTabFolder.getItemCount()){
//			idx = 0;
//		}
//		rightTabFolder.setSelection(idx);
	}
	// END OF TABBING STUFF

	private void initSettings() {
		trpSets = new TrpSettings();
		TrpConfig.registerBean(trpSets, true);
	}

	private void initToolBar() {
//		cb = new CoolBar(this, SWT.NONE);
//		cbItem = new CoolItem(cb, SWT.NONE);
		
		pagesPagingToolBar = new PagingToolBar("Page: ", false, false, this, SWT.NONE);
		
		toolBarGridData = new GridData(SWT.FILL, SWT.TOP, true, true);
//		pagesPagingToolBar.setLayoutData(toolBarGridData);
		pagesPagingToolBar.getReloadBtn().setToolTipText("Reload page");
		
//		ToolbarToolItem tmpTi = new ToolbarToolItem(allToolBar, SWT.NONE, pagesPagingToolBar.getToolBar());
//		tmpTi.setControl(pagesPagingToolBar);
//		tmpTi.setWidth(pagesPagingToolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
//		pagesPagingToolBar.setDoublePageButtonsVisible(false);
//		transcriptsPagingToolBar = new PagingToolBar("Transcripts: ", false, this, SWT.NONE);
		
		// retrieve toolbar from pagesPagingToolBar -> this will be the main toolbar where all other items are prepended / appended
		ToolBar toolBar = pagesPagingToolBar.getToolBar();
		
		saveDrowDown = new DropDownToolItem(toolBar, false, true, SWT.RADIO);
		
		saveTranscriptButton = saveDrowDown.addItem(RegionTypeUtil.TEXT_REGION, Images.DISK, "", true);
		saveTranscriptButton.setText("Save");
		
		saveTranscriptWithMessageButton = saveDrowDown.addItem(RegionTypeUtil.TEXT_REGION, Images.DISK_MESSAGE, "", false);
		saveTranscriptWithMessageButton.setText("Save with message");
		
		replacePageImgButton = new ToolItem(toolBar, SWT.PUSH);
		replacePageImgButton.setToolTipText("Replace page image on server");
		replacePageImgButton.setImage(Images.IMAGE_EDIT);
		replacePageImgButton.setEnabled(false);		
		
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
		menu.getMenuBar().addMenuListener(new org.eclipse.swt.events.MenuListener() {
			@Override public void menuShown(MenuEvent e) {
				menuButton.setSelection(true);
			}
			@Override public void menuHidden(MenuEvent e) {
				menuButton.setSelection(false);
			}
		});
		
//		languageDropDown = new DropDownToolItem(toolBar, false, true, SWT.RADIO, preInsertIndex++);
//		for (Locale l : Msgs.LOCALES) {
//			languageDropDown.addItem(l.getDisplayName(), Images.COMMENT, Msgs.get2("language")+": "+l.getLanguage(), false, l);
//		}
//		
//		MenuItem i = languageDropDown.getItemWithData(TrpConfig.getTrpSettings().getLocale());
//		if (i == null) {
//			i = languageDropDown.getItemWithData(Msgs.DEFAULT_LOCALE);
//		}
//		languageDropDown.selectItem(i, false);
		
		
//		new ToolItem(toolBar, SWT.SEPARATOR, preInsertIndex++);
		
		loginToggle = new ToolItem(toolBar, SWT.PUSH, preInsertIndex++);
		loginToggle.setToolTipText("Login");
		loginToggle.setImage(Images.getOrLoad("/icons/disconnect.png"));
//		loginToggle.setImage(Images.getOrLoad("/icons/connect.png"));
		if (true) {
//			class DockingSl extends SelectionAdapter {
//				DropDownToolItem item;
//				Position pos;
//				
//				public DockingSl(DropDownToolItem item, Position pos) {
//					this.item = item;
//					this.pos = pos;
//				}
//				
//				@Override public void widgetSelected(SelectionEvent e) {
//					logger.debug("selected: "+pos+", menu visible: "+item.isMenuVisible());
//					
//					if (e.detail == SWT.ARROW)
//						return;
//					if (item.isMenuVisible())
//						return;
//					
////					logger.debug("selected: "+e);
//					switch (portalWidget.getDocking(pos)) {
//					case DOCKED:
//						portalWidget.setWidgetDockingType(pos, Docking.INVISIBLE);
//						break;
//					case UNDOCKED:
//					case INVISIBLE:
//						portalWidget.setWidgetDockingType(pos, Docking.DOCKED);
//						break;					
//					}		
//				}
//			};
			
			viewDockingDropItem = new DropDownToolItem(toolBar, false, true, SWT.CASCADE, preInsertIndex++);

//			MenuItem lvMi = viewDockingDropItem.addItem("Left view", Images.APPLICATION, "Change docking states of the different views");
//			MenuItem rvMi = viewDockingDropItem.addItem("Right view", Images.APPLICATION, "Change docking states of the different views");
//			MenuItem bvMi = viewDockingDropItem.addItem("Bottom view", Images.APPLICATION, "Change docking states of the different views");
			
			
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

			// for all positions
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

			if (!DISABLE_OLD_DOCK_STATE_BTNS) {
			leftViewDockingDropItem = new DropDownToolItem(toolBar, false, true, SWT.RADIO, preInsertIndex++);
			leftViewDockingDropItem.addItem("Docked", Images.APPLICATION_SIDE_CONTRACT, "Left view docking state: docked", false, Docking.DOCKED);
			leftViewDockingDropItem.addItem("Undocked", Images.APPLICATION_SIDE_CONTRACT, "Left view docking state: undocked", false, Docking.UNDOCKED);
			leftViewDockingDropItem.addItem("Invisible", Images.APPLICATION_SIDE_CONTRACT, "Left view docking state: invisible", false, Docking.INVISIBLE);
			dockingToolItems.put(Position.LEFT, leftViewDockingDropItem);
			leftViewDockingDropItem.selectItem(0, false);
//			leftViewDockingDropItem.ti.addSelectionListener(new DockingSl(leftViewDockingDropItem, Position.LEFT));
			
			rightViewDockingDropItem = new DropDownToolItem(toolBar, false, true, SWT.RADIO, preInsertIndex++);
			rightViewDockingDropItem.addItem("Docked", Images.APPLICATION_SIDE_EXPAND, "Right view docking state: docked", false, Docking.DOCKED);
			rightViewDockingDropItem.addItem("Undocked", Images.APPLICATION_SIDE_EXPAND, "Right view docking state: undocked", false, Docking.UNDOCKED);
			rightViewDockingDropItem.addItem("Invisible", Images.APPLICATION_SIDE_EXPAND, "Right view docking state: invisible", false, Docking.INVISIBLE);
			dockingToolItems.put(Position.RIGHT, rightViewDockingDropItem);
			rightViewDockingDropItem.selectItem(0, false);
//			rightViewDockingDropItem.ti.addSelectionListener(new DockingSl(rightViewDockingDropItem, Position.RIGHT));
			
			bottomViewDockingDropItem = new DropDownToolItem(toolBar, false, true, SWT.RADIO, preInsertIndex++);
			bottomViewDockingDropItem.addItem("Docked", Images.APPLICATION_SIDE_PUT, "Bottom view docking state: docked", false, Docking.DOCKED);
			bottomViewDockingDropItem.addItem("Undocked", Images.APPLICATION_SIDE_PUT, "Bottom view docking state: undocked", false, Docking.UNDOCKED);
			bottomViewDockingDropItem.addItem("Invisible", Images.APPLICATION_SIDE_PUT, "Bottom view docking state: invisible", false, Docking.INVISIBLE);
			dockingToolItems.put(Position.BOTTOM, bottomViewDockingDropItem);
			bottomViewDockingDropItem.selectItem(0, false);
//			bottomViewDockingDropItem.ti.addSelectionListener(new DockingSl(bottomViewDockingDropItem, Position.BOTTOM));
			}
		}
		
		else {
		leftViewVisibleToggle = new ToolItem(toolBar, SWT.CHECK, preInsertIndex++);
		leftViewVisibleToggle.setToolTipText("Show left view");
		leftViewVisibleToggle.setImage(Images.getOrLoad("/icons/application_side_contract.png"));
		
		rightViewVisibleToggle = new ToolItem(toolBar, SWT.CHECK, preInsertIndex++);
		rightViewVisibleToggle.setToolTipText("Show right view");
		rightViewVisibleToggle.setImage(Images.getOrLoad("/icons/application_side_expand.png"));		
		
		bottomViewVisibleToggle = new ToolItem(toolBar, SWT.CHECK, preInsertIndex++);
		bottomViewVisibleToggle.setToolTipText("Show bottom view");
		bottomViewVisibleToggle.setImage(Images.getOrLoad("/icons/application_put.png"));
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
		uploadDocsItem.setImage(Images.getOrLoad("/icons/folder_import.png"));
		
		exportDocumentButton = new ToolItem(toolBar, SWT.PUSH, preInsertIndex++);
		exportDocumentButton.setToolTipText("Export document");
		exportDocumentButton.setImage(Images.getOrLoad("/icons/folder_go.png"));
		exportDocumentButton.setEnabled(false);		
		
		if (false) {
		closeDocBtn = new ToolItem(toolBar, SWT.PUSH, preInsertIndex++);
		closeDocBtn.setToolTipText("Close document");
		closeDocBtn.setImage(Images.getOrLoad("/icons/cancel.png"));
		}
				
//		saveDropItem = new DropDownToolItem(toolBar, false, true, SWT.RADIO, preInsertIndex++);
//		saveDropItem.addItem("Save page", Images.getOrLoad("/icons/disk.png"), "");
//		saveDropItem.addItem("Save page with commit message", Images.getOrLoad("/icons/disk_message.png"), "");

//		saveTranscriptAlwaysButton = new ToolItem(toolBar, SWT.CHECK, preInsertIndex++);
//		saveTranscriptAlwaysButton.setToolTipText("Save page always, i.e. on every change of the page - this means the page is also saved when no changes happened -> use this option with care!");
//		saveTranscriptAlwaysButton.setImage(Images.getOrLoad("/icons/disk_multiple.png"));
//		new ToolItem(toolBar, SWT.SEPARATOR, preInsertIndex++);
		
		if (false) {
		reloadDocumentButton = new ToolItem(toolBar, SWT.PUSH, preInsertIndex++);
		reloadDocumentButton.setToolTipText("Reload document");
		reloadDocumentButton.setImage(Images.getOrLoad("/icons/refresh.gif"));
		}
		
		/*
		 * moved to Main Menu
		 */
//		deletePageButton = new ToolItem(toolBar, SWT.PUSH, preInsertIndex++);
//		deletePageButton.setToolTipText("Delete page on server");
//		deletePageButton.setImage(Images.IMAGE_DELETE);
//		deletePageButton.setEnabled(false);
		
//		new ToolItem(toolBar, SWT.SEPARATOR, preInsertIndex++);
		
		searchBtn = new ToolItem(toolBar, SWT.PUSH, preInsertIndex++);
		searchBtn.setToolTipText("Search for documents, keywords... tbc");
		searchBtn.setImage(Images.getOrLoad("/icons/find.png"));
		
		if (false) {
		exportPdfButton = new ToolItem(toolBar, SWT.PUSH, preInsertIndex++);
		exportPdfButton.setToolTipText("Export document as PDF");
		exportPdfButton.setImage(Images.getOrLoad("/icons/page_white_acrobat.png"));
		exportPdfButton.setEnabled(false);
		
		exportTeiButton = new ToolItem(toolBar, SWT.PUSH, preInsertIndex++);
		exportTeiButton.setToolTipText("Export document as TEI XML");
		exportTeiButton.setImage(Images.getOrLoad("/icons/page_white_code.png"));
		exportTeiButton.setEnabled(false);
		
		exportRtfButton = new ToolItem(toolBar, SWT.PUSH, preInsertIndex++);
		exportRtfButton.setToolTipText("Export document as RTF");
		exportRtfButton.setImage(Images.getOrLoad("/icons/page_white_word.png"));
		exportRtfButton.setEnabled(false);
		}
				
//		new ToolItem(toolBar, SWT.SEPARATOR, 3);
		
//		new ToolItem(toolBar, SWT.SEPARATOR, preInsertIndex++);
		
		if (!DISABLE_EXPLICIT_VISIBILITY_BTNS) {
		showPrintSpaceToggle = new ToolItem(toolBar, SWT.CHECK);
		showPrintSpaceToggle.setToolTipText("Show printspace (F1)");
		showPrintSpaceToggle.setImage(Images.getOrLoad("/icons/show_ps_shape.png"));
		
		showRegionsToggle= new ToolItem(toolBar, SWT.CHECK);
		showRegionsToggle.setToolTipText("Show regions (F2)");
		showRegionsToggle.setImage(Images.getOrLoad("/icons/show_regions_shape.png"));
		
		showLinesToggle= new ToolItem(toolBar, SWT.CHECK);
		showLinesToggle.setToolTipText("Show lines (F3)");
		showLinesToggle.setImage(Images.getOrLoad("/icons/show_lines_shape.png"));
		
		showBaselinesToggle= new ToolItem(toolBar, SWT.CHECK);
		showBaselinesToggle.setToolTipText("Show baselines (F4)");
		showBaselinesToggle.setImage(Images.getOrLoad("/icons/show_baselines_shape.png"));
		
		showWordsToggle= new ToolItem(toolBar, SWT.CHECK);
		showWordsToggle.setToolTipText("Show words (F5)");
		showWordsToggle.setImage(Images.getOrLoad("/icons/show_word_shape.png"));
		}
		
		// NEW view item:
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
		
//		new ToolItem(toolBar, SWT.SEPARATOR);
//		
//		renderBlackeningsToggle = new ToolItem(toolBar, SWT.CHECK);
//		renderBlackeningsToggle.setToolTipText("If toggled, blackening regions are rendered with opaque background");
//		//renderBlackeningsToggle.setText("Render blackenings");
//		renderBlackeningsToggle.setImage(Images.getOrLoad("/icons/rabbit-silhouette.png"));
		
//		showWordsToggle.setImage(Images.getOrLoad("/icons/show_word_shape.png"));
		
		if (false) {
		new ToolItem(toolBar, SWT.SEPARATOR);
		
		//showReadingOrderToolItem = new DropDownToolItem(toolBar, false, false, SWT.CHECK);

		
		showReadingOrderRegionsItem = new ToolItem(toolBar, SWT.CHECK);
		showReadingOrderRegionsItem.setToolTipText("Show reading order of regions");
		showReadingOrderRegionsItem.setImage(Images.getOrLoad("/icons/reading_order_r.png"));
		
		showReadingOrderLinesItem = new ToolItem(toolBar, SWT.CHECK);
		showReadingOrderLinesItem.setToolTipText("Show reading order of lines");
		showReadingOrderLinesItem.setImage(Images.getOrLoad("/icons/reading_order_l.png"));
		
		showReadingOrderWordsItem = new ToolItem(toolBar, SWT.CHECK);
		showReadingOrderWordsItem.setToolTipText("Show reading order of words");
		showReadingOrderWordsItem.setImage(Images.getOrLoad("/icons/reading_order_w.png"));
		
//		showReadingOrderToolItem.addItem("Show reading order of lines", Images.getOrLoad( "/icons/reading_order_l.png"), "Show the reading order of all lines on this page", SWT.NONE);
//		showReadingOrderToolItem.addItem("Show reading order of words", Images.getOrLoad("/icons/reading_order_w.png"), "Show the reading order of all words on this page", SWT.NONE);
//		showReadingOrderRegionsItem = showReadingOrderToolItem.addItem("Show reading order of regions", Images.getOrLoad("/icons/reading_order_r.png"), "Show the reading order of all text or image or graphics regions");
//		showReadingOrderLinesItem = showReadingOrderToolItem.addItem("Show reading order of lines", Images.getOrLoad( "/icons/reading_order_l.png"), "Show the reading order of all lines on this page");
//		showReadingOrderWordsItem = showReadingOrderToolItem.addItem("Show reading order of words", Images.getOrLoad("/icons/reading_order_w.png"), "Show the reading order of all words on this page");
		
		//showReadingOrderToolItem.ti.setImage( Images.getOrLoad("/icons/readingOrder.png"));
		
		//showReadingOrderToolItem.addItem("Show reading order of all shapes", Images.getOrLoad("/icons/readingOrder.png"), "Show the reading order of all shapes on this page", SWT.NONE);
		}
				
		if (TrpSettings.ENABLE_LINE_EDITOR) {
			new ToolItem(toolBar, SWT.SEPARATOR);			
			showLineEditorToggle = new ToolItem(toolBar, SWT.CHECK);
			showLineEditorToggle.setImage(Images.getOrLoad("/icons/pencil.png"));
			showLineEditorToggle.setToolTipText("Show line transcription editor");
		}
		
//		new ToolItem(toolBar, SWT.SEPARATOR);
//		sendBugReportButton = new ToolItem(toolBar, SWT.PUSH);
//		sendBugReportButton.setImage(Images.BUG);
//		sendBugReportButton.setToolTipText("Send a bug report / feature request");
		
		toolBar.pack();
		
		getShell().addListener(SWT.Resize, new Listener() {
		      @Override
			public void handleEvent(Event e) {
		    	  updateToolBarSize();
		      }
		    });
		
//		cbItem.setControl(pagesPagingToolBar.getToolBar());
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
	
	private void addInternalListener() {
		// segmentation menu listener:
		menuListener = new MenuListener(this);
		
		if (!DISABLE_OLD_DOCK_STATE_BTNS) {
		// if 'selection' event in PortalWidget, then some dock status has changed -> adjust button selection!
		portalWidget.addListener(SWT.Selection, new Listener() {
			@Override public void handleEvent(Event event) {
				Position pos = (Position) event.data;
				Docking docking = portalWidget.getDocking(pos);
				logger.debug("selection event in PortalWidget, pos = "+pos+" docking = "+docking);
				
				MenuItem mi = dockingToolItems.get(pos).getItemWithData(docking);
				if (mi!=null)
					dockingToolItems.get(pos).selectItem(mi, false);
				
//				MenuItem mi2 = dockingMenuItems.get(new PositionDocking(pos, docking));
//				logger.debug("mi2 = "+mi2);
//				
//				if (mi2 != null) {
//					mi2.setSelection(true);
//				}
			}
		});
		}

		// if 'selection' event in PortalWidget, then some dock status has changed -> adjust button selection!
		portalWidget.addListener(SWT.Selection, new Listener() {
			@Override public void handleEvent(Event event) {
				updateDockingStateButtons();
			}
		});
		
		if (!DISABLE_OLD_DOCK_STATE_BTNS) {
		// set docking state in PortalWidget according to button selections:
		class DockingItemSelectionListener extends SelectionAdapter {
			Position pos;
			DropDownToolItem item;
			
			public DockingItemSelectionListener(DropDownToolItem item, PortalWidget.Position pos) {
				this.item = item;
				this.pos = pos;
			}
			
			@Override public void widgetSelected(SelectionEvent e) {
				if (e.detail != SWT.ARROW && e.detail == DropDownToolItem.IS_DROP_DOWN_ITEM_DETAIL) {
					logger.debug("widgetSelected: "+item.getSelected().getData());
					portalWidget.setWidgetDockingType(pos, (Docking) item.getSelected().getData());
				}
			}
		};
		leftViewDockingDropItem.ti.addSelectionListener(new DockingItemSelectionListener(leftViewDockingDropItem, PortalWidget.Position.LEFT));
		rightViewDockingDropItem.ti.addSelectionListener(new DockingItemSelectionListener(rightViewDockingDropItem, PortalWidget.Position.RIGHT));
		bottomViewDockingDropItem.ti.addSelectionListener(new DockingItemSelectionListener(bottomViewDockingDropItem, PortalWidget.Position.BOTTOM));
		}

//		trpSets.addPropertyChangeListener(new PropertyChangeListener() {
//			@Override
//			public void propertyChange(PropertyChangeEvent evt) {
//				if (evt.getPropertyName().equals(TrpSettings.SHOW_LEFT_VIEW_PROPERTY)) {
//					portalWidget.setWidgetDockingType(PortalWidget.Position.LEFT, trpSets.isShowLeftView()?PortalWidget.Docking.DOCKED:PortalWidget.Docking.INVISIBLE);
////					portalWidget.setLeftViewVisible(trpSets.isShowLeftView());				
//				}
//				else if (evt.getPropertyName().equals(TrpSettings.SHOW_RIGHT_VIEW_PROPERTY)) {
//					portalWidget.setWidgetDockingType(PortalWidget.Position.RIGHT, trpSets.isShowRightView()?PortalWidget.Docking.DOCKED:PortalWidget.Docking.INVISIBLE);
////					portalWidget.setRightViewVisible(trpSets.isShowRightView());				
//				}				
//				else if (evt.getPropertyName().equals(TrpSettings.SHOW_BOTTOM_VIEW_PROPERTY)) {
//					portalWidget.setWidgetDockingType(PortalWidget.Position.BOTTOM, trpSets.isShowBottomView()?PortalWidget.Docking.DOCKED:PortalWidget.Docking.INVISIBLE);
////					portalWidget.setBottomViewVisible(trpSets.isShowBottomView());				
//				}
//			}
//		});		
	}
	
	private void addBindings() {
		DataBinder db = DataBinder.get();
				
		db.bindBeanPropertyToObservableValue(TrpSettings.LEFT_VIEW_DOCKING_STATE_PROPERTY, trpSets, 
				Observables.observeMapEntry(portalWidget.getDockingMap(), Position.LEFT));
		db.bindBeanPropertyToObservableValue(TrpSettings.RIGHT_VIEW_DOCKING_STATE_PROPERTY, trpSets,
				Observables.observeMapEntry(portalWidget.getDockingMap(), Position.RIGHT));
		db.bindBeanPropertyToObservableValue(TrpSettings.BOTTOM_VIEW_DOCKING_STATE_PROPERTY, trpSets, 
				Observables.observeMapEntry(portalWidget.getDockingMap(), Position.BOTTOM));
		
		if (!DISABLE_EXPLICIT_VISIBILITY_BTNS) {
		db.bindBoolBeanValueToToolItemSelection(TrpSettings.SHOW_PRINTSPACE_PROPERTY, trpSets, showPrintSpaceToggle);
		db.bindBoolBeanValueToToolItemSelection(TrpSettings.SHOW_TEXT_REGIONS_PROPERTY, trpSets, showRegionsToggle);
		db.bindBoolBeanValueToToolItemSelection(TrpSettings.SHOW_LINES_PROPERTY, trpSets, showLinesToggle);
		db.bindBoolBeanValueToToolItemSelection(TrpSettings.SHOW_BASELINES_PROPERTY, trpSets, showBaselinesToggle);
		db.bindBoolBeanValueToToolItemSelection(TrpSettings.SHOW_WORDS_PROPERTY, trpSets, showWordsToggle);
		}
		
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_PRINTSPACE_PROPERTY, trpSets, showPrintspaceItem);
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_TEXT_REGIONS_PROPERTY, trpSets, showRegionsItem);
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_LINES_PROPERTY, trpSets, showLinesItem);
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_BASELINES_PROPERTY, trpSets, showBaselinesItem);
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_WORDS_PROPERTY, trpSets, showWordsItem);
		db.bindBeanToWidgetSelection(TrpSettings.RENDER_BLACKENINGS_PROPERTY, trpSets, renderBlackeningsItem);	
		
		//db.bindBoolBeanValueToToolItemSelection(TrpSettings.RENDER_BLACKENINGS_PROPERTY, trpSets, renderBlackeningsToggle);
		
		//db.bindBoolBeanValueToToolItemSelection(TrpSettings.SHOW_READING_ORDER_PROPERTY, trpSets, showReadingOrderToolItem);
		
//		db.bindBoolBeanValueToToolItemSelection(TrpSettings.SHOW_LEFT_VIEW_PROPERTY, trpSets, leftViewVisibleToggle);
//		db.bindBoolBeanValueToToolItemSelection(TrpSettings.SHOW_RIGHT_VIEW_PROPERTY, trpSets, rightViewVisibleToggle);
//		db.bindBoolBeanValueToToolItemSelection(TrpSettings.SHOW_BOTTOM_VIEW_PROPERTY, trpSets, bottomViewVisibleToggle);
		
		if (TrpSettings.ENABLE_LINE_EDITOR)
			db.bindBoolBeanValueToToolItemSelection(TrpSettings.SHOW_LINE_EDITOR_PROPERTY, trpSets, showLineEditorToggle);
		
//		db.bindBeanToWidgetSelection(TrpSettings.SHOW_LEFT_VIEW_PROPERTY, trpSets, menu.getShowLeftViewMenuItem());
//		db.bindBeanToWidgetSelection(TrpSettings.SHOW_BOTTOM_VIEW_PROPERTY, trpSets, menu.getShowBottomViewMenuItem());
		
//		db.bindBoolBeanValueToToolItemSelection(TrpSettings.RECT_MODE_PROPERTY, trpSets, canvasWidget.getToolBar().getShapeAddRectMode());
//		db.bindBoolBeanValueToToolItemSelection(TrpSettings.AUTO_CREATE_PARENT_PROPERTY, trpSets, canvasWidget.getToolBar().getAutoCreateParent());
		
		db.bindBeanToWidgetSelection(TrpSettings.RECT_MODE_PROPERTY, trpSets, canvasWidget.getToolbar().getRectangleModeItem());
		db.bindBeanToWidgetSelection(TrpSettings.AUTO_CREATE_PARENT_PROPERTY, trpSets, canvasWidget.getToolbar().getAutoCreateParentItem());
		
		db.bindBeanToWidgetSelection(TrpSettings.ADD_LINES_TO_OVERLAPPING_REGIONS_PROPERTY, trpSets, canvasWidget.getToolbar().getAddLineToOverlappingRegionItem());
		db.bindBeanToWidgetSelection(TrpSettings.ADD_BASELINES_TO_OVERLAPPING_LINES_PROPERTY, trpSets, canvasWidget.getToolbar().getAddBaselineToOverlappingLineItem());
		db.bindBeanToWidgetSelection(TrpSettings.ADD_WORDS_TO_OVERLAPPING_LINES_PROPERTY, trpSets, canvasWidget.getToolbar().getAddWordsToOverlappingLineItem());
		
		db.bindBeanToWidgetSelection(CanvasSettings.LOCK_ZOOM_ON_FOCUS_PROPERTY, TrpConfig.getCanvasSettings(), canvasWidget.getToolbar().getLockZoomOnFocusItem());
		
		db.bindBeanToWidgetSelection(TrpSettings.DELETE_LINE_IF_BASELINE_DELETED_PROPERTY, trpSets, canvasWidget.getToolbar().getDeleteLineIfBaselineDeletedItem());
		
		db.bindBeanToWidgetSelection(TrpSettings.SELECT_NEWLY_CREATED_SHAPE_PROPERTY, trpSets, canvasWidget.getToolbar().getSelectNewlyCreatedShapeItem());
		
		db.bindBoolBeanValueToToolItemSelection(TrpSettings.SHOW_READING_ORDER_REGIONS_PROPERTY, trpSets, showReadingOrderRegionsItem);
		db.bindBoolBeanValueToToolItemSelection(TrpSettings.SHOW_READING_ORDER_LINES_PROPERTY, trpSets, showReadingOrderLinesItem);
		db.bindBoolBeanValueToToolItemSelection(TrpSettings.SHOW_READING_ORDER_WORDS_PROPERTY, trpSets, showReadingOrderWordsItem);
		
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_READING_ORDER_REGIONS_PROPERTY, trpSets, showReadingOrderRegionsMenuItem);
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_READING_ORDER_LINES_PROPERTY, trpSets, showReadingOrderLinesMenuItem);
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_READING_ORDER_WORDS_PROPERTY, trpSets, showReadingOrderWordsMenuItem);		
				
//		db.bindBeanToWidgetSelection(TrpSettings.ENABLE_INDEXED_STYLES, trpSets, metadataWidget.getTextStyleWidget().getEnableIndexedStylesBtn());
		
//		DataBinder.get().bindWidgetSelection(menu.getSaveTranscriptionMenuItem(), saveTranscriptButton);
//		DataBinder.get().bindBeanToWidgetSelection(TrpSettings.SHOW_BOTTOM_VIEW_PROPERTY, trpSets, menu.getShowBottomViewMenuItem());
	}
	
//	public ToolItem getLeftViewVisibleToggle() {
//		return leftViewVisibleToggle;
//	}

//	private void initAddShapeActionStuff() {
//		// add actions for shapes to draw -----------------------------------------------------------
//		CanvasToolBar tb = canvasWidget.getToolBar();
//		
//		addShapeActionCombo = new ComboToolItem(tb, SWT.DROP_DOWN | SWT.READ_ONLY, tb.indexOf(tb.getAddShape())+1);
//		addShapeActionCombo.getCombo().setToolTipText("Determines which type of shape is added");
//		
////		addShapeActionComboToolItem = new ToolItem(tb, SWT.SEPARATOR, tb.indexOf(tb.getAddShape())+1);
////		addShapeActionComboToolItem.setImage(null);
////		addShapeActionCombo = new Combo(tb, SWT.DROP_DOWN | SWT.READ_ONLY);
////		addShapeActionCombo.setToolTipText("Determines which type of shape is added");
////		addShapeActionComboToolItem.setControl(addShapeActionCombo);
////		addShapeActionComboToolItem.setWidth(addShapeActionCombo.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
//		
//		for (String segType : SegmentationTypes.TYPE_TO_SHAPES_MAP.keySet()) {
//			addShapeActionCombo.getCombo().add(segType);
//			
////			canvasWidget.addAddShapeAction(type, SegmentationTypes.getShapeToDraw(type));
////			addAddShapeAction(segType, SegmentationTypes.getShapeToDraw(segType));
//		}
//		addShapeActionCombo.getCombo().select(0);
////		tb.getLayout();
//		
//		shapeTypeCombo = new ComboToolItem(tb, SWT.DROP_DOWN | SWT.READ_ONLY, tb.indexOf(tb.getAddShape())+2);
//		shapeTypeCombo.getCombo().setToolTipText("Determines the shape with which to segment");
//		updateShapeToDrawCombo();
//		
////		shapeTypeCombo.getCombo().addSelectionListener(new SelectionAdapter() {
////			@Override
////			public void widgetSelected(SelectionEvent e) {
////				setShapeToDraw();
////			}
////		});
//		
//		// change shape to draw on selection changed:
//		addShapeActionCombo.getCombo().addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				updateShapeToDrawCombo();
////				logger.debug("add shape type changed "+getSelectedAddShapeActionText()+ " shape to draw = "+getSelectedShapeActionShapeToDraw());
////				canvas.setShapeToDraw(getSelectedShapeActionShapeToDraw());
//			}
//		});
//		
//		
//		// if not in selection mode, disable add shape action combo box:
//		getCanvas().getSettings().addPropertyChangeListener(new PropertyChangeListener() {
//			@Override
//			public void propertyChange(PropertyChangeEvent evt) {
//				logger.debug("setttings changed - updating edit status!");
//				addShapeActionCombo.setEnabled(canvasWidget.getCanvas().getSettings().getMode() == CanvasMode.SELECTION);
//			}
//		});
//		
//		// -------------------------------------------------------------------------------
//		
//	}
//	
//	private void updateShapeToDrawCombo() {
//		String shapeAction = getSelectedAddShapeActionText();
//		logger.debug("updateShapeToDrawCombo, shapeAction = "+shapeAction);
//		
//		shapeTypeCombo.getCombo().removeAll();
//		for (CanvasShapeType st : SegmentationTypes.TYPE_TO_SHAPES_MAP.get(shapeAction)) {
//			shapeTypeCombo.getCombo().add(st.toString());
//		}
//		shapeTypeCombo.getCombo().select(0);
//		
////		setShapeToDraw();
//	}
	
//	private void setShapeToDraw() {
//		String shapeType = shapeTypeCombo.getCombo().getItem(shapeTypeCombo.getCombo().getSelectionIndex());
//		
//		logger.debug("setShapeToDraw: "+CanvasShapeType.fromString(shapeType));
//		canvas.getShapeEditor().setShapeToDraw(CanvasShapeType.fromString(shapeType));
//	}
	
	
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
	
	/**
	 * Adds a shape action to the corresponding Combo. 
	 * If an item with the given text already exists it will not be added again and false is returned
	 */
//	private boolean addAddShapeAction(String text, Class<? extends ICanvasShape> shapeToDraw) {
//		for (String itemText : addShapeActionCombo.getItems()) {
//			if (itemText.equals(text))
//				return false;
//		}
//		
//		addShapeActionCombo.add(text);
//		shapeActionMap.put(text, shapeToDraw);
//		
//		// select first if first added:
//		if (addShapeActionCombo.getSelectionIndex() < 0) {
//			addShapeActionCombo.select(0);
//			canvasWidget.getCanvas().getShapeEditor().setShapeToDraw(shapeToDraw);
//		}
//		
//		addShapeActionComboToolItem.setWidth(addShapeActionCombo.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
//		return true;
//	}	
	
//	public String getSelectedAddShapeActionText() {
//		return addShapeActionCombo.getCombo().getText();
//	}
	
//	public Class<? extends ICanvasShape> getSelectedShapeActionShapeToDraw() {
//		return shapeActionMap.get(getSelectedAddShapeActionText());
//	}
	
//	public CCombo getAddShapeActionCombo() { return addShapeActionCombo.getCombo(); }
//	@Deprecated public void updateTreeColumnSize() { structureTreeWidget.updateTreeColumnSize(); }
//	public Tree getSegmentationElementsTree() { return treeWidget.getTree(); }
	
	public TrpMenuBar getTrpMenuBar() { return menu; }
	public StructureTreeWidget getStructureTreeWidget() { return structureTreeWidget; }
	public DocOverviewWidget getDocOverviewWidget() { return docOverviewWidget; }
	public JobTableWidgetPagination getJobOverviewWidget() { return jobOverviewWidget; }
	public TranscriptsTableWidgetPagination getVersionsWidget() { return versionsWidget; };
	
	public CanvasWidget getCanvasWidget() { return canvasWidget; }
	public CanvasToolBar getCanvasToolBar() { return canvasWidget.getToolbar(); }
	
	public TrpSWTCanvas getCanvas() {
//		return canvasWidget.getCanvas();
		
		if (canvasWidget != null && canvasWidget.getCanvas()!=null)
			return canvasWidget.getCanvas();
		else
			return null;
	}
	
	public PagingToolBar getPagesPagingToolBar() { return this.pagesPagingToolBar; }
//	public PagingToolBar getTranscriptsPagingToolBar() { return this.transcriptsPagingToolBar; }
//	public ToolItem getUpdateIDsItem() { return structureTreeWidget.getUpdateIDsItem(); }
	
//	public ToolItem getReloadTranscriptsButton() { return reloadTranscriptsButton; }
	
//	public ToolItem getShowLineEditorToggle() { return showLineEditorToggle; }
	
	public ToolItem getReloadDocumentButton() { return reloadDocumentButton; }
	public ToolItem getExportDocumentButton() { return exportDocumentButton; }
	public ToolItem getReplacePageImgButton() { return replacePageImgButton; }
	//public ToolItem getDeletePageButton() { return deletePageButton; }
	public ToolItem getExportPdfButton() { return exportPdfButton; }
	public ToolItem getExportTeiButton() { return exportTeiButton; }
	public ToolItem getExportRtfButton() { return exportRtfButton; }
	public MenuItem getSaveTranscriptButton() { return saveTranscriptButton; }
	public MenuItem getSaveTranscriptWithMessageButton() { return saveTranscriptWithMessageButton; }
//	public DropDownToolItem getSaveDropItem() { return saveDropItem; }
	public ToolItem getOpenLocalFolderButton() { return openLocalFolderButton; }
	public ToolItem getCloseDocBtn() { return closeDocBtn; }
//	public ToolItem getSaveTranscriptAlwaysButton() { return saveTranscriptAlwaysButton; }
	public ToolItem getLoadTranscriptInTextEditor() { return loadTranscriptInTextEditor; }
//	public ToolItem getSendBugReportButton() { return sendBugReportButton; }
	
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
	
	public ToolItem getLoginToggle() { return loginToggle; }
	
	public PageMetadataWidget getMetadataWidget() { return metadataWidget; }
	public TrpVirtualKeyboardsWidget getVkeyboards() { return vkeyboards; }
	public ToolsWidget getToolsWidget() { return toolsWidget; }
//	public AnalyticsWidget getAnalyticsWidget() { return analyticsWidget; }
		
	public void updateLoginInfo(boolean loggedIn, String username, String server) {
		if (loggedIn) {
			docOverviewWidget.getUsernameLabel().setText("Logged in as: "+username);
			docOverviewWidget.getServerLabel().setText("Server: "+server);

			loginToggle.setToolTipText("Logout "+username);
//			loginToggle.setSelection(true);
			loginToggle.setImage(Images.getOrLoad("/icons/connect.png"));
		} else {
			docOverviewWidget.getUsernameLabel().setText("Not logged in");
			docOverviewWidget.getServerLabel().setText("");
			loginToggle.setToolTipText("Login");
//			loginToggle.setSelection(false);
			loginToggle.setImage(Images.getOrLoad("/icons/disconnect.png"));
		}
		
		docOverviewWidget.getUsernameLabel().pack();
		docOverviewWidget.getServerLabel().pack();
		
		uploadDocsItem.setEnabled(loggedIn);
		searchBtn.setEnabled(loggedIn);
		
		loginToggle.setSelection(loggedIn);
	}

	public void center() {
		Monitor primary = Display.getCurrent().getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = getShell().getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;

		getShell().setLocation(x, y);
	}

	public TaggingWidget getTaggingWidgetNew() {
		return taggingWidget;
	}

	public CommentsWidget getCommentsWidget() {
		return commentsWidget;
	}

//	public DropDownToolItem getShowReadingOrderToolItem() {
//		return showReadingOrderToolItem;
//	}
	
	public DropDownToolItem getProfilesToolItem() {
		return profilesToolItem;
	}
	
	public PortalWidget getPortalWidget() {
		return portalWidget;
	}
	
//	public DropDownToolItem getLanguageDropDown() {
//		return languageDropDown;
//	}
	
	public ThumbnailWidget getThumbnailWidget() {
		return thumbnailWidget;
	}

	public DropDownToolItem getSaveDropDown() {
		return saveDrowDown;
	}

	public DocMetadataWidget getDocMetadataWidget() {
		return docMetadataWidget;
	}

}
