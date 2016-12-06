package eu.transkribus.swt_gui.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.util.Fonts;

public class CharSetViewerDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(TextRecognitionConfigDialog.class);

	private final String title;
	private final List<String> charSet;
	
	Font fat = Fonts.createFont(Fonts.getSystemFontName(false, true, false), 30, SWT.NONE);
	
	public CharSetViewerDialog(Shell parent, final String title, List<String> charSet) {
		super(parent);
		this.title = title;
		this.charSet = charSet;
	}

	public void setVisible() {
		if (super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		GridLayout gl = new GridLayout(15, true);
		gl.horizontalSpacing = 15;
		gl.verticalSpacing = 15;
		cont.setLayout(gl);
		
		for(String c : charSet) {
			Label l = new Label(cont, SWT.BORDER);
			l.setText(c);
			l.setFont(fat);
			l.setToolTipText(Character.getName(c.codePointAt(0)));
		}
		cont.pack();
		return cont;
	}

	
	@Override
	protected void okPressed() {
		super.okPressed();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
		newShell.setMinimumSize(300, 200);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(640, 480);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
		// setBlockOnOpen(false);
	}
}
