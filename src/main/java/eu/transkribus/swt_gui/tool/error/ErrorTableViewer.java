package eu.transkribus.swt_gui.tool.error;

import org.eclipse.swt.widgets.Composite;

import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;

public class ErrorTableViewer extends MyTableViewer {

	public static final String ERR_PAGE_COL = "Page";
	public static final String ERR_WORD_COL = "Word Error Rate ";
	public static final String ERR_CHAR_COL = "Char Error Rate";
	public static final String ACC_WORD_COL = "Word Accuracy";
	public static final String ACC_CHAR_COL = "Char Accuracy";
	public static final String BAG_PREC_COL = "Bag Tokens Precision";
	public static final String BAG_REC_COL = "Bag Tokens Recall";
	public static final String BAG_FMEA_COL = "Bag Tokens F-Measure";
	
	
	
	public final ColumnConfig[] ERR_COLS = new ColumnConfig[] {};
	
	/*
	 * FIXME ErrorTableColumnViewerSorter type is missing!
	
	public final ColumnConfig[] ERR_COLS = new ColumnConfig[] { 
			new ColumnConfig(ERR_PAGE_COL, 100,false, ErrorTableColumnViewerSorter.ASC),
			new ColumnConfig(ERR_WORD_COL, 150,false,ErrorTableColumnViewerSorter.ASC),
			new ColumnConfig(ERR_CHAR_COL, 150,false,ErrorTableColumnViewerSorter.ASC),
			new ColumnConfig(ACC_WORD_COL, 150,false,ErrorTableColumnViewerSorter.ASC ),
			new ColumnConfig(ACC_CHAR_COL, 150,false,ErrorTableColumnViewerSorter.ASC ),
			new ColumnConfig(BAG_PREC_COL, 150,false,ErrorTableColumnViewerSorter.ASC ),
			new ColumnConfig(BAG_REC_COL, 150,false,ErrorTableColumnViewerSorter.ASC ),
			new ColumnConfig(BAG_FMEA_COL, 150,false,ErrorTableColumnViewerSorter.ASC ) };
	 */
	
	public ErrorTableViewer(Composite parent, int style) {
		super(parent, style);
		this.addColumns(ERR_COLS);

		
	}

}	
	
