package eu.transkribus.swt_gui.models;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.ATrpModel;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class ModelFilterComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(ModelFilterComposite.class);

	public static interface ModelFilterChangedListener {
		public void onFilterChanged();
	}
	
	public static enum ModelFilter {
		COLLECTION, USER, PUBLIC, ALL
	}

	public Label label;
	public Button collBasedRadio, userBasedRadio, showPublicRadio, showAllRadio, reloadModelsBtn;

	Storage store = Storage.getInstance();
	List<ModelFilterChangedListener> listener = new ArrayList<>();
	
	public ModelFilterComposite(Composite parent) {
		super(parent, 0);
		
		this.setLayout(new GridLayout(6, false));
		this.setLayout(SWTUtil.createGridLayout(6, false, 0, 0));

		label = new Label(this, 0);
		label.setText("Model filter: ");
		Fonts.setBoldFont(label);

		collBasedRadio = new Button(this, SWT.RADIO);
		collBasedRadio.setText("Collection");
		collBasedRadio.setToolTipText("Show only models of the current colllection");
		collBasedRadio.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		collBasedRadio.setSelection(true);

		userBasedRadio = new Button(this, SWT.RADIO);
		userBasedRadio.setText("User");
		userBasedRadio.setToolTipText("Show only models that were trained by you");
		userBasedRadio.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));

		showPublicRadio = new Button(this, SWT.RADIO);
		showPublicRadio.setText("Public models");
		showPublicRadio.setToolTipText("Show only models that are publicly available");
		showPublicRadio.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));

		showAllRadio = new Button(this, SWT.RADIO);
		showAllRadio.setText("All");
		showPublicRadio.setToolTipText("Show all models (only for admins)");
		showAllRadio.setVisible(store.isAdminLoggedIn());
		showAllRadio.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
//			if (store.isAdminLoggedIn()) {
//				collBasedRadio.setSelection(false);
//				showAllRadio.setSelection(true);
//			}			

		reloadModelsBtn = new Button(this, SWT.PUSH);
		reloadModelsBtn.setImage(Images.REFRESH);
		reloadModelsBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		reloadModelsBtn.setToolTipText("Reload models according to filter");

		SWTUtil.onSelectionEvent(reloadModelsBtn, e -> onFilterChanged());
		SWTUtil.onSelectionEvent(collBasedRadio, e -> onFilterChanged());
		SWTUtil.onSelectionEvent(userBasedRadio, e -> onFilterChanged());
		SWTUtil.onSelectionEvent(showPublicRadio, e -> onFilterChanged());
		SWTUtil.onSelectionEvent(showAllRadio, e -> onFilterChanged());
	}
	
	public void setModelFilter(ModelFilter modelFilter) {
		if (modelFilter == null) {
			return;
		}
		switch (modelFilter) {
		case COLLECTION:
			collBasedRadio.setSelection(true);
			break;
		case USER:
			userBasedRadio.setSelection(true);
			break;
		case PUBLIC:
			showPublicRadio.setSelection(true);
			break;
		case ALL:
			showAllRadio.setSelection(true);
			break;			
		}
	}
	
	public ModelFilter getModelFilter() {
		if (collBasedRadio.getSelection()) {
			return ModelFilter.COLLECTION;
		}
		if (userBasedRadio.getSelection()) {
			return ModelFilter.USER;
		}
		if (showPublicRadio.getSelection()) {
			return ModelFilter.PUBLIC;
		}
		if (showAllRadio.getSelection()) {
			return ModelFilter.ALL;
		}
		
		return null;
	}

	public void addListener(ModelFilterChangedListener l) {
		this.listener.add(l);
	}

	public void onFilterChanged() {
		for (ModelFilterChangedListener l : listener) {
			l.onFilterChanged();
		}
	}

	public <T extends ATrpModel> List<T> loadModelsForCurrentFilter(Class<T> clazz) {
		boolean showAll = showAllRadio.getSelection();
		Integer colId = collBasedRadio.getSelection() ? store.getCollId() : null;
		Integer userId = userBasedRadio.getSelection() ? store.getUserId() : null;
		Integer releaseLevel = showPublicRadio.getSelection() ? 1 : null;
		try {
			return store.getConnection().getModelCalls().getModels(true, showAll, colId, userId, releaseLevel, clazz);
		} catch (Exception e1) {
			DialogUtil.showErrorMessageBox(getShell(), "Error loading models", e1.getMessage());
			return new ArrayList<>();
		}
	}
}