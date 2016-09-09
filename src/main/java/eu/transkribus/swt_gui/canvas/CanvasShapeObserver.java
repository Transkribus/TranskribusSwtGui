package eu.transkribus.swt_gui.canvas;

import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.util.PointStrUtils;
import eu.transkribus.swt_canvas.canvas.shapes.ACanvasShape;
import eu.transkribus.swt_canvas.canvas.shapes.CanvasQuadPolygon;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

/**
 * Observes changes in a given shape (i.e. changes in their coordinates) and updates the corresponding TreeItem and PAGE data
 */
public class CanvasShapeObserver implements Observer {
	private final static Logger logger = LoggerFactory.getLogger(CanvasShapeObserver.class);
	
	TrpMainWidget mainWidget;
	
	public CanvasShapeObserver(TrpMainWidget mainWidget) {
		this.mainWidget = mainWidget;
	}
	
	public void addShapeToObserve(ICanvasShape shape) {
		((ACanvasShape<?>) shape).deleteObservers(); // only one observer!!
		((ACanvasShape<?>) shape).addObserver(this);
	}
	
	public void updateObserverForAllShapes() {
		for (ICanvasShape shape : mainWidget.getCanvas().getScene().getShapes()) {
			addShapeToObserve(shape);
		}
	}
	
	private void updateCoordinatesFromShapeData(ICanvasShape shape) {
//		logger.debug("Updating coordinates of shape");
		String ptsStr = PointStrUtils.pointsToString(shape.getPoints());
		
		// update points in JAXB:
		if (shape.getData() instanceof ITrpShapeType) {
			((ITrpShapeType)shape.getData()).setCoordinates(ptsStr, this);
		}

		if (shape.getData() instanceof TrpTableCellType && shape instanceof CanvasQuadPolygon) {			
			CanvasQuadPolygon qp = (CanvasQuadPolygon) shape;
			TrpTableCellType tc = (TrpTableCellType) shape.getData();
			String cornersStr = PointStrUtils.cornerPtsToString(qp.getCorners());
			tc.setCornerPts(cornersStr, this);
		}
		
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof ACanvasShape)
			updateCoordinatesFromShapeData((ACanvasShape<?>) o);	
	}
}
