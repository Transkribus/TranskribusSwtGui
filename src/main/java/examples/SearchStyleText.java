package examples;
/******************************************************************************
 * All Right Reserved. 
 * Copyright (c) 1998, 2004 Jackwind Li Guojie
 * 
 * Created on Feb 22, 2004 12:43:18 AM by JACK
 * $Id$
 * 
 *****************************************************************************/



import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SearchStyleText {
  Display display = new Display();
  Shell shell = new Shell(display);

  StyledText styledText;
  Text keywordText;
  Button button;
  
  String keyword;
  
  public SearchStyleText() {
    shell.setLayout(new GridLayout(2, false));
    
    styledText = new StyledText(shell, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
    GridData gridData = new GridData(GridData.FILL_BOTH);
    gridData.horizontalSpan = 2;    
    styledText.setLayoutData(gridData);
    
    keywordText = new Text(shell, SWT.SINGLE | SWT.BORDER);
    keywordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    Font font = new Font(shell.getDisplay(), "Courier New", 12, SWT.NORMAL);
    styledText.setFont(font);
    
    button = new Button(shell, SWT.PUSH);
    button.setText("Search");
    button.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        keyword = keywordText.getText();
        styledText.redraw();
      }
    });
    
    styledText.addLineStyleListener(new LineStyleListener() {
      public void lineGetStyle(LineStyleEvent event) {
        if(keyword == null || keyword.length() == 0) {
          event.styles = new StyleRange[0];
          return;
        }
        
        String line = event.lineText;
        int cursor = -1;
        
        LinkedList list = new LinkedList();
        while( (cursor = line.indexOf(keyword, cursor+1)) >= 0) {
          list.add(getHighlightStyle(event.lineOffset+cursor, keyword.length()));
        }
        
        event.styles = (StyleRange[]) list.toArray(new StyleRange[list.size()]);
      }
    });
    
    keyword = "SW";
    
    styledText.setText("AWT, SWING \r\nSWT & JFACE");
    
    shell.pack();
    shell.open();
    //textUser.forceFocus();

    // Set up the event loop.
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        // If no more entries in event queue
        display.sleep();
      }
    }

    display.dispose();
  }
  
  private StyleRange getHighlightStyle(int startOffset, int length) {
    StyleRange styleRange = new StyleRange();
    styleRange.start = startOffset;
    styleRange.length = length;
    styleRange.background = shell.getDisplay().getSystemColor(SWT.COLOR_YELLOW);
    return styleRange;
  }


  public static void main(String[] args) {
    new SearchStyleText();
  }
}

