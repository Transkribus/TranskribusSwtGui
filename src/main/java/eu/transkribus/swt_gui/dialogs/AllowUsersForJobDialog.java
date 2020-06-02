package eu.transkribus.swt_gui.dialogs;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.model.beans.job.enums.JobImpl;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.LabeledCombo;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class AllowUsersForJobDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(AllowUsersForJobDialog.class);
	
	LabeledText userListText;
	LabeledCombo jobImplCombo;
	Button addUserBtn;
	Button addUsersToJobBtn;
	Text allowedUsersText;
	Button reloadAllowedUsersBtn;
	Label allowedUsersLabel;

	public AllowUsersForJobDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	protected Point getInitialSize() {
		Point p = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		return new Point(Math.max(500, p.x + 100), Math.max(500, p.y + 50));
	}
	
	@Override
	protected boolean isResizable() {
	    return true;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Allow users for job");
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		cont.setLayout(new GridLayout(2, false));
		
		userListText = new LabeledText(cont, "List of users: ");
		userListText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		userListText.setToolTipText("A list of users, i.e. their email addresses, separated by whitespaces, that should be added to be allowed for a job");
		
		addUserBtn = new Button(cont, 0);
		addUserBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		addUserBtn.setImage(Images.ADD);
		SWTUtil.onSelectionEvent(addUserBtn, e -> {
			FindUserDialog d = new FindUserDialog(getShell());
			if (d.open() == IDialogConstants.OK_ID) {
				String newUsers = d.getSelectedUsers().stream().map(u -> u.getUserName()).collect(Collectors.joining(" "));
				userListText.setText(userListText.getText()+" "+newUsers);
			}
		});
		
		jobImplCombo = new LabeledCombo(cont, "Job type: ", false, SWT.DROP_DOWN | SWT.READ_ONLY);
		jobImplCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		jobImplCombo.getCombo().add(JobImpl.CITlabHtrPlusTrainingJob.toString());
		jobImplCombo.getCombo().add(JobImpl.P2PaLATrainJob.toString());
		jobImplCombo.getCombo().add(JobImpl.PyLaiaTrainingJob.toString());
		jobImplCombo.getCombo().select(0);
		SWTUtil.onSelectionEvent(jobImplCombo.getCombo(), e -> reloadAllowedUsersForJob());
		
		addUsersToJobBtn = new Button(cont, 0);
		addUsersToJobBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		addUsersToJobBtn.setImage(Images.ARROW_RIGHT);
		addUsersToJobBtn.setText("Allow users for job");
		SWTUtil.onSelectionEvent(addUsersToJobBtn, e -> {
			Storage store = Storage.getInstance();
			if (!store.isAdminLoggedIn()) {
				DialogUtil.showErrorMessageBox(getShell(), "No admin logged in!", "Login as an adming to allow users for a job!");
				return;
			}
			String userListTxt = userListText.getText().trim();
			if (StringUtils.isEmpty(userListTxt)) {
				return;
			}
			
			List<String> userList = Arrays.asList(userListTxt.split(" "));
			String userListStr = userList.stream().collect(Collectors.joining(", "));
			String jobImpl = jobImplCombo.getCombo().getText();
			int res = DialogUtil.showYesNoDialog(getShell(), "Add "+userList.size()+" users", "Do you really want to add the following "+userList.size()+" user to job "+jobImpl+":\n"+userListStr);
			if (res == SWT.YES) {
				logger.info("adding users to job '"+jobImpl+"': "+userListStr);
				try {
					store.getConnection().getAdminCalls().allowUsersForJob(userList, jobImpl);
					DialogUtil.showInfoMessageBox(getShell(), "Success", "Successfully added "+userList.size()+" user to '"+jobImpl+"'");
				} catch (TrpServerErrorException | TrpClientErrorException | SessionExpiredException e1) {
					DialogUtil.showErrorMessageBox(getShell(), "Error allowing users for job "+jobImpl, e1.getMessage());
				}
				reloadAllowedUsersForJob();
			}
		});
		
		allowedUsersLabel = new Label(cont, 0);
		allowedUsersLabel.setText("Allowed Users for job: ");
		allowedUsersLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		Fonts.setBoldFont(allowedUsersLabel);
		
		allowedUsersText = new Text(cont, SWT.READ_ONLY | SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gridData.heightHint = 10 * allowedUsersText.getLineHeight();
		allowedUsersText.setLayoutData(gridData);
		
		reloadAllowedUsersBtn = new Button(cont, 0);
		reloadAllowedUsersBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		SWTUtil.onSelectionEvent(reloadAllowedUsersBtn, e -> reloadAllowedUsersForJob());
		reloadAllowedUsersBtn.setText("Reload");
		reloadAllowedUsersBtn.setImage(Images.REFRESH);
		
		reloadAllowedUsersForJob();
		
		return cont;
	}
	
	private void reloadAllowedUsersForJob() {
		Storage store = Storage.getInstance();
		if (!store.isAdminLoggedIn()) {
			allowedUsersText.setText("");
			return;
		}		
		
		String jobImpl = jobImplCombo.getCombo().getText();
		try {
			List<String> usernames = store.getConnection().getAdminCalls().getUserNamesForJobImpl(jobImpl);
			int N = CoreUtils.size(usernames);
			allowedUsersLabel.setText("Allowed users for job '"+jobImpl+"' ("+N+")");
			if (N>0) {
				Collections.sort(usernames);
			}
			allowedUsersText.setText(usernames.isEmpty() ? "<all users allowed>" : usernames.stream().collect(Collectors.joining("\n")));
		} catch (TrpServerErrorException | TrpClientErrorException | SessionExpiredException e1) {
			DialogUtil.showErrorMessageBox(getShell(), "Error querying allowed users for job: "+jobImpl, e1.getMessage());
		}
	}
	
}
