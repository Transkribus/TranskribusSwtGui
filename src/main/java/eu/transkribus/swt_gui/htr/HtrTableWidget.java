package eu.transkribus.swt_gui.htr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.util.HtrCITlabUtils;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.util.DelayedTask;

public class HtrTableWidget extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(HtrTableWidget.class);
	
	public final static String[] providerValues = { HtrCITlabUtils.PROVIDER_CITLAB, HtrCITlabUtils.PROVIDER_CITLAB_PLUS };		
	
	public static final String HTR_NAME_COL = "Name";
	public static final String HTR_LANG_COL = "Language";
	public static final String HTR_CREATOR_COL = "Curator";
	public static final String HTR_TECH_COL = "Technology";
	public static final String HTR_DATE_COL = "Created";
	public static final String HTR_ID_COL = "ID";
	
	private MyTableViewer htrTv;	
	private HtrTableLabelProvider labelProvider;
	
	// filter:
	Composite filterComposite;
	private Text filter;
	private Combo providerCombo;
	
	private final String providerFilter;
	
	public final ColumnConfig[] HTR_COLS = new ColumnConfig[] {
		new ColumnConfig(HTR_NAME_COL, 220, false, DefaultTableColumnViewerSorter.DESC),
		new ColumnConfig(HTR_LANG_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(HTR_CREATOR_COL, 120, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(HTR_TECH_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(HTR_DATE_COL, 70, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(HTR_ID_COL, 50, true, DefaultTableColumnViewerSorter.ASC),
	};
	
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
				
		htrTv = new MyTableViewer(this, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		htrTv.setContentProvider(new ArrayContentProvider());
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
		filterComposite = new Composite(this, SWT.NONE);
		filterComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filterComposite.setLayout(new GridLayout(4, false));
		
		Label filterLabel = new Label(filterComposite, SWT.NONE);
		filterLabel.setText("Search:");
		filterLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		filter = new Text(filterComposite, SWT.BORDER | SWT.SINGLE);
		filter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filter.addKeyListener(new KeyAdapter() {			
			@Override
			public void keyPressed(KeyEvent e) {
				if (!isDisposed() && e.keyCode == SWT.KEYPAD_CR || e.keyCode == SWT.CR) {
					htrTv.refresh();
				}
			}
		});
		Label providerLabel = new Label(filterComposite, SWT.NONE);
		providerLabel.setText("Technology:");
		providerLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		providerCombo = new Combo(filterComposite, SWT.READ_ONLY);
		
		if(providerFilter == null) {
			addProviderFilter(providerCombo, "Show all", null);
			for (String p : providerValues) {
				addProviderFilter(providerCombo, labelProvider.getLabelForHtrProvider(p), p);
			}
		} else {
			addProviderFilter(providerCombo, labelProvider.getLabelForHtrProvider(providerFilter), providerFilter);
			//lock the combo as no choice is allowed
			providerCombo.setEnabled(false);
		}
		
		providerCombo.select(0);
		providerCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		
		filterComposite.moveAbove(htrTv.getTable());
		
		//FIXME the filter should be replaced by a server API endpoint
		ViewerFilter viewerFilter = new ViewerFilter() {
			@Override public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (SWTUtil.isDisposed(filter)) {
					return true;
				}
				String ft = filter.getText();
				if (StringUtils.isEmpty(ft))
					return true;
				
				ft = Pattern.quote(ft);
				
				String reg = "(?i)(.*"+ft+".*)";
				final String[] filterProperties = { "htrId", "name", "language" };
				for (String property : filterProperties) {
					try {
						String propValue = BeanUtils.getSimpleProperty(element, property);
						if (propValue.matches(reg)) {
							return true;
						}
					} catch (Exception e) {
						logger.error("Error getting filter property '"+property+"': "+e.getMessage());
					}
				}

				return false;
			}
		};
		
		ModifyListener filterModifyListener = new ModifyListener() {
			DelayedTask dt = new DelayedTask(() -> {
				if (isDisposed())
					return;
				
				htrTv.refresh();
			}, true);
			
			@Override public void modifyText(ModifyEvent e) {
				dt.start();
			}
		};
		
		filter.addModifyListener(filterModifyListener);
		htrTv.addFilter(viewerFilter);
	}
	
	private void addProviderFilter(Combo providerCombo, String label, String data) {
		providerCombo.add(label);
		providerCombo.setData(label, data);
	}

	public MyTableViewer getTableViewer() {
		return htrTv;
	}
	
	public Combo getProviderCombo() {
		return providerCombo;
	}
	
	public String getProviderComboValue() {
		return (String)providerCombo.getData(providerCombo.getText());
	}
	
	public Text getFilterText() {
		return filter;
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
			if(htrs.get(i).getHtrId() == htrId){
				htr = (TrpHtr)htrTv.getElementAt(i);
				break;
			}
		}
		if(htr != null) { //if model has been removed from this collection it is not in the list.
			htrTv.setSelection(new StructuredSelection(htr), true);
		}
	}	
}