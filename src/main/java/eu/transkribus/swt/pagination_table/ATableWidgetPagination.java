package eu.transkribus.swt.pagination_table;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.nebula.widgets.pagination.IPageLoaderHandler;
import org.eclipse.nebula.widgets.pagination.PageableController;
import org.eclipse.nebula.widgets.pagination.collections.PageResult;
import org.eclipse.nebula.widgets.pagination.collections.PageResultContentProvider;
import org.eclipse.nebula.widgets.pagination.renderers.pagesize.PageSizeComboRenderer;
import org.eclipse.nebula.widgets.pagination.table.PageableTable;
import org.eclipse.nebula.widgets.pagination.table.SortTableColumnSelectionListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.util.TableViewerUtils;

public abstract class ATableWidgetPagination<T> extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(ATableWidgetPagination.class);

	static int DEFAULT_INITIAL_PAGE_SIZE = 50;
	
	protected PageableTable pageableTable;
		
	protected TableViewer tv;
	
	protected IPageLoadMethods<T> methods = null;
	
	protected int initialPageSize = DEFAULT_INITIAL_PAGE_SIZE;
	
	protected LoadingComposite loadingComposite;
	
//	T itemToSelect=null;
	
//	public ATableWidgetPagination(Composite parent, int style) {
//		super(parent, style);
//		this.setLayout(new GridLayout(1, false));
//		this.methods = null;
//		
//		createTable();
//	}
	
	public ATableWidgetPagination(Composite parent, int style, int initialPageSize) {
		this(parent, style, initialPageSize, null, false);
	}

	public ATableWidgetPagination(Composite parent, int style, int initialPageSize, IPageLoadMethods<T> methods) {
		this(parent, style, initialPageSize, methods, false);
	}
	
	public ATableWidgetPagination(Composite parent, int style, int initialPageSize, IPageLoadMethods<T> methods, boolean singleSelection) {
		super(parent, style);
		this.setLayout(new GridLayout(1, false));
		
		this.initialPageSize = initialPageSize;
		this.methods = methods;
		
		createTable(singleSelection);
	}
	
	public String getSortPropertyName() {
		return pageableTable.getController().getSortPropertyName();
	}
	
	public String getSortDirection() {
		return pageableTable.getController().getSortDirection()==SWT.UP ? "desc" : "asc";
	}
	
	public PageableTable getPageableTable() { return pageableTable; }
	
	private static <T> T findItem(List<T> items, String propertyName, Object value) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (items == null)
			return null;
		
		for (T i : items) {
			Object v = PropertyUtils.getProperty(i, propertyName);
			
			logger.trace("prop value: "+v+" values classes: "+(value.getClass())+" / "+(v.getClass())+" values: "+value+" / "+v);
			if (value.equals(v)) {
				return i;
			}
		}
		return null;
	}
	
	/**
	 * Loads the page that contains the specified values
	 */
	public synchronized void loadPage(String propertyName, Object value, boolean refreshFirst) {
		if (propertyName == null || value == null) {
			logger.error("propertyName or value is null - doin' nothin'!");
			return;
		}
		PageableController c = pageableTable.getController();
		if (refreshFirst) {
			logger.debug("refreshing first...");
			pageableTable.refreshPage(true);
		}
		
		logger.debug("loading page, propertyName = "+propertyName+" value = "+value+" currentPage = "+c.getCurrentPage());

		try {			
			// 1st: check if object is present at locally loaded dataset:
			List<T> items = (List<T>) pageableTable.getViewer().getInput();
			List<T> itemsCopy = CoreUtils.copyList(items);
					
			T item = findItem(itemsCopy, propertyName, value);
			if (item != null) {
				logger.debug("found item in current page!");
				selectElement(item);
				return;
			}
			// 2nd: search pages one by one:
			else {
				int currentPage = c.getCurrentPage();
				logger.debug("total elements = "+c.getTotalElements());
				
				PageableController c1 = new PageableController();
				c1.setPageSize(c.getPageSize());
				c1.setTotalElements(c.getTotalElements());
				c1.setSort(c.getSortPropertyName(), c.getSortDirection());
				c1.setCurrentPage(c.getCurrentPage());
				
				for (int i=0; i<c1.getTotalPages(); ++i) {
					if (i == currentPage) // already checked!
						continue;

					c1.setCurrentPage(i);
					PageResult<T> res = (PageResult<T>) pageableTable.getPageLoader().loadPage(c1);
					
//					Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
//					    public void uncaughtException(Thread th, Throwable ex) {
//					        System.out.println("Uncaught exception: " + ex);
//					        ex.printStackTrace();
//					    }
//					};
//					Thread t = new Thread() {
//					    public void run() {
//					        System.out.println("Sleeping ...");
//					        try {
//					            Thread.sleep(1000);
//					        } catch (InterruptedException e) {
//					            System.out.println("Interrupted.");
//					        }
//					        System.out.println("Throwing exception ...");
//					        throw new RuntimeException();
//					    }
//					};
//					t.setUncaughtExceptionHandler(h);
//					t.start();

					
//					PageResult<T> res = PagingUtils.loadPage(methods, c);
					
					c.setCurrentPage(i);
					items = (List<T>) pageableTable.getViewer().getInput();
					itemsCopy = CoreUtils.copyList(items);
					
					//items = res.getContent();
					//

					item = findItem(itemsCopy, propertyName, value);
					
					if (item != null) {
						logger.debug("found item in page "+i);
						logger.debug("item found "+ item);
						
						selectElement(item);
						
						return;
					}
				}
			}
		}
		catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			logger.error(e.getMessage(), e);
		}
		
		
		
		
	}

	void createTable(boolean singleSelection) {
		int tableStyle = SWT.BORDER | SWT.MULTI  | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL;
		if (singleSelection)
			tableStyle |= SWT.SINGLE;
		
		pageableTable = new PageableTable(this, SWT.BORDER, tableStyle, initialPageSize
				, PageResultContentProvider.getInstance(),
				PagingToolBarNavigationRendererFactory.getFactory(),
				PageableTable.getDefaultPageRendererBottomFactory()
				) {
			
//			@Override protected Composite createCompositeTop(Composite parent) {
//				final PageableController c = pageableTable.getController();
//				
//				Composite container = new Composite(parent, 0);
//				container.setLayout(new FillLayout());
//				
//				PagingToolBar pagingToolBar = new PagingToolBar("", true, false, container, SWT.NONE);
//					...
//				
//				container.setLayoutData(new GridData(
//						GridData.FILL_HORIZONTAL));
//				return container;
//			}
			
			@Override
			protected Composite createCompositeBottom(Composite parent) {
//				Composite bottom = new LoadingComposite(parent);
//				bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//				return bottom;
				
				Composite bottom = new Composite(parent, 0);
				bottom.setLayout(new GridLayout(2, false));
				
				PageSizeComboRenderer pageSizeComboDecorator = new PageSizeComboRenderer(
						bottom, SWT.NONE, getController(), new Integer[] { 5, 10, 25, 50, 75, 100, 200 }) {
					
					public void widgetSelected(SelectionEvent e) {
						super.widgetSelected(e);
//						pageableTable.setCurrentPage(0);
						pageableTable.refreshPage(true); // needed to refresh pagination control -> bug in original code!
					}
				};
				pageSizeComboDecorator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				pageSizeComboDecorator.pageSizeChanged(initialPageSize, initialPageSize, getController());
				
				loadingComposite = new LoadingComposite(bottom);
				loadingComposite.reload.addSelectionListener(new SelectionAdapter() {
					@Override public void widgetSelected(SelectionEvent e) {
						pageableTable.refreshPage();
					}
				});
				loadingComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				
				return bottom;
			}
		};
		
//		pageableTable.getPageRendererTopFactory()
	
//		loadingComposite = (LoadingComposite) pageableTable.getCompositeBottom();
//		loadingComposite.reload.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				pageableTable.refreshPage(true);
//			}
//		});

		pageableTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		
//		PageSizeComboRenderer pageSizeComboDecorator = new PageSizeComboRenderer(
//				this, SWT.NONE, pageableTable.getController(), new Integer[] { 5, 10, 25, 50, 75, 100, 200 }) {
//			
//			public void widgetSelected(SelectionEvent e) {
//				super.widgetSelected(e);
////				pageableTable.setCurrentPage(0);
//				pageableTable.refreshPage(true); // needed to refresh pagination control -> bug!
//			}
//		};
//		pageSizeComboDecorator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		pageSizeComboDecorator.pageSizeChanged(initialPageSize, initialPageSize, pageableTable.getController());
		
		tv = pageableTable.getViewer();
		tv.setContentProvider(new ArrayContentProvider());
		tv.setLabelProvider(new LabelProvider());
//		documentsTv.setLabelProvider(new DocTableLabelProvider(this));
		
		Table table = tv.getTable();
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		createColumns();
		setPageLoader();
		
		if (true) {
		pageableTable.setPageLoaderHandler(new IPageLoaderHandler<PageableController>() {
			long time = 0;
			
			public void onBeforePageLoad(PageableController controller) {
//				logger.debug("onBeforePageLoad");
				
				time = System.currentTimeMillis();
				String text = "Loading...";
				logger.trace(text);
				loadingComposite.setText(text);
			}

			public boolean onAfterPageLoad(
					PageableController controller, Throwable e) {
//				logger.debug("onAfterPageLoad");
				long diff = System.currentTimeMillis() - time;
				logger.trace("after page reload: "+diff);
				String text = "Loaded in "+ diff + "(ms) ";
				logger.trace(text);
				loadingComposite.setText(text);
				
//				if (itemToSelect != null) {
//					logger.debug("itemToSelect = "+itemToSelect);
//					selectElement(itemToSelect);
//					itemToSelect = null;
//				}
				
				return true;
			}
		});		
		}
	}
	
	protected abstract void setPageLoader();
	
	public void refreshPage(boolean resetToFirstPage) {
		pageableTable.refreshPage(resetToFirstPage);
	}
	
	public Button getReloadButton() {
		return loadingComposite.reload;
	}
	
	public TableViewer getTableViewer() { return tv; }
	
	public void selectElement(T el) {
		if (el != null) {
			getTableViewer().setSelection(new StructuredSelection(el), true);
		}
	}
	
	public T getFirstSelected() {
		IStructuredSelection sel = (IStructuredSelection) tv.getSelection();		
		return (T) sel.getFirstElement();
	}
	
	public List<T> getSelected() {
		return ((IStructuredSelection) tv.getSelection()).toList();
	}
	
	public IStructuredSelection getSelectedAsIStructuredSelection() {
		return ((IStructuredSelection) tv.getSelection());
	}
	
	protected abstract void createColumns();

	protected TableViewerColumn createColumn(String columnName, int colSize, String sortPropertyName, CellLabelProvider lp) {
		TableViewerColumn col = TableViewerUtils.createTableViewerColumn(tv, 0, columnName, colSize);
		col.setLabelProvider(lp);
		if (sortPropertyName != null)
			col.getColumn().addSelectionListener(new SortTableColumnSelectionListener(sortPropertyName));
		return col;
	}
	
	protected TableViewerColumn createDefaultColumn(String columnName, int colSize, String propertyName, boolean sortable) {
		return createColumn(columnName, colSize, sortable ? propertyName : null, new TableColumnBeanLabelProvider(propertyName));
	}
	
	protected TableViewerColumn createDefaultColumn(String columnName, int colSize, String labelPropertyName, String sortPropertyName) {
		return createColumn(columnName, colSize, sortPropertyName, new TableColumnBeanLabelProvider(labelPropertyName));
	}

}
