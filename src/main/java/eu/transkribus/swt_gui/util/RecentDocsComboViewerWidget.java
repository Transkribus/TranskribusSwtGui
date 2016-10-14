package eu.transkribus.swt_gui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.util.RecentDocsPreferences;

public class RecentDocsComboViewerWidget extends Composite implements Observer {
	private final static Logger logger = LoggerFactory.getLogger(RecentDocsComboViewerWidget.class);
	
	public ComboViewer lastDocsComboViewer;
	public Combo lastDocsCombo;
	
	Storage storage = Storage.getInstance();
		
	public RecentDocsComboViewerWidget(Composite parent, int style) {
		super(parent, style);
				
//		Composite collsContainer = new Composite(this, 0);
//		collsContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.setLayout(new GridLayout(2, false));
		
				
		lastDocsCombo = new Combo(this, SWT.READ_ONLY | SWT.DROP_DOWN);
		lastDocsCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		lastDocsComboViewer = new ComboViewer(lastDocsCombo);
				
		lastDocsComboViewer.setLabelProvider(new LabelProvider() {
			@Override public String getText(Object element) {
				if (element instanceof String) {
					return ((String) element);
				}
				else return "i am error";
			}
		});
		
		lastDocsComboViewer.setContentProvider(new ArrayContentProvider());
						
		updateDocs(true);
	}
	
		
	public void updateDocs(boolean sendSelectionEvent) {
		setRecentDocs(sendSelectionEvent);
	}
			
	public String getSelectedDoc(){
		IStructuredSelection sel = (IStructuredSelection) lastDocsComboViewer.getSelection();
		if (!sel.isEmpty())
			return (String) sel.getFirstElement();
		
		return null;
	}
	
	public void setRecentDocs(boolean sendSelectionEvent) {
		
		lastDocsComboViewer.refresh();
		lastDocsComboViewer.setInput(RecentDocsPreferences.getItems());
		lastDocsComboViewer.refresh(true);
		
		if (sendSelectionEvent)
			sendComboSelectionEvent();
	}
	
	
	void sendComboSelectionEvent() {
		Event event = new Event(); 
		event.type = SWT.Selection;
		event.widget = lastDocsCombo;
//		event.widget = this;
		lastDocsCombo.notifyListeners(SWT.Selection, event);
	}
	


	@Override public void update(Observable o, Object arg) {
	}
	
	

}
