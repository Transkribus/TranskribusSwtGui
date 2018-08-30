package eu.transkribus.swt_gui.dialogs;

import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpErrorList;
import eu.transkribus.core.model.beans.TrpErrorRate;
import eu.transkribus.core.util.JaxbUtils;

public class ErrorAdvandedStatsTest {
	
	private final static Logger logger = LoggerFactory.getLogger(ErrorRateAdvancedTest.class);	

	public static void main(String[] args) {
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
		
				TrpErrorRate e = new TrpErrorRate();
				e.setWer("12,2%");
				e.setwAcc("6,98%");
				e.setCer("10,66%");
				e.setcAcc("29,38%");
				e.setBagTokensF("18,96%");
				e.setBagTokensPrec("24,91%");
				e.setBagTokensRec("40.21528861154446177");
				
				ArrayList<TrpErrorList> list = new ArrayList<TrpErrorList>();
				
				TrpErrorList page1 = new TrpErrorList();
				page1.setWer("2,2%");
				page1.setwAcc("16,98%");
				page1.setCer("20,66%");
				page1.setcAcc("39,38%");
				page1.setBagTokensF("13,92%");
				page1.setBagTokensPrec("27,91%");
				page1.setBagTokensRec("30.21528861154446177");
				page1.setPageNumber(2);
				
				TrpErrorList page2 = new TrpErrorList();
				page2.setWer("21,2%");
				page2.setwAcc("62,98%");
				page2.setCer("2,66%");
				page2.setcAcc("9,38%");
				page2.setBagTokensF("28,96%");
				page2.setBagTokensPrec("17,91%");
				page2.setBagTokensRec("15.21528861154446177");
				page2.setPageNumber(5);
				
				TrpErrorList page3 = new TrpErrorList();
				page3.setWer("1,2%");
				page3.setwAcc("2,98%");
				page3.setCer("42,66%");
				page3.setcAcc("19,38%");
				page3.setBagTokensF("28,96%");
				page3.setBagTokensPrec("7,91%");
				page3.setBagTokensRec("5.21528861154446177");
				page3.setPageNumber(6);
				
				TrpErrorList page4 = new TrpErrorList();
				page4.setWer("71,2%");
				page4.setwAcc("2,98%");
				page4.setCer("42,66%");
				page4.setcAcc("19,38%");
				page4.setBagTokensF("28,96%");
				page4.setBagTokensPrec("27,91%");
				page4.setBagTokensRec("5.31528861154446177");
				page4.setPageNumber(7);
				
				TrpErrorList page5 = new TrpErrorList();
				page5.setWer("61,2%");
				page5.setwAcc("2,98%");
				page5.setCer("42,66%");
				page5.setcAcc("19,38%");
				page5.setBagTokensF("28,96%");
				page5.setBagTokensPrec("67,91%");
				page5.setBagTokensRec("5.51528861154446177");
				page5.setPageNumber(8);
				
				TrpErrorList page6 = new TrpErrorList();
				page6.setWer("41,2%");
				page6.setwAcc("2,98%");
				page6.setCer("42,66%");
				page6.setcAcc("19,38%");
				page6.setBagTokensF("28,96%");
				page6.setBagTokensPrec("97,91%");
				page6.setBagTokensRec("5.21528861154446177");
				page6.setPageNumber(9);
				
				TrpErrorList page7 = new TrpErrorList();
				page7.setWer("11,2%");
				page7.setwAcc("2,98%");
				page7.setCer("42,66%");
				page7.setcAcc("19,38%");
				page7.setBagTokensF("28,96%");
				page7.setBagTokensPrec("27,91%");
				page7.setBagTokensRec("27.21528861154446177");
				page7.setPageNumber(12);
				
				TrpErrorList page8 = new TrpErrorList();
				page8.setWer("21,2%");
				page8.setwAcc("2,98%");
				page8.setCer("22,66%");
				page8.setcAcc("19,38%");
				page8.setBagTokensF("28,96%");
				page8.setBagTokensPrec("57,91%");
				page8.setBagTokensRec("18.21528861154446177");
				page8.setPageNumber(14);
				
				TrpErrorList page9 = new TrpErrorList();
				page9.setWer("31,2%");
				page9.setwAcc("2,98%");
				page9.setCer("32,66%");
				page9.setcAcc("59,38%");
				page9.setBagTokensF("78,96%");
				page9.setBagTokensPrec("87,91%");
				page9.setBagTokensRec("9.21528861154446177");
				page9.setPageNumber(18);
				
				
				list.add(page1);
				list.add(page2);
				list.add(page3);
				list.add(page4);
				list.add(page5);
				list.add(page6);
				list.add(page7);
				list.add(page8);
				list.add(page9);
	
				e.setList(list);
				
				try {
					logger.debug(JaxbUtils.marshalToString(e, TrpErrorRate.class));
				} catch (JAXBException ex) {
					ex.printStackTrace();
				}
				
				ErrorRateAdvancedStats eDia = new ErrorRateAdvancedStats(getShell(),e,3133);
			
				eDia.open();
				

				return parent;
			}
		};
		aw.setBlockOnOpen(true);
		aw.open();
		
		
	}

}
