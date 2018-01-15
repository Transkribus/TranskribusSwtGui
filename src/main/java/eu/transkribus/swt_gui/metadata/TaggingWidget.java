package eu.transkribus.swt_gui.metadata;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory;
import eu.transkribus.swt.util.Fonts;

public class TaggingWidget extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(TaggingWidget.class);
	
	SashForm verticalSf;
	SashForm horizontalSf;
	
	TagSpecsWidget tagDefsWidget;
	CustomTagPropertyTable propsTable;
	
	TagListWidget tagListWidget; 

//	public TaggingWidget(Composite parent, int style) {
//		super(parent, style);
//		this.setLayout(new FillLayout());
//
//		verticalSf = new SashForm(this, SWT.VERTICAL);
//		
//		initTagDefsWidget(verticalSf);
//		
//		horizontalSf = new SashForm(verticalSf, SWT.HORIZONTAL); 
//		
//		initTagListWidget(horizontalSf);
//		initPropsWidget(horizontalSf);
//		
//		verticalSf.setWeights(new int[] { 50, 50 } );
//		horizontalSf.setWeights(new int[] { 50, 50 } );
//	}
	
	public TaggingWidget(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new GridLayout(1, false));
		
		tagListWidget = new TagListWidget(this, 0);
		tagListWidget.setLayoutData(new GridData(GridData.FILL_BOTH));

//		verticalSf = new SashForm(this, SWT.VERTICAL);
//		
//		initTagDefsWidget(verticalSf);
//		
//		horizontalSf = new SashForm(verticalSf, SWT.HORIZONTAL); 
//		
//		initTagListWidget(horizontalSf);
//		initPropsWidget(horizontalSf);
//		
//		verticalSf.setWeights(new int[] { 50, 50 } );
//		horizontalSf.setWeights(new int[] { 50, 50 } );
	}
	
	private void initTagDefsWidget(Composite parent) {
		tagDefsWidget = new TagSpecsWidget(parent, 0, false);
		tagDefsWidget.setLayoutData(new GridData(GridData.FILL_BOTH));		
	}
	
	private void initPropsWidget(Composite parent) {
		Composite propsContainer = new Composite(parent, ExpandableComposite.COMPACT);
		propsContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		propsContainer.setLayout(new GridLayout(2, false));
		
		Label headerLbl = new Label(propsContainer, 0);
		headerLbl.setText("Properties");
		Fonts.setBoldFont(headerLbl);
		headerLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		propsTable = new CustomTagPropertyTable(propsContainer, 0, false);
		propsTable.getTableViewer().getTable().setHeaderVisible(false);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2);
//		gd.heightHint = 200;
		propsTable.setLayoutData(gd);
		
//		initPropertyTable();

		layout();
	}
	
	private void updatePropertiesForSelectedTag() {
		CustomTag selectedTag = tagListWidget.getSelected();
		if (selectedTag == null) {
			propsTable.setInput(null, null);
			propsTable.update();
			return;
		}
			
		try {
			CustomTag tag = CustomTagFactory.getTagObjectFromRegistry(selectedTag.getTagName());
			if (tag == null)
				throw new Exception("could not retrieve tag from registry: "+selectedTag.getTagName()+" - should not happen here!");
			
			logger.debug("tag from object registry: "+tag);
			logger.debug("tag atts: "+tag.getAttributeNames());
			
			CustomTag protoTag = tag.copy();
			logger.debug("protoTag copy: "+protoTag);
			logger.debug("protoTag atts: "+protoTag.getAttributeNames());
			
			propsTable.setInput(protoTag, selectedTag);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return;
		}
	}
	
//	private String getSelectedTagName() {
//		if (tagDefsWidget.getSelected()!=null) {
//			return tagDefsWidget.getSelected().getCustomTag().getTagName();
//		} else {
//			return null;
//		}
//	}
	
	private void initTagListWidget(Composite parent) {
		tagListWidget = new TagListWidget(parent, 0);
		
		tagListWidget.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				updatePropertiesForSelectedTag();
			}
		});
	}

}
