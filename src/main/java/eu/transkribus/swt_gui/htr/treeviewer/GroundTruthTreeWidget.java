package eu.transkribus.swt_gui.htr.treeviewer;

import java.util.List;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.htr.HtrFilterWidget;
import eu.transkribus.swt_gui.structure_tree.StructureTreeWidget.ColConfig;

public class GroundTruthTreeWidget extends Composite {

	private TreeViewer treeViewer;
	private final CellLabelProvider labelProvider;
	
	private final ITreeContentProvider treeContentProvider;
	
	private Composite filterWidget;
	private Button reloadBtn;
	
	//TODO paging
	//ToolItem clearPageItem, deleteSelectedBtn;
	//List<ToolItem> editToolItems;
	
	public final static ColConfig NAME_COL = new ColConfig("Name", 240);
	public final static ColConfig SIZE_COL = new ColConfig("Size", 150);
	public final static ColConfig CURATOR_COL = new ColConfig("Curator", 240);
	public final static ColConfig ID_COL = new ColConfig("HTR ID", 100);

	public final static ColConfig[] COLUMNS = new ColConfig[] { NAME_COL, SIZE_COL, CURATOR_COL, ID_COL };
	
	public GroundTruthTreeWidget(Composite parent, ITreeContentProvider contentProvider, CellLabelProvider labelProvider) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout(1, true));		
		
		this.treeViewer = new TreeViewer(this, SWT.BORDER | SWT.MULTI);
		this.treeViewer.getTree().setHeaderVisible(true);
		
		//providers may be passed as arguments to make this more flexible
		if(contentProvider != null) {
			this.treeContentProvider = contentProvider;
		} else {
			//default contentProvider shows HTR GT for now
			this.treeContentProvider = new HtrGroundTruthContentProvider(null);
		}
		
		if(labelProvider != null) {
			this.labelProvider = labelProvider;
		} else {
			this.labelProvider = new HtrGroundTruthTableLabelAndFontProvider(treeViewer.getControl().getFont());
		}
		
		this.treeViewer.setContentProvider(this.treeContentProvider);
		this.treeViewer.setLabelProvider(this.labelProvider);
		this.treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite filterAndReloadComp = new Composite(this, SWT.NONE);
		filterAndReloadComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filterAndReloadComp.setLayout(new GridLayout(2, false));
		
		this.filterWidget = new HtrFilterWidget(filterAndReloadComp, treeViewer, SWT.None);
		this.reloadBtn = new Button(filterAndReloadComp, SWT.PUSH);
		reloadBtn.setToolTipText("Reload current page");
		reloadBtn.setImage(Images.getOrLoad("/icons/refresh.gif"));
		reloadBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true));

		initCols();
		
		initListener();
	}
	
	public GroundTruthTreeWidget(Composite parent) {
		this(parent, null, null);
	}
	
	void initToolBar() {
//		ToolBar toolBar = new ToolBar(this, SWT.FLAT | SWT.RIGHT);
//		toolBar.setBounds(0, 0, 93, 25);
//		editToolItems = new ArrayList<>(2);
//		
//		clearPageItem = new ToolItem(toolBar, 0);
//		clearPageItem.setToolTipText("Clear page content");
//		clearPageItem.setImage(Images.CROSS);
//		editToolItems.add(clearPageItem);
//		
//		deleteSelectedBtn = new ToolItem(toolBar, 0);
//		deleteSelectedBtn.setToolTipText("Delete selected shapes");
//		deleteSelectedBtn.setImage(Images.DELETE);
//		editToolItems.add(deleteSelectedBtn);
	}
	
	void initListener() {
		//TODO add listeners for paging tool items
	}
	
	private void initCols() {
		for (ColConfig cf : COLUMNS) {
			TreeViewerColumn column = new TreeViewerColumn(treeViewer, SWT.MULTI);
			column.getColumn().setText(cf.name);
			column.getColumn().setWidth(cf.colSize);
			column.setLabelProvider(labelProvider);
		}
	}

	public TreeViewer getTreeViewer() {
		return treeViewer;
	}
	
	public Button getReloadButton() {
		return reloadBtn;
	}

	public void refreshLabels(Object source) {
		if(source == null) {
			treeViewer.refresh(true);
		} else {
			treeViewer.refresh(source, true);
		}
	}

	public void expandTreeItem(Object o) {
		final ITreeContentProvider provider = (ITreeContentProvider) treeViewer.getContentProvider();
		if(!provider.hasChildren(o)) {
			return;
		}
		if (treeViewer.getExpandedState(o)) {
			treeViewer.collapseToLevel(o, AbstractTreeViewer.ALL_LEVELS);
		} else {
			treeViewer.expandToLevel(o, 1);
		}
	}

	public void setInput(List<TrpHtr> treeViewerInput) {
		treeViewer.setInput(treeViewerInput);
	}
}
