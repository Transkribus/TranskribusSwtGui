package eu.transkribus.swt_gui.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.ThumbnailWidgetVirtualMinimal;
import eu.transkribus.swt_gui.canvas.CanvasImage;
import eu.transkribus.swt_gui.canvas.SWTCanvas;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class DocImgViewerDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(TextRecognitionConfigDialog.class);

	private Storage store = Storage.getInstance();

	private ThumbnailWidgetVirtualMinimal tw;
	private SWTCanvas canvas;
	
	private Button prevPageBtn, nextPageBtn;

	private final TrpDoc doc;
	private final String title;
	
	private int currPageIndex = 0;

	public DocImgViewerDialog(Shell parent, final String title, TrpDoc doc) {
		super(parent);
		this.title = title;
		this.doc = doc;
	}

	public void setVisible() {
		if (super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		cont.setLayout(new GridLayout(1, false));
		
		Composite btnCont = new Composite(cont, SWT.NONE);
		btnCont.setLayout(new GridLayout(10, false));
		btnCont.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,true, false));
		
		prevPageBtn = new Button(btnCont, SWT.PUSH);
		prevPageBtn.setImage(Images.PAGE_PREV);
		prevPageBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadImg(doc.getPages().get(--currPageIndex));
			}
		});
		
		nextPageBtn = new Button(btnCont, SWT.PUSH);
		nextPageBtn.setImage(Images.PAGE_NEXT);
		nextPageBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadImg(doc.getPages().get(++currPageIndex));
			}
		});
		
		Button zoomInBtn = new Button(btnCont, SWT.PUSH);
		zoomInBtn.setImage(Images.getOrLoad("/icons/zoom_in.png"));
		zoomInBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				canvas.zoomIn();
			}
		});
		
		Button zoomOutBtn = new Button(btnCont, SWT.PUSH);
		zoomOutBtn.setImage(Images.getOrLoad("/icons/zoom_out.png"));
		zoomOutBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				canvas.zoomOut();
			}
		});
		
		Button fitToPageBtn = new Button(btnCont, SWT.PUSH);
		fitToPageBtn.setImage(Images.getOrLoad("/icons/arrow_in.png"));
		fitToPageBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				canvas.fitToPage();
			}
		});
		
		
		
		SashForm mainSash = new SashForm(cont, SWT.HORIZONTAL);
		mainSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainSash.setLayout(new GridLayout(2, false));

		tw = new ThumbnailWidgetVirtualMinimal(mainSash, false, SWT.BORDER);
		tw.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tw.setDoc(doc, false);
		
		tw.getGallery().addListener(SWT.MouseDoubleClick, new Listener() {
			@Override
			public void handleEvent(Event event) {
				List<TrpPage> selList = tw.getSelection();
				if(!selList.isEmpty()) {
					loadImg(selList.get(0));
				}
			}
		});

		canvas = new SWTCanvas(mainSash, SWT.NONE);
		
		loadImg(doc.getPages().get(currPageIndex));	

		mainSash.setWeights(new int[] { 20, 80 });
		
		return cont;
	}

	private void loadImg(TrpPage p) {
		try {
			canvas.getScene().setMainImage(new CanvasImage(p.getUrl()));
		} catch (Exception e1) {
			logger.debug("Could not load image for page: " + p);
			DialogUtil.showErrorMessageBox(this.getParentShell(), "Error loading image", "Could not load image!");
		}
		
		nextPageBtn.setEnabled(p.getPageNr() != doc.getPages().size());
		prevPageBtn.setEnabled(p.getPageNr() != 1);
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
		newShell.setMinimumSize(800, 600);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(1024, 768);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
		// setBlockOnOpen(false);
	}
}
