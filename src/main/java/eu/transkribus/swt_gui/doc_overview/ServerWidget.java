package eu.transkribus.swt_gui.doc_overview;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
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
import eu.transkribus.swt_gui.collection_comboviewer.CollectionTableComboViewerWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.util.RecentDocsComboViewerWidget;

public class ServerWidget extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(ServerWidget.class);

	Label usernameLabel;
	DocTableWidgetPagination docTableWidget;

//	CollectionComboViewerWidget collectionComboViewerWidget;
	CollectionTableComboViewerWidget collectionComboViewerWidget;
	RecentDocsComboViewerWidget recentDocsComboViewerWidget;
	
	Button manageCollectionsBtn;
	Button showActivityWidgetBtn;
	Text quickLoadByID;
			
	Storage store = Storage.getInstance();
	Composite remoteDocsGroup;
	Composite container;
	
	Button loginBtn;
		
	int selectedId=-1;
	
	Button showJobsBtn, showVersionsBtn;
	
	ServerWidgetListener serverWidgetListener;
	
	List<Control> userControls = new ArrayList<>();
	
	Menu docOverviewMenu;
	
	MenuItem addToCollectionMenuItem;
	MenuItem removeFromCollectionMenuItem;
	MenuItem deleteDocMenuItem;
	MenuItem duplicateDocMenuItem;
		
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
		
		for (Control c : userControls) {			
			c.setEnabled(isLoggedIn);
		}
	}
	
	private void init() {
		this.setLayout(new GridLayout());
				
		container = new Composite(this, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		container.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		
		loginBtn = new Button(container, 0);
		loginBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		loginBtn.setImage(Images.DISCONNECT);
		Fonts.setBoldFont(loginBtn);
		
		Composite btns1 = new Composite(container, 0);
		btns1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btns1.setLayout(SWTUtil.createGridLayout(2, false, 0, 0));
		
		showJobsBtn = new Button(btns1, 0);
		showJobsBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		showJobsBtn.setText("Jobs on server...");
		showJobsBtn.setImage(Images.CUP);
		userControls.add(showJobsBtn);
		
		showVersionsBtn = new Button(btns1, 0);
		showVersionsBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		showVersionsBtn.setText("Versions of page...");
		showVersionsBtn.setImage(Images.PAGE_WHITE_STACK);
		userControls.add(showVersionsBtn);

		manageCollectionsBtn = new Button(btns1, SWT.PUSH);
		manageCollectionsBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		manageCollectionsBtn.setImage(Images.getOrLoad("/icons/user_edit.png"));
		manageCollectionsBtn.setText("Manage collections...");
		userControls.add(manageCollectionsBtn);
		
		showActivityWidgetBtn = new Button(btns1, SWT.PUSH);
		showActivityWidgetBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		showActivityWidgetBtn.setImage(Images.GROUP);
		showActivityWidgetBtn.setText("User activity...");
		userControls.add(showActivityWidgetBtn);
				
		recentDocsComboViewerWidget = new RecentDocsComboViewerWidget(container, 0);
		recentDocsComboViewerWidget.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		userControls.add(recentDocsComboViewerWidget);
		
		////////////////
		remoteDocsGroup = new Composite(container, 0); // orig-parent = container
		
		remoteDocsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		remoteDocsGroup.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));

		collectionComboViewerWidget = new CollectionTableComboViewerWidget(remoteDocsGroup, 0, true, true, false);
		collectionComboViewerWidget.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		collectionComboViewerWidget.getCollectionFilterLabel().setText("Collections ");
		userControls.add(collectionComboViewerWidget);
		
		Composite docsContainer = new Composite(remoteDocsGroup, 0);
		docsContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		docsContainer.setLayout(new GridLayout(2, false));
		docsContainer.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));

		docTableWidget = new DocTableWidgetPagination(docsContainer, SWT.MULTI, 100);
		docTableWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		userControls.add(docTableWidget);
		
		ColumnViewerToolTipSupport.enableFor(docTableWidget.getPageableTable().getViewer(), ToolTip.NO_RECREATE);
		docTableWidget.getPageableTable().setToolTipText("");
		
		if (remoteDocsGroup instanceof SashForm) {
			((SashForm)remoteDocsGroup).setWeights(new int[]{50, 50});
		}
		
		initDocOverviewMenu();
		
		updateHighlightedRow(-1);
	}
	
	private void initDocOverviewMenu() {
		Table t = docTableWidget.getPageableTable().getViewer().getTable();
		docOverviewMenu = new Menu(t);
		t.setMenu(docOverviewMenu);
		
		addToCollectionMenuItem = new MenuItem(docOverviewMenu, SWT.PUSH);
		addToCollectionMenuItem.setImage(Images.ADD);
		addToCollectionMenuItem.setText("Add to different collection...");
		
		removeFromCollectionMenuItem = new MenuItem(docOverviewMenu, SWT.PUSH);
		removeFromCollectionMenuItem.setImage(Images.DELETE);
		removeFromCollectionMenuItem.setText("Remove from this collection...");		
		
		deleteDocMenuItem = new MenuItem(docOverviewMenu, SWT.PUSH);
		deleteDocMenuItem.setImage(Images.DELETE);
		deleteDocMenuItem.setText("Delete document...");
		
		duplicateDocMenuItem = new MenuItem(docOverviewMenu, SWT.PUSH);
		duplicateDocMenuItem.setImage(Images.PAGE_COPY);
		duplicateDocMenuItem.setText("Duplicate...");
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
	public Label getUsernameLabel() { return usernameLabel; }
	public Button getLoginBtn() { return loginBtn; }
		
	public TrpDocMetadata getSelectedDocument() {
		return docTableWidget.getFirstSelected();
	}
	
	public List<TrpDocMetadata> getSelectedDocuments() {
		return docTableWidget.getSelected();
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
		recentDocsComboViewerWidget.setRecentDocs(false);	
	}
	
	public Button getShowJobsBtn() { return showJobsBtn; }
	public Button getShowVersionsBtn() { return showVersionsBtn; }
	
}
