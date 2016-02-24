package examples;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import eu.transkribus.swt_canvas.util.Images;



public class PopupDemo
{
  public static void main(String[] args)
  {
    final Display display = new Display();
//    final BalloonWindow bw = new BalloonWindow(display, SWT.ON_TOP | SWT.CLOSE);
    final BalloonWindow bw = new BalloonWindow(display, SWT.ON_TOP);
    bw.setAnchor(SWT.RIGHT|SWT.BOTTOM);
    bw.setLocation(display.getClientArea().width, display.getClientArea().height);
    bw.setText("Lorem ipsum dolor sit amet");
    Image img = Images.getOrLoad("/icons/pencil.png");
    bw.setImage(img);

    Label label = new Label(bw.getContents(), SWT.WRAP);
    label.setText("Consectetur adipisicing elit, sed do eiusmod tempor incididunt ut "+
      "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation "+
      "ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in "+
      "reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. "+
      "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt "+
      "mollit anim id est laborum.");
    label.setSize(label.computeSize(300, SWT.DEFAULT));
    label.setBackground(bw.getShell().getBackground());
    bw.getContents().setSize(label.getSize());
    bw.addSelectionControl(label);
 
    //bw.setAnchor(SWT.BOTTOM|SWT.RIGHT);
    //bw.setLocation(1300, 400);

    bw.addListener(SWT.Selection, new Listener()
    {
      @Override
	public void handleEvent(Event event)
      {
        System.out.println("Balloon Window was selected");
        bw.close();
      }
    });

    bw.setVisible(true);
    while(!bw.getShell().isDisposed())
    {
      if(!display.readAndDispatch()) display.sleep();
    }
    img.dispose();
    display.dispose();
  }
}
