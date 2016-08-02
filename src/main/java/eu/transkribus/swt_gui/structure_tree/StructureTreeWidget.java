package eu.transkribus.swt_gui.structure_tree;

import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.customtags.StructureTag;
import eu.transkribus.core.model.beans.pagecontent.RegionType;
import eu.transkribus.core.model.beans.pagecontent.TextLineType;
import eu.transkribus.core.model.beans.pagecontent.TextTypeSimpleType;
import eu.transkribus.core.model.beans.pagecontent.WordType;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.core.util.EnumUtils;
import eu.transkribus.swt_canvas.util.Images;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class StructureTreeWidget extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(StructureTreeWidget.class);

	public static class ColConfig {
		public ColConfig(String name, int colSize) {
			super();
			this.name = name;
			this.colSize = colSize;
		}

		public String name;
		public int colSize;

	}

	Tree tree;
	TreeViewer treeViewer;
	ToolItem clearPageItem;
	ToolItem updateIDsItem, expandAll, collapseAll, setReadingOrderRegions /*, setReadingOrderLines, setReadingOrderWords*/;
	//ToolItem deleteReadingOrderRegions;
	
	ToolItem moveUpButton;
	ToolItem moveDownButton;
	
	

	public final static ColConfig TYPE_COL = new ColConfig("Type", 100);
	public final static ColConfig ID_COL = new ColConfig("ID", 65);
	public final static ColConfig TEXT_COL = new ColConfig("Text", 100);
	public final static ColConfig COORDS_COL = new ColConfig("Coords", 200);
	public final static ColConfig STRUCTURE_TYPE = new ColConfig("Structure", 100);
	public final static ColConfig READING_ORDER_TYPE = new ColConfig("Reading Order", 50);

	public final static ColConfig[] COLUMNS = new ColConfig[] { TYPE_COL, TEXT_COL, STRUCTURE_TYPE, READING_ORDER_TYPE, ID_COL, COORDS_COL };
	
	static final int UP = 0;
	static final int DOWN = 1;
	
	/**
	 * @wbp.parser.constructor
	 */
	public StructureTreeWidget(Composite parent) {
		super(parent, SWT.NONE);

		this.setLayout(new GridLayout());
		initToolBar();

		treeViewer = new TreeViewer(this, SWT.FULL_SELECTION | SWT.MULTI);
		treeViewer.setContentProvider(new StructureTreeContentProvider());
		treeViewer.getTree().setHeaderVisible(true);
		treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		tree = treeViewer.getTree();

		// initEditOnDoubleClick();
		initCols();
		
		initListener();
		initDragAndDrop();
	}
	
	void initToolBar() {
		ToolBar toolBar = new ToolBar(this, SWT.FLAT | SWT.RIGHT);
		toolBar.setBounds(0, 0, 93, 25);
		
		expandAll = new ToolItem(toolBar, SWT.NONE);
		expandAll.setToolTipText("Expand all");
		expandAll.setImage(Images.getOrLoad("/icons/expandall.gif"));
		
		collapseAll = new ToolItem(toolBar, SWT.NONE);
		collapseAll.setToolTipText("Collapse all");
		collapseAll.setImage(Images.getOrLoad("/icons/collapseall.png"));
		
		clearPageItem = new ToolItem(toolBar, 0);
		clearPageItem.setToolTipText("Clear page content");
		clearPageItem.setImage(Images.CROSS);

		updateIDsItem = new ToolItem(toolBar, SWT.NONE);
		updateIDsItem.setToolTipText("Assigns unique IDs to all elements according to their current sorting");
//		updateIDsItem.setImage(Images.getOrLoad("/icons/refresh.gif"));
		updateIDsItem.setImage(Images.getOrLoad("/icons/update_id.png"));
//		updateIDsItem.setText("Update IDs");
		
		setReadingOrderRegions = new ToolItem(toolBar, SWT.NONE);
		setReadingOrderRegions.setToolTipText("Sets the reading order of the children of the selected element(s) according to their coordinates!");
		setReadingOrderRegions.setImage(Images.getOrLoad("/icons/reading_order_r.png"));
//		
//		deleteReadingOrderRegions = new ToolItem(toolBar, SWT.NONE);
//		deleteReadingOrderRegions.setToolTipText("Deletes the reading order from the children of the selected element(s)!");
//		deleteReadingOrderRegions.setImage(Images.getOrLoad("/icons/reading_order_r_delete.png"));	
		
		moveUpButton = new ToolItem(toolBar, SWT.NONE);
		moveUpButton.setToolTipText("move shape up");
		moveUpButton.setImage(Images.getOrLoad("/icons/up2.gif"));

		moveDownButton = new ToolItem(toolBar, SWT.NONE);
		moveDownButton.setToolTipText("move shape down");
		moveDownButton.setImage(Images.getOrLoad("/icons/down2.gif"));
		
//		setReadingOrderLines = new ToolItem(toolBar, SWT.NONE);
//		setReadingOrderLines.setToolTipText("Sets the reading order of lines in the selected regions according to their y-x-coordinates!");
//		setReadingOrderLines.setImage(Images.getOrLoad("/icons/reading_order_lines.png"));
//		
//		setReadingOrderWords = new ToolItem(toolBar, SWT.NONE);
//		setReadingOrderWords.setToolTipText("Sets the reading order of words in the selected lines according to their x-y-coordinates!");
//		setReadingOrderWords.setImage(Images.getOrLoad("/icons/reading_order_words.png"));		
	}
	
	void initListener() {
		expandAll.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				treeViewer.expandAll();
			}
		});
		collapseAll.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				treeViewer.collapseAll();
			}
		});
		
		moveUpButton.addListener(SWT.Selection, new StructureTreeMoveListener(SWT.UP, this));
		moveDownButton.addListener(SWT.Selection, new StructureTreeMoveListener(SWT.DOWN, this));
	}
	
	void initDragAndDrop() {
		int operations = DND.DROP_MOVE;
		Transfer[] transferTypes = new Transfer[]{ LocalSelectionTransfer.getTransfer() };
		treeViewer.addDragSupport(operations, transferTypes, new DragSourceAdapter() {			
			@Override public void dragStart(DragSourceEvent event) {
				LocalSelectionTransfer.getTransfer().setSelection(treeViewer.getSelection()); // not really needed since we can get selection from member variable
			}
//			@Override public void dragSetData(DragSourceEvent event) {
//			}
//			@Override public void dragFinished(DragSourceEvent event) {
//			}
		});
		
		treeViewer.addDropSupport(operations, transferTypes, new ViewerDropAdapter(treeViewer) {
//			@Override
//			  public void drop(DropTargetEvent event) {
//				logger.debug("drop: "+event);
//				int location = this.determineLocation(event);
//			    ITrpShapeType target = (ITrpShapeType) determineTarget(event);
//			    
//			    String translatedLocation ="";
//			    switch (location) {
//			    case LOCATION_BEFORE :
//			      translatedLocation = "Dropped before the target ";
//			      break;
//			    case LOCATION_AFTER :
//			      translatedLocation = "Dropped after the target ";
//			      break;
//			    case LOCATION_ON :
//			      translatedLocation = "Dropped on the target ";
//			      break;
//			    case LOCATION_NONE :
//			      translatedLocation = "Dropped into nothing ";
//			      break;
//			    }
//			    logger.debug(translatedLocation);
//			    logger.debug("The drop was done on the element: " + target.getId());
//			    super.drop(event);
//			  }

			@Override public boolean performDrop(Object data) {
				// Note: data is null here since the Transfer type is LocalSelectionTransfer
				logger.debug("performorming drop!");
				
			    // perform dropping of data:
				IStructuredSelection sel = (IStructuredSelection) LocalSelectionTransfer.getTransfer().getSelection();

				
				ITrpShapeType targetSt = (ITrpShapeType) getCurrentTarget();
				
				int targetRo = 0;
				int targetIdx = 0;
				int oldIdx = 0;
				
				List<TrpRegionType> regions = null;
				List<TextLineType> lines = null;
				List<WordType> words = null;
				
				if (targetSt != null){
					ITrpShapeType parentShape = targetSt.getParentShape();
					TrpTextRegionType parentRegion = null;
					TrpTextLineType parentLine = null;

					if (targetSt instanceof TrpTextLineType){
						//parent is a region
						lines = ((TrpTextRegionType) parentShape).getTextLine();
					}
					else if(targetSt instanceof TrpWordType){
						//parentLine is a line;
						words = ((TrpTextLineType) parentShape).getWord();
					}
					else if(targetSt instanceof TrpRegionType){
						//get all regions
						regions = ((TrpRegionType) targetSt).getPage().getTextRegionOrImageRegionOrLineDrawingRegion(); 
					}
					
				}
				
				if (targetSt.getReadingOrder() != null){
					targetRo = targetSt.getReadingOrder();
				}
				
				int location = this.determineLocation(getCurrentEvent());					
				int newRo = 0;


				switch (location) {
				case LOCATION_BEFORE:

					Iterator it = sel.iterator();
					int count = 0;
					while (it.hasNext()) {
						ITrpShapeType st = (ITrpShapeType) it.next();

						newRo = targetRo + count;
						
						if (regions != null){
							oldIdx = regions.indexOf(st);
						}
						else if(lines != null){
							oldIdx = lines.indexOf(st);
						}
						else if (words != null){
							oldIdx = words.indexOf(st);
						}
						
						//drag form list
						st.removeFromParent();
						
						//get target idx only after removing drag shape because index changes if dragged before the target
						if (regions != null){
							logger.debug("regions != null");
							targetIdx = regions.indexOf(targetSt);
						}
						else if(lines != null){
							logger.debug("lines != null");
							targetIdx = lines.indexOf(targetSt);
						}
						else if (words != null){
							logger.debug("word != null");
							targetIdx = words.indexOf(targetSt);
						}
						
						if (targetIdx == -1 && oldIdx != -1){
							st.reInsertIntoParent(oldIdx);
						}
						else{
							logger.debug("new targetIdx: " + targetIdx);
							st.reInsertIntoParent(targetIdx);
						}
						
						count++;
						
						logger.debug("ro after reinsert: " + st.getReadingOrder());
												
					}
											
					logger.debug("Dropped before the target " + targetSt.getId() + " with index: " + targetIdx);
					break;
				case LOCATION_AFTER:
					//if (isLine && st instanceof TrpTextLineType){
					//newTarget = targetSt.getParent();
					Iterator it2 = sel.iterator();
					int count2 = 0;
					while (it2.hasNext()) {
						ITrpShapeType st = (ITrpShapeType) it2.next();

						newRo = targetRo+1+count2;
						
						if (regions != null){
							oldIdx = regions.indexOf(st);
						}
						else if(lines != null){
							oldIdx = lines.indexOf(st);
						}
						else if (words != null){
							oldIdx = words.indexOf(st);
						}
						//drag form list
						st.removeFromParent();
						
						boolean insert = false;
						
						//get target idx only after removing drag shape because index changes if dragged before the target
						if (regions != null){
							targetIdx = regions.indexOf(targetSt);
							insert = (targetIdx < (regions.size()-1));
						}
						else if(lines != null){
							targetIdx = lines.indexOf(targetSt);
							insert = (targetIdx < (lines.size()-1));
						}
						else if (words != null){
							targetIdx = words.indexOf(targetSt);
							insert = (targetIdx < (words.size()-1));
						}
						
						//st.setReadingOrder(newRo, StructureTreeWidget.this);
						
						if (targetIdx == -1 && oldIdx != -1){
							st.reInsertIntoParent(oldIdx);
						}
						else{
							targetIdx += 1;
							if (insert){
								st.reInsertIntoParent(targetIdx);
							}
							else{
								st.reInsertIntoParent();
							}
						}
						
						count2++;

					}
					logger.debug("Dropped after the target " + targetSt.getId() + " with index: " + targetIdx);
					break;
				case 3:
					logger.debug("Dropped on the target " + targetSt.getId());
					//newTarget = targetSt;
					break;
				case 4:
					logger.debug("Dropped into nothing");
					break;
				}

				
				TrpMainWidget.getInstance().getScene().updateAllShapesParentInfo();
				targetSt.getPage().sortContent();
				
//				treeViewer.refresh();
//				if (targetSt!=null)
//					treeViewer.refresh(targetSt.getParent(), true);
//				getViewer().refresh();
				return true;
			}


			@Override public boolean validateDrop(Object target, int operation, TransferData transferType) {
				int location = this.determineLocation(getCurrentEvent());
				
				if (!(target instanceof ITrpShapeType) || target instanceof TrpBaselineType)
					return false;
				
				ITrpShapeType targetSt = (ITrpShapeType) target;
				
				boolean isRegion = targetSt instanceof TrpTextRegionType;
				boolean isLine = targetSt instanceof TrpTextLineType;
				boolean isWord = targetSt instanceof TrpWordType;
				
				String targetParentId = "parentID";
				//because region has page as parent which is no shape type
				
				Object targetParent = targetSt.getParent();
				
				if (targetParent instanceof ITrpShapeType){
					ITrpShapeType targetStParent = (ITrpShapeType) targetParent;
					targetParentId = targetStParent.getId();
				}	

				if (location == LOCATION_ON) {
					//no parent switching?
					return false;
//					// can only add to regions and lines
//					if (!isRegion && !isLine)
//						return false;
//					// now check if all dragged elements are 'addable' to the target:
//					IStructuredSelection sel = (IStructuredSelection) treeViewer.getSelection();
//					Iterator<?> it = sel.iterator();
//					while (it.hasNext()) {
//						ITrpShapeType st = (ITrpShapeType) it.next();
//						// target is region but selected not a line -> do not allow drop
//						if (isRegion && !(st instanceof TrpTextLineType))
//							return false;
//						// target is line but selected not a word -> do not allow drop			
//						else if (isLine && !(st instanceof TrpWordType))
//							return false;
//					}
				}
				else if (location == LOCATION_BEFORE){
//					if (targetSt.getSiblingShape(true)==null && isRegion)
//						return false;
					
					IStructuredSelection sel = (IStructuredSelection) treeViewer.getSelection();
					Iterator<?> it = sel.iterator();
					
					//its not allowed to change the parent -> so dropping is only allowed before the same shape type
					while (it.hasNext()) {
						ITrpShapeType st = (ITrpShapeType) it.next();
						
						Object stParent = st.getParent();
						if (stParent instanceof ITrpShapeType){
							ITrpShapeType stParentShape = (ITrpShapeType) st.getParent();
							// target is region but selected not a region -> do not allow drop
							if (!isRegion && stParentShape.getId() !=  targetParentId){
								return false;
							}
						}
						if (isRegion && !(st instanceof TrpTextRegionType))
							return false;
						// target is line but selected not a line -> do not allow drop			
						else if (isLine && !(st instanceof TrpTextLineType))
							return false;
						else if (isWord && !(st instanceof TrpWordType))
							return false;
					}
				}
				else if (location == LOCATION_AFTER){
					
					IStructuredSelection sel = (IStructuredSelection) treeViewer.getSelection();
					Iterator<?> it = sel.iterator();
					
					//its not allowed to change the parent -> so dropping is only allowed before the same shape type
					while (it.hasNext()) {
						ITrpShapeType st = (ITrpShapeType) it.next();
						Object stParent = st.getParent();
						if (stParent instanceof ITrpShapeType){
							ITrpShapeType stParentShape = (ITrpShapeType) st.getParent();
							// target is region but selected not a region -> do not allow drop
							if (!isRegion && stParentShape.getId() !=  targetParentId){
								return false;
							}
						}
						
						if (isRegion && !(st instanceof TrpTextRegionType))
							return false;
						// target is line but selected not a line -> do not allow drop			
						else if (isLine && !(st instanceof TrpTextLineType))
							return false;
						else if (isWord && !(st instanceof TrpWordType))
							return false;
					}
				}

				return true;
			}

//			@Override public boolean validateDrop(Object target, int operation, TransferData transferType) {
//				int location = this.determineLocation(getCurrentEvent());
//				// do not permit dropping in between elements:
//				if (location != LOCATION_ON) {
//					//return false;
//				}
//				if (!(target instanceof ITrpShapeType))
//					return false;
//				
//				ITrpShapeType targetSt = (ITrpShapeType) target;
//				boolean isRegion = targetSt instanceof TrpTextRegionType;
//				boolean isLine = targetSt instanceof TrpTextLineType;
//
//				// can only add to regions and lines
//				if (!isRegion && !isLine)
//					return false;
//				
//				// now check if all dragged elements are 'addable' to the target:
//				IStructuredSelection sel = (IStructuredSelection) treeViewer.getSelection();
//				Iterator<?> it = sel.iterator();
//				while (it.hasNext()) {
//					ITrpShapeType st = (ITrpShapeType) it.next();
//					
//					// do not allow dropping on same parent:
//					if (st.getParent() == targetSt){
//						//return false;
//					}
//					// target is region but selected not a line -> do not allow drop
//					else if (isRegion && !(st instanceof TrpTextLineType))
//						return false;
//					// target is line but selected not a word -> do not allow drop			
//					else if (isLine && !(st instanceof TrpWordType))
//						return false;
//				}
//				
//				return true;
//			}
		});
	}
	
	
	public ToolItem getUpdateIDsItem() {
		return updateIDsItem;
	}
	
	public ToolItem getClearPageItem() {
		return clearPageItem;
	}
	
	public ToolItem getSetReadingOrderRegions() {
		return setReadingOrderRegions;
	}
	
	

//	public ToolItem getSetReadingOrderLines() {
//		return setReadingOrderLines;
//	}
//
//	public ToolItem getSetReadingOrderWords() {
//		return setReadingOrderWords;
//	}

//	public ToolItem getDeleteReadingOrderRegions() {
//		return deleteReadingOrderRegions;
//	}

	private void initCols() {
		for (ColConfig cf : COLUMNS) {
			TreeViewerColumn column = new TreeViewerColumn(treeViewer, SWT.MULTI);
			column.getColumn().setText(cf.name);
			column.getColumn().setWidth(cf.colSize);
			column.setLabelProvider(new StructureTreeLabelProvider());

			if (cf.equals(STRUCTURE_TYPE)) {
				column.setEditingSupport(new EditingSupport(treeViewer) {
					@Override protected void setValue(Object element, Object value) {
						ITrpShapeType s = (ITrpShapeType) element;
						int i = (int) value;
						if (i >= 1 && i <= TextTypeSimpleType.values().length) {
							s.setStructure(TextTypeSimpleType.values()[i - 1].value(), false, this);
						}
						if (i == 0)
							s.setStructure(null, false, this);
						treeViewer.refresh();
					}

					@Override protected Object getValue(Object element) {
						ITrpShapeType s = (ITrpShapeType) element;
						String struct = s.getStructure();
						return EnumUtils.indexOf(StructureTag.parseTextType(struct)) + 1;
					}

					@Override protected CellEditor getCellEditor(Object element) {
						List<String> values = EnumUtils.valuesList(TextTypeSimpleType.class);
						values.add(0, ""); // add empty string as value to
											// delete structure type!

						return new ComboBoxCellEditor(treeViewer.getTree(), values.toArray(new String[0]), SWT.READ_ONLY);
					}

					@Override protected boolean canEdit(Object element) {
						boolean isPageLocked = Storage.getInstance().isPageLocked();
						boolean isRegionOrLineOrWord = element instanceof TrpTextRegionType || element instanceof TrpTextLineType || element instanceof TrpWordType;
						
						return !isPageLocked && isRegionOrLineOrWord;
					}
				});
			}
			
			if (cf.equals(READING_ORDER_TYPE)) {
				column.setEditingSupport(new EditingSupport(treeViewer) {
					@Override protected void setValue(Object element, Object value) {
						ITrpShapeType s = (ITrpShapeType) element;
						//logger.debug("value is: "+value);
						String valueStr = (String) value;
						//logger.debug("valueStr is: "+valueStr);
					
						if (valueStr.isEmpty()) {
							s.setReadingOrder(null, StructureTreeWidget.this);
						} else {
							try {
								int ro = Integer.parseInt(valueStr);
								logger.debug("++++++++++++reInsertIntoParent(ro) " + (ro-1));
								s.removeFromParent();
								s.reInsertIntoParent(ro-1);
								//s.setReadingOrder(ro, StructureTreeWidget.this);
							} catch (NumberFormatException ne) {
								logger.debug("not a valid number: "+valueStr);
							}
						}
						treeViewer.refresh();
					}

					@Override protected Object getValue(Object element) {
						ITrpShapeType s = (ITrpShapeType) element;
						//increase reding order with one to have sorting from 1 to n instead of 0 to n
						return s.getReadingOrder()==null ? "" : ""+(s.getReadingOrder()+1);
					}

					@Override protected CellEditor getCellEditor(Object element) {
						return new TextCellEditor(treeViewer.getTree());
					}

					@Override protected boolean canEdit(Object element) {
						boolean isPageLocked = Storage.getInstance().isPageLocked();
						boolean isRegionOrLineOrWord = element instanceof TrpRegionType || element instanceof TrpTextLineType || element instanceof TrpWordType;
						return !isPageLocked && isRegionOrLineOrWord;
					}
				});
			}

			// editing support for text column:
			if (cf.equals(TEXT_COL) && false) { // disable editing of text in structure widget -> too dangerous...
				column.setEditingSupport(new EditingSupport(treeViewer) {

					@Override protected void setValue(Object element, Object value) {
						if (element instanceof ITrpShapeType)
							((ITrpShapeType) element).setUnicodeText((String) value, StructureTreeWidget.this);
					}

					@Override protected Object getValue(Object element) {
						String text = "";
						if (element instanceof ITrpShapeType)
							text = ((ITrpShapeType) element).getUnicodeText();

						return text;
					}

					@Override protected CellEditor getCellEditor(Object element) {
						return new TextCellEditor(treeViewer.getTree());
					}

					@Override protected boolean canEdit(Object element) {
//						boolean isPageLocked = Storage.getInstance().isPageLocked();
//						boolean isLineOrWord = element instanceof TrpTextLineType || element instanceof TrpWordType;
//						return !isPageLocked && (isLineOrWord);
						
						return false;
					}
				});

			}

		}

	}

	private void initEditOnDoubleClick() {
		TreeViewerFocusCellManager focusCellManager = new TreeViewerFocusCellManager(treeViewer, new FocusCellOwnerDrawHighlighter(treeViewer));
		ColumnViewerEditorActivationStrategy activationSupport = new ColumnViewerEditorActivationStrategy(treeViewer) {
			@Override
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				// Enable editor only with mouse double click
				if (event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION) {
					EventObject source = event.sourceEvent;
					if (source instanceof MouseEvent && ((MouseEvent) source).button == 3)
						return false;

					return true;
				}

				return false;
			}
		};
		TreeViewerEditor.create(treeViewer, focusCellManager, activationSupport,
		// ColumnViewerEditor.TABBING_HORIZONTAL |
				ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR | ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);
	}

	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	// public Tree getTree() { return tree; }

	public void refreshLabels(Object source) {
		treeViewer.refresh(source, true);
	}
	
	public void updateTextLabels(Object source) {
		if (source != null)
			treeViewer.update(source, new String[] { TEXT_COL.name });
		else
			treeViewer.refresh(true);
//		else
//			treeViewer.update(treeViewer.getTree().get, new String[] { TEXT_COL.name });
	}

	@Deprecated public void updateTreeColumnSize() {
		if (true)
			return;

		int[] maxColSize = new int[tree.getColumnCount()];
		for (int i = 0; i < tree.getColumnCount(); ++i) {
			maxColSize[i] = 0;
		}

		Stack<TreeItem> itemStack = new Stack<TreeItem>();
		TreeItem[] children = this.tree.getItems();

		GC gc = new GC(tree);
		do {
			if (children != null && children.length != 0)
				for (TreeItem child : children) {
					itemStack.push(child);
				}
			// try {
			TreeItem ci = itemStack.pop();
			for (int i = 0; i < tree.getColumnCount(); ++i) {
				int te = gc.textExtent(ci.getText(i)).x;
				// int te = ci.getText(i).length();
				logger.debug("col = " + i + " text = " + ci.getText(i) + " te = " + te);
				if (te > maxColSize[i])
					maxColSize[i] = te;

				// logger.debug("child, col "+i+": "+ci.getText(i)+", bounds = "+ci.getBounds()+", textbounds = "+ci.getTextBounds(0));
			}
			children = ci.getItems();
			// }
			// catch (EmptyStackException e) {
			// break;
			// }
		} while (!itemStack.isEmpty());
		gc.dispose();

		// update size of cols depending on max size of text inside:
		for (int i = 0; i < tree.getColumnCount(); ++i) {
			logger.debug("maxcolsize[" + i + "]: " + maxColSize[i]);

			if (i == 0) {
				this.tree.getColumn(i).setWidth(maxColSize[i] + 60);
			} else
				this.tree.getColumn(i).setWidth(maxColSize[i] + 10);
		}

	}

	public void moveUpItem() {
		logger.debug("move item up");
		IStructuredSelection sel = (IStructuredSelection) treeViewer.getSelection();
		Iterator<?> it = sel.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof TrpPageType) {
				//do nothing;
			} else if (o instanceof RegionType || o instanceof TrpTextLineType || o instanceof TrpWordType){
				logger.debug("type found - call swap UP");
				ITrpShapeType currShape = (ITrpShapeType) o;
				currShape.swap(UP);
			}
		}

	}
	
	public void moveDownItem() {
		logger.debug("move item down");
		IStructuredSelection sel = (IStructuredSelection) treeViewer.getSelection();
		
		List selElementsList = sel.toList();
		
		// Generate an iterator. Start just after the last element.
		ListIterator li = selElementsList.listIterator(selElementsList.size());

		// Iterate in reverse because if two neighbor elements are selected only reverse shifting works 
		while(li.hasPrevious()) {
			Object o = li.previous();
			if (o instanceof TrpPageType) {
				//do nothing;
			} else if (o instanceof RegionType || o instanceof TrpTextLineType || o instanceof TrpWordType){
				ITrpShapeType currShape = (ITrpShapeType) o;
				currShape.swap(DOWN);
			}
		}		
		
//		Iterator<?> it = sel.iterator();
//		while (it.hasNext()) {
//			Object o = it.next();
//			if (o instanceof TrpPageType) {
//				//do nothing;
//			} else if (o instanceof RegionType || o instanceof TrpTextLineType || o instanceof TrpWordType){
//				ITrpShapeType currShape = (ITrpShapeType) o;
//				currShape.swap(DOWN);
//			}
//		}

	}
}
