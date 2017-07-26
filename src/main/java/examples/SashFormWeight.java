package examples;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SashFormWeight {
  public static void main(String[] args) {
    Display display = new Display();
    Shell shell = new Shell(display);
    shell.setText("SashForm Test");
    // Fill the parent window with the buttons and sash
    shell.setLayout(new FillLayout());

    // Create the SashForm and the buttons
    SashForm sashForm = new SashForm(shell, SWT.VERTICAL);
    new Button(sashForm, SWT.PUSH).setText("Left");
    new Button(sashForm, SWT.PUSH).setText("Right");
    new Button(sashForm, SWT.PUSH).setText("Right2");
    new Button(sashForm, SWT.PUSH).setText("Right3");

    sashForm.setWeights(new int[] { 1, 3, 2, 3});
    
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    display.dispose();

  }
}