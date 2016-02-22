package eu.transkribus.swt_gui.pagination_tables;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.ws.rs.ServerErrorException;

import org.dea.swt.pagination_table.ATableWidgetPagination;
import org.dea.swt.util.DialogUtil;
import org.dea.swt.util.SWTUtil;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.exceptions.NotImplementedException;
import eu.transkribus.core.model.beans.PageLock;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.swt_gui.collection_comboviewer.CollectionComboViewerWidget;
import eu.transkribus.swt_gui.mainwidget.Storage;

public class PageLockTablePagination extends ATableWidgetPagination<PageLock> {
	private final static Logger logger = LoggerFactory.getLogger(PageLockTablePagination.class);
	
	static final boolean USE_LIST_LOADER = true;
	PageResultLoaderList<PageLock> listLoader;
	List<PageLock> locks = new ArrayList<>();
	
	public static final String USERNAME_COL = "User";
	public static final String LOGINTIME_COL = "Login-Time";
	public static final String COL_ID_COL = "Col-ID";
	public static final String DOC_ID_COL = "Doc-ID";
	public static final String PAGE_NR_COL = "Page";
	
	Button showAllLocksBtn;
	CollectionComboViewerWidget collectionsViewer;
	Text docIdText;
	
	Storage store = Storage.getInstance();

	public PageLockTablePagination(Composite parent, int style, int initialPageSize) {
		super(parent, style, initialPageSize);
		
		Composite btns = new Composite(this, 0);
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.fill = true;
//		btns.setLayout(rl);
		btns.setLayout(new GridLayout(2, false));
		btns.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btns.moveAbove(pageableTable);
		
		if (store.isAdminLoggedIn()) {
			showAllLocksBtn = new Button(btns, SWT.CHECK);
			showAllLocksBtn.setText("Show all");
			showAllLocksBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		}
		
		collectionsViewer = new CollectionComboViewerWidget(btns, SWT.READ_ONLY | SWT.DROP_DOWN, false, false, false);
		collectionsViewer.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		Label l1 = new Label(collectionsViewer, 0);
		l1.setText("Collection: ");
		l1.moveAbove(collectionsViewer.collectionCombo);
		collectionsViewer.layout();
		
		Label l0 = new Label(btns, 0);
		l0.setText("Doc-Id: ");
		docIdText = new Text(btns, SWT.SINGLE);
		docIdText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
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
	
	void addListener() {
		getReloadButton().addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				refreshLocks();
			}
		});
		
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
		
		collectionsViewer.collectionCombo.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				refreshLocks();
			}
		});
		
	}
	
	boolean isShowAllLocks() {
		return showAllLocksBtn==null ? false : showAllLocksBtn.getSelection();
	}
	
	void refreshLocks() {
		try {
			TrpCollection col = collectionsViewer.getSelectedCollection();
			int colId = (col == null || isShowAllLocks()) ? -1 : col.getColId();
			int docId = parseDocId();
			
			logger.debug("listing locks from server, colId = "+colId+" docId = "+docId);
			List<PageLock> locks = store.listPageLocks(colId, -1, -1);
			// filter docId locally:
			if (docId != -1) {
				for (ListIterator<PageLock> it = locks.listIterator(); it.hasNext(); ) {
					PageLock l = it.next();
					if (l.getDocId() != docId)
						it.remove();
				}
			}

			logger.debug("got "+locks.size()+" locks!");
			refreshList(locks);
		} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException | NoConnectionException e1) {
			onError("Error loading page locks", e1);
		}
	}
	
	protected void onError(String title, Throwable th) {
		DialogUtil.showBallonToolTip(this, SWT.ICON_ERROR, title, th.getMessage());
//		DialogUtil.showErrorMessageBox(getShell(), title, th.getMessage());
	}
	
	public void refreshList(List<PageLock> locks) {
		this.locks = locks;
		
		if (USE_LIST_LOADER && listLoader!=null) {
			listLoader.setItems(this.locks);
		}
		
		refreshPage(true);
	}

	@Override protected void setPageLoader() {
		IPageLoader<PageResult<PageLock>> loader;
		
		if (USE_LIST_LOADER) {
//			List<TrpCollection> collections = Storage.getInstance().getCollections();
			listLoader = new PageResultLoaderList<PageLock>(locks);
			loader = listLoader;
		} else {
			throw new NotImplementedException("remote paging loader not implemented for page locks yet!");
		}

		pageableTable.setPageLoader(loader);		
	}

	@Override protected void createColumns() {
		createDefaultColumn(USERNAME_COL, 200, "userName", true);
		createDefaultColumn(LOGINTIME_COL, 180, "loginTime", true);
		createDefaultColumn(COL_ID_COL, 50, "colId", true);
		createDefaultColumn(DOC_ID_COL, 60, "docId", true);
		createDefaultColumn(PAGE_NR_COL, 50, "pageNr", true);
	}
	
	public static void main(String[] args) {
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				// getShell().setLayout(new FillLayout());
				getShell().setSize(300, 200);
				
				PageLockTablePagination w = new PageLockTablePagination(getShell(), 0, 25);
				
				
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

}
