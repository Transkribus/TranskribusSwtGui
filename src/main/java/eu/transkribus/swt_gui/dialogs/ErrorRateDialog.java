package eu.transkribus.swt_gui.dialogs;

import java.awt.Desktop;
import java.awt.Label;
import java.net.URI;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import eu.transkribus.client.util.FtpConsts;
import eu.transkribus.core.model.beans.TrpErrorRate;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.pagination_tables.PageLockTablePagination;


public class ErrorRateDialog extends Dialog{

	public static final String ERR_EMPTY_COL = " ";
	public static final String ERR_WORD_COL = "Error Rate";
	public static final String ERR_CHAR_COL = "Accuracy";
	public static final String BAG_PREC_COL = "Precision";
	public static final String BAG_REC_COL = "Recall";
	public static final String BAG_FMEA_COL = "F-Measure";
	public static final String ERR_DESC_COL = "Description";
	
	MyTableViewer viewer;
	
	
	
	private TrpErrorRate resultErr;
	
	public final ColumnConfig[] ERR_COLS = new ColumnConfig[] {
			new ColumnConfig(ERR_EMPTY_COL, 200),
			new ColumnConfig(ERR_WORD_COL, 100),
			new ColumnConfig(ERR_CHAR_COL, 100),
			new ColumnConfig(BAG_PREC_COL, 100),
			new ColumnConfig(BAG_REC_COL, 100),
			new ColumnConfig(BAG_FMEA_COL, 100),
			new ColumnConfig(ERR_DESC_COL, 400),
		};

	public ErrorRateDialog(Shell parentShell, TrpErrorRate resultErr ) {
		
		super(parentShell);
		this.resultErr = resultErr;
		
	}

	@Override protected void configureShell(Shell shell) {
	      super.configureShell(shell);
	      shell.setText("Error Rate Results");
	}
	
	@Override
	  protected Control createDialogArea(final Composite parent)
	  {
	    final Composite body = (Composite)super.createDialogArea(parent);

	    final MyTableViewer viewer = new MyTableViewer(body, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

	    viewer.addColumns(ERR_COLS);
	    
	    Table table = viewer.getTable();
		table.setHeaderVisible(true);
		
		TableItem itemWord = new TableItem(table, SWT.NONE);
		itemWord.setText(new String[] { "Word", resultErr.getWer(), resultErr.getwAcc(),"","","","https://en.wikipedia.org/wiki/Word_error_rate"});
		
		TableItem itemChar = new TableItem(table,SWT.NONE);
		itemChar.setText(new String[] { "Character", resultErr.getCer(), resultErr.getcAcc(),"","","","https://en.wikipedia.org/wiki/Word_error_rate" });
		
		TableItem itemBag = new TableItem(table,SWT.NONE);
		itemBag.setText(new String[] {"Bag of Tokens","","", resultErr.getBagTokensPrec(),resultErr.getBagTokensRec(), resultErr.getBagTokensF(), "https://en.wikipedia.org/wiki/F1_score"});

	    return body;
	  }

}
