package eu.transkribus.swt_gui.htr;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.util.TextRecognitionConfig;
import eu.transkribus.util.TextRecognitionConfig.Mode;

public class HtrTextRecognitionConfigDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(HtrTextRecognitionConfigDialog.class);

	private HtrDictionaryComposite htrDictComp;
	private HtrModelsComposite htrModelsComp;

//	private TrpHtr htr;
	private TextRecognitionConfig config;

	public HtrTextRecognitionConfigDialog(Shell parent, TextRecognitionConfig config) {
		super(parent);
		this.config = config;
	}

	public void setVisible() {
		if (super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		
		SashForm sash = new SashForm(cont, SWT.HORIZONTAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sash.setLayout(new GridLayout(2, false));
		
		htrModelsComp = new HtrModelsComposite(sash, 0);
		htrModelsComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Group dictGrp = new Group(sash, SWT.NONE);
		dictGrp.setLayout(new GridLayout(1, false));
		dictGrp.setText("Dictionary");
		
		htrDictComp = new HtrDictionaryComposite(dictGrp, 0);
		htrDictComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		applyConfig();

		sash.setWeights(new int[] { 88, 12 });
		
		htrModelsComp.htw.getTableViewer().getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				okPressed();
			}
		});

		return cont;
	}

	private void applyConfig() {
		if (config == null) {
			return;
		}
		
		Mode mode = config.getMode();
		switch (mode) {
		case CITlab:
			htrModelsComp.setSelection(config.getHtrId());
			htrDictComp.selectDictionary(config.getDictionary());
			break;
		case UPVLC:
			break;
		default:
			break;
		}
	}

	public TextRecognitionConfig getConfig() {
		return config;
	}

	@Override
	protected void okPressed() {
		config = new TextRecognitionConfig(Mode.CITlab);
		config.setDictionary(htrDictComp.getSelectedDictionary());
		TrpHtr htr = htrModelsComp.getSelectedHtr();
		if (htr == null) {
			DialogUtil.showErrorMessageBox(this.getParentShell(), "Error", "Please select a HTR.");
			return;
		}
		config.setHtrId(htr.getHtrId());
		config.setHtrName(htr.getName());
		config.setLanguage(htr.getLanguage());
		super.okPressed();
	}
	
	//This was used when the HTR Composite had tabs. remove by occasion.
//	@Override
//	protected void okPressed() {
//
//		if (htrModelsComp.isCitlabHtrTabSelected()) {
//			config = new TextRecognitionConfig(Mode.CITlab);
//
//			config.setDictionary(htrDictComp.getSelectedDictionary());
//			TrpHtr htr = htrModelsComp.getSelectedHtr();
//			if (htr == null) {
//				DialogUtil.showErrorMessageBox(this.getParentShell(), "Error", "Please select a HTR.");
//				return;
//			}
//			config.setHtrId(htr.getHtrId());
//			config.setHtrName(htr.getName());
//			config.setLanguage(htr.getLanguage());
//		} else {
//			DialogUtil.showErrorMessageBox(this.getParentShell(), "Error", "Bad configuration!");
//		}
//		super.okPressed();
//	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Text Recognition Configuration");
		newShell.setMinimumSize(800, 600);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(1280, 768);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
		// setBlockOnOpen(false);
	}
}
