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
import eu.transkribus.core.util.DescriptorUtils.AGtDataSet;
import eu.transkribus.core.util.JaxbUtils;
import eu.transkribus.core.util.PageTranscriptSelector;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionContentProvider;
import eu.transkribus.swt_gui.htr.DataSetMetadata;
import eu.transkribus.swt_gui.htr.treeviewer.DataSetSelectionSashForm.VersionComboStatus;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.AGtDataSetElement;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSetElement;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

/**
 * Controller for the DataSetSelectionSashform widget.
 * <br><br>
 * TODO open issues:
 * <br><br>
 * - Label- and ContentProvider for tree viewer is not yet generic<br>
 * - implicit inclusion of GT pages is not displayed in the tree on HTR and HtrGtDataSet level but only on page level. <br>
 * 
 * @author philip
 *
 */
public class DataSetSelectionController {
	private static final Logger logger = LoggerFactory.getLogger(DataSetSelectionController.class);
	
	/*
	 * maps containing current selection. 
	 * Maybe handling becomes less complex if this solely handled in table? But then again accessing the data in table viewers is less convenient...
	 */
	private Map<TrpDocMetadata, List<TrpPage>> trainDocMap, valDocMap;
	private Map<AGtDataSet<?>, List<AGtDataSetElement<?>>> trainGtMap, valGtMap;
	
	private final DataSetSelectionSashForm view;
	
	/**
	 * The collectionId the view was started in.
	 */
	private final int colId;
	
	/**
	 * TrainDataValidator is responsible for picking a transcript from a page's version history and determining
	 * if the transcript has content to be trained on.
	 */
	private final PageTranscriptSelector selector;
	
	boolean SHOW_DEBUG_DIALOG = false;
	DebugDialog diag = null;
	
	private VersionComboStatus transcriptVersionToUse = VersionComboStatus.Latest;
	
	/**
	 * Encapsulate logic for handling the selection of distinct datasets upon user selection and updating the view accordingly.
	 * 
	 * @param colId
	 * @param view
	 */
	public DataSetSelectionController(final int colId, DataSetSelectionSashForm view) {
		trainDocMap = new TreeMap<>();
		valDocMap = new TreeMap<>();
		trainGtMap = new TreeMap<>();
		valGtMap = new TreeMap<>();
		this.view = view;
		this.colId = colId;
		
		//this should be exchangeable at some point
		selector = new HtrTrainDataSelector();
	}
	
	public void addDocumentSelectionToTrainSet() {
		String infoLabelText = addDocumentSelectionToDataMap((IStructuredSelection) view.docTv.getSelection(), trainDocMap, valDocMap);
		updateView(infoLabelText);
	}

	public void addDocumentSelectionToValidationSet() {
		String infoLabelText = addDocumentSelectionToDataMap((IStructuredSelection) view.docTv.getSelection(), valDocMap, trainDocMap);
		updateView(infoLabelText);
	}
	
	public void addGtSelectionToTrainSet() {
		String infoLabelText = addGtSelectionToDataMap((IStructuredSelection) view.groundTruthTv.getSelection(), trainGtMap, valGtMap);
		updateView(infoLabelText);
	}
	
	public void addGtSelectionToValidationSet() {
		String infoLabelText = addGtSelectionToDataMap((IStructuredSelection) view.groundTruthTv.getSelection(), valGtMap, trainGtMap);
		updateView(infoLabelText);
	}

	public void removeSelectionFromTrainSet(List<IDataSelectionEntry<?, ?>> entries) {
		if (entries == null || entries.isEmpty()) {
			return;
		}
		for (IDataSelectionEntry<?, ?> entry : entries) {
			if(entry instanceof DocumentDataSelectionEntry) {
				trainDocMap.remove(((DocumentDataSelectionEntry)entry).getDoc());
			} else if (entry instanceof GroundTruthDataSelectionEntry) {
				trainGtMap.remove(((GroundTruthDataSelectionEntry)entry).getDoc());
			}
		}
		updateView();
	}


	public void removeSelectionFromValSet(List<IDataSelectionEntry<?, ?>> entries) {
		if (entries == null || entries.isEmpty()) {
			return;
		}
		for (IDataSelectionEntry<?, ?> entry : entries) {
			if(entry instanceof DocumentDataSelectionEntry) {
				valDocMap.remove(((DocumentDataSelectionEntry)entry).getDoc());
			} else if (entry instanceof GroundTruthDataSelectionEntry) {
				valGtMap.remove(((GroundTruthDataSelectionEntry)entry).getDoc());
			}
		}
		updateView();
	}
	
	/**
	 * Remove all GT from selection, only keep documents. Needed when switching to T2I configuration.
	 */
	public void removeAllGtFromSelection() {
		trainGtMap.clear();
		valGtMap.clear();
		updateView();		
	}
	
	private void updateView() {
		updateView(null);
	}
	
	private void updateView(String infoLabelText) {
		view.trainSetOverviewTable.setInput(createTableEntries(trainDocMap, trainGtMap));
		view.valSetOverviewTable.setInput(createTableEntries(valDocMap, valGtMap));
		view.updateDocTvColors();
		view.updateGtTvColors();
		view.updateInfoLabel(infoLabelText);
		
		if(SHOW_DEBUG_DIALOG) {
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
		Map<AGtDataSet<?>, List<AGtDataSetElement<?>>> gtMap) {
		List<IDataSelectionEntry<?, ?>> list = new ArrayList<>(docMap.entrySet().size() + gtMap.entrySet().size());
		for (Entry<TrpDocMetadata, List<TrpPage>> entry : docMap.entrySet()) {
			
			//filter pages that match the current selection in VersionComboStatus
			List<TrpPage> pagesMatchingVersionComboStatus = entry.getValue().stream()
					.filter(p -> selector.isQualifiedForTraining(p, getTranscriptVersionToUse().getStatus()))
					.collect(Collectors.toList());
			
			list.add(new DocumentDataSelectionEntry(entry.getKey(), pagesMatchingVersionComboStatus));
		}
		for (Entry<AGtDataSet<?>, List<AGtDataSetElement<?>>> entry : gtMap.entrySet()) {
			list.add(new GroundTruthDataSelectionEntry(entry.getKey(), entry.getValue()));
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
	protected String addDocumentSelectionToDataMap(IStructuredSelection selection,
			Map<TrpDocMetadata, List<TrpPage>> targetDataMap, 
			Map<TrpDocMetadata, List<TrpPage>> nonIntersectingDataMap) {
		int nrOfEmptyItemsOmitted = 0;
		int nrOfItemsOmitted = 0;
		Iterator<?> it = selection.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof TrpDocMetadata) {
				TrpDocMetadata docMd = (TrpDocMetadata) o;
				TrpPage[] pageObjArr = ((CollectionContentProvider)view.docTv.getContentProvider()).getChildren(docMd);
				
				final int originalPageSelectionSize = pageObjArr.length;
				//remove overlap  with selection
				logger.debug("Adding pages to selection: " + originalPageSelectionSize);
				
				//filter for pages that e.g. contain transcribed lines
				List<TrpPage> pageList = Arrays.stream(pageObjArr)
						.filter(p -> selector.isQualifiedForTraining(p, getTranscriptVersionToUse().getStatus()))
						.collect(Collectors.toList());
				
				final int nrOfTranscribedPagesInSelection = pageList.size();
				logger.debug("Filtered pages with transcribed lines. Remaining: " + nrOfTranscribedPagesInSelection);
				nrOfEmptyItemsOmitted += originalPageSelectionSize - nrOfTranscribedPagesInSelection;
				
				//filter for elements that are not included via other selected GT sets
				pageList = pageList.stream()
						.filter(p -> !isPageInSelection(p))
						.collect(Collectors.toList());
				
				logger.debug("Filtered already included pages. Remaining: " + pageList.size());
				nrOfItemsOmitted += nrOfTranscribedPagesInSelection - pageList.size();
				
				//check for images already in the selection and ask user which ones to use
				List<TrpPage> pagesToAdd = detectAndResolveConflicts(docMd, pageList);
				if(pagesToAdd.isEmpty()) {
					nrOfItemsOmitted += nrOfTranscribedPagesInSelection;
					continue;
				}
				
				targetDataMap.put(docMd, pagesToAdd);
				if (nonIntersectingDataMap.containsKey(docMd)) {
					nonIntersectingDataMap.remove(docMd);
				}
			} else if (o instanceof TrpPage) {
				TrpPage p = (TrpPage) o;
				TrpDocMetadata parent = (TrpDocMetadata) ((CollectionContentProvider)view.docTv.getContentProvider()).getParent(p);
				
				//omit if this page has no transcribed lines
				if(!selector.isQualifiedForTraining(p, getTranscriptVersionToUse().getStatus())) {
					nrOfEmptyItemsOmitted++;
					continue;
				}
				
				//omit if this page is already included
				if(isPageInSelection(p)) {
					nrOfItemsOmitted++;
					continue;
				}
				
				TrpPage pageToAdd = detectAndResolveConflicts(parent, p);
				if(pageToAdd == null) {
					nrOfItemsOmitted++;
					continue;
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
		return getPageItemsOmittedMessage(nrOfItemsOmitted, nrOfEmptyItemsOmitted);
	}

	/**
	 * Add selected items to the targetDataMap and remove them from the nonIntersectingDataMap if included.
	 * <br><br>
	 * FIXME: the migration of existing train docs revealed that there is a large amount of HTR "ground truth" that actually contains no transcription.
	 * This data should be deleted from the database at some point and HTR_GROUND_TRUTH.PAGE_NR has to be reassigned then.
	 * So I don't take effort to handle that here. However, "empty" pages are filtered out when document data is added so new ground truth without text is not produced.
	 *  
	 * @param selection
	 * @param targetDataMap
	 * @param nonIntersectingDataMap
	 * @return message for display to user with information on issues with the given selection (e.g. is items where omitted due to inclusion). null if the whole selection was added.
	 */
	private String addGtSelectionToDataMap(IStructuredSelection selection,
			Map<AGtDataSet<?>, List<AGtDataSetElement<?>>> targetDataMap, 
			Map<AGtDataSet<?>, List<AGtDataSetElement<?>>> nonIntersectingDataMap) {
		int nrOfItemsOmitted = 0;
		Iterator<?> it = selection.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof TrpHtr) {
				TrpHtr htr = (TrpHtr) o;
				Object[] htrGtSets = ((HtrGroundTruthContentProvider)view.groundTruthTv.getContentProvider()).getChildren(htr);
				
				for (Object gtDataSet : htrGtSets) {
					AGtDataSet<?> htrGtDataSet = (AGtDataSet<?>)gtDataSet;
					nrOfItemsOmitted += addGtSetToDataMap(htrGtDataSet, targetDataMap, nonIntersectingDataMap);
				}
			} else if (o instanceof AGtDataSet<?>) {
				AGtDataSet<?> htrGtDataSet = (AGtDataSet<?>) o;
				nrOfItemsOmitted += addGtSetToDataMap(htrGtDataSet, targetDataMap, nonIntersectingDataMap);
			} else if (o instanceof AGtDataSetElement<?>) {
				AGtDataSetElement<?> p = (AGtDataSetElement<?>) o;
				AGtDataSet<?> parent = (AGtDataSet<?>) ((HtrGroundTruthContentProvider)view.groundTruthTv.getContentProvider()).getParent(p);
				
				//check if Ground Truth entity with this ID is already in selection and omit automatically
				if(!getGtSetsFromSelectionIncludingElement(p, false).isEmpty()) {
					//element already included via other set
					nrOfItemsOmitted++;
					continue;
				}
				
				//if another entity, using the same image is already included in selection, ask user to resolve conflict
				p = detectAndResolveConflicts(parent, p);
				if(p == null) {
					continue;
				}
				
				if (targetDataMap.containsKey(parent) && !targetDataMap.get(parent).contains(p)) {
					targetDataMap.get(parent).add(p);
				} else if (!targetDataMap.containsKey(parent)) {
					List<AGtDataSetElement<?>> pageList = new ArrayList<>();
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
		return getGtItemsOmittedMessage(nrOfItemsOmitted);
	}
	
	private String getGtItemsOmittedMessage(int nrOfItemsOmitted) {
		if(nrOfItemsOmitted < 1) {
			return null; 
		}
		return nrOfItemsOmitted + " pages are already included by other data sets. Expand items for details.";
	}
	
	private String getPageItemsOmittedMessage(int nrOfItemsOmitted, int nrOfItemsWithoutTextOmitted) {
		String msg = "";
		if(nrOfItemsWithoutTextOmitted > 0) {
			msg += nrOfItemsWithoutTextOmitted + " pages without transcription ignored.\n";
		}
		
		if(nrOfItemsOmitted > 0) {
			msg += nrOfItemsOmitted + " pages from selection were already included.";
		}
		return msg.trim();
	}

	private int addGtSetToDataMap(AGtDataSet<?> htrGtDataSet,
			Map<AGtDataSet<?>, List<AGtDataSetElement<?>>> targetDataMap, 
			Map<AGtDataSet<?>, List<AGtDataSetElement<?>>> nonIntersectingDataMap) {
		AGtDataSetElement<?>[] gtPageArr = (AGtDataSetElement<?>[])((HtrGroundTruthContentProvider)view.groundTruthTv.getContentProvider()).getChildren(htrGtDataSet);
		if(gtPageArr == null) {
			logger.error("No children could be determined for HTR GT set: " + htrGtDataSet);
			return 0;
		}
		final int gtSetSize = gtPageArr.length;
		logger.debug("Adding gt pages to selection: " + gtSetSize);
		
		//filter for elements that are not included via other selected GT sets
		List<AGtDataSetElement<?>> gtPageList = Arrays.stream(gtPageArr)
				.filter(e -> getGtSetsFromSelectionIncludingElement(e, false).isEmpty())
				.collect(Collectors.toList());
		logger.debug("Filtered already included gt pages. Remaining: " + gtPageList.size());
		
		if(gtPageList.isEmpty()) {
			//all GT entities already included
			return gtSetSize;
		}
		
		List<AGtDataSetElement<?>> gtPagesToAdd = detectAndResolveConflicts(htrGtDataSet, gtPageList);
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
	
	/**
	 * Retrieve all HtrGtDataSets from train set and validation set selection tables that include the given HtrGtDataSetElement.
	 * This is needed for detecting conflicts when adding new data to the selection (GT may be used in several models) and in 
	 * the GroundTruthTreeViewer's label provider for showing implicit inclusion of GT pages.
	 * 
	 * @param element the HtrGtDataSetElement to search for
	 * @param excludeOriginalSet if true then the result list will exclude the HtrGtDataSet where this element belongs to
	 * @return
	 */
	public List<AGtDataSet<?>> getGtSetsFromSelectionIncludingElement(AGtDataSetElement<?> element, boolean excludeOriginalSet) {
		List<AGtDataSet<?>> includedBySetList = new ArrayList<>();
		for(Entry<AGtDataSet<?>, List<AGtDataSetElement<?>>> e : getTrainGtMap().entrySet()) {
			AGtDataSet<?> gtSet = e.getKey();
			List<AGtDataSetElement<?>> gtPageList = e.getValue();
			logger.debug("Check if gtId = " + element.getGroundTruthPage().getGtId() + " is included with train set selection in HTR '" 
					+ gtSet.getName() + "' " + gtSet.getDataSetType().getLabel());
			
			if(excludeOriginalSet && gtSet.equals(element.getParentGtDataSet())) {
				logger.debug("Skipping original gt set as excludeOriginalSet = true");
				continue;
			}
			
			for(AGtDataSetElement<?> g : gtPageList) {
				logger.trace(element.getGroundTruthPage().getGtId() + " <-> " + g.getGroundTruthPage().getGtId());
				
				if(g.getGroundTruthPage().getGtId() == element.getGroundTruthPage().getGtId()) {
					logger.debug("Element already in trainset: " + element.getGroundTruthPage().getGtId());
					includedBySetList.add(e.getKey());
					break;
				}
			}
			/* use for loop with logging
			if(gtPageList.stream()
					.anyMatch(g -> g.getGroundTruthPage().getGtId() == element.getGroundTruthPage().getGtId()) 
					&& (excludeOriginalSet || !gtSet.equals(element.getParentHtrGtDataSet()))) {
				logger.debug("Element already in trainset: " + element.getGroundTruthPage().getGtId());
				includedBySetList.add(e.getKey());
			}
			*/
		}
		
		for(Entry<AGtDataSet<?>, List<AGtDataSetElement<?>>> e : getValGtMap().entrySet()) {
			AGtDataSet<?> gtSet = e.getKey();
			List<AGtDataSetElement<?>> gtPageList = e.getValue();
			logger.debug("Check if gtId  = " + element.getGroundTruthPage().getGtId() + " is included with validation set selection in '" 
					+ gtSet.getName() + "' " + gtSet.getDataSetType().getLabel());
			
			if(excludeOriginalSet && gtSet.equals(element.getParentGtDataSet())) {
				logger.debug("Skipping original gt set as excludeOriginalSet = true");
				continue;
			}
			for(AGtDataSetElement<?> g : gtPageList) {
				logger.trace(element.getGroundTruthPage().getGtId() + " <-> " + g.getGroundTruthPage().getGtId());
			
				if(g.getGroundTruthPage().getGtId() == element.getGroundTruthPage().getGtId()) {
					logger.debug("Element already in validationset: " + element.getGroundTruthPage().getGtId());
					includedBySetList.add(e.getKey());
					break;
				}
			}
			
			/* use for loop with logging
			if(gtPageList.stream()
				.anyMatch(g -> g.getGroundTruthPage().getGtId() == element.getGroundTruthPage().getGtId())) {
				logger.debug("Element already in validationset: " + element.getGroundTruthPage().getGtId());
				includedBySetList.add(e.getKey());
			}
			*/
		}
		return includedBySetList;
	}
	
	private boolean isPageInSelection(TrpPage page) {
		List<TrpPage> trainPages = trainDocMap.values().stream()
				.flatMap(List::stream)
				.collect(Collectors.toList());
		if(trainPages.stream().anyMatch(p -> p.getPageId() == page.getPageId())) {
			return true;
		}
	
		List<TrpPage> valPages = valDocMap.values().stream()
				.flatMap(List::stream)
				.collect(Collectors.toList());
		if(valPages.stream().anyMatch(p -> p.getPageId() == page.getPageId())) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Find implicit overlap by image IDs in data to be added to and data already in the selection.
	 * User is asked how he wants to resolve any conflict.
	 * 
	 * @param docMd
	 * @param page
	 * @return
	 */
	private TrpPage detectAndResolveConflicts(TrpDocMetadata docMd, TrpPage page) {
		List<TrpPage> pagesToAdd = detectAndResolveConflicts(docMd, Arrays.asList(new TrpPage[]{page}));
		if(pagesToAdd.isEmpty()) {
			return null;
		}
		return pagesToAdd.get(0);
	}
	
	/**
	 * Find implicit overlap by image IDs in data to be added to and data already in the selection.
	 * User is asked how he wants to resolve any conflict.
	 * 
	 * @param docMd
	 * @param pageList
	 * @return
	 */
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
	
	/**
	 * Find implicit overlap by image IDs in data to be added to and data already in the selection.
	 * User is asked how he wants to resolve any conflict.
	 * 
	 * @param gtSet
	 * @param gt
	 * @return
	 */
	private AGtDataSetElement<?> detectAndResolveConflicts(AGtDataSet<?> gtSet, AGtDataSetElement<?> gt) {
		List<AGtDataSetElement<?>> gtToAdd = detectAndResolveConflicts(gtSet, Arrays.asList(new AGtDataSetElement<?>[]{gt}));
		if(gtToAdd.isEmpty()) {
			return null;
		}
		return gtToAdd.get(0);
	}
	
	/**
	 * Find implicit overlap by image IDs in data to be added to and data already in the selection.
	 * User is asked how he wants to resolve any conflict.
	 * 
	 * @param gtSet
	 * @param gtList
	 * @return
	 */
	private List<AGtDataSetElement<?>> detectAndResolveConflicts(AGtDataSet<?> gtSet, List<AGtDataSetElement<?>> gtList) {
		List<AGtDataSetElement<?>> gtOverlapByImageId = findGtImageOverlapWithSelection(gtList);
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
	private List<AGtDataSetElement<?>> findGtImageOverlapWithSelection(List<AGtDataSetElement<?>> gtList) {
		List<Integer> imageIds = getAllImageIdsInSelection();
		//return overlap
		return gtList.stream()
				.filter(g -> imageIds.contains(g.getGroundTruthPage().getImageId()))
				.collect(Collectors.toList());
	}
	
	private List<Integer> getAllImageIdsInSelection() {
		List<Integer> imageIds = new ArrayList<>(getNrOfPagesInSelection());
		imageIds.addAll(extractImageIdsFromGtSelection(trainGtMap));
		imageIds.addAll(extractImageIdsFromGtSelection(valGtMap));
		imageIds.addAll(extractImageIdsFromDocSelection(trainDocMap));
		imageIds.addAll(extractImageIdsFromDocSelection(valDocMap));
		return imageIds;
	}
	
	private List<Integer> extractImageIdsFromGtSelection(Map<AGtDataSet<?>, List<AGtDataSetElement<?>>> map) {
		//collect all included imageIds
		List<Integer> imageIdsInGt = new ArrayList<>();
		for(Entry<AGtDataSet<?>, List<AGtDataSetElement<?>>> e : map.entrySet()) {
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
		removeGtFromSelectionByImageId(imageIds, valGtMap);
		removePagesFromSelectionByImageId(imageIds, trainDocMap);
		removePagesFromSelectionByImageId(imageIds, valDocMap);
	}
	
	/**
	 * remove overlap from selected documents. GT entities are not checked here as they have no versioning, are unambiguous and can be de-duplicated automatically.
	 * 
	 * @param overlap
	 */
	private void removeOverlapFromSelectionByGt(List<AGtDataSetElement<?>> overlap) {
		List<Integer> imageIds = overlap.stream().map(g -> g.getGroundTruthPage().getImageId()).collect(Collectors.toList());
		removeGtFromSelectionByImageId(imageIds, trainGtMap);
		removeGtFromSelectionByImageId(imageIds, valGtMap);
		removePagesFromSelectionByImageId(imageIds, trainDocMap);
		removePagesFromSelectionByImageId(imageIds, valDocMap);
	}

	private void removeGtFromSelectionByImageId(List<Integer> imageIds,
			Map<AGtDataSet<?>, List<AGtDataSetElement<?>>> map) {
		Iterator<Entry<AGtDataSet<?>, List<AGtDataSetElement<?>>>> setIt = map.entrySet().iterator();
		while(setIt.hasNext()) {
			Entry<AGtDataSet<?>, List<AGtDataSetElement<?>>> set = setIt.next();
			Iterator<AGtDataSetElement<?>> it = set.getValue().iterator();
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
		count += valDocMap.entrySet().stream().mapToInt(e -> e.getValue().size()).sum();
		count += trainGtMap.entrySet().stream().mapToInt(e -> e.getValue().size()).sum();
		count += valGtMap.entrySet().stream().mapToInt(e -> e.getValue().size()).sum();
		logger.debug("Nr of pages in selection: " + count);
		return count;
	}
	
	/**
	 * Summarize the number of lines and words for the given data map.
	 * 
	 * @param map with selected documents and pages respectively
	 * @return a {@link DataSetMetadata} object with statistics
	 */
	private DataSetMetadata computeDataSetSize(Map<TrpDocMetadata, List<TrpPage>> map) {
		EditStatus status = view.getVersionComboStatus().getStatus();
		logger.debug("Computing data set size with version selection = {}", status);
		int pages = 0;
		int lines = 0;
		int words = 0;
		for (Entry<TrpDocMetadata, List<TrpPage>> e : map.entrySet()) {
			for (TrpPage p : e.getValue()) {
				TrpTranscriptMetadata tmd = p.getCurrentTranscript();
				if (status != null) {
					for (TrpTranscriptMetadata t : p.getTranscripts()) {
						if (t.getStatus().equals(status)) {
							tmd = t;
							break;
						}
					}
				}
				pages++;
				lines += tmd.getNrOfTranscribedLines();
				words += tmd.getNrOfWordsInLines();
			}
		}
		return new DataSetMetadata("Document Data", pages, lines, words);
	}
	
	private DataSetMetadata computeGtDataSetSize(Map<AGtDataSet<?>, List<AGtDataSetElement<?>>> map) {
		int pages = 0;
		int lines = 0;
		int words = 0;
		for (Entry<AGtDataSet<?>, List<AGtDataSetElement<?>>> e : map.entrySet()) {
			for (AGtDataSetElement<?> el : e.getValue()) {
				pages++;
				lines += el.getGroundTruthPage().getNrOfTranscribedLines();
				words += el.getGroundTruthPage().getNrOfWordsInLines();
			}
		}
		return new DataSetMetadata("Ground Truth Data", pages, lines, words);
	}
	
	Map<TrpDocMetadata, List<TrpPage>> getTrainDocMap() {
		return trainDocMap;
	}
	
	Map<TrpDocMetadata, List<TrpPage>> getValDocMap() {
		return valDocMap;
	}
	
	Map<AGtDataSet<?>, List<AGtDataSetElement<?>>> getTrainGtMap() {
		return trainGtMap;
	}
	
	Map<AGtDataSet<?>, List<AGtDataSetElement<?>>> getValGtMap() {
		return valGtMap;
	}
	
	public List<DataSetMetadata> getTrainSetMetadata() {
		DataSetMetadata trainDocMd = computeDataSetSize(getTrainDocMap());
		DataSetMetadata trainGtMd = computeGtDataSetSize(getTrainGtMap());
		return Arrays.asList(new DataSetMetadata[] { trainDocMd, trainGtMd });
	}
	
	public List<DataSetMetadata> getValSetMetadata() {
		DataSetMetadata valDocMd = computeDataSetSize(getValDocMap());
		DataSetMetadata valGtMd = computeGtDataSetSize(getValGtMap());
		return Arrays.asList(new DataSetMetadata[] { valDocMd, valGtMd });
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

	public void loadPageInMainWidget(TrpPage p) {
		TrpLocation loc = new TrpLocation();
		loc.collId = colId;
		loc.docId = p.getDocId();
		loc.pageNr = p.getPageNr();
		TrpMainWidget.getInstance().showLocation(loc);
	}

	public DataSetSelection getSelection() {
		
		//FIXME activate this method once the training job can handle such descriptors
//		config.setTrain(DescriptorUtils.buildSelectionDescriptorList(trainDocMap, status));
//		config.setTest(DescriptorUtils.buildSelectionDescriptorList(testDocMap, status));
	
		List<DocumentSelectionDescriptor> trainDocDescs = DescriptorUtils.buildSelectionDescriptorList(getTrainDocMap(), selector, getTranscriptVersionToUse().getStatus());
		List<DocumentSelectionDescriptor> validationDocDescs = DescriptorUtils.buildSelectionDescriptorList(getValDocMap(), selector, getTranscriptVersionToUse().getStatus());
		
		//build the GT descriptor
		List<GroundTruthSelectionDescriptor> trainGtDescs = buildGtSelectionDescriptorList(getTrainGtMap());
		List<GroundTruthSelectionDescriptor> validationGtDescs = buildGtSelectionDescriptorList(getValGtMap());
		
		return new DataSetSelection(trainDocDescs, validationDocDescs, trainGtDescs, validationGtDescs);
	}
	
	/**
	 * Translate internal HTR specific representation into a generic one and build the descriptor using DescriptorUtils.
	 * 
	 * @param internalGtMap
	 * @return
	 */
	private List<GroundTruthSelectionDescriptor> buildGtSelectionDescriptorList(
			Map<AGtDataSet<?>, List<AGtDataSetElement<?>>> internalGtMap) {
		Map<AGtDataSet<?>, List<TrpGroundTruthPage>> gtMap = new HashMap<>();
		for(Entry<AGtDataSet<?>, List<AGtDataSetElement<?>>> e : internalGtMap.entrySet()) {
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
				DataSetSelection sel = getSelection();
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

	/**
	 * Depending on the versionComboStatus the display is updated but the selection remains the same. 
	 * If items would be discarded we are not able to restore them when the selection changes again.
	 * The ultimate selection is done when the DocumentSelectionDescriptor is built using the same TrainDataSelector.
	 * 
	 * @param versionComboStatus
	 */
	public void setTranscriptVersionToUse(VersionComboStatus versionComboStatus) {
		this.transcriptVersionToUse = versionComboStatus;
		updateView();
	}

	public VersionComboStatus getTranscriptVersionToUse() {
		return transcriptVersionToUse;
	}

	public TrpTranscriptMetadata getSelectedTranscriptForPage(TrpPage p) {
		return selector.selectTranscript(p, getTranscriptVersionToUse().getStatus());
	}

	public String getTrainDataSizeLabel(TrpPage p) {
		return selector.getTrainDataSizeLabel(p, getTranscriptVersionToUse().getStatus());
	}

	public boolean isQualifiedForTraining(TrpPage p) {
		return selector.isQualifiedForTraining(p, getTranscriptVersionToUse().getStatus());
	}
}
