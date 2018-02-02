package eu.transkribus.swt_gui.metadata;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.comments_widget.CommentsWidget;
import eu.transkribus.swt_gui.transcription.ATranscriptionWidget;

public class TranscriptionTaggingWidget extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(TranscriptionTaggingWidget.class);
	
	CTabFolder tabFolder;
	
	CTabFolder tagsTf;
	CTabItem tagsItem;
	TagSpecsWidget tagDefsWidget;
	
	CTabFolder propsTf;
	CTabItem propsItem;
	TagPropertyEditor tagPropEditor;
	
	CTabFolder commentsTf;
	CTabItem commentsItem;
	CommentsWidget commentsWidget;	
	
//	ATranscriptionWidget tWidget;
	
	public TranscriptionTaggingWidget(Composite parent, int style) {
		super(parent, style);
		this.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		
//		this.tWidget = tWidget;
		
		tabFolder = createTabFolder(this);
		
		tagsTf = createTabFolder(tabFolder);
		tagsItem = createCTabItem(tabFolder, tagsTf, "Tags", null);
		tagsItem.setFont(Fonts.createBoldFont(tagsItem.getFont()));
		
		tagDefsWidget = new TagSpecsWidget(tabFolder, 0, false);
		tagDefsWidget.setLayoutData(new GridData(GridData.FILL_BOTH));
		tagsItem.setControl(tagDefsWidget);
				
		propsTf = createTabFolder(tabFolder);
		propsItem = createCTabItem(tabFolder, propsTf, "Properties", null);
		propsItem.setFont(Fonts.createBoldFont(propsItem.getFont()));
		
		tagPropEditor = new TagPropertyEditor(tabFolder, /*tWidget,*/ false);
		tagPropEditor.setLayoutData(new GridData(GridData.FILL_BOTH));
		propsItem.setControl(tagPropEditor);
		
		if (false) {
		commentsTf = createTabFolder(tabFolder);
		commentsItem = createCTabItem(tabFolder, commentsTf, "Comments", null);
		
		commentsWidget = new CommentsWidget(tabFolder, 0);
		commentsWidget.setLayoutData(new GridData(GridData.FILL_BOTH));
		commentsItem.setControl(commentsWidget);
		}
		
		// listener:
		SWTUtil.onSelectionEvent(tabFolder, e -> {
			updateTabItemStyles();
			
			if (isTagPropertyEditorSelected()) {
				tagPropEditor.findAndSetNextTag();
			}
		});
		
		tabFolder.setSelection(tagsItem);
		tagDefsWidget.getTableViewer().getTable().getColumn(0).setWidth(150);
		
		updateTabItemStyles();
	}
	
	private void updateTabItemStyles() {
		SWTUtil.setBoldFontForSelectedCTabItem(tabFolder);
	}
		
	public void updateSelectedTag(ATranscriptionWidget tWidget) {
		if (isTagPropertyEditorSelected() /*&& tWidget.isTagEditorVisible()*/) {
			if (tagPropEditor.isSettingCustomTag()) { // if currently setting a custom tag in the property editor, ignore selection changed events from transcription widget!
				return;
			}
			
			List<CustomTag> tags = tWidget.getCustomTagsForCurrentOffset();
			if (tags.isEmpty()) {
				return;
			}
			
			tagPropEditor.setCustomTag(tags.get(0));
		}
	}
	
	public CTabFolder getTabFolder() {
		return tabFolder;
	}
	
	public TagSpecsWidget getTagDefsWidget() {
		return tagDefsWidget;
	}
	
	public TagPropertyEditor getTagPropertyEditor() {
		return tagPropEditor;
	}
	
	private CTabItem createCTabItem(CTabFolder tabFolder, Control control, String Text, List<CTabItem> list) {
		CTabItem ti = new CTabItem(tabFolder, SWT.NONE);
		ti.setText(Text);
		ti.setControl(control);

		if (list != null)
			list.add(ti);

		return ti;
	}
	
	private CTabFolder createTabFolder(Composite parent) {
		CTabFolder tf = new CTabFolder(parent, SWT.BORDER | SWT.FLAT);
		tf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tf.setBorderVisible(true);
		tf.setSelectionBackground(Colors.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

//		tabfolder.add(tf);

		return tf;
	}

	public boolean isTagPropertyEditorSelected() {
		return tabFolder.getSelection() == propsItem; 
	}

//	public void reloadComments() {
//		if (commentsWidget != null) {
//			commentsWidget.reloadComments();
//		}
//	}
	
	
	
	
	

}
