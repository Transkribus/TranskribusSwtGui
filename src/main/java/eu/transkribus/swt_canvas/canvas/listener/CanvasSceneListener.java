package eu.transkribus.swt_canvas.canvas.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;

import eu.transkribus.swt_canvas.canvas.CanvasScene;
import eu.transkribus.swt_canvas.canvas.editing.ShapeEditOperation;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;

public abstract class CanvasSceneListener implements EventListener {
	static public enum SceneEventType {
		BEFORE_UNDO, UNDO, BEFORE_ADD, ADD, BEFORE_REMOVE, REMOVE, BEFORE_MOVE, MOVE, SELECTION_CHANGED,
		BEFORE_SPLIT, AFTER_SPLIT, SPLIT, BEFORE_MERGE, MERGE, READING_ORDER_CHANGED
	}
	
	
	@SuppressWarnings({"serial"})
	static public class SceneEvent extends EventObject {
		public final SceneEventType type;
//		public ICanvasShape shape=null;
		public List<ICanvasShape> shapes=new ArrayList<>();
		public ShapeEditOperation op;
		public Object data;
		
		public boolean stop=false;
		
		public SceneEvent(SceneEventType type, CanvasScene scene, ICanvasShape inputShape) {
			super(scene);
			this.type = type;
			
			this.shapes.add(inputShape);
		}
		
		public SceneEvent(SceneEventType type, CanvasScene scene, Collection<ICanvasShape> inputShapes) {
			super(scene);
			this.type = type;
			for (ICanvasShape s : inputShapes)
				this.shapes.add(s);
		}		
		
		public SceneEvent(SceneEventType type, CanvasScene scene, ShapeEditOperation op) {
			super(scene);
			this.type = type;
			this.op = op;
		}
		
		public ICanvasShape getFirstShape() { return shapes.isEmpty() ? null : shapes.get(0); }

		@Override
		public CanvasScene getSource() { return (CanvasScene) source; }
	}
	
//	@SuppressWarnings({"serial"})
//	static public class BeforeUndoEvent extends SceneEvent {
//		ShapeEditOperation op;
//		
//		public BeforeUndoEvent(CanvasScene scene, ShapeEditOperation op) {
//			super(scene, op.getFirstShape());
//			this.op = op;
//		}
//		
//		public ShapeEditOperation getOp() { return op; }
//	}
//	
//	@SuppressWarnings({"serial"})
//	static public class UndoEvent extends SceneEvent {
//		ShapeEditOperation op;
//		
//		public UndoEvent(CanvasScene scene, ShapeEditOperation op) {
//			super(scene, op.getFirstShape());
//			this.op = op;
//		}
//		
//		public ShapeEditOperation getOp() { return op; }
//	}	
//	
//	@SuppressWarnings({"serial"})
//	static public class BeforeAddShapeEvent extends SceneEvent {		
//		public BeforeAddShapeEvent(CanvasScene scene, ICanvasShape shape) {
//			super(scene, shape);
//		}
//	}
//	
//	@SuppressWarnings({"serial"})
//	static public class AddShapeEvent extends SceneEvent {		
//		public AddShapeEvent(CanvasScene scene, ICanvasShape shape) {
//			super(scene, shape);
//		}
//	}
//	
//	@SuppressWarnings({"serial"})
//	static public class BeforeRemoveShapeEvent extends SceneEvent {		
//		public BeforeRemoveShapeEvent(CanvasScene scene, ICanvasShape shape) {
//			super(scene, shape);
//		}
//	}	
//	
//	@SuppressWarnings({"serial"})
//	static public class RemoveShapeEvent extends SceneEvent {
//		public RemoveShapeEvent(CanvasScene scene, ICanvasShape shape) {
//			super(scene, shape);
//		}
//	}
//	
//	@SuppressWarnings({"serial"})
//	static public class BeforeMoveShapeEvent extends SceneEvent {
//		public int tx, ty;
//		public BeforeMoveShapeEvent(CanvasScene scene, ICanvasShape shape, int tx, int ty) {
//			super(scene, shape);
//			this.tx = tx; this.ty = ty;
//		}
//	}	
//	
//	@SuppressWarnings({"serial"})
//	static public class MoveShapeEvent extends SceneEvent {
//		public int tx, ty;
//		public MoveShapeEvent(CanvasScene scene, ICanvasShape shape, int tx, int ty) {
//			super(scene, shape);
//			this.tx = tx; this.ty = ty;
//		}
//	}
//		
//	@SuppressWarnings({"serial"})
//	static public class SelectionChangedEvent extends SceneEvent {
//		public SelectionChangedEvent(CanvasScene scene, ICanvasShape shape) {
//			super(scene, shape);
//		}
//	}

	public CanvasSceneListener() {
	}
	
	public boolean triggerEventMethod(SceneEvent e) {
		if (e==null) return false;
		
		switch (e.type) {
		case SELECTION_CHANGED:
			onSelectionChanged(e);
			break;
		
		case BEFORE_ADD:
			onBeforeShapeAdded(e);
			break;
		case ADD:
			onShapeAdded(e);
			break;
			
		case BEFORE_REMOVE:
			onBeforeShapeRemoved(e);
			break;
		case REMOVE:
			onShapeRemoved(e);
			break;
			
		case BEFORE_MOVE:
			onBeforeShapeMoved(e);
			break;
		case MOVE:
			onShapeMoved(e);
			break;
			
		case BEFORE_UNDO:
			onBeforeUndo(e);
			break;
		case UNDO:
			onUndo(e);
			break;
			
		case BEFORE_SPLIT:
			onBeforeSplit(e);
			break;
		case SPLIT:
			onSplit(e);
			break;
		case AFTER_SPLIT:
			onAfterSplit(e);
			break;
			
		case BEFORE_MERGE:
			onBeforeMerge(e);
			break;
		case MERGE:
			onMerge(e);
			break;
		case READING_ORDER_CHANGED:
			onReadingOrderChanged(e);
			break;
		}
			
		return e.stop;
	}
	
	public void onBeforeShapeAdded(SceneEvent e) {}
	
	public void onShapeAdded(SceneEvent e) {}
	
	public void onBeforeShapeRemoved(SceneEvent e) {}
	
	public void onShapeRemoved(SceneEvent e) {}
	
	public void onBeforeShapeMoved(SceneEvent e) {}
	
	public void onShapeMoved(SceneEvent e) {}
	
	public void onSelectionChanged(SceneEvent e) {}
	
	public void onBeforeUndo(SceneEvent e) {}

	public void onUndo(SceneEvent e) {}
	
	public void onBeforeSplit(SceneEvent e) {}

	public void onSplit(SceneEvent e) {}
	
	public void onAfterSplit(SceneEvent e) {}
	
	public void onBeforeMerge(SceneEvent e) {}

	public void onMerge(SceneEvent e) {}
	
	public void onReadingOrderChanged(SceneEvent e) {}

}
