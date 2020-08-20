package eu.transkribus.swt.pagination_table;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.nebula.widgets.pagination.IPageLoaderHandler;
import org.eclipse.nebula.widgets.pagination.MyPageSizeComboRenderer;
import org.eclipse.nebula.widgets.pagination.PageableController;
import org.eclipse.nebula.widgets.pagination.collections.PageResult;
import org.eclipse.nebula.widgets.pagination.collections.PageResultContentProvider;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.util.TreeViewerUtils;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthTableLabelAndFontProvider;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public abstract class ATreeWidgetPagination<T> extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(ATreeWidgetPagination.class);

	static int DEFAULT_INITIAL_PAGE_SIZE = 50;
	
	protected PageableTree pageableTree;
		
	protected TreeViewer tv;
	
	protected IPageLoadMethods<T> methods = null;
	
	protected int initialPageSize = DEFAULT_INITIAL_PAGE_SIZE;
	
	protected LoadingComposite loadingComposite;
	
	protected Text filter;
	protected boolean withFilter;
	
	protected final ITreeContentProvider treeContentProvider;
	protected final CellLabelProvider labelProvider;
		
	
	public ATreeWidgetPagination(Composite parent, int treeStyle, int initialPageSize, ITreeContentProvider contentProvider, CellLabelProvider labelProvider) {
		this(parent, treeStyle, initialPageSize, null, false, contentProvider, labelProvider);
	}

	public ATreeWidgetPagination(Composite parent, int treeStyle, int initialPageSize, IPageLoadMethods<T> methods) {
		this(parent, treeStyle, initialPageSize, methods, false, null, null);
	}
	

	public ATreeWidgetPagination(Composite parent, int treeStyle, int initialPageSize, IPageLoadMethods<T> methods, boolean withFilter, ITreeContentProvider contentProvider, CellLabelProvider labelProvider) {
		super(parent, 0);
		this.setLayout(new GridLayout(1, false));
		
		if(contentProvider != null) {
			this.treeContentProvider = contentProvider;
		} else {
			//default contentProvider shows HTR GT for now
			this.treeContentProvider = new HtrGroundTruthContentProvider(null);
		}
		
		if(labelProvider != null) {
			this.labelProvider = labelProvider;
		} else {
			this.labelProvider = new HtrGroundTruthTableLabelAndFontProvider(this.getFont());
		}

		this.withFilter = withFilter;
		this.initialPageSize = initialPageSize;
		this.methods = methods;
		
		createTable(treeStyle);
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
	
	public PageableTree getPageableTree() { return pageableTree; }
	
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
					PageResult<T> res = (PageResult<T>) pageableTree.getPageLoader().loadPage(c1);
					
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

	void createTable(int style) {
		int treeStyle = SWT.BORDER | SWT.MULTI  | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL;
		
		if ((style & SWT.SINGLE) != 0) {
			treeStyle |= SWT.SINGLE;
		}
				
		
		pageableTree = new PageableTree(this, SWT.BORDER, treeStyle, initialPageSize
				, PageResultContentProvider.getInstance(),
				PagingToolBarNavigationRendererFactory.getFactory(),
				PageableTree.getDefaultPageRendererBottomFactory()
				) {

			@Override
			protected Composite createCompositeBottom(Composite parent) {
				
				Composite bottom = new Composite(parent, 0);
				bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				bottom.setLayout(new GridLayout(withFilter ? 3 : 2, false));
				
				MyPageSizeComboRenderer pageSizeComboDecorator = new MyPageSizeComboRenderer(
						bottom, SWT.NONE, getController(), new Integer[] { 10, 20, 40, 80, 100, 200 }) {
					
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
//					filter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 2));
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
		
		tv = pageableTree.getViewer();
		tv.setLabelProvider(labelProvider);
		tv.setContentProvider(treeContentProvider);
//		tv.setContentProvider(new HtrGroundTruthContentProvider(Storage.getInstance().getCollId()));
//		tv.setLabelProvider(new HtrGroundTruthTableLabelAndFontProvider(tv.getControl().getFont()));
		
		Tree tree = tv.getTree();
		tree.setHeaderVisible(true);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		
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
	
	
	public TreeViewer getTreeViewer() { return tv; }
	
	public void selectElement(T el) {
		if (el != null) {
			getTreeViewer().setSelection(new StructuredSelection(el), true);
		}
	}
	
	public T getFirstSelected() {
		if(tv == null) {
			return null;
		}
		IStructuredSelection sel = (IStructuredSelection) tv.getSelection();		
		return (T) sel.getFirstElement();
	}
	
	public List<T> getSelected() {
		if(tv == null) {
			return new ArrayList<>(0);
		}
		return ((IStructuredSelection) tv.getSelection()).toList();
	}
	
	public IStructuredSelection getSelectedAsIStructuredSelection() {
		return ((IStructuredSelection) tv.getSelection());
	}
	
	protected abstract void createColumns();


}
