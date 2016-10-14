package eu.transkribus.swt.pagination_table;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.junit.Test;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.pagination_tables.PageLockTablePagination;
import examples.Person;

public class ATableWidgetPaginationTest {
	
	private static List<Person> createList() {
		List<Person> names = new ArrayList<Person>();
		for (int i = 1; i < 2012; i++) {
			names.add(new Person("Name " + i, i < 100 ? "Adress "
					+ Math.random() : null));
		}
		return names;
	}
	
	static class TableWidgetPaginationTest extends ATableWidgetPagination<Person> {

		public TableWidgetPaginationTest(Composite parent, int style, int initialPageSize) {
			super(parent, style, initialPageSize);
		}

		@Override protected void setPageLoader() {
		}

		@Override protected void createColumns() {
		}

	}

	public static void main(String[] args) throws LoginException {
		Storage s = Storage.getInstance();
		s.login(TrpServerConn.SERVER_URIS[0], args[0], args[1]);
		
		final List<Person> items = createList();
		
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
