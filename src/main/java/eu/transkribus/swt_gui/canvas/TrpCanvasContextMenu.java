package eu.transkribus.swt_gui.canvas;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MenuItem;

import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableRegionType;
import eu.transkribus.swt_canvas.canvas.CanvasContextMenu;
import eu.transkribus.swt_canvas.canvas.SWTCanvas;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_canvas.util.Images;
import eu.transkribus.swt_canvas.util.SWTUtil;
import eu.transkribus.swt_gui.table_editor.TableUtils;

public class TrpCanvasContextMenu extends CanvasContextMenu {
	public static final String SELECT_TABLE_ROW_CELLS_EVENT = "SELECT_TABLE_ROW_CELLS_EVENT";
	public static final String SELECT_TABLE_COLUMN_CELLS_EVENT = "SELECT_TABLE_COLUMN_CELLS_EVENT";
	
	public static final String DELETE_TABLE_EVENT = "DELETE_TABLE_EVENT";
	public static final String DELETE_TABLE_ROW_EVENT = "DELETE_TABLE_COLUMN_CELLS_EVENT";
	public static final String DELETE_TABLE_COLUMN_EVENT = "DELETE_TABLE_COLUMN_CELLS_EVENT";
	
	MenuItem selectTableRowCellsItem;
	MenuItem selectTableColumnCellsItem;
	MenuItem deleteTableRowItem;
	MenuItem deleteTableColumnItem;
	MenuItem deleteTableItem;

	public TrpCanvasContextMenu(SWTCanvas canvas) {
		super(canvas);
	}
	
//	@Override protected Object notifyObservers(MenuItem item) {
//		Object evt = super.notifyObservers(item);
//		if (evt != null)
//			return evt;
//				
//		if (item.equals(selectTableRowCellsItem)) {
//			evt = SELECT_TABLE_ROW_CELLS_EVENT;
//		}
//		else if (item.equals(selectTableColumnCellsItem)) {
//			evt = SELECT_TABLE_COLUMN_CELLS_EVENT;
//		}
//		else if (item.equals(deleteTableRowItem)) {
//			evt = DELETE_TABLE_ROW_EVENT;
//		}
//		else if (item.equals(deleteTableColumnItem)) {
//			evt = DELETE_TABLE_COLUMN_EVENT;
//		}
//		
//		if (evt != null) {
//			setChanged();
//			notifyObservers(evt);
//		}
//		
//		return evt;
//	}
	
	private void createTableItems(ICanvasShape s) {
		SWTUtil.dispose(deleteTableItem);
		
		TrpTableRegionType t = TableUtils.getTable(s);
		if (t == null)
			return;
		
		deleteTableItem = new MenuItem(popupMenu, SWT.NONE);
		deleteTableItem.setText("Delete table");
		deleteTableItem.setImage(Images.DELETE);
		deleteTableItem.setData(DELETE_TABLE_EVENT);
		deleteTableItem.addSelectionListener(itemSelListener);	
	}
	
	private void createTableCellItems(ICanvasShape s) {
		SWTUtil.dispose(selectTableRowCellsItem);
		SWTUtil.dispose(selectTableColumnCellsItem);
		SWTUtil.dispose(deleteTableRowItem);
		SWTUtil.dispose(deleteTableColumnItem);
		
		TrpTableCellType cell = TableUtils.getTableCell(s);
		if (cell == null)
			return;
		
		selectTableRowCellsItem = new MenuItem(popupMenu, SWT.NONE);
		selectTableRowCellsItem.setText("Select cells of row "+cell.getPos()[0]);
		selectTableRowCellsItem.setData(SELECT_TABLE_ROW_CELLS_EVENT);
		selectTableRowCellsItem.addSelectionListener(itemSelListener);
		
		selectTableColumnCellsItem = new MenuItem(popupMenu, SWT.NONE);
		selectTableColumnCellsItem.setText("Select cells of column "+cell.getPos()[1]);
		selectTableColumnCellsItem.setData(SELECT_TABLE_COLUMN_CELLS_EVENT);
		selectTableColumnCellsItem.addSelectionListener(itemSelListener);
				
		deleteTableRowItem = new MenuItem(popupMenu, SWT.NONE);
		deleteTableRowItem.setText("Delete row");
		deleteTableRowItem.setImage(Images.DELETE);
		deleteTableRowItem.setData(DELETE_TABLE_ROW_EVENT);
		deleteTableRowItem.addSelectionListener(itemSelListener);
		
		deleteTableColumnItem = new MenuItem(popupMenu, SWT.NONE);
		deleteTableColumnItem.setText("Delete column");
		deleteTableColumnItem.setImage(Images.DELETE);
		deleteTableColumnItem.setData(DELETE_TABLE_COLUMN_EVENT);
		deleteTableColumnItem.addSelectionListener(itemSelListener);		
	}
	
	protected void initItems(ICanvasShape s) {
		super.initItems(s);
		createTableItems(s);
		createTableCellItems(s);		
	}

}
