package examples;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author https://stackoverflow.com/a/28492105/6029399 
 */
public class AnimatedGif extends Canvas {
	private static final Logger logger = LoggerFactory.getLogger(AnimatedGif.class);
	
	private final ImageLoader loader = new ImageLoader();
	private int img = 0;
	private volatile boolean animating = false;
	private Thread animateThread;

	public AnimatedGif(Composite parent, int style) {
		super(parent, style);
	}
	
	public boolean isAnimating() {
		return animating;
	}
	
	public void load2(String resource) {
		try {
			load(resource);
		} catch (Exception e) {
			logger.error("Unable to load animated gif at "+resource+", error: "+e.getMessage(), e);
		}
	}
	
	public void load(String resource) throws IOException {
		load(getClass().getResourceAsStream(resource));
	}

	public void load(InputStream resource) throws IOException {
		loader.load(resource);
	}

	public void animate() {
		if (animateThread == null) {
			animateThread = createThread();
			animateThread.setDaemon(true);
		}

		if (animateThread.isAlive())
			return;

		animateThread.start();
	}

	public void stop() {
		animating = false;
		if (animateThread != null)
			try {
				animateThread.join();
				animateThread = null;
			} catch (InterruptedException e) {
				// do nothing
			}
	}

	private Thread createThread() {
		return new Thread() {
			long currentTime = System.currentTimeMillis();
			final Display display = getParent().getDisplay();

			public void run() {
				animating = true;
				while (animating) {
					img = (img == loader.data.length - 1) ? 0 : img + 1;
					System.out.println("delay time = "+loader.data[img].delayTime);
					int delayTime = Math.max(150, 10 * loader.data[img].delayTime);
					long now = System.currentTimeMillis();
					long ms = Math.max(currentTime + delayTime - now, 5);
					System.out.println("ms = "+ms);
					currentTime += delayTime;
					try {
						Thread.sleep(ms);
					} catch (Exception e) {
						return;
					}

					if (!display.isDisposed())
						display.asyncExec(new Runnable() {

							@Override
							public void run() {
								ImageData nextFrameData = loader.data[img];
								Image frameImage = new Image(display, nextFrameData);
								new GC(AnimatedGif.this).drawImage(frameImage, nextFrameData.x, nextFrameData.y);
								frameImage.dispose();
								// canvas.redraw();
							}
						});
				}

				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						new GC(AnimatedGif.this).fillRectangle(0, 0, getBounds().width, getBounds().height);
					}
				});
			}
		};
	}

}