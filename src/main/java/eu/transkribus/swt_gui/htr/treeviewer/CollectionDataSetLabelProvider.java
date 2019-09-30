package eu.transkribus.swt_gui.htr.treeviewer;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.swt.graphics.Color;

import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionLabelProvider;

/**
 * {@link CollectionLabelProvider} for use in training data set preparation with a {@link DataSetSelectionController}.
 * Label texts and colors reflect information on data set inclusion of an element.
 */
public class CollectionDataSetLabelProvider extends CollectionLabelProvider implements IColorProvider {
		
	DataSetSelectionController controller;
	
	public CollectionDataSetLabelProvider(DataSetSelectionController controller) {
		this.controller = controller;
	}
	
	@Override
	public String getText(Object element) {
		if(element instanceof TrpDocMetadata) {
			return super.getText(element);
		} else if (element instanceof TrpPage) {
			//adapt behavior for pages for respecting the selected transcript version
			TrpPage p = (TrpPage)element;
			return "Page " + p.getPageNr() + " (" + controller.getTrainDataSizeLabel(p) + ")";
		}
		return null;
	}
	
	@Override
	public Color getBackground(Object element) {
		if(element instanceof TrpDocMetadata) {
			TrpDocMetadata doc = (TrpDocMetadata) element;
			if (controller.getTrainDocMap().containsKey(doc) && controller.getValDocMap().containsKey(doc)) {
				return DataSetSelectionSashForm.CYAN;
			} else if (controller.getTrainDocMap().containsKey(doc)) {
				if (doc.getNrOfPages() == controller.getTrainDocMap().get(doc).size()) {
					return DataSetSelectionSashForm.BLUE;
				} else {
					return DataSetSelectionSashForm.LIGHT_BLUE;
				}
			} else if (controller.getValDocMap().containsKey(doc)) {
				if (doc.getNrOfPages() == controller.getValDocMap().get(doc).size()) {
					return DataSetSelectionSashForm.GREEN;
				} else {
					return DataSetSelectionSashForm.LIGHT_GREEN;
				}
			}
		} else if (element instanceof TrpPage) {
			TrpPage page = (TrpPage) element;
			if (isPageInMap(page, controller.getTrainDocMap())) {
				return DataSetSelectionSashForm.BLUE;
			} else if (isPageInMap(page, controller.getValDocMap())) {
				return DataSetSelectionSashForm.GREEN;
			}
		}
		return DataSetSelectionSashForm.WHITE;
	}

	@Override
	public Color getForeground(Object element) {
		if(!DataSetSelectionSashForm.WHITE.equals(getBackground(element))) {
			return DataSetSelectionSashForm.WHITE;
		}
		if(element instanceof TrpPage && !controller.isQualifiedForTraining((TrpPage) element)) {
			return DataSetSelectionSashForm.GRAY;
		}
		return DataSetSelectionSashForm.BLACK;
	}

	private boolean isPageInMap(TrpPage page, Map<TrpDocMetadata, List<TrpPage>> docMap) {
		for(Entry<TrpDocMetadata, List<TrpPage>> e : docMap.entrySet()) {
			if(e.getKey().getDocId() != page.getDocId()) {
				continue;
			}
			return e.getValue().stream()
					.anyMatch(p -> p.getPageId() == page.getPageId());
		}
		return false;
	}
}
