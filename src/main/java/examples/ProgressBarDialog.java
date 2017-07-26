package examples;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

/**
 * progress bar dialog.
 * the first, you must know your app execute times,
 * you need implement two method: 
 * 
 * process(int times);
 * initGuage();
 * 
 * you can implements method:
 *  
 * cleanUp()
 * doBefore()
 * doAfter() 
 * @author yin_zhiguo
 *         yin_zhiguo@hotmail.com
 * 
 */
public abstract class ProgressBarDialog extends Dialog {

    private Label processMessageLabel; //info of process finish 
    private Button cancelButton; //cancel button
    private Composite cancelComposite;
    private Label lineLabel;//
    private Composite progressBarComposite;//
    private CLabel message;//
    private ProgressBar progressBar = null; //

    private Object result; //
    private Shell shell; //
    private Display display = null; 
    
    protected volatile boolean isClosed = false;//closed state
    
    protected int executeTime = 50;//process times
    protected String processMessage = "process......";//procress info
    protected String shellTitle = "Progress..."; //
    protected Image processImage = SWTUtil.getImageOfMessage();//image
    protected boolean mayCancel = true; //cancel
    protected int processBarStyle = SWT.SMOOTH; //process bar style

  public void setMayCancel(boolean mayCancel) {
    this.mayCancel = mayCancel;
  }

    public void setExecuteTime(int executeTime) {
        this.executeTime = executeTime;
    }

    public void setProcessImage(Image processImage) {
    this.processImage = processImage;
  }

  public void setProcessMessage(String processInfo) {
    this.processMessage = processInfo;
  }

  public ProgressBarDialog(Shell parent) {
        super(parent);
    }
    
  
    public abstract void initGuage();

    public Object open() {
        createContents(); //create window
        shell.open();
        shell.layout();
        
        //start work
        new ProcessThread(executeTime).start();

        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return result;
    }

    protected void createContents() {
        shell = new Shell(getParent(), SWT.TITLE | SWT.PRIMARY_MODAL);
        display = shell.getDisplay();
        final GridLayout gridLayout = new GridLayout();
        gridLayout.verticalSpacing = 10;

        shell.setLayout(gridLayout);
        shell.setSize(483, 181);
        shell.setText(shellTitle);

        final Composite composite = new Composite(shell, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        composite.setLayout(new GridLayout());

        message = new CLabel(composite, SWT.NONE);
        message.setImage(processImage);
        message.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        message.setText(processMessage);

        progressBarComposite = new Composite(shell, SWT.NONE);
        progressBarComposite.setLayoutData(new GridData(GridData.FILL,
                GridData.CENTER, false, false));
        progressBarComposite.setLayout(new FillLayout());

        progressBar = new ProgressBar(progressBarComposite, processBarStyle);
        progressBar.setMaximum(executeTime);

        processMessageLabel = new Label(shell, SWT.NONE);
        processMessageLabel.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
        lineLabel = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
        lineLabel.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));

        cancelComposite = new Composite(shell, SWT.NONE);
        cancelComposite.setLayoutData(new GridData(GridData.END,
                GridData.CENTER, false, false));
        final GridLayout gridLayout_1 = new GridLayout();
        gridLayout_1.numColumns = 2;
        cancelComposite.setLayout(gridLayout_1);

        cancelButton = new Button(cancelComposite, SWT.NONE);
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent e) {
                isClosed = true;
                //System.out.println(isClosed);
            }
        });
        cancelButton.setLayoutData(new GridData(78, SWT.DEFAULT));
        cancelButton.setText("cancel");
        cancelButton.setEnabled(this.mayCancel);

    }

    protected abstract String process(int times);


    protected void cleanUp()
    {
      
    }
    

    protected void doBefore()
    {
      
    }
    
    protected void doAfter()
    {
      
    }

    class ProcessThread extends Thread {
        private int max = 0;
        private volatile boolean shouldStop = false;

        ProcessThread(int max) {
            this.max = max;
        }

        @Override
		public void run() {
          doBefore();
            for (final int[] i = new int[] {1}; i[0] <= max; i[0]++) 
            {
                //
                final String info = process(i[0]);
                if (display.isDisposed()) {
                    return;
                }
                display.syncExec(new Runnable() {
                    @Override
					public void run() {
                        if (progressBar.isDisposed()) {
                            return;
                        }
                        //
                        processMessageLabel.setText(info);
                        //
                        progressBar.setSelection(i[0]);
                        //
                        if (i[0] == max || isClosed) {
                            if (isClosed) {
                                shouldStop = true;//
                                cleanUp();//
                            }
                            shell.close();//
                        }
                    }
                });

                if (shouldStop) {
                    break;
                }
            }
            doAfter();
        }
    }

  public void setShellTitle(String shellTitle) {
    this.shellTitle = shellTitle;
  }

  public void setProcessBarStyle(boolean pStyle) {
    if(pStyle)
      this.processBarStyle = SWT.SMOOTH;
    else
      this.processBarStyle = SWT.NONE;
      
  }
  
  public static void main(String args[]) {
      try {
        Display display = Display.getDefault();
        PBDialogDemo shell = new PBDialogDemo(display, SWT.SHELL_TRIM);
        shell.open();
        shell.layout();
        while (!shell.isDisposed()) {
          if (!display.readAndDispatch())
            display.sleep();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
}


class DemoProgressBar extends ProgressBarDialog {
  
  private String[] info = null;
  
  public DemoProgressBar(Shell parent) {
       super(parent);
   }
  

  @Override
  public void initGuage() {
    info = new String[100];
    for (int i = 0; i < info.length; i++) {
      info[i] = "process task " + i + ".";
    }
    this.setExecuteTime(100);
    this.setMayCancel(true);
    this.setProcessMessage("please waiting....");
    this.setShellTitle("Demo");

  }

  @Override
  protected String process(int arg0) {
    try{
      Thread.sleep((long)(Math.random() * 300));
    }catch(Exception e){e.printStackTrace();}
    
    return info[arg0 - 1];
  }

}


class SWTUtil {


    private static ImageRegistry imageRegistry = new ImageRegistry();

    private static Clipboard clipboard = new Clipboard(Display.getCurrent());
    
    private static final String ___IMAGE_Of_MESSAGE = "";
    
    static{
      imageRegistry.put(___IMAGE_Of_MESSAGE, ImageDescriptor.createFromURL(newURL("file:icons/info2.gif")));
    }


    private SWTUtil(){}

    
    public static URL newURL(String url_name) {
        try {
            return new URL(url_name);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL " + url_name, e);
        }
    }
    
    
    public static void registryImage(String id, String urlName)
    {
      imageRegistry.put(id, ImageDescriptor.createFromURL(newURL(urlName)));
    }
    
    
    public static Image getImage(String id)
    {
      return imageRegistry.get(id);
    }

    
    public static Clipboard getClipboard() {
        return clipboard;
    }
    
    
    public static Image getImageOfMessage()
    {
      return imageRegistry.get(___IMAGE_Of_MESSAGE);
    }
}

class PBDialogDemo extends Shell {



  public PBDialogDemo(Display display, int style) {
    super(display, style);
    createContents();
  }

  protected void createContents() {
    setText("ProgressBar Dialog");
    setSize(218, 98);
    setLayout(new FillLayout());

    final Button openProgressbarDialogButton = new Button(this, SWT.NONE);
    openProgressbarDialogButton.addSelectionListener(new SelectionAdapter() {
      @Override
	public void widgetSelected(SelectionEvent e) {
        DemoProgressBar dpb = new DemoProgressBar(PBDialogDemo.this);
        dpb.initGuage();
        dpb.open();
      }
    });
    openProgressbarDialogButton.setText("Open ProgressBar Dialog");
    
  }

  @Override
protected void checkSubclass() {
  }

}