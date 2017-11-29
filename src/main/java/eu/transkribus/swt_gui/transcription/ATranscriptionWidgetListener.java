package eu.transkribus.swt_gui.transcription;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.enums.TranscriptionLevel;
import eu.transkribus.swt_gui.canvas.CanvasKeys;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.metadata.TagPropertyPopup;

public abstract class ATranscriptionWidgetListener implements Listener, KeyListener {
	private final static Logger logger = LoggerFactory.getLogger(ATranscriptionWidgetListener.class);
	
	TrpMainWidget mainWidget;
	ATranscriptionWidget transcriptionWidget;
	
	TagPropertyPopup tagPropertyPopup;

	public ATranscriptionWidgetListener(TrpMainWidget mainWidget, ATranscriptionWidget transcriptionWidget) {
		this.mainWidget = mainWidget;
		this.transcriptionWidget = transcriptionWidget;
		
		transcriptionWidget.addListener(SWT.FocusIn, this);
		transcriptionWidget.addListener(SWT.Selection, this);
		transcriptionWidget.addListener(SWT.DefaultSelection, this);
		transcriptionWidget.addListener(SWT.Modify, this);
		
//		transcriptionWidget.addListener(SWT.KeyDown, listener);
		transcriptionWidget.getText().addKeyListener(this);
		
		transcriptionWidget.getVkItem().addListener(SWT.Selection, this);
		
		// listener for change of transcription widget type:
		SelectionAdapter transcriptTypeListener = new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				if (!(e.getSource() instanceof MenuItem))
					return;
				
				MenuItem mi = (MenuItem) e.getSource();
				if (mi.getSelection()) {
					mainWidget.getUi().changeToTranscriptionWidget((TranscriptionLevel) mi.getData());
				}
			}
		};
		transcriptionWidget.getTranscriptionTypeLineBasedItem().addSelectionListener(transcriptTypeListener);
		transcriptionWidget.getTranscriptionTypeWordBasedItem().addSelectionListener(transcriptTypeListener);
		
//		transcriptionWidget.getTranscriptionTypeItem().ti.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				if (e.detail != SWT.ARROW) {
//					ATranscriptionWidget.Type type = (Type) transcriptionWidget.getTranscriptionTypeItem().getSelected().getData();
//					mainWidget.getUi().changeToTranscriptionWidget(type);					
//				}
//			}
//		});
		
		tagPropertyPopup = new TagPropertyPopup(transcriptionWidget.getShell());
	}
	
	@Override public void keyPressed(KeyEvent e) {
		logger.trace("key pressed: "+e);
		
		if ( CanvasKeys.isAltKeyDown(e.stateMask) && (e.keyCode == SWT.ARROW_RIGHT || e.keyCode == SWT.ARROW_LEFT) ) {			
			if (e.keyCode == SWT.ARROW_LEFT)
				mainWidget.jumpToPreviousRegion();
			else
				mainWidget.jumpToNextRegion();
		}
		if ( CanvasKeys.isCtrlKeyDown(e.stateMask) && (e.keyCode == SWT.ARROW_DOWN 
					|| e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT)) {
				mainWidget.jumpToNextCell(e.keyCode);
		}
		
		// show properties popup
		if ( CanvasKeys.isCtrlOrCommandKeyDown(e.stateMask) && e.keyCode == 'p') {
			// TODO check if custom tag under cursor and set it to 

			int lo = transcriptionWidget.getText().getOffsetAtLine(transcriptionWidget.getCurrentLineIndex());
			List<CustomTag> tagsForOffset = transcriptionWidget.getCustomTagsForCurrentOffset();
			if (tagsForOffset.isEmpty()) {
				int co = transcriptionWidget.getText().getCaretOffset();
				for (int i=co+1; i<transcriptionWidget.getText().getText().length(); ++i) {
					List<CustomTag> tagsForOffset1 = transcriptionWidget.getCustomTagsForOffset(i);
					if (!tagsForOffset1.isEmpty()) {
						tagsForOffset = tagsForOffset1;
						transcriptionWidget.getText().setSelection(i);
						break;
					}
				}
			}
			
			if (!tagsForOffset.isEmpty()) {
				CustomTag tag = tagsForOffset.get(0);
				List<Rectangle> bounds = transcriptionWidget.getTagDrawBounds(tag, lo);
				if (!bounds.isEmpty()) {
					Rectangle r = bounds.get(0);
					
					Point p = transcriptionWidget.getText().toDisplay(r.x+r.width, r.y+r.height);
					// FIXME if popup overlaps bounds, re-position!
//					int xDiff = (p.x+tagPropertyPopup.getShell().getSize().x) - Display.getDefault().getBounds().width;
//					if (xDiff > 0) {
//						p.x -= xDiff;
//					}
//					int yDiff = (p.y+tagPropertyPopup.getShell().getSize().y) - Display.getDefault().getBounds().height;
//					if (yDiff > 0) {
//						p.y -= yDiff;
//					}
//					System.out.println("bounds = "+Display.getDefault().getBounds()+" xDiff = "+xDiff+" yDiff = "+yDiff+" shell size: "+tagPropertyPopup.getShell().getSize());
					
					tagPropertyPopup.showAt(p.x, p.y);
					tagPropertyPopup.setCustomTag(tag);
				}
			}
		}
		
	}
	
	@Override public void keyReleased(KeyEvent e) {
	}
		
	@Override
	public void handleEvent(Event event) {
		if (event.type == SWT.FocusIn) {
			handleFocus(event);
		}
		else if (event.type == SWT.Selection) {
			if (event.widget == transcriptionWidget.getVkItem()) {
				mainWidget.openVkDialog();
			} else {
				handleSelectionChanged(event);	
			}
		}
		else if (event.type == SWT.DefaultSelection) {
			handleDefaultSelectionChanged(event);
		}		
		else if (event.type == SWT.Modify) {
			handleTextModified(event);
		}
	}

	protected abstract void handleTextModified(Event event);

	protected abstract void handleDefaultSelectionChanged(Event event);

	protected abstract void handleSelectionChanged(Event event);

	protected abstract void handleFocus(Event event);

}
