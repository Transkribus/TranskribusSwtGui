package eu.transkribus.swt_gui.models;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.model.beans.ATrpModel;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.collection_comboviewer.CollectionSelectorDialog;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class ShareModelDialog extends Dialog  {
	private static final Logger logger = LoggerFactory.getLogger(ShareModelDialog.class);
	
	private ATrpModel model;
	Label collsInfoLbl;
	Button addBtn, removeBtn, reloadBtn;
	
	Storage store;

	public ShareModelDialog(Shell parentShell, ATrpModel model) {
		super(parentShell);
		this.model = model;
		store = Storage.i();
	}
	
	@Override protected void configureShell(Shell shell) {
	      super.configureShell(shell);
	      shell.setText("Share model '"+model.getName()+"'");
	      SWTUtil.centerShell(shell);
	}
	
	@Override protected boolean isResizable() {
	    return true;
	}
	
	@Override
	protected Point getInitialSize() {
		Point s = SWTUtil.getPreferredSize(getShell());
		return new Point(s.x+200, s.y+50);
	}	
	
	@Override protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
//		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	@Override protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		cont.setLayout(new GridLayout(3, false));
				
		Label l = new Label(cont, 0);
		l.setText("Collections: ");
		Fonts.setBoldFont(l);
		l.setLayoutData(new GridData(GridData.BEGINNING));
		
		collsInfoLbl = new Label(cont, 0);
		collsInfoLbl.setText("");
//		collsInfoLbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		collsInfoLbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		addBtn = new Button(cont, 0);
		addBtn.setText("Add to collection...");
		addBtn.setToolTipText("Add this model to a new collection");
		addBtn.setImage(Images.ADD);
		addBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		SWTUtil.onSelectionEvent(addBtn, e -> addSelectedModelToCollection());
		
		removeBtn = new Button(cont, 0);
		removeBtn.setText("Remove from current collection");
		removeBtn.setToolTipText("Remove this model from the current collection");
		removeBtn.setImage(Images.DELETE);
		removeBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		SWTUtil.onSelectionEvent(removeBtn, e -> removeSelectedModelFromCollection());
		
		reloadBtn = new Button(cont, 0);
		reloadBtn.setToolTipText("Reload collections for this model");
		reloadBtn.setImage(Images.REFRESH);
		reloadBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		SWTUtil.onSelectionEvent(reloadBtn, e -> updateGui());
		
		updateGui();
				
		return cont;
	}
	
	private void addSelectedModelToCollection() {
		if (model==null) {
			return;
		}		
		
		Predicate<TrpCollection> collSelectorPredicate = null;
//		Predicate<TrpCollection> collSelectorPredicate = (c) -> { return c!=null && AuthUtils.canManage(c.getRole()); }; // show only collections users can manage
		CollectionSelectorDialog d = new CollectionSelectorDialog(getShell(), collSelectorPredicate, null);
		if (d.open() != Dialog.OK) {
			return;
		}
					
		TrpCollection c = d.getSelectedCollection();
		logger.debug("selected collection: "+c);
		if (c == null) {
			return;
		}
		
		try {
			store.getConnection().getModelCalls().addOrRemoveModelFromCollection(model.getModelId(), c.getColId(), false);
			updateGui();
		} catch (Exception e) {
			logger.debug("Could not add model to collection: "+e.getMessage(), e);
			String errorMsg = e.getMessage();
			DialogUtil.showErrorMessageBox(getShell(), "Error sharing model", errorMsg);
		}
	}
	
	private void removeSelectedModelFromCollection() {
		if (model==null) {
			return;
		}
		
		try {
			store.getConnection().getModelCalls().addOrRemoveModelFromCollection(model.getModelId(), store.getCollId(), true);
			updateGui();
		} catch (Exception e) {
			logger.debug("Could not remove model from collection: "+e.getMessage(), e);
			DialogUtil.showErrorMessageBox(getShell(), "Error removing model from collection", e.getMessage());
		}
	}
	
	private void updateGui() {
		try {
			List<TrpCollection> colls = store.getConnection().getModelCalls().getModelCollections(model.getModelId());
			logger.debug("loaded n-colls = "+colls.size());
//			String collsSummary = colls.stream().map(c -> "("+c.getColId()+","+c.getColName()+")").reduce((t, u) ->  t+ ", " + u).orElse("");
			String collsSummary = (colls.isEmpty() ? "<none>" : colls.stream().map(c -> c.getColName()+" ("+c.getColId()+")").reduce((t, u) ->  t+ ", " + u).orElse(""));
			collsInfoLbl.setText(collsSummary);
		} catch (Exception e) {
			logger.error("Error updating collections for model: "+e.getMessage());
//			DialogUtil.showErrorMessageBox(getShell(), "Error updating collectios for model", e.getMessage());
			DialogUtil.showErrorBalloonToolTip(collsInfoLbl, "Error updating collectios for model", e.getMessage());
			collsInfoLbl.setText("<error>");
		}
		
//		SWTUtil.resizeFromPreferredSize(getShell(), 100, 10);
	}
	
	private void storeResults() {
		// nothing to do here I guess...
	}
	
	@Override protected void okPressed() {
		storeResults();
		
		super.okPressed();
	}

}
