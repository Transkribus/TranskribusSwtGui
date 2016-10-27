package examples;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import eu.transkribus.swt.util.Images;

public class ToolBarVertical {

  public static void main(String[] args) {
    Display display = new Display();
    Shell shell = new Shell(display);
    ToolBar bar = new ToolBar(shell, SWT.BORDER|SWT.VERTICAL);
    for (int i = 0; i < 4; i++) {
      ToolItem item = new ToolItem(bar, 0);
//      item.setText("Item " + i);
      item.setImage(Images.APPLICATION);
    }
    bar.pack();

    shell.pack();
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }
}