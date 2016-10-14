package eu.transkribus.swt_gui.mainwidget.settings;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.portal.PortalWidget.Docking;
import eu.transkribus.swt.portal.PortalWidget.Position;
import eu.transkribus.swt_gui.canvas.SWTCanvas;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidgetView;

public class TrpSettingsPropertyChangeListener implements PropertyChangeListener {
	private final static Logger logger = LoggerFactory.getLogger(TrpSettingsPropertyChangeListener.class);
	
	TrpMainWidget mainWidget;
	TrpMainWidgetView ui;
	SWTCanvas canvas;
		
	public TrpSettingsPropertyChangeListener(TrpMainWidget mainWidget) {
		this.mainWidget = mainWidget;
		this.ui = mainWidget.getUi();
		this.canvas = mainWidget.getCanvas();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		logger.debug("get source of property: " + evt.getSource());
		logger.debug(evt.getPropertyName() + " property changed, new value: " + evt.getNewValue());
		String pn = evt.getPropertyName();

		if (pn.equals(TrpSettings.AUTOCOMPLETE_PROPERTY)) {
			mainWidget.enableAutocomplete();
		} else if (pn.equals(TrpSettings.SHOW_LINE_EDITOR_PROPERTY)) {
			canvas.getLineEditor().updateEditor();
		} else if (TrpSettings.isSegmentationVisibilityProperty(pn)){
			mainWidget.getScene().updateSegmentationViewSettings();	
		} else if (pn.equals(TrpSettings.ENABLE_INDEXED_STYLES)) {
			logger.debug("indexed styles visibility toggled: "+evt.getNewValue());
			ui.getSelectedTranscriptionWidget().redrawText(true);
			mainWidget.updatePageRelatedMetadata();
		} else if (pn.equals(TrpSettings.RENDER_BLACKENINGS_PROPERTY)) {
			canvas.redraw();
		}
		else if (pn.equals(TrpSettings.LEFT_VIEW_DOCKING_STATE_PROPERTY)) {
			ui.getPortalWidget().setWidgetDockingType(Position.LEFT, (Docking) evt.getNewValue());
			canvas.redraw();
		}
//		else if (pn.equals(TrpSettings.RIGHT_VIEW_DOCKING_STATE_PROPERTY)) {
//			ui.getPortalWidget().setWidgetDockingType(Position.RIGHT, (Docking) evt.getNewValue());
//			canvas.redraw();
//		}
		else if (pn.equals(TrpSettings.BOTTOM_VIEW_DOCKING_STATE_PROPERTY)) {
			ui.getPortalWidget().setWidgetDockingType(Position.BOTTOM, (Docking) evt.getNewValue());
			canvas.redraw();
		}
		
		else if (pn.equals(TrpSettings.NEW_WEIGHTS_FOR_VERTICAL_TOP_LEVEL)) {
			ui.getPortalWidget().setNewSashFormVerticalTopLevelWeights((int[]) evt.getNewValue());
		}
		
		else if (pn.equals(TrpSettings.LEFT_TAB_SELECTION_ID)){
			ui.selectLeftTab((int) evt.getNewValue());
		}
		else if (pn.equals(TrpSettings.RIGHT_TAB_SELECTION_ID)){
			ui.selectRightTab((int) evt.getNewValue());
		}
		else if (pn.equals(TrpSettings.IMG_FIT_TO)){
			String howtoFit = (String) evt.getNewValue();
			if (howtoFit.equals("page")){
				mainWidget.getCanvas().fitToPage();
			}
			else if (howtoFit.equals("width")){
				mainWidget.getCanvas().fitWidth();
			}
			else if (howtoFit.equals("height")){
				mainWidget.getCanvas().fitHeight();
			}
			else{
				mainWidget.getCanvas().fitWidth();
			}
		}
		
		
		
		
		
		if (TrpSettings.isColorProperty(pn)) {
			logger.debug("color info changed - updating!");
			canvas.updateShapeColors();
		}
		
		canvas.redraw();
	}
}
