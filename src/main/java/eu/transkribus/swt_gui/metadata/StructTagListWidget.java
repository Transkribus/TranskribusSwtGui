package eu.transkribus.swt_gui.metadata;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.structure_tree.StructureTreeContentProvider;
import eu.transkribus.swt_gui.structure_tree.StructureTreeLabelProvider;
import eu.transkribus.swt_gui.structure_tree.StructureTreeListener;
import eu.transkribus.swt_gui.structure_tree.StructureTreeWidget;
import eu.transkribus.swt_gui.structure_tree.StructureTreeWidget.ColConfig;
import eu.transkribus.swt_gui.structure_tree.StructureTypeEditingSupport;

public class StructTagListWidget extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(StructTagListWidget.class);
	
	public final static ColConfig[] COLUMNS = new ColConfig[] { 
			StructureTreeWidget.TYPE_COL, 
			StructureTreeWidget.STRUCTURE_TYPE_COL,
			StructureTreeWidget.TEXT_COL, 
//			StructureTreeWidget.READING_ORDER_COL, 
			StructureTreeWidget.ID_COL, 
//			StructureTreeWidget.COORDS_COL, 
//			StructureTreeWidget.OTHER_COL
			};
	
	TreeViewer viewer;
	Storage store = Storage.getInstance();
	
	private final class StructureTreeContentProvider2 extends StructureTreeContentProvider {
		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof TrpTextLineType) {
				return ((TrpTextLineType) parentElement).getWord().toArray();
			}
			else {
				return super.getChildren(parentElement);
			}
		}
		
//		@Override
//		public Object getParent(Object element) {
//			if (element instanceof TrpBaselineType) {
//				return null;
//			}
//			else {
//				return super.getParent(element);
//			}
//		}
//
//		@Override
//		public boolean hasChildren(Object element) {
//			if (element instanceof TrpTextLineType) {
//				return false;
//			}
//			else {
//				return super.hasChildren(element);
//			}
//		}
	}
	
	public StructTagListWidget(Composite parent, int style) {
		super(parent, style);

		this.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		
		Label l = new Label(this, 0);
		l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		l.setText("Layout");
		Fonts.setBoldFont(l);
		
		viewer = new TreeViewer(this, SWT.FULL_SELECTION | SWT.SINGLE);
		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setContentProvider(new StructureTreeContentProvider2());
		viewer.expandAll();
		
		IStorageListener storageListener = new IStorageListener() {
			public void handleTranscriptLoadEvent(TranscriptLoadEvent event) {
				viewer.setInput(store.getTranscript().getPage());
				viewer.expandAll();
			}
		};
		store.addListener(storageListener);
		StructureTreeListener listener = new StructureTreeListener(viewer, true);
		
//		viewer.addSelectionChangedListener(new StructureTreeListener(viewer, false));
//		viewer.addDoubleClickListener(new StructureTreeListener(viewer, false));
		
		this.addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(DisposeEvent e) {
				store.removeListener(storageListener);
				listener.detach(); // not needed I guess...
			}
		});
		
		initCols();
	}
	
	private void initCols() {
		for (ColConfig cf : COLUMNS) {
			if (cf.equals(StructureTreeWidget.TEXT_COL)) {
				cf.colSize = 200;
			}			
			
			TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.MULTI);
			column.getColumn().setText(cf.name);
			column.getColumn().setWidth(cf.colSize);
			column.setLabelProvider(new StructureTreeLabelProvider(viewer, true));

			if (cf.equals(StructureTreeWidget.STRUCTURE_TYPE_COL)) {
				column.setEditingSupport(new StructureTypeEditingSupport(viewer));
			}
			
//			if (cf.equals(StructureTreeWidget.READING_ORDER_COL)) {
//				column.setEditingSupport(new ReadingOrderEditingSupport(this, treeViewer));
//			}
		}
	}
	
	public TreeViewer getTreeViewer() {
		return viewer;
	}
	
//	public void setInput(Object input) {
//		viewer.setInput(input);
//		viewer.expandAll();
//	}

}
