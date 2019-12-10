package eu.transkribus.swt_gui.htr;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ServerErrorException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.nebula.widgets.pagination.table.SortTableColumnSelectionListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.rest.JobConst;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.TableViewerUtils;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class HtrDictionaryComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(HtrDictionaryComposite.class);
	
	public final static String NO_DICTIONARY = "No dictionary";
	public final static String INTEGRATED_DICTIONARY = "Dictionary from training data";
	public final static String INTEGRATED_LM = "Language model from training data";
	public final static String CUSTOM_DICTIONARY = "Custom dictionary";
	
	Combo dictOptionCombo;
	DictNameTableWidget tableWidget;
	
	Storage store = Storage.getInstance();
	List<String> htrDicts;
	
	public HtrDictionaryComposite(Composite parent, int flags) {
		super(parent, flags);
		this.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		
		dictOptionCombo = new Combo(this, SWT.READ_ONLY);
		dictOptionCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		tableWidget = new DictNameTableWidget(this, SWT.NONE);
		tableWidget.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		addListeners();
		updateUi(true, false);
	}
	
	private void addListeners() {
		dictOptionCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String option = dictOptionCombo.getText();
				logger.debug("DictOption = {}", option);
				updateTableViewerEnabledState(option);
			}
		});
	}

	protected void updateTableViewerEnabledState(String option) {
		if(option == null) {
			return;
		}
		tableWidget.setEnabled(option.equals(CUSTOM_DICTIONARY));
	}

	/**
	 * @return Depending on the selected dictComboOption: null for no dictionary, 
	 * the correct JobConst value for triggering requested behavior on the server side
	 *  or the name of the selected custom dictionary from the table
	 */
	public String getDictionarySetting() {
		switch (dictOptionCombo.getText()) {
		case INTEGRATED_DICTIONARY:
			return JobConst.PROP_TRAIN_DATA_DICT_VALUE;
		case INTEGRATED_LM:
			return JobConst.PROP_TRAIN_DATA_LM_VALUE;
		case CUSTOM_DICTIONARY:
			return tableWidget.getSelection();
		case NO_DICTIONARY:
		default:
			return null;
		}
	}
	
	/**
	 * @param dictConfigValue the value set in the current config to be displayed in the composite. 
	 */
	public void updateSelection(String dictConfigValue) {
		if(StringUtils.isEmpty(dictConfigValue)) {
			dictConfigValue = NO_DICTIONARY;
		}
		switch (dictConfigValue) {
		case NO_DICTIONARY:
			selectDictionaryOption(NO_DICTIONARY);
		case JobConst.PROP_TRAIN_DATA_DICT_VALUE:
			selectDictionaryOption(INTEGRATED_DICTIONARY);
			return;
		case JobConst.PROP_TRAIN_DATA_LM_VALUE:
			selectDictionaryOption(INTEGRATED_LM);
			return;
		default:
			selectDictionaryOption(CUSTOM_DICTIONARY);
			selectDictionary(dictConfigValue);
		}
	}
	
	public void selectDictionary(String dictionaryName) {
		tableWidget.setSelection(dictionaryName);
	}
	
	private void selectDictionaryOption(final String option) {
		int selectionIndex = 0;
		for(int i = 0; i < dictOptionCombo.getItemCount(); i++) {
			if(dictOptionCombo.getItem(i).equals(option)) {
				selectionIndex = i;
				break;
			}
		}
		logger.debug("Selecting dict option: {} - {}", selectionIndex, option);
		dictOptionCombo.select(selectionIndex);
		updateTableViewerEnabledState(option);
	}
	
	/**
	 * @param reloadDicts if true then reload the dict. list from the server
	 * @param showIntegratedDictOptions if true then the combo will allow to select the respective options
	 */
	public void updateUi(boolean reloadDicts, boolean showIntegratedDictOptions) {
		if (reloadDicts) {
			this.htrDicts = loadHtrDicts();
		}
		final String selectedOption = dictOptionCombo.getText();
		dictOptionCombo.removeAll();
		dictOptionCombo.add(NO_DICTIONARY);
		if(showIntegratedDictOptions) {
			dictOptionCombo.add(INTEGRATED_DICTIONARY);
			dictOptionCombo.add(INTEGRATED_LM);
		}
		dictOptionCombo.add(CUSTOM_DICTIONARY);
		
		selectDictionaryOption(selectedOption);
		
		final String dictName = tableWidget.getSelection();
		//update the list and keep former selection
		tableWidget.refreshList(this.htrDicts);
		if(dictName != null) {
			selectDictionary(dictName);
		}
	}
	
	private List<String> loadHtrDicts() {
		try {
			return store.getHtrDicts();
		} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException | NoConnectionException e) {
			TrpMainWidget.getInstance().onError("Error", "Could not load HTR model list!", e);
			return new ArrayList<>(0);
		}
	}

	public class DictNameTableWidget extends Composite {
		private final static String NAME_COL = "Name";
		MyTableViewer tv;
		
		public DictNameTableWidget(Composite parent, int style) {
			super(parent, style);
			tv = new MyTableViewer(parent, SWT.V_SCROLL | SWT.SINGLE);
			this.setLayout(new GridLayout(1, false));
			tv.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
			tv.getTable().setHeaderVisible(false);
			tv.getTable().setLinesVisible(true);
			tv.setContentProvider(new ArrayContentProvider());
			createColumns();
		}

		protected void createColumns() {
//			createColumn(NAME_COL, 250, "name", new TableColumnBeanLabelProvider("name"));
			
			TableViewerColumn col = TableViewerUtils.createTableViewerColumn(tv, 0, NAME_COL, 250);
			col.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					return "" + element;
				}
			});
		}
		
		protected TableViewerColumn createColumn(String columnName, int colSize, String sortPropertyName, CellLabelProvider lp) {
			TableViewerColumn col = TableViewerUtils.createTableViewerColumn(tv, 0, columnName, colSize);
			col.setLabelProvider(lp);
			if (sortPropertyName != null) {
				col.getColumn().addSelectionListener(new SortTableColumnSelectionListener(sortPropertyName));
			}
			return col;
		}
		
		public void refreshList(List<String> nameList) {
			tv.setInput(nameList);
		}
		
		public String getSelection() {
			IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
			if (sel.getFirstElement() != null && sel.getFirstElement() instanceof String) {
				return (String) sel.getFirstElement();
			} else {
				return null;
			}
		}
		
		public void setSelection(String dictName) {
			if(StringUtils.isEmpty(dictName)) {
				// if this is null, no dictionary will be used
				// first entry in dictCombo is always NO_DICTIONARY
//				dictName = NO_DICTIONARY;
				
				//deselect all
				logger.debug("dictName is null or empty: deselect all...");
				tv.setSelection(null);
				return;
			}
			logger.trace("Selecting dictionary in table viewer: {}", dictName);
			tv.setSelection(new StructuredSelection(dictName), true);
		}
		
		public void setEnabled(boolean enabled) {
			tv.getTable().setEnabled(enabled);
		}
	}
}
