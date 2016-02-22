package org.dea.swt.canvas.shapes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CanvasShapeFactory {
	static Logger logger = LoggerFactory.getLogger(CanvasShapeFactory.class);
	
	public static ICanvasShape copyShape(ICanvasShape src) {
		if (src == null)
			throw new NullPointerException("src shape is null!");
		
		if (src instanceof CanvasPolygon) {
			return new CanvasPolygon((CanvasPolygon)src);
		}
		else if (src instanceof CanvasPolyline) {
			return new CanvasPolyline((CanvasPolyline)src);
		}
		else if (src instanceof CanvasRect) {
			return new CanvasRect((CanvasRect)src);
		}
		else {
			logger.error("Could not determine ICanvasShape type: "+src+ " - should not happen! Now just returning a polygon...");
			return new CanvasPolygon(src.getPoints());
		}
		
//		throw new CloneNotSupportedException("Cannot copy shape of type: "+src.getClass().getSimpleName());
	}

}
