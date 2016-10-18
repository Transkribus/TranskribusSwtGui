package eu.transkribus.swt_gui.doc_overview;

import java.util.Arrays;
import java.util.ListIterator;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class DocTableLabelProvider implements ITableLabelProvider, ITableFontProvider {
	private final static Logger logger = LoggerFactory.getLogger(DocTableLabelProvider.class);
	
//	DocOverviewWidget docOverviewWidget;
	Font boldFont;
//	DocTableWidget docTableWidget;
	Table table;
	TableViewer tableViewer;
	
//	public DocOverviewLabelProvider(DocOverviewWidget docOverviewWidget) {
//		this.docOverviewWidget = docOverviewWidget;
//		this.boldFont = Fonts.createBoldFont(docOverviewWidget.getTableViewer().getControl().getFont());
//	}

	public DocTableLabelProvider(TableViewer tableViewer) {
//		this.docTableWidget = docTableWidget;
		this.tableViewer = tableViewer;
		this.table = tableViewer.getTable();
		this.boldFont = Fonts.createBoldFont(tableViewer.getControl().getFont());
	}

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

	@Override
	public String getColumnText(Object element, int columnIndex) {
		//logger.trace("get column text: "+element+" id: "+columnIndex);
		
		if (element instanceof TrpDocMetadata) {
			TrpDocMetadata doc = (TrpDocMetadata) element;
			
			TableColumn column = table.getColumn(columnIndex);
			String ct = column.getText();
			
			if (ct.equals(DocTableWidget.DOC_NR_COL)) {
			    if (tableViewer != null) {
			    	
			    	int sizeOfRealTableEntries = 1;
			    	/*
			    	 * for numbering the documents in ascending order
			    	 * search for the TableItem which corresponds to the current docID and use the found index to label the Doc_Nr_Col 
			    	 */
			    	ListIterator iterator = Arrays.asList(tableViewer.getTable().getItems()).listIterator();
			    	while (iterator.hasNext()){
			    		TableItem ti = (TableItem) iterator.next();
			    		//logger.debug("TableItem text at docId position: " + ti.getText(1));
			    		if(ti.getText() != null && ti.getText(1) != ""){
			    			sizeOfRealTableEntries++;
			    			//logger.debug("real table entries " + sizeOfRealTableEntries);
			    		}
			    		//logger.debug("doc ID from doc " + doc.getDocId());
			    		if (ti.getText(1).equals(String.valueOf(doc.getDocId()))){	
			    			//logger.debug("return doc ID found in table " + doc.getDocId());
			    			return "" + (Arrays.asList(tableViewer.getTable().getItems()).indexOf(ti) + 1);
			    			
			    		}
			    	}	
			    	

			    	/*
			    	 *while the table is empty this method gets used
			    	 */
//			    	int index = Arrays.asList(docTableWidget.getTableViewer().getTable().getItems()).size();
//			    	logger.debug("size of table viewer items is " + index);
			    	if (sizeOfRealTableEntries != 0){
			    		//logger.debug("return " + sizeOfRealTableEntries);
			    		return "" + (sizeOfRealTableEntries);
			    	}
			        
			    }
			}
			else if (ct.equals(DocTableWidget.DOC_ID_COL)) {
				return ""+doc.getDocId();
			} else if (ct.equals(DocTableWidget.DOCS_TITLE_COL)) {
				return doc.getTitle();
			} else if (ct.equals(DocTableWidget.DOC_NPAGES_COL)) {
				return ""+doc.getNrOfPages();
			} else if (ct.equals(DocTableWidget.DOC_OWNER_COL)) {
				return ""+doc.getUploader();
			} else if (ct.equals(DocTableWidget.DOC_COLLECTIONS_COL)) {
				return doc.getColString();
			}
		}
		
//		if (element instanceof TrpDocMetadata) {
//			TrpDocMetadata md = (TrpDocMetadata) element;
//			switch (columnIndex) {
//			case 0:
//				return ""+md.getDocId();
//			case 1:
//				return md.getTitle();
//			case 2:
//				return ""+md.getNrOfPages();
//			case 3:
//				return ""+md.getUploader();
////			case 4:
////				return ""+md.isFfa();
//			}
//		}

		return "i am error";
	}

	@Override public Font getFont(Object element, int columnIndex) {
		if (element instanceof TrpDocMetadata) {
			TrpDocMetadata md = (TrpDocMetadata) element;
//			if (md.getDocId() == docTableWidget.getSelectedId())
//				return boldFont;
			if (md.getDocId() == Storage.getInstance().getDocId())
				return boldFont;
		}
		
		return null;
	}



}
