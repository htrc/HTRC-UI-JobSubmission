package edu.indiana.d2i.wso2;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import edu.indiana.d2i.Constants;
import edu.indiana.d2i.exception.PathNotExistException;

public class WSO2Agent {
	private static final Logger logger = Logger.getLogger(WSO2Agent.class);
	private String remoteRegistryUrl;
	private String username;
	private String password;
	private String trueStorePath;
	private String storePassword;
	private String storeType;
	private String axis2Repo;
	private String axis2Conf;
	private WSRegistryServiceClient wsregistry = null;
	private RemoteRegistry registry = null;
	private static final String separator = "/";

	public WSO2Agent(String remoteRegistryUrl, String username,
			String password, String trueStorePath, String storePassword,
			String storeType, String axis2Repo, String axis2Conf)
			throws RegistryException, MalformedURLException, AxisFault {
		super();
		this.remoteRegistryUrl = remoteRegistryUrl;
		this.username = username;
		this.password = password;
		this.trueStorePath = trueStorePath;
		this.storePassword = storePassword;
		this.storeType = storeType;
		this.axis2Repo = axis2Repo;
		this.axis2Conf = axis2Conf;

		init();
	}

	private void init() throws RegistryException, MalformedURLException,
			AxisFault {
		System.setProperty("javax.net.ssl.trustStore", trueStorePath);
		System.setProperty("javax.net.ssl.trustStorePassword", storePassword);
		System.setProperty("javax.net.ssl.trustStoreType", storeType);

		ConfigurationContext configContext = ConfigurationContextFactory
				.createConfigurationContextFromFileSystem(axis2Repo, axis2Conf);
		wsregistry = new WSRegistryServiceClient(remoteRegistryUrl
				+ "services/", username, password, configContext);

		registry = new RemoteRegistry(new URL(remoteRegistryUrl + "registry"),
				username, password);
	}

	public InputStream getResource(String pathToResource)
			throws RegistryException {
		Resource resource = registry.get(pathToResource);
		return resource.getContentStream();
	}

	public Resource getRawResource(String pathToResource)
			throws RegistryException {
		return registry.get(pathToResource);
	}

	public boolean isResourceExist(String resourcePathName)
			throws RegistryException {
		return registry.resourceExists(resourcePathName);
	}

	public String createDir(String pathName) throws RegistryException {
		if (registry.resourceExists(pathName))
			return pathName;

		Collection collection = registry.newCollection();
		return registry.put(pathName, collection);
	}

	public void updateResource(String pathToResource, Resource res)
			throws RegistryException {
		registry.put(pathToResource, res);
	}

	public String postResource(String pathToResource, Object resourceToPost,
			String mediaType) throws RegistryException {
		/**
		 * check whether the collection/path exists first
		 */
		String pathName = null;
		int idx = pathToResource.lastIndexOf(WSO2Agent.separator);
		if (idx > 0) {
			// deal with the case that resource to be posted not directly under
			// root
			pathName = pathToResource.substring(0, idx);
			if (!registry.resourceExists(pathName)) {
				if (logger.isDebugEnabled())
					logger.debug(pathName + " doesn't exist, create it first!");
				Collection collection = registry.newCollection();
				registry.put(pathName, collection);
			}
		}

		Resource resource;
		resource = registry.newResource();
		resource.setContent(resourceToPost);
		resource.setMediaType(mediaType);
		String resultOfPut = registry.put(pathToResource, resource);

		return resultOfPut;
	}

	/**
	 * To delete symlink, we may have to invoke
	 * 
	 * @param pathToResource
	 * @throws RegistryException
	 */
	public void deleteResource(String pathToResource) throws RegistryException {
		registry.delete(pathToResource);

		/**
		 * If it is symlink, we may have to delete twice
		 */
		if (registry.resourceExists(pathToResource)) {
			registry.delete(pathToResource);
		}
	}

	/**
	 * Get all children names under the folder specified by repoPath
	 * 
	 * @param repoPath
	 * @return
	 * @throws RegistryException
	 * @throws PathNotExistException
	 */
	public List<String> getAllChildren(String repoPath)
			throws RegistryException, PathNotExistException {
		/**
		 * Check whether repoPath exists
		 */
		if (!registry.resourceExists(repoPath))
			throw new PathNotExistException(repoPath + " doesn't exist");

		Resource repo = registry.get(repoPath);
		if (!(repo instanceof Collection)) {
			System.err.println(repoPath + " is not a valid pathname");
			return null;
		}

		Collection userRepo = (Collection) repo;
		if (logger.isDebugEnabled()) {
			logger.debug("Pathname: " + repoPath);
			logger.debug("# of entries : " + userRepo.getChildCount());
		}
		String[] children = userRepo.getChildren();

		List<String> entries = new ArrayList<String>();
		for (String child : children) {
			if (logger.isDebugEnabled()) {
				logger.debug("entry : " + child);
			}

			int idx = child.lastIndexOf(WSO2Agent.separator);
			entries.add(child.substring(idx + 1));

		}
		return entries;
	}

	public String copy(String sourcePath, String targetPath)
			throws RegistryException {
		return registry.copy(sourcePath, targetPath);
	}

	public void createSymLink(String linkPath, String resPath)
			throws RegistryException {
		wsregistry.createLink(linkPath, resPath);
	}

	/**
	 * Simple test case
	 * 
	 * @param args
	 * @throws IOException
	 * @throws RegistryException
	 * @throws PathNotExistException
	 */
	public static void main(String[] args) throws IOException,
			RegistryException, PathNotExistException {

		String remoteRegistryUrl = "https://coffeetree.cs.indiana.edu:9445/";
		String username = "admin";
		String passwd = "BillionsOfPages11";
		String trueStorePath = "resources/client-truststore.jks";
		String storePassword = "wso2carbon";
		String storeType = "JKS";
		String axis2Repo = "D:\\workspace\\java_workspace\\HTRCJobManagementService\\WebContent\\wso2_axis2repo";
		String axis2Conf = "D:\\workspace\\java_workspace\\HTRCJobManagementService\\WebContent\\wso2_axis2conf\\axis2_client.xml";

		WSO2Agent wso2Agent = new WSO2Agent(remoteRegistryUrl, username,
				passwd, trueStorePath, storePassword, storeType, axis2Repo,
				axis2Conf);

		// String result = wso2Agent.postResource("/htrc/repo/yim/test.txt",
		// "A test doc", "Some descriptions");
		// System.out.println("Result = " + result);
		//
		// wso2Agent
		// .deleteResource("/htrc/repo/yim/matlabplot/dependancy/plot_client.m");
		//
		// wso2Agent.getAllChildren("/htrc/repo/yim/matlabplot/dependancy");

		// wso2Agent.createSymLink("/htrc/repo/yim/symlinkfile",
		// "/htrc/resource/executable/public/hello.sh");

		// Resource res = wso2Agent
		// .getRawResource("/htrc/repo/yim/ss/job.properties");
		// System.out.println("Created time : " + res.getCreatedTime());
		// System.out.println("Media Type : " + res.getMediaType());
		// System.out.println("Author name : " + res.getAuthorUserName());
		// System.out.println("Property : " + res.getProperty("job.title"));
		//
		// Properties prop = res.getProperties();
		// System.out.println("Property : " + prop.getProperty("job.title"));
		// Set<String> propNames = prop.stringPropertyNames();
		// System.out.println("# of props : " + propNames.size());
		// for (String name : propNames) {
		// System.out.println("name : " + name + " value : "
		// + prop.getProperty(name));
		// }
		//
		// res.addProperty("author", "guangchen");
		// wso2Agent.updateResource("/htrc/repo/yim/ss/job.properties", res);

		// wso2Agent.removeSymLink("/htrc/repo/yim/5118be03-f37b-4b49-ab13-ec114d634414/dependancy/htrc-app-0.2.jar");
		// wso2Agent.deleteResource("/htrc/repo/yim/5118be03-f37b-4b49-ab13-ec114d634414/dependancy/Ray-Tracer-API-B534-V1.jar");

		// wso2Agent.removeSymLink("/htrc/repo/yim/ss/dependancy/rings.jar");
		// String path = wso2Agent
		// .copy("/htrc/repo/yim/d3eb57c6-2965-4f5a-861e-8fd955dc7e4c/dependancy/rings.jar",
		// "/htrc/repo/yim/ss/dependancy/rings.jar");
		// System.out.println(path);
		// InputStream is =
		// wso2Agent.getResource("/htrc/repo/yim/d3eb57c6-2965-4f5a-861e-8fd955dc7e4c/job.properties");
		//
		// Properties jobProp = new Properties();
		// jobProp.load(is);
		// System.out.println("Job Id = "
		// + jobProp.getProperty(Constants.SIGIRI_JOB_ID));

		wso2Agent
				.deleteResource("/htrc/repo/yim/a009c862-a93f-4fbe-9602-c77391cdb090/dependancy/rings.jar");
		System.out.println("done");
	}
}
