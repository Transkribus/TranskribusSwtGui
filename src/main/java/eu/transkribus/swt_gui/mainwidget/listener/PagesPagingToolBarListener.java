package eu.transkribus.swt_gui.mainwidget.listener;

import eu.transkribus.swt_canvas.pagingtoolbar.PagingToolBar;
import eu.transkribus.swt_canvas.pagingtoolbar.PagingToolBarListener;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class PagesPagingToolBarListener extends PagingToolBarListener {
	TrpMainWidget widget;
	PagingToolBar toolbar;

	public PagesPagingToolBarListener(PagingToolBar toolbar, TrpMainWidget widget) {
		super(toolbar);
		this.toolbar = toolbar;
		this.widget = widget;
	}
	
	@Override
	public void onFirstPressed() {
		widget.firstPage();
		updateToolBarValue();
	}

	@Override
	public void onPrevPressed() {
		widget.prevPage();
		updateToolBarValue();
	}

	@Override
	public void onNextPressed() {
		widget.nextPage();
		updateToolBarValue();
	}

	@Override
	public void onLastPressed() {
		widget.lastPage();
		updateToolBarValue();
	}

	@Override
	public void onReloadPressed() {
		widget.reloadCurrentPage(false);
		updateToolBarValue();
	}

	private void updateToolBarValue() {
		toolbar.setCurrentPageValue("" + (Storage.getInstance().getPageIndex() + 1));
	}

	@Override
	public void onEnterInPageFieldPressed() {
		String val = toolbar.getCurrentPageValue();
		int i = 0;
		try {
			i = Integer.valueOf(val) - 1;
			if (!widget.getStorage().hasPageIndex(i))
				throw new Exception();
		} catch (Exception ex) {
			toolbar.setCurrentPageValue("" + (Storage.getInstance().getPageIndex() + 1));
		}
		widget.jumpToPage(i);
	}

}