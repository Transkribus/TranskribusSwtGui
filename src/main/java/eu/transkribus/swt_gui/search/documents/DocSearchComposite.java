package eu.transkribus.swt_gui.search.documents;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.auth.TrpUser;
import eu.transkribus.swt.pagination_table.IPageLoadMethods;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.ComboInputDialog;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt_gui.doc_overview.DocTableWidgetPagination;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class DocSearchComposite extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(DocSearchComposite.class);
	
//	DocTableWidget docWidget;
	DocTableWidgetPagination docWidgetPaged;
	
	public LabeledText collectionId, documentId, title, description, author, writer; 
	//, uploader;
//	LabeledCombo collection;
	Button collectionCheck;
	Button exactMatch, caseSensitive;
	
	Button findBtn;
	Label infoLabel;

	public DocSearchComposite(Composite parent, int style) {
		super(parent, style);
		createContents();
//		updateCollections();
	}
	
	private void createContents() {
		this.setLayout(new FillLayout());
		SashForm sf = new SashForm(this, SWT.VERTICAL);
//		sf.setLayoutData(new GridData(GridData.FILL_BOTH));
		sf.setLayout(new GridLayout());
		
		Composite facetsC = new Composite(sf, 0);
		facetsC.setLayoutData(new GridData(GridData.FILL_BOTH));
		facetsC.setLayout(new GridLayout());
		
		TraverseListener tl = new TraverseListener() {
			@Override public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					searchDocuments();
				}
			}
		};
		
//		collection = new LabeledCombo(facetsC, "Restrict search to collection: ");
//		collection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		collectionCheck = new Button(facetsC, SWT.CHECK);
		collectionCheck.setText("Restrict search to current collection only");
		collectionCheck.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		// enable / disable collection field
		collectionCheck.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				if (collectionCheck.getSelection()) {
					collectionId.setText(getColId().toString());
					collectionId.text.setEditable(false);
				} else {
					collectionId.text.setEditable(true);
				} 
					
			}
		});
		
		collectionId = new LabeledText(facetsC, "Col-ID: ");
		collectionId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		collectionId.text.addTraverseListener(tl);
		
		documentId = new LabeledText(facetsC, "Doc-ID: ");
		documentId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		documentId.text.addTraverseListener(tl);
		
		title = new LabeledText(facetsC, "Title: ");
		title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		title.text.addTraverseListener(tl);
		
		description = new LabeledText(facetsC, "Description");
		description.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		description.text.addTraverseListener(tl);
		
		author = new LabeledText(facetsC, "Author: ");
		author.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		author.text.addTraverseListener(tl);
		
		writer = new LabeledText(facetsC, "Writer: ");
		writer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		writer.text.addTraverseListener(tl);
		
//		uploader = new LabeledText(facetsC, "Uploaded by: ");
//		uploader.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//		uploader.text.addTraverseListener(tl);
		
		exactMatch = new Button(facetsC, SWT.CHECK);
		exactMatch.setText("Exact match of keywords ");
		
		caseSensitive = new Button(facetsC, SWT.CHECK);
		caseSensitive.setText("Case sensitve search ");		
		
		findBtn = new Button(facetsC, SWT.PUSH);
		findBtn.setText("Find Documents");
		findBtn.setImage(Images.getOrLoad("/icons/find.png"));
		
		findBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				searchDocuments();
			}
		});
		
		infoLabel = new Label(facetsC, 0);
		infoLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				
		docWidgetPaged = new DocTableWidgetPagination(sf, 0, 50, new IPageLoadMethods<TrpDocMetadata>() {
			Storage store = Storage.getInstance();
			
			@Override public int loadTotalSize() {
				int N = 0;
				
				Integer colId = getColId();				
				Integer docid = getDocId();
				if (!documentId.txt().isEmpty() && docid == null) {
					return 0;
				}
								
				if (store.isLoggedIn()) {
					try {
						N = store.getConnection().countFindDocuments(colId, docid, title.txt(), description.txt(), author.txt(), writer.txt(), exactMatch.getSelection(), caseSensitive.getSelection());
						logger.debug("N search docs = "+N);
					} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
						TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
					}
				}
				
				return N;
			}
			
			@Override public List<TrpDocMetadata> loadPage(int fromIndex, int toIndex, String sortPropertyName, String sortDirection) {
				List<TrpDocMetadata> docs = new ArrayList<>();
				List<TrpDocMetadata> userdocs = new ArrayList<>();
				
				int colId = getColId();				
				Integer docid = getDocId();
				if (!documentId.txt().isEmpty() && docid == null) {
					return docs;
				}
				
				if (store.isLoggedIn()) {
					try {
						docs = store.getConnection().findDocuments(colId, docid, title.txt(), description.txt(), author.txt(), writer.txt(), exactMatch.getSelection(), caseSensitive.getSelection(), fromIndex, toIndex-fromIndex, sortPropertyName, sortDirection);
						logger.debug("search docs pagesize = "+docs.size());
					} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
						TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
					}
					
					/// TODO: get user id from email / name
//					try {
//						userdocs = store.getConnection().getAllDocsByUser(0, -1, null, null);
//						System.out.println("found "+userdocs.size());
//						logger.debug("search user pagesize = "+userdocs.size());
//					} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e){
//						TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
//					}
//					List<TrpUser> users = store.getConnection().findUsers(userNameText.getText(), firstNameText.getText(), lastNameText.getText(), exactMatch, caseSensitive);
//					docs = store.getConnection().getAllDocsByUser(index, nValues, sortPropertyName, sortDirection);
				}
				
				return docs;
			}
		});
				
		docWidgetPaged.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		IDoubleClickListener openSelectedDocListener = new IDoubleClickListener() {
			@Override public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (sel.isEmpty())
					return;
				
				openDocument((TrpDocMetadata) sel.getFirstElement());
			}
		};		
		docWidgetPaged.getTableViewer().addDoubleClickListener(openSelectedDocListener);
		
		sf.setWeights(new int[]{55, 45});
	}
	
//	void updateCollections() {
//		Storage s = Storage.getInstance();
//		
//		List<String> items = new ArrayList<>();
//		
//		for (TrpCollection c : s.getCollections()) {
//			String key = c.getColId()+" - "+c.getColName();
//			collection.combo.setData(key, c);
//			items.add(key);
//		}
//		
//		items.add(0, "");
//		collection.combo.setData("", null);
//		
//		collection.combo.setItems(items.toArray(new String[0]));
//		collection.combo.select(0);		
//	}
	
	void searchDocuments() {
		Storage s = Storage.getInstance();
		if (s.isLoggedIn()) {
			try {	
				
				int colId = getColId();
				logger.debug("searching for docs, collId = "+colId);
				
				Integer docid = getDocId();
				
				if (!documentId.txt().isEmpty() && docid == null) {
					infoLabel.setForeground(Colors.getSystemColor(SWT.COLOR_RED));
					infoLabel.setText("Invalid document id!");
					return;
				}
				
				docWidgetPaged.refreshPage(true);
				infoLabel.setForeground(Colors.getSystemColor(SWT.COLOR_DARK_GREEN));
				infoLabel.setText("Found "+docWidgetPaged.getPageableTable().getController().getTotalElements()+" documents!");
				
//				if (docWidget != null) {
//					List<TrpDocMetadata> docList = 
//							s.getConnection().findDocuments(colId, docid, title.txt(), description.txt(), author.txt(), writer.txt(), exactMatch.getSelection(), caseSensitive.getSelection(), 0, 0, null, null);
//					logger.debug("found docs: "+docList.size());
//					
//					infoLabel.setForeground(Colors.getSystemColor(SWT.COLOR_DARK_GREEN));
//					infoLabel.setText("Found "+docList.size()+" documents!");
//	//				for (TrpDocMetadata doc : docList)
//	//					logger.debug(doc.toString());
//					
//					
//					docWidget.refreshList(docList);
//				}
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
				
				infoLabel.setForeground(Colors.getSystemColor(SWT.COLOR_RED));
				infoLabel.setText("Error: "+e1.getMessage());
			}
		}
	}
	
	Integer getColId() {
		Integer id = new Integer(0);
		try {
			if (collectionCheck.getSelection()) {
				id = TrpMainWidget.getInstance().getSelectedCollectionId(); 
			} else {
				String collectionIdTxt = collectionId.txt().trim();
				if (!collectionIdTxt.isEmpty()) 
					id = Integer.parseInt(collectionIdTxt);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			
			infoLabel.setForeground(Colors.getSystemColor(SWT.COLOR_RED));
			infoLabel.setText("Could not retrieve current collection");
			return 0;
		}
		return id;
	}

	Integer getDocId() {
		try {
			return Integer.parseInt(documentId.txt().trim());
//			logger.debug("parsed docid = "+docid);
		} catch (Exception e) {
			return null;
		}
	}
	
	void openDocument(TrpDocMetadata md) {
		logger.debug("openDocument: "+md);
		if (md == null)
			return;

		int docId = md.getDocId();		
		int colId = 0;
		if (md.getColList().isEmpty()) {
			DialogUtil.showMessageBox(getShell(), "Error loading document", 
					"Collection list is empty - should not happen here!", SWT.ICON_ERROR);
			
			logger.error("Collection list is empty - should not happen here!");
			return;
		}
		
		if (md.getColList().size() == 1)
			colId = md.getColList().get(0).getColId();
		else {
			List<String> items = new ArrayList<>();
			for (TrpCollection c : md.getColList()) {
				items.add(c.getColName());
			}
			
			ComboInputDialog cd = 
					new ComboInputDialog(getShell(), "Select collection to load document from: ", items.toArray(new String[0]));
			
			if (cd.open() != IDialogConstants.OK_ID) {
				return;
			}

			logger.debug("selected index: "+cd.getSelectedIndex());
			
			TrpCollection coll = md.getColList().get(cd.getSelectedIndex());
			colId = coll.getColId();
		}
		
		logger.debug("loading from collection id: "+colId);
		
		TrpMainWidget.getInstance().loadRemoteDoc(docId, colId);
	}
}
