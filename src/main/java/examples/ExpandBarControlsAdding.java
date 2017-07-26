package examples;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Shell;

public class ExpandBarControlsAdding {

  public static void main(String[] args) {
    Display display = new Display();
    Shell shell = new Shell(display);
    shell.setLayout(new FillLayout());
    shell.setText("ExpandBar Example");
    ExpandBar bar = new ExpandBar(shell, SWT.V_SCROLL);
//    Image image = new Image(display, "yourFile.gif");

    // First item
    Composite composite = new Composite(bar, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 10;
    layout.verticalSpacing = 10;
    composite.setLayout(layout);
    Button button = new Button(composite, SWT.PUSH);
    button.setText("SWT.PUSH");
    button = new Button(composite, SWT.RADIO);
    button.setText("SWT.RADIO");
    button = new Button(composite, SWT.CHECK);
    button.setText("SWT.CHECK");
    button = new Button(composite, SWT.TOGGLE);
    button.setText("SWT.TOGGLE");
    ExpandItem item0 = new ExpandItem(bar, SWT.NONE, 0);
    item0.setText("What is your favorite button");
    item0.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
    item0.setControl(composite);
    
    
    ExpandItem item1 = new ExpandItem(bar, SWT.NONE, 0);
    item1.setText("What is your favorite button2");
    item1.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
    Button btn = new Button(bar, SWT.PUSH);
    btn.setText("hello");
    item1.setControl(btn);    
    
    
//    item0.setImage(image);

    item0.setExpanded(true);

    bar.setSpacing(8);
    shell.setSize(400, 350);
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
//    image.dispose();
    display.dispose();
  }

}