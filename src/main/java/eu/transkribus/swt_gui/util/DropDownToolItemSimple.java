package eu.transkribus.swt_gui.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import eu.transkribus.swt.util.SWTUtil;

public class DropDownToolItemSimple {
	Menu menu;
	ToolItem toolItem;
	
	List<MenuItem> items = new ArrayList<>();

	public DropDownToolItemSimple(ToolBar parent, int btnStyle, String text, Image img, String toolTip) {
//		super(tb, 0);
//		setLayout(new FillLayout());
		
		toolItem = new ToolItem(parent,  btnStyle);
		if (!StringUtils.isEmpty(text))
			toolItem.setText(text);
		if (img != null)
			toolItem.setImage(img);
		if (!StringUtils.isEmpty(toolTip))
			toolItem.setToolTipText(toolTip);
		
		menu = new Menu(parent.getShell(), SWT.POP_UP);
		toolItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Rectangle rect = toolItem.getBounds();
				Point pt = toolItem.getParent().toDisplay(new Point(rect.x, rect.y));
				
				menu.setLocation(pt.x + rect.width, pt.y + rect.height);
				menu.setVisible(true);
				
//				Point loc = toolItem.getLocation();
//                Rectangle rect = toolItem.getBounds();
//
//                Point mLoc = new Point(loc.x-1, loc.y+rect.height);
//                
//                menu.setLocation(parent.getShell().getDisplay().map(toolItem.getParent(), null, mLoc));
//
//                menu.setVisible(true);			
               }
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	public void setEnabled(boolean enabled) {
//		super.setEnabled(enabled);
		toolItem.setEnabled(enabled);
	}
	
	public MenuItem addItem(String text, Image img, int style) {
		return SWTUtil.createMenuItem(menu, text, img, style);
    }
	
	public Menu getMenu() {
		return menu;
	}
	
	public ToolItem getToolItem() {
		return toolItem;
	}

}
