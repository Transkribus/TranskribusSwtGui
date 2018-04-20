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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
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

	public static final String ERR_LABEL_COL = "Metrics";
	public static final String ERR_WORD_COL = "Word";
	public static final String ERR_CHAR_COL = "Character";
	public static final String ERR_DESC_COL = "Description";
	
	MyTableViewer viewer;
	
	private TrpErrorRate resultErr;
	
	public final ColumnConfig[] ERR_COLS = new ColumnConfig[] {
			new ColumnConfig(ERR_LABEL_COL,200),
			new ColumnConfig(ERR_WORD_COL, 220),
			new ColumnConfig(ERR_CHAR_COL, 100),
			new ColumnConfig(ERR_DESC_COL, 500),
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
		
		TableItem itemErr = new TableItem(table, SWT.NONE);
		itemErr.setText(new String[] { "Error Rate", resultErr.getWer(), resultErr.getCer(),"https://en.wikipedia.org/wiki/Word_error_rate" });
		
		TableItem itemAcc = new TableItem(table,SWT.NONE);
		itemAcc.setText(new String[] { "Accuracy", resultErr.getwAcc(), resultErr.getcAcc(),"https://en.wikipedia.org/wiki/Word_error_rate" });
		 
		 
		
		


	    return body;
	  }

}
