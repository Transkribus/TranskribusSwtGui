package eu.transkribus.swt_gui.canvas;

import org.eclipse.jface.dialogs.MessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpShapeTypeUtils;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.canvas.ICanvasContextMenuListener.FocusTableEvent;
import eu.transkribus.swt_gui.canvas.ICanvasContextMenuListener.MergeTableCellsEvent;
import eu.transkribus.swt_gui.canvas.shapes.CanvasPolygon;
import eu.transkribus.swt_gui.canvas.shapes.CanvasPolyline;
import eu.transkribus.swt_gui.canvas.shapes.CanvasShapeUtil;
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
			TrpMainWidget.getInstance().onError("Error", ex.getMessage(), ex);
		}
	}
	
	public void handleCreateDefaultLineEvent(CreateDefaultLineEvent event) {
		TrpMainWidget.getInstance().createDefaultLineForSelectedShape();
	}
	
	public void handleTableHelpEvent(TableHelpEvent event) {
		String shortCuts = ""
				+ "To add new rows or columns use the split tool\n"
				+ "To merge table cells select them and click on the merge tool\n"
				+ "ctrl + move table cell -> move table row\n"
				+ "ctrl + alt + moving table cell -> move table column\n"
				+ "ctrl + move table cell border -> move table row / cell border \n";
				
		DialogUtil.showMessageDialog(mw.getShell(), "Table help", shortCuts, Images.HELP, MessageDialog.INFORMATION, 
				new String[] {"OK"}, 0);
	}

	public void handleTableBorderEditEvent(TableBorderEditEvent event) {
		try {
			canvas.getShapeEditor().applyBorderToSelectedTableCells(canvas.getScene().getSelectedTableCellShapes(), event.borderFlags, true);
			canvas.redraw();
		} catch (Throwable ex) {
			TrpMainWidget.getInstance().onError("Error", ex.getMessage(), ex);
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
			TrpMainWidget.getInstance().onError("Error", ex.getMessage(), ex);
		}
	}

	public void handleRemoveIntermediatePointsTableEvent(RemoveIntermediatePointsTableEvent event) {
		try {
			canvas.getShapeEditor().removeIntermediatePointsOfTableCell(canvas.getFirstSelected(), true);
		} catch (Throwable ex) {
			TrpMainWidget.getInstance().onError("Error", ex.getMessage(), ex);
		}
	}

	public void handleSplitTableCellEvent(SplitTableCellEvent event) {
		try {
			canvas.getShapeEditor().splitMergedTableCell(canvas.getFirstSelected(), true);
		} catch (Throwable ex) {
			TrpMainWidget.getInstance().onError("Error", ex.getMessage(), ex);
		}
	}

	public void handleSelectTableCellsEvent(SelectTableCellsEvent event) {
		try {
			ICanvasShape ls = canvas.getLastSelected();
			
			TrpTableCellType cell = TableUtils.getTableCell(ls);
			if (cell != null) {
				boolean isMultiselect = CanvasKeys.isCtrlOrCommandKeyDown(canvas.getMouseListener().getCurrentMoveStateMask());
				TableUtils.selectCells(canvas, cell, event.dim, isMultiselect);
			}
		} catch (Throwable ex) {
			TrpMainWidget.getInstance().onError("Error", ex.getMessage(), ex);
		}
	}
	
	public void handleFocusTableEvent(FocusTableEvent event) {
		try {
			ICanvasShape ls = canvas.getLastSelected();
			
			TrpTableCellType cell = TableUtils.getTableCell(ls);
			if (cell != null) {
				canvas.getScene().selectObjectWithData(cell.getTable(), true, false);
				canvas.focusFirstSelected();
			}
		} catch (Throwable ex) {
			TrpMainWidget.getInstance().onError("Error", ex.getMessage(), ex);
		}		
	}
	
	public void handleMergeTableCellsEvent(MergeTableCellsEvent event) {
		try {
			canvas.getShapeEditor().mergeSelected();
		} catch (Throwable ex) {
			TrpMainWidget.getInstance().onError("Error", ex.getMessage(), ex);
		}
		
	}
	
	public void handleSetStructureEvent(SetStructureEvent event) {
		TrpMainWidget.getInstance().setStructureTypeOfSelected(event.st.getType(), false);
	}
}
