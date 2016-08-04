package eu.transkribus.swt_gui.canvas;

import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableRegionType;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_canvas.canvas.shapes.TableDimension;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.table_editor.TableUtils;

public class TrpCanvasContextMenuListener implements Observer {
	private final static Logger logger = LoggerFactory.getLogger(TrpCanvasContextMenuListener.class);
	
	TrpMainWidget mw;
	TrpSWTCanvas canvas;
	
	public TrpCanvasContextMenuListener(TrpMainWidget mw) {
		this.mw = mw;
		this.canvas = mw.getCanvas();
		
		mw.getCanvas().getContextMenu().addObserver(this);
	}

	@Override public void update(Observable o, Object arg) {
		if (arg.equals(TrpCanvasContextMenu.DELETE_ITEM_EVENT)) {
			canvas.getShapeEditor().removeShapesFromCanvas(canvas.getScene().getSelectedAsNewArray(), true);
			
		} 
		else if (arg.equals(TrpCanvasContextMenu.SELECT_TABLE_ROW_CELLS_EVENT) || arg.equals(TrpCanvasContextMenu.SELECT_TABLE_COLUMN_CELLS_EVENT)) {
			TrpTableCellType cell = TableUtils.getTableCell(canvas.getFirstSelected());
			if (cell != null) {
				TableDimension dim = arg.equals(TrpCanvasContextMenu.SELECT_TABLE_ROW_CELLS_EVENT) ? TableDimension.ROW : TableDimension.COLUMN;
				TableUtils.selectCells(canvas, cell, dim);	
			}
		} 
		else if (arg.equals(TrpCanvasContextMenu.DELETE_TABLE_ROW_EVENT) || arg.equals(TrpCanvasContextMenu.DELETE_TABLE_COLUMN_EVENT)) {
			TrpTableCellType cell = TableUtils.getTableCell(canvas.getFirstSelected());
			if (cell != null) {
				TableDimension dim = arg.equals(TrpCanvasContextMenu.DELETE_TABLE_ROW_EVENT) ? TableDimension.ROW : TableDimension.COLUMN;
				canvas.getShapeEditor().deleteTableRowOrColumn(canvas.getFirstSelected(), dim, true);	
			}
		}
		else if (arg.equals(TrpCanvasContextMenu.DELETE_TABLE_EVENT)) {
			TrpTableRegionType t = TableUtils.getTable(canvas.getFirstSelected(), true);
			logger.debug("deleting table, t = "+t);
			if (t != null) {
				canvas.getShapeEditor().removeShapeFromCanvas((ICanvasShape) t.getData(), true);
			}
		}
	}
}
