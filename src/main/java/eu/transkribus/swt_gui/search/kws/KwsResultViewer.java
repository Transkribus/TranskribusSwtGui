package eu.transkribus.swt_gui.search.kws;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.kws.TrpKeyWord;
import eu.transkribus.core.model.beans.kws.TrpKwsHit;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.Msgs;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class KwsResultViewer extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(KwsResultViewer.class);
		
	private static final boolean SHOW_PROGRESSBAR = false;
	
	final String title = Msgs.get("search.kws.results", "Keyword Spotting Results");
	final TrpKwsResultTableEntry result;

	Composite cont;
	SashForm sash;
	Group previewGrp;
	Canvas canvas;
	CTabFolder folder;
	HoverShell hShell;

	//buffer to store last hit that was previewed
	TrpKwsHit lastHoverHit;
	//store currently displayed preview image reference
	Image currentImgOrig = null;
	Image currentImgScaled = null;
	//cache for images
	volatile Map<URL, Image> cache;
	//cache for icon-sized images in table
	Map<TrpKwsHit, Image> icons;

	private List<TableViewer> tvList;

	public KwsResultViewer(Shell parent, TrpKwsResultTableEntry result) {
		super(parent);
		
		this.result = result;
		cache = new HashMap<>();
		icons = new HashMap<>();
		tvList = new ArrayList<>(result.getResult().getKeyWords().size());
	}

	public void setVisible() {
		if (super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		cont = (Composite) super.createDialogArea(parent);
		cont.setLayout(new FillLayout());
		sash = new SashForm(cont, SWT.VERTICAL);
		sash.setLayout(new FillLayout());
		int[] sashWeights = new int[]{67, 33};
		
		folder = new CTabFolder(sash, SWT.BORDER | SWT.FLAT);
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		hShell = new HoverShell(getShell());
		for (TrpKeyWord k : result.getResult().getKeyWords()) {
			createKwTab(k);
		}
		
		createPreviewArea(sash);
		
		ProgressBar pb = null;
		if(SHOW_PROGRESSBAR) {
			pb = new ProgressBar(sash, SWT.SMOOTH);
			pb.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
			pb.setMaximum(result.getResult().getTotalNrOfHits());
			sashWeights = new int[]{67, 30, 3};
		}
		
		Thread loaderThread = preloadImages(pb);
		
		
		cont.addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(DisposeEvent e) {
				logger.debug("Disposing KwsResultViewer.");
//				detach();
				if(loaderThread.isAlive()) {
					loaderThread.interrupt();
				}
				for(Entry<URL, Image> entry : cache.entrySet()) {
					entry.getValue().dispose();
				}
				for(Entry<TrpKwsHit, Image> entry : icons.entrySet()) {
					entry.getValue().dispose();
				}
			}
		});
		
		sash.setWeights(sashWeights);
		return cont;
	}

	private void createPreviewArea(Composite cont) {
		previewGrp = new Group(cont, SWT.NONE);
		previewGrp.setText(Msgs.get("search.kws.preview"));
		previewGrp.setLayout(new FillLayout());
//		previewGrp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		canvas = new Canvas(previewGrp, SWT.NONE);
		canvas.setBackground(Colors.getSystemColor(SWT.COLOR_GRAY));
		
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if(currentImgOrig != null) {
					Rectangle client = canvas.getClientArea();
					if(currentImgScaled != null) {
						currentImgScaled.dispose();
					}
					currentImgScaled = Images.resize(currentImgOrig, client.width, client.height,
							Colors.getSystemColor(SWT.COLOR_GRAY));
					Rectangle imgBounds = currentImgScaled.getBounds();
					final int xOffset = (client.width - imgBounds.width) / 2; 
					e.gc.drawImage(currentImgScaled, xOffset, 0);
				}
			}
		});
	    
	    canvas.addListener (SWT.Resize,  e -> {
	    	if(currentImgOrig != null) {
				canvas.redraw ();
	    	}
		});
	}

	private Thread preloadImages(ProgressBar pb) {
		Runnable loader = new Runnable() {
			private final static int TABLE_COLUMN_HEIGHT = 23; //for the icons
			
			@Override
			public void run() {
				int i = 0;
				for(TrpKeyWord k : result.getResult().getKeyWords()){
					for(TrpKwsHit h : k.getHits()) {
						URL imgUrl = h.getImgUrl();
						Image img = ImageDescriptor.createFromURL(imgUrl).createImage();
						cache.put(imgUrl, img);
						final int work = i++;
						if(Thread.currentThread().isInterrupted()) {
							logger.debug("Loader thread exits on interrupt.");
							return;
						}
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								if(pb != null && !pb.isDisposed() && !Thread.currentThread().isInterrupted()) {
									pb.setSelection(work);
								}
								for(TableViewer tv : tvList) {
									for(int j = 0; j < tv.getTable().getItemCount(); j++) {
										TableItem tableItem = tv.getTable().getItem(j);
										TrpKwsHit item = (TrpKwsHit)tableItem.getData();
										if(item.equals(h)) {
											Image icon = Images.resize(img, 
													Integer.MAX_VALUE,
													TABLE_COLUMN_HEIGHT);
											tableItem.setText(3, "");
											tableItem.setImage(3, icon);
											icons.put(item, icon);
										}										
									}
								}
							}
						});
					}
				}
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						if(pb != null && !pb.isDisposed() && !Thread.currentThread().isInterrupted()) {
							pb.setVisible(false);
						}
					}
				});
			}
		};
		Thread t = new Thread(loader, "KWS Hit Image Loader Thread");
		t.start();
		return t;
	}

	private void createKwTab(TrpKeyWord k) {
		CTabItem item = new CTabItem(folder, SWT.NONE);
		item.setText("\"" + k.getKeyWord() + "\" (" + k.getHits().size() + " hits)");

		Composite c = new Composite(folder, SWT.NONE);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		c.setLayout(new GridLayout(1, false));

		KwsHitTableWidget hitTableWidget = new KwsHitTableWidget(c, SWT.BORDER, icons);
		hitTableWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		hitTableWidget.getTableViewer().setInput(k.getHits());
		
		tvList.add(hitTableWidget.getTableViewer());
		
		addHoverListeners(hitTableWidget.getTableViewer().getTable());
		item.setControl(c);
		
		addDoubleClickListener(hitTableWidget.getTableViewer());
	}

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
		super.setShellStyle(SWT.MIN | SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
		// setBlockOnOpen(false);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	private class MouseMoveListener implements Listener {
		final Table table;
		public MouseMoveListener(Table resultTable) {
			this.table = resultTable;
		}
		
		public void handleEvent(Event e) {
			Point p = new Point(e.x, e.y);

			TableItem hoverItem = table.getItem(p);
			TrpKwsHit currentHit;
//			logger.debug(""+hoverItem);
			if (hoverItem != null 
					&& (currentHit = ((TrpKwsHit) hoverItem.getData())) != null
					&& !currentHit.equals(lastHoverHit)) {

				URL imgUrl = currentHit.getImgUrl();
				if(cache.containsKey(imgUrl)) {
					currentImgOrig = cache.get(imgUrl);
					canvas.redraw();
				}
				lastHoverHit = currentHit;
			}
		}
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
	
	
	private class HoverShellMouseMoveListener implements Listener {
		final Table table;
		public HoverShellMouseMoveListener(Table resultTable) {
			this.table = resultTable;
		}
		
		public void handleEvent(Event e) {
			logger.debug("handling hover event...");
			Point p = new Point(e.x, e.y);

			TableItem hoverItem = table.getItem(p);
			TrpKwsHit currentHit;
//			logger.debug(""+hoverItem);
			if (hoverItem != null 
					&& (currentHit = ((TrpKwsHit) hoverItem.getData())) != null
					&& !currentHit.equals(lastHoverHit)) {
				
				Point mousePos = Display.getCurrent().getCursorLocation();
				URL imgUrl = currentHit.getImgUrl();
				if(cache.containsKey(imgUrl)) {
					currentImgOrig = cache.get(imgUrl);
					canvas.redraw();
					hShell.imgLabel.setImage(currentImgOrig);
					
					hShell.hoverShell.setVisible(true);
				}
				
				final int xPos, yPos;
				//set location next to the mouse pointer
				xPos = mousePos.x + 20;
				yPos = mousePos.y - 20;
				
				hShell.hoverShell.setLocation(xPos, yPos);
				hShell.hoverShell.pack();
				hShell.hoverShell.redraw();
				lastHoverHit = currentHit;

			} else {
				hShell.hoverShell.setVisible(false);
			}
		}
	}

}
