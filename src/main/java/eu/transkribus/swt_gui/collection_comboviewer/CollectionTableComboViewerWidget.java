package eu.transkribus.swt_gui.collection_comboviewer;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.nebula.jface.tablecomboviewer.MyTableComboViewer;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.MyTableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
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
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.util.DelayedTask;

public class CollectionTableComboViewerWidget extends Composite implements Observer {
	private final static Logger logger = LoggerFactory.getLogger(CollectionComboViewerWidget.class);
	
	public Label collectionFilterLabel;
	public Text collectionFilterText;
	
	public MyTableComboViewer collectionComboViewer;
	public MyTableCombo collectionCombo;
	
	public Button reloadCollectionsBtn;
	public Label collectionLabel;
	public Composite headerComposite;
	public Composite filterComposite;
	public Composite collComposite;
	
	ModifyListener filterModifyListener;
	
//	private List<TrpCollection> collections = new ArrayList<>();
	
	Storage storage = Storage.getInstance();
	
	boolean withFilter, withReloadButton;
		
	private class TrpCollectionLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider, ITableFontProvider {
		/**
		 * We return null, because we don't support images yet.
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage (Object element, int columnIndex) {
			return null;
		}

		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText (Object element, int columnIndex) {
//			System.out.println("HI! "+element+" columIndex = "+columnIndex);
			
			TrpCollection c = (TrpCollection) element;
			
			switch (columnIndex) {
			case 0:
				return c.getColId() + "";
			case 1:
				return c.getColName();
			case 2:
				return (c.getRole() == null ? "Admin" : c.getRole().toString());
			}
			return "";
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object, int)
		 */
		public Color getBackground(Object element, int columnIndex) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
		 */
		public Color getForeground(Object element, int columnIndex) {
//			TrpCollection item = (TrpCollection)element;
//			
//			if (item.getId() == 1 || item.getId() == 15) {
//				return darkRed;
//			}
//			else if (item.getId() == 5 || item.getId() == 20) {
//				return darkBlue;
//			}
//			else if (item.getId() == 10) {
//				return darkGreen;
//			}
//			else {
//				return null;
//			}
			return null;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableFontProvider#getFont(java.lang.Object, int)
		 */
		public Font getFont(Object element, int index) {
			TrpCollection item = (TrpCollection)element;
			if (index == 1) { // highlight collection name bold
				return Fonts.createBoldFont(collectionComboViewer.getTableCombo().getFont());
			}
			
			return null;
		}		
	}
	
	/**
	 * @deprecated lazy loading does not work with tablecomboviewer
	 */
	private class TrpCollectionLazyContentProvider implements ILazyContentProvider, IStructuredContentProvider {

		List<TrpCollection> collections;

		public Object[] getElements(Object inputElement) {
			return this.collections.toArray();
		}

		public void dispose() {

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.collections = (List<TrpCollection>) newInput;
		}

		public void updateElement(int index) {
			System.out.println("updateElement: "+index);
			
			if (collections!=null && index >= 0 && index <collections.size()) {
				TrpCollection c = collections.get(index);
				
						
//						Object[] row = model.getData().get(index);
//						row[2] = row[0] + " " + row[1];
//						tableViewer.replace(row, index);
				
				collectionComboViewer.replace(c, index);
			}
			
		}

	}
	
	public CollectionTableComboViewerWidget(Composite parent, int style, boolean withFilter, boolean withReloadButton, boolean withHeader) {
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
		collComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		collComposite.setLayout(new GridLayout(2, false));
//		collComposite.setLayout(new RowLayout());
		
		int colSize = withReloadButton ? 1 : 2;
		
		// create TableCombo
		collectionComboViewer = new MyTableComboViewer(collComposite, SWT.READ_ONLY | SWT.FLAT);
		collectionComboViewer.getTableCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, colSize, 1));
//		collectionComboViewer.getTableCombo().setLayoutData(new RowData(100, SWT.DEFAULT));
		
		new Button(collComposite, SWT.ARROW | SWT.UP | SWT.DOWN);
		
		collectionComboViewer.getTableCombo().defineColumns(new String[] { "ID", "Name", "Role"}, new int[] { 50 , 300, SWT.DEFAULT});

		// set the content provider
		collectionComboViewer.setContentProvider(ArrayContentProvider.getInstance());
//		collectionComboViewer.setContentProvider(new TrpCollectionLazyContentProvider()); // lazy loading does not work as TableCombo does not support SWT.VIRTUAL flag!
		
		// set the label provider
		collectionComboViewer.setLabelProvider(new TrpCollectionLabelProvider());

		// tell the TableCombo that I want 3 blank columns auto sized.
//		collectionComboViewer.getTableCombo().defineColumns(3);
		
		// set which column index will be used to display the selected item.
		collectionComboViewer.getTableCombo().setDisplayColumnIndex(1);
		
		collectionComboViewer.getTableCombo().setShowTableHeader(true);
		collectionComboViewer.getTableCombo().setVisibleItemCount(10);
		
		// load the data
//		tcv.setInput(modelList);
		
		// add listener
//		tcv.addSelectionChangedListener(new ItemSelected("Sample4"));
		
		collectionCombo = collectionComboViewer.getTableCombo();
		collectionCombo.setBackground(Colors.getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		
		// add listener to *not* highlight text of the selected item
		collectionCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				collectionCombo.setSelection(new Point(0,0));
				CollectionTableComboViewerWidget.this.getParent().forceFocus();
			}
		});
				
		if (withReloadButton) {
			reloadCollectionsBtn = new Button(collComposite, SWT.PUSH);
			reloadCollectionsBtn.setImage(Images.getOrLoad("/icons/refresh.gif"));
			reloadCollectionsBtn.setToolTipText("Reload list of available collections!");
		}
		
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
	
	public Composite getCollComposite() {
		return collComposite;
	}
	
	public void refreshCombo(boolean setListVisible) {
		collectionComboViewer.refresh();
		if (setListVisible) {
//			collectionComboViewer.getCombo().setListVisible(true);
			collectionCombo.setTableVisible(true);
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
				if (SWTUtil.isDisposed(CollectionTableComboViewerWidget.this) || SWTUtil.isDisposed(getShell()))
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
		
		for (int i=0; i<getCollections().size(); ++i) {
			if (getCollections().get(i).getColId() == colId)
				return i;
		}
		return -1;
	}	
	
	public void setAvailableCollections(List<TrpCollection> collections) {
//		collectionsTable.refreshList(collections);
		
//		logger.info("1");
//		this.collections = collections;
		TrpCollection selectedBefore = getSelectedCollection();
//		logger.info("2");
		
		collectionComboViewer.setInput(collections);
		
//		if (selectedBefore != null) {
//			collectionComboViewer.setSelection(new StructuredSelection(selectedBefore));
//			collectionCombo.setSelection(new Point(0,0));
//			sendComboSelectionEvent();
//		}
		
//		logger.info("3");
		int i = getIndexOfCollection(selectedBefore!=null ? selectedBefore.getColId() : -1);
		if (i != -1) { // select collection selected before if possible
			collectionCombo.select(i);
//			logger.info("4");
		} else if (collections!=null && !collections.isEmpty()) { // select first collection if there
			collectionCombo.select(0);
//			logger.info("5");
		}
		
		collectionCombo.setSelection(new Point(0,0));
		
		sendComboSelectionEvent();
	}
	
	public List<TrpCollection> getCollections() {
		return (List<TrpCollection>) collectionComboViewer.getInput();
	}
	
	public int getSelectedCollectionId() {
		TrpCollection c = getSelectedCollection();
		return c == null ? 0 : c.getColId();
	}
	
	public void setSelectedCollection(int colId, boolean fireSelectionEvent) {
		int i = getIndexOfCollection(colId);
		
		if (i >= 0) {
			collectionCombo.select(i);
			collectionCombo.setSelection(new Point(0,0));
			if (fireSelectionEvent) {
				sendComboSelectionEvent();
			}
		}
	}
	
	void sendComboSelectionEvent() {
//		if (true)
//			return;
		
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
	
	

}
