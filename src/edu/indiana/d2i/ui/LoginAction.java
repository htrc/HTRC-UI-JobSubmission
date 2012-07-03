package edu.indiana.d2i.ui;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Properties;

import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.log4j.Logger;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

import edu.indiana.d2i.AgentsRepoSingleton;
import edu.indiana.d2i.Constants;
import edu.indiana.d2i.oauth2.OAuth2Agent;
import edu.indiana.d2i.wso2.WSO2Agent;

@SuppressWarnings("serial")
public class LoginAction extends ActionSupport {
	private static final Logger logger = Logger.getLogger(LoginAction.class);
	private String username;
	private String password;
	private String oauth2Token;

	public String execute() {
		return SUCCESS;
	}

	public void validate() {
		if (getUsername().length() == 0) {
			addFieldError("username", getText("error.login.username"));
		} else if (getPassword().length() == 0) {
			addFieldError("password", getText("error.login.password"));
		} else {
			// Authenticate against OAuth2 server
			try {
				AgentsRepoSingleton agentsRepo = AgentsRepoSingleton
						.getInstance();
				OAuth2Agent oauth2Agent = agentsRepo.getOAuth2Agent();
				oauth2Token = oauth2Agent.authenticate(getUsername(),
						getPassword());
				logger.info(String.format("Token %s granted to user %s",
						oauth2Token, username));

				/**
				 * Set token into the session so we can check the token
				 * expiration later
				 */
				Map<String, Object> session = ActionContext.getContext()
						.getSession();
				session.put("oauth2_token", oauth2Token);

				Properties props = agentsRepo.getProps();

				String pathPrefix = props
						.getProperty(Constants.PN_WSO2_REPO_PREFIX);

				// Check whether user home directory exists, if not, create it
				WSO2Agent wso2Agent = agentsRepo.getWSO2Agent();
				String userHome = pathPrefix + username;
				if (!wso2Agent.isResourceExist(userHome)) {
					wso2Agent.createDir(userHome);
					logger.info(String.format(
							"%s 's home directory doesn't exist, create it",
							username));
					logger.info(String.format("Home dir %s created", userHome));
				}

				// Check whether user's exe home exists, if not, create it
				String exeRepoPrefix = props
						.getProperty(Constants.PN_WSO2_RES_EXE_PREFIX);
				String exeHome = exeRepoPrefix + username;
				if (!wso2Agent.isResourceExist(exeHome)) {
					wso2Agent.createDir(exeHome);
					logger.info(String.format(
							"%s 's executable home doesn't exist, create it",
							username));
					logger.info(String.format("Executable home dir %s created", exeHome));
				}

				// Check whether user's lib home exists, if not, create it
				String libRepoPrefix = props
						.getProperty(Constants.PN_WSO2_RES_LIB_PREFIX);
				String libHome = libRepoPrefix + username;
				if (!wso2Agent.isResourceExist(libHome)) {
					wso2Agent.createDir(libHome);
					logger.info(String
							.format("%s 's lib home doesn't exist, create it",
									username));
					logger.info(String.format("Lib home dir %s created", libHome));
				}
			} catch (KeyManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(String
						.format("KeyManagementException when authenticating user %s against OAuth2 server",
								username));
				addActionError(getText("error.oauth2.authen"));
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(String
						.format("NoSuchAlgorithmException when authenticating user %s against OAuth2 server",
								username));
				addActionError(getText("error.oauth2.authen"));
			} catch (RegistryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(e.getMessage());
				addActionError(getText("error.wso2.err"));
			} catch (OAuthSystemException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(String
						.format("OAuthSystemException when authenticating user %s against OAuth2 server",
								username));
				addActionError(getText("error.oauth2.authen"));
			} catch (OAuthProblemException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(String
						.format("Invalid Credentials when authenticating user %s against OAuth2 server",
								username));
				addActionError(getText("error.login.invalid"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(e.getMessage());
				addActionError(getText("error.io"));
			}
		}
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
