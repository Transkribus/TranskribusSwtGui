package eu.transkribus.swt_gui.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;

import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagUtil;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableLabelProvider;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.TableViewerUtils;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class TagListWidget extends Composite {
	
	MyTableViewer tv;
//	TreeViewer treeViewer;
	
	Map<CustomTag, ControlEditor> delSelectedEditors = new HashMap<>();
	Button clearTagsBtn;
	
	List<CustomTag> tagList = new ArrayList<>();
	
	public static final String DOC_COL = "Doc";
//	public static final String TITLE_COL = "Title";
	public static final String PAGE_COL = "Page";
	public static final String REGION_COL = "Region";
//	public static final String LINE_COL = "Line";
//	public static final String WORD_COL = "Word";
	public static final String TAG_COL = "Tag";
	public static final String CONTEXT_COL = "Text";
	public static final String TAG_VALUE_COL = "Value";
	
	public static final ColumnConfig[] RESULT_COLS = new ColumnConfig[] {
		new ColumnConfig(TAG_COL, 150, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(TAG_VALUE_COL, 150, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(CONTEXT_COL, 250, false, DefaultTableColumnViewerSorter.ASC),
//		new ColumnConfig(DOC_COL, 60, true, DefaultTableColumnViewerSorter.ASC),
//		new ColumnConfig(PAGE_COL, 60, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(REGION_COL, 60, false, DefaultTableColumnViewerSorter.ASC),
	};
	
	static Storage store = Storage.getInstance();

	public TagListWidget(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());
		
		Composite container = new Composite(this, 0);
//		container.setLayout(new GridLayout(1, false));
		container.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
//		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Label headerLbl = new Label(container, 0);
		headerLbl.setText("Tags in selected region");
		Fonts.setBoldFont(headerLbl);
		headerLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		tv = new MyTableViewer(container, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.VIRTUAL);
		tv.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		tv.getTable().setHeaderVisible(true);
		tv.getTable().setLinesVisible(true);
		
		tv.addColumns(RESULT_COLS);
		
		tv.setContentProvider(ArrayContentProvider.getInstance());
		
		final int CONTEXT_LENGTH=15;
		
		final MyTableLabelProvider mtlp = new MyTableLabelProvider() {
			@Override public String getColumnText(String cn, Object element, Object data) {
				
				if (element instanceof CustomTag) {
					CustomTag t = (CustomTag) element;
					
//					if (cn.equals(DOC_COL)) {
//						return ""+t.getDocid();
//					}
	//				else if (cn.equals(TITLE_COL)) {
	//				}
//					else if (cn.equals(PAGE_COL)) {
//						int pgnr = t.getPagenr();
//						return pgnr<10? "0"+pgnr : ""+pgnr;
//					}
					
					if (cn.equals(REGION_COL)) {
						if (t.getCustomTagList()!=null && t.getCustomTagList().getShape()!=null) {
							return t.getCustomTagList().getShape().getId();	
						} else {
							return "";
						}
					}
//					else if (cn.equals(LINE_COL)) {
//						return "";
//					}
//					else if (cn.equals(WORD_COL)) {
//						return "";
//					}		
					else if (cn.equals(TAG_COL)) {
						return t.getCssStr();
					}
					else if (cn.equals(CONTEXT_COL)) {
						return t.getLeftContext(CONTEXT_LENGTH)+t.getContainedText()+t.getRightContext(CONTEXT_LENGTH);
					}
					else if (cn.equals(TAG_VALUE_COL)) {
						return t.getContainedText();
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

					cell.setText(txt);
				}
			}
		});
		
//		tv = new TableViewer(container);
//		tv.setContentProvider(new ArrayContentProvider());
//		tv.getTable().setHeaderVisible(false);
//		tv.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		
//		TableViewerColumn tagCol = TableViewerUtils.createTableViewerColumn(tv, SWT.LEFT, "Tags in region", 300);
//		
//		tagCol.setLabelProvider(new CellLabelProvider() {
//			@Override public void update(ViewerCell cell) {
//				final CustomTag tag = (CustomTag) cell.getElement();
//				cell.setText(tag.getCssStr());
//			}
//		});
		
		class DeleteTagDefSelectionListener extends SelectionAdapter {
			CustomTag tag;
			
			public DeleteTagDefSelectionListener(CustomTag tag) {
				this.tag = tag;
			}
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tag != null) {
				 	if (TrpMainWidget.getInstance() != null) {
				 		TrpMainWidget.getInstance().deleteTags(tag);
				 	}
				}
			}
		};
		
		TableViewerColumn deleteTagCol = TableViewerUtils.createTableViewerColumn(tv, SWT.LEFT, "", 50);
		deleteTagCol.setLabelProvider(new CellLabelProvider() {
			@Override public void update(ViewerCell cell) {
				final CustomTag tag = (CustomTag) cell.getElement();
//				String tagName = tag.getTagName();
				
				final TableItem item = (TableItem) cell.getItem();
				TableEditor editor = new TableEditor(item.getParent());
				Button removeButton = new Button((Composite) cell.getViewerRow().getControl(), SWT.PUSH);
		        removeButton.setImage(Images.getOrLoad("/icons/delete_12.png"));
		        removeButton.setToolTipText("Delete this tag");
		        removeButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
		        removeButton.addSelectionListener(new DeleteTagDefSelectionListener(tag));
		        Control c = removeButton;
				
				c.pack();
				   
                Point size = c.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                
				editor.minimumWidth = size.x;
				editor.horizontalAlignment = SWT.LEFT;
                editor.setEditor(c , item, cell.getColumnIndex());
                editor.layout();
                
//                TaggingWidgetUtils.replaceEditor(delSelectedEditors, tagName, editor);
                TaggingWidgetUtils.replaceEditor(delSelectedEditors, tag, editor);
			}
		});
		
//		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
//			@Override public void selectionChanged(SelectionChangedEvent event) {
//				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
//				String selectedTagName = sel.isEmpty() ? null : ((CustomTag) sel.getFirstElement()).getTagName();
//				selectTagname(selectedTagName);				
//			}
//		});
		
		Composite btnsContainer = new Composite(container, 0);
		btnsContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnsContainer.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		Button reloadBtn = new Button(btnsContainer, SWT.PUSH);
		reloadBtn.setImage(Images.REFRESH);
		reloadBtn.setToolTipText("Reload tags for page");
		SWTUtil.onSelectionEvent(reloadBtn, (e) -> {
			refreshTable();
		});
		
		Button clearTagsBtn = new Button(btnsContainer, SWT.PUSH);
//		clearTagsBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		clearTagsBtn.setImage(Images.DELETE);
		clearTagsBtn.setText("Delete tags for selection");
		clearTagsBtn.setToolTipText("Clears all tags from the current selection in the transcription widget");
//		clearTagsBtn.addSelectionListener(new TagActionSelectionListener(this, listener, TaggingActionType.CLEAR_TAGS));
//		clearTagsBtn.addSelectionListener(new ClearTagsSelectionListener(listener));
		// TODO: add clear tags listener
		
		
		store.addListener(new IStorageListener() {
			 public void handleTranscriptLoadEvent(TranscriptLoadEvent arg) {
				 refreshTable();
			 }
		});
	}
	
	public void refreshTable() {
		tagList.clear();
		
		if (store.getTranscript()!= null && store.getTranscript().getPage()!=null) {
			List<CustomTag> tagsForPage = CustomTagUtil.extractCustomTags(store.getTranscript().getPage(), true);
			tagList.addAll(tagsForPage);
		}
		
		Display.getCurrent().asyncExec(() -> {
			tv.setInput(tagList);
			TaggingWidgetUtils.updateEditors(delSelectedEditors, tagList);
		});
	}
	
	public TableViewer getTableViewer() {
		return tv;
	}
	
	public CustomTag getSelected() {
		return (CustomTag) ((IStructuredSelection) tv.getSelection()).getFirstElement();
	}

}
