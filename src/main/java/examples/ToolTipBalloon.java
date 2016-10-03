package examples;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

import eu.transkribus.swt.util.Images;

public class ToolTipBalloon {

  public static void main(String[] args) {
    Display display = new Display();
    Shell shell = new Shell(display);
    Image image = null;
    final ToolTip tip = new ToolTip(shell, SWT.BALLOON | SWT.ICON_INFORMATION);
    tip.setMessage("Here is a message for the user. When the message is too long it wraps. I should say something cool but nothing comes to my mind.");
    Tray tray = display.getSystemTray();
    if (tray != null) {
      final TrayItem item = new TrayItem(tray, SWT.NONE);
//      image = new Image(display, "/icons/yourFile.gif");
      image = Images.getOrLoad("/icons/information.png");
      item.setImage(image);
      tip.setText("Notification from a tray item");
      item.setToolTip(tip);
      tip.addSelectionListener(new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			System.out.println("hello!");
			item.setToolTipText("blablabla");
			
		}
		
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// TODO Auto-generated method stub
			
		}
	});
    } else {
      tip.setText("Notification from anywhere");
      tip.setLocation(400, 400);
    }
    Button button = new Button(shell, SWT.PUSH);
    button.setText("Press for balloon tip");
    button.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        tip.setVisible(true);
      }
    });
    button.pack();
    shell.setBounds(50, 50, 300, 200);
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    if (image != null)
      image.dispose();
    display.dispose();
  }
}