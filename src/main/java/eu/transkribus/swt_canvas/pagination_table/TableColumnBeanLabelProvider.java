package eu.transkribus.swt_canvas.pagination_table;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;

public class TableColumnBeanLabelProvider extends ColumnLabelProvider {
	protected String colName;
	
	public TableColumnBeanLabelProvider(String colName) {
		this.colName = colName;
	}
	
    @Override public String getText(Object element) {
    	try {
			return BeanUtils.getSimpleProperty(element, colName);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			return "i am error";
		}
    }
}