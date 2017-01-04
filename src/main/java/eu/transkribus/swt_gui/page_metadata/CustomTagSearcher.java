package eu.transkribus.swt_gui.page_metadata;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.TrpDbTag;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.search.CustomTagSearchFacets;
import eu.transkribus.core.model.beans.pagecontent.TextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.util.SebisStopWatch;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class CustomTagSearcher {
	private final static Logger logger = LoggerFactory.getLogger(CustomTagSearcher.class);
	
	static Storage s = Storage.getInstance();
	
	
//	public static List<CustomTag> searchOnCollection(int collectionId, String nameRegex, Map<String, Object> props, IProgressMonitor monitor) { 
//		throw new NotImplementedException("searching in collections not implemented yet!");
//	}
//	
//	public static List<CustomTag> searchOnRemoteDoc(int docId, String nameRegex, Map<String, Object> props, IProgressMonitor monitor) {
//		throw new NotImplementedException("searching on remote doc not implemented yet!");
//	}
	
	public static void searchOnCollection_WithoutIndex(int collId, List<TrpDbTag> result, CustomTagSearchFacets facets, IProgressMonitor monitor) throws NoConnectionException, SessionExpiredException, IllegalArgumentException {		
		Storage s = Storage.getInstance();
		s.checkConnection(true);
		TrpServerConn conn = Storage.getInstance().getConnection();
		
		if (collId <= 0)
			throw new IllegalArgumentException("No collection loaded!");
		
		monitor.setTaskName("Loading documents for collection "+collId);
		List<TrpDocMetadata> docs = conn.getAllDocs(collId);
		
		int nD = docs.size();
		if (monitor != null)
			monitor.beginTask("Searching for tags in documents of collection...", nD);

		int i=0;
		for (TrpDocMetadata dm : docs) {
			if (monitor != null && monitor.isCanceled())
				break;
			
			monitor.setTaskName("Doc "+(i+1)+"/"+nD+" - "+dm.getTitle()+" ("+dm.getDocId()+") ");
			
			TrpDoc d = conn.getTrpDoc(collId, dm.getDocId(), 1);
				
			searchOnDoc_WithoutIndex(result, collId, d, facets, 0, 0, 0, false, 0, false, monitor, true);

			++i;
			if (monitor != null)
				monitor.worked(i);
		}
	}
	
	// TODO: search on doc with index???
	public static void searchOnDoc_WithoutIndex(List<TrpDbTag> result, int collId, TrpDoc doc, CustomTagSearchFacets facets, int startPageIndex, int startRegionIndex, int startLineIndex, boolean stopOnFirst, int startOffset, boolean previous, IProgressMonitor monitor, boolean onlyMonitorSubTask) { 		
//		int nP = doc.getNPages()-startPageIndex;
		int nP = previous ? startPageIndex+1 : doc.getNPages()-startPageIndex;

		if (monitor != null && !onlyMonitorSubTask)
			monitor.beginTask("Searching in document "+doc.getMd().getTitle(), nP);
		
		List<TrpPage> pages = doc.getPages();
		int inc = previous ? -1 : 1;
		
		if (startPageIndex == -1) {
			startPageIndex = previous ? pages.size()-1 : 0;
		}
		
		int c=0;
		for (int i=startPageIndex; previous && i>=0 || !previous && i<pages.size(); i+=inc) {
			if (monitor != null && monitor.isCanceled())
				break;
			
			TrpPage p = pages.get(i);
			logger.debug("processing page "+i);
			if (i != startPageIndex) { // // vely impoltant
				startRegionIndex = -1;
				startLineIndex = -1;
				startOffset = -1;		
			}
			
			if (monitor != null)
				monitor.subTask("Page "+(c+1)+"/"+nP);
			
//			if (monitor != null && onlyMonitorSubTask)
//				monitor.subTask("Page "+(c+1)+"/"+nP);
//			else if (monitor!=null && !onlyMonitorSubTask)
//				monitor.setTaskName("Page "+(c+1)+"/"+nP);
			
			try {
				SebisStopWatch.SW.start();
				TrpPageType pt = s.getOrBuildPage(p.getCurrentTranscript(), false);
//				TrpPageType pt = TrpPageTranscriptBuilder.build(p.getCurrentTranscript()).getPage();
				SebisStopWatch.SW.stop(true, "time for unmarshal: ", logger);
				
				SebisStopWatch.SW.start();
				searchOnPage(result, collId, doc.getId(), pt, facets, startRegionIndex, startLineIndex, stopOnFirst, startOffset, previous);
				if (!result.isEmpty() && stopOnFirst)
					break;
				SebisStopWatch.SW.stop(true, "time for searching: ", logger);
				
			} catch (Exception e) {
				logger.error("Error searching tags on page "+(i+1)+": "+e.getMessage(), e);
			}
			
			++c;
			if (monitor != null && !onlyMonitorSubTask)
				monitor.worked(c);
		}
	}
	
	public static void searchOnPage(List<TrpDbTag> result, int collId, int docid, TrpPageType p, CustomTagSearchFacets facets, int startRegionIndex, int startLineIndex, boolean stopOnFirst, int startOffset, boolean previous) {
		if (p==null)
			return;
		
		SebisStopWatch.SW.start();
		List<TrpTextRegionType> regions = p.getTextRegions(true);
		SebisStopWatch.SW.stop(true, "time for getting regions: ", logger);
		
		if (startRegionIndex == -1) {
			startRegionIndex = previous ? regions.size()-1 : 0;
		}
		
		logger.debug("searching on page, region = "+startRegionIndex+" line = "+startLineIndex);
		
		int inc = previous ? -1 : 1;
		for (int i=startRegionIndex; previous && i>=0 || !previous && i<regions.size(); i+=inc) {
			TrpTextRegionType r = regions.get(i);
			if (i != startRegionIndex) { // vely impoltant
				startLineIndex = -1;
				startOffset = -1;
			}

			searchOnRegion(result, collId, docid, p.getMd().getPageId(), p.getMd().getTsId(), r, facets, startLineIndex, stopOnFirst, startOffset, previous);
			if (!result.isEmpty() && stopOnFirst)
				break;
		}
	}
	
	public static void searchOnRegion(List<TrpDbTag> result, int collId, int docid, int pid, int tid, TrpTextRegionType region, CustomTagSearchFacets facets, int startLineIndex, boolean stopOnFirst, int startOffset, boolean previous) {
		List<TextLineType> lines = region.getTextLine();
//		if (startLineIndex<0 || startLineIndex>=lines.size())
//			return;
		
//		result.collId = collId;
		
		if (startLineIndex == -1) {
			startLineIndex = previous ? lines.size()-1 : 0;
		}
		logger.debug("searching on region, line = "+startLineIndex);
		
		
		int inc = previous ? -1 : 1;
		for (int i=startLineIndex; previous && i>=0 || !previous && i<lines.size(); i+=inc) {
			TextLineType l = lines.get(i);
			TrpTextLineType tl = (TrpTextLineType) l;
			if (i != startLineIndex) // // vely impoltant
				startOffset = -1;
			
			List<CustomTag> lineTags = tl.getCustomTagList().findTags((CustomTagSearchFacets) facets, stopOnFirst, startOffset, previous);
//			if (facets instanceof TextSearchFacets) {
//				lineTags = tl.getCustomTagList().findText((TextSearchFacets) facets, stopOnFirst, startOffset, previous);
//			} else if (facets instanceof CustomTagSearchFacets) {
//				lineTags = tl.getCustomTagList().findTags((CustomTagSearchFacets) facets, stopOnFirst, startOffset, previous);
//			} else {
//				throw new RuntimeException("Unknown facets type: "+facets.getClass().getCanonicalName());
//			}
			
			List<TrpDbTag> lineTagsDb = new ArrayList<>();
			for (CustomTag t : lineTags) {
				lineTagsDb.add(fromCustomTag(collId, docid, pid, tid, t));
			}
			
			result.addAll(lineTagsDb);
			
			if (!result.isEmpty() && stopOnFirst)
				break;			
		}
	}
	
	public static TrpDbTag fromCustomTag(int collId, int docid, int pid, int tid, CustomTag t) {
		TrpDbTag dbTag = new TrpDbTag();

		dbTag.setCustomTagCss(t.getCssStr());
		
		dbTag.setCollId(collId);
		dbTag.setDocid(docid);
		dbTag.setPageid(pid);
		dbTag.setTsid(tid);
		
		if (t.getCustomTagList() != null) {
			dbTag.setRegionid(t.getCustomTagList().getShape().getId());
		}
		
		return dbTag;
	}
	
}
