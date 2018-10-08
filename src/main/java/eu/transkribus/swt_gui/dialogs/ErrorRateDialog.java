package eu.transkribus.swt_gui.dialogs;



import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import eu.transkribus.core.model.beans.TrpErrorRate;
import eu.transkribus.core.model.beans.TrpErrorRateResult;
import eu.transkribus.swt.util.DesktopUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.tool.error.ErrorTableViewer;

public class ErrorRateDialog extends Dialog {

	protected static final String HELP_WIKI_ERR = "https://en.wikipedia.org/wiki/Word_error_rate";
	protected static final String HELP_WIKI_FMEA = "https://en.wikipedia.org/wiki/F1_score";

	private Composite composite;

	private TrpErrorRateResult resultErr;
	
	Button expertBtn, partialMatchBtn, caseSensitivityBtn, wikiErrButton, wikiFmeaButton;

	public ErrorRateDialog(Shell parentShell, TrpErrorRateResult resultErr) {

		super(parentShell);
		this.resultErr = resultErr;

	}
	
	public void createHeader() {
		
		Composite config = new Composite(composite,SWT.NONE);
		
		config.setLayout(new GridLayout(4,false));
		config.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,false));
		
		Label firstLabel = new Label(config, SWT.NONE);
	    firstLabel.setText("Quick compare for current page ");
	     
	}
	
	public void createTable() {
		
		Composite body = new Composite(composite,SWT.NONE);
		
		body.setLayout(new GridLayout(1,false));
		body.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,false));
	
		final ErrorTableViewer viewer = new ErrorTableViewer(body, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

		viewer.getTable().setLinesVisible(false);

		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		TableItem itemPage = new TableItem(table, SWT.NONE);
		
		if(resultErr.getWerDouble().isNaN() || resultErr.getCerDouble().isNaN()) {
			itemPage.setText(new String[] { "Page", "Could not be calculated", "Could not be calculated"," "," "," "," "," "});
		}
		else {
			itemPage.setText(new String[] { "Page", ""+resultErr.getWerDouble(), ""+resultErr.getCerDouble()," "," "," "," "," "});
		}
	
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Error Rate Results");
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		
		this.composite = (Composite) super.createDialogArea(parent);
		
		createHeader();
		
		createTable();
		
		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		wikiErrButton = createButton(parent, IDialogConstants.HELP_ID, "Error Rate", false);
		wikiErrButton.setImage(Images.HELP);

		wikiFmeaButton = createButton(parent, IDialogConstants.HELP_ID, "F-Measure", false);
		wikiFmeaButton.setImage(Images.HELP);

		createButton(parent, IDialogConstants.OK_ID, "Ok", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
		GridData buttonLd = (GridData) getButton(IDialogConstants.CANCEL_ID).getLayoutData();
		wikiErrButton.setLayoutData(buttonLd);
		wikiErrButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DesktopUtil.browse(HELP_WIKI_ERR, "You can find the relevant information on the Wikipedia page.",
						getParentShell());
			}
		});

		wikiFmeaButton.setLayoutData(buttonLd);
		wikiFmeaButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DesktopUtil.browse(HELP_WIKI_FMEA, "You can find the relevant information on the Wikipedia page.",
						getParentShell());
			}
		});

	}
}
