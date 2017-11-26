package eu.transkribus.swt_gui.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.customtags.CustomTagFactory;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

/**
 * @deprecated not used
 * @author sebas
 *
 */
public class TaggingController {
	private static final Logger logger = LoggerFactory.getLogger(TaggingController.class);
	
	TrpMainWidget mw;
	TrpSettings trpSets;
	static Storage storage = Storage.getInstance();

	public TaggingController(TrpMainWidget mw) {
		this.mw = mw;
		this.trpSets = mw.getTrpSets();
	}
	
	public void createNewTag(String tagName) {
		try {
			logger.debug("creating new tag: "+tagName);
			CustomTagFactory.addToRegistry(CustomTagFactory.create(tagName), null);
			
			mw.getUi().getLineTranscriptionWidget().redrawText(true);
			mw.getUi().getWordTranscriptionWidget().redrawText(true);
		} catch (Exception ex) {
			mw.onError("Cannot add tag", ex.getMessage(), ex);
		}
	}
	
	
}
