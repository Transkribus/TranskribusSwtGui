package solrSearch;

import java.util.ArrayList;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;



public class SearchTest {

	
	  public static void main(String[] args) {
	        Display display = new Display();
	        Shell shell = new Shell(display);

	        shell.setLayout(new FillLayout());
	        
	        TableViewer viewer = new TableViewer(shell);
	        viewer.getTable().setHeaderVisible(true);
	        viewer.getTable().setLinesVisible(true);
	        viewer.setContentProvider(new ArrayContentProvider());
	        
	        TableColumn column = new TableColumn(viewer.getTable(), SWT.NONE);
	        column.setText("Context");
	        column.setWidth(500);
	        TableViewerColumn contextCol = new TableViewerColumn(viewer, column);
	        contextCol.setLabelProvider(new StyledCellLabelProvider(){

	        	  @Override
	        	  public void update(ViewerCell cell) {
	        		
	        		String hlText = ((Hit)cell.getElement()).getHighlightText();
	        	    cell.setText( hlText.replaceAll("<em>", "").replaceAll("</em>", "") );
	        	    
	        	    int hlStart = hlText.indexOf("<em>");
	        	    int hlEnd = hlText.indexOf("</em>");
	        	    int hlLen = hlEnd-hlStart-4;
	        	    
	        	    StyleRange myStyledRange = 
	        	        new StyleRange(hlStart, hlLen, null, 
	        	            Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW));
	        	    StyleRange[] range = { myStyledRange };
	        	    cell.setStyleRanges(range);
	        	    super.update(cell);
	        	  }
	        	
//	            @Override
//	            public String getText(Object element) {
//	                Hit hit = (Hit)element;
//
//	                return hit.getHighlightText();
//	            }

	        });
	        
	        column = new TableColumn(viewer.getTable(), SWT.NONE);
	        column.setText("DocId");
	        column.setWidth(100);
	        TableViewerColumn docCol = new TableViewerColumn(viewer, column);
	        docCol.setLabelProvider(new ColumnLabelProvider(){

	            @Override
	            public String getText(Object element) {
	                Hit hit = (Hit)element;

	                return Integer.toString(hit.getDocId());
	            }

	        });
	        
	        column = new TableColumn(viewer.getTable(), SWT.NONE);
	        column.setText("Page");
	        column.setWidth(100);
	        TableViewerColumn pageCol = new TableViewerColumn(viewer, column);
	        pageCol.setLabelProvider(new ColumnLabelProvider(){

	            @Override
	            public String getText(Object element) {
	                Hit hit = (Hit)element;

	                return Integer.toString(hit.getPageNr());
	            }

	        });
	        
	        
	        Hit h1 = new Hit("asdasdasd <em>asdas</em> blaba <em>asd</em> to", 1, 1);
	        Hit h2 = new Hit("<em>kro</em> sadasd aasd ", 1, 2);
	        Hit h3 = new Hit("asd hit <em>aasd</em> 3", 2, 20);
	        
	        ArrayList<Hit> hits = new ArrayList<Hit>();
	        
	        hits.add(h1);
	        hits.add(h2);
	        hits.add(h3);
	        
	        viewer.setInput(hits);
	        
	        shell.open();
	        while(!shell.isDisposed())
	        {

	            if(!display.readAndDispatch())
	            {
	                display.sleep();
	            }
	        }

	        display.dispose();
	        
	  }
	  

	  private static class Hit
	  {
	    String highlightText;
	    String regionId, lineId, wordId;

		int docId, pageNr;
		      
	    Hit(String hl, int doc, int page, String region, String line, String word){
	    	highlightText = hl;
	    	docId = doc;
	    	pageNr = page;
	    	regionId = region;
	    	lineId = line;
	    	wordId = word;	    	
	    }
	    
	    Hit(String hl, int doc, int page){
	    	highlightText = hl;
	    	docId = doc;
	    	pageNr = page;	    	
	    }
		      
	      public String getHighlightText() {
			return highlightText;
		}

		public void setHighlightText(String highlightText) {
			this.highlightText = highlightText;
		}

		public int getDocId() {
			return docId;
		}

		public void setDocId(int docId) {
			this.docId = docId;
		}

		public int getPageNr() {
			return pageNr;
		}

		public void setPageNr(int pageNr) {
			this.pageNr = pageNr;
		}

		public String getLineId() {
			return lineId;
		}

		public void setLineId(String lineId) {
			this.lineId = lineId;
		}

		public String getWordId() {
			return wordId;
		}

		public void setWordId(String wordId) {
			this.wordId = wordId;
		}		
		
	  }
}