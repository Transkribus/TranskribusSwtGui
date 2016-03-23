package eu.transkribus.swt_gui.page_metadata;

import java.util.List;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.customtags.CustomTag;


class ClearTagsSelectionListener extends ATagActionSelectionListener {

	public ClearTagsSelectionListener(List<ITaggingWidgetListener> listener) {
		super(listener);
	}
	
	@Override public void performAction(ITaggingWidgetListener l) {
		l.deleteTagsOnSelection();
	}
}

class DeleteTagSelectionListener extends ATagActionSelectionListener {
	CustomTag tag;
	public DeleteTagSelectionListener(List<ITaggingWidgetListener> listener, CustomTag tag) {
		super(listener);
		this.tag = tag;
	}

	@Override public void performAction(ITaggingWidgetListener l) {
		if (this.tag != null)
			l.deleteTag(tag);
	}
}

class AddTagSelectionListener extends ATagActionSelectionListener {
	String tagName;
	TaggingWidget tw;

	public AddTagSelectionListener(List<ITaggingWidgetListener> listener, TaggingWidget tw, String tagName) {
		super(listener);
		this.tw = tw;
		this.tagName = tagName;
	}

	@Override public void performAction(ITaggingWidgetListener l) {
		if (tagName != null) {
			if (tagName.equals(tw.getTagNameSelectedInTable()))
				l.addTagForSelection(tagName, tw.getCurrentAttributes());
			else
				l.addTagForSelection(tagName, null); // if tagName does not match currently selected in taggingwidget -> set no attributes!
		}
	}
}

/**
 * Delivers selection events to given ITaggingWidgetListener's according to a specified action type.
 */
abstract class ATagActionSelectionListener extends SelectionAdapter {
	List<ITaggingWidgetListener> listener;
	
	public ATagActionSelectionListener(List<ITaggingWidgetListener> listener) {
		this.listener = listener;
	}
	
	@Override public void widgetSelected(SelectionEvent e) {
		for (ITaggingWidgetListener l : listener) {
			performAction(l);
		}
	}
	
	public abstract void performAction(ITaggingWidgetListener l); 
}


///**
// * Delivers selection events to given ITaggingWidgetListener's according to a specified action type.
// */
//public class TagActionSelectionListener extends SelectionAdapter {
//	private final static Logger logger = LoggerFactory.getLogger(TagActionSelectionListener.class);
//	
//	List<ITaggingWidgetListener> listener;
//	TaggingActionType type;
//	String tagName=null;
////	CustomTagAndList ctl=null;
//	TaggingWidget tw;
//				
// 	public TagActionSelectionListener(TaggingWidget tw, List<ITaggingWidgetListener> listener, TaggingActionType type) {
// 		this.tw = tw;
// 		this.listener = listener;
// 		this.type = type;
//	}
// 	    	
//	public TagActionSelectionListener(TaggingWidget tw, List<ITaggingWidgetListener> listener, TaggingActionType type, String tagName) {
//		this(tw, listener, type);
//		this.tagName = tagName;
//	}
//	
////	public TagActionSelectionListener(List<ITaggingWidgetListener> listener, TaggingActionType type, CustomTagAndList ctl) {
////		this(listener, type);
////		this.ctl = ctl;
////	}
//
//	@Override public void widgetSelected(SelectionEvent e) {
//		// old:
////		Event se = new Event();		
////		if ( (type == TaggingActionType.ADD_TAG || type == TaggingActionType.DELETE_TAG) && tagName != null) {
////			se.text = tagName;
////		}
////		se.data = type;
////		notifyListeners(SWT.Selection, se);
//		
//		// new:
//		for (ITaggingWidgetListener l : listener) {
//			switch (type) {
//			
//			case ADD_TAG:
////				logger.debug("adding tag, tagName = "+tagName);
//				if (tagName != null) {
//					if (tagName.equals(tw.getTagNameSelectedInTable()))
//						l.addTagForSelection(tagName, tw.getCurrentAttributes());
//					else
//						l.addTagForSelection(tagName, null); // if tagName does not match currently selected in taggingwidget -> set no attributes!
//				}
//				break;
//			case DELETE_TAG:
////				if (ctl != null) {
////					l.deleteTag(ctl);
////					break;
////				}
//				if (tagName != null) {
//					l.deleteTagForSelection(tagName);
//					break;
//				}
//				break;
//			case CLEAR_TAGS:
//				l.deleteTagsOnSelection();
//				break;
////			case TAGS_UPDATED:
////				l.tagsUpdated();
////				break;
//			}
//			
//		}
//	}
//};
