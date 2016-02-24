package eu.transkribus.swt_canvas.util;

import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Table;

public class MyTextCellEditor extends TextCellEditor {
	
	public MyTextCellEditor(Table table) {
		super(table);
	}
	
    protected void doSetValue(Object value) {
    	if (value == null)
    		super.doSetValue("");
    	else
    		super.doSetValue(""+value);
    }

}
