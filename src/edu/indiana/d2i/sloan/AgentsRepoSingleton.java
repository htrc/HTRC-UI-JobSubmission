package edu.indiana.d2i.sloan;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.wso2.carbon.registry.core.exceptions.RegistryException;

import edu.indiana.d2i.sigiri.SigiriAgent;
import edu.indiana.d2i.sloan.ui.LoginAction;
import edu.indiana.d2i.sloan.ui.PortalConfiguration;
import edu.indiana.d2i.wso2.WSO2Agent;

public class AgentsRepoSingleton {
	private static AgentsRepoSingleton instance = null;
	private static WSO2Agent wso2Agent = null;
	private static SigiriAgent sigiriAgent = null;
	private static Properties props = null;

	private AgentsRepoSingleton() throws RegistryException, IOException {
		init();
	}

	private void init() throws IOException, RegistryException {
		InputStream inStream = LoginAction.class.getClassLoader()
				.getResourceAsStream(Constants.PROPERTY_FNAME);
		props = new Properties();
		props.load(inStream);

		String remoteRegistryUrl = PortalConfiguration.getRegistryEPR();
		String username = PortalConfiguration.getRegistryRootName();
		String password = PortalConfiguration.getRegistryRootPass();
		String trueStorePath = PortalConfiguration.getRegistryStorePath();
		String storePassword = PortalConfiguration.getRegistryStorePwd();
		String storeType = PortalConfiguration.getRegistryStoreType();

		String sigiriServerURL = PortalConfiguration.getSigiriEPR();
		String axis2Repo = PortalConfiguration.getAxis2Repo();
		String axis2Conf = PortalConfiguration.getAxis2Conf();

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
