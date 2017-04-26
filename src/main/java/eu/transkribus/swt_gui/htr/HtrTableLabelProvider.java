package eu.transkribus.swt_gui.htr;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpHtr;

public class HtrTableLabelProvider implements ITableLabelProvider, ITableFontProvider {
	private final static Logger logger = LoggerFactory.getLogger(HtrTableLabelProvider.class);
	
	Table table;
	TableViewer tableViewer;
	

	public HtrTableLabelProvider(TableViewer tableViewer) {
		this.tableViewer = tableViewer;
		this.table = tableViewer.getTable();
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		
		
	}

	@Override
	public void dispose() {
		
		
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		
		
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		//logger.trace("get column text: "+element+" id: "+columnIndex);
		
		if (element instanceof TrpHtr) {
			TrpHtr htr = (TrpHtr) element;
			
			TableColumn column = table.getColumn(columnIndex);
			String ct = column.getText();
			
			if (ct.equals(HtrTableWidget.HTR_NAME_COL)) {
				return htr.getName();
			} else if (ct.equals(HtrTableWidget.HTR_LANG_COL)) {
				return htr.getLanguage();
			} else if (ct.equals(HtrTableWidget.HTR_ID_COL)) {
				return ""+htr.getHtrId();
			}
		}
		return "i am error";
	}

	@Override
	public Font getFont(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}
}
