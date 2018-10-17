package eu.transkribus.swt_gui.dialogs;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent.PcGtsType;
import eu.transkribus.core.util.PageXmlUtils;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.xmlviewer.XmlViewer;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class PAGEXmlViewer extends XmlViewer {
	private final static Logger logger = LoggerFactory.getLogger(PAGEXmlViewer.class);
	
	Button reloadBtn;
	
	Storage store = Storage.getInstance();
	TrpMainWidget mw = TrpMainWidget.getInstance();

	public PAGEXmlViewer(Shell parent, int style) {
		super(parent, style);
	}
	
	@Override protected void createContents() {
		super.createContents();
		
		reloadBtn = new Button(topRightBtns, SWT.PUSH);
		reloadBtn.setImage(Images.REFRESH);
		reloadBtn.setToolTipText("Reload page from (edited) file");
		
		reloadBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				reloadTranscriptFromModifiedText();
			}
		});
		reloadBtn.setEnabled(editBtn.getSelection());
	}
	
	@Override protected void editBtnClicked() {
		super.editBtnClicked();
		
		if (reloadBtn != null)
			reloadBtn.setEnabled(editBtn.getSelection());
	}
	
	void reloadTranscriptFromModifiedText() {
		logger.debug("reloading transcript from modified xml text, textChanged = "+textChanged);
		
		if (!textChanged)
			return;
		
		try {
			if (!mw.saveTranscriptDialogOrAutosave()) {
				return;
			}
			
			PcGtsType pcGtsType = PageXmlUtils.unmarshal(currentText);
			logger.debug("successfully unmarshalled new page from modified text - reload!");
			
			store.getTranscript().setPageData(pcGtsType);
			store.setCurrentTranscriptEdited(true);
			
			mw.reloadCurrentTranscript(true, true);
			
//			store.getTranscript().getPage().setEdited(true);
//			mw.updatePageInfo();
		} 
		catch (JAXBException e) {
			String message = e.getCause()!=null ? e.getCause().getMessage() : e.getMessage();
			if (StringUtils.isEmpty(message))
				message = "Error unmarshalling PAGE XML";
			
			DialogUtil.showDetailedErrorMessageBox(shell, "Error loading transcript from text", message, e);
		}
		catch (Throwable e) {
			mw.onError("Error loading transcript from XML text", e.getMessage(), e);
		}
		
		
	}
	
	
	
	public Button getReloadBtn() { 
		return reloadBtn;
	}
	
	
	
	

}
