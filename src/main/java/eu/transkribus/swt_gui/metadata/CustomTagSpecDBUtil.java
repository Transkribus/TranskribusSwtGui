package eu.transkribus.swt_gui.metadata;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagAttribute;
import net.sf.json.JSONArray;


public class CustomTagSpecDBUtil {
	private static final Logger logger = LoggerFactory.getLogger(CustomTagSpecDBUtil.class);
	
	public static JsonArray writeCustomTagSpecsToJson(List<CustomTagSpec> customTagSpecs) {
		
		logger.debug("start json builder ");
		//JsonBuilderFactory factory = Json.createBuilderFactory(null);
		JsonArrayBuilder tagArr = Json.createArrayBuilder();

		for (CustomTagSpec cust : customTagSpecs){
			CustomTag ct = cust.getCustomTag();
			
			JsonObjectBuilder builder = Json.createObjectBuilder();
			
			builder.add("name", ct.getTagName());
	        
	        logger.trace("tagname to write: " + ct.getTagName());

	        JsonArrayBuilder arrb = Json.createArrayBuilder();

	        for (CustomTagAttribute cta : ct.getAttributes()){
	        	//logger.debug("attribute name " + cta.getName());
	        	arrb.add(cta.getName());
	        }

	        builder.add("attributes", arrb);
	        
			if (cust.getColor() != null){
				builder.add("color", cust.getColor());
			}
			
			if (cust.getShortCut() != null) {
				builder.add("shortCut", cust.getShortCut());	
			}
			
			//JsonObject jo = builder.build();
			//tagArr = Json.createArrayBuilder().build();
			tagArr.add(builder);

	        //logger.debug("write json: " + jo);         
			
		}

		JsonArray tmp = tagArr.build();
        logger.debug("json string " + tmp);
        return tmp;
		//return GsonUtil.toJson(customTagSpecs);
	}
	
	public static List<String> readCustomTagSpecsFromJsonString(String jsonStr) {
		
	    final JsonParser parser = Json.createParser(new StringReader(jsonStr));
	    
	    List<String> tagnames = new ArrayList<String>();
	    
        while (parser.hasNext()) {
            JsonParser.Event event = parser.next();
            switch (event) {
                case KEY_NAME:
                    if (parser.getString().equalsIgnoreCase("name")) {
                        event = parser.next();
                        if (event == Event.VALUE_STRING || event == Event.VALUE_NUMBER) {
                        	logger.debug("tag names " + parser.getString());
                        	tagnames.add(parser.getString());
                        }
                    }
                    break;
			default:
				break;
            }
        }
        
        parser.close();
		
        return tagnames;
			    
		
		// Parse back

//	    String key = null;
//	    String value = null;
//	    while (parser.hasNext()) {
//	         final Event event = parser.next();
//	         switch (event) {
//	            case KEY_NAME:
//	                 key = parser.getString();
//	                 if (key.equals("name")){
//	                	 
//	                 }
//	                 logger.debug("key is tagname? " + key);
//	                 break;
//	            case VALUE_STRING:
//	                 String string = parser.getString();
//	                 logger.debug("value string " + string);
//	                 break;
//	            case VALUE_NUMBER:
//	                BigDecimal number = parser.getBigDecimal();
//	                logger.debug("value number " + number);
//	                break;
//	            case VALUE_TRUE:
//	            	logger.debug("value true ");
//	                break;
//	            case VALUE_FALSE:
//	            	logger.debug("value false ");
//	                break;
//		        case VALUE_NULL:
//		            break;
//		        }
//	        }

	}
	
	public static JsonArray getCollectionTagSpecsAsJsonString(List<CustomTagSpec> tagSpecs) {
		try {
			return writeCustomTagSpecsToJson(tagSpecs);
		}
		catch (Exception e) {
			logger.error("Could not write tag specs!");
		}
		return null;
	}
	
}
