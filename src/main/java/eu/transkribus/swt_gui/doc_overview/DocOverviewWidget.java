package eu.transkribus.swt_gui.doc_overview;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.auth.TrpRole;
import eu.transkribus.swt_canvas.util.Fonts;
import eu.transkribus.swt_canvas.util.Images;
import eu.transkribus.swt_canvas.util.SWTUtil;
import eu.transkribus.swt_gui.collection_comboviewer.CollectionComboViewerWidget;
import eu.transkribus.swt_gui.collection_manager.CollectionManagerDialog2;
import eu.transkribus.swt_gui.dialogs.ActivityDialog;
import eu.transkribus.swt_gui.edit_decl_manager.EditDeclManagerDialog;
import eu.transkribus.swt_gui.edit_decl_manager.EditDeclViewerDialog;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.Storage.LoginOrLogoutEvent;
import eu.transkribus.swt_gui.pagination_tables.DocTableWidgetPagination;

public class DocOverviewWidget extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(DocOverviewWidget.class);
	
	Label usernameLabel, serverLabel;
	Label loadedDocLabel;
	Text loadedDocText, currentCollectionText;
	
	Text loadedPageText, loadedImageUrl, loadedTranscriptUrl;
	DocTableWidgetPagination docTableWidget;
	Button uploadDocsItem;

	CollectionComboViewerWidget collectionComboViewerWidget;
	
	Button manageCollectionsBtn;
	Button showActivityWidgetBtn;
	Button searchBtn;
	Text quickLoadByID;
	
	Button openMetadataEditorBtn;
	Button openEditDeclManagerBtn;
	
	CollectionManagerDialog2 cm;
	EditDeclManagerDialog edm;
	ActivityDialog ad;
	
	DocMetadataEditor docMetadataEditor;
	Storage store = Storage.getInstance();
	
	ExpandableComposite docMdExp;
	ExpandableComposite adminAreaExp;
	ExpandableComposite remotedocsgroupExp;
	Composite container;
	
	Button syncWithLocalDocBtn, applyAffineTransformBtn, batchReplaceImgsBtn;
	
//	List<TrpDocMetadata> docs=new ArrayList<>();
	
	int selectedId=-1;

//	private List<TrpCollection> collections;
//	TrpCollection selectedCollection=null;
		
	public DocOverviewWidget(Composite parent) {
		super(parent, SWT.NONE);
				
		init();
		addListener();
		updateLoggedIn();
	}
	
	private void addListener() {
		Storage.getInstance().addObserver(new Observer() {
			@Override public void update(Observable arg0, Object arg1) {
				if (arg1 instanceof LoginOrLogoutEvent) {
					updateLoggedIn();
				}
			}
		});
	}
	
	private void updateLoggedIn() {
		boolean isLoggedIn = store.isLoggedIn();

//		uploadSingleDocItem.setEnabled(isLoggedIn);
		uploadDocsItem.setEnabled(isLoggedIn);
		searchBtn.setEnabled(isLoggedIn);
		
//		boolean canDelete = getSelectedCollection().getRole()==null || getSelectedCollection().getRole().canDelete();
//		deleteItem.setEnabled(isLoggedIn);
		
		collectionComboViewerWidget.setEnabled(isLoggedIn);
		
//		reloadCollectionsBtn.setEnabled(isLoggedIn);
		manageCollectionsBtn.setEnabled(isLoggedIn);
		showActivityWidgetBtn.setEnabled(isLoggedIn);
	}

//	public Button getReloadCollectionsBtn() { return reloadCollectionsBtn; }
//	public Button getCreateCollectionBtn() { return createCollectionBtn; }
//	public Button getManageCollectionsBtn() { return manageCollectionsBtn; }
		
	private void init() {
		this.setLayout(new GridLayout());
		
		docMdExp = new ExpandableComposite(this, ExpandableComposite.COMPACT);
		Fonts.setBoldFont(docMdExp);
		
		Composite c1 = new Composite(docMdExp, SWT.NONE);
		c1.setLayout(new GridLayout(2, false));
		c1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				
		loadedDocLabel = new Label(c1, SWT.NONE);
		loadedDocLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		loadedDocLabel.setText("Loaded doc: ");
		
		loadedDocText = new Text(c1, SWT.BORDER | SWT.READ_ONLY);
		loadedDocText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label l0 = new Label(c1, 0);
		l0.setText("Current collection: ");
		
		currentCollectionText = new Text(c1, SWT.BORDER | SWT.READ_ONLY);
		currentCollectionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label loadedPageLabel = new Label(c1, SWT.NONE);
		loadedPageLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		loadedPageLabel.setText("Current filename: ");
		
		loadedPageText = new Text(c1, SWT.BORDER | SWT.READ_ONLY);
		loadedPageText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
//		Label loadedPageKeyLabel = new Label(c1, SWT.NONE);
//		loadedPageKeyLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
//		loadedPageKeyLabel.setText("Key: ");
//		
//		loadedPageKey = new Text(c1, SWT.BORDER | SWT.READ_ONLY);
//		loadedPageKey.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label loadedImageUrlLabel = new Label(c1, SWT.NONE);
		loadedImageUrlLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		loadedImageUrlLabel.setText("Current image URL: ");
		
		loadedImageUrl = new Text(c1, SWT.BORDER | SWT.READ_ONLY);
		loadedImageUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label loadedTranscriptUrlLabel = new Label(c1, SWT.NONE);
		loadedTranscriptUrlLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		loadedTranscriptUrlLabel.setText("Current transcript URL: ");
		
		loadedTranscriptUrl = new Text(c1, SWT.BORDER | SWT.READ_ONLY);
		loadedTranscriptUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
//		ExpandBar container = new ExpandBar (this, SWT.V_SCROLL);
		
		openMetadataEditorBtn = new Button(c1, SWT.PUSH);
		openMetadataEditorBtn.setText("Document metadata...");
		openMetadataEditorBtn.setToolTipText("Edit document metadata");
		
		openEditDeclManagerBtn = new Button(c1, SWT.PUSH);
		openEditDeclManagerBtn.setText("Editorial Declaration...");
		//TODO activate this
//		openEditDeclManagerBtn.setVisible(false);
		
		usernameLabel = new Label(c1, SWT.NONE);
		usernameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		Fonts.setBoldFont(usernameLabel);
		
		serverLabel = new Label(c1, SWT.NONE);
		serverLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		configExpandable(docMdExp, c1, "Document metadata", this);
		docMdExp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		
		container = new Composite(this, SWT.NONE);
//		final SashForm container = new SashForm(this, SWT.VERTICAL);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		container.setLayout(new GridLayout());
//		container.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		
		if (false) {
		final ExpandableComposite metadatagroupExp = new ExpandableComposite(container, ExpandableComposite.COMPACT);
//		Group metadatagroup = new Group(container, SWT.SHADOW_ETCHED_IN);
		Composite metadatagroup = new Composite(metadatagroupExp, SWT.SHADOW_ETCHED_IN);
		metadatagroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		metadatagroup.setText("Document metadata");
		metadatagroup.setLayout(new GridLayout(1, false));
		
		metadatagroupExp.setClient(metadatagroup);
		metadatagroupExp.setText("Document metadata");
		docMetadataEditor = new DocMetadataEditor(metadatagroup, SWT.NONE);
//		metadataEditor.pack();
		docMetadataEditor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		metadatagroupExp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		metadatagroupExp.setExpanded(true);
		metadatagroupExp.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				container.layout();
			}
		});
		}
		
		// admin area:
		adminAreaExp = new ExpandableComposite(container, ExpandableComposite.COMPACT);
		Fonts.setBoldFont(adminAreaExp);
		Composite adminAreaComp = new Composite(adminAreaExp, SWT.SHADOW_ETCHED_IN);
		adminAreaComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		adminAreaComp.setLayout(new GridLayout(1, false));
		syncWithLocalDocBtn = new Button(adminAreaComp, SWT.PUSH);
		syncWithLocalDocBtn.setText("Sync with local doc");
		
		applyAffineTransformBtn = new Button(adminAreaComp, SWT.PUSH);
		applyAffineTransformBtn.setText("Apply affine transformation");
		
		batchReplaceImgsBtn = new Button(adminAreaComp, SWT.PUSH);
		batchReplaceImgsBtn.setText("Batch replace images");
		
		configExpandable(adminAreaExp, adminAreaComp, "Admin area", container);
		adminAreaExp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		adminAreaExp.setExpanded(false);
		/////////////////		
		remotedocsgroupExp = new ExpandableComposite(container, ExpandableComposite.COMPACT);
		Fonts.setBoldFont(remotedocsgroupExp);
//		Composite remotedocsgroup = new Group(remotedocsgroupExp, SWT.SHADOW_ETCHED_IN); // orig-parent = container
//		Composite remotedocsgroup = new SashForm(remotedocsgroupExp, SWT.VERTICAL); // orig-parent = container
		Composite remotedocsgroup = new Composite(remotedocsgroupExp, 0); // orig-parent = container
		
		remotedocsgroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		remotedocsgroup.setText("Remote docs");
		remotedocsgroup.setLayout(new GridLayout(1, false));
		
		configExpandable(remotedocsgroupExp, remotedocsgroup, "Server documents", container);
		remotedocsgroupExp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		///////////////
		collectionComboViewerWidget = new CollectionComboViewerWidget(remotedocsgroup, 0);
		collectionComboViewerWidget.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		manageCollectionsBtn = new Button(collectionComboViewerWidget.headerComposite, SWT.PUSH);
//		manageCollectionsBtn.setText("...");
		manageCollectionsBtn.setImage(Images.getOrLoad("/icons/user_edit.png"));
		manageCollectionsBtn.setToolTipText("Manage collections...");
//		manageCollectionsBtn.pack();
		
		showActivityWidgetBtn = new Button(collectionComboViewerWidget.headerComposite, SWT.PUSH);
		showActivityWidgetBtn.setImage(Images.GROUP);
		showActivityWidgetBtn.setToolTipText("Show user activity");

		///////////////		
		
		Composite docsContainer = new Composite(remotedocsgroup, 0);
		docsContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		docsContainer.setLayout(new GridLayout(2, false));		
		
		Composite btns = new Composite(docsContainer, SWT.NONE);
		btns.setLayout(new GridLayout(4, false));
		btns.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
//		btns.setLayout(new RowLayout());
//		btns.setLayout(new FillLayout());
		
		Label docLabel = new Label(btns, SWT.CENTER);
		docLabel.setText("Documents:");

//		uploadSingleDocItem = new Button(btns, SWT.NONE);
//		uploadSingleDocItem.setToolTipText("Upload a document to the current collection");
//		uploadSingleDocItem.setImage(Images.getOrLoad("/icons/folder_add.png"));
//		uploadSingleDocItem.setVisible(false);
		
		uploadDocsItem = new Button(btns, SWT.NONE);
		uploadDocsItem.setToolTipText("Ingest or upload documents");
//		uploadFromPrivateFtpItem.setImage(Images.getOrLoad("/icons/weather_clouds.png"));
		uploadDocsItem.setImage(Images.getOrLoad("/icons/folder_add.png"));
		
		searchBtn = new Button(btns, 0);
		searchBtn.setToolTipText("Search for documents, keywords... tbc");
		searchBtn.setImage(Images.getOrLoad("/icons/find.png"));
		
//		Label l = new Label(btns, 0);
//		l.setText("Quick load by ID: ");
//		
//		quickLoadByID = new Text(btns, SWT.BORDER | SWT.SINGLE);
//		quickLoadByID.setToolTipText("ID of the doc - press enter to load");
//		quickLoadByID.addTraverseListener(new TraverseListener() {
//			@Override public void keyTraversed(TraverseEvent e) {
//				if (e.detail == SWT.TRAVERSE_RETURN) {
//					try {				
//						int docid = Integer.parseInt(quickLoadByID.getText());
//						TrpMainWidget.getInstance().loadRemoteDoc(docid);
//					} catch (NumberFormatException ex) {
//						logger.warn("cannot parse docid : "+quickLoadByID.getText());
//					}
//				}
//			}
//		});

//		deleteItem = new Button(btns, SWT.NONE);
//		deleteItem.setToolTipText("Delete selected document from the server - only allowed for admins!");
//		deleteItem.setImage(Images.getOrLoad("/icons/delete.png"));
		
//		showFreeForAllBtn = new Button(remotedocsgroup, SWT.CHECK);
//		showFreeForAllBtn.setText("Show public documents");
//		showFreeForAllBtn.setSelection(true);
//		showFreeForAllBtn.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				setInput(docs);
//			}
//		});
		
//		Label filterLabel = new Label(remotedocsgroup, SWT.NONE);
//		filterLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
//		filterLabel.setText("Filter: ");
//		filterCombo = new Combo(remotedocsgroup, SWT.READ_ONLY);
//		filterCombo.setItems(visibilityLabels);
//		filterCombo.select(0);
//		filterCombo.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				setInput(docs);
//			}
//		});
//		filterCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
//		docTableWidget = new DocTableWidget(remotedocsgroup, 0);
		docTableWidget = new DocTableWidgetPagination(docsContainer, 0, 25);
		docTableWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		ColumnViewerToolTipSupport.enableFor(docTableWidget.getPageableTable().getViewer(), ToolTip.NO_RECREATE);
		docTableWidget.getPageableTable().setToolTipText("");
		
//		tableViewer = docTableWidget.getTableViewer();
		
//		tableViewer = new MyTableViewer(remotedocsgroup, SWT.SINGLE);
//		tableViewer.setContentProvider(new ArrayContentProvider());
//		tableViewer.setLabelProvider(new DocOverviewLabelProvider(this));
		
//		table = tableViewer.getTable();
//		table.setHeaderVisible(true);
//		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
//		initColumns();

//		TableColumn coordsCol = new TableColumn(tree, SWT.LEFT);
//		coordsCol.setText("Coords"); 
//		coordsCol.setWidth(200);
		
		if (remotedocsgroup instanceof SashForm) {
			((SashForm)remotedocsgroup).setWeights(new int[]{50, 50});
		}
		
		updateHighlightedRow(-1);
		
		setAdminAreaVisible(false);
	}
	
	public void setAdminAreaVisible(boolean visible) {
		adminAreaExp.setParent(visible ? container : SWTUtil.dummyShell);
		adminAreaExp.moveAbove(remotedocsgroupExp);
		container.layout();
	}
	
	private void configExpandable(ExpandableComposite exp, Composite client, String text, final Composite container) {
		exp.setClient(client);
		exp.setText(text);
		exp.addExpansionListener(new ExpansionAdapter() {
		      public void expansionStateChanged(ExpansionEvent e) {
		    	  container.layout();		    	  
		      }
		    });
		exp.setExpanded(true);
//		exp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
//		exp.setLayoutData(new GridData(SWT.TOP, SWT.TOP, true, false, 2, 1));
	}
	
//	private void initColumns() {		
//		createColumn(SWT.LEFT, "ID", 65, true, DefaultTableColumnViewerSorter.ASC);
//		createColumn(SWT.LEFT, "Title", 100, false, DefaultTableColumnViewerSorter.ASC);
//		createColumn(SWT.RIGHT, "N-Pages", 50, false, DefaultTableColumnViewerSorter.ASC);
//		createColumn(SWT.LEFT, "Owner", 50, false, DefaultTableColumnViewerSorter.ASC);
////		createColumn(SWT.LEFT, "Is public", 50, false, DefaultTableColumnViewerSorter.ASC);
//		
////		TableColumn typeCol = new TableColumn(table, SWT.LEFT);
////		typeCol.setText("ID"); 
////		typeCol.setWidth(65);
////		DefaultTableColumnViewerSorter typeSorter = new DefaultTableColumnViewerSorter(tableViewer, typeCol);
//		
////		TableColumn idCol = new TableColumn(table, SWT.LEFT);
////		idCol.setText("Title"); 
////		idCol.setWidth(100);
////		new DefaultTableColumnViewerSorter(tableViewer, idCol);
////		
////		TableColumn npCol = new TableColumn(table, SWT.LEFT);
////		npCol.setText("N-Pages"); 
////		npCol.setWidth(30);		
////		new DefaultTableColumnViewerSorter(tableViewer, npCol);
////				
////		typeSorter.setSorter(typeSorter, TableViewerSorter.ASC);
//	}
	
//	private Pair<TableColumn, DefaultTableColumnViewerSorter> createColumn(int style, String text, int width, boolean setAsSorter, int sortDirection) {
//		TableColumn col = new TableColumn(table, style);
//		col.setText(text); 
//		col.setWidth(width);
//		DefaultTableColumnViewerSorter sorter = new DefaultTableColumnViewerSorter(tableViewer, col);
//		
//		if (setAsSorter) {
//			sorter.setSorter(sorter, sortDirection);
//		}
//		
//		return Pair.of(col, sorter);
//		
//	}
	
	public void updateHighlightedRow(int selectedId) {
//		docTableWidget.setSelectedId(selectedId);
		docTableWidget.getTableViewer().refresh();
	}
	
//	public List<TrpDocMetadata> getInput() { 
//		return docs;
//	}
	
	public void refreshDocList() {
		docTableWidget.refreshList(getSelectedCollectionId(), true);
	}
	
	public void clearDocList() {
		docTableWidget.refreshList(0, true);
	}
	
	public DocTableWidgetPagination getDocTableWidget() { return docTableWidget; }
	
//	public void setInput(List<TrpDocMetadata> trpDocs) {
//		docTableWidget.refreshList(getSelectedCollectionId());
//	}
	
	public TableViewer getTableViewer() { return docTableWidget.getTableViewer(); }
//	public DocMetadataEditor getDocMetadataEditor() { 
//		return docMetadataEditor;
//	}
//	public Tree getTree() { return tree; }
//	public Label getLoadedDocLabel() { return loadedDocLabel; }
	public Text getLoadedDocText() { return loadedDocText; }
	public Text getCurrentCollectionText() { return currentCollectionText; }
	
	
	
//	public void setCurrentCollection(String currentCollection) {
//		currentCollectionText.setText(currentCollection);
//		int i = collectionComboViewer.getCombo().indexOf(currentCollection);
//		if (i != -1) {
//			collectionComboViewer.getCombo().select(i);
//		}
//	}
	
	public Text getLoadedImageUrl() {
		return loadedImageUrl;
	}

	public Text getLoadedTranscriptUrl() {
		return loadedTranscriptUrl;
	}

	public Text getLoadedPageText() { return loadedPageText; }
//	public Text getLoadedPageKey() { return loadedPageKey; }
	public Label getServerLabel() { return serverLabel; }
	public Label getUsernameLabel() { return usernameLabel; }
		
//	public void updateTreeColumnSize() {
//		int [] maxColSize = new int[table.getColumnCount()];
//		for (int i=0; i<table.getColumnCount(); ++i) {
//			maxColSize[i] = 0;
//		}
//				
//		GC gc = new GC(table);
//		if (table.getItems() != null)
//		for (TableItem child : table.getItems()) {
//			for (int i=0; i<table.getColumnCount(); ++i) {
//				int te = gc.textExtent(child.getText(i)).x;
//				if (te > maxColSize[i])
//					maxColSize[i] = te;	
//			}				
//		}
//		gc.dispose();
//		
//		// update size of cols depending on max size of text inside:
//		for (int i=0; i<table.getColumnCount(); ++i) {			
//			logger.debug("maxcolsize["+i+"]: "+maxColSize[i]);
//			
//			if (i==0) {
//				this.table.getColumn(i).setWidth(maxColSize[i]+60);	
//			}
//			else
//				this.table.getColumn(i).setWidth(maxColSize[i]+10);
//		}
//	}

	public TrpDocMetadata getSelectedDocument() {
		return docTableWidget.getFirstSelected();
	}
	
	public void setAvailableCollections(List<TrpCollection> collections) {
		collectionComboViewerWidget.setAvailableCollections(collections);
	}
	
	public int getSelectedCollectionId() {
		return collectionComboViewerWidget.getSelectedCollectionId();
	}
	
	public TrpCollection getSelectedCollection() {
		return collectionComboViewerWidget.getSelectedCollection();
	}
	
	public void setSelectedCollection(int colId, boolean fireSelectionEvent) {
		collectionComboViewerWidget.setSelectedCollection(colId, fireSelectionEvent);
	}
	
	public void clearCollectionFilter() {
		collectionComboViewerWidget.clearFilter();
	}
	
	public Button getOpenMetadataEditorBtn() { return openMetadataEditorBtn; }
	public Button getOpenEditDeclManagerBtn() { return openEditDeclManagerBtn; }
	
	public void openEditDeclManagerWidget() {
		if(!store.isDocLoaded()) {
			return;
		}
		if (!isEditDeclManagerOpen()) {
			if(store.getRoleOfUserInCurrentCollection().getValue() < TrpRole.Editor.getValue()){
				edm = new EditDeclViewerDialog(getShell(), SWT.NONE);
			} else {
				edm = new EditDeclManagerDialog(getShell(), SWT.NONE);
			}
			edm.open();
		} else {
			edm.getShell().setVisible(true);
		}
	}
	
	public CollectionManagerDialog2 getCollectionManagerDialog() {
		return cm;
	}
	
	public boolean isEditDeclManagerOpen() {
		return edm != null && edm.getShell() != null && !edm.getShell().isDisposed();
	}
	
	
	public boolean isCollectionManagerOpen() {
		return cm != null && cm.getShell() != null && !cm.getShell().isDisposed();
	}
	
	public void openCollectionsManagerWidget() {
		if (!isCollectionManagerOpen()) {
			logger.debug("creating NEW CM Dialog!!");
			cm = new CollectionManagerDialog2(getShell(), SWT.NONE, this);
			cm.open();
		} else
			cm.getShell().setVisible(true);
		
//		SWTUtil.centerShell(cm.getShell());
	}
	
	public boolean isActivitiyDialogOpen() {
		return ad != null && ad.getShell() != null && !ad.getShell().isDisposed();
	}
	

	public void openActivityDialog() {
		logger.debug("opening activity dialog...");
		if (!isActivitiyDialogOpen()) {
			ad = new ActivityDialog(getShell());
		}
		ad.open();
		
	}
	
}
