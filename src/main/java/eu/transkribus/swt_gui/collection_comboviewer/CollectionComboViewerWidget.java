package eu.transkribus.swt_gui.collection_comboviewer;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.util.DelayedTask;

public class CollectionComboViewerWidget extends Composite implements Observer {
	private final static Logger logger = LoggerFactory.getLogger(CollectionComboViewerWidget.class);
	
	public Label collectionFilterLabel;
	public Text collectionFilterText;
	public ComboViewer collectionComboViewer;
	public Combo collectionCombo;
	public Button reloadCollectionsBtn;
	public Label collectionLabel;
	public Composite headerComposite;
	public Composite filterComposite;
	public Composite collComposite;
	
	ModifyListener filterModifyListener;
	
	private List<TrpCollection> collections = new ArrayList<>();
	
	Storage storage = Storage.getInstance();
	
	boolean withFilter, withReloadButton;
	
//	public CollectionComboViewerWidget(Composite parent, int style) {
//		this(parent, style, true, true, true);
//	}
	
	public CollectionComboViewerWidget(Composite parent, int style, boolean withFilter, boolean withReloadButton, boolean withHeader) {
		super(parent, style);
		
		this.withFilter = withFilter;
		this.withReloadButton = withReloadButton;
		
		///////////////
		
//		Composite collsContainer = new Composite(this, 0);
//		collsContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.setLayout(new GridLayout(2, false));
		
		RowLayout rl = new RowLayout();
		rl.center = true;
		rl.marginLeft = 0;
		rl.marginRight = 0;
		
		if (withHeader) {
			headerComposite = new Composite(this, SWT.NONE);
			headerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
	//		headerComposite.setLayout(new GridLayout(3, false));
			headerComposite.setLayout(rl);
			
			collectionLabel = new Label(headerComposite, SWT.NONE);
			collectionLabel.setText("Collections: ");
		}
				
		if (withFilter) {
			filterComposite = new Composite(this, SWT.NONE);
			filterComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			filterComposite.setLayout(new GridLayout(2, false));
//			filterComposite.setLayout(new FillLayout());
			
			collectionFilterLabel = new Label(filterComposite, 0);
			collectionFilterLabel.setText("Filter: ");
//			collectionFilterLabel.setLayoutData(new GridData(GridData.BEGINNING));
			
			collectionFilterText = new Text(filterComposite, SWT.BORDER);
			collectionFilterText.setToolTipText("Collection name filter");
//			collectionFilterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			collectionFilterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			filterModifyListener = new ModifyListener() {
				DelayedTask dt = new DelayedTask(() -> { refreshCombo(true); }, true);
				@Override public void modifyText(ModifyEvent e) {
					dt.start();
				}
			};
			collectionFilterText.addModifyListener(filterModifyListener);
			
			collectionFilterText.addTraverseListener(new TraverseListener() {
				@Override public void keyTraversed(TraverseEvent e) {
					if (e.detail == SWT.TRAVERSE_RETURN)
						refreshCombo(true);
				}
			});
		}
		
		collComposite = new Composite(this, 0);
		collComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		collComposite.setLayout(new GridLayout(2, false));
		
		int colSize = withReloadButton ? 1 : 2;
				
		collectionCombo = new Combo(collComposite, SWT.READ_ONLY | SWT.DROP_DOWN);
		collectionCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, colSize, 1));
//		collectionCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		collectionComboViewer = new ComboViewer(collectionCombo);
		
		if (withReloadButton) {
			reloadCollectionsBtn = new Button(collComposite, SWT.PUSH);
//			reloadCollectionsBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, colSize, 1));
//			reloadCollectionsBtn.setLayoutData(new GridData(GridData.END));
			reloadCollectionsBtn.setImage(Images.getOrLoad("/icons/refresh.gif"));
			reloadCollectionsBtn.setToolTipText("Reload list of available collections!");
//			reloadCollectionsBtn.pack();
		}
				
		collectionComboViewer.setLabelProvider(new LabelProvider() {
			@Override public String getText(Object element) {
				if (element instanceof TrpCollection) {
					return ((TrpCollection) element).getSummary();
				}
				else return "i am error";
			}
		});
		
		collectionComboViewer.setContentProvider(new ArrayContentProvider());
		
		if (withFilter) {
			collectionComboViewer.addFilter(new ViewerFilter() {		
				@Override public boolean select(Viewer viewer, Object parentElement, Object element) {
					logger.trace("selecting: "+element);
					String ft = collectionFilterText.getText();
					if (StringUtils.isEmpty(ft))
						return true;
					
					ft = Pattern.quote(ft);
					
					String reg = "(?i)(.*"+ft+".*)";
					logger.trace("reg = "+reg);
					TrpCollection c = (TrpCollection) element;
	//				boolean matches = c.getColName().matches(reg);
					boolean matches = c.getSummary().matches(reg);
					
					logger.trace("colName = "+c.getColName()+" matches = "+matches);
					return matches;
				}
			});
		}
		
		///////////////	
		
		addListener();
		
		updateCollections();
	}
	
	public void refreshCombo(boolean setListVisible) {
		collectionComboViewer.refresh();
		if (setListVisible) {
			collectionComboViewer.getCombo().setListVisible(true);
		}
	}
	
	public void clearFilter() {
		collectionFilterText.removeModifyListener(filterModifyListener);
		collectionFilterText.setText("");
		collectionFilterText.addModifyListener(filterModifyListener);
		refreshCombo(false);
	}
	
	void addListener() {
		if (reloadCollectionsBtn!=null) {
			reloadCollectionsBtn.addSelectionListener(new SelectionAdapter() {
				
				@Override public void widgetSelected(SelectionEvent e) {
					try {
						logger.debug("reloading collections!");
						if (!storage.isLoggedIn()) {
							// DialogUtil.showErrorMessageBox(getShell(), "Not logged in",
							// "You have to log in to reload the collections list");
							return;
						}
	
						storage.reloadCollections();
					} catch (Throwable ex) {
	//					onError("Error", "Error reload of collections: " + e.getMessage(), e);
	
						showBallonMessage(SWT.ICON_ERROR, "Error loading collections", ex.getMessage());
					}
				}
			});
		}
		
		storage.addListener(new IStorageListener() {
			@Override public void handleCollectionsLoadEvent(CollectionsLoadEvent cle) {
				if (SWTUtil.isDisposed(CollectionComboViewerWidget.this) || SWTUtil.isDisposed(getShell()))
						return;
				
				updateCollections();
			}
		});
	}
	
	void updateCollections() {
		setAvailableCollections(Storage.getInstance().getCollections());
	}
	
	private void showBallonMessage(Integer iconType, String title, String message) {
		if (iconType == null)
			iconType = SWT.ICON_INFORMATION;
		
		DialogUtil.showBallonToolTip(collectionCombo, iconType, title, message);	
	}
	
	public TrpCollection getSelectedCollection() {
		IStructuredSelection sel = (IStructuredSelection) collectionComboViewer.getSelection();
		if (!sel.isEmpty())
			return (TrpCollection) sel.getFirstElement();
		
		return null;
	}
	
	private int getIndexOfCollection(int colId) {
		if (colId<=0)
			return -1;
		
		for (int i=0; i<collections.size(); ++i) {
			if (collections.get(i).getColId() == colId)
				return i;
		}
		return -1;
	}	
	
	public void setAvailableCollections(List<TrpCollection> collections) {
//		collectionsTable.refreshList(collections);
		
//		logger.info("1");
		this.collections = collections;
		TrpCollection selectedBefore = getSelectedCollection();
//		logger.info("2");
		
		collectionComboViewer.setInput(collections);
//		logger.info("3");
		int i = getIndexOfCollection(selectedBefore!=null ? selectedBefore.getColId() : -1);
		if (i != -1) { // select collection selected before if possible
			collectionComboViewer.getCombo().select(i);
//			logger.info("4");
		} else if (collections!=null && !collections.isEmpty()) { // select first collection if there
			collectionComboViewer.getCombo().select(0);
//			logger.info("5");
		}
		
		sendComboSelectionEvent();
	}
	
	public int getSelectedCollectionId() {
		TrpCollection c = getSelectedCollection();
		return c == null ? 0 : c.getColId();
	}
	
	public void setSelectedCollection(int colId, boolean fireSelectionEvent) {
		int i = getIndexOfCollection(colId);
		
		if (i >= 0) {
			collectionCombo.select(i);
			if (fireSelectionEvent) {
				sendComboSelectionEvent();
			}
		}
	}
	
	void sendComboSelectionEvent() {
		Event event = new Event(); 
		event.type = SWT.Selection;
		event.widget = collectionCombo;
//		event.widget = this;
		collectionCombo.notifyListeners(SWT.Selection, event);
	}
	
	@Override public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		SWTUtil.setEnabled(collectionFilterText, enabled);
		SWTUtil.setEnabled(collectionCombo, enabled);
		SWTUtil.setEnabled(reloadCollectionsBtn, enabled);
	}

	@Override public void update(Observable o, Object arg) {
	}

	public Label getCollectionFilterLabel() {
		return collectionFilterLabel;
	}

	public Text getCollectionFilterText() {
		return collectionFilterText;
	}

	public ComboViewer getCollectionComboViewer() {
		return collectionComboViewer;
	}

	public Combo getCollectionCombo() {
		return collectionCombo;
	}

	public Button getReloadCollectionsBtn() {
		return reloadCollectionsBtn;
	}

	public Label getCollectionLabel() {
		return collectionLabel;
	}
	
	

}
