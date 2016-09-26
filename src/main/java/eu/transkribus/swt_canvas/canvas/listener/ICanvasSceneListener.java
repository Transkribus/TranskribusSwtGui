package eu.transkribus.swt_canvas.canvas.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.List;

import eu.transkribus.swt_canvas.canvas.CanvasScene;
import eu.transkribus.swt_canvas.canvas.editing.ShapeEditOperation;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;

public interface ICanvasSceneListener {
	
	public static enum SceneEventType {
		BEFORE_UNDO, UNDO, BEFORE_ADD, ADD, BEFORE_REMOVE, REMOVE, BEFORE_MOVE, MOVE, SELECTION_CHANGED,
		BEFORE_SPLIT, AFTER_SPLIT, SPLIT, BEFORE_MERGE, MERGE, READING_ORDER_CHANGED
	}
	
	@SuppressWarnings({"serial"})
	public static class SceneEvent extends EventObject {
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
	
	default public boolean triggerEventMethod(ICanvasSceneListener l, SceneEvent e) {
		if (e==null) return false;
		
		switch (e.type) {
		case SELECTION_CHANGED:
			l.onSelectionChanged(e);
			break;
		
		case BEFORE_ADD:
			l.onBeforeShapeAdded(e);
			break;
		case ADD:
			l.onShapeAdded(e);
			break;
			
		case BEFORE_REMOVE:
			l.onBeforeShapeRemoved(e);
			break;
		case REMOVE:
			l.onShapeRemoved(e);
			break;
			
		case BEFORE_MOVE:
			l.onBeforeShapeMoved(e);
			break;
		case MOVE:
			l.onShapeMoved(e);
			break;
			
		case BEFORE_UNDO:
			l.onBeforeUndo(e);
			break;
		case UNDO:
			l.onUndo(e);
			break;
			
		case BEFORE_SPLIT:
			l.onBeforeSplit(e);
			break;
		case SPLIT:
			l.onSplit(e);
			break;
		case AFTER_SPLIT:
			l.onAfterSplit(e);
			break;
			
		case BEFORE_MERGE:
			l.onBeforeMerge(e);
			break;
		case MERGE:
			l.onMerge(e);
			break;
		case READING_ORDER_CHANGED:
			l.onReadingOrderChanged(e);
			break;
		}
			
		return e.stop;
	}

	default void onBeforeShapeAdded(SceneEvent e) {}

	default void onShapeAdded(SceneEvent e) {}

	default void onBeforeShapeRemoved(SceneEvent e) {}

	default void onShapeRemoved(SceneEvent e) {}

	default void onSelectionChanged(SceneEvent e) {}

	default void onBeforeShapeMoved(SceneEvent e) {}

	default void onShapeMoved(SceneEvent e) {}

	default void onBeforeUndo(SceneEvent e) {}

	default void onUndo(SceneEvent e) {}

	default void onBeforeSplit(SceneEvent e) {}

	default void onSplit(SceneEvent e) {}

	default void onAfterSplit(SceneEvent e) {}

	default void onBeforeMerge(SceneEvent e) {}

	default void onMerge(SceneEvent e) {}

	default void onReadingOrderChanged(SceneEvent e) {}

}