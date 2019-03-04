package eu.transkribus.swt_gui.htr.treeviewer;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.swt.graphics.Color;

import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionLabelProvider;

public class CollectionColoredLabelProvider extends CollectionLabelProvider implements IColorProvider {
		
	DataSetSelectionHandler handler;
	
	public CollectionColoredLabelProvider(DataSetSelectionHandler handler) {
		this.handler = handler;
	}
	
	@Override
	public Color getBackground(Object element) {
		if(element instanceof TrpDocMetadata) {
			TrpDocMetadata doc = (TrpDocMetadata) element;
			if (handler.getTrainDocMap().containsKey(doc) && handler.getTestDocMap().containsKey(doc)) {
				return DataSetSelectionSashForm.CYAN;
			} else if (handler.getTrainDocMap().containsKey(doc)) {
				if (doc.getNrOfPages() == handler.getTrainDocMap().get(doc).size()) {
					return DataSetSelectionSashForm.BLUE;
				} else {
					return DataSetSelectionSashForm.LIGHT_BLUE;
				}
			} else if (handler.getTestDocMap().containsKey(doc)) {
				if (doc.getNrOfPages() == handler.getTestDocMap().get(doc).size()) {
					return DataSetSelectionSashForm.GREEN;
				} else {
					return DataSetSelectionSashForm.LIGHT_GREEN;
				}
			}
		} else if (element instanceof TrpPage) {
			TrpPage page = (TrpPage) element;
			if (isPageInMap(page, handler.getTrainDocMap())) {
				return DataSetSelectionSashForm.BLUE;
			} else if (isPageInMap(page, handler.getTestDocMap())) {
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
