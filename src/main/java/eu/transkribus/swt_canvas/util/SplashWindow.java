package eu.transkribus.swt_canvas.util;
 
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.SebisStopWatch;
 
public class SplashWindow {
    private final static Logger logger = LoggerFactory.getLogger(SplashWindow.class); 
	
    private int splashPos = 0;
 
    public final int SPLASH_MAX = 100;
    
    Display display;
    Image image;
    Shell splash;
    ProgressBar bar;
 
    public SplashWindow(Display display) 
    { 	
    	this.display = display;
//        image = Images.getOrLoad(SplashWindow.class, "/wolpertinger.jpg");
        image = Images.getOrLoad("/wolpertinger_transparent_test.png");   
        logger.debug("splash image: "+image+ " size: "+image.getImageData().width+" x "+image.getImageData().height);
        
        splash = new Shell(/*SWT.ON_TOP |*/ SWT.APPLICATION_MODAL | SWT.NO_TRIM);
        
//        splash.setBackgroundImage(image);
//        splash.setAlpha(0);
//        splash.setBackgroundMode(SWT.INHERIT_FORCE);
//        splash.setBackground(display.getSystemColor(Color.TRANSLUCENT));
        
        
//        splash.setBackgroundMode(SWT.INHERIT_FORCE);
        splash.setBackgroundMode(SWT.INHERIT_DEFAULT);
        
        splash.setLayout(new FillLayout());
        splash.setBackgroundImage(image);
        splash.setSize(image.getBounds().width, image.getBounds().height);
        
//        Canvas canvas = new Canvas(splash, SWT.NO_REDRAW_RESIZE);
//        canvas.addPaintListener(new PaintListener() {
//            public void paintControl(PaintEvent e) {
//             e.gc.drawImage(image,0,0);
//            }
//        }); 
        
        SWTUtil.centerShell(splash);
                
        if (false) {
        SebisStopWatch sw = new SebisStopWatch();
        sw.start();
        ImageData id = image.getImageData();
        Region region = new Region();
        
//        ArrayList<Integer> pts = new ArrayList<>();
        
        int[] pts = new int[id.width*id.height*2];
        int c=0;
        Rectangle pixel = new Rectangle(0, 0, 1, 1);
        for (int y = 0; y < id.height; y++) {
            for (int x = 0; x < id.width; x++) {
                if (id.getAlpha(x,y) > 0) {
//                    pixel.x = locx + x;
//                    pixel.y = locy + y;
                    
                    pixel.x = id.y + x;
                    pixel.y = id.x + y;
                    
//                    pixel.x = x;
//                    pixel.y = y;
//                    pts.add(pixel.x, pixel.y);
                    pts[c++] = pixel.x;
                    pts[c++] = pixel.y;
                    
                    region.add(new Rectangle(pixel.x, pixel.y, 1, 1));
                    
//                    region.add(pixel);
                }
            }
        }
        logger.debug("nr of pts = "+pts.length);
//        int[] pts2 = Arrays.copyOf(pts, c);
//        region.add(pts2);
        
        sw.stop(true);
        
        
        sw.start();
        splash.setRegion(region);
        sw.stop(true);
        
        }
        
        
        
//        Region region = new Region();
        
        
        
        
//        splash.setBackground(display.getSystemColor(SWT.TRANSPARENT));
//        bar = new ProgressBar(splash, SWT.INDETERMINATE | SWT.SMOOTH);
//        splash.setSize(image.getImageData().width, image.getImageData().height+bar.getSize().y);
        
//        bar.setMaximum(SPLASH_MAX);
 
//        Label label = new Label(splash, SWT.NONE);
//        label.setImage(image);
//        label.setBackground(display.getSystemColor(Color.TRANSLUCENT));
 
//        FormLayout layout = new FormLayout();
//        splash.setLayout(layout);
// 
//        FormData labelData = new FormData();
//        labelData.right = new FormAttachment(100, 0);
//        labelData.bottom = new FormAttachment(100, 0);
//        label.setLayoutData(labelData);
// 
//        FormData progressData = new FormData();
//        progressData.left = new FormAttachment(0, -5);
//        progressData.right = new FormAttachment(100, 0);
//        progressData.bottom = new FormAttachment(100, 0);
//        bar.setLayoutData(progressData);
//        splash.pack();
 
//        splash.open();
//        
//        display.asyncExec(initRunnable);
 
//        display.asyncExec(new Runnable()
//        {
//            public void run()
//            {
// 
//                for(int splashPos = 0; splashPos < SPLASH_MAX; splashPos++)
//                {
//                    try {
// 
//                        Thread.sleep(100);
//                    }
//                    catch(InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    setProgress(splashPos);
//                }
//                ApplicationLauncher.reportWindow.initWindow();
//                splash.close();
//                image.dispose(); 
//            }
//       });
//        while(splashPos != SPLASH_MAX)
//        {
//            if(!display.readAndDispatch())
//            {
//                display.sleep();
//            }
//        }

    }
    
    public void start(Runnable initRunnable) {
        splash.open();
        
        if (bar != null)
        	bar.setSelection(0);
        
        display.asyncExec(initRunnable);
        
        while(splashPos != SPLASH_MAX)
        {
//        	logger.debug("splashPos = "+splashPos);
            if(!display.readAndDispatch())
            {
                display.sleep();
            }
        }
        
        splash.dispose();
    }
    
    public void stop() {
    	splashPos = SPLASH_MAX;
    }
    
    public void setProgress(int progress) {
//    	if (progress < 0) {
//    		splashPos = 0;
//    	}
//    	else if (progress < SPLASH_MAX) {
//    		splashPos = progress;
//    	} else {
//    		splashPos = SPLASH_MAX;
//    	}
//    	    	
//    	bar.setSelection(splashPos);
    	
    	
    	
    }
}
