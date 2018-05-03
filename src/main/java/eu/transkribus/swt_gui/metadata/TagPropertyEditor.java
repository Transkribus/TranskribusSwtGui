package eu.transkribus.swt_gui.metadata;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagList;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpShapeTypeUtils;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.databinding.DataBinder;
import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.canvas.CanvasKeys;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import eu.transkribus.swt_gui.transcription.ATranscriptionWidget;
import eu.transkribus.swt_gui.util.DropDownButton;

public class TagPropertyEditor extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(TagPropertyEditor.class);
	
	CustomTagPropertyTable propsTable;
	CustomTag tag;
	
	Composite headerComp;
	Label tagInfo;
	
	Composite btnsComposite;
	Button nextBtn, prevBtn, refreshBtn;
//	ATranscriptionWidget tWidget;
	
	MenuItem autoSelectTagInTranscriptionWidgetItem, showNonEditablePropsItem;
	
	boolean settingCustomTag=false;
	
	public TagPropertyEditor(Composite parent, /*ATranscriptionWidget tWidget,*/ boolean withHeader) {
		super(parent, 0);
		this.setLayout(SWTUtil.createGridLayout(2, false, 0, 0));
		
//		Assert.assertNotNull("Transcription widget cannot be null!", tWidget);
//		this.tWidget = tWidget;
		
		if (withHeader) {
			Label header = new Label(this, 0);
			header.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			header.setText("Tag Property Editor");
			Fonts.setBoldFont(header);
		}
		
		headerComp = new Composite(this, 0);
		headerComp.setLayout(new GridLayout(2, false));
		headerComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		tagInfo = new Label(headerComp, 0);
		tagInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		tagInfo.setText("Props for tag: no tag selected");
		Fonts.setBoldFont(tagInfo);
		
		propsTable = new CustomTagPropertyTable(this, 0, false);
		propsTable.getTableViewer().getTable().setHeaderVisible(false);
		propsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		btnsComposite = new Composite(this, 0);
		btnsComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		btnsComposite.setLayout(new RowLayout());
		
		prevBtn = new Button(btnsComposite, 0);
		prevBtn.setText("Previous");
		prevBtn.setImage(Images.PAGE_PREV);
		SWTUtil.onSelectionEvent(prevBtn, e -> {
			jumpToNextTag(true);
		});
		
		nextBtn = new Button(btnsComposite, 0);
		nextBtn.setText("Next");
		nextBtn.setImage(Images.PAGE_NEXT);
		SWTUtil.onSelectionEvent(nextBtn, e -> {
			jumpToNextTag(false);
		});
		
		refreshBtn = new Button(btnsComposite, 0);
		refreshBtn.setImage(Images.REFRESH);
		SWTUtil.onSelectionEvent(refreshBtn, e -> {
			findAndSetNextTag();
		});
		
		DropDownButton prefsBtn = new DropDownButton(btnsComposite, 0, "", Images.WRENCH, null);
		autoSelectTagInTranscriptionWidgetItem = prefsBtn.addItem("Auto select tag in transcription widget", null, SWT.CHECK);
		autoSelectTagInTranscriptionWidgetItem.setSelection(false);
		
		showNonEditablePropsItem = prefsBtn.addItem("Show non-editable properties", null, SWT.CHECK);
		showNonEditablePropsItem.setSelection(false);
		DataBinder.get().bindBeanToWidgetSelection(TrpSettings.SHOW_NON_EDITABLE_TEXT_TAG_PROPERTIES_PROPERTY, TrpConfig.getTrpSettings(), showNonEditablePropsItem);
		
		propsTable.getTableViewer().getTable().addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				logger.debug("traverse event in TagPropertyEditor!");
				if (e.detail == SWT.TRAVERSE_RETURN) {
					e.doit = false;
					jumpToNextTag(CanvasKeys.isShiftKeyDown(e.stateMask));
				}
				else if (e.detail == SWT.TRAVERSE_ARROW_NEXT) {
					e.doit = false;
					jumpToNextTag(false);
				}
				else if (e.detail == SWT.TRAVERSE_ARROW_PREVIOUS) {
					e.doit = false;
					jumpToNextTag(true);
				}
			}
		});
		
		TrpConfig.getTrpSettings().addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(TrpSettings.SHOW_NON_EDITABLE_TEXT_TAG_PROPERTIES_PROPERTY)) {
					propsTable.setShowNonEditableProperties(showNonEditablePropsItem.getSelection());
				}
			}
		});
		
		setCustomTag(null, false);
	}
	
	public Composite getBtnsComposite() {
		return btnsComposite;
	}
	
	public void setCustomTag(CustomTag tag, boolean selectFirstAttribute) {
		ATranscriptionWidget tWidget = getCurrentTranscriptionWidget();
		if (tWidget == null) {
			return;
		}		
		
		settingCustomTag = true;
		this.tag = tag;

		if (this.tag != null) {
			tagInfo.setText("Props for tag: '"+tag.getTagName()+"'  -  value: '"+tag.getContainedText()+"'");
			propsTable.setInput(this.tag);

			// select tag in widget:
			if (this.tag.getCustomTagList() != null) {
				// select shape first if not yet done:
				ITrpShapeType shape = this.tag.getCustomTagList().getShape();
				TrpMainWidget.getInstance().selectObjectWithData(shape, true, false);

				// TODO: introduce flag
				if (autoSelectTagInTranscriptionWidgetItem.getSelection()) {
					tWidget.selectCustomTag(tag);
				}
			}
		} else {
			tagInfo.setText("Props for tag: no tag selected");
			propsTable.setInput(null);
		}
		
		if (selectFirstAttribute) {
			propsTable.selectFirstAttribute();
		}
		settingCustomTag = false;
	}
	
	public boolean isSettingCustomTag() {
		return settingCustomTag;
	}
	
	public void findAndSetNextTag() {
		ATranscriptionWidget tWidget = getCurrentTranscriptionWidget();
		if (tWidget == null) {
			return;
		}
		
		List<CustomTag> tagsForOffset = tWidget.getCustomTagsForCurrentOffset();
		if (tagsForOffset.isEmpty()) {
			int co = tWidget.getText().getCaretOffset();
			for (int i=co+1; i<tWidget.getText().getText().length(); ++i) {
				List<CustomTag> tagsForOffset1 = tWidget.getCustomTagsForOffset(i);
				if (!tagsForOffset1.isEmpty()) {
					tagsForOffset = tagsForOffset1;
//					tWidget.getText().setSelection(i);
					break;
				}
			}
		}
		
		if (!tagsForOffset.isEmpty()) {
			setCustomTag(tagsForOffset.get(0), true);
		}
	}
	
	public void jumpToNextTag(boolean previous) {
		ATranscriptionWidget tWidget = getCurrentTranscriptionWidget();
		if (tWidget == null) {
			return;
		}		
		
		if (tag == null) {
			findAndSetNextTag();
		}
		
		ITrpShapeType shape = tWidget.getTranscriptionUnit();
		CustomTagList ctl = shape.getCustomTagList();
		List<CustomTag> tags = ctl.getIndexedTags();
		CustomTag neighborTag = CoreUtils.getNeighborElement(tags, tag, previous, false);
		if (neighborTag == null) { // neighbor tag not found in this shape -> search for in neighbor shapes!
			ITrpShapeType neighborShape = TrpShapeTypeUtils.getNeighborShape(shape, previous, false);
			while (neighborShape!=null) {
				List<CustomTag> tagsOfNeighborShape = neighborShape.getCustomTagList().getIndexedTags();
				if (!tagsOfNeighborShape.isEmpty()) {
					neighborTag = tagsOfNeighborShape.get(previous ? tagsOfNeighborShape.size()-1 : 0);
					break;
				}
				
				neighborShape = TrpShapeTypeUtils.getNeighborShape(neighborShape, previous, false);
			}
		}
				
		if (neighborTag != null) {
			setCustomTag(neighborTag, true);
		}
	}
	
	private ATranscriptionWidget getCurrentTranscriptionWidget() {
		if (TrpMainWidget.getInstance()==null) {
			return null;
		}
		else {
			return TrpMainWidget.getInstance().getUi().getSelectedTranscriptionWidget();
		}
	}

}
