package eu.transkribus.swt_gui.dialogs;

import java.io.IOException;

import javax.security.auth.login.LoginException;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.SWTUtil;

public class TextFieldDialogTest {

	public static void main(String[] args) throws LoginException {		
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				// getShell().setLayout(new FillLayout());
				getShell().setSize(600, 600);
				
//				TextFieldDialog rtd = new TextFieldDialog(getShell(), "title!!", "<strong>bold text!</strong>");
				
				try {
					String helpText = CoreUtils.readStringFromTxtFile("CHANGES.txt");
					DialogUtil.showMessageBox(getShell(), 
							"aasdf", helpText, SWT.RESIZE);					
				} catch (IOException e) {
					e.printStackTrace();
				}
				

				
				
				
				
//				rtd.open();
				
//				PageLockTablePagination w = new PageLockTablePagination(getShell(), 0, 25);
//				Button btn = new Button(parent, SWT.PUSH);
//				btn.setText("Open upload dialog");
//				btn.addSelectionListener(new SelectionAdapter() {
//					@Override public void widgetSelected(SelectionEvent e) {
//						(new UploadDialogUltimate(getShell(), null)).open();
//					}
//				});

				SWTUtil.centerShell(getShell());

				return parent;
			}
		};
		aw.setBlockOnOpen(true);
		aw.open();

		Display.getCurrent().dispose();
	}

}
