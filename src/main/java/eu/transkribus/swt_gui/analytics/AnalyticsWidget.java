package eu.transkribus.swt_gui.analytics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyticsWidget extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(AnalyticsWidget.class);
	
	Composite mdGroup;
	
	Combo refVersionCombo, hypVersionCombo;
	Button computeWerBtn;
	
	public AnalyticsWidget(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new GridLayout(1, true));
				
		initWerGroup();
	}
	
	private void initWerGroup() {
		ExpandableComposite werExp = new ExpandableComposite(this, ExpandableComposite.COMPACT);
		Composite werGroup = new Composite(werExp, SWT.SHADOW_ETCHED_IN);
		werGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		metadatagroup.setText("Document metadata");
		werGroup.setLayout(new GridLayout(2, false));
		
		Label refLabel = new Label(werGroup, 0);
		refLabel.setText("Reference:");
		refVersionCombo = new Combo(werGroup, SWT.READ_ONLY);
		
		Label hypLabel = new Label(werGroup, 0);
		hypLabel.setText("Hypothesis:");
		hypVersionCombo = new Combo(werGroup, SWT.READ_ONLY);
		
		Label emptyLabel = new Label(werGroup,0);
		computeWerBtn = new Button(werGroup, SWT.PUSH);
		computeWerBtn.setText("Compare");
		computeWerBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		computeWerBtn.setToolTipText("Compares the two selected transcripts and computes word error rate and character error rate.");
		computeWerBtn.pack();
		
		werExp.setClient(werGroup);
		werExp.setText("Compute WER");
		werExp.setExpanded(true);
		werExp.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				layout();
			}
		});
	}
	
	private void postInitExpandable(ExpandableComposite exp, Composite c, String title) {
		exp.setClient(c);
		exp.setText("OCR");
		exp.setExpanded(true);
		exp.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				layout();
			}
		});
	}	
}
