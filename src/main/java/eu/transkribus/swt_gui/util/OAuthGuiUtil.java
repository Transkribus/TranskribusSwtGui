package eu.transkribus.swt_gui.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.ws.rs.ClientErrorException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.enums.OAuthProvider;
import eu.transkribus.swt_canvas.progress.ProgressBarDialog;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class OAuthGuiUtil {
	private static final Logger logger = LoggerFactory.getLogger(OAuthGuiUtil.class);

	private static final int PORT = 8999;
	public static final String REDIRECT_URI = "http://127.0.0.1:" + PORT;
	
	public static String getUserConsent(final Shell sh, final String state, final OAuthProvider prov) throws IOException {
		String code = null;
		final String clientId;
		final String uriStr;
		final String codePattern;
		switch (prov) {
		case Google:
			//transkribusSwtGui
//			clientId = "660348649554-nch5pp6ptq5gmq901fn1le7659q4g2qj.apps.googleusercontent.com";
			// if this is used, then do not set access type to offline (at least it is not needed)
			
			//transkribusServer
			clientId = "660348649554-85q3k21p65e09pr91je1qnuej0mlk78d.apps.googleusercontent.com";
			
			uriStr = "https://accounts.google.com/o/oauth2/v2/auth?" + "scope=email%20profile" + "&state=" + state
					+ "&redirect_uri=" + REDIRECT_URI + "&response_type=code" + "&client_id=" + clientId
					+ "&access_type=offline"; //access_type=offline if google credentials are for web apps
			codePattern = ".*\\?state=" + state + "&code=(.*)\\s.*";
			break;
		default:
			throw new IOException("Unknown OAuth Provider: " + prov);
		}
		
		org.eclipse.swt.program.Program.launch(uriStr);
		
//		OAuthCallbackServerSocket sock = new OAuthCallbackServerSocket(PORT);		
//		code = sock.accept(codePattern); //blocks!!
		
		ServerSocketRunnable ssr = new ServerSocketRunnable(codePattern);
		try {
			ProgressBarDialog.open(sh, ssr, "Waiting for Connection..." , true);
			code = ssr.getCode();
		} catch (Throwable e) {}
		
		return code;
	}

	public static boolean authorizeOAuth(final String server, final String code, final String state,
			final OAuthProvider prov) {
		final String grantType = "authorization_code";
		try {
			Storage.getInstance().loginOAuth(server, code, state, grantType, REDIRECT_URI, prov);
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		} finally {
			TrpMainWidget.getInstance().logout(true, false);
		}
	}
	
	public static void revokeOAuthToken(final String refreshToken, final OAuthProvider prov) throws IOException{
		
		final String uriStr;
		switch (prov) {
		case Google:
			uriStr = "https://accounts.google.com/o/oauth2/revoke?token=";
			break;
		default:
			throw new IOException("Unknown OAuth Provider: " + prov);
		}
		
		CloseableHttpClient client = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();		
		HttpGet get = new HttpGet(uriStr + refreshToken);
		HttpResponse response = client.execute(get);
		final int status = response.getStatusLine().getStatusCode();		
		if (status != 200) {
			String reason = response.getStatusLine().getReasonPhrase();
			logger.error(reason);
			throw new ClientErrorException(reason, status);
		}
	}
	
	private static class ServerSocketRunnable implements IRunnableWithProgress {
		String codePattern; 
		String code = null;
		
		public ServerSocketRunnable(String codePattern) {
			this.codePattern = codePattern;
		}

		@Override
		public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			
			final OAuthCallbackServerSocket s;
			try {
				s = new OAuthCallbackServerSocket(PORT);
			
				monitor.beginTask("Waiting for connection...", 2);
				
				Runnable r = new Runnable() {
					@Override
					public void run() {
						try {
							s.accept(codePattern);
						} catch (IOException e) {}				
					}
					
				};
				Thread t = new Thread(r);
				t.start();
				
				while(s.getCode() == null ){
					monitor.worked(1);
					if(monitor.isCanceled()) {
						s.close();
						return;
					}
					if(!t.isAlive()) {
						//user did not give consent
						return;
					}
					Thread.sleep(1000);
				}
				
				this.code = s.getCode();
				monitor.done();
				
			} catch (IOException e) {}
			
		}
		
		
		public String getCode() {
			return code;
		}
	}
}
