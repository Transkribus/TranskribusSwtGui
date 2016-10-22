package eu.transkribus.swt.util.databinding;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.internal.dnd.SwtUtil;

import eu.transkribus.swt.util.DropDownToolItem;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.util.IPropertyChangeSupport;

public class DataBinder {
	static DataBinder binder;
	DataBindingContext ctx = new DataBindingContext();

	List<BindSelectionListener> selectionListener = new ArrayList<>();
	List<BindBoolBeanValueToToolItemSelectionListener> selectionBoolPropertyListener = new ArrayList<>();
	List<BindColorToButtonListener> selectionToColorPropertyListener = new ArrayList<>();

	private DataBinder() {
	}

	public static DataBinder get() {
		if (binder == null) {

			binder = new DataBinder();
		}
		return binder;
	}
	
	public void bindWidgetSelection(Widget src, Widget target) {
		selectionListener.add(new BindSelectionListener(src, target));
	}

	public void bindBoolBeanValueToToolItemSelection(String property, IPropertyChangeSupport bean, ToolItem ti) {
		if (property != null && bean != null && ti != null)
			selectionBoolPropertyListener.add(new BindBoolBeanValueToToolItemSelectionListener(property, bean, ti));
	}

//	public void bindBoolBeanValueToSelection(String property, IPropertyChangeSupport bean, Widget wi) {
//		IObservableValue v1 = BeanProperties.value(property).observe(bean);
//		IObservableValue v2 = SWTObservables.observeSelection(wi);
//		
//		ctx.bindValue(v2, v1);	}

	public Binding bindBeanPropertyToObservableValue(String property, Object bean, IObservableValue v) {
		IObservableValue model = BeanProperties.value(property).observe(bean);

		return ctx.bindValue(v, model);
	}

	public Binding bindBeanToWidgetSelection(String property, Object bean, Widget w) {
		if (SWTUtil.isDisposed(w) || bean == null || property == null)
			return null;
		
		IObservableValue v1 = BeanProperties.value(property).observe(bean);
		IObservableValue v2 = SWTObservables.observeSelection(w);

		return ctx.bindValue(v2, v1);
	}

	public Binding bindBeanToWidgetText(String property, Object bean, Text t) {
		IObservableValue v1 = BeanProperties.value(property).observe(bean);
		IObservableValue v2 = SWTObservables.observeText(t, SWT.Modify);

		return ctx.bindValue(v2, v1);
	}

	public void bindColorToButton(String property, IPropertyChangeSupport bean, Button b) {
		selectionToColorPropertyListener.add(new BindColorToButtonListener(property, bean, b));
	}

	public void removeWidgetSelectionBinding(Widget w1, Widget w2) {
		BindSelectionListener bl = findListener(w1, w2);
		if (bl != null) {
			bl.detachListener();
			selectionListener.remove(bl);
		}
	}

	private BindSelectionListener findListener(Widget w1, Widget w2) {
		for (BindSelectionListener bl : selectionListener) {
			if (bl.source == w1 && bl.target == w2) {
				return bl;
			}
		}
		return null;
	}

}
