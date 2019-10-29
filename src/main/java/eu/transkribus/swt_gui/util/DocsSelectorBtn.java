package eu.transkribus.swt_gui.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.DocSelection;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.dialogs.DocumentsSelectorDialog;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class DocsSelectorBtn extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(DocsSelectorBtn.class);
	
	public static final String DEFAULT_BTN_LABEL = "Choose docs...";
	
	Label lbl;
	Button btn;
	
	List<TrpDocMetadata> checkedDocs = new ArrayList<>();
	List<DocSelection> docSelection = new ArrayList<>();
	
	public DocsSelectorBtn(Composite parent) {
		this(parent, null);
	}

	public DocsSelectorBtn(Composite parent, String label) {
		super(parent, 0);
		
		boolean hasLabel = !StringUtils.isEmpty(label);
		
		this.setLayout(SWTUtil.createGridLayout(hasLabel ? 2 : 1, false, 0, 0));
		
		if (hasLabel) {
			lbl = new Label(this, 0);
			lbl.setText(label);
		}
		
		btn = new Button(this, SWT.PUSH);
		btn.setText(DEFAULT_BTN_LABEL);
		btn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		SWTUtil.onSelectionEvent(btn, e -> {
			DocumentsSelectorDialog dsd = new DocumentsSelectorDialog(getShell(), "Select documents", 
					Storage.getInstance().getDocList(), true, docSelection);
			if (dsd.open() == IDialogConstants.OK_ID) {
				checkedDocs = dsd.getCheckedDocs();
				docSelection = dsd.getCheckedDocSelections();
				//System.out.println("n selected documents: "+dsd.getCheckedDocs().size());
			}
			
			updateLabel();
		});
		
//	    btn.setEnabled(false);		
	    updateLabel();
	}
	
	@Override
	public void setToolTipText(String tp) {
		if (lbl != null) {
			lbl.setToolTipText(tp);
		}
		btn.setToolTipText(tp);
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		SWTUtil.setEnabled(lbl, enabled);
		SWTUtil.setEnabled(btn, enabled);
	}
	
	private void updateLabel() {
		if (CoreUtils.isEmpty(docSelection)) {
			btn.setText(DEFAULT_BTN_LABEL);
		}
		else {
			int nDocs=docSelection.size(); 
			int nPages=0;
			try {
				for (int i=0; i<docSelection.size(); ++i) {
					nPages += docSelection.get(i).getNrOfSelectedPages(checkedDocs.get(i).getNrOfPages());
				}
				btn.setText(nDocs+(nDocs==1?"doc, ":" docs, ")+nPages+(nPages==1?" page":"pages")+" ...");
			} catch (Exception e) {
				logger.error("Could not determine nr of pages for all selected documents "+e.getMessage(), e);
				DialogUtil.showErrorMessageBox(getShell(), "Error", "Could not determine nr of pages for all selected documents "+e.getMessage());
				btn.setText(DEFAULT_BTN_LABEL);
			}
		}
	}
	
	public Button getBtn() {
		return btn;
	}
	
	public Label getLbl() {
		return lbl;
	}

	public List<TrpDocMetadata> getCheckedDocs() {
		return checkedDocs;
	}

	public List<DocSelection> getDocSelection() {
		return docSelection;
	}

}
