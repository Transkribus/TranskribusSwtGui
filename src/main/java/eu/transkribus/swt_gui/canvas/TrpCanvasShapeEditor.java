package eu.transkribus.swt_gui.canvas;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.swt_canvas.canvas.editing.CanvasShapeEditor;
import eu.transkribus.swt_canvas.canvas.editing.ShapeEditOperation;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;

///**
// * @deprecated not used currently
// */
public class TrpCanvasShapeEditor extends CanvasShapeEditor {
	private final static Logger logger = LoggerFactory.getLogger(TrpCanvasShapeEditor.class);

	public TrpCanvasShapeEditor(TrpSWTCanvas canvas) {
		super(canvas);
	}
	
	@Override public List<ShapeEditOperation> splitShape(int x1, int y1, int x2, int y2) {		
		ICanvasShape selected = canvas.getFirstSelected();
		if (selected == null) {
			logger.warn("Cannot split - no shape selected!");
			return null;
		}
		
		// if this is a baseline, select parent line and split it, s.t. undlerying baseline gets splits too
		// next, try to select the first baseline split
		if (selected.getData() instanceof TrpBaselineType) {
			
			TrpBaselineType bl = (TrpBaselineType) selected.getData();
			scene.selectObjectWithData(bl.getLine(), false, false);
			logger.debug("selected = "+canvas.getFirstSelected());
			
//			logger.debug("Parent = "+selected.getParent()); // IS NULL...			
//			scene.selectObject(selected.getParent(), false, false);
			
			// try to select first split of baseline:
			List<ShapeEditOperation> splitOps = super.splitShape(x1, y1, x2, y2);
			logger.debug("trying to select left baseline split, nr of ops = "+splitOps.size());
			if (splitOps != null) {
				for (ShapeEditOperation o : splitOps) {
					if (!o.getShapes().isEmpty() && o.getShapes().get(0).getData() instanceof TrpBaselineType) {
						if (!o.getNewShapes().isEmpty()) {
							logger.debug("found left baseline split - selecting: "+o.getNewShapes().get(0));
//							scene.selectObjectWithData(o.getNewShapes().get(0), true, false);
							scene.selectObject(o.getNewShapes().get(0), true, false);
							break;
						}
					}
				}
			}
			
			return splitOps;
		} else { // not splitting a basline -> perform default split operation on base class
			return super.splitShape(x1, y1, x2, y2);
		}
	}
	
}
