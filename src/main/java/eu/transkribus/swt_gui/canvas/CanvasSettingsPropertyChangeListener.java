package eu.transkribus.swt_gui.canvas;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidgetView;

import org.dea.swt.canvas.CanvasMode;
import org.dea.swt.canvas.CanvasSettings;
import org.dea.swt.canvas.shapes.CanvasShapeType;

public class CanvasSettingsPropertyChangeListener implements PropertyChangeListener {
	private final static Logger logger = LoggerFactory.getLogger(CanvasSettingsPropertyChangeListener.class);
	
	TrpMainWidget mainWidget;
	TrpMainWidgetView ui;
	TrpSWTCanvas canvas;
	
	public CanvasSettingsPropertyChangeListener(TrpMainWidget mainWidget) {
		this.mainWidget = mainWidget;
		this.ui = mainWidget.getUi();
		this.canvas = mainWidget.getCanvas();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		logger.debug("property changed: " + evt.getPropertyName());

		if (evt.getPropertyName().equals(CanvasSettings.EDITING_ENABLED_PROPERTY)) {
			mainWidget.updateSegmentationEditStatus();
		} else if (evt.getPropertyName().equals(CanvasSettings.MODE_PROPERTY)) {
			CanvasMode m = (CanvasMode) evt.getNewValue();
			if (m.isSplitOperation())
				canvas.getShapeEditor().setShapeToDraw(CanvasShapeType.POLYLINE);
			else if (m.isAddOperation()) {
				canvas.getShapeEditor().setShapeToDraw(ui.getShapeTypeToDraw());
			}
			
			// update message:
//			if (m.isAddOperation()) {
//				ui.setStatusMessage("Press Esc to return to select mode, Press <Enter> to confirm a shape", 0);
//			}
//			else if (m == CanvasMode.SELECTION)
//				ui.setStatusMessage("", 0);

		}

	}
}
