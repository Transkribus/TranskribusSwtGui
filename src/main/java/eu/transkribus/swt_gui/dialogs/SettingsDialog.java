package eu.transkribus.swt_gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import eu.transkribus.swt_canvas.canvas.CanvasSettings;
import eu.transkribus.swt_canvas.util.databinding.DataBinder;
import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.mainwidget.TrpSettings;

public class SettingsDialog extends Dialog {

	protected Object result;
	protected Shell shell;

	private Spinner lineWidthSpinner;
	private Spinner selectedLineWidthSpinner;
	private Spinner foregroundAlphaSpinner, backgroundAlphaSpinner;
	private Spinner selectedPointRadiusSpinner;
	private Spinner newDrawLineWidthSpinner;
	private Button newDrawColorButton;
	
	private Spinner readingOrderCircleWidthSpinner;
	

	CanvasSettings canvasSets;
	TrpSettings trpSets;
	private Button drawSelectedCornerNumbersCb;
	private Button doTransitionCb;
	private Button psColorButton;
	private Button regionsColorButton;
	private Button linesColorButton;
	private Button baselinesColorButton;
	private Button wordsColorButton;
	
	private Button tablesColorButton;
	private Button tableCellsColorButton;
	
	
	Button closebutton;
	private Button drawPolylineArcs;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public SettingsDialog(Shell parent, int style, CanvasSettings canvasSets, TrpSettings trpSets) {
		super(parent, style);
		setText("Change Viewing Settings");
		
		this.trpSets = trpSets;
		this.canvasSets = canvasSets;
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		addBindings();
		
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}
	
	private void addBindings() {
		DataBinder.get().bindBeanToWidgetSelection(CanvasSettings.DRAW_LINE_WIDTH_PROPERTY, canvasSets, lineWidthSpinner);
		DataBinder.get().bindBeanToWidgetSelection(CanvasSettings.SELECTED_LINE_WIDTH_PROPERTY, canvasSets, selectedLineWidthSpinner);
		DataBinder.get().bindBeanToWidgetSelection(CanvasSettings.SELECTED_POINT_RADIUS_PROPERTY, canvasSets, selectedPointRadiusSpinner);
		
		DataBinder.get().bindBeanToWidgetSelection(CanvasSettings.NEW_DRAW_LINE_WIDTH_PROPERTY, canvasSets, newDrawLineWidthSpinner);
		DataBinder.get().bindColorToButton(CanvasSettings.NEW_DRAW_COLOR_PROPERTY, canvasSets, newDrawColorButton);
		
		DataBinder.get().bindBeanToWidgetSelection(CanvasSettings.DRAW_SELECTED_CORNER_NUMBERS_PROPERTY, canvasSets, drawSelectedCornerNumbersCb);
		DataBinder.get().bindBeanToWidgetSelection(CanvasSettings.DRAW_POLYLINE_ARCS_PROPERTY, canvasSets, drawPolylineArcs);
		DataBinder.get().bindBeanToWidgetSelection(CanvasSettings.DO_TRANSITION_PROPERTY, canvasSets, doTransitionCb);
		
		DataBinder.get().bindBeanToWidgetSelection(CanvasSettings.FOREGROUND_ALPHA_PROPERY, canvasSets, foregroundAlphaSpinner);
		DataBinder.get().bindBeanToWidgetSelection(CanvasSettings.BACKGROUND_ALPHA_PROPERTY, canvasSets, backgroundAlphaSpinner);
		
		DataBinder.get().bindBeanToWidgetSelection(CanvasSettings.READING_ORDER_PROPERTY, canvasSets, readingOrderCircleWidthSpinner);
		
		DataBinder.get().bindColorToButton(TrpSettings.COLOR_PS_PROPERTY, trpSets, this.psColorButton);
		DataBinder.get().bindColorToButton(TrpSettings.COLOR_REGIONS_PROPERTY, trpSets, this.regionsColorButton);
		DataBinder.get().bindColorToButton(TrpSettings.COLOR_LINES_PROPERTY, trpSets, this.linesColorButton);
		DataBinder.get().bindColorToButton(TrpSettings.COLOR_BASELINES_PROPERTY, trpSets, this.baselinesColorButton);
		DataBinder.get().bindColorToButton(TrpSettings.COLOR_WORDS_PROPERTY, trpSets, this.wordsColorButton);
		DataBinder.get().bindColorToButton(TrpSettings.COLOR_TABLES_PROPERTY, trpSets, this.tablesColorButton);
		DataBinder.get().bindColorToButton(TrpSettings.COLOR_TABLE_CELLS_PROPERTY, trpSets, this.tableCellsColorButton);
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
//		shell.setSize(673, 420);
		shell.setText(getText());
		shell.setLayout(new GridLayout(2, false));
		
		shell.setLocation(getParent().getSize().x/2, getParent().getSize().y/3);
		
		Label lblNewLabel = new Label(shell, SWT.NONE);
		lblNewLabel.setText("Line width");
		
		lineWidthSpinner = new Spinner(shell, SWT.BORDER);
		lineWidthSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lineWidthSpinner.setMinimum(1);
		
		Label lblSelectedLineWidth = new Label(shell, SWT.NONE);
		lblSelectedLineWidth.setText("Selected line width");
		
		selectedLineWidthSpinner = new Spinner(shell, SWT.BORDER);
		GridData gd_selectedLineWidthSpinner = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_selectedLineWidthSpinner.widthHint = 79;
		selectedLineWidthSpinner.setLayoutData(gd_selectedLineWidthSpinner);
		selectedLineWidthSpinner.setMinimum(1);
		
		Label labelPointsSize = new Label(shell, SWT.NONE);
		labelPointsSize.setText("Points size");
		
		selectedPointRadiusSpinner = new Spinner(shell, SWT.BORDER);
		selectedPointRadiusSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		selectedPointRadiusSpinner.setMinimum(1);
		selectedPointRadiusSpinner.setToolTipText("The radius of corner points when a shape is selected");
		
		Label labelNewDrawLineWidth = new Label(shell, SWT.NONE);
		labelNewDrawLineWidth.setText("New draw line width");
				
		newDrawLineWidthSpinner = new Spinner(shell, SWT.BORDER);
		newDrawLineWidthSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		newDrawLineWidthSpinner.setMinimum(1);
		newDrawLineWidthSpinner.setToolTipText("The width of the line when a new shape is drawn");
		
		Label newDrawColorLabel = new Label(shell, SWT.NONE);
		newDrawColorLabel.setText("New draw color");

		newDrawColorButton = new Button(shell, SWT.NONE);
		newDrawColorButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		newDrawColorButton.setToolTipText("The color of the line when a new shape is drawn");
		
		Label l1 = new Label(shell, SWT.NONE);
		l1.setText("Foreground alpha (0-255): ");
		l1.setToolTipText("0 = transparent, 255 = opaque");
		
		foregroundAlphaSpinner = new Spinner(shell, SWT.BORDER);
		GridData gd_foregroundAlphaSpinner = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_foregroundAlphaSpinner.widthHint = 79;
		foregroundAlphaSpinner.setLayoutData(gd_selectedLineWidthSpinner);
		foregroundAlphaSpinner.setMinimum(0);
		foregroundAlphaSpinner.setMaximum(255);
		
		Label l2 = new Label(shell, SWT.NONE);
		l2.setText("Background alpha (0-255): ");
		l2.setToolTipText("0 = transparent, 255 = opaque");
		
		backgroundAlphaSpinner = new Spinner(shell, SWT.BORDER);
		GridData gd_backgroundAlphaSpinner = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_backgroundAlphaSpinner.widthHint = 79;
		backgroundAlphaSpinner.setLayoutData(gd_backgroundAlphaSpinner);
		backgroundAlphaSpinner.setMinimum(0);
		backgroundAlphaSpinner.setMaximum(255);
		
		Label l3 = new Label(shell, SWT.NONE);
		l3.setText("Width of reading order circle: ");
		
		readingOrderCircleWidthSpinner = new Spinner(shell, SWT.BORDER);
		GridData gd_readingOrderCircleWidthSpinner = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_readingOrderCircleWidthSpinner.widthHint = 80;
		readingOrderCircleWidthSpinner.setLayoutData(gd_readingOrderCircleWidthSpinner);
		readingOrderCircleWidthSpinner.setMinimum(1);
		//readingOrderCircleWidthSpinner.setMaximum(255);
		
		drawPolylineArcs = new Button(shell, SWT.CHECK);
		drawPolylineArcs.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		drawPolylineArcs.setText("Draw baseline arcs");
		drawPolylineArcs.setToolTipText("Draw arcs on the lines of a baseline to visualize the direction");
		
		drawSelectedCornerNumbersCb = new Button(shell, SWT.CHECK);
		drawSelectedCornerNumbersCb.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		drawSelectedCornerNumbersCb.setText("Draw selected corner numbers");
		drawSelectedCornerNumbersCb.setToolTipText("Draws the corner numbers of the polygon of the selected element - for debugging purposes...");
		
		
		doTransitionCb = new Button(shell, SWT.CHECK);
		doTransitionCb.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		doTransitionCb.setText("Do transition on focus");
		doTransitionCb.setToolTipText("Determines, whether a smooth transition is performed on focusing a shape (set to false for performance enhancement)");
		
		Label psColorLabel = new Label(shell, SWT.NONE);
		psColorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		psColorLabel.setText("Printspace color");
		
		psColorButton = new Button(shell, SWT.FLAT);
//		psColorButton.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		psColorButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		Label regionsColorLabel = new Label(shell, SWT.NONE);
		regionsColorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		regionsColorLabel.setText("Regions color");
		
		regionsColorButton = new Button(shell, SWT.NONE);
		regionsColorButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
//		regionsColorButton.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		
		Label linesColorLabel = new Label(shell, SWT.NONE);
		linesColorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		linesColorLabel.setText("Lines Color");
		
		linesColorButton = new Button(shell, SWT.NONE);
		linesColorButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
//		linesColorButton.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		
		Label baselinesColorLabel = new Label(shell, SWT.NONE);
		baselinesColorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		baselinesColorLabel.setText("Baselines color");
		
		baselinesColorButton = new Button(shell, SWT.NONE);
		baselinesColorButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
//		baselinesColorButton.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		
		Label wordsColorLabel = new Label(shell, SWT.NONE);
		wordsColorLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		wordsColorLabel.setText("Words color");
		
		wordsColorButton = new Button(shell, SWT.NONE);
		wordsColorButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
//		wordsColorButton.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		
		Label tablesColorLabel = new Label(shell, SWT.NONE);
		tablesColorLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		tablesColorLabel.setText("Tables color");
		
		tablesColorButton = new Button(shell, SWT.NONE);
		tablesColorButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		Label tableCellsColorLabel = new Label(shell, SWT.NONE);
		tableCellsColorLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		tableCellsColorLabel.setText("Table cell color");
		
		tableCellsColorButton = new Button(shell, SWT.NONE);
		tableCellsColorButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		Composite buttonComposite = new Composite(shell, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		buttonComposite.setLayout(new FillLayout());
		
		closebutton = new Button(buttonComposite, SWT.NONE);
		closebutton.setText("Close");
		closebutton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
//				TrpConfig.save(); // props get saved anyway by property change listeners!
				shell.close();
			}
		});
//		saveButton.setToolTipText("Stores the configuration in the configuration file and closes the dialog");
		
//		Button closeButton = new Button(buttonComposite, SWT.PUSH);
//		closeButton.setText("Cancel");
//		closeButton.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				shell.close();
//			}	
//		});
//		closeButton.setToolTipText("Closes this dialog without saving");
		
		shell.pack();
	}
	
//	public Spinner getLineWidthSpinner() {
//		return lineWidthSpinner;
//	}
//
//	public Spinner getSelectedLineWidthSpinner() {
//		return selectedLineWidthSpinner;
//	}
}
