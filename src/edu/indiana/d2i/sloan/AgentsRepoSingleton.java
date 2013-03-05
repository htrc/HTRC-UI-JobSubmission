package edu.indiana.d2i.sloan;

import java.io.IOException;

import edu.indiana.d2i.registryext.RegistryExtAgent;
import edu.indiana.d2i.sigiri.SigiriAgent;
import edu.indiana.d2i.sloan.ui.PortalConfiguration;

public class AgentsRepoSingleton {
	private static AgentsRepoSingleton instance = null;
	private static SigiriAgent sigiriAgent = null;
	private static RegistryExtAgent registryExtAgent = null;

	private AgentsRepoSingleton() throws IOException {
		init();
	}

	private void init() throws IOException {
		sigiriAgent = new SigiriAgent(PortalConfiguration.getSigiriEPR());
		registryExtAgent = new RegistryExtAgent(
				PortalConfiguration.getRegistryEPR(),
				PortalConfiguration.getRegistrySelfSign(),
				PortalConfiguration.getOAuth2TokenEndpoint(),
				PortalConfiguration.getOAuth2ClientID(),
				PortalConfiguration.getOAuth2ClientSecrete());
	}

	public static synchronized AgentsRepoSingleton getInstance()
			throws IOException {
		if (instance == null) {
			instance = new AgentsRepoSingleton();
		}
		return instance;
	}

	public SigiriAgent getSigiriAgent() {
		return sigiriAgent;
	}

	public RegistryExtAgent getRegistryExtAgent() {
		return registryExtAgent;
	}

}
