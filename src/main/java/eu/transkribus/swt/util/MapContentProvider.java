package eu.transkribus.swt.util;

import java.util.Map;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class MapContentProvider implements IContentProvider, IStructuredContentProvider {
	
	private boolean returnValues=false;
	
	public MapContentProvider() {
	}
	
	public MapContentProvider(boolean returnValues) {
		this.returnValues = returnValues;
	}
	
    @Override
    public Object[] getElements(Object inputElement) {
            Map<Object,Object> inputMap = (Map<Object,Object>)inputElement;
            if (returnValues)
            	return inputMap.values().toArray();
            else
            	return inputMap.entrySet().toArray();
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
}

