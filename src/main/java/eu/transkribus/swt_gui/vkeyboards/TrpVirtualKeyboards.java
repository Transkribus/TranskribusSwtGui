package eu.transkribus.swt_gui.vkeyboards;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.dea.swt.util.VirtualKeyboard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//public class TrpVirtualKeyboards extends TabFolder {
public class TrpVirtualKeyboards extends CTabFolder {
	private final static Logger logger = LoggerFactory.getLogger(TrpVirtualKeyboards.class);
	
	final String VK_XML = "./virtualKeyboards.xml";
	
	Set<SelectionListener> selListener = new HashSet<>();
	
	static String unicodeRangeRegex="[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}";
	static Pattern pattern = Pattern.compile("[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}");
	
	public TrpVirtualKeyboards(Composite parent, int style) {
		super(parent, style);
		
		Properties p = new Properties();
		try {
			p.loadFromXML(new FileInputStream(new File(VK_XML)));
			
			// sort keys:
			Enumeration<Object> keys = p.keys();
			List<String> keysList = new ArrayList<>();
			while (keys.hasMoreElements()) {
				keysList.add((String) keys.nextElement());
			}
			Collections.sort(keysList);
			
			// for each key, construct virtual keyboard:
			for (String key : keysList) {
				String value = p.getProperty(key);
				
				List<Character> chars = new ArrayList<Character>();
				for (String split : value.split(" ")) {
					if (split.matches(unicodeRangeRegex)) {
						Pair<Character, Character> range = tryParseRange(split);
						if (range!=null) {
							for (Character c = range.getLeft(); c <= range.getRight(); ++c) {
								if (!chars.contains(c))
									chars.add(c);
							}
						}
					} else {
						for (int j=0; j<value.length(); ++j) {
							Character c = new Character(value.charAt(j));
							if (!Character.isWhitespace(c))
								if (!chars.contains(c))
									chars.add(c);
						}
					}
				}
				addVirtualKeyboardTab(chars, key);
			}
			
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		
	}
	
	private Pair<Character, Character> tryParseRange(String value) {
		if (!value.contains("-"))
			return null;
		
		String[] splits = value.split("-");
		if (splits.length!=2)
			return null;
		
		if (splits[0].length()!=4 || splits[1].length()!=4)
			return null;
		
		try {
			
			char start=(char) Integer.parseInt(splits[0], 16);
			char end=(char) Integer.parseInt(splits[1], 16);
			
//			logger.debug("start = "+start+" end = "+end);
			
			return Pair.of(start, end);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public void checkSubclass() {}	
	
	
	@Override
	public void addSelectionListener(SelectionListener l) {
		selListener.add(l);
	}
	
	@Override
	public void removeSelectionListener(SelectionListener l) {
		selListener.remove(l);
	}
	
	private VirtualKeyboard addVirtualKeyboardTab(Character start, Character end, String name) {
		List<Character> chars = new ArrayList<>();
		for (Character c = start; c <= end; ++c) {
			chars.add(c);
		}
		
		return addVirtualKeyboardTab(chars, name);
	}
	
	private VirtualKeyboard addVirtualKeyboardTab(Collection<Character> chars, String name) {
		VirtualKeyboard vk = new VirtualKeyboard(this, SWT.NONE, chars) {
			@Override protected void onKeyPressed(Button source, Character character, String name) {
				for (SelectionListener l : selListener) {
					Event e = new Event();
					e.widget = TrpVirtualKeyboards.this;
					e.detail = character;
					e.text = name;
					
					l.widgetSelected(new SelectionEvent(e));
				}
			}
		};
		
		vk.pack();
		CTabItem tabItem = new CTabItem(this, SWT.NONE);
		
		tabItem.setText(name);
		tabItem.setControl(vk);			
				
		return vk;
	}
	
	public static void main(String[] args) {
		String css = "0012-45AF";
		String regex = "[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}";
		
		System.out.println(css.matches(regex));
		

		Matcher matcher = pattern.matcher(css);
		
		while (matcher.find()) {
		    System.out.println(matcher.group()+" "+matcher.start()+" "+matcher.end());
		}
	}

}
