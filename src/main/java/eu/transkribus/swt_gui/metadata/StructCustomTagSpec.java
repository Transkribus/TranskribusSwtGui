package eu.transkribus.swt_gui.metadata;

import org.eclipse.swt.graphics.RGB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.JsonAdapter;

import eu.transkribus.core.model.beans.customtags.StructureTag;
import eu.transkribus.swt.util.Colors;

@JsonAdapter(StructCustomTagSpecAdapter.class)
public class StructCustomTagSpec extends CustomTagSpec {
	private static final Logger logger = LoggerFactory.getLogger(StructCustomTagSpec.class);
	
	RGB rgb;
	public static String RGB_PROPERTY="rgb";
	public static RGB DEFAULT_COLOR = new RGB(255, 255, 255);
	
//	StructureTag customTag;

	public StructCustomTagSpec(StructureTag customTag) {
		super(customTag);
	}
	
	public StructCustomTagSpec(StructureTag customTag, String colorHexCode) {
		super(customTag);
		
		try {
			setRGB(colorHexCode);
		}
		catch (NumberFormatException e) {
			logger.warn("Could not parse color '"+colorHexCode+"' for tag: "+customTag+" - "+e.getMessage());
		}
	}
	
	public RGB getRGB() {
		return rgb==null ? DEFAULT_COLOR : rgb;
	}

	public void setRGB(RGB rgb) {
		RGB old = this.rgb;
		this.rgb = rgb;
		firePropertyChange(RGB_PROPERTY, old, this.rgb);
	}
	
	public void setRGB(String colorHexCode) throws NumberFormatException  {
		setRGB(Colors.toRGB(colorHexCode));
	}
	
	public StructureTag getCustomTag() {
		return (StructureTag) customTag;
	}

	@Override
	public String toString() {
		return "StructCustomTagSpec [rgb=" + rgb + ", customTag=" + customTag + ", shortCut=" + shortCut + "]";
	}
	
}
