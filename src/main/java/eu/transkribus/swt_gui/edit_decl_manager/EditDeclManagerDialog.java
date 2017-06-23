package eu.transkribus.swt_gui.edit_decl_manager;

import java.io.FileNotFoundException;
import java.util.List;

import javax.ws.rs.ServerErrorException;
import javax.xml.bind.JAXBException;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.EdFeature;
import eu.transkribus.core.model.beans.EdOption;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.doc_overview.DocTableWidgetPagination;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

//public class CollectionManagerWidget extends Composite {
public class EditDeclManagerDialog extends Dialog {
	
	private final static Logger logger = LoggerFactory.getLogger(EditDeclManagerDialog.class);
		
	Shell shlEditorialDeclaration;
	Table featTable;
	MyTableViewer featTv;
	Table optTable;
	MyTableViewer optTv;
	Table editDeclTable;
	MyTableViewer editDeclTv;
	
	Button createFeatBtn, delFeatBtn, edtFeatBtn;
	Button createOptBtn, delOptBtn, edtOptBtn;
	Button addFeatBtn, removeFeatBtn;
	Button saveBtn; 
	Button copyBtn;
	DocTableWidgetPagination docTable;

	EditFeaturesListener efl;

	List<EdFeature> editDecl;
	
	public static final String FEAT_ID_COL = "ID";
	public static final String FEAT_TITLE_COL = "Title";
	public static final String FEAT_DESC_COL = "Description";
	public static final String FEAT_COL_ID_COL = "Collection";
	
	public static final String OPT_ID_COL = "ID";
	public static final String OPT_TEXT_COL = "Text";
	
	public static final String EDT_DECL_ID_COL = "ID";
	public static final String EDT_DECL_TITLE_COL = "Title";
	public static final String EDT_DECL_DESC_COL = "Description";
	public static final String EDT_DECL_OPT_COL = "Selected Option";
	
	static final Storage store = Storage.getInstance();
	
	// This are the columns, sorted in their order of appearence in the table:
	public static final ColumnConfig[] FEAT_COLS = new ColumnConfig[] {
		new ColumnConfig(FEAT_ID_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(FEAT_TITLE_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(FEAT_DESC_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(FEAT_COL_ID_COL, 35, false, DefaultTableColumnViewerSorter.ASC),
	};
	
	public static final ColumnConfig[] OPT_COLS = new ColumnConfig[] {
		new ColumnConfig(OPT_ID_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(OPT_TEXT_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
	};

	public static final ColumnConfig[] EDT_DECL_COLS = new ColumnConfig[] {
		new ColumnConfig(EDT_DECL_ID_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(EDT_DECL_TITLE_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(EDT_DECL_DESC_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(EDT_DECL_OPT_COL, 35, false, DefaultTableColumnViewerSorter.ASC),
	};

	public EditDeclManagerDialog(Shell parent, int style) {
		super(parent, style |= (SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MODELESS | SWT.MAX));
//		this.setSize(800, 800);
		this.setText("Editorial Declaration");
	}
	
	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlEditorialDeclaration.open();
		shlEditorialDeclaration.layout();
		Display display = getParent().getDisplay();
		while (!shlEditorialDeclaration.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return null;
	}
	
	void createContents() {
				
		shlEditorialDeclaration = new Shell(getParent(), getStyle());
		shlEditorialDeclaration.setSize(1200, 900);
		shlEditorialDeclaration.setText("Editorial Declaration");
//		shell.setLayout(new GridLayout(2, false));
//		shell.setLayout(new GridLayout(4, false));
		shlEditorialDeclaration.setLayout(new FillLayout());
		
		//SashForm allows table resize, but no unequal column width
//		Composite composite = new SashForm(shlEditorialDeclaration, SWT.HORIZONTAL);
		SashForm container = new SashForm(shlEditorialDeclaration, 0);
//		Composite container = new Composite(shlEditorialDeclaration, 0);
		container.setLayout(new GridLayout(4, false));

		// left:
		Group grpFeats = new Group(container, SWT.SHADOW_ETCHED_IN);
		grpFeats.setText("Transcription Features");
		grpFeats.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpFeats.setLayout(new GridLayout());

		featTv = new MyTableViewer(grpFeats, SWT.SINGLE | SWT.FULL_SELECTION);
		featTv.setContentProvider(new ArrayContentProvider());
		featTv.setLabelProvider(new FeatureTableLabelProvider(featTv));
		
		featTable = featTv.getTable();
		featTable.setHeaderVisible(true);
		featTable.setLinesVisible(true);
		featTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		featTv.addColumns(FEAT_COLS);
		
		Composite btns = new Composite(grpFeats, 0);
		btns.setLayout(new GridLayout(3, false));
		btns.setLayoutData(new GridData(SWT.TOP, SWT.FILL, true, false, 1, 1));
		
		createFeatBtn = new Button(btns, SWT.PUSH);
		createFeatBtn.setImage(Images.getOrLoad("/icons/add.png"));
		createFeatBtn.setToolTipText("Create a new feature");
		createFeatBtn.pack();
		delFeatBtn = new Button(btns, SWT.PUSH);
		delFeatBtn.setImage(Images.getOrLoad("/icons/delete.png"));
		delFeatBtn.setToolTipText("Delete a feature");
		delFeatBtn.pack();
		edtFeatBtn = new Button(btns, SWT.PUSH);
		edtFeatBtn.setImage(Images.getOrLoad("/icons/pencil.png"));
		edtFeatBtn.setToolTipText("Edit this feature");
		edtFeatBtn.pack();
		
		// middle: 
		Group grpOpts = new Group(container, SWT.SHADOW_ETCHED_IN);
		grpOpts.setText("Transription Options");
		grpOpts.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpOpts.setLayout(new GridLayout());
		
		optTv = new MyTableViewer(grpOpts, SWT.SINGLE | SWT.FULL_SELECTION);
		optTv.setContentProvider(new ArrayContentProvider());
		optTv.setLabelProvider(new OptionTableLabelProvider(optTv));
		
		optTable = optTv.getTable();
		optTable.setHeaderVisible(true);
		optTable.setLinesVisible(true);
		optTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		optTv.addColumns(OPT_COLS);
		
		Composite btns2 = new Composite(grpOpts, 0);
		btns2.setLayout(new GridLayout(3, false));
		btns2.setLayoutData(new GridData(SWT.TOP, SWT.FILL, true, false, 1, 1));
		
		createOptBtn = new Button(btns2, SWT.PUSH);
		createOptBtn.setImage(Images.getOrLoad("/icons/add.png"));
		createOptBtn.setToolTipText("Create a new feature");
		createOptBtn.pack();
		delOptBtn = new Button(btns2, SWT.PUSH);
		delOptBtn.setImage(Images.getOrLoad("/icons/delete.png"));
		delOptBtn.setToolTipText("Delete a feature");
		delOptBtn.pack();
		edtOptBtn = new Button(btns2, SWT.PUSH);
		edtOptBtn.setImage(Images.getOrLoad("/icons/pencil.png"));
		edtOptBtn.setToolTipText("Edit this feature");
		edtOptBtn.pack();
		
		// right:
		
		Composite rightContainer = new Composite(container, 0);
		
		rightContainer.setLayout(new GridLayout(2, false));
//		grpEditDecl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite edtBtns = new Composite(rightContainer, 0);
		edtBtns.setLayout(new RowLayout(SWT.VERTICAL));
		edtBtns.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		addFeatBtn = new Button(edtBtns, SWT.PUSH);
//		addFeatBtn.setText(">");
		addFeatBtn.setImage(Images.getOrLoad("/icons/resultset_next.png"));
		addFeatBtn.setToolTipText("Add feature to selection");
		addFeatBtn.pack();
		removeFeatBtn = new Button(edtBtns, SWT.PUSH);
//		removeFeatBtn.setText("<");
		removeFeatBtn.setImage(Images.getOrLoad("/icons/delete.png"));
		removeFeatBtn.setToolTipText("Remove feature from selection");
		removeFeatBtn.pack();
		
//		saveBtn = new Button(edtBtns, SWT.PUSH);
//		saveBtn.setImage(Images.getOrLoad("/icons/disk.png"));
//		saveBtn.setToolTipText("Save selection");
//		saveBtn.pack();
		
		edtBtns.pack();
		
		Group grpEditDecl = new Group(rightContainer, SWT.SHADOW_ETCHED_IN);
		grpEditDecl.setText("Selected Features");
		grpEditDecl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		grpEditDecl.setLayout(new FillLayout());
		
		SashForm s1 = new SashForm(grpEditDecl, SWT.VERTICAL);
		
		editDeclTv = new MyTableViewer(s1, SWT.SINGLE | SWT.FULL_SELECTION);
		editDeclTv.setContentProvider(new ArrayContentProvider());
		editDeclTv.setLabelProvider(new EditDeclTableLabelProvider(editDeclTv));
		
		editDeclTable = editDeclTv.getTable();
		editDeclTable.setHeaderVisible(true);
		editDeclTable.setLinesVisible(true);
//		editDeclTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		editDeclTv.addColumns(EDT_DECL_COLS);
		
		Composite btns3 = new Composite(s1, 0);
		btns3.setLayout(new GridLayout(1, false));
//		btns3.setLayoutData(new GridData(SWT.TOP, SWT.FILL, true, false, 1, 1));
					
//		Label copyLabel = new Label(btns3, 0);
//		copyLabel.setText("Copy to document:");
		
//		List<TrpDocMetadata> colDocs;
//		if(store.isLoggedIn()){
//			colDocs = store.getRemoteDocList();
//		} else {
//			colDocs = new ArrayList<>(0);
//		}
		
		copyBtn = new Button(btns3, SWT.NONE);
		copyBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		copyBtn.setText("Copy to document:");
		docTable = new DocTableWidgetPagination(btns3, 0, 25);
		docTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		docTable.refreshList(store.getCurrentDocumentCollectionId(), true, false);
		
		s1.setWeights(new int[] { 50, 50 } );
				
		container.setWeights(new int[] { 33, 33, 33 });
		
		addListener();
		updateFeatures();
		updateEditDecl();
		if(!store.isLoggedIn()){
			setEdtButtonsEnabled(false);
			createFeatBtn.setEnabled(false);
		}
	}
	
	public Shell getShell() { return shlEditorialDeclaration; }
	public EditDeclManagerDialog getEditFeaturesDialog() { return this; }
	
	public void updateFeatures() {
		logger.debug("updating feature table");
		try {
			featTv.setInput(store.getAvailFeatures());
		} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException
				| NoConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateEditDecl() {
		logger.debug("updating editorial declaration table");
		try {
			editDecl = store.getEditDeclFeatures();
		} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException
				| NoConnectionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		editDeclTv.setInput(editDecl);
	}
	
	public void updateOptions(){
		logger.debug("updating options table");
		if(getSelectedFeature() != null){
			optTv.setInput(getSelectedFeature().getOptions());
		}
	}
	
	EdFeature getSelectedFeature() {
		IStructuredSelection sel = (IStructuredSelection) featTv.getSelection();
		return (EdFeature) sel.getFirstElement();		
	}
	
	EdOption getSelectedOption() {
		IStructuredSelection sel = (IStructuredSelection) optTv.getSelection();
		return (EdOption) sel.getFirstElement();		
	}
	
	EdFeature getSelectedEditDeclFeature() {
		IStructuredSelection sel = (IStructuredSelection) editDeclTv.getSelection();
		return (EdFeature) sel.getFirstElement();		
	}
	
	private void setEdtButtonsEnabled(boolean enabled){
		edtFeatBtn.setEnabled(enabled);
		delFeatBtn.setEnabled(enabled);
		edtOptBtn.setEnabled(enabled);
		delOptBtn.setEnabled(enabled);
		createOptBtn.setEnabled(enabled);
	}
	
	void addListener() {
//		efl = new EditFeaturesListener(this);
		
		featTv.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override public void selectionChanged(SelectionChangedEvent event) {
				
				updateOptions();
				
				if(!store.isLoggedIn() || 
						(getSelectedFeature() != null && getSelectedFeature().getColId() == null && !store.getUser().isAdmin())){
					setEdtButtonsEnabled(false);
				} else {
					setEdtButtonsEnabled(true);
				}
			}
		});
		
		final ISelectionChangedListener optTvSelChangedListener = new ISelectionChangedListener() {
			@Override public void selectionChanged(SelectionChangedEvent event) {
				EdFeature feat = getSelectedFeature();
				EdFeature edFeat = getSelectedEditDeclFeature();
				EdOption opt = getSelectedOption();
				if(feat != null && edFeat != null && opt != null && feat.getFeatureId() == edFeat.getFeatureId()){
					for(EdFeature f : editDecl){
						if(f.getFeatureId() == feat.getFeatureId()){
							for(EdOption o : f.getOptions()){
								o.setSelected(o.getOptionId() == opt.getOptionId());
							}
						}
					}
					
					editDeclTv.setInput(editDecl);
					saveEditDecl();
				}
			}
		};
		
		optTv.addSelectionChangedListener(optTvSelChangedListener);
		
		optTv.addDoubleClickListener(new IDoubleClickListener(){

			@Override
			public void doubleClick(DoubleClickEvent event) {
				addSelectedFeatureToSelection();
			}
			
		});

		editDeclTv.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override public void selectionChanged(SelectionChangedEvent event) {
				optTv.removeSelectionChangedListener(optTvSelChangedListener);
				EdFeature f = getSelectedEditDeclFeature();
				if(f != null){
					setSelectedFeature(f.getFeatureId());
					
					List<EdOption> opts = f.getOptions();
					EdOption selOpt = null;
					for(EdOption o : opts) {
						if(o.isSelected()) selOpt = o;
					}
					setSelectedOption(selOpt.getOptionId());
				}
				optTv.addSelectionChangedListener(optTvSelChangedListener);
			}
		});
		
		edtFeatBtn.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(getSelectedFeature() != null){
					EditFeatureDialog fd = new EditFeatureDialog(getSelectedFeature(), getEditFeaturesDialog());
					fd.open();
				}
			}
		});
		
		createFeatBtn.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				AddFeatureDialog fd = new AddFeatureDialog(getEditFeaturesDialog());
				fd.open();
			}
		});
		
		delFeatBtn.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(getSelectedFeature() != null){
					try {
						Storage.getInstance().deleteEdFeature(getSelectedFeature());
						updateFeatures();
						updateEditDecl();
					} catch (SessionExpiredException | ServerErrorException
							| IllegalArgumentException | NoConnectionException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		
		edtOptBtn.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(getSelectedFeature() != null){
					EditOptionDialog fd = new EditOptionDialog(getSelectedOption(), getEditFeaturesDialog());
					fd.open();
//					updateFeatures();
				}
			}
		});
		
		createOptBtn.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				AddOptionDialog fd = new AddOptionDialog(getSelectedFeature().getFeatureId(), 
						getEditFeaturesDialog());
				fd.open();
//				updateFeatures();
			}
		});
		
		delOptBtn.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(getSelectedFeature() != null){
					try {
						EdFeature feat = getSelectedFeature();
						Storage.getInstance().deleteEdOption(getSelectedOption());
						updateFeatures();
				        updateOptions();
				        updateEditDecl();
				        setSelectedFeature(feat.getFeatureId());
					} catch (SessionExpiredException | ServerErrorException
							| IllegalArgumentException | NoConnectionException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		
		addFeatBtn.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				addSelectedFeatureToSelection();
			}
		});	
		
		removeFeatBtn.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.debug("Removing feature...");				
				if(getSelectedEditDeclFeature() != null){
					EdFeature feat = getSelectedEditDeclFeature();
					editDecl.remove(feat);
					editDeclTv.setInput(editDecl);
					saveEditDecl();
				}
			}
		});
		
//		saveBtn.addSelectionListener(new SelectionAdapter(){
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				logger.debug("Saving selected features");
//				try {
//					store.saveEditDecl(editDecl);
//				} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException | NoConnectionException | FileNotFoundException | JAXBException ex) {
//					MessageBox mb = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
//					mb.setText("Error");
//					mb.setMessage("Could not save editorial declaration!\n" + ex.getMessage());
//					mb.open();
//				}
//				
//			}
//		});
		
		copyBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TrpDocMetadata md = docTable.getFirstSelected();
				if (md == null || md.getDocId() == store.getDocId())
					return;
				
				logger.debug("Apply editdecl to doc: " + md.getDocId());
				try {
					store.saveEditDecl(md.getDocId(), editDecl);
				} catch (Exception e1) {
					TrpMainWidget.getInstance().onError("Error saving Editorial Declaration", "Error saving Editorial Declaration", e1, true, true);
				}				
			}
		});
	}
	
	private void saveEditDecl(){
		logger.debug("Saving selected features");
		try {
			store.saveEditDecl(editDecl);
		} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException | NoConnectionException | FileNotFoundException | JAXBException ex) {
			MessageBox mb = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			mb.setText("Error");
			mb.setMessage("Could not save editorial declaration!\n" + ex.getMessage());
			mb.open();
		}
	}
	
	private void addSelectedFeatureToSelection() {
		if(getSelectedFeature() != null && getSelectedOption() != null){
			EdFeature feat = getSelectedFeature();
			
			boolean isContained = false;
			for(EdFeature f : editDecl){
				isContained |= f.getFeatureId() == feat.getFeatureId();
			}
			if(isContained){
				MessageBox mb = new MessageBox(getParent(), SWT.ICON_INFORMATION | SWT.OK);
				mb.setText("Error");
				mb.setMessage("This feature is already selected.");
				mb.open();
				return;
			}
			
			EdOption opt = getSelectedOption();
			for(EdOption o : feat.getOptions()){
				o.setSelected(o.getOptionId() == opt.getOptionId());
			}
			editDecl.add(feat);
			editDeclTv.setInput(editDecl);
			
			saveEditDecl();
		}
	}

	public void setSelectedFeature(int featureId) {
		List<EdFeature> feats = (List<EdFeature>)featTv.getInput();
		
		EdFeature feat = null;
		for(int i = 0; i < feats.size(); i++){
			if(feats.get(i).getFeatureId() == featureId){
				feat = (EdFeature)featTv.getElementAt(i);
				break;
			}
		}
		setSelectedFeature(feat);		
	}
	
	public void setSelectedFeature(EdFeature feat) {
		try {
			featTv.setSelection(new StructuredSelection(feat), true);
		} catch (Exception e) {
			//do nothing. may happen on collection-specific features and duplicated documents
		}
	}
	
	public void setSelectedOption(int optionId) {
		List<EdOption> opts = (List<EdOption>)optTv.getInput();
		
		EdOption opt = null;
		for(int i = 0; i < opts.size(); i++){
			if(opts.get(i).getOptionId() == optionId){
				opt = (EdOption)optTv.getElementAt(i);
				break;
			}
		}
		setSelectedOption(opt);		
	}
	
	public void setSelectedOption(EdOption opt){
		try{ 
			optTv.setSelection(new StructuredSelection(opt), true);
		} catch (Exception e) {
			//do nothing. may happen on collection-specific features and duplicated documents
		}
	}
}
