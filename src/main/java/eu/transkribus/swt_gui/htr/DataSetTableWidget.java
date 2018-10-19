package eu.transkribus.swt_gui.htr;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt_gui.htr.TreeViewerDataSetSelectionSashForm.DataSetEntry;

public class DataSetTableWidget extends Composite {
//	private static final Logger logger = LoggerFactory.getLogger(DataSetTableWidget.class);

	public static final String TITLE_COL = "Title";
	public static final String ID_COL = "ID";
	public static final String PAGES_COL = "Pages";

	// make sure you dispose these buttons when viewer input changes
	// Map<Object, Button> buttons = new HashMap<Object, Button>();
	// private final static GridData BUTTON_GD = new GridData(SWT.FILL,
	// SWT.FILL, true, true);

	MyTableViewer tv;

	public final ColumnConfig[] COLS = new ColumnConfig[] {
			new ColumnConfig(ID_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(TITLE_COL, 250, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(PAGES_COL, 100, false, DefaultTableColumnViewerSorter.ASC) };

	public DataSetTableWidget(Composite parent, int style) {
		super(parent, style);

		this.setLayout(new FillLayout());

		tv = new MyTableViewer(this, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		tv.setContentProvider(new ArrayContentProvider());
		tv.setLabelProvider(new DataSetTableLabelProvider(tv));

		Table table = tv.getTable();
		table.setHeaderVisible(true);

		// =========== Button in column does not work....
		// ===========================
		// TableColumn column = new TableColumn(tv.getTable(), SWT.NONE);
		// // column.setText("");
		// column.setWidth(30);
		// TableViewerColumn removeCol = new TableViewerColumn(tv, column);
		// removeCol.getColumn().setResizable(false);
		// removeCol.setLabelProvider(new ColumnLabelProvider() {
		//
		// @Override
		// public void update(ViewerCell cell) {
		//
		// TableItem item = (TableItem) cell.getItem();
		// Button button;
		// if (buttons.containsKey(cell.getElement())) {
		// button = buttons.get(cell.getElement());
		// } else {
		// button = new Button((Composite) cell.getViewerRow().getControl(),
		// SWT.NONE);
		// button.setLayoutData(BUTTON_GD);
		//// button.setText("Remove");
		// button.setImage(Images.CROSS);
		// button.pack();
		// buttons.put(cell.getElement(), button);
		// }
		// TableEditor editor = new TableEditor(item.getParent());
		// editor.grabHorizontal = true;
		// editor.grabVertical = true;
		// editor.setEditor(button, item, cell.getColumnIndex());
		// editor.layout();
		// }
		//
		// });
		// =================== END ===================
		tv.addColumns(COLS);
	}

	public void setInput(Object input) {
		// for(Entry<Object, Button> e : buttons.entrySet()) {
		// e.getValue().dispose();
		// }
		tv.setInput(input);
	}

	public List<DataSetEntry> getSelectedDataSets() {
		IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
		if (!sel.isEmpty()) {
			return (List<DataSetEntry>)sel.toList();
		} else {
			return new ArrayList<DataSetEntry>(0);
		}
	}
}