package eu.transkribus.swt.util;

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
    
//    @Override protected void keyReleaseOccured(KeyEvent keyEvent) {
//    	System.out.println("keyEvent: "+keyEvent);
//    	
//    	if (keyEvent.keyCode == SWT.TAB) {
//    		keyEvent.doit = false;	
//    	}
//    }

}
