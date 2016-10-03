package eu.transkribus.swt.util;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;

public class TableLabelProvider implements ITableLabelProvider, ITableFontProvider {

	@Override public void addListener(ILabelProviderListener listener) {
	}

	@Override public void dispose() {
	}

	@Override public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	@Override public void removeListener(ILabelProviderListener listener) {
	}

	@Override public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override public String getColumnText(Object element, int columnIndex) {
		return null;
	}

	@Override public Font getFont(Object element, int columnIndex) {
		
		Font f = new Font(null, new FontData());
		
		return null;
	}

}
