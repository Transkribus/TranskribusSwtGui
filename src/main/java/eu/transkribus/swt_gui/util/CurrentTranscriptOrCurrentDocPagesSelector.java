package eu.transkribus.swt_gui.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class CurrentTranscriptOrCurrentDocPagesSelector extends Composite {
	
	Button currentTanscriptRadio;
	Button pagesRadio;
	
	boolean withCurrTranscript;
	
	CurrentDocPagesSelector ps;

	public CurrentTranscriptOrCurrentDocPagesSelector(Composite parent, int style, boolean oneRow, boolean withCurrentTranscript) {
		super(parent, style);
		
		int nColumns = oneRow ? 3 : 2;
		GridLayout gl = new GridLayout(nColumns, false);
		gl.marginHeight = gl.marginWidth = 0;
		this.setLayout(gl);
		
		withCurrTranscript = withCurrentTranscript;
		
		if(withCurrentTranscript) {
			currentTanscriptRadio = new Button(this, SWT.RADIO);
			currentTanscriptRadio.setText("Current page");
			currentTanscriptRadio.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, oneRow ? 1 : 2, 1));
			currentTanscriptRadio.setSelection(true);
			currentTanscriptRadio.setToolTipText("Restrict method to current transcript");
		}
		
		
		pagesRadio = new Button(this, SWT.RADIO);
		pagesRadio.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		pagesRadio.setToolTipText("Restrict method to selected pages");
		pagesRadio.setText("");
		
		ps = new CurrentDocPagesSelector(this, 0, true, false, false);
		ps.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		SelectionAdapter radioSelection = new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				updateGui();
			}
		};
		
		pagesRadio.addSelectionListener(radioSelection);
		if(withCurrentTranscript) {
			currentTanscriptRadio.addSelectionListener(radioSelection);
		}
		updateGui();
		
		if (!withCurrentTranscript) {
			selectPagesRadio();
		}
	}
	
	public void updateGui() {
		if (ps != null) {
			ps.setEnabled(pagesRadio.getSelection());	
		}
	}
	
	public boolean isCurrentTranscript() {
		if (withCurrTranscript) {
			return currentTanscriptRadio.getSelection();
		}
		else {
			return false;
		}
	}
	
	public Button getCurrentTranscriptButton() {
		return this.currentTanscriptRadio;
	}
	
	public boolean isPages() {
		return pagesRadio.getSelection();
	}
	
	public String getPagesStr() {
		return ps.getPagesStr();
	}
	
	public void selectPagesRadio(){
		SWTUtil.setSelection(pagesRadio, true);
		SWTUtil.setSelection(currentTanscriptRadio, false);
		updateGui();
	}
	
	public void setPagesStr(String pages) {
		ps.setPagesStr(pages);
	}
	
	public CurrentDocPagesSelector getPagesSelector() {
		return ps;
	}
	
	/**
	 * if current-transcript is selected, return the page index of this transcript, else the indices of the pages-str
	 * @throws IOException 
	 */
	public Set<Integer> getSelectedPageIndices() throws IOException {
		Storage store = Storage.getInstance();
		if (isCurrentTranscript()) {
			Set<Integer> res = new HashSet<>();
			if (store != null && store.getPage()!=null) {
				res.add(store.getPage().getPageNr()-1);
			}
			return res;
		}
		else {
			return ps.getSelectedPageIndices();
		}
	}
	
}
