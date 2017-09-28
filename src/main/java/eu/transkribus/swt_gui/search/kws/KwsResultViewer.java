package eu.transkribus.swt_gui.search.kws;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.kws.TrpKeyWord;
import eu.transkribus.core.model.beans.kws.TrpKwsHit;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class KwsResultViewer extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(KwsResultViewer.class);
	final String title = "Keyword Spotting Results";
	final TrpKwsResultTableEntry result;

	Composite cont;
	CTabFolder folder;
	HoverShell hShell;

	//buffer to store last hit that was previewed
	TrpKwsHit lastHoverHit;
	//cache for images
	volatile Map<URL, Image> cache;

	public KwsResultViewer(Shell parent, TrpKwsResultTableEntry result) {
		super(parent);
		this.result = result;
		cache = new HashMap<>();
	}

	public void setVisible() {
		if (super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		cont = (Composite) super.createDialogArea(parent);
		cont.setLayout(new GridLayout(1, false));
		folder = new CTabFolder(cont, SWT.BORDER | SWT.FLAT);
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		hShell = new HoverShell(getShell());
		for (TrpKeyWord k : result.getResult().getKeyWords()) {
			createKwTab(k);
		}
		
//		preloadImages();
		
		return cont;
	}

	private void preloadImages() {
		Runnable loader = new Runnable() {
			@Override
			public void run() {
				result.getResult().getKeyWords()
					.stream()
					.forEach(k -> {
						k.getHits()
						.stream()
						.forEach(h -> getImage(h.getImgUrl()));
					});				
			}
		};
		Display.getDefault().asyncExec(loader);
//		new Thread(loader, "KWS Hit Image Loader Thread").start();
	}

	private void createKwTab(TrpKeyWord k) {
		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText("\"" + k.getKeyWord() + "\" (" + k.getHits().size() + " hits)");

		Composite c = new Composite(folder, SWT.NONE);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		c.setLayout(new GridLayout(1, false));

		KwsHitTableWidget hitTableWidget = new KwsHitTableWidget(c, SWT.BORDER);
		hitTableWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		hitTableWidget.getTableViewer().setInput(k.getHits());
		
		for(TableItem i : hitTableWidget.getTableViewer().getTable().getItems()) {
			//setGrayed->false when image is loaded
			i.setGrayed(true);
		}
		addHoverListeners(hitTableWidget.getTableViewer().getTable());
		item.setControl(c);
		
		addDoubleClickListener(hitTableWidget.getTableViewer());
		
//		loadImages(hitTableWidget.getTableViewer().getTable());
	}

	private void loadImages(Table table) {
		Runnable loader = new Runnable() {
			@Override
			public void run() {
				for(TableItem i : table.getItems()) {
					URL url = ((TrpKwsHit) i.getData()).getImgUrl();
					logger.debug("loading: " + url);
					getImage(url);
					i.setGrayed(false);	
				}
			}
		};
		new Thread(loader, "KWS image loader").start();
	}

	//		resultTable.addListener(SWT.MouseEnter, new Listener() {
//		public void handleEvent(Event e) {
//			// Do nothing
//		}
//	});
	private void addHoverListeners(Table hitTable) {
		hitTable.addListener(SWT.MouseExit, new Listener() {
			public void handleEvent(Event e) {
				hShell.hoverShell.setVisible(false);
			}
		});
		hitTable.addListener(SWT.MouseMove, new MouseMoveListener(hitTable));
	}
	
	private void addDoubleClickListener(TableViewer table) {
		table.addDoubleClickListener(new IDoubleClickListener(){
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Object el = selection.getFirstElement();
				logger.debug("double click on element: "+el);
				TrpLocation loc;
				if (el instanceof TrpKwsHit){
					loc = new TrpLocation();
					TrpKwsHit h = ((TrpKwsHit)el);
					loc.collectionId = h.getColId();
					loc.docId = h.getDocId();
					loc.pageNr = h.getPageNr();	
					loc.shapeId = h.getLineId();
				} else {
					loc = null;
				}
				TrpMainWidget.getInstance().showLocation(loc);
			}
		});
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}
	@Override
	public boolean close() {
		logger.debug("Closing viewer");
		//dispose all images
		cache.entrySet().stream().forEach(e -> e.getValue().dispose());
		return super.close();
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

	@Override
	protected boolean isResizable() {
		return true;
	}

	private class HoverShell {
		Shell hoverShell;
		Label imgLabel;
		Image wordPrev;

		public HoverShell(Shell shell) {
			hoverShell = new Shell(shell, SWT.ON_TOP | SWT.TOOL);
			hoverShell.setLayout(new FillLayout());
			imgLabel = new Label(hoverShell, SWT.NONE);
			imgLabel.setImage(wordPrev);
		}
	}
	
	private class MouseMoveListener implements Listener {
		final Table table;
		public MouseMoveListener(Table resultTable) {
			this.table = resultTable;
		}
		
		public void handleEvent(Event e) {
//			logger.debug("handling hover event...");
			Point p = new Point(e.x, e.y);

			TableItem hoverItem = table.getItem(p);
//			logger.debug(""+hoverItem);
			if (hoverItem != null && !hoverItem.getGrayed()) {

				TrpKwsHit currentHit = ((TrpKwsHit) hoverItem.getData());
				Point mousePos = Display.getCurrent().getCursorLocation();
				if (currentHit != null && !currentHit.equals(lastHoverHit)) {
					
					URL imgUrl = currentHit.getImgUrl();
					try {
						final Image img = getImage(imgUrl);
						hShell.imgLabel.setImage(img);
						hShell.hoverShell.setVisible(true);
					} catch (Exception ex) {
						logger.error("Could not load image", ex);
						hShell.imgLabel.setText("Could not load preview image");
					}

				}
				hShell.hoverShell.setLocation(mousePos.x + 20, mousePos.y - 20);
				hShell.hoverShell.pack();
				hShell.hoverShell.redraw();
				lastHoverHit = currentHit;

			} else {
				hShell.hoverShell.setVisible(false);
			}
		}
	}

	private Image getImage(URL imgUrl) {
		final Image img;
		if(cache.containsKey(imgUrl)) {
			img = cache.get(imgUrl); 
		} else {
			img = ImageDescriptor.createFromURL(imgUrl).createImage();
			cache.put(imgUrl, img);
		}
		return img;
	}
}
