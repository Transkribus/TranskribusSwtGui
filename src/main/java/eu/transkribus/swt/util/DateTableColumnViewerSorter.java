package eu.transkribus.swt.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class DateTableColumnViewerSorter extends TableViewerSorter {
	private final static Logger logger = LoggerFactory.getLogger(DefaultTableColumnViewerSorter.class);
	
	static DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
	
	public DateTableColumnViewerSorter(TableViewer viewer, TableColumn column) {
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
	
	
	@Override protected int doCompare(Viewer viewer, Object e1, Object e2) {
		logger.trace("e1 = "+e1+" e2 = "+e2);
		
		String l1 = null, l2 = null;

		Table t = ((TableViewer)viewer).getTable();
		int r1 = getRowIndex(viewer, e1);
		int r2 = getRowIndex(viewer, e2);
		
		if (r1 != -1)
			l1 = t.getItem(r1).getText(columnIndex);
		if (r2 != -1)
			l2 = t.getItem(r2).getText(columnIndex);


		if (l1 == null && l2 == null)
			return 0;
		else if (l1 == null && l2 != null)
			return -1;
		else if (l1 != null && l2 == null)
			return 1;
		
//		logger.debug("date 1: " + l1);
//		logger.debug("date 2: " + l2);
		
		String dateStr1 = l1;
		String dateStr2 = l2;
		
		try {
			Date i1 = (Date)formatter.parse(dateStr1);
			Date i2 = (Date)formatter.parse(dateStr2);

			if (i1.after(i2))
				return -1;
			else if (i2.after(i1))
				return 1;
			else
				return 0;
		} catch (NullPointerException e) {
			logger.debug("One date in the sorting method was null - should not happen");
			return 0;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}

	}
};
