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
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.transcription.ATranscriptionWidget;

public class TranscriptionTaggingWidget extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(TranscriptionTaggingWidget.class);
	
	CTabFolder tabFolder;
	
	CTabFolder tagsTf;
	CTabItem tagsItem;
	TagDefsWidget tagDefsWidget;
	
	CTabFolder propsTf;
	CTabItem propsItem;
	TagPropertyEditor tagPropEditor;
	
	ATranscriptionWidget tWidget;

	public TranscriptionTaggingWidget(Composite parent, int style, ATranscriptionWidget tWidget) {
		super(parent, style);
		this.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		
		this.tWidget = tWidget;
		
		tabFolder = createTabFolder(this);
		
		tagsTf = createTabFolder(tabFolder);
		tagsItem = createCTabItem(tabFolder, tagsTf, "Tags", null);
		
		tagDefsWidget = new TagDefsWidget(tabFolder, 0, false);
		tagDefsWidget.setLayoutData(new GridData(GridData.FILL_BOTH));
		tagsItem.setControl(tagDefsWidget);
				
		propsTf = createTabFolder(tabFolder);
		propsItem = createCTabItem(tabFolder, propsTf, "Properties", null);
		
		tagPropEditor = new TagPropertyEditor(tabFolder, tWidget, false);
		tagPropEditor.setLayoutData(new GridData(GridData.FILL_BOTH));
		propsItem.setControl(tagPropEditor);
		
		SWTUtil.onSelectionEvent(tabFolder, e -> {
			if (isTagPropertyEditorSelected()) {
				tagPropEditor.findAndSetNextTag();
			}
		});
		
		tWidget.addListener(SWT.DefaultSelection, e -> {
			if (isTagPropertyEditorSelected() && tWidget.isTagEditorVisible()) {
				List<CustomTag> tags = tWidget.getCustomTagsForCurrentOffset();
				if (tags.isEmpty()) {
					return;
				}
				
				tagPropEditor.setCustomTag(tags.get(0));
			}
		});
		
		tabFolder.setSelection(tagsItem);
		
		tagDefsWidget.getTableViewer().getTable().getColumn(0).setWidth(100);
	}
	
	public TagDefsWidget getTagDefsWidget() {
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
	
	
	
	
	

}
