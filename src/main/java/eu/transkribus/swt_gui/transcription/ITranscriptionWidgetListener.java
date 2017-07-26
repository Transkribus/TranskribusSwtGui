package eu.transkribus.swt_gui.transcription;

import eu.transkribus.core.catti.CattiRequest;

/**
 * Note: this interface is currenlty *not* the base interface for the ATranscriptionWidgetListener
 * but should become it long term.
 * It was introduced to handle catti messages in the debugger dialog...
 * @author sebastian
 *
 */
public interface ITranscriptionWidgetListener {
	
	default void onCattiMessage(CattiRequest r, String message) {}
	default void onVkItemPressed() {}

}
