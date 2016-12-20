package eu.transkribus.swt_gui.htr;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.EdFeature;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;

public class HtrTableWidget extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(HtrTableWidget.class);
	
	public static final String HTR_NAME_COL = "Name";
	public static final String HTR_ID_COL = "ID";
	
	MyTableViewer htrTv;
	int selectedId=-1;
	
	public final ColumnConfig[] HTR_COLS = new ColumnConfig[] {
		new ColumnConfig(HTR_NAME_COL, 250, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(HTR_ID_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
	};
	
	public HtrTableWidget(Composite parent, int style) {
		super(parent, style);
		
		this.setLayout(new FillLayout());
//		this.setLayout(new RowLayout(1, true));
		
		htrTv = new MyTableViewer(this, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		htrTv.setContentProvider(new ArrayContentProvider());
		htrTv.setLabelProvider(new HtrTableLabelProvider(htrTv));
		
		Table table = htrTv.getTable();
		table.setHeaderVisible(true);
//		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		htrTv.addColumns(HTR_COLS);
	}
	
	public void setDocuments() {
		
	}

	public MyTableViewer getTableViewer() {
		return htrTv;
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
		htrTv.setSelection(new StructuredSelection(htr), true);
	}	
}