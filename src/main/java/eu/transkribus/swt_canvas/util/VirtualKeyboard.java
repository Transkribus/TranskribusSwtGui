package eu.transkribus.swt_canvas.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class VirtualKeyboard extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(VirtualKeyboard.class);
	
	SelectionListener internalSelectionListener;
	List<Button> unicodeButtons=new ArrayList<>();
	
	public VirtualKeyboard(Composite parent, int style, char unicodeStart, char unicodeEnd) {
		super(parent, style);
		
		initLayout();
		initInput();
		
        List<Character> unicode = new ArrayList<>();
        for (Character c=unicodeStart; c<=unicodeEnd; ++c)
        	unicode.add(c);
        
        initButtons(unicode);
        initInternalListener();
	}

	public VirtualKeyboard(Composite parent, int style, Collection<Character> unicode) {
		super(parent, style);
				
		initLayout();
		initInput();
        initButtons(unicode);
        initInternalListener();
	}
	
	private void initInput() {
//		Text inputText = new Text(this, SWT.NONE);
		
	}
	
	private void initLayout() {
		RowLayout rowLayout = new RowLayout();
        rowLayout.wrap = true;
        rowLayout.pack = true;
        rowLayout.justify = false;
        rowLayout.type = SWT.HORIZONTAL;
        rowLayout.marginLeft = 5;
        rowLayout.marginTop = 5;
        rowLayout.marginRight = 5;
        rowLayout.marginBottom = 5;
        rowLayout.spacing = 0;
        setLayout(rowLayout);
	}
	
	private void initButtons(Collection<Character> unicode) {
		String undefined = "";
		for (Character i : unicode) {
			if (!Character.isDefined(i)) {
				undefined += (int) i+" ";
				
//				throw new RuntimeException("Undefined unicode character: "+(int)i);
//				logger.warn("Undefined unicode value: "+(int)i);
				continue;
			}
			
			Button b = new Button(this, SWT.PUSH);
			
			FontData[] fD = b.getFont().getFontData();
			fD[0].setHeight(16);
//			b.setFont( new Font(getDisplay(), fD[0]));
			b.setFont(Fonts.createFont(fD[0]));
			
			b.setText(Character.toString(i));
			b.setToolTipText(Character.getName(i));
			
			unicodeButtons.add(b);
		}
		if (!undefined.isEmpty()) {
			logger.warn("Undefined unicode values in virtual keyboard: "+undefined);	
		}
	}
	
	private void initInternalListener() {
		internalSelectionListener = new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
//				logger.info("key pressed!!!");
				if (e.getSource() instanceof Button) {
					Button b = (Button) e.getSource();
					String text = b.getText();
					
					if (!text.isEmpty())
						onKeyPressed(b, text.charAt(0), b.getToolTipText());
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};
		
		addSelectionListener(internalSelectionListener);
	}
	
	protected void onKeyPressed(Button source, Character character, String name) {
//		logger.info("key pressed: "+character+" name = "+name);
//		logger.info("name = "+Character.getName(character));
	}
	
	public void addSelectionListener(SelectionListener listener) {
		for (Button b : getUnicodeButtons()) {
			b.addSelectionListener(listener);
		}
	}
	
	public void removeSelectionListener(SelectionListener listener) {
		for (Button b : getUnicodeButtons()) {
			b.removeSelectionListener(listener);
		}
	}	
	
	private List<Button> getUnicodeButtons() {
		return unicodeButtons;
//		List<Button> btns = new ArrayList<>();
//		for (Control c : this.getChildren()) {
//			if (c instanceof Button) {
//				btns.add((Button)c);
//			}
//		}
//		return btns;
	}
	
	public static void main(String [] args) throws Exception {
		Display display = new Display ();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		
		final VirtualKeyboard vk = new VirtualKeyboard(shell, SWT.SHELL_TRIM, (char) 65, (char) 122);
//		final VirtualKeyboard vk = new VirtualKeyboard(shell, SWT.NONE, (char) 65, (char) 122);
//		
		shell.pack();
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
