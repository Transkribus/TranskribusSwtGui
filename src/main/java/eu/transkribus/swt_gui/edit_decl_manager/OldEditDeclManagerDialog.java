package eu.transkribus.swt_gui.edit_decl_manager;

import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt_gui.doc_overview.DocOverviewWidget;
import eu.transkribus.swt_gui.mainwidget.Storage;

//public class CollectionManagerWidget extends Composite {
public class OldEditDeclManagerDialog extends Dialog {
	
	private final static Logger logger = LoggerFactory.getLogger(OldEditDeclManagerDialog.class);
		
	private Shell shlEditorialDeclaration;	
	private Table docFeatTable;
	private MyTableViewer docFeatTv;
	private Table availFeatTable;
	private MyTableViewer availFeatTv;
	
	public static final String FEAT_ID_COL = "ID";
	public static final String FEAT_TITLE_COL = "Title";
	public static final String FEAT_DESC_COL = "Description";
	public static final String FEAT_COL_ID_COL = "Collection";
	public static final String FEAT_OPTS = "Options";
	
	static final Storage store = Storage.getInstance();
	
	// This are the columns, sorted in their order of appearence in the table:
	public static final ColumnConfig[] FEAT_COLS = new ColumnConfig[] {
		new ColumnConfig(FEAT_ID_COL, 35, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(FEAT_TITLE_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(FEAT_DESC_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(FEAT_COL_ID_COL, 35, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(FEAT_OPTS, 75, false, DefaultTableColumnViewerSorter.ASC)
	};

	

	public OldEditDeclManagerDialog(Shell parent, int style) {
		super(parent, style |= (SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MODELESS));
//		this.setSize(800, 800);
		this.setText("Collection Manager");
	}
	
	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlEditorialDeclaration.open();
		shlEditorialDeclaration.layout();
		Display display = getParent().getDisplay();
		while (!shlEditorialDeclaration.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return null;
	}
	
	void createContents() {
		shlEditorialDeclaration = new Shell(getParent(), getStyle());
		shlEditorialDeclaration.setSize(1200, 900);
		shlEditorialDeclaration.setText("Editorial Declaration");
//		shell.setLayout(new GridLayout(2, false));
//		shell.setLayout(new GridLayout(4, false));
		shlEditorialDeclaration.setLayout(new FillLayout());
		
//		Composite composite = new Composite(shlEditorialDeclaration, SWT.NONE);
		
		Composite composite = new SashForm(shlEditorialDeclaration, SWT.HORIZONTAL);
		composite.setLayout(new GridLayout(3, false));
		
		Group grpDocFeats = new Group(composite, SWT.SHADOW_ETCHED_IN);
		grpDocFeats.setText("Document Features");
//		grpDocFeats.setBounds(10, 10, 538, 840);
		grpDocFeats.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpDocFeats.setLayout(new GridLayout());
		
		
		docFeatTv = new MyTableViewer(grpDocFeats, SWT.SINGLE | SWT.FULL_SELECTION);
		docFeatTv.setContentProvider(new ArrayContentProvider());
		docFeatTv.setLabelProvider(new FeatureTableLabelProvider(docFeatTv));
		
		docFeatTable = docFeatTv.getTable();
//		docFeatTable.setBounds(10, 25, 518, 777);
		docFeatTable.setHeaderVisible(true);
		docFeatTable.setLinesVisible(true);
		docFeatTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		docFeatTv.addColumns(FEAT_COLS);
		
		Group btnGroup = new Group(composite, SWT.NONE);
//		btnGroup.setText("Available Features");
//		btnGroup.setBounds(650, 10, 538, 840);
		btnGroup.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnGroup.setLayout(new GridLayout());
		
		Button removeFeatBtn = new Button(btnGroup, SWT.NONE);
//		removeFeatBtn.setBounds(554, 370, 90, 31);
		removeFeatBtn.setText(">>");
		
		Button addFeatBtn = new Button(btnGroup, SWT.NONE);
//		addFeatBtn.setBounds(554, 407, 90, 31);
		addFeatBtn.setText("<<");
		
		Group grpFeats = new Group(composite, SWT.SHADOW_ETCHED_IN);
		grpFeats.setText("Available Features");
//		grpFeats.setBounds(650, 10, 538, 840);
		grpFeats.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpFeats.setLayout(new GridLayout());
		
		availFeatTv = new MyTableViewer(grpFeats, SWT.SINGLE | SWT.FULL_SELECTION);
		availFeatTv.setContentProvider(new ArrayContentProvider());
		availFeatTv.setLabelProvider(new FeatureTableLabelProvider(availFeatTv));
		
		availFeatTable = availFeatTv.getTable();
//		availFeatTable.setBounds(10, 25, 518, 772);
		availFeatTable.setHeaderVisible(true);
		availFeatTable.setLinesVisible(true);
		availFeatTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		availFeatTv.addColumns(FEAT_COLS);
		
		updateEditDecl();
		updateFeatures();
	}
	
	public Shell getShell() { return shlEditorialDeclaration; }
	
	public void updateFeatures() {
		logger.debug("updating features");
		try {
			availFeatTv.setInput(store.getAvailFeatures());
		} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException
				| NoConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateEditDecl() {
		logger.debug("updating editorial declaration");
		try {
			docFeatTv.setInput(store.getEditDeclFeatures());
		} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException
				| NoConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
