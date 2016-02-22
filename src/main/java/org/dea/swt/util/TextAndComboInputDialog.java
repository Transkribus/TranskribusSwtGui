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
import org.eclipse.swt.widgets.Text;
import org.junit.Assert;

public class TextAndComboInputDialog extends Dialog {
	
	public Label textLabel;
	public Text text;
	
	public Label comboLabel;
	public Combo combo;
	
	public String selectedText;
	public int selectedIndex;
	
	public String inputText;
	
	String comboLabelTxt;
	String textLabelTxt;
	String[] items;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public TextAndComboInputDialog(Shell parentShell, String textLabelText, String defaultText, String comboLabelTxt, String[] items) {
		super(parentShell);

		Assert.assertTrue(items != null && items.length > 0);
		Assert.assertNotNull(comboLabelTxt);
		
		this.textLabelTxt = textLabelText;
		this.inputText = defaultText;
		
		this.comboLabelTxt = comboLabelTxt;
		this.items = items;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));
		
		textLabel = new Label(container, 0);
		textLabel.setText(textLabelTxt);
		
		text = new Text(container, 0);
		text.setText(inputText);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		comboLabel = new Label(container, 0);
		comboLabel.setText(comboLabelTxt);
		
		combo = new Combo(container, SWT.READ_ONLY | SWT.DROP_DOWN);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		combo.addModifyListener(new ModifyListener() {
			@Override public void modifyText(ModifyEvent e) {
				updateVals();
			}
		});
		
		text.addModifyListener(new ModifyListener() {
			@Override public void modifyText(ModifyEvent e){
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
		
		inputText = text.getText();
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
