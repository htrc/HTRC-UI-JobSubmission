package edu.indiana.d2i.registryext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.JAXBException;

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

import edu.indiana.d2i.registryext.schema.Entries;
import edu.indiana.d2i.registryext.schema.Entry;
import edu.indiana.d2i.registryext.schema.FileSchemaUtil;
import edu.indiana.d2i.sloan.exception.RegistryExtException;

public class RegistryExtAgent {
	private static final Logger logger = Logger
			.getLogger(RegistryExtAgent.class);

	private String registryEPR = null;
	public static final String separator = "/";
	
	// prefix for file related operations
	private static String FILEOPPREFIX = "files";

	public RegistryExtAgent(String registryEPR, boolean isSelfSigned) {
		this.registryEPR = registryEPR;

		if (isSelfSigned) {
			if (!disableSSL()) {
				logger.error("Cannot disable SSL");
			}
		}
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
	 * @return
	 * @throws RegistryExtException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws IllegalStateException
	 * @throws JAXBException
	 */
	public ListResourceResponse getAllChildren(String repoPath)
			throws RegistryExtException, ClientProtocolException, IOException,
			IllegalStateException, JAXBException {

		int statusCode = 200;

		String requestURL = composeURL(FILEOPPREFIX, repoPath);

		if (logger.isDebugEnabled()) {
			logger.debug("List request URL=" + requestURL);
		}

		HttpClient httpclient = new HttpClient();
		OptionsMethod options = new OptionsMethod(requestURL);

		options.addRequestHeader("Accept", "application/vnd.htrc-entry+xml");

		try {

			httpclient.executeMethod(options);

			statusCode = options.getStatusLine().getStatusCode();

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

	public void deleteResource(String repoPath) throws HttpException,
			IOException, RegistryExtException {

		String requestURL = composeURL(FILEOPPREFIX, repoPath);

		if (logger.isDebugEnabled()) {
			logger.debug("Deletion request URL=" + requestURL);
		}

		HttpClient httpclient = new HttpClient();
		DeleteMethod delete = new DeleteMethod(requestURL);

		try {

			httpclient.executeMethod(delete);

			if (delete.getStatusLine().getStatusCode() != 204) {
				throw new RegistryExtException(
						"Failed in delete resource : HTTP error code : "
								+ delete.getStatusLine().getStatusCode());
			}

		} finally {
			if (delete != null)
				delete.releaseConnection();
		}

	}

	public boolean isResourceExist(String repoPath) throws HttpException,
			IOException, RegistryExtException {

		boolean exist = true;

		String requestURL = composeURL(FILEOPPREFIX, repoPath);

		if (logger.isDebugEnabled()) {
			logger.debug("Check existence request URL=" + requestURL);
		}

		HttpClient httpclient = new HttpClient();
		HeadMethod head = new HeadMethod(requestURL);

		try {

			httpclient.executeMethod(head);

			int statusCode = head.getStatusLine().getStatusCode();

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
	 * Post single resource
	 * 
	 * @param repoPath
	 * @param is
	 * @param mediaType
	 * @return
	 * @throws RegistryExtException
	 * @throws HttpException
	 * @throws IOException
	 */
	public String postResource(String repoPath, ResourceISType resource)
			throws RegistryExtException, HttpException, IOException {

		String requestURL = composeURL(FILEOPPREFIX, repoPath);

		if (logger.isDebugEnabled()) {
			logger.debug("Put request URL=" + requestURL);
		}

		HttpClient httpclient = new HttpClient();
		PutMethod put = new PutMethod(requestURL);
		put.addRequestHeader("Content-Type", resource.getMediaType());

		put.setRequestEntity(new InputStreamRequestEntity(resource.getIs()));

		try {

			httpclient.executeMethod(put);

			if (put.getStatusLine().getStatusCode() != 204) {
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

	public String postMultiResources(String repoPath,
			List<ResourceFileType> resourceList) throws HttpException,
			IOException, RegistryExtException {

		String requestURL = composeURL(FILEOPPREFIX, repoPath);

		if (logger.isDebugEnabled()) {
			logger.debug("Post request URL=" + requestURL);
		}

		HttpClient httpclient = new HttpClient();
		PostMethod post = new PostMethod(requestURL);
		post.addRequestHeader("Content-Type", "multipart/form-data");

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

	public GetResourceResponse getResource(String repoPath)
			throws HttpException, IOException, RegistryExtException {
		String requestURL = composeURL(FILEOPPREFIX, repoPath);

		if (logger.isDebugEnabled()) {
			logger.debug("Get request URL=" + requestURL);
		}

		HttpClient httpclient = new HttpClient();
		GetMethod get = new GetMethod(requestURL);

		httpclient.executeMethod(get);

		if (get.getStatusLine().getStatusCode() != 200) {
			throw new RegistryExtException(
					"Failed in get resource : HTTP error code : "
							+ get.getStatusLine().getStatusCode());
		}

		return new GetResourceResponse(get.getResponseBodyAsStream(), get);

	}

	public void closeConnection(HttpMethodBase method) {
		if (method != null)
			method.releaseConnection();

	}
}
