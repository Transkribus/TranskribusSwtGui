package eu.transkribus.swt_gui.doc_overview;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class DocMetadataEditorListener implements SelectionListener {
	DocMetadataEditor mdEd;
	TrpMainWidget mw;

	public DocMetadataEditorListener(TrpMainWidget mw, DocMetadataEditor mdEd) {
		this.mw = mw;
		this.mdEd = mdEd;
		
		mdEd.getApplyBtn().addSelectionListener(this);
		
	}

	@Override public void widgetSelected(SelectionEvent e) {
		
		
		
	}

	@Override public void widgetDefaultSelected(SelectionEvent e) {
	}

}
