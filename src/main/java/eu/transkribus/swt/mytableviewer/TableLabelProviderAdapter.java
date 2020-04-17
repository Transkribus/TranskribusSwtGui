package eu.transkribus.swt.mytableviewer;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

public interface TableLabelProviderAdapter extends ITableLabelProvider {
	
	@Override
	default void removeListener(ILabelProviderListener arg0) {
	}
	
	@Override
	default boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}
	
	@Override
	default void dispose() {
	}
	
	@Override
	default void addListener(ILabelProviderListener arg0) {
	}
	
//	@Override
//	default String getColumnText(Object element, int columnIndex) {
//		return null;
//	}
	
	@Override
	default Image getColumnImage(Object element, int columnIndex) {
		return null;
	}
	
}
