package org.dea.swt.canvas.editing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dea.swt.canvas.SWTCanvas;
import org.dea.swt.canvas.shapes.ICanvasShape;
import org.dea.swt.canvas.shapes.CanvasShapeFactory;

public class ShapeEditOperation {
	private static Logger logger = LoggerFactory.getLogger(ShapeEditOperation.class);
	
	public static enum ShapeEditType {
		EDIT, ADD, DELETE, SPLIT, MERGE;
		
		public boolean doBackup() {
//			return this.equals(EDIT) || this.equals(SPLIT) || this.equals(MERGE);
			return this.equals(EDIT) /*|| this.equals(SPLIT) || this.equals(MERGE)*/;
		}
	}
	
	SWTCanvas canvas;
//	String description;
	
	List<ICanvasShape> shapes = new ArrayList<>();
	List<ICanvasShape> backupShapes = new ArrayList<>();
	
	List<ICanvasShape> newShapes = new ArrayList<>();
		
	ShapeEditType type;
	String description;
	public boolean isFollowUp=false; // specifies if this operation is a follow-up operation, e.g. for splitting the split of a child shape!
	
	public ShapeEditOperation(SWTCanvas canvas, ShapeEditType type, String description, ICanvasShape affectedShape) {
		this.canvas = canvas;
		this.type = type;
		this.description = description;
		
		this.shapes = new ArrayList<ICanvasShape>();
		this.shapes.add(affectedShape);
		
		if (type.doBackup()) {
			backupShapes();	
		}
	}
	
	public ShapeEditOperation(SWTCanvas canvas, ShapeEditType type, String description, Collection<ICanvasShape> affectedShapes) {
		this.canvas = canvas;
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
			backupShapes.add(CanvasShapeFactory.copyShape(s));
		}
	}
	
	public ShapeEditType getType() { return type; }
	public String getDescription() { return description; }
	public ICanvasShape getFirstShape() { return shapes.isEmpty() ? null : shapes.get(0); }
	public ICanvasShape getFirstBackupShape() { return backupShapes.isEmpty() ? null : backupShapes.get(0); }
	
	public List<ICanvasShape> getShapes() { return shapes; }
	public List<ICanvasShape> getBackupShapes() { return backupShapes; }
	public List<ICanvasShape> getNewShapes() { return newShapes; }
	
	public void undoEdit() {

	}
	
	public void undoAdd() {

	}
	
	public void undoDelete() {

	}

	public void setDescription(String description) {
		this.description = description;
	}
}
