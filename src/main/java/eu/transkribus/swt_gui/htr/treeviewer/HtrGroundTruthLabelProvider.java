package eu.transkribus.swt_gui.htr.treeviewer;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import eu.transkribus.core.model.beans.TrpGroundTruthPage;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSet;

public class HtrGroundTruthLabelProvider implements ILabelProvider {

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
		if(element instanceof TrpGroundTruthPage) {
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
		} else if (element instanceof TrpGroundTruthPage) {
			return getText((TrpGroundTruthPage)element);
		}
		return null;
	}

	private String getText(HtrGtDataSet s) {
		final String nrOfPages = "(" + s.getNrOfPages() + " pages)";
		final String name;
		switch (s.getSetType()) {
		case TRAIN:
			name = "Training Set";
			break;
		case VALIDATION:
			name = "Validation Set";
			break;
		default:
			//This might happen if another set type is defined and not catched here.
			name = "Data Set";
		}
		return StringUtils.rightPad(name, 15) + nrOfPages;
	}
	
	private String getText(TrpGroundTruthPage p) {
		return "Page " + StringUtils.rightPad("" + p.getPageNr(), 5) 
				+ "(" + p.getNrOfLines() + " lines, " + p.getNrOfWordsInLines() + " words)";
	}
}
