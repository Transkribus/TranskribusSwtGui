package eu.transkribus.swt_gui.htr.treeviewer;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.CitLabHtrTrainConfig;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.model.beans.GroundTruthSelectionDescriptor;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpGroundTruthPage;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.core.util.DescriptorUtils;
import eu.transkribus.core.util.JaxbUtils;
import eu.transkribus.core.util.DescriptorUtils.GroundTruthDataSetDescriptor;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionContentProvider;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSet;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSetElement;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class DataSetSelectionController {
	private static final Logger logger = LoggerFactory.getLogger(DataSetSelectionController.class);
	
	//maps containing current selection. Maybe handling becomes less complex if this solely handled in table?
	private Map<TrpDocMetadata, List<TrpPage>> trainDocMap, testDocMap;
	private Map<HtrGtDataSet, List<HtrGtDataSetElement>> trainGtMap, testGtMap;
	
	private final DataSetSelectionSashForm view;
	
	/**
	 * The collectionId the view was started in.
	 */
	private final int colId;
	
	final boolean DEBUG = true;
	DebugDialog diag = null;
	
	/**
	 * Encapsulate logic for handling the selection of distinct datasets upon user selection and updating the view accordingly.
	 * 
	 * @param colId
	 * @param view
	 */
	public DataSetSelectionController(final int colId, DataSetSelectionSashForm view) {
		trainDocMap = new TreeMap<>();
		testDocMap = new TreeMap<>();
		trainGtMap = new TreeMap<>();
		testGtMap = new TreeMap<>();
		this.view = view;
		this.colId = colId;
	}
	
	public void addDocumentSelectionToTrainSet() {
		addDocumentSelectionToDataMap((IStructuredSelection) view.docTv.getSelection(), trainDocMap, testDocMap);
		updateView();
	}


	public void addDocumentSelectionToValidationSet() {
		addDocumentSelectionToDataMap((IStructuredSelection) view.docTv.getSelection(), testDocMap, trainDocMap);
		updateView();
	}
	
	public void addGtSelectionToTrainSet() {
		String infoLabelText = addGtSelectionToDataMap((IStructuredSelection) view.groundTruthTv.getSelection(), trainGtMap, testGtMap);
		updateView(infoLabelText);
	}
	
	public void addGtSelectionToValidationSet() {
		String infoLabelText = addGtSelectionToDataMap((IStructuredSelection) view.groundTruthTv.getSelection(), testGtMap, trainGtMap);
		updateView(infoLabelText);
	}

	public void removeSelectionFromTrainSet(List<IDataSelectionEntry<?, ?>> entries) {
		if (entries == null || entries.isEmpty()) {
			return;
		}
		for (IDataSelectionEntry<?, ?> entry : entries) {
			if(entry instanceof DocumentDataSelectionEntry) {
				trainDocMap.remove(((DocumentDataSelectionEntry)entry).getDoc());
			} else if (entry instanceof HtrGroundTruthDataSelectionEntry) {
				trainGtMap.remove(((HtrGroundTruthDataSelectionEntry)entry).getDoc());
			}
		}
		updateView();
	}


	public void removeSelectionFromTestSet(List<IDataSelectionEntry<?, ?>> entries) {
		if (entries == null || entries.isEmpty()) {
			return;
		}
		for (IDataSelectionEntry<?, ?> entry : entries) {
			if(entry instanceof DocumentDataSelectionEntry) {
				testDocMap.remove(((DocumentDataSelectionEntry)entry).getDoc());
			} else if (entry instanceof HtrGroundTruthDataSelectionEntry) {
				testGtMap.remove(((HtrGroundTruthDataSelectionEntry)entry).getDoc());
			}
		}
		updateView();
	}
	
	/**
	 * Remove all GT from selection, only keep documents. Needed when switching to T2I configuration.
	 */
	public void removeAllGtFromSelection() {
		trainGtMap.clear();
		testGtMap.clear();
		updateView();		
	}
	
	private void updateView() {
		updateView(null);
	}
	
	private void updateView(String infoLabelText) {
		view.trainSetOverviewTable.setInput(createTableEntries(trainDocMap, trainGtMap));
		view.testSetOverviewTable.setInput(createTableEntries(testDocMap, testGtMap));
		view.updateDocTvColors(trainDocMap, testDocMap);
		view.updateGtTvColors(trainGtMap, testGtMap);
		updateInfoLabel(infoLabelText);
		
		if(DEBUG) {
			if(diag == null || diag.getShell() == null || diag.getShell().isDisposed()) {
				diag = new DebugDialog(view.getShell());
				diag.open();
			} else {
				diag.setVisible();
				diag.updateText();
				diag.setVisible();
			}
		}
	}

	private List<IDataSelectionEntry<?, ?>> createTableEntries(Map<TrpDocMetadata, List<TrpPage>> docMap, 
		Map<HtrGtDataSet, List<HtrGtDataSetElement>> gtMap) {
		List<IDataSelectionEntry<?, ?>> list = new ArrayList<>(docMap.entrySet().size() + gtMap.entrySet().size());
		for (Entry<TrpDocMetadata, List<TrpPage>> entry : docMap.entrySet()) {
			list.add(new DocumentDataSelectionEntry(entry.getKey(), entry.getValue()));
		}
		for (Entry<HtrGtDataSet, List<HtrGtDataSetElement>> entry : gtMap.entrySet()) {
			list.add(new HtrGroundTruthDataSelectionEntry(entry.getKey(), entry.getValue()));
		}
		Collections.sort(list);
		return list;
	}
	
	/**
	 * Add selected items to the targetDataMap and remove them from the nonIntersectingDataMap if included.
	 * 
	 * @param selection
	 * @param targetDataMap
	 * @param nonIntersectingDataMap
	 */
	private void addDocumentSelectionToDataMap(IStructuredSelection selection,
			Map<TrpDocMetadata, List<TrpPage>> targetDataMap, 
			Map<TrpDocMetadata, List<TrpPage>> nonIntersectingDataMap) {
		Iterator<?> it = selection.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof TrpDocMetadata) {
				TrpDocMetadata docMd = (TrpDocMetadata) o;
				TrpPage[] pageObjArr = ((CollectionContentProvider)view.docTv.getContentProvider()).getChildren(docMd);
				
				List<TrpPage> pagesToAdd = detectAndResolveConflicts(docMd, new ArrayList<>(Arrays.asList(pageObjArr)));
				if(pagesToAdd.isEmpty()) {
					return;
				}
				
				targetDataMap.put(docMd, pagesToAdd);
				if (nonIntersectingDataMap.containsKey(docMd)) {
					nonIntersectingDataMap.remove(docMd);
				}
			} else if (o instanceof TrpPage) {
				TrpPage p = (TrpPage) o;
				TrpDocMetadata parent = (TrpDocMetadata) ((CollectionContentProvider)view.docTv.getContentProvider()).getParent(p);
				TrpPage pageToAdd = detectAndResolveConflicts(parent, p);
				if(pageToAdd == null) {
					return;
				}
				
				if (targetDataMap.containsKey(parent) && !targetDataMap.get(parent).contains(p)) {
					targetDataMap.get(parent).add(p);
				} else if (!targetDataMap.containsKey(parent)) {
					List<TrpPage> pageList = new ArrayList<>();
					pageList.add(p);
					targetDataMap.put(parent, pageList);
				}

				if (nonIntersectingDataMap.containsKey(parent) && nonIntersectingDataMap.get(parent).contains(p)) {
					if (nonIntersectingDataMap.get(parent).size() == 1) {
						nonIntersectingDataMap.remove(parent);
					} else {
						nonIntersectingDataMap.get(parent).remove(p);
					}
				}
			}
		}			
	}

	/**
	 * Add selected items to the targetDataMap and remove them from the nonIntersectingDataMap if included.
	 * 
	 * TODO
	 * 
	 * @param selection
	 * @param targetDataMap
	 * @param nonIntersectingDataMap
	 * @return message for display to user with information on issues with the given selection (e.g. is items where omitted due to inclusion). null if the whole selection was added.
	 */
	private String addGtSelectionToDataMap(IStructuredSelection selection,
			Map<HtrGtDataSet, List<HtrGtDataSetElement>> targetDataMap, 
			Map<HtrGtDataSet, List<HtrGtDataSetElement>> nonIntersectingDataMap) {
		int nrOfItemsOmitted = 0;
		Iterator<?> it = selection.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof TrpHtr) {
				TrpHtr htr = (TrpHtr) o;
				Object[] htrGtSets = ((HtrGroundTruthContentProvider)view.groundTruthTv.getContentProvider()).getChildren(htr);
				
				for (Object gtDataSet : htrGtSets) {
					HtrGtDataSet htrGtDataSet = (HtrGtDataSet)gtDataSet;
					nrOfItemsOmitted += addGtSetToDataMap(htrGtDataSet, targetDataMap, nonIntersectingDataMap);
				}
			} else if (o instanceof HtrGtDataSet) {
				HtrGtDataSet htrGtDataSet = (HtrGtDataSet) o;
				nrOfItemsOmitted += addGtSetToDataMap(htrGtDataSet, targetDataMap, nonIntersectingDataMap);
			} else if (o instanceof HtrGtDataSetElement) {
				HtrGtDataSetElement p = (HtrGtDataSetElement) o;
				HtrGtDataSet parent = (HtrGtDataSet) ((HtrGroundTruthContentProvider)view.groundTruthTv.getContentProvider()).getParent(p);
				
				//check if Ground Truth entity with this ID is already in selection and omit automatically
				if(!getGtSetsFromSelectionIncludingElement(p).isEmpty()) {
					//element already included via other set
					nrOfItemsOmitted++;
					continue;
				}
				
				//if another entity, using the same image is already included in selection, ask user to resolve conflict
				p = detectAndResolveImageConflicts(parent, p);
				if(p == null) {
					continue;
				}
				
				if (targetDataMap.containsKey(parent) && !targetDataMap.get(parent).contains(p)) {
					targetDataMap.get(parent).add(p);
				} else if (!targetDataMap.containsKey(parent)) {
					List<HtrGtDataSetElement> pageList = new ArrayList<>();
					pageList.add(p);
					targetDataMap.put(parent, pageList);
				}
				if (nonIntersectingDataMap.containsKey(parent) && nonIntersectingDataMap.get(parent).contains(p)) {
					if (nonIntersectingDataMap.get(parent).size() == 1) {
						nonIntersectingDataMap.remove(parent);
					} else {
						nonIntersectingDataMap.get(parent).remove(p);
					}
				}
			}
		}
		return getItemsOmittedMessage(nrOfItemsOmitted);
	}
	
	private String getItemsOmittedMessage(int nrOfItemsOmitted) {
		if(nrOfItemsOmitted < 1) {
			return null; 
		}
		return nrOfItemsOmitted + " pages are already included by other data sets. Expand items for details.";
	}

	private int addGtSetToDataMap(HtrGtDataSet htrGtDataSet,
			Map<HtrGtDataSet, List<HtrGtDataSetElement>> targetDataMap, 
			Map<HtrGtDataSet, List<HtrGtDataSetElement>> nonIntersectingDataMap) {
		HtrGtDataSetElement[] gtPageArr = ((HtrGroundTruthContentProvider)view.groundTruthTv.getContentProvider()).getChildren(htrGtDataSet);
		if(gtPageArr == null) {
			logger.error("No children could be determined for HTR GT set: " + htrGtDataSet);
			return 0;
		}
		final int gtSetSize = gtPageArr.length;
		
		//filter for elements that are not included via other selected GT sets
		List<HtrGtDataSetElement> gtPageList = Arrays.stream(gtPageArr)
				.filter(e -> getGtSetsFromSelectionIncludingElement(e).isEmpty())
				.collect(Collectors.toList());
		
		if(gtPageList.isEmpty()) {
			//all GT entities already included
			return gtSetSize;
		}
		
		List<HtrGtDataSetElement> gtPagesToAdd = detectAndResolveConflicts(htrGtDataSet, gtPageList);
		if(gtPagesToAdd.isEmpty()) {
			return 0;
		}
		
		targetDataMap.put(htrGtDataSet, gtPageList);
		if (nonIntersectingDataMap.containsKey(htrGtDataSet)) {
			nonIntersectingDataMap.remove(htrGtDataSet);
		}
		//return nr of pages that have been automatically removed from selection
		return gtSetSize - gtPageList.size();
	}
	
	public List<HtrGtDataSet> getGtSetsFromSelectionIncludingElement(HtrGtDataSetElement element) {
		List<HtrGtDataSet> includedBySetList = new ArrayList<>();
		for(Entry<HtrGtDataSet, List<HtrGtDataSetElement>> e : getTrainGtMap().entrySet()) {
			if(e.getValue().stream()
					.anyMatch(g -> g.getGroundTruthPage().getGtId() == element.getGroundTruthPage().getGtId()) 
					&& !e.getKey().equals(element.getParentHtrGtDataSet())) {
				includedBySetList.add(e.getKey());
			}
		}
		for(Entry<HtrGtDataSet, List<HtrGtDataSetElement>> e : getTestGtMap().entrySet()) {
			if(e.getValue().stream()
					.anyMatch(g -> g.getGroundTruthPage().getGtId() == element.getGroundTruthPage().getGtId()) 
					&& !e.getKey().equals(element.getParentHtrGtDataSet())) {
				includedBySetList.add(e.getKey());
			}
		}
		return includedBySetList;
	}
	
	private TrpPage detectAndResolveConflicts(TrpDocMetadata docMd, TrpPage page) {
		List<TrpPage> pagesToAdd = detectAndResolveConflicts(docMd, Arrays.asList(new TrpPage[]{page}));
		if(pagesToAdd.isEmpty()) {
			return null;
		}
		return pagesToAdd.get(0);
	}
	
	private List<TrpPage> detectAndResolveConflicts(TrpDocMetadata docMd, List<TrpPage> pageList) {
		List<TrpPage> gtOverlapByImageId = findPageImageOverlapWithSelection(pageList);
		if(gtOverlapByImageId.isEmpty()) {
			return pageList; //nothing to do
		}
		int ret = view.openConflictDialog(docMd, gtOverlapByImageId);
		switch(ret) {
		case SWT.YES:
			removeOverlapFromSelectionByPages(gtOverlapByImageId);
			break;
		case SWT.NO:
			pageList = new ArrayList<>(pageList);
			pageList.removeAll(gtOverlapByImageId);
			break;
		default: //SWT.CANCEL
			return new ArrayList<>(0);
		}
		return pageList;
	}
	
	private HtrGtDataSetElement detectAndResolveImageConflicts(HtrGtDataSet gtSet, HtrGtDataSetElement gt) {
		List<HtrGtDataSetElement> gtToAdd = detectAndResolveConflicts(gtSet, Arrays.asList(new HtrGtDataSetElement[]{gt}));
		if(gtToAdd.isEmpty()) {
			return null;
		}
		return gtToAdd.get(0);
	}
	
	private List<HtrGtDataSetElement> detectAndResolveConflicts(HtrGtDataSet gtSet, List<HtrGtDataSetElement> gtList) {
		List<HtrGtDataSetElement> gtOverlapByImageId = findGtOverlapWithSelection(gtList);
		if(gtOverlapByImageId.isEmpty()) {
			return gtList; //nothing to do
		}
		int ret = view.openConflictDialog(gtSet, gtOverlapByImageId);
		switch(ret) {
		case SWT.YES:
			removeOverlapFromSelectionByGt(gtOverlapByImageId);
			break;
		case SWT.NO:
			gtList = new ArrayList<>(gtList);
			gtList.removeAll(gtOverlapByImageId);
			break;
		default: //SWT.CANCEL
			return new ArrayList<>(0);
		}
		return gtList;
	}

	/**
	 * Get pages from pageList where imageId is already included in current selection
	 * 
	 * @param pageList
	 * @return the list of overlapping elements
	 */
	private List<TrpPage> findPageImageOverlapWithSelection(List<TrpPage> pageList) {	
		List<Integer> imageIds = getAllImageIdsInSelection();
		return pageList.stream()
				.filter(p -> imageIds.contains(p.getImageId()))
				.collect(Collectors.toList());
	}
	
	/**
	 * Get GT pages from gtList where imageId is already included in current selection
	 * 
	 * @param pageList
	 * @return the list of overlapping elements
	 */
	private List<HtrGtDataSetElement> findGtOverlapWithSelection(List<HtrGtDataSetElement> gtList) {
		List<Integer> imageIds = getAllImageIdsInSelection();
		//return overlap
		return gtList.stream()
				.filter(g -> imageIds.contains(g.getGroundTruthPage().getImageId()))
				.collect(Collectors.toList());
	}
	
	private List<Integer> getAllImageIdsInSelection() {
		List<Integer> imageIds = new ArrayList<>(getNrOfPagesInSelection());
		imageIds.addAll(extractImageIdsFromGtSelection(trainGtMap));
		imageIds.addAll(extractImageIdsFromGtSelection(testGtMap));
		imageIds.addAll(extractImageIdsFromDocSelection(trainDocMap));
		imageIds.addAll(extractImageIdsFromDocSelection(testDocMap));
		return imageIds;
	}
	
	private List<Integer> extractImageIdsFromGtSelection(Map<HtrGtDataSet, List<HtrGtDataSetElement>> map) {
		//collect all included imageIds
		List<Integer> imageIdsInGt = new ArrayList<>();
		for(Entry<HtrGtDataSet, List<HtrGtDataSetElement>> e : map.entrySet()) {
			imageIdsInGt.addAll(e.getValue().stream()
					.map(g -> g.getGroundTruthPage().getImageId())
					.collect(Collectors.toList()));
		}
		return imageIdsInGt;
	}
	
	private List<Integer> extractImageIdsFromDocSelection(Map<TrpDocMetadata, List<TrpPage>> map) {
		//collect all included imageIds
		List<Integer> imageIdsInDocs = new ArrayList<>();
		for(Entry<TrpDocMetadata, List<TrpPage>> e : map.entrySet()) {
			imageIdsInDocs.addAll(e.getValue().stream()
					.map(p -> p.getImageId())
					.collect(Collectors.toList()));
		}
		return imageIdsInDocs;
	}
	
	private void removeOverlapFromSelectionByPages(List<TrpPage> overlap) {
		List<Integer> imageIds = overlap.stream().map(p -> p.getImageId()).collect(Collectors.toList());
		removeGtFromSelectionByImageId(imageIds, trainGtMap);
		removeGtFromSelectionByImageId(imageIds, testGtMap);
		removePagesFromSelectionByImageId(imageIds, trainDocMap);
		removePagesFromSelectionByImageId(imageIds, testDocMap);
	}
	
	/**
	 * remove overlap from selected documents. GT entities are not checked here as they have no versioning, are unambiguous and can be de-duplicated automatically.
	 * 
	 * @param overlap
	 */
	private void removeOverlapFromSelectionByGt(List<HtrGtDataSetElement> overlap) {
		List<Integer> imageIds = overlap.stream().map(g -> g.getGroundTruthPage().getImageId()).collect(Collectors.toList());
		removeGtFromSelectionByImageId(imageIds, trainGtMap);
		removeGtFromSelectionByImageId(imageIds, testGtMap);
		removePagesFromSelectionByImageId(imageIds, trainDocMap);
		removePagesFromSelectionByImageId(imageIds, testDocMap);
	}

	private void removeGtFromSelectionByImageId(List<Integer> imageIds,
			Map<HtrGtDataSet, List<HtrGtDataSetElement>> map) {
		Iterator<Entry<HtrGtDataSet, List<HtrGtDataSetElement>>> setIt = map.entrySet().iterator();
		while(setIt.hasNext()) {
			Entry<HtrGtDataSet, List<HtrGtDataSetElement>> set = setIt.next();
			Iterator<HtrGtDataSetElement> it = set.getValue().iterator();
			while(it.hasNext()) {
				if(imageIds.contains(it.next().getGroundTruthPage().getImageId())) {
					it.remove();
				}
			}
			if(set.getValue().isEmpty()) {
				//all pages have been removed!
				setIt.remove();
			}
		}
	}
	
	private void removePagesFromSelectionByImageId(List<Integer> imageIds,
			Map<TrpDocMetadata, List<TrpPage>> map) {
		Iterator<Entry<TrpDocMetadata, List<TrpPage>>> setIt = map.entrySet().iterator();
		while(setIt.hasNext()) {
			Entry<TrpDocMetadata, List<TrpPage>> set = setIt.next();
			Iterator<TrpPage> it = set.getValue().iterator();
			while(it.hasNext()) {
				if(imageIds.contains(it.next().getImageId())) {
					it.remove();
				}
			}
			if(set.getValue().isEmpty()) {
				//all pages have been removed!
				setIt.remove();
			}
		}
	}

	private int getNrOfPagesInSelection() {
		int count = trainDocMap.entrySet().stream().mapToInt(e -> e.getValue().size()).sum();
		count += testDocMap.entrySet().stream().mapToInt(e -> e.getValue().size()).sum();
		count += trainGtMap.entrySet().stream().mapToInt(e -> e.getValue().size()).sum();
		count += testGtMap.entrySet().stream().mapToInt(e -> e.getValue().size()).sum();
		logger.debug("Nr of pages in selection: " + count);
		return count;
	}
	
	/**
	 * TODO
	 * 
	 * @param map
	 * @return
	 */
	private DataSetMetadata computeDataSetSize(Map<TrpDocMetadata, List<TrpPage>> map) {
		final boolean useGt = view.getUseGtVersionChk().isEnabled() && view.getUseGtVersionChk().getSelection();
		final boolean useInitial = view.getUseNewVersionChk().isEnabled() && view.getUseNewVersionChk().getSelection();
		int pages = 0;
		int lines = 0;
		int words = 0;
		for (Entry<TrpDocMetadata, List<TrpPage>> e : map.entrySet()) {
			for (TrpPage p : e.getValue()) {
				TrpTranscriptMetadata tmd = p.getCurrentTranscript();
				if (useGt || useInitial) {
					for (TrpTranscriptMetadata t : p.getTranscripts()) {
						if (useGt && t.getStatus().equals(EditStatus.GT)) {
							tmd = t;
							break;
						}
						if (useInitial && t.getStatus().equals(EditStatus.NEW)){
							tmd=t;
							break;
						}
					}
				}
				pages++;
				lines += tmd.getNrOfTranscribedLines();
				words += tmd.getNrOfWordsInLines();
			}
		}
		return new DataSetMetadata(pages, lines, words);
	}
	
	Map<TrpDocMetadata, List<TrpPage>> getTrainDocMap() {
		return trainDocMap;
	}
	
	Map<TrpDocMetadata, List<TrpPage>> getTestDocMap() {
		return testDocMap;
	}
	
	Map<HtrGtDataSet, List<HtrGtDataSetElement>> getTrainGtMap() {
		return trainGtMap;
	}
	
	Map<HtrGtDataSet, List<HtrGtDataSetElement>> getTestGtMap() {
		return testGtMap;
	}
	
	public DataSetMetadata getTrainSetMetadata() {
		return computeDataSetSize(getTrainDocMap());
	}
	
	public DataSetMetadata getTestSetMetadata() {
		return computeDataSetSize(getTestDocMap());
	}
	
	public int getColId() {
		return colId;
	}
	
	void updateThumbnail(IStructuredSelection selection) {
		Object o = selection.getFirstElement();
		URL thumbUrl = null;
		if (o instanceof TrpPage) {
			TrpPage p = (TrpPage) o;
			thumbUrl = p.getThumbUrl();
		} else if (o instanceof HtrGtDataSetElement) {
			HtrGtDataSetElement g = (HtrGtDataSetElement) o;
			thumbUrl = g.getGroundTruthPage().getImage().getThumbUrl();
		} else {
			thumbUrl = null;
		}
		view.updateThumbnail(thumbUrl);
	}
	
	/**
	 * TODO move to view
	 * 
	 * @param infoLabelText
	 */
	private void updateInfoLabel(String infoLabelText) {
		if(infoLabelText == null) {
			infoLabelText = "";
		}
		view.infoLbl.setText(infoLabelText);
		view.infoLbl.requestLayout();
	}

	public void loadPageInMainWidget(TrpPage p) {
		TrpLocation loc = new TrpLocation();
		loc.collId = colId;
		loc.docId = p.getDocId();
		loc.pageNr = p.getPageNr();
		TrpMainWidget.getInstance().showLocation(loc);
	}

	public DataSetSelection getSelection(EditStatus status) {
		
		//FIXME activate this method once the training job can handle such descriptors
//		config.setTrain(DescriptorUtils.buildSelectionDescriptorList(trainDocMap, status));
//		config.setTest(DescriptorUtils.buildSelectionDescriptorList(testDocMap, status));
	
		List<DocumentSelectionDescriptor> trainDocDescs = DescriptorUtils.buildCompleteSelectionDescriptorList(getTrainDocMap(), status);
		List<DocumentSelectionDescriptor> validationDocDescs = DescriptorUtils.buildCompleteSelectionDescriptorList(getTestDocMap(), status);
		
		//TODO
		List<GroundTruthSelectionDescriptor> trainGtDescs = buildGtSelectionDescriptorList(getTrainGtMap());
		List<GroundTruthSelectionDescriptor> validationGtDescs = buildGtSelectionDescriptorList(getTestGtMap());
		
		return new DataSetSelection(trainDocDescs, validationDocDescs, trainGtDescs, validationGtDescs);
	}
	
	/**
	 * Translate internal HTR specific representation into a generic one and build the descriptor using DescriptorUtils.
	 * 
	 * @param internalGtMap
	 * @return
	 */
	private List<GroundTruthSelectionDescriptor> buildGtSelectionDescriptorList(
			Map<HtrGtDataSet, List<HtrGtDataSetElement>> internalGtMap) {
		Map<GroundTruthDataSetDescriptor, List<TrpGroundTruthPage>> gtMap = new HashMap<>();
		for(Entry<HtrGtDataSet, List<HtrGtDataSetElement>> e : internalGtMap.entrySet()) {
			List<TrpGroundTruthPage> unwrappedGt = e.getValue().stream()
					.map(g -> g.getGroundTruthPage()).collect(Collectors.toList());
			gtMap.put(e.getKey(), unwrappedGt);
		}
		return DescriptorUtils.buildGtSelectionDescriptorList(gtMap);
	}

	public static class DataSetSelection {
		final List<DocumentSelectionDescriptor> trainDocDescriptorList;
		final List<DocumentSelectionDescriptor> validationDocDescriptorList;
		final List<GroundTruthSelectionDescriptor> trainGtDescriptorList;
		final List<GroundTruthSelectionDescriptor> validationGtDescriptorList;
		
		DataSetSelection(List<DocumentSelectionDescriptor> trainDocDescriptorList,
				List<DocumentSelectionDescriptor> validationDocDescriptorList,
				List<GroundTruthSelectionDescriptor> trainGtDescriptorList,
				List<GroundTruthSelectionDescriptor> validationGtDescriptorList) {
			this.trainDocDescriptorList = trainDocDescriptorList;
			this.validationDocDescriptorList = validationDocDescriptorList;
			this.trainGtDescriptorList = trainGtDescriptorList;
			this.validationGtDescriptorList = validationGtDescriptorList;
		}

		public List<DocumentSelectionDescriptor> getTrainDocDescriptorList() {
			return trainDocDescriptorList;
		}

		public List<DocumentSelectionDescriptor> getValidationDocDescriptorList() {
			return validationDocDescriptorList;
		}

		public List<GroundTruthSelectionDescriptor> getTrainGtDescriptorList() {
			return trainGtDescriptorList;
		}

		public List<GroundTruthSelectionDescriptor> getValidationGtDescriptorList() {
			return validationGtDescriptorList;
		}
	}
	
	private class DebugDialog extends Dialog {
		Text text;
		protected DebugDialog(Shell parentShell) {
			super(parentShell);
		}
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite c = (Composite) super.createDialogArea(parent);
			text = new Text(c, SWT.READ_ONLY | SWT.V_SCROLL);
			text.setLayoutData(new GridData(GridData.FILL_BOTH));
			updateText();
			c.pack();
			return c;
		}
		@Override
		protected Point getInitialSize() {
			return new Point(800, 1000);
		}

		@Override
		protected void setShellStyle(int newShellStyle) {
			super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE);
		}
		
		public void setVisible() {
			if (super.getShell() != null && !super.getShell().isDisposed()) {
				super.getShell().setVisible(true);
			}
		}

		void updateText() {
			try {
				DataSetSelection sel = getSelection(null);
				CitLabHtrTrainConfig conf = new CitLabHtrTrainConfig();
				conf.setTrain(sel.getTrainDocDescriptorList());
				conf.setTest(sel.getValidationDocDescriptorList());
				if(!sel.getTrainGtDescriptorList().isEmpty()) {
					conf.setTrainGt(sel.getTrainGtDescriptorList());
				}
				if(!sel.getValidationGtDescriptorList().isEmpty()) {
					conf.setTestGt(sel.getValidationGtDescriptorList());
				}
				
				text.setText(JaxbUtils.marshalToJsonString(conf, true));
			} catch (JAXBException e) {
				text.setText(e.getMessage());
			}
		}
	}
}
