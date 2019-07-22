package eu.transkribus.swt_gui.doc_overview;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TextToolItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.collection_comboviewer.CollectionSelectorWidget;
import eu.transkribus.swt_gui.htr.treeviewer.GroundTruthTreeWidget;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthLabelAndFontProvider;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.util.RecentDocsComboViewerWidget;

public class ServerWidget extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(ServerWidget.class);

	Label usernameLabel;
	DocTableWidgetPagination docTableWidget;

//	CollectionComboViewerWidget collectionComboViewerWidget;
//	CollectionComboViewerWidget collectionComboViewerWidget;
	CollectionSelectorWidget collectionSelectorWidget;
	
	CTabFolder tabFolder;
	CTabItem documentsTabItem, gtTabItem;
	
	GroundTruthTreeWidget groundTruthTreeWidget;
//	TreeViewer groundTruthTv;
	
	RecentDocsComboViewerWidget recentDocsComboViewerWidget;
	
	Button manageCollectionsBtn;
	Button showActivityWidgetBtn;
	Text quickLoadByID;
	
	MenuItem openLocalDocBtn;
	MenuItem importBtn;
	MenuItem exportBtn;
	MenuItem findBtn;
	
	Menu editCollectionMenu;
	MenuItem collectionUsersBtn;
	MenuItem createCollectionBtn;
	MenuItem deleteCollectionBtn, modifyCollectionBtn;
	
	Button openEditCollectionMenuBtn;

	Storage store = Storage.getInstance();
	Composite remoteDocsGroup;
	Composite container;
	
	Button loginBtn;
		
	int selectedId=-1;
	
	Button docManager, userManager;
	Button showJobsBtn, showVersionsBtn;
	
	ServerWidgetListener serverWidgetListener;
	
	List<Control> userControls = new ArrayList<>();
	
	Menu docOverviewMenu;
	
	MenuItem addToCollectionMenuItem;
	MenuItem removeFromCollectionMenuItem;
	MenuItem deleteDocMenuItem;
	MenuItem duplicateDocMenuItem;
	
	ToolItem addToCollectionTi, removeFromCollectionTi, deleteDocTi, duplicateDocTi, administerCollectionTi, recycleBin;
	TextToolItem quickLoadByDocId;
		
	public ServerWidget(Composite parent) {
		super(parent, SWT.NONE);
				
		init();
		addListener();
		updateLoggedIn();
	}
	
	private void addListener() {
		serverWidgetListener = new ServerWidgetListener(this);
	}
	
	void updateLoggedIn() {
		boolean isLoggedIn = store.isLoggedIn();
		
		for (Control c : userControls) {			
			c.setEnabled(isLoggedIn);
		}
		
		if (!isLoggedIn) {
			setSelectedCollection(null);
			clearDocList();
			updateGroundTruthTreeViewer();
		}
	}
	
	private void init() {
		this.setLayout(new GridLayout());
				
		container = new Composite(this, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		container.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		
		loginBtn = new Button(container, 0);
		loginBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		loginBtn.setImage(Images.DISCONNECT);
		Fonts.setBoldFont(loginBtn);
		
		Composite btns1 = new Composite(container, 0);
		btns1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btns1.setLayout(SWTUtil.createGridLayout(2, true, 0, 0));
		
		
		
		
//		DropDownButton docDropDown = new DropDownButton(btns1, SWT.PUSH, "Document...", Images.FOLDER, null);
//		docDropDown.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		userControls.add(docDropDown);
//		
//		openLocalDocBtn = docDropDown.addItem("Open local document...", Images.FOLDER);
//		importBtn = docDropDown.addItem("Import document to server...", Images.FOLDER_IMPORT);
//		exportBtn = docDropDown.addItem("Export document to your local machine...", Images.FOLDER_GO);
//		findBtn = docDropDown.addItem("Find documents, text or tags...", Images.FIND);
		
//		if (false) {
//		Composite docsComposite = new Composite(btns1, 0);
//		docsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
//		docsComposite.setLayout(SWTUtil.createGridLayout(4, false, 3, 3));
//		        		
//		openLocalDocBtn = new Button(docsComposite, SWT.PUSH);
//		openLocalDocBtn.setText("Open");
//		openLocalDocBtn.setToolTipText("Open a local document from a folder of images");
//		openLocalDocBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		openLocalDocBtn.setImage(Images.FOLDER);
//		
//		importBtn = new Button(docsComposite, SWT.PUSH);
//		importBtn.setText("Import");
//		importBtn.setToolTipText("Import a document to the server");
//		importBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		importBtn.setImage(Images.FOLDER_IMPORT);
//		
//		exportBtn = new Button(docsComposite, SWT.PUSH);
//		exportBtn.setText("Export");
//		exportBtn.setToolTipText("Export the current document to your local machine");
//		exportBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		exportBtn.setImage(Images.FOLDER_GO);
//		
//		findBtn = new Button(docsComposite, SWT.PUSH);
//		findBtn.setText("Find");
//		findBtn.setToolTipText("Find documents, text or tags");
//		findBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		findBtn.setImage(Images.FIND);
//		}
		
		docManager = new Button(btns1, 0);
		docManager.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		docManager.setText("Document Manager");
		docManager.setToolTipText("Add pages, add transcripts, choose symbolic images,...");
		docManager.setImage(Images.FOLDER_WRENCH);
		userControls.add(docManager);
			
		userManager = new Button(btns1, 0);
		userManager.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		userManager.setText("User Manager");
		userManager.setToolTipText("Add/Remove users to your collections");
		userManager.setImage(Images.USER_EDIT);
		userControls.add(userManager);
		
		showVersionsBtn = new Button(btns1, 0);
		showVersionsBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		showVersionsBtn.setText("Versions");
		showVersionsBtn.setToolTipText("Show versions of the current page");
		showVersionsBtn.setImage(Images.PAGE_WHITE_STACK);
		userControls.add(showVersionsBtn);
		
		showJobsBtn = new Button(btns1, 0);
		showJobsBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		showJobsBtn.setText("Jobs");
		showJobsBtn.setToolTipText("Show jobs on server");
		showJobsBtn.setImage(Images.CUP);
		userControls.add(showJobsBtn);
		
//		manageCollectionsBtn = new Button(btns1, SWT.PUSH);
//		manageCollectionsBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		manageCollectionsBtn.setImage(Images.getOrLoad("/icons/folder_edit.png"));
//		manageCollectionsBtn.setText("Manage collections...");
//		userControls.add(manageCollectionsBtn);
	
		recentDocsComboViewerWidget = new RecentDocsComboViewerWidget(btns1, 0);
		recentDocsComboViewerWidget.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		userControls.add(recentDocsComboViewerWidget);
		
		showActivityWidgetBtn = new Button(btns1, SWT.PUSH);
		showActivityWidgetBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		showActivityWidgetBtn.setImage(Images.GROUP);
		showActivityWidgetBtn.setText("User activity");
		userControls.add(showActivityWidgetBtn);
		
		Label collectionsLabel = new Label(container, 0);
		collectionsLabel.setText("Collections:");
		Fonts.setBoldFont(collectionsLabel);
		
		collectionSelectorWidget = new CollectionSelectorWidget(container, 0, false, null);
		collectionSelectorWidget.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		if (false) {
			Composite collComp = collectionSelectorWidget.getCollComposite();
//			collComp.setLayout(SWTUtil.createGridLayout(2, false, 0, 0)); // have to change nr of columns to add a new buttons
			openEditCollectionMenuBtn = new Button(collComp, SWT.PUSH);
			openEditCollectionMenuBtn.setImage(Images.PENCIL);
			openEditCollectionMenuBtn.setToolTipText("Manage collection...");
			openEditCollectionMenuBtn.addSelectionListener(new SelectionAdapter() {			
				@Override
				public void widgetSelected(SelectionEvent e) {
					Point loc = openEditCollectionMenuBtn.getLocation();
	                Rectangle rect = openEditCollectionMenuBtn.getBounds();
	                Point mLoc = new Point(loc.x-1, loc.y+rect.height);
	
	                editCollectionMenu.setLocation(getShell().getDisplay().map(openEditCollectionMenuBtn.getParent(), null, mLoc));
	                editCollectionMenu.setVisible(true);
				}
			});
			
			editCollectionMenu = new Menu(getShell(), SWT.POP_UP);
			
			createCollectionBtn = new MenuItem(editCollectionMenu, SWT.PUSH);
			createCollectionBtn.setImage(Images.ADD);
			createCollectionBtn.setText("Create a new collection...");
			
			deleteCollectionBtn = new MenuItem(editCollectionMenu, SWT.PUSH);
			deleteCollectionBtn.setImage(Images.DELETE);
			deleteCollectionBtn.setText("Delete this collection...");
			
			modifyCollectionBtn = new MenuItem(editCollectionMenu, SWT.PUSH);
			modifyCollectionBtn.setText("Edit metadata of collection...");
			
			collectionUsersBtn = new MenuItem(editCollectionMenu, SWT.PUSH);
			collectionUsersBtn.setImage(Images.USER_EDIT);
			collectionUsersBtn.setText("Manage users in collection...");
			
			collComp.layout();
		}
		userControls.add(collectionSelectorWidget);
		
		// tabFolder contains documents tab item and ground-truth tab item
		tabFolder = new CTabFolder(container, SWT.BORDER | SWT.FLAT);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		//create documents tab item
		documentsTabItem = new CTabItem(tabFolder, SWT.NONE);
		documentsTabItem.setText("Documents");		
		//container for documents tab elements
		remoteDocsGroup = new Composite(tabFolder, 0);
		remoteDocsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		remoteDocsGroup.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		
		documentsTabItem.setControl(remoteDocsGroup);
		tabFolder.setSelection(documentsTabItem);
		
		
		Composite docsContainer = new Composite(remoteDocsGroup, 0);
		docsContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		docsContainer.setLayout(new GridLayout(2, false));
		docsContainer.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));

		docTableWidget = new DocTableWidgetPagination(docsContainer, SWT.MULTI, 100, false);
		docTableWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		userControls.add(docTableWidget);
		
		ColumnViewerToolTipSupport.enableFor(docTableWidget.getPageableTable().getViewer(), ToolTip.NO_RECREATE);
		docTableWidget.getPageableTable().setToolTipText("");
		
		// 	get top toolbar of docTableWidget:
		Control c = docTableWidget.getPageableTable().getCompositeTop().getChildren()[0];
		ToolBar tb = (ToolBar) c;
		
		new ToolItem(tb, SWT.SEPARATOR);
		
		addToCollectionTi = new ToolItem(tb, SWT.PUSH);
		addToCollectionTi.setImage(Images.ADD);
		addToCollectionTi.setToolTipText("Link selected documents to a different collection...");
				
		removeFromCollectionTi = new ToolItem(tb, SWT.PUSH);
		removeFromCollectionTi.setImage(Images.DELETE);
		removeFromCollectionTi.setToolTipText("Unlink selected documents from this collection....");
		
		deleteDocTi = new ToolItem(tb, SWT.PUSH);
		deleteDocTi.setImage(Images.FOLDER_DELETE);
		deleteDocTi.setToolTipText("Delete the selected documents from Transkribus...");
		
		duplicateDocTi = new ToolItem(tb, SWT.PUSH);
		duplicateDocTi.setImage(Images.PAGE_COPY);
		duplicateDocTi.setToolTipText("Duplicate/Copy the selected documents into another collection...");
		
		administerCollectionTi = new ToolItem(tb, SWT.PUSH);
		administerCollectionTi.setImage(Images.COG_EDIT);
		administerCollectionTi.setToolTipText("Administrate docs in collection, e.g. Add pages, Choose symbolic images,...");
		
		recycleBin = new ToolItem(tb, SWT.PUSH);
		recycleBin.setImage(Images.BIN);
		recycleBin.setToolTipText("Contains deleted documents!");
		
		quickLoadByDocId = new TextToolItem(tb, SWT.NONE);
		quickLoadByDocId.setAutoSelectTextOnFocus();
		quickLoadByDocId.setMessage("Doc-ID");
		quickLoadByDocId.resizeToMessage();
		quickLoadByDocId.setToolTipText("Load document with this id from the first collection it is contained");

//		Composite docBtns = new Composite(docsContainer, 0);
//		docBtns.setLayout(new RowLayout());
//		docBtns.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
//		
////		addToCollectionBtn, removeFromCollectionBtn, deleteDocBtn, duplicateDocBtn;
//		addToCollectionBtn = new Button(docBtns, SWT.PUSH);
//		addToCollectionBtn.setText("Add to collection");
//		addToCollectionBtn.setImage(Images.ADD);
//		
//		removeFromCollectionBtn = new Button(docBtns, SWT.PUSH);
//		removeFromCollectionBtn.setText("Remove from collection");
//		
//		deleteDocBtn = new Button(docBtns, SWT.PUSH);
//		deleteDocBtn.setText("Delete");
//		
//		duplicateDocBtn = new Button(docBtns, SWT.PUSH);
//		duplicateDocBtn.setText("Duplicate");
		
		if (remoteDocsGroup instanceof SashForm) {
			((SashForm)remoteDocsGroup).setWeights(new int[]{50, 50});
		}
		
		/*
		 * Create ground truth treeviewer. 
		 * The tab item is created/disposed depending on availability of data in collection
		 */
//		groundTruthTv = createGroundTruthTreeViewer(tabFolder);
		groundTruthTreeWidget = createGroundTruthTreeWidget(tabFolder);
//		groundTruthTv = groundTruthTreeWidget.getTreeViewer();
		
		initDocOverviewMenu();
		
		updateHighlightedRow();
	}
	
	private TreeViewer createGroundTruthTreeViewer(Composite parent) {
		TreeViewer tv = new TreeViewer(parent, SWT.BORDER | SWT.MULTI);
		final ITreeContentProvider htrGtContentProvider = new HtrGroundTruthContentProvider(null);
		final ILabelProvider htrGtLabelProvider = new HtrGroundTruthLabelAndFontProvider(tv.getControl().getFont());
		tv.setContentProvider(htrGtContentProvider);
		tv.setLabelProvider(htrGtLabelProvider);
		tv.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		return tv;
	}
	
	private GroundTruthTreeWidget createGroundTruthTreeWidget(Composite parent) {
		GroundTruthTreeWidget tw = new GroundTruthTreeWidget(parent);
		tw.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		return tw;
	}
	
	void expandGroundTruthTreeItem(Object o) {
		groundTruthTreeWidget.expandTreeItem(o);
	}
	
	public void updateGroundTruthTreeViewer() {	
		List<TrpHtr> treeViewerInput;
		//filter for HTRs with train GT
		treeViewerInput = store.getHtrs(null).stream()
				.filter(h -> h.getNrOfTrainGtPages() != null && h.getNrOfTrainGtPages() > 0)
				.collect(Collectors.toList());
		if(!treeViewerInput.isEmpty()) {
			if (gtTabItem == null || gtTabItem.isDisposed()) {
				gtTabItem = new CTabItem(tabFolder, SWT.NONE);
				gtTabItem.setText("HTR Model Data");
				gtTabItem.setControl(groundTruthTreeWidget);
			}
			groundTruthTreeWidget.setInput(treeViewerInput);
		} else if(gtTabItem != null) {
				gtTabItem.dispose();
				gtTabItem = null;
		}
	}
	
	private void initDocOverviewMenu() {
		Table t = docTableWidget.getPageableTable().getViewer().getTable();
		docOverviewMenu = new Menu(t);
		t.setMenu(docOverviewMenu);
		
		addToCollectionMenuItem = new MenuItem(docOverviewMenu, SWT.PUSH);
		addToCollectionMenuItem.setImage(Images.ADD);
		addToCollectionMenuItem.setText("Link to different collection...");
		
		removeFromCollectionMenuItem = new MenuItem(docOverviewMenu, SWT.PUSH);
		removeFromCollectionMenuItem.setImage(Images.DELETE);
		removeFromCollectionMenuItem.setText("Unlink from this collection...");		
		
		deleteDocMenuItem = new MenuItem(docOverviewMenu, SWT.PUSH);
		deleteDocMenuItem.setImage(Images.FOLDER_DELETE);
		deleteDocMenuItem.setText("Delete document...");
		
		duplicateDocMenuItem = new MenuItem(docOverviewMenu, SWT.PUSH);
		duplicateDocMenuItem.setImage(Images.PAGE_COPY);
		duplicateDocMenuItem.setText("Duplicate/Copy...");
	}
	
	private void configExpandable(ExpandableComposite exp, Composite client, String text, final Composite container, boolean expand) {
		exp.setClient(client);
		exp.setText(text);
		exp.addExpansionListener(new ExpansionAdapter() {
		      public void expansionStateChanged(ExpansionEvent e) {
		    	  container.layout();		    	  
		      }
		    });
		exp.setExpanded(expand);
	}
	
	public void selectCollectionsTab() {
		if(!isCollectionsTabSelected()) {
			tabFolder.setSelection(documentsTabItem);
		}
	}
	
	public boolean isCollectionsTabSelected() {
		return documentsTabItem.equals(tabFolder.getSelection());
	}
	
	public boolean isGroundTruthTabSelected() {
		return gtTabItem.equals(tabFolder.getSelection());
	}
	
	public void updateHighlightedRow() {
		docTableWidget.getTableViewer().refresh();
	}
	
	public void updateHighlightedGroundTruthTreeViewerRow() {
		groundTruthTreeWidget.getTreeViewer().refresh();
	}
	
	public void refreshDocListFromStorage() {
		docTableWidget.refreshList(getSelectedCollectionId(), true, false);
	}
		
	public void clearDocList() {
		docTableWidget.refreshList(0, true, false);
	}
	
	public DocTableWidgetPagination getDocTableWidget() { return docTableWidget; }
	
	public TableViewer getTableViewer() { return docTableWidget.getTableViewer(); }
	public Label getUsernameLabel() { return usernameLabel; }
	public Button getLoginBtn() { return loginBtn; }
		
	public TrpDocMetadata getSelectedDocument() {
		return docTableWidget.getFirstSelected();
	}
	
	public List<TrpDocMetadata> getSelectedDocuments() {
		return docTableWidget.getSelected();
	}
	
	public int getSelectedCollectionId() {
		return collectionSelectorWidget.getSelectedCollectionId();
	}
	
	public TrpCollection getSelectedCollection() {
		return collectionSelectorWidget.getSelectedCollection();
	}
	
	public String getSelectedRecentDoc(){
		return recentDocsComboViewerWidget.getSelectedDoc();
	}
		
	public void setSelectedCollection(TrpCollection trpCollection) {
		collectionSelectorWidget.setSelectedCollection(trpCollection);
	}
				
	public void updateRecentDocs() {
		recentDocsComboViewerWidget.setRecentDocs(false);	
	}
	
	public void updateBtnVisibility(boolean canManage){
		deleteDocTi.setEnabled(canManage);
		removeFromCollectionTi.setEnabled(canManage);
		addToCollectionTi.setEnabled(canManage);
		duplicateDocTi.setEnabled(canManage);
		administerCollectionTi.setEnabled(canManage);
		recycleBin.setEnabled(canManage);
		
		addToCollectionMenuItem.setEnabled(canManage);
		removeFromCollectionMenuItem.setEnabled(canManage);
		duplicateDocMenuItem.setEnabled(canManage);
		deleteDocMenuItem.setEnabled(canManage);
	}
	
	public Button getShowJobsBtn() { return showJobsBtn; }
	public Button getShowVersionsBtn() { return showVersionsBtn; }
}
