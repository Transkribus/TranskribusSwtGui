package eu.transkribus.swt_gui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.swt_gui.pagination_tables.TranscriptsTableWidgetPagination;

public class ChooseTranscriptDialog extends Dialog {
	
//	TrpDoc target, source;
//	DocPageViewer targetViewer, sourceViewer;
//	
//	List<TrpPage> sourcePages;
//	List<Boolean> checked;
	
	TrpTranscriptMetadata md = null;
	TranscriptsTableWidgetPagination trWidget;

	public ChooseTranscriptDialog(Shell parentShell) {
		super(parentShell);
		
//		Assert.assertNotNull("target document null!", target);
//		Assert.assertNotNull("source document null!", source);
//		
//		this.target = target;
//		this.source = source;
	}
	
	@Override protected void configureShell(Shell shell) {
	      super.configureShell(shell);
	      shell.setText("Choose version by doubleclick");
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
		container.setLayout(new GridLayout(1, true));
		
		trWidget = new TranscriptsTableWidgetPagination(container, 0, 50);
		trWidget.getDeleteBtn().dispose();
		trWidget.setLayoutData(new GridData(GridData.FILL_BOTH));
		trWidget.getTableViewer().addDoubleClickListener(new IDoubleClickListener() {
			@Override public void doubleClick(DoubleClickEvent event) {
				md = trWidget.getFirstSelected();
				okPressed();
			}
		});

		return container;
	}
	
//	@Override protected void okPressed() {
//		super.okPressed();
//	}
	
	public TrpTranscriptMetadata getTranscript() { return md; }

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
		return new Point(500, 700);
	}

}
