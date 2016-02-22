package eu.transkribus.swt_gui.mainwidget.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.canvas.TrpSWTCanvas;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidgetView;
import eu.transkribus.swt_gui.mainwidget.TrpSettings;

public class TrpSettingsPropertyChangeListener implements PropertyChangeListener {
	private final static Logger logger = LoggerFactory.getLogger(TrpSettingsPropertyChangeListener.class);
	
	TrpMainWidget mainWidget;
	TrpMainWidgetView ui;
	TrpSWTCanvas canvas;
	
	public TrpSettingsPropertyChangeListener(TrpMainWidget mainWidget) {
		this.mainWidget = mainWidget;
		this.ui = mainWidget.getUi();
		this.canvas = mainWidget.getCanvas();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		logger.debug(evt.getPropertyName() + " property changed, new value: " + evt.getNewValue());
		String pn = evt.getPropertyName();
		
		

		if (pn.equals(TrpSettings.AUTOCOMPLETE_PROPERTY)) {
			mainWidget.enableAutocomplete();
		} else if (pn.equals(TrpSettings.SHOW_LINE_EDITOR_PROPERTY)) {
			canvas.getLineEditor().updateEditor();
		} else if (TrpSettings.isSegmentationVisibilityProperty(pn)){
			mainWidget.updateSegmentationViewSettings();	
		} else if (pn.equals(TrpSettings.ENABLE_INDEXED_STYLES)) {
			logger.debug("indexed styles visibility toggled: "+evt.getNewValue());
			ui.getSelectedTranscriptionWidget().redrawText(true);
			mainWidget.updatePageRelatedMetadata();
		} else if (pn.equals(TrpSettings.RENDER_BLACKENINGS_PROPERTY)) {
			canvas.redraw();
		}
		if (TrpSettings.isColorProperty(pn)) {
			logger.debug("color info changed - updating!");
			canvas.updateShapeColors();
		}
		
		if (true) {
			logger.debug("saving config file...");
			TrpConfig.save(pn);
		}

	}
}
