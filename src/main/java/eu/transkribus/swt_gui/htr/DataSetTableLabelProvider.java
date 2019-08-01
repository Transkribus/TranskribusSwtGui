package eu.transkribus.swt_gui.htr;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthDataSelectionEntry;
import eu.transkribus.swt_gui.htr.treeviewer.IDataSelectionEntry;

public class DataSetTableLabelProvider implements ITableLabelProvider, ITableFontProvider {
	
	Table table;
	TableViewer tableViewer;
	

	public DataSetTableLabelProvider(TableViewer tableViewer) {
		this.tableViewer = tableViewer;
		this.table = tableViewer.getTable();
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		//logger.trace("get column text: "+element+" id: "+columnIndex);
		
		if (element instanceof IDataSelectionEntry) {
			IDataSelectionEntry<?, ?> d = (IDataSelectionEntry<?, ?>) element;
			
			TableColumn column = table.getColumn(columnIndex);
			String ct = column.getText();
			
			if (ct.equals(DataSetTableWidget.ID_COL)) {
				if(element instanceof HtrGroundTruthDataSelectionEntry) {
					return "HTR " + d.getId();
				}
				return ""+d.getId();
			} else if (ct.equals(DataSetTableWidget.TITLE_COL)) {
				return d.getTitle();
			} else if (ct.equals(DataSetTableWidget.PAGES_COL)) {
				return d.getPageString();
			}
		}
		return "i am error";
	}

	@Override
	public Font getFont(Object element, int columnIndex) {
		return null;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {}

	@Override
	public void dispose() {}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}
}
