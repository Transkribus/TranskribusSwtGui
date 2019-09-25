package eu.transkribus.swt.util;

import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_gui.util.DelayedTask;

/**
 * Abstract {@link ViewerFilter} (taken from {@link DocTableWidgetPagination}) to be used on jface structured viewers in combination with a swt Text field for search term input.<br>
 * The {@link #updateView()} implementation must trigger updates of the view and is called upon user input on the text field.
 */
public abstract class TrpViewerFilter extends ViewerFilter {
	private static final Logger logger = LoggerFactory.getLogger(TrpViewerFilter.class);
	
	/**
	 * The text field for the search term input
	 */
	private Text filterTxt;
	/**
	 * field names in viewer data to search in
	 */
	private final String[] filterProperties;
	
	/**
	 * if not null, only objects of this type will be respected by the filter. This is only important for TreeViewers so far.
	 */
	private final Class<?> filterTarget;
	
	/**
	 * @param filterTxt the text field for the input
	 * @param filterProperties the properties (i.e. fields) of the objects to be considered for filtering
	 */
	public TrpViewerFilter(Text filterTxt, String... filterProperties) {
		this(filterTxt, null, filterProperties);
	}
	
	/**
	 * @param filterTxt the text field for the input
	 * @param filterTarget specify a class here in case the filter funcionality has to be restricted on a certain type of object, e.g. in tree viewers
	 * @param filterProperties the properties (i.e. fields) of the objects to be considered for filtering
	 */
	public TrpViewerFilter(Text filterTxt, Class<?> filterTarget, String... filterProperties) {
		super();
		this.filterTxt = filterTxt;
		this.filterProperties = filterProperties;
		this.filterTarget = filterTarget;
		
		addListeners();
	}
	
	private void addListeners() {
		ModifyListener filterModifyListener = new ModifyListener() {
			DelayedTask dt = new DelayedTask(() -> {
				if (filterTxt == null || filterTxt.isDisposed()) {
					return;
				}
				
				updateView();
			}, true);
			
			@Override public void modifyText(ModifyEvent e) {
				dt.start();
			}
		};
		filterTxt.addModifyListener(filterModifyListener);
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (SWTUtil.isDisposed(filterTxt)) {
			return true;
		}
		
		logger.trace("filter testing element: {}", element);
		logger.trace("parent: {}", parentElement);
		
		if(filterTarget != null 
				&& element != null
				&& !(filterTarget.isAssignableFrom(element.getClass()))) {
			//let other types pass the filter
			logger.debug("Foreign object of type {} passes filter.", element.getClass().getSimpleName());
			return true;
		}
		
		String ft = filterTxt.getText();
		logger.trace("ft = "+ft);
		if (StringUtils.isEmpty(ft)) {
			return true;
		}
		
		ft = Pattern.quote(ft);
		
		String reg = "(?i)(.*"+ft+".*)";
		logger.trace("reg = "+reg);
		
		for (String property : filterProperties) {
			try {
				String propValue = BeanUtils.getSimpleProperty(element, property);
				logger.trace("property: "+property+" value: "+propValue);
				
				if (propValue.matches(reg)) {
					return true;
				}
			} catch (Exception e) {
				logger.error("Error getting filter property '"+property+"': "+e.getMessage());
			}
		}

		return false;
	}
	
	/**
	 * This method is called upon changes in the filter input and needs to trigger updates of the view (and/or the model).
	 */
	protected abstract void updateView();
}
