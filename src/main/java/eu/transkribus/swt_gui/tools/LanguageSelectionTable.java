package eu.transkribus.swt_gui.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_canvas.util.Colors;

public class LanguageSelectionTable extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(LanguageSelectionTable.class);
	
	 CheckboxTableViewer tv;
	String[] languages;
	Text currentLanguages;
	
	static final String CURR_LANG_LABEL_FRONT = "";

	public LanguageSelectionTable(Composite parent, int style) {
		super(parent, style);

//		this.setLayout(new FillLayout());
		this.setLayout(new GridLayout(1, false));
		 
		currentLanguages = new Text(this, SWT.READ_ONLY);
		currentLanguages.setBackground(Colors.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		currentLanguages.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		tv =  CheckboxTableViewer.newCheckList(this, SWT.BORDER | SWT.V_SCROLL);
		tv.setContentProvider(new ArrayContentProvider());
		tv.getTable().setHeaderVisible(false);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint=80;
		gd.widthHint=150;
		tv.getTable().setLayoutData(gd);

		TableViewerColumn tvc = new TableViewerColumn(tv, SWT.NONE);
		tvc.getColumn().setWidth(gd.widthHint);
		tvc.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return (String) element;
			}
		});
		
		tv.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				updateCurrentLanguageLabel();
			}
		});
	}
	
//	public TableViewer getTableViewer() { return tv.getTable().add; }
	
	public void addCheckStateListener(ICheckStateListener l) {
		tv.addCheckStateListener(l);
	}
	
	public void removeCheckStateListener(ICheckStateListener l) {
		tv.removeCheckStateListener(l);
	}
	
	public CheckboxTableViewer getTv() { return tv; }
	
	void updateCurrentLanguageLabel() {
		currentLanguages.setText(CURR_LANG_LABEL_FRONT+getSelectedLanguagesString());
	}

	public void setAvailableLanguages(String[] languages) {
		List<String> langs = Arrays.asList(languages);
		Collections.sort(langs, String.CASE_INSENSITIVE_ORDER);
		this.languages = languages;
		tv.setInput(this.languages);
		tv.refresh();
	}
	
	public String getSelectedLanguagesString() {
		String l="";
		for (int i=0; i<tv.getTable().getItemCount(); ++i) {
			TableItem ti = tv.getTable().getItems()[i];
			if (ti.getChecked())
				l += ti.getText(0)+", ";
		}
		l = StringUtils.removeEnd(l, ", ");
		return l;
	}
	
	public void setSelectedLanguages(String languages) {
		logger.debug("setting selected languages: "+languages);
		if (languages==null)
			languages = "";
		
		String[] ls = languages.split(",");
		List<String> langList = new ArrayList<>();
		for (String l : ls) {
			langList.add(l.trim());
		}
		
		for (int i=0; i<tv.getTable().getItemCount(); ++i) {
			TableItem ti = tv.getTable().getItems()[i];
			String l = ti.getText(0);
			
			ti.setChecked(langList.contains(l));
		}
		
		updateCurrentLanguageLabel();
	}

}
