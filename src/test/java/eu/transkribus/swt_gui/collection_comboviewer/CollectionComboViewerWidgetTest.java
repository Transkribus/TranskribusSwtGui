package eu.transkribus.swt_gui.collection_comboviewer;

import javax.security.auth.login.LoginException;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import eu.transkribus.swt.util.SWTUtil;

public class CollectionComboViewerWidgetTest {

	public static void main(String[] args) throws LoginException {		
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				// getShell().setLayout(new FillLayout());
//				getShell().setSize(600, 600);
				
//				CollectionComboViewerWidget c = new CollectionComboViewerWidget(parent, 0, false, true, true);
				CollectionComboViewerWidget c = new CollectionComboViewerWidget(parent, 0, true, false, false);
				
//				InstallSpecificVersionDialog d = new InstallSpecificVersionDialog(getShell(), 0);
//				d.open();
				
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
