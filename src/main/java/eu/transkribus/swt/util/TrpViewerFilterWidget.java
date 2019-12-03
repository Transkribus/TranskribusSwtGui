package eu.transkribus.swt.util;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;

public class TrpViewerFilterWidget extends Composite {

	public final static String FILTER_TOOLTIP = "Filter this list with a keyword";
	public final static String FILTER_MESSAGE = "Filter";
	
	protected StructuredViewer viewer;
	protected TrpViewerFilter viewerFilter;
	protected Text filterTxt;

	public TrpViewerFilterWidget(Composite parent, StructuredViewer viewer, int style, Class<?> filterTargetClass, String...fieldNames) {
		super(parent, style);
		this.viewer = viewer;
		this.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.setLayout(createLayout());
		
		Label filterLabel = new Label(this, SWT.NONE);
		filterLabel.setText("Search:");
		filterLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		filterTxt = new Text(this, SWT.BORDER | SWT.SINGLE);
		filterTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filterTxt.setMessage(FILTER_MESSAGE);
		filterTxt.setToolTipText(FILTER_TOOLTIP);
		filterTxt.addKeyListener(new KeyAdapter() {			
			@Override
			public void keyPressed(KeyEvent e) {
				if (!isDisposed() && e.keyCode == SWT.KEYPAD_CR || e.keyCode == SWT.CR) {
					refreshViewer();
				}
			}
		});
		
		//FIXME the filter should be replaced by a server API endpoint
		
		Class<?> targetClass = null;
		if(viewer instanceof TreeViewer) {
			//we only want to filter for objects in the top-level of the tree, i.e. HTR models
			targetClass = filterTargetClass;
		}
		this.viewerFilter = newTrpViewerFilter(filterTxt, targetClass, fieldNames);
		attachFilter();
	}
	
	protected TrpViewerFilter newTrpViewerFilter(Text filterTxt, Class<?> targetClass, String[] fieldNames) {
		return new TrpViewerFilter(filterTxt, targetClass, fieldNames) {
			@Override
			protected void updateView() {
				refreshViewer();
			}
		};
	}
	
	/**
	 * Creates the layout that will be set in the constructor
	 * 
	 * @return
	 */
	protected Layout createLayout() {
		return new GridLayout(2, false);
	}

	protected void refreshViewer() {
		viewer.refresh();
	}
	
	protected void attachFilter() {
		viewer.addFilter(viewerFilter);
	}

	public Text getFilterText() {
		return filterTxt;
	}
	
	public void reset() {
		filterTxt.setText("");
	}
}
