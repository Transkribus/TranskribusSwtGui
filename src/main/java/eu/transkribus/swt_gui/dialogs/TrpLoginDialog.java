package eu.transkribus.swt_gui.dialogs;

import java.io.IOException;
import java.util.UUID;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.exceptions.ClientVersionNotSupportedException;
import eu.transkribus.core.exceptions.OAuthTokenRevokedException;
import eu.transkribus.core.model.beans.enums.OAuthProvider;
import eu.transkribus.swt.util.LoginDialog;
import eu.transkribus.swt.util.databinding.DataBinder;
import eu.transkribus.swt_gui.TrpGuiPrefs;
import eu.transkribus.swt_gui.TrpGuiPrefs.OAuthCreds;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import eu.transkribus.swt_gui.util.OAuthGuiUtil;

public class TrpLoginDialog extends LoginDialog {
	private final static Logger logger = LoggerFactory.getLogger(TrpLoginDialog.class);
	
	TrpMainWidget mw;
	
	public TrpLoginDialog(Shell parentShell, TrpMainWidget mw, String message, String[] userProposals, String[] serverProposals,
			int defaultUriIndex) {
		super(parentShell, message, userProposals, serverProposals, defaultUriIndex);
		this.mw = mw;
	}
	
	@Override protected void okPressed() {
		String server = super.getSelectedServer();
		String accType = getAccountType();

		boolean success = false;
		String state = UUID.randomUUID().toString();
		String errorMsg = "";
		
		try {
			switch (accType) {
			case "Google":
				OAuthCreds creds = TrpGuiPrefs.getOAuthCreds(OAuthProvider.Google);
				if (creds == null) {
					success = false;
				} else {
					success = mw.loginOAuth(server, creds.getRefreshToken(), state, OAuthGuiUtil.REDIRECT_URI, OAuthProvider.Google);
				}
				break;
			default: //Transkribus
				String user = getUser();
				char[] pw = getPassword();
				boolean rememberCreds = isRememberCredentials();
				success = mw.login(server, user, String.valueOf(pw), rememberCreds);
				break;
			} // end switch
		
		}
		catch (OAuthTokenRevokedException oau) {
			// get new consent
			TrpGuiPrefs.clearOAuthToken(OAuthProvider.Google);
			String code;
			try {
				code = OAuthGuiUtil.getUserConsent(this.getShell(), state, OAuthProvider.Google);
				if (code == null) {
					success = false;
				} else {
					success = OAuthGuiUtil.authorizeOAuth(server, code, state, OAuthProvider.Google);
				}
			} catch (IOException e) {
				success = false;
			}
			
			errorMsg = oau.getMessage();
		}		
		catch (ClientVersionNotSupportedException e) {
			logger.error(e.getMessage(), e);
			close();
			String errorMsgStripped = StringUtils.removeStart(e.getMessage(), "Client error: ");
			mw.notifyOnRequiredUpdate(errorMsgStripped);
			return;
		}
		catch (LoginException e) {
			mw.logout(true, false);
			logger.error(e.getMessage(), e);
			success = false;
			errorMsg = e.getMessage();
		}
		catch (IllegalStateException e) {
			mw.logout(true, false);
			logger.error(e.getMessage(), e);
			success = false;
			errorMsg = e.getMessage();
			if("Already connected".equals(e.getMessage()) && e.getCause() != null) {
				/*
				 * Jersey throws an IllegalStateException "Already connected" for a variety of issues where actually no connection can be established.
				 * see https://github.com/jersey/jersey/issues/3000
				 */
				Throwable cause = e.getCause();
				//override misleading "Already connected" message
				errorMsg = cause.getMessage();
				logger.error("'Already connected' caused by: " + cause.getMessage(), cause);
			}
		}
		catch (Exception e) {
			mw.logout(true, false);
			logger.error(e.getMessage(), e);
			success = false;
			errorMsg = e.getMessage();
		}
		
		if (success) {
			close();
			mw.onSuccessfullLoginAndDialogIsClosed();
		} else {
			String msg = StringUtils.isEmpty(errorMsg) ? "Login failed" : "Login failed: "+errorMsg;
			setInfo(msg);
		}
	}

	@Override protected void postInit() {
		DataBinder db = DataBinder.get();

		db.bindBeanToWidgetSelection(TrpSettings.AUTO_LOGIN_PROPERTY, mw.getTrpSets(), autoLogin);
	}
}
