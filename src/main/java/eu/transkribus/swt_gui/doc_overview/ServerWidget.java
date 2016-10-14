package eu.transkribus.swt_gui.doc_overview;

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.collection_comboviewer.CollectionComboViewerWidget;
import eu.transkribus.swt_gui.doclist_widgets.DocTableWidgetPagination;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.util.RecentDocsComboViewerWidget;

public class ServerWidget extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(ServerWidget.class);

	Label usernameLabel, serverLabel;
	DocTableWidgetPagination docTableWidget;

	CollectionComboViewerWidget collectionComboViewerWidget;
	RecentDocsComboViewerWidget recentDocsComboViewerWidget;
	
	Button manageCollectionsBtn;
	Button showActivityWidgetBtn;
	Text quickLoadByID;
			
	Storage store = Storage.getInstance();
	
	ExpandableComposite adminAreaExp;
	ExpandableComposite lastDocsAreaExp;
	ExpandableComposite remotedocsgroupExp;
	Composite container;
	
	Button syncWithLocalDocBtn, applyAffineTransformBtn, batchReplaceImgsBtn;
		
	int selectedId=-1;
	
	Button showJobsBtn, showVersionsBtn;
	
	ServerWidgetListener serverWidgetListener;
		
	public ServerWidget(Composite parent) {
		super(parent, SWT.NONE);
				
		init();
		addListener();
		updateLoggedIn();
	}
	
	private void addListener() {
		serverWidgetListener = new ServerWidgetListener(this);
	}
	
	void updateLoggedIn() {
		boolean isLoggedIn = store.isLoggedIn();
		
		for (Control c : this.getChildren()) {
			c.setEnabled(isLoggedIn);
		}
	}
	
	private void init() {
		this.setLayout(new GridLayout());
				
		container = new Composite(this, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		container.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		
		usernameLabel = new Label(container, SWT.NONE);
		usernameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		Fonts.setBoldFont(usernameLabel);
		
		serverLabel = new Label(container, SWT.NONE);
		serverLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		Composite btns1 = new Composite(container, 0);
		btns1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btns1.setLayout(new GridLayout(2, true));
		
		showJobsBtn = new Button(btns1, 0);
		showJobsBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		showJobsBtn.setText("Jobs on server...");
		showJobsBtn.setImage(Images.CUP);
		
		showVersionsBtn = new Button(btns1, 0);
		showVersionsBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		showVersionsBtn.setText("Versions of page...");
		showVersionsBtn.setImage(Images.PAGE_WHITE_STACK);

		manageCollectionsBtn = new Button(btns1, SWT.PUSH);
		manageCollectionsBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		manageCollectionsBtn.setImage(Images.getOrLoad("/icons/user_edit.png"));
		manageCollectionsBtn.setText("Manage collections...");
		
		showActivityWidgetBtn = new Button(btns1, SWT.PUSH);
		showActivityWidgetBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		showActivityWidgetBtn.setImage(Images.GROUP);
		showActivityWidgetBtn.setText("User activity...");
				
		// admin area:
		adminAreaExp = new ExpandableComposite(container, ExpandableComposite.COMPACT);
		Fonts.setBoldFont(adminAreaExp);
		Composite adminAreaComp = new Composite(adminAreaExp, SWT.SHADOW_ETCHED_IN);
		adminAreaComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		adminAreaComp.setLayout(new GridLayout(1, false));
		syncWithLocalDocBtn = new Button(adminAreaComp, SWT.PUSH);
		syncWithLocalDocBtn.setText("Sync with local doc");
		
		applyAffineTransformBtn = new Button(adminAreaComp, SWT.PUSH);
		applyAffineTransformBtn.setText("Apply affine transformation");
		
		batchReplaceImgsBtn = new Button(adminAreaComp, SWT.PUSH);
		batchReplaceImgsBtn.setText("Batch replace images");
		
		configExpandable(adminAreaExp, adminAreaComp, "Admin area", container, false);
		adminAreaExp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		/////////////////		
		lastDocsAreaExp = new ExpandableComposite(container, ExpandableComposite.COMPACT);
		Fonts.setBoldFont(lastDocsAreaExp);
		Composite lastDocsAreaComp = new Composite(lastDocsAreaExp, SWT.SHADOW_ETCHED_IN);
		lastDocsAreaComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		lastDocsAreaComp.setLayout(new GridLayout(1, false));
		
		recentDocsComboViewerWidget = new RecentDocsComboViewerWidget(lastDocsAreaComp, 0);
		recentDocsComboViewerWidget.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		configExpandable(lastDocsAreaExp, lastDocsAreaComp, "Recent Documents", container, true);
		lastDocsAreaExp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		
		////////////////
		remotedocsgroupExp = new ExpandableComposite(container, ExpandableComposite.COMPACT);
		Fonts.setBoldFont(remotedocsgroupExp);
		Composite remotedocsgroup = new Composite(remotedocsgroupExp, 0); // orig-parent = container
		
		remotedocsgroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		remotedocsgroup.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
			
		configExpandable(remotedocsgroupExp, remotedocsgroup, "Server documents", container, true);
		remotedocsgroupExp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		collectionComboViewerWidget = new CollectionComboViewerWidget(remotedocsgroup, 0);
		collectionComboViewerWidget.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		Composite docsContainer = new Composite(remotedocsgroup, 0);
		docsContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		docsContainer.setLayout(new GridLayout(2, false));
		docsContainer.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));

		docTableWidget = new DocTableWidgetPagination(docsContainer, 0, 25);
		docTableWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		ColumnViewerToolTipSupport.enableFor(docTableWidget.getPageableTable().getViewer(), ToolTip.NO_RECREATE);
		docTableWidget.getPageableTable().setToolTipText("");
		
		if (remotedocsgroup instanceof SashForm) {
			((SashForm)remotedocsgroup).setWeights(new int[]{50, 50});
		}
		
		updateHighlightedRow(-1);
		
		setAdminAreaVisible(false);
	}
	
	public void setAdminAreaVisible(boolean visible) {
		adminAreaExp.setParent(visible ? container : SWTUtil.dummyShell);
		adminAreaExp.moveAbove(remotedocsgroupExp);
		container.layout();
	}
	
	private void configExpandable(ExpandableComposite exp, Composite client, String text, final Composite container, boolean expand) {
		exp.setClient(client);
		exp.setText(text);
		exp.addExpansionListener(new ExpansionAdapter() {
		      public void expansionStateChanged(ExpansionEvent e) {
		    	  container.layout();		    	  
		      }
		    });
		exp.setExpanded(expand);
	}
	
	public void updateHighlightedRow(int selectedId) {
		docTableWidget.getTableViewer().refresh();
	}
	
	public void refreshDocList() {
		docTableWidget.refreshList(getSelectedCollectionId(), true);
	}
	
	public void clearDocList() {
		docTableWidget.refreshList(0, true);
	}
	
	public DocTableWidgetPagination getDocTableWidget() { return docTableWidget; }
	
	public TableViewer getTableViewer() { return docTableWidget.getTableViewer(); }
	public Label getServerLabel() { return serverLabel; }
	public Label getUsernameLabel() { return usernameLabel; }
		
	public TrpDocMetadata getSelectedDocument() {
		return docTableWidget.getFirstSelected();
	}
	
	public int getSelectedCollectionId() {
		return collectionComboViewerWidget.getSelectedCollectionId();
	}
	
	public TrpCollection getSelectedCollection() {
		return collectionComboViewerWidget.getSelectedCollection();
	}
	
	public String getSelectedRecentDoc(){
		return recentDocsComboViewerWidget.getSelectedDoc();
	}
		
	public void setSelectedCollection(int colId) {
		collectionComboViewerWidget.clearFilter();
		collectionComboViewerWidget.setSelectedCollection(colId, false);
	}
				
	public void updateRecentDocs() {
		recentDocsComboViewerWidget.updateDocs(false);	
	}
	
	public Button getShowJobsBtn() { return showJobsBtn; }
	public Button getShowVersionsBtn() { return showVersionsBtn; }
	
}
