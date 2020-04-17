package eu.transkribus.swt_gui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import eu.transkribus.core.model.beans.TrpBaselineErrorRate;
import eu.transkribus.core.model.beans.TrpBaselineErrorRateListEntry;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.mytableviewer.TableLabelProviderAdapter;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt.util.SWTUtil;

public class BaselineErrorRateStatsDiag extends Dialog {
	
	private TrpBaselineErrorRate err;
	private int docId;
	private String query;
	
	MyTableViewer tv;
	LabeledText doc, precison, recall, f1;

	public BaselineErrorRateStatsDiag(Shell parentShell, TrpBaselineErrorRate err, int docId, String query) {
		super(parentShell);
		this.err = err;
		this.query = query;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Baseline Statistics");
		newShell.setSize(600, 600);
		SWTUtil.centerShell(newShell);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
	}
	
	@Override
	protected boolean isResizable() {
	    return true;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Ok", true);
	}
	
	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);
		c.setLayout(new GridLayout(1, false));
		
		boolean makeColsEqual=false;
//		int txtStyle = SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY;
		int txtStyle = SWT.SINGLE | SWT.READ_ONLY;
		
		doc = new LabeledText(c, "Doc: ", makeColsEqual, txtStyle);
		Fonts.setBoldFont(doc.getLabel());
		doc.setText(""+docId);
		doc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		precison = new LabeledText(c, "Precision: ", makeColsEqual, txtStyle);
		Fonts.setBoldFont(precison.getLabel());
		precison.setText(""+CoreUtils.roundTo2(err.getPrecision()));
		precison.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		recall = new LabeledText(c, "Recall: ", makeColsEqual, txtStyle);
		Fonts.setBoldFont(recall.getLabel());
		recall.setText(""+CoreUtils.roundTo2(err.getRecall()));
		recall.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		f1 = new LabeledText(c, "F-Measure: ", makeColsEqual, txtStyle);
		Fonts.setBoldFont(f1.getLabel());
		f1.setText(""+CoreUtils.roundTo2(err.getF1()));
		f1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		tv = new MyTableViewer(c, 0);
		tv.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		tv.addColumns(new ColumnConfig("Page", 60));
		tv.addColumns(new ColumnConfig("Precision", 100));
		tv.addColumns(new ColumnConfig("Recall", 100));
		tv.addColumns(new ColumnConfig("F-Measure", 100));
		tv.setContentProvider(new ArrayContentProvider());
		tv.setLabelProvider(new TableLabelProviderAdapter() {
			@Override
			public String getColumnText(Object element, int columnIndex) {
				if (element instanceof TrpBaselineErrorRateListEntry) {
					TrpBaselineErrorRateListEntry e = (TrpBaselineErrorRateListEntry) element;
					String cn = tv.getColumn(columnIndex).getText();
					if ("Page".equals(cn)) {
						return ""+e.getPageNumber();
					}
					if ("Precision".equals(cn)) {
						return ""+CoreUtils.roundTo2(e.getPrecision());
					}
					if ("Recall".equals(cn)) {
						return ""+CoreUtils.roundTo2(e.getRecall());
					}
					if ("F-Measure".equals(cn)) {
						return ""+CoreUtils.roundTo2(e.getF1());
					}					
				}
				
				return "i am error";
			}
		});
		Table table = tv.getTable();
		table.setHeaderVisible(true);
		tv.setInput(err.getList());
				
		return c;
	}

}
