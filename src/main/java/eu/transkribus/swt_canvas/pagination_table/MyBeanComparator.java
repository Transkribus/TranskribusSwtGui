package eu.transkribus.swt_canvas.pagination_table;

import java.util.Comparator;

import org.eclipse.nebula.widgets.pagination.collections.BeanUtils;
import org.eclipse.swt.SWT;

/**
 * Implementation of {@link Comparator} to compare POJO.
 * 
 */
@SuppressWarnings("rawtypes")
public class MyBeanComparator implements Comparator {

	/** property name used to sort **/
	private final String sortPropertyName;
	/** the sort direction **/
	private int sortDirection;

	public MyBeanComparator(String sortPropertyName, int sortDirection) {
		this.sortPropertyName = sortPropertyName;
		this.sortDirection = sortDirection;
	}

	public int compare(Object o1, Object o2) {
		// commented out from original code --> this is a bug, because objects are always sorted by the compareTo method disregaring the sortPropertyName value!!
//		if ((o1 instanceof Comparable) && (o2 instanceof Comparable)) {
//			// Compare simple type like String, Integer etc
//			Comparable c1 = ((Comparable) o1);
//			Comparable c2 = ((Comparable) o2);
//			return compare(c1, c2);
//		}

		o1 = BeanUtils.getValue(o1, sortPropertyName);
		o2 = BeanUtils.getValue(o2, sortPropertyName);
		if ((o1 instanceof Comparable) && (o2 instanceof Comparable)) {
			// Compare simple type like String, Integer etc
			Comparable c1 = ((Comparable) o1);
			Comparable c2 = ((Comparable) o2);
			return compare(c1, c2);
		}

		return 0;
	}

	private int compare(Comparable c1, Comparable c2) {
		if (sortDirection == SWT.UP) {
			return c2.compareTo(c1);
		}
		return c1.compareTo(c2);
	}

}