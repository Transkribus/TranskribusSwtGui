package eu.transkribus.swt_gui.htr.treeviewer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.viewers.IStructuredSelection;

import java.util.Map.Entry;

import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpGroundTruthPage;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionContentProvider;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionLabelProvider;

public class DataSetSelectionController {

	private final CollectionContentProvider docContentProvider;
	private final CollectionLabelProvider docLabelProvider;
	private final HtrGroundTruthContentProvider htrGtContentProvider;
	private final HtrGroundTruthLabelProvider htrGtLabelProvider;
	
	//maps containing current selection. Maybe handling becomes less complex if this solely handled in table?
	private Map<TrpDocMetadata, List<TrpPage>> trainDocMap, testDocMap;
	private Map<TrpHtr, List<TrpGroundTruthPage>> trainGtMap, testGtMap;
	private final DataSetSelectionSashForm view;
	
	public DataSetSelectionController(DataSetSelectionSashForm view) {
		trainDocMap = new TreeMap<>();
		testDocMap = new TreeMap<>();
		trainGtMap = new TreeMap<>();
		testGtMap = new TreeMap<>();
		docContentProvider = new CollectionContentProvider();
		docLabelProvider = new CollectionLabelProvider();
		htrGtContentProvider = new HtrGroundTruthContentProvider();
		htrGtLabelProvider = new HtrGroundTruthLabelProvider();
		this.view = view;
	}
	
	public void addDocumentSelectionToTrainSet(IStructuredSelection selection) {
		addDocumentSelectionToDataMap(selection, trainDocMap, testDocMap);
		updateView();
	}


	public void addDocumentSelectionToValidationSet(IStructuredSelection selection) {
		addDocumentSelectionToDataMap(selection, testDocMap, trainDocMap);
		updateView();
	}

	public void removeFromTrainSetSelection(List<IDataSetEntry<Object, Object>> entries) {
		if (entries == null || entries.isEmpty()) {
			return;
		}
		for (IDataSetEntry<Object, Object> entry : entries) {
			trainDocMap.remove(entry.getDoc());
		}
		updateView();
	}


	public void removeFromTestSetSelection(List<IDataSetEntry<Object, Object>> entries) {
		if (entries == null || entries.isEmpty()) {
			return;
		}
		for (IDataSetEntry<Object, Object> entry : entries) {
			testDocMap.remove(entry.getDoc());
		}
		updateView();
	}
	
	private void updateView() {
		view.updateTrainSetTable(trainDocMap);
		view.updateValidationSetTable(testDocMap);
		view.updateDocTvColors(trainDocMap, testDocMap);
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
	
	public Map<TrpHtr, List<TrpGroundTruthPage>> getTrainGtMap() {
		return trainGtMap;
	}
	
	public Map<TrpHtr, List<TrpGroundTruthPage>> getTestGtMap() {
		return testGtMap;
	}
	
	public DataSetMetadata getTrainSetMetadata() {
		return computeDataSetSize(getTrainDocMap());
	}
	
	public DataSetMetadata getTestSetMetadata() {
		return computeDataSetSize(getTestDocMap());
	}
}
