package eu.transkribus.swt_gui.metadata;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

import eu.transkribus.core.util.GsonUtil;
import eu.transkribus.swt_gui.TrpConfig;

public class CustomTagDefUtil {
	private static final Logger logger = LoggerFactory.getLogger(CustomTagDefUtil.class);
	
	public static String writeCustomTagDefsToJsonString(List<CustomTagDef> customTagDefs) {
		return GsonUtil.toJson(customTagDefs);
	}
	
	public static List<CustomTagDef> readCustomTagDefsFromJsonString(String jsonStr) {
		return GsonUtil.fromJson(jsonStr, new TypeToken<List<CustomTagDef>>(){}.getType());
	}
	
	public static String writeSingleCustomTagDefToJsonString(CustomTagDef customTagDefs) {
		return GsonUtil.toJson(customTagDefs);
	}
	
	public static CustomTagDef readSingleCustomTagDefFromJsonString(String jsonStr) {
		return GsonUtil.fromJson(jsonStr, CustomTagDef.class);
	}
	
	public static List<CustomTagDef> readCustomTagDefsFromSettings() {
		try {
			String tagDefStr = TrpConfig.getTrpSettings().getTagDefs();
			logger.debug("tagDefStr: "+tagDefStr);
			
			if (StringUtils.isEmpty(tagDefStr)) {
				logger.debug("no tag definitions found - returning empty array!");
				return new ArrayList<>();
			}
	
			List<CustomTagDef> tagDefs = readCustomTagDefsFromJsonString(tagDefStr);
			logger.debug("read "+tagDefs.size()+" tag definitions");
			return tagDefs;
		}
		catch (Exception e) {
			logger.error("Could not read tag def string: "+e.getMessage(), e);
			return new ArrayList<>();
		}
	}
	
	public static void writeCustomTagDefsToSettings(List<CustomTagDef> tagDefs) {
		try {
			String tagDefString = writeCustomTagDefsToJsonString(tagDefs);
			logger.debug("writing tagDefString: "+tagDefString);
			TrpConfig.getTrpSettings().setTagDefs(tagDefString);
		}
		catch (Exception e) {
			logger.error("Could not write tag defs!");
		}
	}
}
