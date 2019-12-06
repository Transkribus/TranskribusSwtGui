package eu.transkribus.swt_gui.htr;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
import eu.transkribus.core.util.HtrCITlabUtils;
import eu.transkribus.core.util.HtrPyLaiaUtils;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.util.TextRecognitionConfig;
import eu.transkribus.util.TextRecognitionConfig.Mode;

public class HtrTextRecognitionConfigDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(HtrTextRecognitionConfigDialog.class);

	private HtrDictionaryComposite htrDictComp;
	private HtrModelsComposite htrModelsComp;

	private TextRecognitionConfig config;
	
	Group dictGrp;
	SashForm sash;

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
		
		sash = new SashForm(cont, SWT.HORIZONTAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sash.setLayout(new GridLayout(2, false));
		
		htrModelsComp = new HtrModelsComposite(sash, 0);
		GridLayout gl = (GridLayout) htrModelsComp.getLayout();
		gl.marginHeight = gl.marginWidth = 0;
		htrModelsComp.setLayout(gl);
		
		htrModelsComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		htrModelsComp.htw.htrTv.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				updateUi();
			}
		});
		
		Group dictGrp = new Group(sash, SWT.NONE);
		dictGrp.setLayout(new GridLayout(1, false));
		dictGrp.setText("Dictionary");
		
		htrDictComp = new HtrDictionaryComposite(dictGrp, 0);
		htrDictComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		applyConfig();

		sash.setWeights(new int[] { 88, 12 });
		
		htrModelsComp.htw.getTableViewer().getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				okPressed();
			}
		});
		
		updateUi();

		return cont;
	}
	
	private void updateUi() {
		if(htrModelsComp.getSelectedHtr() == null) {
			return;
		}
		final String provider = htrModelsComp.getSelectedHtr().getProvider();
		if (!HtrPyLaiaUtils.doesDecodingSupportDicts() 
				&& provider.equals(HtrPyLaiaUtils.PROVIDER_PYLAIA)) {
			sash.setWeights(new int[] { 100, 0 });
		} else if (provider.equals(HtrCITlabUtils.PROVIDER_CITLAB_PLUS)
				|| provider.equals(HtrCITlabUtils.PROVIDER_CITLAB)) {
			//show option to select integrated dictionary if available for this model
			htrDictComp.updateDictionaries(false, htrModelsComp.getSelectedHtr().isLanguageModelExists());
			sash.setWeights(new int[] { 88, 12 });
		} else {
			sash.setWeights(new int[] { 88, 12 });
		}
	}

	private void applyConfig() {
		if (config == null) {
			return;
		}
		
		Mode mode = config.getMode();
		switch (mode) {
		case CITlab:
			htrModelsComp.setSelection(config.getHtrId());
			htrDictComp.updateDictionaries(false, htrModelsComp.getSelectedHtr().isLanguageModelExists());
			htrDictComp.selectDictionary(config.getDictionary());
			break;
		case UPVLC:
			htrModelsComp.setSelection(config.getHtrId());
			htrDictComp.selectDictionary(config.getDictionary());
			break;
		default:
			break;
		}
	}

	public TextRecognitionConfig getConfig() {
		return config;
	}
	
	private Mode getModeForProvider(String provider) {
		logger.debug("provider = "+provider);
				
		if (HtrCITlabUtils.PROVIDER_CITLAB.equals(provider) || HtrCITlabUtils.PROVIDER_CITLAB_PLUS.equals(provider)) {
			return Mode.CITlab;
		}
		if (HtrPyLaiaUtils.PROVIDER_PYLAIA.equals(provider)) {
			return Mode.UPVLC;
		}
		
		return null;
	}

	@Override
	protected void okPressed() {
		htrModelsComp.hdw.checkForUnsavedChanges();
		TrpHtr htr = htrModelsComp.getSelectedHtr();
		
		Mode mode = getModeForProvider(htr.getProvider());
		if (mode == null) {
			DialogUtil.showErrorMessageBox(getShell(), "Error parsing mode from provider", "Unknown model provider: "+htr.getProvider());
			return;
		}
		config = new TextRecognitionConfig(mode);
		config.setDictionary(htrDictComp.getSelectedDictionary());
		
		if (htr == null) {
			DialogUtil.showErrorMessageBox(this.getParentShell(), "Error", "Please select a HTR.");
			return;
		}
		config.setHtrId(htr.getHtrId());
		config.setHtrName(htr.getName());
		config.setLanguage(htr.getLanguage());
		super.okPressed();
	}
	
	@Override
	protected void cancelPressed() {
		htrModelsComp.hdw.checkForUnsavedChanges();
		super.cancelPressed();
	}

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
