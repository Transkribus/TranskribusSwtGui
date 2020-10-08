package eu.transkribus.swt.util;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
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
	
	private int comboStyle;
	private AutoCompleteField comboAutoComplete;
	private boolean addAutoComplete;
	
	String selectedText;
	int selectedIndex;
	
	String labelTxt;
	String[] items;
	
	IInputValidator validator;

	public ComboInputDialog(Shell parentShell, String labelTxt, String[] items) {
		this(parentShell, labelTxt, items, SWT.READ_ONLY | SWT.DROP_DOWN, false);
	}
	
	public ComboInputDialog(Shell parentShell, String labelTxt, String[] items, int comboStyle, boolean addAutoComplete) {
		super(parentShell);

		Assert.assertTrue(items != null && items.length > 0);
		Assert.assertNotNull(labelTxt);
		
		this.comboStyle = comboStyle;
		this.addAutoComplete = addAutoComplete;
		this.labelTxt = labelTxt;
		this.items = items;
	}
	
	public void setValidator(IInputValidator validator) {
		this.validator = validator;
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
		
		combo = new Combo(container, comboStyle);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		combo.addModifyListener(new ModifyListener() {
			@Override public void modifyText(ModifyEvent e) {
				updateVals();
			}
		});
		combo.setItems(items);
		combo.select(0);
		
		if (addAutoComplete) {
			comboAutoComplete = new AutoCompleteField(combo, new ComboContentAdapter(), items);
		}
		
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
	
	@Override
	public void okPressed() {
		updateVals();
		if (validator != null) {
			String errMsg = validator.isValid(selectedText);
			if (!StringUtils.isEmpty(errMsg)) {
				DialogUtil.showErrorMessageBox(getShell(), "Invalid value", errMsg);
				return;
			}
		}
		super.okPressed();
	}

}
