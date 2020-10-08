package eu.transkribus.swt_gui.mainwidget.storage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.InvocationCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.rest.TrpHtrList;

public class HtrStorage extends AEntityListProxy<TrpHtr> {
	private static final Logger logger = LoggerFactory.getLogger(HtrStorage.class);
	
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
		Future<TrpHtrList> fut = storage.getConnection().getHtrs(colId, provider, index, nValues, null, null, new InvocationCallback<TrpHtrList>() {
			
			@Override
			public void completed(TrpHtrList htrList) {					
				logger.debug("async loaded HTR list: total = {}, size = {}, index = {}, nValues = {}, thread = {} ", 
						htrList.getTotal(), htrList.getList().size(), htrList.getIndex(), 
						htrList.getnValues(), Thread.currentThread().getName());
//				sendEvent(new HtrListLoadEvent(this, Storage.this.collId, htrList));
			}

			@Override public void failed(Throwable throwable) {
				logger.error("Error loading HTR models: " + throwable.getMessage(), throwable);
//				htrList.clear();
			}
		});
		try {
			return fut.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new IllegalStateException("Could not load HTR list!", e);
		}
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
