package eu.transkribus.swt_gui.search.kws;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class KeywordSpottingComposite extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(KeywordSpottingComposite.class);
	Storage store;
	
	Combo scopeCombo;
	Group queryGroup, resultGroup;
	Composite queryComp;
	Button searchBtn;
	
	KwsResultTableWidget resultTable;
	
	List<QueryWidget> queryWidgets;
	
	protected static final String SCOPE_DOC = "Current document";
	protected static final String SCOPE_COLL = "Current collection";
	
	String[] SCOPES = new String[] { SCOPE_COLL, SCOPE_DOC };
	
	public KeywordSpottingComposite(Composite parent, int style) {
		super(parent, style);
		store = Storage.getInstance();
		queryWidgets = new ArrayList<>();
		createContents();
	}
	
	private void createContents() {
		this.setLayout(new GridLayout(1, false));
		Composite kwsC = new Composite(this, 0);
		kwsC.setLayoutData(new GridData(GridData.FILL_BOTH));
		kwsC.setLayout(new GridLayout(2, false));
		
		Label scopeLbl = new Label(kwsC, SWT.NONE);
		scopeLbl.setText("Search in:");
		scopeCombo = new Combo(kwsC, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		scopeCombo.setItems(SCOPES);
		//FIXME Java Heap space error when to many confmats are loaded. Thus for now only scope "document"
		scopeCombo.select(1);
		scopeCombo.setEnabled(false);
		
		GridLayout groupLayout = new GridLayout(1, false);
		GridData groupGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		
		queryGroup = new Group(kwsC, SWT.NONE);
		queryGroup.setText("Queries");
		queryGroup.setLayout(groupLayout);
		queryGroup.setLayoutData(groupGridData);
		
		queryComp = new Composite(queryGroup, SWT.NONE);
		queryComp.setLayout(groupLayout);
		queryComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		addQueryWidget();
		
		searchBtn = new Button(queryGroup, SWT.PUSH);
		searchBtn.setText("Search");
		searchBtn.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 2, 1));
		searchBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				startKws();
			}
		});	
		
		resultGroup = new Group(kwsC, SWT.NONE);
		resultGroup.setText("Search Results");
		resultGroup.setLayout(groupLayout);
		resultGroup.setLayoutData(groupGridData);
		
		resultTable = new KwsResultTableWidget(resultGroup, SWT.BORDER);
		resultTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		updateKwsResults();
		
//		treeViewer.addDoubleClickListener(new IDoubleClickListener(){
//			@Override
//			public void doubleClick(DoubleClickEvent event) {
//				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
//				Object el = selection.getFirstElement();
//				logger.debug("double click on element: "+el);
//				TrpLocation loc;
//				if (el instanceof KwsDocHit) {
//					loc = new TrpLocation();
//					KwsDocHit h = ((KwsDocHit)el);
//					loc.collectionId = h.getColId();
//					loc.docId = h.getDocId();
//				} else if (el instanceof KwsPageHit) {
//					loc = new TrpLocation();
//					KwsPageHit h = ((KwsPageHit)el);
//					loc.collectionId = h.getColId();
//					loc.docId = h.getDocId();
//					loc.pageNr = h.getPageNr();					
//				} else if (el instanceof KwsHit){
//					loc = new TrpLocation();
//					KwsHit h = ((KwsHit)el);
//					loc.collectionId = h.getColId();
//					loc.docId = h.getDocId();
//					loc.pageNr = h.getPageNr();	
//					loc.shapeId = h.getLineId();
//				} else {
//					loc = null;
//				}
//				TrpMainWidget.getInstance().showLocation(loc);
//			}
//		});
		
//		initCols();
	}
	
//	private void initCols() {
//		for (ColConfig cf : COLUMNS) {
//			TreeViewerColumn column = new TreeViewerColumn(treeViewer, SWT.SINGLE);
//			column.getColumn().setText(cf.name);
//			column.getColumn().setWidth(cf.colSize);
//			column.setLabelProvider(new KwsTreeLabelProvider());
//		}
//	}
	
	protected void startKws() {
		List<String> queries = getQueries();
		if(queries.isEmpty()) {
			DialogUtil.showErrorMessageBox(getShell(), "No keywords given", "Please enter at least one keyword.");
			return;
		}
		
		final String scope = scopeCombo.getText();
		logger.debug("searching on scope: "+scope);
		TrpMainWidget mw = TrpMainWidget.getInstance();
		final TrpCollection currCol =  mw.getUi().getServerWidget().getSelectedCollection();
		boolean isValidScope = scope.equals(SCOPE_COLL) && currCol == null 
				|| (scope.equals(SCOPE_DOC) && !store.isLocalDoc());
		
		if (!store.isLoggedIn() || !isValidScope) {
			DialogUtil.showErrorMessageBox(getShell(), "Not logged in", "Keyword Spotting is only available for online documents.");
			return;
		}
		
		final int colId = currCol.getColId();
		final String colName = currCol.getColName();
		final int docId = store.getDocId();
		final String docTitle = store.getDoc().getMd().getTitle();
		final String queryOverviewStr = "\"" + StringUtils.join(queries, "\"\n\t\"") + "\"";
		
		final String message = "You are about to start a Keyword Spotting job:\n\n" +
					"\tCollection: \"" + colName + "\" (ID = " + colId + ")\n" +
					"\tDocument: \"" + docTitle + "\" (ID = " + docId + ")\n" +
					"\tKeywords:\n\t" + queryOverviewStr +
					"\n\nStart the process?";
		
		int ret = DialogUtil.showYesNoDialog(this.getShell(), "Start Keyword Spotting?", message);
		if(ret == SWT.YES) {
			logger.debug("OK. Starting job.");
			try {
				store.getConnection().doCITlabKwsSearch(colId, docId, queries);
				updateKwsResults();
			} catch (SessionExpiredException | ServerErrorException | ClientErrorException
					| IllegalArgumentException e) {
				DialogUtil.showErrorMessageBox(getShell(), "Something went wrong.", e.getMessage());
				return;
			}
		}
	}

	private List<String> getQueries() {
		List<String> queries = new ArrayList<>(queryWidgets.size()-1);
		for(QueryWidget qw : queryWidgets) {
			final String q = qw.getQuery();
			if(!StringUtils.isEmpty(q)) {
				queries.add(qw.getQuery());
			}
		}
		return queries;
	}

	private void addQueryWidget() {
		final int index = queryWidgets.size();
		final QueryWidget qw = new QueryWidget(queryComp, index, SWT.NONE);
		qw.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		queryWidgets.add(qw);
		if(index > 0) {
			qw.getRemoveBtn().addSelectionListener(new SelectionAdapter() {
				@Override public void widgetSelected(SelectionEvent e) {
					queryWidgets.removeIf(q -> q.getIndex() == index);
					updateQueryGroup();
					qw.dispose();
					queryComp.layout();
					queryGroup.layout();
				}
			});
		}
		qw.getText().addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				//DO nothing
			}

			@Override
			public void keyReleased(KeyEvent e) {
				final String text = qw.getText().getText();
				if(!StringUtils.isEmpty(text) && isLast(qw)) {
					addQueryWidget();
				}
			}
			
		});
		updateRemoveBtnVisibility();
		queryComp.layout();
		queryGroup.layout();
	}

	private void updateRemoveBtnVisibility() {
		queryWidgets.stream().forEach(q -> q.setRemoveBtnVisible(!isFirst(q) && !isLast(q)));
	}

	private void updateQueryGroup() {
		for(int i = 0; i < queryWidgets.size(); i++) {
			logger.debug("index = " + i + " | #queryWidgets = " + queryWidgets.size());
			QueryWidget qw = queryWidgets.get(i);
			qw.setIndex(i);
		}
		updateRemoveBtnVisibility();
	}
	
	private boolean isFirst(QueryWidget qw) {
		return queryWidgets.isEmpty() || qw.getIndex() == 0;
	}
	
	private boolean isLast(QueryWidget qw) {
		return queryWidgets.isEmpty() || qw.getIndex() == queryWidgets.size()-1;
	}
	
	private void updateKwsResults() {
		Runnable loader = new Runnable() {
			@Override
			public void run() {
				logger.debug("refreshing kws results...");
				List<TrpKwsResultTableEntry> kwsList = new LinkedList<>();
				if (store != null && store.isLoggedIn()) {
					List<TrpJobStatus> jobs;
					try {
						jobs = store.getConnection().getJobs(false, null, "CITlab Keyword Spotting", null, 0, 0, null, null);
						logger.debug("got finished kws jobs: "+jobs.size());
					} catch (SessionExpiredException | ServerErrorException | ClientErrorException
							| IllegalArgumentException e) {
						logger.error(e.getMessage(), e);
						jobs = new ArrayList<>(0);
					}
					for(TrpJobStatus j : jobs) {
						if(!j.isFailed()) {
							kwsList.add(new TrpKwsResultTableEntry(j));
						}
					}
				}
				Display.getDefault().asyncExec(() -> {
					resultTable.getTableViewer().setInput(kwsList);
				});
			}
		};
		new Thread(loader).start();
	}
	
	private class QueryWidget extends Composite {
		private static final String LBL_TXT = "Keyword ";
		private int index;
		
		Label queryLbl;
		Text queryTxt;
		Button removeBtn;

		public QueryWidget(Composite parent, int index, int style) {
			super(parent, style);
			this.setLayout(new GridLayout(3, false));
			this.index = index;
			queryLbl = new Label(this, SWT.NONE);
			queryTxt = new Text(this, SWT.BORDER);
			queryTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			removeBtn = new Button(this, SWT.PUSH);
			removeBtn.setText("X");
			updateLblTxt();
			this.layout();
		}
		
		private void updateLblTxt() {
			if(!queryLbl.isDisposed()) {
				queryLbl.setText(LBL_TXT + (this.index+1));
			}
		}
		
		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
			updateLblTxt();
		}
		
		public String getQuery() {
			return queryTxt.getText();
		}
		
		public Button getRemoveBtn() {
			return removeBtn;
		}
		public Text getText() {
			return queryTxt;
		}
		public void setRemoveBtnVisible(boolean visible) {
			removeBtn.setVisible(visible);
		}
	}
}
