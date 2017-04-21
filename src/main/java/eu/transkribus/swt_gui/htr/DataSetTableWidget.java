package eu.transkribus.swt_gui.htr;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;

public class DataSetTableWidget extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(DataSetTableWidget.class);
	
	public static final String TITLE_COL = "Title";
	public static final String ID_COL = "ID";
	public static final String PAGES_COL = "Pages";
	
	MyTableViewer tv;
//	int selectedId=-1;
	
	public final ColumnConfig[] COLS = new ColumnConfig[] {
		new ColumnConfig(ID_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(TITLE_COL, 250, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(PAGES_COL, 100, false, DefaultTableColumnViewerSorter.ASC)
	};
	
	public DataSetTableWidget(Composite parent, int style) {
		super(parent, style);
		
		this.setLayout(new FillLayout());
		
		tv = new MyTableViewer(this, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		tv.setContentProvider(new ArrayContentProvider());
		tv.setLabelProvider(new DataSetTableLabelProvider(tv));
		
		Table table = tv.getTable();
		table.setHeaderVisible(true);
//		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		tv.addColumns(COLS);
	}

	public MyTableViewer getTableViewer() {
		return tv;
	}

//	public TrpHtr getSelectedHtr() {
//		IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
//		if (sel.getFirstElement() != null && sel.getFirstElement() instanceof TrpHtr) {
//			return (TrpHtr) sel.getFirstElement();
//		} else
//			return null;
//
//	}

//	public void refreshList(List<TrpHtr> htrs) {
//		logger.debug("setting documents: "+(htrs==null ? "null" : htrs.size()));
//		tv.setInput(htrs==null ? new ArrayList<>() : htrs);
////		this.layout(true);
//	}

//	public void setSelection(int htrId) {
//		List<TrpHtr> htrs = (List<TrpHtr>)tv.getInput();
//		
//		TrpHtr htr = null;
//		for(int i = 0; i < htrs.size(); i++){
//			if(htrs.get(i).getHtrId() == htrId){
//				htr = (TrpHtr)tv.getElementAt(i);
//				break;
//			}
//		}
//		tv.setSelection(new StructuredSelection(htr), true);
//	}	
}