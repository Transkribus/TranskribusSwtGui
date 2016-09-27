package eu.transkribus.swt_gui.canvas;

import java.util.Observable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableRegionType;
import eu.transkribus.swt_canvas.util.Images;
import eu.transkribus.swt_canvas.util.SWTUtil;
import eu.transkribus.swt_gui.canvas.editing.CanvasShapeEditor.BorderFlags;
import eu.transkribus.swt_gui.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.table_editor.TableUtils;

public class CanvasContextMenu extends Observable {
	private final static Logger logger = LoggerFactory.getLogger(CanvasContextMenu.class);
	
	public static final String DELETE_ITEM_EVENT = "DELETE_EVENT";

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

	protected SWTCanvas canvas;
	protected Menu menu;
	protected MenuItem deleteItem;
	protected SelectionListener itemSelListener;

	Menu borderMenu;

	MenuItem borderMenuItem;

	MenuItem selectTableCellsItem;

	MenuItem selectTableRowCellsItem;

	MenuItem selectTableColumnCellsItem;

	MenuItem deleteTableRowItem;
	
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

	MenuItem deleteTableColumnItem;

	MenuItem deleteTableItem;

	public CanvasContextMenu(SWTCanvas canvas) {
		this.canvas = canvas;
		
		init();
	}
	
	private void init() {	    
	    itemSelListener = new SelectionListener() {
			@Override public void widgetSelected(SelectionEvent e) {
				if (e.getSource() instanceof MenuItem) {
					MenuItem mi = (MenuItem) e.getSource();
					setChanged();
					notifyObservers(mi.getData());
				}
			}
			
			@Override public void widgetDefaultSelected(SelectionEvent e) {
			}
		};
	}
	
//	protected Object notifyObservers(MenuItem item) {
//		if (item==null)
//			return null;
//		
//		if (item.equals(deleteItem)) {
//			setChanged();
//			notifyObservers(DELETE_ITEM_EVENT);
//			return DELETE_ITEM_EVENT;
//		}
//		
//		return null;
//	}
	
//	private void disposeMenuItems(Menu m) {
//		for (MenuItem mi : popupMenu.getItems()) {
//			SWTUtil.dispose(mi);
//		}		
//	}
//	
//	private void disposeMenuItems() {
//		for (MenuItem mi : popupMenu.getItems()) {
//			if (mi != null && mi.getMenu()!=null)
//			
//			SWTUtil.dispose(mi);
//		}
//	}

	private void createDeleteItem(ICanvasShape s) {			
//		SWTUtil.dispose(deleteItem);
		
		if (s==null || TableUtils.getTableCell(s)!=null)
			return;
		
		deleteItem = createMenuItem("Delete", Images.DELETE, DELETE_ITEM_EVENT);
	}
	
	protected MenuItem createMenuItem(String txt, Image img, Object data) {
		return createMenuItem(txt, img, data, menu, itemSelListener);
	}
	
	protected MenuItem createMenuItem(String txt, Image img, Object data, Menu menu) {
		return createMenuItem(txt, img, data, menu, itemSelListener);
	}
		
	protected static MenuItem createMenuItem(String txt, Image img, Object data, Menu menu, SelectionListener listener) {
		MenuItem item = new MenuItem(menu, SWT.NONE);
		item.setText(txt);
		if (img != null)
			item.setImage(img);
		item.setData(data);
		item.addSelectionListener(listener);
		
		return item;
	}
			
	public void show(ICanvasShape s, int x, int y) {
		if (menu!=null && !menu.isDisposed())
			menu.dispose();
				
		menu = new Menu(canvas);		
		
		initItems(s);
		
		menu.setLocation(x, y);
		menu.setVisible(true);	
	}
	
	public void hide() {
		menu.setVisible(false);
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
		
		selectTableCellsItem = createMenuItem("Select all cells", null, SELECT_TABLE_CELLS_EVENT);
		selectTableRowCellsItem = createMenuItem("Select row cells", null, SELECT_TABLE_ROW_CELLS_EVENT);
		selectTableColumnCellsItem = createMenuItem("Select columns cells", null, SELECT_TABLE_COLUMN_CELLS_EVENT);
		deleteTableRowItem = createMenuItem("Delete row", Images.DELETE, DELETE_TABLE_ROW_EVENT);
		deleteTableColumnItem = createMenuItem("Delete column", Images.DELETE, DELETE_TABLE_COLUMN_EVENT);
		
		borderMenuItem = new MenuItem(menu, SWT.CASCADE);
		borderMenuItem.setText("Border");
		
		borderMenu = new Menu(menu);
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
		createDeleteItem(s);
		createTableItems(s);
		createTableCellItems(s);		
	}	

}
