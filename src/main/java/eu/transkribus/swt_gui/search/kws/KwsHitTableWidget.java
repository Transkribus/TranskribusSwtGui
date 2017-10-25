package eu.transkribus.swt_gui.search.kws;

import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.kws.TrpKwsHit;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;

public class KwsHitTableWidget extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(KwsHitTableWidget.class);
	
	public static final String KWS_CONF_COL = "Confidence";
	public static final String KWS_PAGE_COL = "Page Nr.";
	public static final String KWS_TEXT_COL = "Line transcription";
	public static final String KWS_PREVIEW_COL = "Preview";
	
	MyTableViewer tv;
	int selectedId=-1;
	
	// filter:
//	Text filter;
//	boolean withFilter;
	
	public final ColumnConfig[] KWS_HIT_COLS = new ColumnConfig[] {
		new ColumnConfig(KWS_CONF_COL, 150, true, DefaultTableColumnViewerSorter.DESC),
		new ColumnConfig(KWS_PAGE_COL, 100, false, DefaultTableColumnViewerSorter.DESC),
		new ColumnConfig(KWS_TEXT_COL, 400, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(KWS_PREVIEW_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
	};
	
	public KwsHitTableWidget(Composite parent, int style, Map<TrpKwsHit, Image> icons) {
		super(parent, style);

		this.setLayout(new GridLayout(1, false));
				
		tv = new MyTableViewer(this, SWT.H_SCROLL | SWT.V_SCROLL);
		tv.setContentProvider(new ArrayContentProvider());
		tv.setLabelProvider(new KwsHitTableLabelProvider(tv, icons));
		tv.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
				
		Table table = tv.getTable();
		table.setHeaderVisible(true);
		
		tv.addColumns(KWS_HIT_COLS);
		
	}

	public MyTableViewer getTableViewer() {
		return tv;
	}

	public TrpKwsHit getSelectedKws() {
		IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
		if (sel.getFirstElement() != null && sel.getFirstElement() instanceof TrpKwsHit) {
			return (TrpKwsHit) sel.getFirstElement();
		} else
			return null;

	}

	public TableItem getItem(Point p) {
		return tv.getTable().getItem(p);
	}
}