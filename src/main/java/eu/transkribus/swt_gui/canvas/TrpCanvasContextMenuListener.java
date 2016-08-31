package eu.transkribus.swt_gui.canvas;

import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableRegionType;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_canvas.canvas.shapes.TableDimension;
import eu.transkribus.swt_gui.canvas.TrpCanvasShapeEditor.BorderFlags;
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
		try {
			ICanvasShape s = canvas.getFirstSelected();
			
			if (arg.equals(TrpCanvasContextMenu.DELETE_ITEM_EVENT)) {
				canvas.getShapeEditor().removeShapesFromCanvas(canvas.getScene().getSelectedAsNewArray(), true);
				
			}
			else if (arg.equals(TrpCanvasContextMenu.SELECT_TABLE_ROW_CELLS_EVENT) || arg.equals(TrpCanvasContextMenu.SELECT_TABLE_COLUMN_CELLS_EVENT) || arg.equals(TrpCanvasContextMenu.SELECT_TABLE_CELLS_EVENT)) {
				TrpTableCellType cell = TableUtils.getTableCell(s);
				if (cell != null) {
					TableDimension dim = null;
					if (arg.equals(TrpCanvasContextMenu.SELECT_TABLE_ROW_CELLS_EVENT))
						dim = TableDimension.ROW;
					else if (arg.equals(TrpCanvasContextMenu.SELECT_TABLE_COLUMN_CELLS_EVENT))
						dim = TableDimension.COLUMN;
					
					TableUtils.selectCells(canvas, cell, dim);
				}
			} 
			else if (arg.equals(TrpCanvasContextMenu.DELETE_TABLE_ROW_EVENT) || arg.equals(TrpCanvasContextMenu.DELETE_TABLE_COLUMN_EVENT)) {
				TrpTableCellType cell = TableUtils.getTableCell(s);
				if (cell != null) {
					TableDimension dim = arg.equals(TrpCanvasContextMenu.DELETE_TABLE_ROW_EVENT) ? TableDimension.ROW : TableDimension.COLUMN;
					logger.debug("deleting table "+dim);
					canvas.getShapeEditor().deleteTableRowOrColumn(s, dim, true);	
				}
			}
			else if (arg.equals(TrpCanvasContextMenu.DELETE_TABLE_EVENT)) {
				TrpTableRegionType t = TableUtils.getTable(s, true);
				logger.debug("deleting table, t = "+t);
				if (t != null) {
					canvas.getShapeEditor().removeShapeFromCanvas((ICanvasShape) t.getData(), true);
				}
			}
			else if (arg.equals(TrpCanvasContextMenu.SPLIT_MERGED_CELL_EVENT)) {
				canvas.getShapeEditor().splitMergedTableCell(s, true);
			}
			else if (arg.equals(TrpCanvasContextMenu.REMOVE_INTERMEDIATE_TABLECELL_POINTS_EVENT)) {
				canvas.getShapeEditor().removeIntermediatePointsOfTableCell(s, true);
			}
			else if (TrpCanvasContextMenu.isBorderEvent(arg)) {
				BorderFlags bf = TrpCanvasContextMenu.getBorderFlags(arg);
				if (bf == null)
					return;
				
				canvas.getShapeEditor().applyBorderToSelectedTableCells(canvas.getScene().getSelectedTableCellShapes(), bf, true);
				canvas.redraw();
			}
		} catch (Throwable ex) {
			canvas.getMainWidget().onError("Error", ex.getMessage(), ex);
		}
	}
}
