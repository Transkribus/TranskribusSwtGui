package eu.transkribus.swt_gui.canvas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.swt.graphics.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent.TableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableRegionType;
import eu.transkribus.swt_canvas.canvas.editing.CanvasShapeEditor;
import eu.transkribus.swt_canvas.canvas.editing.ShapeEditOperation;
import eu.transkribus.swt_canvas.canvas.editing.ShapeEditOperation.ShapeEditType;
import eu.transkribus.swt_canvas.canvas.shapes.CanvasQuadPolygon;
import eu.transkribus.swt_canvas.canvas.shapes.CanvasShapeType;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_canvas.canvas.shapes.RectDirection;
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
	
	private void moveTableCellPoints(ICanvasShape selected, int selectedPoint, int mouseX, int mouseY, boolean firstMove) {
		ICanvasShape selectedCopy = selected.copy();
		
		// 1st, move selected pt(s), then move affected points from neighbors too
		List<ShapeEditOperation> ops = new ArrayList<>();
		if (firstMove) {
			ShapeEditOperation op = new ShapeEditOperation(canvas, ShapeEditType.EDIT, "Moved point(s) of shape", selected);					
			ops.add(op);
		}
		Point mousePtWoTr = canvas.inverseTransform(mouseX, mouseY);
		List<Integer> pts = selected.movePointAndSelected(selectedPoint, mousePtWoTr.x, mousePtWoTr.y);

		TrpTableCellType c = (TrpTableCellType) selected.getData();
		List<TrpTableCellType> neighbors = c.getNeighborCells();
		logger.debug("n-neighbors: "+neighbors.size());
		
		for (TrpTableCellType n : neighbors) {
			ICanvasShape ns = (ICanvasShape) n.getData();
			for (int i : pts) {
				java.awt.Point pOld = selectedCopy.getPoint(i);
				java.awt.Point pNew = selected.getPoint(i);
				logger.debug("pOld = "+pOld+" pNew = "+pNew);
				
				if (pOld != null && pOld != null) {
					int j = ns.getPointIndex(pOld.x, pOld.y);
					logger.debug("j = "+j);
					if (j != -1) {
						
						if (firstMove) {
							ShapeEditOperation op = new ShapeEditOperation(canvas, ShapeEditType.EDIT, "Moved point(s) of shape", ns);					
							ops.add(op);
						}							
						
						logger.debug("moved point "+j+" in cell "+n.print());
						ns.movePoint(j, pNew.x, pNew.y);
					}
				}
			}
		}
		
		if (!ops.isEmpty())
			addToUndoStack(ops);
	}
	
	private int addPointToTableCell(ICanvasShape selected, int mouseX, int mouseY) {
		logger.debug("adding pt to neighbor table cell!");
		
		CanvasQuadPolygon qp = (CanvasQuadPolygon) selected;
		
		Point mousePtWoTr = canvas.inverseTransform(mouseX, mouseY);
		
		List<ShapeEditOperation> ops = new ArrayList<>();
		ShapeEditOperation op = new ShapeEditOperation(canvas, ShapeEditType.EDIT, "Added point to shape", selected);
		ops.add(op);
		
		int ii = selected.insertPoint(mousePtWoTr.x, mousePtWoTr.y);
		int ptIndex = selected.getPointIndex(mousePtWoTr.x, mousePtWoTr.y);
		if (ptIndex == -1)
			return -1;
		
		int side = qp.getPointSide(ptIndex);
		int sideOpposite = (side+2) % 4; 
		
		logger.debug("add pt - side: "+side+" sideOpposite = "+sideOpposite);
		
		TrpTableCellType c = (TrpTableCellType) selected.getData();
		
		TrpTableCellType neighbor = c.getNeighborCell(side);
		logger.debug("add pt, neighbor: "+neighbor);
		
		if (neighbor != null) {
			CanvasQuadPolygon nc = (CanvasQuadPolygon) neighbor.getData();
			
			ShapeEditOperation opN = new ShapeEditOperation(canvas, ShapeEditType.EDIT, "Added point to shape", nc);
			ops.add(opN);
			
			nc.insertPointOnSide(mousePtWoTr.x, mousePtWoTr.y, sideOpposite);
		}
				
		if (!ops.isEmpty())
			addToUndoStack(ops);
		
		return ii;
	}
	
	private void removePointFromTableCell(ICanvasShape selected, int pointIndex) {
		CanvasQuadPolygon qp = (CanvasQuadPolygon) selected;
		int side = qp.getPointSide(pointIndex);
		if (side == -1) {
			logger.warn("Cannot find side of point to remove: "+pointIndex);
			return;
		}
		
		List<ShapeEditOperation> ops = new ArrayList<>();
		ShapeEditOperation op = new ShapeEditOperation(canvas, ShapeEditType.EDIT, "Removed point from shape", selected);
		ops.add(op);
		
		java.awt.Point removedPt = selected.getPoint(pointIndex);
		
		if (!selected.removePoint(pointIndex)) {
			logger.warn("Could not remove point "+pointIndex+" from shape!");
			return;
		}
		
		logger.debug("remove pt - side: "+side);
		
		TrpTableCellType c = (TrpTableCellType) selected.getData();
		
		TrpTableCellType neighbor = c.getNeighborCell(side);
		logger.debug("remove pt, neighbor: "+neighbor);
		
		if (neighbor != null) {
			CanvasQuadPolygon nc = (CanvasQuadPolygon) neighbor.getData();
			
			ShapeEditOperation opN = new ShapeEditOperation(canvas, ShapeEditType.EDIT, "Removed point from shape", nc);
			ops.add(opN);
			
			int ri = nc.getPointIndex(removedPt.x, removedPt.y);
			
			if (ri != -1) {
				if (nc.removePoint(ri)) {
					ops.add(opN);
				}
			}
		}
		
		if (!ops.isEmpty())
			addToUndoStack(ops);
		
	}
		
	@Override public void resizeBoundingBoxFromSelected(RectDirection direction, int mouseTrX, int mouseTrY, boolean firstMove) {
		ICanvasShape selected = canvas.getFirstSelected();
		
		if (selected!=null && selected.isEditable() && direction!=RectDirection.NONE) {
			if (selected.getData() instanceof TrpTableCellType && selected instanceof CanvasQuadPolygon) {
				// PREVENT RESIZING BOUNDING BOX FOR TABLE CELLS
			} 
			else {
				super.resizeBoundingBoxFromSelected(direction, mouseTrX, mouseTrY, firstMove);
			}
		}
	}
	
	@Override public void moveSelected(int mouseTrX, int mouseTrY, boolean firstMove) {
		ICanvasShape selected = canvas.getFirstSelected();
		if (selected != null && selected.isEditable()) {
			if (selected.getData() instanceof TrpTableCellType && selected instanceof CanvasQuadPolygon) {
				// PREVENT RESIZING BOUNDING BOX FOR TABLE CELLS
			} 
			else {
				super.moveSelected(mouseTrX, mouseTrY, firstMove);
			}
		}
	}
	
	@Override public void removePointFromSelected(int pointIndex) {
		logger.debug("removing point "+pointIndex);
		
		ICanvasShape selected = canvas.getFirstSelected();
		if (selected != null && selected.isEditable()) {
			if (selected.getData() instanceof TrpTableCellType && selected instanceof CanvasQuadPolygon) {
				removePointFromTableCell(selected, pointIndex);
			} 
			else {
				super.removePointFromSelected(pointIndex);
			}
		}
	}
	
	@Override public int addPointToSelected(int mouseX, int mouseY) {
		logger.debug("inserting point!");
		
		ICanvasShape selected = canvas.getFirstSelected();
		if (selected != null && selected.isEditable()) {
			if (selected.getData() instanceof TrpTableCellType && selected instanceof CanvasQuadPolygon) {
				return addPointToTableCell(selected, mouseX, mouseY);
			} 
			else {
				return super.addPointToSelected(mouseX, mouseY);
			}
		}
		return -1;
	}
	
	@Override public void movePointsFromSelected(int selectedPoint, int mouseX, int mouseY, boolean firstMove) {
		ICanvasShape selected = canvas.getFirstSelected();
		
		if (selected!=null && selected.isEditable() && selectedPoint != -1) {
			if (selected.getData() instanceof TrpTableCellType && selected instanceof CanvasQuadPolygon) {
				moveTableCellPoints(selected, selectedPoint, mouseX, mouseY, firstMove);
			}
			else {
				super.movePointsFromSelected(selectedPoint, mouseX, mouseY, firstMove);
			}
		}
	}
	
}
