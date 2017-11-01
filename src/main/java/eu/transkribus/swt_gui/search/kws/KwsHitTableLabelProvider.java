package eu.transkribus.swt_gui.search.kws;

import java.text.DecimalFormat;
import java.util.Map;

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

import eu.transkribus.core.model.beans.kws.TrpKwsHit;

public class KwsHitTableLabelProvider implements ITableLabelProvider, ITableFontProvider {
	private final static Logger logger = LoggerFactory.getLogger(KwsHitTableLabelProvider.class);
	
	private final static DecimalFormat DF = new DecimalFormat("0.0000");
	
	Table table;
	TableViewer tableViewer;

	private Map<TrpKwsHit, Image> icons;

	public KwsHitTableLabelProvider(TableViewer tableViewer, Map<TrpKwsHit, Image> icons) {
		this.tableViewer = tableViewer;
		this.table = tableViewer.getTable();
		this.icons = icons;
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
		if (element instanceof TrpKwsHit) {
			TrpKwsHit hit = (TrpKwsHit) element;
			
			TableColumn column = table.getColumn(columnIndex);
			String ct = column.getText();
	
			if (ct.equals(KwsHitTableWidget.KWS_PREVIEW_COL) && icons.containsKey(hit)) {
				return icons.get(hit);
			}
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		//logger.trace("get column text: "+element+" id: "+columnIndex);
		
		if (element instanceof TrpKwsHit) {
			TrpKwsHit hit = (TrpKwsHit) element;
			
			TableColumn column = table.getColumn(columnIndex);
			String ct = column.getText();
			
			if (ct.equals(KwsHitTableWidget.KWS_CONF_COL)) {
				return DF.format(hit.getConfidence());
			} else if (ct.equals(KwsHitTableWidget.KWS_PAGE_COL)) {
				return ""+hit.getPageNr();
			} else if (ct.equals(KwsHitTableWidget.KWS_TEXT_COL)) {
				return hit.getTranscription();
			} else if (ct.equals(KwsHitTableWidget.KWS_PREVIEW_COL)) {
				if(!icons.containsKey(hit)) {
					return "Loading...";
				} else {
					return "";
				}
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
