package eu.transkribus.swt_gui.versions;

import org.dea.swt.util.Fonts;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpTranscriptMetadata;

public class VersionsWidgetLabelProvider implements ITableLabelProvider, ITableFontProvider {
	private final static Logger logger = LoggerFactory.getLogger(VersionsWidgetLabelProvider.class);
	
	VersionsWidget vw;
	Table table;
	Font boldFont;
	
	public VersionsWidgetLabelProvider(VersionsWidget vw) {
		this.vw = vw;
		this.table = vw.tableViewer.getTable();
		this.boldFont = Fonts.createBoldFont(vw.tableViewer.getControl().getFont());
	}

	@Override public void addListener(ILabelProviderListener listener) {
	}

	@Override public void dispose() {
	}

	@Override public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override public void removeListener(ILabelProviderListener listener) {
	}

	@Override public Font getFont(Object element, int columnIndex) {
		if (vw.selected!=null && element instanceof TrpTranscriptMetadata) {
			TrpTranscriptMetadata md = (TrpTranscriptMetadata) element;
			if (md.getTimestamp() == vw.selected.getTimestamp())
				return boldFont;
		}
		
		return null;
	}

	@Override public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override public String getColumnText(Object element, int columnIndex) {
		if (element instanceof TrpTranscriptMetadata) {
			TrpTranscriptMetadata md = (TrpTranscriptMetadata) element;
			
			String ct = table.getColumn(columnIndex).getText();					
			if (ct.equals(VersionsWidget.DATE_COL)) {
				return md.getTime().toString();
			} else if (ct.equals(VersionsWidget.STATUS_COL)) {
//				logger.debug("status (vw): "+md.getStatus());
				return md.getStatus() != null ? md.getStatus().toString() : "";
			} else if (ct.equals(VersionsWidget.USER_ID_COL)) {				
				return md.getUserName();
			} else if (ct.equals(VersionsWidget.TOOLNAME_COL)) {				
				return md.getToolName();
			}
		}

		return "i am error";
	}

}
