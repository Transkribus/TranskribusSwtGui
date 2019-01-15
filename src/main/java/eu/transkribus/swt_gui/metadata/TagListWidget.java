package eu.transkribus.swt_gui.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.customtags.CssSyntaxTag;
import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagUtil;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableLabelProvider;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class TagListWidget extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(TagListWidget.class);
	
	MyTableViewer tv;
	
//	Map<CustomTag, ControlEditor> delSelectedEditors = new HashMap<>();
	Button clearTagsBtn;
	
	List<CustomTag> tagList = new ArrayList<>();
	
	Composite btnsContainer;
	
	public static final String POSITION_COL = "";
	public static final String DOC_COL = "Doc";
//	public static final String TITLE_COL = "Title";
	public static final String PAGE_COL = "Page";
	public static final String REGION_COL = "Region";
//	public static final String LINE_COL = "Line";
//	public static final String WORD_COL = "Word";
	public static final String TAG_COL = "Tag";
	public static final String PROPERTIES_COL = "Properties";
	public static final String CONTEXT_COL = "Text";
	public static final String TAG_VALUE_COL = "Value";
	
	public static final ColumnConfig[] RESULT_COLS = new ColumnConfig[] {
		new ColumnConfig(POSITION_COL, 35, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(TAG_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(TAG_VALUE_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(CONTEXT_COL, 200, false, DefaultTableColumnViewerSorter.ASC),
//		new ColumnConfig(REGION_COL, 40, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(PROPERTIES_COL, 1000, false, DefaultTableColumnViewerSorter.ASC),
//		new ColumnConfig(DOC_COL, 60, true, DefaultTableColumnViewerSorter.ASC),
//		new ColumnConfig(PAGE_COL, 60, false, DefaultTableColumnViewerSorter.ASC),
	};
	
	static Storage store = Storage.getInstance();
	
	private boolean disableTagUpdate=false;
	
	public TagListWidget(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		
		Composite container = this;
		
		Label headerLbl = new Label(container, 0);
		headerLbl.setText("Tags of current Transcript");
		Fonts.setBoldFont(headerLbl);
		headerLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		initBtns(container);
		initTable(container);
	}
	
	private void initTable(Composite container) {
		tv = new MyTableViewer(container, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION /*| SWT.VIRTUAL*/);
		tv.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		tv.getTable().setHeaderVisible(true);
		tv.getTable().setLinesVisible(true);
		
		tv.addColumns(RESULT_COLS);
		
		// ignore case when sorting columns:
		for (int i=0; i<tv.getNColumns(); ++i) {
			tv.getSorter(i).setIgnoreCase(true);
		}
		tv.setContentProvider(ArrayContentProvider.getInstance());
		
//		tv.setContentProvider(new IStructuredContentProvider() {
//			public Object[] getElements(Object inputElement) {
//				String[] ret = new String[folders.size()];
//				folders.toArray(ret);
//				return ret;
//			}
//			public void dispose() {
//			}
//			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
//			}
//		});
		
		final int CONTEXT_LENGTH=15;
		
		final MyTableLabelProvider mtlp = new MyTableLabelProvider() {
			@Override public String getColumnText(String cn, Object element, Object data) {
				
				if (element instanceof CustomTag) {
					CustomTag t = (CustomTag) element;
					
					if (cn.equals(POSITION_COL)) {
						return ""+(tagList.indexOf(t) + 1);
					}
					else if (cn.equals(REGION_COL)) {
						if (t.getCustomTagList()!=null && t.getCustomTagList().getShape()!=null) {
							return t.getCustomTagList().getShape().getId();	
						} else {
							return "";
						}
					}	
					else if (cn.equals(TAG_COL)) {
						return t.getTagName();
					}
					else if (cn.equals(CONTEXT_COL)) {
						return t.getLeftContext(CONTEXT_LENGTH)+t.getContainedText()+t.getRightContext(CONTEXT_LENGTH);
					}
					else if (cn.equals(TAG_VALUE_COL)) {
						return t.getContainedText();
					}
					else if (cn.equals(PROPERTIES_COL)) {
						String propsStr = t.getAttributesCssStrWoOffsetAndLength(true);
						// remove semicolons and multiple whitespaces:
						propsStr = propsStr.replaceAll(";", "").replaceAll(":", ": ").trim().replaceAll(" +", " ");
						return propsStr;
					}
					
					return "";
				}

				return "i am error";
			}
		};
		
		tv.setLabelProvider(new StyledCellLabelProvider() {
			public void update(ViewerCell cell) {
				if (cell.getElement() instanceof CustomTag) {
					int ci = cell.getColumnIndex();
					String cn = RESULT_COLS[ci].name;
					CustomTag t = (CustomTag) cell.getElement();
					
					String txt = mtlp.getColumnText(cn, cell.getElement(), null);
					if (cn.equals(CONTEXT_COL)) {
						int o=StringUtils.length(t.getLeftContext(CONTEXT_LENGTH));
						int l=StringUtils.length(t.getContainedText());
						
						if (CoreUtils.isInIndexRange(o, 0, txt.length()) && CoreUtils.isInIndexRange(o+l, 0, txt.length())) {
							StyleRange sr = new StyleRange(o, l, cell.getForeground(), Colors.getSystemColor(SWT.COLOR_YELLOW));
							cell.setStyleRanges(new StyleRange[] { sr } );
						}
					}
					else if (cn.equals(PROPERTIES_COL)) {
						if (!StringUtils.isEmpty(txt)) {
							final Pattern propsPattern = Pattern.compile
									(CssSyntaxTag.CSS_ATTRIBUTE_NAME_STYLE+":"/*+CssSyntaxTag.CSS_ATTRIBUTE_VALUE_STYLE*/,
											Pattern.UNICODE_CHARACTER_CLASS);
							Matcher matcher = propsPattern.matcher(txt);
							List<StyleRange> srs = new ArrayList<>();
							while (matcher.find()) {
								StyleRange sr = new StyleRange(matcher.start(), matcher.end()-matcher.start(), Colors.getSystemColor(SWT.COLOR_DARK_GRAY), cell.getBackground());
								srs.add(sr);
							}			
							logger.trace("srs: "+srs.size());
							if (!srs.isEmpty()) {
								cell.setStyleRanges(srs.toArray(new StyleRange[srs.size()]));
							}
						}
					}
					else if (cn.equals(TAG_COL)) {
						Color c = TagConfWidget.getTagColor(txt, SWT.COLOR_BLACK);
						StyleRange sr = new StyleRange(0, txt.length(), c, null);
						cell.setStyleRanges(new StyleRange[] { sr } );
					}

					cell.setText(txt);
				}
			}
		});
		

	}
	
	private void initBtns(Composite container) {
		btnsContainer = new Composite(container, 0);
		btnsContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnsContainer.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		Button reloadBtn = new Button(btnsContainer, SWT.PUSH);
		reloadBtn.setImage(Images.REFRESH);
		reloadBtn.setToolTipText("Refreshes the tag list for the loaded transcript");
		SWTUtil.onSelectionEvent(reloadBtn, (e) -> {
			refreshTable();
		});
		
		Button clearTagsBtn = new Button(btnsContainer, SWT.PUSH);
//		clearTagsBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		clearTagsBtn.setImage(Images.DELETE);
//		clearTagsBtn.setText("Delete selected");
		clearTagsBtn.setToolTipText("Deletes the selected tags from the list");
		SWTUtil.onSelectionEvent(clearTagsBtn, e -> {
			List<CustomTag> selected = getSelectedTags();
			logger.debug("deleting selected tags: "+selected.size());
			if (!selected.isEmpty()) {
				TrpMainWidget.getInstance().deleteTags(selected);
			}
			refreshTable();
		});
	}
	
	public Composite getBtnsContainer() {
		return btnsContainer;
	}
	
	public synchronized void refreshTable() {
		tagList.clear();
		
		if (store.getTranscript()!= null && store.getTranscript().getPage()!=null) {
			List<CustomTag> tagsForPage = CustomTagUtil.extractCustomTags(store.getTranscript().getPage(), true);
			tagList.addAll(tagsForPage);
		}
		
		for (CustomTag t : tagList) {
			logger.debug("tag: "+t);
		}

		tv.setInputAndSortAgain(tagList);
//		tv.setInput(tagList);
//		TaggingWidgetUtils.updateEditors(delSelectedEditors, tagList);
	}
	
	public TableViewer getTableViewer() {
		return tv;
	}
	
	public boolean isSelectedTagsOfSameType() {
		List<CustomTag> selected = getSelectedTags();
		if (selected.size() <= 0) {
			return true;
		}
		
		CustomTag ref = selected.get(0);
		for (int i=1; i<selected.size(); ++i) {
			CustomTag comp = selected.get(i);
			if (!ref.getTagName().equals(comp.getTagName())) {
				return false;
			}
		}
		return true;
	}
	
	public CustomTag getSelectedTag() {
		List<CustomTag> selected = getSelectedTags();
		if (!selected.isEmpty()) {
			return selected.get(0);
		}
		else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<CustomTag> getSelectedTags() {
		return ((IStructuredSelection) tv.getSelection()).toList();
//		return (CustomTag) ((IStructuredSelection) tv.getSelection()).getFirstElement();
	}
	
	public synchronized void setDisableTagUpdate(boolean disableTagUpdate) {
		this.disableTagUpdate = disableTagUpdate;
	}
	
	public boolean isDisableTagUpdate() {
		return this.disableTagUpdate;
	}
	
	private void printTagHashCodes() {
		for (CustomTag t : tagList) {
			logger.debug("t-hashcode: "+t.getCssStr()+": "+t.hashCode());
		}
	}
	
	private void printCurrentTranscriptHashCodes() {
		if (store.getTranscript()!= null && store.getTranscript().getPage()!=null) {
			logger.debug("transcript: "+store.getTranscript()+" page: "+store.getTranscript().getPage());
			logger.debug("cls:");
			for (TrpTextLineType l : store.getTranscript().getPage().getLines()) {
				logger.debug("l: "+l+" cl: "+l.getCustomTagList().hashCode());
			}
			
			List<CustomTag> tagsForPage = CustomTagUtil.extractCustomTags(store.getTranscript().getPage(), true);
			for (CustomTag t : tagsForPage) {
				logger.debug("t-hashcode1: "+t.getCssStr()+": "+t.hashCode());
			}		
		}
	}

	public synchronized void updateSelectedTag(List<CustomTag> tags) {
		if (false) {
			printTagHashCodes();
			printCurrentTranscriptHashCodes();
		}
		logger.debug("updating selected tag, isDisableTagUpdate: "+isDisableTagUpdate()+" tags: "+tags);
		if (isDisableTagUpdate()) {
			return;
		}	
		
		tv.setSelection(new StructuredSelection(tags), true);
	}
	
	public List<CustomTag> getTagsAsSortedInUi() {
		TableItem[] items = tv.getTable().getItems();
		
		List<CustomTag> tagsAsInUi = new ArrayList<>();
		for (int i = 0; i < items.length; i++) {
			tagsAsInUi.add((CustomTag) items[i].getData());
		}
		
		return tagsAsInUi;
	}

}
