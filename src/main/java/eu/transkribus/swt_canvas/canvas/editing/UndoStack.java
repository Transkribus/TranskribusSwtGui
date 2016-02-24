package eu.transkribus.swt_canvas.canvas.editing;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_canvas.canvas.SWTCanvas;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;

/** A stack of {@link ShapeEditOperation} objects to undo edit operations */
public class UndoStack extends Observable {
	static Logger logger = LoggerFactory.getLogger(UndoStack.class);
	
	public static final String BEFORE_UNDO = "BEFORE_UNDO";
	public static final String AFTER_UNDO = "AFTER_UNDO";
	
	public static final String BEFORE_ADD_OP = "BEFORE_ADD_OP";
	public static final String AFTER_ADD_OP = "AFTER_ADD_OP";
	
//	protected Deque<ShapeEditOperation> undoStack = new ArrayDeque<ShapeEditOperation>();
	protected Deque<Object> undoStack = new ArrayDeque<Object>();
//	protected Stack undoStack = new Stack();
	
	protected SWTCanvas canvas;
	
	public UndoStack(SWTCanvas canvas) {
		this.canvas = canvas;
	}
		
	public String getLastOperationDescription() {
		if (undoStack.isEmpty())
			return null;
		
		Object top = undoStack.peek();
		
		if (top instanceof ShapeEditOperation) {
			return ((ShapeEditOperation) top).description;
		} else if (top instanceof List<?>) {
			List<ShapeEditOperation> opsList = (List<ShapeEditOperation>) top;
			return opsList.isEmpty() ? null : opsList.get(0).description; 	
		} else
			return null;
	}
	
	public void clear() { undoStack.clear(); }
	
	protected SWTCanvas getCanvas() {
		return canvas;
	}
		
	@SuppressWarnings("unchecked")
	public void undo() {
		if (!undoStack.isEmpty()) {
			Object top = undoStack.pop();
			
			List<ShapeEditOperation> opsToUndo = new ArrayList<>();
			if (top instanceof ShapeEditOperation) {
				opsToUndo.add((ShapeEditOperation) top);
			} else if (top instanceof List<?>) {
				opsToUndo.addAll((List<ShapeEditOperation>) top);				
			}
			
			logger.debug("nr of ops to undo: "+opsToUndo.size());
			for (int i=opsToUndo.size()-1; i>=0; --i) { // undo all operations in reverse order!
//				ShapeEditOperation op = undoStack.pop();
				ShapeEditOperation op = opsToUndo.get(i);
			
				this.setChanged();
				this.notifyObservers(BEFORE_UNDO);
				undo(op);
				this.setChanged();
				this.notifyObservers(AFTER_UNDO);
				logger.debug("Undone operation, stack size: "+undoStack.size());				
			}
		}
		else
			logger.debug("Cannot undo anymore - stack is empty!");
	}
	
	public void addToUndoStack(List<ShapeEditOperation> ops) {
		this.setChanged();
		this.notifyObservers(BEFORE_ADD_OP);
		undoStack.push(ops);
		this.setChanged();
		this.notifyObservers(AFTER_ADD_OP);
		logger.debug("adding multiple operations ("+ops.size()+") to undo stack, stack size: "+undoStack.size());
	}	
	
	public void addToUndoStack(ShapeEditOperation op) {
		this.setChanged();
		this.notifyObservers(BEFORE_ADD_OP);
		undoStack.push(op);
		this.setChanged();
		this.notifyObservers(AFTER_ADD_OP);
		logger.debug("adding operation of type "+op.getClass().getSimpleName()+" to undo stack, size: "+undoStack.size());
	}
	
	public int getSize() {
		return undoStack.size();
	}
	
	protected void undo(ShapeEditOperation op) {
		switch (op.getType()) {
		case EDIT:
			undoEdit(op);
			break;
		case ADD:
			undoAdd(op);
			break;
		case DELETE:
			undoDelete(op);
			break;
		case SPLIT:
			undoSplit(op);
			break;
		case MERGE:
			undoMerge(op);
			break;
		}
	}
	
	protected void undoEdit(ShapeEditOperation op) {
		logger.debug("undoing EditOperation: "+op.getDescription()+", nShapes = "+op.getShapes().size());
		
		// find shape with data -> necessary since shape reference itself can be obsolete!
		for (int i=0; i<op.getShapes().size(); ++i) {
			ICanvasShape shape = op.getShapes().get(i);
			
			ICanvasShape backupShape = op.getBackupShapes().get(i);
			if (shape != null) {
				shape.setPoints(backupShape.getPoints());
			}
			else {
				logger.error("Error undoing edit operation: could not find edited shape by its content");
			}
		}
		
		canvas.redraw();
	}
	
	protected void undoAdd(ShapeEditOperation op) {
		logger.debug("Undoing AddOperation");
		
		// find shape with data -> necessary since shape reference itself can be obsolete!
		ICanvasShape shape = canvas.getScene().findShapeWithData(op.getFirstShape().getData());
			
		if (shape != null) {
			canvas.getScene().removeShape(shape, true, true);
		}
		else {
			logger.error("Error undoing add operation: could not find added shape by its content");
		}
		
		canvas.redraw();
	}
	
	protected void undoDelete(ShapeEditOperation op) {
		logger.debug("Undoing DeleteOperation 1");
		
		canvas.getScene().notifyOnBeforeUndo(op);
		for (ICanvasShape s : op.getShapes()) {
			// reinsert shape:
			canvas.getScene().addShape(s, null, false); // do not send signal, otherwise a new JAXB element would be created!
			s.setEditable(canvas.getSettings().isEditingEnabled());
			
			// add child shapes:
			for (ICanvasShape c : s.getChildren(true)) {
//				logger.debug("adding child shape, level = "+c.getLevel());
				canvas.getScene().addShape(c, null, false); // do not send signal, otherwise a new JAXB element would be created!
				c.setEditable(canvas.getSettings().isEditingEnabled());
			}
		}
		canvas.getScene().notifyOnUndo(op);
		canvas.redraw();
		
		// the old shit from TrpUndoStack:
//		try {
//			ITrpShapeType st = (ITrpShapeType) op.getFirstShape().getData();
////			ICanvasShape parentShape = canvas.getScene().findShapeWithData(st.getParentShapeType());
//			
//			// create all canvas shapes that were removed
//			List<ICanvasShape> shapes = mw.getShapeFactory().createAllCanvasShapes(st);
//
//			// add all shapes to the canvas again:
//			for (ICanvasShape s : shapes) {
//				canvas.getScene().addShape(s, false); // do not send signal, otherwise a new JAXB element would be created!
//				mw.getCanvasShapeObserver().addShapeToObserve(s);
//				s.setEditable(canvas.getSettings().isEditingEnabled());
//			}
//			
//			// add removed element to jaxb again:
//			st.reInsertIntoParent();
//			
//			mw.updateSegmentationViewSettings();
//			mw.getTreeViewer().refresh();
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//			mw.showError("Error undoing", "Could not undo delete operation "+op.getDescription(), e);
//		}
//		
//		canvas.redraw();		
//		
//		
		
		// the event older shit:		
//		logger.debug("Undoing DeleteOperation");
//		
//		ICanvasShape shape = canvas.getScene().findShapeWithData(op.getFirstShape().getData());
//		if (shape != null) {
//			canvas.getScene().addShape(shape, true);	
//		}
//		else {
//			logger.error("Error undoing delete operation: could not find added shape by its content");
//		}	
//		canvas.redraw();
			
		
	}
	
	protected void undoSplit(ShapeEditOperation op) {
		logger.debug("undoing split: "+op.getShapes().size());
				
		canvas.getScene().notifyOnBeforeUndo(op);
		
		ICanvasShape origShape = op.getFirstShape();
		ICanvasShape s1 = op.getNewShapes().get(0);
		ICanvasShape s2 = op.getNewShapes().get(1);
		
		origShape.removeChildren();
		for (ICanvasShape c : s1.getChildren(false)) {
			origShape.addChild(c);
		}
		for (ICanvasShape c : s2.getChildren(false)) {
			origShape.addChild(c);
		}
		
		canvas.getScene().addShape(origShape, null, false);
		boolean r1 = canvas.getScene().removeShape(s1, false, false);
		boolean r2 = canvas.getScene().removeShape(s2, false, false);
		
		logger.debug("removed: "+r1+"/"+r2);
		
		canvas.getScene().notifyOnUndo(op);
		canvas.redraw();		
	}
	
	protected void undoMerge(ShapeEditOperation op) {
		logger.debug("undoing merge: "+op.getShapes().size());
		canvas.getScene().notifyOnBeforeUndo(op);
		
		// remove the merged shape:
		canvas.getScene().removeShape(op.getNewShapes().get(0), false, true);
		// add the formerly removed shapes:
		for (ICanvasShape s : op.getShapes()) {
			canvas.getScene().addShape(s, s.getParent(), false);	
		}
		
		canvas.getScene().notifyOnUndo(op);
		canvas.redraw();
	}

	
	
	
	
	

}
