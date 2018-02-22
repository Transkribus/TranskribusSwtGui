package eu.transkribus.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

public abstract class APropertyChangeSupport implements IPropertyChangeSupport {
	private PropertyChangeSupport changes = new PropertyChangeSupport(this);
	
	public APropertyChangeSupport() {	
	}
	
//	public APropertyChangeSupport(Properties props) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
//		init(props);
//	}
	
//	public void init(Properties props) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
//		Utils.setBeanProperties(this, props);
//	}
	
	
	@Override
	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
		changes.firePropertyChange(propertyName, oldValue, newValue);
	}
	
	public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		changes.firePropertyChange(propertyName, oldValue, newValue);
	}
	
	public void firePropertyChange(String propertyName, int oldValue, int newValue) {
		changes.firePropertyChange(propertyName, oldValue, newValue);
	}	
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener l) {
		changes.addPropertyChangeListener(l);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener l) {
		changes.removePropertyChangeListener(l);
	}	
	
	@Override
	public PropertyChangeSupport getChanges() {
		return changes;
	}
	
	public List<String> getPropertiesToNotSave() {
		return new ArrayList<>();
	}
	
	public boolean isSaveProperty(String propertyName) {
		return !(getPropertiesToNotSave().contains(propertyName));
	}
	

}
