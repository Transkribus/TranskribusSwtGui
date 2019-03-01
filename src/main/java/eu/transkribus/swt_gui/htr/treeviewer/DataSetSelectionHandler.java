package eu.transkribus.swt_gui.htr.treeviewer;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.swt.util.ImgLoader;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionContentProvider;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionLabelProvider;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSet;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class DataSetSelectionHandler {
	private static final Logger logger = LoggerFactory.getLogger(DataSetSelectionHandler.class);
	
	private final CollectionContentProvider docContentProvider;
	private final CollectionLabelProvider docLabelProvider;
	private final HtrGroundTruthContentProvider htrGtContentProvider;
	private final HtrGroundTruthLabelProvider htrGtLabelProvider;
	
	//maps containing current selection. Maybe handling becomes less complex if this solely handled in table?
	private Map<TrpDocMetadata, List<TrpPage>> trainDocMap, testDocMap;
	private Map<HtrGtDataSet, List<TrpGroundTruthPage>> trainGtMap, testGtMap;
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
		docLabelProvider = new CollectionLabelProvider();
		htrGtContentProvider = new HtrGroundTruthContentProvider(colId);
		htrGtLabelProvider = new HtrGroundTruthLabelProvider();
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

	public void removeSelectionFromTrainSet(List<IDataSetEntry<Object, Object>> entries) {
		if (entries == null || entries.isEmpty()) {
			return;
		}
		for (IDataSetEntry<?, ?> entry : entries) {
			if(entry instanceof DocumentDataSetEntry) {
				trainDocMap.remove(((DocumentDataSetEntry)entry).getDoc());
			} else if (entry instanceof HtrGroundTruthDataSetEntry) {
				
				//FIXME do I really need two types for this?
				HtrGroundTruthDataSetEntry e = (HtrGroundTruthDataSetEntry)entry;
				HtrGtDataSet key = new HtrGtDataSet(e.getDoc(), e.getGtSetType());
				trainGtMap.remove(key);
			}
		}
		updateView();
	}


	public void removeSelectionFromTestSet(List<IDataSetEntry<Object, Object>> entries) {
		if (entries == null || entries.isEmpty()) {
			return;
		}
		for (IDataSetEntry<?, ?> entry : entries) {
			if(entry instanceof DocumentDataSetEntry) {
				testDocMap.remove(((DocumentDataSetEntry)entry).getDoc());
			} else if (entry instanceof HtrGroundTruthDataSetEntry) {
				
				//FIXME see FIXME above. And the names are ambiguous
				HtrGroundTruthDataSetEntry e = (HtrGroundTruthDataSetEntry)entry;
				HtrGtDataSet key = new HtrGtDataSet(e.getDoc(), e.getGtSetType());
				testGtMap.remove(key);
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
	
	private List<IDataSetEntry<?, ?>> createTableEntries(Map<TrpDocMetadata, List<TrpPage>> docMap, 
		Map<HtrGtDataSet, List<TrpGroundTruthPage>> gtMap) {
		List<IDataSetEntry<?, ?>> list = new ArrayList<>(docMap.entrySet().size() + gtMap.entrySet().size());
		for (Entry<TrpDocMetadata, List<TrpPage>> entry : docMap.entrySet()) {
			list.add(new DocumentDataSetEntry(entry.getKey(), entry.getValue()));
		}
		for (Entry<HtrGtDataSet, List<TrpGroundTruthPage>> entry : gtMap.entrySet()) {
			list.add(new HtrGroundTruthDataSetEntry(entry.getKey().getHtr(), entry.getKey().getSetType(), entry.getValue()));
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
				Object[] pageObjArr = docContentProvider.getChildren(docMd);
				List<TrpPage> pageList = new LinkedList<>();
				for (Object page : pageObjArr) {
					pageList.add((TrpPage) page);
				}
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
			Map<HtrGtDataSet, List<TrpGroundTruthPage>> targetDataMap, 
			Map<HtrGtDataSet, List<TrpGroundTruthPage>> nonIntersectingDataMap) {
		Iterator<?> it = selection.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof HtrGtDataSet) {
				HtrGtDataSet docMd = (HtrGtDataSet) o;
				Object[] pageObjArr = htrGtContentProvider.getChildren(docMd);
				List<TrpGroundTruthPage> pageList = new LinkedList<>();
				for (Object page : pageObjArr) {
					pageList.add((TrpGroundTruthPage) page);
				}
				targetDataMap.put(docMd, pageList);

				if (nonIntersectingDataMap.containsKey(docMd)) {
					nonIntersectingDataMap.remove(docMd);
				}
			} else if (o instanceof HtrGtDataSet) {
//				HtrGtDataSet p = (HtrGtDataSet) o;
//				TrpHtr parent = p.getHtr();
//				if (targetDataMap.containsKey(parent) && !targetDataMap.get(parent).contains(p)) {
//					targetDataMap.get(parent).add(p);
//				} else if (!targetDataMap.containsKey(parent)) {
//					List<TrpPage> pageList = new LinkedList<>();
//					pageList.add(p);
//					targetDataMap.put(parent, pageList);
//				}
//
//				if (nonIntersectingDataMap.containsKey(parent) && nonIntersectingDataMap.get(parent).contains(p)) {
//					if (nonIntersectingDataMap.get(parent).size() == 1) {
//						nonIntersectingDataMap.remove(parent);
//					} else {
//						nonIntersectingDataMap.get(parent).remove(p);
//					}
//				}
			} else if (o instanceof TrpGroundTruthPage) {
//				TrpGroundTruthPage p = (TrpGroundTruthPage) o;
//				TrpDocMetadata parent = (TrpDocMetadata) htrGtContentProvider.getParent(p);
//				if (targetDataMap.containsKey(parent) && !targetDataMap.get(parent).contains(p)) {
//					targetDataMap.get(parent).add(p);
//				} else if (!targetDataMap.containsKey(parent)) {
//					List<TrpPage> pageList = new LinkedList<>();
//					pageList.add(p);
//					targetDataMap.put(parent, pageList);
//				}
//
//				if (nonIntersectingDataMap.containsKey(parent) && nonIntersectingDataMap.get(parent).contains(p)) {
//					if (nonIntersectingDataMap.get(parent).size() == 1) {
//						nonIntersectingDataMap.remove(parent);
//					} else {
//						nonIntersectingDataMap.get(parent).remove(p);
//					}
//				}
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
	
	public Map<HtrGtDataSet, List<TrpGroundTruthPage>> getTrainGtMap() {
		return trainGtMap;
	}
	
	public Map<HtrGtDataSet, List<TrpGroundTruthPage>> getTestGtMap() {
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
