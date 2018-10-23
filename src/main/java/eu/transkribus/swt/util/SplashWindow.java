package eu.transkribus.swt.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SplashWindow {
	private final static Logger logger = LoggerFactory.getLogger(SplashWindow.class);

	private int splashPos = 0;

	public final int SPLASH_MAX = 100;

	Display display;
	Image image;
	Shell splash;
	ProgressBar bar;

	public SplashWindow(Display display) {
		this.display = display;
		image = Images.getOrLoad("/transkribus_splash_screen_wolpertinger.png");
		logger.debug("splash image: " + image + " size: " + image.getImageData().width + " x "
				+ image.getImageData().height);

		splash = new Shell(SWT.APPLICATION_MODAL | SWT.NO_TRIM);
		splash.setBackgroundMode(SWT.INHERIT_DEFAULT);

		splash.setLayout(new FillLayout());
		splash.setBackgroundImage(image);
		splash.setSize(image.getBounds().width, image.getBounds().height);

		SWTUtil.centerShell(splash);
	}

	public void start(Runnable initRunnable) {
		splash.open();

		if (bar != null)
			bar.setSelection(0);

		display.asyncExec(initRunnable);

		while (splashPos != SPLASH_MAX) {
			// logger.debug("splashPos = "+splashPos);
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		splash.dispose();
	}

	public void stop() {
		splashPos = SPLASH_MAX;
	}

	public void setProgress(int progress) {
		// FIXME
		// if (progress < 0) {
		// splashPos = 0;
		// }
		// else if (progress < SPLASH_MAX) {
		// splashPos = progress;
		// } else {
		// splashPos = SPLASH_MAX;
		// }
		//
		// bar.setSelection(splashPos);
	}
}
