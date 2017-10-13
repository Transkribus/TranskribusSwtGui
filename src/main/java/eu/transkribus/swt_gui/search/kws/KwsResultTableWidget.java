package eu.transkribus.swt_gui.search.kws;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.kws.TrpKwsResult;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;

public class KwsResultTableWidget extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(KwsResultTableWidget.class);
	
	public static final String KWS_CREATED_COL = "Created";
	public static final String KWS_STATUS_COL = "Status";
	public static final String KWS_SCOPE_COL = "Scope";
	public static final String KWS_DURATION_COL = "Duration";
	public static final String KWS_QUERY_COL = "ID";
	
	MyTableViewer kwsResTv;
	int selectedId=-1;
	
	// filter:
//	Text filter;
//	boolean withFilter;
	
	public final ColumnConfig[] KWS_RES_COLS = new ColumnConfig[] {
		new ColumnConfig(KWS_CREATED_COL, 150, true, DefaultTableColumnViewerSorter.DESC),
		new ColumnConfig(KWS_STATUS_COL, 100, false, DefaultTableColumnViewerSorter.DESC),
		new ColumnConfig(KWS_QUERY_COL, 250, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(KWS_DURATION_COL, 125, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(KWS_SCOPE_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		
	};
	
	public KwsResultTableWidget(Composite parent, int style) {
		super(parent, style);

		this.setLayout(new GridLayout(1, false));
				
		kwsResTv = new MyTableViewer(this, SWT.H_SCROLL | SWT.V_SCROLL);
		kwsResTv.setContentProvider(new ArrayContentProvider());
		kwsResTv.setLabelProvider(new KwsResultTableLabelProvider(kwsResTv));
		kwsResTv.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
				
		Table table = kwsResTv.getTable();
		table.setHeaderVisible(true);
		
		kwsResTv.addColumns(KWS_RES_COLS);
		
//		addFilter();
	}
	
//	private void addFilter() {
//		filter = new Text(this, SWT.BORDER | SWT.SINGLE);
//		filter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		filter.addKeyListener(new KeyAdapter() {			
//			@Override
//			public void keyPressed(KeyEvent e) {
//				if (!isDisposed() && e.keyCode == SWT.KEYPAD_CR || e.keyCode == SWT.CR) {
//					kwsResTv.refresh();
//				}
//			}
//		});
//		filter.moveAbove(kwsResTv.getTable());
//		
//		ViewerFilter viewerFilter = new ViewerFilter() {
//			@Override public boolean select(Viewer viewer, Object parentElement, Object element) {
//				if (SWTUtil.isDisposed(filter)) {
//					return true;
//				}
//				String ft = filter.getText();
//				if (StringUtils.isEmpty(ft))
//					return true;
//				
//				ft = Pattern.quote(ft);
//				
//				String reg = "(?i)(.*"+ft+".*)";
//				final String[] filterProperties = { "htrId", "name", "language" };
//				for (String property : filterProperties) {
//					try {
//						String propValue = BeanUtils.getSimpleProperty(element, property);
//						if (propValue.matches(reg)) {
//							return true;
//						}
//					} catch (Exception e) {
//						logger.error("Error getting filter property '"+property+"': "+e.getMessage());
//					}
//				}
//
//				return false;
//			}
//		};
//		
//		ModifyListener filterModifyListener = new ModifyListener() {
//			DelayedTask dt = new DelayedTask(() -> {
//				if (isDisposed())
//					return;
//				
//				kwsResTv.refresh();
//			}, true);
//			
//			@Override public void modifyText(ModifyEvent e) {
//				dt.start();
//			}
//		};
//		
//		filter.addModifyListener(filterModifyListener);
//		kwsResTv.addFilter(viewerFilter);
//	}

	public MyTableViewer getTableViewer() {
		return kwsResTv;
	}

	public TrpKwsResultTableEntry getSelectedKws() {
		IStructuredSelection sel = (IStructuredSelection) kwsResTv.getSelection();
		if (sel.getFirstElement() != null && sel.getFirstElement() instanceof TrpKwsResultTableEntry) {
			return (TrpKwsResultTableEntry) sel.getFirstElement();
		} else
			return null;

	}
//	public void setSelection(int htrId) {
//		List<TrpHtr> htrs = (List<TrpHtr>)kwsResTv.getInput();
//		
//		TrpHtr htr = null;
//		for(int i = 0; i < htrs.size(); i++){
//			if(htrs.get(i).getHtrId() == htrId){
//				htr = (TrpHtr)kwsResTv.getElementAt(i);
//				break;
//			}
//		}
//		kwsResTv.setSelection(new StructuredSelection(htr), true);
//	}	
}