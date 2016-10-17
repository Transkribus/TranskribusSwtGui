package eu.transkribus.swt_gui.util;

import javax.security.auth.login.LoginException;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.util.RecentDocsPreferences;

public class RecentDocsComboViewerWidgetTest {

	public static void main(String[] args) throws LoginException {		
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				// getShell().setLayout(new FillLayout());
//				getShell().setSize(600, 600);
				
				RecentDocsPreferences.init();
				
//				CollectionComboViewerWidget c = new CollectionComboViewerWidget(parent, 0, false, true, true);
				RecentDocsComboViewerWidget c = new RecentDocsComboViewerWidget(parent, 0);
//				c.set
				
				getShell().setSize(500, 200);

				SWTUtil.centerShell(getShell());

				return parent;
			}
		};
		aw.setBlockOnOpen(true);
		aw.open();

		Display.getCurrent().dispose();
	}

}
