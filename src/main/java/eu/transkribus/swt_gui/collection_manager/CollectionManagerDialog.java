package eu.transkribus.swt_gui.collection_manager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.InvocationCallback;

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

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpUserCollection;
import eu.transkribus.core.model.beans.auth.TrpRole;
import eu.transkribus.core.model.beans.auth.TrpUser;
import eu.transkribus.swt.pagination_table.IPageLoadMethods;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.doc_overview.ServerWidget;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.pagination_tables.CollectionsTableWidgetPagination;
import eu.transkribus.swt_gui.pagination_tables.DocTableWidgetPagination;
import eu.transkribus.swt_gui.pagination_tables.UserTableWidgetPagination;
import eu.transkribus.swt_gui.search.SimpleSearchDialog;

//public class CollectionManagerWidget extends Composite {
public class CollectionManagerDialog extends Dialog {
	
	private final static Logger logger = LoggerFactory.getLogger(CollectionManagerDialog.class);

	CollectionsTableWidgetPagination collectionsTv;
	UserTableWidgetPagination collectionUsersTv;
	DocTableWidgetPagination docsTableWidget; 
	DocTableWidgetPagination myDocsTableWidget;
	
	Button addCollectionBtn, deleteCollectionBtn, modifyCollectionBtn;
//	Text newCollNameText;
	
//	Button showUploadedDocsCheck;
	Button addUserToColBtn, removeUserFromColBtn/*, editUserFromColBtn*/;
	Button addDocumentToCollBtn, removeDocumentFromCollBtn;
	Button deleteDocumentBtn;
	Button duplicatedDocumentBtn;
	Button searchBtn, closeBtn;
//	Button reloadCollectionsBtn;
	Combo role;
	
	Group docGroup;
	
	FindUsersWidget findUsersWidget;
	
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
//		shell.setLayout(new GridLayout(2, false));
//		shell.setLayout(new GridLayout(4, false));
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
		
		Composite second = new SashForm(container, SWT.VERTICAL);
		second.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		second.setLayout(new GridLayout(1, false));
		
//		Composite c1 = new SashForm(container, SWT.VERTICAL);
//		c1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		c1.setLayout(new GridLayout(1, false));
			
		createCollectionUsersTable(second);
		createFindUsersWidget(second);
	
//		Composite c2 = new SashForm(container, SWT.VERTICAL);
//		c2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		c2.setLayout(new GridLayout(1, false));
		
//		createMyDocsTable(c2);
		
//		addTableEditors();
		addListener();
		
		updateCollections();
		updateUsersForSelectedCollection();
		updateDocumentsTable(serverWidget.getSelectedDocument(), true);
//		shell.pack();
		
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
				updateDocumentsTable(serverWidget.getSelectedDocument(), true);
			}
		});
		
		collectionUsersTv.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override public void selectionChanged(SelectionChangedEvent event) {
				//new user selected in 'Users in collection' ==> deselect user in 'Find users' table and vice versa
				ISelection users = collectionUsersTv.getSelectedAsIStructuredSelection();
				if (!findUsersWidget.getSelectedUsers().isEmpty()){
					findUsersWidget.getUsersTableViewer().setSelection(null);
					if(users != null)
						collectionUsersTv.getTableViewer().setSelection(users);
				}
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
		group.setFont(Fonts.createBoldFont(group.getFont()));
		
		findUsersWidget = new FindUsersWidget(group, 0);
		findUsersWidget.getUsersTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override public void selectionChanged(SelectionChangedEvent event) {
				//new user selected in 'Find users' ==> deselect user in 'Users in collection' table and vice versa
				IStructuredSelection users = findUsersWidget.getSelectedUsersAsStructuredSelection();
				if (!getSelectedUsersInCollection().isEmpty()){
					collectionUsersTv.getTableViewer().setSelection(null);
					if(users != null)
						findUsersWidget.setSelectedUsers(users);
				}
				
				updateBtnVisibility();
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
				serverWidget.setSelectedCollection(col.getColId(), true);
			}
		};		
		collectionsTv.getTableViewer().addDoubleClickListener(openSelectedColListener);
		
		Composite btns = new Composite(group, 0);
//		btns.setLayout(new RowLayout(SWT.HORIZONTAL));
		btns.setLayout(new GridLayout(6, false));
		btns.setLayoutData(new GridData(SWT.TOP, SWT.FILL, true, false, 1, 1));
		
//		reloadCollectionsBtn = new Button(btns, SWT.PUSH);
//		reloadCollectionsBtn.setImage(Images.getOrLoad("/icons/refresh.png"));
//		reloadCollectionsBtn.setToolTipText("(Re)load currently selected collection from main widget");
		
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
		
//		Text l = new Text(btns, SWT.SINGLE | SWT.READ_ONLY
//			    | SWT.BORDER | SWT.CENTER);
//		l.setText("New name: ");
//		Label l = new Label(btns, SWT.CENTER);
//		l.setText("New name: ");
//		
//		newCollNameText = new Text(btns, SWT.SINGLE | SWT.BORDER);
//		newCollNameText.setToolTipText("The new name of the selected collection");
		
		modifyCollectionBtn = new Button(btns, SWT.PUSH);
		modifyCollectionBtn.setText("Modify collection...");
		modifyCollectionBtn.setImage(Images.getOrLoad("/icons/pencil.png"));
		modifyCollectionBtn.setToolTipText("Modify the selected collection using the name on the left");
						
		group.pack();
	}
	
	String getSelectedCollectionName() {
		return getSelectedCollection()!=null ? getSelectedCollection().getColName() : "";
	}
	
//	void updateDocsTableTitle() {
//		if (showUploadedDocsCheck.getSelection()) {
//			docGroup.setText("Documents (uploaded)");
//		} else {
//			docGroup.setText("Documents ("+getSelectedCollectionName()+")");
//		}
//	}
	
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
				
//		docsTableWidget = new DocTableWidgetPagination(docTabFolder, 0, 25, new IPageLoadMethods<TrpDocMetadata>() {
//			Storage store = Storage.getInstance();
//			
//			@Override public int loadTotalSize() {			
//				int N = 0;
//				TrpCollection c = getSelectedCollection();
//				
//				if (store.isLoggedIn()) {
//					try {
//						if (!showUploadedDocsCheck.getSelection()) {
//							if (c!=null)
//								N = store.getConnection().countDocs(c.getColId());
//						} else {
//							N = store.getConnection().countMyDocs();
//						}
//						logger.trace("n-docs = "+N);
//					} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
//						TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
//					}
//				}
//				
//				return N;
//			}
//			
//			@Override public List<TrpDocMetadata> loadPage(int fromIndex, int toIndex, String sortPropertyName, String sortDirection) {
//				List<TrpDocMetadata> docs = new ArrayList<>();
//				TrpCollection c = getSelectedCollection();
//				
//				if (store.isLoggedIn()) {
//					try {
//						if (!showUploadedDocsCheck.getSelection()) {
//							if (c!=null)
//								docs = store.getConnection().getAllDocs(c.getColId(), fromIndex, toIndex-fromIndex, sortPropertyName, sortDirection);
//						} else {
//							docs = store.getConnection().getAllDocsByUser(fromIndex, toIndex-fromIndex, sortPropertyName, sortDirection);
//						}						
//						
//						logger.trace("docs pagesize = "+docs.size());
//					} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
//						TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
//					}
//				}
//				
//				return docs;
//			}
//		});
		
		docsTableWidget.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override public void selectionChanged(SelectionChangedEvent event) {
				updateBtnVisibility();
			}
		});
		

		
		myDocsTableWidget = new DocTableWidgetPagination(docTabFolder, 0, 25, new IPageLoadMethods<TrpDocMetadata>() {
			Storage store = Storage.getInstance();
			
			@Override public int loadTotalSize() {			
				int N = 0;
				
				if (store.isLoggedIn()) {
					try {
						N = store.getConnection().countMyDocs();
						logger.debug("n-docs = "+N);
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
						Future fut = store.getConnection().getAllDocsByUserAsync(0, 0, null, null, new InvocationCallback<List<TrpDocMetadata>>() {

							@Override public void completed(List<TrpDocMetadata> docs) {
								logger.info("SUCCCCESSSS");
								logger.info("response = "+docs);
							}

							@Override public void failed(Throwable throwable) {
								logger.info("ERRRROOORr");
								logger.error("error getting my docs: "+throwable.getMessage(), throwable);
							}
							
						});	
						fut.get();
						
						/*
						docs = store.getConnection().getAllDocsByUser(fromIndex, toIndex-fromIndex, sortPropertyName, sortDirection);
						logger.debug("docs pagesize = "+docs.size());
						*/
						
					} catch (Exception e) {
						TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
					}
				}
				
				return docs;
			}
		});		
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

//		docsTableWidget.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		
//		Composite btns = new Composite(group, 0);
//		btns.setLayout(new RowLayout(SWT.HORIZONTAL));
//		btns.setLayoutData(new GridData(SWT.TOP, SWT.FILL, true, false, 1, 1));
		
		Composite btns = new Composite(docGroup, 0);
		btns.setLayout(new GridLayout(4, false));
		btns.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 4, 1));

//		showUploadedDocsCheck = new Button(btns, SWT.CHECK);
//		showUploadedDocsCheck.setText("Show my uploaded documents");
//		showUploadedDocsCheck.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 2, 1));
//		showUploadedDocsCheck.setToolTipText("Check to show the documents you uploaded in the table");
//		showUploadedDocsCheck.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				updateDocumentsTable(true);
//			}
//		});		
						
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
		duplicatedDocumentBtn.setImage(Images.getOrLoad("/icons/page_copy.png"));
		duplicatedDocumentBtn.setToolTipText("Duplicate document from server");		
		duplicatedDocumentBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
		
		searchBtn = new Button(btns, 0);
		searchBtn.setToolTipText("Search for documents, keywords... tbc");
		searchBtn.setText("Find documents");
		searchBtn.setImage(Images.getOrLoad("/icons/find.png"));
		searchBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		searchBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				openSimpleSearchDialog();
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
//	private void createMyDocsTable(Composite container) {
//		Group group = new Group(container, SWT.SHADOW_ETCHED_IN);
//		group.setText("My documents");
//		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
//		group.setLayout(new GridLayout(1, false));
//		
//		myDocsTableWidget = new DocTableWidgetPagination(group, 0, 25, new IPageLoadMethods<TrpDocMetadata>() {
//			Storage store = Storage.getInstance();
//			
//			@Override public int loadTotalSize() {
//				int N = 0;
//				
//				if (store.isLoggedIn()) {
//					try {
//						N = store.getConnection().countMyDocs();
//						logger.debug("N MYDOCS = "+N);
//					} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
//						TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
//					}
//				}
//				
//				return N;
//			}
//			
//			@Override public List<TrpDocMetadata> loadPage(int fromIndex, int toIndex, String sortPropertyName, String sortDirection) {
//				List<TrpDocMetadata> docs = new ArrayList<>();
//				
//				if (store.isLoggedIn()) {
//					try {
//						docs = store.getConnection().getAllDocsByUser(fromIndex, toIndex-fromIndex, sortPropertyName, sortDirection);
//						logger.debug("MYDOCS pagesize = "+docs.size());
//					} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
//						TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
//					}
//				}
//				
//				return docs;
//			}
//		});
//		
//		myDocsTableWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		
////		MyTableViewer documentsTv = new MyTableViewer(group, SWT.MULTI);
////		documentsTv.setContentProvider(new ArrayContentProvider());
////		documentsTv.setLabelProvider(new DocTableLabelProvider(this));
////		Table table = collectionUsersTv.getTable();
////		table.setHeaderVisible(true);
////		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		
//		Composite btns = new Composite(group, 0);
//		btns.setLayout(new RowLayout(SWT.HORIZONTAL));
//		btns.setLayoutData(new GridData(SWT.TOP, SWT.FILL, true, false, 4, 1));
//		
//		reloadMyDocsBtn = new Button(btns, SWT.PUSH);
//		reloadMyDocsBtn.setImage(Images.getOrLoad("/icons/refresh.png"));
//		
//		addDocumentToCollBtn = new Button(btns, SWT.PUSH);
//		addDocumentToCollBtn.setText("Add to collection");
//		addDocumentToCollBtn.setImage(Images.getOrLoad("/icons/add.png"));
//		addDocumentToCollBtn.setToolTipText("Add document to selected collection");
//		
//		delDocumentBtn = new Button(btns, SWT.PUSH);
//		delDocumentBtn.setText("Delete document");
//		delDocumentBtn.setImage(Images.getOrLoad("/icons/delete.png"));
//		delDocumentBtn.setToolTipText("Delete document from server");
//		
//		duplicateBtn = new Button(btns, SWT.PUSH);
//		duplicateBtn.setText("Duplicate document");
//		duplicateBtn.setImage(Images.getOrLoad("/icons/page_copy.png"));
//		duplicateBtn.setToolTipText("Duplicate document from server");
//	}
	
	private void createCollectionUsersTable(Composite container) {
		Group group = new Group(container, SWT.SHADOW_ETCHED_IN);
		group.setText("Users in collection");
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		group.setLayout(new GridLayout(1, false));
		group.setFont(Fonts.createBoldFont(group.getFont()));
		
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
		btns.setLayout(new GridLayout(4, false));
		
		btns.setLayoutData(new GridData(SWT.TOP, SWT.FILL, true, false, 1, 1));
		addUserToColBtn = new Button(btns, SWT.PUSH);
//		addUserToColBtn.setText("Add user");
		addUserToColBtn.setText("Add user");
		addUserToColBtn.setImage(Images.getOrLoad("/icons/user_add.png"));
		addUserToColBtn.setToolTipText("Add selected users from search window on the right to collection");
		addUserToColBtn.setLayoutData(new GridData(GridData.FILL_VERTICAL));
				
		removeUserFromColBtn = new Button(btns, SWT.PUSH);
		removeUserFromColBtn.setText("Remove user");
		removeUserFromColBtn.setToolTipText("Remove selected users from collection");
		removeUserFromColBtn.setImage(Images.getOrLoad("/icons/user_delete.png"));
		removeUserFromColBtn.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		
//		editUserFromColBtn = new Button(btns, SWT.PUSH);
//		editUserFromColBtn.setText("Edit role");
//		editUserFromColBtn.setToolTipText("Edit role of selected users from collection");
//		editUserFromColBtn.setImage(Images.getOrLoad("/icons/user_edit.png"));
		
//		Composite btns2 = new Composite(group, 0);
//		btns2.setLayout(new RowLayout(SWT.HORIZONTAL));
//		btns2.setLayoutData(new GridData(SWT.TOP, SWT.FILL, true, false, 1, 1));
		
		Label l = new Label(btns, 0);
		l.setText("Change Role:");
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		gd.verticalAlignment = SWT.CENTER;
		removeUserFromColBtn.setLayoutData(gd);
		
		role = new Combo(btns, SWT.READ_ONLY);
		role.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		for (TrpRole r : TrpRole.values()) {
			if (!r.isVirtual()) {
				role.add(r.toString());
			}
		}
		
		selectRole(TrpRole.Transcriber);
				
		group.pack();
	}
	
	public TrpRole getSelectedRole() {
		return TrpRole.fromStringNonVirtual(role.getItem(role.getSelectionIndex()));
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
	
	public int getSelectedCollectionId() {
		return getSelectedCollection()==null ? -1 : getSelectedCollection().getColId();
	}

	
	public TrpCollection getSelectedCollection() {
		return collectionsTv.getFirstSelected();
//		IStructuredSelection sel = (IStructuredSelection) collectionsTv.getTableViewer().getSelection();
//		return (TrpCollection) sel.getFirstElement();		
	}
	
	public boolean isUploadedDocTabOpen() {
		return docTabFolder.getSelectionIndex()==1;
	}
		
	public List<TrpDocMetadata> getSelectedDocuments() {
		IStructuredSelection sel = null;
		if (!isUploadedDocTabOpen())
			sel = (IStructuredSelection) docsTableWidget.getTableViewer().getSelection();
		else
			sel = (IStructuredSelection) myDocsTableWidget.getTableViewer().getSelection();
		
		return sel.toList();
	}
	
	public List<TrpUser> getSelectedUsersInCollection() {
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
		boolean hasDocsSelected = !getSelectedDocuments().isEmpty();
		boolean hasCollSelected = getSelectedCollection()!=null;
		
		deleteCollectionBtn.setEnabled(hasCollSelected);
		modifyCollectionBtn.setEnabled(hasCollSelected && isOwner);
			
		addUserToColBtn.setEnabled(canManage && hasFindUsersSelected);
		removeUserFromColBtn.setEnabled(canManage && hasCollectionUsersSelected);
		 
//		editUserFromColBtn.setEnabled(isOwner && hasCollectionUsersSelected);
		
		removeDocumentFromCollBtn.setEnabled(hasDocsSelected && !isUploadedDocTabOpen());
		duplicatedDocumentBtn.setEnabled(hasDocsSelected);
		addDocumentToCollBtn.setEnabled(hasDocsSelected);
		deleteDocumentBtn.setEnabled(hasDocsSelected);
		
		if (canManage && hasCollectionUsersSelected){
			role.setEnabled(true);
			// update role combo:
			List<TrpUser> us = getSelectedUsersInCollection();
			if (us.size() > 0) {
				TrpUserCollection uc = us.get(0).getUserCollection();
				TrpRole r = uc == null ? null : uc.getRole(); 
				selectRole(r);
			}
		}
		else{
			role.setEnabled(false);
		}
	}
	
	public void updateDocumentsTable(TrpDocMetadata docMd, boolean resetToFirstPage) {
		logger.debug("updating documents...");
		TrpCollection c = getSelectedCollection();
		
		//docOverviewWidget.getSelectedDocument();
		
		if (c!=null && store.isLoggedIn()) {
			if(resetToFirstPage){
				docsTableWidget.refreshList(c.getColId(), resetToFirstPage);
			}

				
				//TrpDocMetadata docMd = docOverviewWidget.getSelectedDocument();
				if (docMd != null){
					docsTableWidget.loadPage("docId", docMd.getDocId(), false);
				}
				
				//docsTableWidget.selectElement(docMd);
				
			
			//updateDocsTableTitle();			
		}
	}
		
	public void updateUsersForSelectedCollection() {
		logger.debug("updating users for selected collection...");
		TrpCollection c = getSelectedCollection();
		updateBtnVisibility();
		
		if (c!=null && store.isLoggedIn()) {
			try {
				collectionUsersTv.refreshList(c.getColId());
			} catch (ServerErrorException | IllegalArgumentException e) {
				DialogUtil.createAndShowBalloonToolTip(shell, SWT.ICON_ERROR, e.getMessage(), "Error loading users", -1, -1, true);
			}
		}
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
		
		collectionsTv.loadPage("colId", c.getColId(), false);
	}
	
	private void openSimpleSearchDialog() {
		TrpCollection c = getSelectedCollection();
		if (c == null)
			return;
		SimpleSearchDialog d = new SimpleSearchDialog(shell, c.getColId(), this);
		d.open();
		
	}
	
	public DocTableWidgetPagination getCurrentDocTableWidgetPagination(){
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
				
//	public void setCollectionsUsers(List<TrpUser> users) {
//		collectionUsersTv.refreshList(users);
//	}
	
//	public boolean isShowUploadedDocs() {
//		return showUploadedDocsCheck.getSelection();
//	}
	
}
