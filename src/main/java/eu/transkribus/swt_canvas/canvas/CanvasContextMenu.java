package eu.transkribus.swt_canvas.canvas;

import java.util.Observable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_canvas.util.Images;
import eu.transkribus.swt_gui.table_editor.TableUtils;

public class CanvasContextMenu extends Observable {
	private final static Logger logger = LoggerFactory.getLogger(CanvasContextMenu.class);
	
	public static final String DELETE_ITEM_EVENT = "DELETE_EVENT";

	protected SWTCanvas canvas;
	protected Menu popupMenu;
	protected MenuItem deleteItem;
	protected SelectionListener itemSelListener;
	
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
		return createMenuItem(txt, img, data, popupMenu, itemSelListener);
	}
	
	protected MenuItem createMenuItem(String txt, Image img, Object data, Menu menu) {
		return createMenuItem(txt, img, data, menu, itemSelListener);
	}
		
	protected MenuItem createMenuItem(String txt, Image img, Object data, Menu menu, SelectionListener listener) {
		MenuItem item = new MenuItem(menu, SWT.NONE);
		item.setText(txt);
		if (img != null)
			item.setImage(Images.DELETE);
		item.setData(data);
		item.addSelectionListener(listener);
		
		return item;
	}
		
	protected void initItems(ICanvasShape s) {
		createDeleteItem(s);
	}
	
	public void show(ICanvasShape s, int x, int y) {
		if (popupMenu!=null && !popupMenu.isDisposed())
			popupMenu.dispose();
				
		popupMenu = new Menu(canvas);		
		
		initItems(s);
		
		popupMenu.setLocation(x, y);
		popupMenu.setVisible(true);	
	}
	
	public void hide() {
		popupMenu.setVisible(false);
	}	

}
