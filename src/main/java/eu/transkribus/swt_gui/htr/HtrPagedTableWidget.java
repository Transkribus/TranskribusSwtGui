package eu.transkribus.swt_gui.htr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.ServerErrorException;
import javax.xml.bind.JAXBException;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.widgets.pagination.IPageLoader;
import org.eclipse.nebula.widgets.pagination.collections.PageResult;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.job.JobError;
import eu.transkribus.core.model.beans.rest.JobErrorList;
import eu.transkribus.core.model.beans.rest.TrpHtrList;
import eu.transkribus.core.util.HtrCITlabUtils;
import eu.transkribus.core.util.HtrPyLaiaUtils;
import eu.transkribus.core.util.JaxbUtils;
import eu.transkribus.swt.pagination_table.ATableWidgetPagination;
import eu.transkribus.swt.pagination_table.IPageLoadMethod;
import eu.transkribus.swt.pagination_table.IPageLoadMethods;
import eu.transkribus.swt.pagination_table.RemotePageLoader;
import eu.transkribus.swt.pagination_table.RemotePageLoaderSingleRequest;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class HtrPagedTableWidget extends ATableWidgetPagination<TrpHtr> {
	private static final Logger logger = LoggerFactory.getLogger(HtrTableWidget.class);
	
	public final static String[] providerValues = { HtrCITlabUtils.PROVIDER_CITLAB, HtrCITlabUtils.PROVIDER_CITLAB_PLUS, HtrPyLaiaUtils.PROVIDER_PYLAIA };	
	
//	public class HtrLazyContentProvider implements ILazyContentProvider {
//		private MyTableViewer viewer;
//		private List<TrpHtr> elements=new ArrayList<>();
//		private List<TrpHtr> filteredElements=new ArrayList<>();
//		private ViewerFilter filter;
//
//		public HtrLazyContentProvider(MyTableViewer viewer) {
//			this.viewer = viewer;
//		}
//		
//		public void setFilter(ViewerFilter filter) {
//			this.filter = filter;
//		}
//
//		public void dispose() {
//		}
//
//		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
//			logger.trace("inputChanged: "+CoreUtils.size((List<TrpHtr>) newInput));
//			this.elements = (List<TrpHtr>) newInput;
//			filterElements();
//		}
//		
//		public void filterElements() {
//			if (elements==null) {
//				return;
//			}
//			if (filter!=null) {
//				filteredElements = elements.stream().filter(htr -> filter.select(viewer, null, htr)).collect(Collectors.toList());
//			}
//			else {
//				filteredElements = elements;
//			}
//			viewer.setItemCount(CoreUtils.size(filteredElements));
//		}
//
//		public void updateElement(int index) {
//			logger.debug("updating element: "+index);
//			viewer.replace(filteredElements.get(index), index);
//		}
//	};	
	
	public static final String HTR_NAME_COL = "Name";
	public static final String HTR_LANG_COL = "Language";
	public static final String HTR_CREATOR_COL = "Curator";
	public static final String HTR_TECH_COL = "Technology";
	public static final String HTR_DATE_COL = "Created";
	public static final String HTR_ID_COL = "ID";
	
//	MyTableViewer htrTv;	
//	HtrTableLabelProvider labelProvider;
	
	// filter:
	Composite filterAndReloadComp;
	HtrFilterWithProviderWidget filterComposite;
	Button reloadBtn;

//	private HtrLazyContentProvider lazyContentProvider;
	
	private final String providerFilter;
	
//	public final ColumnConfig[] HTR_COLS = new ColumnConfig[] {
//		new ColumnConfig(HTR_NAME_COL, 220, false, DefaultTableColumnViewerSorter.DESC),
//		new ColumnConfig(HTR_LANG_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
//		new ColumnConfig(HTR_CREATOR_COL, 120, false, DefaultTableColumnViewerSorter.ASC),
//		new ColumnConfig(HTR_TECH_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
//		new ColumnConfig(HTR_DATE_COL, 70, false, DefaultTableColumnViewerSorter.ASC),
//		new ColumnConfig(HTR_ID_COL, 50, true, DefaultTableColumnViewerSorter.ASC),
//	};
	
	private final static boolean USE_LAZY_LOADING = true;
	
	public HtrPagedTableWidget(Composite parent, int style, String providerFilter) {
		super(parent, style, 30);
		
		if(providerFilter != null && !Arrays.stream(providerValues).anyMatch(s -> s.equals(providerFilter))) {
			throw new IllegalArgumentException("Invalid providerFilter value");
		}
		
		this.providerFilter = providerFilter;
//		this.setLayout(new FillLayout());
//		this.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		this.setLayout(new GridLayout(1, false));
//		this.setLayout(new RowLayout(1, true));
		
//		int tableFlags = SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION;
//		if (USE_LAZY_LOADING) {
//			tableFlags |= SWT.VIRTUAL;
//		}
//		htrTv = new MyTableViewer(this, tableFlags);
//		
//		if (USE_LAZY_LOADING) {
//			lazyContentProvider = new HtrLazyContentProvider(htrTv);
//			htrTv.setContentProvider(lazyContentProvider);
//			htrTv.setUseHashlookup(true);
//		}
//		else {
//			htrTv.setContentProvider(new ArrayContentProvider());	
//		}
//		
//		labelProvider = new HtrTableLabelProvider(htrTv);
//		htrTv.setLabelProvider(labelProvider);
//		htrTv.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
//				
//		Table table = htrTv.getTable();
//		table.setHeaderVisible(true);
////		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		
//		htrTv.addColumns(HTR_COLS);
				
//		htrTv.getTable().setSortDirection(SWT.UP);
//		htrTv.getTable().setSortColumn(htrTv.getColumn(0));
//		htrTv.refresh();
		
		addFilter();
	}
	
	@Override
	public void addListener(int eventType, Listener listener) {
		super.addListener(eventType, listener);
		filterComposite.addListener(eventType, listener);
	}
	
	private void addFilter() {
		filterAndReloadComp = new Composite(this, SWT.NONE);
		filterAndReloadComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filterAndReloadComp.setLayout(new GridLayout(2, false));
		filterAndReloadComp.moveAbove(tv.getTable());
		
//		if(USE_LAZY_LOADING) {
//			filterComposite = new HtrFilterWithProviderWidget(filterAndReloadComp, getTableViewer(), providerFilter, SWT.NONE) {
//				@Override
//				protected void refreshViewer() {
//					lazyContentProvider.filterElements();
//					super.refreshViewer();
//				}
//				
//				@Override
//				protected void attachFilter() {
//					//set the viewerFilter on the contentProvider instead of the viewer.
//					lazyContentProvider.setFilter(viewerFilter);
//				}
//			};
//		} else {
//			filterComposite = new HtrFilterWithProviderWidget(filterAndReloadComp, getTableViewer(), providerFilter, SWT.NONE);
//		}
//		filterComposite = new HtrFilterWithProviderWidget(filterAndReloadComp, getTableViewer(), providerFilter, SWT.NONE);
		
		filterComposite = new HtrFilterWithProviderWidget(filterAndReloadComp, getTableViewer(), providerFilter, SWT.NONE) {
			@Override
			protected void refreshViewer() {
				logger.debug("refreshing viewer...");
				refreshPage(true);
				
//				refreshPage(resetToFirstPage);
//				lazyContentProvider.filterElements();
//				super.refreshViewer();
			}
			
			@Override
			protected void attachFilter() {
				//set the viewerFilter on the contentProvider instead of the viewer.
//				lazyContentProvider.setFilter(viewerFilter);
			}
		};	
		
		
		this.reloadBtn = new Button(filterAndReloadComp, SWT.PUSH);
		reloadBtn.setToolTipText("Reload current page");
		reloadBtn.setImage(Images.getOrLoad("/icons/refresh.gif"));
		reloadBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true));
		filterAndReloadComp.moveAbove(getTableViewer().getTable());
	}
	
	private List<TrpHtr> filterHtrList(List<TrpHtr> htrs) {
		if (filter!=null && htrs!=null) {
			return htrs.stream().filter(htr -> filterComposite.getViewerFilter().select(getTableViewer(), null, htr)).collect(Collectors.toList());
		}
		else {
			return htrs;
		}
	}

//	public MyTableViewer getTableViewer() {
//		return htrTv;
//	}
	
	void resetProviderFilter() {
		filterComposite.resetProviderFilter();
	}

	public Button getReloadButton() {
		return reloadBtn;
	}
	
	public String getProviderComboValue() {
		Combo providerCombo = filterComposite.getProviderCombo();
		return (String) providerCombo.getData(providerCombo.getText());
	}
	
	public TrpHtr getSelectedHtr() {
		return getFirstSelected();
//		IStructuredSelection sel = (IStructuredSelection) htrTv.getSelection();
//		if (sel.getFirstElement() != null && sel.getFirstElement() instanceof TrpHtr) {
//			return (TrpHtr) sel.getFirstElement();
//		} else
//			return null;

	}

	public void refreshList(List<TrpHtr> htrs) {
		logger.debug("refreshList");
		refreshPage(true);
		
//		logger.debug("setting documents: "+(htrs==null ? "null" : htrs.size()));
//		htrTv.setInput(htrs==null ? new ArrayList<>() : htrs);
	}

	public void setSelection(int htrId) {
		
		
//		selectElement(el);
		
		// TODO
//		List<TrpHtr> htrs = (List<TrpHtr>)htrTv.getInput();
//		TrpHtr htr = null;
//		for(int i = 0; i < htrs.size(); i++){
//			final TrpHtr curr = htrs.get(i);
//			if(curr.getHtrId() == htrId){
//				logger.trace("Found htrId {}", htrId);
//				htr = curr;
//				break;
//			}
//		}
//		logger.trace("Selecting HTR in table viewer: {}", htr);
//		if(htr != null) { //if model has been removed from this collection it is not in the list.
//			htrTv.setSelection(new StructuredSelection(htr), true);
//		} else {
//			htrTv.setSelection(null);
//		}
	}

	@Override
	protected void setPageLoader() {
		IPageLoadMethod<TrpHtrList, TrpHtr> plm = new IPageLoadMethod<TrpHtrList, TrpHtr>() {
			Storage store = Storage.getInstance();
			TrpHtrList l;
			
			private void load(int fromIndex, int toIndex, String sortPropertyName, String sortDirection) {
				if (store.isLoggedIn()) {
					try {
						l = store.getConnection().getHtrsSync(store.getCollId(), providerFilter, fromIndex, toIndex-fromIndex, sortPropertyName, sortDirection);
					} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
						TrpMainWidget.getInstance().onError("Error loading HTRs", e.getMessage(), e);
					}
				}
				else {
					l = new TrpHtrList(new ArrayList<>(), 0, 0, 0, null, null);
				}
			}			
			
			private void filter() {
				logger.debug("in filter function");
				if (filterComposite!=null && l!=null && l.getList()!=null) {
					logger.debug("filtering htrs..., N-before = "+l.getList().size());
					l.getList().removeIf(htr -> !filterComposite.getViewerFilter().select(getTableViewer(), null, htr));
					l.setTotal(l.getList().size());
					logger.debug("filtering htrs..., N-after = "+l.getList().size());
//					return l.getList().stream().filter(htr -> filterComposite.getViewerFilter().select(getTableViewer(), null, htr)).collect(Collectors.toList());
				}
//				else {
//					return htrs;
//				}				
//				l.setList(filterHtrList(l.getList()));
			}
			
			@Override
			public TrpHtrList loadPage(int fromIndex, int toIndex, String sortPropertyName,
					String sortDirection) {
				
				load(fromIndex, toIndex, sortPropertyName, sortDirection);
				filter();
				
				return l;
				
//				if (store != null && store.isLoggedIn()) {
//					try {
//						// sw.start();
//						logger.debug("loading job errors from server...");
//						errors = store.getConnection().getJobErrors(""+getJobId(), fromIndex, toIndex - fromIndex, sortPropertyName, sortDirection);
//					} catch (SessionExpiredException | TrpServerErrorException | TrpClientErrorException | IllegalArgumentException e) {
//						TrpMainWidget.getInstance().onError("Error loading job errors", e.getMessage(), e);
//					}
//				}
//				try {
//					logger.debug(JaxbUtils.marshalToString(errors, JobError.class));
//					for(JobError e : errors.getList()) {
//						logger.debug(e.getClass().getCanonicalName() + " -> "+e);
//					}
//				} catch (JAXBException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				logger.debug("returning error list object of size = " + errors.getList().size());
//				return errors;
			}
		};
		final IPageLoader<PageResult<TrpHtr>> pl = new RemotePageLoaderSingleRequest<>(pageableTable.getController(), plm);
		pageableTable.setPageLoader(pl);		
		
//		if (methods == null) {
//			methods = new IPageLoadMethods<TrpHtr>() {
//				Storage store = Storage.getInstance();
//				
//				List<TrpHtr> htrList=new ArrayList<>();
//				int N=0;
//				
//				private void loadHtrs(int fromIndex, int toIndex, String sortPropertyName, String sortDirection) {
//					if (store.isLoggedIn()) {
//						try {
//							TrpHtrList l = store.getConnection().getHtrsSync(store.getCollId(), providerFilter, fromIndex, toIndex-fromIndex, sortPropertyName, sortDirection);
//							N = l.getTotal();
//							htrList = l.getList();
//							logger.debug("N = "+N);
//						} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
//							TrpMainWidget.getInstance().onError("Error loading HTRs", e.getMessage(), e);
//						}
//					}
//					else {
//						htrList = new ArrayList<>();
//					}
//				}
//				
//				@Override public int loadTotalSize() {
//					loadHtrs(0, 1, null, null);
//					
//					int N = 0;
//					if (store.isLoggedIn()) {
//						try {
//							TrpHtrList l = store.getConnection().getHtrsSync(store.getCollId(), providerFilter, 0, 1, null, null);
//							N = l.getTotal();
//							logger.debug("N = "+N);
//						} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
//							TrpMainWidget.getInstance().onError("Error loading HTRs", e.getMessage(), e);
//						}
//					}
//					return N;
//				}
//	
//				@Override public List<TrpHtr> loadPage(int fromIndex, int toIndex, String sortPropertyName, String sortDirection) {			
//					List<TrpHtr> list = new ArrayList<>();
//					if (store.isLoggedIn()) {
//						try {
//							TrpHtrList l = store.getConnection().getHtrsSync(store.getCollId(), providerFilter, fromIndex, toIndex-fromIndex, sortPropertyName, sortDirection);
//							list = l.getList();
//							logger.debug("loadPage, size of htr-list = "+list.size());
//							if (list.size() > 0) {
//								logger.debug("first: "+list.get(0)+" second: "+list.get(1));
//							}
//						} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
//							TrpMainWidget.getInstance().onError("Error loading jobs", e.getMessage(), e);
//						}
//					}
//					return list;
//				}
//			};
//		}
//			
//		RemotePageLoader<TrpHtr> pl = new RemotePageLoader<>(pageableTable.getController(), methods);
//		pageableTable.setPageLoader(pl);
	}

	@Override
	protected void createColumns() {
		
		
		
//		new ColumnConfig(HTR_NAME_COL, 220, false, DefaultTableColumnViewerSorter.DESC),
//		new ColumnConfig(HTR_LANG_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
//		new ColumnConfig(HTR_CREATOR_COL, 120, false, DefaultTableColumnViewerSorter.ASC),
//		new ColumnConfig(HTR_TECH_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
//		new ColumnConfig(HTR_DATE_COL, 70, false, DefaultTableColumnViewerSorter.ASC),
//		new ColumnConfig(HTR_ID_COL, 50, true, DefaultTableColumnViewerSorter.ASC),
		
//		public static final String HTR_NAME_COL = "Name";
//		public static final String HTR_LANG_COL = "Language";
//		public static final String HTR_CREATOR_COL = "Curator";
//		public static final String HTR_TECH_COL = "Technology";
//		public static final String HTR_DATE_COL = "Created";
//		public static final String HTR_ID_COL = "ID";
		
		HtrTableLabelProvider lp = new HtrTableLabelProvider(tv);
		createDefaultColumn(HTR_NAME_COL, 220, "name", true);
		createDefaultColumn(HTR_LANG_COL, 100, "language", true);
		
		createColumn(HTR_CREATOR_COL, 120, "userName", new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				if (cell.getElement() instanceof TrpHtr) {
					cell.setText(lp.getColumnText((TrpHtr)cell.getElement(), HTR_CREATOR_COL));	
				}
			}
		});
		createColumn(HTR_TECH_COL, 100, "provider", new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				if (cell.getElement() instanceof TrpHtr) {
					cell.setText(lp.getColumnText((TrpHtr)cell.getElement(), HTR_TECH_COL));	
				}
			}
		});
		createColumn(HTR_DATE_COL, 70, "created", new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				if (cell.getElement() instanceof TrpHtr) {
					cell.setText(lp.getColumnText((TrpHtr)cell.getElement(), HTR_DATE_COL));
				}
			}
		});			
		createDefaultColumn(HTR_ID_COL, 50, "htrId", true);
	}	
}