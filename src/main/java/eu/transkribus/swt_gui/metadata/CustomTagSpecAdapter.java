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

/**
 * A gson adapter for reading / writing CustomTagSpec objects to / from json
 */
public class CustomTagSpecAdapter extends TypeAdapter<CustomTagSpec> {

	@Override
	public void write(JsonWriter writer, CustomTagSpec value) throws IOException {
		writer.beginObject();
				
		writer.name("customTag").value(value.getCustomTag().toString());

//		if (value.getRGB() != null) {
//			writer.name("rgb").value(Colors.toHex(value.getRGB()));
//		}
		
		if (value.getColor() != null){
			writer.name("color").value(value.getColor());
		}
        
        if (value.getShortCut() != null) {
        	writer.name("shortCut").value(value.getShortCut());	
        }
             
        writer.endObject();
	}

	@Override
	public CustomTagSpec read(JsonReader reader) throws IOException {
		reader.beginObject();
		
		CustomTag ct=null;
		RGB rgb=null;
		String color= null;
		String shortCut=null;
		
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
//		      case "rgb":
//		        String colorCode = reader.nextString();
//		        rgb = Colors.toRGB(colorCode);
//		        break;
		      case "color":
		    	color = reader.nextString();
		    	break;
		      case "shortCut":
		        shortCut = reader.nextString();
		        break; 

		      }
		}
		
		if (ct == null) {
			throw new IOException("No custom tag definition found!");
		}
		
		CustomTagSpec cDef = new CustomTagSpec(ct);
//		cDef.setRGB(rgb); 
		cDef.setShortCut(shortCut);
		cDef.setColor(color);
		
		reader.endObject();
		
		return cDef;
	}

}
