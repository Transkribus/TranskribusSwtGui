package eu.transkribus.swt_gui.htr.treeviewer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpTranscriptMetadata;

public class HtrTrainDataValidator extends TrainDataValidator {
	private static final Logger logger = LoggerFactory.getLogger(HtrTrainDataValidator.class);
	
	@Override
	protected String getTrainableItemCount(TrpTranscriptMetadata tmd) {
		return tmd.getNrOfTranscribedLines() + " lines";
	}
	@Override
	public boolean isQualifiedForTraining(TrpTranscriptMetadata tmd) {
		if(tmd == null) {
			//null should not be passed
			logger.warn("Transcript object is null!");
			return false;
		}
		return tmd.getNrOfTranscribedLines() > 0;
	}
}
