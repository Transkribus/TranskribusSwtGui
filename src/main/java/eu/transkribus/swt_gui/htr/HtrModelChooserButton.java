package eu.transkribus.swt_gui.htr;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.swt.util.SWTUtil;

public class HtrModelChooserButton extends Composite {
	
	Button baseModelBtn;

	public HtrModelChooserButton(Composite parent) {
		super(parent, 0);
		
		this.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		
		baseModelBtn = new Button(this, 0);
		baseModelBtn.setLayoutData(new GridData(GridData.FILL_BOTH));
		updateModelText();
		
		SWTUtil.onSelectionEvent(baseModelBtn, (e) -> {
			HtrModelsDialog diag = new HtrModelsDialog(getShell());
			if (diag.open() == Dialog.OK) {
				setModel(diag.getSelectedHtr());
			}
		});
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

	public void setText(String string) {
		baseModelBtn.setText(string);
	}
	
	public void setImage(Image image) {
		baseModelBtn.setImage(image);
	}

}
