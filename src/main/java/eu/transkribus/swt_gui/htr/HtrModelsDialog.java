package eu.transkribus.swt_gui.htr;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
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
	private final String providerFilter;

	/**
	 * The dialog can be fixated to only show HTRs of a specific provider, e.g. for selecting a base model for the training.
	 *  
	 * @param parentShell
	 * @param providerFilter fixates the HTR provider filter. Pass null to allow the use to filter by that.
	 */
	public HtrModelsDialog(Shell parentShell, final String providerFilter) {
		super(parentShell);
		this.providerFilter = providerFilter;
	}
	
	public HtrModelsDialog(Shell parentShell) {
		this(parentShell, null);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		cont.setLayout(new GridLayout(1, false));
	
		modelsComp = new HtrModelsComposite(cont, providerFilter, 0);
		modelsComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		modelsComp.htw.getTableViewer().getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if (modelsComp.getSelectedHtr()!=null) {
					okPressed();	
				}
			}
		});
		
		modelsComp.htw.getTableViewer().getTable().addKeyListener(new KeyAdapter() {			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR
                        || e.keyCode == SWT.KEYPAD_CR) {
					okPressed();
				}
			}
		});
		
		//this closes the dialog but this is not wanted
//		modelsComp.htw.getTableViewer().getTable().addMouseListener(new MouseAdapter() {
//			@Override
//			public void mouseDoubleClick(MouseEvent e) {
//				//okPressed();
//			}
//		});
		
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
		return new Point(1280, 900);
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
