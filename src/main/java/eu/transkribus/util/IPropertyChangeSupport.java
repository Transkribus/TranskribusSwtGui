package eu.transkribus.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public interface IPropertyChangeSupport {
	
	void addPropertyChangeListener(PropertyChangeListener l);
	void removePropertyChangeListener(PropertyChangeListener l);
	PropertyChangeSupport getChanges();
	void firePropertyChange(String propertyName, boolean oldValue, boolean newValue);

}
