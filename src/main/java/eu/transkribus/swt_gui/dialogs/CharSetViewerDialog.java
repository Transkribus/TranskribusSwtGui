package eu.transkribus.swt_gui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uros.citlab.tokenizer.categorizer.CategoryUtils;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.TableLabelProvider;

public class CharSetViewerDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(CharSetViewerDialog.class);

	// constants
	private final static Font DEFAULT_FONT = Fonts.createFont(Fonts.getSystemFontName(false, true, false), 15,
			SWT.NONE);

	private final static String TITLE = "Character Set of Model: ";

	private final static String SYMBOL_COL_NAME = "Symbol";
	private final static String UNICODE_NAME_COL_NAME = "Unicode Name";
	private final static String UNICODE_CATEGORY_COL_NAME = "Unicode Category";

	// UI elements
	private Button fontButton;
	private MyTableViewer charSetTableViewer;
	private CharSetLabelAndFontProvider labelAndFontProvider;

	// data
	private String title;
	private List<String> charSet;

	public CharSetViewerDialog(Shell parent, final String title, List<String> charSet) {
		super(parent);
		this.title = title;
		setCharSet(charSet);
	}

	public CharSetViewerDialog(Shell parent, final TrpHtr htr) {
		this(parent,
				htr == null ? TITLE : TITLE + htr.getName(),
				htr == null ? null : htr.getCharSetList());
	}

	public void setVisible() {
		if (super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		cont.setLayout(new FillLayout());

		charSetTableViewer = new MyTableViewer(cont, SWT.V_SCROLL);
		charSetTableViewer.addColumns(new ColumnConfig[] {
				new ColumnConfig(SYMBOL_COL_NAME, 75),
				new ColumnConfig(UNICODE_NAME_COL_NAME, 400),
				/*
				 * TODO Unicode Categories in CITlab HTR are based on a newer Unicode
				 * Specification than that implemented in Java 8 and we need to show the same
				 * mapping here as the once CITlab HTR uses (not depending on the Java version
				 * installed on the client!). CategoryUtils from CITlabTokenizer needs to be
				 * used for this but it won't return a descriptive label for the Category but
				 * just a two letter code. We can show the then correct category once this is
				 * implemented.
				 */
//				new ColumnConfig(UNICODE_CATEGORY_COL_NAME, 150)
		});

		charSetTableViewer.setContentProvider(new ArrayContentProvider());
		charSetTableViewer.getTable().setLinesVisible(true);
		charSetTableViewer.getTable().setHeaderVisible(true);

		labelAndFontProvider = new CharSetLabelAndFontProvider(charSetTableViewer);
		charSetTableViewer.setLabelProvider(labelAndFontProvider);

		updateTable();
		cont.pack();
		return cont;
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		// add the font selection button before the close button
		parent.setLayout(new GridLayout(2, false));
		fontButton = new Button(parent, SWT.PUSH);
		fontButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		fontButton.setText("Change font...");
		fontButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FontData fontData = openFontDialog(parent);
				if (fontData == null) {
					return;
				}
				labelAndFontProvider.setFont(fontData);
			}
		});

		// add a close button that essentially triggers a cancel operation
		createButton(parent, CANCEL, "Close", true);
	}

	private FontData openFontDialog(Composite parent) {
		FontDialog fd = new FontDialog(parent.getShell(), SWT.NONE);
		fd.setFontList(labelAndFontProvider.getSelectedFont().getFontData());
		fd.setEffectsVisible(false);
		fd.setText("Select Font");
		return fd.open();
	}

	private void updateTable() {
		charSetTableViewer.setInput(charSet);
	}

	private void setCharSet(List<String> charSet) {
		if (charSet == null) {
			this.charSet = new ArrayList<>(0);
		} else {
			this.charSet = new ArrayList<>(charSet);
		}
	}


	public void update(TrpHtr selectedHtr) {
		if(selectedHtr == null) {
			return;
		}
		this.getShell().setText(TITLE + selectedHtr.getName());
		updateTable(selectedHtr.getCharSetList());
	}
	
	private void updateTable(List<String> charSet) {
		setCharSet(charSet);
		updateTable();
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
		newShell.setMinimumSize(300, 400);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(640, 700);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
	}

	private static class CharSetLabelAndFontProvider extends TableLabelProvider {
		private final MyTableViewer tv;
		private Font font;

		CharSetLabelAndFontProvider(MyTableViewer tv) {
			this.tv = tv;
			this.font = DEFAULT_FONT;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			TableColumn column = tv.getColumn(columnIndex);

			if (!(element instanceof String)) {
				return "Error: Unknown object";
			}
			String symbol = (String) element;

			if (symbol.length() != 1) {
				logger.warn("CharSet contains a symbol with length {}: {}", symbol.length(), symbol);
			}

			switch (column.getText()) {
			case SYMBOL_COL_NAME:
				return symbol;
			case UNICODE_NAME_COL_NAME:
				String name = Character.getName(symbol.codePointAt(0));
				if (name == null) {
					name = "UNASSIGNED";
				}
				return name;
			case UNICODE_CATEGORY_COL_NAME:
				/*
				 * FIXME CategoryUtils returns a two letter code rather than a descriptive
				 * label. See comment in column definition of the table.
				 */
				return CategoryUtils.getCategory(symbol.charAt(0));
			default:
				return super.getColumnText(element, columnIndex);
			}
		}

		@Override
		public Font getFont(Object element, int columnIndex) {
			TableColumn column = tv.getColumn(columnIndex);
			switch (column.getText()) {
			case SYMBOL_COL_NAME:
				return font;
			default:
				return null;
			}
		}

		public Font getSelectedFont() {
			return font;
		}

		public void setFont(FontData fontData) {
			if (fontData == null) {
				font = DEFAULT_FONT;
				return;
			}
			font = Fonts.createFont(fontData.getName(), fontData.getHeight(), fontData.getStyle());
			tv.refresh(true);
		}
	}
}
