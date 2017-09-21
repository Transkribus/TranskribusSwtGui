package eu.transkribus.swt_gui.htr;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpHtr;

public class HtrModelsDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(HtrModelsDialog.class);
	
	HtrModelsComposite modelsComp;
	TrpHtr selectedHtr;

	public HtrModelsDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		cont.setLayout(new GridLayout(1, false));
	
		modelsComp = new HtrModelsComposite(cont, 0);
		modelsComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		modelsComp.htw.getTableViewer().getTable().addKeyListener(new KeyAdapter() {			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR
                        || e.keyCode == SWT.KEYPAD_CR) {
					okPressed();
				}
			}
		});
		modelsComp.htw.getTableViewer().getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				okPressed();
			}
		});
		
		return cont;
	}
	
	@Override
	protected void okPressed() {
		selectedHtr = modelsComp.getSelectedHtr();
		super.okPressed();
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Choose a model");
		newShell.setMinimumSize(800, 600);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(1024, 768);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
		// setBlockOnOpen(false);
	}
	
	public TrpHtr getSelectedHtr() {
		return selectedHtr;
	}
	
}
