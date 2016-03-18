package eu.transkribus.swt_gui.vkeyboards;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.XMLPropertiesConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.UnicodeList;

//public class TrpVirtualKeyboards extends TabFolder {
public class TrpVirtualKeyboardsTabWidget extends CTabFolder {
	private final static Logger logger = LoggerFactory.getLogger(TrpVirtualKeyboardsTabWidget.class);
	
	public static final File VK_XML = new File("virtualKeyboards.xml");
	
	Set<SelectionListener> selListener = new HashSet<>();
	XMLPropertiesConfiguration conf;
	
		
	public TrpVirtualKeyboardsTabWidget(Composite parent, int style) {
		super(parent, style);
		
//		try {
//			List<UnicodeList> unicodeLists = loadVirtualKeyboardsXml(VK_XML);
//			for (UnicodeList ul : unicodeLists) {
//				addVirtualKeyboardTab(ul);
//			}
//		} catch (IOException e) {
//			logger.error(e.getMessage(), e);
//		}
		
		reload();
	}
	
	public void reload() {
		logger.info("reloading vkeyboard tab list...");
		
		for (CTabItem c : getItems()) {
			if (c!=null && !c.isDisposed())
				c.dispose();
		}

		try {
			logger.info("loading virtual keyboards from file: "+VK_XML.getAbsolutePath());
			
			conf = new XMLPropertiesConfiguration();
			conf.setEncoding("UTF-8");
			conf.load(VK_XML);
			
			List<UnicodeList> unicodeLists = loadVirtualKeyboardsXml(conf);
			for (UnicodeList ul : unicodeLists) {
				addVirtualKeyboardTab(ul);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		if (getItemCount()>0)
			setSelection(0);
		
	}
	
	private static List<UnicodeList> loadVirtualKeyboardsXml(XMLPropertiesConfiguration conf)
			throws InvalidPropertiesFormatException, FileNotFoundException, IOException {
		
		List<UnicodeList> unicodeLists = new ArrayList<>();		
		Iterator<String> it=conf.getKeys();
		while (it.hasNext()) {
			String key = it.next();
			String value = conf.getString(key);
			logger.info("value = '"+value+"'");

			UnicodeList ul = new UnicodeList(key, value);
			unicodeLists.add(ul);
		}
		Collections.sort(unicodeLists);

		return unicodeLists;
	}
	


	@Override
	public void checkSubclass() {}	
	
	
	public void addKeySelectionListener(SelectionListener l) {
		selListener.add(l);
	}
	
	public void removeKeySelectionListener(SelectionListener l) {
		selListener.remove(l);
	}
	
	public VirtualKeyboard getSelected() {
		if (getSelection()!=null) {
			return (VirtualKeyboard) getSelection().getControl();
		}
		return null;
	}
	
//	private VirtualKeyboard addVirtualKeyboardTab(Character start, Character end, String name) {
//		List<Character> chars = new ArrayList<>();
//		for (Character c = start; c <= end; ++c) {
//			chars.add(c);
//		}
//		
//		return addVirtualKeyboardTab(chars, name);
//	}
	
	private VirtualKeyboard addVirtualKeyboardTab(UnicodeList ul) {
		VirtualKeyboard vk = new VirtualKeyboard(this, SWT.NONE, ul);
		vk.addKeySelectionListener(new SelectionListener() {
			@Override public void widgetSelected(SelectionEvent e) {
				for (SelectionListener l : TrpVirtualKeyboardsTabWidget.this.selListener) {
					Event e1 = new Event();
					e1.widget = TrpVirtualKeyboardsTabWidget.this;

					e1.detail = e.detail;
					e1.text = e.text;
					
					l.widgetSelected(new SelectionEvent(e1));
				}
			}
			
			@Override public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
//		{
//			@Override protected void onKeyPressed(Button source, Character character, String name) {
//				for (SelectionListener l : TrpVirtualKeyboardsTabWidget.this.selListener) {
//					Event e = new Event();
//					e.widget = TrpVirtualKeyboardsTabWidget.this;
//					e.detail = character;
//					e.text = name;
//					
//					
//					l.widgetSelected(new SelectionEvent(e));
//				}
//			}
//		};
		
		vk.pack();
		CTabItem tabItem = new CTabItem(this, SWT.NONE);
		
		tabItem.setText(ul.getName());
		tabItem.setControl(vk);			
				
		return vk;
	}
	
	public static void main(String [] args) throws Exception {
		Display display = new Display ();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		
		final TrpVirtualKeyboardsTabWidget vk = new TrpVirtualKeyboardsTabWidget(shell, 0);
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
