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
import eu.transkribus.core.model.beans.EdFeature;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class AddFeatureDialog extends Dialog {
    private Text titleField;
//    private String titleString;

    private Text descField;
//    private String descString;
    
    private Button colCheckbox;
    
    private final EdFeature feat;
    
    EditDeclManagerDialog efd;
    
    public AddFeatureDialog(EditDeclManagerDialog efd) {
        super(efd.getShell());
        this.feat = new EdFeature();
        this.efd = efd;
    }

    @Override
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText("Create Feature");
        newShell.setSize(450, 230);
        SWTUtil.centerShell(newShell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite comp = (Composite) super.createDialogArea(parent);
        
        GridLayout layout = (GridLayout) comp.getLayout();
        layout.numColumns = 2;

        Label titleLabel = new Label(comp, SWT.RIGHT);
        titleLabel.setText("Title: ");
        titleField = new Text(comp, SWT.SINGLE | SWT.BORDER);
        titleField.setSize(30, titleField.getSize().y);
        
        Label descLabel = new Label(comp, SWT.RIGHT);
        descLabel.setText("Description: ");
        descField = new Text(comp, SWT.SINGLE | SWT.BORDER);
        descField.setSize(30, descField.getSize().y);
        
        Label isColFeatLabel = new Label(comp, SWT.RIGHT);
        isColFeatLabel.setText("Collection restriced: ");
        colCheckbox = new Button(comp, SWT.CHECK);
        colCheckbox.setSelection(true);
        
        boolean isAdmin = Storage.getInstance().getUser().isAdmin();
        colCheckbox.setEnabled(isAdmin);

        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        titleField.setLayoutData(data);
        descField.setLayoutData(data);
        
        return comp;
    }

    @Override
    protected void okPressed()
    {
        if(titleField.getText().length() < 1){
        	//print some error message
        	MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR);
        	messageBox.setMessage("Feature must have a title!");
        	int rc = messageBox.open();
        	return;
        }
        		
        feat.setTitle(titleField.getText());
        feat.setDescription(descField.getText());
        
        try {
			Storage.getInstance().storeEdFeature(feat, colCheckbox.getSelection());
		} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException
				| NoConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        efd.updateFeatures();
        super.okPressed();
    }

    @Override
    protected void cancelPressed()
    {
//        titleField.setText("");
        super.cancelPressed();
    }

    public String getTitle()
    {
        return feat.getTitle();
    }
}