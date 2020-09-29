package eu.transkribus.swt_gui.htr;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.util.CurrentDocPagesSelector;

public class DUDecodeDialog extends Dialog {
    private final static Logger logger = LoggerFactory.getLogger(DUDecodeDialog.class);

    CurrentDocPagesSelector pagesSelector;
    String sPages;

    public DUDecodeDialog(Shell parent) {
        super(parent);
    }

    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Document Understanding");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        int padding = 15;

        CLabel cl = new CLabel(parent, SWT.LEFT);
        cl.setMargins(padding, padding, padding, padding);
        // Label infoLabel = new Label(parent, SWT.WRAP);
        cl.setText("This tool tries to automatically add text structure tags for a given transcription.\n"
                + "Job progress can be viewed in the Jobs toolbar dialog.");

        Label pagesSelectLabel = new Label(parent, SWT.NONE);
        pagesSelectLabel.setText("Pages:");

        pagesSelector = new CurrentDocPagesSelector(parent, SWT.NONE, false, true, true);
		pagesSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        return container;
    }


    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);

        Button helpBtn = createButton(parent, IDialogConstants.HELP_ID, "Help", false);
        helpBtn.setImage(Images.HELP);
        SWTUtil.onSelectionEvent(helpBtn, e -> {
            org.eclipse.swt.program.Program.launch("https://transkribus.eu/wiki/index.php/Document_Understanding_Tool");
        });

        Button runBtn = createButton(parent, IDialogConstants.OK_ID, "Run", false);
        runBtn.setImage(Images.ARROW_RIGHT);
    }

    public String getPages(){
        return sPages;
    }

   @Override
   protected void okPressed() {
       sPages = pagesSelector.getPagesStr();
       super.okPressed();
   }
    
}