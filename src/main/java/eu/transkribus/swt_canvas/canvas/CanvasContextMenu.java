package eu.transkribus.swt_canvas.canvas;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import eu.transkribus.swt_canvas.util.SWTUtil;

public class CanvasContextMenu {
	
	SWTCanvas canvas;
	Menu popupMenu;
	
	public CanvasContextMenu(SWTCanvas canvas) {
		this.canvas = canvas;
		
		init();
	}
	
	private void init() {
		popupMenu = new Menu(canvas);
	    MenuItem newItem = new MenuItem(popupMenu, SWT.CASCADE);
	    newItem.setText("New");
	    MenuItem refreshItem = new MenuItem(popupMenu, SWT.NONE);
	    refreshItem.setText("Refresh");
	    MenuItem deleteItem = new MenuItem(popupMenu, SWT.NONE);
	    deleteItem.setText("Delete");
	    
	    Menu newMenu = new Menu(popupMenu);
	    newItem.setMenu(newMenu);

	    MenuItem shortcutItem = new MenuItem(newMenu, SWT.NONE);
	    shortcutItem.setText("Shortcut");
	    MenuItem iconItem = new MenuItem(newMenu, SWT.NONE);
	    iconItem.setText("Icon");

//	    canvas.setMenu(popupMenu);
	}
	
	public void show(int x, int y) {
		popupMenu.setLocation(x, y);
		popupMenu.setVisible(true);		
	}
	
	public void hide() {
		popupMenu.setVisible(false);
	}	

}
