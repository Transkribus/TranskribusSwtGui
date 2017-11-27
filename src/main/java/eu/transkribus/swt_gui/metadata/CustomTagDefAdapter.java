package eu.transkribus.swt_gui.metadata;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

import org.eclipse.swt.graphics.RGB;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagUtil;
import eu.transkribus.swt.util.Colors;

/**
 * A gson adapter for reading / writing CustomTagDef objects to / from json
 */
public class CustomTagDefAdapter extends TypeAdapter<CustomTagDef> {

	@Override
	public void write(JsonWriter writer, CustomTagDef value) throws IOException {
		writer.beginObject();
				
		writer.name("customTag").value(value.getCustomTag().toString());

		if (value.getRGB() != null) {
			writer.name("rgb").value(Colors.toHex(value.getRGB()));
		}
        
        if (value.getShortCut() != null) {
        	writer.name("shortCut").value(value.getShortCut());	
        }
             
        writer.endObject();
	}

	@Override
	public CustomTagDef read(JsonReader reader) throws IOException {
		reader.beginObject();
		
		CustomTag ct=null;
		RGB rgb=null;
		Character shortCut=null;
		
		while (reader.hasNext()) {
		      switch (reader.nextName()) {
		      case "customTag":
		        try {
					ct = CustomTagUtil.parseSingleCustomTag(reader.nextString());
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | ClassNotFoundException | NoSuchMethodException
						| ParseException e) {
					throw new IOException("Could not read tag definition: "+e.getMessage(), e);
				}
		        break;
		      case "rgb":
		        String colorCode = reader.nextString();
		        rgb = Colors.toRGB(colorCode);
		        break;
		      case "shortCut":
		        shortCut = new Character((char) Integer.parseInt(reader.nextString()));
		        break;
		      }
		}
		
		if (ct == null) {
			throw new IOException("No custom tag definition found!");
		}
		
		CustomTagDef cDef = new CustomTagDef(ct);
		cDef.setRGB(rgb); 
		cDef.setShortCut(shortCut);
		
		reader.endObject();
		
		return cDef;
	}

}
