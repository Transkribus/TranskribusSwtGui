package eu.transkribus.swt_canvas.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt_canvas.canvas.CanvasKeys;

import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class XmlViewer extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(XmlViewer.class);
	
	Shell shell;
	boolean result=false;
	URL url;
	StyledText text;
	Text keywordText;
	Button searchBtn, caseSensitveCheck, wholeWordCheck, previousCheck, wrapSearchCheck;
	String keyword;
	int lastFoundIndex=-1;
	
	public XmlViewer(Shell parent, int style) {
		super(parent, style |= /*SWT.DIALOG_TRIM  |*/ SWT.SHELL_TRIM );
//		super(parent, style);
	}
	
	private void createContents() throws IOException {
		shell = new Shell(getParent(), getStyle() );
//		shell.setSize(673, 420);
		shell.setSize(300, 200);
		shell.setText("XML Viewer");
		int nCols = 2;
		shell.setLayout(new GridLayout(nCols, false));
//		shell.setLayout(new FillLayout());
		
		Label l1 = new Label(shell, SWT.NONE);
		l1.setText("Source: ");
		FontData fd = l1.getFont().getFontData()[0];
		fd.setStyle(SWT.BOLD);
//		l1.setFont(new Font(shell.getDisplay(), fd));
		l1.setFont(Fonts.createFont(fd));
		
		Text srcText = new Text(shell, SWT.READ_ONLY);
		srcText.setText(url.toString());
		
		Composite btnsC = new Composite(shell, 0);
		btnsC.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		btnsC.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		keywordText = new Text(btnsC, SWT.SINGLE | SWT.BORDER);
		keywordText.addKeyListener(new KeyAdapter() {
			@Override public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					search();
				}
			}
		});
		keywordText.addModifyListener(new ModifyListener() {
			@Override public void modifyText(ModifyEvent e) {
				search();
			}
		});
		keywordText.setFocus();
		
//	    keywordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    
//	    Font font = new Font(shell.getDisplay(), "Courier New", 12, SWT.NORMAL);
//	    text.setFont(font);
	    
	    searchBtn = new Button(btnsC, SWT.PUSH);
	    searchBtn.setText("Search");
	    searchBtn.addSelectionListener(new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent e) {
	    	  search();
//	    	  keyword = keywordText.getText();
//	    	  styledText.redraw();
	      }
	    });
	    
	    caseSensitveCheck = new Button(btnsC, SWT.CHECK);
	    caseSensitveCheck.setText("Case sensitive");
//	    caseSensitveCheck.setSelection(true);
	    
	    wholeWordCheck = new Button(btnsC, SWT.CHECK);
	    wholeWordCheck.setText("Whole word");
	    
	    previousCheck = new Button(btnsC, SWT.CHECK);
	    previousCheck.setText("Previous");
	    
	    wrapSearchCheck = new Button(btnsC, SWT.CHECK);
	    wrapSearchCheck.setText("Wrap search");
	    wrapSearchCheck.setSelection(true);
		
		String textStr = readUrl(url);
		text = new StyledText(shell, SWT.READ_ONLY | SWT.BORDER | SWT.VERTICAL 
				| SWT.H_SCROLL);
		GridData gld = new GridData(SWT.FILL, SWT.FILL, true, true, nCols, 1);
		gld.widthHint = 800;
		gld.heightHint = 600;
		text.setLayoutData(gld);
		text.setText(textStr);
		
		text.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {
				logger.debug("key pressed: "+e.keyCode);
				if (CanvasKeys.isCtrlKeyDown(e) && e.keyCode == 'f') {
					keywordText.forceFocus();
					keywordText.selectAll();
				}
			}
		});		
		
		highlightXml();
		
		shell.pack();
	}
	
	private void search() {
		int startSearchIndex = lastFoundIndex+1 >= text.getCharCount() ? 0 : lastFoundIndex+1;
		if (!keywordText.getText().equals(keyword)) { // new word!
			lastFoundIndex = -1;
			startSearchIndex = text.getCaretOffset();
		}
		keyword = keywordText.getText();		
		logger.debug("search for keyword: "+keyword+" startSearchIndex: "+startSearchIndex);
		
		// NEW:
		boolean caseSensitive = caseSensitveCheck.getSelection();
		boolean wholeWord = wholeWordCheck.getSelection();
		boolean previous = previousCheck.getSelection();
		boolean wrap = wrapSearchCheck.getSelection();
		
		int index = CoreUtils.indexOf(text.getText(), keyword, startSearchIndex, previous, caseSensitive, wholeWord);
		logger.debug("index = "+index);
		if (index == -1 && wrap) {
			int newStart = previous ? text.getCharCount()-1 : 0;
			logger.debug("newStart = "+newStart);
			index = CoreUtils.indexOf(text.getText(), keyword, newStart, previous, caseSensitive, wholeWord);
			logger.debug("index, wrapped = "+index);
		}
		
		if (index != -1) {
			lastFoundIndex = index;
		}
		
//		lastFoundIndex = CoreUtils.indexOf(text.getText(), keyword, startSearchIndex, previous, caseSensitive, wholeWord);
//		if (lastFoundIndex == -1 && wrap)
//			lastFoundIndex = CoreUtils.indexOf(text.getText(), keyword, 0, previous, caseSensitive, wholeWord);

		// OLD:
//		lastFoundIndex = text.getText().indexOf(keyword, startSearchIndex);
//		if (lastFoundIndex == -1)
//			lastFoundIndex = text.getText().indexOf(keyword, 0);
		
		highlightXml();
		
		if (lastFoundIndex!=-1) {
			text.setCaretOffset(lastFoundIndex);
			centerCurrentLine();
		}
	}
		
	private void centerCurrentLine() {
		int ci = text.getLineAtOffset(text.getCaretOffset());
		int ti = text.getTopIndex();
		int bi = JFaceTextUtil.getBottomIndex(text);
//		logger.debug("ci = "+ci+" ti = "+ti+" bi = "+bi);
		if ((ci < ti && ci-1>=0) || (ci > bi && ci-1>=0)) {
//			logger.debug("setting top index to: "+(ci-1));
			text.setTopIndex(ci-1);
		}
	}
	
	private void setLineBullet() {
		StyleRange style = new StyleRange();
		style.metrics = new GlyphMetrics(0, 0, Integer.toString(text.getLineCount() + 1).length() * 12);
		style.background = Colors.getSystemColor(SWT.COLOR_GRAY);
		Bullet bullet = new Bullet(ST.BULLET_NUMBER, style);
		text.setLineBullet(0, text.getLineCount(), bullet);
//		text.setLineIndent(0, text.getLineCount(), 25);
//		text.setLineAlignment(0, text.getLineCount(), textAlignment);
//		text.setLineWrapIndent(0, text.getLineCount(), 25+style.metrics.width);		
	}
	
	private void highlightXml() {
		setLineBullet();
		
		TextPresentation tr = new TextPresentation();
		StyleRange dsr = new StyleRange(0, text.getCharCount(), Colors.getSystemColor(SWT.COLOR_BLACK), Colors.getSystemColor(SWT.COLOR_WHITE));
		tr.setDefaultStyleRange(dsr);

		List<XmlRegion> regions = new XmlRegionAnalyzer().analyzeXml(text.getText());
		List<StyleRange> ranges = computeStyleRanges(regions);
		
		tr.replaceStyleRanges(ranges.toArray(new StyleRange[0]));
		
		text.setStyleRanges(ranges.toArray(new StyleRange[0]));
		
		if (lastFoundIndex !=-1) {
			StyleRange styleRange = new StyleRange();
		    styleRange.start = lastFoundIndex;
		    styleRange.length = keyword.length();
		    styleRange.background = shell.getDisplay().getSystemColor(SWT.COLOR_YELLOW);
		    tr.mergeStyleRange(styleRange);
//		    text.setStyleRange(styleRange);
		}
		
		List<StyleRange> allSrs = new ArrayList<>();
		Iterator<StyleRange> it = tr.getAllStyleRangeIterator();
		while (it.hasNext()) {
			StyleRange sr = it.next();
			allSrs.add(sr);
		}
		text.setStyleRanges(allSrs.toArray(new StyleRange[0]));
		
	}
	
	private String readUrl(URL url) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(
				url.openStream()));

		String str = "";
		String inputLine;
		while ((inputLine = in.readLine()) != null)
			str += inputLine + "\n";
		in.close();
		return str;
	}
	
	public boolean open(URL url) throws IOException {
		
		result = false;
		this.url = url;
		createContents();
		
		SWTUtil.centerShell(shell);
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Computes style ranges from XML regions.
	 * @param regions an ordered list of XML regions
	 * @return an ordered list of style ranges for SWT styled text
	 */
	public static List<StyleRange> computeStyleRanges( List<XmlRegion> regions ) {
	 
	    List<StyleRange> styleRanges = new ArrayList<StyleRange> ();
	    for( XmlRegion xr : regions ) {
	 
	        // The style itself depends on the region type
	        // In this example, we use colors from the system
	        StyleRange sr = new StyleRange();
	        int colorCode = -1;
	        switch( xr.getXmlRegionType()) {
	            case MARKUP:
	            	colorCode = SWT.COLOR_DARK_GREEN;
	                sr.fontStyle = SWT.BOLD;
	                break;
	 
	            case ATTRIBUTE:
	            	colorCode =  SWT.COLOR_DARK_RED;
	                break;
	 
	            // And so on...
	            case ATTRIBUTE_VALUE: colorCode = SWT.COLOR_DARK_MAGENTA; break;
	            case MARKUP_VALUE: break;
	            case COMMENT: colorCode = SWT.COLOR_GRAY; break;
	            case INSTRUCTION: break;
	            case CDATA: break;
	            case WHITESPACE: break;
	            default: break;
	        }
	        if (colorCode != -1) {
	        	sr.foreground = Display.getDefault().getSystemColor( colorCode );
	        }
	 
	        // Define the position and limit
	        sr.start = xr.getStart();
	        sr.length = xr.getEnd() - xr.getStart();
	        styleRanges.add( sr );
	    }
	 
	    return styleRanges;
	}
}
