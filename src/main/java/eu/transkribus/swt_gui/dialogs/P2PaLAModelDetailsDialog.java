package eu.transkribus.swt_gui.dialogs;

import java.util.List;

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
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.core.model.beans.TrpP2PaLAModel;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.TableLabelProvider;

public class P2PaLAModelDetailsDialog extends Dialog {
	MyTableViewer modelsTable;
	
	public static String NAME_COL = "Name";
	public static String DESC_COL = "Description";
	public static String BASELINES_COL = "Baselines";
	public static String STRUCT_TYPES_COL = "Structure Types";
	
	public static String TRAIN_SET_SIZE_COL = "N-Train";
	public static String VAL_SET_SIZE_COL = "N-Validation";
	public static String TEST_SET_SIZE_COL = "N-Test";
	
	public static final ColumnConfig[] COLS = new ColumnConfig[] {
			new ColumnConfig(NAME_COL, 120, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(DESC_COL, 250, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(BASELINES_COL, 75, false, DefaultTableColumnViewerSorter.ASC, "Does this model detect Baselines?"),
			new ColumnConfig(STRUCT_TYPES_COL, 750, false, DefaultTableColumnViewerSorter.ASC, "The region structure types this model detects"),
			new ColumnConfig(TRAIN_SET_SIZE_COL, 75, false, DefaultTableColumnViewerSorter.ASC, "The size of the training set"),
			new ColumnConfig(VAL_SET_SIZE_COL, 75, false, DefaultTableColumnViewerSorter.ASC, "The size of the validation set (which is used after every epoch during training to evaluate the model)"),
			new ColumnConfig(TEST_SET_SIZE_COL, 75, false, DefaultTableColumnViewerSorter.ASC, "The size of the test set (which is used once after training to evaluate the model)"),
		};	
	
	List<TrpP2PaLAModel> models;

	public P2PaLAModelDetailsDialog(Shell parentShell, List<TrpP2PaLAModel> models) {
		super(parentShell);
		this.models = models;
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
				if (!(element instanceof TrpP2PaLAModel)) {
					return "i am serious error";
				}
				TrpP2PaLAModel m = (TrpP2PaLAModel) element;
				String cn = COLS[index].name;
				
				if (cn.equals(NAME_COL)) {
					return m.getName();
				}
				else if (cn.equals(DESC_COL)) {
					return m.getDescription();
				}
				else if (cn.equals(BASELINES_COL)) {
					return StringUtils.contains(m.getOut_mode(), "L") ? "Yes" : "No";
				}
				else if (cn.equals(STRUCT_TYPES_COL)) {
//					return StringUtils.contains(m.getOut_mode(), "R") ? m.getStruct_types() : "";
					return m.getStruct_types();
				}
				else if (cn.equals(TRAIN_SET_SIZE_COL)) {
					return m.getTrain_set_size()!=null ? ""+m.getTrain_set_size() : "NA";
				}
				else if (cn.equals(VAL_SET_SIZE_COL)) {
					return m.getVal_set_size()!=null ? ""+m.getVal_set_size() : "NA";
				}
				else if (cn.equals(TEST_SET_SIZE_COL)) {
					return m.getTest_set_size()!=null ? ""+m.getTest_set_size() : "NA";
				}				
				
				return "i am error";
			}
		});
		modelsTable.setInput(models);
		
		return cont;
	}
}
