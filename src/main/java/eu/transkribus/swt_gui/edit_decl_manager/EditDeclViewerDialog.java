package eu.transkribus.swt_gui.edit_decl_manager;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.mytableviewer.MyTableViewer;

//public class CollectionManagerWidget extends Composite {
public class EditDeclViewerDialog extends EditDeclManagerDialog {
	private final static Logger logger = LoggerFactory.getLogger(EditDeclViewerDialog.class);

	public EditDeclViewerDialog(Shell parent, int style) {
		super(parent, style |= (SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MODELESS | SWT.MAX));
		this.setText("Editorial Declaration");
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
	
}
