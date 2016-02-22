package org.dea.swt.util;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.junit.Assert;

public class ComboInputDialog extends Dialog {
	
	public Label label;
	public Combo combo;
	
	String selectedText;
	int selectedIndex;
	
	String labelTxt;
	String[] items;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public ComboInputDialog(Shell parentShell, String labelTxt, String[] items) {
		super(parentShell);

		Assert.assertTrue(items != null && items.length > 0);
		Assert.assertNotNull(labelTxt);
		
		this.labelTxt = labelTxt;
		this.items = items;
	}
	
	

	public String getSelectedText() {
		return selectedText;
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));
		
		label = new Label(container, 0);
		label.setText(labelTxt);
		
		combo = new Combo(container, SWT.READ_ONLY | SWT.DROP_DOWN);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		combo.addModifyListener(new ModifyListener() {
			@Override public void modifyText(ModifyEvent e) {
				updateVals();
			}
		});
		combo.setItems(items);
		combo.select(0);
		
		updateVals();
		
		container.pack();

		return container;
	}
	
	public void updateVals() {
		selectedText = combo.getText();
		selectedIndex = combo.getSelectionIndex();
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
		return new Point(450, 200);
	}

}
