package eu.transkribus.swt_gui.transcription.listener;

import eu.transkribus.core.catti.CattiRequest;

/**
 * Note: this interface is currenlty *not* the base interface for the ATranscriptionWidgetListener
 * but should become it long term.
 * It was introduced to handle catti messages in the debugger dialog...
 * @author sebastian
 *
 */
public interface ITranscriptionWidgetListener {
	
	void onCattiMessage(CattiRequest r, String message);

}
