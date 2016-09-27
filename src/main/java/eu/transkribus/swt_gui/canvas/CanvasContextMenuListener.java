package eu.transkribus.swt_gui.canvas;

import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableRegionType;
import eu.transkribus.swt_canvas.canvas.CanvasContextMenu;
import eu.transkribus.swt_canvas.canvas.CanvasKeys;
import eu.transkribus.swt_canvas.canvas.SWTCanvas;
import eu.transkribus.swt_canvas.canvas.editing.CanvasShapeEditor.BorderFlags;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_canvas.canvas.shapes.TableDimension;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.table_editor.TableUtils;

public class CanvasContextMenuListener implements Observer {
	private final static Logger logger = LoggerFactory.getLogger(CanvasContextMenuListener.class);
	
	TrpMainWidget mw;
	SWTCanvas canvas;
	
	public CanvasContextMenuListener(TrpMainWidget mw) {
		this.mw = mw;
		this.canvas = mw.getCanvas();
		
		mw.getCanvas().getContextMenu().addObserver(this);
	}

	@Override public void update(Observable o, Object arg) {
		try {
			ICanvasShape fs = canvas.getFirstSelected();
			ICanvasShape ls = canvas.getLastSelected();
			
			if (arg.equals(CanvasContextMenu.DELETE_ITEM_EVENT)) {
				canvas.getShapeEditor().removeShapesFromCanvas(canvas.getScene().getSelectedAsNewArray(), true);
				
			}
			else if (arg.equals(CanvasContextMenu.SELECT_TABLE_ROW_CELLS_EVENT) || arg.equals(CanvasContextMenu.SELECT_TABLE_COLUMN_CELLS_EVENT) || arg.equals(CanvasContextMenu.SELECT_TABLE_CELLS_EVENT)) {
				TrpTableCellType cell = TableUtils.getTableCell(ls);
				if (cell != null) {
					TableDimension dim = null;
					if (arg.equals(CanvasContextMenu.SELECT_TABLE_ROW_CELLS_EVENT))
						dim = TableDimension.ROW;
					else if (arg.equals(CanvasContextMenu.SELECT_TABLE_COLUMN_CELLS_EVENT))
						dim = TableDimension.COLUMN;
	
					boolean isMultiselect = canvas.getMouseListener().isKeyDown(CanvasKeys.MULTISELECTION_REQUIRED_KEY);
					TableUtils.selectCells(canvas, cell, dim, isMultiselect);
				}
			} 
			else if (arg.equals(CanvasContextMenu.DELETE_TABLE_ROW_EVENT) || arg.equals(CanvasContextMenu.DELETE_TABLE_COLUMN_EVENT)) {
				TrpTableCellType cell = TableUtils.getTableCell(fs);
				if (cell != null) {
					TableDimension dim = arg.equals(CanvasContextMenu.DELETE_TABLE_ROW_EVENT) ? TableDimension.ROW : TableDimension.COLUMN;
					logger.debug("deleting table "+dim);
					canvas.getShapeEditor().deleteTableRowOrColumn(fs, dim, true);	
				}
			}
			else if (arg.equals(CanvasContextMenu.DELETE_TABLE_EVENT)) {
				TrpTableRegionType t = TableUtils.getTable(fs, true);
				logger.debug("deleting table, t = "+t);
				if (t != null) {
					canvas.getShapeEditor().removeShapeFromCanvas((ICanvasShape) t.getData(), true);
				}
			}
			else if (arg.equals(CanvasContextMenu.SPLIT_MERGED_CELL_EVENT)) {
				canvas.getShapeEditor().splitMergedTableCell(fs, true);
			}
			else if (arg.equals(CanvasContextMenu.REMOVE_INTERMEDIATE_TABLECELL_POINTS_EVENT)) {
				canvas.getShapeEditor().removeIntermediatePointsOfTableCell(fs, true);
			}
			else if (CanvasContextMenu.isBorderEvent(arg)) {
				BorderFlags bf = CanvasContextMenu.getBorderFlags(arg);
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
