package eu.transkribus.swt_gui.canvas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.swt_gui.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.table_editor.TableUtils;

public class CanvasContextMenuListener implements ICanvasContextMenuListener {
	private final static Logger logger = LoggerFactory.getLogger(CanvasContextMenuListener.class);
	
	TrpMainWidget mw;
	SWTCanvas canvas;
	
	public CanvasContextMenuListener(TrpMainWidget mw) {
		this.mw = mw;
		this.canvas = mw.getCanvas();
		
		mw.getCanvas().getContextMenu().addListener(this);
	}
	
	public void handleDeleteItemEvent(DeleteItemEvent event) {
		try {
			canvas.getShapeEditor().removeShapesFromCanvas(canvas.getScene().getSelectedAsNewArray(), true);
		} catch (Throwable ex) {
			canvas.getMainWidget().onError("Error", ex.getMessage(), ex);
		}
	}
	
	public void handleTableHelpEvent(TableHelpEvent event) {
		
	}

	public void handleTableBorderEditEvent(TableBorderEditEvent event) {
		try {
			canvas.getShapeEditor().applyBorderToSelectedTableCells(canvas.getScene().getSelectedTableCellShapes(), event.borderFlags, true);
			canvas.redraw();
		} catch (Throwable ex) {
			canvas.getMainWidget().onError("Error", ex.getMessage(), ex);
		}
	}

	public void handleDeleteTableEvent(DeleteTableEvent event) {
		try {
			ICanvasShape fs = canvas.getFirstSelected();
			TrpTableCellType cell = TableUtils.getTableCell(fs);
			if (cell != null) {
				logger.debug("deleting table "+event.dim);
				canvas.getShapeEditor().deleteTableRowOrColumn(fs, event.dim, true);	
			}
		} catch (Throwable ex) {
			canvas.getMainWidget().onError("Error", ex.getMessage(), ex);
		}
	}

	public void handleRemoveIntermediatePointsTableEvent(RemoveIntermediatePointsTableEvent event) {
		try {
			canvas.getShapeEditor().removeIntermediatePointsOfTableCell(canvas.getFirstSelected(), true);
		} catch (Throwable ex) {
			canvas.getMainWidget().onError("Error", ex.getMessage(), ex);
		}
	}

	public void handleSplitTableCellEvent(SplitTableCellEvent event) {
		try {
			canvas.getShapeEditor().splitMergedTableCell(canvas.getFirstSelected(), true);
		} catch (Throwable ex) {
			canvas.getMainWidget().onError("Error", ex.getMessage(), ex);
		}
	}

	public void handleSelectTableCellsEvent(SelectTableCellsEvent event) {
		try {
			ICanvasShape ls = canvas.getLastSelected();
			
			TrpTableCellType cell = TableUtils.getTableCell(ls);
			if (cell != null) {
				boolean isMultiselect = canvas.getMouseListener().isKeyDown(CanvasKeys.MULTISELECTION_REQUIRED_KEY);
				TableUtils.selectCells(canvas, cell, event.dim, isMultiselect);
			}
		} catch (Throwable ex) {
			canvas.getMainWidget().onError("Error", ex.getMessage(), ex);
		}
	}
}
