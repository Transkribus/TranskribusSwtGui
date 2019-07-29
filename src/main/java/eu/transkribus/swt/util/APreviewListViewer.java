package eu.transkribus.swt.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;

public abstract class APreviewListViewer<T> extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(ImgUrlListViewer.class);
	
	protected MyTableViewer tv;
//	protected Label imgLabel;
	protected Control previewControl;
	protected Label titleLabel;
	
	protected List<T> dataList;
	protected Button upBtn, downBtn, sortBtn;
	protected Composite tableContainer;
	protected Composite previewContainer;
	protected Button showPreviewBtn;
	protected SashForm sf;
	
	protected boolean showUpDownBtns;
	protected boolean withCheckboxes;
	protected boolean showPreview;

	protected List<PreviewListViewerListener> listener = new ArrayList<>();
	protected Comparator<T> comparator = null;
	
	public interface PreviewListViewerListener {
		void sortingChanged();
		void checkStatesChanged();
	}
		
	public APreviewListViewer(Composite parent, int style, ColumnConfig[] columns, ITableLabelProvider labelProvider, boolean showUpDownBtns, boolean withCheckboxes) {
		this(parent, style, columns, labelProvider, showUpDownBtns, withCheckboxes, true);
	}

	public APreviewListViewer(Composite parent, int style, ColumnConfig[] columns, ITableLabelProvider labelProvider, boolean showUpDownBtns, boolean withCheckboxes, boolean showPreview) {
		super(parent, style);
		this.setLayout(new GridLayout(1, false));
		
		this.showUpDownBtns = showUpDownBtns;
		this.withCheckboxes = withCheckboxes;
		this.showPreview = showPreview;
		
		boolean hasBtns = showUpDownBtns || withCheckboxes;
		
		int tableStyle = SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION;
		if (withCheckboxes) {
			tableStyle |= SWT.CHECK;
		}
		
		titleLabel = new Label(SWTUtil.dummyShell, 0);
		titleLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		sf = new SashForm(this, SWT.VERTICAL);
		sf.setLayout(new GridLayout(1, false));
		sf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		tableContainer = new Composite(sf, 0);
		tableContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableContainer.setLayout(new FillLayout(SWT.HORIZONTAL));
		tableContainer.setLayout(new GridLayout(2, false));
				
		tv = new MyTableViewer(tableContainer, tableStyle);
		tv.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, hasBtns ? 1 : 2, 1));
		tv.getTable().setHeaderVisible(true);
		tv.setContentProvider(new ArrayContentProvider());
		
		if (labelProvider!=null) {
			tv.setLabelProvider(labelProvider);
		}
		
		tv.addColumns(columns);
		tv.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override public void selectionChanged(SelectionChangedEvent event) {
				reloadPreviewForSelection();
			}
		});
		
		if (withCheckboxes) {
			tv.getTable().addListener(SWT.Selection, (e) -> {      
			    if (e.detail == SWT.CHECK) {
			    	onCheckStatesChanged();
			    }
			});
		}
		
		// makes sure that sorting the table via the column buttons does not clear checkboxes and sends events:
		for (int i=0; i<tv.getTable().getColumnCount(); ++i) {
			TableColumn tc = tv.getTable().getColumn(i);
			Listener[] listener = tc.getListeners(SWT.Selection);
			logger.debug("got "+listener.length+" selection listener");
			for (Listener l : listener) {
				tc.removeListener(SWT.Selection, l);
				
				tc.addListener(SWT.Selection, event -> {
						preserveSelection(() -> {
							l.handleEvent(event);
						});
						onSortingChanged();
					});
			}
		}		
		
		if (hasBtns) {
			Composite btns = new Composite(tableContainer, 0);
			btns.setLayout(new RowLayout(SWT.VERTICAL));
			btns.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true, 1, 1));
			
			if (withCheckboxes) {
				final Button selectAllBtn = new Button(btns, SWT.PUSH);
				selectAllBtn.setImage(Images.TICK);
				selectAllBtn.setToolTipText("Select all");
				
				final Button deselectAllBtn = new Button(btns, SWT.PUSH);
				deselectAllBtn.setImage(Images.CROSS);
				deselectAllBtn.setToolTipText("Deselect all");
				
				SelectionAdapter selectAllLis = new SelectionAdapter() {
					@Override public void widgetSelected(SelectionEvent event) {
						selectAll(event.getSource() == selectAllBtn);
					}
				};
				
				selectAllBtn.addSelectionListener(selectAllLis);
				deselectAllBtn.addSelectionListener(selectAllLis);
			}
			
			if (showUpDownBtns) {
				upBtn = new Button(btns, SWT.PUSH);
				upBtn.setImage(Images.ARROW_UP);
				upBtn.setToolTipText("Move page up");
	
				downBtn = new Button(btns, SWT.PUSH);
				downBtn.setImage(Images.ARROW_DOWN);
				upBtn.setToolTipText("Move page down");
				
				// not really required, since sorting can be done via column buttons
//				sortBtn = new Button(btns, SWT.PUSH);
//				sortBtn.setImage(Images.TABLE_SORT);
//				sortBtn.setToolTipText("Sort according to filename");
//				SWTUtil.onSelectionEvent(sortBtn, e -> {
//					sortDataList();
//				});
				
				SelectionAdapter upDownSelLis = new SelectionAdapter() {
					@Override public void widgetSelected(SelectionEvent event) {
						T url = getFirstSelected();
						if (url == null)
							return;
						
						int index = dataList.indexOf(url);
						if (index == -1)
							return;
	
						int sign = event.getSource() == upBtn ? -1 : 1;
						logger.debug("sign = "+sign+" index = "+index);
						
						int iSwap = index + sign*1;
						if (iSwap < 0 || iSwap >= dataList.size())
							return;
	
						boolean c1 = tv.getTable().getItem(index).getChecked();
						boolean c2 = tv.getTable().getItem(iSwap).getChecked();							
						
						dataList.remove(index);
						dataList.add(iSwap, url);
						tv.refresh(false, true);
						
						tv.getTable().getItem(index).setChecked(c2);
						tv.getTable().getItem(iSwap).setChecked(c1);
						
						onSortingChanged();
					}
				};
				
				upBtn.addSelectionListener(upDownSelLis);
				downBtn.addSelectionListener(upDownSelLis);
			}
			
		}
		
		if (this.showPreview) {
			showPreviewBtn = new Button(tableContainer, SWT.CHECK);
			showPreviewBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 1));
			showPreviewBtn.setText("Show preview");
			showPreviewBtn.setSelection(true);
			showPreviewBtn.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					togglePreview(showPreviewBtn.getSelection());
				}
				
//				@Override
//				public void widgetDefaultSelected(SelectionEvent arg0) {
//					togglePreview(showPreviewBtn.getSelection());
//				}
			});
			
			previewContainer = new Composite(sf, 0);
			previewContainer.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));			
			previewControl = createPreviewArea(previewContainer);

			sf.setWeights(new int[] { 2, 1} );
		}
	}
	
	abstract protected Control createPreviewArea(Composite previewContainer);
	
	abstract protected void reloadPreviewForSelection();
	
	/**
	 * Performs an operation, specified as a Runnable, preserving the original check states
	 */
	protected void preserveSelection(Runnable r) {
		List<T> checked = getCheckedDataList();
		r.run();
		setCheckedElements(checked);
	}
	
	public void setComparator(Comparator<T> comparator) {
		this.comparator = comparator;
	}
	
	protected void sortDataList() {
		preserveSelection(() -> {
			dataList.sort(comparator);
			tv.refresh(false, true);
			onSortingChanged();
		});
	}

	public T getFirstSelected() {
		IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
		if (sel.isEmpty())
			return null;
		else
			return (T) sel.getFirstElement();
	}
	
	protected void onSortingChanged() {
		logger.debug("onSortingChanged");
		for (PreviewListViewerListener l : listener) {
			l.sortingChanged();
		}
	}
	
	protected void onCheckStatesChanged() {
		logger.debug("onCheckStatesChanged");
		for (PreviewListViewerListener l : listener) {
			l.checkStatesChanged();
		}
	}	
	
	public void addListener(PreviewListViewerListener l) {
		if (!listener.contains(l)) {
			listener.add(l);
		}
	}
	
	public boolean removeListener(PreviewListViewerListener l) {
		return listener.remove(l);
	}
	
	public void setLabelProvider(ITableLabelProvider labelProvider) {
		tv.setLabelProvider(labelProvider);
	}
	
//	void reloadSelectedImage() {
//		TrpPage p = getSelectedPage();
//		logger.debug("reloading selected image for page: "+p);
//		SWTUtil.tryDelete(imgLabel.getImage());
//		if (p == null) {
//			imgLabel.setImage(null);
//			return;
//		}
//		
//		// TODO: scale image...
//		
//		try {
////			Image img = Image.
////			img.getImageData().scaledTo(width, height)
//			Image img = ImgLoader.load(p.getThumbUrl());
//			logger.info("returned image: "+img);
//			imgLabel.setImage(img);
//		} catch (IOException e) {
//			logger.warn("Error loading image: "+e.getMessage());
//			if (imgLabel.getImage() != Images.ERROR_IMG) // setting same image multiple times leads to core dump error...
//				imgLabel.setImage(Images.ERROR_IMG);
//		}
//	}
	
	public void setTitle(String title) {
		if (StringUtils.isEmpty(title))
			titleLabel.setParent(SWTUtil.dummyShell);
		else {
			titleLabel.setText(title);
			titleLabel.setParent(this);
			titleLabel.moveAbove(sf);
		}
		layout();
	}
	
	public Label getTitleLabel() {
		return titleLabel;
	}
	
	public void setDataList(List<T> dataList) {
		Assert.assertNotNull("dataList cannot be null!", dataList);
		
		this.dataList = new ArrayList<>(dataList);	
		reloadList(true);
	}
	
	public List<T> getDataList() {
		return dataList;
	}
	
	public int indexOf(T data) {
		return dataList==null ? -1 : dataList.indexOf(data);
	}
		
	void reloadList(boolean initCheckState) {
		tv.setInput(dataList);
		
		if (initCheckState)
			selectAll(true);
				
		reloadPreviewForSelection();
	}
	
	void selectAll(boolean checked) {
		for (TableItem ti : tv.getTable().getItems()) {
			ti.setChecked(checked);
		}
		
		onCheckStatesChanged();
	}
	
	public void selectFromList(List<Integer> checked) {

		int idx = 0;
		for (TableItem ti : tv.getTable().getItems()) {

			ti.setChecked(checked.contains(idx++));
			
		}
		
	}
	
	public List<Boolean> getCheckedList() {
		List<Boolean> checked = new ArrayList<>();
		for (TableItem ti : tv.getTable().getItems()) {
			checked.add(ti.getChecked());
		}
		
		return checked;
	}
	
	public List<T> getCheckedDataList() {
		List<T> checked = new ArrayList<>();

		for (TableItem ti : tv.getTable().getItems()) {
			if (ti.getChecked()) {
				checked.add((T) ti.getData());
			}
		}
		
		return checked;
	}
	
	public void setCheckedElements(List<T> checked) {
		for (TableItem ti : tv.getTable().getItems()) {
			ti.setChecked(checked.contains((T) ti.getData()));
		}
	}	

	public void togglePreview(boolean newState) {
		showPreviewBtn.setSelection(newState);
		reloadPreviewForSelection();
		
		if (!showPreviewBtn.getSelection()) {
			previewControl.setParent(SWTUtil.dummyShell);
			previewContainer.layout();
			sf.setWeights(new int[] { 1, 0} );
		}
		else {
			previewControl.setParent(previewContainer);
			previewControl.moveBelow(showPreviewBtn);
			sf.setWeights(new int[] { 2, 1} );
		}
	}
}

