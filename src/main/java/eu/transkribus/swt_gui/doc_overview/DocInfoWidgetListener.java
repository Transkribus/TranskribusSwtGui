package eu.transkribus.swt_gui.doc_overview;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_gui.mainwidget.Storage;

public class DocInfoWidgetListener extends SelectionAdapter {
	private final static Logger logger = LoggerFactory.getLogger(DocOverviewListener.class);

	DocInfoWidget docMdWidget;
	Storage store = Storage.getInstance();
	
	public DocInfoWidgetListener(DocInfoWidget docMdWidget) {
		this.docMdWidget = docMdWidget;

		attach();
	}
	
	public void detach() {	
		docMdWidget.getOpenEditDeclManagerBtn().removeSelectionListener(this);
	}
	
	public void attach() {
		docMdWidget.getOpenEditDeclManagerBtn().addSelectionListener(this);
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		Object s = e.getSource();

		if (s == docMdWidget.getOpenEditDeclManagerBtn()) {
			docMdWidget.openEditDeclManagerWidget();
		}
	}
		
}

