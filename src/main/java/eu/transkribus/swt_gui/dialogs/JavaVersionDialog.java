package eu.transkribus.swt_gui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.SysUtils.JavaInfo;
import eu.transkribus.swt.util.DesktopUtil;
import eu.transkribus.swt.util.Images;

public class JavaVersionDialog extends TitleAreaDialog {

	private final static Logger logger = LoggerFactory.getLogger(JavaVersionDialog.class);
	protected Shell shell;
	private Button javaVersionButton;
	List list;
	
	protected static final String TEXT_UTF = "JVM file.encoding is not \"UTF-8\". Please start Transkribus with the .exe or .bat file";
	protected static final String TEXT_ARCH = "You are running a Java 32-bit version on a 64-bit OS. Please download a 64-bit Java version.";
	protected static final String TEXT_JAVA10 = "Java 10 version is not yet compatible with Transkribus. Please download Java Version 8";
	protected static final String HELP_JAVA = "https://transkribus.eu/wiki/index.php/Download_and_Installation";
	String realArch;
	String javaArch;
	String version;
	String fileEnc;
	
	public JavaVersionDialog(Shell parent, int style, JavaInfo java) {
		super(parent);
		this.realArch = java.getSystemArch();
		this.javaArch = java.getJavaArch();
		this.version = java.getVersion();
		this.fileEnc = java.getFileEnc();
	}
	
	public boolean isJava10() {
		if (version.startsWith("1.10")) {
			return true;
		}else {
			return false;
		}
	}
	
	public boolean isWrongArch() {
		if (!realArch.equals(javaArch)) {
			return true;
		}else {
			return false;
		}
	}
	
	public boolean isNotUTF8() {
		if (!fileEnc.startsWith("UTF")) {
			return true;
		}else {
			return false;
		}
	}
	
	@Override
	public void create() {
	    super.create();
	    setErrorMessage("The following issues can influence the performance of Transkribus");
	    
	}
	
	 protected Control createDialogArea(Composite parent) {
		 
		    final Composite area = new Composite(parent, SWT.FILL);
		    final GridLayout gridLayout = new GridLayout();
		    area.setLayout(gridLayout);
		    list = new List(area, SWT.BORDER | SWT.MULTI);
		    final GridData gridData = new GridData();
		    gridData.widthHint = 590;
		    list.setLayoutData(gridData);
		    
		    if(isWrongArch()) {
		    	list.add(TEXT_ARCH);
		    }
		    if(isJava10()) {
		    	list.add(TEXT_JAVA10);
		    }
		    if(isNotUTF8()) {
		    	list.add(TEXT_UTF);
		    }
		    
		    return area;
	 }
	
	 @Override
	    protected void configureShell(Shell newShell) {
	        super.configureShell(newShell);
	        newShell.setText("Java Version not compatible with Transkribus");
	    }
	
	 @Override
	    protected Point getInitialSize() {
	        return new Point(610, 200);
	    }
	 
	 @Override
		protected void createButtonsForButtonBar(Composite parent) {

			javaVersionButton = createButton(parent, IDialogConstants.HELP_ID, "Java Help", false);
			javaVersionButton.setImage(Images.HELP);

			createButton(parent, IDialogConstants.OK_ID, "Ok", true);
			createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
			GridData buttonLd = (GridData) getButton(IDialogConstants.CANCEL_ID).getLayoutData();	
			
			javaVersionButton.setLayoutData(buttonLd);
			javaVersionButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					DesktopUtil.browse(HELP_JAVA, "You can find the relevant information on the Wiki page.",
							getParentShell());
				}
			});
	
	
	
	
	 }
}
