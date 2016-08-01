package eu.transkribus.swt_gui.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.ClientErrorException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.enums.OAuthProvider;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class OAuthUtil {
	private static final Logger logger = LoggerFactory.getLogger(OAuthUtil.class);

	public static String getUserConsent(final String state, final OAuthProvider prov) throws IOException {
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
					+ "&redirect_uri=http://127.0.0.1:" + 8999 + "&response_type=code" + "&client_id=" + clientId
					+ "&access_type=offline"; //access_type=offline if google credentials are for web apps
			codePattern = ".*\\?state=" + state + "&code=(.*)\\s.*";
			break;
		default:
			throw new IOException("Unknown OAuth Provider: " + prov);
		}

		org.eclipse.swt.program.Program.launch(uriStr);

		try (ServerSocket serverSocket = new ServerSocket(8999);
				Socket clientSocket = serverSocket.accept();
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));) {

			String inputLine;
			String text = "";

			Pattern p = Pattern.compile(codePattern);
			while ((inputLine = in.readLine()) != null) {
				text += inputLine;
				Matcher m = p.matcher(inputLine);
				if (m.find()) {
					code = m.group(1);
					break;
				}
				if (inputLine.isEmpty()) {
					break;
				}
			}

			logger.debug(text);
			// System.out.println(code);

			final String doc = "<!doctype html><html><head>"
					+ "</head><body>You can now close the tab and return to Transkribus</body></html>";
			String response = "HTTP/1.1 200 OK\r\n" + "Server: Server\r\n" + "Content-Type: text/html\r\n"
					+ "Content-Length: " + doc.length() + "\r\n" + "Connection: close\r\n\r\n";
			String result = response + doc;
			out.write(result);
			out.flush();
		}

		return code;
	}

	public static boolean authorizeOAuth(final String server, final String code, final String state,
			final OAuthProvider prov) {
		final String grantType = "authorization_code";
		try {
			Storage.getInstance().loginOAuth(server, code, state, grantType, prov);
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
}
