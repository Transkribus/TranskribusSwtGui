package eu.transkribus.swt.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;

public abstract class APreviewListViewer<T> extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(ImgUrlListViewer.class);
	
	MyTableViewer tv;
	Label imgLabel;
	Label titleLabel;
	
	List<T> dataList;
	Button upBtn, downBtn;
	Composite tableContainer;
	Button showPreviewBtn;
	SashForm sf;
	
	Image selectedImage = null;
	
	protected boolean showUpDownBtns;
	protected boolean withCheckboxes;
	protected boolean renderOriginalImages;
	protected boolean showPreviewImage;
		
	public APreviewListViewer(Composite parent, int style, ColumnConfig[] columns, ITableLabelProvider labelProvider, boolean showUpDownBtns, boolean withCheckboxes, boolean renderOriginalImages) {
		this(parent, style, columns, labelProvider, showUpDownBtns, withCheckboxes, renderOriginalImages, true);
	}

	public APreviewListViewer(Composite parent, int style, ColumnConfig[] columns, ITableLabelProvider labelProvider, boolean showUpDownBtns, boolean withCheckboxes, boolean renderOriginalImages, boolean showPreviewImage) {
		super(parent, style);
		this.setLayout(new GridLayout(1, false));
		
		this.showUpDownBtns = showUpDownBtns;
		this.withCheckboxes = withCheckboxes;
		this.renderOriginalImages = renderOriginalImages;
		this.showPreviewImage = showPreviewImage;
		
		boolean hasBtns = showUpDownBtns || withCheckboxes;
		
		int tableStyle = SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION;
		if (withCheckboxes)
			tableStyle |= SWT.CHECK;
		
		titleLabel = new Label(SWTUtil.dummyShell, 0);
		titleLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		if (this.showPreviewImage) {
			showPreviewBtn = new Button(this, SWT.CHECK);
			showPreviewBtn.setText("Show preview");
			showPreviewBtn.setSelection(true);
			showPreviewBtn.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent event) {
					// TODO Auto-generated method stub
					togglePreview(showPreviewBtn.getSelection());
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
					// TODO Auto-generated method stub
					togglePreview(showPreviewBtn.getSelection());
				}
			});
		}
		
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
		
		tv.setLabelProvider(labelProvider);
		
		tv.addColumns(columns);
		tv.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override public void selectionChanged(SelectionChangedEvent event) {
				reloadImageForSelection();
			}
		});
		
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
					}
				};
				
				upBtn.addSelectionListener(upDownSelLis);
				downBtn.addSelectionListener(upDownSelLis);
			}
			
		}
		
		if (this.showPreviewImage) {
			imgLabel = new Label(sf, SWT.BORDER);
			GridData gd = new GridData(SWT.CENTER, SWT.BOTTOM, true, false, 1, 1);
			gd.widthHint = 120;
			gd.heightHint = 180;
			imgLabel.setLayoutData(gd);
			imgLabel.addPaintListener(new PaintListener() {
				@Override public void paintControl(PaintEvent e) {
					if (selectedImage != null && !selectedImage.isDisposed()) {
						e.gc.setInterpolation(SWT.HIGH);
						
						int srcWidth = selectedImage.getImageData().width;
						int srcHeight = selectedImage.getImageData().height;
						
						double sf = (double) imgLabel.getSize().y / (double) srcHeight;
						int destWidth = (int)(sf * srcWidth);
						e.gc.drawImage(selectedImage, 0, 0, srcWidth, srcHeight, 0, 0, destWidth, imgLabel.getSize().y);
					} else {
						e.gc.drawImage(Images.LOCK, 0, 0);
						e.gc.drawText("No image selected", 0, 0);
					}
				}
			});
			sf.setWeights(new int[] { 2, 1} );
		}
		
		this.addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(DisposeEvent e) {
				SWTUtil.dispose(selectedImage);
			}
		});
	}
	
	T getFirstSelected() {
		IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
		if (sel.isEmpty())
			return null;
		else
			return (T) sel.getFirstElement();
	}
	
	protected abstract Image loadImageForData(T data) throws IOException;
		
	void reloadImageForSelection() {
		if ( !showPreviewImage || !showPreviewBtn.getSelection() ) {
			selectedImage = Images.ERROR_IMG;
			if(imgLabel != null) {
				imgLabel.redraw();
			}
			return;
		}
		T selected = getFirstSelected();
		logger.debug("reloading image for element: "+selected);
		SWTUtil.dispose(selectedImage);
		selectedImage = null;
		if (selected == null) {
			//if no page is selected in GUI show the first page on canvas
			selected = dataList.get(0);
		}
		
		try {
			Image img = loadImageForData(selected);
			selectedImage = img;
		} catch (IOException e) {
			selectedImage = Images.ERROR_IMG;
		}
		
		if (imgLabel != null) {
			imgLabel.redraw();
		}
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
	
	public void setDataList(List<T> dataList) {
		Assert.assertNotNull("dataList cannot be null!", dataList);
		
		this.dataList = new ArrayList<>(dataList);	
		reloadList(true);
	}
	
	public List<T> getDataList() {
		return dataList;
	}
		
	void reloadList(boolean initCheckState) {
		tv.setInput(dataList);
		
		if (initCheckState)
			selectAll(true);
				
		reloadImageForSelection();
	}
	
	void selectAll(boolean checked) {
		for (TableItem ti : tv.getTable().getItems()) {
			ti.setChecked(checked);
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

	public void togglePreview(boolean newState) {
		showPreviewBtn.setSelection(newState);
		if (!newState && imgLabel != null) {
			selectedImage = null;
		}
		reloadImageForSelection();
	}
}

