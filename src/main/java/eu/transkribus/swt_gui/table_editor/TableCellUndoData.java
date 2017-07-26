package eu.transkribus.swt_gui.table_editor;

import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;

public class TableCellUndoData {
	
	public TrpTableCellType cell;
	public int row, col, rowSpan, colSpan;
	public boolean leftBorder, rightBorder, bottomBorder, topBorder;
	
	public TableCellUndoData(TrpTableCellType cell) {
		this.cell = cell;
		
		this.row = cell.getRow();
		this.col = cell.getCol();
		this.rowSpan = cell.getRowSpan();
		this.colSpan = cell.getColSpan();
		
		this.leftBorder = cell.isLeftBorderVisible();
		this.rightBorder = cell.isRightBorderVisible();
		this.bottomBorder = cell.isBottomBorderVisible();
		this.topBorder = cell.isTopBorderVisible();
	}
	
	public boolean matches(TrpTableCellType cell) {
		return this.cell.getId().equals(cell.getId());
	}
	
}
