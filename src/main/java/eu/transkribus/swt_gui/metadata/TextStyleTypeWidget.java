package eu.transkribus.swt_gui.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

import eu.transkribus.core.model.beans.JAXBPageTranscript;
import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.TextStyleTag;
import eu.transkribus.core.model.beans.pagecontent.ColourSimpleType;
import eu.transkribus.core.model.beans.pagecontent.TextStyleType;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.util.EnumUtils;
import eu.transkribus.core.util.SebisStopWatch;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.ExclusiveButtonSelectionGroup;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.transcription.ATranscriptionWidget;
import eu.transkribus.swt_gui.util.GuiUtil;

public class TextStyleTypeWidget extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(TextStyleTypeWidget.class);
	
	public Composite container;
	public Group styleSheetGroup;
	
//	public Combo styleSheetCombo;
	public org.eclipse.swt.widgets.List styleSheetList;
	
//	public Text fontFamilyText;
	public Combo fontFamilyText;
//	public Button enableIndexedStylesBtn;
	public Button serifCheck, monospaceCheck, reverseVideoCheck, boldCheck, italicCheck, subscriptCheck, superscriptCheck,
			underlinedCheck, strikethroughCheck, smallCapsCheck, letterSpacedCheck;
	
	public Text styleSheetName;
	public Button addAsStyleSheet;
	public Button deleteStyleSheet;
	
	public Combo textColorCombo, bgColorCombo;
	public Spinner fontSizeSpinner, kerningSpinner;
	
	public Map<String, TextStyleType> styleSheets = new TreeMap<>();
	
	public final static int FLOAT_DIGITS = 1;
	public final static int FLOAT_MULTIPLICATOR = (int) Math.pow(10, FLOAT_DIGITS);
	
	public final static String styleSheetsFn = "style_sheets.xml";
	
//	public Button applyBtn;
	
	private List<Widget> textStyleSources;
	Listener listener;

	private SelectionAdapter styleSheetSelectionListener;
	
//	public Button applyBtn, applyRecursiveBtn;
	
	public Button underlineTextStylesBtn;
	
	static boolean USE_GROUP_CONTAINER = false;
	
//	static TrpMainWidget mw = TrpMainWidget.getInstance();

	public TextStyleTypeWidget(Composite parent, int style) {
		super(parent, style);
//		this.setLayout(new FillLayout());
		this.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));

		textStyleSources = new ArrayList<>();
		
//		this.setLayout(new GridLayout(2, false));
		
		if (USE_GROUP_CONTAINER) {
			container = new Group(this, SWT.NONE);
			GridLayout layout = new GridLayout(2, true);
			layout.marginHeight = 15;
			layout.marginWidth = 5;
			layout.verticalSpacing = 1;
			container.setLayout(layout);
			
			((Group) container).setText("Text style");
		} else {
			container = new Composite(this, SWT.NONE);
			container.setLayout(SWTUtil.createGridLayout(2, true, 0, 0));
		}
		
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		underlineTextStylesBtn = new Button(container, SWT.CHECK);
		underlineTextStylesBtn.setText("Underline styled text");
		underlineTextStylesBtn.setToolTipText("If enabled, text styles are underlined like other tags in the transcription widget");
		underlineTextStylesBtn.setSelection(true);
		underlineTextStylesBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		underlineTextStylesBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				if (TrpMainWidget.getInstance().getUi().getSelectedTranscriptionWidget() != null)
					TrpMainWidget.getInstance().getUi().getSelectedTranscriptionWidget().redrawText(true);
			}
		});
		
//		enableIndexedStylesBtn = new Button(textStyleGroup, SWT.CHECK);
//		enableIndexedStylesBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
//		enableIndexedStylesBtn.setText("Enable indexed styles");
//		enableIndexedStylesBtn.setEnabled(true);
		
		initStyleSheetsWidget();
				
//		textStyleGroup.setLayout(new FillLayout());
		
		Label l1 = new Label(container, SWT.LEFT);
		l1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		l1.setText("Font family: ");
//		fontFamilyText = new Text(textStyleGroup, SWT.LEFT | SWT.BORDER);
		fontFamilyText = new Combo(container, SWT.LEFT | SWT.BORDER);
		fontFamilyText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		fontFamilyText.setData("propertyName", "fontFamily");
		fontFamilyText.setItems(new String [] { "Antiqua", "Gothic", "Normal" });

		boldCheck = createButton(container, SWT.CHECK, "Bold", 1, false, "bold");
		italicCheck = createButton(container, SWT.CHECK, "Italic", 1, false, "italic");
		subscriptCheck = createButton(container, SWT.CHECK, "Subscript", 1, false, "subscript");
		superscriptCheck = createButton(container, SWT.CHECK, "Superscript", 1, false, "superscript");
		underlinedCheck = createButton(container, SWT.CHECK, "Underlined", 1, false, "underlined");		
		strikethroughCheck = createButton(container, SWT.CHECK, "Strikethrough", 1, false, "strikethrough");		
		serifCheck = createButton(container, SWT.CHECK, "Serif", 1, false, "serif");
		monospaceCheck = createButton(container, SWT.CHECK, "Monospace", 1, false, "monospace");		
		reverseVideoCheck = createButton(container, SWT.CHECK, "Reverse Video", 1, false, "reverseVideo");
		smallCapsCheck = createButton(container, SWT.CHECK, "Small caps", 1, false, "smallCaps");
		letterSpacedCheck = createButton(container, SWT.CHECK, "Letter spaced", 2, false, "letterSpaced");
		
		fontSizeSpinner = createSpinner(container, "Font size: ", true, "fontSize");
		kerningSpinner = createSpinner(container, "Kerning: ", false, "kerning");
		
		textColorCombo = createComboWithLabel(container, "Text color: ", SWT.DROP_DOWN | SWT.READ_ONLY, "textColor");
		textColorCombo.setItems(EnumUtils.valuesArray(ColourSimpleType.class));
		
		bgColorCombo = createComboWithLabel(container, "Background color: ", SWT.DROP_DOWN | SWT.READ_ONLY, "bgColor");
		bgColorCombo.setItems(EnumUtils.valuesArray(ColourSimpleType.class));
		
		
//		applyBtn = createButton(container, SWT.PUSH, "Apply", 1, false, null);
//		applyRecursiveBtn = createButton(container, SWT.PUSH, "Apply recursively", 1, false, null);
		
//		applyBtn.setToolTipText("Applies the style to all selected elements (note: if multiple elements are selected, the metadata is not applied automatically but with this button)");
//		applyRecursiveBtn.setToolTipText("Applies the style to all selected elements and its child elements, e.g. for a region and all its line and word elements!");		
		
//		applyBtn = new Button(textStyleGroup, SWT.PUSH);
//		applyBtn.setText("Apply");
//		applyBtn.setToolTipText("If multiple elements are selected, the metadata is not applied automatically but with this button!");
		
		
		textStyleSources.add(bgColorCombo);
		textStyleSources.add(boldCheck);
		textStyleSources.add(fontFamilyText);
		textStyleSources.add(fontSizeSpinner);
		textStyleSources.add(italicCheck);
		textStyleSources.add(kerningSpinner);
		textStyleSources.add(letterSpacedCheck);
		textStyleSources.add(monospaceCheck);
		textStyleSources.add(reverseVideoCheck);
		textStyleSources.add(serifCheck);
		textStyleSources.add(smallCapsCheck);
		textStyleSources.add(strikethroughCheck);
		textStyleSources.add(underlinedCheck);
		textStyleSources.add(subscriptCheck);
		textStyleSources.add(superscriptCheck);
		textStyleSources.add(textColorCombo);
		
		addInternalListener();
	}
	
	private void initStyleSheetsWidget() {
		ExpandableComposite styleSheetExp = new ExpandableComposite(container, ExpandableComposite.COMPACT);
		styleSheetExp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		Composite styleSheetGroup = new Composite(styleSheetExp, SWT.SHADOW_ETCHED_IN);
//		styleSheetGroup = new Group(textStyleGroup, SWT.NONE);
//		styleSheetGroup.setText("Stylesheets");
		styleSheetGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		styleSheetGroup.setLayout(new GridLayout(3, false));
		
		styleSheetList = new org.eclipse.swt.widgets.List(styleSheetGroup, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
		styleSheetList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3));
		styleSheetList.pack();
		int nrOfVisibleItems = 4;
		((GridData)styleSheetList.getLayoutData()).heightHint = styleSheetList.getItemHeight()*nrOfVisibleItems;		
//		Menu m = new Menu(textStyleGroup);
//		MenuItem mi = new MenuItem(m, 0);
//		mi.setText("Delete");
//		styleSheetList.setMenu(m);
		
		Label l = new Label(styleSheetGroup, SWT.LEFT);
		l.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		l.setText("Name: ");
		styleSheetName = new Text(styleSheetGroup, SWT.LEFT | SWT.BORDER);
		styleSheetName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		
		addAsStyleSheet = new Button(styleSheetGroup, SWT.PUSH);
		addAsStyleSheet.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 2, 1));
		addAsStyleSheet.setText("Add stylesheet");
		
		deleteStyleSheet = new Button(styleSheetGroup, SWT.PUSH);
		deleteStyleSheet.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 2, 1));
		deleteStyleSheet.setText("Delete stylesheet");			
		
		
//		textStyleGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
//		styleSheetCombo = createComboWithLabel(textStyleGroup, "Style sheet", SWT.DROP_DOWN | SWT.READ_ONLY);
		
//		styleSheetList = createListWithLabel(styleSheetGroup, "Style sheet", SWT.SINGLE | SWT.V_SCROLL);

//		styleSheetList.setSize(styleSheetList.computeSize(SWT.DEFAULT, styleSheetList.getItemHeight()*3));
//		styleSheetList.setSize(styleSheetList.computeSize(SWT.DEFAULT, 10));
		
		loadStyleSheets();
		
		styleSheetExp.setClient(styleSheetGroup);
		styleSheetExp.setText("Style sheets");
		styleSheetExp.setExpanded(false);
		styleSheetExp.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				container.layout();
			}
		});
	}

	private TextStyleType getSelectedStyleSheetStyle() {
		if (styleSheetList.getSelectionCount()==1) {
			String name = styleSheetList.getSelection()[0];
			if (name!=null && !name.isEmpty())
				return styleSheets.get(name);
		}
		return null;
	}
	
	private void addInternalListener() {
		// make super and subscript exclusively selectable:
		ExclusiveButtonSelectionGroup subSuperGroup = new ExclusiveButtonSelectionGroup();
		subSuperGroup.addButton(subscriptCheck);
		subSuperGroup.addButton(superscriptCheck);
		
//		styleSheetList.addMouseListener(new MouseAdapter() {
//			@Override
//			public void mouseDoubleClick(MouseEvent e) {				
//				TextStyleType ts = getSelectedStyleSheetStyle();
//				if (ts != null) {
//					updateTextStyleFromData(ts);
//					boldCheck.notifyListeners(SWT.Selection, new Event());// send signal that some data has changed
//				}
//			}
//		});
		
		styleSheetSelectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TextStyleType ts = getSelectedStyleSheetStyle();
				if (ts != null) {
					updateTextStyleFromData(ts);
					boldCheck.notifyListeners(SWT.Selection, new Event());// send signal that some data has changed
				}
			}
		};
		styleSheetList.addSelectionListener(styleSheetSelectionListener);
		
		addAsStyleSheet.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				String name = styleSheetName.getText();
				if (name==null || name.isEmpty()) {
					DialogUtil.showErrorMessageBox(getShell(), "Error adding stylesheet", "Please specify a name for the new stylesheet!");
					return;
				}
				
				if (!styleSheets.containsKey(name) || DialogUtil.showYesNoDialog(getShell(), "Style sheet already exists", "A stylesheet with this name already"
						+ "exists - overwrite?") == SWT.YES ) {
					styleSheets.put(name, getTextStyleTypeFromUi());
					saveStyleSheets();
				}				
			}
		});
		
		deleteStyleSheet.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				if (styleSheetList.getSelectionCount()==1) {
					String name = styleSheetList.getSelection()[0];
					if (styleSheets.containsKey(name)) {
						styleSheets.remove(name);
						saveStyleSheets();
					}
				}			
			}
		});
	}
	
	private void updateStyleSheetCombo() {
		if (styleSheetList!=null)
			styleSheetList.setItems(styleSheets.keySet().toArray(new String[0]));
		
//		int nrOfVisibleItems = 4;
//		((GridData)styleSheetList.getLayoutData()).heightHint = styleSheetList.getItemHeight()*nrOfVisibleItems;
	}
	
	private void loadStyleSheets() {
		XStream xstream = new XStream();
		
		if (!(new File(styleSheetsFn)).exists()) {
			styleSheets = new TreeMap<String, TextStyleType>();
			return;
		}
		
		try (InputStream input = new FileInputStream(styleSheetsFn) ) {
			styleSheets = (Map<String, TextStyleType>) xstream.fromXML(input);
			logger.debug("loaded "+styleSheets.size()+" stylesheets!");
			updateStyleSheetCombo();
		} catch (IOException e1) {
			logger.error(e1.getMessage(), e1);
		}
	}
	
	private void saveStyleSheets() {
		logger.debug("saving "+styleSheets.size()+ " stylesheets to "+styleSheetsFn);
		for (TextStyleType ts : styleSheets.values()) {
			logger.debug("ts = "+ts.toString());
			
		}
		
		XStream xstream = new XStream();
		try (OutputStream output = new FileOutputStream(styleSheetsFn)) {
			xstream.toXML(styleSheets, output);

			updateStyleSheetCombo();
		} catch (IOException e1) {
			logger.error(e1.getMessage(), e1);
		}
	}
	
	private void detachListener() {
		if (listener!=null) {
			removeTextStyleListener(listener);
		}
	}
	
	private void attachListener() {
		if (listener!=null) {
			addTextStyleListener(listener);
		}
	}
	
//	public void updateData(JAXBPageTranscript transcript, ITrpShapeType firstSelected, int nSelectedShapes, String structureType, TextStyleType textStyle, List<CustomTag> selectedTags) {
//	public void updateData(int nSelectedShapes, TextStyleType textStyle) {
	public void updateData() {
		SebisStopWatch.SW.start();
		
		detachListener();
		
		TrpMainWidget mw = TrpMainWidget.getInstance();
		int nSelected = mw.getCanvas().getNSelected();
		
		// get text style(s) for selection:
		boolean isSelectedInTranscriptionWidget = mw.isTextSelectedInTranscriptionWidget();
		logger.debug("isSelectedInTranscriptionWidget = " + isSelectedInTranscriptionWidget);
		TextStyleType textStyle = new TextStyleType();
		if (nSelected > 0) {
			if (!mw.getTrpSets().isEnableIndexedStyles()) { // OUTDATED
				textStyle = mw.getCanvas().getScene().getCommonTextStyleOfSelected();
			} else { // get common TextStyleType for selection
				ATranscriptionWidget aw = mw.getUi().getSelectedTranscriptionWidget();
				if (aw != null) {
					TextStyleTag tst = aw.getCommonIndexedCustomTagForCurrentSelection(TextStyleTag.TAG_NAME);
					if (tst != null)
						textStyle = tst.getTextStyle();
				}
			}
		}
		
		// update text style widget:
		SWTUtil.recursiveSetEnabled(this, nSelected>0);
		updateTextStyleFromData(textStyle);
		
		attachListener();
		
		SebisStopWatch.SW.stop(true, "time for updating text style widget: ", logger);
	}
	
	public void savePage(){
		//TrpMainWidget.getInstance().saveTranscriptionSilent();
	}
	
	public void removeTextStyleListener(Listener listener) {
		if (listener == null)
			return;
		
		bgColorCombo.removeSelectionListener((SelectionListener)listener);		
		boldCheck.removeSelectionListener((SelectionListener)listener);
		fontFamilyText.removeModifyListener((ModifyListener)listener);
		fontSizeSpinner.removeSelectionListener((SelectionListener)listener);
		italicCheck.removeSelectionListener((SelectionListener)listener);
		kerningSpinner.removeSelectionListener((SelectionListener)listener);
		letterSpacedCheck.removeSelectionListener((SelectionListener)listener);
		monospaceCheck.removeSelectionListener((SelectionListener)listener);
		reverseVideoCheck.removeSelectionListener((SelectionListener)listener);
		serifCheck.removeSelectionListener((SelectionListener)listener);
		smallCapsCheck.removeSelectionListener((SelectionListener)listener);
		strikethroughCheck.removeSelectionListener((SelectionListener)listener);
		underlinedCheck.removeSelectionListener((SelectionListener)listener);
		subscriptCheck.removeSelectionListener((SelectionListener)listener);
		superscriptCheck.removeSelectionListener((SelectionListener)listener);
		textColorCombo.removeSelectionListener((SelectionListener)listener);
		
//		applyBtn.removeSelectionListener((SelectionListener)listener);
//		applyRecursiveBtn.removeSelectionListener((SelectionListener)listener);
	}	
	
	public void addTextStyleListener(Listener listener) {
		if (listener == null)
			return;
		
		this.listener = listener;
		
		bgColorCombo.addSelectionListener((SelectionListener)listener);
		boldCheck.addSelectionListener((SelectionListener)listener);
		fontFamilyText.addModifyListener((ModifyListener)listener);
		fontSizeSpinner.addSelectionListener((SelectionListener)listener);
		italicCheck.addSelectionListener((SelectionListener)listener);
		kerningSpinner.addSelectionListener((SelectionListener)listener);
		letterSpacedCheck.addSelectionListener((SelectionListener)listener);
		monospaceCheck.addSelectionListener((SelectionListener)listener);
		reverseVideoCheck.addSelectionListener((SelectionListener)listener);
		serifCheck.addSelectionListener((SelectionListener)listener);
		smallCapsCheck.addSelectionListener((SelectionListener)listener);
		strikethroughCheck.addSelectionListener((SelectionListener)listener);
		underlinedCheck.addSelectionListener((SelectionListener)listener);
		subscriptCheck.addSelectionListener((SelectionListener)listener);
		superscriptCheck.addSelectionListener((SelectionListener)listener);
		textColorCombo.addSelectionListener((SelectionListener)listener);
		
//		applyBtn.addSelectionListener((SelectionListener)listener);
//		applyRecursiveBtn.addSelectionListener((SelectionListener)listener);		
	}
	
	public TextStyleType getTextStyleTypeFromUi() {
		TextStyleType ts = new TextStyleType();
		
		if (!fontFamilyText.getText().isEmpty()) ts.setFontFamily(fontFamilyText.getText());
		if (boldCheck.getSelection()) ts.setBold(boldCheck.getSelection());
		if (italicCheck.getSelection()) ts.setItalic(italicCheck.getSelection());
		if (subscriptCheck.getSelection()) ts.setSubscript(subscriptCheck.getSelection());
		if (superscriptCheck.getSelection()) ts.setSuperscript(superscriptCheck.getSelection());
		if (strikethroughCheck.getSelection()) ts.setStrikethrough(strikethroughCheck.getSelection());
		if (underlinedCheck.getSelection()) ts.setUnderlined(underlinedCheck.getSelection());
		if (serifCheck.getSelection()) ts.setSerif(serifCheck.getSelection());
		if (monospaceCheck.getSelection()) ts.setMonospace(monospaceCheck.getSelection());
		if (reverseVideoCheck.getSelection()) ts.setReverseVideo(reverseVideoCheck.getSelection());
		if (smallCapsCheck.getSelection()) ts.setSmallCaps(smallCapsCheck.getSelection());
		if (letterSpacedCheck.getSelection()) ts.setLetterSpaced(letterSpacedCheck.getSelection());
		
//		logger.debug("fs = "+(float)fontSizeSpinner.getSelection()/(float)(100*FLOAT_DIGITS));
		
		if (fontSizeSpinner.getSelection()!=0)
			ts.setFontSize((float)fontSizeSpinner.getSelection()/(float)FLOAT_MULTIPLICATOR);
		if (kerningSpinner.getSelection()!=0)
			ts.setKerning(kerningSpinner.getSelection());
				
//		logger.debug("text color = "+textColorCombo.getText());
//		logger.debug("bg color = '"+bgColorCombo.getText()+" isEmpty = "+bgColorCombo.getText().isEmpty());
		
		ts.setTextColour(EnumUtils.fromValue(ColourSimpleType.class, textColorCombo.getText()));
		ts.setBgColour(EnumUtils.fromValue(ColourSimpleType.class, bgColorCombo.getText()));
				
		return ts;
	}
	
	private Pair<String, Integer> getStyleSheetNameAndIndexFromStyle(TextStyleType ts) {
		
		int i=0;
		for (Map.Entry<String, TextStyleType> e : styleSheets.entrySet()) {
		    String key = e.getKey();
//		    logger.debug("ss: "+e.getValue());
		    
		    if (GuiUtil.isEqualStyle(e.getValue(), ts)) {
		    	return Pair.of(key, i);
		    }
		    ++i;
		}
		return null;
	}
	
	public void updateStyleSheetAccordingToCurrentSelection() {
		
		TextStyleType ts = getTextStyleTypeFromUi();
		logger.trace("updating text stylesheet: "+ts);
		
		if (styleSheetSelectionListener!=null) styleSheetList.removeSelectionListener(styleSheetSelectionListener);
		
		Pair<String, Integer> styleSheetNameAndIndex = getStyleSheetNameAndIndexFromStyle(ts);
		if (styleSheetNameAndIndex != null) {
			logger.debug("found the style: "+styleSheetNameAndIndex);
			styleSheetList.select(styleSheetNameAndIndex.getRight());
		} else {
			styleSheetList.deselectAll();
		}
		
		if (styleSheetSelectionListener!=null) styleSheetList.addSelectionListener(styleSheetSelectionListener);
	}
	
	public void updateTextStyleFromData(TextStyleType ts) {
		if (ts == null)
			ts = new TextStyleType();
				
		logger.trace("updating text style in widget: "+ts);
//		logger.debug("font family: "+ts.getFontFamily());
		SWTUtil.set(fontFamilyText, ts.getFontFamily());
		SWTUtil.set(boldCheck, ts.isBold());
		SWTUtil.set(italicCheck, ts.isItalic());
		SWTUtil.set(subscriptCheck, ts.isSubscript());
		SWTUtil.set(superscriptCheck, ts.isSuperscript());
		SWTUtil.set(strikethroughCheck, ts.isStrikethrough());
		SWTUtil.set(underlinedCheck, ts.isUnderlined());
		SWTUtil.set(serifCheck, ts.isSerif());
		SWTUtil.set(monospaceCheck, ts.isMonospace());
		SWTUtil.set(reverseVideoCheck, ts.isReverseVideo());
		SWTUtil.set(smallCapsCheck, ts.isSmallCaps());
		SWTUtil.set(letterSpacedCheck, ts.isLetterSpaced());
		
		int fs = 0;
		if (ts.getFontSize()!=null) fs = (int)(FLOAT_MULTIPLICATOR*ts.getFontSize()); 
		SWTUtil.set(fontSizeSpinner, fs);
		SWTUtil.set(kerningSpinner, ts.getKerning());
		
		SWTUtil.select(textColorCombo, EnumUtils.indexOf(ts.getTextColour()));
		SWTUtil.select(bgColorCombo, EnumUtils.indexOf(ts.getBgColour()));
		
		// custom tags:
//		for (Button b : structureRadios) {
//			b.setSelection(ts.getStructureType()!=null && b.getText().equals(ts.getStructureType().value()));
//		}
		
		updateStyleSheetAccordingToCurrentSelection();
	}
		
	private static Combo createComboWithLabel(Composite parent, String label, int comboStyle, String propertyName) {		
		Label l = new Label(parent, SWT.LEFT);
		l.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		l.setText(label);
		
		Combo combo = new Combo(parent, comboStyle);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (propertyName!=null && !propertyName.isEmpty())
			combo.setData("propertyName", propertyName);		
		
		return combo;
	}
	
	private static org.eclipse.swt.widgets.List createListWithLabel(Composite parent, String label, int style, String propertyName) {		
		Label l = new Label(parent, SWT.LEFT);
		l.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		l.setText(label);
		
		org.eclipse.swt.widgets.List list = new org.eclipse.swt.widgets.List(parent, style);
		list.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (propertyName!=null && !propertyName.isEmpty())
			list.setData("propertyName", propertyName);
		
		return list;
	}	
	
	private Spinner createSpinner(Composite parent, String label, boolean isFloat, String propertyName) {		
		Label l = new Label(parent, SWT.LEFT);
		l.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		l.setText(label);
				
		Spinner sp = new Spinner(parent, SWT.BORDER);
		sp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (propertyName!=null && !propertyName.isEmpty())
			sp.setData("propertyName", propertyName);

	    // set the minimum value to 0.1
		sp.setMinimum(0);
	    // set the maximum value to 20
		sp.setMaximum((int) 1e6);
		
		if (isFloat) {
			// allow 3 decimal places
			sp.setDigits(FLOAT_DIGITS);
			sp.setIncrement(FLOAT_MULTIPLICATOR);
//			sp.setSelection(FLOAT_MULTIPLICATOR);
		} else {
			sp.setIncrement(0);
			sp.setIncrement(1);
//			sp.setSelection(1);			
		}
		sp.setSelection(0);
		sp.pack();
		
	    return sp;
		
	}
	
	private Button createButton(Composite parent, int style, String text, int horSpan, boolean grabExcessHorizontal, String propertyName) {
		Button btn = new Button(parent, style);
		btn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, grabExcessHorizontal, false, horSpan, 1));
		
		if (propertyName!=null && !propertyName.isEmpty())
			btn.setData("propertyName", propertyName);
		
		btn.setText(text);
		return btn;
	}
	
	public List<Widget> getTextStyleSources() {
		return textStyleSources;
	}
	
//	public Button getApplyBtn() { return applyBtn; }
//	public Button getApplyRecursiveBtn() { return applyRecursiveBtn; }
//	public Button getEnableIndexedStylesBtn() { return enableIndexedStylesBtn; }
}
