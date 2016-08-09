package eu.transkribus.swt_gui;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.net.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.enums.OAuthProvider;
import eu.transkribus.swt_gui.util.OAuthGuiUtil;

/**
 * In linux prefs are stored in: (by default - can be changed by setting java.util.prefs.userRoot property!)
 * 	${user.home}/.java/.userPrefs/org/dea/transcript/trp/gui/creds
 * In windows in the registry under:
 * 	HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Prefs and HKEY_CURRENT_USER\Software\JavaSoft\Prefs\org\dea\transcript\trp\gui\creds
 * 
 * @author sebastianc
 */
public class TrpGuiPrefs {
	private final static Logger logger = LoggerFactory.getLogger(TrpGuiPrefs.class);
	
//	final static String wtf = "PMpCIaf8HUUjbNMW1DvpmERZqov9ZMQaE6e7SFypaFg=";
	final static String wtf = "PMpCIaf8HUUjbNMW1DvpmE";
	
	public static final String CREDS_NODE = "creds";
	public static final String OAUTH_NODE = "oauth";
	public static final String ACCOUNT_NODE = "acc";
	
	private static final String OAUTH_UN_KEY = "_un";
	private static final String OAUTH_PIC_KEY = "_pic";
	
	public static final String LAST_USER_KEY = "lastUser";
	public static final String UN_KEY = "trpUn";
	public static final String PW_KEY = "trpPw";
	
	private static final String LAST_ACCOUNT_TYPE_KEY = "accType";
	
	static Preferences pref = Preferences.userNodeForPackage(TrpGui.class).node(CREDS_NODE);
	static Preferences oAuthPrefs = Preferences.userNodeForPackage(TrpGui.class).node(OAUTH_NODE);
	static Preferences accountPrefs = Preferences.userNodeForPackage(TrpGui.class).node(ACCOUNT_NODE);
	
	public static String encryptAes(String key, String strToEncrypt) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		SecretKeySpec secretKey = new SecretKeySpec(Base64.decodeBase64(key), "AES");
		
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		String encryptedString = Base64.encodeBase64String(cipher.doFinal(strToEncrypt.getBytes()));
		return encryptedString;
	}
	
	public static byte[] generateAesKey() throws NoSuchAlgorithmException {
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(256); // for example
		SecretKey secretKey = keyGen.generateKey();
		
		return secretKey.getEncoded();
	}
	
	public static String decryptAes(String key, String strToDecrypt) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
		SecretKeySpec secretKey = new SecretKeySpec(Base64.decodeBase64(key), "AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		String decryptedString = new String(cipher.doFinal(Base64.decodeBase64(strToDecrypt)));
		return decryptedString;
	}
	
	public static List<String> getUsers() {
		List<String> users = new ArrayList<>();
		try {
			for (String k : pref.keys()) {
				if (!k.equals(CREDS_NODE)) {
					users.add(k);
				}
			}
		} catch (BackingStoreException e) {
			logger.error(e.getMessage());
		}
		return users;
	}
	
	public static void clearCredentials() throws Exception {
		pref.clear();
	}
	
	public static void clearCredentials(String username) throws Exception {
		pref.remove(username);
	}
	
	public static void storeCredentials(String username, String pw) throws Exception {
		logger.debug("storing credentials for user: "+username);
		
		pref.put(username, encryptAes(wtf, pw));
	}
	
	public static void storeLastLogin(String username) throws Exception {
		pref.put(LAST_USER_KEY, username);
	}
	
	public static Pair<String, String> getLastStoredCredentials() {
		return getStoredCredentials(null);
	}
	
	/**
	 * Retrieves stored credentials for the given username if it exists, returns null elsewise.
	 * If the parameter username is null it will return the user stored by the storeLastLogin call.
	 */
	public static Pair<String, String> getStoredCredentials(String username) {
		try {
			if (username == null) {
				username = pref.get(LAST_USER_KEY, null);
				if (username == null) // no "last user" stored
					return null;
			}
			
			String pwEnc = pref.get(username, null);
			if (pwEnc == null) { // no credentials stored for this user
				return null;
			}
			return Pair.of(username, decryptAes(wtf, pwEnc));
		} catch (Exception e) {
			logger.error("Could not retrieve stored creds: "+e.getMessage());
			return null;
		}
	}
	
	public static void storeLastAccountType(String accType) throws Exception {
		accountPrefs.put(LAST_ACCOUNT_TYPE_KEY, accType);
	}
	
	public static String getLastLoginAccountType() {
		return accountPrefs.get(LAST_ACCOUNT_TYPE_KEY, OAuthGuiUtil.TRANSKRIBUS_ACCOUNT_TYPE);
	}
	
	public static void storeOAuthCreds(final OAuthProvider prov, final String userName, final String profilePicUrl, final String refreshToken) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		logger.debug("storing refreshToken for OAuth Provider "+ prov.toString());
		oAuthPrefs.put(prov.toString() + OAUTH_UN_KEY, userName);
		if(profilePicUrl != null){
			oAuthPrefs.put(prov.toString() + OAUTH_PIC_KEY, profilePicUrl);
		}
		oAuthPrefs.put(prov.toString(), encryptAes(wtf, refreshToken));
	}

	/**
	 * Retrieves stored refreshToken for the given OauthProvider if it exists, returns null elsewise.
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws InvalidKeyException 
	 */
	public static OAuthCreds getOAuthCreds(OAuthProvider prov) {
		
		if (prov == null) {
			throw new IllegalArgumentException("OAuthProvider is null!");
		}
		
		String tokenEnc = oAuthPrefs.get(prov.toString(), null);
		if (tokenEnc == null) { // no credentials stored for this provider
			logger.debug("Did not find token for OAuth Provider: "+ prov);
			return null;
		} else {
			logger.debug("Found token for OAuth Provider: "+ prov);
		}
		String tokenClear;
		try {
			tokenClear = decryptAes(wtf, tokenEnc);
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException
				| NoSuchPaddingException e) {
			logger.error("Could not decrypt token for OAuthProvider: " + prov, e);
			return null;
		}
		
		final String userName = oAuthPrefs.get(prov.toString() + OAUTH_UN_KEY, null);
		final String profilePicUrl = oAuthPrefs.get(prov.toString() + OAUTH_PIC_KEY, null);
		
		OAuthCreds creds = new OAuthCreds(prov);
		creds.setProfilePicUrl(profilePicUrl);
		creds.setUserName(userName);
		creds.setRefreshToken(tokenClear);
		
		return creds;
	}
	
	public static void clearOAuthToken(OAuthProvider prov) {
		oAuthPrefs.remove(prov.toString());
		oAuthPrefs.remove(prov.toString()+ OAUTH_UN_KEY);
		oAuthPrefs.remove(prov.toString()+ OAUTH_PIC_KEY);
	}
	
	public static void testPreferences() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		
//		byte[] key = generateAesKey();
//		System.out.println(Base64.encodeBase64String(key));
		
		
		String key = "whatever";
//		pref.put(key, encryptAes(rawKey, "test"));
		
		String value = pref.get(key, "");
		System.out.println("value encyrpted: "+value);
		System.out.println("value decrypted: "+decryptAes(wtf, value));
		
//		Preferences sp = new Preferences(TrpGui.class, rawKey, true);
//		
//		
//		
//		sp.put(key, "blabla");
//		
//		String value = sp.getString(key, "default");
//		System.out.println("value of "+key+": "+value);
	}
	
	public static void main(String[] args) throws Exception {
		testPreferences();
	}
	
	public static class OAuthCreds {
		private OAuthProvider prov;
		private String refreshToken;
		private String userName;
		private String profilePicUrl;
		public OAuthCreds(OAuthProvider prov){
			this.prov = prov;
		}
		public OAuthProvider getProvider(){
			return prov;
		}
		public String getRefreshToken() {
			return refreshToken;
		}
		public void setRefreshToken(String refreshToken) {
			this.refreshToken = refreshToken;
		}
		public String getUserName() {
			return userName;
		}
		public void setUserName(String userName) {
			this.userName = userName;
		}
		public String getProfilePicUrl() {
			return profilePicUrl;
		}
		public void setProfilePicUrl(String profilePicUrl) {
			this.profilePicUrl = profilePicUrl;
		}
		@Override
		public String toString() {
			return "OAuthCreds [refreshToken=" + refreshToken + ", userName=" + userName + ", profilePicUrl="
					+ profilePicUrl + "]";
		}
	}
}
