package eu.transkribus.swt_gui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class TextFieldDialog extends Dialog {
		
	String text;
	String title;
	StyledText styledText;

	public TextFieldDialog(Shell parentShell, String title, String htmlText) {
		super(parentShell);
		
		this.title = title;
		this.text = htmlText;
	}
	
	  @Override
	  protected Control createDialogArea(Composite parent) {
	    Composite container = (Composite) super.createDialogArea(parent);
	    
//	    container.setLayout(new FillLayout());
	    
	    styledText = new StyledText(parent, SWT.READ_ONLY | SWT.BORDER | SWT.VERTICAL | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
	    styledText.setText(text);
	    styledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    
	    return container;
	  }

	  // overriding this methods allows you to set the
	  // title of the custom dialog
	  @Override
	  protected void configureShell(Shell newShell) {
	    super.configureShell(newShell);
	    newShell.setText(title);
	  }

	  @Override
	  protected Point getInitialSize() {
	    return new Point(450, 400);
	  }
	  
	  public static void main(String[] args) {
		  
		  
		  
		  
		
	}

}
