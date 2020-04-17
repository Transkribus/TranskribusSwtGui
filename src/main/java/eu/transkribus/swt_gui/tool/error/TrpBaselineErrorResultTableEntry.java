package eu.transkribus.swt_gui.tool.error;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.io.util.TrpProperties;
import eu.transkribus.core.model.beans.TrpBaselineErrorRate;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.model.beans.rest.ParameterMap;
import eu.transkribus.core.rest.JobConst;
import eu.transkribus.core.util.JaxbUtils;
import eu.transkribus.core.util.JobDataUtils;
import eu.transkribus.swt_gui.search.kws.AJobResultTableEntry;

public class TrpBaselineErrorResultTableEntry  extends AJobResultTableEntry<TrpBaselineErrorRate> {
	private static final Logger logger = LoggerFactory.getLogger(TrpBaselineErrorResultTableEntry.class);

	public TrpBaselineErrorResultTableEntry(TrpJobStatus job) {
		super(job);	
	}

	@Override
	protected TrpBaselineErrorRate extractResult(TrpProperties props) {
//		return props.getString(JobConst.PROP_RESULT);
		final String xmlStr = props.getString(JobConst.PROP_RESULT);
		TrpBaselineErrorRate res = null;
		if(!StringUtils.isEmpty(xmlStr)) {
			try {
				res = JaxbUtils.unmarshal(xmlStr, TrpBaselineErrorRate.class);
			} catch (JAXBException e) {
				logger.warn("Could not unmarshal error result result from job - skipping");
			}
		}
		return res;	
	}

	@Override
	protected String extractQueries(TrpProperties props, TrpBaselineErrorRate result) {
		
		String option = null;
//		ParameterMap params = null;
//		if(result != null) {
//			params = result.getParams();
//		}
//		if(params == null) {
			ParameterMap paramMap = JobDataUtils.getParameterMap(props.getProperties(), JobConst.PROP_PARAMETERS);
			
			String hyp = paramMap.getParameterValue("hyp");
			String gt = paramMap.getParameterValue("ref");
			String pages = paramMap.getParameterValue("pages");
			option = getOption(paramMap.getParameterValue("option"));
			
			return "Page(s) : "+pages +" | Ref: "+gt+" | Hyp : "+hyp ;
//		}
//		if(params.getParameterValue("option") != null) {
//			option = getOption(params.getParameterValue("option"));
//		}
//		return "Page(s) : "+params.getParameterValue("pages") +" | Option : "+option +" | Ref: "+params.getParameterValue("ref")+" | Hyp : "+params.getParameterValue("hyp") ;
		
	} 
	
	String getOption (String option) {
		switch(option) {
		case "-1":
			option = "Quick Compare";
			break;
		case "0": 
			option = "case-sensitive";
			break;
		case "1":
			option = "case-insensitive";
			break;
		}
		return option;
	}

}
