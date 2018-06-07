package eu.transkribus.swt_gui.tool.error;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpErrorList;

public class ErrorTableLabelProvider implements ITableLabelProvider, ITableFontProvider {
	private final static Logger logger = LoggerFactory.getLogger(ErrorTableLabelProvider.class);
	Table table;
	ErrorTableViewer tableViewer;
	

	public ErrorTableLabelProvider(ErrorTableViewer tableViewer) {
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
		logger.debug("setColumnText: " + element);
		if (element instanceof TrpErrorList) {
			
			TrpErrorList list = (TrpErrorList) element;
			TableColumn column = table.getColumn(columnIndex);
			String ct = column.getText();
			
			
				if (ct.equals(ErrorTableViewer.ERR_PAGE_COL)) {
					return "Page "+list.getPageNumber();
				} else if (ct.equals(tableViewer.ERR_WORD_COL)) {
					return  list.getWerDouble()+" %" ;
				} else if (ct.equals(tableViewer.ERR_CHAR_COL)) {
					return list.getCerDouble()+" %";
				} else if (ct.equals(tableViewer.ACC_WORD_COL)) {
					return list.getwAccDouble()+" %";
				}else if (ct.equals(tableViewer.ACC_CHAR_COL)) {
					return list.getcAccDouble()+" %";
				}else if (ct.equals(tableViewer.BAG_PREC_COL)) {
					return list.getBagTokensPrecDouble()+" %";
				}else if (ct.equals(tableViewer.BAG_REC_COL)) {
					return list.getBagTokensRecDouble()+"";
				}else if (ct.equals(tableViewer.BAG_FMEA_COL)) {
					return list.getBagTokensFDouble()+" %";

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