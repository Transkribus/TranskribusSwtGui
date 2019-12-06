package eu.transkribus.swt_gui.htr;

import java.util.List;

import org.eclipse.swt.widgets.Shell;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.swt_gui.models.ShareModelDialog;

public class ShareHtrDialog extends ShareModelDialog {

	public ShareHtrDialog(Shell parentShell, TrpHtr model) {
		super(parentShell, model.getHtrId(), model.getName());
	}

	protected void addModelToCollection(int colId)
			throws TrpClientErrorException, TrpServerErrorException, SessionExpiredException {
		store.getConnection().addHtrToCollection(getModelId(), store.getCollId(), colId);
	}

	protected void removeModelFromCollection(int colId)
			throws TrpClientErrorException, TrpServerErrorException, SessionExpiredException {
		store.getConnection().removeHtrFromCollection(getModelId(), colId);
	}

	protected List<TrpCollection> getCollectionList()
			throws TrpServerErrorException, TrpClientErrorException, SessionExpiredException {
		return store.getConnection().getCollectionsByHtr(store.getCollId(), getModelId());
	}
}
