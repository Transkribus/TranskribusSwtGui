package eu.transkribus.swt_gui.metadata;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.nebula.widgets.pagination.table.SortTableColumnSelectionListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.pagination_table.TableColumnBeanLabelProvider;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.TableViewerUtils;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class CollectionsTableWidget extends Composite {
	static String ID_COL = "ID";
	static String NAME_COL = "Name";
	static String ROLE_COL = "Role";
	static String DESC_COL = "Description";
	static String LABEL_COL = "Label";
	
	MyTableViewer tv;
	
	public CollectionsTableWidget(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new GridLayout(1, false));
		tv = new MyTableViewer(this, SWT.SINGLE | SWT.V_SCROLL);
		tv.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		tv.getTable().setLinesVisible(true);
		tv.getTable().setHeaderVisible(true);
		tv.setContentProvider(new ArrayContentProvider());
		createColumns();
	}
	
	
	protected void createColumns() {
		class CollectionsTableColumnLabelProvider extends TableColumnBeanLabelProvider {
			Font boldFont = Fonts.createBoldFont(tv.getControl().getFont());
			
			public CollectionsTableColumnLabelProvider(String colName) {
				super(colName);
			}
            
        	@Override public Font getFont(Object element) {
        		if (element instanceof TrpCollection) {
        			TrpCollection c = (TrpCollection) element;
        			
        			if (c.getColId() == TrpMainWidget.getInstance().getUi().getServerWidget().getSelectedCollectionId())
        				return boldFont;
        		}
        		
        		return null;
        	}
		}
		
		createColumn(ID_COL, 50, "colId", new CollectionsTableColumnLabelProvider("colId"));
		createColumn(NAME_COL, 250, "colName", new CollectionsTableColumnLabelProvider("colName"));
		createColumn(ROLE_COL, 80, "colName", new CollectionsTableColumnLabelProvider("role"));
		createColumn(DESC_COL, 500, "description", new CollectionsTableColumnLabelProvider("description"));
	}
	protected TableViewerColumn createColumn(String columnName, int colSize, String sortPropertyName, CellLabelProvider lp) {
		TableViewerColumn col = TableViewerUtils.createTableViewerColumn(tv, 0, columnName, colSize);
		col.setLabelProvider(lp);
		if (sortPropertyName != null)
			col.getColumn().addSelectionListener(new SortTableColumnSelectionListener(sortPropertyName));
		return col;
	}
	
	public void refreshList(List<TrpCollection> colList) {
		tv.setInput(colList);
	}


	public TrpCollection getSelection() {
		IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
		if (sel.getFirstElement() != null && sel.getFirstElement() instanceof TrpCollection) {
			return (TrpCollection) sel.getFirstElement();
		} else {
			return null;
		}
	}
	
}
