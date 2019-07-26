package eu.transkribus.swt_gui.mainwidget.storage;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.rest.TrpHtrList;

public class HtrStorage extends AEntityListProxy<TrpHtr> {

	int colId;
	
	HtrStorage(Storage storage) {
		super(storage);
		colId = storage.getCollId();
	}

	@Override
	protected TrpHtrList loadEntities(int index, int nValues, String...filters) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		if(colId != storage.getCollId()) {
			clear();
			colId = storage.getCollId();
		}
		String provider = filters != null && filters.length > 0 ? filters[0] : null;
		return storage.getConnection().getHtrs(colId, provider, index, nValues);
	}
	
	@Override
	protected boolean didFiltersChange(String[] filters) {
		boolean collectionChanged = colId != storage.getCollId();
		if(collectionChanged) {
			this.colId = storage.getCollId();
		}
		return collectionChanged || super.didFiltersChange(filters);
	}
}
