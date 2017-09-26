package eu.transkribus.swt_gui.search.kws;

import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.io.util.TrpProperties;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.model.beans.kws.TrpKeyWord;
import eu.transkribus.core.model.beans.kws.TrpKwsHit;
import eu.transkribus.core.model.beans.kws.TrpKwsResult;
import eu.transkribus.core.rest.JobConst;
import eu.transkribus.core.util.JaxbUtils;
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
		this.created = job.getCreated();
		if (job.getEndTime() < 1) {
			this.duration = "N/A";
		} else {
			final long diff = job.getEndTime() - job.getCreateTime();
			this.duration = (diff / 1000) / 60f + " minutes";
		}
		this.scope = job.getDocId() < 1 ? "Collection" : "Document";
		this.status = job.getEnded() == null ? "Processing..." : "Completed";
		TrpProperties props = job.getJobDataProps();
		List<String> queries = JobDataUtils.getStringList(props.getProperties(), JobConst.PROP_QUERY);
		String queriesStr = StringUtils.join(queries, "\", \"");
		this.query = "\"" + queriesStr + "\"";
		this.result = extractResult(props);
	}

	private TrpKwsResult extractResult(TrpProperties props) {
		final String xmlStr = props.getString("result");
		TrpKwsResult res = null;
		if(xmlStr != null) {
			try {
				res = JaxbUtils.unmarshal(xmlStr, TrpKwsResult.class, TrpKeyWord.class, TrpKwsHit.class);
			} catch (JAXBException e) {
				logger.error("Could not unmarshal kws result from job!");
			}
		}
		return res;
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
