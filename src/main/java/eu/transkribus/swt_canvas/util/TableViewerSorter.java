package eu.transkribus.swt_canvas.util;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TableViewerSorter extends ViewerComparator {
	private final static Logger logger = LoggerFactory.getLogger(TableViewerSorter.class);
	
	public static final int ASC = 1;
	
	public static final int NONE = 0;
	
	public static final int DESC = -1;
	
	protected int direction = 0;
	
	private ViewerColumn viewerColumn;
	protected TableColumn tableColumn;
//	protected ColumnViewer viewer;
	protected TableViewer viewer;
//	protected TreeViewer tw;
	
	protected int columnIndex;
	
	public TableViewerSorter(TableViewer viewer) {
		this.viewer = viewer;
	}
	
	public TableViewerSorter(TableViewer viewer, TableViewerColumn column) {
		this(viewer, column.getColumn());
	}
	
	public TableViewerSorter(TableViewer viewer, TableColumn column) {
		this.viewer = viewer;
		setColumn(column);		
	}
	
	public void setColumn(TableColumn col) {
		tableColumn = col;
		
		if (tableColumn != null) {
			this.tableColumn.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if( TableViewerSorter.this.viewer.getComparator() != null ) {
						if( TableViewerSorter.this.viewer.getComparator() == TableViewerSorter.this ) {
							int tdirection = TableViewerSorter.this.direction;
							
							if( tdirection == ASC ) {
								setSorter(TableViewerSorter.this, DESC);
							} else if( tdirection == DESC ) {
								setSorter(TableViewerSorter.this, NONE);
							}
						} else {
							setSorter(TableViewerSorter.this, ASC);
						}
					} else {
						setSorter(TableViewerSorter.this, ASC);
					}
				}
			});
			initColumnIndex();
		}
	}
	
	private void initColumnIndex() {
//		logger.debug("columns: "+viewer.getTable().getColumns().length+" "+viewer.getTable().getColumns()[0]+" "+tableColumn);
//		viewer.getCo
		
		for (int i=0; i<viewer.getTable().getColumns().length; ++i) {
			if (viewer.getTable().getColumns()[i].equals(tableColumn)) {
				columnIndex=i;
				logger.trace("columnIndex = "+columnIndex);
				return;
			}
		}
		throw new RuntimeException("Cannot determine column index for column: "+tableColumn.getText());
	}

	public void setSorter(TableViewerSorter sorter, int direction) {
		if (tableColumn == null || viewer == null)
			return;
		
		if( direction == NONE ) {
			tableColumn.getParent().setSortColumn(null);
			tableColumn.getParent().setSortDirection(SWT.NONE);
			viewer.setComparator(null);
		} else {
			tableColumn.getParent().setSortColumn(tableColumn);
			sorter.direction = direction;
			
			if( direction == ASC ) {
				tableColumn.getParent().setSortDirection(SWT.DOWN);
			} else {
				tableColumn.getParent().setSortDirection(SWT.UP);
			}
			
			if( viewer.getComparator() == sorter ) {
				viewer.refresh();
			} else {
				viewer.setComparator(sorter);
			}
		}
	}

	public int compare(Viewer viewer, Object e1, Object e2) {
		return direction * doCompare(viewer, e1, e2);
	}
	
	protected abstract int doCompare(Viewer viewer, Object e1, Object e2);

	public void setViewer(TableViewer viewer2) {
		this.viewer = viewer2;
	}


}
