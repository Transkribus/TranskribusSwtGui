package eu.transkribus.swt_gui.dialogs;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.swt.util.ImgUrlListViewer;
import eu.transkribus.swt_gui.util.DocPageViewer;

public class BatchImageReplaceDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(BatchImageReplaceDialog.class);
	
	TrpDoc target;
	DocPageViewer targetViewer;
	ImgUrlListViewer sourceViewer;
	
//	List<TrpPage> sourcePages;
	List<File> sourceImgs;
	ArrayList<URL> sourceUrls;
	
	List<URL> checkedUrls;
	List<TrpPage> checkedPages;
	
//	List<Boolean> checked;

	public BatchImageReplaceDialog(Shell parentShell, TrpDoc target, List<File> sourceImgs) {
		super(parentShell);

		Assert.assertNotNull("target document null!", target);
		Assert.assertNotNull("source imgs null!", sourceImgs);
		
		this.target = target;
		this.sourceImgs = sourceImgs;
		this.sourceUrls = new ArrayList<URL>();
		for (File f : sourceImgs) {
			try {
				sourceUrls.add(f.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	@Override protected boolean isResizable() {
	    return true;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, true));
		
		Label titleLabel = new Label(container, SWT.WRAP | SWT.LEFT);
		titleLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 2));
		titleLabel.setText("Move pages from the source document on the right to correspond with pages on the target document on the left. Only checked pages are synced");
		
		SashForm sf = new SashForm(container, SWT.HORIZONTAL);
		sf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sf.setLayout(new GridLayout(2, true));
		
		targetViewer = new DocPageViewer(sf, 0, true, true, false);
		targetViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		targetViewer.setDataList(target.getPages());
		targetViewer.setTitle("Target doc: "+target.getMd().getTitle()+" ("+target.getNPages()+" Pages)");
		
		sourceViewer = new ImgUrlListViewer(sf, 0, true, true, false);
		sourceViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sourceViewer.setDataList(sourceUrls);
		sourceViewer.setTitle("Source Images ("+sourceUrls.size()+")");
		
		sf.setWeights(new int[] { 1, 1 } );

		return container;
	}
	
	@Override protected void okPressed() {
		
		checkedUrls = sourceViewer.getCheckedDataList();
		checkedPages = targetViewer.getCheckedDataList();
		
		super.okPressed();
	}
	
	public List<URL> getCheckedUrls() {
		return checkedUrls;
	}

	public List<TrpPage> getCheckedPages() {
		return checkedPages;
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
		return new Point(800, 600);
	}

}
