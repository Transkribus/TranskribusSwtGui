package eu.transkribus.swt_gui.metadata;

import java.util.List;
import java.util.Map;

import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagAttribute;


public interface ITaggingWidgetListener {
	void addTagForSelection(String tagName, Map<String, Object> attributes);
	void addTagsForSelection(List<String> checkedTags);
	
	/**
	 * @deprecated Ambiguous -> for the current selection (i.e. the current cursor position), two tags with the same tagName can exist!
	 * @param tagName
	 */
	void deleteTagForSelection(String tagName);
	
	void deleteTagsForCurrentSelection();
//	void tagsUpdated();
//	void deleteTag(CustomTagAndList ctal);
	
	void addAttributeOnCustomTag(String tn, CustomTagAttribute att);
	void deleteAttributeOnCustomTag(String tn, String attributeName);
	void createNewTag(String tagName);
	void removeTagDefinition(String tagName);
	void deleteTag(CustomTag tag);
}
