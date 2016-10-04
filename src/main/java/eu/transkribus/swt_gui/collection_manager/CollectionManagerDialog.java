package eu.transkribus.swt_gui.collection_manager;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpUserCollection;
import eu.transkribus.core.model.beans.auth.TrpRole;
import eu.transkribus.core.model.beans.auth.TrpUser;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.pagination_table.IPageLoadMethods;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.doc_overview.ServerDocsWidget;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.pagination_tables.CollectionsTableWidgetPagination;
import eu.transkribus.swt_gui.pagination_tables.DocTableWidgetPagination;
import eu.transkribus.swt_gui.pagination_tables.UserTableWidgetPagination;


@Deprecated
public class CollectionManagerDialog extends Dialog {
	
	private final static Logger logger = LoggerFactory.getLogger(CollectionManagerDialog.class);
//	MyTableViewer collectionsTv;
	CollectionsTableWidgetPagination collectionsTv;
//	MyTableViewer collectionUsersTv;
	UserTableWidgetPagination collectionUsersTv;
	DocTableWidgetPagination docsForCollectionTableWidget; 
	DocTableWidgetPagination myDocsTableWidget;
	
	Button addCollectionBtn, deleteCollectionBtn, modifyCollectionBtn;
	Text newCollNameText;
	
	Button addUserToColBtn, removeUserFromColBtn, editUserFromColBtn;
	Button addDocumentToCollBtn, removeDocumentFromCollBtn, delDocumentBtn, duplicateBtn;
	Button reloadMyDocsBtn, reloadCollectionsBtn;
	Combo role;
	
	FindUsersWidget findUsersWidget;
	
	CollectionManagerListener cml;
	ServerDocsWidget docOverviewWidget;
	
	Shell shell;
	
	public static final String COLL_ID_COL = "ID";
	public static final String COLL_NAME_COL = "Name";
	public static final String COLL_DESC_COL = "Description";
	public static final String COLL_ROLE = "Role";
	
	static final Storage store = Storage.getInstance();
	
	// This are the columns, sorted in their order of appearence in the table:
	public static final ColumnConfig[] COLL_COLS = new ColumnConfig[] {
		new ColumnConfig(COLL_ID_COL, 35, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(COLL_NAME_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(COLL_DESC_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(COLL_ROLE, 75, false, DefaultTableColumnViewerSorter.ASC),
	};
	
	public static final String USER_USERNAME_COL = "Username";
	public static final String USER_FULLNAME_COL = "Name";
	public static final String USER_ROLE_COL = "Role";
	
	public static final ColumnConfig[] USER_COLS = new ColumnConfig[] {
		new ColumnConfig(USER_USERNAME_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(USER_FULLNAME_COL, 75, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(USER_ROLE_COL, 75, false, DefaultTableColumnViewerSorter.ASC),
	};
	
	public static final String DOC_ID_COL = "ID";
	public static final String DOCS_TITLE_COL = "Title";
	public static final String DOC_NPAGES_COL = "N-Pages";
	public static final String DOC_COL = "Owner";
	
	public static final ColumnConfig[] DOCS_COLS = new ColumnConfig[] {
		new ColumnConfig(DOC_ID_COL, 65, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(DOCS_TITLE_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(DOC_NPAGES_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(DOC_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
	};

	public CollectionManagerDialog(Shell parent, int style, ServerDocsWidget docOverviewWidget) {
		super(parent, style |= (SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MODELESS | SWT.MAX));
//		this.setSize(800, 800);
		this.setText("Collection Manager");
		
		this.docOverviewWidget = docOverviewWidget;
	}
	
	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shell.setSize(1500, 900);
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
//		shell.setLayout(new GridLayout(2, false));
//		shell.setLayout(new GridLayout(4, false));
		shell.setLayout(new FillLayout());
		
		Composite container = new SashForm(shell, SWT.HORIZONTAL);
		container.setLayout(new GridLayout(3, false));
		
		createCollectionsTable(container);
		Composite c1 = new SashForm(container, SWT.VERTICAL);
		c1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		c1.setLayout(new GridLayout(1, false));
			
		createCollectionUsersTable(c1);
		createFindUsersWidget(c1);
	
		Composite c2 = new SashForm(container, SWT.VERTICAL);
		c2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		c2.setLayout(new GridLayout(1, false));
		createDocsForCollectionTable(c2);
		createMyDocsTable(c2);
		
//		addTableEditors();
		addListener();
		
		updateCollections();
		updateUsersForSelectedCollection();
		updateDocumentsForSelectedCollection();
//		shell.pack();
	}
	
	public Shell getShell() { return shell; }
	
	void postInit() {
		updateMyDocuments(); // FIXME: if I do this in createContents, the other widgets disapper -> do it here...		
		// select collection selected in doc overview:
		// TODO: get page opened in doc overview widget and open it here, then select collection!
//		TrpCollection selC = docOverviewWidget.getSelectedCollection();
//		if (selC != null) {
//			collectionsTv.getTableViewer().setSelection(new StructuredSelection(selC), true);
//		}
	}
	
//	private void addTableEditors() {
//		final TableEditor cnEditor = new TableEditor(collectionsTv.getTable());
//		cnEditor.horizontalAlignment = SWT.LEFT;
//		cnEditor.grabHorizontal = true;
//		final int EDITABLECOLUMN = 1; // edit 2nd column
//		final Table table = collectionsTv.getTable();
//
//		collectionsTv.getTable().addMouseListener(new MouseListener() {
//			
//			@Override public void mouseUp(MouseEvent e) {
//			}
//			
//			@Override public void mouseDown(MouseEvent e) {
//			}
//			
//			@Override public void mouseDoubleClick(MouseEvent e) {
//                // Clean up any previous editor control
//                Control oldEditor = cnEditor.getEditor();
//                if (oldEditor != null) oldEditor.dispose();
//
//                // Identify the selected row
//                TableItem item = collectionsTv.getTable().getItem(new Point(e.x, e.y));
//                if (item == null) return;
//
//                // The control that will be the editor must be a child of the Table
//                Text newEditor = new Text(table, SWT.NONE);
//                newEditor.setText(item.getText(EDITABLECOLUMN));
//                newEditor.addModifyListener(new ModifyListener() {
//                        public void modifyText(ModifyEvent e) {
//                                Text text = (Text)cnEditor.getEditor();
//                                cnEditor.getItem().setText(EDITABLECOLUMN, text.getText());
//                        }
//                });
//                newEditor.selectAll();
//                newEditor.setFocus();
//                cnEditor.setEditor(newEditor, item, EDITABLECOLUMN);
//			}
//			});
//	}
	
	void addListener() {
		cml = new CollectionManagerListener(this);
		
		collectionsTv.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override public void selectionChanged(SelectionChangedEvent event) {
				updateUsersForSelectedCollection();
				updateDocumentsForSelectedCollection();
			}
		});
		
		collectionUsersTv.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override public void selectionChanged(SelectionChangedEvent event) {
				updateBtnVisibility();
			}
		});
		
		shell.addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(DisposeEvent e) {
				cml.detach();
			}
		});
		
	}
	
	private void createFindUsersWidget(Composite container) {
		Group group = new Group(container, SWT.SHADOW_ETCHED_IN);
		group.setText("Find users");
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		group.setLayout(new FillLayout());
		
		findUsersWidget = new FindUsersWidget(group, 0);
		findUsersWidget.getUsersTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override public void selectionChanged(SelectionChangedEvent event) {
				updateBtnVisibility();
			}
		});
	}
	
	private void createCollectionsTable(Composite container) {
		Group group = new Group(container, SWT.SHADOW_ETCHED_IN);
		group.setText("Collections");
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		group.setLayout(new GridLayout());
		
		collectionsTv = new CollectionsTableWidgetPagination(group, SWT.SINGLE | SWT.FULL_SELECTION, 25);
		collectionsTv.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite btns = new Composite(group, 0);
//		btns.setLayout(new RowLayout(SWT.HORIZONTAL));
		btns.setLayout(new GridLayout(6, false));
		btns.setLayoutData(new GridData(SWT.TOP, SWT.FILL, true, false, 1, 1));
		
		reloadCollectionsBtn = new Button(btns, SWT.PUSH);
		reloadCollectionsBtn.setImage(Images.getOrLoad("/icons/refresh.png"));
		reloadCollectionsBtn.setToolTipText("(Re)load currently selected collection from main widget");
		
		addCollectionBtn = new Button(btns, SWT.PUSH);
		addCollectionBtn.setImage(Images.getOrLoad("/icons/add.png"));
		addCollectionBtn.setToolTipText("Create a new collection");
		addCollectionBtn.pack();
		
		deleteCollectionBtn = new Button(btns, SWT.PUSH);
		deleteCollectionBtn.setImage(Images.getOrLoad("/icons/delete.png"));
		deleteCollectionBtn.setToolTipText("Delete a new collection (only possible for empty collections!)");
		deleteCollectionBtn.pack();
		
//		Text l = new Text(btns, SWT.SINGLE | SWT.READ_ONLY
//			    | SWT.BORDER | SWT.CENTER);
//		l.setText("New name: ");
		Label l = new Label(btns, SWT.CENTER);
		l.setText("New name: ");
		
		newCollNameText = new Text(btns, SWT.SINGLE | SWT.BORDER);
		newCollNameText.setToolTipText("The new name of the selected collection");
		
		modifyCollectionBtn = new Button(btns, SWT.PUSH);
		modifyCollectionBtn.setImage(Images.getOrLoad("/icons/pencil.png"));
		modifyCollectionBtn.setToolTipText("Modify the selected collection using the name on the left");
						
		group.pack();
	}
	
	private void createDocsForCollectionTable(Composite container) {
		Group group = new Group(container, SWT.SHADOW_ETCHED_IN);
		group.setText("Documents in collection");
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		group.setLayout(new GridLayout(1, false));
		
		docsForCollectionTableWidget = new DocTableWidgetPagination(group, 0, 25);
		docsForCollectionTableWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite btns = new Composite(group, 0);
		btns.setLayout(new RowLayout(SWT.HORIZONTAL));
		btns.setLayoutData(new GridData(SWT.TOP, SWT.FILL, true, false, 1, 1));
		
		removeDocumentFromCollBtn = new Button(btns, SWT.PUSH);
		removeDocumentFromCollBtn.setText("Remove from collection");
		removeDocumentFromCollBtn.setToolTipText("Remove selected document from collection");
		removeDocumentFromCollBtn.setImage(Images.getOrLoad("/icons/delete.png"));		
	}
	
	private void createMyDocsTable(Composite container) {
		Group group = new Group(container, SWT.SHADOW_ETCHED_IN);
		group.setText("My documents");
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		group.setLayout(new GridLayout(1, false));
		
		myDocsTableWidget = new DocTableWidgetPagination(group, 0, 25, new IPageLoadMethods<TrpDocMetadata>() {
			Storage store = Storage.getInstance();
			
			@Override public int loadTotalSize() {
				int N = 0;
				
				if (store.isLoggedIn()) {
					try {
						N = store.getConnection().countMyDocs();
						logger.debug("N MYDOCS = "+N);
					} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
						TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
					}
				}
				
				return N;
			}
			
			@Override public List<TrpDocMetadata> loadPage(int fromIndex, int toIndex, String sortPropertyName, String sortDirection) {
				List<TrpDocMetadata> docs = new ArrayList<>();
				
				if (store.isLoggedIn()) {
					try {
						docs = store.getConnection().getAllDocsByUser(fromIndex, toIndex-fromIndex, sortPropertyName, sortDirection);
						logger.debug("MYDOCS pagesize = "+docs.size());
					} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
						TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
					}
				}
				
				return docs;
			}
		});
		
		myDocsTableWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
//		MyTableViewer documentsTv = new MyTableViewer(group, SWT.MULTI);
//		documentsTv.setContentProvider(new ArrayContentProvider());
//		documentsTv.setLabelProvider(new DocTableLabelProvider(this));
//		Table table = collectionUsersTv.getTable();
//		table.setHeaderVisible(true);
//		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite btns = new Composite(group, 0);
		btns.setLayout(new RowLayout(SWT.HORIZONTAL));
		btns.setLayoutData(new GridData(SWT.TOP, SWT.FILL, true, false, 4, 1));
		
		reloadMyDocsBtn = new Button(btns, SWT.PUSH);
		reloadMyDocsBtn.setImage(Images.getOrLoad("/icons/refresh.png"));
		
		addDocumentToCollBtn = new Button(btns, SWT.PUSH);
		addDocumentToCollBtn.setText("Add to collection");
		addDocumentToCollBtn.setImage(Images.getOrLoad("/icons/add.png"));
		addDocumentToCollBtn.setToolTipText("Add document to selected collection");
		
		delDocumentBtn = new Button(btns, SWT.PUSH);
		delDocumentBtn.setText("Delete document");
		delDocumentBtn.setImage(Images.getOrLoad("/icons/delete.png"));
		delDocumentBtn.setToolTipText("Delete document from server");
		
		duplicateBtn = new Button(btns, SWT.PUSH);
		duplicateBtn.setText("Duplicate document");
		duplicateBtn.setImage(Images.getOrLoad("/icons/page_copy.png"));
		duplicateBtn.setToolTipText("Duplicate document from server");
	}
	
	private void createCollectionUsersTable(Composite container) {
		Group group = new Group(container, SWT.SHADOW_ETCHED_IN);
		group.setText("Users in collection");
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		group.setLayout(new GridLayout(1, false));
		
		// NEW:
		collectionUsersTv = new UserTableWidgetPagination(group, 0, 25);
		collectionUsersTv.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		// OLD:
//		collectionUsersTv = new MyTableViewer(group, SWT.MULTI | SWT.FULL_SELECTION);
//		collectionUsersTv.setContentProvider(new ArrayContentProvider());
//		collectionUsersTv.setLabelProvider(new UsersTableLabelProvider(collectionUsersTv));
//		
//		Table table = collectionUsersTv.getTable();
//		table.setHeaderVisible(true);
//		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		
//		collectionUsersTv.addColumns(USER_COLS);
		
		// add buttons:
		Composite btns = new Composite(group, 0);
		btns.setLayout(new RowLayout(SWT.HORIZONTAL));
		btns.setLayoutData(new GridData(SWT.TOP, SWT.FILL, true, false, 1, 1));
		addUserToColBtn = new Button(btns, SWT.PUSH);
//		addUserToColBtn.setText("Add user");
		addUserToColBtn.setText("Add user");
		addUserToColBtn.setImage(Images.getOrLoad("/icons/user_add.png"));
		addUserToColBtn.setToolTipText("Add selected users from search window on the right to collection");
				
		removeUserFromColBtn = new Button(btns, SWT.PUSH);
		removeUserFromColBtn.setText("Remove user");
		removeUserFromColBtn.setToolTipText("Remove selected users from collection");
		removeUserFromColBtn.setImage(Images.getOrLoad("/icons/user_delete.png"));
		
		editUserFromColBtn = new Button(btns, SWT.PUSH);
		editUserFromColBtn.setText("Edit role");
		editUserFromColBtn.setToolTipText("Edit role of selected users from collection");
		editUserFromColBtn.setImage(Images.getOrLoad("/icons/user_edit.png"));
		
		Composite btns2 = new Composite(group, 0);
		btns2.setLayout(new RowLayout(SWT.HORIZONTAL));
		btns2.setLayoutData(new GridData(SWT.TOP, SWT.FILL, true, false, 1, 1));
		
		Label l = new Label(btns2, 0);
		l.setText("Role: ");
		
		role = new Combo(btns2, SWT.READ_ONLY);
		for (TrpRole r : TrpRole.values()) {
			if (!r.isVirtual()) {
				role.add(r.toString());
			}
		}
		selectRole(TrpRole.Transcriber);
				
		group.pack();
	}
	
	void selectRole(TrpRole r) {
		if (r == null)
			return;
		
		for (int i=0; i<role.getItemCount(); ++i) {
			if (role.getItem(i).equals(r.toString())) {
				role.select(i);
				break;
			}
		}		
	}

	
	TrpCollection getSelectedCollection() {
		return collectionsTv.getFirstSelected();
//		IStructuredSelection sel = (IStructuredSelection) collectionsTv.getTableViewer().getSelection();
//		return (TrpCollection) sel.getFirstElement();		
	}
	
	List<TrpDocMetadata> getSelectedMyDocuments() {
		IStructuredSelection sel = (IStructuredSelection) myDocsTableWidget.getTableViewer().getSelection();
		return sel.toList();
	}
	
	List<TrpDocMetadata> getSelectedCollectionDocuments() {
		IStructuredSelection sel = (IStructuredSelection) docsForCollectionTableWidget.getTableViewer().getSelection();
		return sel.toList();
	}
	
	List<TrpUser> getSelectedUsersInCollection() {
		return ((IStructuredSelection) collectionUsersTv.getTableViewer().getSelection()).toList();
	}
		
	void updateBtnVisibility() {
		TrpCollection c = getSelectedCollection();
		boolean isAdmin = store.getUser() != null ? store.getUser().isAdmin() : false;
		
		boolean hasRole = c!=null && c.getRole()!=null;
		boolean canManage = hasRole && c.getRole().canManage() || isAdmin;
		boolean isOwner = hasRole && c.getRole().getValue()>=TrpRole.Owner.getValue() || isAdmin;
		
		boolean hasFindUsersSelected = !findUsersWidget.getSelectedUsers().isEmpty();
		boolean hasCollectionUsersSelected = !getSelectedUsersInCollection().isEmpty();
			
		addUserToColBtn.setEnabled(canManage && hasFindUsersSelected);
		removeUserFromColBtn.setEnabled(canManage && hasCollectionUsersSelected);
		editUserFromColBtn.setEnabled(isOwner && hasCollectionUsersSelected);
		modifyCollectionBtn.setEnabled(isOwner);
		
		// update role combo:
		List<TrpUser> us = getSelectedUsersInCollection();
		if (us.size() > 0) {
			TrpUserCollection uc = us.get(0).getUserCollection();
			TrpRole r = uc == null ? null : uc.getRole(); 
			selectRole(r);
		}
	}
	
	public void updateDocumentsForSelectedCollection() {
		logger.debug("updating documents for selected collection...");
		TrpCollection c = getSelectedCollection();
//		docsForCollectionTableWidget.refreshList(null);
		
		if (c!=null && store.isLoggedIn()) {
			try {
//				List<TrpDocMetadata> docs = store.getConnection().getAllDocs(c.getColId());
				docsForCollectionTableWidget.refreshList(c.getColId(), true);
			} catch (ServerErrorException | IllegalArgumentException e) {
				ToolTip tt = DialogUtil.createAndShowBalloonToolTip(shell, SWT.ICON_ERROR, e.getMessage(), "Error loading documents", -1, -1, true);
			}
		}
	}
	
	public void updateMyDocuments() {
		logger.debug("updating my documents...");
		if (store.isLoggedIn()) {
			try {				
				// OLD:
//				List<TrpDocMetadata> docs = store.getConnection().getAllDocsByUser();
//				logger.debug("docs = "+docs);
//				myDocsTableWidget.setInput(docs);
				// NEW (Pagination):
				myDocsTableWidget.refreshList(0, true);
			} catch (/*SessionExpiredException |*/ ServerErrorException | IllegalArgumentException e) {
				ToolTip tt = DialogUtil.createAndShowBalloonToolTip(shell, SWT.ICON_ERROR, e.getMessage(), "Error loading documents", -1, -1, true);
			}
		}
//		panel.stop();
		
	}
	
	public void updateUsersForSelectedCollection() {
		logger.debug("updating users for selected collection...");
		TrpCollection c = getSelectedCollection();
//		setCollectionsUsers(null);
		updateBtnVisibility();
		
		if (c!=null && store.isLoggedIn()) {
			try {
				// TODO: paging!!
//				List<TrpUser> users = store.getConnection().getUsersForCollection(c.getColId(), null, 0, 0);
//				logger.debug("updating users for collection: "+users.size());
				collectionUsersTv.refreshList(c.getColId());
//				...

				
//				ToolTip tt = DialogUtil.createAndShowBalloonToolTip(getShell(), SWT.ICON_INFORMATION, "Successfully loaded users for collection", "Success", -1, -1, true);
			} catch (ServerErrorException | IllegalArgumentException e) {
				ToolTip tt = DialogUtil.createAndShowBalloonToolTip(shell, SWT.ICON_ERROR, e.getMessage(), "Error loading users", -1, -1, true);
			}
		}
	}
	
	public void updateAll() {
		updateCollections();
		updateMyDocuments();
	}
	
	public void updateCollections() {
		logger.debug("updating collections");
		collectionsTv.refreshList(Storage.getInstance().getCollections());
		selectCurrentCollection();
		
//		collectionsTv.setInput(Storage.getInstance().getCollections());
		updateUsersForSelectedCollection();
		updateDocumentsForSelectedCollection();
//		setCollections(Storage.getInstance().getCollections());
	}
	
	public void selectCurrentCollection() {
		TrpCollection c = docOverviewWidget.getSelectedCollection();
		if (c == null)
			return;
		
		collectionsTv.loadPage("colId", c.getColId(), false);
	}
				
//	public void setCollectionsUsers(List<TrpUser> users) {
//		collectionUsersTv.refreshList(users);
//	}

	public void modifySelectedCollection() {
		logger.debug("modifying selected collection, new name: "+newCollNameText.getText());
		
		String newName = newCollNameText.getText();
		if (newName.isEmpty())
			return;
		
		TrpCollection c = getSelectedCollection();
		if (c!=null && store.isLoggedIn()) {
			try {
				store.getConnection().modifyCollection(c.getColId(), newName);
				store.reloadCollections();
			} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException | NoConnectionException e) {
				ToolTip tt = DialogUtil.createAndShowBalloonToolTip(shell, SWT.ICON_ERROR, e.getMessage(), "Error modifying collection", -1, -1, true);
			}
		}
	}
	
}
