package eu.transkribus.swt_gui.metadata;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

import eu.transkribus.core.util.GsonUtil;
import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class CustomTagSpecUtil {
	private static final Logger logger = LoggerFactory.getLogger(CustomTagSpecUtil.class);
	
	public static String writeCustomTagSpecsToJsonString(List<CustomTagSpec> customTagSpecs) {
		return GsonUtil.toJson(customTagSpecs);
	}
	
	public static List<CustomTagSpec> readCustomTagSpecsFromJsonString(String jsonStr) {
		return GsonUtil.fromJson(jsonStr, new TypeToken<List<CustomTagSpec>>(){}.getType());
	}
	
	public static String writeSingleCustomTagSpecToJsonString(CustomTagSpec customTagSpecs) {
		return GsonUtil.toJson(customTagSpecs);
	}
	
	public static CustomTagSpec readSingleCustomTagSpecFromJsonString(String jsonStr) {
		return GsonUtil.fromJson(jsonStr, CustomTagSpec.class);
	}
	
	public static List<CustomTagSpec> readCustomTagSpecsFromSettings() {
		try {
			String tagSpecStr = TrpConfig.getTrpSettings().getTagSpecs();
			logger.debug("tagSpecStr: "+tagSpecStr);
			
			if (StringUtils.isEmpty(tagSpecStr)) {
				logger.debug("no tag specifications found - returning empty array!");
				return new ArrayList<>();
			}
	
			List<CustomTagSpec> tagSpecs = readCustomTagSpecsFromJsonString(tagSpecStr);
			logger.debug("read "+tagSpecs.size()+" tag specifications");
			return tagSpecs;
		}
		catch (Exception e) {
			logger.error("Could not read tag spec string: "+e.getMessage(), e);
			return new ArrayList<>();
		}
	}
	
	public static void writeCustomTagSpecsToSettings(List<CustomTagSpec> tagSpecs) {
		try {
			String tagSpecString = writeCustomTagSpecsToJsonString(tagSpecs);
			logger.debug("writing tagSpecString: "+tagSpecString);
			TrpConfig.getTrpSettings().setTagSpecs(tagSpecString);
		}
		catch (Exception e) {
			logger.error("Could not write tag specs!");
		}
	}
	
	public static String getCollectionTagSpecsAsJsonString(List<CustomTagSpec> tagSpecs) {
		try {
			return writeCustomTagSpecsToJsonString(tagSpecs);
		}
		catch (Exception e) {
			logger.error("Could not write tag specs!");
		}
		return null;
	}
	
}
