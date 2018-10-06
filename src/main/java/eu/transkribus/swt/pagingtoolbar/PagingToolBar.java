package eu.transkribus.swt.pagingtoolbar;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.LabelToolItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TextToolItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;

/**
 * @author sebastianc
 *
 */
public class PagingToolBar /*extends Composite*/ {

//	private Text currentPageText;
	private TextToolItem currentPageTextItem;
	private ToolItem pageFirstBtn;
	private ToolItem pagePrevDoubleBtn;
	private ToolItem pagePrevBtn;
	private ToolItem pageNextBtn;
	private ToolItem pageNextDoubleBtn;
	private ToolItem pageLastBtn;
	private ToolItem reloadBtn;
	
	private LabelToolItem labelItem;
//	private Text nPagesLabel;
	private LabelToolItem nPagesLabelItem;
//	private TextToolItem nPagesLabelItem;
	
	private ToolBar toolbar;
//	private CoolBar toolbar;
	
	private ArrayList<ToolItem> pagingItems = new ArrayList<ToolItem>();
	
	private final int MIN_CURRENT_PAGE_WIDTH = 35;
	private boolean withDoubleButtons=true;
	private boolean withFirstLastButtons=false;
	private boolean withLabel=true;
	private boolean addEndSeparator=false;
	private boolean withReloadBtn=true;
	
	private String labelText="";
	
//	public PagingToolBar(String labelText, boolean withLabel, boolean withDoubleButtons, Composite parent, int style) {
//		this(labelText, withLabel, withDoubleButtons, true, parent, style);
//	
//	}

	public PagingToolBar(String labelText, boolean withLabel, boolean withDoubleButtons, boolean withFirstLastButtons, boolean withReloadBtn, boolean addEndSeparator, Composite parent, int style) {
		this(labelText, withLabel, withDoubleButtons, withFirstLastButtons, withReloadBtn, addEndSeparator, parent, style, null);
	}

	public PagingToolBar(String labelText, boolean withLabel, boolean withDoubleButtons, boolean withFirstLastButtons, boolean withReloadBtn, boolean addEndSeparator, Composite parent, int style, ToolBar toolBar) {
//		super(parent, style);
//		setLayout(new FillLayout());
		this.withDoubleButtons = withDoubleButtons;
		this.withFirstLastButtons = withFirstLastButtons;
		this.withLabel = withLabel;
		this.labelText = labelText;
		this.addEndSeparator = addEndSeparator;
		this.withReloadBtn = withReloadBtn;
		
		if (toolBar == null)		
			toolbar = new ToolBar(parent, SWT.FLAT | SWT.WRAP | SWT.RIGHT | style);
		else
			this.toolbar = toolBar;
		
		clearItems();
		initItems(-1);
	}
	
	private void initItems(int startIndex) {
		int i=startIndex;
		if (startIndex < 0) {
			i = toolbar.getItemCount();
		}
		
		if (this.withLabel) {
			labelItem = new LabelToolItem(toolbar, SWT.NONE, i++);	
			
			labelItem.setText(labelText);
			pagingItems.add(labelItem);
		}
		
		if (this.withFirstLastButtons) {
			pageFirstBtn = new ToolItem(toolbar, SWT.PUSH, i++);
			pageFirstBtn.setImage(Images.getOrLoad("/icons/page-first.gif"));
			pagingItems.add(pageFirstBtn);
		}
		
		if (this.withDoubleButtons) {
			pagePrevDoubleBtn = new ToolItem(toolbar, SWT.PUSH, i++);
			pagePrevDoubleBtn.setImage(Images.getOrLoad("/icons/page-prev-double.gif"));
			pagingItems.add(pagePrevDoubleBtn);
		}

		pagePrevBtn = new ToolItem(toolbar, SWT.PUSH, i++);
		pagePrevBtn.setImage(Images.getOrLoad("/icons/page-prev.gif"));
		pagingItems.add(pagePrevBtn);
		
		currentPageTextItem = new TextToolItem(toolbar, SWT.NONE, i++);
		currentPageTextItem.setAutoSelectTextOnFocus();
		currentPageTextItem.setMinWidth(MIN_CURRENT_PAGE_WIDTH);
		currentPageTextItem.setText("0");
		currentPageTextItem.getTextControl().setOrientation(SWT.RIGHT_TO_LEFT);
		pagingItems.add(currentPageTextItem);

		nPagesLabelItem = new LabelToolItem(toolbar, SWT.NONE, i++);
//		nPagesLabelItem = new TextToolItem(toolbar, SWT.NONE);
//		nPagesLabelItem.setNonEditable();
		nPagesLabelItem.setText("/0");
		nPagesLabelItem.setWidth(MIN_CURRENT_PAGE_WIDTH);
		pagingItems.add(nPagesLabelItem);
		
		pageNextBtn = new ToolItem(toolbar, SWT.PUSH, i++);
		pageNextBtn.setImage(Images.getOrLoad("/icons/page-next.gif"));
		pagingItems.add(pageNextBtn);
		
		if (this.withDoubleButtons) {
			pageNextDoubleBtn = new ToolItem(toolbar, SWT.PUSH, i++);
			pageNextDoubleBtn.setImage(Images.getOrLoad("/icons/page-next-double.gif"));
			pagingItems.add(pageNextDoubleBtn);
		}
		
		if (this.withFirstLastButtons) {
			pageLastBtn = new ToolItem(toolbar, SWT.PUSH, i++);
			pageLastBtn.setImage(Images.getOrLoad("/icons/page-last.gif"));
			pagingItems.add(pageLastBtn);
		}
		
		if (this.withReloadBtn) {
			reloadBtn = new ToolItem(toolbar, SWT.PUSH, i++);
			reloadBtn.setImage(Images.getOrLoad("/icons/refresh.gif"));
			pagingItems.add(reloadBtn);
		}
		
		if (this.addEndSeparator) {
			ToolItem pagingSeparator = new ToolItem(toolbar, SWT.SEPARATOR);
			pagingItems.add(pagingSeparator);
		}
	}
	
	private void clearItems() {
		pagingItems.stream().forEach(c -> { 
			if (!SWTUtil.isDisposed(c)) {
				c.dispose();
			}
		});
		pagingItems.clear();
	}
	
	public LabelToolItem getLabelItem() { 
		return labelItem;
	}
	
	public ToolItem addControlItem(Control control) {
		ToolItem ti = new ToolItem(toolbar, SWT.SEPARATOR);
		ti.setControl(control);
		ti.setWidth(control.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
		
		return ti;
	}
	
	public ToolBar getToolBar() { return toolbar; }
	
	public void setToolbarEnabled(boolean value) {
		for (ToolItem c : pagingItems) {
			SWTUtil.setEnabled(c, value);
		}
		if (!value) {
			setValues(0, 0);
		}
	}
	
	/**
	 * @deprecated does not work yet...
	 * @param visible
	 */
	public void setToolbarItemsVisible(boolean visible, int reinsertIndex) {
		if (!visible) {
			clearItems();
		} else if (pagingItems.isEmpty()) {
			initItems(reinsertIndex);
		}
	}
	
//	public void addListener(PagingToolBarListener listener) {
//		if (pageFirstBtn!=null && !pageFirstBtn.isDisposed()) pageFirstBtn.addSelectionListener(listener);
//		if (pagePrevDoubleBtn!=null && !pagePrevDoubleBtn.isDisposed()) pagePrevDoubleBtn.addSelectionListener(listener);
//		if (pageNextDoubleBtn!=null && !pageNextDoubleBtn.isDisposed()) pageNextDoubleBtn.addSelectionListener(listener);
//		if (pagePrevBtn!=null && !pagePrevBtn.isDisposed()) pagePrevBtn.addSelectionListener(listener);
//		if (currentPageText!=null && !currentPageText.isDisposed()) currentPageText.addKeyListener(listener);
//		if (pageNextBtn!=null && !pageNextBtn.isDisposed()) pageNextBtn.addSelectionListener(listener);
//		if (pageLastBtn!=null && !pageLastBtn.isDisposed()) pageLastBtn.addSelectionListener(listener);
//		if (reloadBtn!=null && !reloadBtn.isDisposed()) reloadBtn.addSelectionListener(listener);
//	}
	
	public void removeToolItem(ToolItem toolItem) {
		if (toolItem.getParent()!=getToolBar())
			return;
		
		toolItem.dispose();
		toolItem = null;
		
//		this.pack();	
	}
	
//	public void removeDoubleButtons() {
//		this.removeToolItem(pagePrevDoubleBtn);
//		this.removeToolItem(pageNextDoubleBtn);
//	}
//	public void removeReloadButton() {
//		this.removeToolItem(reloadBtn);
//	}
	
	public String getCurrentPageValue() {
		if (!SWTUtil.isDisposed(currentPageTextItem)) {
			return currentPageTextItem.getText();
		}
		return "";
	}
	public void setCurrentPageValue(String text) {
		if (!SWTUtil.isDisposed(currentPageTextItem)) {
			currentPageTextItem.setText(text);	
		}
	}
	
	public Text getCurrentPageText() {
		if (!SWTUtil.isDisposed(currentPageTextItem)) {
			return (Text) currentPageTextItem.getControl();	
		}
		return null;
	}

//	public Text getCurrentPageText() {
//		return currentPageText;
//	}

	public ToolItem getPageFirstBtn() {
		return pageFirstBtn;
	}

	public ToolItem getPagePrevDoubleBtn() {
		return pagePrevDoubleBtn;
	}

	public ToolItem getPagePrevBtn() {
		return pagePrevBtn;
	}
	
	public void setNPagesValue(String value) {
		if (!SWTUtil.isDisposed(nPagesLabelItem)) {
			nPagesLabelItem.setText(value);	
		}
	}
	
	public String getNPagesValue() {
		if (!SWTUtil.isDisposed(nPagesLabelItem)) {
			return nPagesLabelItem.getText();
		}
		return "";
	}	

//	public Text getNPagesLabel() {
//		return nPagesLabel;
//	}

	public ToolItem getPageNextBtn() {
		return pageNextBtn;
	}

	public ToolItem getPageNextDoubleBtn() {
		return pageNextDoubleBtn;
	}

	public ToolItem getPageLastBtn() {
		return pageLastBtn;
	}

	public ToolItem getReloadBtn() {
		return reloadBtn;
	}

	public void setValues(int currentPage, int nPages) {
		setCurrentPageValue(""+currentPage);
//		this.currentPageText.setText(""+currentPage);
		
		setNPagesValue("/"+nPages);
	}
	
	public boolean isWithDoubleButtons() {
		return withDoubleButtons;
	}
	
	public boolean isWithFirstLastButton() {
		return withFirstLastButtons;
	}



}
