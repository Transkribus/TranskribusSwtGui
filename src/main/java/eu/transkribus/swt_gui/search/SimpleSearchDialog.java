package eu.transkribus.swt_gui.search;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.util.LabeledCombo;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt_gui.collection_manager.CollectionManagerDialog;
import eu.transkribus.swt_gui.search.documents.DocSearchComposite;
import eu.transkribus.swt_gui.search.documents.SimpleDocSearchComposite;
import eu.transkribus.swt_gui.search.kws.KeywordSpottingComposite;
import eu.transkribus.swt_gui.search.text_and_tags.TagSearchComposite;
import eu.transkribus.swt_gui.search.text_and_tags.TextSearchComposite;

public class SimpleSearchDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(SimpleSearchDialog.class);
	
	SimpleDocSearchComposite docSearchComposite;
		
	int collectionID;

	/**
	 * Create the dialog.
	 * @param collectionManagerDialog2
	 */
	public SimpleSearchDialog(Shell shell, int colID) {
		super(shell);
		
		setShellStyle(SWT.SHELL_TRIM | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
		setBlockOnOpen(false);
		
		collectionID = colID;
	}
	
	@Override protected boolean isResizable() {
		return true;
	}
	
	@Override protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Search for...");
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);
		c.setLayout(new GridLayout());
				
		docSearchComposite = new SimpleDocSearchComposite(c, 0, collectionID);
		docSearchComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		return c;
	}
	
	
	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override protected void createButtonsForButtonBar(Composite parent) {
//		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override protected Point getInitialSize() {
		return new Point(1000, 800);
	}
	

}
