package eu.transkribus.swt_gui.doc_overview;

import java.util.Date;
import java.util.Observable;
import java.util.Observer;

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

import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.auth.TrpRole;
import eu.transkribus.core.model.beans.enums.ScriptType;
import eu.transkribus.core.util.EnumUtils;
import eu.transkribus.core.util.FinereaderUtils;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SCSimpleDateTimeWidget;
import eu.transkribus.swt_gui.edit_decl_manager.EditDeclManagerDialog;
import eu.transkribus.swt_gui.edit_decl_manager.EditDeclViewerDialog;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.Storage.DocLoadEvent;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.tools.LanguageSelectionTable;

public class DocMetadataEditor extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(DocMetadataEditor.class);
	
	private Text titleText;
	private Text authorText;
	private Label uploadedLabel;
	private Text genreText;
	private Text writerText;
	private Button saveBtn;
	private Text descriptionText;
//	private Combo langCombo;
	private Combo scriptTypeCombo, scriptTypeCombo2;
	private LanguageSelectionTable langTable;
//	private TrpDateTime createdFrom, createdTo;
	private SCSimpleDateTimeWidget createdFrom, createdTo;
	private Button enableCreatedFromBtn, enableCreatedToBtn;
	private Button openEditDeclManagerBtn;
	EditDeclManagerDialog edm;
	
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
		langTable.setAvailableLanguages(FinereaderUtils.FINEREADER_LANGUAGES);
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
//		createdFrom = new TrpDateTime (dateComposite, SWT.DROP_DOWN);
		createdFrom = new SCSimpleDateTimeWidget(dateComposite, SWT.NONE); 
		createdFrom.setEnabled(false);

		enableCreatedToBtn = new Button(dateComposite, SWT.CHECK);
		enableCreatedToBtn.setSelection(false);
		enableCreatedToBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				createdTo.setEnabled(enableCreatedToBtn.getSelection());
			}
		});
		enableCreatedToBtn.setText("To:");
//		createdTo = new TrpDateTime (dateComposite, SWT.DROP_DOWN);
		createdTo = new SCSimpleDateTimeWidget(dateComposite, SWT.NONE);
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
	}
	
	void addListener() {
		saveBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				saveMd();
			}
		});
		
		openEditDeclManagerBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				openEditDeclManagerWidget();
			}
		});
		
		Storage.getInstance().addObserver(new Observer() {
			@Override public void update(Observable o, Object arg) {
				if (arg instanceof DocLoadEvent) {
					DocLoadEvent dle = (DocLoadEvent) arg;
					setMetadataToGui(dle.doc.getMd());
				}
			}
		});
	}
	
	private void saveMd() {
		TrpMainWidget mw = TrpMainWidget.getInstance();
		if (mw == null)
			return;
		
		updateMetadataFromGui(Storage.getInstance().getDoc().getMd());
		mw.saveDocMetadata();
	}
	
	public void updateMetadataFromGui(TrpDocMetadata md) {		
		logger.debug("updating metadata: "+md);
		
		md.setTitle(titleText.getText());
		md.setAuthor(authorText.getText());
		md.setGenre(genreText.getText());
		md.setWriter(writerText.getText());
		md.setDesc(descriptionText.getText());
		md.setLanguage(langTable.getSelectedLanguagesString());
		
		final String stStr = getScriptTypeCombo().getText();
		ScriptType st;
		if(PRINT_META_SCRIPTTYPE.equals(stStr)){
			st = EnumUtils.fromString(ScriptType.class, getScriptTypeCombo2().getText());
		} else {
			st = EnumUtils.fromString(ScriptType.class, getScriptTypeCombo().getText());
		}
		logger.debug("script type1: "+st);
		md.setScriptType(st);
		if(isCreatedFromEnabled()){
			logger.debug("from date: "+getCreatedFrom().getDate());
//			md.setCreatedFromDate(dmEd.getCreatedFrom().getTime());
			md.setCreatedFromDate(getCreatedFrom().getDate());
		} else {
			md.setCreatedFromDate(null);
		}
		if(isCreatedToEnabled()){
			logger.debug("to date: "+getCreatedTo().getDate());
//			md.setCreatedToDate(dmEd.getCreatedTo().getTime());
			md.setCreatedToDate(getCreatedTo().getDate());
		} else {
			md.setCreatedToDate(null);
		}
	}
	
	public void setMetadataToGui(TrpDocMetadata md) {
		if (md == null) {
			titleText.setText("");
			authorText.setText("");
			uploadedLabel.setText("");
			genreText.setText("");
			writerText.setText("");
			descriptionText.setText("");
//			langCombo.setText("");
			langTable.setSelectedLanguages("");
			scriptTypeCombo.select(-1);
			scriptTypeCombo2.select(-1);
			createdFrom.setEnabled(false);
			createdTo.setEnabled(false);
		}
		else {
			titleText.setText(md.getTitle()!=null ? md.getTitle() : "");
			authorText.setText(md.getAuthor()!=null ? md.getAuthor() : "");
			uploadedLabel.setText(md.getUploadTime()!=null&&md.getDocId()!=-1 ? md.getUploadTime().toString() : "NA");
			genreText.setText(md.getGenre()!=null ? md.getGenre() : "");
			writerText.setText(md.getWriter() != null ? md.getWriter() : "");
			descriptionText.setText(md.getDesc() != null ? md.getDesc() : "");
//			langCombo.setText(md.getLanguage() != null ? md.getLanguage() : "");
			langTable.setSelectedLanguages(md.getLanguage());
//			scriptTypeCombo.select(EnumUtils.indexOf(md.getScriptType()));
			initScriptTypeCombos(md.getScriptType());
			
			if(md.getCreatedFromTimestamp() != null){
//				createdFrom.setDate(md.getCreatedFromTimestamp());
				createdFrom.setDate(new Date(md.getCreatedFromTimestamp()));
				createdFrom.setEnabled(true);
				enableCreatedFromBtn.setSelection(true);
			} else {
				createdFrom.setEnabled(false);
				enableCreatedFromBtn.setSelection(false);
			}
			if(md.getCreatedToTimestamp() != null){
//				createdTo.setDate(md.getCreatedToTimestamp());
				createdTo.setDate(new Date(md.getCreatedToTimestamp()));
				createdTo.setEnabled(true);
				enableCreatedToBtn.setSelection(true);
			} else {
				createdTo.setEnabled(false);
				enableCreatedToBtn.setSelection(false);
			}
		}
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
	
//	public Button getApplyBtn() {
//		return saveBtn;
//	}

	public Text getDescriptionText() {
		return descriptionText;
	}
	
//	public Combo getLangCombo(){
//		return langCombo;
//	}
	
	public Combo getScriptTypeCombo() { 
		return scriptTypeCombo;
	}
	
	public Combo getScriptTypeCombo2() { 
		return scriptTypeCombo2;
	}
	
	public LanguageSelectionTable getLangTable(){
		return langTable;
	}
	
//	public TrpDateTime getCreatedFrom(){
//		return createdFrom;
//	}
	
	public SCSimpleDateTimeWidget getCreatedFrom(){
		return createdFrom;
	}
	
	public boolean isCreatedFromEnabled(){
		return enableCreatedFromBtn.getSelection();
	}
	
//	public TrpDateTime getCreatedTo(){
//		return createdTo;
//	}
	
	public SCSimpleDateTimeWidget getCreatedTo(){
		return createdTo;
	}
	
	public boolean isCreatedToEnabled(){
		return enableCreatedToBtn.getSelection();
	}
	
	public void openEditDeclManagerWidget() {
		Storage store = Storage.getInstance();
		
		if(!store.isDocLoaded()) {
			return;
		}
		if (!isEditDeclManagerOpen()) {
			if(store.getRoleOfUserInCurrentCollection().getValue() < TrpRole.Editor.getValue()){
				edm = new EditDeclViewerDialog(getShell(), SWT.NONE);
			} else {
				edm = new EditDeclManagerDialog(getShell(), SWT.NONE);
			}
			edm.open();
		} else {
			edm.getShell().setVisible(true);
		}
	}
	
	public boolean isEditDeclManagerOpen() {
		return edm != null && edm.getShell() != null && !edm.getShell().isDisposed();
	}
}
