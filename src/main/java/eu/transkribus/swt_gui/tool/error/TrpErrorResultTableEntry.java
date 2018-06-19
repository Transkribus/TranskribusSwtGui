package eu.transkribus.swt_gui.tool.error;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.io.util.TrpProperties;
import eu.transkribus.core.model.beans.TrpErrorRate;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.rest.JobConst;
import eu.transkribus.core.util.JaxbUtils;
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
				logger.error("Could not unmarshal error result result from job!");
			}
		}
		return res;	
	}

	@Override
	protected String extractQueries(TrpProperties props, TrpErrorRate result) {
//		logger.debug(props.writeToString());
//		String option = null;
//		switch((String)props.get("parameters.3.value")) {
//		case "0": 
//			option = "";
//			break;
//		case "1":
//			option = "normcompatibility";
//		case "2":
//			option = "normcanonic";
//		case "3":
//			option = "non-case-sensitive";
//		}
		return "Pages : "+props.getOrDefault("parameters.0.value", "Page-Query missing") ;
	} 

}
