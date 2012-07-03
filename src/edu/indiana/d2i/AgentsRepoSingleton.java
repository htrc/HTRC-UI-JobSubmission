package edu.indiana.d2i;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.wso2.carbon.registry.core.exceptions.RegistryException;

import edu.indiana.d2i.oauth2.OAuth2Agent;
import edu.indiana.d2i.sigiri.SigiriAgent;
import edu.indiana.d2i.ui.LoginAction;
import edu.indiana.d2i.wso2.WSO2Agent;

public class AgentsRepoSingleton {
	private static AgentsRepoSingleton instance = null;
	private static OAuth2Agent oauth2Agent = null;
	private static WSO2Agent wso2Agent = null;
	private static SigiriAgent sigiriAgent = null;
	private static Properties props = null;

	private AgentsRepoSingleton() throws RegistryException, IOException {
		init();
	}

	private void init() throws IOException, RegistryException {
		InputStream inStream = LoginAction.class.getClassLoader()
				.getResourceAsStream("htrc-job-management.properties");
		props = new Properties();
		props.load(inStream);

		String oauth2ServerURL = props
				.getProperty(Constants.PN_OAUTH2_SERVER_EPR);
		String remoteRegistryUrl = props
				.getProperty(Constants.PN_WSO2_SERVER_EPR);
		String username = props.getProperty(Constants.PN_WSO2_SERVER_UNAME);
		String password = props.getProperty(Constants.PN_WSO2_SERVER_PASS);
		String trueStorePath = props
				.getProperty(Constants.PN_WSO2_TRUESTORE_PATH);
		String storePassword = props.getProperty(Constants.PN_WSO2_STORE_PASS);
		String storeType = props.getProperty(Constants.PN_WSO2_STORE_TYPE);
		String sigiriServerURL = props
				.getProperty(Constants.PN_SIGIRI_SERVER_ERP);
		String axis2Repo = props.getProperty(Constants.PN_WSO2_AXIS2_REPO);
		String axis2Conf = props.getProperty(Constants.PN_WSO2_AXIS2_CONF);

		boolean isSelfSigned = Boolean.valueOf(props
				.getProperty(Constants.PN_OAUTH2_SERVER_SELF_SIGNED));
		oauth2Agent = new OAuth2Agent(oauth2ServerURL, isSelfSigned);
		wso2Agent = new WSO2Agent(remoteRegistryUrl, username, password,
				trueStorePath, storePassword, storeType, axis2Repo, axis2Conf);
		sigiriAgent = new SigiriAgent(sigiriServerURL);
	}

	public static synchronized AgentsRepoSingleton getInstance()
			throws RegistryException, IOException {
		if (instance == null) {
			instance = new AgentsRepoSingleton();
		}
		return instance;
	}

	public OAuth2Agent getOAuth2Agent() {
		return oauth2Agent;
	}

	public WSO2Agent getWSO2Agent() {
		return wso2Agent;
	}

	public SigiriAgent getSigiriAgent() {
		return sigiriAgent;
	}

	public Properties getProps() {
		return props;
	}
}
