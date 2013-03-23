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
# File:  PortalConfiguration.java
# Description:  
#
# -----------------------------------------------------------------
# 
 */
package edu.indiana.d2i.sloan.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import edu.indiana.d2i.sloan.Constants;

public class PortalConfiguration {

	// oauth2 properties
	private static String authEndpoint = null;
	private static String tokenEndpoint = null;
	private static String userinfoEndpoint = null;
	private static String clientID = null;
	private static String clientSecrete = null;
	private static String authScope = null;
	private static String authType = null;

	// registry properties
	private static String registryEPR = null;
	private static Boolean registrySelfSign = false;

	/* workset home */
	private static String registryWorksetPrefix = null;

	/* job home */
	private static String registryJobPrefix = null;

	/* job archive folder name */
	private static String registryArchiveFolder = null;

	// sigiri properties
	private static String sigiriEPR = null;

	private static void loadProperties() throws IOException {
		// Technically this method should be synchronized, since it is accessed
		// by many request threads. But it does not hurt even if we load the
		// property file several times occasionally.
		InputStream inStream = LoginAction.class.getClassLoader()
				.getResourceAsStream(Constants.PROPERTY_FNAME);
		Properties props = new Properties();
		props.load(inStream);

		// oauth properties
		authEndpoint = props.getProperty(Constants.PN_OAUTH2_AUTH_EPR,
				"https://localhost:9443/oauth2/authorize");
		tokenEndpoint = props.getProperty(Constants.PN_OAUTH2_TOKEN_EPR,
				"https://localhost:9443/oauth2endpoints/token");
		userinfoEndpoint = props.getProperty(Constants.PN_OAUTH2_USERINFO_EPR,
				"https://localhost:9443/oauth2userinfo/userinfo");
		clientID = props.getProperty(Constants.PN_OAUTH2_CLIENT_ID);
		clientSecrete = props.getProperty(Constants.PN_OAUTH2_CLIENT_PASS);
		authType = props.getProperty(Constants.PN_OAUTH2_GRANT_TYPE, "code");
		authScope = props.getProperty(Constants.PN_OAUTH2_SCOPE, "default");

		// registry properties
		registryEPR = props.getProperty(Constants.PN_WSO2_SERVER_EPR,
				"https://localhost:9443/ExtensionAPI-0.1.0/services/");
		registrySelfSign = Boolean.parseBoolean(props.getProperty(
				Constants.PN_WSO2_SELF_SIGN, "false"));

		registryWorksetPrefix = props.getProperty(
				Constants.PN_WSO2_WORKSET_PREFIX, "/sloan/worksets/");
		registryJobPrefix = props.getProperty(Constants.PN_WSO2_JOB_PREFIX,
				"/sloan/jobs/");
		registryArchiveFolder = props.getProperty(
				Constants.PN_WSO2_REPO_JOB_ARCHIVE, "archive");

		// sigiri properties
		sigiriEPR = props.getProperty(Constants.PN_SIGIRI_SERVER_EPR,
				"http://localhost:8080/axis2/services/SigiriService");
	}

	/** OAuth2 related properties */
	public static String getOAuth2AuthEndpoint() throws IOException {
		if (authEndpoint == null)
			loadProperties();
		return authEndpoint;
	}

	public static String getOAuth2TokenEndpoint() throws IOException {
		if (tokenEndpoint == null)
			loadProperties();
		return tokenEndpoint;
	}

	public static String getOAuth2UserinfoEndpoint() throws IOException {
		if (userinfoEndpoint == null)
			loadProperties();
		return userinfoEndpoint;
	}

	public static String getOAuth2ClientID() throws IOException {
		if (clientID == null)
			loadProperties();
		return clientID;
	}

	public static String getOAuth2ClientSecrete() throws IOException {
		if (clientSecrete == null)
			loadProperties();
		return clientSecrete;
	}

	public static String getOAuth2AuthScope() throws IOException {
		if (authScope == null)
			loadProperties();
		return authScope;
	}

	public static String getOAuth2AuthType() throws IOException {
		if (authType == null)
			loadProperties();
		return authType;
	}

	/** Registry related properties */
	public static String getRegistryEPR() throws IOException {
		if (registryEPR == null)
			loadProperties();
		return registryEPR;
	}

	public static String getRegistryWorksetPrefix() throws IOException {
		if (registryWorksetPrefix == null)
			loadProperties();
		return registryWorksetPrefix;
	}

	public static String getSigiriEPR() throws IOException {
		if (sigiriEPR == null)
			loadProperties();
		return sigiriEPR;
	}

	public static String getRegistryArchiveFolder() throws IOException {
		if (registryArchiveFolder == null)
			loadProperties();
		return registryArchiveFolder;
	}

	public static Boolean getRegistrySelfSign() throws IOException {
		if (registrySelfSign == null)
			loadProperties();
		return registrySelfSign;
	}

	public static String getRegistryJobPrefix() throws IOException {
		if (registryJobPrefix == null)
			loadProperties();
		return registryJobPrefix;
	}

}
