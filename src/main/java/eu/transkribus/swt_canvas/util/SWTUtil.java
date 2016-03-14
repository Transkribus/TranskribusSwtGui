package eu.transkribus.swt_canvas.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.io.LocalDocConst;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.util.ImgUtils;
import math.geom2d.Vector2D;

public class SWTUtil {
	private final static Logger logger = LoggerFactory.getLogger(SWTUtil.class);
	
	public static void mask2(final Composite c) {
		c.setEnabled(false);
		
		Control children[] = c.getChildren();
		Control lastChild = null;
		if (children.length > 0) {
			lastChild = children[children.length-1];
		}

		final Image img = Images.LOADING_IMG;
//		final Image img = Images.getOrLoad("/icons/loading_spinner.gif");
		
		final Rectangle cb = c.getClientArea();
		final Rectangle ib = img.getBounds();
		
		final int x = cb.x + cb.width / 2 - ib.width / 2;
		final int y = cb.y + cb.height / 2 - ib.height / 2;
		
		class MaskCanvas extends Canvas {
			int c = 0;
			
			public MaskCanvas(Composite parent, int style) {
				super(parent, style);
			}
		}

		// Create a canvas
		final MaskCanvas canvas = new MaskCanvas(c, SWT.TRANSPARENT);
//		final Composite canvas = new Composite(c, SWT.TRANSPARENT);
//		canvas.setBounds(new Rectangle(x, y, ib.width, ib.height));
		canvas.setBounds(new Rectangle(0, 0, cb.width,cb.height));
		
//		canvas.setBounds(new Rectangle(x, y, ib.width, ib.height));
		
//		final Image image = new Image(display, "C:/sample_image.png");
		if (lastChild != null)
			canvas.moveAbove(children[0]);
		



		// Create a paint handler for the canvas    
		canvas.addPaintListener(new PaintListener() {
		  public void paintControl(PaintEvent e) {
//			  e.gc.setBackground(Colors.getSystemColor(SWT.COLOR_CYAN)); 
				logger.debug("painting image!");
				e.gc.drawImage(img, x, y);
				
				e.gc.drawString("Loading"+StringUtils.repeat('.', canvas.c), x, y+img.getBounds().height+10);
		        e.gc.drawLine(0,0, cb.width,cb.height);     
		  }
		});
		
		Timer t = new Timer();
		t.scheduleAtFixedRate(new TimerTask() {
			@Override public void run() {
				canvas.c++;
				if (canvas.c > 3)
					canvas.c = 0;
				
				Display.getDefault().asyncExec(new Runnable() {
					@Override public void run() {
						canvas.redraw();
					}
				});
				
			}
		}, 0, 500);
		
	}
	
	public static void mask(final Composite c) {
		
		
		final Shell s = new Shell(c.getShell(), SWT.MODELESS | SWT.NO_TRIM | SWT.ON_TOP);
		s.setBackground(Colors.getSystemColor(SWT.COLOR_RED));
		
		// define a region
	    Region region = new Region();
	    Rectangle pixel = new Rectangle(0, 0, 1, 1);
	    Rectangle ca = c.getClientArea();
//	    Rectangle ca = c.getBounds();
	    logger.debug("ca = "+ca);
	    
	    for (int y = ca.y; y < ca.height; y += 2) {
	      for (int x = ca.x; x < ca.width; x += 2) {
	        pixel.x = x;
	        pixel.y = y;
	        region.add(pixel);
	      }
	    }
	    // define the shape of the shell using setRegion
	    s.setRegion(region);
	    Rectangle size = region.getBounds();
	    
	    
	    s.setLocation(ca.x, ca.y);
	    s.setSize(ca.width, ca.height);
//	    s.setLocation(100, 100);
//	    s.setSize(350, 500);	    
	    
	    c.addPaintListener(new PaintListener() {
			@Override public void paintControl(PaintEvent e) {
				s.redraw();
				
			}
		});
	    
	    s.addPaintListener(new PaintListener() {
	      public void paintControl(PaintEvent e) {
				logger.debug("painting image!");
				Image img = Images.ADD;
				Rectangle cb = c.getClientArea();
				logger.debug("cb = "+cb);

			    s.setLocation(cb.x, cb.y);
			    s.setSize(cb.width, cb.height);
				
				Rectangle ib = img.getBounds();
				
				int x = cb.x + cb.width / 2 - ib.width / 2;
				int y = cb.y + cb.height / 2 - ib.height / 2;
				
				logger.debug("x = "+x+" y = "+y);
				
				e.gc.drawImage(img, x, y);
				
				Rectangle clientArea = c.getClientArea(); 
		        e.gc.drawLine(0,0,clientArea.width,clientArea.height);
	      }
	    });
	    s.open();
	    
//	    while (!s.isDisposed()) {
//	        if (!Display.getDefault().readAndDispatch())
//	        	Display.getDefault().sleep();
//	      }
//	      region.dispose();
		
//		Dialog d = new Dialog(c.getShell()) {
////			public void Dialog(Shell s) {
////				super(s);
//				
////				setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
////				setBlockOnOpen(false);				
////			}
//			
//			protected void setShellStyle(int arg0) 
//			{
//				setShellStyle(SWT.MODELESS | SWT.NO_TRIM | SWT.ON_TOP);
//				setBlockOnOpen(false);
//			}
//			
//			/* (non-Javadoc)
//			 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
//			 */
//			protected Control createDialogArea(Composite parent) 
//			{
////				/*
////				 * Create the dialog area where you can place the UI components
////				 */
//				Composite composite = ( Composite )super.createDialogArea(parent);				
//				
//				composite.addPaintListener(new PaintListener() {
//					@Override public void paintControl(PaintEvent e) {
//						
//						
//						logger.debug("painting image!");
//						Image img = Images.ADD;
//						Rectangle cb = c.getClientArea();
//						Rectangle ib = img.getBounds();
//						
//						int x = cb.x + cb.width / 2 - ib.width / 2;
//						int y = cb.y + cb.height / 2 - ib.height / 2;
//						
//						logger.debug("x = "+x+" y = "+y);
//						
//						e.gc.drawImage(img, x, y);
//						
//						Rectangle clientArea = c.getClientArea(); 
//				        e.gc.drawLine(0,0,clientArea.width,clientArea.height);
//
//					}
//				});
//				
//
////				//Set the shell message
////				composite.getShell().setText("A dialog box with no buttons at all press 'ESC' to close");
////				try 
////				{
////					composite.setLayout(new FormLayout());
////					{
////						//Place all your UI Components
////						/*
////						 * I have created the dummy UI components
////						 * so that you will feel comfortable
////						 */
////						//Create a Label
////						createLabel(composite);
////						//Create a Text field
////						createTextField(composite);
////						//Create a push button
////						createButton(composite);
////					}
////				}
////				catch (Exception e) 
////				{
////					e.printStackTrace();
////				}
//				
//				//Set the size of the parent shell
//				getShell().setLocation(c.getClientArea().x, c.getClientArea().y);
//				getShell().setSize(c.getClientArea().width, c.getClientArea().height);
//				
//				//Set the dialog position in the middle of the monitor
//				return composite;
//			}			
//		};
//		d.open();
		
//		c.addPaintListener(new PaintListener() {
//			@Override public void paintControl(PaintEvent e) {
//				
//				
//				logger.debug("painting image!");
//				Image img = Images.ADD;
//				Rectangle cb = c.getClientArea();
//				Rectangle ib = img.getBounds();
//				
//				int x = cb.x + cb.width / 2 - ib.width / 2;
//				int y = cb.y + cb.height / 2 - ib.height / 2;
//				
//				logger.debug("x = "+x+" y = "+y);
//				
//				e.gc.drawImage(img, x, y);
//				
//				Rectangle clientArea = c.getClientArea(); 
//		        e.gc.drawLine(0,0,clientArea.width,clientArea.height);
//
//			}
//		});
		
		
	}
	
	public static ICellEditorValidator createNumberCellValidator(Class<?> t) {
		ICellEditorValidator v = null;
		if (t.equals(Float.class) || t.equals(float.class)) {
			v = new ICellEditorValidator() {
				@Override public String isValid(Object value) {
					try {
						Float.parseFloat(String.valueOf(value));
						return null;
					} catch (NumberFormatException e) {
						return e.getMessage();
					}
				}
			};
		}
		else if (t.equals(Integer.class) || t.equals(int.class)) {
			v =new ICellEditorValidator() {
				@Override public String isValid(Object value) {
					try {
						logger.debug("validating int: "+value);
						int i = Integer.parseInt(String.valueOf(value));
						logger.debug("i = "+i);
						return null;
					} catch (NumberFormatException e) {
						return e.getMessage();
					}
				}
			};
		}
		else if (t.equals(Double.class) || t.equals(double.class)) {
			v = new ICellEditorValidator() {
				@Override public String isValid(Object value) {
					try {
						Double.parseDouble(String.valueOf(value));
						return null;
					} catch (NumberFormatException e) {
						return e.getMessage();
					}
				}
			};
		}
		
		return v;
	}
			
	public static void createThumbsForDoc(TrpDoc doc, boolean overwrite) throws Exception {
		if (doc.getMd()==null || doc.getMd().getLocalFolder()==null)
			throw new Exception("No local folder!");
		
//		checkIfLocalDoc(doc);
		
		File thmbsDir = new File(doc.getMd().getLocalFolder().getAbsolutePath() + File.separator + LocalDocConst.THUMBS_FILE_SUB_FOLDER);
		FileUtils.forceMkdir(thmbsDir);
		
		int newHeight = LocalDocConst.THUMB_SIZE_HEIGHT;
		for (TrpPage p : doc.getPages()) {
			File imgFile = FileUtils.toFile(p.getUrl());
			if (imgFile == null) 
				throw new IOException("Cannot retrieve image url from: "+p.getUrl());
			
			File thumbsFile = FileUtils.toFile(p.getThumbUrl());
			if (thumbsFile == null)
				throw new IOException("Cannot retrieve thumbs url from: "+p.getThumbUrl());
			
			if (thumbsFile.exists() && !overwrite) // skip if already there and overwrite not specified 
				continue;
			
			logger.debug("creating thumb file: "+thumbsFile);
			long st = System.currentTimeMillis();
			
			if (true)  {
				SWTUtil.createThumbnailFileOverSWTSecure(imgFile, thumbsFile, -1, LocalDocConst.THUMB_SIZE_HEIGHT);
			}
			
			if (false) {
			BufferedImage originalImage = ImgUtils.readImage(imgFile);
			if (originalImage==null)
				throw new IOException("Cannot load image "+imgFile.getAbsolutePath());
			
			double sf = (double)newHeight / (double)originalImage.getHeight();
			int newWidth = (int)(sf * originalImage.getWidth());

			BufferedImage thmbImg = new BufferedImage(newWidth, newHeight, originalImage.getType());
			Graphics2D g = thmbImg.createGraphics();
			RenderingHints rh = new RenderingHints(
		             RenderingHints.KEY_INTERPOLATION,
		             RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.setRenderingHints(rh);
			g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
			g.dispose();
//				logger.debug("thmbImg: "+originalImage+ " size: "+thmbImg.getWidth()+"x"+thmbImg.getHeight());
			
			if (!ImageIO.write(thmbImg, FilenameUtils.getExtension(thumbsFile.getName()), thumbsFile))
				throw new Exception("Could not write thumb file - no appropriate writer found!");
			}
			
		    logger.debug("created thumb file: "+thumbsFile.getAbsolutePath()+" time = "+(System.currentTimeMillis()-st));
		}
	}
	
	public static Rectangle normalizeRect(Rectangle r) {
		Rectangle newR = new Rectangle(r.x, r.y, r.width, r.height);
		if (newR.width < 0) {
			newR.width *= -1;
			newR.x -= newR.width;
		}
		if (newR.height < 0) {
			newR.height *= -1;
			newR.y -= newR.height;
		}
		return newR;
	}
	
	public static java.awt.Rectangle extendRect(java.awt.Rectangle r, int left, int right, int top, int bottom) {
		return toAWTRect(extendRect(toSWTRect(r), left, right, top, bottom));
	}
	
	public static Rectangle extendRect(Rectangle r, int left, int right, int top, int bottom) {
		Rectangle newR = new Rectangle(r.x, r.y, r.width, r.height);
		newR.x -= left;
		newR.y -= top;
		newR.width += left+right;
		newR.height += top+bottom;
		
		return newR;
	}
	
	public static void centerShell(Shell shell) {
		Display display = shell.getDisplay();

		Monitor primary = display.getPrimaryMonitor();
	    Rectangle bounds = primary.getBounds();
	    Rectangle rect = shell.getBounds();
	    
	    int x = bounds.x + (bounds.width - rect.width) / 2;
	    int y = bounds.y + (bounds.height - rect.height) / 2;
	    
	    shell.setLocation(x, y);
	}
	
	public static ImageData scaleImageData(ImageData actImage, int width, int height) throws IOException {
		if (width<=0 && height<=0) {
			throw new IOException("Width and height are <= 0!");
		}
		else if (height<=0) {
			double sf = (double) width / (double) actImage.width;
			height = (int) (sf * actImage.height);
		} else if (width<=0) {
			double sf = (double) height / (double) actImage.height;
			width = (int)(sf * actImage.width);
		}
		return actImage.scaledTo(width, height);		
	}
	
	public static boolean createThumbnailFileOverSWTSecure(File origImg, File thumb, int width, int height) throws IOException {
		boolean result = false;

		ImageLoader imgLoader = new ImageLoader();

		Image img = new Image(Display.getCurrent(), origImg.getAbsolutePath());
		ImageData imgData = img.getImageData();

		if (imgData != null) {
			ImageData actImage = imgData;
			
			actImage = scaleImageData(actImage, width, height);

			imgLoader.data = new ImageData[] { actImage };
			imgLoader.save(thumb.getAbsolutePath(), SWT.IMAGE_PNG);
			result = true;

		} else {
			logger.warn("Unable to load " + origImg.getAbsolutePath());
		}

		img.dispose();
		return result;

	}

	public static boolean createThumbnailFileOverSWTFast(File origImg, File thumb, int width, int height) throws IOException {

		boolean result = false;

		ImageLoader imgLoader = new ImageLoader();

		ImageData[] imgData = imgLoader.load(origImg.getAbsolutePath());

		if (imgData.length > 0) {
			ImageData actImage = imgData[0];

			actImage = scaleImageData(actImage, width, height);

			imgLoader.data = new ImageData[] { actImage };
			imgLoader.save(thumb.getAbsolutePath(), SWT.IMAGE_PNG);
			result = true;

		} else {
			logger.info("Unable to load " + origImg.getAbsolutePath());
		}

		return result;

	}
	
	public static List<Control> getAllChildren(Control ctrl) {
		if (ctrl==null || ctrl.isDisposed())
				return new ArrayList<>();
		
		List<Control> children = new ArrayList<>();
		Stack<Control> stack = new Stack<>();
		stack.push(ctrl);
		while (!stack.isEmpty()) {
			Control parent = stack.pop();
			if (parent instanceof Composite) {
				for (Control c : ((Composite) parent).getChildren()) {
					children.add(c);
					stack.push(c);
				}
			}	
		}
		return children;
	}
	
	/** En-/Disables the given control and all its child elements recursively. 
//	 * @deprecated Can lead to massive leak shit behavior
	 * */
//	@Deprecated
	public static void recursiveSetEnabled(Control ctrl, boolean enabled) {
//		if (true) return;
		
//		System.gc();
		SWTUtil.setEnabled(ctrl, enabled);		
		List<Control> children = getAllChildren(ctrl);
		for (Control c : children) {
			SWTUtil.setEnabled(c, enabled);
		}
	}
	
	/** Sets the selection value of button. False if the value is null. */
	public static void set(Button checkBtn, Boolean val) {
		checkBtn.setSelection(val==null?false:val);
	}
	
	public static void set(Spinner spinner, Integer val) {
//		int mult = (int) Math.pow(10, spinner.getDigits());
		
		spinner.setSelection(val==null ? spinner.getMinimum() : val);
	}	
	
	public static void set(Text text, String str) {
		String strToSet = str==null ? "" : str;
		if (!text.getText().equals(strToSet))
			text.setText(strToSet);	
	}
	
	public static void set(Combo combo, String str) {
		String strToSet = str==null ? "" : str;
		
		if (!combo.getText().equals(strToSet)) {
			combo.setText(strToSet);
			combo.setSelection(new Point(strToSet.length(), strToSet.length()));
		}
		
//		combo.setText(str==null?"":str);
	}
	
	/** Selects the the given index in the combobox or deselects all if the index is not valid. */
	public static void select(Combo combo, int i) {
		if (i != -1)
			combo.select(i);
		else {
			combo.deselectAll();
		}
	}
	
	/** An invisible dummy shell to temporarily add widgets */
	public static Shell dummyShell = new Shell(SWT.MODELESS);
	public static Menu dummyMenu = new Menu(dummyShell);
	static {
		dummyShell.setVisible(false);
		dummyShell.setLayout(new GridLayout());
		dummyMenu.setVisible(false);
	}
	
	public static void addToolItemSelectionListener(ToolItem ti, SelectionListener listener) {
		if (ti!=null && !ti.isDisposed())
			ti.addSelectionListener(listener);	
	}
	
	public static void setEnabled(ToolItem item, boolean enabled) {
		if (item!=null && !item.isDisposed() && item.getEnabled()!=enabled) 
			item.setEnabled(enabled);
	}
	
	public static void setEnabled(Control composite, boolean enabled) {
		if (composite!=null && !composite.isDisposed() && composite.getEnabled()!=enabled) 
			composite.setEnabled(enabled);
	}
	
	public static List<java.awt.Point> getPoints(java.awt.Rectangle r) {
		List<java.awt.Point> pts = new ArrayList<>();
		pts.add(new java.awt.Point(r.x, r.y));
		pts.add(new java.awt.Point(r.x+r.width, r.y));
		pts.add(new java.awt.Point(r.x+r.width, r.y+r.height));
		pts.add(new java.awt.Point(r.x, r.y+r.height));
		
		return pts;
	}
		
	public static Rectangle toSWTRect(java.awt.Rectangle r) {
		return new Rectangle(r.x, r.y, r.width, r.height);
	}
	
	public static java.awt.Rectangle toAWTRect(Rectangle r) {
		return new java.awt.Rectangle(r.x, r.y, r.width, r.height);
	}
		
	public static void drawRectangleAroundPoint(GC gc, int x, int y, int size) {
		gc.drawRectangle(x-size/2, y-size/2, size, size);
	}
	
	/** Bounds the given value to the range [min, max] */
	public static double bound(double value, double min, double max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	public static Point transform(AffineTransform tr, Point ptSrc) {
		Point2D.Double dstPtTmp = new Point2D.Double();
		tr.transform(new Point2D.Double(ptSrc.x, ptSrc.y), dstPtTmp);
				
		return new Point((int)dstPtTmp.x, (int)dstPtTmp.y);
	}

	/**
	 * True if test rectangle is inside root rectangle
	 */
	public static boolean isInside(Rectangle root, Rectangle test) {
		return root.contains(test.x, test.y) && root.contains(test.x+test.width, test.y+test.height);
	}

	/**
	 * Given an arbitrary rectangle, get the rectangle with the given transform.
	 * The result rectangle is positive width and positive height.
	 * @param af AffineTransform
	 * @param src source rectangle
	 * @return rectangle after transform with positive width and height
	 */
	public static Rectangle transformRect(AffineTransform af, Rectangle src){
		Rectangle dest= new Rectangle(0,0,0,0);
		src=absRect(src);
		Point p1=new Point(src.x,src.y);
		p1=transformPoint(af,p1);
		dest.x=p1.x; dest.y=p1.y;
		dest.width=(int)(src.width*af.getScaleX());
		dest.height=(int)(src.height*af.getScaleY());
		return dest;
	}

	/**
	 * Given an arbitrary rectangle, get the rectangle with the inverse given transform.
	 * The result rectangle is positive width and positive height.
	 * @param af AffineTransform
	 * @param src source rectangle
	 * @return rectangle after transform with positive width and height
	 */
	public static Rectangle inverseTransformRect(AffineTransform af, Rectangle src){
		Rectangle dest= new Rectangle(0,0,0,0);
		src=absRect(src);
		Point p1=new Point(src.x,src.y);
		p1=inverseTransformPoint(af,p1);
		dest.x=p1.x; dest.y=p1.y;
		dest.width=(int)(src.width/af.getScaleX());
		dest.height=(int)(src.height/af.getScaleY());
		
		return dest;
	}

	/**
	 * Given an arbitrary point, get the point with the given transform.
	 * @param af affine transform
	 * @param pt point to be transformed
	 * @return point after tranform
	 */
	public static Point transformPoint(AffineTransform af, Point pt) {
		Point2D src = new Point2D.Float(pt.x, pt.y);
		Point2D dest= af.transform(src, null);
		Point point=new Point((int)Math.floor(dest.getX()), (int)Math.floor(dest.getY()));
		return point;
	}
	
	/**
	 * Given an arbitrary point, get the point with the inverse given transform.
	 * @param af AffineTransform
	 * @param pt source point
	 * @return point after transform
	 */
	public static Point inverseTransformPoint(AffineTransform af, Point pt){
		Point2D src=new Point2D.Float(pt.x,pt.y);
		try{
			Point2D dest= af.inverseTransform(src, null);
			return new Point((int)Math.floor(dest.getX()), (int)Math.floor(dest.getY()));
		}catch (Exception e){
			e.printStackTrace();
			return new Point(0,0);
		}
	}

	/**
	 * Given arbitrary rectangle, return a rectangle with upper-left 
	 * start and positive width and height.
	 * @param src source rectangle
	 * @return result rectangle with positive width and height
	 */
	public static Rectangle absRect(Rectangle src){
		Rectangle dest= new Rectangle(0,0,0,0);
		if(src.width<0) { dest.x=src.x+src.width+1; dest.width=-src.width; } 
		else{ dest.x=src.x; dest.width=src.width; }
		if(src.height<0) { dest.y=src.y+src.height+1; dest.height=-src.height; } 
		else{ dest.y=src.y; dest.height=src.height; }
		return dest;
	}
	
	public static void drawLineExtended(GC gc, int x1, int y1, int x2, int y2) {
		final double EXTENSION = 9e3;
//		final double EXTENSION = 1e6;
		
		Vector2D p1 = new Vector2D(x1, y1);
		Vector2D p2 = new Vector2D(x2, y2);
		Vector2D v =  p2.minus(p1);
		v = v.normalize();
		
		p1 = p1.minus(v.times(EXTENSION));
		p2 = p2.plus(v.times(EXTENSION));
		
//		logger.debug("p1 = "+p1);
//		logger.debug("p2 = "+p2);
		
		gc.drawLine((int)p1.x(), (int)p1.y(), (int)p2.x(), (int)p2.y());
	}
	
	
	/**
     * Converts a buffered image to SWT <code>ImageData</code>.
     * 
     * <br> NOTE: this code was 'copy & paste'ed from the internet...
     *
     * @param bufferedImage  the buffered image (<code>null</code> not
     *         permitted).
     *
     * @return The image data.
	 * @throws IOException 
     */
    public static ImageData convertToSWT(BufferedImage bufferedImage) throws IOException {
    	if (bufferedImage == null)
    		throw new IOException("Null value as input image!");
    	if (bufferedImage.getColorModel() == null)
    		throw new IOException("Null value as input image colour model!");
    	
    	logger.debug("converting from awt image to swt image, color model: "+bufferedImage.getColorModel()+" - "+bufferedImage.getColorModel().getClass().getSimpleName());
        if (bufferedImage.getColorModel() instanceof DirectColorModel) {
            DirectColorModel colorModel
                    = (DirectColorModel) bufferedImage.getColorModel();
            PaletteData palette = new PaletteData(colorModel.getRedMask(),
                    colorModel.getGreenMask(), colorModel.getBlueMask());
            ImageData data = new ImageData(bufferedImage.getWidth(),
                    bufferedImage.getHeight(), colorModel.getPixelSize(),
                    palette);
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[3];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    int pixel = palette.getPixel(new RGB(pixelArray[0],
                            pixelArray[1], pixelArray[2]));
                    data.setPixel(x, y, pixel);
                }
            }
            return data;
        }
        else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
            IndexColorModel colorModel = (IndexColorModel)
                    bufferedImage.getColorModel();
            int size = colorModel.getMapSize();
            byte[] reds = new byte[size];
            byte[] greens = new byte[size];
            byte[] blues = new byte[size];
            colorModel.getReds(reds);
            colorModel.getGreens(greens);
            colorModel.getBlues(blues);
            RGB[] rgbs = new RGB[size];
            for (int i = 0; i < rgbs.length; i++) {
                rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF,
                        blues[i] & 0xFF);
            }
            PaletteData palette = new PaletteData(rgbs);
            ImageData data = new ImageData(bufferedImage.getWidth(),
                    bufferedImage.getHeight(), colorModel.getPixelSize(),
                    palette);
            data.transparentPixel = colorModel.getTransparentPixel();
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[1];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    data.setPixel(x, y, pixelArray[0]);
                }
            }
            return data;
        }
        else if (bufferedImage.getColorModel() instanceof ComponentColorModel) {
            ComponentColorModel colorModel = (ComponentColorModel)bufferedImage.getColorModel();

            //ASSUMES: 3 BYTE BGR IMAGE TYPE

            PaletteData palette = new PaletteData(0x0000FF, 0x00FF00,0xFF0000);
            ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);

            //This is valid because we are using a 3-byte Data model with no transparent pixels
            data.transparentPixel = -1;

            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[3];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    int pixel = palette.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2]));
                    data.setPixel(x, y, pixel);
                }
            }
            return data;
        }
        
        
        throw new IOException("Unknown colour model: "+bufferedImage.getColorModel().getClass().getCanonicalName());
    }
    
    public static void tryDelete(Image img) {
    	if (img != null && !img.isDisposed())
    		img.dispose();
    }
    
    public static void tryDelete(Control ctrl) {
    	if (ctrl != null && !ctrl.isDisposed())
    		ctrl.dispose();
    }
    
    public static void drawTriangleArc(GC gc, int sX, int sY, int dX, int dY, int l, int w, boolean fill) {
    	logger.trace("drawTriangleArc, sX="+sX+" sY="+sY+" dX="+dX+" dY="+dY+" l="+l+" w="+w+" fill="+fill);
    	// compute directions:
    	double dirX = sX-dX;
    	double dirY = sY-dY;
    	double norm = Math.sqrt(dirX*dirX+dirY*dirY);
    	if (norm > 0) {
    		dirX /= norm;
    		dirY /= norm;
    	}
    	double dirNX = -dirY;
    	double dirNY = dirX;
    	double tmpX = dX + dirX*l;
    	double tmpY = dY + dirY*l;
    	
    	// set points of polyon
    	int[] pts = new int[6];
    	pts[0] = dX;
    	pts[1] = dY;
    	pts[2] = (int) (tmpX + dirNX*w/2);
    	pts[3] = (int) (tmpY + dirNY*w/2);
    	pts[4] = (int) (tmpX - dirNX*w/2);
    	pts[5] = (int) (tmpY - dirNY*w/2);
    	
//    	logger.trace("drawTriangleArc, pts = "+pts[0]+" "+pts[1]+" "+pts[2]+" "+pts[3]+" "+pts[4]+" "+pts[5]);
    	if (fill) {
    		gc.fillPolygon(pts);
    	} else {
    		gc.drawPolygon(pts);
    	}
    }

	public static boolean isDisposed(Item item) {
		return item==null || item.isDisposed();
	}
	

}
