package eu.transkribus.swt_gui.canvas;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableRegionType;
import eu.transkribus.swt_canvas.canvas.CanvasContextMenu;
import eu.transkribus.swt_canvas.canvas.CanvasException;
import eu.transkribus.swt_canvas.canvas.SWTCanvas;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_canvas.util.Images;
import eu.transkribus.swt_canvas.util.SWTUtil;
import eu.transkribus.swt_gui.canvas.TrpCanvasShapeEditor.BorderFlags;
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
	
	public static final String BORDER_NONE_EVENT = "BORDER_NONE_EVENT";
	public static final String BORDER_LEFT_EVENT = "BORDER_LEFT_EVENT";
	public static final String BORDER_RIGHT_EVENT = "BORDER_RIGHT_EVENT";
	public static final String BORDER_LEFT_RIGHT_EVENT = "BORDER_LEFT_RIGHT_EVENT";
	public static final String BORDER_BOTTOM_EVENT = "BORDER_BOTTOM_EVENT";
	public static final String BORDER_TOP_EVENT = "BORDER_TOP_EVENT";
	public static final String BORDER_BOTTOM_TOP_EVENT = "BORDER_BOTTOM_TOP_EVENT";
	public static final String BORDER_HORIZONTAL_CLOSED_EVENT = "BORDER_HORIZONTAL_CLOSED_EVENT";
	public static final String BORDER_HORIZONTAL_OPEN_EVENT = "BORDER_HORIZONTAL_OPEN_TOP_EVENT";
	public static final String BORDER_VERTICAL_CLOSED_EVENT = "BORDER_VERTICAL_CLOSED_EVENT";
	public static final String BORDER_VERTICAL_OPEN_EVENT = "BORDER_VERTICAL_OPEN_EVENT";
	public static final String BORDER_ALL_EVENT = "BORDER_ALL_EVENT";
	private static final String BORDER_CLOSED_EVENT = "BORDER_CLOSED_EVENT";
	
	public static boolean isBorderEvent(Object evt) {
		return evt instanceof String && StringUtils.startsWith((String) evt, "BORDER_");
	}
	
	public static BorderFlags getBorderFlags(Object evt) {
		if (!isBorderEvent(evt))
			return null;
		
		BorderFlags bf = new BorderFlags();
		
		if (evt.equals(BORDER_NONE_EVENT))
			return bf;
		else if (evt.equals(BORDER_ALL_EVENT)) {
			bf.setAll(true);
			return bf;
		}
		
		else if (evt.equals(BORDER_LEFT_EVENT)) {
			bf.vertLeft=true;
			return bf;
		}
		else if (evt.equals(BORDER_RIGHT_EVENT)) {
			bf.vertRight=true;
			return bf;
		}
		else if (evt.equals(BORDER_LEFT_RIGHT_EVENT)) {
			bf.vertLeft=true;
			bf.vertRight=true;
			return bf;
		}
		
		else if (evt.equals(BORDER_BOTTOM_EVENT)) {
			bf.horBottom=true;
			return bf;
		}
		else if (evt.equals(BORDER_TOP_EVENT)) {
			bf.horTop=true;
			return bf;
		}
		else if (evt.equals(BORDER_BOTTOM_TOP_EVENT)) {
			bf.horBottom=true;
			bf.horTop=true;
			return bf;
		}
		
		else if (evt.equals(BORDER_HORIZONTAL_CLOSED_EVENT)) {
			bf.horBottom = true;
			bf.horTop = true;
			bf.horInner = true;
			bf.vertLeft = true;
			bf.vertRight = true;
			return bf;
		}
		else if (evt.equals(BORDER_HORIZONTAL_OPEN_EVENT)) {
			bf.horBottom = true;
			bf.horTop = true;
			bf.horInner = true;
			return bf;
		}	
		
		else if (evt.equals(BORDER_VERTICAL_CLOSED_EVENT)) {
			bf.vertLeft = true;
			bf.vertRight = true;
			bf.vertInner = true;
			
			bf.horBottom = true;
			bf.horTop = true;
			return bf;
		}
		else if (evt.equals(BORDER_VERTICAL_OPEN_EVENT)) {
			bf.vertLeft = true;
			bf.vertRight = true;
			bf.vertInner = true;
			return bf;
		}
		else if (evt.equals(BORDER_CLOSED_EVENT)) {
			bf.horBottom = true;
			bf.horTop = true;
			bf.vertLeft = true;
			bf.vertRight = true;
			return bf;
		}
		
		throw new CanvasException("Invalid border type event: "+evt);
	}

//	Menu tableMenu;
//	MenuItem tableMenuItem;
	
	Menu borderMenu;
	MenuItem borderMenuItem;
	
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
		
		borderMenuItem = new MenuItem(popupMenu, SWT.CASCADE);
		borderMenuItem.setText("Border");
		
		borderMenu = new Menu(popupMenu);
		borderMenuItem.setMenu(borderMenu);
		
		createMenuItem("None", Images.BORDER_NONE, BORDER_NONE_EVENT, borderMenu);
		createMenuItem("All", Images.BORDER_ALL, BORDER_ALL_EVENT, borderMenu);
		createMenuItem("Closed", Images.BORDER_CLOSED, BORDER_CLOSED_EVENT, borderMenu);
		
		createMenuItem("Left", Images.BORDER_LEFT, BORDER_LEFT_EVENT, borderMenu);
		createMenuItem("Right", Images.BORDER_RIGHT, BORDER_RIGHT_EVENT, borderMenu);
		createMenuItem("Left / Right", Images.BORDER_LEFT_RIGHT, BORDER_LEFT_RIGHT_EVENT, borderMenu);
		
		createMenuItem("Bottom", Images.BORDER_BOTTOM, BORDER_BOTTOM_EVENT, borderMenu);
		createMenuItem("Top", Images.BORDER_TOP, BORDER_TOP_EVENT, borderMenu);
		createMenuItem("Bottom / Top", Images.BORDER_BOTTOM_TOP, BORDER_BOTTOM_TOP_EVENT, borderMenu);
		
		createMenuItem("Horizontally closed", Images.BORDER_HORIZONTAL_CLOSED, BORDER_HORIZONTAL_CLOSED_EVENT, borderMenu);
		createMenuItem("Horizontally open", Images.BORDER_HORIZONTAL_OPEN, BORDER_HORIZONTAL_OPEN_EVENT, borderMenu);
		
		createMenuItem("Vertically closed", Images.BORDER_VERTICAL_CLOSED, BORDER_VERTICAL_CLOSED_EVENT, borderMenu);
		createMenuItem("Vertically open", Images.BORDER_VERTICAL_OPEN, BORDER_VERTICAL_OPEN_EVENT, borderMenu);
		
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
