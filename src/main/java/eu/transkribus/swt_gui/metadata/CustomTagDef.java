package eu.transkribus.swt_gui.metadata;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.RGB;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;

import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagUtil;
import eu.transkribus.core.util.GsonUtil;

/**
 * A specific definition of a custom tag for usage in the UI.
 * Includes also additional information such as color, shortcut character etc.
 */
@JsonAdapter(CustomTagDefAdapter.class)
public class CustomTagDef {
	public static RGB DEFAULT_COLOR = new RGB(0, 0, 255);
	
	RGB rgb;
	CustomTag customTag;
	String shortCut;
	
	public static String[] VALID_SHORTCUTS = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
	
	public CustomTagDef(CustomTag customTag) {
		this.customTag = customTag;
	}

	public RGB getRGB() {
		return rgb;
	}

	public void setRGB(RGB rgb) {
		this.rgb = rgb;
	}

	public CustomTag getCustomTag() {
		return customTag;
	}

	public void setCustomTag(CustomTag customTag) {
		this.customTag = customTag;
	}

	public String getShortCut() {
		return shortCut;
	}

	public void setShortCut(String shortCut) {
		if (shortCut==null) {
			this.shortCut = null;
		}
		else if (isValidShortCut(shortCut)) {
			this.shortCut = shortCut;
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
