package examples;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class ToolItemRadioGroups {

  public static void main(String[] args) {
    Display display = new Display();
    Shell shell = new Shell(display);
    ToolBar toolBar = new ToolBar(shell, SWT.BORDER | SWT.VERTICAL);

    ToolItem item = new ToolItem(toolBar, SWT.RADIO);
    item.setText("One");
    item = new ToolItem(toolBar, SWT.RADIO);
    item.setText("Two");
    item = new ToolItem(toolBar, SWT.RADIO);
    item.setText("Three");
    new ToolItem(toolBar, SWT.SEPARATOR); // Signals end of group
    item = new ToolItem(toolBar, SWT.RADIO);
    item.setText("One");
    item = new ToolItem(toolBar, SWT.RADIO);
    item.setText("Two");
    item = new ToolItem(toolBar, SWT.RADIO);
    item.setText("Three");

    toolBar.pack();

    shell.pack();
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }
}