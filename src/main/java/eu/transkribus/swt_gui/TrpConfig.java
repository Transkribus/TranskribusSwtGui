package eu.transkribus.swt_gui;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.dea.swt.canvas.CanvasSettings;
import org.dea.util.APropertyChangeSupport;
import org.dea.util.Utils;
import org.eclipse.swt.graphics.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_gui.mainwidget.TrpSettings;

public class TrpConfig {
	private final static Logger logger = LoggerFactory.getLogger(TrpConfig.class);
	
	public final static String CONFIG_FILE="config.properties";
	public final static String TIPS_FILE="/tips.properties";
	public static PropertiesConfiguration config = null;
	
	public static Properties tips = new Properties();
	
	static List<APropertyChangeSupport> beans = new ArrayList<APropertyChangeSupport>();
	static TrpSettings trpSettings = new TrpSettings();
	static CanvasSettings canvasSettings = new CanvasSettings();

	static {
		init();
	}
	
	public static void init() {
		if (config == null) {
			try {
				PropertiesConfiguration.setDefaultListDelimiter('\0'); // prevents [ ] brackets around complex attributes
				config = new PropertiesConfiguration();
				config.setFileName(CONFIG_FILE);
				config.load();
			} catch (ConfigurationException e1) {
				logger.error("Could not load configuration file "+CONFIG_FILE+": "+e1.getMessage(), e1);
			}
			
			try {			
				InputStream istream = TrpConfig.class.getResourceAsStream(TIPS_FILE);
				if (istream != null)
					tips.load(istream);
			} catch (IOException e) {
				logger.error("Could not load tips file "+TIPS_FILE+": "+e.getMessage(), e);
			}
		}
	}
	
	public static Properties getTipsOfTheDay() { return tips; }
	
	public static void registerBean(APropertyChangeSupport bean) {
		beans.add(bean);
		
		initBean(bean);
		
		if (bean instanceof TrpSettings)
			trpSettings = (TrpSettings) bean;
		else if (bean instanceof CanvasSettings)
			canvasSettings = (CanvasSettings) bean;
	}
	
	private static void initBean(APropertyChangeSupport bean) {
//		if (props != null) {
//			try {
//				bean.init(props);
//			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
//				logger.error(e.getMessage(), e);
//			}
//		}
		
		if (config != null) {
			try {
				Utils.setBeanProperties(bean, config);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	private static void updateConfigFromBean(APropertyChangeSupport bean, String... forceAddThisProperties) {
		List<String> addThoseL = Arrays.asList(forceAddThisProperties);
		
		for (PropertyDescriptor pd :  PropertyUtils.getPropertyDescriptors(bean)) {
			Object value = null;
			try {
//				logger.debug("property name: "+pd.getName());
				value = PropertyUtils.getProperty(bean, pd.getName());
				String strVal = getValue(value);
				if (config.getProperty(pd.getName())!=null) {
					logger.debug("updating property "+pd.getName()+" to "+strVal);
					config.setProperty(pd.getName(), strVal);
				} else if (addThoseL.contains(pd.getName())) {
					logger.debug("adding new property "+pd.getName()+" to "+strVal);
					config.addProperty(pd.getName(), strVal);
				}
			} catch (Exception e) {
				logger.warn("Could not set property "+pd.getName()+" to "+value+", error: "+e.getMessage());
			}
		}
	}
	
	public static String getValue(Object value) {
//		logger.debug("getting value of: "+value+" class = "+value.getClass().getSimpleName());
		
		if (value.getClass() ==  org.eclipse.swt.graphics.Color.class) {
//			strVal = ((Color)value).getRGB().toString();
			return Utils.toVecString(((Color)value).getRGB());
		}
		else if (value.getClass() == Locale.class) {
			return ((Locale) value).toString();
		}
		else {
			return String.valueOf(value);
		}
	}
	
	public static void save(String... forceAddThisProperties) {
		try {
			for (APropertyChangeSupport bean : beans) {
				updateConfigFromBean(bean, forceAddThisProperties);
			}
			
			config.save();
		} catch (ConfigurationException e) {
			logger.error("Could not save config file: "+e.getMessage(), e);
		}
	}

	public static TrpSettings getTrpSettings() {
		return trpSettings;
	}

	public static CanvasSettings getCanvasSettings() {
		return canvasSettings;
	}


	
	
}
