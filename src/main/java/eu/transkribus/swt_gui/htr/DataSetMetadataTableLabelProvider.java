package eu.transkribus.swt_gui.htr;

import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.TableLabelProvider;

public class DataSetMetadataTableLabelProvider extends TableLabelProvider implements ITableFontProvider {
	
	public static final String TOTAL_ROW_LABEL = "Total";
	
	Table table;
	TableViewer tableViewer;
//	private final Font defaultFont;
	private final Font boldFont;
	
	public DataSetMetadataTableLabelProvider(TableViewer tableViewer) {
		this.tableViewer = tableViewer;
		this.table = tableViewer.getTable();
//		defaultFont = tableViewer.getControl().getFont();
		boldFont = Fonts.createBoldFont(tableViewer.getControl().getFont());
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof DataSetMetadata) {
			DataSetMetadata d = (DataSetMetadata) element;
			
			TableColumn column = table.getColumn(columnIndex);
			String ct = column.getText();
			
			if (ct.equals(DataSetMetadataTableWidget.LABEL_COL)) {
				return d.getLabel();
			} else if (ct.equals(DataSetMetadataTableWidget.PAGES_COL)) {
				return "" + d.getPages();
			} else if (ct.equals(DataSetMetadataTableWidget.LINES_COL)) {
				return "" + d.getLines();
			} else if (ct.equals(DataSetMetadataTableWidget.WORDS_COL)) {
				return "" + d.getWords();
			}
		}
		return "i am error";
	}

	@Override
	public Font getFont(Object element, int columnIndex) {
		if (element instanceof DataSetMetadata 
				&& TOTAL_ROW_LABEL.equals(((DataSetMetadata) element).getLabel())) {
			return boldFont;
		}
		return null;
	}
}
