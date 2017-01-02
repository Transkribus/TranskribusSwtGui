package eu.transkribus.swt.util;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * works for Array and List input's
 * @author sebastian
 *
 */
public class LazyTableViewerArrayContentProvider implements ILazyContentProvider {
	Object input;
	TableViewer viewer=null;
	
	public static LazyTableViewerArrayContentProvider instance() {
		return new LazyTableViewerArrayContentProvider();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void updateElement(int index) {
        if (input instanceof Object[]) {
    		if (index >= 0 && index < ((Object[]) input).length) {
    			viewer.replace(((Object[]) input)[index], index);
    		}
		}
        else if (input instanceof List<?>) {
    		if (index >= 0 && index < ((List<?>) input).size()) {
    			viewer.replace(((List<?>) input).get(index), index);
    		}        	
		}
        
        return;
	}
	
    /**
     * Returns the elements in the input, which must be either an array or a
     * <code>Collection. 
     */
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof Object[]) {
			return (Object[]) inputElement;
		}
        if (inputElement instanceof Collection) {
			return ((Collection) inputElement).toArray();
		}
        return new Object[0];
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    	if (viewer instanceof TableViewer) {
    		this.viewer = (TableViewer) viewer;
    		this.input = newInput;
    	}
    }



}

