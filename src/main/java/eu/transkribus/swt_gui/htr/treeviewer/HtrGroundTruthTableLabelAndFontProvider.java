package eu.transkribus.swt_gui.htr.treeviewer;

import java.text.DateFormat;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.ReleaseLevel;
import eu.transkribus.core.model.beans.TrpGroundTruthPage;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSet;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSetElement;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.TrpHtrGtDocMetadata;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.structure_tree.StructureTreeWidget.ColConfig;

/**
 * CellLabelProvider that sets a bold font on the GT pages loaded in the MainWidget/Storage 
 */
public class HtrGroundTruthTableLabelAndFontProvider extends CellLabelProvider implements ITableLabelProvider, IFontProvider {
	private static final Logger logger = LoggerFactory.getLogger(HtrGroundTruthTableLabelAndFontProvider.class);
	
	protected final Font boldFont;
	protected Storage store;
	DateFormat createDateFormat;
	
	public HtrGroundTruthTableLabelAndFontProvider(Font defaultFont) {
		super();
		this.store = Storage.getInstance();
		this.boldFont = Fonts.createBoldFont(defaultFont);
		this.createDateFormat = CoreUtils.newDateFormatddMMYY();
	}
	
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		ColConfig col = HtrPagedTreeWidget.COLUMNS[columnIndex];
		if(!HtrPagedTreeWidget.NAME_COL.equals(col)) {
			return null;
		}
		if(element instanceof HtrGtDataSetElement) {
			return Images.IMAGE;
		} else if (element instanceof HtrGtDataSet) {
			return Images.FOLDER;
		} else if (element instanceof TrpHtr) {
			if(((TrpHtr)element).getReleaseLevelValue() > 0) {
				return Images.MODEL_SHARED_ICON;
			}
			return Images.MODEL_ICON;
		}
		return null;
	}
	
	@Override
	public String getColumnText(Object element, int columnIndex) {
		ColConfig col = HtrPagedTreeWidget.COLUMNS[columnIndex];
		if(HtrPagedTreeWidget.ID_COL.equals(col)) {
			if(element instanceof TrpHtr) {
				return "" + ((TrpHtr)element).getHtrId();
			}
		} else if(HtrPagedTreeWidget.NAME_COL.equals(col)) {
			if(element instanceof TrpHtr) {
				return ((TrpHtr)element).getName();
			} else if (element instanceof HtrGtDataSet) {
				HtrGtDataSet set = (HtrGtDataSet) element;
				String suffix = "";
				if(set.getModel().getReleaseLevelValue() > 0 
						&& ReleaseLevel.isPrivateDataSet(set.getModel().getReleaseLevel())) {
					suffix = " (private)";
				}
				return set.getDataSetType().getLabel() + suffix;
			} else if (element instanceof HtrGtDataSetElement) {
				return "Page " + ((HtrGtDataSetElement)element).getGroundTruthPage().getPageNr();
			}
		} else if(HtrPagedTreeWidget.SIZE_COL.equals(col)) {
			if (element instanceof HtrGtDataSet) {
				return((HtrGtDataSet)element).getSize() + " pages" ;
			} else if (element instanceof HtrGtDataSetElement) {
				TrpGroundTruthPage p = ((HtrGtDataSetElement)element).getGroundTruthPage();
				return p.getNrOfLines() + " lines, " + p.getNrOfWordsInLines() + " words";
			}
		} else if(HtrPagedTreeWidget.CURATOR_COL.equals(col)) {
			if(element instanceof TrpHtr) {
				return ((TrpHtr)element).getUserName();
			}
		} else if(HtrPagedTreeWidget.DATE_COL.equals(col)) {
			if(element instanceof TrpHtr) {
				return createDateFormat.format(((TrpHtr)element).getCreated());
			}
		} else if(HtrPagedTreeWidget.WORD_COL.equals(col)) {
			if(element instanceof TrpHtr) {
				return Integer.toString(((TrpHtr)element).getNrOfWords());
			}
	}
	
		
		return null;
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

	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getViewerRow().getElement();
		cell.setText(getColumnText(element, cell.getColumnIndex()));
		cell.setImage(getColumnImage(element, cell.getColumnIndex()));
		cell.setFont(getFont(element));
	}
}
