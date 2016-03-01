package eu.transkribus.swt_gui.doc_overview;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.KwsDocHit;
import eu.transkribus.core.model.beans.KwsHit;
import eu.transkribus.core.model.beans.KwsPageHit;
import eu.transkribus.swt_gui.structure_tree.StructureTreeWidget.ColConfig;

public class KwsTreeLabelProvider extends CellLabelProvider implements ITableLabelProvider {
	private final static Logger logger = LoggerFactory.getLogger(KwsTreeLabelProvider.class);

	@Override
	public void addListener(ILabelProviderListener listener) {		
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}
	
	public static String getTextForElement(Object element, int columnIndex) {
		String type="", docId="", title="", pageNr = "", hits="", score="", lineId="";
		
		if (element instanceof KwsDocHit) {
			type = "Document";
			KwsDocHit hit = ((KwsDocHit)element);
			docId = ""+hit.getDocId();
			title = hit.getDocTitle();
			score = ""+hit.getScore();
			hits = ""+hit.getHitList().size();
		} else if (element instanceof KwsPageHit) {
			type = "Page";
			KwsPageHit hit = (KwsPageHit)element;
			pageNr = ""+hit.getPageNr();
			hits = ""+hit.getHitList().size();
			score = ""+hit.getScore();					
		} else if(element instanceof KwsHit){
			type = "Hit";
			KwsHit hit = (KwsHit)element;
			score = ""+hit.getScore();
			lineId = ""+hit.getLineId();
		}

		if (columnIndex < 0 || columnIndex >= DocSearchDialog.COLUMNS.length)
			return "wrong col index";
		else if (DocSearchDialog.COLUMNS[columnIndex] == DocSearchDialog.TYPE_COL)
			return type;
		else if (DocSearchDialog.COLUMNS[columnIndex] == DocSearchDialog.DOC_ID_COL)
			return docId;
		else if (DocSearchDialog.COLUMNS[columnIndex] == DocSearchDialog.TITLE_COL)
			return title;
		else if (DocSearchDialog.COLUMNS[columnIndex] == DocSearchDialog.PAGE_NR_COL)
			return pageNr;
		else if (DocSearchDialog.COLUMNS[columnIndex] == DocSearchDialog.SCORE_COL)
			return score;
		else if (DocSearchDialog.COLUMNS[columnIndex] == DocSearchDialog.LINE_ID_COL)
			return lineId;
		else if (DocSearchDialog.COLUMNS[columnIndex] == DocSearchDialog.HITS_COL)
			return hits;
		
		return "fucked up code!";
		
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		return getTextForElement(element, columnIndex);
	}
	
	// CellLabelProvider:

	@Override
	public void update(ViewerCell cell) {
		ColConfig cf = DocSearchDialog.COLUMNS[cell.getColumnIndex()];
		Object element = cell.getViewerRow().getElement();
//		logger.trace("column = "+cf.name);
		 
		String text = KwsTreeLabelProvider.getTextForElement(element, cell.getColumnIndex());
		cell.setText(text);
	}



}
