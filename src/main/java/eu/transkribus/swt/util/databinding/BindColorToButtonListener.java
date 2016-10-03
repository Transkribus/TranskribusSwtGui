package eu.transkribus.swt.util.databinding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Display;

import eu.transkribus.swt.util.Colors;
import eu.transkribus.util.IPropertyChangeSupport;

public class BindColorToButtonListener implements SelectionListener, PropertyChangeListener {
	Button button;
//	Label label;
	String property;
	Object bean;
	
	public BindColorToButtonListener(String property, IPropertyChangeSupport bean, Button button) {
		this.button = button;
		this.property = property;
		this.bean = bean;
		
		
		
//		String sp = BeanUtils.getSimpleProperty(bean, property);
//		System.out.println("VALUE OF PROPERTY: "+sp);
	
//		this.label = label;
		updateSelectionIfChanged();
		
		
		bean.addPropertyChangeListener(this);
		button.addSelectionListener(this);
		
		// this draws the current color of the property into the background of the button: 
		button.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				e.gc.setForeground(getValueOfProperty());
				e.gc.setBackground(getValueOfProperty());
				
				int o = 5;
				e.gc.fillRectangle(e.x+o, e.y+o, e.width-2*o, e.height-2*o);
			}
		});
	}
	
	private Color getValueOfProperty() {
		try {
			PropertyUtilsBean p = new PropertyUtilsBean();
			return (Color) p.getProperty(bean, property);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void updatePropertyFromWidget() {
		ColorDialog cd = new ColorDialog(Display.getCurrent().getActiveShell());
		cd.setText("Choose color");
		cd.setRGB(getValueOfProperty().getRGB());
		RGB newColor = cd.open();
		if (newColor == null)
			return;
				
		try {
			BeanUtils.setProperty(bean, property, Colors.createColor(newColor));
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}	
	}
	
	private void updateSelectionIfChanged() {
//		if (!label.getBackground().getRGB().equals(getValueOfProperty())) {
//			logger.debug("setting background of label to: "+getValueOfProperty());
//		button.setForeground(getValueOfProperty());
//		button.setBackground(getValueOfProperty());
		

		
//		button.setImage(new Image(getValueOfProperty()));
//		}
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
