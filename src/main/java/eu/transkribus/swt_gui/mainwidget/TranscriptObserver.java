package eu.transkribus.swt_gui.mainwidget;

import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPrintSpaceType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.core.model.beans.pagecontent_trp.observable.TrpObservable;
import eu.transkribus.core.model.beans.pagecontent_trp.observable.TrpObserveEvent;
import eu.transkribus.core.model.beans.pagecontent_trp.observable.TrpObserveEvent.TrpCoordsChangedEvent;
import eu.transkribus.core.model.beans.pagecontent_trp.observable.TrpObserveEvent.TrpReadingOrderChangedEvent;
import eu.transkribus.core.model.beans.pagecontent_trp.observable.TrpObserveEvent.TrpStructureChangedEvent;
import eu.transkribus.core.model.beans.pagecontent_trp.observable.TrpObserveEvent.TrpTagsChangedEvent;
import eu.transkribus.core.model.beans.pagecontent_trp.observable.TrpObserveEvent.TrpTextChangedEvent;
import eu.transkribus.core.model.beans.pagecontent_trp.observable.TrpObserveEvent.TrpTextStyleChangedEvent;
import eu.transkribus.swt_gui.transcription.LineEditor;
import eu.transkribus.swt_gui.transcription.LineTranscriptionWidget;
import eu.transkribus.swt_gui.transcription.WordTranscriptionWidget;

/**
 * Observes changes (text, coordinates...) in any PAGE segmentation element, 
 * i.e. TrpPrintSpaceType, TrpTextRegionType, TrpTextLineType, TrpBaselineType or TrpWordType
 */
public class TranscriptObserver implements Observer {
	private final static Logger logger = LoggerFactory.getLogger(TranscriptObserver.class);
	
	TrpMainWidget mainWidget;
	LineTranscriptionWidget lineTranscriptionWidget;
	WordTranscriptionWidget wordTranscriptionWidget;
	LineEditor lineEditor;
	boolean active=true;
//	DelayedTask tagListUpdateTask;

	public TranscriptObserver(TrpMainWidget mainWidget) {
		this.mainWidget = mainWidget;
		
		lineTranscriptionWidget = mainWidget.getUi().getLineTranscriptionWidget();
		wordTranscriptionWidget = mainWidget.getUi().getWordTranscriptionWidget();
		lineEditor = mainWidget.getCanvas().getLineEditor();
		
//		this.tagListUpdateTask = new DelayedTask(() -> {
////			mainWidget.getUi().getTaggingWidget().getTagListWidget().refreshTable();
//			mainWidget.getUi().getTaggingWidget().refreshTagList();
//		}, true);
	}
	
	public void setActive(boolean active) { this.active = active; }
	public boolean isActive() { return active; }
	
	private void onReadingOrderChanged(Object source, TrpReadingOrderChangedEvent e) {
		logger.trace("Reading order changed: "+e);
		
		if (false) { // inefficient... 
			if (mainWidget.getStorage().hasTranscript())
				mainWidget.getStorage().getTranscript().getPage().sortContent();
	
			mainWidget.updateSelectedTranscriptionWidgetData();
			mainWidget.refreshStructureView();
			mainWidget.redrawCanvas();
		}
	}
	
	private void onCoordinatesChanged(Object source, TrpCoordsChangedEvent e) {
//		logger.debug("coordinates of shape changed! who did it: "+e.who);
		
		if (source instanceof TrpPrintSpaceType) {
		}
		else if (source instanceof TrpTextRegionType) {
		}
		else if (source instanceof TrpTextLineType) {
			lineEditor.updatePosition();
		}
		else if (source instanceof TrpBaselineType) {
		}
		
//		logger.debug("refreshing tree for element "+source);
		// update tree:
		mainWidget.getUi().getStructureTreeWidget().updateTextLabels(source);
	}
	
	private void onTextChanged(Object source, TrpTextChangedEvent e) {
		if (!(source instanceof ITrpShapeType))
			return;
		
		if (source instanceof TrpTextRegionType) {
		}
		else if (source instanceof TrpTextLineType) {
			logger.trace("a line has changed, who: "+e.who);
			// update line transcription widget:
//			mainWidget.updateTranscriptionWidgetsData();
			if (e.who != lineTranscriptionWidget) {
				mainWidget.updateLineTranscriptionWidgetData();
			}
			// update text in lineeditor:
			if (e.who != lineEditor) {
//				logger.debug("updating line editor");
				lineEditor.updateEditor();
			}
			
			// update tree:
//			mainWidget.getUi().getStructureTreeWidget().updateTextLabels(source);
		}
		else if (source instanceof TrpWordType) {
			logger.debug("a word has changed!");
//			mainWidget.updateTranscriptionWidgetsData();
			if (e.who == wordTranscriptionWidget) {
				mainWidget.updateWordTranscriptionWidgetData();
			}
			
			// update tree:
//			mainWidget.getUi().getStructureTreeWidget().refreshLabels(source);			
		}
		
		// update word tag editor:
//		mainWidget.getCanvas().getWordTagEditor().updateData();
	}
	
	private void onTagsChanged(Object source, TrpTagsChangedEvent e) {
		logger.trace("tags changed, source = "+source+" e = "+e);
		
//		if (false) { // inefficient... keep out if possible!
//		if (source instanceof TrpTextLineType) {
//			lineTranscriptionWidget.redrawText(true);
//			lineEditor.redraw();
//		}
//		else if (source instanceof TrpWordType) {
//			wordTranscriptionWidget.redrawText(true);
//		}
//		}
		
		// usage of DelayedTask makes sure that update of list only occurs once after a 500ms time between two tag changes!
//		tagListUpdateTask.start();
	}
	
	private void onStylesChanged(Object source, TrpTextStyleChangedEvent e) {
		if (!(source instanceof TrpTextLineType) && !(source instanceof TrpWordType))
			return;
		
//		logger.debug("styles have changed, source = "+source+", who = "+e.who);
		
//		if (false) { // inefficient
//		if (source instanceof TrpTextLineType) {
//			lineTranscriptionWidget.redrawText(true);
//			lineEditor.redraw();
//		}
//		else if (source instanceof TrpWordType) {
//			wordTranscriptionWidget.redrawText(true);
//		}
//		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if (!isActive())
			return;
		
		if (arg == null || !(arg instanceof TrpObserveEvent))
			return;
		
		TrpObserveEvent e = (TrpObserveEvent) arg;
		
		if (!(o instanceof TrpObservable)) {
			return;
		}
		
		TrpObservable to = (TrpObservable) o;
		Object s = to.getSource();
		if (s == null) return;
		
		logger.trace(e.description+" event in "+s.getClass().getSimpleName()+", who: "+e.who);
		if (e instanceof TrpCoordsChangedEvent) {
			onCoordinatesChanged(s, (TrpCoordsChangedEvent)e);
		}
		else if (e instanceof TrpTextChangedEvent) {
			onTextChanged(s, (TrpTextChangedEvent) e);
		}
		else if (e instanceof TrpTagsChangedEvent) {
			onTagsChanged(s, (TrpTagsChangedEvent) e);
		}
		else if (e instanceof TrpStructureChangedEvent || e instanceof TrpTextStyleChangedEvent) {
			if (e.who != mainWidget.getUi().getStructuralMetadataWidget())
				mainWidget.updatePageRelatedMetadata();
			if (e instanceof TrpTextStyleChangedEvent) {
				onStylesChanged(s, (TrpTextStyleChangedEvent) e);
			}
		}
		else if (e instanceof TrpReadingOrderChangedEvent) {
			onReadingOrderChanged(s, (TrpReadingOrderChangedEvent) e);
		}
		
//		mainWidget.updatePageInfo();
	}

}
