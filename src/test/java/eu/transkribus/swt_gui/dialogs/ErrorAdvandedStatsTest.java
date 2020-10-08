package eu.transkribus.swt_gui.dialogs;

import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.model.beans.TrpErrorRate;
import eu.transkribus.core.model.beans.TrpErrorRateListEntry;
import eu.transkribus.core.util.JaxbUtils;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class ErrorAdvandedStatsTest {
	
	private final static Logger logger = LoggerFactory.getLogger(ErrorRateAdvancedTest.class);	

	public static void main(String[] args) {
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				try {
					Storage.getInstance().login(TrpServerConn.TEST_SERVER_URI, args[0], args[1]);
					logger.info("login success!");
				} catch (Exception e) {
					e.printStackTrace();
				}				
		
				TrpErrorRate e = new TrpErrorRate();
				e.setWer("12,2%");
				e.setwAcc("6,98%");
				e.setCer("10,66%");
				e.setcAcc("29,38%");
				e.setBagTokensF("18,96%");
				e.setBagTokensPrec("24,91%");
				e.setBagTokensRec("40.21528861154446177");
				
				ArrayList<TrpErrorRateListEntry> list = new ArrayList<TrpErrorRateListEntry>();
				
				TrpErrorRateListEntry page1 = new TrpErrorRateListEntry();
				page1.setWer("2,2%");
				page1.setwAcc("16,98%");
				page1.setCer("20,66%");
				page1.setcAcc("39,38%");
				page1.setBagTokensF("13,92%");
				page1.setBagTokensPrec("27,91%");
				page1.setBagTokensRec("30.21528861154446177");
				page1.setPageNumber(2);
				
				TrpErrorRateListEntry page2 = new TrpErrorRateListEntry();
				page2.setWer("21,2%");
				page2.setwAcc("62,98%");
				page2.setCer("2,66%");
				page2.setcAcc("9,38%");
				page2.setBagTokensF("28,96%");
				page2.setBagTokensPrec("17,91%");
				page2.setBagTokensRec("15.21528861154446177");
				page2.setPageNumber(5);
				
				TrpErrorRateListEntry page3 = new TrpErrorRateListEntry();
				page3.setWer("1,2%");
				page3.setwAcc("2,98%");
				page3.setCer("42,66%");
				page3.setcAcc("19,38%");
				page3.setBagTokensF("28,96%");
				page3.setBagTokensPrec("7,91%");
				page3.setBagTokensRec("5.21528861154446177");
				page3.setPageNumber(6);
				
				TrpErrorRateListEntry page4 = new TrpErrorRateListEntry();
				page4.setWer("71,2%");
				page4.setwAcc("2,98%");
				page4.setCer("42,66%");
				page4.setcAcc("19,38%");
				page4.setBagTokensF("28,96%");
				page4.setBagTokensPrec("27,91%");
				page4.setBagTokensRec("5.31528861154446177");
				page4.setPageNumber(7);
				
				TrpErrorRateListEntry page5 = new TrpErrorRateListEntry();
				page5.setWer("61,2%");
				page5.setwAcc("2,98%");
				page5.setCer("42,66%");
				page5.setcAcc("19,38%");
				page5.setBagTokensF("28,96%");
				page5.setBagTokensPrec("67,91%");
				page5.setBagTokensRec("5.51528861154446177");
				page5.setPageNumber(8);
				
				TrpErrorRateListEntry page6 = new TrpErrorRateListEntry();
				page6.setWer("41,2%");
				page6.setwAcc("2,98%");
				page6.setCer("42,66%");
				page6.setcAcc("19,38%");
				page6.setBagTokensF("28,96%");
				page6.setBagTokensPrec("97,91%");
				page6.setBagTokensRec("5.21528861154446177");
				page6.setPageNumber(9);
				
				TrpErrorRateListEntry page7 = new TrpErrorRateListEntry();
				page7.setWer("11,2%");
				page7.setwAcc("2,98%");
				page7.setCer("42,66%");
				page7.setcAcc("19,38%");
				page7.setBagTokensF("28,96%");
				page7.setBagTokensPrec("27,91%");
				page7.setBagTokensRec("27.21528861154446177");
				page7.setPageNumber(12);
				
				TrpErrorRateListEntry page8 = new TrpErrorRateListEntry();
				page8.setWer("21,2%");
				page8.setwAcc("2,98%");
				page8.setCer("22,66%");
				page8.setcAcc("19,38%");
				page8.setBagTokensF("28,96%");
				page8.setBagTokensPrec("57,91%");
				page8.setBagTokensRec("18.21528861154446177");
				page8.setPageNumber(14);
				
				TrpErrorRateListEntry page9 = new TrpErrorRateListEntry();
				page9.setWer("31,2%");
				page9.setwAcc("2,98%");
				page9.setCer("32,66%");
				page9.setcAcc("59,38%");
				page9.setBagTokensF("78,96%");
				page9.setBagTokensPrec("87,91%");
				page9.setBagTokensRec("9.21528861154446177");
				page9.setPageNumber(18);
				
				TrpErrorRateListEntry page10 = new TrpErrorRateListEntry();
				page10.setWer("31,2%");
				page10.setwAcc("2,98%");
				page10.setCer("32,66%");
				page10.setcAcc("59,38%");
				page10.setBagTokensF("78,96%");
				page10.setBagTokensPrec("87,91%");
				page10.setBagTokensRec("9.21528861154446177");
				page10.setPageNumber(19);
				
				TrpErrorRateListEntry page11 = new TrpErrorRateListEntry();
				page11.setWer("31,2%");
				page11.setwAcc("2,98%");
				page11.setCer("32,66%");
				page11.setcAcc("59,38%");
				page11.setBagTokensF("78,96%");
				page11.setBagTokensPrec("87,91%");
				page11.setBagTokensRec("9.21528861154446177");
				page11.setPageNumber(20);
				
				
				list.add(page1);
				list.add(page2);
				list.add(page3);
				list.add(page4);
				list.add(page5);
				list.add(page6);
				list.add(page7);
				list.add(page8);
				list.add(page9);
				list.add(page10);
				list.add(page11);
	
				e.setList(list);
				
				try {
					logger.debug(JaxbUtils.marshalToString(e, TrpErrorRate.class));
				} catch (JAXBException ex) {
					ex.printStackTrace();
				}
				
				ErrorRateAdvancedStats eDia = new ErrorRateAdvancedStats(getShell(),e,3133,"add | query | here |  adffsasdf | asdsadg | asds");
			
				eDia.open();
				

				return parent;
			}
		};
		aw.setBlockOnOpen(true);
		aw.open();
		
		
	}

}
