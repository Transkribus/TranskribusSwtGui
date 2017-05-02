package eu.transkribus.swt_gui.search.documents;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ClientErrorException;
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
import eu.transkribus.swt.pagination_table.IPageLoadMethods;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.ComboInputDialog;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.LabeledCombo;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt_gui.collection_manager.CollectionManagerDialog;
import eu.transkribus.swt_gui.doc_overview.DocTableWidget;
import eu.transkribus.swt_gui.doc_overview.DocTableWidgetPagination;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class SimpleDocSearchComposite extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(SimpleDocSearchComposite.class);
	
	DocTableWidget docWidget;
	DocTableWidgetPagination docWidgetPaged;
	
	Label col;
	LabeledText documentId, title, description, author, writer;
	//LabeledCombo collection;
	Button exactMatch, caseSensitive;
	
	Button findBtn;
	Label infoLabel;
	
	CollectionManagerDialog colDialog;
	
	List<TrpDocMetadata> resultList;
	List<TrpDocMetadata> userDocs;
	
	Storage store = Storage.getInstance();
	
	int colId;
	int colIdToSearch;

	public SimpleDocSearchComposite(Composite parent, int style, int collectiondId, CollectionManagerDialog colDialog) {
		super(parent, style);
			try{
			
			colId = collectiondId;
			this.colDialog = colDialog;
			userDocs = store.getConnection().getAllDocsByUser(0, -1, null, null);
			createContents();
			//updateCollections();
			} catch (ServerErrorException | IllegalArgumentException | SessionExpiredException | ClientErrorException e) {
				TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
		}
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
		
		col = new Label(facetsC, SWT.None);
		
		if (colDialog.getDocTabFolder().getSelection().getText().equals("Documents in collection")){
			col.setText("Search in the current collection");
			colIdToSearch = colId;
		}
		else{
			col.setText("Search in all YOUR uploaded documents");
			colIdToSearch = -1;
			
		}

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
				
				//int colId = getColId();				
				Integer docid = getDocId();
				if (!documentId.txt().isEmpty() && docid == null) {
					return 0;
				}
								
				if (store.isLoggedIn()) {
					try {
						/* First we have to find all documents for the given search
						 * later restrict them to the documents owned by the user
						 * TODO: make database query to findDocuments for a single user
						 * 
						 */

						//store.getConnection().countFindDocuments(colIdToSearch, docid, title.txt(), description.txt(), author.txt(), writer.txt(), exactMatch.getSelection(), caseSensitive.getSelection());
						List<TrpDocMetadata> docList = store.getConnection().findDocuments(colIdToSearch, docid, title.txt(), description.txt(), author.txt(), writer.txt(), exactMatch.getSelection(), caseSensitive.getSelection(), -1, -1, null, null);

						resultList = new ArrayList<TrpDocMetadata>(docList);
						
						//table is Mydocuments
						if (colIdToSearch == -1){
							resultList = intersection(docList, userDocs);
						}
						
						N = resultList.size();
						logger.debug("found TOTAL docs: "+docList.size());

					} catch (ServerErrorException | IllegalArgumentException | SessionExpiredException | ClientErrorException e) {
						TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
					}
				}
				
				return N;
			}
			
			@Override public List<TrpDocMetadata> loadPage(int fromIndex, int toIndex, String sortPropertyName, String sortDirection) {
				List<TrpDocMetadata> docs = new ArrayList<>();
				
				//int colId = getColId();				
				Integer docid = getDocId();
				if (!documentId.txt().isEmpty() && docid == null) {
					return docs;
				}
				
				if (store.isLoggedIn()) {
					try {
						/* First we have to find all documents for the given search
						 * later restrict them to the documents owned by the user
						 * TODO: make database query to findDocuments for a single user
						 * 
						 */
						
						List<TrpDocMetadata> docList = store.getConnection().findDocuments(colIdToSearch, docid, title.txt(), description.txt(), author.txt(), writer.txt(), exactMatch.getSelection(), caseSensitive.getSelection(), -1, -1, sortPropertyName, sortDirection);

						resultList = new ArrayList<TrpDocMetadata>(docList);
						
						if (colIdToSearch == -1){
							resultList = intersection(docList, userDocs);
						}
						
						if (resultList != null && resultList.size() > 0){
							docs = resultList.subList(fromIndex, toIndex);//store.getConnection().findDocuments(colIdToSearch, docid, title.txt(), description.txt(), author.txt(), writer.txt(), exactMatch.getSelection(), caseSensitive.getSelection(), fromIndex, toIndex-fromIndex, sortPropertyName, sortDirection);
						}
						
						logger.debug("search docs pagesize = "+docs.size());
					} catch (ServerErrorException | IllegalArgumentException | SessionExpiredException | ClientErrorException e) {
						TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
					}
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
		
//		if (false) {
//		docWidget = new DocTableWidget(this, 0);
//		docWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//		docWidget.getTableViewer().addDoubleClickListener(openSelectedDocListener);
//		}
		
		sf.setWeights(new int[]{55, 45});
//		sf.setWeights(new int[]{facetsC.computeSize(SWT.DEFAULT, SWT.DEFAULT).y, docWidgetPaged.computeSize(SWT.DEFAULT, SWT.DEFAULT).y});

//		searchItem = createCTabItem(tabFolder, sf, "Search");

		
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
				//int colId = getColId();
				logger.debug("searching for docs, collId = "+colIdToSearch);
				
				Integer docid = getDocId();
				
				if (!documentId.txt().isEmpty() && docid == null) {
					infoLabel.setForeground(Colors.getSystemColor(SWT.COLOR_RED));
					infoLabel.setText("Invalid document id!");
					return;
				}
				
				docWidgetPaged.refreshPage(true);
				infoLabel.setForeground(Colors.getSystemColor(SWT.COLOR_DARK_GREEN));
				infoLabel.setText("Found "+docWidgetPaged.getPageableTable().getController().getTotalElements()+" documents!");
				
				if (docWidget != null) {
					List<TrpDocMetadata> docList = 
							s.getConnection().findDocuments(colIdToSearch, docid, title.txt(), description.txt(), author.txt(), writer.txt(), exactMatch.getSelection(), caseSensitive.getSelection(), 0, 0, null, null);
					logger.debug("found docs: "+docList.size());
					

	//				for (TrpDocMetadata doc : docList)
	//					logger.debug(doc.toString());
					
					
					//TODO: doclist contains all found docs in the database
					//But we want to have only the docs in the myDocs - find docs contained in these list
					//these are all docs from the user
					
					//search in all uploaded docs - total search 
					resultList = new ArrayList<TrpDocMetadata>(docList);
					
					if (colIdToSearch == -1){
						System.out.println("colToSearch");
						List<TrpDocMetadata> list2 = s.getConnection().getAllDocsByUser(0, -1, "DOCID", "down");
						System.out.println("list2 length " + list2.size());
						resultList = intersection(docList, list2);
					}
					
					//s.getConnection().getAllDocsByUser(0, -1, sortPropertyName, sortDirection);
					infoLabel.setForeground(Colors.getSystemColor(SWT.COLOR_DARK_GREEN));
					infoLabel.setText("Found "+resultList.size()+" documents!");

					docWidgetPaged.refreshList(-1, false);
					//docWidgetPaged.refreshPage(true);
				}
			} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e1) {
				logger.error(e1.getMessage(), e1);
				
				infoLabel.setForeground(Colors.getSystemColor(SWT.COLOR_RED));
				infoLabel.setText("Error: "+e1.getMessage());
			}
		}
	}
	
	
//	int getColId() {
//		String key = collection.combo.getText();
//		Object d = collection.combo.getData(key);
//		int collId = d==null ? 0 : ((TrpCollection) d).getColId();
//		
//		return collId;
//	}

	Integer getDocId() {
		try {
			return Integer.parseInt(documentId.txt().trim());
//			logger.debug("parsed docid = "+docid);
		} catch (Exception e) {
			return null;
		}
	}
	
	void openDocument(TrpDocMetadata md) {
		
//		TrpDocMetadata md = docWidgetPaged.getFirstSelected();
		
//		TrpDocMetadata md = docWidget.getSelectedDocument();
		
		if (md!=null) {
			logger.debug("md = "+md);
			
			int docId = md.getDocId();

			logger.debug("Loading doc with id: "+docId);
			
//			int colId = 0;
//			if (md.getColList().isEmpty()) {
//				DialogUtil.showMessageBox(getShell(), "Error loading document", 
//						"Collection list is empty - should not happen here!", SWT.ICON_ERROR);
//				
//				logger.error("Collection list is empty - should not happen here!");
//				return;
//			}
//			if (md.getColList().size() == 1)
//				colId = md.getColList().get(0).getColId();
//			else {
//				if (colIdToSearch == -1){
//					List<String> items = new ArrayList<>();
//					for (TrpCollection c : md.getColList()) {
//						items.add(c.getColName());
//					}
//					
//					ComboInputDialog cd = 
//							new ComboInputDialog(getShell(), "Select collection to load document from: ", items.toArray(new String[0]));
//					
//					if (cd.open() != IDialogConstants.OK_ID) {
//						return;
//					}
//	
//					logger.debug("selected index: "+cd.getSelectedIndex());
//					
//					TrpCollection coll = md.getColList().get(cd.getSelectedIndex());
//					colId = coll.getColId();
//				}
//			}
//			
//			logger.debug("loading from collection id: "+colId);

			colDialog.getCurrentDocTableWidgetPagination().loadPage("docId", docId, true);
		}
		
	}
	
    private List<TrpDocMetadata> intersection(List<TrpDocMetadata> list1, List<TrpDocMetadata> list2) {
        List<TrpDocMetadata> list = new ArrayList<TrpDocMetadata>();
        
        List<Integer> intList2 = new ArrayList<Integer>();
       // List<Integer> intList2 = new ArrayList<Integer>();
        
        for (TrpDocMetadata t : list2) {
        	intList2.add(t.getDocId());
        }

        for (TrpDocMetadata t : list1) {
        	System.out.println("docID in list1  " + t.getDocId());
        	System.out.println("title " + t.getTitle());
        	
            if(intList2.contains(t.getDocId())) {
                list.add(t);
                System.out.println("list element in intersection" + t);
            }
        }
        
        return list;
    }
}
