package eu.transkribus.swt.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.junit.Assert;

public class ToolBox {
	Shell shell;
	Map<Widget, TriggerWidgetListener> triggerWidgets = new HashMap<>();
		
	class TriggerWidgetListener implements SelectionListener {
		Widget triggerWidget;
		
		public TriggerWidgetListener(Widget triggerWidget) {
			Assert.assertTrue("Only Button or ToolItem object supported!", triggerWidget instanceof Button || triggerWidget instanceof ToolItem);

			this.triggerWidget = triggerWidget;
			
			if (triggerWidget instanceof Button) {
				((Button) triggerWidget).addSelectionListener(this);
			}
			else if (triggerWidget instanceof ToolItem) {
				((ToolItem) triggerWidget).addSelectionListener(this);
			}			
		}
				
		public boolean hasTriggerWidget() {
			return triggerWidget != null;
		}
		
		public Rectangle getTriggerWidgetBounds() {
			if (triggerWidget instanceof Button) {
				return ((Button) triggerWidget).getBounds();
			}
			else if (triggerWidget instanceof ToolItem) {
				return ((ToolItem) triggerWidget).getBounds();
			}
			else {
				return null;
			}
		}
		
		public Composite getTriggerWidgetParent() {
			if (triggerWidget instanceof Button) {
				return ((Button) triggerWidget).getParent();
			}
			else if (triggerWidget instanceof ToolItem) {
				return ((ToolItem) triggerWidget).getParent();
			}
			else {
				return null;
			}
			
		}		
		
		public boolean isSelected() {
			if (triggerWidget instanceof Button) {
				return ((Button) triggerWidget).getSelection();
			}
			else if (triggerWidget instanceof ToolItem) {
				return ((ToolItem) triggerWidget).getSelection();
			}
			else {
				return false;
			}
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (!hasTriggerWidget())
				return;
			
			if (!isSelected()) {
				hide();
			} else {	
				Rectangle rect = getTriggerWidgetBounds();
				Composite c = getTriggerWidgetParent();
				
				Point pt = c.toDisplay(new Point(rect.x, rect.y));
				showAt(pt.x + rect.width, pt.y + rect.height);
			}
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
		
	}
	
	public ToolBox(Shell parent, boolean vertical) {
		shell = new Shell(parent, SWT.RESIZE | SWT.CLOSE | SWT.MODELESS);

		shell.setLayout(new RowLayout(vertical ? SWT.VERTICAL : SWT.HORIZONTAL));		
		shell.addShellListener(new ShellListener() {
			
			@Override
			public void shellIconified(ShellEvent e) {
			}
			
			@Override
			public void shellDeiconified(ShellEvent e) {
			}
			
			@Override
			public void shellDeactivated(ShellEvent e) {
			}
			
			@Override
			public void shellClosed(ShellEvent e) {
				e.doit = false;
				hide();
			}
			
			@Override
			public void shellActivated(ShellEvent e) {
			}
		});
		
		shell.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					hide();
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
//		shell.addListener(SWT.Traverse, new Listener() {
//		      public void handleEvent(Event event) {
//		        switch (event.detail) {
//		        case SWT.TRAVERSE_ESCAPE:
//		          shell.close();
//		          event.detail = SWT.TRAVERSE_NONE;
//		          event.doit = false;
//		          break;
//		        }
//		      }
//		    });
	}
	
	public void addTriggerWidget(Widget w) {
		TriggerWidgetListener twl = new TriggerWidgetListener(w);
		triggerWidgets.put(w, twl);
	}
	
	public void removeTriggerWidget(Widget w) {
		TriggerWidgetListener twl = triggerWidgets.get(w);
		if (twl != null) {
			if (w instanceof Button) {
				((Button) w).removeSelectionListener(twl);
			}
			else if (w instanceof ToolItem) {
				((ToolItem) w).removeSelectionListener(twl);
			}
			
			triggerWidgets.remove(w);
		}
	}
	
	public Button addButton(String text, Image img, int style) {
		Button b = new Button(shell, style);
		if (!StringUtils.isEmpty(text))
			b.setText(text);
		if (img != null)
			b.setImage(img);
		
		shell.pack();
		
		return b;
	}
	
	int posX=0, posY=0;
	
	public void showAt(int x, int y) {
		posX=x;
		posY=y;
		
		shell.pack();
		shell.setVisible(true);
		shell.setLocation(x, y);
		shell.setActive();

		for (Widget w : triggerWidgets.keySet()) {
			SWTUtil.setSelection(w, true);	
		}
	}
	
	public void hide() {
		shell.setLocation(posX, posY);
		shell.setVisible(false);
		
		for (Widget w : triggerWidgets.keySet()) {
			SWTUtil.setSelection(w, false);	
		}
	}
	
	public Shell getShell() {
		return shell;
	}

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
				
				ToolBox box = new ToolBox(parent.getShell(), true);
				
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
