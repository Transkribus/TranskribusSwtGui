package eu.transkribus.swt_gui.canvas;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent.TableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableRegionType;
import eu.transkribus.swt_canvas.canvas.editing.CanvasShapeEditor;
import eu.transkribus.swt_canvas.canvas.editing.ShapeEditOperation;
import eu.transkribus.swt_canvas.canvas.shapes.CanvasQuadPolygon;
import eu.transkribus.swt_canvas.canvas.shapes.CanvasShapeType;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_canvas.canvas.shapes.SplitDirection;

///**
// * @deprecated not used currently
// */
public class TrpCanvasShapeEditor extends CanvasShapeEditor {
	private final static Logger logger = LoggerFactory.getLogger(TrpCanvasShapeEditor.class);

	public TrpCanvasShapeEditor(TrpSWTCanvas canvas) {
		super(canvas);
	}
	
	@Override protected ICanvasShape constructShapeFromPoints(List<java.awt.Point> pts, CanvasShapeType shapeType) {
		if (canvas.getMode() == TrpCanvasAddMode.ADD_TABLECELL) {
			// assume table cell is drawn as rectangle
			List<java.awt.Point> polyPts = new ArrayList<>();
			polyPts.add(pts.get(0));
			polyPts.add(new java.awt.Point(pts.get(0).x, pts.get(1).y));
			polyPts.add(pts.get(1));
			polyPts.add(new java.awt.Point(pts.get(1).x, pts.get(0).y));
				
			return new CanvasQuadPolygon(polyPts);
		} else {
			return super.constructShapeFromPoints(pts, shapeType);
		}
	}
	
	private List<ShapeEditOperation> splitTrpBaselineType(int x1, int y1, int x2, int y2, ICanvasShape selected, TrpBaselineType bl) {
//		TrpBaselineType bl = (TrpBaselineType) selected.getData();
		scene.selectObjectWithData(bl.getLine(), false, false);
		logger.debug("selected = "+canvas.getFirstSelected());
		
//		logger.debug("Parent = "+selected.getParent()); // IS NULL...			
//		scene.selectObject(selected.getParent(), false, false);

		List<ShapeEditOperation> splitOps = super.splitShape(selected, x1, y1, x2, y2, null, true);

		// try to select first split of baseline:
		logger.debug("trying to select left baseline split, nr of ops = "+splitOps.size());
		if (splitOps != null) {
			for (ShapeEditOperation o : splitOps) {
				if (!o.getShapes().isEmpty() && o.getShapes().get(0).getData() instanceof TrpBaselineType) {
					if (!o.getNewShapes().isEmpty()) {
						logger.debug("found left baseline split - selecting: "+o.getNewShapes().get(0));
//						scene.selectObjectWithData(o.getNewShapes().get(0), true, false);
						scene.selectObject(o.getNewShapes().get(0), true, false);
						break;
					}
				}
			}
		}
		
		return splitOps;
	}
	
	private Pair<SplitDirection, List<TrpTableCellType>> getSplittableCells(int x1, int y1, int x2, int y2, TrpTableRegionType table) {
		Pair<Integer, Integer> maxRowCol = table.getMaxRowCol();
		
		int nRows = maxRowCol.getLeft() + 1;
		int nCols = maxRowCol.getRight() + 1;
		
		List<TrpTableCellType> splittableCells=null;
		SplitDirection dir = null;
		for (int j=0; j<2; ++j) {
			int N = j==0 ? nRows : nCols;
			
			for (int i=0; i<N; ++i) {
				List<TrpTableCellType> cells = table.getCells(j==0, i);
				dir = j==0 ? SplitDirection.VERTICAL : SplitDirection.HORIZONAL;
				
				logger.debug("cells i = "+i+" first cell: "+cells.get(0));
				
				for (TrpTableCellType c : cells) {
					CanvasQuadPolygon qp = (CanvasQuadPolygon) c.getData();
					
					if (qp.computeSplitPoints(x1, y1, x2, y2, true, dir) == null) {
						logger.debug("cells not splittable in dir = "+dir);
						cells = null;
						break;
					}
				}
				
				if (cells != null) {
					splittableCells = cells;
					break;				
				}
			}
			
			if (splittableCells != null) {
				break;				
			}
		}
		
		if (splittableCells == null)
			return null;
		else
			return Pair.of(dir, splittableCells);
	}
	
	private List<ShapeEditOperation> splitTrpTableType(int x1, int y1, int x2, int y2, TrpTableRegionType table) {	
		// search for row / col cells to split:
		Pair<SplitDirection, List<TrpTableCellType>> splittableCells = getSplittableCells(x1, y1, x2, y2, table);
		if (splittableCells == null) {
			logger.debug("cells not splittable in this direction!");
			return null;
		}
		
		SplitDirection dir = splittableCells.getLeft();
		logger.debug("n-splittableCells: "+splittableCells.getRight().size());
		// FIXME??ß
		
		List<ShapeEditOperation> splitOps = new ArrayList<>();
		for (TrpTableCellType c : splittableCells.getRight()) {
			List<ShapeEditOperation> splitOps4Cell = super.splitShape((ICanvasShape) c.getData(), x1, y1, x2, y2, dir, false);
			for (ShapeEditOperation op : splitOps4Cell) {
				op.data = dir;
			}
			
			splitOps.addAll(splitOps4Cell);
		}
		
		// add to undo stack:
		if (!splitOps.isEmpty())
			addToUndoStack(splitOps);
		
		// adjust indexes on table:
		int insertIndex = dir==SplitDirection.HORIZONAL ? splittableCells.getRight().get(0).getCol() : splittableCells.getRight().get(0).getRow();
//		boolean isRowInserted = dir==SplitDirection.VERTICAL;
		
//		table.adjustCellIndexesOnRowOrColInsert(insertIndex, isRowInserted);
				
		for (ShapeEditOperation op : splitOps) {
			logger.debug("t-op = "+op);
//			op.getFirstShape();
//			TrpTableCellType tc1 = (TrpTableCellType) op.getNewShapes().get(0).getData();
			TrpTableCellType tc2 = (TrpTableCellType) op.getNewShapes().get(1).getData();
			logger.debug("tc2 = "+tc2);
			
			if (dir == SplitDirection.HORIZONAL) {
				tc2.setCol(-1);
			} else {
				tc2.setRow(-1);
			}
//			op.data = splittableCells.getLeft();
		}
		
		for (TableCellType tc : table.getTableCell()) {
			logger.debug("tc: "+tc);
			
			if (dir == SplitDirection.HORIZONAL) {
				if (tc.getCol() > insertIndex) {
					tc.setCol(tc.getCol()+1);
				} else if (tc.getCol() == -1) {
					logger.debug("here1 "+insertIndex);
					tc.setCol(insertIndex+1);
				}
			} else {
				if (tc.getRow() > insertIndex) {
					tc.setRow(tc.getRow()+1);
				} else if (tc.getRow() == -1) {
					logger.debug("here2 "+insertIndex);
					tc.setRow(insertIndex+1);
				}	
			}
		}

		return splitOps;
	}
	
	@Override public List<ShapeEditOperation> splitShape(ICanvasShape shape, int x1, int y1, int x2, int y2, Object data, boolean addToUndoStack) {		
//		ICanvasShape selected = canvas.getFirstSelected();
		if (shape == null) {
			logger.warn("Cannot split - no shape selected!");
			return null;
		}
		
		// if this is a baseline, select parent line and split it, s.t. undlerying baseline gets splits too
		// next, try to select the first baseline split
		if (shape.getData() instanceof TrpBaselineType) {
			return splitTrpBaselineType(x1, y1, x2, y2, shape, (TrpBaselineType) shape.getData());
		}
		else if (shape.getData() instanceof TrpTableCellType || shape.getData() instanceof TrpTableRegionType) {
			TrpTableRegionType table = null;
			if (shape.getData() instanceof TrpTableCellType)
				table = ((TrpTableCellType) shape.getData()).getTable();
			else
				table = (TrpTableRegionType) shape.getData();

			return splitTrpTableType(x1, y1, x2, y2, table);
		}
		
		
		else { // not splitting a basline -> perform default split operation on base class
			return super.splitShape(shape, x1, y1, x2, y2, null, addToUndoStack);
		}
	}
	
}
