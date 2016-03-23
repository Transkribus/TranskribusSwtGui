package eu.transkribus.swt_gui.dialogs;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_canvas.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.util.ProgramUpdater;

public class DebuggerDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(DebuggerDialog.class);

	public Object result;
	public Shell shell;
	
	public Button invalidateSessionBtn;
	
	public TrpMainWidget mw = TrpMainWidget.getInstance();
	public Storage storage = Storage.getInstance();
	public Button processUploadedZipFileBtn;
	public Text processZipFileText;
	public Button listLibsBtn, clearDebugText;
	
	public StyledText debugText;
	

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
				} catch (Exception ex) {
					mw.onError("An error occured", ex.getMessage(), ex);
				}
			}
		};
		invalidateSessionBtn.addSelectionListener(selectionAdapter);
		
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
		return result;
	}

	public Shell getShell() {
		return shell;
	}



}
