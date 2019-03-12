package eu.transkribus.swt_gui.htr.treeviewer;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.swt.graphics.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.GroundTruthSelectionDescriptor.GtSetType;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSet;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSetElement;

/**
 * {@link HtrGroundTruthLabelProvider} for use in training data set preparation with a {@link DataSetSelectionHandler}.
 * Label texts and colors reflect information on data set inclusion of an element.
 */
public class HtrGroundTruthDataSetLabelProvider extends HtrGroundTruthLabelProvider implements IColorProvider {
	private static final Logger logger = LoggerFactory.getLogger(HtrGroundTruthDataSetLabelProvider.class);
	
	protected final DataSetSelectionHandler handler;
	
	public HtrGroundTruthDataSetLabelProvider(DataSetSelectionHandler handler) {
		this.handler = handler;
	}

	@Override
	protected String getText(HtrGtDataSetElement element) {
		final String text = super.getText(element);
		List<HtrGtDataSet> includedBySetList = handler.getGtSetsFromSelectionIncludingElement(element);
		if(includedBySetList.isEmpty()) {
			return text;
		} else {
			return text + " (included by " + 
					includedBySetList.stream()
						.map(s -> "HTR '" + s.getHtr().getName() + "' " + s.getSetType().getLabel())
						.collect(Collectors.joining(", "))
					+ ")";
		}
	}

	@Override
	public Color getBackground(Object element) {
		logger.trace("getBackground() on " + element.getClass().getSimpleName());
		if(element instanceof TrpHtr) {
			HtrGtDataSet trainSet = new HtrGtDataSet((TrpHtr)element, GtSetType.TRAIN);
			HtrGtDataSet validationSet = new HtrGtDataSet((TrpHtr)element, GtSetType.VALIDATION);
			if(handler.getTrainGtMap().containsKey(trainSet) && handler.getTestGtMap().containsKey(validationSet)) {
				return DataSetSelectionSashForm.CYAN;
			} else if (handler.getTrainGtMap().containsKey(trainSet)) {
				return DataSetSelectionSashForm.BLUE;
			} else if (handler.getTestGtMap().containsKey(validationSet)) {
				return DataSetSelectionSashForm.GREEN;
			}
		} else if (element instanceof HtrGtDataSet) {
			HtrGtDataSet dataSet = (HtrGtDataSet) element;
			if(handler.getTrainGtMap().containsKey(dataSet)) {
				if(handler.getTrainGtMap().get(dataSet).size() == dataSet.getNrOfPages()) {
					return DataSetSelectionSashForm.BLUE;
				} else {
					return DataSetSelectionSashForm.LIGHT_BLUE;
				}
			}
			if(handler.getTestGtMap().containsKey(dataSet)) {
				if(handler.getTestGtMap().get(dataSet).size() == dataSet.getNrOfPages()) {
					return DataSetSelectionSashForm.GREEN;
				} else {
					return DataSetSelectionSashForm.LIGHT_GREEN;
				}
			}
		} else if (element instanceof HtrGtDataSetElement) {
			HtrGtDataSetElement gtPage = (HtrGtDataSetElement) element;
			for(Entry<HtrGtDataSet, List<HtrGtDataSetElement>> e : handler.getTrainGtMap().entrySet()) {
				if(e.getValue().stream()
						.anyMatch(g -> g.getGroundTruthPage().getGtId() == gtPage.getGroundTruthPage().getGtId())) {
					if(e.getKey().equals(gtPage.getParentHtrGtDataSet())) {
						return DataSetSelectionSashForm.BLUE;
					} else {
						return DataSetSelectionSashForm.LIGHT_BLUE;
					}
				}
			}
			for(Entry<HtrGtDataSet, List<HtrGtDataSetElement>> e : handler.getTestGtMap().entrySet()) {
				if(e.getValue().stream()
						.anyMatch(g -> g.getGroundTruthPage().getGtId() == gtPage.getGroundTruthPage().getGtId())) {
					if(e.getKey().equals(gtPage.getParentHtrGtDataSet())) {
						return DataSetSelectionSashForm.GREEN;
					} else {
						return DataSetSelectionSashForm.LIGHT_GREEN;
					}
				}
			}
		}
		return DataSetSelectionSashForm.WHITE;
	}

	@Override
	public Color getForeground(Object element) {
		logger.trace("getForeground() on " + element.getClass().getSimpleName());
		if(!DataSetSelectionSashForm.WHITE.equals(getBackground(element))) {
			return DataSetSelectionSashForm.WHITE;
		}
		return DataSetSelectionSashForm.BLACK;
	}
}
