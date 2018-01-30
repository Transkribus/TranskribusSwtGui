package eu.transkribus.swt_gui.metadata;

import java.util.List;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import eu.transkribus.core.model.beans.customtags.CustomTag;

/**
 * @deprecated
 * @author jkloe
 *
 */
class ClearTagsSelectionListener extends ATagActionSelectionListener {

	public ClearTagsSelectionListener(List<ITaggingWidgetListener> listener) {
		super(listener);
	}
	
	@Override public void performAction(ITaggingWidgetListener l) {
		l.deleteTagsForCurrentSelection();
	}
}

/**
 * @deprecated
 * @author jkloe
 *
 */
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

/**
 * @deprecated
 * @author jkloe
 *
 */
class AddTagSelectionListener extends ATagActionSelectionListener {
	String tagName;
	TaggingWidgetOld tw;

	public AddTagSelectionListener(List<ITaggingWidgetListener> listener, TaggingWidgetOld tw, String tagName) {
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
 * @deprecated
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
