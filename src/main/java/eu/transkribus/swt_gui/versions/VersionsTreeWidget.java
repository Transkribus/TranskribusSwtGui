package eu.transkribus.swt_gui.versions;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.LabeledText;

/**
 * @deprecated not used and not finished yet
 */
public class VersionsTreeWidget extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(VersionsTreeWidget.class);
	
	TreeViewer tv;
	Button reloadBtn;
	LabeledText nrOfVersionsText;
	TrpDoc doc;
	
	public static final int DEFAULT_N_VERSIONS = 5;
	
	public static final String PAGE_COL = "Page";
	
	public static final String STATUS_COL = "Status";
	public static final String USER_NAME_COL = "Username";
	public static final String DATE_COL = "Date";
	public static final String TOOLNAME_COL = "Toolname";
	public static final String ID_COL = "ID";
	public static final String PARENT_ID_COL = "Parent-ID";
	public static final String MESSAGE_COL = "Message";
	
//	createColumn(STATUS_COL, 100, "status", new TranscriptsColumnLabelProvider("status"));
//	createColumn(USER_NAME_COL, 100, "userName", new TranscriptsColumnLabelProvider("userName"));
//	createColumn(DATE_COL, 225, "timestamp", new TranscriptsColumnLabelProvider("timeFormatted")); // TODO: time-str!
//	createColumn(TOOLNAME_COL, 100, "toolName", new TranscriptsColumnLabelProvider("toolName"));
//	
//	createColumn(ID_COL, 100, "tsId", new TranscriptsColumnLabelProvider("tsId"));
//	createColumn(PARENT_ID_COL, 100, "parentTsId", new TranscriptsColumnLabelProvider("parentTsId"));
//	createColumn(MESSAGE_COL, 200, "note", new TranscriptsColumnLabelProvider("note"));
	
	public static final ColumnConfig[] COLS = new ColumnConfig[] { 
//			"Page", "ID", "Date", "Comment"
			new ColumnConfig(PAGE_COL, 80),
			
			new ColumnConfig(STATUS_COL, 100),
			new ColumnConfig(USER_NAME_COL, 100),
			new ColumnConfig(DATE_COL, 225),
			new ColumnConfig(TOOLNAME_COL, 225),
			new ColumnConfig(ID_COL, 100),
			new ColumnConfig(PARENT_ID_COL, 100),
			new ColumnConfig(MESSAGE_COL, 200),
	};
	
	class VersionsTreeWidgetContentProvider implements ITreeContentProvider {
		TrpDoc doc;

		@Override
		public Object[] getChildren(Object inputElement) {
			this.doc = (TrpDoc) doc;
			return getElements(inputElement);
		}

		@Override
		public Object[] getElements(Object parent) {
			if (parent instanceof TrpDoc) {
				return ((TrpDoc) parent).getPages().toArray();
			}
			if (parent instanceof TrpPage) {
				return ((TrpPage) parent).getTranscripts().toArray();
			}
			
			return null;
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof TrpTranscriptMetadata) {
				return doc.getPageWithId(((TrpTranscriptMetadata) element).getPageId());
			}
			if (element instanceof TrpPage) {
				return this.doc;
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return element instanceof TrpDoc || element instanceof TrpPage;
		}
	}
	
	class VersionsTreeWidgetLabelProvider extends CellLabelProvider implements ITableLabelProvider  {
		@Override
		public Image getColumnImage(Object arg0, int arg1) {
			return null;
		}
		
		public String getPageColumnText(TrpPage p, int ci) {
			String cn = COLS[ci].name;
			if (cn.equals(PAGE_COL)) {
				return p.getPageNr()+"";
			}
			return "";
		}
		
		public String getTranscriptColumnText(TrpTranscriptMetadata t, int ci) {
			String cn = COLS[ci].name;
			if (cn.equals(STATUS_COL)) {
				return ""+t.getStatus();
			}
			if (cn.equals(USER_NAME_COL)) {
				return t.getUserName();
			}
			if (cn.equals(DATE_COL)) {
				return t.getTimeFormatted();
			}
			
			if (cn.equals(TOOLNAME_COL)) {
				return t.getToolName();
			}
			if (cn.equals(ID_COL)) {
				return ""+t.getTsId();
			}
			if (cn.equals(PARENT_ID_COL)) {
				return ""+t.getParentTsId();
			}
			if (cn.equals(MESSAGE_COL)) {
				return t.getNote();
			}			
			
			
			return "";
		}		

		@Override
		public String getColumnText(Object e, int ci) {
			System.out.println("ci = "+ci);
			if (e instanceof TrpPage) {
				return getPageColumnText((TrpPage) e, ci);
			}
			else if (e instanceof TrpTranscriptMetadata) {
				return getTranscriptColumnText((TrpTranscriptMetadata) e, ci);
			}
			
			return "i am error";
		}
		
			@Override
			public void update(ViewerCell c) {
				logger.debug("HERE: "+c);
				String txt = getColumnText(c.getElement(), c.getColumnIndex());
				
//				String txt=""+System.currentTimeMillis();
//				if (c.getColumnIndex()==0) {
//					txt = c.getElement().getClass().getSimpleName();
//				}
				c.setText(txt);
			}		
	}
	
	public VersionsTreeWidget(Composite parent, int style) {
		super(parent, style);
		
		this.setLayout(new GridLayout(2, false));
		
		nrOfVersionsText = new LabeledText(this, "N-Versions per page: ");
		nrOfVersionsText.setText(""+DEFAULT_N_VERSIONS);
		nrOfVersionsText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		
		reloadBtn = new Button(this, 0);
		reloadBtn.setImage(Images.REFRESH);
		reloadBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		
		tv = new TreeViewer(this, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		tv.setContentProvider(new VersionsTreeWidgetContentProvider());
//		tv.setLabelProvider(new VersionsTreeWidgetLabelProvider());     
		tv.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		tv.getTree().setHeaderVisible(true);
		for (ColumnConfig cf : COLS) {
			addColumn(cf);
		}
	}
	
	private void addColumn(ColumnConfig cf) {
        TreeViewerColumn c = new TreeViewerColumn(tv, SWT.LEFT | SWT.BORDER);
		c.getColumn().setText(cf.name);
		c.getColumn().setWidth(cf.width);
		c.setLabelProvider(new VersionsTreeWidgetLabelProvider());
	}
	
	public void setDoc(TrpDoc doc) {
		this.doc = doc;
		tv.setInput(doc);
	}

}
