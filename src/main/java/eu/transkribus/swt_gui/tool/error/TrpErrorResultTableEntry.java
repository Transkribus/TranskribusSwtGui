package eu.transkribus.swt_gui.tool.error;

import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.io.util.TrpProperties;
import eu.transkribus.core.model.beans.TrpErrorRate;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.rest.JobConst;
import eu.transkribus.core.util.JaxbUtils;
import eu.transkribus.core.util.JobDataUtils;
import eu.transkribus.swt_gui.search.kws.AJobResultTableEntry;

public class TrpErrorResultTableEntry extends AJobResultTableEntry<TrpErrorRate>{


	public TrpErrorResultTableEntry(TrpJobStatus job) {
		super(job);
		
	}
	private static final Logger logger = LoggerFactory.getLogger(TrpErrorResultTableEntry.class);

	@Override
	protected TrpErrorRate extractResult(TrpProperties props) {
		final String xmlStr = props.getString(JobConst.PROP_RESULT);
		TrpErrorRate res = null;
		if(xmlStr != null) {
			try {
				res = JaxbUtils.unmarshal(xmlStr, TrpErrorRate.class);
			} catch (JAXBException e) {
				logger.error("Could not unmarshal kws result from job!");
			}
		}
		return res;	
	}

	@Override
	protected String extractQueries(TrpProperties props, TrpErrorRate result) {
		String query = null;
		if(result == null) {
			List<String> queries = JobDataUtils.getStringList(props.getProperties(), JobConst.PROP_QUERY);
			query = "\"" + StringUtils.join(queries, "\", \"") + "\"";
		} else {
			query = JobConst.PROP_QUERY;
		}
		
		return query;
	}

}
