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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpGroundTruthPage;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.swt.util.ImgLoader;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionContentProvider;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionLabelProvider;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSet;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSetElement;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class DataSetSelectionHandler {
	private static final Logger logger = LoggerFactory.getLogger(DataSetSelectionHandler.class);
	
	private final CollectionContentProvider docContentProvider;
	private final CollectionLabelProvider docLabelProvider;
	private final HtrGroundTruthContentProvider htrGtContentProvider;
	private final HtrGroundTruthLabelProvider htrGtLabelProvider;
	
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
		docContentProvider = new CollectionContentProvider(colId);
		docLabelProvider = new CollectionColoredLabelProvider(this);
		htrGtContentProvider = new HtrGroundTruthContentProvider(colId);
		htrGtLabelProvider = new HtrGroundTruthLabelProvider(this);
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
		addGtSelectionToDataMap((IStructuredSelection) view.groundTruthTv.getSelection(), trainGtMap, testGtMap);
		updateView();
	}
	
	public void addGtSelectionToValidationSet() {
		addGtSelectionToDataMap((IStructuredSelection) view.groundTruthTv.getSelection(), testGtMap, trainGtMap);
		updateView();
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
		view.trainSetOverviewTable.setInput(createTableEntries(trainDocMap, trainGtMap));
		view.testSetOverviewTable.setInput(createTableEntries(testDocMap, testGtMap));
		view.updateDocTvColors(trainDocMap, testDocMap);
		view.updateGtTvColors(trainGtMap, testGtMap);
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
				TrpPage[] pageObjArr = docContentProvider.getChildren(docMd);
				List<TrpPage> pageList = Arrays.asList(pageObjArr);
				targetDataMap.put(docMd, pageList);

				if (nonIntersectingDataMap.containsKey(docMd)) {
					nonIntersectingDataMap.remove(docMd);
				}
			} else if (o instanceof TrpPage) {
				TrpPage p = (TrpPage) o;
				TrpDocMetadata parent = (TrpDocMetadata) docContentProvider.getParent(p);
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
	 */
	private void addGtSelectionToDataMap(IStructuredSelection selection,
			Map<HtrGtDataSet, List<HtrGtDataSetElement>> targetDataMap, 
			Map<HtrGtDataSet, List<HtrGtDataSetElement>> nonIntersectingDataMap) {
		Iterator<?> it = selection.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof TrpHtr) {
				
				/*
				 * TODO should adding the complete data of a HTR split up into train and validation set automatically?
				 */
				
				TrpHtr htr = (TrpHtr) o;
				Object[] htrGtSets = htrGtContentProvider.getChildren(htr);
				for (Object gtDataSet : htrGtSets) {
					HtrGtDataSet htrGtDataSet = (HtrGtDataSet)gtDataSet;
					HtrGtDataSetElement[] gtPageArr = htrGtContentProvider.getChildren(htrGtDataSet);
					if(gtPageArr == null) {
						logger.error("No children could be determined for HTR GT set: " + htrGtDataSet);
						return;
					}
					List<HtrGtDataSetElement> gtPageList = Arrays.asList(gtPageArr);
					targetDataMap.put(htrGtDataSet, gtPageList);
					if (nonIntersectingDataMap.containsKey(htrGtDataSet)) {
						nonIntersectingDataMap.remove(htrGtDataSet);
					}
				}
			} else if (o instanceof HtrGtDataSet) {
				HtrGtDataSet htrGtDataSet = (HtrGtDataSet) o;
				HtrGtDataSetElement[] gtPageArr = htrGtContentProvider.getChildren(htrGtDataSet);
				List<HtrGtDataSetElement> gtPageList = Arrays.asList(gtPageArr);
				targetDataMap.put(htrGtDataSet, gtPageList);
				if (nonIntersectingDataMap.containsKey(htrGtDataSet)) {
					nonIntersectingDataMap.remove(htrGtDataSet);
				}
			} else if (o instanceof TrpGroundTruthPage) {
				HtrGtDataSetElement p = (HtrGtDataSetElement) o;
				HtrGtDataSet parent = (HtrGtDataSet) htrGtContentProvider.getParent(p);
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
	
	public CollectionContentProvider getDocContentProvider() {
		return docContentProvider;
	}


	public CollectionLabelProvider getDocLabelProvider() {
		return docLabelProvider;
	}


	public HtrGroundTruthContentProvider getHtrGtContentProvider() {
		return htrGtContentProvider;
	}


	public HtrGroundTruthLabelProvider getHtrGtLabelProvider() {
		return htrGtLabelProvider;
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
		} else if (o instanceof TrpGroundTruthPage) {
			TrpGroundTruthPage g = (TrpGroundTruthPage) o;
			image = loadThumbnail(g.getImage().getThumbUrl());		
		} else {
			image = null;
		}
		updateThumbnail(image);
	}
	
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
