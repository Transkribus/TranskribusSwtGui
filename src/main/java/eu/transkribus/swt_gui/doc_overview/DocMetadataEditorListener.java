package eu.transkribus.swt_gui.doc_overview;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;

import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.listener.IStorageListener;

public class DocMetadataEditorListener implements SelectionListener,
													IStorageListener,
													FocusListener,
													ICheckStateListener,
													TraverseListener{
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
		dme.titleText.addFocusListener(this);
		dme.authorText.addFocusListener(this);
		dme.genreText.addFocusListener(this);
		dme.writerText.addFocusListener(this);
		dme.descriptionText.addFocusListener(this);
		dme.langTable.addFocusListener(this);
		dme.langTable.addCheckStateListener(this);
		dme.scriptTypeCombo.addSelectionListener(this);
		dme.scriptTypeCombo2.addSelectionListener(this);
		dme.createdFrom.addFocusListener(this);
		dme.createdTo.addFocusListener(this);				
		dme.addTraverseListener(this);
		Storage.getInstance().addListener(this);		
	}
	
	void detach() {
		dme.saveBtn.removeSelectionListener(this);
		dme.openEditDeclManagerBtn.removeSelectionListener(this);
		dme.titleText.removeFocusListener(this);
		dme.authorText.removeFocusListener(this);
		dme.genreText.removeFocusListener(this);
		dme.writerText.removeFocusListener(this);
		dme.descriptionText.removeFocusListener(this);
		dme.langTable.removeFocusListener(this);
		dme.langTable.removeCheckStateListener(this);
		dme.scriptTypeCombo.removeSelectionListener(this);
		dme.scriptTypeCombo2.removeSelectionListener(this);
		dme.createdFrom.removeFocusListener(this);
		dme.createdTo.removeFocusListener(this);
		dme.removeTraverseListener(this);
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
		if (	s == dme.scriptTypeCombo 
			 || s == dme.scriptTypeCombo2 
			 || s == dme.enableCreatedFromBtn
			 || s == dme.enableCreatedToBtn		){
			
			dme.saveMd();
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {

		
	}

	@Override
	public void focusGained(FocusEvent e) {
		// do nothing
		
	}

	@Override
	public void focusLost(FocusEvent e) {
		dme.saveMd();
		
	}

	@Override
	public void checkStateChanged(CheckStateChangedEvent arg0) {
		dme.saveMd();
		
	}

	@Override
	public void keyTraversed(TraverseEvent e) {
		if (e.detail == SWT.TRAVERSE_RETURN) {
			dme.saveMd();
		}
	}


}
