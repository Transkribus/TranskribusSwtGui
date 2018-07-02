package eu.transkribus.swt_gui.structure_tree;

import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

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
		getViewer().update(element, null);
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
		values.add(0, "--delete--"); // add --delete-- string as value to
							// delete structure type!

		ComboBoxCellEditor ce = new ComboBoxCellEditor(getTreeViewer().getTree(), values.toArray(new String[0]), SWT.READ_ONLY | SWT.FLAT);
//		ce.setActivationStyle();
		ce.setActivationStyle(ComboBoxCellEditor.DROP_DOWN_ON_MOUSE_ACTIVATION);
		CCombo c = (CCombo) ce.getControl();
		c.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setValue(element, c.getSelectionIndex()); // on selection of the new structure type in the combo box, set the new value and close the editor	
			}
		});
		
		return ce;
	}

	@Override protected boolean canEdit(Object element) {
		boolean isPageLocked = Storage.getInstance().isPageLocked();
		boolean isRegionOrLineOrWord = element instanceof TrpTextRegionType || element instanceof TrpTextLineType || element instanceof TrpWordType;
		
		return !isPageLocked && isRegionOrLineOrWord;
	}
}