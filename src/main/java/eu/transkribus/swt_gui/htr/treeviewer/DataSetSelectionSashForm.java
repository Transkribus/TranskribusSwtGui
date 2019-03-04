package eu.transkribus.swt_gui.htr.treeviewer;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.htr.DataSetTableWidget;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSet;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSetElement;

public class DataSetSelectionSashForm extends SashForm {
	
//	private static final RGB BLUE_RGB = new RGB(0, 0, 140);
//	private static final RGB LIGHT_BLUE_RGB = new RGB(0, 140, 255);
//	private static final RGB GREEN_RGB = new RGB(0, 140, 0);
//	private static final RGB LIGHT_GREEN_RGB = new RGB(0, 255, 0);
//	private static final RGB CYAN_RGB = new RGB(85, 240, 240);
	
	private static final RGB BLUE_RGB = new RGB(0, 83, 138);
	private static final RGB LIGHT_BLUE_RGB = new RGB(115, 161, 191);
	private static final RGB GREEN_RGB = new RGB(0, 105, 66);
	private static final RGB LIGHT_GREEN_RGB = new RGB(69, 145, 117);
	private static final RGB CYAN_RGB = new RGB(0, 150, 240);
	
	static final Color BLUE = Colors.createColor(BLUE_RGB);
	static final Color LIGHT_BLUE = Colors.createColor(LIGHT_BLUE_RGB);
	static final Color GREEN = Colors.createColor(GREEN_RGB);
	static final Color LIGHT_GREEN = Colors.createColor(LIGHT_GREEN_RGB);
	static final Color CYAN = Colors.createColor(CYAN_RGB);
	static final Color WHITE = Colors.getSystemColor(SWT.COLOR_WHITE);
	static final Color BLACK = Colors.getSystemColor(SWT.COLOR_BLACK);
	
	TreeViewer docTv, groundTruthTv;
	Button useGtVersionChk, useNewVersionChk;
	Button addToTrainSetBtn, addToTestSetBtn, removeFromTrainSetBtn, removeFromTestSetBtn;
	DataSetTableWidget<IDataSelectionEntry<?, ?>> testSetOverviewTable, trainSetOverviewTable;
	CTabFolder dataTabFolder;
	CTabItem documentsTabItem;
	CTabItem gtTabItem;
	Label previewLbl;
	
	private DataSetSelectionHandler dataHandler;

	//the input to select data from
	private List<TrpDocMetadata> docList;
	private List<TrpHtr> htrList;

	public DataSetSelectionSashForm(Composite parent, int style, final int colId, List<TrpHtr> htrList, List<TrpDocMetadata> docList) {
		super(parent, style);
		this.docList = docList;
		this.htrList = htrList;
		dataHandler = new DataSetSelectionHandler(colId, this);
		
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.setLayout(new GridLayout(1, false));
		
		dataTabFolder = new CTabFolder(this, SWT.BORDER | SWT.FLAT);
		dataTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		documentsTabItem = new CTabItem(dataTabFolder, SWT.NONE);
		documentsTabItem.setText("Documents");
		docTv = createDocumentTreeViewer(dataTabFolder);
		documentsTabItem.setControl(docTv.getControl());

		groundTruthTv = createGroundTruthTreeViewer(dataTabFolder);
		if(!htrList.isEmpty()) {
			gtTabItem = new CTabItem(dataTabFolder, SWT.NONE);
			gtTabItem.setText("HTR Model Data");
			gtTabItem.setControl(groundTruthTv.getControl());
		}
		
		Composite buttonComp = new Composite(this, SWT.NONE);
		buttonComp.setLayout(new GridLayout(1, true));

		previewLbl = new Label(buttonComp, SWT.NONE);
		GridData gd2 = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		gd2.heightHint = 120;
		gd2.widthHint = 100;
		previewLbl.setLayoutData(gd2);

		addToTrainSetBtn = new Button(buttonComp, SWT.PUSH);
		addToTrainSetBtn.setImage(Images.ADD);
		addToTrainSetBtn.setText("Training");
		addToTrainSetBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		addToTestSetBtn = new Button(buttonComp, SWT.PUSH);
		addToTestSetBtn.setImage(Images.ADD);
		addToTestSetBtn.setText("Testing");
		addToTestSetBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		Group trainOverviewCont = new Group(this, SWT.NONE);
		trainOverviewCont.setText("Overview");
		trainOverviewCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		trainOverviewCont.setLayout(new GridLayout(1, false));

		useGtVersionChk = new Button(trainOverviewCont, SWT.CHECK);
		useGtVersionChk.setText("Use Groundtruth versions");
		
		useNewVersionChk = new Button(trainOverviewCont, SWT.CHECK);
		useNewVersionChk.setText("Use initial('New') versions");
		useNewVersionChk.setSelection(true);

		GridData tableGd = new GridData(SWT.FILL, SWT.FILL, true, true);
		GridLayout tableGl = new GridLayout(1, true);

		Group trainSetGrp = new Group(trainOverviewCont, SWT.NONE);
		trainSetGrp.setText("Training Set");
		trainSetGrp.setLayoutData(tableGd);
		trainSetGrp.setLayout(tableGl);

		trainSetOverviewTable = new DataSetTableWidget<IDataSelectionEntry<?, ?>>(trainSetGrp, SWT.BORDER) {};
		trainSetOverviewTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		GridData buttonGd = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		removeFromTrainSetBtn = new Button(trainSetGrp, SWT.PUSH);
		removeFromTrainSetBtn.setLayoutData(buttonGd);
		removeFromTrainSetBtn.setImage(Images.CROSS);
		removeFromTrainSetBtn.setText("Remove selected entries from train set");

		Group testSetGrp = new Group(trainOverviewCont, SWT.NONE);
		testSetGrp.setText("Test Set");
		testSetGrp.setLayoutData(tableGd);
		testSetGrp.setLayout(tableGl);

		testSetOverviewTable = new DataSetTableWidget<IDataSelectionEntry<?, ?>>(testSetGrp, SWT.BORDER) {};
		testSetOverviewTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		removeFromTestSetBtn = new Button(testSetGrp, SWT.PUSH);
		removeFromTestSetBtn.setLayoutData(buttonGd);
		removeFromTestSetBtn.setImage(Images.CROSS);
		removeFromTestSetBtn.setText("Remove selected entries from test set");

		this.setWeights(new int[] { 45, 10, 45 });
		dataTabFolder.setSelection(documentsTabItem);
		
		docTv.getTree().pack();
		buttonComp.pack();
		trainOverviewCont.pack();
		trainSetGrp.pack();
		testSetGrp.pack();
		
		new DataSetSelectionSashFormListener(this, dataHandler);
	}

	private TreeViewer createDocumentTreeViewer(Composite parent) {
		TreeViewer tv = new TreeViewer(parent, SWT.BORDER | SWT.MULTI);
		tv.setContentProvider(dataHandler.getDocContentProvider());
		tv.setLabelProvider(dataHandler.getDocLabelProvider());
		tv.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tv.setInput(this.docList);
		return tv;
	}
	
	private TreeViewer createGroundTruthTreeViewer(Composite parent) {
		TreeViewer tv = new TreeViewer(parent, SWT.BORDER | SWT.MULTI);
		tv.setContentProvider(dataHandler.getHtrGtContentProvider());
		tv.setLabelProvider(dataHandler.getHtrGtLabelProvider());
		tv.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tv.setInput(this.htrList);
		return tv;
	}
	
	/**
	 * Update ground truth treeviewer row colors according to selected data set.
	 * 
	 * For now this will expect train/validation data to be selected to the respective sets!
	 * 
	 * @param trainGtMap
	 * @param testGtMap
	 */
	void updateGtTvColors(Map<HtrGtDataSet, List<HtrGtDataSetElement>> trainGtMap, Map<HtrGtDataSet, List<HtrGtDataSetElement>> testGtMap) {
		groundTruthTv.refresh(true);
	}
	
	void updateDocTvColors(Map<TrpDocMetadata, List<TrpPage>> trainDocMap, Map<TrpDocMetadata, List<TrpPage>> testDocMap) {
		docTv.refresh(true);
	}
	
	public Map<TrpDocMetadata, List<TrpPage>> getTrainDocMap() {
		return dataHandler.getTrainDocMap();
	}
	
	public Map<TrpDocMetadata, List<TrpPage>> getTestDocMap() {
		return dataHandler.getTestDocMap();
	}
	
	public Map<HtrGtDataSet, List<HtrGtDataSetElement>> getTrainGtMap() {
		return dataHandler.getTrainGtMap();
	}
	
	public Map<HtrGtDataSet, List<HtrGtDataSetElement>> getTestGtMap() {
		return dataHandler.getTestGtMap();
	}
	
	public DataSetMetadata getTrainSetMetadata() {
		return dataHandler.getTrainSetMetadata();
	}
	
	public DataSetMetadata getTestSetMetadata() {
		return dataHandler.getTestSetMetadata();
	}
	
	public Button getUseGtVersionChk() {
		return useGtVersionChk;
	}
	
	public Button getUseNewVersionChk() {
		return useNewVersionChk;
	}

	DataSetSelectionHandler getDataHandler() {
		return dataHandler;
	}
}
