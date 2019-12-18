package eu.transkribus.swt_gui.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.batik.dom.util.DocumentDescriptor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import eu.transkribus.core.model.beans.DocSelection;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt_gui.dialogs.DocumentsSelectorDialog;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class CurrentTranscriptOrDocPagesOrCollectionSelector extends Composite {
	
	Button currentTanscriptRadio;
	Button pagesRadio;
	Button multipleDocsRadio;
	Button docsSelectorBtn;
	
	Color red = Colors.getSystemColor(SWT.COLOR_DARK_RED);
	
	Label docsLabel;
	
	CurrentDocPagesSelector ps;
	ExpandableComposite docChooserExp;
	
	List<DocumentSelectionDescriptor> documentsSelected = null;
	List<DocSelection> docSelections = null; 
	
	boolean withPagesSelector=false;
	
	/*
	 * selection of pages can be done for current page, several/all pages of current doc or several/all documents of loaded collection
	 */
	public CurrentTranscriptOrDocPagesOrCollectionSelector(Composite parent, int style, boolean oneRow, boolean withCurrentTranscript, boolean withPagesSelector) {
		super(parent, style);
		
		int nColumns = oneRow ? 3 : 3;
		GridLayout gl = new GridLayout(nColumns, false);
		gl.marginHeight = gl.marginWidth = 0;
		this.setLayout(gl);
		
		this.withPagesSelector = withPagesSelector;
		
		if(withCurrentTranscript) {
			currentTanscriptRadio = new Button(this, SWT.RADIO);
			currentTanscriptRadio.setText("Current page");
			currentTanscriptRadio.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, oneRow ? 3 : 1, 1));
			currentTanscriptRadio.setSelection(true);
			currentTanscriptRadio.setToolTipText("Restrict method to current transcript");
		}
		
		
		pagesRadio = new Button(this, SWT.RADIO);
		pagesRadio.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		pagesRadio.setToolTipText("Restrict method to selected pages");
		pagesRadio.setText("Pages (0):");
		
		ps = new CurrentDocPagesSelector(this, 0, false, false, false);
		ps.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, oneRow ? 2 : 1, 1));
		
		SelectionAdapter radioSelection = new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				//System.out.println("e source: " + ((Button) e.getSource()));
				if (e.getSource().equals(pagesRadio)){
					multipleDocsRadio.setSelection(!pagesRadio.getSelection());
				}
				else if (e.getSource().equals(currentTanscriptRadio)){
					multipleDocsRadio.setSelection(!currentTanscriptRadio.getSelection());
				}
				else if (e.getSource().equals(multipleDocsRadio)){
					currentTanscriptRadio.setSelection(!multipleDocsRadio.getSelection());
					pagesRadio.setSelection(!multipleDocsRadio.getSelection());
				}
				updateGui();
			}
		};
		
		pagesRadio.addSelectionListener(radioSelection);
		if(withCurrentTranscript) {
			currentTanscriptRadio.addSelectionListener(radioSelection);
		}

		docChooserExp = new ExpandableComposite(this, ExpandableComposite.COMPACT);
		docChooserExp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Composite laDocChooserGroup = new Composite(docChooserExp, SWT.SHADOW_ETCHED_IN);
		laDocChooserGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		laDocChooserGroup.setLayout(new GridLayout(3, false));
		
	    docsLabel = new Label(laDocChooserGroup, 0);
	    docsLabel.setFont(Fonts.createBoldFont(docsLabel.getFont()));
	    docsLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 3, 1));
	    //docsLabel.setForeground(red);
		
	    multipleDocsRadio = new Button(laDocChooserGroup, SWT.RADIO);
	    multipleDocsRadio.setText("Current collection");
	    multipleDocsRadio.addSelectionListener(radioSelection);
	    multipleDocsRadio.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
	    //multipleDocsRadio.setForeground(red);
	    
	    docsSelectorBtn = new Button(laDocChooserGroup, SWT.PUSH);
	    docsSelectorBtn.setText("Choose docs...");
	    docsSelectorBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
	    docsSelectorBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DocumentsSelectorDialog dsd = new DocumentsSelectorDialog(getShell(), "Select documents for job", Storage.getInstance().getDocList(), withPagesSelector);
				if (dsd.open() == IDialogConstants.OK_ID) {
					documentsSelected = dsd.getCheckedDocumentDescriptors();
					docSelections = dsd.getCheckedDocSelections();
					//System.out.println("n selected documents: "+dsd.getCheckedDocs().size());
				}
				
				updateDocsNumberLabel();
			}
		});
	    docsSelectorBtn.setEnabled(false);
	    //docsSelectorBtn.setForeground(red);
	    
	    docChooserExp.setClient(laDocChooserGroup);
	    docChooserExp.setText("Document Selection (for admins)");
	    docChooserExp.setTitleBarForeground(red);
	    docChooserExp.setForeground(red);
		Fonts.setBoldFont(docChooserExp);
		docChooserExp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		docChooserExp.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				parent.layout();
			}
		});
		
		parent.pack();
		updateGui();
	}
	
	private void updateDocsNumberLabel() {
		//String txt = "(only choosable for admins)" + System.lineSeparator();
		String txt = "";
		if (!multipleDocsRadio.getSelection()) {
			TrpDoc d = Storage.getInstance().getDoc();
			if (d == null) {
				txt += "No document loaded!";
			} else {
				txt += d.getMd().getTitle()+" ("+d.getId()+")";
			}
		} else {
			int id = Storage.getInstance().getCollId();
			if (id == 0) {
				txt += "No collection found!";
			} else {
				TrpCollection c = Storage.getInstance().getCollection(id);
				int total = Storage.getInstance().getDocList().size();
				int nToExport = documentsSelected == null ? 0 : documentsSelected.size();
				txt += "Select "+nToExport+"/"+total+" docs from collection "+c.getColName()+" ("+c.getColId()+")";
			}
		}
		
		docsLabel.setText(txt);
	}
	
	public List<DocumentSelectionDescriptor> getDocumentsSelected() {
		if (documentsSelected == null || documentsSelected.isEmpty()){
			return null;
		}
		return documentsSelected;
	}
	
	public List<DocSelection> getDocSelections() {
		return docSelections;
	}

	public void updateGui() {
		ps.setEnabled(pagesRadio.getSelection());
		
//		System.out.println("user name " + (Storage.getInstance().getUserName()));
//		System.out.println("Storage.getInstance().isAdminLoggedIn() " + Storage.getInstance().isAdminLoggedIn());
//		System.out.println("StorageUtil.getRoleOfUserInCurrentCollection() " + StorageUtil.getRoleOfUserInCurrentCollection());
//		System.out.println("role of user in current collection is admin? " + (StorageUtil.getRoleOfUserInCurrentCollection() == TrpRole.Admin));
//		try {
//			System.in.read();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		/*
		 * admin can start jobs for the whole collection
		 */
		boolean docSelectionAllowed = (Storage.getInstance().isAdminLoggedIn() && Storage.getInstance().isDocLoaded());
		docsLabel.setVisible(docSelectionAllowed);
		multipleDocsRadio.setVisible(docSelectionAllowed);
		docsSelectorBtn.setVisible(docSelectionAllowed);
		if (Storage.getInstance().isDocLoaded()){
			pagesRadio.setText("Pages ("+Storage.getInstance().getDoc().getPages().size()+"):");
		}
		if (docSelectionAllowed){
			docsSelectorBtn.setEnabled(multipleDocsRadio.getSelection());
			updateDocsNumberLabel();
		}
		docChooserExp.setExpanded(docSelectionAllowed);
		docChooserExp.setVisible(docSelectionAllowed);
		
		//to omit space when docChooser not visible
		((GridData) docChooserExp.getLayoutData()).exclude = !docSelectionAllowed;
	}
	
	public boolean isCurrentTranscript() {
		return currentTanscriptRadio.getSelection();
	}
	
	public Button getCurrentTranscriptButton() {
		return this.currentTanscriptRadio;
	}
	
	public boolean isDocsSelection() {
		return multipleDocsRadio != null && multipleDocsRadio.getSelection();
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
