package eu.transkribus.swt_gui.mainwidget;

import java.util.Objects;

import org.eclipse.swt.widgets.Shell;

import eu.transkribus.swt_gui.canvas.SWTCanvas;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public abstract class AMainWidgetController {
	protected TrpMainWidget mw;
	protected TrpSettings trpSets;
	protected Storage storage;
	protected SWTCanvas canvas;
	
	public AMainWidgetController(TrpMainWidget mw) {
		this.mw = mw;
		this.trpSets = mw.getTrpSets();
		this.storage = Storage.getInstance();
		this.canvas = mw.getCanvas();
		
		Objects.requireNonNull(this.mw, "TrpMainWidget cannot be null!");
		Objects.requireNonNull(this.storage, "Storage cannot be null!");
	}
	
	public Shell getShell() {
		return mw.getShell();
	}
	
	public TrpMainWidget getMainWidget() {
		return mw;
	}
	
	public TrpSettings getTrpSets() {
		return trpSets;
	}
	
	public SWTCanvas getCanvas() {
		return canvas;
	}

}
