package eu.transkribus.swt_gui.edit_decl_manager;

import java.util.List;

import javax.ws.rs.ServerErrorException;

import org.dea.swt.mytableviewer.ColumnConfig;
import org.dea.swt.mytableviewer.MyTableViewer;
import org.dea.swt.util.DefaultTableColumnViewerSorter;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.EdFeature;
import eu.transkribus.swt_gui.mainwidget.Storage;

//public class CollectionManagerWidget extends Composite {
public class EditDeclViewerDialog extends EditDeclManagerDialog {
	
	private final static Logger logger = LoggerFactory.getLogger(EditDeclViewerDialog.class);
		
	private Shell shlEditorialDeclaration;

	private Table editDeclTable;
	private MyTableViewer editDeclTv;
	private List<EdFeature> editDecl;
	
	public static final String EDT_DECL_ID_COL = "ID";
	public static final String EDT_DECL_TITLE_COL = "Title";
	public static final String EDT_DECL_DESC_COL = "Description";
	public static final String EDT_DECL_OPT_COL = "Selected Option";
	
	static final Storage store = Storage.getInstance();
	
	// This are the columns, sorted in their order of appearence in the table:
	public static final ColumnConfig[] EDT_DECL_COLS = new ColumnConfig[] {
		new ColumnConfig(EDT_DECL_ID_COL, 35, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(EDT_DECL_TITLE_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(EDT_DECL_DESC_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(EDT_DECL_OPT_COL, 35, false, DefaultTableColumnViewerSorter.ASC),
	};

	public EditDeclViewerDialog(Shell parent, int style) {
		super(parent, style |= (SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MODELESS | SWT.MAX));
		this.setText("Editorial Declaration");
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
		shlEditorialDeclaration.setLayout(new FillLayout());
		
		Composite composite = new Composite(shlEditorialDeclaration, 0);
		composite.setLayout(new GridLayout(1, false));
				
		Group grpEditDecl = new Group(composite, SWT.SHADOW_ETCHED_IN);
		grpEditDecl.setText("Selected Features");
		grpEditDecl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpEditDecl.setLayout(new GridLayout());
		
		editDeclTv = new MyTableViewer(grpEditDecl, SWT.SINGLE | SWT.FULL_SELECTION);
		editDeclTv.setContentProvider(new ArrayContentProvider());
		editDeclTv.setLabelProvider(new EditDeclTableLabelProvider(editDeclTv));
		
		editDeclTable = editDeclTv.getTable();
		editDeclTable.setHeaderVisible(true);
		editDeclTable.setLinesVisible(true);
		editDeclTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		editDeclTv.addColumns(EDT_DECL_COLS);
		updateEditDecl();
	}
	
	public Shell getShell() { return shlEditorialDeclaration; }
	public EditDeclViewerDialog getEditFeaturesDialog() { return this; }
	
	public void updateEditDecl() {
		logger.debug("updating editorial declaration table");
		try {
			editDecl = store.getEditDeclFeatures();
		} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException
				| NoConnectionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		editDeclTv.setInput(editDecl);
	}
}
