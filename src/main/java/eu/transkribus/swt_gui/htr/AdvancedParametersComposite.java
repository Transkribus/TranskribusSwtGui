package eu.transkribus.swt_gui.htr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancedParametersComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(AdvancedParametersComposite.class);

	final CustomParameter[] parameters;

	private List<KeyValueWidget> keyValueWidgets;
	
	public AdvancedParametersComposite(Composite parent, CustomParameter... parameters) {
		super(parent, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		GridLayout gl = new GridLayout(3, false);
		this.setLayout(gl);
		this.parameters = parameters == null ? new CustomParameter[] {} : parameters;
		keyValueWidgets = new ArrayList<>();
		addKeyValueWidget();
	}

	private List<String> getAvailableParamNames() {
		List<String> paramsSet = new ArrayList<>(keyValueWidgets.size());
		keyValueWidgets.stream().filter(w -> !StringUtils.isEmpty(w.getKey())).forEach(w -> paramsSet.add(w.getKey()));
		return Arrays.asList(parameters).stream().filter(p -> !paramsSet.contains(p.getLabel()))
				.map(p -> p.getLabel()).collect(Collectors.toList());
	}

	private void addKeyValueWidget() {
		final int index = keyValueWidgets.size();
		final Composite thisComposite = this;
		final KeyValueWidget kvw = new KeyValueWidget(this, index, SWT.NONE);
//		kvw.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		keyValueWidgets.add(kvw);
		
		kvw.getRemoveBtn().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				keyValueWidgets.removeIf(w -> w.getIndex() == index);
				updateKeyValueWidgets();
				
				kvw.dispose();
				thisComposite.layout();
			}
		});

		kvw.getValueText().addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				// Do nothing
			}
			@Override
			public void keyReleased(KeyEvent e) {
				final String text = kvw.getValueText().getText();
				if (!StringUtils.isEmpty(text) && isLast(kvw)) {
					addKeyValueWidget();
				}
			}
		});
		
		kvw.getKeyCombo().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateKeyValueWidgets();
			}
		});
		kvw.getKeyCombo().addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				// wait for it...
			}
			@Override
			public void keyReleased(KeyEvent e) {
				updateKeyValueWidgets();
			}
		});
		updateRemoveBtnVisibility();
		updateKeyValueWidgets();
		this.layout();
	}

	private boolean isFirst(KeyValueWidget kvw) {
		return keyValueWidgets.isEmpty() || kvw.getIndex() == 0;
	}

	private boolean isLast(KeyValueWidget kvw) {
		return keyValueWidgets.isEmpty() || kvw.getIndex() == keyValueWidgets.size() - 1;
	}

	private void updateRemoveBtnVisibility() {
		keyValueWidgets.stream().forEach(q -> q.setRemoveBtnVisible(!isFirst(q) && !isLast(q)));
	}
	
	private void updateKeyValueWidgets() {
		List<String> paramsAvail = getAvailableParamNames();
		for(int index = 0; index < keyValueWidgets.size(); index ++) {
			KeyValueWidget w = keyValueWidgets.get(index);
			logger.debug("index = " + index + " | #keyValueWidgets = " + keyValueWidgets.size());
			String[] params = new String[paramsAvail.size() + 1];
			params[0] = w.getKey();
			for (int i = 1; i <= paramsAvail.size(); i++) {
				params[i] = paramsAvail.get(i - 1);
			}
			w.setIndex(index);
			w.getKeyCombo().setItems(params);
			w.getKeyCombo().select(0);
		}
		updateRemoveBtnVisibility();
	}

	// check this for number
	// https://stackoverflow.com/questions/11831927/how-to-set-a-mask-to-a-swt-text-to-only-allow-decimals

	/**
	 * This does not extend Composite as we need to make use of the parent's 3 column layout for equals column size
	 *
	 */
	private class KeyValueWidget {
		private int index;
		Combo keyCombo;
		Text valueTxt;
		Button removeBtn;

		public KeyValueWidget(Composite parent, int index, int style) {
			this.index = index;
			keyCombo = new Combo(parent, SWT.BORDER);
			keyCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			valueTxt = new Text(parent, SWT.BORDER);
			valueTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			removeBtn = new Button(parent, SWT.PUSH);
			removeBtn.setText("X");
			keyCombo.setItems(getAvailableParamNames().toArray(new String[] {}));
			keyCombo.select(0);
			parent.layout();
		}

		public void dispose() {
			keyCombo.dispose();
			valueTxt.dispose();
			removeBtn.dispose();
		}
		
		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public Combo getKeyCombo() {
			return keyCombo;
		}

		public String getKey() {
			return keyCombo.getText();
		}

		public String getValue() {
			return valueTxt.getText();
		}

		public Button getRemoveBtn() {
			return removeBtn;
		}

		public Text getValueText() {
			return valueTxt;
		}

		public void setRemoveBtnVisible(boolean visible) {
			removeBtn.setVisible(visible);
		}
	}
	
	public static class CustomParameter {
		private String name;
		private String label = null;
		private String defaultValue;
		
		public CustomParameter() {}
		public CustomParameter(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getLabel() {
			if(label == null) {
				return name;
			} else {
				return label;
			}
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public String getDefaultValue() {
			return defaultValue;
		}
		public void setDefaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
		}
	}
}
