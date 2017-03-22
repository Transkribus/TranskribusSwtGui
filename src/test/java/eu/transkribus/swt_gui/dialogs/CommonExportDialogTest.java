package eu.transkribus.swt_gui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.swt.util.SWTUtil;

public class CommonExportDialogTest {
	
	public static void main(String[] args) {
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				getShell().setSize(600, 600);
				SWTUtil.centerShell(getShell());
				
				List<TrpPage> pages = new ArrayList<>();
				CommonExportDialog ced = new CommonExportDialog(getShell(), 0, null, "docName", pages);
				ced.open();

				return parent;
			}
		};
		aw.setBlockOnOpen(true);
		aw.open();

		Display.getCurrent().dispose();
		
		
	}

}
