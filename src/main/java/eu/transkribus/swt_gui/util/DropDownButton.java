package eu.transkribus.swt_gui.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class DropDownButton extends Composite {
	Menu menu;
	Button button;
	
	List<MenuItem> items = new ArrayList<>();
	
	public DropDownButton(Composite parent, int btnStyle, String text, Image img, Menu menu) {
		super(parent, 0);
		setLayout(new FillLayout());
		
		button = new Button(this,  btnStyle);
		button.setText(text);
		button.setImage(img);
		
		if (menu != null) {
			this.menu = menu;
		} else {
			this.menu = new Menu(getShell(), SWT.POP_UP);
		}
		
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Point loc = button.getLocation();
				Rectangle rect = button.getBounds();
				Point mLoc = new Point(loc.x - 1, loc.y + rect.height);

				DropDownButton.this.menu.setLocation(getShell().getDisplay().map(button.getParent(), null, mLoc));
				DropDownButton.this.menu.setVisible(true);
			}
		});
		
	}
	
	@Override public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		button.setEnabled(enabled);
	}
	
	public MenuItem addItem(String text, Image img) {
		MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText(text);
        item.setImage(img);
        
        return item;
    }
	
	public Menu getMenu() {
		return menu;
	}
	
	public Button getButton() {
		return button;
	}

}
