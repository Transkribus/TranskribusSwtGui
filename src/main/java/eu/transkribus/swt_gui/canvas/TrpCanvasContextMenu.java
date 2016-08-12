package eu.transkribus.swt_gui.canvas;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
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
	public static final String SELECT_TABLE_CELLS_EVENT = "SELECT_TABLE_CELLS_EVENT";
	
	public static final String DELETE_TABLE_EVENT = "DELETE_TABLE_EVENT";
	public static final String DELETE_TABLE_ROW_EVENT = "DELETE_TABLE_COLUMN_CELLS_EVENT";
	public static final String DELETE_TABLE_COLUMN_EVENT = "DELETE_TABLE_COLUMN_CELLS_EVENT";
	public static final String SPLIT_MERGED_CELL_EVENT = "SPLIT_MERGED_CELL_EVENT";
	
	public static final String DELETE_ROW_EVENT = "DELETE_ROW_EVENT";
	public static final String DELETE_COLUMN_EVENT = "DELETE_COLUMN_EVENT";
	
	public static final String REMOVE_INTERMEDIATE_TABLECELL_POINTS_EVENT = "REMOVE_INTERMEDIATE_TABLECELL_POINTS_EVENT";
	
	
	
	Menu tableMenu;
	MenuItem tableMenuItem;
	
	MenuItem selectTableCellsItem;
	MenuItem selectTableRowCellsItem;
	MenuItem selectTableColumnCellsItem;
	MenuItem deleteTableRowItem;
	MenuItem deleteTableColumnItem;
	MenuItem deleteTableItem;
	
	public TrpCanvasContextMenu(SWTCanvas canvas) {
		super(canvas);
	}

	private void createTableItems(ICanvasShape s) {
		SWTUtil.dispose(deleteTableItem);
		
		TrpTableRegionType t = TableUtils.getTable(s, true);
		if (t == null)
			return;
		
		deleteTableItem = createMenuItem("Delete Table", Images.DELETE, DELETE_TABLE_EVENT);
	}
	
	private void createTableCellItems(ICanvasShape s) {
		TrpTableCellType cell = TableUtils.getTableCell(s);
		if (cell == null)
			return;
		
		selectTableCellsItem = createMenuItem("Select cells", null, SELECT_TABLE_CELLS_EVENT);
		selectTableRowCellsItem = createMenuItem("Select row cells", null, SELECT_TABLE_ROW_CELLS_EVENT);
		selectTableColumnCellsItem = createMenuItem("Select columns cells", null, SELECT_TABLE_COLUMN_CELLS_EVENT);
		deleteTableRowItem = createMenuItem("Delete row", Images.DELETE, DELETE_TABLE_ROW_EVENT);
		deleteTableColumnItem = createMenuItem("Delete column", Images.DELETE, DELETE_TABLE_COLUMN_EVENT);
		
		if (cell.isMergedCell())
			createMenuItem("Split merged cell", null, SPLIT_MERGED_CELL_EVENT);
		
		if (s.getNPoints() > 4) // TODO: better check if there are intermediate points -> have to check also if a point is corner point of neighbor!!
			createMenuItem("Remove intermediate points", null, REMOVE_INTERMEDIATE_TABLECELL_POINTS_EVENT);
	}
		
	protected void initItems(ICanvasShape s) {
		super.initItems(s);
		createTableItems(s);
		createTableCellItems(s);		
	}

}
