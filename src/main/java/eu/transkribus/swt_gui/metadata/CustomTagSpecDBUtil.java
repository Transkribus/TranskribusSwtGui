package eu.transkribus.swt_gui.metadata;

import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagAttribute;
import eu.transkribus.core.util.GsonUtil;
import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

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
	        
	        logger.debug("tagname " + ct.getTagName());

	        JsonArrayBuilder arrb = Json.createArrayBuilder();

	        for (CustomTagAttribute cta : ct.getAttributes()){
	        	logger.debug("attribute name " + cta.getName());
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

        logger.debug("json string " + tagArr.build());
        return tagArr.build();
		//return GsonUtil.toJson(customTagSpecs);
	}
	
//	public static List<CustomTagSpecDB> readCustomTagSpecsFromJsonString(String jsonStr) {
//		return GsonUtil.fromJson(jsonStr, new TypeToken<List<CustomTagSpecDB>>(){}.getType());
//	}
	
	
}
