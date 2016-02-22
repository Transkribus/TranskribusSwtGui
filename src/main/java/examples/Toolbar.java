package examples;
/******************************************************************************
 * All Right Reserved. 
 * Copyright (c) 1998, 2004 Jackwind Li Guojie
 * 
 * Created on Feb 25, 2004 7:48:37 PM by JACK
 * $Id$
 * 
 *****************************************************************************/



import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class Toolbar {
  Display display = new Display();
  Shell shell = new Shell(display);

  ToolBar toolBar;
  
  public Toolbar() {
    toolBar = new ToolBar(shell, SWT.FLAT | SWT.WRAP | SWT.RIGHT);
    
    ToolItem itemPush = new ToolItem(toolBar, SWT.PUSH);
    itemPush.setText("PUSH item");
//    Image icon = new Image(shell.getDisplay(), "icons/new.gif");
//    itemPush.setImage(icon);
    
    ToolItem itemCheck = new ToolItem(toolBar, SWT.CHECK);
    itemCheck.setText("CHECK item");
    
    ToolItem itemRadio1 = new ToolItem(toolBar, SWT.RADIO);
    itemRadio1.setText("RADIO item 1");
    
    ToolItem itemRadio2 = new ToolItem(toolBar, SWT.RADIO);
    itemRadio2.setText("RADIO item 2");
    
    ToolItem itemSeparator = new ToolItem(toolBar, SWT.SEPARATOR);
    Text text = new Text(toolBar, SWT.BORDER | SWT.SINGLE);
    text.pack();
    itemSeparator.setWidth(text.getBounds().width);
    itemSeparator.setControl(text);
    
    final ToolItem itemDropDown = new ToolItem(toolBar, SWT.DROP_DOWN);
    itemDropDown.setText("DROP_DOWN item");
    itemDropDown.setToolTipText("Click here to see a drop down menu ...");
    
    final Menu menu = new Menu(shell, SWT.POP_UP);
    new MenuItem(menu, SWT.PUSH).setText("Menu item 1");
    new MenuItem(menu, SWT.PUSH).setText("Menu item 2");
    new MenuItem(menu, SWT.SEPARATOR);
    new MenuItem(menu, SWT.PUSH).setText("Menu item 3");
    
    itemDropDown.addListener(SWT.Selection, new Listener() {
      @Override
	public void handleEvent(Event event) {
        if(event.detail == SWT.ARROW) {
          Rectangle bounds = itemDropDown.getBounds();
          Point point = toolBar.toDisplay(bounds.x, bounds.y + bounds.height);
          menu.setLocation(point);
          menu.setVisible(true);
        }
      }
    });
    
    Listener selectionListener = new Listener() {
      @Override
	public void handleEvent(Event event) {
        ToolItem item = (ToolItem)event.widget;
        System.out.println(item.getText() + " is selected");
        if( (item.getStyle() & SWT.RADIO) != 0 || (item.getStyle() & SWT.CHECK) != 0 ) 
          System.out.println("Selection status: " + item.getSelection());
      }
    };
    
    itemPush.addListener(SWT.Selection, selectionListener);
    itemCheck.addListener(SWT.Selection, selectionListener);
    itemRadio1.addListener(SWT.Selection, selectionListener);
    itemRadio2.addListener(SWT.Selection, selectionListener);
    itemDropDown.addListener(SWT.Selection, selectionListener);

    toolBar.pack();
    
    shell.addListener(SWT.Resize, new Listener() {
      @Override
	public void handleEvent(Event event) {
        Rectangle clientArea = shell.getClientArea();
        toolBar.setSize(toolBar.computeSize(clientArea.width, SWT.DEFAULT));
      }
    });
    
    shell.setSize(500, 100);
    shell.open();
    //textUser.forceFocus();

    // Set up the event loop.
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        // If no more entries in event queue
        display.sleep();
      }
    }

    display.dispose();
  }

  private void init() {

  }

  public static void main(String[] args) {
    new Toolbar();
  }
}
