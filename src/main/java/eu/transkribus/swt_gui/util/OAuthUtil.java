package eu.transkribus.swt_gui.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
			clientId = "660348649554-q57qcde5rln0l40g06n3u2tt2h6l068a.apps.googleusercontent.com";
			uriStr = "https://accounts.google.com/o/oauth2/v2/auth?" + "scope=email%20profile" + "&state=" + state
					+ "&redirect_uri=http://127.0.0.1:" + 8999 + "&response_type=code" + "&client_id=" + clientId;
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
}
