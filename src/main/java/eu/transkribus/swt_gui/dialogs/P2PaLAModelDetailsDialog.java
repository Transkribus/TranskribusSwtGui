package eu.transkribus.swt_gui.dialogs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.io.util.TrpProperties;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpP2PaLA;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.GsonUtil;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.TableLabelProvider;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.models.EditModelDialog;
import eu.transkribus.swt_gui.models.ModelFilterComposite;
import eu.transkribus.swt_gui.models.ModelFilterComposite.ModelFilter;
import eu.transkribus.swt_gui.models.ShareModelDialog;

public class P2PaLAModelDetailsDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(P2PaLAModelDetailsDialog.class);
	
	public static String ID_COL = "ID";
	public static String NAME_COL = "Name";
	public static String DESC_COL = "Description";
	public static String BASELINES_COL = "Baselines";
	public static String STRUCT_TYPES_COL = "Structure Types";
//	public static String IS_PUBLIC_COL = "Structure Types";
	public static String COLLECTIONS_COL = "Collections";
	
	public static String TRAIN_SET_SIZE_COL = "N-Train";
	public static String VAL_SET_SIZE_COL = "N-Validation";
	public static String TEST_SET_SIZE_COL = "N-Test";
	
	public static String USER_COL = "Owner";
	public static String MIN_ERROR_COL = "Min-Error";
	
	public static final ColumnConfig[] COLS = new ColumnConfig[] {
			new ColumnConfig(ID_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(NAME_COL, 120, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(DESC_COL, 250, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(BASELINES_COL, 75, false, DefaultTableColumnViewerSorter.ASC, "Does this model detect Baselines?"),
			new ColumnConfig(STRUCT_TYPES_COL, 150, false, DefaultTableColumnViewerSorter.ASC, "The region structure types this model detects"),
//			new ColumnConfig(COLLECTIONS_COL, 750, false, DefaultTableColumnViewerSorter.ASC, "The collections this models is part of"),
			
			new ColumnConfig(MIN_ERROR_COL, 75, false, DefaultTableColumnViewerSorter.ASC, "The minimum error rate this model has achieved"),
			new ColumnConfig(TRAIN_SET_SIZE_COL, 60, false, DefaultTableColumnViewerSorter.ASC, "The size of the training set"),
			new ColumnConfig(VAL_SET_SIZE_COL, 60, false, DefaultTableColumnViewerSorter.ASC, "The size of the validation set (which is used after every epoch during training to evaluate the model)"),
//			new ColumnConfig(TEST_SET_SIZE_COL, 75, false, DefaultTableColumnViewerSorter.ASC, "The size of the test set (which is used once after training to evaluate the model)"),
			new ColumnConfig(USER_COL, 120, false, DefaultTableColumnViewerSorter.ASC, "The user that has trained this model"),
		};
	
	MyTableViewer modelsTable;
	ModelFilterComposite modelFilterComp;
	
	Button shareSelectedModelBtn, removeModelFromThisCollBtn;
	Button shareModelBtn, editModelBtn;
	
	List<TrpP2PaLA> models;
	ModelFilter modelFilter;
	Storage store;
	Map<Integer, String> modelCollections = new HashMap<>(); // not used currently!!
	
	MenuItem shareItem, editItem, delItem;
	Menu menu;

	public P2PaLAModelDetailsDialog(Shell parentShell, List<TrpP2PaLA> models, ModelFilter modelFilter) {
		super(parentShell);
		
		this.store = Storage.getInstance();
		this.models = models;
		this.modelFilter = modelFilter;
//		this.models.forEach(m -> updateCollectionsForModel(m)); // disabled for now...
	}
	
	private Point getPreferredSize() {
		Point preferredSize = new Point(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT).x, getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		return new Point(preferredSize.x, Math.min(preferredSize.y, 750));
	}
	
	@Override
	protected Point getInitialSize() {
//		return new Point(1000, 1000);
//		return new Point(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT).x, getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		return getPreferredSize();
	}
	
	@Override
	protected boolean isResizable() {
	    return true;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button helpBtn = createButton(parent, IDialogConstants.HELP_ID, "Help", false);
		helpBtn.setImage(Images.HELP);
		SWTUtil.onSelectionEvent(helpBtn, e -> {
			org.eclipse.swt.program.Program.launch("https://transkribus.eu/wiki/index.php/P2PaLA");
		});		
		
		createButton(parent, IDialogConstants.OK_ID, "OK", true);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("P2PaLA model details");
		SWTUtil.centerShell(newShell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		cont.setLayout(new GridLayout(2, false));
		
		modelsTable = new MyTableViewer(cont, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		modelsTable.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		modelsTable.getTable().setHeaderVisible(true);
		modelsTable.getTable().setLinesVisible(true);
		modelsTable.addColumns(COLS);
		// ignore case when sorting columns:
//		for (int i=0; i<tv.getNColumns(); ++i) {
//			tv.getSorter(i).setIgnoreCase(true);
//		}
		modelsTable.setContentProvider(ArrayContentProvider.getInstance());
		modelsTable.setLabelProvider(new TableLabelProvider() {
			@Override
			public String getColumnText(Object element, int index) {
				if (!(element instanceof TrpP2PaLA)) {
					return "i am serious error";
				}
				TrpP2PaLA m = (TrpP2PaLA) element;
				String cn = COLS[index].name;
				TrpProperties customProps = m.parseCustomProperties();
				List<Integer> dataSetSizes = null;
				try {
					dataSetSizes = GsonUtil.toIntegerList(customProps.getString("dataSetSizes"));
				} catch (Exception e) {
				}
				String structInfo = customProps.getString("structInfo");
				
				if (cn.equals(ID_COL)) {
					return m.getModelId()+"";
				}
				else if (cn.equals(NAME_COL)) {
					return m.getName();
				}
				else if (cn.equals(DESC_COL)) {
					return m.getDescription();
				}
				else if (cn.equals(BASELINES_COL)) {
					return StringUtils.contains(m.getOutMode(), "L") ? "Yes" : "No";
				}
				else if (cn.equals(STRUCT_TYPES_COL)) {
					return structInfo!=null ? structInfo : (m.getStructTypes() != null ? m.getStructTypes() : "");
//					return StringUtils.contains(m.getOut_mode(), "R") ? m.getStruct_types() : "";
//					return m.getStructTypes();
				}
				else if (cn.equals(COLLECTIONS_COL)) {
					String collsStr = modelCollections.get(m.getModelId());
					return collsStr == null ? "" : collsStr;
				}
				else if (cn.equals(USER_COL)) {
					return m.getUserName()==null ? "unknown" : m.getUserName();
				}
				// TODO
				else if (cn.equals(TRAIN_SET_SIZE_COL)) {
//					return m.getTrain_set_size()!=null ? ""+m.getTrain_set_size() : "NA";
					return dataSetSizes != null ? (""+dataSetSizes.get(0)) : "";
//					return "NA";
				}
				else if (cn.equals(VAL_SET_SIZE_COL)) {
//					return m.getVal_set_size()!=null ? ""+m.getVal_set_size() : "NA";
					return dataSetSizes != null ? (""+dataSetSizes.get(1)) : "";
//					return "NA";
				}
				else if (cn.equals(TEST_SET_SIZE_COL)) {
//					return m.getTest_set_size()!=null ? ""+m.getTest_set_size() : "NA";
					return dataSetSizes != null ? (""+dataSetSizes.get(2)) : "";
//					return "NA";
				}
				else if (cn.equals(MIN_ERROR_COL)) {
					return m.getMinError()==null ? "" : (""+m.getMinError());
				}
				
				return "i am error";
			}
		});
		modelsTable.setInput(models);

		createMenu();
		
		modelFilterComp = new ModelFilterComposite(cont);
		modelFilterComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		modelFilterComp.addListener(() -> reloadModels());
		modelFilterComp.setModelFilter(modelFilter);
		
		if (false) {
		shareSelectedModelBtn = new Button(cont, 0);
		shareSelectedModelBtn.setText("Share selected model...");
		SWTUtil.onSelectionEvent(shareSelectedModelBtn, e -> {
			addSelectedModelToCollection();
		});
		
		removeModelFromThisCollBtn = new Button(cont, 0);
		removeModelFromThisCollBtn.setText("Remove seleced model from current collection");
		SWTUtil.onSelectionEvent(removeModelFromThisCollBtn, e -> {
			removeSelectedModelFromCollection();
		});
		}
		shareModelBtn = new Button(cont, 0);
		shareModelBtn.setText("Share model...");
		shareModelBtn.setImage(Images.GROUP);
		SWTUtil.onSelectionEvent(shareModelBtn, e -> openShareModelDialog());
		
		editModelBtn = new Button(cont, 0);
		editModelBtn.setText("Edit model...");
		editModelBtn.setImage(Images.PENCIL);
		SWTUtil.onSelectionEvent(editModelBtn, e -> editSelectedModel());
		
		return cont;
	}
	
	private void createMenu() {
		menu = new Menu(modelsTable.getTable());
		
		shareItem = new MenuItem(menu, SWT.NONE);
		shareItem.setText("Share model...");
		shareItem.setImage(Images.GROUP);
		SWTUtil.onSelectionEvent(shareItem, e -> {
			openShareModelDialog();
		});
		
		editItem = new MenuItem(menu, 0);
		editItem.setText("Edit model...");
		editItem.setImage(Images.PENCIL);
		SWTUtil.onSelectionEvent(editItem, ev -> editSelectedModel());
		
		if (store.isAdminLoggedIn()) { // for now, restrict deleting models to admins...
			delItem = new MenuItem(menu, 0);
			delItem.setText("Delete model...");
			delItem.setImage(Images.DELETE);
			SWTUtil.onSelectionEvent(delItem, ev -> deleteSelectedModel());			
		}
		
		menu.addMenuListener(new MenuListener() {
			@Override
			public void menuShown(MenuEvent e) {
				TrpP2PaLA m = getSelectedModel();
				if (m==null) {
					return;
				}
				
				boolean canEditModel = store.isAdminLoggedIn() || CoreUtils.compareTo(m.getUserId(), store.getUserId())==0;
				editItem.setEnabled(canEditModel);
				SWTUtil.setEnabled(editModelBtn, canEditModel);
				
				boolean canDeleteModel = store.isAdminLoggedIn() || CoreUtils.compareTo(m.getUserId(), store.getUserId())==0;
				SWTUtil.setEnabled(delItem, canDeleteModel);
			}
			
			@Override
			public void menuHidden(MenuEvent e) {
			}
		});
		
		modelsTable.getTable().setMenu(menu);		
	}
	
	public void reloadModels() {
		this.models = modelFilterComp.loadModelsForCurrentFilter(TrpP2PaLA.class);
		setModels(this.models);
		
		getShell().setSize(getPreferredSize());
		SWTUtil.centerShell(getShell());
	}
	
	private void setModels(List<TrpP2PaLA> models) {
		this.models = models;
		modelsTable.setInput(models);
		modelCollections.clear();
		this.models.forEach(m -> updateCollectionsForModel(m));		
	}

	public TrpP2PaLA getSelectedModel() {
		return (TrpP2PaLA) modelsTable.getStructuredSelection().getFirstElement();
	}
	
	private void openShareModelDialog() {
		if (getSelectedModel()==null) {
			return;
		}
		
		ShareModelDialog d = new ShareModelDialog(getShell(), getSelectedModel());
		d.open();
	}
	
	private void editSelectedModel() {
		TrpP2PaLA model = getSelectedModel();
		logger.debug("editing selected model, selected = "+model);
		if (model==null) {
			return;
		}
		
		EditModelDialog d = new EditModelDialog(getShell(), model);
		if (d.open() == IDialogConstants.OK_ID) {
			modelsTable.refresh(d.getModel());
		}
	}
	
	private void deleteSelectedModel() {
		TrpP2PaLA model = getSelectedModel();
		if (model==null) {
			return;
		}	
		
		if (DialogUtil.showYesNoDialog(getShell(), "Confirm deletion", "Do you really want to delete model "+model.getModelId()+"/"+model.getName()+"?") != SWT.YES) {
			return;
		}
		
		logger.debug("deleting model: "+model);
		
		try {
			store.getConnection().getModelCalls().setModelDeleted(model.getModelId());
			reloadModels();
		} catch (Exception e) {
			logger.debug("Could not add model to collection: "+e.getMessage(), e);
			String errorMsg = e.getMessage();
			DialogUtil.showErrorMessageBox(getShell(), "Error deleting model", errorMsg);
		}
	}
	
	private void addSelectedModelToCollection() {
		TrpP2PaLA model = getSelectedModel();
		if (model==null) {
			return;
		}		
		
		ChooseCollectionDialog ccd = new ChooseCollectionDialog(getShell());
		
		@SuppressWarnings("unused")
		int ret = ccd.open();
		TrpCollection col = ccd.getSelectedCollection();
		if (col==null) {
			return;
		}

		// because admin can see all models and if he then wants to share it to the collection he is actually in it is forbidden
//		if (store.getCollId() == col.getColId() && !store.getUser().isAdmin()) {
//			DialogUtil.showInfoMessageBox(getShell(), "Info", "The selected model is already included in this collection.");
//			return;
//		}
		
		try {
			store.getConnection().getModelCalls().addOrRemoveModelFromCollection(model.getModelId(), col.getColId(), false);
			updateCollectionsForModel(model);
			modelsTable.refresh(model);
		} catch (Exception e) {
			logger.debug("Could not add model to collection: "+e.getMessage(), e);
			String errorMsg = e.getMessage();
			DialogUtil.showErrorMessageBox(getShell(), "Error sharing model", errorMsg);
		}
//		DialogUtil.showInfoMessageBox(getShell(), "Success", "The HTR was added to the selected collection.");
//		super.widgetSelected(e);
	}
	
	private void removeSelectedModelFromCollection() {
		TrpP2PaLA model = getSelectedModel();
		if (model==null) {
			return;
		}
		
		try {
			store.getConnection().getModelCalls().addOrRemoveModelFromCollection(model.getModelId(), store.getCollId(), true);
			updateCollectionsForModel(model);
			modelsTable.refresh(model);
		} catch (Exception e) {
			logger.debug("Could not remove model from collection: "+e.getMessage(), e);
			DialogUtil.showErrorMessageBox(getShell(), "Error removing model from collection", e.getMessage());
		}
//		super.widgetSelected(e);
	}
	
	private void updateCollectionsForModel(TrpP2PaLA model) {
		if (true) { // disabled due to inefficiency of querying collections for all models...
			return;
		}
		
		if (model != null) {
			try {
				List<TrpCollection> colls = store.getConnection().getModelCalls().getModelCollections(model.getModelId());
				logger.trace("loaded n-colls = "+colls.size());
				String collsSummary = colls.stream().map(c -> "("+c.getColId()+","+c.getColName()+")").reduce((t, u) ->  t+ ", " + u).orElse("");
				modelCollections.put(model.getModelId(), collsSummary);
			} catch (TrpServerErrorException | TrpClientErrorException | SessionExpiredException e) {
				logger.error("Error updating collections for model: "+e.getMessage());
				modelCollections.put(model.getModelId(), "Error: "+e.getMessage());
			}
		}
	}
}
