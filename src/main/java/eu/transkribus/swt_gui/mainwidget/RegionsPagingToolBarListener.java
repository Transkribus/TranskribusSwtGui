package eu.transkribus.swt_gui.mainwidget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.pagingtoolbar.PagingToolBar;
import eu.transkribus.swt.pagingtoolbar.PagingToolBarListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class RegionsPagingToolBarListener extends PagingToolBarListener {
	private final static Logger logger = LoggerFactory.getLogger(RegionsPagingToolBarListener.class);

	TrpMainWidget widget;

	public RegionsPagingToolBarListener(PagingToolBar toolbar, TrpMainWidget widget) {
		super(toolbar);
		this.widget = widget;
	}

	@Override
	public void onFirstPressed() {
		widget.jumpToRegion(0);
	}

	@Override
	public void onPrevDoublePressed() {

	}

	@Override
	public void onPrevPressed() {
		widget.jumpToRegion(Storage.getInstance().getCurrentRegion() - 1);
	}

	@Override
	public void onNextPressed() {
		logger.debug("NEXT PRESSED");
		widget.jumpToRegion(Storage.getInstance().getCurrentRegion() + 1);
	}

	@Override
	public void onNextDoublePressed() {

	}

	@Override
	public void onLastPressed() {
		widget.jumpToRegion(Storage.getInstance().getNTextRegions() - 1);
	}

	@Override
	public void onReloadPressed() {
		widget.jumpToRegion(Storage.getInstance().getCurrentRegion());
	}

	@Override
	public void onEnterInPageFieldPressed() {
		String val = toolbar.getCurrentPageValue();
		int i = 0;
		try {
			i = Integer.valueOf(val) - 1;
			if (!widget.getStorage().hasTextRegion(i))
				throw new Exception();
		} catch (Exception ex) {
			toolbar.setCurrentPageValue("" + (Storage.getInstance().getCurrentRegion() + 1));
		}
		widget.jumpToRegion(i);
	}

}
