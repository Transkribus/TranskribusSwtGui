package eu.transkribus.swt_gui.dialogs;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.util.DocPagesSelector;

public class AffineTransformDialog extends Dialog {

	LabeledText txText, tyText, sxText, syText, rotText;
	
	Double tx, ty, sx, sy, rot;
	
	DocPagesSelector pagesSelector;
	Set<Integer> selectedPages;
	
	List<TrpPage> pages;
	
	public AffineTransformDialog(Shell parentShell, List<TrpPage> pages) {
		super(parentShell);
		
		this.pages = pages;
	}
	
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Apply an affine transformation to the selected pages");
		shell.setSize(600, 400);
		SWTUtil.centerShell(shell);
	}
	
	@Override protected boolean isResizable() {
	    return true;
	}
	
	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, true));
		
		txText = new LabeledText(container, "X-Translation: ");
		tyText = new LabeledText(container, "Y-Translation: ");
		sxText = new LabeledText(container, "X-Scale: ");
		syText = new LabeledText(container, "Y-Scale: ");
		rotText = new LabeledText(container, "Rotation: ");
		
		pagesSelector = new DocPagesSelector(container, 0, pages);
		pagesSelector.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));

		return container;
	}
	
	@Override protected void okPressed() {
		tx = txText.toDoubleVal();
		ty = tyText.toDoubleVal();
		sx = sxText.toDoubleVal();
		sy = syText.toDoubleVal();
		rot = rotText.toDoubleVal();
		
		try {
			selectedPages = pagesSelector.getSelectedPageIndices();
		} catch (IOException e) {
			selectedPages = null;
		}
		
		super.okPressed();
	}

	public boolean hasTransform() {
		return (!CoreUtils.equalsEps(getTx(), 0.0f, 1e-4)
			|| !CoreUtils.equalsEps(getTy(), 0.0f, 1e-4)
			|| !CoreUtils.equalsEps(getSx(), 1.0f, 1e-4)
			|| !CoreUtils.equalsEps(getSy(), 1.0f, 1e-4)
			|| !CoreUtils.equalsEps(getRot(), 0.0f, 1e-4));
	}

	public double getTx() {
		return tx==null ? 0.0d : tx;
	}

	public Double getTy() {
		return ty==null ? 0.0d : ty;
	}

	public Double getSx() {
		return sx==null ? 1.0d : sx;
	}

	public Double getSy() {
		return sy==null ? 1.0d : sy;
	}

	public Double getRot() {
		return rot==null ? 0.0d : rot;
	}
	
	public Set<Integer> getSelectedPages() {
		return selectedPages;
	}

}
