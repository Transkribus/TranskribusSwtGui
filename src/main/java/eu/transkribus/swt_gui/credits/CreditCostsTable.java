package eu.transkribus.swt_gui.credits;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import eu.transkribus.core.model.beans.TrpCreditCosts;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt.util.TableLabelProvider;

public class CreditCostsTable extends MyTableViewer {
	private static final String JOB_NAME_COL = "Job Type";
	private static final String PAGES_REMAINING_COL = "Pages Remaining";
	
	private final ColumnConfig[] COLS = new ColumnConfig[] {
		new ColumnConfig(JOB_NAME_COL, 220, false, DefaultTableColumnViewerSorter.DESC),
		new ColumnConfig(PAGES_REMAINING_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
	};
	
	private double nrOfCredits;
	private List<TrpCreditCosts> costs;
	
	public CreditCostsTable(Composite parent, int style, double nrOfCredits, List<TrpCreditCosts> costs) {
		super(parent, style);
		this.nrOfCredits = nrOfCredits;
		this.costs = costs;
		this.setContentProvider(new ArrayContentProvider());	
	
		TableLabelProvider labelProvider = new TableLabelProvider() {
			@Override
			public String getColumnText(Object element, int columnIndex) {
				String txt = "N/A";
				if(element instanceof TrpCreditCosts ) {
					TrpCreditCosts c = (TrpCreditCosts) element;
					TableColumn column = table.getColumn(columnIndex);
					String colName = column.getText();
					switch(colName) {
					case JOB_NAME_COL:
						txt = c.getJobImpl();
						break;
					case PAGES_REMAINING_COL:
						if(c.getCostFactor() != null) {
							txt = "" + CreditCostsTable.this.nrOfCredits / c.getCostFactor();
						}
						break;
					}
				}
				return txt;
			}
		};
		this.setLabelProvider(labelProvider);
		this.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
				
		Table table = this.getTable();
		table.setHeaderVisible(true);
		
		this.addColumns(COLS);
		
		this.setInput(this.costs);
	}
	
	public void setNrOfCredits(double nrOfCredits) {
		this.nrOfCredits = nrOfCredits;
		this.refresh(true);
	}
}
