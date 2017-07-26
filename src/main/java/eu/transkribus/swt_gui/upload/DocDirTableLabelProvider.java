package eu.transkribus.swt_gui.upload;

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

import eu.transkribus.core.model.beans.TrpDocDir;

public class DocDirTableLabelProvider implements ITableLabelProvider, ITableColorProvider {
	private static final Logger logger = LoggerFactory.getLogger(DocDirTableLabelProvider.class);
	TableViewer tv;
	Table table;
	
	public DocDirTableLabelProvider(TableViewer tv) {
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
		if (element instanceof TrpDocDir) {
			TrpDocDir dir = (TrpDocDir) element;
			
			TableColumn column = table.getColumn(columnIndex);
			String ct = column.getText();

			if (ct.equals(UploadDialogUltimate.DIRECTORY_COL)) {
				return dir.getName();
			} else if (ct.equals(UploadDialogUltimate.TITLE_COL)) {
				return dir.getName();
			}
			else if (ct.equals(UploadDialogUltimate.NR_OF_FILES_COL)) {
				return ""+dir.getNrOfFiles();
			} else if (ct.equals(UploadDialogUltimate.CREATE_DATE_COL)) {
				return ""+dir.getCreateDate().toString();
			}
		}

		return "i am error";
	}

}
