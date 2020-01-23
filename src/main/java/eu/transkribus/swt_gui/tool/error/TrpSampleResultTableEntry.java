package eu.transkribus.swt_gui.tool.error;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.io.util.TrpProperties;
import eu.transkribus.core.model.beans.TrpComputeSample;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.model.beans.rest.ParameterMap;
import eu.transkribus.core.rest.JobConst;
import eu.transkribus.core.util.JaxbUtils;
import eu.transkribus.core.util.JobDataUtils;
import eu.transkribus.swt_gui.search.kws.AJobResultTableEntry;

public class TrpSampleResultTableEntry extends AJobResultTableEntry<TrpComputeSample>{
	
	
	private TrpJobStatus job;
	public TrpSampleResultTableEntry(TrpJobStatus job) {
		super(job);	
		this.job = job;
	}
	private static final Logger logger = LoggerFactory.getLogger(TrpErrorResultTableEntry.class);

	@Override
	protected TrpComputeSample extractResult(TrpProperties props) {
		final String xmlStr = props.getString(JobConst.PROP_RESULT);
		TrpComputeSample res = null;
		if(xmlStr != null) {
			try {
				res = JaxbUtils.unmarshal(xmlStr, TrpComputeSample.class);
			} catch (JAXBException e) {
				logger.error("Could not unmarshal error result result from job!");
			}
		}
		return res;	
	}
	
	public TrpJobStatus getJob() {
		return job;
	}

	@Override
	protected String extractQueries(TrpProperties props, TrpComputeSample result) {
		
		ParameterMap paramMap = JobDataUtils.getParameterMap(props.getProperties(), JobConst.PROP_PARAMETERS);
		String hyp = paramMap.getParameterValue("hyp");
		String gt = paramMap.getParameterValue("ref");
		return "Ref: "+gt+" | Hyp : "+hyp ;
		
	} 

}
