package eu.transkribus.swt_gui.collection_comboviewer;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.swt.util.SWTUtil;

public class CollectionTableComboViewerWidgetTest {

	static List<TrpCollection> createTestCollections() {
		int N = 10000;
		
		List<TrpCollection> colls = new ArrayList<>();
		for (int i=0; i<N; ++i) {
			TrpCollection c = new TrpCollection(i, "coll-"+i, "i am coll "+i);
			colls.add(c);
		}
		
		return colls;
	}

	public static void main(String[] args) throws LoginException {		
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				// getShell().setLayout(new FillLayout());
//				getShell().setSize(600, 600);
				
//				CollectionComboViewerWidget c = new CollectionComboViewerWidget(parent, 0, false, true, true);
				CollectionTableComboViewerWidget c = new CollectionTableComboViewerWidget(parent, 0, true, false, false);
				c.setAvailableCollections(createTestCollections());
				
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
