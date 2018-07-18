package eu.transkribus.swt_gui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class CurrentTranscriptOrCurrentDocPagesSelector extends Composite {
	
	Button currentTanscriptRadio;
	Button pagesRadio;
	
	CurrentDocPagesSelector ps;

	public CurrentTranscriptOrCurrentDocPagesSelector(Composite parent, int style, boolean oneRow) {
		super(parent, style);
		
		int nColumns = oneRow ? 3 : 2;
		GridLayout gl = new GridLayout(nColumns, false);
		gl.marginHeight = gl.marginWidth = 0;
		this.setLayout(gl);
		
		currentTanscriptRadio = new Button(this, SWT.RADIO);
		currentTanscriptRadio.setText("Current page");
		currentTanscriptRadio.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, oneRow ? 1 : 2, 1));
		currentTanscriptRadio.setSelection(true);
		currentTanscriptRadio.setToolTipText("Restrict method to current transcript");
		
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
		currentTanscriptRadio.addSelectionListener(radioSelection);
		
		updateGui();
	}
	
	public void updateGui() {
		ps.setEnabled(pagesRadio.getSelection());
	}
	
	public boolean isCurrentTranscript() {
		return currentTanscriptRadio.getSelection();
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
		pagesRadio.setSelection(true);
		currentTanscriptRadio.setSelection(false);
		updateGui();
	}
	
	public void setPagesStr(String pages) {
		ps.setPagesStr(pages);
	}
	
	public CurrentDocPagesSelector getPagesSelector() {
		return ps;
	}
	
}
