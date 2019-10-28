package eu.transkribus.swt_gui.dialogs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.SWT;
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
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpP2PaLA;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.TableLabelProvider;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class P2PaLAModelDetailsDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(P2PaLAModelDetailsDialog.class);
	
	MyTableViewer modelsTable;
	
	public static String NAME_COL = "Name";
	public static String DESC_COL = "Description";
	public static String BASELINES_COL = "Baselines";
	public static String STRUCT_TYPES_COL = "Structure Types";
//	public static String IS_PUBLIC_COL = "Structure Types";
	public static String COLLECTIONS_COL = "Collections";
	
	public static String TRAIN_SET_SIZE_COL = "N-Train";
	public static String VAL_SET_SIZE_COL = "N-Validation";
	public static String TEST_SET_SIZE_COL = "N-Test";
	
	public static final ColumnConfig[] COLS = new ColumnConfig[] {
			new ColumnConfig(NAME_COL, 120, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(DESC_COL, 250, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(BASELINES_COL, 75, false, DefaultTableColumnViewerSorter.ASC, "Does this model detect Baselines?"),
			new ColumnConfig(STRUCT_TYPES_COL, 150, false, DefaultTableColumnViewerSorter.ASC, "The region structure types this model detects"),
			new ColumnConfig(COLLECTIONS_COL, 750, false, DefaultTableColumnViewerSorter.ASC, "The collections this models is part of"),
			
//			new ColumnConfig(TRAIN_SET_SIZE_COL, 75, false, DefaultTableColumnViewerSorter.ASC, "The size of the training set"),
//			new ColumnConfig(VAL_SET_SIZE_COL, 75, false, DefaultTableColumnViewerSorter.ASC, "The size of the validation set (which is used after every epoch during training to evaluate the model)"),
//			new ColumnConfig(TEST_SET_SIZE_COL, 75, false, DefaultTableColumnViewerSorter.ASC, "The size of the test set (which is used once after training to evaluate the model)"),
		};
	
	List<TrpP2PaLA> models;
	Storage store;
	Map<Integer, String> modelCollections = new HashMap<>();

	public P2PaLAModelDetailsDialog(Shell parentShell, List<TrpP2PaLA> models) {
		super(parentShell);
		this.models = models;
		this.store = Storage.getInstance();
		
		this.models.forEach(m -> updateCollectionsForModel(m));
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(1000, 800);
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
				
				if (cn.equals(NAME_COL)) {
					return m.getName();
				}
				else if (cn.equals(DESC_COL)) {
					return m.getDescription();
				}
				else if (cn.equals(BASELINES_COL)) {
					return StringUtils.contains(m.getOutMode(), "L") ? "Yes" : "No";
				}
				else if (cn.equals(STRUCT_TYPES_COL)) {
//					return StringUtils.contains(m.getOut_mode(), "R") ? m.getStruct_types() : "";
					return m.getStructTypes();
				}
				else if (cn.equals(COLLECTIONS_COL)) {
					String collsStr = modelCollections.get(m.getModelId());
					return collsStr == null ? "" : collsStr;
				}
				// TODO
//				else if (cn.equals(TRAIN_SET_SIZE_COL)) {
//					return m.getTrain_set_size()!=null ? ""+m.getTrain_set_size() : "NA";
//				}
//				else if (cn.equals(VAL_SET_SIZE_COL)) {
//					return m.getVal_set_size()!=null ? ""+m.getVal_set_size() : "NA";
//				}
//				else if (cn.equals(TEST_SET_SIZE_COL)) {
//					return m.getTest_set_size()!=null ? ""+m.getTest_set_size() : "NA";
//				}				
				
				return "i am error";
			}
		});
		modelsTable.setInput(models);
		
		Menu menu = new Menu(modelsTable.getTable());
		modelsTable.getTable().setMenu(menu);

		MenuItem shareItem = new MenuItem(menu, SWT.NONE);
		shareItem.setText("Share model...");
		SWTUtil.onSelectionEvent(shareItem, e -> {
			addSelectedModelToCollection();
		});

		MenuItem delItem = new MenuItem(menu, SWT.NONE);
		delItem.setText("Remove model from current collection");
		SWTUtil.onSelectionEvent(delItem, e -> {
			removeSelectedModelFromCollection();
		});		
		
		
//		modelsTable.getTable().addMenuDetectListener(new MenuDetectListener() {
//			@Override
//			public void menuDetected(MenuDetectEvent e) {
//				TrpP2PaLA model = (TrpP2PaLA) modelsTable.getStructuredSelection().getFirstElement();
//
//				int index = table.getSelectionIndex();
//				if (index == -1)
//					return; // no row selected
//
//				TableItem item = table.getItem(index);
//				item.getData(); // use this to identify which row was clicked.
//				// The popup can now be displayed as usual using table.toDisplay(e.x, e.y)
//			}
//		});
		
		return cont;
	}

	public TrpP2PaLA getSelectedModel() {
		return (TrpP2PaLA) modelsTable.getStructuredSelection().getFirstElement();
	}
	
	private void addSelectedModelToCollection() {
		ChooseCollectionDialog ccd = new ChooseCollectionDialog(getShell());
		
		@SuppressWarnings("unused")
		int ret = ccd.open();
		TrpCollection col = ccd.getSelectedCollection();
		if (col==null) {
			return;
		}
		TrpP2PaLA model = getSelectedModel();
		if (model==null) {
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
		if (model != null) {
			try {
				List<TrpCollection> colls = store.getConnection().getModelCalls().getModelCollections(model.getModelId());
				logger.debug("loaeded n-colls = "+colls.size());
				String collsSummary = colls.stream().map(c -> "("+c.getColId()+","+c.getColName()+")").reduce((t, u) ->  t+ ", " + u).orElse("");
				modelCollections.put(model.getModelId(), collsSummary);
			} catch (TrpServerErrorException | TrpClientErrorException | SessionExpiredException e) {
				logger.error("Error updating collections for model: "+e.getMessage());
				modelCollections.put(model.getModelId(), "Error: "+e.getMessage());
			}
		}
	}
}
