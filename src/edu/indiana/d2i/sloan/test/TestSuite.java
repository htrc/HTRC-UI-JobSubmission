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
# File:  TestSuite.java
# Description:  
#
# -----------------------------------------------------------------
# 
 */

package edu.indiana.d2i.sloan.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.commons.httpclient.HttpException;
import org.apache.http.client.ClientProtocolException;

import edu.indiana.d2i.registryext.RegistryExtAgent;
import edu.indiana.d2i.registryext.RegistryExtAgent.GetResourceResponse;
import edu.indiana.d2i.registryext.RegistryExtAgent.ListResourceResponse;
import edu.indiana.d2i.registryext.RegistryExtAgent.ResourceFileType;
import edu.indiana.d2i.registryext.RegistryExtAgent.ResourceISType;
import edu.indiana.d2i.registryext.schema.Entry;
import edu.indiana.d2i.sloan.exception.RegistryExtException;

/**
 * Test class
 * 
 * @author Guangchen
 * 
 */
public class TestSuite {
	private static String registryEPR = "http://htrc4.pti.indiana.edu:9763/ExtensionAPI-0.3.0-SNAPSHOT/services/";
	private static boolean isRegistrySelfSigned = false;
	private static String ACCESS_TOKEN_URL = null;
	private static String CLIENT_ID = null;
	private static String CLIENT_SECRET = null;

	public static void testRegistryExtListChildren()
			throws ClientProtocolException, IllegalStateException,
			RegistryExtException, IOException, JAXBException,
			OAuthSystemException, OAuthProblemException {

		RegistryExtAgent registryAgent = new RegistryExtAgent(registryEPR,
				isRegistrySelfSigned, ACCESS_TOKEN_URL, CLIENT_ID,
				CLIENT_SECRET);

		String repoPath = "/sloan/worksets?recursive=false";

		ListResourceResponse response = registryAgent.getAllChildren(repoPath);

		if (response.getStatusCode() == 404) {
			System.out.println(repoPath + " doesn't exist");
		} else if (response.getStatusCode() == 200) {
			for (Entry entry : response.getEntries().getEntry()) {
				System.out.println("entry =" + entry.getName());
			}
		}

	}

	public static void testRegistryExtDeletion() throws HttpException,
			IOException, RegistryExtException, OAuthSystemException,
			OAuthProblemException {

		RegistryExtAgent registryAgent = new RegistryExtAgent(registryEPR,
				isRegistrySelfSigned, ACCESS_TOKEN_URL, CLIENT_ID,
				CLIENT_SECRET);

		String repoPath = "/a/b/workset.zip";
		registryAgent.deleteResource(repoPath);

	}

	public static void testRegistryExtResExist() throws HttpException,
			IOException, RegistryExtException, OAuthSystemException,
			OAuthProblemException {
		RegistryExtAgent registryAgent = new RegistryExtAgent(registryEPR,
				isRegistrySelfSigned, ACCESS_TOKEN_URL, CLIENT_ID,
				CLIENT_SECRET);

		String repoPath = "/a/b/bar.xml";
		if (registryAgent.isResourceExist(repoPath)) {
			System.out.println(String.format("Resource %s exists", repoPath));
		} else {
			System.out.println(String.format("Resource %s doesn't exist",
					repoPath));
		}

		repoPath = "/c";
		if (registryAgent.isResourceExist(repoPath)) {
			System.out.println(String.format("Resource %s exists", repoPath));
		} else {
			System.out.println(String.format("Resource %s doesn't exist",
					repoPath));
		}
	}

	public static void testRegistryExtPutRes() throws HttpException,
			IOException, RegistryExtException, OAuthSystemException,
			OAuthProblemException {
		RegistryExtAgent registryAgent = new RegistryExtAgent(registryEPR,
				isRegistrySelfSigned, ACCESS_TOKEN_URL, CLIENT_ID,
				CLIENT_SECRET);

		String testFilePath = "D:\\tmp\\jobs.zip";
		String repoPath = "/a/b/workset.zip";

		String destPath = registryAgent.postResource(repoPath,
				new ResourceISType(new FileInputStream(testFilePath),
						"workset.zip", "application/zip"));

		System.out.println("Resource has been posted to " + destPath);
	}

	public static void testRegistryExtPostMultiRes() throws IOException,
			RegistryExtException, OAuthSystemException, OAuthProblemException {
		RegistryExtAgent registryAgent = new RegistryExtAgent(registryEPR,
				isRegistrySelfSigned, ACCESS_TOKEN_URL, CLIENT_ID,
				CLIENT_SECRET);

		final String localTestFilePath1 = "D:\\tmp\\token.tmp";
		final String localTestFilePath2 = "D:\\tmp\\niodev.jar";
		String repoPath = "/c";

		List<ResourceFileType> resourceList = new ArrayList<ResourceFileType>() {
			private static final long serialVersionUID = 1L;

			{
				add(new ResourceFileType(new File(localTestFilePath1),
						"token.tmp", "text/plain"));
				add(new ResourceFileType(new File(localTestFilePath2),
						"niodev.jar", "application/jar"));
			}
		};

		String destPath = registryAgent.postMultiResources(repoPath,
				resourceList);

		System.out.println("Resource has been posted to " + destPath);
	}

	public static void testRegistryExtGetRes() throws HttpException,
			IOException, RegistryExtException, OAuthSystemException,
			OAuthProblemException {
		RegistryExtAgent registryAgent = new RegistryExtAgent(registryEPR,
				isRegistrySelfSigned, ACCESS_TOKEN_URL, CLIENT_ID,
				CLIENT_SECRET);

		String repoPath = "/c/token.tmp";

		GetResourceResponse response = registryAgent.getResource(repoPath);

		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new InputStreamReader(response.getIs()));

			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
		} finally {
			if (reader != null)
				reader.close();
		}

		registryAgent.closeConnection(response.getMethod());
	}

	/**
	 * @param args
	 * @throws JAXBException
	 * @throws IOException
	 * @throws RegistryExtException
	 * @throws IllegalStateException
	 * @throws ClientProtocolException
	 * @throws OAuthProblemException
	 * @throws OAuthSystemException
	 */
	public static void main(String[] args) throws ClientProtocolException,
			IllegalStateException, RegistryExtException, IOException,
			JAXBException, OAuthSystemException, OAuthProblemException {
		// TODO Auto-generated method stub

		testRegistryExtListChildren();
		// testRegistryExtDeletion();
		// testRegistryExtResExist();
		// testRegistryExtPutRes();
		// testRegistryExtPostMultiRes();
		// testRegistryExtGetRes();
	}

}
