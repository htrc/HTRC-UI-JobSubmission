/*
#
# Copyright 2007 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: HTRC Sloan job submission web interface
# File:  LoginSuccessAction.java
# Description:  
#
# -----------------------------------------------------------------
# 
 */
package edu.indiana.d2i.sloan.ui;

import htrc.security.oauth2.client.OAuth2Client;
import htrc.security.oauth2.client.OAuthUserInfoRequest;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.amber.oauth2.client.URLConnectionClient;
import org.apache.amber.oauth2.client.request.OAuthClientRequest;
import org.apache.amber.oauth2.client.response.OAuthAuthzResponse;
import org.apache.amber.oauth2.client.response.OAuthClientResponse;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.types.GrantType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

import edu.indiana.d2i.sloan.Constants;

/**
 * Login success action. Retrieve access token, refresh token and user info from
 * WS IS when redirect back to job submission interface
 * 
 * @author Guangchen
 * 
 */
public class LoginSuccessAction extends ActionSupport implements
		ServletRequestAware, ServletResponseAware, SessionAware {
	private static final long serialVersionUID = 1L;

	private static final Log logger = LogFactory
			.getLog(LoginSuccessAction.class);

	private HttpServletRequest httpRequest = null;
	private HttpServletResponse httpResponse = null;
	private Map<String, Object> session = null;

	private String getServerContext() {
		HttpServletRequest request = getServletRequest();
		final StringBuilder serverPath = new StringBuilder();
		serverPath.append(request.getScheme() + "://");
		serverPath.append(request.getServerName());
		if (request.getServerPort() != 80) {
			serverPath.append(":" + request.getServerPort());
		}
		serverPath.append(request.getContextPath());
		return serverPath.toString();
	}

	private boolean disableSSL() {
		// Create empty HostnameVerifier
		HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String urlHostName, SSLSession session) {
				return true;
			}
		};

		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };

		// install all-trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			SSLSocketFactory sslSocketFactory = sc.getSocketFactory();
			HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
			return true;
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return false;
		} catch (KeyManagementException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return false;
		}
	}

	/**
	 * retrieve access token, refresh token and user info from WS IS
	 */
	@Override
	public String execute() {
		String token = (String) ActionContext.getContext().getSession()
				.get(Constants.SESSION_TOKEN);
		if (token == null) {
			try {
				OAuthAuthzResponse authzResponse = OAuthAuthzResponse
						.oauthCodeAuthzResponse(getServletRequest());
				String authzCode = authzResponse.getCode();
				if (authzCode == null) {
					logger.error("OAuth2 provider returned empty authorization code.");
					addActionError("OAuth2 provider returned empty authorization code.");
					return ERROR;
				}

				// set SSL
				if (!disableSSL())
					return ERROR;

				// get access token
				String webAppContext = getServerContext();
				String redirectUrl = webAppContext + "/LoginSuccessAction";
				OAuthClientRequest tokenRequest = OAuthClientRequest
						.tokenLocation(
								PortalConfiguration.getOAuth2TokenEndpoint())
						.setGrantType(GrantType.AUTHORIZATION_CODE)
						.setClientId(PortalConfiguration.getOAuth2ClientID())
						.setClientSecret(
								PortalConfiguration.getOAuth2ClientSecrete())
						.setRedirectURI(redirectUrl).setCode(authzCode)
						.buildBodyMessage();

				OAuth2Client tokenClient = new OAuth2Client(
						new URLConnectionClient());
				OAuthClientResponse tokenResponse = tokenClient
						.accessToken(tokenRequest);
				String accessToken = tokenResponse
						.getParam(Constants.OAUTH2_ACCESS_TOKEN);
				String refreshToken = tokenResponse
						.getParam(Constants.OAUTH2_REFRESH_TOKEN);
				Long expireInSec = Long.valueOf(tokenResponse
						.getParam(Constants.OAUTH2_EXPIRE));

				// get user info

				// token relative stuffs
				session.put(Constants.SESSION_TOKEN, accessToken);
				session.put(Constants.SESSION_REFRESH_TOKEN, refreshToken);
				session.put(Constants.SESSION_TOKEN_EXPIRE_SEC, expireInSec);

				if (logger.isDebugEnabled()) {
					logger.debug("Access Token from Oauth2:" + accessToken);
				}

				// request userinfo
				OAuthClientRequest userInfoRequest = OAuthUserInfoRequest
						.userInfoLocation(
								PortalConfiguration.getOAuth2UserinfoEndpoint())
						.setClientId(PortalConfiguration.getOAuth2ClientID())
						.setClientSecret(
								PortalConfiguration.getOAuth2ClientSecrete())
						.setAccessToken(accessToken).buildBodyMessage();
				OAuth2Client userInfoClient = new OAuth2Client(
						new URLConnectionClient());
				OAuthClientResponse userInfoResponse = userInfoClient
						.userInfo(userInfoRequest);
				String userName = userInfoResponse.getParam("authorized_user");

				if (userName == null || "".equals(userName)) {
					String errMsg = "Cannot obtain username from Oauth2";
					logger.error(errMsg);
					addActionError(errMsg);
					return ERROR;
				}

				if (logger.isDebugEnabled()) {
					logger.debug("Username from Oauth2:" + userName);
				}

				// user name
				session.put(Constants.SESSION_USERNAME, userName);

				// session cookie
				httpResponse.addCookie(new Cookie(
						Constants.SESSION_EXIST_BEFORE, "true")); // username
				session.put(Constants.SESSION_EXIST_BEFORE, new Boolean(false));

				return SUCCESS;
			} catch (OAuthProblemException e) {
				logger.error(e.getError(), e);
				addActionError(e.getError());
				return ERROR;
			} catch (OAuthSystemException e) {
				logger.error(e.getMessage(), e);
				addActionError(e.getMessage());
				return ERROR;
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				addActionError(e.getMessage());
				return ERROR;
			}
		}
		return SUCCESS;
	}

	@Override
	public void setServletRequest(HttpServletRequest request) {
		this.httpRequest = request;
	}

	public HttpServletRequest getServletRequest() {
		return this.httpRequest;
	}

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}

	@Override
	public void setServletResponse(HttpServletResponse response) {
		this.httpResponse = response;
	}
}
