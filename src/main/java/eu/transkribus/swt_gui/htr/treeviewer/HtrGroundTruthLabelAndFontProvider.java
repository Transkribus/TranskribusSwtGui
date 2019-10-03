package eu.transkribus.swt_gui.htr.treeviewer;

import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.swt.graphics.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSet;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSetElement;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.TrpHtrGtDocMetadata;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

/**
 * HtrGroundTruthLabelAndFontProvider that sets a bold font on the GT pages loaded in the MainWidget/Storage 
 */
public class HtrGroundTruthLabelAndFontProvider extends HtrGroundTruthLabelProvider implements IFontProvider {
	private static final Logger logger = LoggerFactory.getLogger(HtrGroundTruthLabelAndFontProvider.class);
	
	protected final Font boldFont;
	protected Storage store;
	public HtrGroundTruthLabelAndFontProvider(Font defaultFont) {
		super();
		this.store = Storage.getInstance();
		this.boldFont = Fonts.createBoldFont(defaultFont);
	}
	@Override
	public Font getFont(Object element) {
		if(!store.isGtDoc()) {
			return null;
		}
		HtrGtDataSet loadedSet = ((TrpHtrGtDocMetadata) store.getDoc().getMd()).getDataSet();

		if(element instanceof HtrGtDataSet) {
			logger.debug("getFont for " + element + " | loadedSet = " + loadedSet);
			if(((HtrGtDataSet)element).equals(loadedSet)) {
				return boldFont;
			}
		} else if (element instanceof HtrGtDataSetElement 
				&& ((HtrGtDataSetElement)element).getParentGtDataSet().equals(loadedSet) 
				&& ((HtrGtDataSetElement)element).getGroundTruthPage().getPageNr() == store.getPage().getPageNr()) {
			return boldFont;
		}
		return null;
	}
}
