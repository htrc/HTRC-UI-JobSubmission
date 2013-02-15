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
	private static String registryRootName = null;
	private static String registryRootPass = null;
	private static String registryStorePath = null;
	private static String registryStorePwd = null;
	private static String registryStoreType = null;

	/* repo home */
	private static String registryPrefix = null;

	/* workset home */
	private static String registryWorksetPrefix = null;

	/* tmpoutput home */
	private static String registryTmpOutputPrefix = null;

	/* job archive folder name */
	private static String registryArchiveFolder = null;

	private static String axis2Repo = null;
	private static String axis2Conf = null;

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
				"https://coffeetree.cs.indiana.edu:9445/");
		registryRootName = props.getProperty(Constants.PN_WSO2_ROOT_UNAME,
				"admin");
		registryRootPass = props.getProperty(Constants.PN_WSO2_ROOT_PASS,
				"BillionsOfPages11");
		registryStorePath = props.getProperty(Constants.PN_WSO2_TRUESTORE_PATH,
				"client-truststore.jks");
		registryStorePwd = props.getProperty(Constants.PN_WSO2_STORE_PASS,
				"wso2carbon");
		registryStoreType = props.getProperty(Constants.PN_WSO2_STORE_TYPE,
				"JKS");
		registryPrefix = props.getProperty(Constants.PN_WSO2_REPO_PREFIX,
				"/htrc/repo/");
		registryWorksetPrefix = props.getProperty(
				Constants.PN_WSO2_WORKSET_PREFIX, "/htrc/workset/");
		registryTmpOutputPrefix = props.getProperty(
				Constants.PN_WSO2_TMPOUTPUT_PREFIX, "/htrc/tmpoutput/");
		registryArchiveFolder = props.getProperty(
				Constants.PN_WSO2_REPO_JOB_ARCHIVE, "archive");
		axis2Repo = props.getProperty(Constants.PN_WSO2_AXIS2_REPO,
				"wso2_axis2repo");
		axis2Conf = props.getProperty(Constants.PN_WSO2_AXIS2_CONF,
				"wso2_axis2conf/axis2_client.xml");

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

	public static String getRegistryStorePath() throws IOException {
		if (registryStorePath == null)
			loadProperties();
		return registryStorePath;
	}

	public static String getRegistryStorePwd() throws IOException {
		if (registryStorePwd == null)
			loadProperties();
		return registryStorePwd;
	}

	public static String getRegistryStoreType() throws IOException {
		if (registryStoreType == null)
			loadProperties();
		return registryStoreType;
	}

	public static String getRegistryPrefix() throws IOException {
		if (registryPrefix == null)
			loadProperties();
		return registryPrefix;
	}

	public static String getRegistryWorksetPrefix() throws IOException {
		if (registryWorksetPrefix == null)
			loadProperties();
		return registryWorksetPrefix;
	}

	public static String getRegistryTmpOutputPrefix() throws IOException {
		if (registryTmpOutputPrefix == null)
			loadProperties();
		return registryTmpOutputPrefix;
	}

	public static String getRegistryRootName() throws IOException {
		if (registryRootName == null)
			loadProperties();
		return registryRootName;
	}

	public static String getRegistryRootPass() throws IOException {
		if (registryRootPass == null)
			loadProperties();
		return registryRootPass;
	}

	public static String getAxis2Repo() throws IOException {
		if (axis2Repo == null)
			loadProperties();
		return axis2Repo;
	}

	public static String getAxis2Conf() throws IOException {
		if (axis2Conf == null)
			loadProperties();
		return axis2Conf;
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
}
