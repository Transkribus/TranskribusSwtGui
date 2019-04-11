package eu.transkribus.swt.pagination_tree;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.nebula.widgets.pagination.IPageLoaderHandler;
import org.eclipse.nebula.widgets.pagination.MyPageSizeComboRenderer;
import org.eclipse.nebula.widgets.pagination.PageableController;
import org.eclipse.nebula.widgets.pagination.collections.PageResultContentProvider;
import org.eclipse.nebula.widgets.pagination.table.PageableTable;
import org.eclipse.nebula.widgets.pagination.table.SortTableColumnSelectionListener;
import org.eclipse.nebula.widgets.pagination.tree.PageableTree;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.pagination_table.IPageLoadMethods;
import eu.transkribus.swt.pagination_table.LoadingComposite;
import eu.transkribus.swt.pagination_table.PagingToolBarNavigationRendererFactory;
import eu.transkribus.swt.pagination_table.TableColumnBeanLabelProvider;

public abstract class ATreeWidgetPagination<T> extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(ATreeWidgetPagination.class);

	static int DEFAULT_INITIAL_PAGE_SIZE = 50;
	
	protected PageableTree pageableTree;
	
	protected IPageLoadMethods<T> methods = null;
	
	protected int initialPageSize = DEFAULT_INITIAL_PAGE_SIZE;
	
	protected LoadingComposite loadingComposite;
	
	protected Text filter;
	protected boolean withFilter;
	
	public ATreeWidgetPagination(Composite parent, int tableStyle, int initialPageSize) {
		this(parent, tableStyle, initialPageSize, null, false, false);
	}

	public ATreeWidgetPagination(Composite parent, int tableStyle, int initialPageSize, IPageLoadMethods<T> methods) {
		this(parent, tableStyle, initialPageSize, methods, false, false);
	}
	
	public ATreeWidgetPagination(Composite parent, int tableStyle, int initialPageSize, IPageLoadMethods<T> methods, boolean withFilter) {
		this(parent, tableStyle, initialPageSize, methods, withFilter, false);
	}

	public ATreeWidgetPagination(Composite parent, int tableStyle, int initialPageSize, IPageLoadMethods<T> methods, boolean withFilter, boolean recycleBin) {
		super(parent, 0);
		this.setLayout(new GridLayout(1, false));
		
		this.withFilter = withFilter;
		this.initialPageSize = initialPageSize;
		this.methods = methods;
		
		createTree(tableStyle);
	}
	
	public Text getFilter() {
		return filter;
	}
	
	public String getSortPropertyName() {
		return pageableTree.getController().getSortPropertyName();
	}
	
	public String getSortDirection() {
		return pageableTree.getController().getSortDirection()==SWT.UP ? "desc" : "asc";
	}
	
	public PageableTree getPageableTable() { return pageableTree; }
	
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
	 * Loads the page that contains the specified property / value pair; if found, the element is selected
	 */
	public synchronized void loadPage(String propertyName, Object value, boolean refreshFirst) {
		if (propertyName == null || value == null) {
			logger.error("propertyName or value is null - doin' nothin'!");
			return;
		}
		PageableController c = pageableTree.getController();
		if (refreshFirst) {
			logger.debug("refreshing first...");
			pageableTree.refreshPage(true);
		}
		
		logger.debug("loading page, propertyName = "+propertyName+" value = "+value+" currentPage = "+c.getCurrentPage());

		try {			
			// 1st: check if object is present at locally loaded dataset:
			List<T> items = (List<T>) pageableTree.getViewer().getInput();
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
							
					c.setCurrentPage(i);
					items = (List<T>) pageableTree.getViewer().getInput();
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

	void createTree(int style) {
		int tableStyle = SWT.BORDER | SWT.MULTI  | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL;
		
		if ((style & SWT.SINGLE) != 0) {
			tableStyle |= SWT.SINGLE;
		}
		
		pageableTree = new PageableTree(this, SWT.BORDER, tableStyle, initialPageSize
				, PageResultContentProvider.getInstance(),
				PagingToolBarNavigationRendererFactory.getFactory(),
				PageableTable.getDefaultPageRendererBottomFactory()
				) {
			
			@Override
			protected Composite createCompositeBottom(Composite parent) {				
				Composite bottom = new Composite(parent, 0);
				bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				bottom.setLayout(new GridLayout(withFilter ? 3 : 2, false));

				MyPageSizeComboRenderer pageSizeComboDecorator = new MyPageSizeComboRenderer(
						bottom, SWT.NONE, getController(), new Integer[] { 5, 10, 25, 50, 75, 100, 200 }) {
					
					public void widgetSelected(SelectionEvent e) {						
						pageableTree.refreshPage(true); // needed to refresh pagination control -> bug in original code!						
						super.widgetSelected(e);
					}
				};				
				
				pageSizeComboDecorator.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
				pageSizeComboDecorator.pageSizeChanged(initialPageSize, initialPageSize, getController());
				
				if (ATreeWidgetPagination.this.withFilter) {
					filter = new Text(bottom, SWT.BORDER | SWT.SEARCH);
					filter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					filter.setToolTipText("Filter this list with a keyword");
					filter.setMessage("Filter");
				}
				
				loadingComposite = new LoadingComposite(bottom, false);
				loadingComposite.reload.addSelectionListener(new SelectionAdapter() {
					@Override public void widgetSelected(SelectionEvent e) {
						onReloadButtonPressed();
					}
				});
				loadingComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
								
				return bottom;
			}
		};
		pageableTree.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Tree tree = pageableTree.getViewer().getTree();
		tree.setHeaderVisible(true);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		createColumns();
		setPageLoader();
		
		pageableTree.setPageLoaderHandler(new IPageLoaderHandler<PageableController>() {
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
				return true;
			}
		});
	}
	
	protected void onReloadButtonPressed() {
		pageableTree.refreshPage();
	}
	
	protected abstract void setPageLoader();
	
	public void refreshPage(boolean resetToFirstPage) {
		pageableTree.refreshPage(resetToFirstPage);
	}
	
	public Button getReloadButton() {
		return loadingComposite.reload;
	}

	public TreeViewer getTreeViewer() { return pageableTree.getViewer(); }
	
	public void selectElement(T el) {
		if (el != null) {
			getTreeViewer().setSelection(new StructuredSelection(el), true);
		}
	}
	
	public T getFirstSelected() {
		if(pageableTree.getViewer() == null) {
			return null;
		}
		IStructuredSelection sel = (IStructuredSelection) pageableTree.getViewer().getSelection();		
		return (T) sel.getFirstElement();
	}
	
	public List<T> getSelected() {
		if(pageableTree.getViewer() == null) {
			return new ArrayList<>(0);
		}
		return ((IStructuredSelection) pageableTree.getViewer().getSelection()).toList();
	}
	
	public IStructuredSelection getSelectedAsIStructuredSelection() {
		return ((IStructuredSelection) pageableTree.getViewer().getSelection());
	}
	
	protected abstract void createColumns();

	protected TreeViewerColumn createColumn(String columnName, int colSize, String sortPropertyName, CellLabelProvider lp) {
		TreeViewerColumn col = createTreeViewerColumn(pageableTree.getViewer(), 0, columnName, colSize);
		col.setLabelProvider(lp);
		if (sortPropertyName != null)
			col.getColumn().addSelectionListener(new SortTableColumnSelectionListener(sortPropertyName));
		return col;
	}
	
	private static TreeViewerColumn createTreeViewerColumn(TreeViewer viewer, int style, String text, int width) {
		TreeViewerColumn tvc = new TreeViewerColumn(viewer, style);
		TreeColumn col = tvc.getColumn();
		col.setText(text);
		col.setWidth(width);
		col.setResizable(true);
		col.setMoveable(true);
		return tvc;
	}
	
	protected TreeViewerColumn createDefaultColumn(String columnName, int colSize, String propertyName, boolean sortable) {
		return createColumn(columnName, colSize, sortable ? propertyName : null, new TableColumnBeanLabelProvider(propertyName));
	}
	
	protected TreeViewerColumn createDefaultColumn(String columnName, int colSize, String labelPropertyName, String sortPropertyName) {
		return createColumn(columnName, colSize, sortPropertyName, new TableColumnBeanLabelProvider(labelPropertyName));
	}

}
