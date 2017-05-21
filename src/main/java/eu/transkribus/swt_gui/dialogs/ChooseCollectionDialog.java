package eu.transkribus.swt_gui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt_gui.collection_comboviewer.CollectionSelectorWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class ChooseCollectionDialog extends Dialog {
		
	Storage store = Storage.getInstance();
		
	Integer initColId=null;
	
	TrpCollection selectedCollection=null;
//	CollectionComboViewerWidget collCombo;
	CollectionSelectorWidget collCombo;
	
	String title;
	
	public ChooseCollectionDialog(Shell parentShell) {
		this(parentShell, "Choose a collection");
	}

	public ChooseCollectionDialog(Shell parentShell, String title) {
		this(parentShell, title, null);
	}
	
	public ChooseCollectionDialog(Shell parentShell, String title, Integer initColId) {
		super(parentShell);
		this.title = title;
		this.initColId = initColId;
	}
	
	@Override protected void configureShell(Shell shell) {
	      super.configureShell(shell);
	      shell.setText(title);
	}
	
	@Override protected boolean isResizable() {
	    return true;
	}
	
	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));
		
//		collCombo = new CollectionComboViewerWidget(parent, 0, true, false, false);
//		collCombo.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label l = new Label(container, 0);
		l.setText("Selected collection: ");
		Fonts.setBoldFont(l);
//		collCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		collCombo = new CollectionSelectorWidget(container, 0, true, false, false);
		collCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		if (initColId != null) {
			collCombo.setSelectedCollection(initColId);
		}

		return container;
	}
	
	@Override protected void okPressed() {
		selectedCollection = collCombo.getSelectedCollection();
		
		super.okPressed();
	}
	
	public TrpCollection getSelectedCollection() { return selectedCollection; }

	@Override protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override protected Point getInitialSize() {
		return new Point(550, 150);
	}

}
