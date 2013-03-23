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
# File:  AgentsRepoSingleton.java
# Description:  
#
# -----------------------------------------------------------------
# 
 */
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
