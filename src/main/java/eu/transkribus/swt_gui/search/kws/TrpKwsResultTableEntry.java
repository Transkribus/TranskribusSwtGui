package eu.transkribus.swt_gui.search.kws;

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

public class TrpKwsResultTableEntry extends AJobResultTableEntry<TrpKwsResult> {
	public TrpKwsResultTableEntry(TrpJobStatus job) {
		super(job);
	}

	private static final Logger logger = LoggerFactory.getLogger(TrpKwsResultTableEntry.class);

	@Override
	protected TrpKwsResult extractResult(TrpProperties props) {
		return KwsTransformer.extractResultDataFromProps(props);
		
	}

	@Override
	protected String extractQueries(TrpProperties props, TrpKwsResult result) {
		String query;
		if(result == null) {
			List<String> queries = JobDataUtils.getStringList(props.getProperties(), JobConst.PROP_QUERY);
			query = "\"" + StringUtils.join(queries, "\", \"") + "\"";
		} else {
			List<String> queriesWithHits = new LinkedList<>();
			result.getKeyWords()
				.forEach(k -> queriesWithHits
							.add("\"" + k.getKeyWord() + "\" (" + k.getHits().size() + ")")
							);
			query = StringUtils.join(queriesWithHits, ", ");
		}
		return query;
	}
	
}
