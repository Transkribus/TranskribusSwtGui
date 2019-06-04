package eu.transkribus.swt_gui.htr;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.swt.util.SWTUtil;

public class HtrModelChooserButton extends Composite {
	
	Button baseModelBtn;
	Label label;
	
	public HtrModelChooserButton(Composite parent, final boolean doubleClickSelectionEnabled, final String providerFilter) {
		this(parent, doubleClickSelectionEnabled, providerFilter, null);
	}
	
	/**
	 * Button opening the HTR models dialog for selecting a HTR or for display purposes.
	 * 
	 * @param parent
	 * @param doubleClickSelectionEnabled if true, then double-clicking a table element confirms and saves the selection and closes the dialog.
	 */
	public HtrModelChooserButton(Composite parent, final boolean doubleClickSelectionEnabled, final String providerFilter, String labelText) {
		super(parent, 0);
		
		boolean withLabel = !StringUtils.isEmpty(labelText);
		
		this.setLayout(SWTUtil.createGridLayout(withLabel ? 2 : 1, false, 0, 0));
		
		if (withLabel) {
			label = new Label(this, 0);
			label.setText(labelText);
			label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		}
		
		baseModelBtn = new Button(this, 0);
		baseModelBtn.setLayoutData(new GridData(GridData.FILL_BOTH));
		updateModelText();
		
		SWTUtil.onSelectionEvent(baseModelBtn, (e) -> {
			HtrModelsDialog diag = new HtrModelsDialog(getShell(), doubleClickSelectionEnabled, providerFilter);
			if (diag.open() == Dialog.OK) {
				setModel(diag.getSelectedHtr());
			}
		});
	}
	
	/**
	 * Button opening the HTR models dialog for selecting a HTR or for display purposes.
	 * 
	 * @param parent
	 * @param doubleClickSelectionEnabled if true, then double-clicking a table element confirms and saves the selection and closes the dialog.
	 */
	public HtrModelChooserButton(Composite parent, final boolean doubleClickSelectionEnabled) {
		this(parent, doubleClickSelectionEnabled, null);
	}
	
	private void updateModelText() {
		if (getModel() == null) {
			baseModelBtn.setText("Choose...");
		} else {
			baseModelBtn.setText(getModel().getName());
		}
	}
	
	public void setModel(TrpHtr htr) {
		baseModelBtn.setData(htr);
		updateModelText();
	}
	
	public TrpHtr getModel() {
		return (TrpHtr) baseModelBtn.getData();
	}
	
	public Button getButton() {
		return baseModelBtn;
	}
	
	public Label getLabel() {
		return label;
	}

	public void setText(String string) {
		baseModelBtn.setText(string);
	}
	
	public void setImage(Image image) {
		baseModelBtn.setImage(image);
	}

}
