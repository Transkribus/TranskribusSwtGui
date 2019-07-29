package eu.transkribus.swt.util;

import java.io.IOException;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.mytableviewer.ColumnConfig;

public abstract class AImagePreviewListViewer<T> extends APreviewListViewer<T> {
	private static final Logger logger = LoggerFactory.getLogger(AImagePreviewListViewer.class);
	
	protected Label imgLabel;
	protected Image selectedImage = null;
	protected boolean renderOriginalImages;
	
	public AImagePreviewListViewer(Composite parent, int style, ColumnConfig[] columns,
			ITableLabelProvider labelProvider, boolean showUpDownBtns, boolean withCheckboxes, boolean showPreview) {
		super(parent, style, columns, labelProvider, showUpDownBtns, withCheckboxes, showPreview);
	}	

	@Override protected Control createPreviewArea(Composite previewContainer) {
		imgLabel = new Label(previewContainer, SWT.BORDER);
		GridData gd = new GridData(SWT.CENTER, SWT.BOTTOM, true, false, 1, 1);
		gd.widthHint = 120;
		gd.heightHint = 180;
		imgLabel.setLayoutData(gd);
		imgLabel.addPaintListener(new PaintListener() {
			@Override public void paintControl(PaintEvent e) {
				if (selectedImage != null && !selectedImage.isDisposed()) {
//					logger.debug("repaining, size of label  = "+imgLabel.getSize());
					
//					e.gc.setBackground(Colors.getSystemColor(SWT.COLOR_CYAN));
					
					e.gc.setInterpolation(SWT.HIGH);
					
					int srcWidth = selectedImage.getImageData().width;
					int srcHeight = selectedImage.getImageData().height;
					
					double sf = (double) imgLabel.getSize().y / (double) srcHeight;
					int destWidth = (int)(sf * srcWidth);
					e.gc.drawImage(selectedImage, 0, 0, srcWidth, srcHeight, 0, 0, destWidth, imgLabel.getSize().y);
					
//					Pair<Boolean, Float> scaleInfo = SWTCanvas.getScaleToInfo(new Rectangle(0, 0, imgLabel.getSize().x, imgLabel.getSize().y), 
//							new Rectangle(0, 0, srcWidth, srcHeight));
//					if (scaleInfo.getLeft()) { // scale to width
//						int destHeight = (int)(scaleInfo.getRight() * srcHeight);
//						e.gc.drawImage(selectedImage, 0, 0, srcWidth, srcHeight, 0, 0, imgLabel.getSize().x, destHeight);
//					}
//					else {
//						int destWidth = (int)(scaleInfo.getRight() * srcWidth);
//						e.gc.drawImage(selectedImage, 0, 0, srcWidth, srcHeight, 0, 0, destWidth, imgLabel.getSize().y);
//					}
				} else {
//					e.gc.drawImage(Images.LOCK, 0, 0);
					e.gc.drawText("No image selected", 0, 0);
				}
			}
		});
		
		return imgLabel;
	}
	
	@Override protected void reloadPreviewForSelection() {
		if (previewControl == null || imgLabel == null) {
			return;
		}
		
		if ( !showPreview || !showPreviewBtn.getSelection() ) {
//			selectedImage = Images.ERROR_IMG;
			selectedImage = null;
			imgLabel.redraw();
			return;
		}
		T selected = getFirstSelected();
		logger.debug("reloading image for element: "+selected);
		// this line caused errors when the dialog was called more than once, so omit!
		// SWTUtil.dispose(selectedImage);
		selectedImage = null;
		if (selected == null && dataList != null && dataList.size() > 0) {
			//if no page is selected in GUI show the first page on canvas
			selected = dataList.get(0);
		}
		
		try {
			Image img = loadImageForData(selected);
			selectedImage = img;
		} catch (IOException e) {
			selectedImage = Images.ERROR_IMG;
		}
		
		imgLabel.redraw();
	}
	
	abstract protected Image loadImageForData(T data) throws IOException;

}
