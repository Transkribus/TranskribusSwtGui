package eu.transkribus.swt_gui.menubar;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import eu.transkribus.swt_gui.mainwidget.menubar.TrpMenuBar;

public class TrpMenuBarTest {

	public static void main(String[] args) {
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				// getShell().setLayout(new FillLayout());
				getShell().setSize(100, 100);
				
				final Button b = new Button(getShell(), SWT.PUSH);
				b.setText("Menu");
				
				final TrpMenuBar menu = new TrpMenuBar(getShell());
				
				b.addSelectionListener(new SelectionListener() {
					
					@Override public void widgetSelected(SelectionEvent e) {
		                Point point = getShell().toDisplay(new Point(b.getBounds().x, b.getBounds().y+b.getBounds().height));
		                menu.getMenuBar().setLocation(point.x, point.y);
		                menu.getMenuBar().setVisible(true);
					}
					
					@Override public void widgetDefaultSelected(SelectionEvent e) {
					}
				});
				
			
				return parent;
			}
		};
		aw.setBlockOnOpen(true);
		aw.open();

		Display.getCurrent().dispose();
		
	}

}
