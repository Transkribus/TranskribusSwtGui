package examples;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Demo {

    static Boolean blnMouseDown=false;
    static int xPos=0;
    static int yPos=0;

    public static void main(final String[] args) {
        Display display=new Display();
        final Shell shell = new Shell( Display.getDefault(), SWT.RESIZE); 
        shell.open();

        shell.addMouseListener(new MouseListener() {
            @Override
            public void mouseUp(MouseEvent arg0) {
                blnMouseDown=false;
            }

            @Override
            public void mouseDown(MouseEvent e) {
                blnMouseDown=true;
                xPos=e.x;
                yPos=e.y;
            }

            @Override
            public void mouseDoubleClick(MouseEvent arg0) {
            }
        });
        shell.addMouseMoveListener(new MouseMoveListener() {

            @Override
            public void mouseMove(MouseEvent e) {
                if(blnMouseDown){
                    shell.setLocation(shell.getLocation().x+(e.x-xPos),shell.getLocation().y+(e.y-yPos));
                }
            }
        });

        while (!shell.isDisposed()) {
          if (!display.readAndDispatch()) {
            display.sleep();
          }
        }  
        display.close();
    }

}