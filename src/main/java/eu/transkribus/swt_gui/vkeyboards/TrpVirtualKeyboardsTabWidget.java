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

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLPropertiesConfiguration;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.UnicodeList;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class TrpVirtualKeyboardsTabWidget extends CTabFolder {
	private final static Logger logger = LoggerFactory.getLogger(TrpVirtualKeyboardsTabWidget.class);
	
	public static final File VK_XML = new File("virtualKeyboards.xml");
	public static final String SHORTCUT_PROP_PREFIX = "__Shortcut.";
	
	XMLPropertiesConfiguration conf;
	
	Set<ITrpVirtualKeyboardsTabWidgetListener> listener = new HashSet<>();
		
	public TrpVirtualKeyboardsTabWidget(Composite parent, int style) {
		super(parent, style);
				
		reload();
	}
	
	public void reload() {
		logger.debug("reloading vkeyboard tab list...");
		
		for (CTabItem c : getItems()) {
			if (c!=null && !c.isDisposed())
				c.dispose();
		}

		try {
			logger.debug("loading virtual keyboards from file: "+VK_XML.getAbsolutePath()+", file exits: "+VK_XML.exists());
			
			conf = new XMLPropertiesConfiguration();
			conf.setEncoding("UTF-8");
			conf.setFile(VK_XML);
			conf.load();
			logger.debug("actual file: "+conf.getFile().getAbsolutePath());
			conf.setAutoSave(true);
			
			List<UnicodeList> unicodeLists = loadVirtualKeyboardsXml(conf);
			for (UnicodeList ul : unicodeLists) {
				addVirtualKeyboardTab(ul);
			}		
			
			// parse and set shortcuts:
			List<Pair<String, Pair<Integer, String>>> scs = loadVirtalKeyboardsShortCuts(conf);
			Storage.getInstance().clearVirtualKeyShortCuts();
			for (Pair<String, Pair<Integer, String>> sc : scs) {
				Storage.getInstance().setVirtualKeyShortCut(sc.getKey(), sc.getValue());
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		if (getItemCount()>0)
			setSelection(0);
		
	}
	
	public void setConfProperty(String name, String value, boolean save) throws ConfigurationException {
		if (conf.containsKey(name)) {
			logger.debug("setting vk "+name+" values to: "+value);
			conf.setProperty(name, value);
			
//			if (save)
//				saveConf();
		}
		else if (name.startsWith(SHORTCUT_PROP_PREFIX)) {
			logger.debug("setting shorcut "+name+" to "+value);
			conf.setProperty(name, value);
			
//			if (save)
//			saveConf();
		}
	}

	
	public void saveConf() throws ConfigurationException {
		conf.save();
		logger.debug("saved "+conf.getPath());
	}
		
	public void saveConfFromTabs() throws ConfigurationException {
		for (CTabItem i : getItems()) {
			if (!(i.getControl() instanceof VirtualKeyboard))
				continue;
				
			VirtualKeyboard vk = (VirtualKeyboard) i.getControl();	
			
			setConfProperty(vk.getVirtualKeyboardName(), vk.getUnicodeHexRange(), false);
		}
		saveConf();
	}
	
	public void clearShortcuts() {
		Iterator<String> it=conf.getKeys();
		while (it.hasNext()) {
			String key = it.next();
			if (key.startsWith(SHORTCUT_PROP_PREFIX) && key.length()>SHORTCUT_PROP_PREFIX.length()) {
				conf.clearProperty(key);
			}
		}
	}
	
	private static List<Pair<String, Pair<Integer, String>>> loadVirtalKeyboardsShortCuts(XMLPropertiesConfiguration conf) {
		List<Pair<String, Pair<Integer, String>>> scs = new ArrayList<>();
		Iterator<String> it=conf.getKeys();
		while (it.hasNext()) {
			String key = it.next();
			logger.debug("key: "+key);
			if (key.startsWith(SHORTCUT_PROP_PREFIX) && key.length()>SHORTCUT_PROP_PREFIX.length()) {
				String scKey = key.substring(SHORTCUT_PROP_PREFIX.length());				
				String value = conf.getString(key);
				
				logger.debug("found shortcut - key = "+scKey+" value = "+value);

				Pair<String, Pair<Integer, String>> sc;
				try {
					sc = Pair.of(scKey, UnicodeList.parseUnicodeString(value));
					scs.add(sc);
				} catch (IOException e) {
					logger.error("Could not parse shortcut "+scKey+": "+e.getMessage());
				}
			}
		}
		
		return scs;
	}
	
	private static List<UnicodeList> loadVirtualKeyboardsXml(XMLPropertiesConfiguration conf)
			throws InvalidPropertiesFormatException, FileNotFoundException, IOException {
		
		List<UnicodeList> unicodeLists = new ArrayList<>();		
		Iterator<String> it=conf.getKeys();
		while (it.hasNext()) {
			String key = it.next();
			
			if (key.startsWith(SHORTCUT_PROP_PREFIX)) {
				// TODO
			}
			else {
				String value = conf.getString(key);
				logger.debug("parsing virtual keyboard entry, key = '"+key+"', value = '"+value+"'");

				UnicodeList ul = new UnicodeList(key, value);
				unicodeLists.add(ul);				
			}
		}
		Collections.sort(unicodeLists);

		return unicodeLists;
	}

	@Override
	public void checkSubclass() {}	
	
	
	public void addListener(ITrpVirtualKeyboardsTabWidgetListener l) {
		listener.add(l);
	}
	
//	public void addKeySelectionListener(SelectionListener l) {
//		selListener.add(l);
//	}
//	
//	public void removeKeySelectionListener(SelectionListener l) {
//		selListener.remove(l);
//	}
	
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
				
				for (ITrpVirtualKeyboardsTabWidgetListener l : TrpVirtualKeyboardsTabWidget.this.listener) {
					l.onVirtualKeyPressed(TrpVirtualKeyboardsTabWidget.this, (char) e.detail, e.text);
				}
				
//				for (SelectionListener l : TrpVirtualKeyboardsTabWidget.this.selListener) {
//					Event e1 = new Event();
//					e1.widget = TrpVirtualKeyboardsTabWidget.this;
//
//					e1.detail = e.detail;
//					e1.text = e.text;
//					
//					l.widgetSelected(new SelectionEvent(e1));
//				}
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
		
		vk.addListener(new ITrpVirtualKeyboardsTabWidgetListener() {
			@Override public void onVirtualKeyPressed(TrpVirtualKeyboardsTabWidget w, char c, String description) {
				logger.debug("virtual keyboard button pressed, widget: "+vk+", c = "+c+", desc = "+description);
			}
		});

//		if (false)
//		vk.addKeySelectionListener(new SelectionListener() {
//			
//			@Override public void widgetSelected(SelectionEvent e) {
//				if (e.getSource() == vk) {
//					logger.info("event = "+e);
//					Character c = (char) e.detail;
//					logger.info("key pressed: "+c+" detail = "+e.detail+", name: "+e.text);
//					logger.info("name =" +e.text);
//					logger.info("e.detail =" +e.detail);
//					
////					ATranscriptionWidget tw = ui.getSelectedTranscriptionWidget();
////					if (tw != null) {
////						tw.insertTextIfFocused(""+c);
////					}
//				}
//			}
//			
//			@Override public void widgetDefaultSelected(SelectionEvent e) {
//			}
//		});

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
