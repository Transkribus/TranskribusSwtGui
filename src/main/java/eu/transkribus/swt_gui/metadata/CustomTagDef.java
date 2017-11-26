package eu.transkribus.swt_gui.metadata;

import org.eclipse.swt.graphics.RGB;

import eu.transkribus.core.model.beans.customtags.CustomTag;

/**
 * A specific definition of a custom tag for usage in the UI.
 * Includes also additional information such as color, shortcut character etc.
 */
public class CustomTagDef {
	public static RGB DEFAULT_COLOR = new RGB(0, 0, 255);
	
	RGB rgb;
	CustomTag customTag;
	Character shortCut;
	
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

	public Character getShortCut() {
		return shortCut;
	}

	public void setShortCut(Character shortCut) {
		this.shortCut = shortCut;
	}

	@Override
	public String toString() {
		return "CustomTagDef [rgb=" + rgb + ", customTag=" + customTag + ", shortCut=" + shortCut + "]";
	}
	
	

}
