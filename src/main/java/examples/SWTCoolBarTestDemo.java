package examples;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class SWTCoolBarTestDemo extends ApplicationWindow {
  public SWTCoolBarTestDemo() {
    super(null);
  }

  protected Control createContents(Composite parent) {
    // --- Create the window title. ---

    getShell().setText("CoolBar Test");

    String asCoolItemSection[] = { "File", "Formatting", "Search" };
    CoolBar composite = new CoolBar(parent, SWT.NONE);
    for (int idxCoolItem = 0; idxCoolItem < 3; ++idxCoolItem) {
      CoolItem item = new CoolItem(composite, SWT.NONE);
      ToolBar tb = new ToolBar(composite, SWT.FLAT);
      for (int idxItem = 0; idxItem < 3; ++idxItem) {
        ToolItem ti = new ToolItem(tb, SWT.NONE);
        ti
            .setText(asCoolItemSection[idxCoolItem] + " Item #"
                + idxItem);
      }
      Point p = tb.computeSize(SWT.DEFAULT, SWT.DEFAULT);
      tb.setSize(p);
      Point p2 = item.computeSize(p.x, p.y);
      item.setControl(tb);
      item.setSize(p2);
    }
    return composite;
  }

  public static void main(String[] args) {
    // --- Display SWTCoolBarTestDemo until the window is closed. ---

    SWTCoolBarTestDemo app = new SWTCoolBarTestDemo();
    app.setBlockOnOpen(true);
    app.open();
    Display.getCurrent().dispose();
  }
}