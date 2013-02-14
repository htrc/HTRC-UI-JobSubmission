package edu.indiana.d2i.sloan.ui;

import javax.servlet.http.HttpServletRequest;

import org.apache.amber.oauth2.client.request.OAuthClientRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.interceptor.ServletRequestAware;

import com.opensymphony.xwork2.ActionSupport;

import edu.indiana.d2i.sloan.Constants;

public class LoginAction extends ActionSupport implements ServletRequestAware {
	private static final long serialVersionUID = 1L;

	private static final Log logger = LogFactory.getLog(LoginAction.class);

	private HttpServletRequest httpRequest = null;

	private String redirectURL = null;

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
