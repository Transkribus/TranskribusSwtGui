package eu.transkribus.swt_gui.canvas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent.TableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpShapeTypeUtils;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableRegionType;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.IntRange;
import eu.transkribus.swt_canvas.canvas.CanvasException;
import eu.transkribus.swt_canvas.canvas.CanvasKeys;
import eu.transkribus.swt_canvas.canvas.editing.CanvasShapeEditor;
import eu.transkribus.swt_canvas.canvas.editing.ShapeEditOperation;
import eu.transkribus.swt_canvas.canvas.editing.ShapeEditOperation.ShapeEditType;
import eu.transkribus.swt_canvas.canvas.shapes.ACanvasShape;
import eu.transkribus.swt_canvas.canvas.shapes.CanvasQuadPolygon;
import eu.transkribus.swt_canvas.canvas.shapes.CanvasShapeType;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_canvas.canvas.shapes.RectDirection;
import eu.transkribus.swt_canvas.canvas.shapes.SplitDirection;
import eu.transkribus.swt_canvas.util.DialogUtil;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.util.TableUtils;
import math.geom2d.Point2D;
import math.geom2d.Vector2D;
import math.geom2d.line.Line2D;

///**
// * @deprecated not used currently
// */
public class TrpCanvasShapeEditor extends CanvasShapeEditor {
	private final static Logger logger = LoggerFactory.getLogger(TrpCanvasShapeEditor.class);
	
	TrpMainWidget mw;

	public TrpCanvasShapeEditor(TrpSWTCanvas canvas) {
		super(canvas);
		
		mw = canvas.getMainWidget();
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
		Pair<Integer, Integer> dims = table.getDimensions();
		
		int nRows = dims.getLeft();
		int nCols = dims.getRight();
		
		List<TrpTableCellType> splittableCells=null;
		SplitDirection dir = null;
		for (int j=0; j<2; ++j) {
			int N = j==0 ? nRows : nCols;
			
			for (int i=0; i<N; ++i) {
				List<TrpTableCellType> cells = table.getCells(j==0, true, i);
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
			if (splitOps4Cell != null) {
				for (ShapeEditOperation op : splitOps4Cell) {
					op.data = dir;
				}
				
				splitOps.addAll(splitOps4Cell);
			}
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
		
		TableUtils.checkTableConsistency(table);

		return splitOps;
	}
	
	@Override public List<ShapeEditOperation> splitShape(ICanvasShape shape, int x1, int y1, int x2, int y2, Object data, boolean addToUndoStack) {
		try {
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
				TrpTableRegionType table = (shape.getData() instanceof TrpTableCellType) ? 
						((TrpTableCellType) shape.getData()).getTable() : (TrpTableRegionType) shape.getData();
						
				return splitTrpTableType(x1, y1, x2, y2, table);
			}
			else { // not splitting a basline -> perform default split operation on base class
				return super.splitShape(shape, x1, y1, x2, y2, null, addToUndoStack);
			}
		} catch (Exception e) {
			mw.onError("Error splitting", e.getMessage(), e);
			return null;
		}
	}
	
	private void moveTableRowOrColumn(ICanvasShape selected, int selectedPoint, int mouseX, int mouseY, boolean firstMove, boolean rowwise) {
		logger.debug("moveTableRowOrColumn, rowwise: "+rowwise);
		
		CanvasQuadPolygon qp = (CanvasQuadPolygon) selected;
		TrpTableCellType tc = (TrpTableCellType) selected.getData();
		
		// determine side of quad poly where this point is on:
		int side = qp.getPointSide(selectedPoint);
		
		// jump out if combination of side and rowwise flag is incompatible:
		boolean isCornerPt = qp.isCornerPoint(selectedPoint);
		if (!isCornerPt && side%2==0 && rowwise) { // if pt on left or right
			logger.debug("cannot use non-corner-point from left or right side to move pts rowwise!");
			return;
		} else if (!isCornerPt && side%2==1 && !rowwise) {
			logger.debug("cannot use non-corner-point from top or bottom side to move pts columnwise!");
			return;
		}
		
		// depending on the rowwise variable which determines the direction to move on,
		// it can happen that the side we want to move is incorrect - correct that here:
		if (rowwise && selectedPoint==qp.getCornerPtIndex(0))
			side = 3;
		else if (!rowwise && selectedPoint==qp.getCornerPtIndex(1))
			side = 0;
		else if (rowwise && selectedPoint==qp.getCornerPtIndex(2))
			side = 1;
		else if (!rowwise && selectedPoint==qp.getCornerPtIndex(3))
			side = 2;
		
		int sideOpposite = (side+2) % 4;
		
		java.awt.Point selPt = qp.getPoint(selectedPoint);
		if (side < 0 || selPt == null) {
			logger.warn("Cannot find side of point to move row or column: "+selectedPoint);
			return;
		}
		
		// compute translation for each point:
		Point mousePtWoTr = canvas.inverseTransform(mouseX, mouseY);
		java.awt.Point trans = new java.awt.Point(mousePtWoTr.x-selPt.x, mousePtWoTr.y-selPt.y);

		// get all neighbor cells in this row / column and move their points according to the side
		boolean startIndex = side==0 || side == 3;
		int index;
		if (rowwise) {
			index = startIndex ? tc.getRow() : tc.getRowEnd();
		} else {
			index = startIndex ? tc.getCol() : tc.getColEnd();
		}
		
		List<ShapeEditOperation> ops = new ArrayList<>();
		List<TrpTableCellType> cells = tc.getTable().getCells(rowwise, startIndex, index);
		for (TrpTableCellType c : cells) {
			CanvasQuadPolygon s = (CanvasQuadPolygon) c.getData();
			
			if (firstMove) {
				ShapeEditOperation op = new ShapeEditOperation(canvas, ShapeEditType.EDIT, "Moved table "+(rowwise?"row":"column")+" points", s);					
				ops.add(op);
			}
			// move all points of that side
			s.translatePointsOfSide(side, trans.x, trans.y);
			
			// now also move all points of the neighbor side
			List<TrpTableCellType> neighbors = c.getNeighborCells(side);

			for (TrpTableCellType neighbor : neighbors) {			
				CanvasQuadPolygon ns = (CanvasQuadPolygon) neighbor.getData();
				if (firstMove) {
					ShapeEditOperation op = new ShapeEditOperation(canvas, ShapeEditType.EDIT, "Moved table "+(rowwise?"row":"column")+" points", ns);					
					ops.add(op);
				}			
				ns.translatePointsOfSide(sideOpposite, trans.x, trans.y);
			}
		}
		
		if (!ops.isEmpty())
			addToUndoStack(ops);		
	}
	
	private void moveTableCellPoints(ICanvasShape selected, int selectedPoint, int mouseX, int mouseY, boolean firstMove) {
		ICanvasShape selectedCopy = selected.copy();
		
		// First move selected pt(s), then move affected points from neighbors too
		
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
		
		final CanvasQuadPolygon qp = (CanvasQuadPolygon) selected;
		
		final Point mousePtWoTr = canvas.inverseTransform(mouseX, mouseY);
		
		List<ShapeEditOperation> ops = new ArrayList<>();
		ShapeEditOperation op = new ShapeEditOperation(canvas, ShapeEditType.EDIT, "Added point to shape", selected);
		ops.add(op);
		
		int ii = selected.insertPoint(mousePtWoTr.x, mousePtWoTr.y);
		int ptIndex = selected.getPointIndex(mousePtWoTr.x, mousePtWoTr.y);
		if (ptIndex == -1)
			return -1;
		
		int side = qp.getPointSide(ptIndex);
		int sideOpposite = (side+2) % 4; 
		
		logger.debug("add pt, side: "+side+" sideOpposite = "+sideOpposite);
		
		TrpTableCellType c = (TrpTableCellType) selected.getData();
		
		// determine nearest neighbor:
		List<TrpTableCellType> neighbors = c.getNeighborCells(side);
		logger.debug("add pt, n-neighbors: "+neighbors.size());
		
		// sort by distance:
		Collections.sort(neighbors, new Comparator<TrpTableCellType>() {
			@Override public int compare(TrpTableCellType o1, TrpTableCellType o2) {
				CanvasQuadPolygon n1 = (CanvasQuadPolygon) o1.getData();
				CanvasQuadPolygon n2 = (CanvasQuadPolygon) o2.getData();
				
				Double d1 = n1.distance(mousePtWoTr.x, mousePtWoTr.y, false);
				Double d2 = n2.distance(mousePtWoTr.x, mousePtWoTr.y, false);
				
				return d1.compareTo(d2);
			}
		});
		
		if (!neighbors.isEmpty()) {
			logger.debug("add pt, nearest neighbor: "+neighbors.get(0));
			CanvasQuadPolygon nc = (CanvasQuadPolygon) neighbors.get(0).getData();

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
		
		TrpTableCellType c = (TrpTableCellType) selected.getData();
		List<TrpTableCellType> neighbors = c.getNeighborCells(side);
		
		logger.debug("remove pt, side: "+side+", n-neighbors: "+neighbors.size());
		
		java.awt.Point pt2Remove = selected.getPoint(pointIndex);
		if (pt2Remove == null) {
			logger.warn("Cannot find point to remove for pointIndex = "+pointIndex);
			return;			
		}

		// check point can be removed from neighbor, jump out if not
		for (TrpTableCellType neighbor : neighbors) {
			CanvasQuadPolygon nc = (CanvasQuadPolygon) neighbor.getData();			
			int ri = nc.getPointIndex(pt2Remove.x, pt2Remove.y);
			if (ri != -1 && !nc.isPointRemovePossible(ri)) {
				return;
			}
		}
		
		// remove point from main shape:
		List<ShapeEditOperation> ops = new ArrayList<>();
		ShapeEditOperation op = new ShapeEditOperation(canvas, ShapeEditType.EDIT, "Removed point from shape", selected);
		ops.add(op);
		
		if (!selected.removePoint(pointIndex)) {
			logger.warn("Could not remove point "+pointIndex+" from shape!");
			return;
		}

		// remove point from neighbor cells
		for (TrpTableCellType neighbor : neighbors) {
			CanvasQuadPolygon nc = (CanvasQuadPolygon) neighbor.getData();

			int ri = nc.getPointIndex(pt2Remove.x, pt2Remove.y);
			if (ri != -1) {
				ShapeEditOperation opN = new ShapeEditOperation(canvas, ShapeEditType.EDIT, "Removed point from shape", nc);
				ops.add(opN);
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
				// TODO? allow resizing on outside -> should trigger resize of whole table region!
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
				
				int sm = canvas.getMouseListener().getCurrentMoveStateMask();
				boolean isCtrl = CanvasKeys.isKeyDown(sm, SWT.CTRL);
				boolean isAlt = CanvasKeys.isKeyDown(sm, SWT.ALT);

				logger.debug("isCtrl: "+isCtrl+" isAlt: "+isAlt);
				
				if (!isCtrl) {
					moveTableCellPoints(selected, selectedPoint, mouseX, mouseY, firstMove);
				} else {
					moveTableRowOrColumn(selected, selectedPoint, mouseX, mouseY, firstMove, !isAlt);
				}
				
				
			}
			else {
				super.movePointsFromSelected(selectedPoint, mouseX, mouseY, firstMove);
			}
		}
	}
	
	
	private static Pair<CanvasQuadPolygon, CanvasQuadPolygon> getMergeableCells(List<CanvasQuadPolygon> toMerge) {
		for (int i=0; i<toMerge.size(); ++i) {
			for (int j=i+1; j<toMerge.size(); ++j) {
				if (toMerge.get(i).getMergeableSide(toMerge.get(j))!=-1) {
					return Pair.of(toMerge.get(i), toMerge.get(j));
				}
			}
		}
		return null;
	}
	
	public ShapeEditOperation mergeSelectedTableCells(boolean sendSignal) {
		List<ICanvasShape> selectedShapes = scene.getSelectedAsNewArray();
		if (selectedShapes.size() < 2)
			return null;
		
		logger.debug("merging "+selectedShapes.size()+" table cells!");
				
		if (sendSignal) {
			if (scene.notifyOnBeforeShapesMerged(selectedShapes))
				return null;
		}
						
//		ICanvasShape merged = selectedShapes.get(0).copy();
	
		List<CanvasQuadPolygon> toMerge = new ArrayList<>();
		for (ICanvasShape s : selectedShapes) {
			toMerge.add((CanvasQuadPolygon) s.copy());
		}
		
		while (toMerge.size() > 1) {
			Pair<CanvasQuadPolygon, CanvasQuadPolygon> mergeable = getMergeableCells(toMerge);
			if (mergeable == null) {
				break;
			}
			
			toMerge.remove(mergeable.getLeft());
			toMerge.remove(mergeable.getRight());
			
			CanvasQuadPolygon m = (CanvasQuadPolygon) mergeable.getLeft().mergeShapes(mergeable.getRight());
			toMerge.add(0, m);
		}
		
		if (toMerge.size() > 1) {
			DialogUtil.showErrorMessageBox(canvas.getShell(), "Error merging cells", "Cannot merge cells - resulting cell must be rectangular!");
			logger.debug("cannot merge shapes, merged.size() = "+toMerge.size());
			return null;
		}
		
		scene.clearSelected();
		
		for (ICanvasShape s : selectedShapes) {
			scene.removeShape(s, true, true);
			for (ICanvasShape child : s.getChildren(false)) {
				toMerge.get(0).addChild(child);
			}
		}
		
//		
//		for (int i=1; i<selectedShapes.size(); ++i) {
//			merged = merged.mergeShapes(selectedShapes.get(i));
//			logger.debug("merged = "+merged);
//			if (merged == null)
//				return null;
//			
//			removeShape(selectedShapes.get(i), false, false);
//			for (ICanvasShape child : selectedShapes.get(i).getChildren(false)) {
//				merged.addChild(child);
//			}
//		}
//		
//		removeShape(selectedShapes.get(0), false, false);
		
		ShapeEditOperation opa = scene.addShape(toMerge.get(0), null, false);
//		logger.debug("merge added: "+opa);
		
		if (opa == null) {
			// TODO: should add removed shapes again here...
			logger.warn("unable to add merged shape: "+toMerge);
			return null;
		}
		
		ShapeEditOperation op = 
				new ShapeEditOperation(canvas, ShapeEditType.MERGE, selectedShapes.size()+" cells merged", selectedShapes);
		op.addNewShape(toMerge.get(0));
		
		if (sendSignal) {
			scene.notifyOnShapesMerged(op);
		}
		
		canvas.redraw();
		
		if (op!=null) {
			addToUndoStack(op);	
		}
		
		TableUtils.checkTableConsistency(((TrpTableCellType)selectedShapes.get(0).getData()).getTable());
		
		return op;
	}
	
	
	@Override public void mergeSelected() {
		logger.debug("mergeSelected, TrpCanvasShapeEditor");
		
		List<ICanvasShape> selected = scene.getSelectedAsNewArray();

		boolean isMergeTableCells=true;
		for (ICanvasShape s : selected) {
			if (!(s instanceof CanvasQuadPolygon) || !(s.getData() instanceof TrpTableCellType)) {
				isMergeTableCells = false;
				break;
			}
		}
		
		if (isMergeTableCells) {
			mergeSelectedTableCells(true);
		} else {
			super.mergeSelected();
		}
				
	}
	
	public static boolean isTableCell(ICanvasShape shape) {
		return shape!=null && shape instanceof CanvasQuadPolygon && shape.getData() instanceof TrpTableCellType;
	}
	

	
	public void splitMergedTableCell(ICanvasShape shape) {
		// warning: code below is hell on earth - don't fuck it up!
		
		logger.debug("splitting merged table cell!");
		
		if (!isTableCell(shape)) {
			DialogUtil.showErrorMessageBox(getShell(), "Error splitting merged cell", "No table cell selected!");
			return;
		}
		
		List<ShapeEditOperation> ops = new ArrayList<>();
		
		TrpTableCellType tc = (TrpTableCellType) shape.getData();
		CanvasQuadPolygon qp = (CanvasQuadPolygon) tc.getData();
		
		class Pt {
			List<java.awt.Point> p = new ArrayList<>();
			java.awt.Point f() { return p.isEmpty()?null:p.get(0); }
			void a(java.awt.Point pA) { p.add(pA); }
			void a(double x, double y) { p.add(new java.awt.Point((int)x, (int)y)); }
			void a(Point2D pA) { p.add(new java.awt.Point((int)pA.x(), (int)pA.y())); }
			
			String pStr() { return StringUtils.join(p, " "); }
		}
		
		Pt[][] pts = new Pt[tc.getRowSpan()+1][tc.getColSpan()+1];
				
		// go around borders and calculate border points
		for (int s=0; s<4; ++s) {
			List<TrpTableCellType> ns = tc.getNeighborCells(s);
			
			logger.debug("ns.size() = "+ns.size());
			
			int count=0;
			boolean rot = s>1;
			
			if (!ns.isEmpty()) {
				int so = (s+2)%4;
				for ( int i=(rot?ns.size()-1:0); i!=(rot?-1:ns.size()); i+=(rot?-1:1) ) {
					TrpTableCellType n = ns.get(i);
					
					CanvasQuadPolygon qpn = (CanvasQuadPolygon) n.getData();
					
					List<java.awt.Point> segPtsBase = qp.getPointsOfSegment(s, true);
					
					List<java.awt.Point> segPts = qpn.getPointsOfSegment(so, true);
					Collections.reverse(segPts);
					
					segPts = CoreUtils.getFirstCommonSequence(segPtsBase, segPts);
					if (segPts.size() < 2)
						throw new CanvasException("less than 2 common points on border to cell: "+n.print());
					
					int N = IntRange.getOverlapLength(n.getPos()[s%2], n.getSpan()[s%2], tc.getPos()[s%2], tc.getSpan()[s%2]);
					logger.debug("N overlapping rows/cols = "+N);
					
//					int N = s%2==0 ? n.getRowSpan() : n.getColSpan(); // nr of rows / cols this cells spans
					
					// construct points in between if necessary:
					Point2D p1 = new Point2D(segPts.get(0).x, segPts.get(0).y);
					Point2D p2 = new Point2D(segPts.get(segPts.size()-1).x, segPts.get(segPts.size()-1).y);
					Vector2D v = new Vector2D(p1, p2);
					
					List<java.awt.Point> insertedPts = new ArrayList<>();
					for (int x=1; x<N; ++x) {
						Point2D np = p1.plus(v.times((double)x/(double)N));
						java.awt.Point ip = new java.awt.Point((int) np.x(), (int) np.y());
						insertedPts.add(ip);
						
						int[] iz = ACanvasShape.getClosestLineIndices(ip.x, ip.y, segPts, false);
						int insertIndex = iz[1];
						int insertIndex4Shape = qpn.getPointIndex(segPts.get(iz[0]).x, segPts.get(iz[0]).y);
						
						logger.debug("1 insertIndex = "+insertIndex+" segPts.size() = "+segPts.size()+" insertIndex4Shape = "+insertIndex4Shape);
						segPts.add(insertIndex, ip);

						// insert new point into the shape:
						ShapeEditOperation op = new ShapeEditOperation(canvas, ShapeEditType.EDIT, "Added point to shape", qpn);
//						qpn.insertPointOnSide(ip.x, ip.y, so);
						qpn.insertPointOnIndex(ip.x, ip.y, insertIndex4Shape);
						ops.add(op);					
					}
					logger.debug("insertedPts.size() = "+insertedPts.size());
					
					for (int x=0; x<N; ++x) {
						Pt p = new Pt();
						int start=0;
						if (x > 0) {
							start = segPts.indexOf(insertedPts.get(x-1));
						}
						int end=segPts.size()-1;
						if (x+1 < N) {
							end = segPts.indexOf(insertedPts.get(x));
						}
						
						logger.debug("start = "+start+" end = "+end);
						
						for ( int j=start; j<end; ++j ) {
							java.awt.Point segPt = segPts.get(j);
							p.a(segPt);
						}
						
						int r=0, c=0;
						if (s==0) {
							r = count;
							c = 0;
						} else if (s==1) {
							r = tc.getRowSpan();
							c = count;
						} else if (s==2) {
							r = tc.getRowSpan()-count;
							c = tc.getColSpan();
						} else if (s==3) {
							r = 0;
							c = tc.getColSpan()-count;					
						}
						logger.debug("r x c = "+r+" x "+c);
						
						pts[r][c] = p;
						
						
						++count;
					} // end for x
				} // end for all neighbor cells
			}
			else { // no neighbor cells!
				logger.debug("no neighbor cells!");
				
				List<java.awt.Point> segPts = qp.getPointsOfSegment(s, true);
				int N = s%2==0 ? tc.getRowSpan() : tc.getColSpan(); // nr of rows / cols this cells spans
				
				// construct points in between if necessary:
				Point2D p1 = new Point2D(segPts.get(0).x, segPts.get(0).y);
				Point2D p2 = new Point2D(segPts.get(segPts.size()-1).x, segPts.get(segPts.size()-1).y);
				Vector2D v = new Vector2D(p1, p2);
				
				List<java.awt.Point> insertedPts = new ArrayList<>();
				for (int x=1; x<N; ++x) {
					Point2D np = p1.plus(v.times((double)x/(double)N));
					java.awt.Point ip = new java.awt.Point((int) np.x(), (int) np.y());
					insertedPts.add(ip);
					
					int[] iz = ACanvasShape.getClosestLineIndices(ip.x, ip.y, segPts, false);
					int insertIndex = iz[1];
					logger.debug("insertIndex = "+insertIndex+" segPts.size() = "+segPts.size());
					
					segPts.add(insertIndex, ip);

//					// insert new point into the shape:
//					ShapeEditOperation op = new ShapeEditOperation(canvas, ShapeEditType.EDIT, "Added point to shape", qp);
//					qp.insertPointOnSide(ip.x, ip.y, s);	
//					ops.add(op);
				}
				logger.debug("insertedPts.size() = "+insertedPts.size());
				
//				segPts = qp.getPointsOfSegment(s, true);
				
				for (int x=0; x<N; ++x) {
					Pt p = new Pt();
					int start=0;
					if (x > 0) {
						start = segPts.indexOf(insertedPts.get(x-1));
					}
					int end=segPts.size()-1;
					if (x+1 < N) {
						end = segPts.indexOf(insertedPts.get(x));
					}
					
					logger.debug("start = "+start+" end = "+end);
					
					for ( int j=start; j<end; ++j ) {
						java.awt.Point segPt = segPts.get(j);
						p.a(segPt);
					}
					
					int r=0, c=0;
					if (s==0) {
						r = count;
						c = 0;
					} else if (s==1) {
						r = tc.getRowSpan();
						c = count;
					} else if (s==2) {
						r = tc.getRowSpan()-count;
						c = tc.getColSpan();
					} else if (s==3) {
						r = 0;
						c = tc.getColSpan()-count;					
					}
					
					logger.debug("r x c = "+r+" x "+c);
					
					
					pts[r][c] = p;
					++count;
				}				
				
				
			}
		} // end calculate border points
		
		// calculate points in the middle
		for (int i=1; i<tc.getRowSpan(); ++i) {
			logger.debug("i = "+i);
			
			Point2D pr1 = new Point2D(pts[i][0].f().x, pts[i][0].f().y);
			Point2D pr2 = new Point2D(pts[i][tc.getColSpan()].f().x, pts[i][tc.getColSpan()].f().y);
			Line2D lr = new Line2D(pr1, pr2);
			
			for (int j=1; j<tc.getColSpan(); ++j) {
				logger.debug("j = "+j);
				Point2D pc1 = new Point2D(pts[0][j].f().x, pts[0][j].f().y);
				Point2D pc2 = new Point2D(pts[tc.getRowSpan()][j].f().x, pts[tc.getRowSpan()][j].f().y);				
				Line2D lc = new Line2D(pc1, pc2);
				
				Point2D ip = lr.intersection(lc);
				if (ip != null) {
					pts[i][j] = new Pt();
					pts[i][j].a(ip);
				} else {
					throw new RuntimeException("No intersection found between lines: "+lr+" - "+lc);
				}
			}
		}
		
		// print pts:
		for (int i=0; i<tc.getRowSpan()+1; ++i) {
			for (int j=0; j<tc.getColSpan()+1; ++j) {
				Pt pt = pts[i][j];
				if (pt != null) {
					logger.debug("i="+i+", j="+j+" pt = "+pt.f()+" N-pts = "+pt.p.size());
				} else {
					logger.debug("i="+i+", j="+j+" is null!!!");
				}
			}
		}
		
		for (int i=0; i<tc.getRowSpan(); ++i) {
			for (int j=0; j<tc.getColSpan(); ++j) {
				
				int c=0;
				int[] corners = { 0, 0, 0, 0 };
				List<java.awt.Point> newPts = new ArrayList<>();
				for (int x=0; x<4; ++x) {
					Pt pt = null;
					if (x==0)
						pt = pts[i][j];
					else if (x==1)
						pt = pts[i+1][j];
					else if (x==2)
						pt = pts[i+1][j+1];
					else
						pt = pts[i][j+1];
					
					corners[x] = c;
					
//					newPts.addAll(pt.p);
//					c += pt.p.size();
					
					if (i==0 && x==3 || i+1==tc.getRowSpan() && x==1 || j==0 && x==0 || j+1==tc.getColSpan() && x==2) {
						newPts.addAll(pt.p);
						c += pt.p.size();						
					} else {
						newPts.add(pt.f());
						++c;
					}
				}
				
				canvas.setMode(TrpCanvasAddMode.ADD_TABLECELL);
				
				CanvasQuadPolygon newQuadCell = new CanvasQuadPolygon(newPts, corners);
				
				logger.debug("new cell: "+newQuadCell);
				
				newQuadCell.setEditable(true);
				ShapeEditOperation op = scene.addShape(newQuadCell, null, true);
				if (op!=null) {
					ops.add(op);
				}
				
				// set new row/col values for shape:
				TrpTableCellType nc = (TrpTableCellType) newQuadCell.getData();
				if (nc != null) {
					nc.setRow(tc.getRow()+i);
					nc.setCol(tc.getCol()+j);
					nc.setRowSpan(1);
					nc.setColSpan(1);
				}
			}
		}	

		if (scene.removeShape(qp, true, true)) {
			ShapeEditOperation op = new ShapeEditOperation(canvas, ShapeEditType.DELETE, "Merged cell shape removed", qp);
			ops.add(op);
		}
		
		canvas.setMode(TrpCanvasAddMode.SELECTION);

		addToUndoStack(ops);
		
		TrpMainWidget.getInstance().refreshStructureView();
		
		TableUtils.checkTableConsistency(tc.getTable());
	}
	
	public Shell getShell() {
		return canvas.getShell();
	}
	
}
