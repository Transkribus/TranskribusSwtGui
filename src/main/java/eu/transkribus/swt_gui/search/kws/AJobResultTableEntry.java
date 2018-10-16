package eu.transkribus.swt_gui.search.kws;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.io.util.TrpProperties;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.model.beans.transformer.KwsTransformer;

public abstract class AJobResultTableEntry<T> {
	private static final Logger logger = LoggerFactory.getLogger(AJobResultTableEntry.class);
	private T result;
	private Date created;
	private String duration;
	private String query;
	private String scope;
	private String status;

	public AJobResultTableEntry(TrpJobStatus job) {
//		logger.debug(job.toString());
		this.created = job.getCreated();
		if (job.getEndTime() < 1) {
			this.duration = "N/A";
		} else {
			final long diff = job.getEndTime() - job.getStartTime();
			this.duration = KwsTransformer.DECIMAL_FORMAT.format(diff / 1000f) + " sec.";
		}
		this.scope = job.getDocId() < 1 ? "Collection " + job.getColId() : "Document " + job.getDocId();
		switch(job.getState()) {
		case TrpJobStatus.RUNNING:
			this.status = "Processing...";
			break;
		case TrpJobStatus.FAILED:
			this.status = "Failed. See job overview for more info.";
			break;
		case TrpJobStatus.FINISHED:
			this.status = "Completed";
			break;
		case TrpJobStatus.CREATED:
		case TrpJobStatus.WAITING:
			this.status = "Waiting";
			break;
		default:
			this.status = "Completed";
			break;
		}
		TrpProperties props = job.getJobDataProps();
		this.result = extractResult(props);
		this.query = extractQueries(props, result);
	}

	protected abstract T extractResult(TrpProperties props);
	
	protected abstract String extractQueries(TrpProperties props, T result);
	
	public T getResult() {
		return result;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
