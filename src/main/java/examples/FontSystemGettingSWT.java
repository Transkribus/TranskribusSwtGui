package examples;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class FontSystemGettingSWT {
  public static void main(String[] args) {
    final Display display = new Display();
    Shell shell = new Shell(display);
    shell.setText("Canvas Example");
    shell.setLayout(new FillLayout());

    Canvas canvas = new Canvas(shell, SWT.NONE);

    canvas.addPaintListener(new PaintListener() {
      @Override
	public void paintControl(PaintEvent e) {
        
        e.gc.setFont(display.getSystemFont());
        
        e.gc.drawText(display.getSystemFont().getFontData()[0].getName(), 5, 5);
        e.gc.drawText(display.getSystemFont().getFontData()[0].getName(), 5, 5);
      }
    });

    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    display.dispose();
  }
}