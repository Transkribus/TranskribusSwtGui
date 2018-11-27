package eu.transkribus.swt_gui.mainwidget;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.ATrpServerConn;
import eu.transkribus.core.util.CoreUtils;

public class ProgramInfo {
	private final static Logger logger = LoggerFactory.getLogger(ProgramInfo.class);
	
	Properties INFO_PROPS = new Properties();
	
	Date timestamp;
	
	public ProgramInfo() {
		try {
//			try {
				INFO_PROPS.load(this.getClass().getClassLoader().getResourceAsStream("info.properties"));
//			} catch (NullPointerException e) {
//				logger.warn("COULD NOT FIND info.properties FILE - UPDATE WILL NOT WORK!");
//				INFO_PROPS.setProperty("version", "NA");
//				INFO_PROPS.setProperty("name", "name");
//				INFO_PROPS.setProperty("helptext", "NA");
//				INFO_PROPS.setProperty("timestamp", "NA");
//			}
				
			ATrpServerConn.guiVersion = getVersion();
			DateFormat df = CoreUtils.newDateFormat();
			try {
				timestamp = df.parse(getTimestampString());
			} catch (ParseException pe) {
				/* 
				 * Might haben when build timestamp is not set:
				 * Could not load info.properties: Unparseable date: "${maven.build.timestamp}"
				 */
				logger.error("Could not read build timestamp!");
				timestamp = df.parse("01_01_1970_00:00");
			}
			
			logger.info("version = "+getVersion()+" timestamp = "+getTimestamp());
		} catch (IOException | ParseException e) {
			logger.error("Could not load info.properties: "+e.getMessage(), e);
			System.exit(1);
		}
	}
	
	public String getVersion() { return INFO_PROPS.getProperty("version"); }
	public String getName() { return INFO_PROPS.getProperty("name"); }
	public String getHelptext() { return INFO_PROPS.getProperty("helptext"); }
	public String getJarName() { return getName()+"-"+getVersion()+".jar"; }
	
	public String getTimestampString() { 
		return INFO_PROPS.getProperty("timestamp");
	}
	public Date getTimestamp() { return timestamp; }
	
	String time = INFO_PROPS.getProperty("timestamp");
	
	

}
