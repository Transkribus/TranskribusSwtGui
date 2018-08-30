package eu.transkribus.swt.util;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class ToolBoxTest {

	public static void main(String[] args) {
		
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {								
				getShell().setSize(500, 200);
				getShell().setLayout(new FillLayout());
				
				SWTUtil.centerShell(getShell());
				
				ToolBar tb = new ToolBar(getShell(), 0);
				ToolItem ti = new ToolItem(tb, SWT.CHECK);
				ti.setImage(Images.REFRESH);
				
				Button b = new Button(getShell(), SWT.CHECK);
				b.setText("Press Me!");
				
				ToolBox box = new ToolBox(parent.getShell(), true, "toolbox...");
				
//				box.addButton("hello", Images.REFRESH, 0);
//				box.addButton(null, Images.APPLICATION, 0);
//				box.addButton("test", null, 0);
				
				box.addButton(null, Images.REFRESH, 0);
				box.addButton(null, Images.APPLICATION, 0);
				box.addButton(null, Images.ADD, 0);
				
				box.addTriggerWidget(b);
				box.addTriggerWidget(ti);
				
				b.addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent e) {
						DialogUtil.createCustomMessageDialog(getShell(), "asdfad", null, null, 0, null, 0, b);
						
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						// TODO Auto-generated method stub
						
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
