package eu.transkribus.swt_gui.doc_overview;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.listener.IStorageListener;

public class DocMetadataEditorListener implements SelectionListener, IStorageListener {
	DocMetadataEditor dme;
	
	public DocMetadataEditorListener(DocMetadataEditor dme) {
		this.dme = dme;
		
		attach();
		
		dme.addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(DisposeEvent e) {
				detach();
			}
		});
	}
	
	void attach() {
		dme.saveBtn.addSelectionListener(this);
		dme.openEditDeclManagerBtn.addSelectionListener(this);
		Storage.getInstance().addListener(this);		
	}
	
	void detach() {
		dme.saveBtn.removeSelectionListener(this);
		dme.openEditDeclManagerBtn.removeSelectionListener(this);
		Storage.getInstance().removeListener(this);		
	}
	
	@Override public void handleDocLoadEvent(DocLoadEvent dle) {
		if (SWTUtil.isDisposed(dme))
			return;
		
		if (dle.doc != null) {
			dme.setMetadataToGui(dle.doc.getMd());
		}
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		Object s = e.getSource();
		
		TrpMainWidget mw = TrpMainWidget.getInstance();
		
		if (s == dme.saveBtn) {
			dme.saveMd();
		}
		else if (s == dme.openEditDeclManagerBtn) {
			mw.openEditDeclManagerDialog();
		}		
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {

		
	}
}
