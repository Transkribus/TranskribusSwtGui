package eu.transkribus.util;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic data cache class, mainly intended for fast access in image browsing applications. 
 * Stores a given fixed size of data objects and replaces always the least important one first
 * if the limit is exceeded. The least important data object is the one with the least access count and the
 * minimal timestamp.
 * @author sebastianc
 *
 * @param <K> The key class used to access a data object
 * @param <D> The data object stored for a key.
 */
public class DataCache<K, D> {
	private final static Logger logger = LoggerFactory.getLogger(DataCache.class);
	
	/**
	 * Wraps the given data object and adds an access count and a timestamp
	 */
	class DataWrapper {
		private D data=null;
		private int count=0;
		private long time=0L;
		
		public DataWrapper(D data) {
			this.data = data; count = 0; time = System.currentTimeMillis();
		}
		
		public D getData() { return data; }
		public void incCount() { count++; }
		public int getCount() { return count; }
		public long getTime() { return time; }
		public void disposeData() { factory.dispose(data); }
	}
	
	public final static int DEFAULT_SIZE=5;
	private int size = DEFAULT_SIZE;
	private DataCacheFactory<K, D> factory;
//	private ConcurrentHashMap<K, DataWrapper> elements = new ConcurrentHashMap<K, DataWrapper>();
	private Map<K, DataWrapper> elements = new ConcurrentHashMap<K, DataWrapper>();
	
	private K doNotDelete = null; // key of the object that is always kept in memory
	
	/**
	 * Inititializes the DataCache with the specified size (must be > 1) and a DataCacheFactory that is
	 * repsonsible for creating a new data element from a given key and disposing a data element.
	 * @param size
	 * @param factory
	 */
	public DataCache(int size, DataCacheFactory<K, D> factory) {
		this.factory = factory;
		setSize(size);
	}
	
	public void setSize(int size) {
		if (size < 1) {
			logger.error("Size must be greater than 0 - setting default size "+DEFAULT_SIZE);
			size = DEFAULT_SIZE;
		}
		this.size = size;
	}
	
//	public synchronized void setDoNotDelete(K doNotDelete) {
//	this.doNotDelete = doNotDelete;
//}
	
	/** Preloads data for a list of keys in a separate thread */
	public void preload(final ArrayList<K> keys, final Object opts) {
		Thread t = new Thread() {
			@Override
			public void run() {
				for (K k : keys) {
					try {
						getOrPut(k, false, opts);
					} catch (Exception e) {
						logger.error("Error while preloading data with key "+k);
					}
				}
				logger.debug("Finished preloading!");
			}
		};
		t.start();
		
	}
	
	public synchronized D getOrPut(K key, boolean keepAlways, Object opts) throws Exception {
		return getOrPut(key, keepAlways, opts, false);
	}
	
	/**
	 * Retrieve element with specified key from cache or create new data and put it into cache if it is not there.
	 * Least important elements are removed if the size of the cache is exceeded.
	 * @param key the search key
	 * @param keepAlways true if the specified key should always be kept in memory
	 * @param opts data specific options for reloading
	 * @param forceReload true to force a reload of this element
	 * @return The data element that has either been just retrieved or newly created.
	 * @throws Exception
	 */
	public synchronized D getOrPut(K key, boolean keepAlways, Object opts, boolean forceReload) throws Exception {
//		if (true) throw new Exception("TEST: Error loading image!");
		
		DataWrapper d = elements.get(key);
		if (keepAlways) {
			doNotDelete = key;
		}
		if (d != null && !forceReload) {
			d.incCount();
			logger.debug("already got element: "+key+", access count: "+d.getCount() + ", size: "+size);			
			return d.data;
		}
		else {
			D newData = factory.createFromKey(key, opts);
			put(key, newData);
			logger.debug("loaded new element: "+key + ", size: "+size);
			return newData;
		}
	}
		
	/**
	 * Puts a new element to the cache store. If the key already exists, it is replaced.
	 * Else, if there is room, the new value is added. If there is no room left, the least
	 * important element is removed and the new one added
	 * @param key
	 * @param data
	 * @return True if an element had to be removed prior to inserting the new one
	 */
	public synchronized boolean put(K key, D data) {
		DataWrapper newData = new DataWrapper(data);
		// key already exists:
		if (elements.containsKey(key)) {
			removeAndDispose(key);
			elements.put(key, newData);
			return true;
		}
		// new element has room:
		else if (elements.size() < size) {
			elements.put(key, newData);
			return false;
		}
		// remove least important element if no room left:
		else {
			K removeKey = findLeastImportant();
			if (removeKey == null) // should never be the case...
				throw new RuntimeException("Fatal error - could not load new object in put since no least important key can be set");
			
			removeAndDispose(removeKey);
			elements.put(key, newData);
			return true;
		}
	}
	
	/**
	 * Removes and disposes the element with the specified key
	 */
	private synchronized void removeAndDispose(K key) {
		DataWrapper deleted = elements.remove(key);
		if (deleted!=null) {
			deleted.disposeData();
		}
	}	
	
	/**
	 * Returns the key of the element that is least important i.e. which has lowest access count and minimal timestamp and is not equal to doNotDelete key.
	 * Returns null if no such element exists (can only be the case for a cache with size 1 and the doNotDelete object set to the key of the object loaded).
	 */
	private K findLeastImportant() {
		// find min value of access count:
		int minCount = Integer.MAX_VALUE;
		for (K key : elements.keySet()) {
			if (!key.equals(doNotDelete) && elements.get(key).getCount() < minCount) {
				minCount = elements.get(key).count;
			}
		}
		// find the element with min value of access count that was inserted first:
		long minTime = Long.MAX_VALUE;
		K minKey=null;
		for (K key : elements.keySet()) {
			if (!key.equals(doNotDelete) && elements.get(key).getCount()==minCount && elements.get(key).getTime()<minTime) {
				minKey = key;
				minTime = elements.get(key).getTime();
			}
		}
		return minKey;
	}	
	
	public boolean containsKey(K key) {
		return elements.containsKey(key);
	}

}
