package eu.transkribus.swt_gui.search.fulltext;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiSelectionCombo extends Composite {
private final static Logger logger = LoggerFactory.getLogger(MultiSelectionCombo.class);

   Shell    shell               = null;
   List     list                = null;
   FullTextSearchComposite parentComp;

   Text     txtCurrentSelection = null;

   String[] textItems           = null;
   int[]    currentSelection    = null;

   public MultiSelectionCombo(Composite parent, String[] items, int[] selection, int style, FullTextSearchComposite searchComposite) {
      super(parent, style);
      currentSelection = selection;
      textItems = items;
      parentComp = searchComposite;
      init();
   }

   private void init() {
      GridLayout layout = new GridLayout();
      layout.marginBottom = 0;
      layout.marginTop = 0;
      layout.marginLeft = 0;
      layout.marginRight = 0;
      layout.marginWidth = 0;
      layout.marginHeight = 0;
      setLayout(new GridLayout());
      txtCurrentSelection = new Text(this, SWT.BORDER | SWT.READ_ONLY);
      txtCurrentSelection.setLayoutData(new GridData(GridData.FILL_BOTH));

      displayText();

      txtCurrentSelection.addMouseListener(new MouseAdapter() {

         @Override
         public void mouseDown(MouseEvent event) {
            super.mouseDown(event);
            parentComp.checkMultiCombos();
            if(shell != null)
            {
            	if(shell.isDisposed() && parentComp.noMultiCombos){
            		initFloatShell();
            		parentComp.noMultiCombos = false;
            	}
            }else if(parentComp.noMultiCombos){
            	initFloatShell();
            	parentComp.noMultiCombos = false;
            }
            
            	

         }

      });
   }

   private void initFloatShell() {
      Point p = txtCurrentSelection.getParent().toDisplay(txtCurrentSelection.getLocation());
      Point size = txtCurrentSelection.getSize();
      Rectangle shellRect = new Rectangle(p.x, p.y + size.y, size.x, 0);
      shell = new Shell(MultiSelectionCombo.this.getShell(), SWT.NO_TRIM);

      GridLayout gl = new GridLayout();
      gl.marginBottom = 2;
      gl.marginTop = 2;
      gl.marginRight = 2;
      gl.marginLeft = 2;
      gl.marginWidth = 0;
      gl.marginHeight = 0;
      shell.setLayout(gl);

      list = new List(shell, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
      for (String value: textItems) {
    	  if(value!=null){
    		  list.add(value);
    	  }	 
         
      }

      list.setSelection(currentSelection);

      GridData gd = new GridData(GridData.FILL_BOTH);
      list.setLayoutData(gd);

      shell.setSize(shellRect.width, 200);
      shell.setLocation(shellRect.x, shellRect.y);

      list.addMouseListener(new MouseAdapter() {

         @Override
         public void mouseUp(MouseEvent event) {
            super.mouseUp(event);
            currentSelection = list.getSelectionIndices();            
            if ((event.stateMask & SWT.CTRL) == 0) {
//               displayText();
               parentComp.start = 0;
               parentComp.findText();               
               shell.dispose();
               
            }
         }
      });

      shell.addShellListener(new ShellAdapter() {

         public void shellDeactivated(ShellEvent arg0) {
            if (shell != null && !shell.isDisposed()) {
               currentSelection = list.getSelectionIndices();
//               displayText();
               parentComp.start = 0;
               parentComp.findText();
               shell.dispose();
            }
         }
      });      
      shell.open();
      
   }

   void displayText() {
      if (currentSelection != null && currentSelection.length > 0) {
         StringBuffer sb = new StringBuffer();
         for (int i = 0; i < currentSelection.length; i++) {
            if (i > 0)
               sb.append(", ");
            sb.append(textItems[currentSelection[i]]);
         }
         txtCurrentSelection.setText(sb.toString());
      }
      else {
         txtCurrentSelection.setText(textItems[0]);
      }
   }

   public int[] getSelections() {
      return this.currentSelection;
   }
   
   public void setTextItems(String[] newTextItems){
	   ArrayList<String> oldSelectionsT = new ArrayList<>();
	   ArrayList<Integer> newSelectionsI = new ArrayList<>();

	   for(int i : currentSelection){
		   oldSelectionsT.add(textItems[i].replaceAll("\\(.*\\)", "").trim());
	   }
	   int j=0;
	   for(String s : newTextItems){
		   if(s!=null){
			   String newText = s.replaceAll("\\(.*\\)", "").trim();

			   if(oldSelectionsT.contains(newText)){
				   newSelectionsI.add(j);			  
				   
			   }
			   j++;
		   }

	   }

	   
	   int[] newSelectionsIArr = new int[newSelectionsI.size()];
	   for(int i = 0; i<newSelectionsI.size(); i++){
		   newSelectionsIArr[i] = newSelectionsI.get(i);
	   }
	   

	   textItems = newTextItems;
	   currentSelection = newSelectionsIArr;
	   
	   displayText();

	   
   }


}