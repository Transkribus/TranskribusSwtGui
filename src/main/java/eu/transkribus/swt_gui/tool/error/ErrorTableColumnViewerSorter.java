package eu.transkribus.swt_gui.tool.error;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpErrorList;
import eu.transkribus.swt.util.TableViewerSorter;

public class ErrorTableColumnViewerSorter extends TableViewerSorter {
	
	private final static Logger logger = LoggerFactory.getLogger(ErrorTableColumnViewerSorter.class);

	public ErrorTableColumnViewerSorter(TableViewer viewer, TableColumn column) {
		super(viewer, column);
	}
	
	int getRowIndex(Viewer viewer, Object e) {
		if (e==null)
			return -1;
		
		for (int i=0; i<((TableViewer)viewer).getTable().getItems().length; ++i) {
			if (((TableViewer)viewer).getTable().getItem(i).getData() == e) {
				return i;
			}
		}
		return -1;
	}

	@Override
	protected int doCompare(Viewer viewer, Object e1, Object e2) {
		
		logger.debug("e1 = "+e1+" e2 = "+e2);
		
		TrpErrorList list1 = (TrpErrorList) e1;
		TrpErrorList list2 = (TrpErrorList) e2;
		
		Integer p1 = null, p2 = null;
		Double l1 = null, l2 = null;
		
		int r1 = getRowIndex(viewer, e1);
		int r2 = getRowIndex(viewer, e2);
		
		if (r1 != -1) {
			switch(r1) {
			
			case 0:
				p1 = list1.getPageNumber();
			case 1: 
				l1 = list1.getWerDouble();
			case 2: 
				l1 = list1.getCerDouble();
			case 3:
				l1 = list1.getwAccDouble();
			case 4:
				l1 = list1.getcAccDouble();
			case 5:
				l1 = list1.getBagTokensPrecDouble();
			case 6:
				l1 = list1.getBagTokensRecDouble();
			case 7:
				l1 = list1.getBagTokensFDouble();
			}
			
		}if (r2 != -1) {
		
			switch(r2) {
			
			case 0:
				p2 = list2.getPageNumber();
			case 1: 
				l2 = list2.getWerDouble();
			case 2: 
				l2 = list2.getCerDouble();
			case 3:
				l2 = list2.getwAccDouble();
			case 4:
				l2 = list2.getcAccDouble();
			case 5:
				l2 = list2.getBagTokensPrecDouble();
			case 6:
				l2 = list2.getBagTokensRecDouble();
			case 7:
				l2 = list2.getBagTokensFDouble();
			}
		}
		
	if (l1 == null && l2 == null) {
		logger.debug("l1 and l2 is null");
		return 0;
	}
	else if (l1 == null && l2 != null) {
		logger.debug("l1 is null");
		return -1;
	}
	else if (l1 != null && l2 == null) {
		logger.debug("l2 is null");
		return 1;
	}
	else if ( r1 == 0 && r2 == 0) {
		logger.debug("p1 :"+p1+"  p2 : +" +p2);
		return Integer.compare(p1, p2);
	}
		

	try {
		logger.debug("l1 "+ l1 +" & l2 "+l2);
		return Double.compare(l1, l2);
	} catch (NumberFormatException e) {
		logger.debug("Error happened");
	}
	logger.debug("Return 0 something went wrong");
	return 0;
}
}