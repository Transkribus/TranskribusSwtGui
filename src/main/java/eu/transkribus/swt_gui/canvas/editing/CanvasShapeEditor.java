package eu.transkribus.swt_gui.canvas.editing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableRegionType.GetCellsType;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.IntRange;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt_gui.canvas.CanvasException;
import eu.transkribus.swt_gui.canvas.CanvasKeys;
import eu.transkribus.swt_gui.canvas.CanvasMode;
import eu.transkribus.swt_gui.canvas.CanvasScene;
import eu.transkribus.swt_gui.canvas.CanvasSettings;
import eu.transkribus.swt_gui.canvas.SWTCanvas;
import eu.transkribus.swt_gui.canvas.editing.ShapeEditOperation.ShapeEditType;
import eu.transkribus.swt_gui.canvas.shapes.CanvasPolygon;
import eu.transkribus.swt_gui.canvas.shapes.CanvasPolyline;
import eu.transkribus.swt_gui.canvas.shapes.CanvasQuadPolygon;
import eu.transkribus.swt_gui.canvas.shapes.CanvasShapeType;
import eu.transkribus.swt_gui.canvas.shapes.CanvasShapeUtil;
import eu.transkribus.swt_gui.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.canvas.shapes.RectDirection;
import eu.transkribus.swt_gui.canvas.shapes.TableDimension;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.table_editor.BorderFlags;
import eu.transkribus.swt_gui.table_editor.TableCellUndoData;
import eu.transkribus.swt_gui.table_editor.TableShapeEditOperation;
import eu.transkribus.swt_gui.table_editor.TableUtils;
import eu.transkribus.swt_gui.table_editor.TableUtils.SplittableCellsStruct;
import math.geom2d.Point2D;
import math.geom2d.Vector2D;
import math.geom2d.line.Line2D;

/**
 * This class is resonsible for editing and drawing new shapes according to interactive points drawn by the user.
 * Currently supported shapes are: Polygon, Polyline, Rectangle
 * 
 * <p><b>Important note from sebic:</b> all the shape editing stuff is done 
 * in three different classes: CanvasShapeEditor, a controller where every edit operation should start,
 * CanvasScene, where the editing of CanvasShape's is done and CanvasSceneListener, where all ITrpShapeType's related stuff is implemented.
 * This is due to historic reasons, when I wanted to implement a clean separation between the canvas shapes and the PAGE related stuff.
 * This separation has been abandoned however and now the code is scattered across those classes. Sorry for the confusion :-)</p> 
 */
public class CanvasShapeEditor {
	static Logger logger = LoggerFactory.getLogger(CanvasShapeEditor.class);
	
	protected SWTCanvas canvas;
	protected CanvasScene scene;
//	protected Class<? extends ICanvasShape> shapeToDraw=CanvasRect.class;
	protected CanvasShapeType shapeToDraw = CanvasShapeType.POLYGON;
	
	protected List<ICanvasShape> drawnShapes = new ArrayList<>();
	protected List<java.awt.Point> drawnPoints = new ArrayList<>();
	
	protected ShapeEditOperation currentMoveOp = null;
	
	protected ICanvasShape backupShape;

	protected TrpMainWidget mw;

	protected boolean stopPostProcess=false;
	
	public CanvasShapeEditor(SWTCanvas canvas) {
		Assert.assertNotNull(canvas);
		
		this.canvas = canvas;
		this.scene = canvas.getScene();
		initListener();
	}
	
	private void initListener() {
		canvas.getSettings().addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				boolean isModeProp = evt.getPropertyName().equals(CanvasSettings.MODE_PROPERTY);
				boolean isOrWasMoving = isModeProp && (evt.getNewValue() == CanvasMode.MOVE || evt.getOldValue() == CanvasMode.MOVE);
				logger.trace("isOrWasMoving: "+isOrWasMoving);
				
				if (!isOrWasMoving) { // do not clear shapes if user is moving canvas!
					clearDrawnShapes();
				}
			}
		});
	}
	
	public void addPointForSplitting(int x, int y) {
		if (!canvas.getMode().isSplitOperation())
			return;
		
		Point invPt = canvas.inverseTransform(x, y);
		
		if (canvas.getMode() == CanvasMode.SPLIT_SHAPE_LINE) {
//			if (drawnPoints.size() == 1) {
//				splitShape(canvas.getFirstSelected(), drawnPoints.get(0).x, drawnPoints.get(0).y, invPt.x,invPt.y, true);
//				drawnPoints.clear();
//			} else {
//				// actually add point:
//				drawnPoints.add(new java.awt.Point(invPt.x,invPt.y));			
//			}
			drawnPoints.add(new java.awt.Point(invPt.x,invPt.y));
		} else if (canvas.getMode() == CanvasMode.SPLIT_SHAPE_BY_HORIZONTAL_LINE) {
			splitShape(canvas.getFirstSelected(), -1, invPt.y, 1, invPt.y, true);
		} else if (canvas.getMode() == CanvasMode.SPLIT_SHAPE_BY_VERTICAL_LINE) {
			splitShape(canvas.getFirstSelected(), invPt.x, -1, invPt.x, 1, true);
		}
	}
	
	public void finishSplitByLine() {
		if (canvas.getMode() != CanvasMode.SPLIT_SHAPE_LINE)
			return;

//		Point invPt = canvas.inverseTransform(x, y);
//		drawnPoints.add(new java.awt.Point(invPt.x, invPt.y));
		
		if (drawnPoints.size() < 2)
			return;
		
		splitShape(canvas.getFirstSelected(), new CanvasPolyline(drawnPoints), true);
		drawnPoints.clear();
	}
	
	public List<ShapeEditOperation> splitShape(ICanvasShape shape, int x1, int y1, int x2, int y2, boolean addToUndoStack) {
		return splitShape(shape, new CanvasPolyline(new java.awt.Point(x1, y1), new java.awt.Point(x2, y2)), addToUndoStack);		
	}
	
//	public List<ShapeEditOperation> splitShape(ICanvasShape shape, CanvasPolyline pl, boolean addToUndoStack) {
////		ICanvasShape selected = canvas.getFirstSelected();
//		if (shape == null) {
//			logger.warn("Cannot split - no shape selected!");
//			return null;
//		}
//		
//		List<ICanvasShape> children = shape.getChildren(true); // get all children (recursively)!
//		List<ShapeEditOperation> splitOps = new ArrayList<>();
//				
//		ShapeEditOperation op = scene.splitShape(shape, pl, true, null, null, false);
//		if (op!=null) {
//			splitOps.add(op);
//		}
//		
//		// Split all child shapes
//		for (ICanvasShape child : children) {
//			// Determine the parent shapes of the child shape that shall be splitted by iterating through the edit operations that were done so far
//			ICanvasShape p1=null, p2=null;
//			for (ShapeEditOperation opParent : splitOps) {
//				if (opParent.getNewShapes().get(0).equals(child.getParent()) 
//						|| opParent.getNewShapes().get(1).equals(child.getParent())) {
//					p1 = opParent.getNewShapes().get(0);
//					p2 = opParent.getNewShapes().get(1);
////					logger.debug("readjusting child elements - parents: "+p1+"/"+p2);
//					break;
//				}
//			}
//			
//			// Try to split child shape using the parents that were just determined:
//			logger.debug("p1 / p2 = "+p1+" / "+p2);
//			ShapeEditOperation opChild = scene.splitShape(child, pl, true, p1, p2, true);
//			if (opChild!=null) {
//				splitOps.add(opChild);
//			}
//		}
//		
//		if (splitOps.isEmpty()) {
//			logger.warn("Cannot split - no shapes actually splitted by line!");
//			return null;
//		}
//		
//		scene.notifyOnAfterShapeSplitted(op);
//		
//		if (addToUndoStack)
//			addToUndoStack(splitOps);
//		
//		return splitOps;
//	}

	/** Adds the given point as a new point while drawing a new shape */
	public void addPointForNewShape(int x, int y, boolean startNewShape) {
		if (startNewShape && !drawnPoints.isEmpty()) {
			finishCurrentShape(false);
		}
		
		Point invPt = canvas.inverseTransform(x, y);
		logger.debug("addPointForNewShape, shape to draw = "+shapeToDraw+", invPt = "+invPt+", drawnPoints = "+drawnPoints);
		
		// cannot add more than one point to a rectangle -> second point will finish shape!
		if (shapeToDraw == CanvasShapeType.RECTANGLE && drawnPoints.size() == 1) {
			drawnPoints.add(new java.awt.Point(invPt.x,invPt.y));
			finishCurrentShape(true);
			return;
		}
		// finish polygon if clicked into the first point:
		else if (shapeToDraw == CanvasShapeType.POLYGON && drawnPoints.size()>=3) {
			if (drawnPoints.get(0).distance(invPt.x, invPt.y) < canvas.getSettings().getSelectedPointRadius()) {
				finishCurrentShape(true);
				return;
			}
		}
		
		// actually add point if not the same as last one:
		if (drawnPoints.isEmpty() || drawnPoints.get(drawnPoints.size()-1).distance(invPt.x, invPt.y) >= canvas.getSettings().getSelectedPointRadius()) {
			drawnPoints.add(new java.awt.Point(invPt.x,invPt.y));	
		}
		
	}
	
	/** Finishes the currently drawn shape and adds it to the canvas */
	public void finishCurrentShape(boolean finishAll) {
		if (drawnPoints.isEmpty())
			return;
		
		// add current mouse point as second rectangle point if not explicitly drawn:
		if (shapeToDraw == CanvasShapeType.RECTANGLE && drawnPoints.size() == 1) {
			Point mPt = canvas.getMouseListener().getMousePt();
			Point invPt = canvas.inverseTransform(mPt.x, mPt.y); 
			drawnPoints.add(new java.awt.Point(invPt.x,invPt.y));
		}
		
		ICanvasShape newShape = constructShapeFromPoints(drawnPoints, shapeToDraw);
		if (newShape == null) {
			logger.error("Could not add new shape for points (maybe only one point drawn?): "+drawnPoints+" \nshapeToDraw = "+shapeToDraw);
			return;
		}
		else {
			drawnShapes.add(newShape);
			drawnPoints.clear();
		}
		
		if (finishAll) {
			postProcessDrawnShapes();
		}
	}
	
	private void postProcessDrawnShapes() {
		CanvasMode m = canvas.getSettings().getMode();
		if (m.isAddOperation()) {
			stopPostProcess=false;
			for (ICanvasShape s : drawnShapes) {
				addShapeToCanvas(s, true);
				if (stopPostProcess==true)
					break;
			}
		}
		
		clearDrawnShapes();
	}
	
	/** Clears the currently drawn shape */
	public void clearDrawnShapes() {
		logger.debug("clearing drawn shapes!");
		stopPostProcess=true;
		drawnShapes.clear();
		drawnPoints.clear();
		
		if (!canvas.isDisposed()){
			canvas.redraw();
		}
	}
	
	protected ICanvasShape constructShapeFromPoints(List<java.awt.Point> pts, CanvasShapeType shapeType) {
		if (canvas.getMode() == CanvasMode.ADD_TABLECELL) {
			// assume table cell is drawn as rectangle
			List<java.awt.Point> polyPts = new ArrayList<>();
			polyPts.add(pts.get(0));
			polyPts.add(new java.awt.Point(pts.get(0).x, pts.get(1).y));
			polyPts.add(pts.get(1));
			polyPts.add(new java.awt.Point(pts.get(1).x, pts.get(0).y));
				
			return new CanvasQuadPolygon(polyPts);
		} else {
			ICanvasShape newShape=null;
			if (shapeType == CanvasShapeType.POLYGON && pts.size()>=3) {
				newShape = new CanvasPolygon(pts);
			}
			else if (shapeType == CanvasShapeType.POLYLINE && pts.size() >= 2) {
				newShape = new CanvasPolyline(pts);
			}
			else if (shapeType == CanvasShapeType.RECTANGLE && pts.size() == 2) {
	//			if (pts.size()==1) {
	//				Point mPt = canvas.getMouseListener().getMousePt();
	//				Point invPt = canvas.inverseTransform(mPt.x, mPt.y); 
	//				pts.add(new java.awt.Point(invPt.x,invPt.y));
	//			}
				
				// FIXME(?) add polygon even if rectangle is added --> for later editing!
				List<java.awt.Point> polyPts = new ArrayList<>();
				java.awt.Point p1 = pts.get(0);
				java.awt.Point p2 = pts.get(1);
				polyPts.add(p1);
				polyPts.add(new java.awt.Point(p2.x, p1.y));
				polyPts.add(p2);
				polyPts.add(new java.awt.Point(p1.x, p2.y));			
				newShape = new CanvasPolygon(polyPts);
				
				// the old shit: here, an actual rectangle is drawn:
	//			try {
	//				newShape = new CanvasRect(drawnPoints);
	//			}
	//			catch (Exception e) { // exception is thrown when drawnPoint does not contain exactly two point - should not happen though
	//				newShape = null;
	//				DialogUtil.showErrorMessageBox(canvas.getShell(), "Error creating rectangle", e.getMessage());
	//			}
			}
			return newShape;
		}
	}	
	
	public void removeAll() {
		logger.debug("removing all shapes: "+scene.getNSelected());
		removeShapesFromCanvas(new ArrayList<ICanvasShape>(scene.getShapes()), true);
		canvas.redraw();
	}
		
	public void removeSelected() {
		List<ICanvasShape> selected = scene.getSelectedAsNewArray();
		logger.debug("removing selected: "+selected.size()+ " / "+scene.nShapes());
		removeShapesFromCanvas(selected, true);
		canvas.redraw();
	}
	
	public void mergeSelected() {
		logger.debug("mergeSelected, TrpCanvasShapeEditor");
		
		List<ICanvasShape> selected = scene.getSelectedAsNewArray();
		boolean isMergeTableCells = TableUtils.isTableCells(selected);
		
		if (isMergeTableCells) {
			mergeSelectedTableCells(selected, true, true);
		} else {
			logger.debug("merging selected: "+selected.size());
			if (selected.size() < 2)
				return;

			ShapeEditOperation op = scene.mergeSelected(true,true);
			if (op!=null) {
				addToUndoStack(op);	
			}
		}
	}
	
	public void simplifySelected(double perc) {
		ShapeEditOperation op = new ShapeEditOperation(ShapeEditType.EDIT, "Polygon simplification", canvas.getScene().getSelectedAsNewArray());
		
		for (ICanvasShape s : op.getShapes()) {			
			double d = s.getPoint(0).distance(s.getPoint(2));
			double eps = (d * perc)/100.0d;
			logger.debug("simplifying selected with perc: "+perc+" diameter = "+d+ " eps = "+eps);
			s.simplify(eps);	
		}
		addToUndoStack(op);

		canvas.redraw();
	}	
	
	public void addToUndoStack(ShapeEditOperation op) {
		if (op != null)
			canvas.getUndoStack().addToUndoStack(op);
	}
	
	public void addToUndoStack(List<ShapeEditOperation> ops) {
		if (ops!=null && !ops.isEmpty())
			canvas.getUndoStack().addToUndoStack(ops);
	}	

	/** Sets the type of shape that is currently drawn */
	public void setShapeToDraw(CanvasShapeType shapeToDraw) { this.shapeToDraw = shapeToDraw; }
	/** Returns the type of shape that is currently drawn */
	public CanvasShapeType getShapeToDraw() { return shapeToDraw; }

//	public ArrayList<java.awt.Point> getDrawnPoints() { return drawnPoints; }
	
	// THOSE ARE THE ACTUAL EDIT OPERATIONS:
	
	public ShapeEditOperation addShapeToCanvas(ICanvasShape newShape, boolean addToUndoStack) {
		if (newShape == null)
			return null;
			
		newShape.setEditable(true);
		ShapeEditOperation op = scene.addShape(newShape, null, true);
		if (op!=null && addToUndoStack) {
			addToUndoStack(op);
		}
		return op;	
	}
	
	public ShapeEditOperation removeShapeFromCanvas(ICanvasShape shapesToRemove, boolean addToUndoStack) {
		List<ICanvasShape> sl = new ArrayList<>();
		sl.add(shapesToRemove);
		return removeShapesFromCanvas(sl, addToUndoStack);
	}

	public ShapeEditOperation removeShapesFromCanvas(List<ICanvasShape> shapesToRemove, boolean addToUndoStack) {
		if (shapesToRemove==null || shapesToRemove.isEmpty())
			return null;
		
		// remove table if attempted to remove table cell:		
		List<ICanvasShape> str2 = new ArrayList<>();
		for (ICanvasShape s : shapesToRemove) {
			if (TableUtils.getTableCell(s)!=null)
				str2.add(s.getParent());
			else
				str2.add(s);
		}		
		
		Collections.sort(str2);			
		logger.debug("removing shapes: "+str2.size());
		
		// remove shapes - add actually removed shapes to new list - it is possible that some shapes are not removed, if its parent element gets removed first!
		List<ICanvasShape> removedShapes = new ArrayList<ICanvasShape>();
		for (ICanvasShape s : str2) {
			if (scene.removeShape(s, true, true)) {
				removedShapes.add(s);
			}
		}
		logger.debug("actually removed shapes: "+removedShapes.size());
		
		ShapeEditOperation op = new ShapeEditOperation(ShapeEditType.DELETE, removedShapes.size()+" shapes removed", removedShapes);
		
		if (addToUndoStack)
			addToUndoStack(op);
		
		return op;
	}
	
	protected ShapeEditOperation insertPointIntoShape(ICanvasShape shape, int x, int y) {
		if (shape != null) {
			ShapeEditOperation op = new ShapeEditOperation(ShapeEditType.EDIT, "Added point to shape", shape);
			shape.insertPoint(x, y);
			return op;
		} else
			return null;
	}

	public ShapeEditOperation addPointToShape(ICanvasShape shape, int mouseX, int mouseY, boolean addToUndoStack) {
		logger.debug("adding point: "+mouseX+" x "+mouseY);
		Point mousePtWoTr = canvas.inverseTransform(mouseX, mouseY);
		
		TrpTableCellType tc = TableUtils.getTableCell(shape);
		if (tc != null) {
			return addPointToTableCell(shape, mouseX, mouseY, addToUndoStack);
		} else if (shape != null && shape.isEditable()) {
			ShapeEditOperation op = new ShapeEditOperation(ShapeEditType.EDIT, "Inserted point to shape", shape);
			if (addToUndoStack)
				addToUndoStack(op);
			
			int index = shape.insertPoint(mousePtWoTr.x, mousePtWoTr.y);
			op.data = index;
			
			return op;
		}
		return null;		
	}
	
	public ShapeEditOperation removePointFromSelected(ICanvasShape shape, int pointIndex, boolean addToUndoStack) {
		if (!isEditable(shape)) {
			return null;
		}
		
		logger.debug("removing point "+pointIndex);			
		
		TrpTableCellType c = TableUtils.getTableCell(shape);
		ShapeEditOperation op;
		if (c != null) {
			op = removePointFromTableCell(shape, pointIndex, addToUndoStack);
			TrpMainWidget.getInstance().refreshStructureView();
		} else {
			op = new ShapeEditOperation(ShapeEditType.EDIT, "Removed point from shape", shape);
			if (!shape.removePoint(pointIndex)) {
				logger.warn("Could not remove point "+pointIndex+" from shape!");
			} else if (addToUndoStack) {
				addToUndoStack(op);
			}
		}
		
		return op;
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

	public static boolean isEditable(ICanvasShape s) {
		return s!=null && s.isEditable();
	}
	
	public ShapeEditOperation movePointAndSelected(ICanvasShape shape, int selectedPoint, int mouseX, int mouseY, boolean addToUndoStack) {
		if (!isEditable(shape) || selectedPoint == -1)
			return null;
		
		TrpTableCellType tc = TableUtils.getTableCell(shape);
		ShapeEditOperation op=null;
		
		if (tc != null) {
			return moveTableCellPoints(shape, selectedPoint, mouseX, mouseY, addToUndoStack);
		} else {
			op = new ShapeEditOperation(ShapeEditType.EDIT, "Moved point(s) of shape", shape);
			
			logger.debug("moving points, shape: "+shape+" point: "+selectedPoint);
			Point mousePtWoTr = canvas.inverseTransform(mouseX, mouseY);
			List<Integer> movedPts = shape.movePointAndSelected(selectedPoint, mousePtWoTr.x, mousePtWoTr.y);
			if (movedPts.isEmpty())
				return null;
			
//			selected.movePoint(selectedPoint, mousePtWoTr.x, mousePtWoTr.y);

			if (addToUndoStack)
				addToUndoStack(op);
			
			return op;
		}
	}
	
	public ShapeEditOperation translatePoints(ICanvasShape shape, int tx, int ty, boolean addToUndoStack, int ...indices) {
		if (shape == null)
			return null;
		
		TrpTableCellType tc = TableUtils.getTableCell(shape);
		ShapeEditOperation op = null;
		if (tc != null) {
			int sm = canvas.getMouseListener().getCurrentMoveStateMask();
			boolean isCtrl = CanvasKeys.isKeyDown(sm, SWT.CTRL);
//			boolean isAlt = CanvasKeys.isKeyDown(sm, SWT.ALT);

			if (isCtrl && indices.length == 2) {
				CanvasQuadPolygon qp = (CanvasQuadPolygon) shape;
				qp.getPointSide(indices[0]);
				
				op = translateTableRowOrColumnPoints(shape, qp.getPointSide(indices[0]), tx, ty, addToUndoStack);
			} else {
				op = translateTableCellPoints(shape, tx, ty, addToUndoStack, indices);	
			}			
		}
		else {
			op = new ShapeEditOperation(ShapeEditType.EDIT, "Altered point(s) of shape", shape);
			for (int index : indices) {			
				if (!shape.translatePoint(index, tx, ty)) {
					return null;
				}
			}
			
			if (addToUndoStack)
				addToUndoStack(op);
		}

		return op;
	}
	
	public void resizeBoundingBoxFromSelected(RectDirection direction, int mouseTrX, int mouseTrY, boolean firstMove) {
		ICanvasShape selected = canvas.getFirstSelected();
		
		if (selected!=null && selected.isEditable() && direction!=RectDirection.NONE) {
			if (selected.getData() instanceof TrpTableCellType && selected instanceof CanvasQuadPolygon) {
				// PREVENT RESIZING BOUNDING BOX FOR TABLE CELLS
			} 
			else if (selected!=null && selected.isEditable() && direction!=RectDirection.NONE) {
				// invert transform:
				Point transWoTr = canvas.invertTranslationTransform(mouseTrX, mouseTrY);
				
				logger.trace("moving bounding box trans: "+transWoTr.x+" x "+transWoTr.y);	
				if (firstMove) {
					currentMoveOp = new ShapeEditOperation(ShapeEditType.EDIT, "Moved bounding box of shape", selected);
					addToUndoStack(currentMoveOp);
				}
				
				// set points back if this is not the first move s.t. total translation can be applied:
				if (!firstMove && currentMoveOp!=null && currentMoveOp.getFirstBackupShape()!=null)
					selected.setPoints(currentMoveOp.getFirstBackupShape().getPoints());			
				
				selected.moveBoundingBox(direction, transWoTr.x, transWoTr.y);
			}
		}		
	}
	
	private ShapeEditOperation moveShapeDefault(ICanvasShape shape, int mouseTrX, int mouseTrY, ShapeEditOperation currentMoveOp, boolean addToUndoStack) {
		// invert transform:
		java.awt.Point transWoTr = canvas.getPersistentTransform().inverseTransformWithoutTranslation(mouseTrX, mouseTrY);
		logger.trace("t = "+transWoTr);
				
		// if first move --> determine shapes to move
		if (currentMoveOp==null) {
			List<ICanvasShape> shapesToMove = new ArrayList<>();
			shapesToMove.add(shape);
			// move subshapes if required key down:
			if (CanvasKeys.isKeyDown(canvas.getKeyListener().getCurrentStateMask(), CanvasKeys.MOVE_SUBSHAPES_REQUIRED_KEY)) {
				logger.debug("moving subshapes!");
				shapesToMove.addAll(shape.getChildren(true));
			}
			currentMoveOp = new ShapeEditOperation(ShapeEditType.EDIT, "Moved shape(s)", shapesToMove);
			if (addToUndoStack) {
				addToUndoStack(currentMoveOp);
			}
		}
		
		// now move all shapes for the current move operation:
//		boolean movedFirst = true;
		for (int i=0; i<currentMoveOp.getShapes().size(); ++i) {
			ICanvasShape s = currentMoveOp.getShapes().get(i);
			
			// reset points if this isnt the first move (translation is always specified global for one move to prevent rounding errors!)
//			if (!firstMove) {
				ICanvasShape bs = currentMoveOp.getBackupShapes().get(i);
				s.setPoints(bs.getPoints());
//			}
						
			boolean moved = scene.moveShape(s, transWoTr.x, transWoTr.y, true); 
			
			if (i == 0 && !moved) { // if first shape (i.e. parent shape) was not moved, jump out
				return null;
//				movedFirst = false;
//				break;
			}
		}
		
//		if (addToUndoStack /*&& movedFirst*/ && firstMove && currentMoveOp!=null) {
//			addToUndoStack(currentMoveOp);
//		}
		
		return currentMoveOp;
		
	}
	
	/** Translate the selection object by the given coordinates. The translation is always given as the \emph{total}
	 * translation for a current move operation so that rounding errors are minimized! 
	 * **/
	public ShapeEditOperation moveShape(ICanvasShape shape, int mouseTrX, int mouseTrY, ShapeEditOperation currentMoveOp, boolean addToUndoStack) {
//		ICanvasShape selected = canvas.getFirstSelected();
		if (shape == null)
			return null;
		
		TrpTableCellType tc = TableUtils.getTableCell(shape);
		
		if (tc != null) {
			// PREVENT RESIZING BOUNDING BOX FOR TABLE CELLS
			// TODO? allow resizing on outside -> should trigger resize of whole table region!
//				super.moveSelected(mouseTrX, mouseTrY, firstMove);
			
			int sm = canvas.getMouseListener().getCurrentMoveStateMask();
			boolean isCtrl = CanvasKeys.isKeyDown(sm, SWT.CTRL);
			boolean isAlt = CanvasKeys.isKeyDown(sm, SWT.ALT);
			if (isCtrl) {
				return moveTableRowOrColumnCells(shape, mouseTrX, mouseTrY, !isAlt, currentMoveOp, addToUndoStack);
			} else
				return moveTableCell(shape, mouseTrX, mouseTrY, currentMoveOp, addToUndoStack);
		} else {
			return moveShapeDefault(shape, mouseTrX, mouseTrY, currentMoveOp, addToUndoStack);
		}
	}

	public SWTCanvas getCanvas() {
		return canvas;
	}

	public List<ICanvasShape> getDrawnShapes() {
		return drawnShapes;
	}

	public List<java.awt.Point> getDrawnPoints() {
		return drawnPoints;
	}
	
	public void removeShapesWithData(List<ITrpShapeType> shapes, boolean addToUndoStack) {
		List<ICanvasShape> css = new ArrayList<>();
		for (ITrpShapeType s : shapes) {
			ICanvasShape cs = canvas.getScene().findShapeWithData(s);
			if (cs != null) {
				css.add(cs);		
			}
		}
		
		removeShapesFromCanvas(css, addToUndoStack);
	}

	/**
	 * If shape is a baseline, select parent line and split it, s.t. undlerying baseline gets splits too
	 * and then try to select the first baseline split
	 */
	private List<ShapeEditOperation> splitBaseline(ICanvasShape shape, CanvasPolyline pl) {
			if (shape == null || !(shape.getData() instanceof TrpBaselineType))
				return null;
			
			TrpBaselineType bl = (TrpBaselineType) shape.getData();
			
			scene.clearSelected();
			scene.selectObjectWithData(bl.getLine(), false, false);
			
	//		logger.debug("Parent = "+selected.getParent()); // IS NULL...			
	//		scene.selectObject(selected.getParent(), false, false);
	
			List<ShapeEditOperation> splitOps = splitShapeDefault(canvas.getFirstSelected(), pl, true);
			
			// try to select first split of baseline:
			if (splitOps != null) {
				logger.debug("trying to select left baseline split, nr of ops = "+splitOps.size());
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
			//if splitOps == null: 
			else{
				/*
				 * line was not intersected by split line but baseline is
				 * hence delete the parent line and add a poly rectangle line for the selected baseline and
				 * than restart the baseline/line split
				 */
				if (shape.intersectionPoints(pl, true).size() > 0){
					//First step: remove parent shape (=line),
					scene.clearSelected();
					canvas.getScene().removeShape(shape.getParent(), false, true);
					//Second step: add parent as a poly rectangle
					CanvasMode rememberMode = canvas.getMode();
					canvas.setMode(CanvasMode.ADD_LINE); 
					CanvasPolyline baselineShape = (CanvasPolyline) shape;
					ICanvasShape shapeOfParent = baselineShape.getDefaultPolyRectangle();
					ShapeEditOperation op = canvas.getShapeEditor().addShapeToCanvas(shapeOfParent, true);
					if (op != null){
						shape.setParentAndAddAsChild(shapeOfParent);
						((TrpBaselineType)shape.getData()).reInsertIntoParent();
					}
	
					//mw.getShapeFactory().syncCanvasShapeAndTrpShape(shapeOfParent, parent);
					canvas.setMode(rememberMode);
					canvas.getScene().selectObject(shapeOfParent, true, false);
					shapeOfParent.setSelected(true);

					//After that the normal line split can be called
					List<ShapeEditOperation> splitOps2 = splitShapeDefault(canvas.getFirstSelected(), pl, true);
					
					// try to select first split of baseline:
					if (splitOps2 != null) {
						logger.debug("trying to select left baseline split, nr of ops = "+splitOps2.size());
						for (ShapeEditOperation o : splitOps2) {
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
				}
			}
			
			return splitOps;
		}

	private List<ShapeEditOperation> splitTable(TrpTableRegionType table, CanvasPolyline pl, boolean addToUndoStack) {
			// search for row / col cells to split according to given polyline:
	//		Pair<SplitDirection, List<TrpTableCellType>> splittableCells = TableUtils.getSplittableCells(x1, y1, x2, y2, table);
			SplittableCellsStruct splittableCells = TableUtils.getSplittableCells(pl, table);
			if (splittableCells == null) {
				logger.debug("cells not splittable with this polyline!");
				return null;
			}
			
			TableDimension dir = splittableCells.dir;
			int pi = dir.val;
			String entityName = dir==TableDimension.COLUMN?"column":"row";
	
			TableShapeEditOperation splitOp = new TableShapeEditOperation("Added a new table "+entityName);
			
			logger.debug("n-splittableCells: "+splittableCells.cells.size());
			
			for (TrpTableCellType c : splittableCells.cells) {
				List<ShapeEditOperation> splitOps4Cell = splitShapeDefault((ICanvasShape) c.getData(), pl, false);
				splitOp.addNestedOps(splitOps4Cell);
				splitOp.addCellBackup(c);
			}
				
			// adjust indexes on table:
			int insertIndex = splittableCells.index;		
			java.util.Iterator<ShapeEditOperation> it = splitOp.getNestedOpsDescendingIterator();
			while (it.hasNext()) {
				ShapeEditOperation op = it.next();
				ICanvasShape s1 = op.getNewShapes().get(0);
				ICanvasShape s2 = op.getNewShapes().get(1);
				
				if (!(s1.getData() instanceof TrpTableCellType))
					continue;
				
				logger.trace("t-op = "+op);
				
				TrpTableCellType tc1 = (TrpTableCellType) s1.getData();
				TrpTableCellType tc2 = (TrpTableCellType) s2.getData();
				
				// set span to 1 for left / upper part of splitted cell:
				int diff = 0;
				if (tc1.getPos()[pi] < insertIndex)
					diff = insertIndex-tc1.getPos()[pi];
				
				tc1.setSpan(pi, 1+diff);
				
				// set position to -1 for right / lower part of splitted cell (to correct it below!)
				tc2.setPos(pi, -1);
				tc2.setSpan(pi, tc2.getSpan()[pi]-diff);
				
	//			splitOp.addCellBackup(tc2);
				logger.trace("tc2 = "+tc2);
				
	//			if (dir == SplitDirection.HORIZONAL) {
	//				tc2.setCol(-1);
	//			} else {
	//				tc2.setRow(-1);
	//			}
	//			op.data = splittableCells.getLeft();
			}
			
			// correct position values 
			for (TrpTableCellType tc : table.getTrpTableCell()) {
				logger.trace("tc: "+tc);
				
				if (dir == TableDimension.COLUMN) {
					if (tc.getCol() > insertIndex) {
						splitOp.addCellBackup(tc);
						tc.setCol(tc.getCol()+1);
					} 
					else if (tc.getCol() == -1) {
						tc.setCol(insertIndex+1);
					}
				} else {
					if (tc.getRow() > insertIndex) {
						splitOp.addCellBackup(tc);
						tc.setRow(tc.getRow()+1);
					} else if (tc.getRow() == -1) {
						tc.setRow(insertIndex+1);
					}	
				}
			}
			
	//		// create custom edit operation to recover cell location values
	//		ShapeEditOperation opC = new ShapeEditOperation(ShapeEditType.CUSTOM, "Added a new table "+entityName) {
	//			@Override protected void customUndoOperation() {
	//				TableUtils.recoverTableCellValues(backup);
	//			}
	//		};
	//		splitOps.add(0, opC); // add operation to front, s.t. undo will be performed at last
			
			// add to undo stack:
			if (addToUndoStack)
				addToUndoStack(splitOp);
			
			TableUtils.checkTableConsistency(table);
			
			List<ShapeEditOperation> ops = new ArrayList<>();
			ops.add(splitOp);
		
			TableUtils.selectCells(canvas, table, insertIndex, dir, false);
	
			return ops;
		}
	
	private List<ShapeEditOperation> splitShapeDefault(ICanvasShape shape, CanvasPolyline pl, boolean addToUndoStack) {
	//		ICanvasShape selected = canvas.getFirstSelected();
		if (shape == null) {
			logger.warn("Cannot split - no shape selected!");
			return null;
		}
		
		List<ICanvasShape> children = shape.getChildren(true); // get all children (recursively)!
		List<ShapeEditOperation> splitOps = new ArrayList<>();
				
		ShapeEditOperation op = scene.splitShape(shape, pl, true, null, null, false);
		if (op!=null) {
			splitOps.add(op);
		}
		else{
			//means that parent was not able to be split and hence also no childs should be splitted
			return null;
			
		}
		
		// Split all child shapes
		for (ICanvasShape child : children) {
			//logger.debug("split ALL childs: " + child.getType());
			// Determine the parent shapes of the child shape that shall be splitted by iterating through the edit operations that were done so far
			ICanvasShape p1=null, p2=null;
			for (ShapeEditOperation opParent : splitOps) {
				if (opParent.getNewShapes().get(0).equals(child.getParent()) 
						|| opParent.getNewShapes().get(1).equals(child.getParent())) {
					p1 = opParent.getNewShapes().get(0);
					p2 = opParent.getNewShapes().get(1);
						//logger.debug("readjusting child elements - parents: "+p1+"/"+p2);
					break;
				}
			}
			
			// Try to split child shape using the parents that were just determined:
			//logger.debug("p1 / p2 = "+p1+" / "+p2);
			ShapeEditOperation opChild = scene.splitShape(child, pl, true, p1, p2, true);
			if (opChild!=null) {
				splitOps.add(opChild);
			}
		}
		
		if (splitOps.isEmpty()) {
			logger.warn("Cannot split - no shapes actually splitted by line!");
			return null;
		}
		
		scene.notifyOnAfterShapeSplitted(op);
		
		if (addToUndoStack)
			addToUndoStack(splitOps);
		
		return splitOps;
	//		return super.splitShape(shape, pl, addToUndoStack);
		
	}

	public List<ShapeEditOperation> splitShape(ICanvasShape shape, CanvasPolyline pl, boolean addToUndoStack) {
		try {
	//		ICanvasShape selected = canvas.getFirstSelected();
			if (shape == null) {
				logger.warn("Cannot split - no shape selected!");
				return null;
			}
			
			TrpTableRegionType table = TableUtils.getTable(shape, true);
			
			if (shape.getData() instanceof TrpBaselineType) {
				return splitBaseline(shape, pl);
			}
			else if (table != null) {
				return splitTable(table, pl, addToUndoStack);
			}
			else {
				return splitShapeDefault(shape, pl, addToUndoStack);
			}
		} catch (Exception e) {
//			logger.debug("error", e);
			TrpMainWidget.getInstance().onError("Error splitting", e.getMessage(), e);
			return null;
		}
	}

	public ShapeEditOperation translateTableRowOrColumnPoints(ICanvasShape selected, int side, int tx, int ty, boolean addToUndoStack) {
			logger.debug("moveTableRowOrColumn, side: "+side);
			
			TrpTableCellType tc = TableUtils.getTableCell(selected);
			if (tc == null)
				return null;
			
			if (side < 0 || side >= 4)
				return null;
	
			// get all neighbor cells in this row / column and move their points according to the side
			boolean rowwise = side==1 || side == 3;
			boolean isLeftOrTopSide = side==0 || side == 3; // if left or top side
			int index;
			if (rowwise) {
				index = isLeftOrTopSide ? tc.getRow() : tc.getRowEnd();
			} else {
				index = isLeftOrTopSide ? tc.getCol() : tc.getColEnd();
			}
			
			String entityName = rowwise?"row":"column";
			
	//		List<ShapeEditOperation> ops = new ArrayList<>();
			ShapeEditOperation op = new ShapeEditOperation(ShapeEditType.EDIT, "Moved table "+entityName+" points");
			
			
			List<TrpTableCellType> cells = tc.getTable().getCells(rowwise, isLeftOrTopSide?GetCellsType.START_INDEX:GetCellsType.END_INDEX, index);
	//		List<TrpTableCellType> cells = tc.getTable().getCells(rowwise, GetCellsType.OVERLAP, index);
			
			Set<String> done = new HashSet<>(); // stores cell/pt combinations already moved
			for (TrpTableCellType c : cells) {
				CanvasQuadPolygon s = (CanvasQuadPolygon) c.getData();
	
				// first, move all common points of neighbors too
				for (TrpTableCellType n : c.getNeighborCells()) {
					CanvasQuadPolygon ns = (CanvasQuadPolygon) n.getData();
					
					ShapeEditOperation opN = new ShapeEditOperation(ShapeEditType.EDIT, "Moved table "+entityName+" points", ns);
					
					int ptsMovedCounter=0;
					for (java.awt.Point pt : s.getPointsOfSegment(side, true)) {
						int i = ns.getPointIndex(pt.x, pt.y);
						if (i != -1) {
							if (done.add(n.getId()+"_"+i)) {
								ns.translatePoint(i, tx, ty);
	//							ns.movePoint(i, pt.x+trans.x, pt.y+trans.y);
								++ptsMovedCounter;
							}
						}
					}
					
					if (ptsMovedCounter>0) {
						op.addNestedOp(opN);
					}
				}
				
				// now, move all points of that side (if not already moved!)
				ShapeEditOperation opM = new ShapeEditOperation(ShapeEditType.EDIT, "Moved table "+entityName+" points", s);					
				op.addNestedOp(opM);
				
				for (java.awt.Point pt : s.getPointsOfSegment(side, true)) {
					int i = s.getPointIndex(pt.x, pt.y);
					if (i != -1) {
						if (done.add(c.getId()+"_"+i)) {
							s.translatePoint(i, tx, ty);
	//						s.movePoint(i, pt.x+trans.x, pt.y+trans.y);
						}
					}
				}
			}
			
			if (addToUndoStack)
				addToUndoStack(op);
					
			return op;
		}

	public ShapeEditOperation moveTableRowOrColumnPoints(ICanvasShape selected, int selectedPoint, int mouseX, int mouseY, boolean addToUndoStack, boolean rowwise) {
			logger.debug("moveTableRowOrColumn, rowwise: "+rowwise);
			
			TrpTableCellType tc = TableUtils.getTableCell(selected);
			if (tc == null)
				return null;
			
			CanvasQuadPolygon qp = (CanvasQuadPolygon) selected;
			
			// determine side of quad poly where this point is on:
			int side = qp.getPointSide(selectedPoint);
			
			// jump out if combination of side and rowwise flag is incompatible:
			boolean isCornerPt = qp.isCornerPoint(selectedPoint);
			if (!isCornerPt && side%2==0 && rowwise) { // if pt on left or right
				logger.debug("cannot use non-corner-point from left or right side to move pts rowwise!");
				return null;
			} else if (!isCornerPt && side%2==1 && !rowwise) {
				logger.debug("cannot use non-corner-point from top or bottom side to move pts columnwise!");
				return null;
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
				return null;
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
			
			String entityName = rowwise?"row":"column";
			
	//		List<ShapeEditOperation> ops = new ArrayList<>();
			ShapeEditOperation op = new ShapeEditOperation(ShapeEditType.EDIT, "Moved table "+entityName+" points");
			
			
			List<TrpTableCellType> cells = tc.getTable().getCells(rowwise, startIndex?GetCellsType.START_INDEX:GetCellsType.END_INDEX, index);
	//		List<TrpTableCellType> cells = tc.getTable().getCells(rowwise, GetCellsType.OVERLAP, index);
			
			Set<String> done = new HashSet<>(); // stores cell/pt combinations already moved
			for (TrpTableCellType c : cells) {
				CanvasQuadPolygon s = (CanvasQuadPolygon) c.getData();
	
				// first, move all common points of neighbors
				for (TrpTableCellType n : c.getNeighborCells()) {
					CanvasQuadPolygon ns = (CanvasQuadPolygon) n.getData();
					
					ShapeEditOperation opN = new ShapeEditOperation(ShapeEditType.EDIT, "Moved table "+entityName+" points", ns);
					
					int ptsMovedCounter=0;
					for (java.awt.Point pt : s.getPointsOfSegment(side, true)) {
						int i = ns.getPointIndex(pt.x, pt.y);
						if (i != -1) {
							if (done.add(n.getId()+"_"+i)) {
								ns.movePoint(i, pt.x+trans.x, pt.y+trans.y);
								++ptsMovedCounter;
							}
						}
					}
					
					if (ptsMovedCounter>0) {
						op.addNestedOp(opN);
					}
				}
				
				// now, move all points of that side of that shape (if not already moved!)
				ShapeEditOperation opM = new ShapeEditOperation(ShapeEditType.EDIT, "Moved table "+entityName+" points", s);					
				op.addNestedOp(opM);
				
				for (java.awt.Point pt : s.getPointsOfSegment(side, true)) {
					int i = s.getPointIndex(pt.x, pt.y);
					if (i != -1) {
						if (done.add(c.getId()+"_"+i)) {
							s.movePoint(s.getPointIndex(pt.x, pt.y), pt.x+trans.x, pt.y+trans.y);
						}
					}
				}
	
			}
			
			if (addToUndoStack)
				addToUndoStack(op);
			
			return op;
		}

	public ShapeEditOperation moveTableCellPoints(ICanvasShape cellShape, int selectedPoint, int mouseX, int mouseY, boolean addToUndoStack) {
		ICanvasShape cellShapeCopy = cellShape.copy();
		
		// First move selected pt(s), then move affected points from neighbors too
		ShapeEditOperation op = new ShapeEditOperation(ShapeEditType.EDIT, "Moved point(s) of table cell", cellShape);					
	
		Point mousePtWoTr = canvas.inverseTransform(mouseX, mouseY);
		List<Integer> pts = cellShape.movePointAndSelected(selectedPoint, mousePtWoTr.x, mousePtWoTr.y);
	
		TrpTableCellType c = (TrpTableCellType) cellShape.getData();
		List<TrpTableCellType> neighbors = c.getNeighborCells();
		logger.debug("n-neighbors: "+neighbors.size());
		
		for (TrpTableCellType n : neighbors) {
			ICanvasShape ns = (ICanvasShape) n.getData();
			for (int i : pts) {
				java.awt.Point pOld = cellShapeCopy.getPoint(i);
				java.awt.Point pNew = cellShape.getPoint(i);
				logger.debug("pOld = "+pOld+" pNew = "+pNew);
				
				if (pOld != null && pOld != null) {
					int j = ns.getPointIndex(pOld.x, pOld.y);
					logger.debug("j = "+j);
					if (j != -1) {
						ShapeEditOperation opC = new ShapeEditOperation(ShapeEditType.EDIT, "Moved point(s) of table cell", ns);					
						op.addNestedOp(opC);
						
						logger.debug("moved point "+j+" in cell "+n.print());
						ns.movePoint(j, pNew.x, pNew.y);
					}
				}
			}
		}
		
		if (addToUndoStack)
			addToUndoStack(op);
		
		return op;
	}

	public ShapeEditOperation translateTableCellPoints(ICanvasShape cellShape, int tx, int ty, boolean addToUndoStack, int ...indices) {
			ICanvasShape cellShapeCopy = cellShape.copy();
			
			// First move selected pt(s), then move affected points from neighbors too
			ShapeEditOperation op = new ShapeEditOperation(ShapeEditType.EDIT, "Altered point(s) of table cell", cellShape);
			
			for (int index : indices) {
				cellShape.translatePoint(index, tx, ty);
			}
	
	//		Point mousePtWoTr = canvas.inverseTransform(tx, tx);
	//		List<Integer> pts = cellShape.movePointAndSelected(selectedPoint, mousePtWoTr.x, mousePtWoTr.y);
	
			TrpTableCellType c = (TrpTableCellType) cellShape.getData();
			List<TrpTableCellType> neighbors = c.getNeighborCells();
			logger.debug("n-neighbors: "+neighbors.size());
			
			for (TrpTableCellType n : neighbors) {
				ICanvasShape ns = (ICanvasShape) n.getData();
				for (int i : indices) {
					java.awt.Point pOld = cellShapeCopy.getPoint(i);
					java.awt.Point pNew = cellShape.getPoint(i);
					logger.debug("pOld = "+pOld+" pNew = "+pNew);
					
					if (pOld != null && pOld != null) {
						int j = ns.getPointIndex(pOld.x, pOld.y);
						logger.debug("j = "+j);
						if (j != -1) {
							ShapeEditOperation opC = new ShapeEditOperation(ShapeEditType.EDIT, "Moved point(s) of table cell", ns);					
							op.addNestedOp(opC);
							
							logger.debug("moved point "+j+" in cell "+n.print());
							ns.movePoint(j, pNew.x, pNew.y);
						}
					}
				}
			}
			
			if (addToUndoStack)
				addToUndoStack(op);
			
			return op;
		}

	private ShapeEditOperation addPointToTableCell(ICanvasShape shape, int mouseX, int mouseY, boolean addToUndoStack) {
			logger.debug("adding pt to neighbor table cell!");
			
			final CanvasQuadPolygon qp = (CanvasQuadPolygon) shape;
			
			final Point mousePtWoTr = canvas.inverseTransform(mouseX, mouseY);
			
	//		List<ShapeEditOperation> ops = new ArrayList<>();
			ShapeEditOperation op = new ShapeEditOperation(ShapeEditType.EDIT, "Added point to shape", shape);
	//		ops.add(op);
			
			int ii = shape.insertPoint(mousePtWoTr.x, mousePtWoTr.y);
			int ptIndex = shape.getPointIndex(mousePtWoTr.x, mousePtWoTr.y);
			if (ptIndex == -1)
				return null;
			
			int side = qp.getPointSide(ptIndex);
			int sideOpposite = (side+2) % 4; 
			
			logger.debug("add pt, side: "+side+" sideOpposite = "+sideOpposite);
			
			TrpTableCellType c = (TrpTableCellType) shape.getData();
			
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
	
				ShapeEditOperation opN = new ShapeEditOperation(ShapeEditType.EDIT, "Added point to shape", nc);
				op.addNestedOp(opN);
	//			ops.add(opN);
				
				nc.insertPointOnSide(mousePtWoTr.x, mousePtWoTr.y, sideOpposite);
			}
			
			op.data = ii;
					
			if (addToUndoStack)
				addToUndoStack(op);
			
			return op;
		}

	private ShapeEditOperation removePointFromTableCell(ICanvasShape cellShape, int pointIndex, boolean addToUndoStack) {
			CanvasQuadPolygon qp = (CanvasQuadPolygon) cellShape;
			int side = qp.getPointSide(pointIndex);
			if (side == -1) {
				logger.warn("Cannot find side of point to remove: "+pointIndex);
				return null;
			}
			
			TrpTableCellType c = (TrpTableCellType) cellShape.getData();
			List<TrpTableCellType> neighbors = c.getNeighborCells(side);
			
			logger.debug("remove pt, side: "+side+", n-neighbors: "+neighbors.size());
			
			java.awt.Point pt2Remove = cellShape.getPoint(pointIndex);
			if (pt2Remove == null) {
				logger.warn("Cannot find point to remove for pointIndex = "+pointIndex);
				return null;			
			}
	
			// check point can be removed from neighbor, jump out if not
			for (TrpTableCellType neighbor : neighbors) {
				CanvasQuadPolygon nc = (CanvasQuadPolygon) neighbor.getData();			
				int ri = nc.getPointIndex(pt2Remove.x, pt2Remove.y);
				if (ri != -1 && !nc.isPointRemovePossible(ri)) {
					return null;
				}
			}
			
			// remove point from main shape:
	//		List<ShapeEditOperation> ops = new ArrayList<>();
			ShapeEditOperation op = new ShapeEditOperation(ShapeEditType.EDIT, "Removed point from shape", cellShape);
	//		ops.add(op);
			
			if (!cellShape.removePoint(pointIndex)) {
				logger.warn("Could not remove point "+pointIndex+" from shape!");
				return null;
			}
	
			// remove point from neighbor cells
			for (TrpTableCellType neighbor : neighbors) {
				CanvasQuadPolygon nc = (CanvasQuadPolygon) neighbor.getData();
	
				int ri = nc.getPointIndex(pt2Remove.x, pt2Remove.y);
				if (ri != -1) {
					ShapeEditOperation opN = new ShapeEditOperation(ShapeEditType.EDIT, "Removed point from shape", nc);
	//				ops.add(opN);
					if (nc.removePoint(ri)) {
						op.addNestedOp(opN);
						
	//					ops.add(opN);
					}
				}
			}
			
			if (addToUndoStack)
				addToUndoStack(op);
			
			return op;
		}
	
	public ShapeEditOperation moveTableRowOrColumnCells(ICanvasShape shape, int mouseTrX, int mouseTrY, boolean row, ShapeEditOperation currentMoveOp, boolean addToUndoStack) {
			TrpTableCellType selectedCell = TableUtils.getTableCell(shape);
			if (selectedCell == null)
				return null;
			
			String entityName = row ? "row" : "column";
			
			boolean firstMove = currentMoveOp==null;
			int pi = row ? 0 : 1;
			TrpTableRegionType table = selectedCell.getTable();
			
			if (firstMove) {
	//			List<TrpTableCellType> cells = table.getCells(row, GetCellsType.OVERLAP, selectedCell.getPos()[pi]);
				List<TrpTableCellType> cells = table.getCells(row, GetCellsType.OVERLAP, selectedCell.getPos()[pi], selectedCell.getSpan()[pi]);
				currentMoveOp = new ShapeEditOperation(ShapeEditType.CUSTOM, "Move table "+entityName+" cells");
				currentMoveOp.data = cells;
			}
			
			List<TrpTableCellType> cells = (List<TrpTableCellType>) currentMoveOp.data;
			Set<String> doneNeighborPts = new HashSet<>();
			
			for (TrpTableCellType c : cells) {
				CanvasQuadPolygon qp = (CanvasQuadPolygon) c.getData();
				
				List<java.awt.Point> oldPts = qp.getPoints();
				
				ShapeEditOperation moveOp = currentMoveOp.findNestedOp(qp);
				moveOp = moveShapeDefault(qp, mouseTrX, mouseTrY, moveOp, false);
				if (moveOp == null)
					return null;
				
				List<ShapeEditOperation> movePtsOps = new ArrayList<>();
				for (int i=0; i<oldPts.size(); ++i) {
					java.awt.Point p = oldPts.get(i);
					List<Pair<Integer, TrpTableCellType>> pon = c.getCommonPointsOnNeighborCells(p.x, p.y);
					
					for (Pair<Integer, TrpTableCellType> ponc : pon) {
						if (cells.contains(ponc.getRight())) // do not move pts of cells that get moved anyway
							continue;
						if (doneNeighborPts.contains(ponc.getLeft()+"_"+ponc.getRight().getId())) // do not move any pts twice
							continue;
						
						doneNeighborPts.add(ponc.getLeft()+"_"+ponc.getRight().getId());
						
						CanvasQuadPolygon qpn = (CanvasQuadPolygon) ponc.getRight().getData();
										
						ShapeEditOperation movePtOp = new ShapeEditOperation(ShapeEditType.EDIT, "Moved point", qpn);
						movePtsOps.add(movePtOp);
						
						java.awt.Point newPt = qp.getPoint(i);		
						qpn.movePoint(ponc.getLeft(), newPt.x, newPt.y);
					}
				}
				
				if (firstMove) {
					moveOp.addNestedOps(movePtsOps);
					currentMoveOp.addNestedOp(moveOp);
				}
			}
			
			if (firstMove && addToUndoStack) {
				addToUndoStack(currentMoveOp);
			}
			
			return currentMoveOp;
		}

	private ShapeEditOperation moveTableCell(ICanvasShape shape, int mouseTrX, int mouseTrY, ShapeEditOperation currentMoveOp, boolean addToUndoStack) {
			logger.debug("moving table cell: "+shape+" currentMoveOp: "+currentMoveOp);
			TrpTableCellType cell = TableUtils.getTableCell(shape);
			if (cell == null)
				return null;
			
			CanvasQuadPolygon qp = (CanvasQuadPolygon) cell.getData();
			List<java.awt.Point> oldPts = qp.getPoints();
			
			boolean firstMove = currentMoveOp == null;
	
			currentMoveOp = moveShapeDefault(shape, mouseTrX, mouseTrY, currentMoveOp, false);
			if (currentMoveOp == null)
				return null;
					
	//		CanvasQuadPolygon qp = (CanvasQuadPolygon) currentMoveOp.getShapes().get(0);
	//		CanvasQuadPolygon qpb = (CanvasQuadPolygon) currentMoveOp.getBackupShapes().get(0);
	
			// move pts of neighboring cells:
			List<ShapeEditOperation> ops = new ArrayList<>();
			for (int i=0; i<oldPts.size(); ++i) {
				java.awt.Point p = oldPts.get(i);
				List<Pair<Integer, TrpTableCellType>> pon = cell.getCommonPointsOnNeighborCells(p.x, p.y);
				logger.trace("pon.size() = "+pon.size());
				for (Pair<Integer, TrpTableCellType> ponc : pon) {
					CanvasQuadPolygon qpn = (CanvasQuadPolygon) ponc.getRight().getData();
									
					ShapeEditOperation op = new ShapeEditOperation(ShapeEditType.EDIT, "Moved point", qpn);
					ops.add(op);
					
					java.awt.Point newPt = qp.getPoint(i);		
					qpn.movePoint(ponc.getLeft(), newPt.x, newPt.y);
				}
			}
			
			if (firstMove) {
				currentMoveOp.addNestedOps(ops);
				if (addToUndoStack) {
					addToUndoStack(currentMoveOp);	
				}
			}
			
			return currentMoveOp;
		}

	public ShapeEditOperation getCurrentMoveOp() {
		return currentMoveOp;
	}

	public ICanvasShape getBackupShape() {
		return backupShape;
	}

	
	
	
	/**
	 * 
	 * @param shapes
	 * @param bf
	 * @param keepExisting keep the existing border
	 * @param addToUndoStack
	 * @return
	 */
	public TableShapeEditOperation applyBorderToSelectedTableCells(List<ICanvasShape> shapes, BorderFlags bf, boolean keepExisting, boolean addToUndoStack) {
		if (!TableUtils.isTableCells(shapes)) {
			return null;
		}
		logger.debug("changing border type for table "+shapes.size()+", bf: "+bf+" keepExisting: "+keepExisting);
		
		TableShapeEditOperation op = new TableShapeEditOperation("Changed border type for "+shapes.size()+" table cells "
				+ "and the respective neighboring cells");
						
		for (ICanvasShape s : shapes) {
			TrpTableCellType c = (TrpTableCellType) s.getData();
			op.addCellBackup(c);
			
			if (!TableUtils.hasLeftNeighbor(c, shapes)) {
				c.setLeftBorderVisible(keepExisting ? (bf.vertLeft || c.isLeftBorderVisible()) : bf.vertLeft);
				for (TrpTableCellType n : c.getNeighborCells(0)) {
					op.addCellBackup(n);
					n.setRightBorderVisible(keepExisting ? (bf.vertLeft || n.isRightBorderVisible()) : bf.vertLeft);
				}
			} else {
				c.setLeftBorderVisible(keepExisting ? (bf.vertInner || c.isLeftBorderVisible()) : bf.vertInner);
			}
			
			if (!TableUtils.hasRightNeighbor(c, shapes)) {
				c.setRightBorderVisible(keepExisting ? (bf.vertRight || c.isRightBorderVisible()) : bf.vertRight);
				for (TrpTableCellType n : c.getNeighborCells(2)) {
					op.addCellBackup(n);
					n.setLeftBorderVisible(keepExisting ? (bf.vertRight || n.isRightBorderVisible()) : bf.vertRight);	
				}
			}
			else {
				c.setRightBorderVisible(keepExisting ? (bf.vertInner || c.isRightBorderVisible()) : bf.vertInner);
			}
			
			if (!TableUtils.hasBottomNeighbor(c, shapes)) {
				c.setBottomBorderVisible(keepExisting ? (bf.horBottom || c.isBottomBorderVisible()) : bf.horBottom);
				for (TrpTableCellType n : c.getNeighborCells(1)) {
					op.addCellBackup(n);
					n.setTopBorderVisible(keepExisting ? (bf.horBottom || n.isBottomBorderVisible()) : bf.horBottom);				
				}
			}
			else {
				c.setBottomBorderVisible(keepExisting ? (bf.horInner || c.isBottomBorderVisible()) : bf.horInner);
			}
			
			if (!TableUtils.hasTopNeighbor(c, shapes)) {
				c.setTopBorderVisible(keepExisting ? (bf.horTop || c.isTopBorderVisible()) : bf.horTop);
				for (TrpTableCellType n : c.getNeighborCells(3)) {
					op.addCellBackup(n);
					n.setBottomBorderVisible(keepExisting ? (bf.horTop || n.isBottomBorderVisible()) : bf.horTop);
				}
			}
			else {
				c.setTopBorderVisible(keepExisting ? (bf.horInner || c.isTopBorderVisible()) : bf.horInner);
			}
		}
		
		if (addToUndoStack)
			addToUndoStack(op);
		
		// notify observers
		scene.notifyOnShapeBorderEdited(op);
		
		return op;
	}
	
	/**
	 * 
	 * @param shapes
	 * @param bf
	 * @param keepExisting keep the existing border
	 * @param addToUndoStack
	 * @return
	 */
	public TableShapeEditOperation subtractBorderFromSelectedTableCells(List<ICanvasShape> shapes, BorderFlags bf, boolean addToUndoStack) {
		if (!TableUtils.isTableCells(shapes)) {
			return null;
		}
		logger.debug("subtracting border type for table "+shapes.size()+", bf: "+bf);
		
		TableShapeEditOperation op = new TableShapeEditOperation("Changed border type for "+shapes.size()+" table cells "
				+ "and the respective neighboring cells");
						
		for (ICanvasShape s : shapes) {
			TrpTableCellType c = (TrpTableCellType) s.getData();
			op.addCellBackup(c);
			
			if (!TableUtils.hasLeftNeighbor(c, shapes)) {
				c.setLeftBorderVisible((bf.vertLeft && c.isLeftBorderVisible()) ? false : c.isLeftBorderVisible());
				for (TrpTableCellType n : c.getNeighborCells(0)) {
					op.addCellBackup(n);
					n.setRightBorderVisible((bf.vertLeft && n.isRightBorderVisible()) ? false : n.isRightBorderVisible());
				}
			} else {
				c.setLeftBorderVisible((bf.vertInner && c.isLeftBorderVisible()) ? false : c.isLeftBorderVisible());
			}
			
			if (!TableUtils.hasRightNeighbor(c, shapes)) {
				c.setRightBorderVisible((bf.vertRight && c.isRightBorderVisible())  ? false : c.isRightBorderVisible());
				for (TrpTableCellType n : c.getNeighborCells(2)) {
					op.addCellBackup(n);
					n.setLeftBorderVisible((bf.vertRight && n.isRightBorderVisible())  ? false : n.isRightBorderVisible());	
				}
			}
			else {
				c.setRightBorderVisible((bf.vertInner && c.isRightBorderVisible()) ? false : c.isRightBorderVisible());
			}
			
			if (!TableUtils.hasBottomNeighbor(c, shapes)) {
				c.setBottomBorderVisible((bf.horBottom && c.isBottomBorderVisible()) ? false: c.isBottomBorderVisible());
				for (TrpTableCellType n : c.getNeighborCells(1)) {
					op.addCellBackup(n);
					n.setTopBorderVisible((bf.horBottom && n.isBottomBorderVisible()) ? false : n.isTopBorderVisible());				
				}
			}
			else {
				c.setBottomBorderVisible((bf.horInner && c.isBottomBorderVisible()) ? false : c.isBottomBorderVisible());
			}
			
			if (!TableUtils.hasTopNeighbor(c, shapes)) {
				c.setTopBorderVisible((bf.horTop && c.isTopBorderVisible()) ? false : c.isTopBorderVisible());
				for (TrpTableCellType n : c.getNeighborCells(3)) {
					op.addCellBackup(n);
					n.setBottomBorderVisible((bf.horTop && n.isBottomBorderVisible()) ? false : n.isBottomBorderVisible());
				}
			}
			else {
				c.setTopBorderVisible((bf.horInner && c.isTopBorderVisible()) ? false : c.isTopBorderVisible());
			}
		}
		
		if (addToUndoStack)
			addToUndoStack(op);
		
		// notify observers
		scene.notifyOnShapeBorderEdited(op);
		
		return op;
	}

	
	public BorderFlags retrieveExistingBordersForTableCells(List<ICanvasShape> shapes) {
		BorderFlags bf = new BorderFlags();
		for (ICanvasShape s : shapes) {
			TrpTableCellType c = (TrpTableCellType) s.getData();
			
			if (!TableUtils.hasLeftNeighbor(c, shapes)) {
				bf.vertLeft = (bf.vertLeft || c.isLeftBorderVisible());
				for (TrpTableCellType n : c.getNeighborCells(0)) {
					bf.vertLeft = (bf.vertLeft || n.isRightBorderVisible());
				}
			} else {
				bf.vertInner = (bf.vertInner || c.isLeftBorderVisible());
			}
			
			if (!TableUtils.hasRightNeighbor(c, shapes)) {
				bf.vertRight = (bf.vertRight || c.isRightBorderVisible());
				for (TrpTableCellType n : c.getNeighborCells(2)) {
					bf.vertRight = (bf.vertRight || n.isLeftBorderVisible());	
				}
			}
			else {
				bf.vertInner = (bf.vertInner || c.isRightBorderVisible());
			}
			
			if (!TableUtils.hasBottomNeighbor(c, shapes)) {
				bf.horBottom = (bf.horBottom || c.isBottomBorderVisible());
				for (TrpTableCellType n : c.getNeighborCells(1)) {
					bf.horBottom = (bf.horBottom || n.isTopBorderVisible());				
				}
			}
			else {
				bf.horInner = (bf.horInner || c.isBottomBorderVisible());
			}
			
			if (!TableUtils.hasTopNeighbor(c, shapes)) {
				bf.horTop = (bf.horTop || c.isTopBorderVisible());
				for (TrpTableCellType n : c.getNeighborCells(3)) {
					bf.horTop = (bf.horTop || n.isBottomBorderVisible());
				}
			}
			else {
				bf.horInner = (bf.horInner || c.isTopBorderVisible());
			}
		}
	
		// notify observers
		//scene.notifyOnShapeBorderRetrieval(shapes, bf);
		
		return bf;
	}

	public ShapeEditOperation mergeSelectedTableCells(List<ICanvasShape> shapes, boolean sendSignal, boolean addToUndoStack) {
			if (shapes.size() < 2)
				return null;
			
			if (!TableUtils.isTableCells(shapes)) {
				return null;
			}
			
			logger.debug("merging "+shapes.size()+" table cells!");
					
			if (sendSignal) {
				if (scene.notifyOnBeforeShapesMerged(shapes))
					return null;
			}
							
	//		ICanvasShape merged = selectedShapes.get(0).copy();
		
			List<CanvasQuadPolygon> toMerge = new ArrayList<>();
			
			final List<TableCellUndoData> backupCells = new ArrayList<>();
			int min[] = {(int)1e36, (int)1e36};
			int max[] = {0, 0};
			for (ICanvasShape s : shapes) {
				backupCells.add(new TableCellUndoData((TrpTableCellType) s.getData()));
				
				toMerge.add((CanvasQuadPolygon) s.copy());
				
				TrpTableCellType c = (TrpTableCellType) s.getData();
				if (c.getRow() < min[0])
					min[0] = c.getRow();
				if (c.getCol() < min[1])
					min[1] = c.getCol();
				if (c.getRowEnd() > max[0])
					max[0] = c.getRowEnd();
				if (c.getColEnd() > max[1])
					max[1] = c.getColEnd();
			}
			
			while (toMerge.size() > 1) {
				Pair<CanvasQuadPolygon, CanvasQuadPolygon> mergeable = getMergeableCells(toMerge);
				if (mergeable == null) {
					break;
				}
				
				toMerge.remove(mergeable.getLeft());
				toMerge.remove(mergeable.getRight());
				
				CanvasQuadPolygon m = (CanvasQuadPolygon) mergeable.getLeft().merge(mergeable.getRight());
				toMerge.add(0, m);
			}
			
			if (toMerge.size() > 1) {
				DialogUtil.showErrorMessageBox(canvas.getShell(), "Error merging cells", "Cannot merge cells - resulting cell must be rectangular!");
				logger.debug("cannot merge shapes, merged.size() = "+toMerge.size());
				return null;
			}
			
			ICanvasShape mergedShape = toMerge.get(0);
			
			scene.clearSelected();
			
			for (ICanvasShape s : shapes) {
				scene.removeShape(s, false, false);
				for (ICanvasShape child : s.getChildren(false)) {
					logger.debug("adding child: "+child);
					mergedShape.addChild(child);
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
			
			ShapeEditOperation opa = scene.addShape(mergedShape, null, false);
	//		logger.debug("merge added: "+opa);
			
			if (opa == null) {
				// TODO: should add removed shapes again here...
				logger.warn("unable to add merged shape: "+toMerge);
				return null;
			}
			
			ShapeEditOperation op = new ShapeEditOperation(ShapeEditType.MERGE, shapes.size()+" table cells merged", shapes) {
				@Override protected void customUndoOperation() {
					TableShapeEditOperation.recoverTableCellValues(backupCells);				
				}
			};
			op.addNewShape(mergedShape);
			
			// correct values for merged table cell:
			TrpTableCellType c = (TrpTableCellType) mergedShape.getData();
			c.setRow(min[0]);
			c.setCol(min[1]);
			c.setRowSpan(max[0]-min[0]);
			c.setColSpan(max[1]-min[1]);
			logger.debug("merged cell: "+c.print());
					
			if (sendSignal) {
				scene.notifyOnShapesMerged(op);
			}
			
			canvas.redraw();
			
			if (addToUndoStack) {
				addToUndoStack(op);	
			}
			
			TableUtils.checkTableConsistency(TableUtils.getTable(mergedShape, true));
			
			return op;
		}

//	@Override public void mergeSelected() {
//		logger.debug("mergeSelected, TrpCanvasShapeEditor");
//		
//		List<ICanvasShape> selected = scene.getSelectedAsNewArray();
//	
//		boolean isMergeTableCells = TableUtils.isTableCells(selected);
//		
//		if (isMergeTableCells) {
//			mergeSelectedTableCells(selected, true, true);
//		} else {
//			super.mergeSelected();
//		}
//				
//	}

	public ShapeEditOperation removeIntermediatePointsOfTableCell(ICanvasShape shape, boolean addToUndoStack) {
		TrpTableCellType cell = TableUtils.getTableCell(shape);
		if (cell == null)
			return null;
		
		ShapeEditOperation op = new ShapeEditOperation(ShapeEditType.CUSTOM, "Removed intermediate points of table cell");
		
		CanvasQuadPolygon qp = (CanvasQuadPolygon) cell.getData();
		
		List<java.awt.Point> pts = new ArrayList<>(qp.getPoints());
		for (java.awt.Point p : pts) {
			int i = qp.getPointIndex(p.x, p.y);
			if (i != -1) {
				ShapeEditOperation opr = removePointFromTableCell(qp, i, false);
				if (op != null) {
					op.addNestedOp(opr);
				}
			}
		}
		
		if (addToUndoStack && op.hasNestedOps()) {
			addToUndoStack(op);
		}
		
		TableUtils.checkTableConsistency(cell.getTable());
		
		return op;
	}

	public void deleteTableRowOrColumn(ICanvasShape shape, TableDimension dim, boolean addToUndoStack) {
			String entityName = dim==TableDimension.ROW ? "row" : "column";
			TrpTableCellType tc = TableUtils.getTableCell(shape);
			
			TableShapeEditOperation tableOp = new TableShapeEditOperation("Deleted a table "+entityName);
			
			logger.debug("deleting "+entityName+" of table, cell: "+tc);
			if (tc==null) {
				DialogUtil.showErrorMessageBox(getShell(), "Error removing "+entityName, "No table cell selected!");
				return;
			}
			TrpTableRegionType table = tc.getTable();
			
			int[] pos = tc.getPos();
			
			int di = dim==TableDimension.ROW ? 0 : 1;
			
			int ni = dim==TableDimension.ROW ? 3 : 0;
			
			int posIndex = pos[di];
			
			if (posIndex == 0) {
				DialogUtil.showErrorMessageBox(getShell(), "Error removing "+entityName, "Cannot remove first "+entityName);
				return;
			}
					
			// FIRST: split up all merged cells that are "in the way"
			List<TrpTableCellType> cells = table.getCells(di==0, GetCellsType.OVERLAP, posIndex);
			for (TrpTableCellType c : cells) {
				CanvasQuadPolygon qp = (CanvasQuadPolygon) c.getData();
	//			if (c.getSpan()[di] > 1) {
				if (c.isMergedCell()) {
					ShapeEditOperation splitOp = splitMergedTableCell(qp, false);
					tableOp.addNestedOp(splitOp);
				}
			}
			
			// SECOND: merge with next row or column for all cells of this row or column
			cells = table.getCells(di==0, GetCellsType.OVERLAP, posIndex);
			
			List<ICanvasShape> mergedCells = new ArrayList<>(); // the list of merged cells
			Set<String> done = new HashSet<>(); // a set of cell id's that were already merged
			for (TrpTableCellType c : cells) {
				if (done.contains(c.getId()))
					continue;
				
	//			CanvasQuadPolygon qp = (CanvasQuadPolygon) c.getData();
				if (c.getSpan()[di] > 1) {
					DialogUtil.showErrorMessageBox(getShell(), "Error", "Multi span cell: "+c);
				}
				
				List<TrpTableCellType> nc = c.getNeighborCells(ni);
				if (nc.size() > 1) {
					throw new CanvasException("More than one neighbor in row / column deletion - should not be possible: \n"+c);
				}
				if (nc.isEmpty())
					continue;
				
				// determine list of shapes to merge -> get neighbor cell, then get cells from opposite side of that neighbor to determine which cells of the deleted row / column have to be merged with that neighbor
				List<ICanvasShape> toMerge = new ArrayList<>();
				toMerge.add((ICanvasShape) nc.get(0).getData());
				
				for (TrpTableCellType c1 : nc.get(0).getNeighborCells((ni+2)%4)) {
					toMerge.add((ICanvasShape) c1.getData());
					done.add(c1.getId());
				}
							
				ShapeEditOperation mergeOp = mergeSelectedTableCells(toMerge, true, false);
				if (mergeOp == null)
					throw new CanvasException("Could not merge with neighbor cell on row / column deletion: \n"+c);
				
				tableOp.addNestedOp(mergeOp);
				
				mergedCells.add(mergeOp.getNewShapes().get(0));
	
	//			ShapeEditOperation op = removeShapeFromCanvas(qp, false);
	//			if (op != null)
	//				ops.add(op);
			}
			
			// correct span of merged cells
			if (true)
			for (ICanvasShape s : mergedCells) {
				TrpTableCellType mc = TableUtils.getTableCell(s);
				tableOp.addCellBackup(mc);
				mc.setSpan(di, mc.getSpan()[di]-1);
			}
			
			// correct row / col pos values for cells after deleted row / col
			if (true) {
			for (TrpTableCellType c : table.getTrpTableCell()) {
				tableOp.addCellBackup(c);
				int i = c.getPos()[di];
				if (i > posIndex) {
					c.setPos(di, c.getPos()[di]-1);
				}
			}
			}
					
			if (addToUndoStack)
				addToUndoStack(tableOp);
			
			TableUtils.checkTableConsistency(table);
		}

	public ShapeEditOperation splitMergedTableCell(ICanvasShape shape, boolean addToUndoStack) {
			// code below is hell on earth - don't fuck it up or the devil will haunt you!
			
			TrpTableCellType tc = TableUtils.getTableCell(shape);
			logger.debug("splitting merged table cell!");
					
			if (tc==null) {
				DialogUtil.showErrorMessageBox(getShell(), "Error splitting merged cell", "No table cell selected!");
				return null;
			}
			
			if (!tc.isMergedCell()) {
				DialogUtil.showErrorMessageBox(getShell(), "Error splitting merged cell", "This is not a merged cell!");
				return null;
			}
			
			// This class holds a list of points representing the points 
			// of the side of the mesh on the position it is stored in the pts matrix 
			class Pt {
				List<java.awt.Point> p = new ArrayList<>();
				java.awt.Point f() { return p.isEmpty()?null:p.get(0); }
				void a(java.awt.Point pA) { p.add(pA); }
				void a(double x, double y) { p.add(new java.awt.Point((int)x, (int)y)); }
				void a(Point2D pA) { p.add(new java.awt.Point((int)pA.x(), (int)pA.y())); }
				
				String pStr() { return StringUtils.join(p, " "); }
			}		
			// the matrix of lists of points that will constitute the splitted cells 
			Pt[][] pts = new Pt[tc.getRowSpan()+1][tc.getColSpan()+1];
			
			ShapeEditOperation op = new ShapeEditOperation(ShapeEditType.CUSTOM, "Splitted merged cell");
			
	//		List<ShapeEditOperation> ops = new ArrayList<>();
			
			CanvasQuadPolygon qp = (CanvasQuadPolygon) tc.getData();
			
			// go around border of cell and calculate border points
			for (int s=0; s<4; ++s) {
				List<TrpTableCellType> ns = tc.getNeighborCells(s);
				
				// must ensure that cells are sorted in row or column direction depending on the side we are working on:
				if (s==0 || s==2) // sort rowwise
					Collections.sort(ns, TrpTableCellType.TABLE_CELL_ROW_COMPARATOR);
				else // sort columnwise
					Collections.sort(ns, TrpTableCellType.TABLE_CELL_COLUMN_COMPARATOR);
				
				logger.trace("ns.size() = "+ns.size());
				for (TrpTableCellType n : ns) {
					logger.trace("n: "+n);
				}
				
				int count=0;
				
				boolean isRightOrTopSide = s>1;
				if (isRightOrTopSide) // for right or top side we have step trough neighbor cells in the opposite direction
					Collections.reverse(ns);
				
				if (!ns.isEmpty()) {
					int so = (s+2)%4;
	//				for ( int asdf=(rot?ns.size()-1:0); i!=(rot?-1:ns.size()); i+=(rot?-1:1) ) {
	//					TrpTableCellType n = ns.get(i);
					for (TrpTableCellType n : ns) {
	//					
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
							
							int[] iz = CanvasShapeUtil.getClosestLineIndices(ip.x, ip.y, segPts, false).getLeft();
							int insertIndex = iz[1];
							int insertIndex4Shape = qpn.getPointIndex(segPts.get(iz[0]).x, segPts.get(iz[0]).y);
							
							logger.debug("1 insertIndex = "+insertIndex+" segPts.size() = "+segPts.size()+" insertIndex4Shape = "+insertIndex4Shape);
							segPts.add(insertIndex, ip);
	
							// insert new point into the shape:
							ShapeEditOperation opA = new ShapeEditOperation(ShapeEditType.EDIT, "Added point to shape", qpn);
	//						qpn.insertPointOnSide(ip.x, ip.y, so);
							qpn.insertPointOnIndex(ip.x, ip.y, insertIndex4Shape);
							op.addNestedOp(opA);					
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
						
						int[] iz = CanvasShapeUtil.getClosestLineIndices(ip.x, ip.y, segPts, false).getLeft();
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
						throw new CanvasException("No intersection found between lines: "+lr+" - "+lc);
					}
				}
			}
			
			// print pts:
			if (false)
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
			
			// create new cells according to the points in the pts matrix
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
						else if (x==3)
							pt = pts[i][j+1];
						
						corners[x] = c;
						
						if (i==0 && x==3 || i+1==tc.getRowSpan() && x==1 || j==0 && x==0 || j+1==tc.getColSpan() && x==2) {
							newPts.addAll(pt.p);
							c += pt.p.size();						
						} else {
							newPts.add(pt.f());
							++c;
						}
					}
					
					canvas.setMode(CanvasMode.ADD_TABLECELL);
					
					CanvasQuadPolygon newQuadCell = new CanvasQuadPolygon(newPts, corners);
					
					logger.debug("new cell2: "+newQuadCell);
					
					newQuadCell.setEditable(true);
					ShapeEditOperation opA = scene.addShape(newQuadCell, null, true);
					if (opA!=null) {
						op.addNestedOp(opA);
					}
					
					// set new row/col values for cell:
					TrpTableCellType nc = (TrpTableCellType) newQuadCell.getData();
					if (nc != null) {
						nc.setRow(tc.getRow()+i);
						nc.setCol(tc.getCol()+j);
						nc.setRowSpan(1);
						nc.setColSpan(1);
					}
				}
			}
	
			// now remove the old cell
			if (scene.removeShape(qp, true, true)) {
				ShapeEditOperation opR = new ShapeEditOperation(ShapeEditType.DELETE, "Merged cell shape removed", qp);
				op.addNestedOp(opR);
			}
			
			canvas.setMode(CanvasMode.SELECTION);
					
			if (addToUndoStack)
				addToUndoStack(op);
			
			TrpMainWidget.getInstance().refreshStructureView();
			
			TableUtils.checkTableConsistency(tc.getTable());
			
			return op;
		}

	public Shell getShell() {
		return canvas.getShell();
	}	

}
