package eu.transkribus.swt_gui.pagination_tables;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.nebula.widgets.pagination.IPageLoader;
import org.eclipse.nebula.widgets.pagination.collections.PageResult;
import org.eclipse.nebula.widgets.pagination.collections.PageResultLoaderList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.exceptions.NotImplementedException;
import eu.transkribus.core.model.beans.PageLock;
import eu.transkribus.core.model.beans.TrpAction;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.swt.pagination_table.ATableWidgetPagination;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.collection_comboviewer.CollectionSelectorWidget;
import eu.transkribus.swt_gui.collection_comboviewer.CollectionTableComboViewerWidget;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class PageLockTablePagination extends ATableWidgetPagination<TrpAction> implements IDoubleClickListener {
	private final static Logger logger = LoggerFactory.getLogger(PageLockTablePagination.class);
	
	static final boolean USE_LIST_LOADER = true;
	PageResultLoaderList<TrpAction> listLoader;
	List<PageLock> locks = new ArrayList<>();
	List<TrpAction> actions = new ArrayList<>();
	
	public static final String USERNAME_COL = "User";
	public static final String LOGINTIME_COL = "Time";
	public static final String COL_ID_COL = "Col-ID";
	public static final String DOC_ID_COL = "Doc-ID";
	public static final String PAGE_NR_COL = "Page-NR";
	public static final String TYPE = "Type";
	
	Button showAllLocksBtn;
	CollectionSelectorWidget collectionsSelector;
	Text docIdText;
	Text pageNrText;
	
	Storage store = Storage.getInstance();

	public PageLockTablePagination(Composite parent, int style, int initialPageSize, int collectionId) {
		super(parent, style, initialPageSize);
		
		Composite btns = new Composite(this, 0);
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.fill = true;
//		btns.setLayout(rl);
		btns.setLayout(new GridLayout(2, false));
		btns.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btns.moveAbove(pageableTable);
		
		//for showing the currently logged in users
//		try {
//			System.out.println(Storage.getInstance().getConnection().countUsersLoggedIn());
//		} catch (SessionExpiredException | ServerErrorException | ClientErrorException | IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		if (store.isAdminLoggedIn()) {
			showAllLocksBtn = new Button(btns, SWT.CHECK);
			showAllLocksBtn.setText("Show all locked pages");
			showAllLocksBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		}
		
		collectionsSelector = new CollectionSelectorWidget(btns, SWT.READ_ONLY | SWT.DROP_DOWN, true, null);
		collectionsSelector.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		if (collectionId != -1)
			collectionsSelector.setSelectedCollection(store.getCollection(collectionId));
		
//		collectionsViewer.getCollectionLabel().setText("Collection:");
//		Label l1 = new Label(collectionsViewer, 0);
//		l1.setText("Collection: ");
//		l1.moveAbove(collectionsViewer.collectionCombo);
//		collectionsViewer.layout();
		
		Label l0 = new Label(btns, 0);
		l0.setText("Doc-Id: ");
		docIdText = new Text(btns, SWT.SINGLE);
		docIdText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		Label l1 = new Label(btns, 0);
		l1.setText("Page-Nr: ");
		pageNrText = new Text(btns, SWT.SINGLE);
		pageNrText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		refreshLocks();
		addListener();
	}
		
	int parseDocId() {
		try {
			return Integer.valueOf(docIdText.getText());
		} catch (Exception e) {
			return -1;
		}
	}
	
	int parsePageNr() {
		try {
			return Integer.valueOf(pageNrText.getText());
		} catch (Exception e) {
			return -1;
		}
	}
	
	@Override protected void onReloadButtonPressed() {
		super.onReloadButtonPressed();
		refreshLocks();
	}
	
	void addListener() {
		if (showAllLocksBtn != null) {
			showAllLocksBtn.addSelectionListener(new SelectionAdapter() {
				@Override public void widgetSelected(SelectionEvent e) {
					refreshLocks();
				}
			});
		}
		
		docIdText.addTraverseListener(new TraverseListener() {
			@Override public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					refreshLocks();
				}
			}
		});
		
		pageNrText.addTraverseListener(new TraverseListener() {
			@Override public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					refreshLocks();
				}
			}
		});
		
		collectionsSelector.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				logger.trace("refreshing locks...");
				docIdText.setText("");
				pageNrText.setText("");
				refreshLocks();				
			}
		});
		
		this.getPageableTable().getViewer().addDoubleClickListener(this);
				
	}
	
	boolean isShowAllLocks() {
		return showAllLocksBtn==null ? false : showAllLocksBtn.getSelection();
	}
	
	void refreshLocks() {
		try {
			//for admins: if show all locks - get list of all users currently have locked a page
			List<TrpAction> actions = new ArrayList<TrpAction>();
			int colId = -1;
			if (isShowAllLocks()){
				logger.debug("listing locks from server, colId = "+colId);
				List<PageLock> locks = store.listPageLocks(colId, -1, -1);
				logger.debug("got "+locks.size()+" locks!");
				for (PageLock lock : locks){
					TrpAction action = new TrpAction();
					action.setUserName(lock.getUserName());
					action.setColId(lock.getColId());
					action.setDocId(lock.getDocId());
					action.setTime(lock.getLoginTime());
					action.setType("Page Lock");
					action.setPageNr(lock.getPageNr());
					actions.add(action);
				}
			}
			//we show the edit history (save, status change) per collection
			else{
			
				TrpCollection col = collectionsSelector.getSelectedCollection();
				colId = (col == null) ? -1 : col.getColId();
				
				logger.debug("listing actions from server, colId = "+colId);
				//List<PageLock> locks = store.listPageLocks(colId, -1, -1);
				actions = store.listAllActions(colId, -1, 1000);
			}
			
			int docId = parseDocId();
			int pageNr = parsePageNr();
		
			// filter docId locally:
			if (docId != -1) {
				for (ListIterator<TrpAction> it = actions.listIterator(); it.hasNext(); ) {
					TrpAction l = it.next();
					if (l.getDocId() != docId)
						it.remove();
				}
				
			}
			
			//filter pageNr
			if (pageNr != -1) {
				for (ListIterator<TrpAction> it = actions.listIterator(); it.hasNext(); ) {
					TrpAction l = it.next();
					if (l.getPageNr() != pageNr)
						it.remove();
				}
				
			}

			logger.debug("got "+actions.size()+" actions!");
			refreshList(actions);
			
		} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException | NoConnectionException e1) {
			onError("Error loading page locks", e1);
		}
	}
	
	protected void onError(String title, Throwable th) {
		DialogUtil.showBallonToolTip(this, SWT.ICON_ERROR, title, th.getMessage());
//		DialogUtil.showErrorMessageBox(getShell(), title, th.getMessage());
	}
	
//	public void refreshLockList(List<PageLock> locks) {
//		this.locks = locks;
//		
//		if (USE_LIST_LOADER && listLoader!=null) {
//			listLoader.setItems(this.locks);
////			for (PageLock lock : this.locks){
////				logger.debug(" login time: " + lock.getLoginTime());
////			}
//		}
//		
//		refreshPage(true);
//	}
	
	public void refreshList(List<TrpAction> actions) {
		this.actions = actions;
		
		if (USE_LIST_LOADER && listLoader!=null) {
			listLoader.setItems(this.actions);
//			for (PageLock lock : this.locks){
//				logger.debug(" login time: " + lock.getLoginTime());
//			}
		}
		
		refreshPage(true);
	}

	@Override protected void setPageLoader() {
		IPageLoader<PageResult<TrpAction>> loader;
		
		if (USE_LIST_LOADER) {
//			List<TrpCollection> collections = Storage.getInstance().getCollections();
			listLoader = new PageResultLoaderList<TrpAction>(actions);
			loader = listLoader;
		} else {
			throw new NotImplementedException("remote paging loader not implemented for page locks yet!");
		}

		pageableTable.setPageLoader(loader);		
	}

	@Override protected void createColumns() {
		createDefaultColumn(USERNAME_COL, 200, "userName", true);
		createDefaultColumn(LOGINTIME_COL, 180, "time", true);
		createDefaultColumn(COL_ID_COL, 50, "colId", true);
		createDefaultColumn(DOC_ID_COL, 60, "docId", true);
		createDefaultColumn(PAGE_NR_COL, 50, "pageNr", true);
		createDefaultColumn(TYPE, 100, "type", true);
	}
	
	public static void main(String[] args) {
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				// getShell().setLayout(new FillLayout());
				getShell().setSize(300, 200);
				
				PageLockTablePagination w = new PageLockTablePagination(getShell(), 0, 25, -1);
				
				
//				Button btn = new Button(parent, SWT.PUSH);
//				btn.setText("Open upload dialog");
//				btn.addSelectionListener(new SelectionAdapter() {
//					@Override public void widgetSelected(SelectionEvent e) {
//						(new UploadDialogUltimate(getShell(), null)).open();
//					}
//				});

				SWTUtil.centerShell(getShell());

				return parent;
			}
		};
		aw.setBlockOnOpen(true);
		aw.open();

		Display.getCurrent().dispose();
	}

	@Override
	public void doubleClick(DoubleClickEvent arg0) {
		TrpMainWidget mw = TrpMainWidget.getInstance();
		
		TrpAction action = this.getFirstSelected();
		logger.debug("double click on action: "+action);
		
		if (action!=null) {
			logger.debug("Loading doc: " + action.getDocId());
			int pageNr = (action.getPageNr() != null ? action.getPageNr() : 1);
			mw.loadRemoteDoc(action.getDocId(), action.getColId(), pageNr-1);
		}	
		
	}

}
