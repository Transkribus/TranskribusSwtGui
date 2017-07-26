package eu.transkribus.swt.pagination_table;

import org.eclipse.nebula.widgets.pagination.PageableController;
import org.eclipse.nebula.widgets.pagination.renderers.ICompositeRendererFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class PagingToolBarNavigationRendererFactory implements ICompositeRendererFactory {
	
	private static final ICompositeRendererFactory FACTORY = new PagingToolBarNavigationRendererFactory();

	public static ICompositeRendererFactory getFactory() {
		return FACTORY;
	}

	@Override public Composite createComposite(Composite parent, int style,
			PageableController controller) {
		return new PagingToolBarNavigationRenderer(parent, SWT.NONE, controller);
	}

}
