package eu.transkribus.swt_canvas.util;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class TableViewerUtils {

	public static TableColumn createTableColumn(Table table, int style, String text, int width) {
//		TableViewerColumn tvc = new TableViewerColumn(viewer, style);
		TableColumn col = new TableColumn(table, style);
//		TableColumn col = tvc.getColumn();
		col.setText(text);
		col.setWidth(width);
		
		return col;
	}
	
	public static TableViewerColumn createTableViewerColumn(TableViewer viewer, int style, String text, int width) {
		TableViewerColumn tvc = new TableViewerColumn(viewer, style);
//		TableColumn col = new TableColumn(viewer.getTable(), style);
		TableColumn col = tvc.getColumn();
		col.setText(text);
		col.setWidth(width);
		col.setResizable(true);
		col.setMoveable(true);
		
		return tvc;
	}
	
}
