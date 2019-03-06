package eu.transkribus.swt_gui.htr.treeviewer;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.swt.util.ImgLoader;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionContentProvider;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSet;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSetElement;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class DataSetSelectionHandler {
	private static final Logger logger = LoggerFactory.getLogger(DataSetSelectionHandler.class);
	
	//maps containing current selection. Maybe handling becomes less complex if this solely handled in table?
	private Map<TrpDocMetadata, List<TrpPage>> trainDocMap, testDocMap;
	private Map<HtrGtDataSet, List<HtrGtDataSetElement>> trainGtMap, testGtMap;
	private final DataSetSelectionSashForm view;
	
	/**
	 * The collectionId the view was started in.
	 */
	private final int colId;
	
	/**
	 * Encapsulate logic for handling the selection of distinct datasets upon user selection and updating the view accordingly.
	 * 
	 * @param colId
	 * @param view
	 */
	public DataSetSelectionHandler(final int colId, DataSetSelectionSashForm view) {
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
	
	private void updateView() {
		updateView(null);
	}
	
	private void updateView(String infoLabelText) {
		view.trainSetOverviewTable.setInput(createTableEntries(trainDocMap, trainGtMap));
		view.testSetOverviewTable.setInput(createTableEntries(testDocMap, testGtMap));
		view.updateDocTvColors(trainDocMap, testDocMap);
		view.updateGtTvColors(trainGtMap, testGtMap);
		updateInfoLabel(infoLabelText);
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
				List<TrpPage> pageList = Arrays.asList(pageObjArr);
				targetDataMap.put(docMd, pageList);

				if (nonIntersectingDataMap.containsKey(docMd)) {
					nonIntersectingDataMap.remove(docMd);
				}
			} else if (o instanceof TrpPage) {
				TrpPage p = (TrpPage) o;
				TrpDocMetadata parent = (TrpDocMetadata) ((CollectionContentProvider)view.docTv.getContentProvider()).getParent(p);
				if (targetDataMap.containsKey(parent) && !targetDataMap.get(parent).contains(p)) {
					targetDataMap.get(parent).add(p);
				} else if (!targetDataMap.containsKey(parent)) {
					List<TrpPage> pageList = new LinkedList<>();
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
				if(!getGtSetsFromSelectionIncludingElement(p).isEmpty()) {
					//element already included via other set
					nrOfItemsOmitted++;
					continue;
				}
				if (targetDataMap.containsKey(parent) && !targetDataMap.get(parent).contains(p)) {
					targetDataMap.get(parent).add(p);
				} else if (!targetDataMap.containsKey(parent)) {
					List<HtrGtDataSetElement> pageList = new LinkedList<>();
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
		List<HtrGtDataSetElement> gtPagesInSet = Arrays.asList(gtPageArr);
		//filter for elements that are not included via other selected sets
		List<HtrGtDataSetElement> gtPageList = gtPagesInSet.stream()
				.filter(e -> getGtSetsFromSelectionIncludingElement(e).isEmpty())
				.collect(Collectors.toList());
		if(gtPageList.isEmpty()) {
			return gtPagesInSet.size();
		}
		targetDataMap.put(htrGtDataSet, gtPageList);
		if (nonIntersectingDataMap.containsKey(htrGtDataSet)) {
			nonIntersectingDataMap.remove(htrGtDataSet);
		}
		//return nr of pages added to selection
		return gtPagesInSet.size() - gtPageList.size();
	}
	
	public List<HtrGtDataSet> getGtSetsFromSelectionIncludingElement(HtrGtDataSetElement element) {
		List<HtrGtDataSet> includedBySetList = new LinkedList<>();
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
	
	public Map<TrpDocMetadata, List<TrpPage>> getTrainDocMap() {
		return trainDocMap;
	}
	
	public Map<TrpDocMetadata, List<TrpPage>> getTestDocMap() {
		return testDocMap;
	}
	
	public Map<HtrGtDataSet, List<HtrGtDataSetElement>> getTrainGtMap() {
		return trainGtMap;
	}
	
	public Map<HtrGtDataSet, List<HtrGtDataSetElement>> getTestGtMap() {
		return testGtMap;
	}
	
	public DataSetMetadata getTrainSetMetadata() {
		return computeDataSetSize(getTrainDocMap());
	}
	
	public DataSetMetadata getTestSetMetadata() {
		return computeDataSetSize(getTestDocMap());
	}
	
	void updateThumbnail(IStructuredSelection selection) {
		Object o = selection.getFirstElement();
		final Image image;
		if (o instanceof TrpPage) {
			TrpPage p = (TrpPage) o;
			image = loadThumbnail(p.getThumbUrl());
		} else if (o instanceof HtrGtDataSetElement) {
			HtrGtDataSetElement g = (HtrGtDataSetElement) o;
			image = loadThumbnail(g.getGroundTruthPage().getImage().getThumbUrl());
		} else {
			image = null;
		}
		updateThumbnail(image);
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
	
	/**
	 * TODO move to view
	 * 
	 * @param image
	 */
	private void updateThumbnail(Image image) {
		if (view.previewLbl.getImage() != null) {
			view.previewLbl.getImage().dispose();
		}
		view.previewLbl.setImage(image);
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

	public void loadPageInMainWidget(TrpPage p) {
		TrpLocation loc = new TrpLocation();
		loc.collId = colId;
		loc.docId = p.getDocId();
		loc.pageNr = p.getPageNr();
		TrpMainWidget.getInstance().showLocation(loc);
	}
}
