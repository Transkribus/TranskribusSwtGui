package eu.transkribus.swt_gui.edit_decl_manager;

import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.EdOption;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class EditOptionDialog extends Dialog {
    private Text textField;
//    private String titleString;

    private final EdOption opt;
    
    EditDeclManagerDialog efd;
    
    public EditOptionDialog(EdOption option, EditDeclManagerDialog efd) {
        super(efd.getShell());
        this.opt = option;
        this.efd = efd;
    }

    @Override
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText("Edit Option");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite comp = (Composite) super.createDialogArea(parent);

        GridLayout layout = (GridLayout) comp.getLayout();
        layout.numColumns = 2;

        Label titleLabel = new Label(comp, SWT.RIGHT);
        titleLabel.setText("Title: ");
        textField = new Text(comp, SWT.SINGLE | SWT.BORDER);
        textField.setText(opt.getText());
        
        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textField.setLayoutData(data);
        
        return comp;
    }

    @Override
    protected void okPressed()
    {
        if(textField.getText().length() < 1){
        	//print some error message
        	MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR);
        	messageBox.setMessage("Option must have a text!");
        	int rc = messageBox.open();
        	return;
        }
        		
        opt.setText(textField.getText());
        
        try {
			Storage.getInstance().storeEdOption(opt);
		} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException
				| NoConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        efd.updateFeatures();
        efd.updateOptions();
        efd.updateEditDecl();
        efd.setSelectedFeature(opt.getFeatureId());
        super.okPressed();
    }

    @Override
    protected void cancelPressed()
    {
//        titleField.setText("");
        super.cancelPressed();
    }
}