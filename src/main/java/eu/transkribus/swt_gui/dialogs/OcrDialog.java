package eu.transkribus.swt_gui.dialogs;

import java.io.IOException;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.enums.ScriptType;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.FinereaderUtils;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.util.DocPagesSelector;
import eu.transkribus.util.OcrConfig;

public class OcrDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(OcrDialog.class);
	
	private Button thisPageBtn, severalPagesBtn;
	private DocPagesSelector dps;
	private Combo typeFaceCombo;
	private Table langTab;
	private Label langStrLbl;
	
	private Storage store = Storage.getInstance();
	
	private OcrConfig config;
	private String pages;

	
	public OcrDialog(Shell parent) {
		super(parent);
	}
    
	public void setVisible() {
		if(super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		cont.setLayout(new GridLayout(3, false));
		
		thisPageBtn = new Button(cont, SWT.RADIO);
		thisPageBtn.setText("On this page");
		thisPageBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		thisPageBtn.setSelection(true);
		
		severalPagesBtn = new Button(cont, SWT.RADIO);
		severalPagesBtn.setText("Pages:");
		severalPagesBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		dps = new DocPagesSelector(cont, SWT.NONE, false, store.getDoc().getPages());
		dps.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		dps.setEnabled(false);
		
		severalPagesBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dps.setEnabled(severalPagesBtn.getSelection());
			}
		});
		
		
		Label typeFaceLbl = new Label(cont, SWT.NONE);
		typeFaceLbl.setText("Type Face:");
		
		typeFaceCombo = new Combo(cont, SWT.READ_ONLY);
		typeFaceCombo.setItems(new String[]{
				ScriptType.COMBINED.getStr(),
				ScriptType.GOTHIC.getStr(),
				ScriptType.NORMAL.getStr(),
				ScriptType.NORMAL_LONG_S.getStr()
				});
		typeFaceCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		typeFaceCombo.select(0);
		
		Label langLbl = new Label(cont, SWT.NONE);
		langLbl.setText("Languages:");
		langLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		langStrLbl = new Label(cont, SWT.NONE);
		langStrLbl.setText("");
		langStrLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		
		langTab = new Table(cont, SWT.CHECK);
		langTab.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		langTab.setItemCount(FinereaderUtils.FINEREADER_LANGUAGES.length);
		for(int i = 0; i < FinereaderUtils.FINEREADER_LANGUAGES.length; i++) {
			langTab.getItem(i).setText(FinereaderUtils.FINEREADER_LANGUAGES[i]);
		}
		
		langTab.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(SelectionEvent e) {
//		        if( e.detail == SWT.CHECK ) {
//		           
//		        }
		        TableItem item = ((TableItem)e.item);
		        item.setChecked(!item.getChecked());
		        updateLangStr();
		    }
		});
		
		config = store.loadOcrConfig();
		if(config == null) {
			config = new OcrConfig();
		}
		
		applyConfig();
		
		return cont;
	}

	private void applyConfig() {
		if(config == null) {
			return;
		}
		ScriptType st = config.getTypeFace();
		if(st != null) {
			for(int i = 0; i < typeFaceCombo.getItems().length; i++) {
				if(typeFaceCombo.getItem(i).equals(st.getStr())) {
					typeFaceCombo.select(i);
				}
			}
		}
		List<String> langs = config.getLanguages();
		logger.debug("Config.langStr: " + config.getLanguageString());

		for(int i = 0; i < langs.size(); i++) {
			int index = FinereaderUtils.getLanguageIndex(langs.get(i));
			langTab.getItem(index).setChecked(true);
			logger.debug("Select item = " + langs.get(i));
		}
		updateLangStr();
	}
	
	public void updateLangStr() {
		config.getLanguages().clear();
		for(int i = 0; i < langTab.getItemCount(); i++) {
			if(langTab.getItem(i).getChecked()) {
				config.getLanguages().add(langTab.getItem(i).getText());
			}
		}
		logger.debug("Setting label text: " + config.getLanguageString());
		langStrLbl.setText(config.getLanguageString());
	}
	
	@Override
	protected void okPressed() {
		if(thisPageBtn.getSelection()) {
			pages = ""+store.getPage().getPageNr();
		} else if(severalPagesBtn.getSelection()) {
			pages = dps.getPagesText().getText();
		}
		
		if(pages == null || pages.isEmpty()) {
			DialogUtil.showErrorMessageBox(this.getParentShell(), "Error", "Please specify pages for recognition.");
			return;
		}
		
		try {
			CoreUtils.parseRangeListStr(pages, store.getDoc().getNPages());
		} catch (IOException e) {
			DialogUtil.showErrorMessageBox(this.getParentShell(), "Error", "Page selection is invalid.");
			return;
		}
		logger.debug("Type face = " + typeFaceCombo.getText());
		config.setTypeFace(ScriptType.fromString(typeFaceCombo.getText()));
		
		if(config.getLanguages().size() == 0) {
			DialogUtil.showErrorMessageBox(this.getParentShell(), "Error", "You have to select at least one language.");
			return;
		}
		
		logger.debug(config.getLanguageString());
		
		store.saveOcrConfig(config);
		
		super.okPressed();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Optical Character Recognition");
		newShell.setMinimumSize(300, 400);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(400, 700);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
		// setBlockOnOpen(false);
	}
	
	public OcrConfig getConfig() {
		return this.config;
	}
	
	public String getPages() {
		return this.pages;
	}
}
