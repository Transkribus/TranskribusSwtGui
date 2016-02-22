package eu.transkribus.swt_gui.page_metadata;

import java.util.List;
import java.util.Map;

import eu.transkribus.core.model.beans.customtags.CustomTagAttribute;


public interface ITaggingWidgetListener {
	void addTagForSelection(String tagName, Map<String, Object> attributes);
	void addTagsForSelection(List<String> checkedTags);
	void deleteTagForSelection(String tagName);
	void deleteTagsOnSelection();
//	void tagsUpdated();
//	void deleteTag(CustomTagAndList ctal);
	
	void addAttributeOnCustomTag(String tn, CustomTagAttribute att);
	void deleteAttributeOnCustomTag(String tn, String attributeName);
	void createNewTag(String tagName);
	void removeTagDefinition(String tagName);
}
