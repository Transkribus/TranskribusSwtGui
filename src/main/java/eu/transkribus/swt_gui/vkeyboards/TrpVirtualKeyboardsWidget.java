package eu.transkribus.swt_gui.vkeyboards;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_canvas.util.Images;
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
		
		editCharBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				
				final VirtualKeyboard vk = tabWidget.getSelected();
				if (vk == null)
					return;
								
				InputDialog dlg = new MultilineInputDialog(getShell(), "Edit characters of '"+getSelectedTabName()+"' table", 
						"Enter unicode character string, i.e.:"
						+ "\n\t- Provide a unicode code directly (U+XXXX)"
						+ "\n\t- Provide a valid unicode code range (U+XXXX-U+YYYY)"
						+ "\n\t- Copy and paste some characters into the text field."
						+ "\nNote: Unicode codes and ranges have to be separated by whitespaces or new lines", vk.getUnicodeHexRange(), null);
				
				if (dlg.open()==Window.OK) {
					// TODO: set input of dialog!
					String value = dlg.getValue();
					logger.info("value = "+value);
					
					vk.reload(value);
					vk.pack();
				}
			}
		});
		
		tabWidget = new TrpVirtualKeyboardsTabWidget(this, 0);
		tabWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	}
	
	public String getSelectedTabName() {
		if (tabWidget.getSelection() != null)
			return tabWidget.getSelection().getText();
		return "NA";
	}
	
	
	public void addKeySelectionListener(SelectionListener l) {
		tabWidget.addKeySelectionListener(l);
	}
	


	public static void main(String [] args) throws Exception {
		Display display = new Display ();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		
		final TrpVirtualKeyboardsWidget vk = new TrpVirtualKeyboardsWidget(shell, 0);
//		if (false)
		vk.addKeySelectionListener(new SelectionListener() {
			
			@Override public void widgetSelected(SelectionEvent e) {
				if (e.getSource() == vk) {
					logger.info("event = "+e);
					Character c = (char) e.detail;
					logger.info("key pressed: "+c+" detail = "+e.detail+", name: "+e.text);
					logger.info("name =" +e.text);
					logger.info("e.detail =" +e.detail);
					
//					ATranscriptionWidget tw = ui.getSelectedTranscriptionWidget();
//					if (tw != null) {
//						tw.insertTextIfFocused(""+c);
//					}
				}
			}
			
			@Override public void widgetDefaultSelected(SelectionEvent e) {
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
