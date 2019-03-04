package eu.transkribus.swt_gui.htr.treeviewer;

import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpGroundTruthPage;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.GtSetType;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSet;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSetElement;

public class HtrGroundTruthLabelProvider extends LabelProvider implements IColorProvider {
	private static final Logger logger = LoggerFactory.getLogger(HtrGroundTruthLabelProvider.class);
	
	private final static String TRAIN_SET_LABEL = "Train Set";
	private final static String VALIDATION_SET_LABEL = "Validation Set";
	
	protected final DataSetSelectionHandler handler;
	
	public HtrGroundTruthLabelProvider(DataSetSelectionHandler handler) {
		this.handler = handler;
	}

	@Override
	public Image getImage(Object element) {
		if(element instanceof HtrGtDataSetElement) {
			return Images.IMAGE;
		} else if (element instanceof HtrGtDataSet) {
			return Images.FOLDER;
		} else if (element instanceof TrpHtr) {
			return Images.CHART_LINE;
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if(element instanceof TrpHtr) {
			return ((TrpHtr)element).getName();
		} else if (element instanceof HtrGtDataSet) {
			return getText((HtrGtDataSet)element) ;
		} else if (element instanceof HtrGtDataSetElement) {
			return getText((HtrGtDataSetElement)element);
		}
		return null;
	}

	private String getText(HtrGtDataSet s) {
		final String nrOfPages = "(" + s.getNrOfPages() + " pages)";
		final String name;
		switch (s.getSetType()) {
		case TRAIN:
			name = TRAIN_SET_LABEL;
			break;
		case VALIDATION:
			name = VALIDATION_SET_LABEL;
			break;
		default:
			//This might happen if another set type is defined and not catched here.
			name = "Data Set";
		}
		return StringUtils.rightPad(name, 15) + nrOfPages;
	}

	private String getText(HtrGtDataSetElement element) {
		TrpGroundTruthPage p = element.getGroundTruthPage();
		String text = "Page " + StringUtils.rightPad("" + p.getPageNr(), 5) 
				+ "(" + p.getNrOfLines() + " lines, " + p.getNrOfWordsInLines() + " words)";
		
		switch (element.getParentHtrGtDataSet().getSetType()) {
		case TRAIN:
			for(Entry<HtrGtDataSet, List<HtrGtDataSetElement>> e : handler.getTrainGtMap().entrySet()) {
				if(e.getValue().stream()
						.anyMatch(g -> g.getGroundTruthPage().getGtId() == p.getGtId()) 
						&& !e.getKey().equals(element.getParentHtrGtDataSet())) {
					text += " (included by HTR '" + e.getKey().getHtr().getName() 
							+ "' " + TRAIN_SET_LABEL + ")";
				}
			}
			break;
		case VALIDATION:
			for(Entry<HtrGtDataSet, List<HtrGtDataSetElement>> e : handler.getTestGtMap().entrySet()) {
				if(e.getValue().stream()
						.anyMatch(g -> g.getGroundTruthPage().getGtId() == p.getGtId()) 
						&& !e.getKey().equals(element.getParentHtrGtDataSet())) {
					text += " (included by HTR '" + e.getKey().getHtr().getName() 
							+ "' " + VALIDATION_SET_LABEL + ")";
				}
			}
			break;
		}
		return text;
	}

	@Override
	public Color getBackground(Object element) {
		logger.debug("getBackground() on " + element);
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
		logger.debug("getForeground() on " + element);
		if(!DataSetSelectionSashForm.WHITE.equals(getBackground(element))) {
			return DataSetSelectionSashForm.WHITE;
		}
		return DataSetSelectionSashForm.BLACK;
	}
}
