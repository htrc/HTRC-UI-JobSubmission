package edu.indiana.d2i.oauth2;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.amber.oauth2.client.OAuthClient;
import org.apache.amber.oauth2.client.URLConnectionClient;
import org.apache.amber.oauth2.client.request.OAuthClientRequest;
import org.apache.amber.oauth2.client.request.OAuthClientRequest.TokenRequestBuilder;
import org.apache.amber.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.types.GrantType;

public class OAuth2Agent {
	private String serverURL;
	private boolean isSelfSigned;

	public OAuth2Agent(String serverURL, boolean isSelfSigned) {
		super();
		this.serverURL = serverURL;
		this.isSelfSigned = isSelfSigned;
	}

	/**
	 * Should implement this method when the oauth2 server provides the service
	 * to check whether the granted token has expired, current always return
	 * false (not expire)
	 * 
	 * @param token
	 * @return
	 */
	public boolean isTokenExpire(String token) {
		return false;
	}

	public String authenticate(String uname, String password)
			throws OAuthSystemException, OAuthProblemException,
			NoSuchAlgorithmException, KeyManagementException {

		if (isSelfSigned) {
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(
						java.security.cert.X509Certificate[] certs,
						String authType) {
				}

				public void checkServerTrusted(
						java.security.cert.X509Certificate[] certs,
						String authType) {
				}

				public boolean isServerTrusted(
						java.security.cert.X509Certificate[] certs) {
					return true;
				}

				public boolean isClientTrusted(
						java.security.cert.X509Certificate[] certs) {
					return true;
				}
			} };

			SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts,
					new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext
					.getSocketFactory());
		}

		TokenRequestBuilder tokenRequestBuilder = OAuthClientRequest
				.tokenLocation(serverURL);
		tokenRequestBuilder.setGrantType(GrantType.CLIENT_CREDENTIALS);
		tokenRequestBuilder.setClientId(uname);
		tokenRequestBuilder.setClientSecret(password);

		OAuthClientRequest clientRequest = tokenRequestBuilder
				.buildQueryMessage();

		OAuthClient client = new OAuthClient(new URLConnectionClient());

		OAuthAccessTokenResponse response = client.accessToken(clientRequest);

		// System.out.println("token: " + response.getAccessToken());

		return response.getAccessToken();
	}
}
