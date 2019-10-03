package eu.transkribus.swt_gui.htr;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Composite;

import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.swt.util.TrpViewerFilterWidget;

public class HtrFilterWidget extends TrpViewerFilterWidget {
	public HtrFilterWidget(Composite parent, StructuredViewer viewer, int style) {
		super(parent, viewer, style, TrpHtr.class, "htrId", "name", "language");
	}
}
