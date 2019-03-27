package eu.transkribus.swt_gui.htr;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;

/**
 * Table that displays a list of DataSetMetadata elements and a total sum in bold at the bottom. 
 *
 */
public class DataSetMetadataTableWidget extends Composite {
//	private static final Logger logger = LoggerFactory.getLogger(DataSetTableWidget.class);

	public static final String LABEL_COL = "Data Type";
	public static final String PAGES_COL = "Pages";
	public static final String LINES_COL = "Lines";
	public static final String WORDS_COL = "Words";

	// make sure you dispose these buttons when viewer input changes
	// Map<Object, Button> buttons = new HashMap<Object, Button>();
	// private final static GridData BUTTON_GD = new GridData(SWT.FILL,
	// SWT.FILL, true, true);

	protected MyTableViewer tv;
	
	public final ColumnConfig[] COLS = new ColumnConfig[] {
		new ColumnConfig(LABEL_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(PAGES_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(LINES_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(WORDS_COL, 50, false, DefaultTableColumnViewerSorter.ASC) 
	};

	public DataSetMetadataTableWidget(Composite parent, int style) {
		super(parent, style);

		this.setLayout(new FillLayout());

		tv = new MyTableViewer(this, SWT.NONE);
		tv.setContentProvider(new ArrayContentProvider());
		tv.setLabelProvider(new DataSetMetadataTableLabelProvider(tv));

		Table table = tv.getTable();
		table.setHeaderVisible(true);

		tv.addColumns(COLS);
	}

	/**
	 * Set the input data for the table. If the list is larger than 1, a total is computed and displayed at the bottom.
	 * 
	 * @param input
	 */
	public void setInput(List<DataSetMetadata> input) {
		if(input == null || input.size() <= 1) {
			tv.setInput(input);
			return;
		}
		//compute a total and add it to the data
		List<DataSetMetadata> enrichedInput = new ArrayList<>(input);
		enrichedInput.add(calculateTotalSize(input));
		tv.setInput(enrichedInput);
	}
	
	private DataSetMetadata calculateTotalSize(List<DataSetMetadata> mds) {
		final String name = DataSetMetadataTableLabelProvider.TOTAL_ROW_LABEL;
		if(mds == null || mds.size() == 0) {
			return new DataSetMetadata(name, 0, 0, 0);
		}
		int pages = 0, lines = 0, words = 0;
		for(DataSetMetadata m : mds) {
			pages += m.getPages();
			lines += m.getLines();
			words += m.getWords();
		}
		return new DataSetMetadata(name, pages, lines, words);
	}		
}