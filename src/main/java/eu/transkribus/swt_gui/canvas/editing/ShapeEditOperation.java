package eu.transkribus.swt_gui.canvas.editing;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_gui.canvas.shapes.ICanvasShape;

public class ShapeEditOperation {
	private static Logger logger = LoggerFactory.getLogger(ShapeEditOperation.class);
	
	public static enum ShapeEditType {
		EDIT, ADD, DELETE, SPLIT, MERGE, CUSTOM;
		
		public boolean doBackup() {
//			return this.equals(EDIT) || this.equals(SPLIT) || this.equals(MERGE);
			return this.equals(EDIT) || this.equals(MERGE) /*|| this.equals(SPLIT) || this.equals(MERGE)*/;
		}
	}
	
//	SWTCanvas canvas;
//	String description;
	
	List<ICanvasShape> shapes = new ArrayList<>();
	List<ICanvasShape> backupShapes = new ArrayList<>();
	
	List<ICanvasShape> newShapes = new ArrayList<>();
		
	ShapeEditType type;
	String description;
	boolean isFollowUp=false; // specifies if this operation is a follow-up operation, e.g. for splitting the split of a child shape!
	
	public Object data = null; // user defined data
	
	Deque<ShapeEditOperation> nestedOps = new ArrayDeque<>();
//	boolean nested=false;
	int nesting=0;
	
	public ShapeEditOperation(ShapeEditType type, String description, ICanvasShape affectedShape) {
//		this.canvas = canvas;
		this.type = type;
		this.description = description;
		
		this.shapes = new ArrayList<ICanvasShape>();
		if (affectedShape != null)
			this.shapes.add(affectedShape);
		
		if (type.doBackup()) {
			backupShapes();	
		}
	}
	
	public ShapeEditOperation(ShapeEditType type, String description) {
//		this.canvas = canvas;
		this.type = type;
		this.description = description;
		
		this.shapes = new ArrayList<ICanvasShape>();
		
		if (type.doBackup()) {
			backupShapes();	
		}
	}
	
	public ShapeEditOperation(ShapeEditType type, String description, Collection<ICanvasShape> affectedShapes) {
		this.type = type;
		this.description = description;
		
		this.shapes = new ArrayList<ICanvasShape>();
		
		for (ICanvasShape s : affectedShapes) {
			this.shapes.add(s);	
		}

		if (type.doBackup()) {
			backupShapes();	
		}
	}
	
	public void addNestedOp(ShapeEditOperation op) {
		if (op != null) {
			op.nesting = this.nesting+1; // increment nesting level
			nestedOps.add(op);
		}
	}
	
	public ShapeEditOperation findNestedOp(ICanvasShape s) {
		for (ShapeEditOperation op : nestedOps) {
			if (op.getFirstShape().equals(s))
				return op;
		}
		return null;
	}
	
	public void addNestedOps(List<ShapeEditOperation> ops) {
		if (ops != null) {
			for (ShapeEditOperation op : ops) {
				addNestedOp(op);
			}
		}
	}
	
	public Iterator<ShapeEditOperation> getNestedOpsDescendingIterator() {
		return nestedOps.descendingIterator();
	}
	
//	public Deque<ShapeEditOperation> getNestedOps() {
//		return nestedOps;
//	}
	
	public boolean hasNestedOps() {
		return !nestedOps.isEmpty();
	}
	
	public int getNesting() { 
		return nesting;
	}

	public boolean isFollowUp() {
		return isFollowUp;
	}
	
	public void setFollowUp(boolean isFollowUp) {
		this.isFollowUp = isFollowUp;
	}
	
	public void addNewShape(ICanvasShape newShape) {
		this.newShapes.add(newShape);
	}
	
//	public ShapeEditOperation(SWTCanvas canvas, ShapeEditType type, Collection<ICanvasShape> shapes) {
//		this.canvas = canvas;
//		this.type = type;
//		
//		this.shapes = new ArrayList<ICanvasShape>();
//		this.shapes.addAll(shapes);
//				
//		if (type == ShapeEditType.EDIT)
//			backupShapes();
//	}
	
	private void backupShapes() {
		backupShapes = new ArrayList<>();
		for (ICanvasShape s : shapes) {
			backupShapes.add(s.copy());
//			backupShapes.add(CanvasShapeFactory.copyShape(s));
		}
	}
	
	/**
	 * This function will get called on every undo as its last operation - overwrite to customize undo of operation!
	 */
	protected void customUndoOperation() {
	}
	
	public ShapeEditType getType() { return type; }
	public String getDescription() { return description; }
	public ICanvasShape getFirstShape() { return shapes.isEmpty() ? null : shapes.get(0); }
	public ICanvasShape getFirstBackupShape() { return backupShapes.isEmpty() ? null : backupShapes.get(0); }
	
	public List<ICanvasShape> getShapes() { return shapes; }
	public List<ICanvasShape> getBackupShapes() { return backupShapes; }
	public List<ICanvasShape> getNewShapes() { return newShapes; }
	
//	public void undoEdit() {
//
//	}
//	
//	public void undoAdd() {
//
//	}
//	
//	public void undoDelete() {
//
//	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override public String toString() {
		return "ShapeEditOperation [n-shapes=" + shapes.size() + ", n-backupShapes=" + backupShapes.size() + ", n-newShapes=" + newShapes.size() + ", type=" + type
				+ ", description=" + description + ", isFollowUp="+isFollowUp + "]";
	}
	
	
}
