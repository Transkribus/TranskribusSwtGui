package eu.transkribus.swt_gui.credits;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpCreditCosts;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class CreditPackageDetailsDialog extends Dialog {
	
	protected Composite dialogArea;
	protected CreditCostsTable table;
	
	private double creditValue;
	private List<TrpCreditCosts> costs;
	
	public CreditPackageDetailsDialog(Shell parent, double creditValue) {
		super(parent);
		this.creditValue = creditValue;
		costs = Storage.getInstance().getCreditCosts(null, false);
	}

	public void setVisible() {
		if (super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		dialogArea = (Composite) super.createDialogArea(parent);
		dialogArea.setLayout(new GridLayout(1, true));
		dialogArea.setLayoutData(new GridData(GridData.FILL_BOTH));
		table = new CreditCostsTable(dialogArea, SWT.BORDER | SWT.V_SCROLL, creditValue, costs);
		return dialogArea;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// do not create button bar
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Remaining Pages with " + creditValue + " Credits");
		newShell.setMinimumSize(480, 400);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(480, 400);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
	}
}
