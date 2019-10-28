package examples;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class StyledTextExample {
	public static void main(String [] args) {
		// create the widget's shell
		Shell shell = new Shell();
		shell.setLayout(new FillLayout());
		shell.setSize(200, 100);
		Display display = shell.getDisplay();
		// create the styled text widget
		StyledText widget = new StyledText(shell, SWT.BORDER);
		widget.setText("This is the StyledText widget.");

		widget.setBlockSelection(true);

		
		
		shell.open();
		while (!shell.isDisposed())
		if (!display.readAndDispatch()) display.sleep();
	}
}
