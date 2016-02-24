package eu.transkribus.util;


/**
 * Abstract factory class for creating and disposing data.
 * Used in conjunction with DataCache class and is responsible for creating an element from a given key
 * and disposing a data element (which is e.g. needed when using DataCache for SWT Image objects!)
 * @author sebastianc
 *
 * @param <K> The key class
 * @param <D> The data element class
 */
public abstract class DataCacheFactory<K, D> {
	public abstract D createFromKey(K key, Object opts) throws Exception;
	public abstract void dispose(D element);
}