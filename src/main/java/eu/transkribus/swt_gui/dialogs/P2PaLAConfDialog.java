package eu.transkribus.swt_gui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpP2PaLAModel;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.TableLabelProvider;

public class P2PaLAConfDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(P2PaLAConfDialog.class);
	MyTableViewer modelsTable;
	
	public static String NAME_COL = "Name";
	public static String DESC_COL = "Description";
	public static String STRUCT_TYPES_COL = "Structure Types";
	
	public static final ColumnConfig[] COLS = new ColumnConfig[] {
			new ColumnConfig(NAME_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(DESC_COL, 250, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(STRUCT_TYPES_COL, 800, false, DefaultTableColumnViewerSorter.ASC),
		};	
	
	List<TrpP2PaLAModel> models;

	public P2PaLAConfDialog(Shell parentShell, List<TrpP2PaLAModel> models) {
		super(parentShell);
		this.models = models;
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(800, 300);
	}
	
	@Override
	protected boolean isResizable() {
	    return true;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
	    createButton(parent, IDialogConstants.OK_ID,"OK", false);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		
		Link infoText = new Link(cont, 0);
		String githubLink="https://github.com/lquirosd/P2PaLA";
		infoText.setText("P2PaLA structure analysis tool, see <a href=\""+githubLink+"\">"+githubLink+"</a>");
		SWTUtil.onSelectionEvent(infoText, e -> {
			try {
				org.eclipse.swt.program.Program.launch(e.text);
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		});
		Fonts.setBoldFont(infoText);
		
		Label modelsLabel = new Label(cont, 0);
		modelsLabel.setText("Available models:");
//		Fonts.setBoldFont(modelsLabel);
		
		modelsTable = new MyTableViewer(cont, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		modelsTable.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
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
				else if (cn.equals(STRUCT_TYPES_COL)) {
					return m.getStruct_types();
				}
				
				return "i am error";
			}
		});
		
		// TEST
		setModels(models);
		
		return cont;
	}
	
	public void setModels(List<TrpP2PaLAModel> models) {
		logger.debug("setting input models, N = "+CoreUtils.size(models));
		
		if (models != null) { // null check needed???
			modelsTable.setInput(models);	
		}
		else {
			modelsTable.setInput(new ArrayList<>());
		}
	}
	
	public void setVisible() {
		if (super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}
	
	@Override
	protected void okPressed() {
//		storeSelectionInParameterMap();
		super.okPressed();
	}	

}
