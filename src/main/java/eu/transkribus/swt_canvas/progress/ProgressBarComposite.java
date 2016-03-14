package eu.transkribus.swt_canvas.progress;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ProgressBar;

import eu.transkribus.swt_canvas.util.Images;

public class ProgressBarComposite extends Composite {
	
	Button cancelButton;
	Composite cancelComposite;
	// Label lineLabel;
	Composite progressBarComposite;

	CLabel taskNameLabel;
	CLabel subTaskLabel, detailLabel;
	ProgressBar progressBar = null;
	Image processImage = Images.getOrLoad("/icons/wait.gif");
	protected String taskName = "Processing...";
	final protected int progressBarStyle = SWT.SMOOTH;
	
	public ProgressBarComposite(Composite parent, int style) {
		super(parent, style);
		createContents();
	}
	
	protected void createContents() {
		this.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		this.setLayout(new GridLayout());		
		Composite composite = this;

		taskNameLabel = new CLabel(composite, SWT.NONE);
		taskNameLabel.setImage(processImage);
		taskNameLabel.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		taskNameLabel.setText(taskName);

		subTaskLabel = new CLabel(composite, SWT.NONE);
		subTaskLabel.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		subTaskLabel.setText("");

		progressBarComposite = new Composite(this, SWT.NONE);
		progressBarComposite.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
		progressBarComposite.setLayout(new FillLayout());

		progressBar = new ProgressBar(progressBarComposite, progressBarStyle);
		progressBar.setMaximum(0);
		
		detailLabel = new CLabel(this, SWT.NONE);
		detailLabel.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));

		cancelComposite = new Composite(this, SWT.NONE);
		cancelComposite.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		cancelComposite.setLayout(gridLayout_1);

		cancelButton = new Button(cancelComposite, SWT.NONE);

		cancelButton.setLayoutData(new GridData(78, SWT.DEFAULT));
		cancelButton.setText("Cancel");
	}


}
