package examples;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
  
public class Main {
   static Display display;
   static Shell shell;
   static Color color;
     
   public static void main(String[] args) {
      display = new Display();
      shell = new Shell(display);
  
      // pos x, pos y, width, height
      shell.setBounds(200, 200, 400, 200);
      shell.setText("SWT Shell Demonstration");
      shell.setLayout(new GridLayout());
  
      final Group buttonGroup = new Group(shell, SWT.NONE);
      GridLayout gridLayout = new GridLayout();
      gridLayout.numColumns = 3;
      buttonGroup.setLayout(gridLayout);
      buttonGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
      final Button createShellButton = new Button(buttonGroup, SWT.PUSH);
      createShellButton.setText("Open Shell");
  
      SelectionListener selectionListener = new SelectionAdapter () {
         public void widgetSelected(SelectionEvent event) {
            final Shell childShell = new Shell(SWT.RESIZE | SWT.CLOSE | SWT.ON_TOP);
            Button closeButton = new Button(childShell, SWT.PUSH);
            closeButton.setBounds(10, 10, 100, 30);
            closeButton.setText("Close Shell");
            closeButton.addListener(SWT.Selection, new Listener() {
               public void handleEvent(Event event) {
                  childShell.dispose();
               }
            });
           
            childShell.setSize (300, 100);
            childShell.setText ("Title of Shell");
            childShell.open ();                 
         };
      };
        
      createShellButton.addSelectionListener(selectionListener);
        
      shell.open();
  
      while (!shell.isDisposed()) {
         if (!display.readAndDispatch()) {
            display.sleep();
         }
      }
      if (color != null && !color.isDisposed()) {
         color.dispose();
      }
      display.dispose();
   }
}