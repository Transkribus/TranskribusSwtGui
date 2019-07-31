package eu.transkribus.swt_gui.mainwidget;

import org.eclipse.swt.graphics.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpShapeTypeUtils;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt_gui.canvas.CanvasMode;
import eu.transkribus.swt_gui.canvas.editing.ShapeEditOperation;
import eu.transkribus.swt_gui.canvas.shapes.CanvasPolygon;
import eu.transkribus.swt_gui.canvas.shapes.CanvasPolyline;
import eu.transkribus.swt_gui.canvas.shapes.CanvasShapeUtil;
import eu.transkribus.swt_gui.canvas.shapes.ICanvasShape;

public class ShapeEditController extends AMainWidgetController {
	private static final Logger logger = LoggerFactory.getLogger(ShapeEditController.class);
	
	public ShapeEditController(TrpMainWidget mw) {
		super(mw);
	}
	
	public static final double DEFAULT_POLYGON_SIMPLICATION_PERCENTAGE = 1.0d;
	
	public void simplifySelectedLineShapes(boolean onlySelected) {
		simplifySelectedLineShapes(DEFAULT_POLYGON_SIMPLICATION_PERCENTAGE, onlySelected);
	}
	
	public void simplifySelectedLineShapes(double percentage, boolean onlySelected) {
		try {
			logger.debug("simplifying selected line shape");
			canvas.getShapeEditor().simplifyTextLines(percentage, onlySelected);
		} catch (Exception e) {
			TrpMainWidget.getInstance().onError("Error", e.getMessage(), e);
		}
	}
	
	public void createImageSizeTextRegion() {
		try {
			if (!storage.hasTranscript()) {
				return;
			}
			
			canvas.getScene().getMainImage().getBounds();
			Rectangle imgBounds = canvas.getScene().getMainImage().getBounds();
			
			if (CanvasShapeUtil.getFirstTextRegionWithSize(storage.getTranscript().getPage(), 0, 0, imgBounds.width, imgBounds.height, false) != null) {
				DialogUtil.showErrorMessageBox(getShell(), "Error", "Top level region with size of image already exists!");
				return;
			}
			
			CanvasPolygon imgBoundsPoly = new CanvasPolygon(imgBounds);
//			CanvasMode modeBackup = canvas.getMode();
			canvas.setMode(CanvasMode.ADD_TEXTREGION);
			ShapeEditOperation op = canvas.getShapeEditor().addShapeToCanvas(imgBoundsPoly, true);
			canvas.setMode(CanvasMode.SELECTION);
		} catch (Exception e) {
			TrpMainWidget.getInstance().onError("Error", e.getMessage(), e);
		}	
	}

	public void createDefaultLineForSelectedShape() {
		if (canvas.getFirstSelected() == null)
			return;
		
		try {
			logger.debug("creating default line for seected line/baseline!");
			
//			CanvasPolyline baselineShape = (CanvasPolyline) shape;
//			shapeOfParent = baselineShape.getDefaultPolyRectangle();
			
			ICanvasShape shape = canvas.getFirstSelected();
			CanvasPolyline blShape = (CanvasPolyline) CanvasShapeUtil.getBaselineShape(shape);
			if (blShape == null)
				return;
			
			CanvasPolygon pl = blShape.getDefaultPolyRectangle();
			if (pl == null)
				return;
			
			ITrpShapeType st = (ITrpShapeType) shape.getData();
			TrpTextLineType line = TrpShapeTypeUtils.getLine(st);
			if (line != null) {
				ICanvasShape lineShape = (ICanvasShape) line.getData();
				if (lineShape != null) {
					lineShape.setPoints(pl.getPoints());
					
					canvas.redraw();
				}
			}
		} catch (Exception e) {
			TrpMainWidget.getInstance().onError("Error", e.getMessage(), e);
		}	
	}

}
