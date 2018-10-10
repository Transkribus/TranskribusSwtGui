package eu.transkribus.swt_gui.collection_comboviewer;

import java.util.Observable;
import java.util.Observer;
import java.util.function.Predicate;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class CollectionSelectorWidget extends Composite implements Observer {
	private final static Logger logger = LoggerFactory.getLogger(CollectionSelectorWidget.class);
	
	Composite collComposite;
	Button collectionBtn;
	
	Composite headerComposite;
	Label collectionLabel;
		
	Storage storage = Storage.getInstance();
	Predicate<TrpCollection> collectionPredicate;
	TrpCollection initColl;
	
	public CollectionSelectorWidget(Composite parent, int style, boolean withHeader, Predicate<TrpCollection> collectionPredicate/*, TrpCollection initColl*/) {
		super(parent, style);
		
		this.collectionPredicate = collectionPredicate;
		
		this.setLayout(SWTUtil.createGridLayout(2, false, 0, 0));
		
		if (withHeader) {
			RowLayout rl = new RowLayout();
			rl.center = true;
			rl.marginLeft = 0;
			rl.marginRight = 0;
			
			headerComposite = new Composite(this, SWT.NONE);
			headerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			headerComposite.setLayout(rl);
			
			collectionLabel = new Label(headerComposite, SWT.NONE);
			collectionLabel.setText("Collections: ");
		}

		collComposite = new Composite(this, 0);
		collComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		collComposite.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		
		collectionBtn = new Button(collComposite, SWT.PUSH);
		collectionBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		collectionBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {				
				CollectionSelectorDialog d = new CollectionSelectorDialog(getShell(), collectionPredicate, getSelectedCollection());
				if (d.open() != Dialog.OK) {
					return;
				}
							
				TrpCollection c = d.getSelectedCollection();
				logger.debug("selected collection: "+c);
				if (c == null)
					return;
				
				setSelectedCollection(c);
				sendSelectionEvent(c);
			}
		});
		
		addListener();
		
		setSelectedCollection(null);
		updateSelectedCollectionForNewCollectionList();
	}
	
	void addListener() {
		storage.addListener(new IStorageListener() {
			@Override public void handleCollectionsLoadEvent(CollectionsLoadEvent cle) {
				if (SWTUtil.isDisposed(CollectionSelectorWidget.this) || SWTUtil.isDisposed(getShell()))
						return;
				
				updateSelectedCollectionForNewCollectionList();
			}
		});
	}

	/**
	 * Checks if currently selected collection is still valid for new list of collections, else sets selection to first of new list that matches collectionPredicate
	 */
	private void updateSelectedCollectionForNewCollectionList() {
		logger.debug("selected collection = "+getSelectedCollection()+" current-doc-collid: "+storage.getCurrentDocumentCollectionId());
		
		if (getSelectedCollection() != null && storage.getCollection(getSelectedCollection().getColId())!=null) {
			return;
		}
		
		for (TrpCollection coll : storage.getCollections()) {
			if (collectionPredicate==null || collectionPredicate.test(coll)) {
				setSelectedCollection(coll);
				//if a new collection is selected we want to handle that event in the ServerWidgetListener mainly to update the user permissions (visibility of buttons)
				sendSelectionEvent(coll);
				return;
			}
		}
		
		setSelectedCollection(null); // no collection found that matches collectionPredicate -> set empty selection
	}
	
	public TrpCollection getSelectedCollection() {
		return (TrpCollection) collectionBtn.getData();
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
		this.notifyListeners(SWT.Selection, event);
	}
	
	public void setSelectedCollection(TrpCollection trpCollection) {
		if (trpCollection != null) {
			collectionBtn.setData(trpCollection);
			collectionBtn.setText(trpCollection.getSummary());
		} else {
			collectionBtn.setData(null);
			collectionBtn.setText("No collection selected");
		}
	}
	
	@Override public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		SWTUtil.setEnabled(collectionBtn, enabled);
	}

	@Override public void update(Observable o, Object arg) {
	}

	public Label getCollectionLabel() {
		return collectionLabel;
	}

	public Composite getCollComposite() {
		return collComposite;
	}
}
