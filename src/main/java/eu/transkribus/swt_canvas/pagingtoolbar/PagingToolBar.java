package eu.transkribus.swt_canvas.pagingtoolbar;

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

import eu.transkribus.swt_canvas.util.Images;

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
	
	public PagingToolBar(String labelText, boolean withLabel, boolean withDoubleButtons, Composite parent, int style) {
		this(labelText, withLabel, withDoubleButtons, false, parent, style);
	}

	public PagingToolBar(String labelText, boolean withLabel, boolean withDoubleButtons, boolean withFirstLastButtons, Composite parent, int style) {
//		super(parent, style);
//		setLayout(new FillLayout());
		this.withDoubleButtons = withDoubleButtons;
		this.withFirstLastButtons = withFirstLastButtons;
		
		toolbar = new ToolBar(parent, SWT.FLAT | SWT.WRAP | SWT.RIGHT | style);

//		toolbar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		toolbar.setLayout(new GridLayout());
		
//		MyToolItem ti = new MyToolItem(toolbar, SWT.PUSH);
//		ti.setText("hello");
		
		if (withLabel) {
			labelItem = new LabelToolItem(toolbar, SWT.NONE);
			labelItem.setText(labelText);
			pagingItems.add(labelItem);
		}

//		System.out.println("instanceoftest: "+(labelItem instanceof ToolItem));
//		label = new Label(toolbar, SWT.READ_ONLY);
//		
////		label.setBackground(toolbar.getBackground());
////		label.setEditable(false);
////		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//		label.setText(labelText);
		
		
		
		
//		labelItem.setControl(label);
		
//		labelItem.setWidth(label.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
//		labelItem.setText("helllllllllllllllo");
//		Rectangle r = label.getBounds();
//		label.setBounds(r.x, r.y+20, r.width, r.height);
		
		
		if (withFirstLastButtons) {
		pageFirstBtn = new ToolItem(toolbar, SWT.PUSH);
		pageFirstBtn.setImage(Images.getOrLoad("/icons/page-first.gif"));
		pagingItems.add(pageFirstBtn);
		}
		
		if (withDoubleButtons) {
		pagePrevDoubleBtn = new ToolItem(toolbar, SWT.PUSH);
		pagePrevDoubleBtn.setImage(Images.getOrLoad("/icons/page-prev-double.gif"));
		pagingItems.add(pagePrevDoubleBtn);
		}

		pagePrevBtn = new ToolItem(toolbar, SWT.PUSH);
		pagePrevBtn.setImage(Images.getOrLoad("/icons/page-prev.gif"));
		pagingItems.add(pagePrevBtn);
		
		currentPageTextItem = new TextToolItem(toolbar, SWT.NONE);
		currentPageTextItem.setMinWidth(MIN_CURRENT_PAGE_WIDTH);
		currentPageTextItem.setText("0");
		currentPageTextItem.getTextControl().setOrientation(SWT.RIGHT_TO_LEFT);
		pagingItems.add(currentPageTextItem);

		nPagesLabelItem = new LabelToolItem(toolbar, SWT.NONE);
//		nPagesLabelItem = new TextToolItem(toolbar, SWT.NONE);
//		nPagesLabelItem.setNonEditable();
		nPagesLabelItem.setText("/0");
		nPagesLabelItem.setWidth(MIN_CURRENT_PAGE_WIDTH);
		pagingItems.add(nPagesLabelItem);
		
		pageNextBtn = new ToolItem(toolbar, SWT.PUSH);
		pageNextBtn.setImage(Images.getOrLoad("/icons/page-next.gif"));
		pagingItems.add(pageNextBtn);
		
		if (withDoubleButtons) {
		pageNextDoubleBtn = new ToolItem(toolbar, SWT.PUSH);
		pageNextDoubleBtn.setImage(Images.getOrLoad("/icons/page-next-double.gif"));
		pagingItems.add(pageNextDoubleBtn);
		}
		
		if (withFirstLastButtons) {
		pageLastBtn = new ToolItem(toolbar, SWT.PUSH);
		pageLastBtn.setImage(Images.getOrLoad("/icons/page-last.gif"));
		pagingItems.add(pageLastBtn);
		}
		
		reloadBtn = new ToolItem(toolbar, SWT.PUSH);
		reloadBtn.setImage(Images.getOrLoad("/icons/refresh.gif"));
		pagingItems.add(reloadBtn);
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
			if (c!=null && !c.isDisposed())
				c.setEnabled(value);
		}
		if (!value) {
			setValues(0, 0);
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
	
	public void removeDoubleButtons() {
		this.removeToolItem(pagePrevDoubleBtn);
		this.removeToolItem(pageNextDoubleBtn);
	}
	public void removeReloadButton() {
		this.removeToolItem(reloadBtn);
	}
	
//	public void setDoublePageButtonsVisible(boolean showDoublePageButtons) {
////		pagePrevDoubleBtn.setVisible(showDoublePageButtons);
////		pageNextDoubleBtn.setVisible(showDoublePageButtons);
//		
//		GridData d = null;
//		if (showDoublePageButtons)
//			d = new GridData(SWT.DEFAULT, SWT.DEFAULT);
//		else
//			d = new GridData(0, 0);
//		
//		pagePrevDoubleBtn = null;
//		
//		pageNextDoubleBtn.setEnabled(showDoublePageButtons);
//		
//		
//		
////		pagePrevDoubleBtn.setLayoutData(d);
////		pageNextDoubleBtn.setLayoutData(d);
//		pack();
//	}
	
	public String getCurrentPageValue() {
		return currentPageTextItem.getText();
	}
	public void setCurrentPageValue(String text) {
		currentPageTextItem.setText(text);
//		int nW = currentPageText.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
//		currentPageTextItem.setWidth(MIN_CURRENT_PAGE_WIDTH > nW ? MIN_CURRENT_PAGE_WIDTH : nW);
	}
	
	public Text getCurrentPageText() {
		return (Text) currentPageTextItem.getControl();
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
		nPagesLabelItem.setText(value);
//		nPagesLabelItem.setWidth(nPagesLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
		
	}
	
	public String getNPagesValue() {
		return nPagesLabelItem.getText();		
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



}
