package eu.transkribus.swt_gui.htr.treeviewer;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import eu.transkribus.core.model.beans.TrpGroundTruthPage;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSet;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSetElement;

public class HtrGroundTruthLabelProvider extends LabelProvider {	
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

	protected String getText(HtrGtDataSet s) {
		final String nrOfPages = "(" + s.getSize() + " pages)";
		return StringUtils.rightPad(s.getDataSetType().getLabel(), 15) + nrOfPages;
	}

	protected String getText(HtrGtDataSetElement element) {
		TrpGroundTruthPage p = element.getGroundTruthPage();
		return "Page " + StringUtils.rightPad("" + p.getPageNr(), 5) 
				+ "(" + p.getNrOfLines() + " lines, " + p.getNrOfWordsInLines() + " words)";
	}
}
