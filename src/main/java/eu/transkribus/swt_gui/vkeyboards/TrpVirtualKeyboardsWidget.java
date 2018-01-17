package eu.transkribus.swt_gui.vkeyboards;

import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.UnicodeList;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.dialogs.MultilineInputDialog;

public class TrpVirtualKeyboardsWidget extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(TrpVirtualKeyboardsWidget.class);

	Composite controlComposite;
	Button reloadBtn;
	Button editCharBtn, addTabBtn;
	
	TrpVirtualKeyboardsTabWidget tabWidget;
	
	public TrpVirtualKeyboardsWidget(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new GridLayout(1, false));
		
		controlComposite = new Composite(this, style);
		controlComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		controlComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		reloadBtn = new Button(controlComposite, SWT.PUSH);
		reloadBtn.setImage(Images.REFRESH);
		reloadBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				tabWidget.reload();
			}
		});
		reloadBtn.setToolTipText("Reload virtual keyboards from "+TrpVirtualKeyboardsTabWidget.VK_XML);
		
		editCharBtn = new Button(controlComposite, SWT.PUSH);
		editCharBtn.setImage(Images.PENCIL);
		editCharBtn.setText("Edit...");
		editCharBtn.setToolTipText("Edit characters of the current keyboard");
		
		SWTUtil.onSelectionEvent(editCharBtn, e -> {
			VirtualKeyboard vk = tabWidget.getSelected();
			if (vk == null) {
				return;
			}
			
			VirtualKeyboardEditor vkEditor = new VirtualKeyboardEditor(parent, 0);
			vkEditor.setUnicodeList(vk.getUnicodeList().getUnicodes());
			
			if (DialogUtil.showCustomMessageDialog(getShell(), "Virtual Keyboard Editor", null, null, SWT.RESIZE, new String[]{"OK", "Cancel"}, 0, vkEditor, new Point(1000, 750))==0) {
				List<Pair<Integer, String>> unicodes = vkEditor.getUnicodes();
				logger.debug("unicodes = "+unicodes);
				
				vk.reload(unicodes);
//				vk.pack();

				try {
					tabWidget.setConfProperrty(vk.getUnicodeList().getName(), vk.getUnicodeList().getUnicodeHexRange(), true);
				} catch (ConfigurationException e1) {
					DialogUtil.showErrorMessageBox(getShell(), "Error", "Could not save virtual keyboard:\n\n"+e1.getMessage());
					logger.error(e1.getMessage(), e1);
				}
			}
		});
		
//		editCharBtn.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				
//				final VirtualKeyboard vk = tabWidget.getSelected();
//				if (vk == null)
//					return;
//				
//				final String tabName = getSelectedTabName();
//								
////				String unicodeStr = vk.getPrintableUnicodeRange();
//				String unicodeStr = vk.getUnicodeHexRange();
//				InputDialog dlg = new MultilineInputDialog(getShell(), "Edit characters of '"+tabName+"' table", 
//						"Enter unicode character string, i.e.:"
//						+ "\n\t- Provide a unicode code directly (U+XXXX)"
//						+ "\n\t- Provide a valid unicode code range (U+XXXX-U+YYYY)"
//						+ "\n\t- Copy and paste some characters into the text field."
//						+ "\nNote: Unicode codes and ranges have to be separated by whitespaces or new lines", unicodeStr, null);
//				
//				if (dlg.open()==Window.OK) {					
//					// TODO: set input of dialog!
//					String value = dlg.getValue();
//					logger.info("value = "+value);
//					
//					vk.reload(value);
//					vk.pack();
//
//					try {
//						tabWidget.setConfProperrty(tabName, value, true);
//					} catch (ConfigurationException e1) {
//						DialogUtil.showErrorMessageBox(getShell(), "Error", "Could not save virtual keyboard:\n\n"+e1.getMessage());
//						logger.error(e1.getMessage(), e1);
//					}
//				}
//			}
//		});
		
		tabWidget = new TrpVirtualKeyboardsTabWidget(this, 0);
		tabWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	}
	
	public String getSelectedTabName() {
		if (tabWidget.getSelection() != null)
			return tabWidget.getSelection().getText();
		return "NA";
	}
	
	
//	public void addKeySelectionListener(final SelectionListener l) {
//		tabWidget.addKeySelectionListener(l);
//	}
	
	public TrpVirtualKeyboardsTabWidget getVirtualKeyboardsTabWidget() {
		return tabWidget;
	}
	
	public static void main(String [] args) throws Exception {
		Display display = new Display ();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		
		final TrpVirtualKeyboardsWidget vk = new TrpVirtualKeyboardsWidget(shell, 0);
		
		vk.getVirtualKeyboardsTabWidget().addListener(new ITrpVirtualKeyboardsTabWidgetListener() {
			@Override public void onVirtualKeyPressed(TrpVirtualKeyboardsTabWidget w, char c, String description) {
					logger.info("key pressed: "+c+" description = "+description);
			}
		});
		
		shell.setSize(400, 800);
		
//		shell.pack();
		shell.open();
		
		
		
//		shell.addListener(SWT.Resize, new Listener() {
//			public void handleEvent(Event e) {
//				vk.pack();
//			}
//		});
		
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();
	}
	

}
