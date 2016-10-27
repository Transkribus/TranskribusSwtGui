package examples;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class MenuClass {

  Display display;

  Shell shell;

  Menu menuBar, fileMenu, editMenu;

  MenuItem fileMenuHeader, editMenuHeader;

  MenuItem fileExitItem, fileSaveItem, fileEnglishItem, fileGermanItem,
      editCopyItem;

  Text text;

  public MenuClass() {
    display = new Display();
    shell = new Shell(display);
    shell.setText("Menu Example");
    shell.setSize(300, 200);

    text = new Text(shell, SWT.BORDER);
    text.setBounds(80, 50, 150, 25);

    menuBar = new Menu(shell, SWT.BAR);
    fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
    fileMenuHeader.setText("&File");

    fileMenu = new Menu(shell, SWT.DROP_DOWN);
    fileMenuHeader.setMenu(fileMenu);

    fileSaveItem = new MenuItem(fileMenu, SWT.PUSH);
    fileSaveItem.setText("&Save");

    fileEnglishItem = new MenuItem(fileMenu, SWT.RADIO);
    fileEnglishItem.setText("English");

    fileGermanItem = new MenuItem(fileMenu, SWT.RADIO);
    fileGermanItem.setText("German");

    fileExitItem = new MenuItem(fileMenu, SWT.PUSH);
    fileExitItem.setText("E&xit");

    editMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
    editMenuHeader.setText("&Edit");

    editMenu = new Menu(shell, SWT.DROP_DOWN);
    editMenuHeader.setMenu(editMenu);

    editCopyItem = new MenuItem(editMenu, SWT.PUSH);
    editCopyItem.setText("&Copy");

    fileExitItem.addSelectionListener(new MenuItemListener());
    fileSaveItem.addSelectionListener(new MenuItemListener());
    editCopyItem.addSelectionListener(new MenuItemListener());

    fileEnglishItem.addSelectionListener(new RadioItemListener());
    fileGermanItem.addSelectionListener(new RadioItemListener());

    shell.setMenuBar(menuBar);
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }

  class MenuItemListener extends SelectionAdapter {
    public void widgetSelected(SelectionEvent event) {
      if (((MenuItem) event.widget).getText().equals("E&xit")) {
        shell.close();
      }
      text.setText("You selected " + ((MenuItem) event.widget).getText());
    }
  }

  class RadioItemListener extends SelectionAdapter {
    public void widgetSelected(SelectionEvent event) {
      MenuItem item = (MenuItem) event.widget;
      text.setText(item.getText() + " is on.");
    }
  }

  public static void main(String[] args) {
    MenuClass menuExample = new MenuClass();
  }
}


