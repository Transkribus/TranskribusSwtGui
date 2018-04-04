package eu.transkribus.swt_gui.metadata;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.JAXBPageTranscript;
import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.model.beans.pagecontent.PageTypeSimpleType;
import eu.transkribus.core.model.beans.pagecontent.RelationType;
import eu.transkribus.core.model.beans.pagecontent.TextStyleType;
import eu.transkribus.core.model.beans.pagecontent.TextTypeSimpleType;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.RegionTypeUtil;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.core.util.EnumUtils;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class StructuralMetadataWidget extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(StructuralMetadataWidget.class);
	
	// page related md:
	Combo pageStyleCombo, statusCombo;
	Button linkBtn, breakLinkBtn;
	org.eclipse.swt.widgets.List linkList;
	MenuItem deleteLinkMenuItem;

	Group structureGroup;
	Text structureText;
	Combo shapeTypeCombo;
	
	StructTagSpecWidget structTagSpecWidget;
	
	List<Button> structureRadios = new ArrayList<>();
	
	// text style md:
//	TextStyleTypeWidget textStyleWidget;
	
	Button applyStructBtn, applyStructRecBtn;
	Listener listener=null;
	
	public static String LINK_DELIMITER = " <--> ";
	
	ModifyListener structModifyListener;
	
	public StructuralMetadataWidget(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new GridLayout(2, false));
		this.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1));
		
		initPageMd();
		
		Label shapeTypeLabel = new Label(this, 0);
		shapeTypeLabel.setText("Selected element type: ");
		Fonts.setBoldFont(shapeTypeLabel);
		shapeTypeCombo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		shapeTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		shapeTypeCombo.setItems(RegionTypeUtil.ALL_REGIONS.toArray(new String[0]));
		
		// NEW STUFF
		structureGroup = new Group(this, SWT.NONE);
		structureGroup.setText("Structure Type");
		structureGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		GridLayout structureGl = new GridLayout(2, false);
		structureGl.verticalSpacing = 0;
		structureGl.marginWidth = 1;
		structureGroup.setLayout(structureGl);
		Fonts.setBoldFont(structureGroup);
		
		Label structLabel = new Label(structureGroup, 0);
		structLabel.setText("Type of selected: ");
		structureText = new Text(structureGroup, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		structureText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		structureText.setToolTipText("The structure type of the selected element");		
		
		structTagSpecWidget = new StructTagSpecWidget(structureGroup, 0, false);
		structTagSpecWidget.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		((GridData)structTagSpecWidget.getLayoutData()).heightHint = 500;
		Fonts.setNormalFont(structTagSpecWidget.getHeaderLbl());
		// END NEW STUFF
		
//		createStructureGroup(this);

//		initTaggingWidget(container);
		
//		initTextStyleMd(container);
	}
	
	/**
	 * @deprecated old!
	 */
	private void createStructureGroup(Composite container) {
		structureGroup = new Group(container, SWT.NONE);
//		structureGroup = new Composite(expandBar, SWT.NONE);
		structureGroup.setText("Structure Type");
		structureGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		GridLayout structureGl = new GridLayout(2, false);
		structureGl.verticalSpacing = 0;
		structureGl.marginWidth = 1;
		structureGroup.setLayout(structureGl);
		
//		Label l1 = new Label(structureGroup, SWT.NONE);
//		l1.setText("Structure: ");
		structureText = new Text(structureGroup, SWT.SINGLE | SWT.BORDER);
		structureText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		structureText.setToolTipText("The structure type of the selected element - you can edit this field to change the type or choose any of the predifined types below. Clear this text to remove a previously defined structure type.");
		structModifyListener = new ModifyListener() {
			@Override public void modifyText(ModifyEvent e) {
				setStructureType(structureText.getText());
			}
		};
		structureText.addModifyListener(structModifyListener);
		
		for (final TextTypeSimpleType t : TextTypeSimpleType.values()) {
			final Button btn = createButton(structureGroup, SWT.RADIO, t.value(), 1, false);
			btn.addSelectionListener(new SelectionAdapter() {
				@Override public void widgetSelected(SelectionEvent e) {
					setStructureType(t.value());
				}
			});
			structureRadios.add(btn);
		}
		new Label(structureGroup, 0); // needed to align following button correctly!
		
		applyStructBtn = new Button(structureGroup, SWT.PUSH);
		applyStructBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		applyStructBtn.setText("Apply");
		applyStructBtn.setToolTipText("Applies the structure to all selected elements (note: if multiple elements are selected, the metadata is not applied automatically but with this button)");
		
		applyStructRecBtn = new Button(structureGroup, SWT.PUSH);
		applyStructRecBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		applyStructRecBtn.setText("Apply down");
		applyStructRecBtn.setToolTipText("Applies the structure to all selected elements and its child elements, e.g. for a region and all its line and word elements!");
		
		structureGroup.pack();
	}
	
	private Button createButton(Composite parent, int style, String text, int horSpan, boolean grabExcessHorizontal) {
		Button btn = new Button(parent, style);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, grabExcessHorizontal, false, horSpan, 1);
		btn.setLayoutData(gd);
		
		btn.setText(text);
		return btn;
	}
	
	private static Combo initComboWithLabel(Composite parent, String label, int comboStyle) {
//		Composite c = new Composite(parent, SWT.NONE);
//		c.setLayout(new GridLayout(2, false));
//		c.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label l = new Label(parent, SWT.LEFT);
		l.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		l.setText(label);
		Fonts.setBoldFont(l);
		
		Combo combo = new Combo(parent, comboStyle);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
//		c.pack();
		
		return combo;
	}
		
	private void initPageMd() {		
//		mdGroup = new Composite(this, SWT.NONE);
//		mdGroup.setLayout(new GridLayout(2, false));
//		mdGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		mdGroup.setText("Metadata");
		
		statusCombo = initComboWithLabel(this, "Edit status: ", SWT.DROP_DOWN | SWT.READ_ONLY);
		statusCombo.setItems(EnumUtils.stringsArray(EditStatus.class));
		
		pageStyleCombo = initComboWithLabel(this, "Page type: ", SWT.DROP_DOWN | SWT.READ_ONLY);
		pageStyleCombo.setItems(EnumUtils.valuesArray(PageTypeSimpleType.class));
		
		Label linksLabel = new Label(this, SWT.LEFT);
		linksLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		linksLabel.setText("Links:");
		Fonts.setBoldFont(linksLabel);
		Composite linkBtnW = new Composite(this, SWT.RIGHT);
		linkBtnW.setLayout(new FillLayout());
		linkBtnW.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		linkBtn = new Button(linkBtnW, SWT.PUSH);
		linkBtn.setImage(Images.getOrLoad("/icons/link.png"));
		linkBtn.setToolTipText("Links two shapes, e.g. a footnote and a link to it");
		
		breakLinkBtn = new Button(linkBtnW, SWT.PUSH);
		breakLinkBtn.setImage(Images.getOrLoad("/icons/link_break.png"));
		breakLinkBtn.setToolTipText("Removes the selected link");		
		
		linkList = new org.eclipse.swt.widgets.List(this, SWT.SINGLE | SWT.V_SCROLL);
		linkList.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 3));
		int nrOfVisibleItems = 3;
		((GridData)linkList.getLayoutData()).heightHint = linkList.getItemHeight()*nrOfVisibleItems;		
		Menu m = new Menu(linkList);
		deleteLinkMenuItem = new MenuItem(m, 0);
		deleteLinkMenuItem.setText("Delete");
		linkList.setMenu(m);
	}
	
//	private void initTextStyleMd(Composite parent) {		
//		textStyleWidget = new TextStyleTypeWidget(parent, SWT.NONE);
//		textStyleWidget.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
////		textStyleWidget.pack();
//		
////		textStyleGroup.pack();
//	}
	
	public void detachListener() {
		if (listener != null)
			removeMetadataListener(listener);
	}
	
	public void attachListener() {
		if (listener!=null) {
			addMetadataListener(listener);
		}
	}
	
	public void removeMetadataListener(Listener listener) {
		if (listener == null)
			return;
		
		pageStyleCombo.removeSelectionListener((SelectionListener)listener);
		SWTUtil.removeModifyListener(structureText, (ModifyListener) listener);
		for (Button b : structureRadios) {
			b.removeSelectionListener((SelectionListener) listener);
		}
				
		SWTUtil.removeSelectionListener(applyStructBtn, (SelectionListener)listener);
		SWTUtil.removeSelectionListener(applyStructRecBtn, (SelectionListener)listener);
		
		linkList.removeSelectionListener((SelectionListener) listener);
		deleteLinkMenuItem.removeSelectionListener((SelectionListener) listener);
		
		linkBtn.removeSelectionListener((SelectionListener) listener);
		breakLinkBtn.removeSelectionListener((SelectionListener) listener);
		
		shapeTypeCombo.removeSelectionListener((SelectionListener) listener);
	}

	public void addMetadataListener(Object listener) {
		this.listener = (Listener) listener;
		
		if (statusCombo != null)
			statusCombo.addSelectionListener((SelectionListener)listener);
		
		if (pageStyleCombo != null)
			pageStyleCombo.addSelectionListener((SelectionListener)listener);
		
		SWTUtil.addModifyListener(structureText, (ModifyListener) listener);
		for (Button b : structureRadios) {
			b.addSelectionListener((SelectionListener) listener);
		}
		
		SWTUtil.addSelectionListener(applyStructBtn, (SelectionListener)listener);
		SWTUtil.addSelectionListener(applyStructRecBtn, (SelectionListener)listener);
		
		linkList.addSelectionListener((SelectionListener) listener);
		deleteLinkMenuItem.addSelectionListener((SelectionListener) listener);
		
		linkBtn.addSelectionListener((SelectionListener) listener);
		breakLinkBtn.addSelectionListener((SelectionListener) listener);
		
		shapeTypeCombo.addSelectionListener((SelectionListener) listener);
		
//		taggingWidget.addListener(SWT.Selection, (Listener) listener);
//		mainWidget.getUi().getDisplay().addFilter(SWT.KeyDown, this);
	}
	
	@Override public void setEnabled(boolean enabled) {

		// custom tags:
//		TaggingWidget taggingWidget;
		
		// text style md:
//		TextStyleTypeWidget textStyleWidget;		
		
		
		super.setEnabled(enabled);
		// page:
		pageStyleCombo.setEnabled(enabled);
		linkBtn.setEnabled(enabled); breakLinkBtn.setEnabled(enabled);
		linkList.setEnabled(enabled); deleteLinkMenuItem.setEnabled(enabled);
		
		SWTUtil.setEnabled(structureGroup, enabled);
		for (Button b : structureRadios) {
			SWTUtil.setEnabled(b, enabled);
		}
		
		SWTUtil.setEnabled(structTagSpecWidget, enabled);
		
		SWTUtil.setEnabled(applyStructBtn, enabled);
		SWTUtil.setEnabled(applyStructRecBtn, enabled);
	}
	
	public void setStructureType(String structureType) {
		logger.debug("setting structure type to text: "+structureType);
			structureText.setText(StringUtils.isEmpty(structureType) ? "" : structureType);
	}
	
	// OLD
//	public void setStructureType(String structureType) {
//		if (structureText != null) {
//			if (structModifyListener!=null) {
//				structureText.removeModifyListener(structModifyListener);
//			}
//			
//			if (!structureText.getText().equals(structureType)) {
//				structureText.setText(structureType==null ? "" : structureType);
//			}
//			
//			if (structModifyListener!=null) {
//				structureText.addModifyListener(structModifyListener);
//			}
//		}
//
//		for (Button b : structureRadios) {
//			b.setSelection(structureType!=null && b.getText().equals(structureType));
//		}
//	}
	
	public void setPageType(TrpPageType page ) {
		PageTypeSimpleType pageType = page.getType();
		SWTUtil.select(pageStyleCombo, EnumUtils.indexOf(pageType));
	}
	
//	public void updateData(TrpPageType page, int nSelectedShapes, boolean hasStructure, TextTypeSimpleType structureType, boolean hasTextStyle, TextStyleType textStyle, List<String> selectedTagNames) {
	public void updateData(JAXBPageTranscript transcript, ITrpShapeType firstSelected, int nSelectedShapes, String structureType, List<CustomTag> selectedTags) {
		logger.debug("updating page metadata widget");
		
		SWTUtil.recursiveSetEnabled(this, transcript!=null);
//		setEnabled(page!=null);
		if (transcript==null)
			return;
		
		TrpPageType page = transcript.getPage();
		
		// page status:
		SWTUtil.select(statusCombo, EnumUtils.indexOf(transcript.getMd().getStatus()));

		// page type:
		setPageType(page);
		
		boolean hasSelected = nSelectedShapes>0;
		detachListener();
		
		// set region type:
		String rt = RegionTypeUtil.getRegionType(firstSelected);
//		selectedShapeTypeText.setText(RegionTypeUtil.getRegionType(firstSelected));
		
		int ri =  RegionTypeUtil.ALL_REGIONS.indexOf(rt);
		
		if (ri != -1)
			shapeTypeCombo.select(ri);
		else
			shapeTypeCombo.deselectAll();
		
		// set structure type:
		setStructureType(structureType);
		//		if (regionTypeCombo!=null) {
		//		SWTUtil.recursiveSetEnabled(regionTypeCombo, hasSelected);
		//		SWTUtil.select(regionTypeCombo, EnumUtils.indexOf(structureType));
		//	}
		//	
		//	logger.debug("updating data, structureType = "+structureType);
		//	if (structureTypeTableViewer!=null) {
		//		for (TextTypeSimpleType s : TextTypeSimpleType.values()) {
		//			String ssValue = structureType==null ? "" : structureType.value();
		//			structureTypeTableViewer.setChecked(s, s.value().equals(ssValue));	
		//		}
		//	}
		logger.debug("st before = "+structureText.getText()+" new = "+structureType);		
				
		// update link list and keep last selected item if still there:
		String lastSel=null;
		if (linkList.getSelectionCount()==1) {
			lastSel = linkList.getSelection()[0];
		}
		linkList.removeAll();
		if (transcript!=null && page.getRelations()!=null) {
			int selIndex=-1;
			for (int i=0; i<page.getRelations().getRelation().size(); ++i) {
				RelationType r = page.getRelations().getRelation().get(i);
				ITrpShapeType s1 = (ITrpShapeType) r.getRegionRef().get(0).getRegionRef();
				ITrpShapeType s2 = (ITrpShapeType) r.getRegionRef().get(1).getRegionRef();
				
				if (s1 == null || s2 == null) {
					logger.warn("Warning: dead link found: "+i);
					continue;
				}
				
				String link = s1.getId() + LINK_DELIMITER +s2.getId();
				linkList.add(link);
				
				if (lastSel!=null && lastSel.equals(link)) {
					selIndex = i;
				}
			}
			if (selIndex != -1)
				linkList.select(selIndex);
		}
		
		attachListener();
	}
	
//	public void updateData(boolean hasPageType, PageTypeSimpleType pageType, boolean hasTextType, TextTypeSimpleType regionType, boolean hasTextStyle, TextStyleType textType) {
//		SWTUtil.recursiveSetEnabled(mdGroup, hasPageType);
//		SWTUtil.select(pageStyleCombo, EnumUtils.indexOf(pageType));
//		
//		SWTUtil.recursiveSetEnabled(regionTypeCombo, hasTextType);
//		SWTUtil.select(regionTypeCombo, EnumUtils.indexOf(regionType));
//		
//		SWTUtil.recursiveSetEnabled(textStyleWidget, hasTextStyle);
//		textStyleWidget.updateTextStyleFromData(textType);
//	}
	
//	public String getStructureType() {
//		return structureText.getText();
//		for (Button b : structureRadios) {
//			if (b.getSelection()) {
//				return b.getText();
//			}
//		}
		
//		logger.debug("selected structure = "+type);
//		return null;
//	}

	public Combo getPageStyleCombo() { return pageStyleCombo; }
	
//	public TextStyleTypeWidget getTextStyleWidget() { return textStyleWidget; }
	public Button getApplyStructBtn() { return applyStructBtn; }
	public Button getApplyStructRecBtn() { return applyStructRecBtn; }
	
	public org.eclipse.swt.widgets.List getLinkList() { return linkList; }
	public MenuItem getDeleteLinkMenuItem() { return deleteLinkMenuItem; }

	public Button getLinkBtn() {
		return linkBtn;
	}

	public Button getBreakLinkBtn() {
		return breakLinkBtn;
	}

	public List<Button> getStructureRadios() {
		return structureRadios;
	}
	
//	public CheckboxTableViewer getStructureTypeTableViewer() { return structureTypeTableViewer; }
	
}
