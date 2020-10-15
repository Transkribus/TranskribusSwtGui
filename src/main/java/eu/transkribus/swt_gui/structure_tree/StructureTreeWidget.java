package eu.transkribus.swt_gui.structure_tree;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
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

import eu.transkribus.core.model.beans.pagecontent.RegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.canvas.CanvasSettings;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class StructureTreeWidget extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(StructureTreeWidget.class);

	public static class ColConfig {
		public ColConfig(String name, int colSize, String db_name) {
			super();
			this.name = name;
			this.colSize = colSize;
			this.dbName = db_name;
		}

		public String name;
		public int colSize;
		public String dbName;

	}

	Tree tree;
	TreeViewer treeViewer;
	ToolItem clearPageItem, deleteSelectedBtn;
	ToolItem updateIDsItem, expandAll, collapseAll, setReadingOrderRegions, assignGeometrically /*, setReadingOrderLines, setReadingOrderWords*/;
	//ToolItem deleteReadingOrderRegions;
	
	ToolItem moveUpButton, moveDownButton;
	
	private StructureTreeDragSourceAdapter structTreeDragSourceAdapter;
	private StructureTreeDropAdapter structTreeDropAdapter;
	
	/**
	 * ToolItems that trigger any kind of editing are collected in this array and disabled in case the edit mode restricts certain actions.
	 */
	private final ArrayList<ToolItem> editToolItems;

	public final static ColConfig TYPE_COL = new ColConfig("Type", 110, "");
	public final static ColConfig ID_COL = new ColConfig("ID", 65, "");
	public final static ColConfig TEXT_COL = new ColConfig("Text", 100, "");
	public final static ColConfig COORDS_COL = new ColConfig("Coords", 200, "");
	public final static ColConfig STRUCTURE_TYPE_COL = new ColConfig("Structure", 100, "");
	public final static ColConfig READING_ORDER_COL = new ColConfig("Reading Order", 50, "");
	public final static ColConfig OTHER_COL = new ColConfig("Other", 100, "");

	public final static ColConfig[] COLUMNS = new ColConfig[] { TYPE_COL, TEXT_COL, STRUCTURE_TYPE_COL, READING_ORDER_COL, ID_COL, COORDS_COL, OTHER_COL };
	
	static final int UP = 0;
	static final int DOWN = 1;
	
	public StructureTreeWidget(Composite parent) {
		super(parent, SWT.NONE);
		this.editToolItems = new ArrayList<>(7);
		this.setLayout(new GridLayout());
		initToolBar();

		treeViewer = new TreeViewer(this, SWT.FULL_SELECTION | SWT.MULTI);
		treeViewer.setContentProvider(new StructureTreeContentProvider());
		treeViewer.getTree().setHeaderVisible(true);
		treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		tree = treeViewer.getTree();
		
		structTreeDropAdapter = new StructureTreeDropAdapter(treeViewer);
		structTreeDragSourceAdapter = new StructureTreeDragSourceAdapter();

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
		editToolItems.add(clearPageItem);
		
		deleteSelectedBtn = new ToolItem(toolBar, 0);
		deleteSelectedBtn.setToolTipText("Delete selected shapes");
		deleteSelectedBtn.setImage(Images.DELETE);
		editToolItems.add(deleteSelectedBtn);

		updateIDsItem = new ToolItem(toolBar, SWT.NONE);
		updateIDsItem.setToolTipText("Assigns unique IDs to all elements according to their current sorting");
		updateIDsItem.setImage(Images.getOrLoad("/icons/update_id.png"));
		editToolItems.add(updateIDsItem);
		
		setReadingOrderRegions = new ToolItem(toolBar, SWT.NONE);
		setReadingOrderRegions.setToolTipText("Sets the reading order of the children of the selected element(s) according to their coordinates!");
		setReadingOrderRegions.setImage(Images.getOrLoad("/icons/reading_order_r.png"));
		editToolItems.add(setReadingOrderRegions);
		
		assignGeometrically = new ToolItem(toolBar, SWT.NONE);
		assignGeometrically.setToolTipText("Assign child shapes to selected shape according to geometric overlap. If page is selected, all shapes will be reinserted according to geometric overlap");
		assignGeometrically.setImage(Images.getOrLoad("/icons/layout.png"));
		editToolItems.add(assignGeometrically);
//		
//		deleteReadingOrderRegions = new ToolItem(toolBar, SWT.NONE);
//		deleteReadingOrderRegions.setToolTipText("Deletes the reading order from the children of the selected element(s)!");
//		deleteReadingOrderRegions.setImage(Images.getOrLoad("/icons/reading_order_r_delete.png"));	
		
		moveUpButton = new ToolItem(toolBar, SWT.NONE);
		moveUpButton.setToolTipText("move shape up");
		moveUpButton.setImage(Images.getOrLoad("/icons/up2.gif"));
		editToolItems.add(moveUpButton);

		moveDownButton = new ToolItem(toolBar, SWT.NONE);
		moveDownButton.setToolTipText("move shape down");
		moveDownButton.setImage(Images.getOrLoad("/icons/down2.gif"));
		editToolItems.add(moveDownButton);
		
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
		
		treeViewer.addDragSupport(operations, transferTypes, structTreeDragSourceAdapter);
		treeViewer.addDropSupport(operations, transferTypes, structTreeDropAdapter);
	}
	
	void setDragAndDropEnabled(boolean enabled) {
		structTreeDragSourceAdapter.setEnabled(enabled);
		structTreeDropAdapter.setEnabled(enabled);
	}
	
	
	public ToolItem getUpdateIDsItem() {
		return updateIDsItem;
	}
	
	public ToolItem getClearPageItem() {
		return clearPageItem;
	}
	
	public ToolItem getDeleteSelectedBtn() {
		return deleteSelectedBtn;
	}
	
	public ToolItem getSetReadingOrderRegions() {
		return setReadingOrderRegions;
	}
	
	public ToolItem getAssignGeometrically() {
		return assignGeometrically;
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
			column.setLabelProvider(new StructureTreeLabelProvider(treeViewer, false));

//			if (cf.equals(STRUCTURE_TYPE_COL)) { // disable here because should be done in StructuralMetadataWidget
//				column.setEditingSupport(new StructureTypeEditingSupport(treeViewer));
//			}
			
			if (cf.equals(READING_ORDER_COL)) {
				column.setEditingSupport(new ReadingOrderEditingSupport(this, treeViewer));
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
		
		//refresh the canvas
		TrpMainWidget.getInstance().getCanvas().redraw();
		TrpMainWidget.getInstance().getCanvas().update();

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
		
		TrpMainWidget.getInstance().getCanvas().redraw();
		TrpMainWidget.getInstance().getCanvas().update();

		
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
	
	/**
	 * En-/disables toolbar items that trigger changes in the structure and drag/drop support on the treeviewer.
	 * Further edit operations, e.g. "del" key press, are caught via the Canvas write lock (see {@link CanvasSettings#setEditingEnabled(boolean)}).
	 * 
	 * @param enabled
	 */
	public void setEditingEnabled(boolean enabled) {
		for(ToolItem i : editToolItems) {
			SWTUtil.setEnabled(i, enabled);
		}
		setDragAndDropEnabled(enabled);
	}
	
	private class StructureTreeDragSourceAdapter extends DragSourceAdapter {
		boolean isEnabled = true;
		@Override public void dragStart(DragSourceEvent event) {
			if(!isEnabled) {
				return;
			}
			LocalSelectionTransfer.getTransfer().setSelection(treeViewer.getSelection()); // not really needed since we can get selection from member variable
		}
//		@Override public void dragSetData(DragSourceEvent event) {
//		}
//		@Override public void dragFinished(DragSourceEvent event) {
//		}
		void setEnabled(boolean enabled) {
			this.isEnabled = enabled;
		}
	}
}
