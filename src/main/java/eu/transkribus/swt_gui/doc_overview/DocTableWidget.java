package eu.transkribus.swt_gui.doc_overview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;

public class DocTableWidget extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(DocTableWidget.class);
	
	public static final String DOC_NR_COL = "NR";
	public static final String DOC_ID_COL = "ID";
	public static final String DOCS_TITLE_COL = "Title";
	public static final String DOC_NPAGES_COL = "N-Pages";
	public static final String DOC_OWNER_COL = "Owner";
	public static final String DOC_COLLECTIONS_COL = "Collections";
	
	MyTableViewer documentsTv;
	int selectedId=-1;
	
	public static final ColumnConfig[] DOCS_COLS = new ColumnConfig[] {
		new ColumnConfig(DOC_NR_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(DOC_ID_COL, 65, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(DOCS_TITLE_COL, 250, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(DOC_NPAGES_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(DOC_OWNER_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(DOC_COLLECTIONS_COL, 200, false, DefaultTableColumnViewerSorter.ASC),
	};
	
	public DocTableWidget(Composite parent, int style) {
		super(parent, style);
		
		this.setLayout(new FillLayout());
//		this.setLayout(new RowLayout(1, true));
		
		documentsTv = new MyTableViewer(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		documentsTv.setContentProvider(new ArrayContentProvider());
		documentsTv.setLabelProvider(new DocTableLabelProvider(documentsTv));
		
		Table table = documentsTv.getTable();
		table.setHeaderVisible(true);
//		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		documentsTv.addColumns(DOCS_COLS);
	}
	
	public void setDocuments() {
		
	}
	
//	public int getSelectedId() {
//		return selectedId;
//	}
//	
//	public void setSelectedId(int selectedId) {
//		logger.debug("updating selected: "+selectedId);
//		this.selectedId = selectedId;
//		documentsTv.refresh();
//	}

	public MyTableViewer getTableViewer() {
		return documentsTv;
	}

	public TrpDocMetadata getSelectedDocument() {
		IStructuredSelection sel = (IStructuredSelection) documentsTv.getSelection();
		if (sel.getFirstElement() != null && sel.getFirstElement() instanceof TrpDocMetadata) {
			return (TrpDocMetadata) sel.getFirstElement();
		} else
			return null;

	}

	public void refreshList(List<TrpDocMetadata> trpDocs) {
		logger.debug("setting documents: "+(trpDocs==null ? "null" : trpDocs.size()));
		documentsTv.setInput(trpDocs==null ? new ArrayList<>() : trpDocs);
//		this.layout(true);
	}	


}
