package eu.transkribus.swt_gui.dialogs.la;

import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.job.enums.JobImpl;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt.util.SWTUtil;

public class LayoutAnalysisConfigDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(LayoutAnalysisConfigDialog.class);
	
	CTabFolder tf;
	
	CTabItem ncsrTab;
	CTabItem cvlTab;
	CTabItem citlabTab;
	CTabItem customTab;
		
	Composite ncsrCtrl;
	Composite cvlCtrl;
	Composite citlabCtrl;
	Composite customCtrl;
	
	LabeledText customJobImplText;
	
	// saved values:
	String jobImpl;
	Map<String, Object> pars;
	
	public LayoutAnalysisConfigDialog(Shell parentShell) {
		super(parentShell);
		
		setShellStyle(SWT.SHELL_TRIM | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
	}
	
	public void setVisible() {
		if(super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}
	
	@Override protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Layout Analysis Configuration");
		SWTUtil.centerShell(newShell);
	}
	
	@Override protected Point getInitialSize() {
		return new Point(300, 300);
	}
	
	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FillLayout());
		
		createTabFolder(container);
		
		getShell().addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(DisposeEvent e) {
				saveValues();
			}
		});
		
		return container;
	}
	
	private void saveValues() {
		
		if (tf.getSelection() == ncsrTab) {
			jobImpl = JobImpl.NcsrLaJob.toString();
			
		}
		else if (tf.getSelection() == cvlTab) {
			jobImpl = JobImpl.CvlLaJob.toString();
			
		}
		else if (tf.getSelection() == citlabTab) {
			jobImpl = JobImpl.CITlabLaJob.toString();
			
		}
		else if (tf.getSelection() == customTab) {
			jobImpl = customJobImplText.getText();
			
		} else {
			logger.error("No tab selected - should not happen!");
		}
		
		logger.debug("saveValue, jobImpl = "+jobImpl);
		
	}
	
	private void createTabFolder(Composite container) {
		tf = new CTabFolder(container, SWT.NONE);
		tf.setLayout(new FillLayout());
	
		createNcsrCtrl();
		ncsrTab = SWTUtil.createCTabItem(tf, ncsrCtrl, SWT.FILL, "NCSR", "");

		createCvlCtrl();
		cvlTab = SWTUtil.createCTabItem(tf, ncsrCtrl, SWT.FILL, "CVL", "");
		
		createCitlabCtrl();
		citlabTab = SWTUtil.createCTabItem(tf, ncsrCtrl, SWT.FILL, "CITlab", "");
		
		createCustomCtrl();
		customTab = SWTUtil.createCTabItem(tf, ncsrCtrl, SWT.FILL, "Custom", "");
		
		tf.setSelection(ncsrTab);
	}
	
	private Composite createNcsrCtrl() {
		ncsrCtrl = new Composite(tf, SWT.NONE);
		return ncsrCtrl;
	}
	
	private Composite createCvlCtrl() {
		cvlCtrl = new Composite(tf, SWT.NONE);
		return ncsrCtrl;
	}
	
	private Composite createCitlabCtrl() {
		citlabCtrl = new Composite(tf, SWT.NONE);
		return ncsrCtrl;
	}
	
	private Composite createCustomCtrl() {
		customCtrl = new Composite(tf, SWT.NONE);
		
		customJobImplText = new LabeledText(customCtrl, "JobImpl: ");
		
		return ncsrCtrl;
	}
		  


}
