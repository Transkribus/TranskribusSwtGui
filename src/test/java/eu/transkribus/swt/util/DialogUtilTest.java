package eu.transkribus.swt.util;

import static org.junit.Assert.*;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.junit.Test;

import eu.transkribus.swt_gui.util.RecentDocsComboViewerWidget;
import eu.transkribus.util.RecentDocsPreferences;

public class DialogUtilTest {

	@Test
	public void test() {
		fail("Not yet implemented");
	}
	
	public static void main(String[] args) {
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				
				
				
				Exception e = new Exception("test exception");
				
				int r = DialogUtil.showDetailedErrorMessageBox(parent.getShell(), "titleee", "messsage",  e);
				System.out.println(r);
				
//				// getShell().setLayout(new FillLayout());
////				getShell().setSize(600, 600);
//				
//				RecentDocsPreferences.init();
//				
////				CollectionComboViewerWidget c = new CollectionComboViewerWidget(parent, 0, false, true, true);
//				RecentDocsComboViewerWidget c = new RecentDocsComboViewerWidget(parent, 0);
////				c.set
//				
//				getShell().setSize(500, 200);
//
//				SWTUtil.centerShell(getShell());

				return parent;
			}
		};
		aw.setBlockOnOpen(true);
		aw.open();

		Display.getCurrent().dispose();
	}

}
