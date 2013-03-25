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
# File:  RegistryExtAgent.java
# Description:  
#
# -----------------------------------------------------------------
# 
 */
package edu.indiana.d2i.registryext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.JAXBException;

import org.apache.amber.oauth2.client.OAuthClient;
import org.apache.amber.oauth2.client.URLConnectionClient;
import org.apache.amber.oauth2.client.request.OAuthClientRequest;
import org.apache.amber.oauth2.client.response.OAuthClientResponse;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.types.GrantType;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.FilePartSource;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;

import com.opensymphony.xwork2.ActionContext;

import edu.indiana.d2i.registryext.schema.Entries;
import edu.indiana.d2i.registryext.schema.Entry;
import edu.indiana.d2i.registryext.schema.FileSchemaUtil;
import edu.indiana.d2i.sloan.Constants;
import edu.indiana.d2i.sloan.exception.RegistryExtException;

/**
 * Agent which encapsulates methods used to access registry extension
 * 
 * @author Guangchen
 * 
 */
public class RegistryExtAgent {
	private static final Logger logger = Logger
			.getLogger(RegistryExtAgent.class);

	private String registryEPR = null;

	private String ACCESS_TOKEN_URL = null;
	private String CLIENT_ID = null;
	private String CLIENT_SECRET = null;

	public static final String separator = "/";

	// prefix for file related operations
	private static String FILEOPPREFIX = "files";

	public RegistryExtAgent(String registryEPR, boolean isSelfSigned,
			String ACCESS_TOKEN_URL, String CLIENT_ID, String CLIENT_SECRET) {
		this.registryEPR = registryEPR;
		this.ACCESS_TOKEN_URL = ACCESS_TOKEN_URL;
		this.CLIENT_ID = CLIENT_ID;
		this.CLIENT_SECRET = CLIENT_SECRET;

		if (isSelfSigned) {
			if (!disableSSL()) {
				logger.error("Cannot disable SSL");
			}
		}
	}

	private void refreshToken(Map<String, Object> session)
			throws OAuthSystemException, OAuthProblemException {
		String refreshToken = (String) session
				.get(Constants.SESSION_REFRESH_TOKEN);

		OAuthClientRequest refreshTokenRequest = OAuthClientRequest
				.tokenLocation(ACCESS_TOKEN_URL)
				.setGrantType(GrantType.REFRESH_TOKEN)
				.setRefreshToken(refreshToken).setClientId(CLIENT_ID)
				.setClientSecret(CLIENT_SECRET).buildBodyMessage();

		OAuthClient refreshTokenClient = new OAuthClient(
				new URLConnectionClient());
		OAuthClientResponse refreshTokenResponse = refreshTokenClient
				.accessToken(refreshTokenRequest);

		String refreshedAccessToken = refreshTokenResponse
				.getParam(Constants.OAUTH2_ACCESS_TOKEN);

		// Update access token
		session.put(Constants.SESSION_TOKEN, refreshedAccessToken);
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
			return false;
		} catch (KeyManagementException e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	private String composeURL(String prefix, String remainder) {
		StringBuilder sb = new StringBuilder();
		return sb.append(registryEPR).append(prefix).append(remainder)
				.toString();

	}

	/**
	 * Class represents returned list when {@link #getAllChildren(String)}
	 * method is called
	 * 
	 * @author Guangchen
	 * 
	 */
	public static class ListResourceResponse {
		private Entries entries;
		private int statusCode;

		public ListResourceResponse(Entries entries, int statusCode) {
			this.entries = entries;
			this.statusCode = statusCode;
		}

		public Entries getEntries() {
			return entries;
		}

		public int getStatusCode() {
			return statusCode;
		}

	}

	/**
	 * Class represents response of get request method
	 * {@link #getResource(String)} method is called
	 * 
	 * @author Guangchen
	 * 
	 */
	public static class GetResourceResponse {
		private InputStream is;
		private HttpMethodBase method;

		public GetResourceResponse(InputStream is, HttpMethodBase method) {
			this.is = is;
			this.method = method;
		}

		public InputStream getIs() {
			return is;
		}

		public HttpMethodBase getMethod() {
			return method;
		}
	}

	/**
	 * Resource in form of InputStream
	 * 
	 * @author Guangchen
	 * 
	 */
	public static class ResourceISType {
		private InputStream is;
		// name used in registry
		private String name;
		private String mediaType;

		public ResourceISType(InputStream is, String name, String mediaType) {
			this.is = is;
			this.name = name;
			this.mediaType = mediaType;

		}

		public InputStream getIs() {
			return is;
		}

		public String getName() {
			return name;
		}

		public String getMediaType() {
			return mediaType;
		}

	}

	/**
	 * Resource in form of File
	 * 
	 * @author Guangchen
	 * 
	 */
	public static class ResourceFileType {
		private File file;
		// name used in registry
		private String name;
		private String mediaType;

		public ResourceFileType(File file, String name, String mediaType) {
			this.file = file;
			this.name = name;
			this.mediaType = mediaType;

		}

		public File getFile() {
			return file;
		}

		public String getName() {
			return name;
		}

		public String getMediaType() {
			return mediaType;
		}

	}

	/**
	 * Only return file names (no directory)
	 * 
	 * @param repoPath
	 *            path in registry
	 * @return
	 * @throws RegistryExtException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws IllegalStateException
	 * @throws JAXBException
	 * @throws OAuthProblemException
	 * @throws OAuthSystemException
	 */
	public ListResourceResponse getAllChildren(String repoPath)
			throws RegistryExtException, ClientProtocolException, IOException,
			IllegalStateException, JAXBException, OAuthSystemException,
			OAuthProblemException {

		Map<String, Object> session = ActionContext.getContext().getSession();

		String accessToken = (String) session.get(Constants.SESSION_TOKEN);
		int statusCode = 200;

		String requestURL = composeURL(FILEOPPREFIX, repoPath);

		if (logger.isDebugEnabled()) {
			logger.debug("List request URL=" + requestURL);
		}

		HttpClient httpclient = new HttpClient();
		OptionsMethod options = new OptionsMethod(requestURL);

		options.addRequestHeader("Accept", "application/xml");
		options.addRequestHeader("Authorization", "Bearer " + accessToken);

		try {

			httpclient.executeMethod(options);

			statusCode = options.getStatusLine().getStatusCode();

			/* handle token expiration */
			if (statusCode == 401) {
				logger.info(String.format(
						"Access token %s expired, going to refresh it",
						accessToken));

				refreshToken(session);

				// use refreshed access token
				accessToken = (String) session.get(Constants.SESSION_TOKEN);
				options.addRequestHeader("Authorization", "Bearer "
						+ accessToken);

				// re-send the request
				httpclient.executeMethod(options);

				statusCode = options.getStatusLine().getStatusCode();

			}

			if (statusCode == 404) {
				logger.equals("List request URL=" + requestURL
						+ " doesn't exist");
				return new ListResourceResponse(null, statusCode);
			}

			if (statusCode != 200) {
				throw new RegistryExtException(
						"Failed in listing children : HTTP error code : "
								+ options.getStatusLine().getStatusCode());
			}

			Entry xmlFile = FileSchemaUtil.readConfigXML(options
					.getResponseBodyAsStream());

			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Returned content:%n%s",
						FileSchemaUtil.toXMLString(xmlFile)));

			}

			Entries entries = xmlFile.getEntries();

			return new ListResourceResponse(entries, statusCode);
		} finally {
			if (options != null)
				options.releaseConnection();
		}

	}

	/**
	 * delete a single resource
	 * 
	 * @param repoPath
	 *            path in registry
	 * @throws HttpException
	 * @throws IOException
	 * @throws RegistryExtException
	 * @throws OAuthSystemException
	 * @throws OAuthProblemException
	 */
	public void deleteResource(String repoPath) throws HttpException,
			IOException, RegistryExtException, OAuthSystemException,
			OAuthProblemException {

		Map<String, Object> session = ActionContext.getContext().getSession();

		int statusCode = 200;
		String accessToken = (String) session.get(Constants.SESSION_TOKEN);

		String requestURL = composeURL(FILEOPPREFIX, repoPath);

		if (logger.isDebugEnabled()) {
			logger.debug("Deletion request URL=" + requestURL);
		}

		HttpClient httpclient = new HttpClient();
		DeleteMethod delete = new DeleteMethod(requestURL);
		delete.addRequestHeader("Authorization", "Bearer " + accessToken);

		try {

			httpclient.executeMethod(delete);

			statusCode = delete.getStatusLine().getStatusCode();
			/* handle token expiration */
			if (statusCode == 401) {
				logger.info(String.format(
						"Access token %s expired, going to refresh it",
						accessToken));

				refreshToken(session);

				// use refreshed access token
				accessToken = (String) session.get(Constants.SESSION_TOKEN);
				delete.addRequestHeader("Authorization", "Bearer "
						+ accessToken);

				// re-send the request
				httpclient.executeMethod(delete);

				statusCode = delete.getStatusLine().getStatusCode();

			}

			if (statusCode != 204) {
				throw new RegistryExtException(
						"Failed in delete resource : HTTP error code : "
								+ delete.getStatusLine().getStatusCode());
			}

		} finally {
			if (delete != null)
				delete.releaseConnection();
		}

	}

	/**
	 * check whether a resource exists
	 * 
	 * @param repoPath
	 *            path in registry
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 * @throws RegistryExtException
	 * @throws OAuthSystemException
	 * @throws OAuthProblemException
	 */
	public boolean isResourceExist(String repoPath) throws HttpException,
			IOException, RegistryExtException, OAuthSystemException,
			OAuthProblemException {
		Map<String, Object> session = ActionContext.getContext().getSession();

		int statusCode = 200;
		String accessToken = (String) session.get(Constants.SESSION_TOKEN);

		boolean exist = true;

		String requestURL = composeURL(FILEOPPREFIX, repoPath);

		if (logger.isDebugEnabled()) {
			logger.debug("Check existence request URL=" + requestURL);
		}

		HttpClient httpclient = new HttpClient();
		HeadMethod head = new HeadMethod(requestURL);
		head.addRequestHeader("Authorization", "Bearer " + accessToken);

		try {

			httpclient.executeMethod(head);

			statusCode = head.getStatusLine().getStatusCode();

			/* handle token expiration */
			if (statusCode == 401) {
				logger.info(String.format(
						"Access token %s expired, going to refresh it",
						accessToken));

				refreshToken(session);

				// use refreshed access token
				accessToken = (String) session.get(Constants.SESSION_TOKEN);
				head.addRequestHeader("Authorization", "Bearer " + accessToken);

				// re-send the request
				httpclient.executeMethod(head);

				statusCode = head.getStatusLine().getStatusCode();

			}

			if (statusCode == 404)
				exist = false;
			else if (statusCode != 200) {
				throw new RegistryExtException(
						"Failed in checking resource existence: HTTP error code : "
								+ statusCode);
			}

		} finally {
			if (head != null)
				head.releaseConnection();
		}
		return exist;
	}

	/**
	 * post a resource
	 * 
	 * @param repoPath
	 *            path in registry
	 * @param resource
	 *            resource to be posted, given in as input stream
	 * @return
	 * @throws RegistryExtException
	 * @throws HttpException
	 * @throws IOException
	 * @throws OAuthSystemException
	 * @throws OAuthProblemException
	 */
	public String postResource(String repoPath, ResourceISType resource)
			throws RegistryExtException, HttpException, IOException,
			OAuthSystemException, OAuthProblemException {

		Map<String, Object> session = ActionContext.getContext().getSession();

		int statusCode = 200;
		String accessToken = (String) session.get(Constants.SESSION_TOKEN);

		String requestURL = composeURL(FILEOPPREFIX, repoPath);

		if (logger.isDebugEnabled()) {
			logger.debug("Put request URL=" + requestURL);
		}

		HttpClient httpclient = new HttpClient();
		PutMethod put = new PutMethod(requestURL);
		put.addRequestHeader("Content-Type", resource.getMediaType());
		put.addRequestHeader("Authorization", "Bearer " + accessToken);
		put.setRequestEntity(new InputStreamRequestEntity(resource.getIs()));

		try {

			httpclient.executeMethod(put);
			statusCode = put.getStatusLine().getStatusCode();

			/* handle token expiration */
			if (statusCode == 401) {
				logger.info(String.format(
						"Access token %s expired, going to refresh it",
						accessToken));

				refreshToken(session);

				// use refreshed access token
				accessToken = (String) session.get(Constants.SESSION_TOKEN);
				put.addRequestHeader("Authorization", "Bearer " + accessToken);

				// re-send the request
				httpclient.executeMethod(put);

				statusCode = put.getStatusLine().getStatusCode();

			}

			if (statusCode != 204) {
				throw new RegistryExtException(
						"Failed in put resource : HTTP error code : "
								+ put.getStatusLine().getStatusCode());
			}

		} finally {
			if (put != null)
				put.releaseConnection();
		}

		return repoPath;
	}

	/**
	 * post multiple resources through a single call, all resources are posted
	 * under 'repoPath'
	 * 
	 * @param repoPath
	 *            path in registry where resources should be posted
	 * @param resourceList
	 *            list of resources to be posted
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 * @throws RegistryExtException
	 * @throws OAuthSystemException
	 * @throws OAuthProblemException
	 */
	public String postMultiResources(String repoPath,
			List<ResourceFileType> resourceList) throws HttpException,
			IOException, RegistryExtException, OAuthSystemException,
			OAuthProblemException {

		Map<String, Object> session = ActionContext.getContext().getSession();

		int statusCode = 200;
		String accessToken = (String) session.get(Constants.SESSION_TOKEN);

		String requestURL = composeURL(FILEOPPREFIX, repoPath);

		if (logger.isDebugEnabled()) {
			logger.debug("Post request URL=" + requestURL);
		}

		HttpClient httpclient = new HttpClient();
		PostMethod post = new PostMethod(requestURL);
		post.addRequestHeader("Content-Type", "multipart/form-data");
		post.addRequestHeader("Authorization", "Bearer " + accessToken);

		Part[] parts = new Part[resourceList.size()];
		for (int i = 0; i < resourceList.size(); i++) {
			parts[i] = new FilePart(resourceList.get(i).getName(),
					new FilePartSource(resourceList.get(i).getName(),
							resourceList.get(i).getFile()));
		}

		post.setRequestEntity(new MultipartRequestEntity(parts, post
				.getParams()));

		try {

			httpclient.executeMethod(post);

			statusCode = post.getStatusLine().getStatusCode();

			/* handle token expiration */
			if (statusCode == 401) {
				logger.info(String.format(
						"Access token %s expired, going to refresh it",
						accessToken));

				refreshToken(session);

				// use refreshed access token
				accessToken = (String) session.get(Constants.SESSION_TOKEN);
				post.addRequestHeader("Authorization", "Bearer " + accessToken);

				// re-send the request
				httpclient.executeMethod(post);

				statusCode = post.getStatusLine().getStatusCode();

			}

			if (post.getStatusLine().getStatusCode() != 204) {
				throw new RegistryExtException(
						"Failed in post resources : HTTP error code : "
								+ post.getStatusLine().getStatusCode());
			}

		} finally {
			if (post != null)
				post.releaseConnection();
		}

		return repoPath;
	}

	/**
	 * retrieve a resource
	 * 
	 * @param repoPath
	 *            path in registry
	 * @return
	 * @throws HttpException
	 * @throws IOException
	 * @throws RegistryExtException
	 * @throws OAuthSystemException
	 * @throws OAuthProblemException
	 */
	public GetResourceResponse getResource(String repoPath)
			throws HttpException, IOException, RegistryExtException,
			OAuthSystemException, OAuthProblemException {

		Map<String, Object> session = ActionContext.getContext().getSession();

		int statusCode = 200;
		String accessToken = (String) session.get(Constants.SESSION_TOKEN);
		String requestURL = composeURL(FILEOPPREFIX, repoPath);

		if (logger.isDebugEnabled()) {
			logger.debug("Get request URL=" + requestURL);
		}

		HttpClient httpclient = new HttpClient();
		GetMethod get = new GetMethod(requestURL);
		get.addRequestHeader("Authorization", "Bearer " + accessToken);

		httpclient.executeMethod(get);

		statusCode = get.getStatusLine().getStatusCode();

		/* handle token expiration */
		if (statusCode == 401) {
			logger.info(String
					.format("Access token %s expired, going to refresh it",
							accessToken));

			refreshToken(session);

			// use refreshed access token
			accessToken = (String) session.get(Constants.SESSION_TOKEN);
			get.addRequestHeader("Authorization", "Bearer " + accessToken);

			// re-send the request
			httpclient.executeMethod(get);

			statusCode = get.getStatusLine().getStatusCode();

		}

		if (statusCode != 200) {
			throw new RegistryExtException(
					"Failed in get resource : HTTP error code : "
							+ get.getStatusLine().getStatusCode());
		}

		return new GetResourceResponse(get.getResponseBodyAsStream(), get);

	}

	/**
	 * close connection
	 * 
	 * @param method
	 *            http method type
	 */
	public void closeConnection(HttpMethodBase method) {
		if (method != null)
			method.releaseConnection();

	}
}
