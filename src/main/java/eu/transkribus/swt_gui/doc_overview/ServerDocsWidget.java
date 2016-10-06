package eu.transkribus.swt_gui.doc_overview;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.nebula.widgets.pagination.IPageLoader;
import org.eclipse.nebula.widgets.pagination.collections.PageResult;
import org.eclipse.nebula.widgets.pagination.collections.PageResultLoaderList;
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
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.collection_comboviewer.CollectionComboViewerWidget;
import eu.transkribus.swt_gui.collection_manager.CollectionManagerDialog;
import eu.transkribus.swt_gui.dialogs.ActivityDialog;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.Storage.LoginOrLogoutEvent;
import eu.transkribus.swt_gui.pagination_tables.DocTableWidgetPagination;
import eu.transkribus.swt_gui.util.RecentDocsComboViewerWidget;

public class ServerDocsWidget extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(ServerDocsWidget.class);

	Label usernameLabel, serverLabel;
	DocTableWidgetPagination docTableWidget;
	Button uploadDocsItem;

	CollectionComboViewerWidget collectionComboViewerWidget;
	RecentDocsComboViewerWidget recentDocsComboViewerWidget;
	
	Button manageCollectionsBtn;
	Button showActivityWidgetBtn;
	Button searchBtn;
	Text quickLoadByID;
		
	CollectionManagerDialog cm;
	ActivityDialog ad;
	
	Storage store = Storage.getInstance();

	//RecentDocsPreferences prefs = new RecentDocsPreferences(5, prefNode);
	
//	ExpandableComposite docMdExp;
	ExpandableComposite adminAreaExp;
	ExpandableComposite lastDocsAreaExp;
	ExpandableComposite remotedocsgroupExp;
	Composite container;
	
	Button syncWithLocalDocBtn, applyAffineTransformBtn, batchReplaceImgsBtn;
	
//	List<TrpDocMetadata> docs=new ArrayList<>();
	
	int selectedId=-1;

//	private List<TrpCollection> collections;
//	TrpCollection selectedCollection=null;
		
	public ServerDocsWidget(Composite parent) {
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
//		uploadDocsItem.setEnabled(isLoggedIn);
//		searchBtn.setEnabled(isLoggedIn);
		
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
				
		container = new Composite(this, SWT.NONE);
//		final SashForm container = new SashForm(this, SWT.VERTICAL);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		GridLayout l = new GridLayout();
		l.marginWidth = l.marginHeight = 0;
		container.setLayout(l);
//		container.setBackground(container.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		
		usernameLabel = new Label(container, SWT.NONE);
		usernameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		Fonts.setBoldFont(usernameLabel);
		
		serverLabel = new Label(container, SWT.NONE);
		serverLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));		
				
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
		
		configExpandable(adminAreaExp, adminAreaComp, "Admin area", container, false);
		adminAreaExp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		/////////////////		
		lastDocsAreaExp = new ExpandableComposite(container, ExpandableComposite.COMPACT);
		Fonts.setBoldFont(lastDocsAreaExp);
		Composite lastDocsAreaComp = new Composite(lastDocsAreaExp, SWT.SHADOW_ETCHED_IN);
		lastDocsAreaComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		lastDocsAreaComp.setLayout(new GridLayout(1, false));
		
		recentDocsComboViewerWidget = new RecentDocsComboViewerWidget(lastDocsAreaComp, 0);
		recentDocsComboViewerWidget.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		configExpandable(lastDocsAreaExp, lastDocsAreaComp, "Recent Documents", container, true);
		lastDocsAreaExp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		
		////////////////
		remotedocsgroupExp = new ExpandableComposite(container, ExpandableComposite.COMPACT);
		Fonts.setBoldFont(remotedocsgroupExp);
//		Composite remotedocsgroup = new Group(remotedocsgroupExp, SWT.SHADOW_ETCHED_IN); // orig-parent = container
//		Composite remotedocsgroup = new SashForm(remotedocsgroupExp, SWT.VERTICAL); // orig-parent = container
		Composite remotedocsgroup = new Composite(remotedocsgroupExp, 0); // orig-parent = container
		
		remotedocsgroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		remotedocsgroup.setText("Remote docs");
		remotedocsgroup.setLayout(new GridLayout(1, false));
			
		configExpandable(remotedocsgroupExp, remotedocsgroup, "Server documents", container, true);
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
		
//		Composite btns = new Composite(docsContainer, SWT.NONE);
//		btns.setLayout(new GridLayout(4, false));
//		btns.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
//		btns.setLayout(new RowLayout());
//		btns.setLayout(new FillLayout());
		
//		Label docLabel = new Label(btns, SWT.CENTER);
//		docLabel.setText("Documents:");

//		uploadSingleDocItem = new Button(btns, SWT.NONE);
//		uploadSingleDocItem.setToolTipText("Upload a document to the current collection");
//		uploadSingleDocItem.setImage(Images.getOrLoad("/icons/folder_add.png"));
//		uploadSingleDocItem.setVisible(false);
		
//		uploadDocsItem = new Button(btns, SWT.NONE);
//		uploadDocsItem.setToolTipText("Ingest or upload documents");
////		uploadFromPrivateFtpItem.setImage(Images.getOrLoad("/icons/weather_clouds.png"));
//		uploadDocsItem.setImage(Images.getOrLoad("/icons/folder_add.png"));
//		
//		searchBtn = new Button(btns, 0);
//		searchBtn.setToolTipText("Search for documents, keywords... tbc");
//		searchBtn.setImage(Images.getOrLoad("/icons/find.png"));
		
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
		docTableWidget = new DocTableWidgetPagination(docsContainer, 0, 25) {
			@Override protected void setPageLoader() {
				logger.debug("setting list page loader!");
				
				List<TrpDocMetadata> docs = Storage.getInstance().getRemoteDocList();
				IPageLoader<PageResult<TrpDocMetadata>> listLoader = new PageResultLoaderList<>(docs);
				
				pageableTable.setPageLoader(listLoader);
			}
		};
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
	
	private void configExpandable(ExpandableComposite exp, Composite client, String text, final Composite container, boolean expand) {
		exp.setClient(client);
		exp.setText(text);
		exp.addExpansionListener(new ExpansionAdapter() {
		      public void expansionStateChanged(ExpansionEvent e) {
		    	  container.layout();		    	  
		      }
		    });
		exp.setExpanded(expand);
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
	
	public TableViewer getTableViewer() { return docTableWidget.getTableViewer(); }
	public Label getServerLabel() { return serverLabel; }
	public Label getUsernameLabel() { return usernameLabel; }
		
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
	
	public String getSelectedRecentDoc(){
		return recentDocsComboViewerWidget.getSelectedDoc();
	}
	
	public void setSelectedCollection(int colId, boolean fireSelectionEvent) {
		collectionComboViewerWidget.setSelectedCollection(colId, fireSelectionEvent);
	}
	
	public void clearCollectionFilter() {
		collectionComboViewerWidget.clearFilter();
	}
	
	public CollectionManagerDialog getCollectionManagerDialog() {
		return cm;
	}
	
	public boolean isCollectionManagerOpen() {
		return cm != null && cm.getShell() != null && !cm.getShell().isDisposed();
	}
	
	public void openCollectionsManagerWidget() {
		if (!isCollectionManagerOpen()) {
			logger.debug("creating NEW CM Dialog!!");
			cm = new CollectionManagerDialog(getShell(), SWT.NONE, this);
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

	public void updateRecentDocs() {
		recentDocsComboViewerWidget.updateDocs(false);
		
	}
	
}
