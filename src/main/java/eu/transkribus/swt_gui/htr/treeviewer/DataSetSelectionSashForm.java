package eu.transkribus.swt_gui.htr.treeviewer;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.ImgLoader;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionContentProvider;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionLabelProvider;
import eu.transkribus.swt_gui.htr.DataSetMetadata;
import eu.transkribus.swt_gui.htr.DataSetTableWidget;
import eu.transkribus.swt_gui.htr.HtrFilterWidget;
import eu.transkribus.swt_gui.htr.treeviewer.DataSetSelectionController.DataSetSelection;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSet;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSetElement;

/**
 * Sashform UI element for selecting datasets from document and ground truth data.
 *
 */
public class DataSetSelectionSashForm extends SashForm {
	private static final Logger logger = LoggerFactory.getLogger(DataSetSelectionSashForm.class);
	
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
//	private static final RGB VERY_LIGHT_BLUE_RGB = new RGB(191, 212, 225);
//	private static final RGB VERY_LIGHT_GREEN_RGB = new RGB(153, 195, 179);
	
	static final Color BLUE = Colors.createColor(BLUE_RGB);
	static final Color LIGHT_BLUE = Colors.createColor(LIGHT_BLUE_RGB);
//	static final Color VERY_LIGHT_BLUE = Colors.createColor(VERY_LIGHT_BLUE_RGB);
	static final Color GREEN = Colors.createColor(GREEN_RGB);
	static final Color LIGHT_GREEN = Colors.createColor(LIGHT_GREEN_RGB);
//	static final Color VERY_LIGHT_GREEN = Colors.createColor(VERY_LIGHT_GREEN_RGB);
	static final Color CYAN = Colors.createColor(CYAN_RGB);
	static final Color WHITE = Colors.getSystemColor(SWT.COLOR_WHITE);
	static final Color BLACK = Colors.getSystemColor(SWT.COLOR_BLACK);
	
	//show pages with no transcribed lines in gray
	static final Color GRAY = Colors.getSystemColor(SWT.COLOR_GRAY);
	
	Composite dataTabComp;
	TreeViewer docTv, groundTruthTv;
	
	Button useGtVersionChk, useNewVersionChk;
	Button addToTrainSetBtn, addToValSetBtn, removeFromTrainSetBtn, removeFromValSetBtn;
	Label infoLbl;
	DataSetTableWidget<IDataSelectionEntry<?, ?>> valSetOverviewTable, trainSetOverviewTable;
	CTabFolder dataTabFolder;
	CTabItem documentsTabItem;
	CTabItem gtTabItem;
	private Label previewLbl;
	private URL currentThumbUrl = null;
	
	private DataSetSelectionController dataSetSelectionController;

	//the input to select data from
	private List<TrpDocMetadata> docList;
	private List<TrpHtr> htrList;
	private final int colId;

	public DataSetSelectionSashForm(Composite parent, int style, final int colId, List<TrpHtr> htrList, List<TrpDocMetadata> docList) {
		super(parent, style);
		this.docList = docList;
		this.htrList = htrList;
		this.colId = colId;
		dataSetSelectionController = new DataSetSelectionController(colId, this);
		
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.setLayout(new GridLayout(1, false));
		
		dataTabFolder = new CTabFolder(this, SWT.BORDER | SWT.FLAT);
		dataTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		documentsTabItem = new CTabItem(dataTabFolder, SWT.NONE);
		documentsTabItem.setText("Documents");
		docTv = createDocumentTreeViewer(dataTabFolder);
		documentsTabItem.setControl(docTv.getControl());

		dataTabComp = new Composite(dataTabFolder, SWT.NONE);
		GridLayout dataTabCompLayout = new GridLayout(1, true);
		//remove margins to make it consistents with documentsTab
		dataTabCompLayout.marginHeight = dataTabCompLayout.marginWidth = 0;
		dataTabComp.setLayout(dataTabCompLayout);
		dataTabComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		groundTruthTv = createGroundTruthTreeViewer(dataTabComp);
		if(!htrList.isEmpty()) {
			setGroundTruthSelectionEnabled(true);
		}
		HtrFilterWidget filterWidget = new HtrFilterWidget(dataTabComp, groundTruthTv, SWT.NONE);
		
		Composite buttonComp = new Composite(this, SWT.NONE);
		buttonComp.setLayout(new GridLayout(1, true));

		previewLbl = new Label(buttonComp, SWT.NONE);
		GridData previewLblGd = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		previewLblGd.heightHint = 120;
		previewLblGd.widthHint = 100;
		previewLbl.setLayoutData(previewLblGd);
		
		infoLbl = new Label(buttonComp, SWT.WRAP);
		GridData infoLblGd = new GridData(SWT.FILL, SWT.BOTTOM, true, true);
		infoLbl.setLayoutData(infoLblGd);

		addToTrainSetBtn = new Button(buttonComp, SWT.PUSH);
		addToTrainSetBtn.setImage(Images.ADD);
		addToTrainSetBtn.setText("Training");
		addToTrainSetBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		addToValSetBtn = new Button(buttonComp, SWT.PUSH);
		addToValSetBtn.setImage(Images.ADD);
		addToValSetBtn.setText("Validation");
		addToValSetBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

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
		removeFromTrainSetBtn.setText("Remove selected entries from training set");

		Group valSetGrp = new Group(trainOverviewCont, SWT.NONE);
		valSetGrp.setText("Validation Set");
		valSetGrp.setLayoutData(tableGd);
		valSetGrp.setLayout(tableGl);

		valSetOverviewTable = new DataSetTableWidget<IDataSelectionEntry<?, ?>>(valSetGrp, SWT.BORDER) {};
		valSetOverviewTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		removeFromValSetBtn = new Button(valSetGrp, SWT.PUSH);
		removeFromValSetBtn.setLayoutData(buttonGd);
		removeFromValSetBtn.setImage(Images.CROSS);
		removeFromValSetBtn.setText("Remove selected entries from validation set");

		this.setWeights(new int[] { 45, 10, 45 });
		dataTabFolder.setSelection(documentsTabItem);
		
		docTv.getTree().pack();
		buttonComp.pack();
		trainOverviewCont.pack();
		trainSetGrp.pack();
		valSetGrp.pack();
		
		new DataSetSelectionSashFormListener(this, dataSetSelectionController);
	}

	public void setGroundTruthSelectionEnabled(boolean enabled) {
		if(enabled) {
			if (gtTabItem == null || gtTabItem.isDisposed()) {
				gtTabItem = new CTabItem(dataTabFolder, SWT.NONE);
				gtTabItem.setText("HTR Model Data");
				gtTabItem.setControl(dataTabComp);
				return;
			}
		} else {
			if(gtTabItem != null) {
				gtTabItem.dispose();
				gtTabItem = null;
				dataSetSelectionController.removeAllGtFromSelection();
				return;
			}
		}
	}

	private TreeViewer createDocumentTreeViewer(Composite parent) {
		TreeViewer tv = new TreeViewer(parent, SWT.BORDER | SWT.MULTI);
		final CollectionContentProvider docContentProvider = new CollectionContentProvider(colId);
		final CollectionLabelProvider docLabelProvider = new CollectionDataSetLabelProvider(dataSetSelectionController);
		tv.setContentProvider(docContentProvider);
		tv.setLabelProvider(docLabelProvider);
		tv.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tv.setInput(this.docList);
		return tv;
	}
	
	private TreeViewer createGroundTruthTreeViewer(Composite parent) {
		TreeViewer tv = new TreeViewer(parent, SWT.BORDER | SWT.MULTI);
		final HtrGroundTruthContentProvider htrGtContentProvider = new HtrGroundTruthContentProvider(colId);
		final HtrGroundTruthDataSetLabelProvider htrGtLabelProvider = new HtrGroundTruthDataSetLabelProvider(dataSetSelectionController);
		tv.setContentProvider(htrGtContentProvider);
		tv.setLabelProvider(htrGtLabelProvider);
		tv.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		//the GroundTruthTreeWidget with table treeviewer is not compatible with the content- and labelprovider here.
//		final ITreeContentProvider htrGtContentProvider = new HtrGroundTruthContentProvider(colId);
//		final CellLabelProvider htrGtLabelProvider = new HtrGroundTruthDataSetLabelProvider(dataSetSelectionController);
//		GroundTruthTreeWidget gtWidget = new GroundTruthTreeWidget(parent, htrGtContentProvider, htrGtLabelProvider);
//		gtWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		tv.setInput(this.htrList);
		
		
		return tv;
	}
	

	/**
	 * Show dialog for resolving conflicts with overlapping images.
	 * <ul>
	 * <li>SWT.YES = replace data in selection with gtOverlapByImageId</li>
	 * <li>SWT.NO = discard gtOverlapByImageId and keep previous selection</li>
	 * <li>SWT.CANCEL = do nothing</li>
	 * </ul>
	 * @param docMd
	 * @param gtOverlapByImageId
	 * @return SWT.YES, SWT.NO, SWT.CANCEL
	 */
	int openConflictDialog(TrpDocMetadata docMd, List<TrpPage> gtOverlapByImageId) {
		String title = "Some of the data is already selected";
		String msg = "The images of the following pages are already included in the selection:\n\n";
		String pageStr = CoreUtils.getRangeListStrFromList(gtOverlapByImageId.stream().map(p -> p.getPageNr()).collect(Collectors.toList()));
		msg += "Document '" + docMd.getTitle() + "' pages " + pageStr;
		msg += "\n\nDo you want to replace the previous selection with those pages?";
		
		MessageBox messageBox = new MessageBox(this.getShell(), SWT.ICON_QUESTION
	            | SWT.YES | SWT.NO | SWT.CANCEL);
        messageBox.setMessage(msg);
        messageBox.setText(title);
        return messageBox.open();	
	}
	
	/**
	 * Show dialog for resolving conflicts with overlapping images.
	 * <ul>
	 * <li>SWT.YES = replace data in selection with gtOverlapByImageId</li>
	 * <li>SWT.NO = discard gtOverlapByImageId and keep previous selection</li>
	 * <li>SWT.CANCEL = do nothing</li>
	 * </ul>
	 * @param docMd
	 * @param gtOverlapByImageId
	 * @return SWT.YES, SWT.NO, SWT.CANCEL
	 */
	int openConflictDialog(HtrGtDataSet gtSet, List<HtrGtDataSetElement> gtOverlapByImageId) {
		String title = "Some of the image data is already included";
		String msg = "The images of the following HTR model data are already included in the selection:\n\n";
		if(gtOverlapByImageId.size() == 1) {
			msg += "HTR " + gtSet.getDataSetType().getLabel() + " '" + gtSet.getHtr().getName() 
					+ "' page " + gtOverlapByImageId.get(0).getGroundTruthPage().getPageNr();
		} else {
			List<Integer> pageIndices = gtOverlapByImageId.stream()
					.map(g -> (g.getGroundTruthPage().getPageNr() - 1))
					.collect(Collectors.toList());
			String pageStr = CoreUtils.getRangeListStrFromList(pageIndices);
			msg += "HTR " + gtSet.getDataSetType().getLabel() + " '" + gtSet.getHtr().getName() + "' pages " + pageStr;
		}
		msg += "\n\nDo you want to replace the previous selection with those pages?";
		
		MessageBox messageBox = new MessageBox(this.getShell(), SWT.ICON_QUESTION
	            | SWT.YES | SWT.NO | SWT.CANCEL);
        messageBox.setMessage(msg);
        messageBox.setText(title);
        return messageBox.open();	
	}
	
	/**
	 * Update ground truth treeviewer row colors according to selected data set.
	 * 
	 * For now this will expect train/validation data to be selected to the respective sets!
	 * 
	 * @param trainGtMap
	 * @param valGtMap
	 */
	void updateGtTvColors(Map<HtrGtDataSet, List<HtrGtDataSetElement>> trainGtMap, Map<HtrGtDataSet, List<HtrGtDataSetElement>> valGtMap) {
		groundTruthTv.refresh(true);
	}
	
	void updateDocTvColors(Map<TrpDocMetadata, List<TrpPage>> trainDocMap, Map<TrpDocMetadata, List<TrpPage>> valDocMap) {
		docTv.refresh(true);
	}
	
	public void updateThumbnail(URL thumbUrl) {
		logger.debug("Update thumbnail: " + thumbUrl + " | current thumnail: " + currentThumbUrl);
		if(thumbUrl == null) {
			logger.debug("Remove image from view");
			updateThumbnail((Image)null);
			return;
		}
		if(!thumbUrl.equals(currentThumbUrl)) {
			//update thumbnail on URL change only
			Runnable r = new Runnable() {
				@Override
				public void run() {
					updateThumbnail(loadThumbnail(thumbUrl));
					currentThumbUrl = thumbUrl;
				}
			};
			getDisplay().asyncExec(r);
		} else {
			logger.debug("Keeping current thumb as URL has not changed");
		}
	}
	
	/**
	 * Update the thumbnail label with given image.
	 * 
	 * @param image the image or null to clear the label
	 */
	private void updateThumbnail(Image image) {
		if (previewLbl.getImage() != null) {
			previewLbl.getImage().dispose();
		}
		previewLbl.setImage(image);
	}

	private Image loadThumbnail(URL thumbUrl) {
		Image image;
		try {
			image = ImgLoader.load(thumbUrl);
		} catch (IOException e) {
			logger.error("Could not load image", e);
			image = null;
		}
		return image;
	}
	
	/**
	 * Set infoLabelText on info label.
	 * 
	 * @param infoLabelText the text or null to clear the label.
	 */
	void updateInfoLabel(String infoLabelText) {
		if(infoLabelText == null) {
			infoLabelText = "";
		}
		this.infoLbl.setText(infoLabelText);
		this.infoLbl.requestLayout();
	}
	
	public List<DataSetMetadata> getTrainSetMetadata() {
		return dataSetSelectionController.getTrainSetMetadata();
	}
	
	public List<DataSetMetadata> getValSetMetadata() {
		return dataSetSelectionController.getValSetMetadata();
	}
	
	public Button getUseGtVersionChk() {
		return useGtVersionChk;
	}
	
	public Button getUseNewVersionChk() {
		return useNewVersionChk;
	}

	DataSetSelectionController getController() {
		return dataSetSelectionController;
	}

	public DataSetSelection getSelection(EditStatus status) {
		return dataSetSelectionController.getSelection(status);
	}
	
	boolean isGtTabActive() {
		return this.gtTabItem.equals(this.dataTabFolder.getSelection());
	}
	boolean isDocumentsTabActive() {
		return this.documentsTabItem.equals(this.dataTabFolder.getSelection());
	}

	public void enableDebugDialog(boolean b) {
		getController().SHOW_DEBUG_DIALOG = b;
	}
}
