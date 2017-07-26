package eu.transkribus.swt_gui.tools;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.HtrModel;
import eu.transkribus.swt_gui.dialogs.TextRecognitionDialog;

public class HtrModelTableLabelProvider implements ITableLabelProvider, ITableColorProvider {
	private static final Logger logger = LoggerFactory.getLogger(HtrModelTableLabelProvider.class);
	TableViewer tv;
	Table table;
	
	public HtrModelTableLabelProvider(TableViewer tv) {
		this.tv = tv;
		this.table = tv.getTable();
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

	@Override public Color getForeground(Object element, int columnIndex) {
		return null;
	}

	@Override public Color getBackground(Object element, int columnIndex) {
		return null;
	}

	@Override public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override public String getColumnText(Object element, int columnIndex) {
		if (element instanceof HtrModel) {
			HtrModel m = (HtrModel) element;
			
			TableColumn column = table.getColumn(columnIndex);
			String ct = column.getText();

			if (ct.equals(TextRecognitionDialog.ID_COL)) {
				return ""+m.getModelId();
			} else if (ct.equals(TextRecognitionDialog.MODEL_NAME_COL)) {
				return m.getModelName();
			} else if (ct.equals(TextRecognitionDialog.LABEL_COL)) {
				return m.getLabel();
			}  else if (ct.equals(TextRecognitionDialog.LANG_COL)) {
				return m.getLanguage();
			} else if (ct.equals(TextRecognitionDialog.NR_OF_TOKENS_COL)) {
				return ""+m.getNrOfTokens();
			} else if (ct.equals(TextRecognitionDialog.NR_OF_LINES_COL)) {
				return ""+m.getNrOfLines();
			} else if (ct.equals(TextRecognitionDialog.NR_OF_DICT_TOKENS_COL)) {
				return ""+m.getNrOfDictTokens();
			}
		}

		return "i am error";
	}

}
