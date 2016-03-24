package eu.transkribus.swt_gui.search.text_and_tags;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory.TagRegistryChangeEvent;
import eu.transkribus.swt_canvas.util.CustomTagPropertyTable;
import eu.transkribus.swt_canvas.util.DialogUtil;
import eu.transkribus.swt_canvas.util.LabeledCombo;

public class AddTagWidget extends Composite /*implements Observer*/ {
	private final static Logger logger = LoggerFactory.getLogger(AddTagWidget.class);
	
	LabeledCombo tagsCombo;
	CustomTagPropertyTable ctPropTable;
	Button tagBtn;

	public AddTagWidget(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		
		tagsCombo = new LabeledCombo(this, "Tag: ");
		
		tagsCombo.combo.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				updatePropertiesForSelectedTag();
			}
		});
		
		initCustomPropertyTable(this);
		
		tagBtn = new Button(this, SWT.PUSH);
		tagBtn.setText("Tag selected");
		tagBtn.setToolTipText("Tags the selected entries on the left using the tag and its properties above");
		tagBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		updateAvailableTags();
		
//		CustomTagFactory.registryObserver.addObserver(this);
	}
	
	private void initCustomPropertyTable(Composite parent) {
		ctPropTable = new CustomTagPropertyTable(parent, 0);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2);
//		gd.heightHint = 200;
		ctPropTable.setLayoutData(gd);
	}
	
	public void updateAvailableTags() {
		String before = getSelectedTag();
		tagsCombo.combo.removeAll();
		
		int i=0;
		int selectIndex=0;
		for (String tn : CustomTagFactory.getRegisteredTagNamesSorted()) {
			tagsCombo.combo.add(tn);
			if (tn.equals(before))
				selectIndex = i;
			
			++i;
		}
		if (tagsCombo.combo.getItemCount() > 0)
			tagsCombo.combo.select(selectIndex);
		
		updatePropertiesForSelectedTag();
	}
	
	String getSelectedTag() {
		if (tagsCombo.combo.getSelectionIndex() != -1) {
			return tagsCombo.combo.getItem(tagsCombo.combo.getSelectionIndex());
		}
		return "";
	}
	
	public void updatePropertiesForSelectedTag() {
		String tn = getSelectedTag();
		logger.debug("selected tag name: "+tn);
		if (tn == null) {
			ctPropTable.setInput(null, null);
			ctPropTable.update();
			return;
		}
				
		try {
			CustomTag tag = CustomTagFactory.getTagObjectFromRegistry(tn);
			if (tag == null)
				throw new IOException("could not retrieve tag from registry: "+tn+" - should not happen here!");
			
			logger.debug("tag from object registry: "+tag);
			logger.debug("tag atts: "+tag.getAttributeNames());
			
			CustomTag protoTag = tag.copy();
			logger.debug("protoTag copy: "+protoTag);
			logger.debug("protoTag atts: "+protoTag.getAttributeNames());
						
			ctPropTable.setInput(protoTag, null);
		} catch (Exception e) {
			DialogUtil.showErrorMessageBox(getShell(), "Error updating properties", e.getMessage());
			logger.error(e.getMessage(), e);
			return;
		}
	}
	
	public Map<String, Object> getCurrentAttributes() {
		Map<String, Object> props = new HashMap<>();
		CustomTag pt = ctPropTable.getPrototypeTag();
		if (pt != null) {
			return pt.getAttributeNamesValuesMap();
		}
	
		return props;
	}
	
	public Button getTagBtn() {
		return tagBtn;
	}
		
//	@Override public void update(Observable o, Object arg) {
//		if (arg instanceof TagRegistryChangeEvent) {
//			logger.debug("updated tag registry "+arg);
//
//			TagRegistryChangeEvent e = (TagRegistryChangeEvent) arg;
//			logger.debug("updating available tags, e = "+arg);
//			
//			updateAvailableTags();
//		}
//	}
	
	

}
