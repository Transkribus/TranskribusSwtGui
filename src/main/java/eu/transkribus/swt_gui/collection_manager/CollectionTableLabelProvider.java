package eu.transkribus.swt_gui.collection_manager;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import eu.transkribus.core.model.beans.TrpCollection;

public class CollectionTableLabelProvider implements ITableLabelProvider, ITableColorProvider {
	TableViewer tv;
	Table table;
	
	public CollectionTableLabelProvider(TableViewer tv) {
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
		if (element instanceof TrpCollection) {
			TrpCollection coll = (TrpCollection) element;
			
			TableColumn column = table.getColumn(columnIndex);
			String ct = column.getText();
					
			if (ct.equals(CollectionManagerDialog.COLL_NAME_COL)) {
				return coll.getColName();
			} else if (ct.equals(CollectionManagerDialog.COLL_DESC_COL)) {
				return coll.getDescription();
			} else if (ct.equals(CollectionManagerDialog.COLL_ROLE)) {
				if (coll.getRole() == null)
					return "";
				else
					return coll.getRole().toString();
			} else if (ct.equals(CollectionManagerDialog.COLL_ID_COL)) {
				return ""+coll.getColId();
			}
		}

		return "i am error";
	}

}
