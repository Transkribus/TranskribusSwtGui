package eu.transkribus.swt_gui.structure_tree;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class ReadingOrderEditingSupport extends EditingSupport {
	private static final Logger logger = LoggerFactory.getLogger(ReadingOrderEditingSupport.class);
	Composite parent;
	
	public ReadingOrderEditingSupport(Composite parent, TreeViewer viewer) {
		super(viewer);
		this.parent = parent;
	}
	
	public TreeViewer getTreeViewer() {
		return (TreeViewer) getViewer();
	}

	@Override protected void setValue(Object element, Object value) {
		ITrpShapeType s = (ITrpShapeType) element;
		//logger.debug("value is: "+value);
		String valueStr = (String) value;
		//logger.debug("valueStr is: "+valueStr);
	
		if (valueStr.isEmpty()) {
			s.setReadingOrder(null, parent);
		} else {
			try {
				int ro = Integer.parseInt(valueStr);
				//logger.debug("++++++++++++reInsertIntoParent(ro) " + (ro-1));
				s.removeFromParent();
				s.reInsertIntoParent(ro-1);
				//s.setReadingOrder(ro, StructureTreeWidget.this);
			} catch (NumberFormatException ne) {
				logger.debug("not a valid number: "+valueStr);
			}
		}
		getTreeViewer().refresh();
	}

	@Override protected Object getValue(Object element) {
		ITrpShapeType s = (ITrpShapeType) element;
		//increase reding order with one to have sorting from 1 to n instead of 0 to n
		return s.getReadingOrder()==null ? "" : ""+(s.getReadingOrder()+1);
	}

	@Override protected CellEditor getCellEditor(Object element) {
		return new TextCellEditor(getTreeViewer().getTree());
	}

	@Override protected boolean canEdit(Object element) {
		boolean isPageLocked = Storage.getInstance().isPageLocked();
		boolean isRegionOrLineOrWord = element instanceof TrpRegionType || element instanceof TrpTextLineType || element instanceof TrpWordType;
		return !isPageLocked && isRegionOrLineOrWord;
	}
}