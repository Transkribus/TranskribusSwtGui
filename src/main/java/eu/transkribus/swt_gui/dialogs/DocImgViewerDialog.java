package eu.transkribus.swt_gui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.swt.util.ThumbnailWidgetVirtualMinimal;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class DocImgViewerDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(TextRecognitionConfigDialog.class);

	private Storage store = Storage.getInstance();
	
	private ThumbnailWidgetVirtualMinimal tw;
	
	private final TrpDoc doc;
	private final String title;
	
	public DocImgViewerDialog(Shell parent, final String title, TrpDoc doc) {
		super(parent);
		this.title = title;
		this.doc = doc;
	}
    
	public void setVisible() {
		if(super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		
		SashForm mainSash = new SashForm(cont, SWT.HORIZONTAL);
		mainSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainSash.setLayout(new GridLayout(2, false));
		
		tw = new ThumbnailWidgetVirtualMinimal(mainSash, false, SWT.BORDER);
		tw.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tw.setDoc(doc, false);
		
		Composite imgCont = new Composite(mainSash, SWT.BORDER);
		imgCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		mainSash.setWeights(new int[]{20, 80});
		
		return cont;
	}
	
	
	
	@Override
	protected void okPressed() {
		super.okPressed();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
		newShell.setMinimumSize(800, 600);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(1024, 768);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
		// setBlockOnOpen(false);
	}
}
