package eu.transkribus.swt_gui.htr;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt.util.SWTUtil;

public class PyLaiaRecognitionConfComposite extends Composite {
	
	Button doLinePolygonSimplificationBtn;
//	Button clearLinesBtn;
	Button doWordSegBtn;
	LabeledText batchSizeText;
	
	Button useExistingLinePolygonsBtn;
	Button useComputedLinePolygonsBtn;

	public PyLaiaRecognitionConfComposite(Composite parent) {
		super(parent, 0);
		this.setLayout(SWTUtil.createGridLayout(2, false, 0, 0));
		
		useComputedLinePolygonsBtn= new Button(this, SWT.RADIO);
		useComputedLinePolygonsBtn.setText("Compute line polygons");
		useComputedLinePolygonsBtn.setToolTipText("Line polygons are automatically computed from baselines - the existing ones are deleted!");
		useComputedLinePolygonsBtn.setSelection(true);
		useComputedLinePolygonsBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		SWTUtil.onSelectionEvent(useComputedLinePolygonsBtn, e -> updateUi());
		
		useExistingLinePolygonsBtn = new Button(this, SWT.RADIO);
		useExistingLinePolygonsBtn.setText("Use existing line polygons");
		useExistingLinePolygonsBtn.setToolTipText("Do *not* perform a baseline to polygon computation but use the existing line polygons.\nUse this if you have exact line polygons e.g. from an OCR engine.");
		useExistingLinePolygonsBtn.setSelection(false);
		useExistingLinePolygonsBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		SWTUtil.onSelectionEvent(useExistingLinePolygonsBtn, e -> updateUi());
		
		doLinePolygonSimplificationBtn = new Button(this, SWT.CHECK);
		doLinePolygonSimplificationBtn.setText("Do polygon simplification");
		doLinePolygonSimplificationBtn.setToolTipText("Perform a line polygon simplification after the recognition process to reduce the number of points");
		doLinePolygonSimplificationBtn.setSelection(true);
		doLinePolygonSimplificationBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
//		clearLinesBtn = new Button(this, SWT.CHECK);
//		clearLinesBtn.setText("Clear lines");
//		clearLinesBtn.setToolTipText("Clear existing transcriptions before recognition");
//		clearLinesBtn.setSelection(true);
//		clearLinesBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));	
		
		doWordSegBtn = new Button(this, SWT.CHECK);
		doWordSegBtn.setText("Add estimated word coordinates");
		doWordSegBtn.setToolTipText("Adds approximate bounding boxes for the recognized words inside the lines");
		doWordSegBtn.setSelection(true);
		doWordSegBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));	
		
		batchSizeText = new LabeledText(this, "Batch size: ");
		batchSizeText.setText(""+10);
		batchSizeText.setToolTipText("Number of lines that are simultaneously decoded - if you get a memory error, decrease this value");
		batchSizeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));	
	}
	
	private void updateUi() {
		if (useExistingLinePolygonsBtn.getSelection()) {
			doLinePolygonSimplificationBtn.setSelection(false);
		}
		else {
			doLinePolygonSimplificationBtn.setSelection(true);
		}
	}

}
