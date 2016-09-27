package eu.transkribus.swt.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class DefaultTableColumnViewerSorter extends TableViewerSorter {
	private final static Logger logger = LoggerFactory.getLogger(DefaultTableColumnViewerSorter.class);
	
	public DefaultTableColumnViewerSorter(TableViewer viewer, TableColumn column) {
		super(viewer, column);
	}
	
	int getRowIndex(Viewer viewer, Object e) {
		if (e==null)
			return -1;
		
		for (int i=0; i<((TableViewer)viewer).getTable().getItems().length; ++i) {
			if (((TableViewer)viewer).getTable().getItem(i).getData() == e) {
				return i;
			}
		}
		return -1;
	}
	
	
	@Override protected int doCompare(Viewer viewer, Object e1, Object e2) {
		logger.trace("e1 = "+e1+" e2 = "+e2);
		
		String l1 = null, l2 = null;
		if (false) {
			ITableLabelProvider labelProvider = (ITableLabelProvider) ((TableViewer)viewer).getLabelProvider();
			l1 = labelProvider.getColumnText(e1, columnIndex);
			l2 = labelProvider.getColumnText(e2, columnIndex);
		} else {
			Table t = ((TableViewer)viewer).getTable();
			int r1 = getRowIndex(viewer, e1);
			int r2 = getRowIndex(viewer, e2);
			
			if (r1 != -1)
				l1 = t.getItem(r1).getText(columnIndex);
			if (r2 != -1)
				l2 = t.getItem(r2).getText(columnIndex);
		}
		
		if (l1 == null && l2 == null)
			return 0;
		else if (l1 == null && l2 != null)
			return -1;
		else if (l1 != null && l2 == null)
			return 1;

		try {
			int i1 = Integer.parseInt(l1);
			int i2 = Integer.parseInt(l2);
			return Integer.compare(i1, i2);
		} catch (NumberFormatException e) {
			return l1.compareTo(l2);	
		}
	}
};
