package eu.transkribus.swt_gui.mainwidget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.pagingtoolbar.PagingToolBar;
import eu.transkribus.swt.pagingtoolbar.PagingToolBarListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class RegionsPagingToolBarListener extends PagingToolBarListener {
	private final static Logger logger = LoggerFactory.getLogger(RegionsPagingToolBarListener.class);

//	TrpMainWidget widget;

//	public RegionsPagingToolBarListener(PagingToolBar toolbar, TrpMainWidget widget) {
//		super(toolbar);
////		this.widget = widget;
//	}
	
	public RegionsPagingToolBarListener(PagingToolBar toolbar) {
		super(toolbar);
	}

	@Override
	public void onFirstPressed() {
		if (TrpMainWidget.getInstance()!=null) {
			TrpMainWidget.getInstance().jumpToRegion(0);	
		}
		
	}

	@Override
	public void onPrevDoublePressed() {

	}

	@Override
	public void onPrevPressed() {
		if (TrpMainWidget.getInstance()!=null) {
			TrpMainWidget.getInstance().jumpToRegion(Storage.getInstance().getCurrentRegion() - 1);
		}
	}

	@Override
	public void onNextPressed() {
		if (TrpMainWidget.getInstance()!=null) {
			TrpMainWidget.getInstance().jumpToRegion(Storage.getInstance().getCurrentRegion() + 1);	
		}
	}

	@Override
	public void onNextDoublePressed() {

	}

	@Override
	public void onLastPressed() {
		if (TrpMainWidget.getInstance()!=null) {
			TrpMainWidget.getInstance().jumpToRegion(Storage.getInstance().getNTextRegions() - 1);
		}
	}

	@Override
	public void onReloadPressed() {
		if (TrpMainWidget.getInstance()!=null) {
			TrpMainWidget.getInstance().jumpToRegion(Storage.getInstance().getCurrentRegion());	
		}
	}

	@Override
	public void onEnterInPageFieldPressed() {
		if (TrpMainWidget.getInstance()!=null) {
			String val = toolbar.getCurrentPageValue();
			int i = 0;
			try {
				i = Integer.valueOf(val) - 1;
				if (!TrpMainWidget.getInstance().getStorage().hasTextRegion(i))
					throw new Exception();
			} catch (Exception ex) {
				toolbar.setCurrentPageValue("" + (Storage.getInstance().getCurrentRegion() + 1));
			}
			TrpMainWidget.getInstance().jumpToRegion(i);
		}
	}

}
