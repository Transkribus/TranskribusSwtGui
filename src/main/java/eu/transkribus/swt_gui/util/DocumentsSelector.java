package eu.transkribus.swt_gui.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.DocSelection;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.util.APreviewListViewer;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;

public class DocumentsSelector extends APreviewListViewer<TrpDocMetadata> {
	private final static Logger logger = LoggerFactory.getLogger(DocPageViewer.class);
	
	public static final String ID_COL = "ID";
	public static final String TITLE_COL = "Title";
	public static final String N_PAGES_COL = "N-Pages";
	public static final String PAGES_COL = "Pages";
	
	public static final ColumnConfig[] COLS = new ColumnConfig[] {
		new ColumnConfig(ID_COL, 65, true, DefaultTableColumnViewerSorter.DESC),
		new ColumnConfig(TITLE_COL, 150, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(N_PAGES_COL, 75, false, DefaultTableColumnViewerSorter.ASC),
	};
	
	public static final ColumnConfig[] COLS_WITH_PAGES = new ColumnConfig[] {
			new ColumnConfig(ID_COL, 65, true, DefaultTableColumnViewerSorter.DESC),
			new ColumnConfig(TITLE_COL, 150, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(N_PAGES_COL, 75, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(PAGES_COL, 75, false, DefaultTableColumnViewerSorter.ASC),
		};	
	
	boolean withPagesSelector;
	Map<Integer, String> pagesStrs=new HashMap<>(); // holds specific pages string for a document-ID; if null, all pages are selected!
		
	public DocumentsSelector(Composite parent, int style, boolean showUpDownBtns, boolean withCheckboxes) {
		this(parent, style, showUpDownBtns, withCheckboxes, false);
	}

	public DocumentsSelector(Composite parent, int style, boolean showUpDownBtns, boolean withCheckboxes, boolean withPagesSelector) {
		super(parent, style, withPagesSelector ? COLS_WITH_PAGES : COLS, null, showUpDownBtns, withCheckboxes, false);
		this.setLabelProvider(new ITableLabelProvider() {
			@Override public void removeListener(ILabelProviderListener listener) {
			}
			
			@Override public boolean isLabelProperty(Object element, String property) {
				return true;
			}
			
			@Override public void dispose() {
			}
			
			@Override public void addListener(ILabelProviderListener listener) {
			}
			
			@Override public String getColumnText(Object element, int columnIndex) {
				if (!(element instanceof TrpDocMetadata)) {
					return "i am error";
				}
				
				String cn = columns[columnIndex].name;
				TrpDocMetadata d = (TrpDocMetadata) element;
				
				if (cn.equals(ID_COL)) {
					return ""+d.getDocId();
				}
				else if (cn.equals(TITLE_COL)) {
					return d.getTitle();
				}
				else if (cn.equals(N_PAGES_COL)) {
					return ""+d.getNrOfPages();
				}
				else if (cn.equals(PAGES_COL)) {
					return getPagesStrForDoc(d);
				}
				
				return null;
			}
			
			@Override public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
		});
		
		this.withPagesSelector = withPagesSelector;
		if (this.withPagesSelector) {
			attachCellEditors(tv, this);
		}
	}
	
	public String getPagesStrForDoc(TrpDocMetadata md) {
		String pagesStr = pagesStrs.get(md.getDocId());
		return pagesStr==null ? "1-"+md.getNrOfPages() : pagesStr;
	}
	
	public Map<Integer, String> getPagesStrs() {
		return pagesStrs;
	}

	private String setPagesStrForDoc(TrpDocMetadata md, String pagesStr) {
		if (StringUtils.isEmpty(pagesStr)) {
			return pagesStrs.remove(md.getDocId());
		}
		else {
			return pagesStrs.put(md.getDocId(), pagesStr);	
		}
	}
	
	/**
	 * Recover a selection of DocSelection objects
	 */
	public void setPreviousSelection(List<DocSelection> checkedDocSelections) {
		for (TableItem ti : tv.getTable().getItems()) {
			TrpDocMetadata md = (TrpDocMetadata) ti.getData();
			DocSelection ds = checkedDocSelections.stream().filter(d -> d.getDocId()==md.getDocId()).findFirst().orElse(null);
			ti.setChecked(ds!=null);
//			if (ds != null) {
//				logger.debug("docId123 = "+md.getDocId()+" pages = "+ds.getPages());
//			}
			
			if (ds != null && !StringUtils.isEmpty(ds.getPages())) {
				setPagesStrForDoc(md, ds.getPages());
				tv.refresh(md);
			}
		}
	}
	
	TableItem findTableItem(TrpDocMetadata md) {
		if (md == null) {
			return null;
		}
		
		for (TableItem ti : tv.getTable().getItems()) {
			if (ti.getData() == md) {
				return ti;
			}
		}
		return null;
	}
	
	private void attachCellEditors(final TableViewer viewer, Composite parent) {
//		viewer.setUseHashlookup(true);
		
		// create and set column names:
		String[] colNames = new String[columns.length];
		for (int i=0; i<colNames.length; ++i) {
			colNames[i] = columns[i].name;
		}
		viewer.setColumnProperties(colNames);
		
		// create and set cell editors:
		CellEditor[] cellEditors = new CellEditor[columns.length];
		for (int i=0; i<colNames.length; ++i) {
			cellEditors[i] = null;
			if (columns[i].name.equals(PAGES_COL)) {
				cellEditors[i] = new TextCellEditor(viewer.getTable());
			}
		}		
		viewer.setCellEditors(cellEditors);
		
		// create and set the cell modifier:
	    viewer.setCellModifier(new ICellModifier() {
			public boolean canModify(Object element, String property) {
				if (PAGES_COL.equals(property)) {
					TableItem ti = findTableItem((TrpDocMetadata) element);
					if (ti != null && ti.getChecked()) {
						return true;
					}
				}
				return false;
			}

	      public Object getValue(Object element, String property) {
	    	  logger.trace("getValue, element="+element+", property="+property);
	    	  TrpDocMetadata d = (TrpDocMetadata) element;
	    	  if (property.equals(PAGES_COL)) {
	    		  return getPagesStrForDoc(d); 
	    	  }
	    	  else {
	    		  return "i am error";  
	    	  }
	      }

	      public void modify(Object element, String property, Object value) {
	    	  TrpDocMetadata d = (TrpDocMetadata) ((TableItem) element).getData(); // note: here, element is a TableItem!!
	    	  logger.debug("modify, element="+element+", property="+property+", value="+value);
	    	  String pagesStr = value==null ? "" : ""+value;
	    	  
	    	  if (StringUtils.isEmpty(pagesStr) || CoreUtils.isValidRangeListStr(pagesStr, d.getNrOfPages())) {
	    		  setPagesStrForDoc(d, ""+value);
	    		  viewer.refresh(d);
	    	  }
	      }
	    });
	  }
		
	public List<TrpDocMetadata> getCheckedDocuments() {
		return getCheckedDataList();
	}
	
	public List<DocSelection> getCheckedDocSelections() {
		logger.debug("getCheckedDocSelections, pagesStrs = "+CoreUtils.mapToString(pagesStrs));
		return getCheckedDocuments().stream()
				.map(d -> {
					String pagesStr = pagesStrs.get(d.getDocId());
					logger.debug("pagesStr = "+pagesStr);
					// if all pages selected -> clear pagesStr -> = select all pages!
					if (!StringUtils.isEmpty(pagesStr) && pagesStr.equals("1-"+d.getNrOfPages())) { // necessary !?
						logger.debug("clearing pagesStr as all pages are selected!");
						pagesStr = null;
					}
					return new DocSelection(d.getDocId(), pagesStr, null, null);
				})
				.collect(Collectors.toList());
	}
	
	public List<DocumentSelectionDescriptor> getCheckedDocumentDescriptors() {
		List<DocumentSelectionDescriptor> dsds = new ArrayList<>();
		for (TrpDocMetadata d : getCheckedDocuments()) {
			DocumentSelectionDescriptor dsd = new DocumentSelectionDescriptor(d.getDocId());
//			if (this.withPagesSelector) { // FIXME does not work properly and has not been tested
//				String pagesStr = pagesStrs.get(dsd.getDocId());
//				if (!StringUtils.isEmpty(pagesStr)) {
//					try {
//						TrpDoc doc = Storage.getInstance().getConnection().getTrpDoc(Storage.getInstance().getCollId(), d.getDocId(), 1);
//						dsd = DocumentSelectionDescriptor.fromDocAndPagesStr(doc, pagesStr);
//					} catch (Exception e) {
//						DialogUtil.showErrorMessageBox(getShell(), "Error parsing pages", "Could not parse pages: "+pagesStr+", doc: "+d+"\nShould not happen here...");
//						logger.debug(e.getMessage(), e);
//					}
//				}
//			}
			dsds.add(dsd);
		}
		return dsds;
	}

	@Override
	protected Control createPreviewArea(Composite previewContainer) {
		return null;
	}

	@Override
	protected void reloadPreviewForSelection() {
	}

}