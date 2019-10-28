package examples;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
 
public class DroplineMenuExample {
 
    public static void main(String[] args) {
        new DroplineMenuExample();
    }
 
    public DroplineMenuExample() {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout(SWT.VERTICAL));
        Label label = new Label(shell, SWT.NONE);
        label.setText("SWT - Dropline Menu Example");
        final Button button = new Button(shell, SWT.ARROW | SWT.DOWN);
        final DropLineMenu dlm = new DropLineMenu(shell);
 
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                dlm.show(button);
            }
        });
        shell.pack();
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }
 
    class DropLineMenu {
 
        private Shell parent;
 
        private Shell droplineShell;
 
        public DropLineMenu(Shell parent) {
            this.parent = parent;
            droplineShell = new Shell(parent, SWT.NONE);
            GridLayout gridLayout = new GridLayout();
            gridLayout.numColumns = 1;
            droplineShell.setLayout(gridLayout);
 
            ToolBar toolBar = new ToolBar(droplineShell, SWT.FLAT | SWT.CENTER);
 
            GridData gridData = new GridData();
            gridData.horizontalAlignment = GridData.CENTER;
            gridData.grabExcessHorizontalSpace = true;
            toolBar.setLayoutData(gridData);
 
            ToolItem item1 = new ToolItem(toolBar, SWT.PUSH);
            item1.setText("Item 1");
            ToolItem item2 = new ToolItem(toolBar, SWT.PUSH);
            item2.setText("Item 2");
            ToolItem item3 = new ToolItem(toolBar, SWT.PUSH);
            item3.setText("Item 3");
 
            toolBar.pack();
            droplineShell.pack();
 
            item1.addListener(SWT.Selection, droplineListener);
            item2.addListener(SWT.Selection, droplineListener);
            item3.addListener(SWT.Selection, droplineListener);
 
            droplineShell.addListener(SWT.Deactivate, new Listener() {
                public void handleEvent(Event e) {
                    droplineShell.setVisible(false);
                }
            });
 
        }
 
        public void show(Button button) {
            Rectangle rect = button.getBounds();
            Point pt = button.getParent().toDisplay(new Point(rect.x, rect.y));
 
            droplineShell.setLocation(pt.x, pt.y + rect.height);
 
            droplineShell.open();
        }
 
        public void hide() {
            droplineShell.setVisible(false);
        }
 
        public boolean isVisible() {
            return droplineShell.isVisible();
        }
 
        private Listener droplineListener = new Listener() {
 
            @Override
            public void handleEvent(Event event) {
                DropLineMenu.this.hide();
 
                ToolItem item = (ToolItem) event.widget;
                System.out.println(item.getText());
            }
        };
    }
}
