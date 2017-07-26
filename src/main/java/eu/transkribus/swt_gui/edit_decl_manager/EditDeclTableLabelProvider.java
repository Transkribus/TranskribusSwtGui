package eu.transkribus.swt_gui.edit_decl_manager;

import java.util.List;

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

import eu.transkribus.core.model.beans.EdFeature;
import eu.transkribus.core.model.beans.EdOption;

public class EditDeclTableLabelProvider implements ITableLabelProvider, ITableColorProvider {
	private static final Logger logger = LoggerFactory.getLogger(EditDeclTableLabelProvider.class);
	TableViewer tv;
	Table table;
	
	public EditDeclTableLabelProvider(TableViewer tv) {
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
		if (element instanceof EdFeature) {
			EdFeature feat = (EdFeature) element;
			
			TableColumn column = table.getColumn(columnIndex);
			String ct = column.getText();

			if (ct.equals(EditDeclManagerDialog.EDT_DECL_TITLE_COL)) {
				return feat.getTitle();
			} else if (ct.equals(EditDeclManagerDialog.EDT_DECL_DESC_COL)) {
				return feat.getDescription();
			} else if (ct.equals(EditDeclManagerDialog.EDT_DECL_ID_COL)) {
				return ""+feat.getFeatureId();
			}  else if (ct.equals(EditDeclManagerDialog.EDT_DECL_OPT_COL)) {
				List<EdOption> opts = feat.getOptions();
				for(EdOption o : opts){
//					logger.debug(o.getText());
					if(o.isSelected()) return o.getText();
				}
			}
		}

		return "i am error";
	}

}
