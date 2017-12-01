package eu.transkribus.swt_gui.search.text_and_tags;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.InvocationCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDbTag;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.customtags.CssSyntaxTag;
import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagAttribute;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory;
import eu.transkribus.core.model.beans.customtags.CustomTagUtil;
import eu.transkribus.core.model.beans.customtags.search.CustomTagSearchFacets;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableLabelProvider;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.progress.ProgressBarDialog;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.LazyTableViewerArrayContentProvider;
import eu.transkribus.swt.util.MapContentProvider;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.TableLabelProvider;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.metadata.CustomTagSearcher;

public class TagSearchComposite extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(TagSearchComposite.class);

	Combo scopeCombo, tagNameInput, propNameCombo, regionTypeCombo;
	Text tagValueInput;
	Button searchBtn, showNormalizeWidgetBtn;
	
	Text propValueTxt;
	
	Label resultsLabel;
	Button caseSensitiveCheck, exactMatchCheck;
	
	Group facetsGroup;
	
	MyTableViewer propsTable;
	MyTableViewer resultsTable;
	
	Map<String, Object> props = new HashMap<String, Object>();
	
	TagNormalizationWidget tagNormWidget;
	SashForm resultsSf;
	
//	Future<?> tagSearchFut;
	static long lastSearch=0; // time of last search, used for canceling existing search tasks
	
	static final String SEARCH_BTN_NOT_SEARCHING = "Search!";
	static final String SEARCH_BTN_SEARCHING = "Cancel!";

	protected static final String SCOPE_DOC = "Current document";
	protected static final String SCOPE_PAGE = "Current page";
	protected static final String SCOPE_REGION = "Current region";
	protected static final String SCOPE_COLL = "Current collection";
	
	String[] SCOPES = new String[] { SCOPE_COLL, SCOPE_DOC, SCOPE_PAGE, SCOPE_REGION };
	
	public static final String PROP_COL = "Property";
	public static final String VALUE_COL = "Value";
	public static final ColumnConfig[] PROPS_COLS = new ColumnConfig[] {
		new ColumnConfig(PROP_COL, 100, true, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(VALUE_COL, 150, false, DefaultTableColumnViewerSorter.ASC),
	};
	
	public static final String DOC_COL = "Doc";
	public static final String TITLE_COL = "Title";
	public static final String PAGE_COL = "Page";
	public static final String REGION_COL = "Region";
	public static final String LINE_COL = "Line";
	public static final String WORD_COL = "Word";
	public static final String TAG_COL = "Tag";
	public static final String CONTEXT_COL = "Text";
	public static final String TAG_VALUE_COL = "Value";
	
	public static final ColumnConfig[] RESULT_COLS = new ColumnConfig[] {
		new ColumnConfig(TAG_COL, 150, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(TAG_VALUE_COL, 150, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(CONTEXT_COL, 250, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(DOC_COL, 60, true, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(PAGE_COL, 60, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(REGION_COL, 60, false, DefaultTableColumnViewerSorter.ASC),
	};
	
	public TagSearchComposite(Composite parent, int style) {
		super(parent, style);
		createContents();
	}
	
	protected void createContents() {
		this.setLayout(new FillLayout());
		Composite c = new Composite(this, 0);
		c.setLayout(new FillLayout());
		
		SashForm sf = new SashForm(c, SWT.VERTICAL);
		sf.setLayout(new GridLayout(1, false));
		
		facetsGroup = new Group(sf, SWT.NONE);
		facetsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		facetsGroup.setLayout(new GridLayout(4, false));
//		facetsGroup.setText("Search facets - use * and ? as wildcards for multiple or one unknown character");
		
		Composite optionsComp = new Composite(facetsGroup, 0);
		optionsComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		optionsComp.setLayout(new GridLayout(4, false));
		
//		Label scopeLabel = new Label(optionsComp, 0);
//		scopeLabel.setText("Search scope: ");
		
		scopeCombo = new Combo(optionsComp, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		scopeCombo.setItems(SCOPES);
		scopeCombo.select(0);
		
//		Label regionTypeLabel = new Label(optionsComp, 0);
//		regionTypeLabel.setText("Text level: ");
		
		regionTypeCombo = new Combo(optionsComp, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		regionTypeCombo.setItems(new String[] {"Line level",  "Word level"});
		regionTypeCombo.select(0);
		
		caseSensitiveCheck = new Button(optionsComp, SWT.CHECK);
		caseSensitiveCheck.setText("Case sensitive");
		
		exactMatchCheck = new Button(optionsComp, SWT.CHECK);
		exactMatchCheck.setText("Exact match");
						
		Label labelTagName = new Label(facetsGroup, 0);
		labelTagName.setText("Name: ");
		
		TraverseListener findTagsOnEnterListener = new TraverseListener() {
			@Override public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					logger.debug("finding tags!");
					findTags();
				}
			}
		};
		
		tagNameInput = new Combo(facetsGroup, SWT.SIMPLE | SWT.DROP_DOWN | SWT.BORDER);
		tagNameInput.setToolTipText("The name of tag to search");
		tagNameInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		updateTagNames();
		tagNameInput.addTraverseListener(findTagsOnEnterListener);
		tagNameInput.addModifyListener(new ModifyListener() {
			@Override public void modifyText(ModifyEvent e) {
				updateTagProps();
			}
		});
		
		Label labelTagValue = new Label(facetsGroup, 0);
		labelTagValue.setText("Text: ");
		
		tagValueInput = new Text(facetsGroup, SWT.SINGLE | SWT.BORDER);
		tagValueInput.setToolTipText("The text this tag contains (if any)");
		tagValueInput.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tagValueInput.addTraverseListener(findTagsOnEnterListener);	

		// PROPERTIES STUFF		
		Label labelProperties = new Label(facetsGroup, 0);
		labelProperties.setText("Properties to search:");
		labelProperties.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		Composite propsComposite = new Composite(facetsGroup, SWT.SHADOW_ETCHED_IN);
		propsComposite.setLayout(new GridLayout(2, false));
		propsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		
		Composite addPropsC = new Composite(propsComposite, 0);
		addPropsC.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		addPropsC.setLayout(new GridLayout(6, false));
		Label addLabelPropertiesLabel = new Label(addPropsC, 0);
		addLabelPropertiesLabel.setText("Add property to search: ");
		propNameCombo = new Combo(addPropsC, SWT.SIMPLE | SWT.DROP_DOWN | SWT.BORDER);
		propNameCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		propNameCombo.setToolTipText("The name of the property to search");
				
		Label lV = new Label(addPropsC, 0);
		lV.setText("Value: ");
		propValueTxt = new Text(addPropsC, SWT.SINGLE | SWT.BORDER);
		propValueTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		propValueTxt.setToolTipText("The value of the property to search - can be empty");
		
		// update property value depending on type of selected attribute
		ModifyListener attributeTextUpdateListener =  new ModifyListener() {
			@Override public void modifyText(ModifyEvent e) {
				String tn = tagNameInput.getText();
				String pn = propNameCombo.getText();
				
				logger.info("modified tagName or attribute name: "+tn+" / "+pn);
				
				propValueTxt.setText("");
				propValueTxt.setEnabled(true);
				
				CustomTagAttribute ca = CustomTagFactory.getAttribute(tn, pn);
				logger.info("ca = "+ca);
				if (ca != null && ca.isBoolean()) {
					propValueTxt.setText("true");
					propValueTxt.setEnabled(false);
				}
			}
		};
		
		tagNameInput.addModifyListener(attributeTextUpdateListener);
		propNameCombo.addModifyListener(attributeTextUpdateListener);		
		
		Button addPropBtn = new Button(addPropsC, SWT.PUSH);
		addPropBtn.setImage(Images.ADD);
		addPropBtn.setToolTipText("Add this property to the search query");
		addPropBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				if (StringUtils.isEmpty(propNameCombo.getText())) {
					return;
				}
				
				if (StringUtils.isEmpty(propValueTxt.getText())) {
					DialogUtil.showErrorMessageBox(getShell(), "Error adding property", "Cannot search  for an empty property value");
					return;
				}
				
				String pn = propNameCombo.getText();
				String value = propValueTxt.getText();
				
				logger.debug("adding property: "+pn+" value: "+value);
				
				props.put(pn, value);
				propsTable.refresh();
			}
		});
				
//		new Label(parameters, 0); // placeholder label
		
		Button deletePropBtn = new Button(addPropsC, SWT.PUSH);
		deletePropBtn.setImage(Images.DELETE);
		deletePropBtn.setToolTipText("Deletes the selected property search facets from the list");
		deletePropBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection) propsTable.getSelection();
				Iterator it = sel.iterator();
				while (it.hasNext()) {
					Map.Entry<String, Object> v = (Map.Entry<String, Object>) it.next();
					props.remove(v.getKey());
				}
				propsTable.refresh();
			}
		});		
		
		propsTable = new MyTableViewer(propsComposite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2);
		gd.heightHint = 40;
		propsTable.getTable().setLayoutData(gd);
		propsTable.getTable().setHeaderVisible(true);
		propsTable.addColumns(PROPS_COLS);
		propsTable.setContentProvider(new MapContentProvider());
		propsTable.setLabelProvider(new TableLabelProvider() {	
			@Override public String getColumnText(Object element, int columnIndex) {
				String cn = PROPS_COLS[columnIndex].name;
				Map.Entry<String, Object> e = (Map.Entry<String, Object>) element;
				if (cn.equals(PROP_COL)) {
					return e.getKey();
				} else if (cn.equals(VALUE_COL)) {
					return e.getValue() == null ? "" : e.getValue().toString();
				}
				
				return "i am error";
			}
		});
		propsTable.setInput(props);	
		// END OF PROPERTIES STUFF
		
//		Label lSpace = new Label(facetsGroup, 0);
		
		Composite btnsComp = new Composite(facetsGroup, 0);
		btnsComp.setLayout(new FillLayout(SWT.HORIZONTAL));
		btnsComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		searchBtn = new Button(btnsComp, SWT.PUSH);
		
		toogleSearchBtn(false);		
		searchBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				if (searchBtn.getText().equals(SEARCH_BTN_NOT_SEARCHING)) {
					findTags();
				} else {
					lastSearch = 0;
					toogleSearchBtn(false);
				}
			}
		});
		
		initResultsTable(sf);
		
		sf.setWeights(new int[] { 50, 40 } );
		
		getShell().addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				lastSearch = 0;
			}
		});
				
		c.addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(DisposeEvent e) {
				logger.debug("disposed!");
			}
		});

	}
	
	protected boolean isNormalizationPossible() {
		return showNormalizeWidgetBtn.getSelection();
	}
	
	protected void updateNormalizationWidgetVisibility() {		
		if (isNormalizationPossible())
			resultsSf.setWeights(new int[] {66,34});
		else
			resultsSf.setWeights(new int[] {100,0});
		
		updateNormalizationSelection();
	}
		
	void initResultsTable(Composite container) {
		Group resultsGroup = new Group(container, SWT.NONE);
		resultsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		resultsGroup.setText("Search results");
		resultsGroup.setLayout(new GridLayout(2, false));

		resultsLabel = new Label(resultsGroup, 0);
		resultsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		showNormalizeWidgetBtn = new Button(resultsGroup, SWT.TOGGLE);
		showNormalizeWidgetBtn.setText("Normalize properties...");
		showNormalizeWidgetBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				updateNormalizationWidgetVisibility();
			}
		});
		showNormalizeWidgetBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		
		resultsSf = new SashForm(resultsGroup, SWT.HORIZONTAL);
		resultsSf.setLayout(new GridLayout(1, false));
		resultsSf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		resultsTable = new MyTableViewer(resultsSf, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.VIRTUAL);
		resultsTable.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		resultsTable.getTable().setHeaderVisible(true);
		resultsTable.getTable().setLinesVisible(true);
		
		resultsTable.addColumns(RESULT_COLS);
		
		final MyTableLabelProvider mtlp = new MyTableLabelProvider() {
			@Override public String getColumnText(String cn, Object element, Object data) {
				
				if (element instanceof TrpDbTag) {
					TrpDbTag t = (TrpDbTag) element;
					
					if (cn.equals(DOC_COL)) {
						return ""+t.getDocid();
					}
	//				else if (cn.equals(TITLE_COL)) {
	//				}
					else if (cn.equals(PAGE_COL)) {
						int pgnr = t.getPagenr();
						return pgnr<10? "0"+pgnr : ""+pgnr;
					}
					else if (cn.equals(REGION_COL)) {
						return t.getRegionid();
					}
//					else if (cn.equals(LINE_COL)) {
//						return "";
//					}
//					else if (cn.equals(WORD_COL)) {
//						return "";
//					}		
					else if (cn.equals(TAG_COL)) {
						return t.getCustomTagCss();
					}
					else if (cn.equals(CONTEXT_COL)) {
						String b = t.getContextBefore()==null ? "" : t.getContextBefore();
						String a = t.getContextAfter()==null ? "" : t.getContextAfter();
						
						return b+t.getValue()+a; // TODO: store context in DB!???
					}
					else if (cn.equals(TAG_VALUE_COL)) {
						return t.getValue();
					}
					
					return "";
				}

				return "i am error";
			}
		};
		
		// set custom sorters (needed as this table is virtual!):
		for (final TableColumn tc : resultsTable.getTable().getColumns()) {
			resultsTable.setCustomListSorterForColumn(tc.getText(), new Comparator<TrpDbTag>() {
				@Override public int compare(TrpDbTag o1, TrpDbTag o2) {
					// NOTE: no debug output here, it will be called very often! 
					
					String t1 = mtlp.getColumnText(tc.getText(), o1, null);
					String t2 = mtlp.getColumnText(tc.getText(), o2, null);
					
					if (t1 == null)
						t1 = "";
					if (t2 == null)
						t2 = "";				
					
					return t1.compareTo(t2);
				}
			});
		}
				
//		resultsTable.setContentProvider(new ArrayContentProvider());
		
		resultsTable.setContentProvider(LazyTableViewerArrayContentProvider.instance());
		resultsTable.setUseHashlookup(true);
//		resultsTable.setItemCount(100);
				
		resultsTable.setLabelProvider(new StyledCellLabelProvider() {
			public void update(ViewerCell cell) {
				if (cell.getElement() instanceof TrpDbTag) {
					int ci = cell.getColumnIndex();
					String cn = RESULT_COLS[ci].name;
					TrpDbTag t = (TrpDbTag) cell.getElement();
					
					String txt = mtlp.getColumnText(cn, cell.getElement(), null);
					if (cn.equals(CONTEXT_COL)) {
						int o=StringUtils.length(t.getContextBefore());
						int l=StringUtils.length(t.getValue());
						
						if (CoreUtils.isInIndexRange(o, 0, txt.length()) && CoreUtils.isInIndexRange(o+l, 0, txt.length())) {
							StyleRange sr = new StyleRange(o, l, cell.getForeground(), Colors.getSystemColor(SWT.COLOR_YELLOW));
							cell.setStyleRanges(new StyleRange[] { sr } );
						}
					}

					cell.setText(txt);
				}
			}
		});

		resultsTable.addDoubleClickListener(new IDoubleClickListener() {
			@Override public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (sel.getFirstElement() instanceof TrpDbTag) {
					TrpDbTag t = (TrpDbTag) sel.getFirstElement();
					logger.debug("opening tag: "+t);
					
					// TODO: convert TrpDbTag to TrpLocation -> including range of text!
					TrpLocation l = new TrpLocation();
					l.collId = t.getCollId();
					l.docId = t.getDocid();
//					l.pageid = t.getPageid();
					l.pageNr = t.getPagenr();
					l.shapeId = t.getRegionid();
					l.t = CustomTagUtil.parseSingleCustomTag2(t.getCustomTagCss());
					
					TrpMainWidget.getInstance().showLocation(l);
				}
				
			}
		});
		
		resultsTable.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override public void selectionChanged(SelectionChangedEvent event) {
				updateNormalizationSelection();
			}
		});
				
		tagNormWidget = new TagNormalizationWidget(resultsSf, 0);
		tagNormWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		updateNormalizationWidgetVisibility();
	}
	
	protected List<TrpDbTag> getSelectedTags() {
		return ((IStructuredSelection) resultsTable.getSelection()).toList();
	}
	
	protected void updateNormalizationSelection() {
		logger.debug("updating norm selection...");
		if (!isNormalizationPossible()) {
			tagNormWidget.setInput(null);
			return;
		}
		
		List<TrpDbTag> selTags = getSelectedTags();
		logger.debug("selTags: "+selTags);
		tagNormWidget.setInput(selTags);
		tagNormWidget.redraw();
	}
	
	void updateTagProps() {
		CustomTag t = CustomTagFactory.getTagObjectFromRegistry(tagNameInput.getText());
		List<String> attributes = new ArrayList<>();
		
		if (t!=null) {
			for (String an : t.getAttributeNames()) {
				if (!CustomTag.isOffsetOrLengthOrContinuedProperty(an)) {
					attributes.add(an);
				}
			}
		}
		Collections.sort(attributes);
		
		if (t != null) {
			propNameCombo.setItems(attributes.toArray(new String[0]));
		} else {
			propNameCombo.setItems(new String[] {});
		}
	}

	public void updateResults(List<TrpDbTag> searchResult) {
		if (SWTUtil.isDisposed(resultsLabel) || SWTUtil.isDisposed(resultsTable.getControl())) {
			return;
		}
		
		int N = searchResult == null ? 0 : searchResult.size();

		logger.debug("updating results table, N = "+N);
		resultsLabel.setText(N+" matches");

		resultsTable.setInput(searchResult);
		resultsTable.setItemCount(N);
		resultsTable.refresh();
	}
	
	void updateTagNames() {		
		tagNameInput.setItems((String[]) CustomTagFactory.getRegisteredTagNamesSorted().toArray(new String[0]));
	}

	public String getScope() {
		return scopeCombo.getText();
	}

	public CustomTagSearchFacets getFacets() {
		return new CustomTagSearchFacets(tagNameInput.getText(), tagValueInput.getText(), props, exactMatchCheck.getSelection(), 
				caseSensitiveCheck.getSelection(), regionTypeCombo.getSelectionIndex()==1 ? "Word" : "Line");
	}
		
	void toogleSearchBtn(boolean isSearching) {
//		searchBtn.setEnabled(isSearching);
		if (!isSearching) {
			searchBtn.setImage(Images.FIND);
			searchBtn.setText(SEARCH_BTN_NOT_SEARCHING);
			searchBtn.setToolTipText("Finds all matches in the selected scope using the specified facets and displays the results in the table below");
		} else {
			searchBtn.setImage(Images.CROSS);
			searchBtn.setText(SEARCH_BTN_SEARCHING);
			searchBtn.setToolTipText("Finds all matches in the selected scope using the specified facets and displays the results in the table below");
		}
	}

	protected void findTags() {
		final CustomTagSearchFacets f = getFacets();
		
		String scope = getScope();
		
		if (StringUtils.isEmpty(f.getTagName(false))) {
			if (scope.equals(SCOPE_COLL) || scope.equals(SCOPE_DOC)) {
				DialogUtil.showErrorMessageBox(getShell(), "Error searching for tags", "Cannot search for all tags in collection or document - result set may be too large!");
				return;
			}			
		}
		else {
			if (StringUtils.length(f.getTagName(false)) < 3) {
				DialogUtil.showErrorMessageBox(getShell(), "Error searching for tags", "Please specify a valid tagname - at least 3 characters are needed!");
				return;
			}
		}
		
		logger.debug("searching tags, facets: "+f);
	
		final Storage s = Storage.getInstance();
		final TrpMainWidget mw = TrpMainWidget.getInstance();
				
		logger.debug("searching on scope: "+scope);
		
		boolean useDbSearch = scope.equals(SCOPE_COLL) || (scope.equals(SCOPE_DOC) && !s.isLocalDoc());
		
		final TrpCollection currCol =  mw.getUi().getServerWidget().getSelectedCollection();
		int collId = currCol == null ? -1 : currCol.getColId();				
		
		try {
			if (useDbSearch) {
				if (!s.isLoggedIn()) {
					DialogUtil.showErrorMessageBox(getShell(), "Not logged in", "You need to connect to the server to search for tags in remote documents!");
					return;
				}

				Set<Integer> collIds = null;
				Set<Integer> docIds = null;
				
				if (scope.equals(SCOPE_COLL)) {
					collIds = CoreUtils.createSet(collId);
				} else {
					docIds = CoreUtils.createSet(s.getDocId());
				}
								
				toogleSearchBtn(true);
				lastSearch = System.currentTimeMillis();
				
				InvocationCallback<List<TrpDbTag>> callback = new InvocationCallback<List<TrpDbTag>>() {
					long time = TagSearchComposite.lastSearch;
					
					@Override
					public void completed(List<TrpDbTag> tags) {
						if (time != TagSearchComposite.lastSearch) {
							logger.debug("search was canceled: "+tags.size());
							return;
						}
						
						logger.debug("found "+tags.size()+" in DB!");
						List<TrpDbTag> searchResult = new ArrayList<>();
						searchResult.addAll(tags);
						searchResult.forEach((t) -> { t.setCollId(collId); });
						
						Display.getDefault().asyncExec(() -> {
							updateResults(searchResult);
							toogleSearchBtn(false);
						});
					}

					@Override public void failed(Throwable throwable) {
						Display.getDefault().asyncExec(() -> {
							TrpMainWidget.getInstance().onError("Error searching tags", throwable.getMessage(), throwable);
							toogleSearchBtn(false);
						});
					}
				};	
				
				s.getConnection().searchTagsAsync(collIds, docIds, null, f.getTagName(false), f.getTagValue(false), f.getRegionType(), f.isExactMatch(), f.isCaseSensitive(), f.getProps(), callback);

				// synchronized version:
//				List<TrpDbTag> tags = s.getConnection().searchTags(collIds, docIds, null, f.getTagName(false), f.getTagValue(false), regionType, f.isExactMatch(), f.isCaseSensitive(), f.getProps());
//				logger.debug("found "+tags.size()+" in DB!");
//
//				List<TrpDbTag> searchResult = new ArrayList<>();
//				searchResult.addAll(tags);
//				searchResult.forEach((t) -> { t.setCollId(collId); });
//				
//				updateResults(searchResult);
			} else {
				List<TrpDbTag> searchResult = new ArrayList<>();
				updateResults(searchResult);
				
				ProgressBarDialog pd = new ProgressBarDialog(getShell()) {
					@Override public void subTask(final String name) {
						super.subTask(name);
						Display.getDefault().syncExec(new Runnable() {
							@Override public void run() {
								Shell s = TagSearchComposite.this.getShell();
								if (!s.isDisposed()) {
									updateResults(searchResult);
								}
							}
						});
					}
				};	
				
				if (scope.equals(SCOPE_COLL)) {
					if (currCol == null) {
						DialogUtil.showErrorMessageBox(getShell(), "Error", "No collection selected!");
						return;
					}

					pd.open(new IRunnableWithProgress() {
						@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							try {
								CustomTagSearcher.searchOnCollection_WithoutIndex(collId, searchResult, f, monitor);
								
							} catch (SessionExpiredException | IllegalArgumentException | NoConnectionException e) {
								throw new InvocationTargetException(e);
							}
						}
					}, "Searching in collection "+currCol.getColName(), true);
					updateResults(searchResult);
				}
				else if (scope.equals(SCOPE_DOC)) {
					if (!s.isDocLoaded()) {
						DialogUtil.showErrorMessageBox(getShell(), "Error", "No document loaded!");
						return;
					}
					String docTitle = s.getDoc().getMd() != null ? s.getDoc().getMd().getTitle() : "NA";
					
					pd.open(new IRunnableWithProgress() {
						@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							CustomTagSearcher.searchOnDoc_WithoutIndex(searchResult, s.getCurrentDocumentCollectionId(), s.getDoc(), f, 0, 0, 0, false, 0, false, monitor, false);
						}
					}, "Searching in document "+docTitle, true);
					updateResults(searchResult);
				}
				else if (scope.equals(SCOPE_PAGE)) {
					if (!s.isPageLoaded() || s.getTranscript().getPageData() == null) {
						DialogUtil.showErrorMessageBox(getShell(), "Error", "No page loaded!");
						return;
					}
					TrpPageType p = s.getTranscript().getPage();
					
					CustomTagSearcher.searchOnPage(searchResult, s.getCurrentDocumentCollectionId(), s.getDocId(), p, f, 0, 0, false, 0, false);
					updateResults(searchResult);
				} else if (scope.equals(SCOPE_REGION)) {
					TrpTextRegionType r = s.getCurrentRegionObject();
					if (r==null) {
						DialogUtil.showErrorMessageBox(getShell(), "Error", "No region selected!");
						return;
					}
						
					CustomTagSearcher.searchOnRegion(searchResult, s.getCurrentDocumentCollectionId(), s.getDocId(), s.getPage().getPageNr(), s.getPage().getPageId(), s.getTranscriptMetadata().getTsId(), r, f, 0, false, 0, false);
					updateResults(searchResult);
				}
			}
		}
		catch (Throwable e) {
			mw.onError("Error in tag search", e.getMessage(), e);
			return;
		}
	}
	
//	/**
//	 * @deprecated not used anymore -> inefficient (stores pages multiple times if multiple tags on a page!)
//	 */
//	protected static void saveAffectedPages(IProgressMonitor monitor, List<TrpDbTag> selectedTags) 
//		throws Exception {
//		// TODO Auto-generated method stub
//		if (monitor != null)
//			monitor.beginTask("Updating tag values", selectedTags.size());
//		
//		Storage s = Storage.getInstance();
//		int c = 0;
//		
//		Map<Integer, TrpPageType> pagesCache = new HashMap<>();
//		
//		for (TrpDbTag t : selectedTags) {
//			if (monitor != null && monitor.isCanceled())
//				return;
//			
//			TrpPageType pt = pagesCache.get(t.getPageid());
//			if (pt == null) { // page not be found in cache -> unmarshall!
//				// retrieve current transcript
//				TrpPage page = s.getConnection().getTrpDoc(t.getCollId(), t.getDocid(), 1).getPages().get(t.getPagenr()-1);
//				pt = s.getOrBuildPage(page.getCurrentTranscript(), true);
//				pagesCache.put(t.getPageid(), pt);
//			}
//
//			// convert DbTag to CustomTag
//			// parse CssTag
//			CssSyntaxTag cssTag =  CssSyntaxTag.parseSingleCssTag(t.getCustomTagCss());
//						
//			CustomTag ct = CustomTagFactory.create(cssTag.getTagName(), t.getOffset(), t.getLength(), cssTag.getAttributes());
//
//			// retrieve parent line / shape
//			TrpTextLineType lt = pt.getLineWithId(t.getRegionid());
//			
//			// add or merge tag on line
//			lt.getCustomTagList().addOrMergeTag(ct, null, true);
//			
//			// save new transcript
//			s.saveTranscript(t.getCollId(), pt, null, t.getTsid(), "Tagged from text in normalization ");
//			
//			if (monitor != null)
//				monitor.worked(c++);
//		}
//		logger.debug("saveAffectedPages, pages loaded: "+pagesCache.size());
//	}
	
	public static void main(String [] args) {
		Shell shell = new Shell();
		shell.setLayout(new FillLayout());
		shell.setSize(800, 700);
		Display display = shell.getDisplay();
		
		TagSearchComposite c = new TagSearchComposite(shell, 0);
		
		SWTUtil.centerShell(shell);

		shell.open();
		while (!shell.isDisposed())
		if (!display.readAndDispatch()) display.sleep();
	}
}

