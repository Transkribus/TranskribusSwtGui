package eu.transkribus.swt_gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.graphics.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt_canvas.canvas.CanvasSettings;
import eu.transkribus.swt_gui.mainwidget.TrpSettings;
import eu.transkribus.util.APropertyChangeSupport;
import eu.transkribus.util.Utils;

public class TrpConfig {
	private final static Logger logger = LoggerFactory.getLogger(TrpConfig.class);
	
	public final static String CONFIG_FILENAME="config.properties";
	public final static File CONFIG_FILE=new File(CONFIG_FILENAME);
	
	public final static String TIPS_FILE="/tips.properties";
	public static PropertiesConfiguration config = null;
	
	public final static String PROFILES_FOLDERNAME = "profiles";
	public final static File PROFILES_FOLDER = new File(PROFILES_FOLDERNAME);
	
	public final static String PROFILE_NAME_REGEX = "[a-zA-Z0-9_]+";
	public final static String PROFILE_SUFFIX = ".properties";
	
	public final static String DEFAULT_PROFILE_NAME = "default";
	public final static String SEGMENTATION_PROFILE_NAME = "Segmentation";
	public final static String TRANSCRIPTION_PROFILE_NAME = "Transcription";
	
	public final static List<String> PREDEFINED_PROFILES = new ArrayList<String>();
	
	public static Properties tips = new Properties();
	
//	static List<APropertyChangeSupport> beans = new ArrayList<APropertyChangeSupport>();
	
	static Map<APropertyChangeSupport, PropertyChangeSaveListener> beans = new HashMap<APropertyChangeSupport, PropertyChangeSaveListener>();
	
	static TrpSettings trpSettings = new TrpSettings();
	static CanvasSettings canvasSettings = new CanvasSettings();
	
	public static class PropertyChangeSaveListener implements PropertyChangeListener {
		APropertyChangeSupport bean;
		boolean enabled;
		
		public PropertyChangeSaveListener(APropertyChangeSupport bean, boolean enabled) {
			this.bean = bean;
			this.enabled = enabled;
		}
		
		public boolean isEnabled() { return enabled; }
		public void setEnabled(boolean enabled) { this.enabled = enabled; }
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (isEnabled() && bean.isSaveProperty(evt.getPropertyName())) {
				logger.debug("saving config file on TrpSettings change of property: "+evt.getPropertyName());
				TrpConfig.save(evt.getPropertyName());
			}
		}
	}

	static {
		init();
		PREDEFINED_PROFILES.add(DEFAULT_PROFILE_NAME);
		PREDEFINED_PROFILES.add(SEGMENTATION_PROFILE_NAME);
		PREDEFINED_PROFILES.add(TRANSCRIPTION_PROFILE_NAME);
	}
	
	public static void init() {
		if (config == null) {
			PropertiesConfiguration.setDefaultListDelimiter('\0'); // prevents [ ] brackets around complex attributes
			config = new PropertiesConfiguration();
			config.setFile(CONFIG_FILE);

			try {
				if (!CONFIG_FILE.exists()) // create empty file if it does not exist!
					config.save();
				
				config.load();
			} catch (ConfigurationException e1) {
				logger.error("Could not load configuration file "+CONFIG_FILENAME+": "+e1.getMessage());
				throw new RuntimeException("Could not load configuration file "+CONFIG_FILENAME+": "+e1.getMessage(), e1);
			}
		}
		
		if (!PROFILES_FOLDER.exists()) {
			if (!PROFILES_FOLDER.mkdirs()) {
				throw new RuntimeException("Could not create profiles folder: "+PROFILES_FOLDER.getAbsolutePath());
			}
			logger.info("Created profiles folder: "+PROFILES_FOLDER.getAbsolutePath());
		}
		
		loadTipsOfTheDay();
	}
	
	private static void loadTipsOfTheDay() {
		try {
			InputStream istream = TrpConfig.class.getResourceAsStream(TIPS_FILE);
			if (istream != null)
				tips.load(istream);
		} catch (IOException e) {
			logger.error("Could not load tips file "+TIPS_FILE+": "+e.getMessage(), e);
		}
	}
	
	public static Properties getTipsOfTheDay() { return tips; }
	
	public static void registerBean(APropertyChangeSupport bean, boolean autoSave) {
		PropertyChangeSaveListener l = new PropertyChangeSaveListener(bean, autoSave);
		bean.addPropertyChangeListener(l);
		beans.put(bean, l);
			
		l.setEnabled(false);
		initBean(bean);
		l.setEnabled(autoSave);
		
		if (bean instanceof TrpSettings)
			trpSettings = (TrpSettings) bean;
		else if (bean instanceof CanvasSettings)
			canvasSettings = (CanvasSettings) bean;
	}
	
	public static Set<APropertyChangeSupport> getRegisteredBeans() {
		return beans.keySet();
	}
	
	public static void setAutoSaveForBean(APropertyChangeSupport bean, boolean autoSave) {
		PropertyChangeSaveListener l = beans.get(bean);
		if (l != null) {
			l.setEnabled(autoSave);
		}
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

		logger.debug("updating config from bean: "+bean.getClass().getName()+ ", adding those props: "+CoreUtils.toListString(addThoseL));
		
		for (PropertyDescriptor pd :  PropertyUtils.getPropertyDescriptors(bean)) {
			Object value = null;
			try {
//				logger.debug("property name: "+pd.getName());
				value = PropertyUtils.getProperty(bean, pd.getName());
				String strVal = getValue(value);
				if (config.getProperty(pd.getName())!=null) {
					logger.trace("updating property "+pd.getName()+" to "+strVal);
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
	
//	private static boolean propertyExists (Object bean, String property) {
//	    return PropertyUtils.isReadable(bean, property) && 
//	           PropertyUtils.isWriteable(bean, property); 
//	}
	
	private static void updateRegisteredBeansFromConfig() {
		// FIXME: de-register property change listener on beans
		
//		boolean detachListener=false;
		
		for (APropertyChangeSupport bean : getRegisteredBeans()) {
			// disable autosave if enabled:
			PropertyChangeSaveListener l = beans.get(bean);
			boolean wasAutosave = l!=null && l.isEnabled(); 
			if (wasAutosave)
				l.setEnabled(false);
			
			Utils.setBeanProperties(bean, config);
			
			// re-enable autosave:
			if (wasAutosave)
				l.setEnabled(true);
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
	
//	public static void save(APropertyChangeSupport bean, String... forceAddThisProperties) {
//		updateConfigFromBean(bean, forceAddThisProperties);
//		
//		config.save();
//	}
	
	
	
	
	public static void save(String... forceAddThisProperties) {
		try {
			for (APropertyChangeSupport bean : getRegisteredBeans()) {
				updateConfigFromBean(bean, forceAddThisProperties);
			}
			
			config.save();
			logger.debug("saved config file to: "+config.getFile().getAbsolutePath());
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
	
	// stuff for profiles:
	
	public static List<String> getAvailableProfiles() {
		List<String> profiles = new ArrayList<>();
		for (String fn : PROFILES_FOLDER.list(new FilenameFilter() {
			@Override public boolean accept(File dir, String name) {
				return name.endsWith(PROFILE_SUFFIX);
			}
		})) {
			profiles.add(StringUtils.removeEnd(fn, PROFILE_SUFFIX));			
		}
		
		Collections.sort(profiles);
		
		return profiles;
	}
	
	public static List<String> getPredefinedProfiles() {
		return PREDEFINED_PROFILES;
		
//		List<String> profiles = new ArrayList<>();
//		for (String pn : getAvailableProfiles()) {
//			if (isPredefinedProfile(pn))
//				profiles.add(pn);
//		}
//		
//		return profiles;
	}
	
	public static List<String> getCustomProfiles() {
		List<String> profiles = new ArrayList<>();
		for (String pn : getAvailableProfiles()) {
			if (!isPredefinedProfile(pn))
				profiles.add(pn);
		}
		
		return profiles;
	}
	
	public static File getProfileFile(String name) throws IOException {
		File pf = new File(PROFILES_FOLDER+"/"+name+PROFILE_SUFFIX);
		if (!pf.exists())
			throw new IOException("Profile does not exist: "+name);
		
		return pf;
	}
	
	public static File saveProfile(String name, boolean overrideExisting) throws IOException, FileExistsException, ConfigurationException {
		if (StringUtils.isEmpty(name))
			throw new IOException("No name given!");
		
		if (!name.matches(PROFILE_NAME_REGEX))
			throw new IOException("Invalid profile name "+name+" - only alphanumeric characters and underscores are allowed!");
		
		if (isPredefinedProfile(name))
			throw new IOException("Cannot ovverride a predefined profile!");
		
		if (getAvailableProfiles().contains(name) && !overrideExisting) {
			throw new FileExistsException("Profile "+name+" already exists!");
		}
		
		File profileFile = new File(PROFILES_FOLDER+"/"+name+PROFILE_SUFFIX);
		logger.info("storing new profile: "+profileFile.getAbsolutePath());
		
		config.save(profileFile);
		return profileFile;
	}
	
	public static boolean isPredefinedProfile(String name) {
		return PREDEFINED_PROFILES.contains(name);
	}
	
	public static void loadProfile(String name) throws IOException, ConfigurationException {
		File pf = getProfileFile(name);
		
		FileUtils.copyFile(pf, CONFIG_FILE);
		
		config.refresh();
		updateRegisteredBeansFromConfig();
		logger.debug("loaded profileee: "+pf);
	}
	
	public static void loadDefaultProfile() throws IOException, ConfigurationException {
		loadProfile(DEFAULT_PROFILE_NAME);
	}




	
	
}
