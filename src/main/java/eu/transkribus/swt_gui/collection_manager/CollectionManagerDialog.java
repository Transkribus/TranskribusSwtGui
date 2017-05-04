package eu.transkribus.swt_gui.collection_manager;

import java.util.List;

import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpUserCollection;
import eu.transkribus.core.model.beans.auth.TrpRole;
import eu.transkribus.core.model.beans.auth.TrpUser;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.doc_overview.DocTableWidgetPagination;
import eu.transkribus.swt_gui.doc_overview.MyDocsTableWidgetPagination;
import eu.transkribus.swt_gui.doc_overview.ServerWidget;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.pagination_tables.CollectionsTableWidgetPagination;
import eu.transkribus.swt_gui.pagination_tables.UserTableWidgetPagination;
import eu.transkribus.swt_gui.search.SimpleSearchDialog;

public class CollectionManagerDialog extends Dialog {
	
	private final static Logger logger = LoggerFactory.getLogger(CollectionManagerDialog.class);

	CollectionsTableWidgetPagination collectionsTv;
	
	DocTableWidgetPagination docsTableWidget;
	MyDocsTableWidgetPagination myDocsTableWidget;
	
	Button addCollectionBtn, deleteCollectionBtn, modifyCollectionBtn;
	
	Button addDocumentToCollBtn, removeDocumentFromCollBtn;
	Button deleteDocumentBtn;
	Button duplicatedDocumentBtn;
	Button searchBtn, closeBtn;
	
	Group docGroup;
	
	private CollectionUsersWidget collectionUsersWidget;
	
	CollectionManagerListener cml;
	
	ServerWidget serverWidget;
	
	CTabFolder docTabFolder;
	
	Shell shell;
	
	public static final String COLL_ID_COL = "ID";
	public static final String COLL_NAME_COL = "Name";
	public static final String COLL_DESC_COL = "Description";
	public static final String COLL_ROLE = "Role";
	
	static final Storage store = Storage.getInstance();
	
	public CollectionManagerDialog(Shell parent, int style, ServerWidget serverWidget) {
		super(parent, style |= (SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MODELESS | SWT.MAX));
//		this.setSize(800, 800);
		this.setText("Collection Manager");
		
		this.serverWidget = serverWidget;
	}
	
	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shell.setSize(1100, 800);
		SWTUtil.centerShell(shell);
				
		shell.open();
		shell.layout();
		
		postInit();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return null;
	}
	
	void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.setText(getText());
		FillLayout l = new FillLayout();
		l.marginHeight = 5;
		l.marginWidth = 5;
		shell.setLayout(l);
		
		Composite container = new SashForm(shell, SWT.HORIZONTAL);
		GridLayout layout = new GridLayout(2, false);
		container.setLayout(layout);
		
		Composite first = new SashForm(container, SWT.VERTICAL);
		first.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		first.setLayout(new GridLayout(1, false));
		
		createCollectionsTable(first);
		createDocsTable(first);
		
		collectionUsersWidget = new CollectionUsersWidget(container, 0);
		collectionUsersWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
//		Composite second = new SashForm(container, SWT.VERTICAL);
//		second.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		second.setLayout(new GridLayout(1, false));
//		
//		createCollectionUsersTable(second);
//		createFindUsersWidget(second);
		
		addListener();
		
		updateCollections();
		updateUsersForSelectedCollection();
		updateDocumentsTable(serverWidget.getSelectedDocument(), true);
		
		((SashForm) container).setWeights(new int[] { 60, 40 });
	}
	
	public Shell getShell() { return shell; }
	
	void postInit() {
//		updateMyDocuments(); // FIXME: if I do this in createContents, the other widgets disapper -> do it here...		
		// select collection selected in doc overview:
		// TODO: get page opened in doc overview widget and open it here, then select collection!
//		TrpCollection selC = docOverviewWidget.getSelectedCollection();
//		if (selC != null) {
//			collectionsTv.getTableViewer().setSelection(new StructuredSelection(selC), true);
//		}
	
	}
	
	void addListener() {
		cml = new CollectionManagerListener(this);
		
		collectionsTv.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override public void selectionChanged(SelectionChangedEvent event) {
				
				updateUsersForSelectedCollection();
				updateDocumentsTable(serverWidget.getSelectedDocument(), true);
			}
		});

		shell.addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(DisposeEvent e) {
				cml.detach();
			}
		});
	}
	
	private void createCollectionsTable(Composite container) {
		Group group = new Group(container, SWT.SHADOW_ETCHED_IN);
		group.setText("Collections");
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		group.setLayout(new GridLayout());
		group.setFont(Fonts.createBoldFont(group.getFont()));
		
		collectionsTv = new CollectionsTableWidgetPagination(group, SWT.SINGLE | SWT.FULL_SELECTION, 25, null, true);
		collectionsTv.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		IDoubleClickListener openSelectedColListener = new IDoubleClickListener() {
			@Override public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (sel.isEmpty())
					return;
				
				TrpCollection col = (TrpCollection) sel.getFirstElement();
				if (col != null)
					TrpMainWidget.getInstance().reloadDocList(col.getColId());
			}
		};		
		collectionsTv.getTableViewer().addDoubleClickListener(openSelectedColListener);
		
		Composite btns = new Composite(group, 0);
		btns.setLayout(new GridLayout(6, false));
		btns.setLayoutData(new GridData(SWT.TOP, SWT.FILL, true, false, 1, 1));
		
		addCollectionBtn = new Button(btns, SWT.PUSH);
		addCollectionBtn.setText("Create collection...");
		addCollectionBtn.setImage(Images.getOrLoad("/icons/add.png"));
		addCollectionBtn.setToolTipText("Create a new collection");
		addCollectionBtn.pack();
		
		deleteCollectionBtn = new Button(btns, SWT.PUSH);
		deleteCollectionBtn.setText("Delete collection");
		deleteCollectionBtn.setImage(Images.getOrLoad("/icons/delete.png"));
		deleteCollectionBtn.setToolTipText("Delete a new collection (only possible for collection owners!)");
		deleteCollectionBtn.pack();
		
		modifyCollectionBtn = new Button(btns, SWT.PUSH);
		modifyCollectionBtn.setText("Rename...");
		modifyCollectionBtn.setImage(Images.getOrLoad("/icons/pencil.png"));
		modifyCollectionBtn.setToolTipText("Change the collection name");
						
		group.pack();
	}
	
	public String getSelectedCollectionName() {
		return getSelectedCollection()!=null ? getSelectedCollection().getColName() : "";
	}
	
	private CTabItem createCTabItem(CTabFolder tabFolder, Control control, String Text) {
		CTabItem ti = new CTabItem(tabFolder, SWT.NONE);
		ti.setText(Text);
		ti.setControl(control);
		return ti;
	}
	
	private void createDocsTable(Composite container) {
		docGroup = new Group(container, SWT.SHADOW_ETCHED_IN);
		docGroup.setText("Documents");
		docGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		docGroup.setLayout(new GridLayout(1, false));
		docGroup.setFont(Fonts.createBoldFont(docGroup.getFont()));
				
		docTabFolder = new CTabFolder(docGroup, /*SWT.BORDER |*/ SWT.FLAT);
		docTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		docsTableWidget = new DocTableWidgetPagination(docTabFolder, 0, 25);		
		docsTableWidget.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override public void selectionChanged(SelectionChangedEvent event) {
				updateBtnVisibility();
			}
		});
		
		myDocsTableWidget = new MyDocsTableWidgetPagination(docTabFolder, 0, 25);		
		myDocsTableWidget.refreshPage(true);
		myDocsTableWidget.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override public void selectionChanged(SelectionChangedEvent event) {
				updateBtnVisibility();
			}
		});
		
		createCTabItem(docTabFolder, docsTableWidget, "Documents in collection");
		createCTabItem(docTabFolder, myDocsTableWidget, "Uploaded documents");
		
		docTabFolder.setSelection(0);
		
		docTabFolder.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {

				updateBtnVisibility();
			}
		});

		Composite btns = new Composite(docGroup, 0);
		btns.setLayout(new GridLayout(4, false));
		btns.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 4, 1));	
						
		addDocumentToCollBtn = new Button(btns, SWT.PUSH);
		addDocumentToCollBtn.setText("Add to collection...");
		addDocumentToCollBtn.setImage(Images.getOrLoad("/icons/add.png"));
		addDocumentToCollBtn.setToolTipText("Add document to selected collection");
		addDocumentToCollBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
		
		removeDocumentFromCollBtn = new Button(btns, SWT.PUSH);
		removeDocumentFromCollBtn.setText("Remove from collection");
		removeDocumentFromCollBtn.setToolTipText("Remove selected document from collection");
		removeDocumentFromCollBtn.setImage(Images.getOrLoad("/icons/delete.png"));			
		removeDocumentFromCollBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));

		deleteDocumentBtn = new Button(btns, SWT.PUSH);
		deleteDocumentBtn.setText("Delete document");
		deleteDocumentBtn.setImage(Images.getOrLoad("/icons/delete.png"));
		deleteDocumentBtn.setToolTipText("Delete document from server");
		deleteDocumentBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
		
		duplicatedDocumentBtn = new Button(btns, SWT.PUSH);
		duplicatedDocumentBtn.setText("Duplicate document");
		duplicatedDocumentBtn.setImage(Images.PAGE_COPY);
		duplicatedDocumentBtn.setToolTipText("Duplicate document from server");		
		duplicatedDocumentBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
		
		searchBtn = new Button(btns, 0);
		searchBtn.setToolTipText("Search for documents, keywords... tbc");
		searchBtn.setText("Find documents");
		searchBtn.setImage(Images.getOrLoad("/icons/find.png"));
		searchBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		SWTUtil.onSelectionEvent(searchBtn, (e) -> { openSimpleSearchDialog(); });
	}
	
	public int getSelectedCollectionId() {
		return getSelectedCollection()==null ? -1 : getSelectedCollection().getColId();
	}

	public TrpCollection getSelectedCollection() {
		return collectionsTv.getFirstSelected();		
	}
	
	public boolean isMyDocsTabOpen() {
		return docTabFolder.getSelectionIndex()==1;
	}
		
	public List<TrpDocMetadata> getSelectedDocuments() {
		return ((IStructuredSelection) getCurrentDocTableWidgetPagination().getTableViewer().getSelection()).toList();
	}
	
	public TrpDocMetadata getFirstSelectedDocument() {
		List<TrpDocMetadata> selected = getSelectedDocuments();
		if (selected.isEmpty())
			return null;
		else
			return selected.get(0);
	}
	
	public List<TrpUser> getSelectedUsersInCollection() {
		return collectionUsersWidget.getSelectedUsersInCollection();
	}
		
	private void updateBtnVisibility() {
		TrpCollection c = getSelectedCollection();
		boolean isAdmin = store.getUser() != null ? store.getUser().isAdmin() : false;
		
		boolean hasRole = c!=null && c.getRole()!=null;
//		boolean canManage = hasRole && c.getRole().canManage() || isAdmin;
		boolean isOwner = hasRole && c.getRole().getValue()>=TrpRole.Owner.getValue() || isAdmin;
		
//		boolean hasCollectionUsersSelected = !getSelectedUsersInCollection().isEmpty();
		boolean hasDocsSelected = !getSelectedDocuments().isEmpty();
		boolean hasCollSelected = getSelectedCollection()!=null;
		
		deleteCollectionBtn.setEnabled(hasCollSelected);
		modifyCollectionBtn.setEnabled(hasCollSelected && isOwner);
		 
//		editUserFromColBtn.setEnabled(isOwner && hasCollectionUsersSelected);
		
		removeDocumentFromCollBtn.setEnabled(hasDocsSelected && !isMyDocsTabOpen());
		duplicatedDocumentBtn.setEnabled(hasDocsSelected);
		addDocumentToCollBtn.setEnabled(hasDocsSelected);
		deleteDocumentBtn.setEnabled(hasDocsSelected);
		
		collectionUsersWidget.updateBtnVisibility();
	}
	
	public void updateDocumentsTable(TrpDocMetadata docMd, boolean resetToFirstPage) {
		logger.debug("updating documents...");
		TrpCollection c = getSelectedCollection();

		if (c!=null && store.isLoggedIn()) {
			if(resetToFirstPage){
				docsTableWidget.refreshList(c.getColId(), resetToFirstPage);
			}
			if (docMd != null){
				docsTableWidget.loadPage("docId", docMd.getDocId(), false);
			}
		}
	}
		
	public void updateUsersForSelectedCollection() {
		collectionUsersWidget.setCollection(getSelectedCollection());
	}
	
	public void updateAll() {
		updateCollections();
//		updateMyDocuments();
	}
	
	public void updateCollections() {
		logger.debug("updating collections");
		collectionsTv.refreshList(Storage.getInstance().getCollections());
		selectCurrentCollection();
		
		updateUsersForSelectedCollection();
		updateDocumentsTable(serverWidget.getSelectedDocument(), true);
	}
	
	public void selectCurrentCollection() {
		TrpCollection c = serverWidget.getSelectedCollection();
		if (c == null)
			return;
		
		logger.debug("loading collection with id: "+c.getColId());
		collectionsTv.loadPage("colId", c.getColId(), false);
	}
	
	private void openSimpleSearchDialog() {
		TrpCollection c = getSelectedCollection();
		if (c == null)
			return;
		SimpleSearchDialog d = new SimpleSearchDialog(shell, c.getColId(), this);
		d.open();
		
	}
	
	public DocTableWidgetPagination getCurrentDocTableWidgetPagination() {
		if (docTabFolder.getSelectionIndex() == 0){
			return docsTableWidget;
		}
		else{
			return myDocsTableWidget;	
		}
	}

	public CTabFolder getDocTabFolder() {
		return docTabFolder;
	}
				
}
