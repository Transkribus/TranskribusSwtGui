package eu.transkribus.swt_gui.structure_tree;

import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;

import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class StructureTypeEditingSupport extends EditingSupport {
	Storage store = Storage.getInstance();

	public StructureTypeEditingSupport(TreeViewer treeViewer) {
		super(treeViewer);
	}
	
	public TreeViewer getTreeViewer() {
		return (TreeViewer) getViewer();
	}

	@Override protected void setValue(Object element, Object value) {
		ITrpShapeType s = (ITrpShapeType) element;
		int i = (int) value;
		
		if (i >= 1 && i <= store.getStructCustomTagSpecsTypeStrings().size()) {
			s.setStructure(store.getStructCustomTagSpecsTypeStrings().get(i-1), false, this);
		}
		if (i == 0) {
			s.setStructure(null, false, this);
		}
		getViewer().refresh();
	}

	@Override protected Object getValue(Object element) {
		ITrpShapeType s = (ITrpShapeType) element;
		String struct = s.getStructure();
		
		int indexOfStruct = store.getStructCustomTagSpecsTypeStrings().indexOf(struct);
		return indexOfStruct + 1; // +1 because first element is empty string, see below
	}

	@Override protected CellEditor getCellEditor(Object element) {
		List<String> values = store.getStructCustomTagSpecsTypeStrings();
		values.toArray(new String[values.size()]);
//					List<String> values = EnumUtils.valuesList(TextTypeSimpleType.class);
		values.add(0, ""); // add empty string as value to
							// delete structure type!

		return new ComboBoxCellEditor(getTreeViewer().getTree(), values.toArray(new String[0]), SWT.READ_ONLY);
	}

	@Override protected boolean canEdit(Object element) {
		boolean isPageLocked = Storage.getInstance().isPageLocked();
		boolean isRegionOrLineOrWord = element instanceof TrpTextRegionType || element instanceof TrpTextLineType || element instanceof TrpWordType;
		
		return !isPageLocked && isRegionOrLineOrWord;
	}
}