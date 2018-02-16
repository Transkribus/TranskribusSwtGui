package eu.transkribus.swt_gui.transcription;

//import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.JAXBPageTranscript;
import eu.transkribus.core.model.beans.customtags.AbbrevTag;
import eu.transkribus.core.model.beans.customtags.BlackeningTag;
import eu.transkribus.core.model.beans.customtags.CommentTag;
import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory;
import eu.transkribus.core.model.beans.customtags.CustomTagList;
import eu.transkribus.core.model.beans.customtags.CustomTagUtil;
import eu.transkribus.core.model.beans.customtags.DateTag;
import eu.transkribus.core.model.beans.customtags.GapTag;
import eu.transkribus.core.model.beans.customtags.PersonTag;
import eu.transkribus.core.model.beans.customtags.PlaceTag;
import eu.transkribus.core.model.beans.customtags.ReadingOrderTag;
import eu.transkribus.core.model.beans.customtags.RegionTypeTag;
import eu.transkribus.core.model.beans.customtags.StructureTag;
import eu.transkribus.core.model.beans.customtags.SuppliedTag;
import eu.transkribus.core.model.beans.customtags.TextStyleTag;
import eu.transkribus.core.model.beans.customtags.UnclearTag;
import eu.transkribus.core.model.beans.enums.TranscriptionLevel;
import eu.transkribus.core.model.beans.pagecontent.TextTypeSimpleType;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.core.util.IntRange;
import eu.transkribus.swt.pagingtoolbar.PagingToolBar;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.DropDownToolItem;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.UndoRedoImpl;
import eu.transkribus.swt.util.databinding.DataBinder;
import eu.transkribus.swt_gui.canvas.CanvasKeys;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidgetView;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.metadata.CustomTagSpec;
import eu.transkribus.swt_gui.transcription.WordGraphEditor.EditType;
import eu.transkribus.swt_gui.transcription.WordGraphEditor.WordGraphEditData;
import eu.transkribus.swt_gui.transcription.autocomplete.StyledTextContentAdapter;
import eu.transkribus.swt_gui.transcription.autocomplete.TrpAutoCompleteField;
import eu.transkribus.swt_gui.util.DropDownToolItemSimple;
import eu.transkribus.util.Utils;

public abstract class ATranscriptionWidget extends Composite{
	public static enum WritingOrientation {
		LEFT_TO_RIGHT,
		RIGHT_TO_LEFT;
	}
	
	private final static Logger logger = LoggerFactory.getLogger(ATranscriptionWidget.class);
	protected final static LocalResourceManager fontManager = new LocalResourceManager(JFaceResources.getResources());
	
	protected PagingToolBar regionsPagingToolBar;
	protected ToolBar regionsToolbar;
	protected MenuItem fontItem;	
	protected MenuItem showLineBulletsItem;
	protected MenuItem underlineTextStyleItem;
	
	protected MenuItem leftAlignmentItem;
	protected MenuItem centerAlignmentItem;
	protected MenuItem rightAlignmentItem;
	
//	protected DropDownToolItemSimple viewSetsDropDown;
	
	protected ToolItem writingOrientationItem;
	protected MenuItem showControlSignsItem;
	
	protected MenuItem centerCurrentLineItem;
//	protected DropDownToolItem textStyleDisplayOptions;
	protected MenuItem textStyleDisplayOptions;
	protected MenuItem renderFontStyleTypeItem, renderTextStylesItem, renderOtherStyleTypeItem, renderTagsItem;
	
	protected MenuItem focusShapeOnDoubleClickInTranscriptionWidgetItem;
	
	private DropDownToolItem deleteTextDropDown;
	protected MenuItem deleteRegionTextItem;
	protected MenuItem deleteLineTextItem;
	protected MenuItem deleteWordTextItem;
	
	protected ToolItem undoItem, redoItem;
	
	protected MenuItem autocompleteToggle;
	
	// additional characters:
	protected ToolItem longDash;
	protected ToolItem notSign;
	
	protected List<ToolItem> additionalToolItems = new ArrayList<>();

	protected StyledText text;
//	protected TagPropertyEditor tagPropertyEditor;
//	protected int textAlignment = SWT.LEFT;
	
	protected TrpTextRegionType currentRegionObject=null;
//	protected TrpRegionType currentRegionObject=null;
	protected TrpTextLineType currentLineObject=null;
	protected TrpWordType currentWordObject=null;
	protected TrpAutoCompleteField autocomplete;
	
	// for word graph editor:
	protected SashForm container;
//	protected SashForm horizontalSf;
	
	protected WordGraphEditor wordGraphEditor;
	protected ToolItem showWordGraphEditorItem, reloadWordGraphEditorItem, enableCattiItem;
	
	// Listener:	
	protected List<MouseListener> mouseListener = new ArrayList<>();
	protected List<LineStyleListener> lineStyleListener = new ArrayList<>();
	protected List<CaretListener> caretListener = new ArrayList<>();
	protected List<ModifyListener> modifyListener = new ArrayList<>();
	protected List<ExtendedModifyListener> extendedModifyListener = new ArrayList<>();
	protected List<VerifyListener> verifyListener = new ArrayList<>();
	protected List<VerifyKeyListener> verifyKeyListener = new ArrayList<>();
	protected SelectionAdapter textAlignmentSelectionAdapter;
	
	protected int lastKeyCode=0;
	protected long lastDefaultSelectionEventTime = 0;
		
//	protected List<StyleRange> styleRanges = new ArrayList<StyleRange>();
	
	protected final static int DEFAULT_FONT_SIZE = 20;
	protected TrpSettings settings;
	protected TrpMainWidgetView view;
	
	protected Point oldTextSelection=new Point(-1, -1);
	protected Point contextMenuPoint=null;
	protected Menu contextMenu = null;
	protected MenuItem deleteTagMenuItem;
	protected Menu deleteTagMenu;
	protected MenuItem addCommentTagMenuItem;
	
	protected UndoRedoImpl undoRedo;
	
//	protected DropDownToolItem transcriptionTypeItem;
	protected MenuItem transcriptionTypeMenuItem;
	protected MenuItem transcriptionTypeLineBasedItem;
	protected MenuItem transcriptionTypeWordBasedItem;
	
//	protected WritingOrientation writingOrientation = WritingOrientation.LEFT_TO_RIGHT;
	protected ExtendedModifyListener rightToLeftModifyListener;
	protected ToolItem addParagraphItem;
	protected ToolItem vkItem;
		
	// some consts:
	public static final int DEFAULT_LINE_SPACING=6;
	public static final int TAG_LINE_WIDTH = 2;
	public static final int SPACE_BETWEEN_TAG_LINES = 1;
	
	public static final boolean USE_AUTOCOMPLETE_FROM_PAGE=true;
	
	List<ITranscriptionWidgetListener> listener = new ArrayList<>(); // custom event listener
//	private DropDownToolItem alignmentDropDown;
	private MenuItem alignmentMenuItem;
	private DropDownToolItemSimple transcriptSetsDropDown;
	private MenuItem toolBarOnTopItem;
	private MenuItem focusShapesAccordingToTextAlignmentItem;
	
	ToolItem boldTagItem;
	ToolItem italicTagItem;
	ToolItem subscriptTagItem;
	ToolItem superscriptTagItem;
	ToolItem underlinedTagItem;
	ToolItem strikethroughTagItem;
	MenuItem serifItem, monospaceItem, reverseVideoItem, smallCapsItem, letterSpacedItem;
	
//	ToolItem showTagEditorItem;
	
	public final static String CATTI_MESSAGE_EVENT="CATTI_MESSAGE_EVENT";
	private static final boolean SHOW_WORD_GRAPH_STUFF = false;
	
	private class ReloadWgRunnable implements Runnable {
		Storage store;
		boolean fromCache;
		
		public ReloadWgRunnable(boolean fromCache) {
			this.store = Storage.getInstance();
			this.fromCache = fromCache;
		}
		
		@Override public void run() {
			try {
				if (currentLineObject == null || !store.isDocLoaded() || !store.isPageLoaded()) {
					return;
				}
				
				final int docId = store.getDoc().getId();
				final int pNr = store.getPage().getPageNr();
				final String lineId = currentLineObject.getId();

				final String[][] wgMat = store.getWordgraphMatrix(fromCache, docId, pNr, lineId);
				logger.debug("loaded word graph matrix of size: " + wgMat.length + " x " + (wgMat.length > 0 ? wgMat[0].length : 0));
				
				if  (false) // debug output of wg-matrix
				for (int i = 0; i < wgMat.length; ++i) {
					System.out.print("line " + i+", l = " + wgMat[i].length+": ");
					for (int j = 0; j < wgMat[i].length; ++j) {
						System.out.print(wgMat[i][j] + "\t");
						// logger.debug("line "+i+": "+wgMat[i][j]+"\t");
					}
					System.out.println();
				}

				getDisplay().asyncExec(new Runnable() {
					@Override public void run() {
						if (currentLineObject != null && store.isDocLoaded() && store.isPageLoaded()) {
							if (docId == store.getDoc().getId() && pNr == store.getPage().getPageNr() && lineId.equals(currentLineObject.getId())) {
								wordGraphEditor.setWordGraphMatrix(currentLineObject.getUnicodeText(), wgMat, fromCache);
								text.setFocus();
								addAutocompleteProposals(wgMat);
							}
						}
					}
				});
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	};	
	
	public ATranscriptionWidget(Composite parent, int style, TrpSettings settings, TrpMainWidgetView view) {
		super(parent, style);
		this.view = view;
		
		GridLayout l = new GridLayout(1, true);
		l.marginTop = 0;
		l.marginBottom = 0;
		l.marginLeft = 0;
		l.marginRight = 0;
		l.marginHeight = 0;
		l.marginWidth = 0;
		
		setLayout(l);

//		setLayout(new FillLayout());
//		FillLayout f = new FillLayout();
//		f.type = SWT.VERTICAL;
//		setLayout(f);
		
		this.settings = settings;
				
//		initToolBar();
//		regionsToolbar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		container = new SashForm(this, SWT.VERTICAL);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		container.setLayout(new GridLayout(1, false));
		
//		horizontalSf = new SashForm(container, SWT.HORIZONTAL);
//		horizontalSf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		horizontalSf.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		
//		tagPropertyEditor = new TagPropertyEditor(horizontalSf, this, true);
//		tagPropertyEditor.setLayoutData(new GridData(GridData.FILL_BOTH));
//		tagPropertyEditor.setCustomTag(null);
		
//		transcriptionTaggingWidget = new TranscriptionTaggingWidget(horizontalSf, 0, this);
//		transcriptionTaggingWidget.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		text = new StyledText(container, SWT.BORDER | SWT.VERTICAL | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
		text.setDoubleClickEnabled(false); // disables default doubleclick and(!) tripleclick behaviour --> for new implementation see mouse listener!
		text.setLineSpacing(DEFAULT_LINE_SPACING);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		// FIXME: enabling BIDI processing triggers a bug in StyledText.getOffsetAtPoint -> Layout.getLineIndex -> IllegalArgumentException
		// quick and dirty solution: added a try/catch block to the main event loop of TrapMainWidget.show method that prints out and ignores unexpected exceptions!
		BidiUtils.applyBidiProcessing(text, BidiUtils.AUTO); 

		wordGraphEditor = new WordGraphEditor(container, SWT.NONE, this);
		wordGraphEditor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
//		GridData gd_styledText = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
//		gd_styledText.widthHint = 608;
//		text.setLayoutData(gd_styledText);
				
		setFontFromSettings();
				
		// autocomplete field:
		autocomplete = new TrpAutoCompleteField(text, 
				new StyledTextContentAdapter(text), new String[]{}, 
				KeyStroke.getInstance(SWT.CTRL, SWT.SPACE), null
				);
		autocomplete.getAdapter().setEnabled(false);
		
		initToolBar();
		regionsToolbar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		initListener();
		
		// TEST RIGHT TO LEFT WRITING
		rightToLeftModifyListener = new ExtendedModifyListener() {
			@Override public void modifyText(ExtendedModifyEvent event) {
				text.setCaretOffset(event.start);
			}
		};
		
//		text.setOrientation(SWT.RIGHT_TO_LEFT);
////		text.setTextDirection(SWT.RIGHT_TO_LEFT);
//		text.addExtendedModifyListener(new ExtendedModifyListener() {
//			@Override public void modifyText(ExtendedModifyEvent event) {
//				text.setCaretOffset(event.start);
//			}
//		});
		

		////////////////////////
		
//		setTaggingEditorVisiblity(settings.isShowTextTagEditor());
		setWordGraphEditorVisibility(false);
		
		undoRedo = new UndoRedoImpl(text);
		
		updateData(null, null, null);
		
		initContextMenu();
		
		moveToolBar(settings.getTranscriptionToolbarOnTop());
	}
	
	public void addListener(ITranscriptionWidgetListener l) {
		listener.add(l);
	}
	
	public boolean removeListener(ITranscriptionWidgetListener l) {
		return listener.remove(l);
	}
	
	public List<ITranscriptionWidgetListener> getListener() {
		return listener;
	}
	
	protected void updateWritingOrientation() {
		if (writingOrientationItem == null)
			return;
		
		logger.debug("updating writing orientation: "+writingOrientationItem.getSelection());
		
		if (getWritingOrientation() == WritingOrientation.LEFT_TO_RIGHT) {
			text.removeExtendedModifyListener(rightToLeftModifyListener);
			
			settings.setTextAlignment(SWT.LEFT);
			leftAlignmentItem.setSelection(true);
			rightAlignmentItem.setSelection(false);
		} else if (getWritingOrientation() == WritingOrientation.RIGHT_TO_LEFT) {
			text.addExtendedModifyListener(rightToLeftModifyListener);
			
			settings.setTextAlignment(SWT.RIGHT);
			leftAlignmentItem.setSelection(false);
			rightAlignmentItem.setSelection(true);			
		}
		
		sendDefaultSelectionChangedSignal(false);
		redrawText(true);
	}
	
	protected WritingOrientation getWritingOrientation() {
		return writingOrientationItem!=null && writingOrientationItem.getSelection() ? WritingOrientation.RIGHT_TO_LEFT : WritingOrientation.LEFT_TO_RIGHT;
	}
	
	public abstract TranscriptionLevel getType();
	
	public abstract boolean selectCustomTag(CustomTag t);
	
	public void addAutocompleteProposals(JAXBPageTranscript transcript) {
		if (!USE_AUTOCOMPLETE_FROM_PAGE)
			return;
		
		if (transcript != null) {
			String pageText = transcript.getPage().getUnicodeText();
			autocomplete.addProposals(pageText.split("\\s+"));
		}
	}
	
	public void addAutocompleteProposals(String[][] wgMat) {	
		ArrayList<String> wordList = new ArrayList<String>();
		if (wgMat != null) {
			for (int i=0; i<wgMat.length; ++i) {
				for (int j=0; j<wgMat[i].length; ++j) {
					wordList.add(wgMat[i][j]);
				}
			}
		}
		
		autocomplete.addProposals(wordList.toArray(new String[wordList.size()]));
	}
	
	public void clearAutocompleteProposals() {
		autocomplete.setProposals(new String[]{});
	}
	
//	protected boolean isWordGraphEditorVisible() {
//		return container.getParent() == this;
//	}
	
//	public boolean isTagEditorVisible() {
//		return showTagEditorItem.getSelection();
//	}
			
	protected void setWordGraphEditorVisibility(boolean visible) {		
		logger.debug("setWordGraphEditorVisibility: "+visible);
		
		container.setWeights(new int[] { 50, 50 });
		
		if (true) // set to false to show wge always for debugging purposes
		if (!visible) {
			container.setMaximizedControl(text);
		} else {
			container.setMaximizedControl(null);
			reloadWordGraphMatrix(false);
		}
				
		SWTUtil.setEnabled(reloadWordGraphEditorItem, visible);
		
		logger.trace("wged size: "+wordGraphEditor.getSize());
	}
	
	/** Returns ranges of (start, end) indices that indicate line wrappings */
	protected List<Pair<Integer, Integer>> getWrapRanges(final int start, final int end) {
		List<Pair<Integer, Integer>> ranges = new ArrayList<>();
		int currentY = text.getLocationAtOffset(start).y;
		if ((end-start)<=1 || currentY == text.getLocationAtOffset(end).y) {
			ranges.add(Pair.of(start, end));
			return ranges;
		}
		
		// at this point: (end-start) >= 2
		int s=start;
		for (int i=start+2; i<=end; ++i) {
			int y = text.getLocationAtOffset(i).y;
			if (y != currentY) {
				currentY = y;
				ranges.add(Pair.of(s, i-1));
				s = i;
			}
		}
		ranges.add(Pair.of(s, end));
		
		return ranges;
	}
	
	protected int getMaxLineTextHeight(int lineIndex) {
		if (lineIndex < 0 || lineIndex >= text.getLineCount())
			return -1;
		String lineText = text.getLine(lineIndex);
		if (lineText.isEmpty())
			return -1;
		
		GC gc = new GC(text);
		int lo = text.getOffsetAtLine(lineIndex);
		
		int maxHeight = -1;
		StyleRange[] srs = text.getStyleRanges(lo, lineText.length());
		for (StyleRange sr : srs) {							
			gc.setFont(sr.font);
			String textRange = text.getTextRange(sr.start, sr.length);
			Point te = gc.textExtent(textRange); // text extent of text in range
			
			// compute max-height:
			int height = te.y+Math.abs(sr.rise);
			if (height > maxHeight) {
				maxHeight = height;
			}
		}
		gc.dispose();
		
		return maxHeight;
	}
	
	private void setFontFromSettings() {
		if (false)
			return;
		
		FontData fd = new FontData();
		
		logger.debug("settings font name: '"+settings.getTranscriptionFontName()
				+"', size:  "+settings.getTranscriptionFontSize()+", style: "+settings.getTranscriptionFontStyle());
		
//		fd.setName(Fonts.getSystemFontName(false, false, false));
//		if (settings.getTranscriptionFontName()==null || settings.getTranscriptionFontName().isEmpty()) {
//			fd.setName(Fonts.getSystemFontName(false, false, false));	
//		} else
//			fd.setName(settings.getTranscriptionFontName());
		
		fd.setName(settings.getTranscriptionFontName());
		fd.setHeight(settings.getTranscriptionFontSize());
		fd.setStyle(settings.getTranscriptionFontStyle());
		
		logger.debug("font name = "+fd.getName());
		
		Font globalTextFont = Fonts.createFont(fd);
		text.setFont(globalTextFont);
	}
	
	public void moveToolBar(boolean top) {
		logger.debug("moveToolBar: "+top);
		if (top) {
			regionsToolbar.moveAbove(container);
		} else {
			regionsToolbar.moveBelow(container);
		}

		logger.trace("layouting...");
		this.layout(true);
	}
	
	protected void initToolBar() {
//		regionsPagingToolBar = new PagingToolBar("Region: ", true, true, true, this, SWT.FLAT | SWT.BOTTOM);
		regionsPagingToolBar = new PagingToolBar("Region: ", true, true, true, this, /*SWT.FLAT |*/ SWT.BOTTOM);
		regionsPagingToolBar.removeReloadButton();
		regionsPagingToolBar.removeDoubleButtons();
		regionsToolbar = regionsPagingToolBar.getToolBar();
				
		new ToolItem(regionsToolbar, SWT.SEPARATOR);
				
//		initViewSetsDropDown();
//		new ToolItem(regionsToolbar, SWT.SEPARATOR);
						
		if (false) { // obsolete button -> remove in later version!
		writingOrientationItem = new ToolItem(regionsToolbar, SWT.CHECK);
//		writingOrientationItem.setImage(Images.getOrLoad("/icons/text_align_right.png"));
		writingOrientationItem.setImage(Images.ARROW_LEFT);
		writingOrientationItem.setToolTipText("Toggle to write from right to left");
//		writingOrientationItem.setSelection(writingOrientation == WritingOrientation.RIGHT_TO_LEFT);
		writingOrientationItem.setSelection(false);
		additionalToolItems.add(writingOrientationItem);
		}
						
//		tagsToolItem = new DropDownToolItem(regionsToolbar, false, false, SWT.PUSH);
//		tagsToolItem.ti.setText("Tags");
//		tagsToolItem.ti.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				if (e.detail == SWT.ARROW) { // menu opened
//					updateTagsUnderCaretOffset(); // update tags under current offset on opening menu
//				} else { // item selected
//					
//				}
//			}
//		});
		
//		showPlainTextItem = new ToolItem(regionsToolbar, SWT.CHECK);
//		showPlainTextItem.setImage(Images.getOrLoad("/icons/paintbrush.png"));
//		showPlainTextItem.setToolTipText("Show text plain or with text styles");
//		showPlainTextItem.setSelection(true);
//		showPlainTextItem.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				setLineStyleRanges();
//				text.redraw();
//			}
//		});
		
//		new ToolItem(regionsToolbar, SWT.SEPARATOR);
		
		deleteTextDropDown = new DropDownToolItem(regionsToolbar, false, true, true, SWT.PUSH);
		deleteTextDropDown.ti.setImage(Images.TEXT_FIELD_DELETE);
		
		deleteRegionTextItem = deleteTextDropDown.addItem("Delete region text", Images.TEXT_FIELD_DELETE, "Delete text of region, line or word");
		deleteLineTextItem = deleteTextDropDown.addItem("Delete line text", Images.TEXT_FIELD_DELETE, "Delete text of region, line or word");
		deleteWordTextItem = deleteTextDropDown.addItem("Delete word text", Images.TEXT_FIELD_DELETE, "Delete text of region, line or word");
		additionalToolItems.add(deleteTextDropDown.ti);
			
//		new ToolItem(regionsToolbar, SWT.SEPARATOR);
				
//		new ToolItem(regionsToolbar, SWT.SEPARATOR);
		
		longDash = new ToolItem(regionsToolbar, SWT.PUSH);
		longDash.setText("\u2014");
		longDash.setToolTipText("Inserts a long dash ('Geviertstrich')");
		additionalToolItems.add(longDash);
		
		notSign = new ToolItem(regionsToolbar, SWT.PUSH);
		notSign.setText("\u00AC");
		notSign.setToolTipText("Inserts an angled dash (not sign) e.g. as a dash at the end of a line");
		additionalToolItems.add(notSign);
		
		addParagraphItem = new ToolItem(regionsToolbar, SWT.CHECK);
		addParagraphItem.setText("+ \u00B6");
		addParagraphItem.setToolTipText("Toggle paragraph on selected line");
		additionalToolItems.add(addParagraphItem);
		
		vkItem = new ToolItem(regionsToolbar, SWT.PUSH);
//		vkItem.setText("Virtual keyboards");
		vkItem.setImage(Images.KEYBOARD);
		vkItem.setToolTipText("Virtual keyboards");
		vkItem.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				for (ITranscriptionWidgetListener l : listener)
					l.onVkItemPressed();
			}
		});
		
		new ToolItem(regionsToolbar, SWT.SEPARATOR);
		
		initTaggingToolbar();
		
		new ToolItem(regionsToolbar, SWT.SEPARATOR);
		
		undoItem = new ToolItem(regionsToolbar, SWT.PUSH);
		undoItem.setImage(Images.getOrLoad("/icons/arrow_undo.png"));
		undoItem.setToolTipText("Undo last text change (ctrl + z)");
		additionalToolItems.add(undoItem);
		
		redoItem = new ToolItem(regionsToolbar, SWT.PUSH);
		redoItem.setImage(Images.getOrLoad("/icons/arrow_redo.png"));
		redoItem.setToolTipText("Redo last undone text change (ctrl + y)");
		additionalToolItems.add(redoItem);
		
		new ToolItem(regionsToolbar, SWT.SEPARATOR);
		
		transcriptSetsDropDown = new DropDownToolItemSimple(regionsToolbar, SWT.PUSH, "", Images.WRENCH, "Transcription settings...");
		additionalToolItems.add(transcriptSetsDropDown.getToolItem());
		initTranscriptionSetsDropDownItems(transcriptSetsDropDown);
		
		if (SHOW_WORD_GRAPH_STUFF && getType() == TranscriptionLevel.LINE_BASED) {
			new ToolItem(regionsToolbar, SWT.SEPARATOR);
			
			showWordGraphEditorItem = new ToolItem(regionsToolbar, SWT.CHECK);
			showWordGraphEditorItem.setImage(null);
			showWordGraphEditorItem.setText("HTR suggestions");
			showWordGraphEditorItem.setToolTipText("Toggle visibility of the suggestion editor using HTR results - green line bullets indicate that an HTR result is available for the line!");
			additionalToolItems.add(showWordGraphEditorItem);
			
			reloadWordGraphEditorItem = new ToolItem(regionsToolbar, SWT.NONE);
			reloadWordGraphEditorItem.setImage(Images.getOrLoad("/icons/refresh.gif"));
			reloadWordGraphEditorItem.setToolTipText("Reload wordgraph editor");
			additionalToolItems.add(reloadWordGraphEditorItem);			
			
//			if (false) {
			enableCattiItem = new ToolItem(regionsToolbar, SWT.CHECK);
			enableCattiItem.setImage(null);
			enableCattiItem.setText("CATTI");
			enableCattiItem.setToolTipText("Enables the CATTI server suggestion mode");
//			enableCattiItem.setSelection(true);
			additionalToolItems.add(enableCattiItem);
//			}

		}
	}
	
	private void initTaggingToolbar() {
		boldTagItem = new ToolItem(regionsToolbar, SWT.PUSH);
		boldTagItem.setImage(Images.getOrLoad("/icons/text_bold.png"));
		boldTagItem.setToolTipText("Tag text as bold");
		boldTagItem.setData(TextStyleTag.getBoldTag());
		boldTagItem.addSelectionListener(new TagItemListener());
		additionalToolItems.add(boldTagItem);
		
		italicTagItem = new ToolItem(regionsToolbar, SWT.PUSH);
		italicTagItem.setImage(Images.getOrLoad("/icons/text_italic.png"));
		italicTagItem.setToolTipText("Tag as italic");
		italicTagItem.setData(TextStyleTag.getItalicTag());
		italicTagItem.addSelectionListener(new TagItemListener());
		additionalToolItems.add(italicTagItem);
		
		subscriptTagItem = new ToolItem(regionsToolbar, SWT.PUSH);
		subscriptTagItem.setImage(Images.getOrLoad("/icons/text_subscript.png"));
		subscriptTagItem.setToolTipText("Tag as subscript");
		subscriptTagItem.setData(TextStyleTag.getSubscriptTag());
		subscriptTagItem.addSelectionListener(new TagItemListener());
		additionalToolItems.add(subscriptTagItem);
		
		superscriptTagItem = new ToolItem(regionsToolbar, SWT.PUSH);
		superscriptTagItem.setImage(Images.getOrLoad("/icons/text_superscript.png"));
		superscriptTagItem.setToolTipText("Tag as superscript");
		superscriptTagItem.setData(TextStyleTag.getSuperscriptTag());
		superscriptTagItem.addSelectionListener(new TagItemListener());
		additionalToolItems.add(superscriptTagItem);
		
		underlinedTagItem = new ToolItem(regionsToolbar, SWT.PUSH);
		underlinedTagItem.setImage(Images.getOrLoad("/icons/text_underline.png"));
		underlinedTagItem.setToolTipText("Tag as underlined");
		underlinedTagItem.setData(TextStyleTag.getUnderlinedTag());
		underlinedTagItem.addSelectionListener(new TagItemListener());
		additionalToolItems.add(underlinedTagItem);
		
		strikethroughTagItem = new ToolItem(regionsToolbar, SWT.PUSH);
		strikethroughTagItem.setImage(Images.getOrLoad("/icons/text_strikethrough.png"));
		strikethroughTagItem.setToolTipText("Tag as strikethrough");
		strikethroughTagItem.setData(TextStyleTag.getStrikethroughTag());
		strikethroughTagItem.addSelectionListener(new TagItemListener());
		additionalToolItems.add(strikethroughTagItem);
		
		DropDownToolItemSimple otherTextStyleTags = new DropDownToolItemSimple(regionsToolbar, SWT.PUSH, "...", null, "Other text styles...");
		additionalToolItems.add(otherTextStyleTags.getToolItem());
		serifItem = otherTextStyleTags.addItem("Serif", null, SWT.PUSH);
		serifItem.addSelectionListener(new TagItemListener());
		serifItem.setData(TextStyleTag.getSerifTag());
		
		monospaceItem = otherTextStyleTags.addItem("Monospace", null, SWT.PUSH);
		monospaceItem.addSelectionListener(new TagItemListener());
		monospaceItem.setData(TextStyleTag.getMonospaceTag());
		
		reverseVideoItem = otherTextStyleTags.addItem("Reverse Video", null, SWT.PUSH);
		reverseVideoItem.addSelectionListener(new TagItemListener());
		reverseVideoItem.setData(TextStyleTag.getReverseVideoTag());
		
		smallCapsItem = otherTextStyleTags.addItem("Small Caps", null, SWT.PUSH);
		smallCapsItem.addSelectionListener(new TagItemListener());
		smallCapsItem.setData(TextStyleTag.getSmallCapsTag());
		
		letterSpacedItem = otherTextStyleTags.addItem("Letter Spaced", null, SWT.PUSH);
		letterSpacedItem.addSelectionListener(new TagItemListener());
		letterSpacedItem.setData(TextStyleTag.getLetterSpacedTag());
		
//		new ToolItem(regionsToolbar, SWT.SEPARATOR);
//		showTagEditorItem = new ToolItem(regionsToolbar, SWT.CHECK);
//		showTagEditorItem.setImage(Images.getOrLoad("/icons/tag_blue_edit.png"));
//		showTagEditorItem.setToolTipText("Show/hide embedded tag editor");
//		additionalToolItems.add(showTagEditorItem);
	}
	
	private void initTranscriptionSetsDropDownItems(DropDownToolItemSimple ti) {				
		transcriptionTypeMenuItem = ti.addItem("Transcription level", null, SWT.CASCADE);
		Menu transcriptionTypeMenu = new Menu(ti.getMenu());
		transcriptionTypeMenuItem.setMenu(transcriptionTypeMenu);
		
		transcriptionTypeLineBasedItem = SWTUtil.createMenuItem(transcriptionTypeMenu, "Line based", null, SWT.RADIO);
		transcriptionTypeLineBasedItem.setSelection(true);
		transcriptionTypeLineBasedItem.setData(TranscriptionLevel.LINE_BASED);
		
		transcriptionTypeWordBasedItem= SWTUtil.createMenuItem(transcriptionTypeMenu, "Word based", null, SWT.RADIO);
		transcriptionTypeWordBasedItem.setData(TranscriptionLevel.WORD_BASED);
		
		autocompleteToggle = ti.addItem("Autocomplete (based on text of current transcript)", Images.getOrLoad("/icons/autocomplete.png"), SWT.CHECK);
		
		// old "view" sets...
		
		fontItem = ti.addItem("Change text field font...", Images.getOrLoad("/icons/font.png"), SWT.PUSH);
		showLineBulletsItem = ti.addItem("Show line bullets", Images.getOrLoad("/icons/text_list_numbers.png"), SWT.CHECK);
		underlineTextStyleItem = ti.addItem("Underline styled text", null, SWT.CHECK);
		showControlSignsItem = ti.addItem("\u00B6 Show control signs", null, SWT.CHECK);
		centerCurrentLineItem = ti.addItem("Always try to show a line above and below the selected one", Images.getOrLoad("/icons/arrow_up_down.png"), SWT.CHECK);
		focusShapeOnDoubleClickInTranscriptionWidgetItem = ti.addItem("Focus shape on double-click", Images.getOrLoad("/icons/mouse_focus.png"), SWT.CHECK);
		toolBarOnTopItem = ti.addItem("Display toolbar on top", null, SWT.CHECK);
		focusShapesAccordingToTextAlignmentItem = ti.addItem("Focus shapes according to text alignment", null, SWT.CHECK);
		
		alignmentMenuItem = ti.addItem("Text alignment", Images.getOrLoad("/icons/text_align_left.png"), SWT.CASCADE);
		Menu textAlignmentMenu = new Menu(ti.getMenu());
		alignmentMenuItem.setMenu(textAlignmentMenu);
		leftAlignmentItem = SWTUtil.createMenuItem(textAlignmentMenu, "Left", Images.getOrLoad("/icons/text_align_left.png"), SWT.RADIO);
		centerAlignmentItem = SWTUtil.createMenuItem(textAlignmentMenu, "Center", Images.getOrLoad("/icons/text_align_center.png"), SWT.RADIO);
		rightAlignmentItem = SWTUtil.createMenuItem(textAlignmentMenu, "Right", Images.getOrLoad("/icons/text_align_right.png"), SWT.RADIO);
		if (settings.getTextAlignment() == SWT.LEFT)
			leftAlignmentItem.setSelection(true);
		else if (settings.getTextAlignment() == SWT.RIGHT)
			rightAlignmentItem.setSelection(true);
		else if (settings.getTextAlignment() == SWT.CENTER)
			centerAlignmentItem.setSelection(true);
		else
			leftAlignmentItem.setSelection(true);
		
		textStyleDisplayOptions = ti.addItem("Rendered tag styles", Images.getOrLoad("/icons/paintbrush.png"), SWT.CASCADE);
		Menu textStyleDisplayOptionsMenu = new Menu(ti.getMenu());
		textStyleDisplayOptions.setMenu(textStyleDisplayOptionsMenu);
		renderFontStyleTypeItem = SWTUtil.createMenuItem(textStyleDisplayOptionsMenu, "Font type styles: serif, monospace, letter spaced (will override default font!)", null, SWT.CHECK);
		renderTextStylesItem= SWTUtil.createMenuItem(textStyleDisplayOptionsMenu, "Text style: normal, italic, bold, bold&italic", null, SWT.CHECK);
		renderOtherStyleTypeItem = SWTUtil.createMenuItem(textStyleDisplayOptionsMenu, "Other: underlined, strikethrough, etc.", null, SWT.CHECK);
		renderTagsItem = SWTUtil.createMenuItem(textStyleDisplayOptionsMenu, "Tags: colored underlines for tags", null, SWT.CHECK);		
		
//		autocompleteToggle = new ToolItem(regionsToolbar, SWT.CHECK);
//		autocompleteToggle.setImage(Images.getOrLoad("/icons/autocomplete.png"));
//		autocompleteToggle.setToolTipText("Enable autocomplete");
	}
	
//	private void initViewSetsDropDown() {
//		viewSetsDropDown = new DropDownToolItemSimple(regionsToolbar, SWT.PUSH, "", Images.EYE);
//		viewSetsDropDown.getToolItem().setToolTipText("Viewing settings for the transcription widget");
//		additionalToolItems.add(viewSetsDropDown.getToolItem());
//		
//		
//		
//		fontItem = viewSetsDropDown.addItem("Change text field font...", Images.getOrLoad("/icons/font.png"), SWT.PUSH);
//		showLineBulletsItem = viewSetsDropDown.addItem("Show line bullets", Images.getOrLoad("/icons/text_list_numbers.png"), SWT.CHECK);
//		showControlSignsItem = viewSetsDropDown.addItem("\u00B6 Show control signs", null, SWT.CHECK);
//		centerCurrentLineItem = viewSetsDropDown.addItem("Always try to show a line above and below the selected one", Images.getOrLoad("/icons/arrow_up_down.png"), SWT.CHECK);
//		focusShapeOnDoubleClickInTranscriptionWidgetItem = viewSetsDropDown.addItem("Focus shape on double-click", Images.getOrLoad("/icons/mouse_focus.png"), SWT.CHECK);
//		toolBarOnTopItem = viewSetsDropDown.addItem("Display toolbar on top", null, SWT.CHECK);
//		
//		textAlignment = SWT.LEFT;
//		alignmentMenuItem = viewSetsDropDown.addItem("Text alignment", Images.getOrLoad("/icons/text_align_left.png"), SWT.CASCADE);
//		Menu textAlignmentMenu = new Menu(viewSetsDropDown.getMenu());
//		alignmentMenuItem.setMenu(textAlignmentMenu);
//		leftAlignmentItem = SWTUtil.createMenuItem(textAlignmentMenu, "Left", Images.getOrLoad("/icons/text_align_left.png"), SWT.RADIO);
//		leftAlignmentItem.setSelection(true);
//		centerAlignmentItem = SWTUtil.createMenuItem(textAlignmentMenu, "Center", Images.getOrLoad("/icons/text_align_center.png"), SWT.RADIO);
//		rightAlignmentItem = SWTUtil.createMenuItem(textAlignmentMenu, "Right", Images.getOrLoad("/icons/text_align_right.png"), SWT.RADIO);
//		
//		textStyleDisplayOptions = viewSetsDropDown.addItem("Rendered tag styles", Images.getOrLoad("/icons/paintbrush.png"), SWT.CASCADE);
//		Menu textStyleDisplayOptionsMenu = new Menu(viewSetsDropDown.getMenu());
//		textStyleDisplayOptions.setMenu(textStyleDisplayOptionsMenu);
//		renderFontStyleTypeItem = SWTUtil.createMenuItem(textStyleDisplayOptionsMenu, "Font type styles: serif, monospace, letter spaced (will override default font!)", null, SWT.CHECK);
//		renderTextStylesItem= SWTUtil.createMenuItem(textStyleDisplayOptionsMenu, "Text style: normal, italic, bold, bold&italic", null, SWT.CHECK);
//		renderOtherStyleTypeItem = SWTUtil.createMenuItem(textStyleDisplayOptionsMenu, "Other: underlined, strikethrough, etc.", null, SWT.CHECK);
//		renderTagsItem = SWTUtil.createMenuItem(textStyleDisplayOptionsMenu, "Tags: colored underlines for tags", null, SWT.CHECK);
//	}

	public ToolItem getVkItem() {
		return vkItem;
	}

//	public ToolItem getAutocompleteToggle() { return autocompleteToggle; }
	
//	public DropDownToolItem getTranscriptionTypeItem() { return transcriptionTypeItem; }
	
	public MenuItem getTranscriptionTypeLineBasedItem() { return transcriptionTypeLineBasedItem; }
	public MenuItem getTranscriptionTypeWordBasedItem() { return transcriptionTypeWordBasedItem; }
	
	protected abstract void onTextChangedFromUser(int start, int end, String replacementText);

	public void insertTextIfFocused(String textToInsert) {
		if (currentRegionObject==null || StringUtils.isEmpty(textToInsert)) {
			return;
		}
		
		logger.debug("text orientation: " + text.getOrientation());
//		if (text.isFocCusControl()) {
		text.setFocus();
			text.insert(textToInsert);
			
			if (/*getWritingOrientation()==WritingOrientation.LEFT_TO_RIGHT &&*/ 
					( getType() == TranscriptionLevel.LINE_BASED
				|| ( getType() == TranscriptionLevel.WORD_BASED && !getTranscriptionUnitText().isEmpty() ) ) ) {
//				this.setFocus();
//				text.setSelection(text.getSelection().x+1);
				text.setCaretOffset(text.getCaretOffset()+StringUtils.length(textToInsert));
				
			}
//		}
	}
	
	public void updateToolbarSize() {
//		regionsPagingToolBar.pack(true);
		int width = this.getSize().x;
//		logger.debug("transcript widget width: "+width);
		Point size = regionsToolbar.computeSize(width, SWT.DEFAULT);
//		logger.debug("toolbar size: "+size);
		regionsToolbar.setSize(size);
	}
	
	protected void initListener() {
//		this.addListener(eventType, listener);
		this.addListener(SWT.Resize, new Listener() {
			@Override public void handleEvent(Event e) {
//				pack();
//				layout();
//				container.layout();
				updateToolbarSize();
			}
		});
		
		this.settings.addPropertyChangeListener(new PropertyChangeListener() {
			@Override public void propertyChange(PropertyChangeEvent evt) {
				String pn = evt.getPropertyName();
				if (pn.equals(TrpSettings.RENDER_TAGS)) {
					logger.debug("render tags property changed: "+settings.isRenderTags());
					if (!settings.isRenderTags())
						text.setLineSpacing(DEFAULT_LINE_SPACING);
				}
								
				else if (pn.equals(TrpSettings.TRANSCRIPTION_FONT_NAME_PROPERTY)) {
					setFontFromSettings();
				}
				
				else if (pn.equals(TrpSettings.TRANSCRIPTION_FONT_SIZE_PROPERTY)) {
					setFontFromSettings();
				}
				
				else if (pn.equals(TrpSettings.TRANSCRIPTION_FONT_STYLE_PROPERTY)) {
					setFontFromSettings();
				}
				
				else if (pn.equals(TrpSettings.TRANSCRIPTION_TOOLBAR_ON_TOP_PROPERTY)) {
					moveToolBar(settings.getTranscriptionToolbarOnTop());
				}
				
				else if (pn.equals(TrpSettings.AUTOCOMPLETE_PROPERTY)) {
					autocomplete.getAdapter().setEnabled(settings.isAutocomplete());
				}
				else if (pn.equals(TrpSettings.UNDERLINE_TEXT_STYLES_PROPERTY)) {
					redrawText(true);
				}
				
				// saving on change not needed anymore... gets saved anyway
//				else if (pn.equals(TrpSettings.CENTER_CURRENT_TRANSCRIPTION_LINE_PROPERTY)) {
//					logger.debug("saving settings due to change in "+pn+" property!");
//					TrpConfig.save(TrpSettings.CENTER_CURRENT_TRANSCRIPTION_LINE_PROPERTY);
//				}
//				else if (pn.equals(TrpSettings.SHOW_LINE_BULLETS_PROPERTY)) {
//					redrawText(true);
//					TrpConfig.save(TrpSettings.SHOW_LINE_BULLETS_PROPERTY);
//				}
//				else if (pn.equals(TrpSettings.SHOW_CONTROL_SIGNS_PROPERTY)) {
//					redrawText(true);
//					TrpConfig.save(TrpSettings.SHOW_CONTROL_SIGNS_PROPERTY);
//				}	
				
				updateLineStyles();
				text.redraw();
			}
		});
		
		SelectionAdapter deleteSelection = new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) { 
				if (ATranscriptionWidget.this instanceof WordTranscriptionWidget) {
					if (e.getSource() == deleteRegionTextItem && currentRegionObject != null) {
						currentRegionObject.clearTextForAllWordsinLines(this);
						currentRegionObject.applyTextFromWords();
					} else if (e.getSource() == deleteLineTextItem && currentLineObject != null) {
						currentLineObject.clearTextForAllWords(this);
					} else if (e.getSource() == deleteWordTextItem && currentWordObject != null) {
						currentWordObject.setUnicodeText("", this);
					}
				} else if (ATranscriptionWidget.this instanceof LineTranscriptionWidget) {
					if (e.getSource() == deleteRegionTextItem && currentRegionObject != null) {
						currentRegionObject.clearTextForAllLines(this);
						currentRegionObject.applyTextFromLines();
					} else if (e.getSource() == deleteLineTextItem && currentLineObject != null) {
						currentLineObject.setUnicodeText("", this);
					} else if (e.getSource() == deleteWordTextItem && currentWordObject != null) {
					}
				}
				

			}
		};

		deleteRegionTextItem.addSelectionListener(deleteSelection);
		deleteLineTextItem.addSelectionListener(deleteSelection);
		deleteWordTextItem.addSelectionListener(deleteSelection);
		
		SelectionAdapter insertUnicode = new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				if (e.getSource() instanceof ToolItem) {
					ToolItem ti = (ToolItem) e.getSource();
					
					insertTextIfFocused(ti.getText());
				}
			}
		};
		longDash.addSelectionListener(insertUnicode);
		notSign.addSelectionListener(insertUnicode);
		
//		this.addControlListener(new ControlListener() {
//			@Override
//			public void controlResized(ControlEvent e) {
//				redraw();
//			}
//			
//			@Override
//			public void controlMoved(ControlEvent e) {
//				redraw();
//			}
//		});
		
		initLocalGuiListenerAndBindings();
		initSelectionListener();
		initCaretListener();
		initMouseListener();
		initModifyListener();
		initVerifyListener();
		
		initBaseVerifyKeyListener();
		initVerifyKeyListener();
		initCustomTagPaintListener();
		
		text.addKeyListener(new KeyAdapter() {
			@Override public void keyReleased(KeyEvent e) {
				boolean isCtrl = (e.stateMask & SWT.CTRL) > 0;
				if (isCtrl && e.keyCode == 'a')
					text.selectAll();
			}
		});
		
		
//		text.getHorizontalBar().addSelectionListener(new SelectionListener() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				setLineStyleRanges();
//			}
//			@Override public void widgetDefaultSelected(SelectionEvent e) {
//				setLineStyleRanges();
//			}
//		});
		
		text.getVerticalBar().addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				logger.trace("text field vertical scroll: "+e);
				updateLineStyles();
			}
		});
		
		text.addControlListener(new ControlListener() {
			@Override public void controlResized(ControlEvent e) {
				logger.trace("text field resized: "+e);
				updateLineStyles();
			}
			@Override public void controlMoved(ControlEvent e) {
			}
		});
//		initContextMenu();
		
		undoItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				undoRedo.undo();
			}
		});
		
		redoItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				undoRedo.redo();
			}
		});
		
		initWordGraphListener();
	}
	
	protected void initWordGraphListener() {
		if (getType() == TranscriptionLevel.WORD_BASED)
			return;
		
		if (reloadWordGraphEditorItem != null)
			reloadWordGraphEditorItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					reloadWordGraphMatrix(false);
				}
			});
		
		if (showWordGraphEditorItem != null) {
			showWordGraphEditorItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setWordGraphEditorVisibility(showWordGraphEditorItem.getSelection());
				}
			});
		}
		
		wordGraphEditor.addListener(SWT.Modify, new Listener() {
			@Override public void handleEvent(Event event) {
				logger.debug("modified line in word graph editor: "+event.index+", new text = "+event.text);
				
				WordGraphEditData editData = (WordGraphEditData) event.data;
				EditType type = editData.editType;
				logger.debug("editAtCursor = "+editData.editAtCursor);
				
				if (currentLineObject != null) {
					int lo = text.getOffsetAtLine(currentLineObject.getIndex());
					
					if (editData.editAtCursor) { // replace word at current index
						java.awt.Point wb = Utils.wordBoundary(text.getText(), text.getCaretOffset());
						logger.debug("word boundary: "+wb+" new text: "+event.text);
						text.replaceTextRange(wb.x, wb.y-wb.x, event.text);
					} else { // replace word at given word index
					    // find all word indexes
						String line = currentLineObject.getUnicodeText();
					    Matcher matcher = Pattern.compile("(\\S+)(\\s*)").matcher(line);
					    
					    int i=0;
					    while (matcher.find()) {
					    	if ((type == EditType.REPLACE || type == EditType.DELETE) && i == event.index) {
					    		int start = matcher.start();
					    		int end = matcher.end();
					    		
					    		if (type == EditType.REPLACE) { // when replacing a word, only replace word itself!
					    			end = matcher.end(1);
					    		} else if (type == EditType.DELETE) { // when deleting a word, also delete subsequent spaces!
					    			end = matcher.end(2);
					    		}
					    		
					    		logger.debug("replacing/deleting word: "+matcher.start()+"/"+matcher.end()+" word: '"+matcher.group()+"' replacement: "+event.text);
	//				    		currentLineObject.editUnicodeText(matcher.start(), matcher.end(), event.text, this);
					    		text.replaceTextRange(lo+start, end-start, event.text);
					    		break;
					    	}
					    	else if (type == EditType.ADD && i == event.index-1 ) {
					    		int start = matcher.end(1);
					    		int end = start;
					    		event.text = " "+event.text; // prepend space for insertion
					    		
					    		logger.debug("adding word: "+matcher.start()+"/"+matcher.end()+" word: '"+matcher.group()+"' replacement: "+event.text);
					    		text.replaceTextRange(lo+start, end-start, event.text);
					    		break;
					    	}
					    	else if (type == EditType.ADD && event.index==0 ) {
					    		event.text = event.text+" "; // append space
					    		
					    		logger.debug("adding word: "+matcher.start()+"/"+matcher.end()+" word: '"+matcher.group()+"' replacement: "+event.text);
					    		text.replaceTextRange(lo, 0, event.text);
					    		break;
					    	}				    	
					        ++i;
					    }
					}
				}
			}
		});
		
	}
	
	protected void setLineBulletAndStuff() {
		text.setLineBullet(0, text.getLineCount(), null); // delete line bullet first to guarantee update! (bug in SWT?)
		if (settings.isShowLineBullets() && currentRegionObject!=null && getNTextLines()>0) {
			Storage store = Storage.getInstance();
			for (int i=0; i<text.getLineCount(); ++i) {				
				final int docId = store.getDoc().getId();
				final int pNr = store.getPage().getPageNr();
				
				int bulletFgColor = SWT.COLOR_BLACK;
				
				int fontStyle = SWT.NORMAL;
				if (i>= 0 && i <currentRegionObject.getTextLine().size()) {
					final String lineId = currentRegionObject.getTextLine().get(i).getId();
					boolean hasWg = store.hasWordGraph(docId, pNr, lineId);
					
					fontStyle = (i == getCurrentLineIndex()) ? SWT.BOLD : SWT.NORMAL;	
					bulletFgColor = hasWg ? SWT.COLOR_DARK_GREEN : SWT.COLOR_BLACK;
				}

				StyleRange style = new StyleRange(0, text.getCharCount(), Colors.getSystemColor(bulletFgColor), Colors.getSystemColor(SWT.COLOR_GRAY), fontStyle);
				style.metrics = new GlyphMetrics(0, 0, Integer.toString(text.getLineCount() + 1).length() * 12);
//				style.background = Colors.getSystemColor(SWT.COLOR_GRAY);
				Bullet bullet = new Bullet(/*ST.BULLET_NUMBER |*/ ST.BULLET_TEXT, style);
				bullet.text = ""+(i+1);
				
				text.setLineBullet(i, 1, bullet);
				text.setLineIndent(i, 1, 25);
				text.setLineAlignment(i, 1, settings.getTextAlignment());
				text.setLineWrapIndent(i, 1, 25+style.metrics.width);
			}
			
//			text.setLineBullet(0, text.getLineCount(), bullet);
//			text.setLineIndent(0, text.getLineCount(), 25);
//			text.setLineAlignment(0, text.getLineCount(), textAlignment);
//			text.setLineWrapIndent(0, text.getLineCount(), 25+style.metrics.width);			
			

		}
	}
	
	protected void updateLineStyles() {
		if (!text.isEnabled()) {
			return;
		}
		
		logger.trace("updating line styles!");
		
		// set line bullet:
		setLineBulletAndStuff();
		
		// set global style(s): <-- NO NEED TO DO SO, AS OVERWRITTEN BY setStyleRanges call below!!		
//		StyleRange srDefault = new StyleRange(TrpUtil.getDefaultSWTTextStyle(text.getFont().getFontData()[0], settings));
//		srDefault.start=0;
//		srDefault.length = text.getText().length();
//		text.setStyleRange(srDefault);

		// get specific style ranges for all lines:
		List<StyleRange> allStyles = new ArrayList<>();
		
		if (true)
		for (int i=JFaceTextUtil.getPartialTopIndex(text); i<=JFaceTextUtil.getPartialBottomIndex(text); ++i) { // only for visible lines		
//		for (int i=0; i<text.getLineCount(); ++i) {
			List<StyleRange> styles = getLineStyleRanges(text.getOffsetAtLine(i));			
			allStyles.addAll(styles);
		}
				
		// set style ranges:
		try {
			text.setStyleRanges(allStyles.toArray(new StyleRange[0]));
		} catch (IllegalArgumentException e) {
			logger.error("Could not update line styles - skipping");
		}
		
//		if (true)
//			centerCurrentLine();

		text.redraw();
	}
	
	/** Makes sure that there is always a line above/below the current line if possible */
	private void centerCurrentLine() {
		if (!settings.isCenterCurrentTranscriptionLine())
			return;
		
		int ci = text.getLineAtOffset(text.getCaretOffset());
		int ti = text.getTopIndex();
		int bi = JFaceTextUtil.getBottomIndex(text);
		if ((ci == ti && ci-1>=0) || (ci == bi && ci-1>=0)) {
			text.setTopIndex(ci-1);
			updateLineStyles();
		}
	}
	
	public int getCurrentLineIndex() {
		return (currentLineObject == null) ? -1 : currentLineObject.getIndex();
	}
	
	protected void initSelectionListener() {
		// This listener tracks every possible (hopefully!) change of the selection in the transcription widget 
		Listener ultimateSelectionListener = new Listener() {
	        @Override
	        public void handleEvent(Event e) {
	        	// exit if mouse-move and left button not down (i.e. no selection ongoing!)
	        	if (e.type == SWT.MouseMove && (e.stateMask & SWT.BUTTON1) == 0) {
	        		return;
	        	}
	        	lastDefaultSelectionEventTime = System.currentTimeMillis();
	        	
	        	// update objects
	        	updateLineAndWordObjects();
	        	
	        	if (true) {
		        	// NEW: try to send event only when time diff between events is greater threshold to prevent overkill of signals!
		    		final long DIFF_T = 500;
		    		new Timer().schedule(new TimerTask() {
		    			@Override public void run() {
		        			long selDiff = System.currentTimeMillis() - lastDefaultSelectionEventTime;
		        			logger.trace("sel-diff = "+selDiff);
		        			if (selDiff >= DIFF_T) {
		        				Display.getDefault().asyncExec(new Runnable() {
									@Override public void run() {
										logger.debug("sending default selection changed signal!");
										sendDefaultSelectionChangedSignal(true);
									}
								});
		        			}
		    			}
		    		}, DIFF_T);	
	        	} else {
		        	// OLD:
		        	sendDefaultSelectionChangedSignal(true);
	        	}
	        	
	    		// make sure that there is always a line above/below the current line if possible:
	    		if (true)
	    			centerCurrentLine();
	        }
	    };
	    text.addListener(SWT.KeyUp, ultimateSelectionListener);
	    text.addListener(SWT.MouseUp, ultimateSelectionListener);
	    text.addListener(SWT.MouseMove, ultimateSelectionListener);
	}
		
	protected void initCaretListener() {		
		CaretListener caretListener = new CaretListener() {
			@Override
			public void caretMoved(CaretEvent event) {
				logger.trace("caret moved = "+event.caretOffset+" selection (old) = "+text.getSelection());	
//				updateLineAndWordObjects();
			}
		};
		addUserCaretListener(caretListener);
	}
	
	protected void initMouseListener() {
		addUserTextMouseListener(new MouseAdapter() {
			@Override public void mouseDoubleClick(MouseEvent e) {	
				if (isEnabled() && settings.isFocusShapeOnDoubleClickInTranscriptionWidget()) {
					if (ATranscriptionWidget.this instanceof WordTranscriptionWidget)
						sendFocusInSignal(currentWordObject);
					else
						sendFocusInSignal(currentLineObject);
				}
			}
			@Override public void mouseDown(MouseEvent e) {
//				logger.debug("char count = "+text.getCharCount());
//				if (text.getCharCount()==0)
//					return;
				
				if (e.count == 2) { // double click --> select surrounding word
					java.awt.Point wb = Utils.wordBoundary(text.getText(), text.getCaretOffset());
//					logger.debug("wb = "+wb+" new = "+Utils.wordBoundary(text.getText(), text.getCaretOffset()));
					if (wb.x < wb.y)
						text.setSelection(wb.x, wb.y);
				}
				if (e.count == 3) { // triple click --> select line (without EOL delimiter!)
					int li = text.getLineAtOffset(text.getCaretOffset());
					int start = text.getOffsetAtLine(li);
					int end = start + text.getLine(li).length();
//					logger.debug("start/end = "+start+"/"+end);
					text.setSelection(start, end);
					e.count = 0;
				}
				
//				if (isEnabled()) {
//					updateLineAndWordObjects();
//				}
			}
		});
	}
		
	protected abstract void initModifyListener();
	protected abstract void initVerifyListener();
	
	protected abstract void updateLineObject();
	protected abstract void updateWordObject();
	
	protected void updateLineAndWordObjects() {
		updateLineObject();
		updateWordObject();
		
//		setLineStyleRanges();
	}
	
	public Pair<ITrpShapeType, Integer> getTranscriptionUnitAndRelativePositionFromCurrentOffset() {
		return getTranscriptionUnitAndRelativePositionFromOffset(text.getCaretOffset());
	}
	public abstract Pair<ITrpShapeType, Integer> getTranscriptionUnitAndRelativePositionFromOffset(int offset);
	
	public TrpTextLineType getTextLineAtOffset(int offset) {
		int li = text.getLineAtOffset(offset);
		return getLineObject(li);
	}

	protected int getNTextLines() {
		return currentRegionObject==null ? 0 : currentRegionObject.getTextLine().size();
	}
	
	protected TrpTextLineType getLineObject(int textLineIndex) {
		if (textLineIndex < 0 || textLineIndex >= getNTextLines())
			return null;
		
		return (TrpTextLineType) currentRegionObject.getTextLine().get(textLineIndex);
	}
	
	protected abstract List<StyleRange> getLineStyleRanges(int lineOffset);
	
	protected void initBaseVerifyKeyListener() {
		VerifyKeyListener baseVerifyKeyListener = new VerifyKeyListener() {
			@Override
			public void verifyKey(VerifyEvent e) {
				lastKeyCode = e.keyCode;
				
				if (currentLineObject == null) {
					return;
//					updateLineObject();
				}
				
				// reinterpret enter as arrow down
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					if (CanvasKeys.isShiftKeyDown(e.stateMask)) { // shift and enter -> mark as paragraph!
						toggleParagraphOnSelectedLine();
					}
					
					e.keyCode = SWT.ARROW_DOWN;
					e.doit = false;
					if (CanvasKeys.isCtrlKeyDown(e)) { // if ctrl-enter pressed: focus element, i.e. send mouse double click signal
						if (ATranscriptionWidget.this instanceof WordTranscriptionWidget)
							sendFocusInSignal(currentWordObject);
						else
							sendFocusInSignal(currentLineObject);
					}
					else { // if just enter pressed then jump one line down:
						if (!autocomplete.getAdapter().isProposalPopupOpen()) {
							sendTextKeyDownEvent(SWT.ARROW_DOWN);
						}
					}
					return;
				}
			}
		};
		
		addUserVerifyKeyListener(baseVerifyKeyListener);
		
		
	}
	protected abstract void initVerifyKeyListener();
	protected abstract void onPaintTags(PaintEvent e);
	
	/**
	 * 
	 * @param e
	 * @param ctl The CustomTagList for which the tag markers are drawn
	 * @param offset The character offset for the line of the given CustomTagList ctl
	 */
	protected void paintTagsFromCustomTagList(PaintEvent e, CustomTagList ctl, int offset) {
		Set<String> tagNames = ctl.getIndexedTagNames();
		
		if (!settings.isUnderlineTextStyles()) {
			tagNames.remove(TextStyleTag.TAG_NAME); // do not underline  TextStyleTags, as they are
			// rendered into the bloody text anyway			
		}
		
		// adjust line spacing to fit all tags:
		int spaceForTags = (TAG_LINE_WIDTH + 2*SPACE_BETWEEN_TAG_LINES) * tagNames.size();
		if (spaceForTags > text.getLineSpacing())
			text.setLineSpacing(spaceForTags);

		int j = -1;
		for (String tagName : tagNames) {
			++j;
			logger.trace("tagName = " + tagName + " j = " + j);
			List<CustomTag> tags = ctl.getIndexedTags(tagName);
			for (CustomTag tag : tags) {
				
//				logger.debug("tag = "+tag);
				
				int li = text.getLineAtOffset(offset + tag.getOffset());
				int lo = text.getOffsetAtLine(li);
				int ll = text.getLine(li).length();
				
				StyleRange sr = null;
				int styleOffset = offset + tag.getOffset();
				if (styleOffset>=0 && styleOffset<text.getCharCount())
					sr = text.getStyleRangeAtOffset(offset + tag.getOffset());
				
				// handle special case where a tag is empty and at the end of the line -> sr will be null from the last call in this case --> construct 'artificial' StyleRange!!
				boolean canBeEmptyAndIsAtTheEnd = tag.canBeEmpty() && ( (offset+tag.getOffset()) == (lo+ll));
				if (canBeEmptyAndIsAtTheEnd)
					sr = new StyleRange(offset+tag.getOffset(), 0, null, null);
				
				logger.trace("stylerange at offset: "+sr);
				if (sr == null)
					continue;
				sr.length = tag.getLength();
				
				// since there is a word-wrap, we have to calculate multiple bounds to draw the tag-line correctly:
				// 1: compute bounds:
				List<Rectangle> bounds = new ArrayList<>();
				Rectangle cb = null;
				int co=sr.start;
				for (int k=sr.start; k<sr.start+sr.length; ++k) {
					logger.trace("text: "+text.getText(co, k)+" (s,e)="+co+"/"+k);
					Rectangle b = text.getTextBounds(co, k);
					logger.trace("y = "+b.y+" height = "+b.height);
					if (cb==null)
						cb = b;
					
					if (cb.height!=b.height) {
						bounds.add(new Rectangle(cb.x, cb.y, cb.width, cb.height));
						co = k;
						cb = null;									
					} else
						cb = b;
				}
				if (cb!=null)
					bounds.add(cb);
				
				// 2: draw them bloody bounds:
//				e.gc.setLineStyle(tag.isContinued() ? SWT.LINE_DASH : SWT.LINE_SOLID);
				e.gc.setLineWidth(TAG_LINE_WIDTH);
				Color c = Colors.decode2(CustomTagFactory.getTagColor(tagName));
				if (c == null) {
					c = Colors.getSystemColor(SWT.COLOR_GRAY); // default tag color
				}
				
				e.gc.setForeground(c);
				e.gc.setBackground(c);				
				
				int spacerHeight = TAG_LINE_WIDTH+SPACE_BETWEEN_TAG_LINES;
//				if (tag.canBeEmpty()) {
//					logger.debug("tag = "+tag);
//					logger.debug("bounds size = "+bounds.size());
//					
//				}
				
				if (tag.canBeEmpty() && tag.isEmpty()) {
					logger.trace("drawing empty tag: "+tag);
					Point p = text.getLocationAtOffset(sr.start);
					int lineHeight = text.getLineHeight(sr.start);
					
					logger.trace("line height: "+lineHeight+" point = "+p);
					
					// draw empty tags as vertical bar:
//					Rectangle b1=bounds.get(0);
					e.gc.drawLine(p.x, p.y, p.x, p.y + lineHeight);
					
//					e.gc.drawLine(x1, y1, x2, y2);
					
					e.gc.fillOval(p.x-spacerHeight, p.y, 2*spacerHeight, 2*spacerHeight);
					e.gc.fillOval(p.x-spacerHeight, p.y + lineHeight, 2*spacerHeight, 2*spacerHeight);
					
//					e.gc.drawLine(b.x-spacerHeight, b.y, b.x+spacerHeight, b.y);
//					e.gc.drawLine(b.x-spacerHeight, b.y + b.height, b.x+spacerHeight, b.y + b.height);
				} else {
					for (int k=0; k<bounds.size(); k++) {
						Rectangle b=bounds.get(k);
						logger.trace("bound: "+b);
						if (tag instanceof CommentTag) {
							if (settings.isHighlightComments()) {							
								e.gc.setBackground(Colors.getSystemColor(SWT.COLOR_YELLOW));
								e.gc.setAlpha(70);
								e.gc.fillRoundRectangle(b.x, b.y, b.width, b.height, 2, 2);
							}
						} else {
		//				for (Rectangle b : bounds) {
							int yBottom = b.y + b.height + TAG_LINE_WIDTH / 2 + j * (TAG_LINE_WIDTH + 2*SPACE_BETWEEN_TAG_LINES);
							
							e.gc.drawLine(b.x, yBottom, b.x + b.width, yBottom);
							// draw start and end vertical lines:
							if (k==0 && ctl.getPreviousContinuedCustomTag(tag)==null) {
								e.gc.drawLine(b.x, yBottom+spacerHeight, b.x, yBottom-spacerHeight);
							}
							if (k==bounds.size()-1 && ctl.getNextContinuedCustomTag(tag)==null) {
								e.gc.drawLine(b.x + b.width, yBottom+spacerHeight, b.x + b.width, yBottom-spacerHeight);
							}
						}
					}
				}
			}
		}
	}	
	
	public List<Rectangle> getTagDrawBounds(CustomTag tag, int offset) {
		int li = text.getLineAtOffset(offset + tag.getOffset());
		int lo = text.getOffsetAtLine(li);
		int ll = text.getLine(li).length();
		
		StyleRange sr = null;
		int styleOffset = offset + tag.getOffset();
		if (styleOffset>=0 && styleOffset<text.getCharCount())
			sr = text.getStyleRangeAtOffset(offset + tag.getOffset());
		
		// handle special case where a tag is empty and at the end of the line -> sr will be null from the last call in this case --> construct 'artificial' StyleRange!!
		boolean canBeEmptyAndIsAtTheEnd = tag.canBeEmpty() && ( (offset+tag.getOffset()) == (lo+ll));
		if (canBeEmptyAndIsAtTheEnd)
			sr = new StyleRange(offset+tag.getOffset(), 0, null, null);
		
		logger.trace("stylerange at offset: "+sr);
		if (sr == null)
			return new ArrayList<>();
		
		sr.length = tag.getLength();
		
		// since there is a word-wrap, we have to calculate multiple bounds to draw the tag-line correctly:
		// 1: compute bounds:
		List<Rectangle> bounds = new ArrayList<>();
		Rectangle cb = null;
		int co=sr.start;
		for (int k=sr.start; k<sr.start+sr.length; ++k) {
			logger.trace("text: "+text.getText(co, k)+" (s,e)="+co+"/"+k);
			Rectangle b = text.getTextBounds(co, k);
			logger.trace("y = "+b.y+" height = "+b.height);
			if (cb==null)
				cb = b;
			
			if (cb.height!=b.height) {
				bounds.add(new Rectangle(cb.x, cb.y, cb.width, cb.height));
				co = k;
				cb = null;									
			} else
				cb = b;
		}
		if (cb!=null)
			bounds.add(cb);
		
		return bounds;
	}
	
	protected void initCustomTagPaintListener() {
		text.addPaintListener(new PaintListener() {
			@Override public void paintControl(PaintEvent e) {
				if (settings.isRenderTags()) {
					onPaintTags(e);
				}
				if (settings.isShowControlSigns()) {
					paintControlSigns(e);
				}
			}
		});
	}
	
	/**
	 * Parses through all visible characters and renders control signs (whitespace, tab, end of line, paragraphs)
	 * into the text area without changing the text itself.
	 */
	protected void paintControlSigns(PaintEvent e) {
		int firstLine = JFaceTextUtil.getPartialTopIndex(text);
		int lastLine = JFaceTextUtil.getPartialBottomIndex(text);
		
		int firstCharIndex = text.getOffsetAtLine(firstLine);
		int lastCharIndex = text.getOffsetAtLine(lastLine)+text.getLine(lastLine).length();
		
		logger.trace("firstCharIndex = "+firstCharIndex+" lastCharIndex = "+lastCharIndex);
		
		StyleRange sr = null;
		if (!text.getText().isEmpty())
			sr = text.getStyleRangeAtOffset(0);
		for (int i = firstCharIndex; i<=lastCharIndex-1; ++i) {			
			String charStr = text.getText(i, i);

			StyleRange sr1 = text.getStyleRangeAtOffset(i);
			if (sr1 != null)
				sr = sr1;
						
			logger.trace("charStr = "+charStr);
			String controlChar = null;
			if (charStr.equals(" ")) { // draw whitespace
				final boolean RENDER_WHITESPACE_CONTROL_CHAR_WITH_OVAL = false;
				if (RENDER_WHITESPACE_CONTROL_CHAR_WITH_OVAL) {
					Rectangle r = text.getTextBounds(i, i);
					logger.trace("painting whitespace - b = "+r);				
					e.gc.setBackground(Colors.getSystemColor(SWT.COLOR_RED));
					
					e.gc.fillOval(r.x+r.width/2, r.y+r.height/2, r.width/2, r.width/2);
				} else {
					controlChar = "\u00B7";	
				}
			} else if (charStr.endsWith("\n")) { // draw end of line or paragraph				
				TrpTextLineType line = getTextLineAtOffset(i);
				logger.trace("line = "+line);
				
				if (line != null && CustomTagUtil.hasParagraphStructure(line)) {
					logger.trace("PARAGRAPH CHAR!!");
					controlChar = "\u00B6";	
				} else {
					logger.trace("END OF LINE CHAR!!");
					controlChar = "\u23CE";
				}
			} else if (charStr.equals("\t")) { // draw tab
				logger.trace("TAB CHAR!!");
				controlChar = "\u21A6";
			}
			
			// draw control character if set:
			if (controlChar != null) {
//				e.gc.setBackground(Colors.getSystemColor(SWT.COLOR_WHITE));
				e.gc.setForeground(Colors.getSystemColor(SWT.COLOR_RED));
				
				if (sr != null) {
					e.gc.setFont(sr.font);
				}
				e.gc.setAntialias(SWT.ON);
				
				boolean BOLD_CONTROL_SIGN_FONT = false;
				if (BOLD_CONTROL_SIGN_FONT)
					e.gc.setFont(Fonts.createBoldFont(e.gc.getFont()));
				
				Point p = text.getLocationAtOffset(i);
				logger.trace("point = "+p);
				e.gc.drawText(controlChar, p.x, p.y, true);
			}
			
		}	
	}
	
	protected void initContextMenu() {
		contextMenu = new Menu(text);
		
		deleteTagMenuItem = new MenuItem(contextMenu, SWT.CASCADE);
		deleteTagMenuItem.setImage(Images.DELETE);
		deleteTagMenuItem.setText("Delete...");
		deleteTagMenu = new Menu(deleteTagMenuItem);
		deleteTagMenuItem.setMenu(deleteTagMenu);
		
		addCommentTagMenuItem = new MenuItem(contextMenu, SWT.PUSH);
		addCommentTagMenuItem.setText("Add a comment");
		SWTUtil.onSelectionEvent(addCommentTagMenuItem, e -> {
			TrpMainWidget.getInstance().addCommentForSelection(null);
		});		
		
		contextMenu.addMenuListener(new MenuListener() {
			void deleteDynamicMenuItems() {
				SWTUtil.deleteMenuItems(contextMenu, deleteTagMenuItem, addCommentTagMenuItem);
				SWTUtil.deleteMenuItems(deleteTagMenu);
			}
			
			@Override
			public void menuShown(MenuEvent e) {
				deleteDynamicMenuItems();
				createCustomTagSpecsMenuItems(contextMenu);
				createDeleteTagsMenuItems(deleteTagMenu);
			}
			
			@Override
			public void menuHidden(MenuEvent e) {
//				deleteDynamicMenuItems();
			}
		});
		
		// used to store location of right-click of menu:
		text.addMouseListener(new MouseAdapter() {
			@Override public void mouseDown(MouseEvent e) {
				if (e.button == 3) {
					logger.debug("right mouse down: "+e.x+"/"+e.y);
					contextMenuPoint = new Point(e.x, e.y);
				}
			}
		});
		
		text.setMenu(contextMenu);	
	}
	
	private List<MenuItem> createDeleteTagsMenuItems(Menu menu) {
		List<MenuItem> items = new ArrayList<>();
		MenuItem deleteTagsForCurrentSelection = new MenuItem(menu, 0);
		deleteTagsForCurrentSelection.setText("Tags for current selection");
		SWTUtil.onSelectionEvent(deleteTagsForCurrentSelection, e -> {
			TrpMainWidget.getInstance().deleteTagsForCurrentSelection();
		});
		
		for (CustomTag t : getCustomTagsForCurrentOffset()) {
			MenuItem deleteTagItem = new MenuItem(menu, 0);
			deleteTagItem.setData(t);
			deleteTagItem.setText(t.getCssStr());
			SWTUtil.onSelectionEvent(deleteTagItem, e -> {
				TrpMainWidget.getInstance().deleteTags((CustomTag) deleteTagItem.getData());
			});
		}
		
		return items;
	}
	
	private List<MenuItem> createCustomTagSpecsMenuItems(Menu menu) {
		List<MenuItem> items = new ArrayList<>();
		
		// create menu items for tag specs:
		for (CustomTagSpec spec : Storage.getInstance().getCustomTagSpecs()) {
			MenuItem tagItem = new MenuItem(menu, SWT.PUSH);
			tagItem.setText(spec.getCustomTag().getCssStr());
			tagItem.addSelectionListener(new TagSpecMenuItemListener(spec));
			items.add(tagItem);
		}
		
		// create sub menu for all tags:
		MenuItem allTagsItem = new MenuItem(contextMenu, SWT.CASCADE);
		allTagsItem.setText("All tags");
		Menu allTagsMenu = new Menu(allTagsItem);
		allTagsItem.setMenu(allTagsMenu);
		items.add(allTagsItem);
		for (String tagName: CustomTagFactory.getRegisteredTagNamesSorted()) {
			if(!tagName.equals(ReadingOrderTag.TAG_NAME) && !tagName.equals(RegionTypeTag.TAG_NAME) && !tagName.equals(StructureTag.TAG_NAME)
					&& !tagName.equals(TextStyleTag.TAG_NAME)) {
				MenuItem tagItem = new MenuItem(allTagsMenu, SWT.NONE);
				tagItem.setText(tagName);
				tagItem.addSelectionListener(new TagSpecMenuItemListener(tagName));
			}
		}
		
		return items;
	}
	
	class TagSpecMenuItemListener extends SelectionAdapter {
		CustomTagSpec cTagSpec;
		String tagName;
		
		public TagSpecMenuItemListener(CustomTagSpec cTagSpec) {
			logger.trace("TagSpec: "+cTagSpec);
			this.cTagSpec = cTagSpec;
		}
		
		public TagSpecMenuItemListener(String tagName) {
			this.tagName = tagName;
		}
		
		@Override
		public void widgetSelected(SelectionEvent e) {
			logger.debug("add tag spec: "+cTagSpec);
			if (cTagSpec != null) {
				TrpMainWidget.getInstance().addTagForSelection(cTagSpec.getCustomTag(), null);
			}
			else if (!StringUtils.isEmpty(tagName)) {
				TrpMainWidget.getInstance().addTagForSelection(tagName, null, null);
			}
		}
	}

	/**
	 * add a tag item menu to choose tags on a mouse right click
	 * @deprecated
	 */
	private void addTagItems() {

		ArrayList<String> tagnamesRest = new ArrayList<String>();
		
		MenuItem tagItem1;
		MenuItem tagItem2;
//		tagItem.setMenu(tagMenu);
		for (String tmp : CustomTagFactory.getRegisteredTagNamesSorted()){
			
			if (tmp.equals(AbbrevTag.TAG_NAME) || tmp.equals(PersonTag.TAG_NAME) || tmp.equals(PlaceTag.TAG_NAME) || 
					tmp.equals(DateTag.TAG_NAME) || tmp.equals(BlackeningTag.TAG_NAME) || tmp.equals(UnclearTag.TAG_NAME) || 
					tmp.equals(CommentTag.TAG_NAME) || tmp.equals(GapTag.TAG_NAME) || tmp.equals(SuppliedTag.TAG_NAME)){
				tagItem1 = new MenuItem(contextMenu, SWT.NONE);
				tagItem1.setText(tmp);
				tagItem1.addSelectionListener(new MenuItemListener());
			}
			else {
				tagnamesRest.add(tmp);
			}

		}

		final MenuItem tagItem = new MenuItem(contextMenu, SWT.CASCADE);
		tagItem.setText("Further Tags");
		
		final Menu tagMenu = new Menu(tagItem);
		tagItem.setMenu(tagMenu);
		
		for (String rest : tagnamesRest){
			if(!rest.equals(ReadingOrderTag.TAG_NAME) && !rest.equals(RegionTypeTag.TAG_NAME) && !rest.equals(StructureTag.TAG_NAME)
					&& !rest.equals(TextStyleTag.TAG_NAME)){
				tagItem2 = new MenuItem(tagMenu, SWT.NONE);
				tagItem2.setText(rest);
				tagItem2.addSelectionListener(new MenuItemListener());
			}
		}
	}
	
	class TagItemListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent event) {
			logger.debug("TagItemListener, widget = "+event.widget);
			
			if (event.widget == null)
				return;
						
			if (!(event.widget.getData() instanceof CustomTag)) {
				logger.debug("no CustomTag as data!");
				return;
			}
			
			CustomTag tOrig = (CustomTag) event.widget.getData();
			CustomTag t = tOrig.copy(); // copy stored tag, s.t. values do not get messed up
			
			logger.debug("adding tag for current selection: "+t);
		
			String addOnlyThisProperty = null;
			
			// merge existing text-styles of current selection into the tag
			if (t instanceof TextStyleTag) {
				if (event.widget == boldTagItem)
					addOnlyThisProperty = "bold";
				else if (event.widget == italicTagItem)
					addOnlyThisProperty = "italic";
				else if (event.widget == subscriptTagItem)
					addOnlyThisProperty = "subscript";
				else if (event.widget == superscriptTagItem)
					addOnlyThisProperty = "superscript";
				else if (event.widget == underlinedTagItem)
					addOnlyThisProperty = "underlined";
				else if (event.widget == strikethroughTagItem)
					addOnlyThisProperty = "strikethrough";
				else if (event.widget == serifItem)
					addOnlyThisProperty = "serif";
				else if (event.widget == monospaceItem)
					addOnlyThisProperty = "monospace";
				else if (event.widget == reverseVideoItem)
					addOnlyThisProperty = "reverseVideo";
				else if (event.widget == smallCapsItem)
					addOnlyThisProperty = "smallCaps";
				else if (event.widget == serifItem)
					addOnlyThisProperty = "letterSpaced";
				
				// try to "invert" property if it is already set:
				if (addOnlyThisProperty != null) {
					TextStyleTag textStyleForCurrentSelection = getCommonIndexedCustomTagForCurrentSelection(TextStyleTag.TAG_NAME);
					logger.debug("textStyleForCurrentSelection = "+textStyleForCurrentSelection);
					if (textStyleForCurrentSelection != null) {
						try {
							String p = BeanUtils.getSimpleProperty(textStyleForCurrentSelection, addOnlyThisProperty);
							if (StringUtils.equals(p, "true")) {
								logger.debug("inverting '"+addOnlyThisProperty+"' property");
								try {
									BeanUtils.setProperty(t, addOnlyThisProperty, false);
								} catch (IllegalAccessException | InvocationTargetException e) {
									logger.error(e.getMessage(), e);
								}								
							}
						} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
							logger.error(e.getMessage(), e);
						}
					}
				} // end addOnlyThisProperty != null
			}

			TrpMainWidget.getInstance().addTagForSelection(t, addOnlyThisProperty);
	    } // end widgetSelected
	}
	
	/**
	 * right click listener for the transcript table
	 * for the latest transcript the new status can be set with the right click button and by choosing the new status
	 * @deprecated
	 */
	class MenuItemListener extends SelectionAdapter {
	    public void widgetSelected(SelectionEvent event) {
	    	logger.debug("You selected " + ((MenuItem) event.widget).getText());
	    		    	
	    	String tagname = ((MenuItem) event.widget).getText();
	    	try {

	    		TrpMainWidget mw = TrpMainWidget.getInstance();
	    		
	    		boolean isTextSelectedInTranscriptionWidget = mw.isTextSelectedInTranscriptionWidget();
	    		if (!isTextSelectedInTranscriptionWidget && !tagname.equals(GapTag.TAG_NAME)) {
	    			DialogUtil.showErrorMessageBox(getShell(), "Error", "No text selected in transcription widget!");
	    			return;
	    		}
	    		
	    		if (tagname.equals(CommentTag.TAG_NAME)) {
	    			String commentText = null;
			
    				InputDialog id = new InputDialog(getShell(), "Comment", "Please enter a comment: ", "", null);
    				id.setBlockOnOpen(true);
    				if (id.open() != Window.OK) {
    					return;
    				}
    				
    				commentText = id.getValue();
	    			if (commentText.isEmpty()) {
	    				DialogUtil.showErrorMessageBox(getShell(), "Error", "Cannot add an empty comment!");
	    				return;
	    			}
	    				    			
	    			Map<String, Object> atts = new HashMap<>();
	    			atts.put(CommentTag.COMMENT_PROPERTY_NAME, commentText);
	    			mw.addTagForSelection(CommentTag.TAG_NAME, atts, null);
	    			
	    			mw.getUi().getCommentsWidget().reloadComments();
	    		}
	    		else {
	    			mw.addTagForSelection(tagname, null, null);
	    		}
	
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
	    }
	}

	protected void initLocalGuiListenerAndBindings() {
		textAlignmentSelectionAdapter = new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				updateTextAlignment();
			}		
		};
		
		leftAlignmentItem.addSelectionListener(textAlignmentSelectionAdapter);
		centerAlignmentItem.addSelectionListener(textAlignmentSelectionAdapter);
		rightAlignmentItem.addSelectionListener(textAlignmentSelectionAdapter);
		
		if (writingOrientationItem != null) {
			writingOrientationItem.addSelectionListener(new SelectionAdapter() {
				@Override public void widgetSelected(SelectionEvent e) {
					updateWritingOrientation();
				}		
			});
		}
		
		fontItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FontDialog fd = new FontDialog(fontItem.getParent().getShell(), SWT.NONE);
				
//				logger.debug("global text font: "+globalTextFont+" "+globalTextFont.getFontData()[0]);
				fd.setFontList(text.getFont().getFontData());
				fd.setText("Select Font");
//				fd.setRGB(text.getForeground().getRGB());
//				logger.debug("fd = "+text.getFont().getFontData()[0]);
				
				FontData fontData = fd.open();
				if (fontData == null)
					return;
				
				settings.setTranscriptionFontName(fontData.getName());
				settings.setTranscriptionFontSize(fontData.getHeight());
				settings.setTranscriptionFontStyle(fontData.getStyle());

//				setFontFromSettings();
//				TrpConfig.save();
								
				updateLineStyles();
				text.redraw();
			}
		});	
		
		addParagraphItem.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				toggleParagraphOnSelectedLine();
			}
		});
		
		DataBinder db = DataBinder.get();
		db.bindBeanToWidgetSelection(TrpSettings.RENDER_FONT_STYLES, settings, renderFontStyleTypeItem);
		db.bindBeanToWidgetSelection(TrpSettings.RENDER_TEXT_STYLES, settings, renderTextStylesItem);
		db.bindBeanToWidgetSelection(TrpSettings.RENDER_OTHER_STYLES, settings, renderOtherStyleTypeItem);
		db.bindBeanToWidgetSelection(TrpSettings.RENDER_TAGS, settings, renderTagsItem);
		
		db.bindBeanToWidgetSelection(TrpSettings.CENTER_CURRENT_TRANSCRIPTION_LINE_PROPERTY,
				settings, centerCurrentLineItem);
		
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_LINE_BULLETS_PROPERTY,
				settings, showLineBulletsItem);
		
		db.bindBeanToWidgetSelection(TrpSettings.UNDERLINE_TEXT_STYLES_PROPERTY, 
				settings, underlineTextStyleItem);
		
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_CONTROL_SIGNS_PROPERTY,
				settings, showControlSignsItem);
		
		db.bindBeanToWidgetSelection(TrpSettings.FOCUS_SHAPE_ON_DOUBLE_CLICK_IN_TRANSCRIPTION_WIDGET,
				settings, focusShapeOnDoubleClickInTranscriptionWidgetItem);
		
		db.bindBeanToWidgetSelection(TrpSettings.TRANSCRIPTION_TOOLBAR_ON_TOP_PROPERTY, settings, toolBarOnTopItem);
		
		db.bindBeanToWidgetSelection(TrpSettings.FOCUS_SHAPES_ACCORDING_TO_TEXT_ALIGNMENT, settings, focusShapesAccordingToTextAlignmentItem);
		
		db.bindBeanToWidgetSelection(TrpSettings.AUTOCOMPLETE_PROPERTY, settings, autocompleteToggle);
		
//		db.bindBoolBeanValueToToolItemSelection(TrpSettings.SHOW_TEXT_TAG_EDITOR_PROPERTY, settings, showTagEditorItem);
	}
	
	protected void toggleParagraphOnSelectedLine() {
		if (currentLineObject != null) {
			logger.debug("toggling structure on line: "+currentLineObject.getId()+" current structure: "+currentLineObject.getStructure());
			if (CustomTagUtil.hasParagraphStructure(currentLineObject)) {
				currentLineObject.setStructure("", false, this);	
			} else {
				currentLineObject.setStructure(TextTypeSimpleType.PARAGRAPH.value(), false, this);
			}
			
			redrawText(true);
		}
	}
	
	protected void updateTextAlignment() {
		if (leftAlignmentItem.getSelection()) {
			settings.setTextAlignment(SWT.LEFT);
		} else if (centerAlignmentItem.getSelection()) {
			settings.setTextAlignment(SWT.CENTER);
		} else if (rightAlignmentItem.getSelection()) {
			settings.setTextAlignment(SWT.RIGHT);
		}
		
		sendDefaultSelectionChangedSignal(false);
//		setLineStyleRanges();
		redrawText(true);
	}
	
	protected void sendTextKeyDownEvent(int keyCode) {
		Event newEvent = new Event();
		newEvent.keyCode = keyCode;
		text.notifyListeners(SWT.KeyDown, newEvent);
	}
	
	protected void sendTextModifiedSignal(ITrpShapeType s, /*int newSelection,*/ int start, int end, String replacement) {
		Event e = new Event();
		e.item = this;
		e.data = s;
//		e.text = completeText;
//		e.index = newSelection;
		e.start = start;
		e.end = end;
		e.text = replacement;

		logger.debug("sending text modified event: "+e.item+", new-text: "
				+e.text+", new-selection: "+e.index+", start/end: "+e.start+"/"+e.end+", replacement-text: "+replacement);
		
		notifyListeners(SWT.Modify, e);
	}
		
	protected void sendDefaultSelectionChangedSignal(boolean onlyIfChanged) {
//		if (true) return;
		
    	if (!onlyIfChanged || !oldTextSelection.equals(text.getSelection())) {
    		logger.trace("sendDefaultSelectionChangedSignal, selection = "+text.getSelection()+ ", oldTextSelection = "+oldTextSelection);
    		
    		// update buttons:
    		updateButtonsOnSelectionChanged();
    		
    		Event e = new Event();
    		e.item = this;
    		e.start = text.getSelection().x;
    		e.end = text.getSelection().y;
    		notifyListeners(SWT.DefaultSelection, e);
    	}
    	oldTextSelection = text.getSelection();
	}
	
	private void updateButtonsOnSelectionChanged() {
		addParagraphItem.setSelection(currentLineObject != null && CustomTagUtil.hasParagraphStructure(currentLineObject));
	}

	protected void sendSelectionChangedSignal() {
		Event e = new Event();
		e.item = this;
//		e.data = s;
		e.data = (this instanceof WordTranscriptionWidget) ? currentWordObject : currentLineObject;
				
		notifyListeners(SWT.Selection, e);
	}
	
	protected void sendFocusInSignal(ITrpShapeType s) {
		Event e = new Event();
		e.item = this;
		e.data = s;
		notifyListeners(SWT.FocusIn, e);
	}	
	
	protected void detachTextListener() {
		for (MouseListener l : mouseListener) {
			text.removeMouseListener(l);
		}
//		for (LineStyleListener l : lineStyleListener) {
//			text.removeLineStyleListener(l);
//		}		
		for (CaretListener l : caretListener) {
			text.removeCaretListener(l);
		}
		for (ModifyListener l : modifyListener) {
			text.removeModifyListener(l);
		}
		for (ExtendedModifyListener l : extendedModifyListener) {
			text.removeExtendedModifyListener(l);
		}
		for (VerifyListener l : verifyListener) {
			text.removeVerifyListener(l);
		}
		for (VerifyKeyListener l : verifyKeyListener) {
			text.removeVerifyKeyListener(l);
		}
	}
	
	protected void attachTextListener() {
		for (MouseListener l : mouseListener) {
			text.addMouseListener(l);
		}			
//		for (LineStyleListener l : lineStyleListener) {
//			text.addLineStyleListener(l);
//		}			
		for (CaretListener l : caretListener) {
			text.addCaretListener(l);
		}
		for (ModifyListener l : modifyListener) {
			text.addModifyListener(l);
		}
		for (ExtendedModifyListener l : extendedModifyListener) {
			text.addExtendedModifyListener(l);
		}		
		for (VerifyListener l : verifyListener) {
			text.addVerifyListener(l);
		}
		for (VerifyKeyListener l : verifyKeyListener) {
			text.addVerifyKeyListener(l);
		}
	}
	
	public void addUserTextMouseListener(MouseListener l) {
		mouseListener.add(l);
		text.addMouseListener(l);
	}
	
	public void addUserLineStyleListener(LineStyleListener l) {
		lineStyleListener.add(l);
		text.addLineStyleListener(l);
	}
	
	public void addUserCaretListener(CaretListener l) {
		caretListener.add(l);
		text.addCaretListener(l);
	}
	
	public void addUserModifyListener(ModifyListener l) {
		modifyListener.add(l);
		text.addModifyListener(l);
	}
	
	public void addUserExtendedModifyListener(ExtendedModifyListener l) {
		extendedModifyListener.add(l);
		text.addExtendedModifyListener(l);
	}
	
	public void addUserVerifyListener(VerifyListener l) {
		verifyListener.add(l);
		text.addVerifyListener(l);
	}
	
	public void addUserVerifyKeyListener(VerifyKeyListener l) {
		verifyKeyListener.add(l);
		text.addVerifyKeyListener(l);
	}
			
	public PagingToolBar getRegionsPagingToolBar() { return regionsPagingToolBar; }
	public TrpAutoCompleteField getAutoComplete() { return autocomplete; }
	public StyledText getText() { return text; }
	
	public abstract ITrpShapeType getTranscriptionUnit();
	public abstract Class<? extends ITrpShapeType> getTranscriptionUnitClass();
	public String getTranscriptionUnitText() {
		ITrpShapeType tu = getTranscriptionUnit();
		if (tu != null)
			return tu.getUnicodeText();
		
		return "";
	}
//	public abstract Point getSelectionRangeRelativeToTranscriptionUnit();
	
	/**
	 * @return A list of shapes for to the current selection in the transcription
	 * widget and their relative selection ranges, i.e. (offset, length) pairs encoded as {@link IntRange} objects
	 * that specify the selected range inside the corresponding shape.
	 */
	public abstract List<Pair<ITrpShapeType, IntRange>> getSelectedShapesAndRanges();
	public List<ITrpShapeType> getSelectedShapes() {
		List<ITrpShapeType> selected=new ArrayList<>();
		for (Pair<ITrpShapeType, IntRange> p: getSelectedShapesAndRanges()) {
			selected.add(p.getLeft());
		}
		return selected;
	}
	
	public boolean isSingleSelection() { 
		return text.getSelectionText().isEmpty();
	}
	
	public boolean hasFocus() {
		return text.isFocusControl();
	}
	
	/**
	 * Returns the common CustomTag for the current selection in the transcription widget.
	 * Returns null if no such tag exists!<br>
	 * Note that the returned tag range is set to (0,0), since the selection can cross multiple shapes!
	 */
	public <T extends CustomTag> T getCommonIndexedCustomTagForCurrentSelection(String tagName) {
		T commonTag = null;
		for (Pair<ITrpShapeType, IntRange> r : getSelectedShapesAndRanges()) {
			if (r.getLeft().getCustomTagList()==null)
				return null;
			
			T t = r.getLeft().getCustomTagList().getCommonIndexedCustomTag(tagName, r.getRight().offset, r.getRight().length);
			if (t==null) // as soon as the common custom-tag for one range is null -> return null!
				return null;
			else if (commonTag!=null) {
				commonTag.mergeEqualAttributes(t, false);
			} else {
				commonTag = t;
			}
		}
		// set range to (0,0) as those values make no sense anyway for a common-tag over multiple shapes:
		if (commonTag!=null) {
			commonTag.setOffset(0);
			commonTag.setLength(0);
		}
		return commonTag;
	}
		
	public List<CustomTag> getSelectedCommonCustomTags() {
		List<CustomTag> sel = new ArrayList<>();
		
		List<Pair<ITrpShapeType, IntRange>> ranges = getSelectedShapesAndRanges();
		for (Pair<ITrpShapeType, IntRange> r : ranges) {
			List<CustomTag> tags4Shape = r.getLeft().getCustomTagList().getCommonIndexedTags(r.getRight().offset, r.getRight().length);
			sel.addAll(tags4Shape);
		}
		return sel;
	}
	
	/** Returns all custom tags at the current offset */
	public List<CustomTag> getCustomTagsForCurrentOffset() {
		return getCustomTagsForOffset(text.getCaretOffset());
	}
	
	/** Returns all custom tags at the given offset */
	public List<CustomTag> getCustomTagsForOffset(int caretOffset) {
		List<CustomTag> tags = new ArrayList<>();
		if (caretOffset<0 || caretOffset>text.getCharCount())
			return tags;
		
		Pair<ITrpShapeType, Integer> shapeAndOffset = getTranscriptionUnitAndRelativePositionFromOffset(caretOffset);
		logger.trace("getting overlapping tags for offset="+caretOffset+", shape at offset = "+shapeAndOffset);
		if (shapeAndOffset != null) {
			tags.addAll(shapeAndOffset.getLeft().getCustomTagList().getOverlappingTags(null, shapeAndOffset.getRight(), 0));
		}
		
		return tags;
	}
	
	/**
	 * not tested yet... is it really needed?
	 */
	public void updateData(ITrpShapeType shape) {
		if (shape == null) {
			updateData(null, null, null);
		}
		
		if (shape instanceof TrpTextRegionType) {
			TrpTextRegionType region = (TrpTextRegionType) shape;
			updateData(region, null, null);
		}
		else if (shape instanceof TrpTextLineType) {
			TrpTextLineType line = (TrpTextLineType) shape;
			updateData(line.getRegion(), line, null);
		}
		else if (shape instanceof TrpWordType) {
			TrpWordType word = (TrpWordType) shape;
			updateData(word.getLine().getRegion(), word.getLine(), word);
		}
	}
	
	/** Updates the data of the transcription widget with the given region, line and word object.
	 * Also checks if something has changed (text, selection etc.) and updates stuff accordingly. */
	public void updateData(TrpTextRegionType region, TrpTextLineType line, TrpWordType word) {
		logger.debug("updateData, type="+getType()+" region = "+region+ " line = "+line+" word = "+word);
		
		currentRegionObject = region;
		if (region != currentRegionObject)
			undoRedo.clear();
		
		detachTextListener();
		
//		boolean enable = currentRegionObject!=null && !currentRegionObject.getTextLine().isEmpty();
		boolean enable = currentRegionObject!=null;
		
		setEnabled(enable);
		if (!enable) {
			currentRegionObject = null;
			currentLineObject = null;
			currentWordObject = null;
			setText("");
			oldTextSelection=new Point(-1, -1);
			updateLineStyles();
			return;
		}
		
		// update text:
		boolean textChanged = initText();
		logger.debug("updateData, text changed = "+textChanged);
		if (textChanged)
			oldTextSelection=new Point(-1, -1);
		
		// init line and word objects:
		if (line!=null)
			logger.debug("updatedata, line = "+line.getId());
		boolean lineChanged = initLineObject(line);
		logger.debug("line changed = "+lineChanged);
		boolean wordChanged = initWordObject(word);
		
		updateSelection(textChanged, lineChanged, wordChanged);
		
		// FIXME update word graph editor:
		reloadWordGraphMatrix(false);
		
		// update toolbar:
		regionsPagingToolBar.setValues(region.getIndex()+1, Storage.getInstance().getNTextRegions());
		
//		if (currentRegionObject.getTextLine().isEmpty()) {
		text.setEditable(!currentRegionObject.getTextLine().isEmpty());
//		}
		
		attachTextListener();
		
//		text.setFocus();
		
		sendDefaultSelectionChangedSignal(true);
		
		updateLineStyles();
		text.redraw();
		
		onDataUpdated();
	}
	
	protected void reloadWordGraphMatrix(final boolean fromCache) {
		if (!SHOW_WORD_GRAPH_STUFF)
			return;
		
		if (getType() == TranscriptionLevel.WORD_BASED)
			return;		
		
//		wordGraphEditor.setWordGraphMatrix(null, null, -1, EditType.RELOAD);
		if (!showWordGraphEditorItem.getSelection())
			return;
		
		logger.debug("reloading wordgraph matrix");
				
		if (currentLineObject != null) {
			ReloadWgRunnable rWgM = new ReloadWgRunnable(fromCache);		
			Thread t = new Thread(rWgM);
			t.start();
		}
	}
	
	private void setText(String str) { 
		text.setText(str);
		undoRedo.clear();
	}
		
	public void redrawText(boolean updateStyles) {
		if (updateStyles) {
			updateLineStyles(); // also redraw's text field...
		}
		else {
			text.redraw();
		}
	}
	
	/** Is called everytime after the method {@link #updateData(TrpTextRegionType, TrpTextLineType, TrpWordType)} was invoked */
	protected void onDataUpdated() {
	}
	
	protected abstract String getTextFromRegion();
	
	protected boolean initText() {
		// only update text if it has changed:
		String textAll = getTextFromRegion();
		if (!textAll.equals(text.getText())) {
			logger.trace("text changed, old = "+text.getText());
			logger.trace("text changed, new = "+textAll);
			setText(textAll);
			return true;
		}
		return false;
	}
	
	protected abstract void updateSelection(boolean textChanged, boolean lineChanged, boolean wordChanged);
	
	protected boolean initLineObject(TrpTextLineType line) {
		if (line != currentLineObject) {
			currentLineObject = line;
			return true;
		}
		return false;
	}
	
	
	protected boolean initWordObject(TrpWordType word) {
		if (word != currentWordObject) {
			currentWordObject = word;
			return true;
		}
		return false;
	}
		
	@Override public void setEnabled(boolean value) {
//		super.setEnabled(value);
		
		regionsPagingToolBar.setToolbarEnabled(value);
		for (ToolItem ti : additionalToolItems)
			ti.setEnabled(value);
		
		text.setEnabled(value);
		
		if (!value) {
			text.setLineBullet(0, text.getLineCount(), null);
			text.setStyleRange(null);
			text.setBackground(Colors.getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		} else
			text.setBackground(Colors.getSystemColor(SWT.COLOR_WHITE));
		
		undoRedo.setEnabled(value);
		
		if (value && showWordGraphEditorItem!=null)
			setWordGraphEditorVisibility(showWordGraphEditorItem.getSelection());
	}
	
	public WordGraphEditor getWordGraphEditor() { return wordGraphEditor; }
	
//	protected void setTextEditable(boolean val) {
//		text.setEditable(val);
//		if (val)
//			text.setBackground(Colors.getSystemColor(SWT.COLOR_WHITE));
//		else
//			text.setBackground(this.getBackground());
//	}
	
	public ToolItem getLongDash() {
		return longDash;
	}

	public ToolItem getNotSign() {
		return notSign;
	}

	public TrpTextRegionType getCurrentRegionObject() {
		return currentRegionObject;
	}

	public TrpTextLineType getCurrentLineObject() {
		return currentLineObject;
	}

	public TrpWordType getCurrentWordObject() {
		return currentWordObject;
	}
	
//	public TranscriptionTaggingWidget getTranscriptionTaggingWidget() {
//		return transcriptionTaggingWidget;
//	}
	
	
		
}
