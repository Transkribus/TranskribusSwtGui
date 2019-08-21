package eu.transkribus.swt_gui.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpP2PaLAModel;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.util.CurrentTranscriptOrCurrentDocPagesSelector;

public class P2PaLAConfDialog extends Dialog {
	public static class P2PaLARecogConf {
		public boolean currentTranscript=true;
		public String pagesStr=null;
		public TrpP2PaLAModel model;
	}
	
	private static final Logger logger = LoggerFactory.getLogger(P2PaLAConfDialog.class);
//	MyTableViewer modelsTable;
	CurrentTranscriptOrCurrentDocPagesSelector pagesSelector;
//	Combo modelCombo;
	ComboViewer modelComboViewer;
	AutoCompleteField modelsAutocomplete;
	Label selectedModelLbl;
	
	Button modelDetailsBtn;
	
	P2PaLARecogConf conf = null;
	
	public static String NAME_COL = "Name";
	public static String DESC_COL = "Description";
	public static String BASELINES_COL = "Baselines";
	public static String STRUCT_TYPES_COL = "Structure Types";
	
	public static String TRAIN_SET_SIZE_COL = "N-Train";
	public static String VAL_SET_SIZE_COL = "N-Validation";
	public static String TEST_SET_SIZE_COL = "N-Test";
	
	public static final ColumnConfig[] COLS = new ColumnConfig[] {
			new ColumnConfig(NAME_COL, 120, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(DESC_COL, 250, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(BASELINES_COL, 75, false, DefaultTableColumnViewerSorter.ASC, "Does this model detect Baselines?"),
			new ColumnConfig(STRUCT_TYPES_COL, 750, false, DefaultTableColumnViewerSorter.ASC, "The region structure types this model detects"),
			new ColumnConfig(TRAIN_SET_SIZE_COL, 75, false, DefaultTableColumnViewerSorter.ASC, "The size of the training set"),
			new ColumnConfig(VAL_SET_SIZE_COL, 75, false, DefaultTableColumnViewerSorter.ASC, "The size of the validation set (which is used after every epoch during training to evaluate the model)"),
			new ColumnConfig(TEST_SET_SIZE_COL, 75, false, DefaultTableColumnViewerSorter.ASC, "The size of the test set (which is used once after training to evaluate the model)"),
		};	
	
	List<TrpP2PaLAModel> models;

	public P2PaLAConfDialog(Shell parentShell, List<TrpP2PaLAModel> models) {
		super(parentShell);
		this.models = models;
	}
	
	@Override
	protected Point getInitialSize() {
//		return new Point(600, 250);
		return new Point(500, getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
	}
	
	@Override
	protected boolean isResizable() {
	    return true;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
		
		Button helpBtn = createButton(parent, IDialogConstants.HELP_ID, "Help", false);
		helpBtn.setImage(Images.HELP);
		SWTUtil.onSelectionEvent(helpBtn, e -> {
			org.eclipse.swt.program.Program.launch("https://transkribus.eu/wiki/index.php/P2PaLA");
		});
		
		Button modelDetailsBtn = createButton(parent, IDialogConstants.HELP_ID, "Model info", false);
		modelDetailsBtn.setImage(Images.INFO);
		SWTUtil.onSelectionEvent(modelDetailsBtn, e -> {
			P2PaLAModelDetailsDialog d = new P2PaLAModelDetailsDialog(getShell(), models);
			d.open();
		});		
		
	    Button runBtn = createButton(parent, IDialogConstants.OK_ID, "Run", false);
	    runBtn.setImage(Images.ARROW_RIGHT);
	    
	    onSelectedModelChanged();
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("P2PaLA structure analysis tool");
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		cont.setLayout(new GridLayout(2, false));
		
//		Link infoText = new Link(cont, 0);
//		infoText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 2, 1));
//		String githubLink="https://github.com/lquirosd/P2PaLA";
//		infoText.setText("This tool detects regions including its structure types and (optionally) baselines, see <a href=\""+githubLink+"\">"+githubLink+"</a>");
//		SWTUtil.onSelectionEvent(infoText, e -> {
//			try {
//				org.eclipse.swt.program.Program.launch(e.text);
//			} catch (Exception ex) {
//				logger.error(ex.getMessage(), ex);
//			}
//		});
//		Fonts.setBoldFont(infoText);
		
		pagesSelector = new CurrentTranscriptOrCurrentDocPagesSelector(cont, SWT.NONE, true,true);
		pagesSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		Label recogModelLabel = new Label(cont, 0);
		recogModelLabel.setText("Select a model for recognition: ");
		Fonts.setBoldFont(recogModelLabel);
		
//		modelCombo = new Combo(cont, SWT.READ_ONLY | SWT.DROP_DOWN);
//		modelCombo = new Combo(cont, SWT.DROP_DOWN);
//		modelCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		modelCombo.setToolTipText("The model used for the P2PaLA Layout Analysis");

//		Text textField = new Text(cont, SWT.BORDER);
//		textField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		modelsAutocomplete = new AutoCompleteField(textField, new TextContentAdapter(), new String[] {});
		
		modelComboViewer = new ComboViewer(cont, SWT.DROP_DOWN);
		modelComboViewer.getCombo().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		modelComboViewer.getCombo().setToolTipText("The model used for the P2PaLA Layout Analysis");
		modelComboViewer.setContentProvider(ArrayContentProvider.getInstance());
		modelComboViewer.setLabelProvider(new LabelProvider() {
	        @Override
	        public String getText(Object element) {
	            if (element instanceof TrpP2PaLAModel) {
	            	TrpP2PaLAModel model = (TrpP2PaLAModel) element;
	            	return model.getName();
	            }
	            return "<i am error>";
	        }
	    });
		modelsAutocomplete = new AutoCompleteField(modelComboViewer.getCombo(), new ComboContentAdapter(), new String[] {});
		
		selectedModelLbl = new Label(cont, SWT.NONE);
		selectedModelLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		Fonts.setItalicFont(selectedModelLbl);
		
		modelComboViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				onSelectedModelChanged();
				
			}
		});
		
		SWTUtil.onSelectionEvent(modelComboViewer.getCombo(), e -> {
			onSelectedModelChanged();
		});
		modelComboViewer.getCombo().addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				onSelectedModelChanged();
			}
		});
		modelComboViewer.getCombo().addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				logger.trace("key released, text = "+modelComboViewer.getCombo().getText());
				onSelectedModelChanged();
			}
			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		
//		modelDetailsBtn = new Button(cont, SWT.PUSH);
//		modelDetailsBtn.setText("Show model details...");
//		modelDetailsBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		SWTUtil.onSelectionEvent(modelDetailsBtn, e -> {
//			P2PaLAModelDetailsDialog d = new P2PaLAModelDetailsDialog(getShell(), models);
//			d.open();
//		});
//		new Label(cont, 0); // placeholder
		
//		Label modelsLabel = new Label(cont, 0);
//		modelsLabel.setText("Available models:");
//		modelsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 2, 1));
//		Fonts.setBoldFont(modelsLabel);
		
//		modelsTable = new MyTableViewer(cont, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
//		modelsTable.getTable().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 2, 1));
//		modelsTable.getTable().setHeaderVisible(true);
//		modelsTable.getTable().setLinesVisible(true);
//		modelsTable.addColumns(COLS);
//		// ignore case when sorting columns:
////		for (int i=0; i<tv.getNColumns(); ++i) {
////			tv.getSorter(i).setIgnoreCase(true);
////		}
//		modelsTable.setContentProvider(ArrayContentProvider.getInstance());
//		modelsTable.setLabelProvider(new TableLabelProvider() {
//			@Override
//			public String getColumnText(Object element, int index) {
//				if (!(element instanceof TrpP2PaLAModel)) {
//					return "i am serious error";
//				}
//				TrpP2PaLAModel m = (TrpP2PaLAModel) element;
//				String cn = COLS[index].name;
//				
//				if (cn.equals(NAME_COL)) {
//					return m.getName();
//				}
//				else if (cn.equals(DESC_COL)) {
//					return m.getDescription();
//				}
//				else if (cn.equals(BASELINES_COL)) {
//					return StringUtils.contains(m.getOut_mode(), "L") ? "Yes" : "No";
//				}
//				else if (cn.equals(STRUCT_TYPES_COL)) {
////					return StringUtils.contains(m.getOut_mode(), "R") ? m.getStruct_types() : "";
//					return m.getStruct_types();
//				}
//				else if (cn.equals(TRAIN_SET_SIZE_COL)) {
//					return m.getTrain_set_size()!=null ? ""+m.getTrain_set_size() : "NA";
//				}
//				else if (cn.equals(VAL_SET_SIZE_COL)) {
//					return m.getVal_set_size()!=null ? ""+m.getVal_set_size() : "NA";
//				}
//				else if (cn.equals(TEST_SET_SIZE_COL)) {
//					return m.getTest_set_size()!=null ? ""+m.getTest_set_size() : "NA";
//				}				
//				
//				return "i am error";
//			}
//		});
		
		setModels(models);
		onSelectedModelChanged();
		
		return cont;
	}
	
	private void onSelectedModelChanged() {
		TrpP2PaLAModel m = getSelectedP2PaLAModel();
		
		if (m == null) { // try to select model from text in combo
			m = models.stream().filter(m1 -> m1.getName().equals(modelComboViewer.getCombo().getText())).findFirst().orElse(null);
			if (m!=null) {
				modelComboViewer.setSelection(new StructuredSelection(m));
				m = getSelectedP2PaLAModel();
			}
		}
		
		if (selectedModelLbl!=null) {
			selectedModelLbl.setText(m==null ? "Selected: <none>" : "Selected: "+m.getName());
		}
		
		Button b = getButton(IDialogConstants.OK_ID);
		if (b!=null) {
			b.setEnabled(m!=null);	
		}
	}
	
//	public void setModels(List<TrpP2PaLAModel> models) {
//		logger.debug("setting input models, N = "+CoreUtils.size(models));
//		
//		if (models != null && !models.isEmpty()) { // null check needed???
//			modelsTable.setInput(models);
//			
//			List<String> items = new ArrayList<>();
//			int i=0;
//			for (TrpP2PaLAModel m : models) {
//				items.add(m.getName());
//				modelCombo.setData(""+i, m);
//				++i;
//			}
//			modelCombo.setItems(items.toArray(new String[0]));
//			modelCombo.select(0);			
//		}
//		else {
//			modelsTable.setInput(new ArrayList<>());
//			modelCombo.setItems(new String[] {});
//		}
//	}
	
	private void setModels(List<TrpP2PaLAModel> models) {
		models = new ArrayList<>(models);
		Collections.sort(models, new Comparator<TrpP2PaLAModel>() {
			@Override
			public int compare(TrpP2PaLAModel o1, TrpP2PaLAModel o2) {
				String n1 = o1.getName()==null ? "" : o1.getName();
				String n2 = o2.getName()==null ? "" : o2.getName();
				return n1.compareTo(n2);
			}
		});
		
		logger.debug("setting input models, N = "+CoreUtils.size(models));
		
		if (models != null && !models.isEmpty()) { // null check needed???
//			modelsTable.setInput(models);
			modelComboViewer.setInput(models);
			
			List<String> items = new ArrayList<>();
			for (TrpP2PaLAModel m : models) {
				items.add(m.getName());
			}
			modelsAutocomplete.setProposals(items.toArray(new String[0]));
			modelComboViewer.getCombo().select(0);
		}
		else {
//			modelsTable.setInput(new ArrayList<>());
			modelComboViewer.setInput(new ArrayList<>());
		}
	}	
	
//	public TrpP2PaLAModel getSelectedP2PaLAModel() {
//		int i = modelCombo.getSelectionIndex();
//		if (i>=0 && i<modelCombo.getItemCount()) {
//			try {
//				return (TrpP2PaLAModel) modelCombo.getData(""+i);
//			} catch (Exception e) {
//				logger.error("Error casting selected P2PaLAModel: "+e.getMessage(), e);
//			}
//		}
//		return null;
//	}	
	
	public TrpP2PaLAModel getSelectedP2PaLAModel() {
		return (TrpP2PaLAModel) modelComboViewer.getStructuredSelection().getFirstElement();
//		IStructuredSelection sel = modelComboViewer.getStructuredSelection();
//		return sel.isEmpty() ? null : (TrpP2PaLAModel) sel.getFirstElement();
	}	
	
	public void setVisible() {
		if (super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}
	
	private void storeConf() {
		TrpP2PaLAModel model = getSelectedP2PaLAModel();
		if (model != null) {
			conf = new P2PaLARecogConf();
			conf.currentTranscript = pagesSelector.isCurrentTranscript();
			conf.pagesStr = pagesSelector.getPagesStr();
			conf.model = model;
		}
		else {
			conf = null;
		}
	}
	
	public P2PaLARecogConf getConf() {
		return conf;
	}
	
	@Override
	protected void okPressed() {
		storeConf();
		super.okPressed();
	}

}
