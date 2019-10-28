package eu.transkribus.swt_gui.edit_decl_manager;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import eu.transkribus.core.model.beans.EdFeature;

public class FeatureTableLabelProvider implements ITableLabelProvider, ITableColorProvider {
	TableViewer tv;
	Table table;
	
	public FeatureTableLabelProvider(TableViewer tv) {
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

			if (ct.equals(EditDeclManagerDialog.FEAT_TITLE_COL)) {
				return feat.getTitle();
			} else if (ct.equals(EditDeclManagerDialog.FEAT_DESC_COL)) {
				return feat.getDescription();
			} else if (ct.equals(EditDeclManagerDialog.FEAT_ID_COL)) {
				return ""+feat.getFeatureId();
			}  else if (ct.equals(EditDeclManagerDialog.FEAT_COL_ID_COL)) {
				final Integer colId = feat.getColId();
				if(colId != null){
					return ""+feat.getColId();
				} else {
					return "preset";
				}
			}
		}

		return "i am error";
	}

}
