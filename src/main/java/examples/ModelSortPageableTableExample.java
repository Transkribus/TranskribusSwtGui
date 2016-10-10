package examples;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.nebula.widgets.pagination.collections.PageResultContentProvider;
import org.eclipse.nebula.widgets.pagination.collections.PageResultLoaderList;
import org.eclipse.nebula.widgets.pagination.table.PageableTable;
import org.eclipse.nebula.widgets.pagination.table.SortTableColumnSelectionListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import eu.transkribus.swt.pagination_table.PagingToolBarNavigationRendererFactory;

/**
 * This sample display a list of model {@link Person} in a SWT Table with
 * pagination banner displayed with Page Results+Page Links on the top of the
 * SWT Table. The 2 columns which display the list of {@link Person} can be
 * clicked to sort the paginated list.
 * 
 */
public class ModelSortPageableTableExample {

	public static void main(String[] args) {

		Display display = new Display();
		Shell shell = new Shell(display);
		GridLayout layout = new GridLayout(1, false);
		shell.setLayout(layout);

		final List<Person> items = createList();

		// 1) Create pageable table with 10 items per page
		// This SWT Component create internally a SWT Table+JFace TreeViewer
		int pageSize = 10;
//		PageableTable pageableTable = new PageableTable(shell, SWT.BORDER,
//				SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, pageSize);
		
		PageableTable pageableTable = new PageableTable(shell, SWT.BORDER, SWT.BORDER | SWT.MULTI /*| SWT.H_SCROLL | SWT.V_SCROLL*/, pageSize
				, PageResultContentProvider.getInstance(),
				PagingToolBarNavigationRendererFactory.getFactory(),
				PageableTable.getDefaultPageRendererBottomFactory()
				);

//		pageablteTable.		
		
//		PagingToolBarNavigationRendererFactory.getFactory(),
//		PageableTable.getDefaultPageRendererBottomFactory()
		
		
		pageableTable.setLayoutData(new GridData(GridData.FILL_BOTH));

		// 2) Initialize the table viewer + SWT Table
		TableViewer viewer = pageableTable.getViewer();
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new LabelProvider());

		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		// 3) Create Table columns with sort of paginated list.
		createColumns(viewer);

		// 3) Set current page to 0 to refresh the table
		pageableTable.setPageLoader(new PageResultLoaderList<Person>(items));
		pageableTable.setCurrentPage(0);

		shell.setSize(400, 250);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private static void createColumns(final TableViewer viewer) {

		// First column is for the first name
		TableViewerColumn col = createTableViewerColumn(viewer, "Name", 150);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Person p = (Person) element;
				return p.getName();
			}
		});
		col.getColumn().addSelectionListener(
				new SortTableColumnSelectionListener("name"));

		// Second column is for the adress
		col = createTableViewerColumn(viewer, "Adress", 150);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Person p = (Person) element;
				Address address = p.getAddress();
				if (address == null) {
					return "";
				}
				return address.getName();
			}
		});
		col.getColumn().addSelectionListener(
				new SortTableColumnSelectionListener("address.name"));
	}

	private static List<Person> createList() {
		List<Person> names = new ArrayList<Person>();
		for (int i = 1; i < 2012; i++) {
			names.add(new Person("Name " + i, i < 100 ? "Adress "
					+ Math.random() : null));
		}
		return names;
	}

	private static TableViewerColumn createTableViewerColumn(
			TableViewer viewer, String title, int bound) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer,
				SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

}
