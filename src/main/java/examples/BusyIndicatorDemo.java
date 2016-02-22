package examples;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class BusyIndicatorDemo {
  // The amount of time to sleep (in ms)
  private static final int SLEEP_TIME = 3000;

  // Labels for the button
  private static final String RUN = "Press to Run";

  private static final String IS_RUNNING = "Running...";

  public static void main(String[] args) {
    Display display = new Display();
    Shell shell = new Shell(display);
    shell.setText("BusyIndicator Test");

    shell.setLayout(new FillLayout());
    final Button button = new Button(shell, SWT.PUSH);
    button.setText(RUN);
    button.addSelectionListener(new SelectionAdapter() {
      @Override
	public void widgetSelected(SelectionEvent event) {
        // Change the button's text
        button.setText(IS_RUNNING);

        // Show the busy indicator
        BusyIndicator.showWhile(button.getDisplay(), new SleepThread(SLEEP_TIME));
        // Thread has completed; reset the button's text
        button.setText(RUN);
      }
    });

    shell.pack();
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    display.dispose();

  }
}

class SleepThread extends Thread {
  private long ms;

  public SleepThread(long ms) {
    this.ms = ms;
  }

  @Override
public void run() {
    try {
      sleep(ms);
    } catch (InterruptedException e) {
    }
  }
}