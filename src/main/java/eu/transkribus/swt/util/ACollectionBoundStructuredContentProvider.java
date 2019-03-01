package eu.transkribus.swt.util;

import org.eclipse.jface.viewers.IStructuredContentProvider;

import eu.transkribus.swt_gui.mainwidget.storage.Storage;

/**
 * Abstract StructuredContentProvider that allows to store a collection ID on initialization 
 * which can be used for access to storage later, even if the user switched collections in the main widget.
 *
 */
public abstract class ACollectionBoundStructuredContentProvider implements IStructuredContentProvider {

	protected Storage store = Storage.getInstance();
	private final Integer colId;
	
	/**
	 * @param colId
	 */
	public ACollectionBoundStructuredContentProvider(Integer colId) {
		this.colId = colId;
	}
	
	/**
	 * @return the collection ID passed at initialization, or Storage.getInstance().getCollId() if null was passed.
	 */
	public int getCollId() {
		if(this.colId != null) {
			return colId;
		} else {
			return store.getCollId();
		}
	}

}
