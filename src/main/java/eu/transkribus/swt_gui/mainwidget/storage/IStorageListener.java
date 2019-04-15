package eu.transkribus.swt_gui.mainwidget.storage;

import java.util.ArrayList;
import java.util.List;

import eu.transkribus.core.model.beans.JAXBPageTranscript;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.auth.TrpUserLogin;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.util.Event;
import eu.transkribus.swt_gui.canvas.CanvasImage;
import eu.transkribus.swt_gui.metadata.CustomTagSpec;
import eu.transkribus.swt_gui.metadata.StructCustomTagSpec;

public interface IStorageListener {

	default void handleMainImageLoadEvent(MainImageLoadEvent mile) {}
	
	default void handleTranscriptSaveEvent(TranscriptSaveEvent tse) {}
	
	default void handleCollectionsLoadEvent(CollectionsLoadEvent cle) {}
	
	default void handleJobUpdate(JobUpdateEvent jue) {}
	
	default void handleDocLoadEvent(DocLoadEvent dle) {}
	
	default void handleGroundTruthLoadEvent(GroundTruthLoadEvent gtle) {}
	
	default void handleTranscriptListLoadEvent(TranscriptListLoadEvent arg) {}

	default void handleTranscriptLoadEvent(TranscriptLoadEvent arg) {}
	
	default void handleLoginOrLogout(LoginOrLogoutEvent arg) {}

	default void handlePageLoadEvent(PageLoadEvent arg) {}
	
	default void handleDocMetadataUpdateEvent(DocMetadataUpdateEvent e) {}
	
	default void handleDocListLoadEvent(DocListLoadEvent e) {}
	
	default void handleUserDocListLoadEvent(UserDocListLoadEvent e) {}
	
	default void handleHtrListLoadEvent(HtrListLoadEvent e) {}
	
	default void handlTagSpecsChangedEvent(TagSpecsChangedEvent e) {}
	
	default void handlStructTagSpecsChangedEvent(StructTagSpecsChangedEvent e) {}
	
	default void handleEvent(Event event) {
		if (event instanceof JobUpdateEvent) {
			handleJobUpdate((JobUpdateEvent) event);
		}
		else if (event instanceof DocLoadEvent) {
			handleDocLoadEvent((DocLoadEvent) event);
		}
		else if (event instanceof GroundTruthLoadEvent) {
			handleGroundTruthLoadEvent((GroundTruthLoadEvent) event);
		}
		else if (event instanceof PageLoadEvent) {
			handlePageLoadEvent((PageLoadEvent) event);
		}
		else if (event instanceof TranscriptLoadEvent) {
			handleTranscriptLoadEvent((TranscriptLoadEvent) event);
		}
		else if (event instanceof TranscriptListLoadEvent) {
			handleTranscriptListLoadEvent((TranscriptListLoadEvent) event);
		}
		else if (event instanceof LoginOrLogoutEvent) {
			handleLoginOrLogout((LoginOrLogoutEvent) event);
		}
		else if (event instanceof CollectionsLoadEvent) {
			handleCollectionsLoadEvent((CollectionsLoadEvent) event);
		}
		else if (event instanceof DocMetadataUpdateEvent) {
			handleDocMetadataUpdateEvent((DocMetadataUpdateEvent) event);
		}
		else if (event instanceof TranscriptSaveEvent) {
			handleTranscriptSaveEvent((TranscriptSaveEvent) event);
		}
		else if (event instanceof MainImageLoadEvent) {
			handleMainImageLoadEvent((MainImageLoadEvent) event);
		}
		else if (event instanceof DocListLoadEvent) {
			handleDocListLoadEvent((DocListLoadEvent) event);
		}
		else if (event instanceof UserDocListLoadEvent) {
			handleUserDocListLoadEvent((UserDocListLoadEvent) event);
		}
		else if (event instanceof HtrListLoadEvent) {
			handleHtrListLoadEvent((HtrListLoadEvent) event);
		}
		else if (event instanceof TagSpecsChangedEvent) {
			handlTagSpecsChangedEvent((TagSpecsChangedEvent) event);
		}
		else if (event instanceof StructTagSpecsChangedEvent) {
			handlStructTagSpecsChangedEvent((StructTagSpecsChangedEvent) event);
		}
	}
	
	@SuppressWarnings("serial")
	public static class LoginOrLogoutEvent extends Event {
		public final boolean login;
		public TrpUserLogin user;
		public String serverUri;

		public LoginOrLogoutEvent(Object source, boolean login, TrpUserLogin user, String serverUri) {
			super(source, login ? "Login" : "Logout");
			this.login = login;
			this.user = user;
			this.serverUri = serverUri;
		}
	}
	
	@SuppressWarnings("serial")
	public static class TagSpecsChangedEvent extends Event {
		List<CustomTagSpec> tagSpecs;
		
		public TagSpecsChangedEvent(Object source, List<CustomTagSpec> tagSpecs) {
			super(source, "Tag specs changed");
			this.tagSpecs = tagSpecs;
		}
	}
	
	@SuppressWarnings("serial")
	public static class StructTagSpecsChangedEvent extends Event {
		List<StructCustomTagSpec> tagSpecs;
		
		public StructTagSpecsChangedEvent(Object source, List<StructCustomTagSpec> tagSpecs) {
			super(source, "Struct tag specs changed");
			this.tagSpecs = tagSpecs;
		}
	}

	@SuppressWarnings("serial")
	public static class JobUpdateEvent extends Event {
		public final TrpJobStatus job;
//		public boolean allJobsUpdated = false;

		public JobUpdateEvent(Object source, TrpJobStatus job) {
			super(source, "Job update");
			this.job = job;
		}
	}

	@SuppressWarnings("serial")
	public static class DocLoadEvent extends Event {
		public final TrpDoc doc;

		public DocLoadEvent(Object source, TrpDoc doc) {
			super(source, "Document loaded");
			this.doc = doc;
		}
	}
	
	@SuppressWarnings("serial")
	public static class GroundTruthLoadEvent extends Event {
		public final TrpDoc doc;

		public GroundTruthLoadEvent(Object source, TrpDoc doc) {
			super(source, "Ground Truth pages loaded");
			this.doc = doc;
		}
	}
	
	@SuppressWarnings("serial")
	public static class DocListLoadEvent extends Event {
		public final int collId;
		public final List<TrpDocMetadata> docs;
		/**
		 * true if this DocListLoadEvent was sent during a collection change
		 */
		public final boolean isCollectionChange;

		public DocListLoadEvent(Object source, int collId, List<TrpDocMetadata> docs, boolean isCollectionChange) {
			super(source, docs.size()+" documents loaded from collection "+collId);
			this.collId = collId;
			this.docs = docs;
			this.isCollectionChange = isCollectionChange;
		}
	}
	
	@SuppressWarnings("serial")
	public static class UserDocListLoadEvent extends Event {
		public final List<TrpDocMetadata> docs;

		public UserDocListLoadEvent(Object source, List<TrpDocMetadata> docs) {
			super(source, docs.size()+" user documents loaded from collection");
			this.docs = docs;
		}
	}
	
	@SuppressWarnings("serial")
	public static class HtrListLoadEvent extends Event {
		public final int collId;
		public final List<TrpHtr> htrs;

		public HtrListLoadEvent(Object source, int collId, List<TrpHtr> htrs) {
			super(source, htrs.size() + " HTRs loaded for collection " + collId);
			this.collId = collId;
			this.htrs = htrs;
		}
	}
	
	@SuppressWarnings("serial")
	public static class MainImageLoadEvent extends Event {
		public final CanvasImage image;

		public MainImageLoadEvent(Object source, CanvasImage image) {
			super(source, "Main image loaded");
			this.image = image;
		}
	}

	@SuppressWarnings("serial")
	public static class PageLoadEvent extends Event {
		public final TrpDoc doc;
		public final TrpPage page;

		public PageLoadEvent(Object source, TrpDoc doc, TrpPage page) {
			super(source, "Page loaded");
			this.doc = doc;
			this.page = page;
		}
	}
	
	@SuppressWarnings("serial")
	public static class TranscriptSaveEvent extends Event {
		public final int colId;
		public final TrpTranscriptMetadata md;

		public TranscriptSaveEvent(Object source, int colId, TrpTranscriptMetadata md) {
			super(source, "Transcript saved");
			this.colId = colId;
			this.md = md;
		}
	}

	@SuppressWarnings("serial")
	public static class TranscriptListLoadEvent extends Event {
		public final TrpDoc doc;
		public final TrpPage page;
		public final List<TrpTranscriptMetadata> transcripts;

		public TranscriptListLoadEvent(Object source, TrpDoc doc, TrpPage page, List<TrpTranscriptMetadata> transcripts) {
			super(source, "Transcript list loaded");
			this.doc = doc;
			this.page = page;

			if (transcripts != null)
				this.transcripts = new ArrayList<>(transcripts);
			else
				this.transcripts = null;
		}
	}
	
	@SuppressWarnings("serial")
	public static class DocMetadataUpdateEvent extends Event {
		public final TrpDoc doc;
		public final TrpDocMetadata md;

		public DocMetadataUpdateEvent(Object source, TrpDoc doc, TrpDocMetadata md) {
			super(source, "Doc metadata updated");
			this.doc = doc;
			this.md = md;
		}
	}

	@SuppressWarnings("serial")
	public static class TranscriptLoadEvent extends Event {
		public final TrpDoc doc;
		public final TrpPage page;
		public final JAXBPageTranscript transcript;

		public TranscriptLoadEvent(Object source, TrpDoc doc, TrpPage page, JAXBPageTranscript transcript) {
			super(source, "Transcript loaded");
			this.doc = doc;
			this.page = page;
			this.transcript = transcript;
		}
	}
	
	@SuppressWarnings("serial")
	public static class CollectionsLoadEvent extends Event {
		public final TrpUserLogin user;
		public final List<TrpCollection> collections;

		public CollectionsLoadEvent(Object source, TrpUserLogin user, List<TrpCollection> collections) {
			super(source, "Collections loaded");
			this.user = user;
			this.collections = collections;
		}
	}	


}
