package eu.transkribus.swt_gui.collection_manager;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.swt.util.DialogUtil;

public class CollectionEditorDialog extends Dialog {
	
	Text nameTxt, descrTxt;
	Button isCrowdsourceBtn, isELearningBtn;
	
	private TrpCollection collection;
	private boolean mdChanged = false;

	public CollectionEditorDialog(Shell parentShell, TrpCollection c) {
		super(parentShell);
		this.collection = c;
	}
	
	public void setVisible() {
		if(super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		cont.setLayout(new GridLayout(2, false));
		
		Label nameLbl = new Label(cont, SWT.NONE);
		nameLbl.setText("Name:");
		nameTxt = new Text(cont, SWT.BORDER);
		nameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label descrLbl = new Label(cont, SWT.NONE);
		descrLbl.setText("Description:");
		descrTxt = new Text(cont, SWT.BORDER | SWT.MULTI);
		descrTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		new Label(cont, SWT.NONE);
		isCrowdsourceBtn = new Button(cont, SWT.CHECK);
		isCrowdsourceBtn.setText("Crowdsourcing");
		
		new Label(cont, SWT.NONE);
		isELearningBtn = new Button(cont, SWT.CHECK);
		isELearningBtn.setText("eLearning");
		
		updateValues();
		
		return cont;
	}
	
	private void updateValues() {
		nameTxt.setText(collection.getColName());
		descrTxt.setText(collection.getDescription() == null ? "" : collection.getDescription());
		isCrowdsourceBtn.setSelection(collection.isCrowdsourcing());
		isELearningBtn.setSelection(collection.isElearning());
	}

	public TrpCollection getCollection() {
		return collection;
	}
	
	public boolean isMdChanged() {
		return mdChanged;
	}
	
	@Override
	protected void okPressed() {
			
		final String name = nameTxt.getText();
		final String descr = descrTxt.getText();
		final boolean isCrowdsource = isCrowdsourceBtn.getSelection();
		final boolean isELearning = isELearningBtn.getSelection();
		
		if(StringUtils.isEmpty(name)) {
			DialogUtil.showErrorMessageBox(this.getShell(), 
					"Invalid Input", 
					"Collection name must not be empty!");
			return;
		}
		
		//if crowdsourcing was checked but the collection was not for crowdsourcing yet
		if(!collection.isCrowdsourcing() && isCrowdsource) {
			int ret = DialogUtil.showYesNoDialog(this.getShell(), 
					"Collection was marked for crowdsourcing", 
					"You have marked the collection to be available for crowdsourcing.\n"
					+ "This will allow any user to subscribe to this collection, see its content "
					+ "and edit the contained documents!\nAre you sure you want to do this?");
			if(ret != SWT.YES) {
				return;
			}
		}
		
		//if crowdsourcing was checked but the collection was not for crowdsourcing yet
		if(!collection.isElearning() && isELearning) {
			int ret = DialogUtil.showYesNoDialog(this.getShell(), 
					"Collection was marked for eLearning", 
					"You have marked the collection to be available for eLearning.\n"
					+ "This will allow any user to subscribe to this collection and see its content.\n"
					+ "Are you sure you want to do this?");
			if(ret != SWT.YES) {
				return;
			}
		}
		
		mdChanged = !name.equals(collection.getColName()) 
				|| !descr.equals(collection.getDescription())
				|| isCrowdsource != collection.isCrowdsourcing()
				|| isELearning != collection.isElearning();
		
		if(mdChanged) {
			collection.setColName(name);
			collection.setDescription(descr);
			collection.setCrowdsourcing(isCrowdsource);
			collection.setElearning(isELearning);
		}
		super.okPressed();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Edit Collection Metadata");
		newShell.setMinimumSize(640, 480);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(800, 480);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.RESIZE | SWT.TITLE);
	}
}
