package eu.transkribus.swt_gui.dialogs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.catti.CattiRequest;
import eu.transkribus.core.model.beans.pagecontent.TextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.canvas.SWTCanvas;
import eu.transkribus.swt_gui.canvas.shapes.CanvasPolyline;
import eu.transkribus.swt_gui.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.transcription.ATranscriptionWidget;
import eu.transkribus.swt_gui.transcription.listener.ITranscriptionWidgetListener;
import eu.transkribus.swt_gui.util.ProgramUpdater;
import eu.transkribus.util.IndexTextUtils;

public class DebuggerDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(DebuggerDialog.class);
	
	public Button invalidateSessionBtn;
	
	public TrpMainWidget mw = TrpMainWidget.getInstance();
	public SWTCanvas canvas = mw.getCanvas();
	
	public Storage storage = Storage.getInstance();
	public Button processUploadedZipFileBtn;
	public Text processZipFileText;
	public Button listLibsBtn, clearDebugText;
	
	public StyledText debugText;
	
	public Button sortBaselinePts;
	public LabeledText sortXText, sortYText;
	public Button sortBaselineAllRegionsBtn;
	Button syncWithLocalDocBtn;
	Button applyAffineTransformBtn;
	Button batchReplaceImgsBtn;
	Button openSleakBtn;
	
	public Button lineToWordSegBtn;
	
	ITranscriptionWidgetListener twl;
	
	public DebuggerDialog(Shell parent) {
		super(parent);
	}
	
	@Override protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setSize(800, 800);
		SWTUtil.centerShell(shell);
		shell.setText("Debugging Dialog");
	}
	
	@Override protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE | SWT.RESIZE);
		setBlockOnOpen(false);
	}
	
	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, true));
		
		Composite btns = new Composite(container, 0);
		btns.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btns.setLayout(new RowLayout(SWT.HORIZONTAL));

		invalidateSessionBtn = new Button(btns, SWT.PUSH);
		invalidateSessionBtn.setText("Invalidate session");
		
		lineToWordSegBtn = new Button(btns, SWT.PUSH);
		lineToWordSegBtn.setText("Line2Word Seg");
		lineToWordSegBtn.setToolTipText("Perform line to word segmentation on current line - WARNING: EXPERIMENTAL!");
		
		syncWithLocalDocBtn = new Button(btns, SWT.PUSH);
		syncWithLocalDocBtn.setText("Sync with local doc");
		
		applyAffineTransformBtn = new Button(btns, SWT.PUSH);
		applyAffineTransformBtn.setText("Apply affine transformation");
		
		batchReplaceImgsBtn = new Button(btns, SWT.PUSH);
		batchReplaceImgsBtn.setText("Batch replace images");	
		
		openSleakBtn = new Button(btns, SWT.PUSH);
		openSleakBtn.setText("Open Sleak");
		
//		new Label(shell, SWT.NONE);
		
		if (false) {
		processUploadedZipFileBtn = new Button(container, SWT.NONE);
		processUploadedZipFileBtn.setText("Process uploaded zip file");
		
		processZipFileText = new Text(container, SWT.BORDER);
		processZipFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		}
		
		listLibsBtn = new Button(container, 0);
		listLibsBtn.setText("List libs");		
		
//		new Label(container, 0);
		
		Group sortBaselinePtsGroup = new Group(container, 0);
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
				
//		new Label(shell, 0); // spacer label
		
		clearDebugText = new Button(container, SWT.PUSH);
		clearDebugText.setText("Clear log");
		clearDebugText.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				debugText.setText("");
			}
		});
		
		debugText = new StyledText(container, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
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
				
		return container;
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
		
		SWTUtil.onSelectionEvent(syncWithLocalDocBtn, (e) -> {mw.syncWithLocalDoc();} );
		SWTUtil.onSelectionEvent(applyAffineTransformBtn, (e) -> {mw.applyAffineTransformToDoc();} );
		SWTUtil.onSelectionEvent(batchReplaceImgsBtn, (e) -> {mw.batchReplaceImagesForDoc();} );
		SWTUtil.onSelectionEvent(openSleakBtn, (e) -> { mw.openSleak(); } );
				
		if (processUploadedZipFileBtn != null) {
			processUploadedZipFileBtn.addSelectionListener(new SelectionAdapter() {		
				@Override public void widgetSelected(SelectionEvent e) {
					String zipFn = processZipFileText.getText().trim();
					logger.debug("trying to process zip file: "+zipFn);
					try {
						int colId = mw.getUi().getServerWidget().getSelectedCollectionId();
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
				if (tw!=null && tw.getCurrentLineObject()!=null) {
					TrpTextLineType tl = tw.getCurrentLineObject();
			
					List<TrpWordType> segmentedWords = IndexTextUtils.getWordsFromLine(tl);
					logger.debug("performed line 2 word seg");
					
					// remove old words:
					List<TrpWordType> oldWords = new ArrayList<TrpWordType>();
					oldWords.addAll(tl.getTrpWord());
					for (TrpWordType w : oldWords) {
						ICanvasShape cs = canvas.getScene().findShapeWithData(w);
						if (cs != null) {
							mw.getCanvas().getShapeEditor().removeShapeFromCanvas(cs, false);
						}
					}
					
					// add new words:
					int i=0;
					for (TrpWordType w : segmentedWords) {
						w.setLine(tl);
						w.reInsertIntoParent(i++);
						
						try {
							mw.getShapeFactory().addCanvasShape(w);
						} catch (Exception e1) {
							e1.printStackTrace();
						}	
					}
					
					canvas.redraw();
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
}
