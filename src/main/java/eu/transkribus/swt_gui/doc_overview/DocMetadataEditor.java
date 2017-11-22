package eu.transkribus.swt_gui.doc_overview;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.nebula.widgets.datechooser.DateChooserCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.enums.ScriptType;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.EnumUtils;
import eu.transkribus.core.util.FinereaderUtils;
import eu.transkribus.core.util.StrUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.edit_decl_manager.EditDeclManagerDialog;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.tools.LanguageSelectionTable;

public class DocMetadataEditor extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(DocMetadataEditor.class);
	
	Text titleText;
	Text authorText;
	Label uploadedLabel;
	Text genreText;
	Text writerText;
	Button saveBtn;
	Text descriptionText;
	Combo scriptTypeCombo, scriptTypeCombo2;
	LanguageSelectionTable langTable;
	
	DateChooserCombo createdFrom, createdTo;
	
	Button enableCreatedFromBtn, enableCreatedToBtn, openEditDeclManagerBtn;
	EditDeclManagerDialog edm;
	
	DocMetadataEditorListener listener;
	
//	FutureTask futureSaveTask;
	
	Thread saveThread;
	
	public static final String PRINT_META_SCRIPTTYPE = "Printed";
	
	public DocMetadataEditor(Composite parent, int style) {
		this(parent, style, null);
	}
	
	public DocMetadataEditor(Composite parent, int style, final String message) {
		super(parent, style);
		setLayout(new GridLayout(2, false));
		
		if(message != null){
			Group warnGrp = new Group(this, SWT.NONE);
			warnGrp.setLayout(new GridLayout(1, false));
			warnGrp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			Label warning = new Label(warnGrp, SWT.NONE);
			warning.setText(message);
			warning.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
		}
		
		Label lblNewLabel = new Label(this, SWT.NONE);
		lblNewLabel.setText("Title:");
		
		titleText = new Text(this, SWT.BORDER);
		GridData gd_titleText = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
//		gd_titleText.widthHint = 366;
//		gd_titleText.widthHint = 200;
		titleText.setLayoutData(gd_titleText);
		
		Label lblAuthor = new Label(this, SWT.NONE);
		lblAuthor.setText("Author:");
		
		authorText = new Text(this, SWT.BORDER);
		authorText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblUploaded = new Label(this, SWT.NONE);
		lblUploaded.setText("Uploaded:");
		
		uploadedLabel = new Label(this, SWT.BORDER);
		uploadedLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblGenre = new Label(this, SWT.NONE);
		lblGenre.setText("Genre:");
		
		genreText = new Text(this, SWT.BORDER);
		genreText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblWriter = new Label(this, SWT.NONE);
		lblWriter.setText("Writer:");
		
		writerText = new Text(this, SWT.BORDER);
		writerText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		new Label(this, SWT.NONE);
		
		Label lblLang = new Label(this, SWT.NONE);
		lblLang.setText("Language:");
				
		langTable = new LanguageSelectionTable(this, 0);
		String[] tmpArr = new String[FinereaderUtils.ALL_LANGUAGES.size()];
		tmpArr = FinereaderUtils.ALL_LANGUAGES.toArray(tmpArr);
		langTable.setAvailableLanguages(tmpArr);
		langTable.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		
		Label l0 = new Label(this, 0);
		l0.setText("Script type: ");
		
		Composite c = new Composite(this, SWT.NONE);
		c.setLayout(new GridLayout(2, true));
		c.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		scriptTypeCombo = new Combo(c, SWT.DROP_DOWN | SWT.READ_ONLY);
		scriptTypeCombo.setItems(new String[]{ScriptType.HANDWRITTEN.getStr(), PRINT_META_SCRIPTTYPE});
		scriptTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		scriptTypeCombo.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				if(PRINT_META_SCRIPTTYPE.equals(scriptTypeCombo.getText())){
					scriptTypeCombo2.setEnabled(true);
				} else {
					scriptTypeCombo2.setText("");
//					scriptTypeCombo2.select(-1);
					scriptTypeCombo2.setEnabled(false);
				}
			}
		});
		scriptTypeCombo2 = new Combo(c, SWT.DROP_DOWN | SWT.READ_ONLY);
//		scriptTypeCombo2.setItems(EnumUtils.stringsArray(ScriptType.class));
		scriptTypeCombo2.setItems(new String[]{"", ScriptType.NORMAL.getStr(), ScriptType.NORMAL_LONG_S.getStr(), 
				ScriptType.GOTHIC.getStr()});
		scriptTypeCombo2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblCreateDates = new Label(this, SWT.NONE);
		lblCreateDates.setText("Date of writing:");
		
		Composite dateComposite = new Composite(this, SWT.NONE);
		dateComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 4, 1));
		dateComposite.setLayout(new GridLayout(2, false));
		
		enableCreatedFromBtn = new Button(dateComposite, SWT.CHECK);
		enableCreatedFromBtn.setSelection(false);
		enableCreatedFromBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				createdFrom.setEnabled(enableCreatedFromBtn.getSelection());
			}
		});
		enableCreatedFromBtn.setText("From:");		
//		createdFrom = new SCSimpleDateTimeWidget(dateComposite, SWT.NONE);
		createdFrom = new DateChooserCombo(dateComposite, SWT.NONE);
		createdFrom.setEnabled(false);

		enableCreatedToBtn = new Button(dateComposite, SWT.CHECK);
		enableCreatedToBtn.setSelection(false);
		enableCreatedToBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				createdTo.setEnabled(enableCreatedToBtn.getSelection());
			}
		});
		enableCreatedToBtn.setText("To:");
		createdTo = new DateChooserCombo(dateComposite, SWT.NONE);
		createdTo.setEnabled(false);
		
		openEditDeclManagerBtn = new Button(this, SWT.PUSH);
		openEditDeclManagerBtn.setText("Editorial Declaration...");
		openEditDeclManagerBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));		
		
		Label lblDescription = new Label(this, SWT.None);
		lblDescription.setText("Description: ");
		lblDescription.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));
		
//		lblDescription.setLayoutData(GridData.HORIZONTAL_ALIGN_BEGINNING);
				
		descriptionText = new Text(this, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		GridData descTextGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		final int nVisibleLines = 5;
		GC gc = new GC(descriptionText);
		descTextGridData.heightHint = nVisibleLines * gc.getFontMetrics().getHeight();
		gc.dispose();
		descriptionText.setLayoutData(descTextGridData);
		
//		descriptionText.setLayoutData(GridData.FILL_BOTH);
		
		saveBtn = new Button(this, SWT.NONE);
		saveBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		saveBtn.setImage(Images.DISK);
		saveBtn.setText("Save");
		
		addListener();
		
		setMetadataToGui(null);
	}
	
	void addListener() {
		listener = new DocMetadataEditorListener(this);
	}
	
//	ExecutorService executor = Executors.newFixedThreadPool(5);
	
	void saveMd() {
		TrpMainWidget mw = TrpMainWidget.getInstance();
		TrpDoc doc = Storage.getInstance().getDoc();
		
		if (mw == null || doc == null)
			return;
		
		boolean hasChanged = updateMetadataObjectFromGui(doc.getMd());
		
		if (!hasChanged)
			return;
		
		logger.debug("doc-md has changed - saving in background!");

		if (saveThread != null && saveThread.isAlive()) {
			saveThread.interrupt();
		}
		
		saveThread = new Thread(() -> { mw.saveDocMetadata(); } );
		saveThread.start();
	}
	
	/**
	 * Compare mutable md fields. Returns true if a value has changed. Empty string vs. null value does not count as change.
	 * 
	 * @param md
	 * @return
	 */
	public boolean hasChanged(TrpDocMetadata md) {
		if (md == null)
			return false;	
		
		if (!StrUtil.equalsContent(titleText.getText(), md.getTitle())) {
			logger.debug("title changed: '" + titleText.getText() + "' <-> '" + md.getTitle() + "'");
			return true;
		}
		if (!StrUtil.equalsContent(authorText.getText(), md.getAuthor())) {
			logger.debug("author changed: '" + authorText.getText() + "' <-> '" + md.getAuthor() + "'");
			return true;
		}
		if (!StrUtil.equalsContent(genreText.getText(), md.getGenre())) {
			logger.debug("genre changed: '" + genreText.getText() + "' <-> '" + md.getGenre() + "'");
			return true;
		}
		if (!StrUtil.equalsContent(writerText.getText(), md.getWriter())) {
			logger.debug("writer changed: '" + writerText.getText() + "' <-> '" + md.getWriter() + "'");
			return true;
		}
		if (!StrUtil.equalsContent(descriptionText.getText(), md.getDesc())) {
			logger.debug("description changed: '" + descriptionText.getText() + "' <-> '" + md.getDesc() + "'");
			return true;
		}
		if (!StrUtil.equalsContent(langTable.getSelectedLanguagesString(), md.getLanguage())) {
			logger.debug("language changed: '" + langTable.getSelectedLanguagesString() + "' <-> '" + md.getLanguage() + "'");
			return true;
		}
		
		if (!CoreUtils.equalsObjects(getSelectedScriptType(), md.getScriptType())) {
			logger.debug("scriptType changed: '" + getSelectedScriptType() + "' <-> '" + md.getScriptType() + "'");
			return true;
		}
		
		if (!CoreUtils.equalsObjects(getSelectedCreatedFromDate(), md.getCreatedFromDate())) {
			logger.debug("createDateFrom changed: '" + getSelectedCreatedFromDate() + "' <-> '" + md.getCreatedFromDate() + "'");
			return true;
		}
		if (!CoreUtils.equalsObjects(getSelectedCreatedToDate(), md.getCreatedToDate())) {
			logger.debug("createDateTo changed: '" + getSelectedCreatedToDate() + "' <-> '" + md.getCreatedToDate() + "'");
			return true;
		}
		
		return false;
	}
	
	public boolean updateMetadataObjectFromGui(TrpDocMetadata md) {	
		if (!hasChanged(md))
			return false;
		
		logger.debug("updating doc-metadata object: "+md);
				
		//don't update missing fields with empty strings
		md.setTitle(StringUtils.isEmpty(titleText.getText()) ? null : titleText.getText());
		md.setAuthor(StringUtils.isEmpty(authorText.getText()) ? null : authorText.getText());
		md.setGenre(StringUtils.isEmpty(genreText.getText()) ? null : genreText.getText());
		md.setWriter(StringUtils.isEmpty(writerText.getText()) ? null : writerText.getText());
		md.setDesc(StringUtils.isEmpty(descriptionText.getText()) ? null : descriptionText.getText());
		md.setLanguage(StringUtils.isEmpty(langTable.getSelectedLanguagesString()) ? null : langTable.getSelectedLanguagesString());
		md.setScriptType(getSelectedScriptType());

		md.setCreatedFromDate(getSelectedCreatedFromDate());
		md.setCreatedToDate(getSelectedCreatedToDate());
		
		logger.debug("doc-metadata object after update: "+md);
		return true;
	}
	
	Date getSelectedCreatedFromDate() {
		if(isCreatedFromEnabled()){
			return getCreatedFromDate();
		} else {
			return null;
		}
	}
	
	Date getSelectedCreatedToDate() {
		if(isCreatedFromEnabled()){
			return getCreatedToDate();
		} else {
			return null;
		}
	}	
	
	ScriptType getSelectedScriptType() {
		final String stStr = getScriptTypeCombo().getText();
		ScriptType st;
		if(PRINT_META_SCRIPTTYPE.equals(stStr)){
			st = EnumUtils.fromString(ScriptType.class, getScriptTypeCombo2().getText());
		} else {
			st = EnumUtils.fromString(ScriptType.class, getScriptTypeCombo().getText());
		}
		logger.trace("script type1: "+st);
		return st;
	};
	
	public void setMetadataToGui(TrpDocMetadata md) {
		titleText.setText(md!=null && md.getTitle()!=null ? md.getTitle() : "");
		authorText.setText(md!=null && md.getAuthor()!=null ? md.getAuthor() : "");
		uploadedLabel.setText(md!=null && md.getUploadTime()!=null&&md.getDocId()!=-1 ? md.getUploadTime().toString() : "NA");
		genreText.setText(md!=null && md.getGenre()!=null ? md.getGenre() : "");
		writerText.setText(md!=null && md.getWriter() != null ? md.getWriter() : "");
		descriptionText.setText(md!=null && md.getDesc() != null ? md.getDesc() : "");
		langTable.setSelectedLanguages(md!=null ? md.getLanguage() : "");
		initScriptTypeCombos(md!=null ? md.getScriptType() : null);
		updateDateChooser(enableCreatedFromBtn, createdFrom, md != null ? md.getCreatedFromDate() : null);
		updateDateChooser(enableCreatedToBtn, createdTo, md != null ? md.getCreatedToDate() : null);
	}
	
	
	/**
	 * FIXME does this set the current date if date is null?
	 * @param b
	 * @param c
	 * @param date
	 */
	private void updateDateChooser(Button b, DateChooserCombo c, Date date) {
		c.setValue(date!=null ? date : new Date());
		c.setEnabled(date!=null);
		b.setSelection(date!=null);
	}
	
	private void initScriptTypeCombos(ScriptType st) {
		if(st == null){
			scriptTypeCombo.select(-1);
			scriptTypeCombo2.select(-1);
			scriptTypeCombo2.setEnabled(false);
		} else if(st.equals(ScriptType.HANDWRITTEN)){
			scriptTypeCombo.select(scriptTypeCombo.indexOf(ScriptType.HANDWRITTEN.getStr()));
			scriptTypeCombo2.select(-1);
			scriptTypeCombo2.setEnabled(false);
		} else {
			scriptTypeCombo.select(scriptTypeCombo.indexOf(PRINT_META_SCRIPTTYPE));
			scriptTypeCombo2.select(scriptTypeCombo2.indexOf(st.getStr()));
			scriptTypeCombo2.setEnabled(true);
		}
	}

	public Text getTitleText() {
		return titleText;
	}

	public Text getAuthorText() {
		return authorText;
	}


	public Label getUploadedLabel() {
		return uploadedLabel;
	}

	public Text getGenreText() {
		return genreText;
	}

	public Text getWriterText() {
		return writerText;
	}
	
	public Text getDescriptionText() {
		return descriptionText;
	}
	
	public Combo getScriptTypeCombo() { 
		return scriptTypeCombo;
	}
	
	public Combo getScriptTypeCombo2() { 
		return scriptTypeCombo2;
	}
	
	public LanguageSelectionTable getLangTable(){
		return langTable;
	}
	
	public Date getCreatedFromDate() {
		return createdFrom.getValue();
	}
		
	public boolean isCreatedFromEnabled(){
		return enableCreatedFromBtn.getSelection();
	}
	
	public Date getCreatedToDate() {
		return createdTo.getValue();
	}
	
	public boolean isCreatedToEnabled(){
		return enableCreatedToBtn.getSelection();
	}
}
