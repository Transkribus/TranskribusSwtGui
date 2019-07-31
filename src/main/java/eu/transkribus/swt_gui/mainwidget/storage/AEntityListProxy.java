package eu.transkribus.swt_gui.mainwidget.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.rest.JaxbPaginatedList;

/**
 * Proxy for paged entity lists that are linked to collections.<br><br> 
 * Previously retrieved entities are cached.<br><br>
 * Observers will be notified once the number of total elements on the server has changed and any UI element need respective updates.
 * 
 * @param <T>
 */
public abstract class AEntityListProxy<T> extends Observable {
	private static final Logger logger = LoggerFactory.getLogger(AEntityListProxy.class);
	
	protected final Storage storage;
	
	private int total = 0;
	private List<T> list;
	private String[] filters;
	
	AEntityListProxy(Storage storage) {
		this.storage = storage;
		total = 0;
		list = null;
		filters = new String[] {};
	}
	
	public List<T> getList() throws SessionExpiredException, ServerErrorException, ClientErrorException {
		return getList(0, -1);
	}
	
	public int getTotal() {
		return total;
	}
	
	public List<T> getList(final int index, final int nValues, String...filters) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		if(!storage.isLoggedIn()) {
			return new ArrayList<>(0);
		}
		if(list == null) {
			//load elements and init list and total
			this.filters = filters;
			JaxbPaginatedList<T> entityList = loadEntities(index, nValues, filters);
			initList(entityList);
		} else {
			//check if the requested range is cached and load if necessary
			loadRange(index, nValues, filters);			
		}
		
		//build a new list with requested elements
		List<T> result;
		if(index < 1 && nValues < 1) {
			//return all in new list
			result = new ArrayList<>(list);
		} else {
			//return requested range
			result = new ArrayList<>(nValues);
			for(int i = index; i < nValues; i++) {
				result.add(list.get(i));
			}
		}
		return result;
	}
	
	private void initList(JaxbPaginatedList<T> entityList) {
		total = entityList.getTotal();
		list = new ArrayList<>(total);
		list.addAll(entityList.getIndex(), entityList.getList());
	}
	
	/**
	 * Update the internal list with the values according to the requested range and filters.
	 * <br><br>
	 * If filters do not match the internally stored filterset or the total number of values (returned with the values) has changed, the list is reinitialized.
	 * 
	 * @param index
	 * @param nValues
	 * @param filters
	 * @throws SessionExpiredException
	 * @throws ServerErrorException
	 * @throws ClientErrorException
	 */
	private void loadRange(int index, int nValues, String...filters) throws SessionExpiredException, ServerErrorException, ClientErrorException {
		//bound the parameters
		if(index < 0) {
			index = 0;
		}
		if(nValues > total) {
			nValues = total;
		} else if(nValues < 1) {
			//go for the whole range from index
			nValues = total - index;	
		}
		//check if loading elements from server is required
		boolean doLoad = false; 
		
		if(didFiltersChange(filters)) {
			//we do need a reload anyway if the filters changed
			this.filters = filters;
			clear();
			doLoad = true;
		} else {
			//check if the range is loaded
			for(int i = index; i < nValues; i++) {
				if(list.get(i) == null) {
					doLoad = true;
					logger.debug("Found null element in list proxy. Triggering reload.");
					//if one element is missing then reload the whole range for simplicity
					break;
				}
			}
		}
		if(doLoad) {
			JaxbPaginatedList<T> entityList = loadEntities(index, nValues, filters);
			if(entityList.getTotal() != total) {
				//total has changed. clear, init and notify observers
				clear();
				initList(entityList);
				setChanged();
				notifyObservers(total);
			} else {
				list.addAll(index, entityList.getList());
			}
		}
	}
	
	protected boolean didFiltersChange(String[] filters) {
		return Arrays.equals(this.filters, filters);
	}

	public void clear() {
		list = null;
		total = 0;
	}

	protected abstract JaxbPaginatedList<T> loadEntities(final int index, final int nValues, String...filters) throws SessionExpiredException, ServerErrorException, ClientErrorException;
}
