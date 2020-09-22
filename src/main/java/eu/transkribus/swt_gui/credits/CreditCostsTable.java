package eu.transkribus.swt_gui.credits;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpCreditCosts;
import eu.transkribus.core.model.beans.enums.DocType;
import eu.transkribus.core.model.beans.job.enums.JobImpl;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt.util.TableLabelProvider;

public class CreditCostsTable extends MyTableViewer {
	private static final Logger logger = LoggerFactory.getLogger(CreditCostsTable.class);
	
	private static final String JOB_NAME_COL = "Job Type";
	private static final String DOC_TYPE_COL = "Document Type";
	private static final String PAGES_REMAINING_COL = "Pages Remaining";
	
	private final ColumnConfig[] COLS = new ColumnConfig[] {
		new ColumnConfig(JOB_NAME_COL, 220, false, DefaultTableColumnViewerSorter.DESC),
		new ColumnConfig(DOC_TYPE_COL, 150, false, DefaultTableColumnViewerSorter.DESC),
		new ColumnConfig(PAGES_REMAINING_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
	};
	
	private double nrOfCredits;
	private List<TrpCreditCosts> costs;
	private final DecimalFormat decimalFormat;
	
	public CreditCostsTable(Composite parent, int style, double nrOfCredits, List<TrpCreditCosts> costs) {
		super(parent, style);
		this.nrOfCredits = nrOfCredits;
		this.costs = costs;
		this.decimalFormat = buildDecimalFormat();
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
						txt = buildJobImplLabel(c.getJobImpl());
						break;
					case DOC_TYPE_COL:
						txt = buildDocTypeLabel(c.getDocType());
						break;
					case PAGES_REMAINING_COL:
						if(c.getCostFactor() != null) {
							txt = buildRemainingPagesLabel(CreditCostsTable.this.nrOfCredits, c.getCostFactor());
						}
						break;
					}
				}
				return txt;
			}

			private String buildRemainingPagesLabel(double nrOfCredits, Double costFactor) {
				final double remainingPages = nrOfCredits / costFactor;
				final String label = decimalFormat.format(remainingPages);
				logger.trace("Rounding: {} -> {}", remainingPages, label);
				return label;
			}

			private String buildJobImplLabel(String jobImpl) {
				try {
					return JobImpl.valueOf(jobImpl).getLabel();
				} catch (IllegalArgumentException e) {
					logger.warn("Could not resolve JobImpl value for String '{}'", jobImpl);
					return jobImpl;
				}
			}

			private String buildDocTypeLabel(Integer docType) {
				try {
					DocType type =  DocType.fromValue(docType);
					return StringUtils.capitalize(("" + type).toLowerCase());
				} catch (IllegalArgumentException e) {
					logger.warn("Could not resolve DocType value for Integer '{}'", docType);
					return "N/A";
				}
			}
		};
		this.setLabelProvider(labelProvider);
		this.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
				
		Table table = this.getTable();
		table.setHeaderVisible(true);
		
		this.addColumns(COLS);
		
		this.setInput(this.costs);
	}
	
	private DecimalFormat buildDecimalFormat() {
		//hide trailing decimal point zeros
		DecimalFormat df = new DecimalFormat("0.##");
		//round down explicitly, otherwise 0.9999996 would be shown as 1
		df.setRoundingMode(RoundingMode.DOWN);
		return df;
	}

	public void setNrOfCredits(double nrOfCredits) {
		this.nrOfCredits = nrOfCredits;
		this.refresh(true);
	}
}
