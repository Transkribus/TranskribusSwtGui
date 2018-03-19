package eu.transkribus.swt.pagination_table;

import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import eu.transkribus.swt.util.Images;

/**
 * Extends {@link ATableWidgetPagination} with an info button in the bottom composite next to the reload panel.</br>
 * Item info button is enabled if exactly one item in the table is selected. To change that behavior override {@link #updateItemInfoBtn()}.
 * 
 * @author philip
 *
 * @param <T>
 */
public abstract class ATableWidgetPaginationWithInfoBtn<T> extends ATableWidgetPagination<T> {	
	protected final static String DEFAULT_ITEM_INFO_BTN_TXT = "Show item info";
	protected final static Image DEFAULT_ITEM_INFO_BTN_IMG = Images.ERROR;
	protected Button itemInfoBtn;
//	private final Image itemInfoBtnImg = DEFAULT_ITEM_INFO_BTN_IMG;
//	private final String itemInfoBtnTooltip = DEFAULT_ITEM_INFO_BTN_TXT;
	
	public ATableWidgetPaginationWithInfoBtn(Composite parent, int tableStyle, int initialPageSize) {
		super(parent, tableStyle, initialPageSize);
		//This does not work as createTable is called and image is set in super-constructor already...
//		this.itemInfoBtnImg = infoBtnImg == null ? DEFAULT_ITEM_INFO_BTN_IMG : infoBtnImg;
//		this.itemInfoBtnTooltip = StringUtils.isEmpty(infoBtnTooltip) ? DEFAULT_ITEM_INFO_BTN_TXT : infoBtnTooltip;
	}

	@Override
	void createTable(int style) {
		super.createTable(style);
		Composite bottom = pageableTable.getCompositeBottom();
		GridLayout gd = (GridLayout)bottom.getLayout();
		gd.numColumns++;
		bottom.setLayout(gd);
		itemInfoBtn = new Button(bottom, SWT.PUSH);
		itemInfoBtn.setImage(DEFAULT_ITEM_INFO_BTN_IMG);
		itemInfoBtn.setToolTipText(DEFAULT_ITEM_INFO_BTN_TXT);
		itemInfoBtn.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true));
		itemInfoBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final T sel = getFirstSelected();
				if(sel != null) {
					onItemInfoBtnPressed(sel);
				}
			}
		});
		updateItemInfoBtn();
		bottom.requestLayout();
		
		getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateItemInfoBtn();
			}
		});
	}
	
	/**
	 * is called when a tableitem is selected and the info button is pressed.
	 */
	protected abstract void onItemInfoBtnPressed(T item);
	
	/**
	 * Default impl sets the item info button to enabled only if exactly one item is selected.
	 */
	protected void updateItemInfoBtn() {
		List<T> selection = getSelected();
		itemInfoBtn.setEnabled(selection.size() == 1);
	}
}
