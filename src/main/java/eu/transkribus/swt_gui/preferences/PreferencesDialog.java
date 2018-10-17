package eu.transkribus.swt_gui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.canvas.CanvasSettings;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class PreferencesDialog extends Dialog {
	
	//top tier tab bar in this dialog
	protected CTabFolder mainTabFolder;
	
	//this list collects all configTabs
	protected final List<AConfigTab> configTabs;
	
	public PreferencesDialog(Shell parentShell) {
		super(parentShell);
		configTabs = new ArrayList<>();
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
		mainTabFolder = new CTabFolder(comp, SWT.BORDER | SWT.FLAT);
		mainTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		mainTabFolder.setLayout(new GridLayout(1, true));
		//add general program settings
//		GeneralTab generalTab = new GeneralTab(mainTabFolder);
		
		//add canvas settings
//		CanvasTab canvasTab = new CanvasTab(mainTabFolder);
		
		//add network settings (formerly known as proxy settings)
		NetworkTab networkTab = new NetworkTab(mainTabFolder);	
		
		//add developer options (e.g. logging http traffic)
//		DeveloperTab devTab =  new DeveloperTab(mainTabFolder);
		
		return comp;
	}
	
	@Override
	protected void okPressed() {
		// TODO do we really want one apply button for all settings tabs? 
		// Proxy tab has an apply button (copy pasted from ProxySettingsDialog) which makes sense as user input has to be checked
		configTabs.forEach(t -> t.applyToSettings());
		super.okPressed();
	}
	
	public void setVisible() {
		if (super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Preferences");
		newShell.setMinimumSize(800, 600);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 600);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
	}
	
	protected class GeneralTab extends AConfigTab {

		public GeneralTab(CTabFolder parentTabFolder) {
			super("General", parentTabFolder);
		}

		@Override
		protected void applyToSettings() {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void createContent(Composite tabContent) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void resetDialogToSettings() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	protected class CanvasTab extends AConfigTab {

		protected CanvasSettings canvasSettings;

		public CanvasTab(CTabFolder parentTabFolder) {
			super("Image Canvas", parentTabFolder);
			canvasSettings = TrpConfig.getCanvasSettings();
		}

		@Override
		protected void applyToSettings() {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void createContent(Composite tabContent) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void resetDialogToSettings() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	protected class NetworkTab extends AConfigTab {

		ProxySettingsWidget widget;
		
		public NetworkTab(CTabFolder parentTabFolder) {
			super("Network", parentTabFolder);
		}

		@Override
		public void applyToSettings() {
			try {
				widget.applyToSettings();
			} catch(IllegalArgumentException e) {
				//widget shows error message in a label now.
				// TODO what to do here then?
				return;
			}
			Storage.getInstance().updateProxySettings();
		}

		@Override
		public void createContent(Composite tabContent) {
			Group proxyGroup = new Group(tabContent, SWT.BORDER);
			proxyGroup.setText("Proxy Settings");
			proxyGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			proxyGroup.setLayout(new GridLayout(1, true));
			
			widget = new ProxySettingsWidget(proxyGroup, SWT.NONE);
			widget.pack();
		}

		@Override
		protected void resetDialogToSettings() {
			widget.updateFields();			
		}		
	}

	protected class DeveloperTab extends AConfigTab {
		
		protected TrpSettings settings;
		
		public DeveloperTab(CTabFolder parentTabFolder) {
			super("Developer", parentTabFolder);
			settings = TrpConfig.getTrpSettings();
		}

		@Override
		public void applyToSettings() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void createContent(Composite tabContent) {
			//checkbox for HTTP traffic logging
		}

		@Override
		protected void resetDialogToSettings() {
			// TODO Auto-generated method stub
			
		}
		
	}	
	
	protected abstract class AConfigTab extends CTabItem {
		
		private final String tabName;
		private Composite content;
		
		private Button applyButton;
		private Button resetButton;
		
		public AConfigTab(final String tabName, CTabFolder parentTabFolder) {
			super(parentTabFolder, SWT.NONE);
			this.tabName = tabName;
			setText(tabName);
			content = new Composite(parentTabFolder, SWT.NONE);
			content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			content.setLayout(new GridLayout(1, true));
			createContent(content);
			createTabButtonBar(content);
			setControl(content);
			configTabs.add(this);
		}
		
		private void createTabButtonBar(Composite tabContent) {
			Composite buttonComposite = new Composite(tabContent, SWT.NONE);
			buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
			buttonComposite.setLayout(new GridLayout(1, true));
			
			applyButton = new Button(buttonComposite, SWT.NONE);
			applyButton.setText("Apply");
			applyButton.addSelectionListener(new SelectionAdapter() {
				@Override public void widgetSelected(SelectionEvent e) {
					applyToSettings();
				}
			});
			applyButton.setToolTipText("Stores the configuration.");
			
			resetButton = new Button(buttonComposite, SWT.NONE);
			resetButton.setText("Reset");
			resetButton.addSelectionListener(new SelectionAdapter() {
				@Override public void widgetSelected(SelectionEvent e) {
					resetDialogToSettings();
				}
			});
			resetButton.setToolTipText("Resets the configuration.");
		}
		
		/**
		 * Store the settings from the dialog to TrpSettings, CanvasSettings or whatever is applicable
		 */
		protected abstract void applyToSettings();
		/**
		 * Reset dialog fields to the values currently in use
		 */
		protected abstract void resetDialogToSettings();
		
		/**
		 * Add the contents for this configuration tab to tabContent
		 * 
		 * @param tabContent
		 */
		protected abstract void createContent(Composite tabContent);
		
		
		
		public String getTabName() {
			return this.tabName;
		}
	}
}
