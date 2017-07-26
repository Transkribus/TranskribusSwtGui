package eu.transkribus.swt_gui.metadata;

import org.mihalis.opal.propertyTable.editor.PTIntegerEditor;

class MyPTIntegerEditor extends PTIntegerEditor {
	@Override
	public Object convertValue() {
		try {
			return Integer.parseInt(this.text.getText());
		} catch (final NumberFormatException e) {
			return null;
		}
	}
}