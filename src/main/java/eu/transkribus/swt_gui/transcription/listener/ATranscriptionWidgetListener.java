package eu.transkribus.swt_gui.transcription.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.swt_canvas.util.databinding.DataBinder;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.TrpSettings;
import eu.transkribus.swt_gui.transcription.ATranscriptionWidget;
import eu.transkribus.swt_gui.transcription.ATranscriptionWidget.Type;

public abstract class ATranscriptionWidgetListener implements Listener {
	
	TrpMainWidget mainWidget;
	ATranscriptionWidget transcriptionWidget;	

	public ATranscriptionWidgetListener(final TrpMainWidget mainWidget, final ATranscriptionWidget transcriptionWidget) {
		this.mainWidget = mainWidget;
		this.transcriptionWidget = transcriptionWidget;
		
		transcriptionWidget.addListener(SWT.FocusIn, this);
		transcriptionWidget.addListener(SWT.Selection, this);
		transcriptionWidget.addListener(SWT.DefaultSelection, this);
		transcriptionWidget.addListener(SWT.Modify, this);
		
		DataBinder db = DataBinder.get();
		db.bindBoolBeanValueToToolItemSelection(TrpSettings.AUTOCOMPLETE_PROPERTY, 
				TrpMainWidget.getTrpSettings(), transcriptionWidget.getAutocompleteToggle());
		
		
		// listener for change of transcription widget type:
		transcriptionWidget.getTranscriptionTypeItem().ti.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				if (e.detail != SWT.ARROW) {
					ATranscriptionWidget.Type type = (Type) transcriptionWidget.getTranscriptionTypeItem().getSelected().getData();
					mainWidget.getUi().changeToTranscriptionWidget(type);					
				}
			}
		});
		
	}
	
	@Override
	public void handleEvent(Event event) {
		if (event.type == SWT.FocusIn) {
			handleFocus(event);
		}
		else if (event.type == SWT.Selection) {
			handleSelectionChanged(event);
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
