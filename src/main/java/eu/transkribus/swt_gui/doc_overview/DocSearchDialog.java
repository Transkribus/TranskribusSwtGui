package eu.transkribus.swt_gui.doc_overview;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ServerErrorException;

import org.dea.swt.pagination_table.IPageLoadMethods;
import org.dea.swt.util.Colors;
import org.dea.swt.util.ComboInputDialog;
import org.dea.swt.util.DialogUtil;
import org.dea.swt.util.Images;
import org.dea.swt.util.LabeledCombo;
import org.dea.swt.util.LabeledText;
import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.pagination_tables.DocTableWidgetPagination;

public class DocSearchDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(DocSearchDialog.class);
	
	DocTableWidget docWidget;
	DocTableWidgetPagination docWidgetPaged;
	
	LabeledText documentId, title, description, author, writer;
	LabeledCombo collection;
	Button exactMatch, caseSensitive;
	
	Button findBtn;
	Label infoLabel;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public DocSearchDialog(Shell parentShell) {
		super(parentShell);
		
		setShellStyle(SWT.SHELL_TRIM | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
		setBlockOnOpen(false);
	}
	
	@Override protected boolean isResizable() {
		return true;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);
		c.setLayout(new FillLayout());
		
		SashForm sf = new SashForm(c, SWT.VERTICAL);
		sf.setLayout(new GridLayout());
		
		Composite facetsC = new Composite(sf, 0);
		facetsC.setLayoutData(new GridData(GridData.FILL_BOTH));
		facetsC.setLayout(new GridLayout());
		
		TraverseListener tl = new TraverseListener() {
			@Override public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					searchThemFuckingDocuments();
				}
			}
		};
		
		collection = new LabeledCombo(facetsC, "Restrict search to collection: ");
		collection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		updateCollections();
		
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
				searchThemFuckingDocuments();
				

			}
		});
		
		infoLabel = new Label(facetsC, 0);
		infoLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				
		docWidgetPaged = new DocTableWidgetPagination(sf, 0, 50, new IPageLoadMethods<TrpDocMetadata>() {
			Storage store = Storage.getInstance();
			
			@Override public int loadTotalSize() {
				int N = 0;
				
				int colId = getColId();				
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
		
		if (false) {
		docWidget = new DocTableWidget(c, 0);
		docWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		docWidget.getTableViewer().addDoubleClickListener(openSelectedDocListener);
		}
		
		sf.setWeights(new int[]{55, 45});
//		sf.setWeights(new int[]{facetsC.computeSize(SWT.DEFAULT, SWT.DEFAULT).y, docWidgetPaged.computeSize(SWT.DEFAULT, SWT.DEFAULT).y});

		return c;
	}
	
	void openDocument(TrpDocMetadata md) {
		
//		TrpDocMetadata md = docWidgetPaged.getFirstSelected();
		
//		TrpDocMetadata md = docWidget.getSelectedDocument();
		
		if (md!=null) {
			logger.debug("md = "+md);
			
			int docId = md.getDocId();
			logger.debug("Loading doc with id: "+docId);
			
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
			
			TrpMainWidget mw = TrpMainWidget.getInstance(); 
			// select collection in DocOverviewWidget
//			mw.getUi().getDocOverviewWidget().
			
			mw.getUi().getDocOverviewWidget().clearCollectionFilter();
			mw.getUi().getDocOverviewWidget().setSelectedCollection(colId, true);
			
			// select page of document in doc-table:
			mw.getUi().getDocOverviewWidget().getDocTableWidget().loadPage("docId", docId, false);

			TrpMainWidget.getInstance().loadRemoteDoc(docId, colId);
		}
		
	}
	
	void updateCollections() {
		Storage s = Storage.getInstance();
		
		List<String> items = new ArrayList<>();
		
		items.add("");
		collection.combo.setData("", null);
		
		for (TrpCollection c : s.getCollections()) {
			String key = c.getColId()+" - "+c.getColName();
			collection.combo.setData(key, c);
			items.add(key);
		}
		
		collection.combo.setItems(items.toArray(new String[0]));
		collection.combo.select(0);
	}
	
	Integer getDocId() {
		try {
			return Integer.parseInt(documentId.txt().trim());
//			logger.debug("parsed docid = "+docid);
		} catch (Exception e) {
			return null;
		}
	}
	
	int getColId() {
		String key = collection.combo.getText();
		Object d = collection.combo.getData(key);
		int collId = d==null ? 0 : ((TrpCollection) d).getColId();
		
		return collId;
	}
	
	void searchThemFuckingDocuments() {
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
				
				if (docWidget != null) {
					List<TrpDocMetadata> docList = 
							s.getConnection().findDocuments(colId, docid, title.txt(), description.txt(), author.txt(), writer.txt(), exactMatch.getSelection(), caseSensitive.getSelection(), 0, 0, null, null);
					logger.debug("found docs: "+docList.size());
					
					infoLabel.setForeground(Colors.getSystemColor(SWT.COLOR_DARK_GREEN));
					infoLabel.setText("Found "+docList.size()+" documents!");
	//				for (TrpDocMetadata doc : docList)
	//					logger.debug(doc.toString());
					
					
					docWidget.refreshList(docList);
				}
			} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e1) {
				logger.error(e1.getMessage(), e1);
				
				infoLabel.setForeground(Colors.getSystemColor(SWT.COLOR_RED));
				infoLabel.setText("Error: "+e1.getMessage());
			}
		}
	}
	
	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override protected void createButtonsForButtonBar(Composite parent) {
//		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override protected Point getInitialSize() {
		return new Point(800, 800);
	}

}
