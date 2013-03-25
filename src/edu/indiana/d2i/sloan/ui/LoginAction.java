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
# File:  LoginAction.java
# Description:  
#
# -----------------------------------------------------------------
# 
 */
package edu.indiana.d2i.sloan.ui;

import javax.servlet.http.HttpServletRequest;

import org.apache.amber.oauth2.client.request.OAuthClientRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.interceptor.ServletRequestAware;

import com.opensymphony.xwork2.ActionSupport;

import edu.indiana.d2i.sloan.Constants;

/**
 * Login action. Redirect user to WS Identity Server (IS) for authentication
 * 
 * @author Guangchen
 * 
 */
public class LoginAction extends ActionSupport implements ServletRequestAware {
	private static final long serialVersionUID = 1L;

	private static final Log logger = LogFactory.getLog(LoginAction.class);

	private HttpServletRequest httpRequest = null;

	private String redirectURL = null;

	/**
	 * get server context
	 * 
	 * @return
	 */
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

	/**
	 * Redirect to WS IS
	 */
	public String execute() throws Exception {
		String webAppContext = getServerContext();
		String redirectUrl = webAppContext + "/LoginSuccessAction";

		if (logger.isDebugEnabled()) {
			logger.debug("redirectUrl:" + redirectUrl);
		}

		OAuthClientRequest authzRequest = OAuthClientRequest
				.authorizationLocation(
						PortalConfiguration.getOAuth2AuthEndpoint())
				.setClientId(PortalConfiguration.getOAuth2ClientID())
				.setRedirectURI(redirectUrl)
				.setResponseType(PortalConfiguration.getOAuth2AuthType())
				.setScope(PortalConfiguration.getOAuth2AuthScope())
				.buildQueryMessage();
		redirectURL = authzRequest.getLocationUri();
		return Constants.OAUTH2_REDIRECT_URL;
	}

	@Override
	public void setServletRequest(HttpServletRequest request) {
		this.httpRequest = request;
	}

	public HttpServletRequest getServletRequest() {
		return this.httpRequest;
	}

	public String getRedirectURL() {
		return redirectURL;
	}
}
