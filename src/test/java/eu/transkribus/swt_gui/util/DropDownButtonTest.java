package eu.transkribus.swt_gui.util;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.swt.util.SWTUtil;

public class DropDownButtonTest {

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
//				CollectionTableComboViewerWidget c = new CollectionTableComboViewerWidget(parent, 0, true, false, false);
//				c.setAvailableCollections(createTestCollections());
				
//				DropDownButton b = new DropDownButton(parent, 0, "adadsf", null);
				
				Combo c = new Combo(parent, SWT.DROP_DOWN);
				c.setText("adsfasdf");
				
				c.setVisibleItemCount(0);
				c.setListVisible(false);
				
				c.addVerifyListener(new VerifyListener() {
					
					@Override
					public void verifyText(VerifyEvent e) {
						e.doit = false;
					}
				});
				
				c.addModifyListener(new ModifyListener() {
					
					@Override
					public void modifyText(ModifyEvent e) {
					}
				});
				
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
