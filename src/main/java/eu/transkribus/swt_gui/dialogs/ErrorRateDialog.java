package eu.transkribus.swt_gui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import eu.transkribus.core.model.beans.TrpErrorRate;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.util.DesktopUtil;
import eu.transkribus.swt.util.Images;

public class ErrorRateDialog extends Dialog {

	public static final String ERR_EMPTY_COL = " ";
	public static final String ERR_WORD_COL = "Error Rate";
	public static final String ERR_CHAR_COL = "Accuracy";
	public static final String BAG_PREC_COL = "Precision";
	public static final String BAG_REC_COL = "Recall";
	public static final String BAG_FMEA_COL = "F-Measure";

	protected static final String HELP_WIKI_ERR = "https://en.wikipedia.org/wiki/Word_error_rate";
	protected static final String HELP_WIKI_FMEA = "https://en.wikipedia.org/wiki/F1_score";

	MyTableViewer viewer;

	private TrpErrorRate resultErr;

	private Button wikiErrButton;
	private Button wikiFmeaButton;

	public final ColumnConfig[] ERR_COLS = new ColumnConfig[] { new ColumnConfig(ERR_EMPTY_COL, 200),
			new ColumnConfig(ERR_WORD_COL, 100), new ColumnConfig(ERR_CHAR_COL, 100),
			new ColumnConfig(BAG_PREC_COL, 100), new ColumnConfig(BAG_REC_COL, 100),
			new ColumnConfig(BAG_FMEA_COL, 100), };

	public ErrorRateDialog(Shell parentShell, TrpErrorRate resultErr) {

		super(parentShell);
		this.resultErr = resultErr;

	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Error Rate Results");
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite body = (Composite) super.createDialogArea(parent);

		final MyTableViewer viewer = new MyTableViewer(body, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

		viewer.getTable().setLinesVisible(true);

		viewer.addColumns(ERR_COLS);

		Table table = viewer.getTable();
		table.setHeaderVisible(true);

		// Hyperlinks in Table , workaround with Buttons
		// TableViewerColumn linksCol = new TableViewerColumn(viewer,links);
		// linksCol.setLabelProvider(new ColumnLabelProvider() {
		//
		// Map<Object, Hyperlink> buttons = new HashMap<Object,Hyperlink>();
		//
		// @Override
		// public void update(ViewerCell cell) {
		// TableItem item = (TableItem) cell.getItem();
		// Hyperlink hyperlink = new Hyperlink(table, SWT.NONE);
		// if (buttons.containsKey(cell.getElement())) {
		// hyperlink = buttons.get(cell.getElement());
		// }
		// else{
		// for(String link : URLs) {
		// hyperlink = new Hyperlink((Composite)
		// cell.getViewerRow().getControl(),SWT.NONE);
		// hyperlink.setText(link);
		// hyperlink.setHref(link);
		// hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
		// public void linkActivated(HyperlinkEvent e) {
		// System.out.println(e.getHref());
		// org.eclipse.swt.program.Program.launch(hyperlink.getHref().toString());
		// }
		//
		// });
		// buttons.put(cell.getElement(), hyperlink);
		//
		// TableEditor editor = new TableEditor(item.getParent());
		// editor.grabHorizontal = true;
		// editor.grabVertical = true;
		// editor.setEditor(hyperlink , item, cell.getColumnIndex());
		// editor.layout();
		//
		// }
		//
		// }
		// }
		//
		// });

		TableItem itemWord = new TableItem(table, SWT.NONE);
		itemWord.setText(new String[] { "Word", resultErr.getWer(), resultErr.getwAcc(), "", "", "" });

		TableItem itemChar = new TableItem(table, SWT.NONE);
		itemChar.setText(new String[] { "Character", resultErr.getCer(), resultErr.getcAcc(), "", "", "" });

		TableItem itemBag = new TableItem(table, SWT.NONE);
		itemBag.setText(new String[] { "Bag of Tokens", "", "", resultErr.getBagTokensPrec(),
				resultErr.getBagTokensRec(), resultErr.getBagTokensF() });

		return body;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		wikiErrButton = createButton(parent, IDialogConstants.HELP_ID, "Error Rate", false);
		wikiErrButton.setImage(Images.HELP);

		wikiFmeaButton = createButton(parent, IDialogConstants.HELP_ID, "F-Measure", false);
		wikiFmeaButton.setImage(Images.HELP);

		createButton(parent, IDialogConstants.OK_ID, "OK", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
		GridData buttonLd = (GridData) getButton(IDialogConstants.CANCEL_ID).getLayoutData();
		wikiErrButton.setLayoutData(buttonLd);
		wikiErrButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DesktopUtil.browse(HELP_WIKI_ERR, "You can find the relevant information on the Wikipedia page.",
						getParentShell());
			}
		});

		wikiFmeaButton.setLayoutData(buttonLd);
		wikiFmeaButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DesktopUtil.browse(HELP_WIKI_FMEA, "You can find the relevant information on the Wikipedia page.",
						getParentShell());
			}
		});

	}
}
