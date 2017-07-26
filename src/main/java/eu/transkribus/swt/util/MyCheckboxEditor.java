package eu.transkribus.swt.util;

import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.swt.widgets.Table;

public class MyCheckboxEditor extends CheckboxCellEditor {
	
	public MyCheckboxEditor(Table table) {
		super(table);
	}
	
    protected void doSetValue(Object value) {
    	if (value == null)
    		super.doSetValue(Boolean.FALSE);
    	
    	String s = String.valueOf(value);
    	super.doSetValue(Boolean.valueOf(s));
    }

}
