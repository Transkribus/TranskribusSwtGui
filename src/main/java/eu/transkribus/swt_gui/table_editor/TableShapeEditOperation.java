package eu.transkribus.swt_gui.table_editor;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableRegionType;
import eu.transkribus.swt_canvas.canvas.editing.ShapeEditOperation;

public class TableShapeEditOperation extends ShapeEditOperation {
	private final static Logger logger = LoggerFactory.getLogger(TableShapeEditOperation.class);
	
	List<TableCellUndoData> cellBackupData = new ArrayList<>();

	public TableShapeEditOperation(String description) {
		super(ShapeEditType.CUSTOM, description);		
	}
	
	@Override protected void customUndoOperation() {
		recoverTableCellValues(cellBackupData);
	}
	
	public void addCellBackup(TrpTableCellType cell) {
		cellBackupData.add(new TableCellUndoData(cell));
	}
	
	public List<TableCellUndoData> getCellBackupData() { 
		return cellBackupData;
	}
	
	public static void recoverTableCellValues(TrpTableCellType cell, TableCellUndoData backup) {
		cell.setRow(backup.row);
		cell.setCol(backup.col);
		cell.setRowSpan(backup.rowSpan);
		cell.setColSpan(backup.colSpan);
	}
	
	public static void recoverTableCellValues(/*TrpTableRegionType table, */List<TableCellUndoData> backup) {
		if (backup==null || backup.isEmpty())
			return;
		
		TrpTableRegionType table = backup.get(0).cell.getTable();
		
		for (TableCellUndoData b : backup) {
			for (TrpTableCellType c : table.getTrpTableCell()) {
				if (b.matches(c)) {
//					logger.debug("match: "+c+" - "+b);
					recoverTableCellValues(c, b);
					
					break;
				}		
			}
		}
	}

}
