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
# File:  WSO2Agent.java
# Description:  
#
# -----------------------------------------------------------------
# 
 */
package edu.indiana.d2i.wso2;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import edu.indiana.d2i.sloan.exception.PathNotExistException;

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
	public static final String separator = "/";

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
	 * To delete symlink, we may have to invoke delete operation twice
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
			logger.error(repoPath + " is not a valid pathname");
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
}
