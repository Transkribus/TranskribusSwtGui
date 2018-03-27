package eu.transkribus.swt_gui.canvas;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.customtags.StructureTag;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.RegionTypeUtil;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableRegionType;
import eu.transkribus.core.util.Event;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.canvas.ICanvasContextMenuListener.CreateDefaultLineEvent;
import eu.transkribus.swt_gui.canvas.ICanvasContextMenuListener.DeleteItemEvent;
import eu.transkribus.swt_gui.canvas.ICanvasContextMenuListener.DeleteTableEvent;
import eu.transkribus.swt_gui.canvas.ICanvasContextMenuListener.FocusTableEvent;
import eu.transkribus.swt_gui.canvas.ICanvasContextMenuListener.MergeTableCellsEvent;
import eu.transkribus.swt_gui.canvas.ICanvasContextMenuListener.RemoveIntermediatePointsTableEvent;
import eu.transkribus.swt_gui.canvas.ICanvasContextMenuListener.SelectTableCellsEvent;
import eu.transkribus.swt_gui.canvas.ICanvasContextMenuListener.SetStructureEvent;
import eu.transkribus.swt_gui.canvas.ICanvasContextMenuListener.SplitTableCellEvent;
import eu.transkribus.swt_gui.canvas.ICanvasContextMenuListener.TableBorderEditEvent;
import eu.transkribus.swt_gui.canvas.ICanvasContextMenuListener.TableHelpEvent;
import eu.transkribus.swt_gui.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.canvas.shapes.TableDimension;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.metadata.StructCustomTagSpec;
import eu.transkribus.swt_gui.table_editor.BorderFlags;
import eu.transkribus.swt_gui.table_editor.TableUtils;

public class CanvasContextMenu extends Observable {
	private final static Logger logger = LoggerFactory.getLogger(CanvasContextMenu.class);
	
	protected SWTCanvas canvas;
	protected Menu menu;
	protected MenuItem deleteItem;
	protected MenuItem createDefaultLineItem;
	protected SelectionListener itemSelListener;

	Menu borderMenu;
	Menu structMenu;

	MenuItem selectTableCellsItem;

	MenuItem selectTableRowCellsItem;

	MenuItem selectTableColumnCellsItem;
	
	MenuItem focusTableItem;

	MenuItem deleteTableRowItem;
	
	Set<ICanvasContextMenuListener> listener = new HashSet<>();
	
	public boolean addListener(ICanvasContextMenuListener l) {
		return listener.add(l);
	}
	
	public boolean removeListener(ICanvasContextMenuListener l) {
		return listener.remove(l);
	}
	
	public void sendEvent(final Event event) {
		if (Thread.currentThread() == Display.getDefault().getThread()) {
			for (ICanvasContextMenuListener l : listener) {
				l.handleEvent(event);
			}
		} else {
			Display.getDefault().asyncExec(() -> {
				for (ICanvasContextMenuListener l : listener) {
					l.handleEvent(event);
				}
			});
		}
	}

	MenuItem deleteTableColumnItem;

//	MenuItem deleteTableItem;

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

	private int createDeleteItem(ICanvasShape s) {			
//		SWTUtil.dispose(deleteItem);
		
		if (s==null || TableUtils.getTableCell(s)!=null) {
			return 0;
		}
		
		deleteItem = createMenuItem("Delete", Images.DELETE, new DeleteItemEvent(this), menu);
		return 1;
	}
	
	private int createCreateDefaultLineItem(ICanvasShape s) {
		if (s == null)
			return 0;
		
		ITrpShapeType st = (ITrpShapeType) s.getData();
		if (st==null || (!RegionTypeUtil.isLine(st) && !RegionTypeUtil.isBaseline(st)))
			return 0;
		
		createDefaultLineItem = createMenuItem("Create default line shape", null, new CreateDefaultLineEvent(this), menu);
		return 1;
	}
	
	protected MenuItem createMenuItem(String txt, Image img, Event event, Menu menu) {
		MenuItem item = new MenuItem(menu, SWT.NONE);
		item.setText(txt);
		if (img != null)
			item.setImage(img);
		
		SWTUtil.onSelectionEvent(item, (e) -> { sendEvent(event); });
		
		return item;
	}
	
//	protected MenuItem createMenuItem(String txt, Image img, Object data) {
//		return createMenuItem(txt, img, data, menu, itemSelListener);
//	}
//	
//	protected MenuItem createMenuItem(String txt, Image img, Object data, Menu menu) {
//		return createMenuItem(txt, img, data, menu, itemSelListener);
//	}
//		
//	protected static MenuItem createMenuItem(String txt, Image img, Object data, Menu menu, SelectionListener listener) {
//		MenuItem item = new MenuItem(menu, SWT.NONE);
//		item.setText(txt);
//		if (img != null)
//			item.setImage(img);
//		item.setData(data);
//		item.addSelectionListener(listener);
//		
//		return item;
//	}
			
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
//		SWTUtil.dispose(deleteTableItem);
		
		TrpTableRegionType t = TableUtils.getTable(s, true);
		if (t == null)
			return;
		
//		deleteTableItem = createMenuItem("Delete Table", Images.DELETE, new DeleteTableEvent(this));
	}
	
	Menu createSubMenu(Menu parentMenu, String text) {
		MenuItem mi = new MenuItem(menu, SWT.CASCADE);
		mi.setText(text);
		
		Menu m = new Menu(parentMenu);
		mi.setMenu(m);
		
		return m;
	}

	private void createTableCellItems(ICanvasShape s) {
		TrpTableCellType cell = TableUtils.getTableCell(s);
		if (cell == null)
			return;
		
		selectTableCellsItem = createMenuItem("Select all cells", null, new SelectTableCellsEvent(this, null), menu);
		selectTableRowCellsItem = createMenuItem("Select row cells", null, new SelectTableCellsEvent(this, TableDimension.ROW), menu);
		selectTableColumnCellsItem = createMenuItem("Select columns cells", null, new SelectTableCellsEvent(this, TableDimension.COLUMN), menu);
		focusTableItem = createMenuItem("Focus on table", null, new FocusTableEvent(this), menu);
		
		deleteTableRowItem = createMenuItem("Delete row", Images.DELETE, new DeleteTableEvent(this, TableDimension.ROW), menu);
		deleteTableColumnItem = createMenuItem("Delete column", Images.DELETE, new DeleteTableEvent(this, TableDimension.COLUMN), menu);
		
		borderMenu = createSubMenu(menu, "Border");
				
		createMenuItem("None", Images.BORDER_NONE, new TableBorderEditEvent(this, BorderFlags.none()), borderMenu);
		createMenuItem("All", Images.BORDER_ALL, new TableBorderEditEvent(this, BorderFlags.all()), borderMenu);
		createMenuItem("Closed", Images.BORDER_CLOSED, new TableBorderEditEvent(this, BorderFlags.closed()), borderMenu);
		
		createMenuItem("Left", Images.BORDER_LEFT, new TableBorderEditEvent(this, BorderFlags.left()), borderMenu);
		createMenuItem("Right", Images.BORDER_RIGHT, new TableBorderEditEvent(this, BorderFlags.right()), borderMenu);
		createMenuItem("Left / Right", Images.BORDER_LEFT_RIGHT, new TableBorderEditEvent(this, BorderFlags.left_right()), borderMenu);
		
		createMenuItem("Bottom", Images.BORDER_BOTTOM, new TableBorderEditEvent(this, BorderFlags.bottom()), borderMenu);
		createMenuItem("Top", Images.BORDER_TOP, new TableBorderEditEvent(this, BorderFlags.top()), borderMenu);
		createMenuItem("Bottom / Top", Images.BORDER_BOTTOM_TOP, new TableBorderEditEvent(this, BorderFlags.bottom_top()), borderMenu);
		
		createMenuItem("Horizontally closed", Images.BORDER_HORIZONTAL_CLOSED, new TableBorderEditEvent(this, BorderFlags.horizontal_closed()), borderMenu);
		createMenuItem("Horizontally open", Images.BORDER_HORIZONTAL_OPEN, new TableBorderEditEvent(this, BorderFlags.horizontal_open()), borderMenu);
		
		createMenuItem("Vertically closed", Images.BORDER_VERTICAL_CLOSED, new TableBorderEditEvent(this, BorderFlags.vertical_closed()), borderMenu);
		createMenuItem("Vertically open", Images.BORDER_VERTICAL_OPEN, new TableBorderEditEvent(this, BorderFlags.vertical_open()), borderMenu);
				
		if (cell.isMergedCell())
			createMenuItem("Split merged cell", null, new SplitTableCellEvent(this), menu);
		
		if (canvas.getNSelected()>=2) {
			createMenuItem("Merge cells", null, new MergeTableCellsEvent(this), menu);
		}
		
		if (s.getNPoints() > 4) // TODO: better check if there are intermediate points -> have to check also if a point is corner point of neighbor!!
			createMenuItem("Remove non-corner points", null, new RemoveIntermediatePointsTableEvent(this), menu);
		
		// about:
		createMenuItem("Table help", Images.HELP, new TableHelpEvent(this), menu);
	}
	
	private int createStructureItems(ICanvasShape s) {
		structMenu = createSubMenu(menu, "Assign structure type");
		int count=0;
		Storage store = Storage.getInstance();
		
		for (StructCustomTagSpec c : store.getStructCustomTagSpecs()) {
			StructureTag st = c.getCustomTag();
			createMenuItem(st.getType(), Images.getOrCreateLabelImage(store.getStructureTypeColor(st.getType()), 16, 16), new SetStructureEvent(this, st), structMenu);
			++count;
		}
		return count;
	}
	
	private void createSeparator(Menu menu) {
		if (menu.getItemCount() > 0) { // only if there is an item to separate!
			int lastItemStyle = menu.getItem(menu.getItemCount()-1).getStyle();
			if ((lastItemStyle & SWT.SEPARATOR) != SWT.SEPARATOR) { // only if last item wasn't also separator!
				new MenuItem(menu, SWT.SEPARATOR);
			}
		}
	}

	protected void initItems(ICanvasShape s) {
		createStructureItems(s);
		createSeparator(menu);
		createCreateDefaultLineItem(s);
		createSeparator(menu);
		createTableItems(s);
		createSeparator(menu);
		createTableCellItems(s);
		createSeparator(menu);
		createDeleteItem(s);
	}	

}
