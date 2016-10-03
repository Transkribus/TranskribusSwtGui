package eu.transkribus.swt.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;

public class DropdownSelectionListener extends SelectionAdapter {
  private ToolItem dropdown;

  private Menu menu;

  public DropdownSelectionListener(ToolItem dropdown) {
    this.dropdown = dropdown;
    menu = new Menu(dropdown.getParent().getShell());
  }

  public void add(String item) {
    MenuItem menuItem = new MenuItem(menu, SWT.NONE);
    menuItem.setText(item);
    menuItem.addSelectionListener(new SelectionAdapter() {
      @Override
	public void widgetSelected(SelectionEvent event) {
        MenuItem selected = (MenuItem) event.widget;
        dropdown.setText(selected.getText());
      }
    });
  }

  @Override
public void widgetSelected(SelectionEvent event) {
    if (event.detail == SWT.ARROW) {
      ToolItem item = (ToolItem) event.widget;
      Rectangle rect = item.getBounds();
      Point pt = item.getParent().toDisplay(new Point(rect.x, rect.y));
      menu.setLocation(pt.x, pt.y + rect.height);
      menu.setVisible(true);
    } else {
      System.out.println(dropdown.getText() + " Pressed");
    }
  }
}