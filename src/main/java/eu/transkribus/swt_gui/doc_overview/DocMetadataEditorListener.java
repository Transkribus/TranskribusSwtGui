package eu.transkribus.swt_gui.doc_overview;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.util.DelayedTask;

public class DocMetadataEditorListener implements SelectionListener,
													IStorageListener,
													FocusListener,
													ICheckStateListener,
													TraverseListener,
													ModifyListener
													{
	private final static Logger logger = LoggerFactory.getLogger(DocMetadataEditorListener.class);
	
	DocMetadataEditor dme;
	
	boolean deactivate=false;
	
	public DocMetadataEditorListener(DocMetadataEditor dme) {
		this.dme = dme;
		
		attach();
		
		dme.addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(DisposeEvent e) {
				detach();
			}
		});
	}
	
	public boolean isDeactivate() {
		return deactivate;
	}

	public void setDeactivate(boolean deactivate) {
		this.deactivate = deactivate;
	}

	void attach() {
		dme.saveBtn.addSelectionListener(this);
		dme.openEditDeclManagerBtn.addSelectionListener(this);
		
		dme.titleText.addFocusListener(this);
		dme.authorText.addFocusListener(this);
		dme.genreText.addFocusListener(this);
		dme.writerText.addFocusListener(this);
		dme.descriptionText.addFocusListener(this);
		
		dme.titleText.addModifyListener(this);
		dme.authorText.addModifyListener(this);
		dme.genreText.addModifyListener(this);
		dme.writerText.addModifyListener(this);
		dme.descriptionText.addModifyListener(this);

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
		
		dme.titleText.removeModifyListener(this);
		dme.authorText.removeModifyListener(this);
		dme.genreText.removeModifyListener(this);
		dme.writerText.removeModifyListener(this);
		dme.descriptionText.removeModifyListener(this);
		
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
		if (deactivate) {
			return;
		}
		
		if (SWTUtil.isDisposed(dme))
			return;
		
		if (dle.doc != null) {
			dme.setMetadataToGui(dle.doc.getMd());
		}
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (deactivate) {
			return;
		}		
		
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
		if (deactivate) {
			return;
		}
		
		
	}

	@Override
	public void focusGained(FocusEvent e) {
		if (deactivate) {
			return;
		}
		
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (deactivate) {
			return;
		}
		
		dme.saveMd();
	}

	@Override
	public void checkStateChanged(CheckStateChangedEvent arg0) {
		if (deactivate) {
			return;
		}		
		
		dme.saveMd();
		
	}

	@Override
	public void keyTraversed(TraverseEvent e) {
		if (deactivate) {
			return;
		}		
		
		if (e.detail == SWT.TRAVERSE_RETURN) {
			dme.saveMd();
		}
	}

	/**
	 * saves doc-md after 500 ms of no further modify event
	 */
	private DelayedTask dt = new DelayedTask(() -> { dme.saveMd(); }, true);
	
	@Override
	public void modifyText(ModifyEvent e) {
		if (deactivate) {
			return;
		}		
//		logger.trace("modified, s = "+e.getSource());
		
		dt.start();
	}


}
