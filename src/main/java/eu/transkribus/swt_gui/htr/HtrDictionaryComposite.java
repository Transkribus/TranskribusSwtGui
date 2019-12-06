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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
	
	DictNameTableWidget table;
	
	Storage store = Storage.getInstance();
	List<String> htrDicts;
	
	public HtrDictionaryComposite(Composite parent, int flags) {
		super(parent, flags);
		this.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		
		table = new DictNameTableWidget(this, SWT.NONE);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		updateDictionaries(true, false);
	}
	
	/**
	 * @return The name of the selected dictionary or null if the first item (no dictionary) has been selected
	 */
	public String getSelectedDictionary() {
		String dictName = table.getSelection();
		
		if(dictName == null) {
			//deselecting all should not be possible
			dictName = NO_DICTIONARY;
		}
		
		switch (dictName) {
		case NO_DICTIONARY:
			return null;
		case INTEGRATED_DICTIONARY:
			return JobConst.PROP_TRAIN_DATA_DICT_VALUE;
		default:
			return dictName;
		}
	}
	
	public void selectDictionary(String dictionaryName) {
		table.setSelection(dictionaryName);
	}
	
	public void updateDictionaries(boolean reloadDicts, boolean showIntegratedDictOption) {
		if (reloadDicts) {
			this.htrDicts = loadHtrDicts();
		}
		List<String> dictOptions = new ArrayList<>(this.htrDicts);
		dictOptions.add(0, NO_DICTIONARY);
		
		if(showIntegratedDictOption) {
			dictOptions.add(1, INTEGRATED_DICTIONARY);
		}
		
		table.refreshList(dictOptions);
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
				dictName = NO_DICTIONARY;
			}
			logger.trace("Selecting dictionary in table viewer: {}", dictName);
			tv.setSelection(new StructuredSelection(dictName), true);
		}
	}
}
