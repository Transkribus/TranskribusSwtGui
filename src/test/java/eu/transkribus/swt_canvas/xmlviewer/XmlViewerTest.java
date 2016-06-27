package eu.transkribus.swt_canvas.xmlviewer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_canvas.util.Images;
import eu.transkribus.swt_gui.dialogs.PAGEXmlViewer;

public class XmlViewerTest {
	private final static Logger logger = LoggerFactory.getLogger(XmlViewerTest.class);
	
	public static void main(String[] args) throws Exception {
//		Storage s = Storage.getInstance();
//		s.login(TrpServerConn.SERVER_URIS[0], args[0], args[1]);
		
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				// getShell().setLayout(new FillLayout());
				getShell().setSize(600, 600);
				
//				PAGEXmlViewer v = new PAGEXmlViewer(getShell(), SWT.MODELESS);
				XmlViewer v = new XmlViewer(getShell(), SWT.MODELESS);
//				v.getReloadBtn().addSelectionListener(new SelectionListener() {
//					
//					@Override public void widgetSelected(SelectionEvent e) {
//						logger.info("reload btn pressed!");
//					}
//					
//					@Override public void widgetDefaultSelected(SelectionEvent e) {
//					}
//				});
				
				try {
					v.open(new URL("https://dbis-thure.uibk.ac.at/f/Get?id=XBJUPGOGPTKOLLGWSFJIALZN"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				


//				SWTUtil.centerShell(getShell());

				return parent;
			}
		};
		aw.setBlockOnOpen(true);
		aw.open();

		Display.getCurrent().dispose();
	}

}
