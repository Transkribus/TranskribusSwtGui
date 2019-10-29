package eu.transkribus.swt_gui.edit_decl_manager;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import eu.transkribus.core.model.beans.EdOption;

public class OptionTableLabelProvider implements ITableLabelProvider, ITableColorProvider {
	TableViewer tv;
	Table table;
	
	public OptionTableLabelProvider(TableViewer tv) {
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
		if (element instanceof EdOption) {
			EdOption opt = (EdOption) element;
			
			TableColumn column = table.getColumn(columnIndex);
			String ct = column.getText();

			if (ct.equals(EditDeclManagerDialog.OPT_TEXT_COL)) {
				return opt.getText();
			} else if (ct.equals(EditDeclManagerDialog.OPT_ID_COL)) {
				return ""+opt.getOptionId();
			}
		}

		return "i am error";
	}

}
