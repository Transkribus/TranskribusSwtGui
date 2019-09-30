package eu.transkribus.swt_gui.htr.treeviewer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.enums.EditStatus;

public class TrainDataValidator {
	private static final Logger logger = LoggerFactory.getLogger(TrainDataValidator.class);

	/**
	 * Check if the page fulfills requirements for this training process.
	 * If status is null, the latest transript will be considered.
	 * If status is not null, the latest transcript with this status will be checked and the method returns false if the status does not exist.
	 * 
	 * @param page
	 * @param status
	 * @return
	 */
	public boolean isQualifiedForTraining(TrpPage page, EditStatus status) {
		TrpTranscriptMetadata tmd = selectTranscript(page, status);
		if(tmd == null) {
			//no transcript with this status containing text
			return false;
		}
		return isQualifiedForTraining(tmd);
	}
	
	/**
	 * The routine that selects a transcript from the version history of a page throughout the training dialog
	 * 
	 * @param status
	 * @return
	 */
	public TrpTranscriptMetadata selectTranscript(TrpPage page, EditStatus status) {
		//if no status filter is set use latest, otherwise check content of transcript with status
		if(status == null) {
			return page.getCurrentTranscript();
		} else {
			return page.getTranscriptWithStatusOrNull(status);
		}
	}
	
	/**
	 * Check if the transcript metadata qualifies for training. The base implementation just does a null check.<br>
	 * For indepth checks of the PageXML caching should be implemented as this check is called very often!
	 * 
	 * @param tmd
	 * @return
	 */
	public boolean isQualifiedForTraining(TrpTranscriptMetadata tmd) {
		if(tmd == null) {
			//null should not be passed
			logger.warn("Transcript object is null!");
			return false;
		}
		return true;
	}
	
	/**
	 * Return a size label to be shown in tables in treeviewers.
	 * 
	 * @param p
	 * @param status
	 * @return
	 */
	public String getTrainDataSizeLabel(TrpPage p, EditStatus status) {
		TrpTranscriptMetadata tmd = selectTranscript(p, status);
		if(tmd == null) {
			return "no transcript with status " + status.getStr();
		} else {
			return getTrainableItemCount(tmd);
		}
	}

	protected String getTrainableItemCount(TrpTranscriptMetadata tmd) {
		return "unknown number of items";
	}
}
