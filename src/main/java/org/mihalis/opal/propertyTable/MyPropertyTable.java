package org.mihalis.opal.propertyTable;

import java.lang.reflect.Field;
import java.util.List;

import javax.ws.rs.NotSupportedException;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.junit.Assert;
import org.mihalis.opal.propertyTable.PTProperty;
import org.mihalis.opal.propertyTable.PTWidget;
import org.mihalis.opal.propertyTable.PropertyTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyPropertyTable extends PropertyTable {
	private final static Logger logger = LoggerFactory.getLogger(MyPropertyTable.class);
	
	PTWidget myWidget;
//	List<PTProperty> myProperties;

	public MyPropertyTable(Composite parent, int style) {
		super(parent, style);
		viewAsFlatList(); // necessary!!
		initMyWidget();
//		initMyProperties();
	}
	
	public void setStyleOfView(int styleOfView) {
		Assert.assertTrue("styleOfView must be either 0 (flat list) or 1 (categories)", styleOfView==0 || styleOfView==1); 
		this.styleOfView = styleOfView;
	}
	
	public PTWidget getWidget() { 
		return myWidget;
	}
	
	public List<PTProperty> getPropertiesListPointer() {
//		return myProperties;
		return properties;
	}
	
	public Table getTable() {
		if (myWidget.getWidget() instanceof Table)
			return (Table) myWidget.getWidget();
		
		throw new NotSupportedException("This widget has no table - try getTree method!");
	}
	
	public Tree getTree() {
		if (myWidget.getWidget() instanceof Tree)
			return (Tree) myWidget.getWidget();
		
		throw new NotSupportedException("This widget has no tree - try getTable method!");
	}
	
	private void initMyWidget()  {
		try {
			logger.info("123INIT!!!");
			Field f = PropertyTable.class.getDeclaredField("widget"); //NoSuchFieldException
			logger.info("field: "+f);
			f.setAccessible(true);
			
			myWidget = (PTWidget) f.get(this);
			Assert.assertNotNull("myWidget is null!!", myWidget);
			logger.debug("inited myWidget: "+myWidget);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new NotSupportedException("Error initing my widget - should not happen here...", e);
		}
	}
	
//	private void initMyProperties() {
//		try {
//			Field f = getClass().getDeclaredField("properties"); //NoSuchFieldException
//			f.setAccessible(true);
//			myProperties = (List<PTProperty>) f.get(this);
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//			throw new NotSupportedException("Error initing my properties - should not happen here...");
//		}
//	}
	


}
