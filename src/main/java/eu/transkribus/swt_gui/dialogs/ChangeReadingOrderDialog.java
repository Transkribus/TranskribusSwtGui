package eu.transkribus.swt_gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ChangeReadingOrderDialog extends Dialog {
  private String message;

  private String input;
  
  Button allFollowingBtn;
  boolean doItForAll = false;

  public boolean isDoItForAll() {
	return doItForAll;
}

public void setDoItForAll(boolean doItForAll) {
	this.doItForAll = doItForAll;
}

public ChangeReadingOrderDialog(Shell parent, int ro) {
    this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL, ro);
  }

  public ChangeReadingOrderDialog(Shell parent, int style, int ro) {
    super(parent, style);
    setText("Change Reading Order");
    setMessage("Please enter new reading order value:");
    setInput(Integer.toString(ro+1));
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getInput() {
    return input;
  }

  public void setInput(String input) {
    this.input = input;
  }

  public String open(int x, int y) {
    Shell shell = new Shell(getParent(), getStyle());
    
    Point cursorLocation = Display.getCurrent().getCursorLocation();
    shell.setLocation(cursorLocation);
    //shell.setLocation(shell.getLocation().x, shell.getLocation().y);
    shell.setText(getText());
    createContents(shell);
    shell.pack();
    shell.open();
    Display display = getParent().getDisplay();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    return input;
  }

  private void createContents(final Shell shell) {
    shell.setLayout(new GridLayout(2, true));

    Label label = new Label(shell, SWT.NONE);
    label.setText(message);
    GridData data = new GridData();
    data.horizontalSpan = 2;
    label.setLayoutData(data);

    final Text text = new Text(shell, SWT.BORDER);
    data = new GridData(GridData.FILL_BOTH);
    data.horizontalSpan = 2;
    text.setLayoutData(data);
    text.setText(getInput());
    
    allFollowingBtn = new Button(shell, SWT.CHECK);
    allFollowingBtn.setLayoutData(data);
    allFollowingBtn.setText("Do it for all following");
    allFollowingBtn.addSelectionListener(new SelectionAdapter() {
    	public void widgetSelected(SelectionEvent event) {
    		setDoItForAll(allFollowingBtn.getSelection());
    	}
	});

    Button ok = new Button(shell, SWT.PUSH);
    ok.setText("OK");
    data = new GridData(GridData.FILL_BOTH);
    ok.setLayoutData(data);
    ok.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        input = text.getText();
        shell.close();
      }
    });

    Button cancel = new Button(shell, SWT.PUSH);
    cancel.setText("Cancel");
    data = new GridData(GridData.FILL_BOTH);
    cancel.setLayoutData(data);
    cancel.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        input = null;
        shell.close();
      }
    });
   
    shell.setDefaultButton(ok);
    //shell.layout(true);
  }
}
