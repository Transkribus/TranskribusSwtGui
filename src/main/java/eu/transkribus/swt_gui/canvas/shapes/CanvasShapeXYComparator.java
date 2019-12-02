package eu.transkribus.swt_gui.canvas.shapes;

import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CanvasShapeXYComparator implements Comparator<ICanvasShape> {

		private static final Logger logger = LoggerFactory.getLogger(CanvasShapeXYComparator.class);
	
	@Override
	public int compare(ICanvasShape o1, ICanvasShape o2) {
		if (o1 == null && o2  == null) {
			return 0;
		}
		else if (o1 == null && o2 != null) {
			return -1;
		}
		else if (o1 != null && o2 == null) {
			return 1;
		}
		else if (o1.getX() == o2.getX()) {
			logger.debug("h1");
				return Integer.compare(o1.getY(), o2.getY());
		}	
		else {
			logger.debug("h2");
			return Integer.compare(o1.getX(), o2.getX());
		}	
	}

}
