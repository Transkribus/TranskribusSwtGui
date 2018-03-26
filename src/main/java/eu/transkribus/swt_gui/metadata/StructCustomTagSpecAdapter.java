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
import eu.transkribus.core.model.beans.customtags.StructureTag;
import eu.transkribus.swt.util.Colors;

public class StructCustomTagSpecAdapter extends TypeAdapter<StructCustomTagSpec> {
	@Override
	public void write(JsonWriter writer, StructCustomTagSpec value) throws IOException {
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
	public StructCustomTagSpec read(JsonReader reader) throws IOException {
		reader.beginObject();
		
		StructureTag ct=null;
		RGB rgb=null;
		String shortCut=null;
		
		while (reader.hasNext()) {
		      switch (reader.nextName()) {
		      case "customTag":
		        try {
					CustomTag ct1 = CustomTagUtil.parseSingleCustomTag(reader.nextString());
					if (!(ct1 instanceof StructureTag)) {
						ct = (StructureTag) ct1;
					}
					else {
						throw new IOException("Not a struture tag: "+ct1.getCssStr());
					}
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | ClassNotFoundException | NoSuchMethodException
						| ParseException | IOException e) {
					throw new IOException("Could not read tag definition: "+e.getMessage(), e);
				}
		        break;
		      case "rgb":
		        String colorCode = reader.nextString();
		        rgb = Colors.toRGB(colorCode);
		        break;
		      case "shortCut":
		        shortCut = reader.nextString();
		        break;
		      }
		}
		
		if (ct == null) {
			throw new IOException("No custom tag definition found!");
		}
		
		StructCustomTagSpec cDef = new StructCustomTagSpec(ct);
		cDef.setRGB(rgb);
		cDef.setShortCut(shortCut);
		
		reader.endObject();
		
		return cDef;
	}
}
