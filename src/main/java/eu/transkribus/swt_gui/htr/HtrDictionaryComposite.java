package eu.transkribus.swt_gui.htr;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ServerErrorException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class HtrDictionaryComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(HtrDictionaryComposite.class);
	
	public final static String NO_DICTIONARY = "No dictionary";
	
	Combo htrDictCombo;
	
	Storage store = Storage.getInstance();
	List<String> htrDicts;
	
	public HtrDictionaryComposite(Composite parent, int flags) {
		super(parent, flags);
		this.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		
		htrDictCombo = new Combo(this, SWT.READ_ONLY);
		htrDictCombo.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		updateDictCombo(true);
	}
	
	/**
	 * @return The name of the selected dictionary or null if the first item (no dictionary) has been selected
	 */
	public String getSelectedDictionary() {
		String dictName = htrDicts.get(htrDictCombo.getSelectionIndex());
		return dictName.equals(NO_DICTIONARY) ? null : dictName; 
	}
	
	public void selectDictionary(String dictionaryName) {
		if (dictionaryName == null) {
			// if this is null, no dictionary will be used
			// first entry in dictCombo is always "No dictionary"
			htrDictCombo.select(0);
		} else {
			// set the dictionary according to config
			for (int i = 0; i < htrDicts.size(); i++) {
				if (dictionaryName.equals(htrDicts.get(i))) {
					htrDictCombo.select(i);
					break;
				}
			}
		}
	}
	
	private void updateDictCombo(boolean reloadDicts) {
		if (reloadDicts) {
			loadHtrDicts();
		}
		
		htrDictCombo.setItems(this.htrDicts.toArray(new String[this.htrDicts.size()]));
		htrDictCombo.select(0);
	}
	
	private void loadHtrDicts() {
		try {
			this.htrDicts = store.getHtrDicts();
		} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException | NoConnectionException e) {
			TrpMainWidget.getInstance().onError("Error", "Could not load HTR model list!", e);
			htrDicts = new ArrayList<>(0);
		}
		htrDicts.add(0, NO_DICTIONARY);
	}

}
