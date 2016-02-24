package org.dea.transcript.trp.gui.pagination_tables;

import javax.security.auth.login.LoginException;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.swt_canvas.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.pagination_tables.PageLockTablePagination;

public class PageLockTablePaginationTest {

	public static void main(String[] args) throws LoginException {
		Storage s = Storage.getInstance();
		s.login(TrpServerConn.SERVER_URIS[0], args[0], args[1]);
		
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				// getShell().setLayout(new FillLayout());
				getShell().setSize(600, 600);
				
				PageLockTablePagination w = new PageLockTablePagination(getShell(), 0, 25);
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
