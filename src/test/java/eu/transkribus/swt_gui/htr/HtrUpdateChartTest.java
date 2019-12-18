package eu.transkribus.swt_gui.htr;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.ws.rs.client.InvocationCallback;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.rest.TrpHtrList;
import eu.transkribus.swt_gui.TestApplicationWindow;

public class HtrUpdateChartTest {
	private static final Logger logger = LoggerFactory.getLogger(HtrUpdateChartTest.class);
	
	public static void main(String[] args) {
		if(args.length == 0) {
			throw new IllegalArgumentException("No arguments.");
		}
		final TestApplicationWindow test = new TestApplicationWindow(TrpServerConn.PROD_SERVER_URI, args[0], args[1], 575) {
			HtrModelsComposite comp;
			List<TrpHtr> htrs;
			@Override
			protected void createTestContents(Composite parent) throws Exception {
				getShell().setSize(500, 700);
				comp = new HtrModelsComposite(parent, SWT.NONE);
				parent.pack();
				
				Future<TrpHtrList> htrList = getStorage().getConnection().getHtrs(null, null, 0, -1, new InvocationCallback<TrpHtrList>() {

					@Override
					public void completed(TrpHtrList htrList) {
						//success
					}

					@Override
					public void failed(Throwable throwable) {
						Assert.fail();
					}
					
				});
				htrs = htrList.get().getList();
				comp.htw.refreshList(htrs);
				
				List<TrpHtr> badHtrs = new LinkedList<>();
				
				for(TrpHtr htr : htrs) {
					try {
						comp.updateDetails(htr);
					} catch (ArrayIndexOutOfBoundsException e) {
						badHtrs.add(htr);
					}
				}
				
				logger.info("Found {} bad HTRs", badHtrs.size());
				logger.info("IDs = " + badHtrs.stream().map(h -> "" + h.getHtrId()).collect(Collectors.joining(", ")) );
			}
		};
		test.show();
	}
}
