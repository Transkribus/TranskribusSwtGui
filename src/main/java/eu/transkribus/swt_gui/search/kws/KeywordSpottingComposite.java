package eu.transkribus.swt_gui.search.kws;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.KwsDocHit;
import eu.transkribus.core.model.beans.KwsHit;
import eu.transkribus.core.model.beans.KwsPageHit;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.LabeledCombo;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.structure_tree.StructureTreeWidget.ColConfig;

public class KeywordSpottingComposite extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(KeywordSpottingComposite.class);
	
	LabeledText kwsDocId;
	LabeledCombo kwsCollection;
	
	TreeViewer treeViewer;
	Tree tree;
	Button kwsBtn;
	LabeledText term;
	Slider confSlider;

	Label kwsInfoLabel;

	Text confValueTxt;
	
	public final static ColConfig TYPE_COL = new ColConfig("Type", 100);
	public final static ColConfig DOC_ID_COL = new ColConfig("Doc ID", 60);
	public final static ColConfig TITLE_COL = new ColConfig("Title", 200);
	public final static ColConfig PAGE_NR_COL = new ColConfig("Page Nr.", 100);
	public final static ColConfig HITS_COL = new ColConfig("Hits", 60);
	public final static ColConfig SCORE_COL = new ColConfig("Score", 100);
	public final static ColConfig LINE_ID_COL = new ColConfig("Line ID", 60);

	public final static ColConfig[] COLUMNS = new ColConfig[] { TYPE_COL, DOC_ID_COL, TITLE_COL, PAGE_NR_COL, HITS_COL, SCORE_COL, LINE_ID_COL };

	public KeywordSpottingComposite(Composite parent, int style) {
		super(parent, style);
		
		createContents();
		updateCollections();
	}
	
	private void createContents() {
		this.setLayout(new GridLayout(1, false));
		Composite kwsC = new Composite(this, 0);
		kwsC.setLayoutData(new GridData(GridData.FILL_BOTH));
		kwsC.setLayout(new GridLayout());
		
		TraverseListener tl2 = new TraverseListener() {
			@Override public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					spotKeyWords();
				}
			}
		};
		
		kwsCollection = new LabeledCombo(kwsC, "Restrict search to collection: ");
		kwsCollection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		kwsDocId = new LabeledText(kwsC, "Doc-ID: ");
		kwsDocId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		kwsDocId.text.addTraverseListener(tl2);
		
		term = new LabeledText(kwsC, "Search term: ");
		term.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		term.text.addTraverseListener(tl2);
		
		Composite sliderComp = new Composite(kwsC, SWT.NONE);
		sliderComp.setLayout(new GridLayout(3, false));
		sliderComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		Label sliderLabel = new Label(sliderComp, SWT.NONE);
		sliderLabel.setText("Confidence:");
		confValueTxt = new Text(sliderComp, SWT.NONE);
		confValueTxt.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		confValueTxt.setEnabled(false);
		confSlider = new Slider(sliderComp, SWT.HORIZONTAL);
		confSlider.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		confSlider.setMaximum(109);
		confSlider.setThumb(10);
		confSlider.setMinimum(0);
		confSlider.setSelection(50);

		confValueTxt.setText(""+confSlider.getSelection());
		
		confSlider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(e.detail == SWT.NONE){
					confValueTxt.setText(""+confSlider.getSelection());
				}
			}
		});
		
		treeViewer = new TreeViewer(kwsC, SWT.FULL_SELECTION | SWT.MULTI);
		treeViewer.setContentProvider(new KwsTreeContentProvider());
		treeViewer.getTree().setHeaderVisible(true);
		treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		tree = treeViewer.getTree();
		
		treeViewer.addDoubleClickListener(new IDoubleClickListener(){
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Object el = selection.getFirstElement();
				logger.debug("double click on element: "+el);
				TrpLocation loc;
				if (el instanceof KwsDocHit) {
					loc = new TrpLocation();
					KwsDocHit h = ((KwsDocHit)el);
					loc.collectionId = h.getColId();
					loc.docId = h.getDocId();
				} else if (el instanceof KwsPageHit) {
					loc = new TrpLocation();
					KwsPageHit h = ((KwsPageHit)el);
					loc.collectionId = h.getColId();
					loc.docId = h.getDocId();
					loc.pageNr = h.getPageNr();					
				} else if (el instanceof KwsHit){
					loc = new TrpLocation();
					KwsHit h = ((KwsHit)el);
					loc.collectionId = h.getColId();
					loc.docId = h.getDocId();
					loc.pageNr = h.getPageNr();	
					loc.shapeId = h.getLineId();
				} else {
					loc = null;
				}
				TrpMainWidget.getInstance().showLocation(loc);
			}
		});
				
		kwsBtn = new Button(kwsC, SWT.PUSH);
		kwsBtn.setText("Find Keywords");
		kwsBtn.setImage(Images.getOrLoad("/icons/find.png"));
		
		kwsBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				spotKeyWords();
			}
		});
		
		kwsInfoLabel = new Label(kwsC, 0);
		kwsInfoLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		initCols();
	}
	
	private void initCols() {
		for (ColConfig cf : COLUMNS) {
			TreeViewerColumn column = new TreeViewerColumn(treeViewer, SWT.SINGLE);
			column.getColumn().setText(cf.name);
			column.getColumn().setWidth(cf.colSize);
			column.setLabelProvider(new KwsTreeLabelProvider());
		}
	}

	void updateCollections() {
		Storage s = Storage.getInstance();
		
		List<String> items = new ArrayList<>();
		
		for (TrpCollection c : s.getCollections()) {
			String key = c.getColId()+" - "+c.getColName();
			kwsCollection.combo.setData(key, c);
			items.add(key);
		}
		kwsCollection.combo.setItems(items.toArray(new String[0]));
		kwsCollection.combo.select(0);
		
		items.add(0, "");	
	}
	
	void spotKeyWords() {
		Storage s = Storage.getInstance();
		if (s.isLoggedIn()) {
			try {				
				int colId = getKwsColId();
				logger.debug("searching for docs, collId = "+colId);
				
				Integer docid = getKwsDocId();
				
				if (!kwsDocId.txt().isEmpty() && docid == null) {
					kwsInfoLabel.setForeground(Colors.getSystemColor(SWT.COLOR_RED));
					kwsInfoLabel.setText("Invalid document id!");
					return;
				}
				
//				docWidgetPaged.refreshPage(true);
//				infoLabel.setForeground(Colors.getSystemColor(SWT.COLOR_DARK_GREEN));
//				infoLabel.setText("Found "+docWidgetPaged.getPageableTable().getController().getTotalElements()+" documents!");
				
//				if (docWidget != null) {
				List<KwsDocHit> docList = s.getConnection().doKwsSearch(colId, docid, term.txt(), confSlider.getSelection());
					
					logger.debug("found docs: "+docList.size());
					
//					infoLabel.setForeground(Colors.getSystemColor(SWT.COLOR_DARK_GREEN));
//					infoLabel.setText("Found "+docList.size()+" documents!");
	//				for (TrpDocMetadata doc : docList)
	//					logger.debug(doc.toString());
					treeViewer.setInput(docList);
					
//					docWidget.refreshList(docList);
//				}
					
			} catch(ClientErrorException cee){
				logger.error(cee.getMessage(), cee);
				treeViewer.setInput(new ArrayList<KwsDocHit>(0));
			} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e1) {
				logger.error(e1.getMessage(), e1);
				
				kwsInfoLabel.setForeground(Colors.getSystemColor(SWT.COLOR_RED));
				kwsInfoLabel.setText("Error: "+e1.getMessage());
			}
		}
	}
	
	Integer getKwsDocId() {
		try {
			return Integer.parseInt(kwsDocId.txt().trim());
//			logger.debug("parsed docid = "+docid);
		} catch (Exception e) {
			return null;
		}
	}
	
	int getKwsColId() {
		String key = kwsCollection.combo.getText();
		Object d = kwsCollection.combo.getData(key);
		int collId = d==null ? 0 : ((TrpCollection) d).getColId();
		
		return collId;
	}

}
