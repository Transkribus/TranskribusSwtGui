package eu.transkribus.swt_gui.htr;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;

import eu.transkribus.swt_gui.htr.treeviewer.DocumentDataSetEntry;

public class DocumentDataSetTableWidget extends DataSetTableWidget<DocumentDataSetEntry> {
	
	public DocumentDataSetTableWidget(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	public List<DocumentDataSetEntry> getSelectedDataSets() {
		IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
		if (!sel.isEmpty()) {
			return (List<DocumentDataSetEntry>)sel.toList();
		} else {
			return new ArrayList<DocumentDataSetEntry>(0);
		}
	}
}