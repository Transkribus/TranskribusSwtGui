package eu.transkribus.swt_gui.htr;

import org.apache.commons.lang3.StringUtils;
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
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.HtrCITlabUtils;

public class HtrTableLabelProvider implements ITableLabelProvider, ITableFontProvider {
	private final static Logger logger = LoggerFactory.getLogger(HtrTableLabelProvider.class);

	private final static String NOT_AVAILABLE_LABEL = "N/A";

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
		if (element instanceof TrpHtr) {
			TrpHtr htr = (TrpHtr) element;

			TableColumn column = table.getColumn(columnIndex);
			String ct = column.getText();
			switch (ct) {
			case HtrTableWidget.HTR_NAME_COL:
				return htr.getName();
			case HtrTableWidget.HTR_LANG_COL:
				return htr.getLanguage();
			case HtrTableWidget.HTR_ID_COL:
				return "" + htr.getHtrId();
			case HtrTableWidget.HTR_CREATOR_COL:
				return htr.getUserName() == null ? "Unknown" : htr.getUserName();
			case HtrTableWidget.HTR_TECH_COL:
				return getLabelForHtrProvider(htr.getProvider());
			case HtrTableWidget.HTR_DATE_COL:
				return CoreUtils.DATE_FORMAT_USER_FRIENDLY.format(htr.getCreated());
			default:
				return NOT_AVAILABLE_LABEL;
			}
		} else {
			return NOT_AVAILABLE_LABEL;
		}
	}

	public static String getLabelForHtrProvider(String provider) {
		if (StringUtils.isEmpty(provider)) {
			return NOT_AVAILABLE_LABEL;
		}
		switch (provider) {
		case HtrCITlabUtils.PROVIDER_CITLAB:
			return "CITlab HTR";
		case HtrCITlabUtils.PROVIDER_CITLAB_PLUS:
			return "CITlab HTR++";
		default:
			return NOT_AVAILABLE_LABEL;
		}
	}

	@Override
	public Font getFont(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}
}
