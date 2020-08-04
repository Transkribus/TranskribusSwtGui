package eu.transkribus.swt_gui.htr;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class DUDecodeDialog extends Dialog {
    private final static Logger logger = LoggerFactory.getLogger(DUDecodeDialog.class);

    Storage storage;

    public DUDecodeDialog(Shell parent) {
        super(parent);
        storage = Storage.getInstance();
    }

    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Document Understanding");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        
        CLabel cl = new CLabel(parent, SWT.LEFT);
        int padding = 15;
        cl.setMargins(padding, padding, padding, padding);
        //Label infoLabel = new Label(parent, SWT.WRAP);
        cl.setText("This tool tries to automatically add text structure tags for a given transcription.\n"
                        + "Job progress can be viewed in the Jobs toolbar dialog.");

        return container;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);

        Button ok = getButton(IDialogConstants.OK_ID);
        ok.setText("Start");
        setButtonLayoutData(ok);

        Button cancel = getButton(IDialogConstants.CANCEL_ID);
        cancel.setText("Cancel");
        setButtonLayoutData(cancel);
    }

    @Override
    protected void okPressed() {
        runRecognition();
        super.okPressed();
    }

    void runRecognition(){
        logger.debug("Button pressed");
        try {
            String result = storage.getInstance().runDocUnderstanding(storage.getInstance().getDocId(), "", 2);
            logger.debug(result);
            MessageDialog.openInformation(this.getShell(), "Info", "Job started with id: " + result);
        } catch (SessionExpiredException | ServerErrorException | ClientErrorException
                | NoConnectionException e) {                    
            logger.debug("Something went wrong", e);
            MessageDialog.openError(this.getShell(), "Error", ""+e);

        }
    }
    
}