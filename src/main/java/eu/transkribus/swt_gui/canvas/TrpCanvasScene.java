package eu.transkribus.swt_gui.canvas;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent.TextStyleType;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.util.TextStyleTypeUtils;
import eu.transkribus.swt_canvas.canvas.CanvasScene;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.util.GuiUtil;

public class TrpCanvasScene extends CanvasScene {
	private final static Logger logger = LoggerFactory.getLogger(TrpCanvasScene.class);

	public TrpCanvasScene(TrpSWTCanvas canvas) {
		super(canvas);
	}
	
	public ICanvasShape selectObjectWithId(String id, boolean sendSignal, boolean multiselect) {
		for (ICanvasShape s : getShapes()) {
			if (s.getData() instanceof ITrpShapeType) {
				ITrpShapeType st = (ITrpShapeType) s.getData();
				if (st.getId().equals(id)) {
					selectObject(s, sendSignal, multiselect);
					return s;
				}
			}
		}
		return null;
	}
	
	public TextStyleType getCommonTextStyleOfSelected() {
		TextStyleType textStyle = null;
		for (int i=0; i<selected.size(); ++i) {
			ITrpShapeType st = GuiUtil.getTrpShape(selected.get(i));
			if (st == null)
				continue;
			
			if (textStyle == null) {
				textStyle = st.getTextStyle();
			}
			else {
				textStyle = TextStyleTypeUtils.mergeEqualTextStyleTypeFields(textStyle, st.getTextStyle());
			}
		}
		
		return textStyle;
	}
	
	public static void updateParentShape(ITrpShapeType st) {
		ICanvasShape shape = GuiUtil.getCanvasShape(st);
		if (shape == null)
			return;
				
		if (st.getParentShape() != null) {
			ICanvasShape pShape = (ICanvasShape) st.getParentShape().getData();
//			logger.debug("parent shape: "+pShape);
			shape.setParentAndAddAsChild(pShape);
		} else {
			shape.setParent(null);
		}
	}
	
	@Override
	public void updateParentInfo(ICanvasShape shape, boolean recursive) {
		ITrpShapeType st = GuiUtil.getTrpShape(shape);
		if (st == null)
			return;
		
		updateParentShape(st);
		if (recursive) {
			for (ITrpShapeType childSt : st.getChildren(recursive)) {
				updateParentShape(childSt);
			}
		}
	}
		
	@Override
	public void updateAllShapesParentInfo() {
		if (!Storage.getInstance().hasTranscript())
			return;
		
		for (ICanvasShape s : shapes) {
			s.setParent(null);
			s.removeChildren();
		}
		
		for (ITrpShapeType st : Storage.getInstance().getTranscript().getPage().getAllShapes(true)) {
			updateParentShape(st);
		}
		
//		for (ICanvasShape s : shapes) {	
//			updateParentInfo(s, false);
//		}
	}


}
