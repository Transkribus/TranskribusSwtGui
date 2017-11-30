package eu.transkribus.swt_gui.metadata;

import org.eclipse.swt.graphics.RGB;

import com.google.gson.annotations.JsonAdapter;

import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.util.APropertyChangeSupport;

/**
 * A specific definition of a custom tag for usage in the UI.
 * Includes also additional information such as color, shortcut character etc.
 */
@JsonAdapter(CustomTagDefAdapter.class)
public class CustomTagDef extends APropertyChangeSupport {
	public static RGB DEFAULT_COLOR = new RGB(0, 0, 255);
	
	RGB rgb;
	public static String RGB_PROPERTY="rgb";
	
	CustomTag customTag;
	public static String CUSTOM_TAG_PROPERTY="customTag";
	
	String shortCut;
	public static String SHORT_CUT_PROPERTY="shortCut";
	
	public static String[] VALID_SHORTCUTS = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
	
	public CustomTagDef(CustomTag customTag) {
		this.customTag = customTag;
	}

	public RGB getRGB() {
		return rgb;
	}

	public void setRGB(RGB rgb) {
		RGB old = this.rgb;
		this.rgb = rgb;
		firePropertyChange(RGB_PROPERTY, old, this.rgb);
	}

	public CustomTag getCustomTag() {
		return customTag;
	}

	public void setCustomTag(CustomTag customTag) {
		CustomTag old = this.customTag;
		this.customTag = customTag;
		firePropertyChange(CUSTOM_TAG_PROPERTY, old, this.customTag);
	}

	public String getShortCut() {
		return shortCut;
	}

	public void setShortCut(String shortCut) {
		if (shortCut==null || isValidShortCut(shortCut)) {
			String old = this.shortCut;
			this.shortCut = shortCut;
			firePropertyChange(SHORT_CUT_PROPERTY, old, this.shortCut);
		}
	}
	
	public static boolean isValidShortCut(String sc) {
		for (String validC : VALID_SHORTCUTS) {
			if (validC.equals(sc)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "CustomTagDef [rgb=" + rgb + ", customTag=" + customTag + ", shortCut=" + shortCut + "]";
	}	

}
