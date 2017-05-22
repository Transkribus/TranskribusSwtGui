package eu.transkribus.swt_gui.collection_comboviewer;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.util.DelayedTask;

public class CollectionSelectorWidget extends Composite implements Observer {
	private final static Logger logger = LoggerFactory.getLogger(CollectionSelectorWidget.class);
	
	Label collectionFilterLabel;
	Text collectionFilterText;
	Button collectionBtn;
	
	Button reloadCollectionsBtn;
	Label collectionLabel;
	Composite headerComposite;
	Composite filterComposite;
	Composite collComposite;
	
	ModifyListener filterModifyListener;
	List<TrpCollection> collections = new ArrayList<>();
	Storage storage = Storage.getInstance();
	boolean withFilter, withAdditionalBtn;
	
	public CollectionSelectorWidget(Composite parent, int style, boolean withFilter, boolean withAdditionalBtn, boolean withHeader) {
		super(parent, style);
		
//		this.withFilter = withFilter;
		this.withAdditionalBtn = withAdditionalBtn;
		
		///////////////
		
//		Composite collsContainer = new Composite(this, 0);
//		collsContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.setLayout(SWTUtil.createGridLayout(2, false, 0, 0));
		
		if (withHeader) {
			RowLayout rl = new RowLayout();
			rl.center = true;
			rl.marginLeft = 0;
			rl.marginRight = 0;
			
			headerComposite = new Composite(this, SWT.NONE);
			headerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
	//		headerComposite.setLayout(new GridLayout(3, false));
			headerComposite.setLayout(rl);
			
			collectionLabel = new Label(headerComposite, SWT.NONE);
			collectionLabel.setText("Collections: ");
		}
				
		if (false && withFilter) {
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
//		collComposite.setLayout(new GridLayout(2, false));
		collComposite.setLayout(SWTUtil.createGridLayout(withAdditionalBtn ? 1 : 2, false, 0, 0));

//		int colSize = withAdditionalBtn ? 1 : 2;
		
		collectionBtn = new Button(collComposite, SWT.PUSH);
		collectionBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		collectionBtn.addSelectionListener(new SelectionListener() {
			@Override public void widgetSelected(SelectionEvent e) {
				CollectionSelectorDialog d = new CollectionSelectorDialog(getShell());
				if (d.open() != Dialog.OK) {
					return;
				}
							
				TrpCollection c = d.getSelectedCollection();
				logger.info("selected collection: "+c);
				if (c == null)
					return;
				
				setSelectedCollection(c.getColId());
				sendSelectionEvent(c);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		setSelectedCollection(-1);
				
//		collectionCombo = new Combo(collComposite, SWT.READ_ONLY | SWT.DROP_DOWN);
//		collectionCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, colSize, 1));
////		collectionCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		collectionComboViewer = new ComboViewer(collectionCombo);
//		collectionComboViewer.setUseHashlookup(true);
		
		if (withAdditionalBtn) {
			reloadCollectionsBtn = new Button(collComposite, SWT.PUSH);
//			reloadCollectionsBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, colSize, 1));
//			reloadCollectionsBtn.setLayoutData(new GridData(GridData.END));
			reloadCollectionsBtn.setImage(Images.getOrLoad("/icons/refresh.gif"));
			reloadCollectionsBtn.setToolTipText("Reload list of available collections!");
//			reloadCollectionsBtn.pack();
		}
				
//		collectionComboViewer.setLabelProvider(new LabelProvider() {
//			@Override public String getText(Object element) {
//				if (element instanceof TrpCollection) {
//					return ((TrpCollection) element).getSummary();
//				}
//				else return "i am error";
//			}
//		});
//		
//		collectionComboViewer.setContentProvider(new ArrayContentProvider());
//		
//		
//		if (withFilter) {
//			collectionComboViewer.addFilter(new ViewerFilter() {		
//				@Override public boolean select(Viewer viewer, Object parentElement, Object element) {
//					logger.trace("selecting: "+element);
//					String ft = collectionFilterText.getText();
//					if (StringUtils.isEmpty(ft))
//						return true;
//					
//					ft = Pattern.quote(ft);
//					
//					String reg = "(?i)(.*"+ft+".*)";
//					logger.trace("reg = "+reg);
//					TrpCollection c = (TrpCollection) element;
//	//				boolean matches = c.getColName().matches(reg);
//					boolean matches = c.getSummary().matches(reg);
//					
//					logger.trace("colName = "+c.getColName()+" matches = "+matches);
//					return matches;
//				}
//			});
//		}
		
		///////////////	
		
		addListener();
		
		updateCollections();
	}
	
	public void refreshCombo(boolean setListVisible) {
//		collectionComboViewer.refresh();
//		if (setListVisible) {
//			collectionComboViewer.getCombo().setListVisible(true);
//		}
	}
	
	public void clearFilter() {
		if (collectionFilterText==null || collectionFilterText.isDisposed())
			return;
		
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
				if (SWTUtil.isDisposed(CollectionSelectorWidget.this) || SWTUtil.isDisposed(getShell()))
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
		
		DialogUtil.showBallonToolTip(this, iconType, title, message);	
	}
	
	public TrpCollection getSelectedCollection() {
		
		TrpCollection c = (TrpCollection) collectionBtn.getData();
		return c;
		
		
//		IStructuredSelection sel = (IStructuredSelection) collectionComboViewer.getSelection();
//		if (!sel.isEmpty())
//			return (TrpCollection) sel.getFirstElement();
//		
//		return null;
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
		TrpCollection selectedBefore = getSelectedCollection();
		this.collections = collections;
		int i = getIndexOfCollection(selectedBefore!=null ? selectedBefore.getColId() : -1);
		
		// if collection selected before could not be found and if the list of new collections is not empty
		// then select the first collection of the new list
		if (i==-1 && !CoreUtils.isEmpty(collections)) {
			setSelectedCollection(collections.get(0).getColId());
			sendSelectionEvent(getSelectedCollection());
		}
	}
	
	public int getSelectedCollectionId() {
		TrpCollection c = getSelectedCollection();
		return c == null ? 0 : c.getColId();
	}
	
	private void sendSelectionEvent(TrpCollection c) {
		Event event = new Event(); 
		event.type = SWT.Selection;
		event.data = c;
		event.widget = this;
//		event.widget = this;
		this.notifyListeners(SWT.Selection, event);
	}
	
	public void setSelectedCollection(int colId) {
		int i = getIndexOfCollection(colId);
		
		if (i >= 0 && i<collections.size()) {
			TrpCollection c = collections.get(i);
			collectionBtn.setData(c);
			collectionBtn.setText(c.getSummary());
		} else {
			collectionBtn.setData(null);
			collectionBtn.setText("No collection selected");
		}
	}
	
//	void sendComboSelectionEvent() {
//		Event event = new Event(); 
//		event.type = SWT.Selection;
//		event.widget = collectionCombo;
////		event.widget = this;
//		collectionCombo.notifyListeners(SWT.Selection, event);
//	}
	
	@Override public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		SWTUtil.setEnabled(collectionFilterText, enabled);
		SWTUtil.setEnabled(collectionBtn, enabled);
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

//	public ComboViewer getCollectionComboViewer() {
//		return collectionComboViewer;
//	}
//
//	public Combo getCollectionCombo() {
//		return collectionCombo;
//	}

	public Button getReloadCollectionsBtn() {
		return reloadCollectionsBtn;
	}

	public Label getCollectionLabel() {
		return collectionLabel;
	}

	public Composite getCollComposite() {
		return collComposite;
	}
	
	

}
