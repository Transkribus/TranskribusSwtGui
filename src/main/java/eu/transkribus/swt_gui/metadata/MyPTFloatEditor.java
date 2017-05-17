package eu.transkribus.swt_gui.metadata;

import org.mihalis.opal.propertyTable.editor.PTFloatEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MyPTFloatEditor extends PTFloatEditor {
	private final static Logger logger = LoggerFactory.getLogger(MyPTFloatEditor.class);
	
	@Override public Object convertValue() {
		if (text.getText() == null || text.getText().isEmpty())
			return null;
		else {
			try {
				return Float.parseFloat(this.text.getText());
			} catch (NullPointerException | NumberFormatException e) {
				logger.warn("Could not convert text to float: "+text.getText());
				return null;
			}
		}
	}

}