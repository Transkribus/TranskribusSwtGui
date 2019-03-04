package eu.transkribus.swt_gui.collection_treeviewer;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

/**
 * CollectionLabelProvider that shows information on transcription progress for pages.
 *
 */
public class CollectionLabelProviderExtended implements ILabelProvider {
	
	@Override
	public void addListener(ILabelProviderListener listener) {}

	@Override
	public void dispose() {}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {}

	@Override
	public Image getImage(Object element) {
		if(element instanceof TrpPage) {
			return Images.IMAGE;
		} else if (element instanceof TrpDocMetadata) {
			return Images.FOLDER;
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if(element instanceof TrpDocMetadata) {
			TrpDocMetadata d = (TrpDocMetadata)element;
			String docLabel = d.getDocId() + " - " + d.getTitle() + " (" + d.getNrOfPages() + " pages) ";
			if (d.getDocId() == Storage.getInstance().getDocId()){
				docLabel += "currently loaded";
			}
			return docLabel;
		} else if (element instanceof TrpPage) {
			TrpPage p = (TrpPage)element;
			TrpTranscriptMetadata tmd;
			tmd = p.getCurrentTranscript();
			Integer transcribedLines = 0;
			Integer segmentedLines = 0;
			if (tmd != null){
				transcribedLines = tmd.getNrOfTranscribedLines();
				segmentedLines = tmd.getNrOfLines();
			}
				
			
			String transcribedLinesText = "";
			String saveInfo = tmd.getUserName() + ", " + tmd.getTimeFormatted() + "]";

			// logger.debug("segmentedLines: " + segmentedLines);
			// logger.debug("transcribedLines: " + transcribedLines);

			if (segmentedLines != null && segmentedLines == 0) {
				transcribedLinesText = "No lines";
			} else{
				transcribedLinesText = ( (transcribedLines != null && transcribedLines > 0) ? transcribedLines + " lines with text"
						: "No text");
			}
			return "("+tmd.getPageNr()+") " + p.getImgFileName() + " (" + transcribedLinesText + ") # [" + p.getCurrentTranscript().getStatus() + ", " + saveInfo;
		}
		return null;
	}


}
