package eu.transkribus.swt_gui.mainwidget;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.catti.TrpCattiClientEndpoint;
import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.model.beans.pagecontent.BaselineType;
import eu.transkribus.core.model.beans.pagecontent.PrintSpaceType;
import eu.transkribus.core.model.beans.pagecontent.RegionType;
import eu.transkribus.core.model.beans.pagecontent.TableCellType;
import eu.transkribus.core.model.beans.pagecontent.TableRegionType;
import eu.transkribus.core.model.beans.pagecontent.TextLineType;
import eu.transkribus.core.model.beans.pagecontent.WordType;
import eu.transkribus.core.model.beans.pagecontent_trp.RegionTypeUtil;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.swt.portal.PortalWidget.Docking;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt_gui.Msgs;
import eu.transkribus.util.APropertyChangeSupport;

/**
 * Segmentation viewing settings bean for the canvas 
 */
public class TrpSettings extends APropertyChangeSupport {
	private final static Logger logger = LoggerFactory.getLogger(TrpSettings.class);
	
	public final static TrpSettings DEFAULT = new TrpSettings();
	
	private String libDir = "libs";
	public static final String LIB_DIR_PROPERTY="libDir";
	
	public Locale locale = Msgs.DEFAULT_LOCALE;
	public static final String LOCALE_PROPERTY = "locale";
	
	private boolean serverSideActivated = true;
	public static final String SERVER_SIDE_ACTIVATED_PROPERTY = "serverSideActivated";
	public boolean isServerSideActivated() { return serverSideActivated; }
	
	private String trpServer = TrpServerConn.SERVER_URIS[TrpServerConn.DEFAULT_URI_INDEX];
	public static final String TRP_SERVER_PROPERTY = "trpServer";
	
	// general view settings:
//	private boolean showLeftView=true;
//	public static final String SHOW_LEFT_VIEW_PROPERTY = "showLeftView";
//	private boolean showRightView=true;
//	public static final String SHOW_RIGHT_VIEW_PROPERTY = "showRightView";	
//	private boolean showBottomView=true;
//	public static final String SHOW_BOTTOM_VIEW_PROPERTY = "showBottomView";	
	
	// segmentation view settings:
	private boolean showPrintSpace=false;
	public static final String SHOW_PRINTSPACE_PROPERTY = "showPrintSpace";
	
	private boolean showTextRegions=true;
	public static final String SHOW_TEXT_REGIONS_PROPERTY = "showTextRegions";
	
	private boolean showLines=true;
	public static final String SHOW_LINES_PROPERTY = "showLines";
	
	private boolean showBaselines=true;
	public static final String SHOW_BASELINES_PROPERTY = "showBaselines";
	
	private boolean showWords=false;
	public static final String SHOW_WORDS_PROPERTY = "showWords";
	
	private boolean renderBlackenings=false;
	public static final String RENDER_BLACKENINGS_PROPERTY = "renderBlackenings";
		
	// autocomplete:
	private boolean autocomplete=false;
	public static final String AUTOCOMPLETE_PROPERTY = "autocomplete";
	
	private boolean showLineEditor=false;
	public static final String SHOW_LINE_EDITOR_PROPERTY = "showLineEditor";
	public static final boolean ENABLE_LINE_EDITOR = false;
	
	private boolean rectMode=true;
	public static final String RECT_MODE_PROPERTY = "rectMode";
	
	private boolean autoCreateParent=false;
	public static final String AUTO_CREATE_PARENT_PROPERTY = "autoCreateParent";
	
//	private boolean addToOverlappingParentLine=false;
//	public static final String ADD_TO_OVERLAPPING_PARENT_LINE_PROPERTY = "addToOverlappingParentLine";
	
//	private boolean addToOverlappingParentRegion=false;
//	public static final String ADD_TO_OVERLAPPING_PARENT_REGION_PROPERTY = "addToOverlappingParentRegion";

	private boolean addLinesToOverlappingRegions=true;
	public static final String ADD_LINES_TO_OVERLAPPING_REGIONS_PROPERTY = "addLinesToOverlappingRegions";
	
	private boolean addBaselinesToOverlappingLines=false;
	public static final String ADD_BASELINES_TO_OVERLAPPING_LINES_PROPERTY = "addBaselinesToOverlappingLines";
	
	private boolean addWordsToOverlappingLines=true;
	public static final String ADD_WORDS_TO_OVERLAPPING_LINES_PROPERTY = "addWordsToOverlappingLines";
	
	private boolean deleteLineIfBaselineDeleted=false;
	public static final String DELETE_LINE_IF_BASELINE_DELETED_PROPERTY = "deleteLineIfBaselineDeleted";
	
	private boolean selectNewlyCreatedShape=false;
	public static final String SELECT_NEWLY_CREATED_SHAPE_PROPERTY = "selectNewlyCreatedShape";
	
	private boolean showReadingOrderRegions=false;
	public static final String SHOW_READING_ORDER_REGIONS_PROPERTY = "showReadingOrderRegions";
	
	private boolean showReadingOrderLines=false;
	public static final String SHOW_READING_ORDER_LINES_PROPERTY = "showReadingOrderLines";
	
	private boolean showReadingOrderWords=false;
	public static final String SHOW_READING_ORDER_WORDS_PROPERTY = "showReadingOrderWords";
	
	private boolean showOnlySelectedBaseline=false;
	public static final String SHOW_ONLY_SELECTED_BASELINE_PROPERTY = "showOnlySelectedBaseline";
	
	// segmentation colors:
	private Color colorPrintSpace = Colors.getSystemColor(SWT.COLOR_MAGENTA);
	public static final String COLOR_PS_PROPERTY = "colorPrintSpace";
	private Color colorTextRegions = Colors.getSystemColor(SWT.COLOR_DARK_GREEN);
	public static final String COLOR_REGIONS_PROPERTY = "colorTextRegions";
	private Color colorLines = Colors.getSystemColor(SWT.COLOR_DARK_BLUE);
	public static final String COLOR_LINES_PROPERTY = "colorLines";
	private Color colorBaselines = Colors.getSystemColor(SWT.COLOR_DARK_MAGENTA);
	public static final String COLOR_BASELINES_PROPERTY = "colorBaselines";
	private Color colorWords = Colors.getSystemColor(SWT.COLOR_RED);
	public static final String COLOR_WORDS_PROPERTY = "colorWords";
	
	private Color colorTables = Colors.getSystemColor(SWT.COLOR_DARK_YELLOW);
	public static final String COLOR_TABLES_PROPERTY = "colorTables";
	
	private Color colorTableCells = Colors.getSystemColor(SWT.COLOR_DARK_GREEN);
	public static final String COLOR_TABLE_CELLS_PROPERTY = "colorTableCells";
	
	// font in transcription window:
	private int transcriptionFontSize=20;
	public static final String TRANSCRIPTION_FONT_SIZE_PROPERTY = "transcriptionFontSize";
	
	private String transcriptionFontName=Fonts.getSystemFontName(false, false, false);
	public static final String TRANSCRIPTION_FONT_NAME_PROPERTY = "transcriptionFontName";
	
	private int transcriptionFontStyle=SWT.NORMAL;
	public static final String TRANSCRIPTION_FONT_STYLE_PROPERTY = "transcriptionFontStyle";
	
	private boolean renderFontStyles=false;
	public static final String RENDER_FONT_STYLES = "renderFontStyles";
	private boolean renderTextStyles=true;
	public static final String RENDER_TEXT_STYLES = "renderTextStyles";
	private boolean renderOtherStyles=true;
	public static final String RENDER_OTHER_STYLES = "renderOtherStyles";
	private boolean renderTags=true;
	public static final String RENDER_TAGS = "renderTags";	
	
	private boolean enableIndexedStyles=true;
	public static final String ENABLE_INDEXED_STYLES="enableIndexedStyles";
	
	private String tagNames="";
	public static final String TAG_NAMES_PROPERTY="tagNames";
	
	private int imageCacheSize = 3;
	public static final String IMAGE_CACHE_SIZE_PROPERTY="imageCacheSize";
	
	private boolean preloadImages=false;
	public static final String PRELOAD_IMAGES_PROPERTY = "preloadImages";
	
//	private boolean useSnapshotUpdates=false;
//	public static final String USE_SNAPSHOT_UPDATES_PROPERTY = "useSnapshotUpdates";
	
	private boolean checkForUpdates=true;
	public static final String CHECK_FOR_UPDATES_PROPERTY = "checkForUpdates";
	
	private boolean centerCurrentTranscriptionLine=true;
	public static final String CENTER_CURRENT_TRANSCRIPTION_LINE_PROPERTY = "centerCurrentTranscriptionLine";
	
	public boolean showLineBullets=true;
	public static final String SHOW_LINE_BULLETS_PROPERTY = "showLineBullets";
	
	public boolean showControlSigns=true;
	public static final String SHOW_CONTROL_SIGNS_PROPERTY = "showControlSigns";
	
	public boolean showTipOfTheDay=true;
	public static final String SHOW_TIP_OF_THE_DAY_PROPERTY="showTipOfTheDay";
	
	public boolean useFtpProgramUpdater=false;
	public static final String USE_FTP_PROGRAM_UPDATER = "useFtpProgramUpdater";
	
	public boolean focusShapeOnDoubleClickInTranscriptionWidget=true;
	public static final String FOCUS_SHAPE_ON_DOUBLE_CLICK_IN_TRANSCRIPTION_WIDGET = "focusShapeOnDoubleClickInTranscriptionWidget";
	
	public int showEventsMaxDays = 5;
	public static final String SHOW_EVENTS_MAX_DAYS = "showEventsMaxDays";
	
	public String eventsTxtFileName = "events.txt";
	public static final String EVENTS_TXT_FILE_NAME = "eventsTxtFileName";

	public boolean proxyEnabled = false;
	public static final String PROXY_ENABLED = "proxyEnabled";
	public String proxyHost = "";
	public static final String PROXY_HOST = "proxyHost";
	public String proxyPort = "";
	public static final String PROXY_PORT = "proxyPort";
	public String proxyUser = "";
	public static final String PROXY_USER = "proxyUser";
	public String proxyPassword = "";
	public static final String PROXY_PW = "proxyPw";
	
	public boolean autoLogin = false;
	public static final String AUTO_LOGIN_PROPERTY = "autoLogin";
	
	public Docking leftViewDockingState = Docking.DOCKED;
	public static final String LEFT_VIEW_DOCKING_STATE_PROPERTY = "leftViewDockingState";
	
	public Docking rightViewDockingState = Docking.DOCKED;
	public static final String RIGHT_VIEW_DOCKING_STATE_PROPERTY = "rightViewDockingState";
	
	public Docking bottomViewDockingState = Docking.DOCKED;
	public static final String BOTTOM_VIEW_DOCKING_STATE_PROPERTY = "bottomViewDockingState";
	
	public int[] newWeightsForVerticalTopLevelSlash = new int [] {80, 20};
	public static final String NEW_WEIGHTS_FOR_VERTICAL_TOP_LEVEL = "newWeightsForVerticalTopLevelSlash";
	
	public String cattiServerUrl = TrpCattiClientEndpoint.DEFAULT_CATTI_URI;
	public static final String CATTI_SERVER_URL_PROPERTY = "cattiServerUrl";
	
	public int leftTabSelectionId = 0;
	public static final String LEFT_TAB_SELECTION_ID = "leftTabSelectionId";
	
	public int rightTabSelectionId = 0;
	public static final String RIGHT_TAB_SELECTION_ID = "rightTabSelectionId";
	
	public String imgFitTo = "width";
	public static final String IMG_FIT_TO = "imgFitTo";
	
	public boolean createThumbs = true;
	public static final String CREATE_THUMBS_PROPERTY = "createThumbs";
	
	static final List<String> DO_NOT_SAVE_THOSE_PROPERTIES = new ArrayList<String>() {{
	}};
	
	public TrpSettings() {
		super();
	}
	
//	public TrpSettings(Properties properties) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
//		super(properties);
//	}
	
	public void setShowAll(boolean value) {
		setShowPrintSpace(value);
		setShowTextRegions(value);
		setShowLines(value);
		setShowBaselines(value);
		setShowWords(value);
		
//		firePropertyChange("showAll", !value, value);
	}
	
	public static boolean isColorProperty(String pn) {
		return pn!=null && 
				(pn.equals(TrpSettings.COLOR_PS_PROPERTY) || pn.equals(TrpSettings.COLOR_REGIONS_PROPERTY) 
						|| pn.equals(TrpSettings.COLOR_LINES_PROPERTY) || pn.equals(TrpSettings.COLOR_BASELINES_PROPERTY) 
						|| pn.equals(TrpSettings.COLOR_WORDS_PROPERTY));
	}
	
	public static boolean isSegmentationVisibilityProperty(String pn) {
		return pn!=null && 
				(pn.equals(TrpSettings.SHOW_PRINTSPACE_PROPERTY) || pn.equals(TrpSettings.SHOW_TEXT_REGIONS_PROPERTY) 
						|| pn.equals(TrpSettings.SHOW_LINES_PROPERTY) || pn.equals(TrpSettings.SHOW_BASELINES_PROPERTY) 
						|| pn.equals(TrpSettings.SHOW_WORDS_PROPERTY));
	}
	

	public boolean isShowAll() {
		return (showPrintSpace && showTextRegions && showLines && showBaselines && showWords);
	}
	
	public boolean isRenderBlackenings() {
		return renderBlackenings;
	}

	public void setRenderBlackenings(boolean renderBlackenings) {
		this.renderBlackenings = renderBlackenings;
		firePropertyChange(RENDER_BLACKENINGS_PROPERTY, !this.renderBlackenings, this.renderBlackenings);
	}

	public boolean isShowPrintSpace() {
		return showPrintSpace;
	}

	public void setShowPrintSpace(boolean showPrintSpace) {
		this.showPrintSpace = showPrintSpace;
		firePropertyChange(SHOW_PRINTSPACE_PROPERTY, !this.showPrintSpace, this.showPrintSpace);
	}

	public boolean isShowTextRegions() {
		return showTextRegions;
	}

	public void setShowTextRegions(boolean showTextRegions) {
		this.showTextRegions = showTextRegions;
		firePropertyChange(SHOW_TEXT_REGIONS_PROPERTY, !this.showTextRegions, this.showTextRegions);
	}

	public boolean isShowLines() {
		return showLines;
	}

	public void setShowLines(boolean showLines) {
		this.showLines = showLines;
		firePropertyChange(SHOW_LINES_PROPERTY, !this.showLines, this.showLines);
	}

	public boolean isShowBaselines() {
		return showBaselines;
	}

	public void setShowBaselines(boolean showBaselines) {
		this.showBaselines = showBaselines;
		firePropertyChange(SHOW_BASELINES_PROPERTY, !this.showBaselines, this.showBaselines);
	}
	
	public boolean isShowOnlySelectedBaseline() {
		return showOnlySelectedBaseline;
	}

	public void setShowOnlySelectedBaseline(boolean showOnlySelectedBaseline) {
		this.showOnlySelectedBaseline = showOnlySelectedBaseline;
		firePropertyChange(SHOW_ONLY_SELECTED_BASELINE_PROPERTY, !this.showOnlySelectedBaseline, this.showOnlySelectedBaseline);
	}

	public boolean isShowWords() {
		return showWords;
	}

	public void setShowWords(boolean showWords) {
		this.showWords = showWords;
		firePropertyChange(SHOW_WORDS_PROPERTY, !this.showWords, this.showWords);
	}
		
	public boolean isAutocomplete() {
		return autocomplete;
	}
	
	public void setAutocomplete(boolean autocomplete) {
		this.autocomplete = autocomplete;
		firePropertyChange(AUTOCOMPLETE_PROPERTY, !this.autocomplete, this.autocomplete);
	}

	public Color getColorPrintSpace() {
		return colorPrintSpace;
	}

	public void setColorPrintSpace(Color colorPrintSpace) {
		Color old = this.colorPrintSpace;
		this.colorPrintSpace = colorPrintSpace;
		firePropertyChange(COLOR_PS_PROPERTY, old, this.colorPrintSpace);
	}

	public Color getColorTextRegions() {
		return colorTextRegions;
	}

	public void setColorTextRegions(Color colorTextRegions) {
		Color old = this.colorTextRegions;
		this.colorTextRegions = colorTextRegions;
		firePropertyChange(COLOR_REGIONS_PROPERTY, old, this.colorTextRegions);
	}

	public Color getColorLines() {
		return colorLines;
	}

	public void setColorLines(Color colorLines) {
		Color old = this.colorLines;
		this.colorLines = colorLines;
		firePropertyChange(COLOR_LINES_PROPERTY, old, this.colorLines);
	}

	public Color getColorBaselines() {
		return colorBaselines;
	}

	public void setColorBaselines(Color colorBaselines) {
		Color old = this.colorBaselines;
		this.colorBaselines = colorBaselines;
		firePropertyChange(COLOR_BASELINES_PROPERTY, old, this.colorBaselines);
	}

	public Color getColorWords() {
		return colorWords;
	}

	public void setColorWords(Color colorWords) {
		Color old = this.colorWords;
		this.colorWords = colorWords;
		firePropertyChange(COLOR_WORDS_PROPERTY, old, this.colorWords);
	}
	
	public Color getColorTables() {
		return colorTables;
	}

	public void setColorTables(Color colorTables) {
		Color old = this.colorTables;
		this.colorTables = colorTables;
		firePropertyChange(COLOR_TABLES_PROPERTY, old, this.colorTables);
	}
	
	public Color getColorTableCells() {
		return colorTableCells;
	}
	
	public void setColorTableCells(Color colorTableCells) {
		Color old = this.colorTableCells;
		this.colorTableCells = colorTableCells;
		firePropertyChange(COLOR_TABLE_CELLS_PROPERTY, old, this.colorTableCells);
	}
	

//	public boolean isShowLeftView() {
//		return showLeftView;
//	}
//	
//	public void setShowLeftView(boolean showLeftView) {
//		this.showLeftView = showLeftView;
//		firePropertyChange(SHOW_LEFT_VIEW_PROPERTY, !this.showLeftView, this.showLeftView);
//	}
//	
//	public boolean isShowRightView() {
//		return showRightView;
//	}
//	
//	public void setShowRightView(boolean showRightView) {
//		this.showRightView = showRightView;
//		firePropertyChange(SHOW_RIGHT_VIEW_PROPERTY, !this.showRightView, this.showRightView);
//	}	
	
	public boolean isShowLineEditor() {
		return ENABLE_LINE_EDITOR && showLineEditor;
	}

	public void setShowLineEditor(boolean showLineEditor) {
		if (ENABLE_LINE_EDITOR) {
			this.showLineEditor = showLineEditor;
			firePropertyChange(SHOW_LINE_EDITOR_PROPERTY, !this.showLineEditor, this.showLineEditor);
		}
	}
	
//	public boolean isShowBottomView() {
//		return showBottomView;
//	}
//
//	public void setShowBottomView(boolean showBottomView) {
//		this.showBottomView = showBottomView;
//		firePropertyChange(SHOW_BOTTOM_VIEW_PROPERTY, !this.showBottomView, this.showBottomView);
//	}
	
	public String getTrpServer() {
		return trpServer;
	}

	public void setTrpServer(String trpServer) {
		this.trpServer = trpServer;
	}
	
	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		Locale old = this.locale;
		this.locale = locale;
		firePropertyChange(LOCALE_PROPERTY, old, this.locale);
	}
	
	public boolean getRectMode() {
		return rectMode;
	}
	
	public void setRectMode(boolean rectMode) { 
		this.rectMode = rectMode;
		firePropertyChange(RECT_MODE_PROPERTY, !this.rectMode, this.rectMode);
	}
	
	public boolean isAutoCreateParent() {
		return autoCreateParent;
	}

	public void setAutoCreateParent(boolean autoCreateParent) {
		this.autoCreateParent = autoCreateParent;
		firePropertyChange(AUTO_CREATE_PARENT_PROPERTY, !this.autoCreateParent, this.autoCreateParent);
	}

	public int getTranscriptionFontSize() {
		return transcriptionFontSize;
	}

	public void setTranscriptionFontSize(int transcriptionFontSize) {
		if (transcriptionFontSize > 0) {
			int old = this.transcriptionFontSize;
			this.transcriptionFontSize = transcriptionFontSize;
			firePropertyChange(TRANSCRIPTION_FONT_SIZE_PROPERTY, old, this.transcriptionFontSize);
		}
	}

	public String getTranscriptionFontName() {
		return transcriptionFontName;
	}

	public void setTranscriptionFontName(String transcriptionFontName) {
		if (transcriptionFontName!=null && !transcriptionFontName.isEmpty()) {
			String old = this.transcriptionFontName;
			this.transcriptionFontName = transcriptionFontName;
			firePropertyChange(TRANSCRIPTION_FONT_NAME_PROPERTY, old, this.transcriptionFontName);
		}
	}
	
	public String getLibDir() {
		return libDir;
	}

	public void setLibDir(String libDir) {
		this.libDir = libDir;
		
//		if (libDir!=null && !libDir.isEmpty()) {
//			String old = this.libDir;
//			this.libDir = libDir;
//			firePropertyChange(LIB_DIR_PROPERTY, old, this.libDir);
//		}
	}
	
	public int getTranscriptionFontStyle() {
		return transcriptionFontStyle;
	}

	public void setTranscriptionFontStyle(int transcriptionFontStyle) {
		if (transcriptionFontStyle>=0 && transcriptionFontStyle<=4) {
			int old = this.transcriptionFontStyle;
			this.transcriptionFontStyle = transcriptionFontStyle;
			firePropertyChange(TRANSCRIPTION_FONT_STYLE_PROPERTY, old, this.transcriptionFontStyle);
		}
	}
	
	public boolean isRenderFontStyles() {
		return renderFontStyles;
	}

	public void setRenderFontStyles(boolean renderFontStyles) {
		this.renderFontStyles = renderFontStyles;
		firePropertyChange(RENDER_FONT_STYLES, !this.renderFontStyles, this.renderFontStyles);
	}

	public boolean isRenderTextStyles() {
		return renderTextStyles;
	}

	public void setRenderTextStyles(boolean renderTextStyles) {
		this.renderTextStyles = renderTextStyles;
		firePropertyChange(RENDER_TEXT_STYLES, !this.renderTextStyles, this.renderTextStyles);
	}

	public boolean isRenderOtherStyles() {
		return renderOtherStyles;
	}

	public void setRenderOtherStyles(boolean renderOtherStyles) {
		this.renderOtherStyles = renderOtherStyles;
		firePropertyChange(RENDER_OTHER_STYLES, !this.renderOtherStyles, this.renderOtherStyles);
	}
	
	public boolean isRenderTags() {
		return renderTags;
	}

	public void setRenderTags(boolean renderTags) {
		this.renderTags = renderTags;
		firePropertyChange(RENDER_TAGS, !this.renderTags, this.renderTags);
	}
	
	/**
	 * @deprecated non-indexed styles are not supported anymore - will be removed
	 */
	public boolean isEnableIndexedStyles() {
		return true;
//		return enableIndexedStyles;
		}
	/**
	 * @deprecated non-indexed styles are not supported anymore - will be removed
	 */	
	public void setEnableIndexedStyles(boolean enableIndexedStyles) { 
		this.enableIndexedStyles = enableIndexedStyles;
		firePropertyChange(ENABLE_INDEXED_STYLES, !this.enableIndexedStyles, this.enableIndexedStyles);
	}
	
	public String getTagNames() { return tagNames; }
	public void setTagNames(String tagNames) {
		String old = this.tagNames;
		this.tagNames = tagNames;
		
		logger.debug("SETTING TAG NAMES: "+tagNames);
		
		firePropertyChange(TAG_NAMES_PROPERTY, old, this.tagNames);
	}

	public static Color determineColor(TrpSettings sets, Object wrappedData) {
		if (wrappedData instanceof TableCellType) {
			return sets.colorTableCells;
		}
		if (wrappedData instanceof PrintSpaceType) {
			return sets.colorPrintSpace;
		}
		if (wrappedData instanceof TextLineType) {
			return sets.colorLines;
		}
		if (wrappedData instanceof BaselineType) {
			return sets.colorBaselines;
		}
		if (wrappedData instanceof WordType) {
			return sets.colorWords;
		}		
		if (wrappedData instanceof RegionType) {
			if (wrappedData instanceof TrpTextRegionType)
				return sets.colorTextRegions;
			else if (wrappedData instanceof TableRegionType)
				return sets.colorTables;
			else {
				String rt = RegionTypeUtil.getRegionType((TrpRegionType)wrappedData);
				if (rt.equals(RegionTypeUtil.BLACKENING_REGION)) {
					return Colors.getSystemColor(SWT.COLOR_BLACK);
				}
				
				return Colors.getSystemColor(SWT.COLOR_DARK_CYAN);				
			}
		}
		
		return Colors.getSystemColor(SWT.COLOR_BLACK);	
	}

	public int getImageCacheSize() {
		return imageCacheSize;
	}

	public void setImageCacheSize(int imageCacheSize) {
		int old = this.imageCacheSize;
		this.imageCacheSize = imageCacheSize;
		firePropertyChange(IMAGE_CACHE_SIZE_PROPERTY, old, this.imageCacheSize);
	}

	public boolean isPreloadImages() {
		return preloadImages;
	}

	public void setPreloadImages(boolean preloadImages) {
		this.preloadImages = preloadImages;
		firePropertyChange(PRELOAD_IMAGES_PROPERTY, !this.preloadImages, this.preloadImages);
	}
	
	public boolean isCheckForUpdates() { return checkForUpdates; }
	public void setcheckForUpdates(boolean checkForUpdates) {
		this.checkForUpdates = checkForUpdates;
		firePropertyChange(CHECK_FOR_UPDATES_PROPERTY, !this.checkForUpdates, this.checkForUpdates);
	}

	public boolean isCenterCurrentTranscriptionLine() {
		return centerCurrentTranscriptionLine;
	}

	public void setCenterCurrentTranscriptionLine(boolean centerCurrentTranscriptionLine) {
		this.centerCurrentTranscriptionLine = centerCurrentTranscriptionLine;
		firePropertyChange(CENTER_CURRENT_TRANSCRIPTION_LINE_PROPERTY, !this.centerCurrentTranscriptionLine, this.centerCurrentTranscriptionLine);
	}
	
	public boolean isShowLineBullets() {
		return showLineBullets;
	}

	public void setShowLineBullets(boolean showLineBullets) {
		this.showLineBullets = showLineBullets;
		firePropertyChange(SHOW_LINE_BULLETS_PROPERTY, !this.showLineBullets, this.showLineBullets);
	}
	
	public boolean isShowControlSigns() {
		return showControlSigns;
	}

	public void setShowControlSigns(boolean showControlSigns) {
		this.showControlSigns = showControlSigns;
		firePropertyChange(SHOW_CONTROL_SIGNS_PROPERTY, !this.showControlSigns, this.showControlSigns);
	}

	public boolean isShowTipOfTheDay() {
		return showTipOfTheDay;
	}

	public void setShowTipOfTheDay(boolean showTipOfTheDay) {
		this.showTipOfTheDay = showTipOfTheDay;
		firePropertyChange(SHOW_TIP_OF_THE_DAY_PROPERTY, !this.showTipOfTheDay, this.showTipOfTheDay);
	}
	
	@Deprecated
	public boolean isUseFtpProgramUpdater() {
		return useFtpProgramUpdater;
	}

	@Deprecated
	public void setUseFtpProgramUpdater(boolean useFtpProgramUpdater) {
		this.useFtpProgramUpdater = useFtpProgramUpdater;
		firePropertyChange(USE_FTP_PROGRAM_UPDATER, !this.useFtpProgramUpdater, this.useFtpProgramUpdater);
	}
	
//	public boolean isAddToOverlappingParentLine() {
//		return addToOverlappingParentLine;
//	}
//
//	public void setAddToOverlappingParentLine(boolean addToOverlappingParentLine) {
//		this.addToOverlappingParentLine = addToOverlappingParentLine;
//		firePropertyChange(ADD_TO_OVERLAPPING_PARENT_LINE_PROPERTY, !this.addToOverlappingParentLine, this.addToOverlappingParentLine);
//	}
//
//	public boolean isAddToOverlappingParentRegion() {
//		return addToOverlappingParentRegion;
//	}
//
//	public void setAddToOverlappingParentRegion(boolean addToOverlappingParentRegion) {
//		this.addToOverlappingParentRegion = addToOverlappingParentRegion;
//		firePropertyChange(ADD_TO_OVERLAPPING_PARENT_REGION_PROPERTY, !this.addToOverlappingParentRegion, this.addToOverlappingParentRegion);
//	}

	public boolean isAddLinesToOverlappingRegions() {
		return addLinesToOverlappingRegions;
	}

	public void setAddLinesToOverlappingRegions(boolean addLinesToOverlappingRegions) {
		this.addLinesToOverlappingRegions = addLinesToOverlappingRegions;
		firePropertyChange(ADD_LINES_TO_OVERLAPPING_REGIONS_PROPERTY, !this.addLinesToOverlappingRegions, this.addLinesToOverlappingRegions);
	}

	public boolean isAddBaselinesToOverlappingLines() {
		return addBaselinesToOverlappingLines;
	}

	public void setAddBaselinesToOverlappingLines(boolean addBaselinesToOverlappingLines) {
		this.addBaselinesToOverlappingLines = addBaselinesToOverlappingLines;
		firePropertyChange(ADD_BASELINES_TO_OVERLAPPING_LINES_PROPERTY, !this.addBaselinesToOverlappingLines, this.addBaselinesToOverlappingLines);
	}

	public boolean isAddWordsToOverlappingLines() {
		return addWordsToOverlappingLines;
	}

	public void setAddWordsToOverlappingLines(boolean addWordsToOverlappingLines) {
		this.addWordsToOverlappingLines = addWordsToOverlappingLines;
		firePropertyChange(ADD_WORDS_TO_OVERLAPPING_LINES_PROPERTY, !this.addWordsToOverlappingLines, this.addWordsToOverlappingLines);
	}
	
	public boolean isDeleteLineIfBaselineDeleted() {
		return deleteLineIfBaselineDeleted;
	}

	public void setDeleteLineIfBaselineDeleted(boolean deleteLineIfBaselineDeleted) {
		this.deleteLineIfBaselineDeleted = deleteLineIfBaselineDeleted;
		firePropertyChange(DELETE_LINE_IF_BASELINE_DELETED_PROPERTY, !this.deleteLineIfBaselineDeleted, this.deleteLineIfBaselineDeleted);
	}

	public boolean isSelectNewlyCreatedShape() {
		return selectNewlyCreatedShape;
	}

	public void setSelectNewlyCreatedShape(boolean selectNewlyCreatedShape) {
		this.selectNewlyCreatedShape = selectNewlyCreatedShape;
		firePropertyChange(SELECT_NEWLY_CREATED_SHAPE_PROPERTY, !this.selectNewlyCreatedShape, this.selectNewlyCreatedShape);
	}
	
	public boolean isFocusShapeOnDoubleClickInTranscriptionWidget() {
		return focusShapeOnDoubleClickInTranscriptionWidget;
	}

	public void setFocusShapeOnDoubleClickInTranscriptionWidget(boolean focusShapeOnDoubleClickInTranscriptionWidget) {
		this.focusShapeOnDoubleClickInTranscriptionWidget = focusShapeOnDoubleClickInTranscriptionWidget;
		firePropertyChange(FOCUS_SHAPE_ON_DOUBLE_CLICK_IN_TRANSCRIPTION_WIDGET, !this.focusShapeOnDoubleClickInTranscriptionWidget, this.focusShapeOnDoubleClickInTranscriptionWidget);
	}

	public boolean isShowReadingOrderRegions() {
		return showReadingOrderRegions;
	}

	public void setShowReadingOrderRegions(boolean showReadingOrderRegions) {
		this.showReadingOrderRegions = showReadingOrderRegions;
		firePropertyChange(SHOW_READING_ORDER_REGIONS_PROPERTY, !this.showReadingOrderRegions, this.showReadingOrderRegions);
	}

	public boolean isShowReadingOrderLines() {
		return showReadingOrderLines;
	}

	public void setShowReadingOrderLines(boolean showReadingOrderLines) {
		this.showReadingOrderLines = showReadingOrderLines;
		firePropertyChange(SHOW_READING_ORDER_LINES_PROPERTY, !this.showReadingOrderLines, this.showReadingOrderLines);
	}

	public boolean isShowReadingOrderWords() {
		return showReadingOrderWords;
	}

	public void setShowReadingOrderWords(boolean showReadingOrderWords) {
		this.showReadingOrderWords = showReadingOrderWords;
		firePropertyChange(SHOW_READING_ORDER_WORDS_PROPERTY, !this.showReadingOrderWords, this.showReadingOrderWords);
	}

	public int getShowEventsMaxDays() {
		return showEventsMaxDays;
	}

	public void setShowEventsMaxDays(int showEventsMaxDays) {
		int old = this.showEventsMaxDays;
		this.showEventsMaxDays = showEventsMaxDays;
		firePropertyChange(SHOW_EVENTS_MAX_DAYS, old, this.showEventsMaxDays);
	}

	public String getEventsTxtFileName() {
		return eventsTxtFileName;
	}

	public void setEventsTxtFileName(String eventsTxtFileName) {
		String old = this.eventsTxtFileName;
		this.eventsTxtFileName = eventsTxtFileName;
		firePropertyChange(EVENTS_TXT_FILE_NAME, old, this.eventsTxtFileName);
	}

	public boolean isProxyEnabled() {
		return proxyEnabled;
	}
	
	public void setProxyEnabled(boolean isProxyEnabled) {
		this.proxyEnabled = isProxyEnabled;
		firePropertyChange(PROXY_ENABLED, !this.proxyEnabled, this.proxyEnabled);
	}
	
	public String getProxyHost() {
		return proxyHost;
	}
	
	public void setProxyHost(String proxyHost) {
		String old = this.proxyHost;
		this.proxyHost = proxyHost;
		firePropertyChange(PROXY_HOST, old, this.proxyHost);
	}
	
	public String getProxyPort(){
		return proxyPort;
	}
	
	public void setProxyPort(String proxyPort) {
		String old = this.proxyPort;
		this.proxyPort = proxyPort;
		firePropertyChange(PROXY_PORT, old, this.proxyPort);
	}
	
	public String getProxyUser(){
		return proxyUser;
	}
	
	public void setProxyUser(String proxyUser) {
		String old = this.proxyUser;
		this.proxyUser = proxyUser;
		firePropertyChange(PROXY_USER, old, this.proxyUser);
	}
	
	public String getProxyPassword(){
		return proxyPassword;
	}

	public void setProxyPassword(String proxyPassword) {
		String old = this.proxyPassword;
		this.proxyPassword  = proxyPassword;
		firePropertyChange(PROXY_PW, old, this.proxyPassword);
	}
	
	public String getCattiServerUrl() {
		return cattiServerUrl;
	}

	public void setCattiServerUrl(String cattiServerUrl) {
		String old = this.cattiServerUrl;
		this.cattiServerUrl = cattiServerUrl;
		firePropertyChange(CATTI_SERVER_URL_PROPERTY, old, this.cattiServerUrl);
	}

	public boolean isAutoLogin() {
		return autoLogin;
	}

	public void setAutoLogin(boolean autoLogin) {
		this.autoLogin = autoLogin;
		firePropertyChange(AUTO_LOGIN_PROPERTY, !this.autoLogin, this.autoLogin);
	}

	public boolean isCreateThumbs() {
		return createThumbs;
	}

	public void setCreateThumbs(boolean createThumbs) {
		this.createThumbs = createThumbs;
		firePropertyChange(CREATE_THUMBS_PROPERTY, !this.createThumbs, this.createThumbs);
	}

	@Override public List<String> getPropertiesToNotSave() {
		return DO_NOT_SAVE_THOSE_PROPERTIES;
	}

	public Docking getLeftViewDockingState() {
		return leftViewDockingState;
	}

	public void setLeftViewDockingState(Docking leftViewDockingState) {
		Docking old = this.leftViewDockingState;
		this.leftViewDockingState = leftViewDockingState;
		logger.debug("set left docking state");
		
		firePropertyChange(LEFT_VIEW_DOCKING_STATE_PROPERTY, old, this.leftViewDockingState);
	}

	public Docking getRightViewDockingState() {
		return rightViewDockingState;
	}

	public void setRightViewDockingState(Docking rightViewDockingState) {
		Docking old = this.rightViewDockingState;
		this.rightViewDockingState = rightViewDockingState;
		firePropertyChange(RIGHT_VIEW_DOCKING_STATE_PROPERTY, old, this.rightViewDockingState);
	}

	public Docking getBottomViewDockingState() {
		return bottomViewDockingState;
	}

	public void setBottomViewDockingState(Docking bottomViewDockingState) {
		Docking old = this.bottomViewDockingState;
		this.bottomViewDockingState = bottomViewDockingState;
		firePropertyChange(BOTTOM_VIEW_DOCKING_STATE_PROPERTY, old, this.bottomViewDockingState);
	}
	
	public int[] getNewWeightsForVerticalTopLevelSlash() {
		return newWeightsForVerticalTopLevelSlash;
	}
	
	public void setNewWeightsForVerticalTopLevelSlash(int [] weights) {		
		int [] old = this.newWeightsForVerticalTopLevelSlash;
		this.newWeightsForVerticalTopLevelSlash = weights;
		firePropertyChange(NEW_WEIGHTS_FOR_VERTICAL_TOP_LEVEL, old, this.newWeightsForVerticalTopLevelSlash);
	}

	public int getLeftTabSelectionId() {
		return leftTabSelectionId;
	}

	public void setLeftTabSelectionId(int leftTabSelectionId) {
		logger.debug("set left tab selection id");
		int old = this.leftTabSelectionId;
		this.leftTabSelectionId = leftTabSelectionId;
		firePropertyChange(LEFT_TAB_SELECTION_ID, old, this.leftTabSelectionId);
	}

	public int getRightTabSelectionId() {
		return rightTabSelectionId;
	}

	public void setRightTabSelectionId(int rightTabSelectionId) {
		int old = this.rightTabSelectionId;
		this.rightTabSelectionId = rightTabSelectionId;
		firePropertyChange(RIGHT_TAB_SELECTION_ID, old, this.rightTabSelectionId);
	}

	public String getImgFitTo() {
		return imgFitTo;
	}

	public void setImgFitTo(String imgFitTo) {
		String oldValue = this.imgFitTo;
		this.imgFitTo = imgFitTo;
		firePropertyChange(IMG_FIT_TO, oldValue, this.imgFitTo);
	}
	
	
//	public boolean isUseSnapshotUpdates() { return useSnapshotUpdates; }
//	public void setUseSnapshotUpdates(boolean useSnapshotUpdates) {
//		this.useSnapshotUpdates = useSnapshotUpdates;
//		firePropertyChange(USE_SNAPSHOT_UPDATES_PROPERTY, !this.useSnapshotUpdates, this.useSnapshotUpdates);
//	}

}
