package eu.transkribus.swt.mytableviewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.TableViewerSorter;
import eu.transkribus.swt.util.TableViewerUtils;

public class MyTableViewer extends TableViewer {
	private final static Logger logger = LoggerFactory.getLogger(MyTableViewer.class);
	
//	List<TableColumn > columns = new ArrayList<>();
	List<TableViewerSorter> sorter = new ArrayList<>();
	protected Table table;
	
	public MyTableViewer(Composite parent) {
		super(parent);
		this.table = getTable();
	}
	
	public MyTableViewer(Composite parent, int style) {
		super(parent, style);
		this.table = getTable();
	}
	
	/** Adds columns to the viewer in the given order. */
	public void addColumns(ColumnConfig... cols) {
		for (ColumnConfig cf : cols) {
			Pair<TableColumn, TableViewerSorter> p = 
					createColumn(this, cf.style, cf.name, cf.width, cf.defaultSorting, cf.sortingDirection, cf.sorter);
//			columns.add(p.getLeft());
			sorter.add(p.getRight());
		}
	}
	
	public int getIndex(String columnName) {
		int i=0;
		for (TableColumn c : getTable().getColumns()) {
			if (c.getText().equals(columnName))
				return i;
			
			++i;
		}
		return -1;
	}
	
	public TableColumn getColumn(String columnName) {
		int i = getIndex(columnName);
		return i==-1 ? null : getColumn(i);
	}
	
	public TableViewerSorter getSorter(String columnName) {
		int i = getIndex(columnName);
		return i==-1 ? null : getSorter(i);		
	}
	
	public TableViewerSorter getSorter(int index) {
		return sorter.get(index);
	}
	
	public TableColumn getColumn(int index) {
		return getTable().getColumn(index);
	}
	
	private static Pair<TableColumn, TableViewerSorter> createColumn(TableViewer viewer, int style, 
			String text, int width, 
			boolean setSorting, int sortDirection, TableViewerSorter sorter) {
		
		TableColumn col = TableViewerUtils.createTableColumn(viewer.getTable(), style, text, width);
	
		if (sorter == null)
			sorter = new DefaultTableColumnViewerSorter(viewer, col);
		else {
			sorter.setViewer(viewer);
			sorter.setColumn(col);
		}
		
		if (setSorting) {
			
			sorter.setSorter(sorter, sortDirection);
		}
		
		return Pair.of(col, sorter);
	}
		
	public <T> SelectionListener setCustomListSorterForColumn(final String cn, final Comparator<T> comp) {
		TableColumn tc = getColumn(cn);
		
		if (tc == null) {
			logger.warn("column not found: "+cn);
			return null;
		}
		
		SelectionListener sl = new SelectionListener() {
			@Override public void widgetSelected(SelectionEvent e) {
				if (!(getInput() instanceof List<?>)) {
					logger.warn("Input is not a list - custom sorting not possible!");
					return;
				}				
				
				int d = getTable().getSortDirection();

				List<T> inputList = (List<T>) getInput();
				
				if (d == SWT.DOWN) {
					logger.debug("sorting down!");
					Collections.sort(inputList, comp);
				} else if (d == SWT.UP) {
					logger.debug("sorting up!");
					Collections.sort(inputList, new Comparator<T>() {
						@Override public int compare(T o1, T o2) {
							return -1*comp.compare(o1, o2);
						}
					});
				}
			}

			@Override public void widgetDefaultSelected(SelectionEvent e) {
				// gets triggered on double click on column header
			}
		};
		
		tc.addSelectionListener(sl);

		return sl;
	}
	
	

}
