package eu.transkribus.swt_gui.htr;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt.util.SWTUtil;

public class PyLaiaRecognitionConfComposite extends Composite {
	
	Button doLinePolygonSimplificationBtn, clearLinesBtn;
	LabeledText batchSizeText;

	public PyLaiaRecognitionConfComposite(Composite parent) {
		super(parent, 0);
		this.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		
		doLinePolygonSimplificationBtn = new Button(this, SWT.CHECK);
		doLinePolygonSimplificationBtn.setText("Do polygon simplification");
		doLinePolygonSimplificationBtn.setToolTipText("Perform a line polygon simplification after the recognition process to reduce the number of points");
		doLinePolygonSimplificationBtn.setSelection(true);
		doLinePolygonSimplificationBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		clearLinesBtn = new Button(this, SWT.CHECK);
		clearLinesBtn.setText("Clear lines");
		clearLinesBtn.setToolTipText("Clear existing transcriptions before recognition");
		clearLinesBtn.setSelection(true);
		clearLinesBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));	
		
		batchSizeText = new LabeledText(this, "Batch size: ");
		batchSizeText.setText(""+10);
		batchSizeText.setToolTipText("Number of lines that are simultaneously decoded - if you get a memory error, decrease this value");
		batchSizeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));	
	}

}
