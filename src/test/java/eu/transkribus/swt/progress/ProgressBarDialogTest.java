package eu.transkribus.swt.progress;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.canvas.CanvasException;

public class ProgressBarDialogTest {
	private static final Logger logger = LoggerFactory.getLogger(ProgressBarDialogTest.class);

	public static class PBDialogDemo extends Shell {
		public PBDialogDemo(Display display, int style) {
			super(display, style);
			createContents();
		}

		protected void createContents() {
			final ProgressBarDialog pbd = new ProgressBarDialog(PBDialogDemo.this);
			
			setText("ProgressBar Dialog Example");
			setSize(218, 98);
			setLayout(new FillLayout());

			final Button openProgressbarDialogButton = new Button(this, SWT.NONE);
			openProgressbarDialogButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					
					try {
						pbd.open(new IRunnableWithProgress() {

							@Override
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								monitor.beginTask("Counting to ten!", IProgressMonitor.UNKNOWN);

								for (int i = 1; i <= 10; ++i) {
									monitor.subTask("i=" + i);
									logger.debug("progress: " + i);

									Thread.sleep(500);
									if (monitor.isCanceled()) {
										monitor.done();
										break;
									}
									if (i==9)
										throw new InvocationTargetException(new CanvasException("yeah!!!"));

									monitor.worked(i);
								}

								monitor.done();
							}

//							@Override
//							public void runInGuiOnSuccess() {
//								logger.debug("successfully completed operation!");
//							}
//
//							@Override
//							public void runInGuiOnError(Throwable ex) {
//								logger.debug("an error occured: "+ex.getMessage(), ex);
//							}
						}, "Counting", true);
					} catch (Throwable e1) {
						e1.printStackTrace();
					}
				}
			});
			openProgressbarDialogButton.setText("Open ProgressBar Dialog");

		}

		@Override
		protected void checkSubclass() {
		}

	}

	public static void main(String[] args) {
		try {
			Display display = Display.getDefault();
			PBDialogDemo shell = new PBDialogDemo(display, SWT.SHELL_TRIM);
			SWTUtil.centerShell(shell);
			shell.open();
			shell.layout();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
