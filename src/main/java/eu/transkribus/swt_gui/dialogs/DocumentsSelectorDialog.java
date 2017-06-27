package eu.transkribus.swt_gui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.swt_gui.util.DocumentsSelector;

public class DocumentsSelectorDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(DocumentsSelectorDialog.class);

	DocumentsSelector ds;
	List<TrpDocMetadata> docs;
	
	List<TrpDocMetadata> checkedDocs;
	
	String title;

	public DocumentsSelectorDialog(Shell parent, final String title, List<TrpDocMetadata> docs) {
		super(parent);
		this.title = title;
		this.docs = docs;
	}

	public void setVisible() {
		if (super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));
		
		ds = new DocumentsSelector(container, 0, false, true);
		ds.setLayoutData(new GridData(GridData.FILL_BOTH));
		ds.setDataList(docs);
		
		return container;
	}

	@Override
	protected void okPressed() {
		checkedDocs = ds.getCheckedDataList();
		
		super.okPressed();
	}
	
	public List<TrpDocMetadata> getCheckedDocs() {
		return checkedDocs;
	}
	
	public List<DocumentSelectionDescriptor> getCheckedDocumentDescriptors() {
		List<DocumentSelectionDescriptor> dsds = new ArrayList<>();
		for (TrpDocMetadata d : getCheckedDocs()) {
			dsds.add(new DocumentSelectionDescriptor(d.getDocId()));
		}
		return dsds;
	}
	
	@Override protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
//		newShell.setMinimumSize(400, 400);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 800);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
		// setBlockOnOpen(false);
	}
}

