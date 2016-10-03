package eu.transkribus.swt.util.databinding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.ToolItem;

import eu.transkribus.util.IPropertyChangeSupport;

public class BindBoolBeanValueToToolItemSelectionListener implements SelectionListener, PropertyChangeListener {
	ToolItem ti;
	String property;
	Object bean;
	
	public BindBoolBeanValueToToolItemSelectionListener(String property, IPropertyChangeSupport bean, ToolItem ti) {
		this.ti = ti;
		this.property = property;
		this.bean = bean;
		
//		String sp = BeanUtils.getSimpleProperty(bean, property);
//		System.out.println("VALUE OF PROPERTY: "+sp);
	
		updateSelectionIfChanged();
		
		bean.addPropertyChangeListener(this);
		ti.addSelectionListener(this);
	}
	
	private boolean getValueOfProperty() {
		try {
			return BeanUtils.getSimpleProperty(bean, property).equals("true");
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void updatePropertyFromWidget() {
		try {
			BeanUtils.setProperty(bean, property, ti.getSelection());
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}	
	}
	
	private void updateSelectionIfChanged() {
		if (ti.getSelection() != getValueOfProperty()) {
			ti.setSelection(getValueOfProperty());
		}
	}


	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(property)) {
			updateSelectionIfChanged();
		}
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		updatePropertyFromWidget();
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

}
