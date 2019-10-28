package eu.transkribus.swt_gui.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.junit.Assert;

import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.swt_gui.util.DocPageViewer;

public class DocSyncDialog extends Dialog {
	
	TrpDoc target, source;
	DocPageViewer targetViewer, sourceViewer;
	
	List<TrpPage> sourcePages;
	List<Boolean> checked;

	public DocSyncDialog(Shell parentShell, TrpDoc target, TrpDoc source) {
		super(parentShell);
		
		Assert.assertNotNull("target document null!", target);
		Assert.assertNotNull("source document null!", source);
		
		this.target = target;
		this.source = source;
		
	}
	
	@Override protected boolean isResizable() {
	    return true;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override protected Control createDialogArea(Composite parent) {
		getShell().setText("Sychronize current document with files on local hard disc");

		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, true));
		
		Label titleLabel = new Label(container, SWT.WRAP | SWT.LEFT);
		titleLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 2));
		titleLabel.setText("Select pages from the source document on the right to correspond with pages on the target document on the left. \n"
				+ "\n"
				+ "Note: Pages are synced according to filename! Only checked pages will be synced.");

		SashForm sf = new SashForm(container, SWT.HORIZONTAL);
		sf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sf.setLayout(new GridLayout(2, true));
		
		targetViewer = new DocPageViewer(sf, 0, false, false, false);
		targetViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		targetViewer.setDataList(target.getPages());
		targetViewer.setTitle("Target: "+target.getMd().getTitle());
		
		sourceViewer = new DocPageViewer(sf, 0, false, true, false,false, true);
		sourceViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sourceViewer.setDataList(source.getPages());
		sourceViewer.setTitle("Source: "+source.getMd().getTitle());
		
		sf.setWeights(new int[] { 1, 1 } );

		return container;
	}
	
	@Override protected void okPressed() {
		
		sourcePages = sourceViewer.getDataList();
		checked = sourceViewer.getCheckedList();
		
		super.okPressed();
	}
	
	public List<TrpPage> getSourcePages() {
		return sourcePages;
	}

	public List<Boolean> getChecked() {
		return checked;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override protected Point getInitialSize() {
		return new Point(800, 600);
	}

}
