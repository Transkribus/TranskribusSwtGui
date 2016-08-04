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

public class OAuthCallbackServerSocket {
	private static final Logger logger = LoggerFactory.getLogger(OAuthCallbackServerSocket.class);

	ServerSocket serverSocket;
	Socket clientSocket;
	PrintWriter out;
	BufferedReader in;
	
	String code = null;

	public OAuthCallbackServerSocket(final int port) throws IOException {

		serverSocket = new ServerSocket(port);

	}

	public String accept(final String codePattern) throws IOException {
		
		try {
			clientSocket = serverSocket.accept();
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
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
		} finally {
			close();
		}
		return code;
	}

	public String getCode(){
		return code;
	}
	
	public void close() {
		try {
			serverSocket.close();
		} catch (IOException e) {}
		try {
			if (clientSocket != null) clientSocket.close();
		} catch (IOException e) {}
		try {
			if (out != null) out.close();
		} catch (Exception e) {}
		try {	
			if (in != null) in.close();
		} catch (IOException e) {}
		
		
	}

}
