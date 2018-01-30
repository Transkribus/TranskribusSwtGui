package eu.transkribus.swt_gui.metadata;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.core.model.beans.customtags.CustomTag;

/**
 * @deprecated not used and outdated
 *
 */
public class TagPropertyPopup {
	int posX=0, posY=0;
	CustomTagPropertyTable propsTable;
	CustomTag tag;
	
	Shell shell;
	
	public Shell getShell() {
		return shell;
	}

	public TagPropertyPopup(Shell parent) {
		shell = new Shell(parent, SWT.RESIZE | SWT.CLOSE | SWT.MODELESS);
		shell.setLayout(new FillLayout());

//		shell.setLayout(new RowLayout(vertical ? SWT.VERTICAL : SWT.HORIZONTAL));		
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
	
		propsTable = new CustomTagPropertyTable(shell, 0, false);
		propsTable.getTableViewer().getTable().setHeaderVisible(false);
//		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
//		gd.heightHint = 200;
//		propsTable.setLayoutData(gd);
		
		shell.setSize(300, 300);
	}
	
	public void setCustomTag(CustomTag tag) {
		this.tag = tag;
		
		if (this.tag != null) {
			shell.setText(tag.getTagName()+"-text='"+tag.getContainedText()+"'");
			
			CustomTag protoTag = tag.copy();
			propsTable.setInput(protoTag, this.tag);
		} else {
			shell.setText("");
			propsTable.setInput(null, null);
		}
		
		propsTable.selectFirstAttribute();
		
//		Display.getDefault().addFilter(SWT.KeyUp, new Listener() {
//			
//			@Override
//			public void handleEvent(Event event) {
//				if (!shell.isVisible()) {
//					return;
//				}
//				
//				System.out.println("KEY PRESSED: "+event);
//				if (event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR) {
//					System.out.println("ENTER PRESSED!");
//				}
//				
//			}
//		});
	}
	
	public void hide() {
		shell.setLocation(posX, posY);
		shell.setVisible(false);
	}
	
	public void showAt(int x, int y) {
		posX=x;
		posY=y;
		
		shell.pack();
		shell.setVisible(true);
		shell.setLocation(x, y);
		shell.setActive();
	}
	
	public CustomTagPropertyTable getPropertyTable() {
		return propsTable;
	}
	
}
