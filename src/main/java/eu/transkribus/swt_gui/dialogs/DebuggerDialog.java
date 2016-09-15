package eu.transkribus.swt_gui.dialogs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.catti.CattiRequest;
import eu.transkribus.core.model.beans.pagecontent.TextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.core.util.SebisStopWatch;
import eu.transkribus.swt_canvas.canvas.shapes.CanvasPolyline;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_canvas.util.LabeledText;
import eu.transkribus.swt_canvas.util.SWTUtil;
import eu.transkribus.swt_gui.canvas.TrpSWTCanvas;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.transcription.ATranscriptionWidget;
import eu.transkribus.swt_gui.transcription.LineTranscriptionWidget;
import eu.transkribus.swt_gui.transcription.listener.ITranscriptionWidgetListener;
import eu.transkribus.swt_gui.util.ProgramUpdater;
import eu.transkribus.util.IndexTextUtils;

public class DebuggerDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(DebuggerDialog.class);

	public Object result;
	public Shell shell;
	
	public Button invalidateSessionBtn;
	
	public TrpMainWidget mw = TrpMainWidget.getInstance();
	public TrpSWTCanvas canvas = mw.getCanvas();
	
	public Storage storage = Storage.getInstance();
	public Button processUploadedZipFileBtn;
	public Text processZipFileText;
	public Button listLibsBtn, clearDebugText;
	
	public StyledText debugText;
	
	public Button sortBaselinePts;
	public LabeledText sortXText, sortYText;
	public Button sortBaselineAllRegionsBtn;
	
	public Button lineToWordSegBtn;
	
	ITranscriptionWidgetListener twl;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public DebuggerDialog(Shell parent, int style) {
		super(parent, style |= (SWT.DIALOG_TRIM | SWT.RESIZE) );
		setText("Debugging Dialog");
	}
	
	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.setSize(600, 600);
		shell.setText(getText());
		shell.setLayout(new GridLayout(2, false));

		invalidateSessionBtn = new Button(shell, SWT.PUSH);
		invalidateSessionBtn.setText("Invalidate session");
		
		new Label(shell, 0);
		
		Group sortBaselinePtsGroup = new Group(shell, 0);
		sortBaselinePtsGroup.setText("Sort baseline pts");
		
		sortBaselinePtsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		sortBaselinePtsGroup.setLayout(new GridLayout(3, false));
		sortXText = new LabeledText(sortBaselinePtsGroup, "X = ");
		sortXText.text.setText("1");
		sortYText = new LabeledText(sortBaselinePtsGroup, "Y = ");
		sortYText.text.setText("0");
		sortBaselineAllRegionsBtn = new Button(sortBaselinePtsGroup, SWT.CHECK);
		sortBaselineAllRegionsBtn.setSelection(true);
		sortBaselineAllRegionsBtn.setText("All regions");
		
		sortBaselinePts = new Button(sortBaselinePtsGroup, SWT.PUSH);
		sortBaselinePts.setText("Sort!");
		
		lineToWordSegBtn = new Button(shell, SWT.PUSH);
		lineToWordSegBtn.setText("Line2Word Seg");
		
//		new Label(shell, SWT.NONE);
		
		if (false) {
		processUploadedZipFileBtn = new Button(shell, SWT.NONE);
		processUploadedZipFileBtn.setText("Process uploaded zip file");
		
		processZipFileText = new Text(shell, SWT.BORDER);
		processZipFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		}
		
		listLibsBtn = new Button(shell, 0);
		listLibsBtn.setText("List libs");
		
//		new Label(shell, 0); // spacer label
		
		clearDebugText = new Button(shell, SWT.PUSH);
		clearDebugText.setText("Clear log");
		clearDebugText.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				debugText.setText("");
			}
		});
		
		debugText = new StyledText(shell, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		debugText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		debugText.addLineStyleListener(new LineStyleListener() {
			public void lineGetStyle(LineStyleEvent e) {
				// Set the line number
				e.bulletIndex = debugText.getLineAtOffset(e.lineOffset);

				// Set the style, 12 pixles wide for each digit
				StyleRange style = new StyleRange();
				style.metrics = new GlyphMetrics(0, 0, Integer.toString(debugText.getLineCount() + 1).length() * 12);

				// Create and set the bullet
				e.bullet = new Bullet(ST.BULLET_NUMBER, style);
			}
		});	
		
		addListener();
		
		// prevent closing:
//		shell.addListener(SWT.Close, new Listener() {
//			@Override public void handleEvent(Event event) {
//				logger.debug("I WONT QUIT!");
//				event.doit = false;
//				shell.setVisible(false);
//			}
//		});
//		shell.pack();
	}
	
	private void addListener() {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				try {
					if (e.widget == invalidateSessionBtn) {
						logger.debug("invalidating session...");
						storage.invalidateSession();
					}
					if (e.widget == sortBaselinePts) {
						sortBaselinePts();
					}
				} catch (Throwable ex) {
					mw.onError("An error occured", ex.getMessage(), ex);
				}
			}
		};
		invalidateSessionBtn.addSelectionListener(selectionAdapter);
		sortBaselinePts.addSelectionListener(selectionAdapter);
		
		if (processUploadedZipFileBtn != null) {
			processUploadedZipFileBtn.addSelectionListener(new SelectionAdapter() {		
				@Override public void widgetSelected(SelectionEvent e) {
					String zipFn = processZipFileText.getText().trim();
					logger.debug("trying to process zip file: "+zipFn);
					try {
						int colId = mw.getUi().getDocOverviewWidget().getSelectedCollectionId();
						storage.getConnection().processTrpDocFromFtp(colId, zipFn);
					} catch (Exception e1) {
						mw.onError("An error occured", e1.getMessage(), e1);
					}
				}
			});
		}
		
		listLibsBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				try {
					ProgramUpdater.getLibs(true);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		twl = new ITranscriptionWidgetListener() {
			@Override public void onCattiMessage(CattiRequest r, String message) {
				logger.debug("catti message: "+message);
				if (!SWTUtil.isDisposed(debugText)) {
					Display.getDefault().asyncExec(() -> debugText.append(message+"\n"));
				}
			}
		};
		
		mw.getUi().getLineTranscriptionWidget().addListener(twl);
		
		// line2word seg
		lineToWordSegBtn.addSelectionListener(new SelectionAdapter() {
			
			@Override public void widgetSelected(SelectionEvent e) {
				ATranscriptionWidget tw = mw.getUi().getSelectedTranscriptionWidget();
				if (tw instanceof LineTranscriptionWidget) {
					LineTranscriptionWidget lw = (LineTranscriptionWidget) tw;
					TrpTextLineType tl = (TrpTextLineType) lw.getTranscriptionUnit();
					if (tl != null) {
						SebisStopWatch.SW.start();
						List<TrpWordType> words = IndexTextUtils.getWordsFromLine(tl);
						SebisStopWatch.SW.stop(true, "performed line 2 word seg", logger);
						
						List<TrpWordType> oldWords = new ArrayList<TrpWordType>();
						oldWords.addAll(tl.getTrpWord());
						
						for (TrpWordType w : oldWords) {
							ICanvasShape cs = canvas.getScene().findShapeWithData(w);
							if (cs != null) {
								mw.getCanvas().getShapeEditor().removeShapeFromCanvas(cs, false);
							}
						}
						
						
						for (TrpWordType w : words) {
							w.setLine(tl);
							try {
								mw.getShapeFactory().addCanvasShape(w);
							} catch (Exception e1) {
								e1.printStackTrace();
							}	
						}
						
						canvas.redraw();
						
//						tl.getWord().clear();
//						tl.getWord().addAll(words);
//						tl.getPage().setEdited(true);
						
//						lw.r
					}
				}
			}
		});
	}
	
	void sortBaselinePts() {
		int x = 1; int y = 0;
		try {
			x = Integer.parseInt(sortXText.getText());
		} catch (Exception ex) {
		}
		try {
			y = Integer.parseInt(sortYText.getText());
		} catch (Exception ex) {
		}						
		
		List<TrpTextRegionType> regions = new ArrayList<>();
		if (sortBaselineAllRegionsBtn.getSelection()) {
			regions.addAll(storage.getTranscript().getPage().getTextRegions(false));
		} else {
			ICanvasShape s = canvas.getFirstSelected();
			if (s != null && s.getData() instanceof TrpTextRegionType) {
				regions.add((TrpTextRegionType) s.getData());
			}
		}
		
		logger.debug("sorting baseline pts, x = "+x+" y = "+y+" nregions = "+regions.size());
		
		for (TrpTextRegionType r : regions) {
			for (TextLineType l : r.getTextLine()) {
				logger.debug("sorting baseline pts for line: "+l);
				TrpTextLineType tl = (TrpTextLineType) l;
				if (tl.getBaseline() != null) {
					ICanvasShape bls = canvas.getScene().findShapeWithData(tl.getBaseline());
//					logger.debug("bls = "+bls);
					if (bls instanceof CanvasPolyline) {
						logger.debug("sorting baseline pts!");
						CanvasPolyline pl = (CanvasPolyline) bls;
						pl.sortPoints(x, y);
					}
				}
			}
		}
		mw.getCanvas().redraw();
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		
		SWTUtil.centerShell(shell);
		shell.open();
		shell.layout();

		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		mw.getUi().getLineTranscriptionWidget().removeListener(twl);
		
		return result;
	}

	public Shell getShell() {
		return shell;
	}



}
