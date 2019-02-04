package eu.transkribus.swt_gui.collection_manager;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import eu.transkribus.core.model.beans.auth.TrpUser;
import eu.transkribus.swt.util.Colors;

public class FindUsersWidgetLabelProvider implements ITableLabelProvider, ITableColorProvider {
	TableViewer tv;
	Table table;
	
	Color greyedOutTextColor;
	
	public FindUsersWidgetLabelProvider(TableViewer tv) {
		this.tv = tv;
		this.table = tv.getTable();
		this.greyedOutTextColor = Colors.getSystemColor(SWT.COLOR_GRAY);
	}
	
	@Override public String getColumnText(Object element, int columnIndex) {
		if (element instanceof TrpUser) {
			TrpUser user = (TrpUser) element;
			
			TableColumn column = table.getColumn(columnIndex);
			String ct = column.getText();
					
			if (ct.equals(FindUsersWidget.USER_USERNAME_COL)) {
				if(user.getIsActive() > 0) {
					return user.getUserName();
				} else {
					return user.getUserName() + " (activation pending)";
				}
			} else if (ct.equals(FindUsersWidget.USER_FULLNAME_COL)) {
				return user.getFirstname()+" "+user.getLastname();
			}
//			else if (ct.equals(CollectionManagerDialog.USER_ROLE_COL)) {
//				TrpUserCollection uc = user.getUserCollection();
//				TrpRole r = uc == null ? TrpRole.None : (uc.getRole() == null ? TrpRole.None : uc.getRole());
//				return r.toString();
//			}
		}

		return "i am error";
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
		if (element instanceof TrpUser) {
			TrpUser user = (TrpUser) element;					
			if (user.getIsActive() < 1) {
				return greyedOutTextColor;
			}
		}
		return null;
	}

	@Override public Color getBackground(Object element, int columnIndex) {
		return null;
	}

	@Override public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

}
