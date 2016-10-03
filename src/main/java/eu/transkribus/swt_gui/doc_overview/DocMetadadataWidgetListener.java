package eu.transkribus.swt_gui.doc_overview;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.auth.TrpRole;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.edit_decl_manager.EditDeclManagerDialog;
import eu.transkribus.swt_gui.edit_decl_manager.EditDeclViewerDialog;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class DocMetadadataWidgetListener extends SelectionAdapter {
	private final static Logger logger = LoggerFactory.getLogger(DocOverviewListener.class);

	DocMetadataWidget docMdWidget;
	Storage store = Storage.getInstance();
	
	public DocMetadadataWidgetListener(DocMetadataWidget docMdWidget) {
		this.docMdWidget = docMdWidget;

		attach();
	}
	
	public void detach() {	
		docMdWidget.getOpenEditDeclManagerBtn().removeSelectionListener(this);
		docMdWidget.getOpenMetadataEditorBtn().removeSelectionListener(this);
	}
	
	public void attach() {
		docMdWidget.getOpenEditDeclManagerBtn().addSelectionListener(this);
		docMdWidget.getOpenMetadataEditorBtn().addSelectionListener(this);
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		Object s = e.getSource();

		if (s == docMdWidget.getOpenMetadataEditorBtn()) {
			docMdWidget.openMetadataEditor(null);
		}
		else if (s == docMdWidget.getOpenEditDeclManagerBtn()) {
			docMdWidget.openEditDeclManagerWidget();
		}
	}
		
}

