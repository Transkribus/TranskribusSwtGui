package eu.transkribus.swt_gui.search.kws;

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

		if (columnIndex < 0 || columnIndex >= KeywordSpottingComposite.COLUMNS.length)
			return "wrong col index";
		else if (KeywordSpottingComposite.COLUMNS[columnIndex] == KeywordSpottingComposite.TYPE_COL)
			return type;
		else if (KeywordSpottingComposite.COLUMNS[columnIndex] == KeywordSpottingComposite.DOC_ID_COL)
			return docId;
		else if (KeywordSpottingComposite.COLUMNS[columnIndex] == KeywordSpottingComposite.TITLE_COL)
			return title;
		else if (KeywordSpottingComposite.COLUMNS[columnIndex] == KeywordSpottingComposite.PAGE_NR_COL)
			return pageNr;
		else if (KeywordSpottingComposite.COLUMNS[columnIndex] == KeywordSpottingComposite.SCORE_COL)
			return score;
		else if (KeywordSpottingComposite.COLUMNS[columnIndex] == KeywordSpottingComposite.LINE_ID_COL)
			return lineId;
		else if (KeywordSpottingComposite.COLUMNS[columnIndex] == KeywordSpottingComposite.HITS_COL)
			return hits;
		
		return "i am error!";
		
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		return getTextForElement(element, columnIndex);
	}
	
	// CellLabelProvider:

	@Override
	public void update(ViewerCell cell) {
		ColConfig cf = KeywordSpottingComposite.COLUMNS[cell.getColumnIndex()];
		Object element = cell.getViewerRow().getElement();
//		logger.trace("column = "+cf.name);
		 
		String text = KwsTreeLabelProvider.getTextForElement(element, cell.getColumnIndex());
		cell.setText(text);
	}



}
