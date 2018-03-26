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
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.SWTUtil;

public class StructuralTaggingWidget extends Composite {

	private static final Logger logger = LoggerFactory.getLogger(StructuralTaggingWidget.class);
	
	CTabFolder tabFolder;
	
	CTabFolder tagsTf;
	CTabItem tagsItem;
	TagSpecsWidget structTagsWidget;
	
	CTabFolder propsTf;
	CTabItem propsItem;
	TagPropertyEditor tagPropEditor;
	
	public StructuralTaggingWidget(Composite parent, int style) {
		super(parent, style);
		this.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		
		tabFolder = createTabFolder(this);
		
		tagsTf = createTabFolder(tabFolder);
		tagsItem = createCTabItem(tabFolder, tagsTf, "Structure Types", null);
		tagsItem.setFont(Fonts.createBoldFont(tagsItem.getFont()));
		
		structTagsWidget = new TagSpecsWidget(tabFolder, 0, false);
		structTagsWidget.setLayoutData(new GridData(GridData.FILL_BOTH));
		tagsItem.setControl(structTagsWidget);
				
		propsTf = createTabFolder(tabFolder);
		propsItem = createCTabItem(tabFolder, propsTf, "Properties", null);
		propsItem.setFont(Fonts.createBoldFont(propsItem.getFont()));
		
		tagPropEditor = new TagPropertyEditor(tabFolder, /*tWidget,*/ false);
		tagPropEditor.setLayoutData(new GridData(GridData.FILL_BOTH));
		propsItem.setControl(tagPropEditor);
		
		// listener:
		SWTUtil.onSelectionEvent(tabFolder, e -> {
			updateTabItemStyles();
			
			if (isTagPropertyEditorSelected()) {
				tagPropEditor.findAndSetNextTag();
			}
		});
		
		tabFolder.setSelection(tagsItem);
		structTagsWidget.getTableViewer().getTable().getColumn(0).setWidth(150);
		
		updateTabItemStyles();
	}
	
	private void updateTabItemStyles() {
		SWTUtil.setBoldFontForSelectedCTabItem(tabFolder);
	}
		
	public void updateSelectedTag(List<CustomTag> tags) {
//		if (tags == null || tags.isEmpty()) {
//			return;
//		}
		
		if (true || isTagPropertyEditorSelected()) {
			if (tagPropEditor.isSettingCustomTag()) { // if currently setting a custom tag in the property editor, ignore selection changed events from transcription widget!
				return;
			}
			
			if (CoreUtils.isEmpty(tags)) {
				tagPropEditor.setCustomTag(null, false);
			} else {
				tagPropEditor.setCustomTag(tags.get(0), false);	
			}
		}
	}
	
	public CTabFolder getTabFolder() {
		return tabFolder;
	}
	
	public TagSpecsWidget getTagDefsWidget() {
		return structTagsWidget;
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
