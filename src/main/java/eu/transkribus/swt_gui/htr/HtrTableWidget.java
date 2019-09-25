package eu.transkribus.swt_gui.htr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.HtrCITlabUtils;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;

public class HtrTableWidget extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(HtrTableWidget.class);
	
	public final static String[] providerValues = { HtrCITlabUtils.PROVIDER_CITLAB, HtrCITlabUtils.PROVIDER_CITLAB_PLUS };	
	
	public class HtrLazyContentProvider implements ILazyContentProvider {
		private MyTableViewer viewer;
		private List<TrpHtr> elements=new ArrayList<>();
		private List<TrpHtr> filteredElements=new ArrayList<>();
		private ViewerFilter filter;

		public HtrLazyContentProvider(MyTableViewer viewer) {
			this.viewer = viewer;
		}
		
		public void setFilter(ViewerFilter filter) {
			this.filter = filter;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			logger.trace("inputChanged: "+CoreUtils.size((List<TrpHtr>) newInput));
			this.elements = (List<TrpHtr>) newInput;
			filterElements();
		}
		
		public void filterElements() {
			if (elements==null) {
				return;
			}
			if (filter!=null) {
				filteredElements = elements.stream().filter(htr -> filter.select(viewer, null, htr)).collect(Collectors.toList());
			}
			else {
				filteredElements = elements;
			}
			viewer.setItemCount(CoreUtils.size(filteredElements));
		}

		public void updateElement(int index) {
			viewer.replace(filteredElements.get(index), index);
		}
	};	
	
	public static final String HTR_NAME_COL = "Name";
	public static final String HTR_LANG_COL = "Language";
	public static final String HTR_CREATOR_COL = "Curator";
	public static final String HTR_TECH_COL = "Technology";
	public static final String HTR_DATE_COL = "Created";
	public static final String HTR_ID_COL = "ID";
	
	private MyTableViewer htrTv;	
	private HtrTableLabelProvider labelProvider;
	
	// filter:
	HtrFilterWithProviderWidget filterComposite;

	private HtrLazyContentProvider lazyContentProvider;
	
	private final String providerFilter;
	
	public final ColumnConfig[] HTR_COLS = new ColumnConfig[] {
		new ColumnConfig(HTR_NAME_COL, 220, false, DefaultTableColumnViewerSorter.DESC),
		new ColumnConfig(HTR_LANG_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(HTR_CREATOR_COL, 120, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(HTR_TECH_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(HTR_DATE_COL, 70, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(HTR_ID_COL, 50, true, DefaultTableColumnViewerSorter.ASC),
	};
	
	public static boolean USE_LAZY_LOADING = true;
	
	public HtrTableWidget(Composite parent, int style, String providerFilter) {
		super(parent, style);
		
		if(providerFilter != null && !Arrays.stream(providerValues).anyMatch(s -> s.equals(providerFilter))) {
			throw new IllegalArgumentException("Invalid providerFilter value");
		}
		
		this.providerFilter = providerFilter;
//		this.setLayout(new FillLayout());
//		this.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		this.setLayout(new GridLayout(1, false));
//		this.setLayout(new RowLayout(1, true));
		
		int tableFlags = SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION;
		if (USE_LAZY_LOADING) {
			tableFlags |= SWT.VIRTUAL;
		}
		htrTv = new MyTableViewer(this, tableFlags);
		
		if (USE_LAZY_LOADING) {
			lazyContentProvider = new HtrLazyContentProvider(htrTv);
			htrTv.setContentProvider(lazyContentProvider);
			htrTv.setUseHashlookup(true);
		}
		else {
			htrTv.setContentProvider(new ArrayContentProvider());	
		}
		
		labelProvider = new HtrTableLabelProvider(htrTv);
		htrTv.setLabelProvider(labelProvider);
		htrTv.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
				
		Table table = htrTv.getTable();
		table.setHeaderVisible(true);
//		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		htrTv.addColumns(HTR_COLS);
				
//		htrTv.getTable().setSortDirection(SWT.UP);
//		htrTv.getTable().setSortColumn(htrTv.getColumn(0));
//		htrTv.refresh();
		
		addFilter();
	}
	
	private void addFilter() {
		if(USE_LAZY_LOADING) {
			filterComposite = new HtrFilterWithProviderWidget(this, htrTv, providerFilter, SWT.NONE) {
				@Override
				protected void refreshViewer() {
					lazyContentProvider.filterElements();
					super.refreshViewer();
				}
				
				@Override
				protected void attachFilter() {
					//set the viewerFilter on the contentProvider instead of the viewer.
					lazyContentProvider.setFilter(viewerFilter);
				}
			};
		} else {
			filterComposite = new HtrFilterWithProviderWidget(this, htrTv, providerFilter, SWT.NONE);
		}
		filterComposite.moveAbove(htrTv.getTable());
	}

	public MyTableViewer getTableViewer() {
		return htrTv;
	}
	
	void resetProviderFilter() {
		filterComposite.resetProviderFilter();
	}
	
	public Combo getProviderCombo() {
		return filterComposite.getProviderCombo();
	}
	
	public String getProviderComboValue() {
		return (String)getProviderCombo().getData(getProviderCombo().getText());
	}

	public TrpHtr getSelectedHtr() {
		IStructuredSelection sel = (IStructuredSelection) htrTv.getSelection();
		if (sel.getFirstElement() != null && sel.getFirstElement() instanceof TrpHtr) {
			return (TrpHtr) sel.getFirstElement();
		} else
			return null;

	}

	public void refreshList(List<TrpHtr> htrs) {
		logger.debug("setting documents: "+(htrs==null ? "null" : htrs.size()));
		htrTv.setInput(htrs==null ? new ArrayList<>() : htrs);
//		this.layout(true);
	}

	public void setSelection(int htrId) {
		List<TrpHtr> htrs = (List<TrpHtr>)htrTv.getInput();
		TrpHtr htr = null;
		for(int i = 0; i < htrs.size(); i++){
			final TrpHtr curr = htrs.get(i);
			if(curr.getHtrId() == htrId){
				logger.trace("Found htrId {}", htrId);
				htr = curr;
				break;
			}
		}
		logger.trace("Selecting HTR in table viewer: {}", htr);
		if(htr != null) { //if model has been removed from this collection it is not in the list.
			htrTv.setSelection(new StructuredSelection(htr), true);
		}
	}	
}