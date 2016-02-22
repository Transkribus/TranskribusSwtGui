package org.dea.swt.mytableviewer;

import org.dea.swt.util.TableViewerSorter;
import org.eclipse.swt.SWT;

public class ColumnConfig {
	public String name = "zipfel";
	public int width = 100;
	public boolean defaultSorting = false;
	public int sortingDirection = TableViewerSorter.ASC;
	public int style = SWT.LEFT;
	public TableViewerSorter sorter=null;
	
	public ColumnConfig(String name, int width) {
		this(name, width, false, TableViewerSorter.ASC, SWT.LEFT, null);
	}
	
	public ColumnConfig(String name, int width, boolean defaultSorting) {
		this(name, width, defaultSorting, TableViewerSorter.ASC, SWT.LEFT, null);
	}
	
	public ColumnConfig(String name, int width, boolean defaultSorting, int sortingDirection) {
		this(name, width, defaultSorting, sortingDirection, SWT.LEFT, null);
	}	
	
	public ColumnConfig(String name, int width, boolean defaultSorting, int sortingDirection, int style, TableViewerSorter sorter) {
		super();
		this.name = name;
		this.width = width;
		this.defaultSorting = defaultSorting;
		this.sortingDirection = sortingDirection;
		this.style = style;
		this.sorter = sorter;
	}

}
