package eu.transkribus.swt_gui.search.kws;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.io.util.TrpProperties;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.model.beans.kws.TrpKwsResult;
import eu.transkribus.core.model.beans.transformer.KwsTransformer;
import eu.transkribus.core.rest.JobConst;
import eu.transkribus.core.util.JobDataUtils;

public class TrpKwsResultTableEntry {
	private static final Logger logger = LoggerFactory.getLogger(TrpKwsResultTableEntry.class);
	private TrpKwsResult result;
	private Date created;
	private String duration;
	private String query;
	private String scope;
	private String status;

	public TrpKwsResultTableEntry(TrpJobStatus job) {
//		logger.debug(job.toString());
		this.created = job.getCreated();
		if (job.getEndTime() < 1) {
			this.duration = "N/A";
		} else {
			final long diff = job.getEndTime() - job.getCreateTime();
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
		default:
			this.status = "Completed";
			break;
		}
		TrpProperties props = job.getJobDataProps();
		this.result = KwsTransformer.extractResultDataFromProps(props);
		if(result == null) {
			List<String> queries = JobDataUtils.getStringList(props.getProperties(), JobConst.PROP_QUERY);
			this.query = "\"" + StringUtils.join(queries, "\", \"") + "\"";
		} else {
			List<String> queriesWithHits = new LinkedList<>();
			result.getKeyWords()
				.forEach(k -> queriesWithHits
							.add("\"" + k.getKeyWord() + "\" (" + k.getHits().size() + ")")
							);
			this.query = StringUtils.join(queriesWithHits, ", ");
		}
	}

	public TrpKwsResult getResult() {
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
