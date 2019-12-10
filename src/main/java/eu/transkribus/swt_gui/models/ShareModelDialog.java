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
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.model.beans.ATrpModel;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.collection_comboviewer.CollectionSelectorDialog;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.metadata.CollectionsTableWidget;

public class ShareModelDialog extends Dialog  {
	private static final Logger logger = LoggerFactory.getLogger(ShareModelDialog.class);

	private final int modelId;
	private final String modelName;
	
	private CollectionsTableWidget collectionsTableWidget;

	Button addBtn, removeBtn, reloadBtn;

	protected Storage store;

	public ShareModelDialog(Shell parentShell,ATrpModel model) {
		this(parentShell, model.getModelId(), model.getName());
	}

	public ShareModelDialog(Shell parentShell, int modelId, String modelName) {
		super(parentShell);
		this.modelId = modelId;
		this.modelName = modelName;
		store = Storage.i();
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Share model '" + modelName + "'");
		SWTUtil.centerShell(shell);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Point getInitialSize() {
		//FIXME getPreferredSize returns bad values on wide screens, e.g. multi-monitor setups
//		Point s = SWTUtil.getPreferredSize(getShell());
//		return new Point(s.x + 200, s.y + 50);
		return new Point(400, 400);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		cont.setLayout(new GridLayout(3, false));

		collectionsTableWidget = new CollectionsTableWidget(cont, SWT.BORDER);
		collectionsTableWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));

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

		Predicate<TrpCollection> collSelectorPredicate = null;
//		Predicate<TrpCollection> collSelectorPredicate = (c) -> { return c!=null && AuthUtils.canManage(c.getRole()); }; // show only collections users can manage
		CollectionSelectorDialog d = new CollectionSelectorDialog(getShell(), collSelectorPredicate, null);
		if (d.open() != Dialog.OK) {
			return;
		}

		TrpCollection c = d.getSelectedCollection();
		logger.debug("selected collection: " + c);
		if (c == null) {
			return;
		}

		try {
			addModelToCollection(c.getColId());
			updateGui();
		} catch (TrpClientErrorException | TrpServerErrorException e) {
			logger.error("Could not add model to collection: " + e.getMessage(), e);
			String errorMsg = e.getMessageToUser();
			DialogUtil.showErrorMessageBox(getShell(), "Error sharing model", errorMsg);
		} catch (Exception e) {
			logger.error("Could not add model to collection: " + e.getMessage(), e);
			String errorMsg = e.getMessage();
			DialogUtil.showErrorMessageBox(getShell(), "Error sharing model", errorMsg);
		}
	}

	private void removeSelectedModelFromCollection() {
		TrpCollection selection = collectionsTableWidget.getSelection();
		if (selection == null) {
			logger.debug("No selection. Doing nothing...");
			return;
		} else {
			logger.debug("Removing HTR from collection: {}", selection.getColName());
		}

		try {
			removeModelFromCollection(collectionsTableWidget.getSelection().getColId());
			updateGui();
		} catch (TrpClientErrorException | TrpServerErrorException e) {
			logger.error("Could not remove model from collection: " + e.getMessage(), e);
			String errorMsg = e.getMessageToUser();
			DialogUtil.showErrorMessageBox(getShell(), "Error removing model from collection", errorMsg);
		} catch (Exception e) {
			logger.error("Could not remove model from collection: " + e.getMessage(), e);
			DialogUtil.showErrorMessageBox(getShell(), "Error removing model from collection", e.getMessage());
		}
	}

	private void updateGui() {
		try {
			List<TrpCollection> colls = getCollectionList();
			logger.debug("loaded n-colls = " + colls.size());
			collectionsTableWidget.refreshList(colls);
		} catch (TrpClientErrorException | TrpServerErrorException e) {
			logger.error("Error updating collections for model: " + e.getMessage());
			String errorMsg = e.getMessageToUser();
			DialogUtil.showErrorBalloonToolTip(collectionsTableWidget, "Error updating collections for model", errorMsg);
		} catch (Exception e) {
			logger.error("Error updating collections for model: " + e.getMessage());
			DialogUtil.showErrorBalloonToolTip(collectionsTableWidget, "Error updating collections for model",
					e.getMessage());
		}
	}

	private void storeResults() {
		// nothing to do here I guess...
	}

	@Override
	protected void okPressed() {
		storeResults();

		super.okPressed();
	}

	public void setVisible() {
		if (super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
	}
	
	protected int getModelId() {
		return modelId;
	}
	
	protected String getModelName() {
		return modelName;
	}

	protected void addModelToCollection(int colId)
			throws TrpClientErrorException, TrpServerErrorException, SessionExpiredException {
		store.getConnection().getModelCalls().addOrRemoveModelFromCollection(getModelId(), colId, false);
	}

	protected void removeModelFromCollection(int colId)
			throws TrpClientErrorException, TrpServerErrorException, SessionExpiredException {
		store.getConnection().getModelCalls().addOrRemoveModelFromCollection(getModelId(), colId, true);
	}

	protected List<TrpCollection> getCollectionList()
			throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		return store.getConnection().getModelCalls().getModelCollections(getModelId());
	}
}
