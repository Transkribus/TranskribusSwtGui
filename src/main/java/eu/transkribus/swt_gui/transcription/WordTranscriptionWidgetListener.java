package eu.transkribus.swt_gui.transcription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

import org.eclipse.swt.widgets.Event;

public class WordTranscriptionWidgetListener extends ATranscriptionWidgetListener {
	private final static Logger logger = LoggerFactory.getLogger(WordTranscriptionWidgetListener.class);
	
//	TrpMainWidget mainWidget;
//	WordTranscriptionWidget transcriptionWidget;
	
	public WordTranscriptionWidgetListener(TrpMainWidget mainWidget, WordTranscriptionWidget transcriptionWidget) {
		super(mainWidget, transcriptionWidget);		
	}
	
	@Override
	protected void handleDefaultSelectionChanged(Event event) {
		try {
			logger.debug("word default selection change, event: ["+event.start+"-"+event.end+"]");
			super.handleDefaultSelectionChanged(event);

			mainWidget.updatePageRelatedMetadata();
		} catch (Throwable th) {
			String msg = "Could not update default selection from transcription widget";
			mainWidget.onError("Error updating selection", msg, th);
		}
	}
	
	@Override
	protected void handleFocus(Event event) {
		if (event.data != null && event.data instanceof TrpWordType) {
			TrpWordType tl = (TrpWordType) event.data;
			ICanvasShape shape = mainWidget.selectObjectWithData(tl, true, false);
			mainWidget.getCanvas().focusShape(shape);
		}
	}
	
	@Override
	protected void handleTextModified(Event event) {
		try {
			if (event.data != null && event.data instanceof TrpWordType) {	
				TrpWordType w = (TrpWordType) event.data;
				logger.debug("word text modified "+w.getId()+ ", new text: "+event.text+" start/end: "+event.start+"/"+event.end);
				
				// NEW 
				w.editUnicodeText(event.start, event.end, event.text, transcriptionWidget);
				
				// OLD set new text if it has changed:
				if (false)
				if (!event.text.equals(w.getUnicodeText())) {
					// FIXME??
					w.setUnicodeText(event.text, transcriptionWidget);
//					if (transcriptionWidget.getApplyTextFromWords().getSelection())
//						w.applyTextToLine(true);
				}
				
				if ( ((WordTranscriptionWidget)transcriptionWidget).getApplyTextFromWords().getSelection())
					w.applyTextToLine(true);				
				
			}
		} catch (Throwable th) {
			String msg = "Error during word modification";
			mainWidget.onError("Error updating transcription", msg, th);					
		}
	}
	
	@Override
	protected void handleSelectionChanged(Event event) {
		try {
//			logger.debug("word selection change, event widget: "+event.widget);
			
			if (event.data!=null && event.data instanceof TrpWordType) {
				TrpWordType tl = (TrpWordType) event.data;
				logger.debug("word selection changed to: "+tl.getId());
				ICanvasShape shape = mainWidget.selectObjectWithData(tl, true, false);
				mainWidget.getScene().makeShapeVisible(shape);
				
				mainWidget.getUi().getStructureTreeWidget().updateTextLabels(null);
			}
		} catch (Throwable th) {
			String msg = "Could not update selection from transcription widget";
			mainWidget.onError("Error updating selection", msg, th);
		}
	}

}
