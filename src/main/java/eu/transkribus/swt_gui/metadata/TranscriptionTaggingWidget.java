package eu.transkribus.swt_gui.metadata;

import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.SWTUtil;

public class TranscriptionTaggingWidget extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(TranscriptionTaggingWidget.class);
	
	CTabFolder tabFolder;
//	CTabItem tagsItem;
	TagSpecsWidget tagSpecsWidget;
	
//	CTabItem propsItem;
	TagPropertyEditor tagPropEditor;
	
//	SashForm sf;
	
	public TranscriptionTaggingWidget(Composite parent, int style) {
		super(parent, style);
		this.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		
//		this.tWidget = tWidget;
		
		tabFolder = createTabFolder(this);
		CTabItem tagsItem = createCTabItem(tabFolder, null, "Tags", null);
		tagsItem.setFont(Fonts.createBoldFont(tagsItem.getFont()));
		
		SashForm sf = new SashForm(tabFolder, SWT.VERTICAL);
		sf.setLayoutData(new GridData(GridData.FILL_BOTH));
//		sf.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		sf.setLayout(new GridLayout(1, false));
		
		tagsItem.setControl(sf);
		
		tagSpecsWidget = new TagSpecsWidget(sf, 0, false);
		tagSpecsWidget.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		tagPropEditor = new TagPropertyEditor(sf, /*tWidget,*/ false);
		tagPropEditor.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		sf.setWeights(new int[] { 55, 45 } );
		
		// listener:
		SWTUtil.onSelectionEvent(tabFolder, e -> {
			updateTabItemStyles();
			
//			if (isTagPropertyEditorSelected()) {
//				tagPropEditor.findAndSetNextTag();
//			}
		});
		
		tabFolder.setSelection(tagsItem);
		tagSpecsWidget.getTableViewer().getTable().getColumn(0).setWidth(150);
		
		updateTabItemStyles();
	}
	
	private void updateTabItemStyles() {
		SWTUtil.setBoldFontForSelectedCTabItem(tabFolder);
	}
		
	public void updateSelectedTag(List<CustomTag> tags) {
//		if (tags == null || tags.isEmpty()) {
//			return;
//		}
		
		getTagSpecsWidget().getTableViewer().getTable().deselectAll();
		
		if (true /*|| isTagPropertyEditorSelected()*/) {
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
	
	public TagSpecsWidget getTagSpecsWidget() {
		return tagSpecsWidget;
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

		return tf;
	}

//	public boolean isTagPropertyEditorSelected() {
//		return tabFolder.getSelection() == propsItem; 
//	}

//	public void reloadComments() {
//		if (commentsWidget != null) {
//			commentsWidget.reloadComments();
//		}
//	}
	
	
	
	
	

}
