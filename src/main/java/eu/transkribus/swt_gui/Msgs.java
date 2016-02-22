package eu.transkribus.swt_gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import eu.transkribus.core.i18n.UTF8Control;

public class Msgs {
	final static String LOCALES_BASENAME = "i18n/Messages";

	public final static Locale EN_LOCALE = new Locale("en", "US");
	public final static Locale DE_LOCALE = new Locale("de", "DE");
	
	public final static Locale DEFAULT_LOCALE = EN_LOCALE;
	
	public final static List<Locale> LOCALES = new ArrayList<>();
	
	static {
		LOCALES.add(EN_LOCALE);
		LOCALES.add(DE_LOCALE);
	}
	
	static ResourceBundle messages; //ResourceBundle.getBundle(LOCALES_BASENAME, TrpConfig.getTrpSettings().getLocale());
	static final UTF8Control utf8Control = new UTF8Control();
	
	
	static {
		Locale.setDefault(DEFAULT_LOCALE);
		
		setLocale(TrpConfig.getTrpSettings().getLocale());
//		setLocale(DE_LOCALE);
	}
	
	public static Locale getLocale() {
		
		return messages.getLocale();
	}
	
	public static void setLocale(Locale l) {
		messages = ResourceBundle.getBundle(LOCALES_BASENAME, l, utf8Control);
//		messages = ResourceBundle.getBundle(LOCALES_BASENAME, l);
	}
	
	/**
	 * Returns a message with the given key
	 */
	public static String get(String key) {
		return messages.getString(key);
	}

	/**
	 * Returns a message with the given key, returning the key itself if it was not found
	 */
	public static String get2(String key) {
		try {
			return get(key);
		} catch (Exception e) {
			return key;
		}
	}
	
	public static String get2(String key, String alternative) {
		try {
			return get(key);
		} catch (Exception e) {
			if (alternative != null)
				return alternative;
			else
				return key;
		}
	}
	
	public static void main(String[] args) {
		String key="documents";
		
		// default locale:
		System.out.println("current locale = "+Msgs.getLocale());
		System.out.println(Msgs.get(key));
		
		// set german locale:
		Msgs.setLocale(DE_LOCALE);

		System.out.println("current locale = "+Msgs.getLocale());
		System.out.println(Msgs.get(key));
		
		
		// set a locale that is not available - should fall back to default
		Msgs.setLocale(new Locale("pt", "BR"));
		
		System.out.println("current locale = "+Msgs.getLocale());
		System.out.println(Msgs.get(key));
		
	
	}

}


