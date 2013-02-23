package edu.indiana.d2i.sloan.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.httpclient.HttpException;
import org.apache.http.client.ClientProtocolException;

import edu.indiana.d2i.registryext.RegistryExtAgent;
import edu.indiana.d2i.registryext.RegistryExtAgent.GetResourceResponse;
import edu.indiana.d2i.registryext.RegistryExtAgent.ListResourceResponse;
import edu.indiana.d2i.registryext.RegistryExtAgent.ResourceFileType;
import edu.indiana.d2i.registryext.RegistryExtAgent.ResourceISType;
import edu.indiana.d2i.registryext.schema.Entry;
import edu.indiana.d2i.sloan.exception.RegistryExtException;

public class TestSuite {
	private static String registryEPR = "https://htrc4.pti.indiana.edu:9443/ExtensionAPI-0.1.0/services/";
	private static boolean isRegistrySelfSigned = false;

	public static void testRegistryExtListChildren()
			throws ClientProtocolException, IllegalStateException,
			RegistryExtException, IOException, JAXBException {

		RegistryExtAgent registryAgent = new RegistryExtAgent(registryEPR,
				isRegistrySelfSigned);

		String repoPath = "?recursive=false&user=gruan";

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
			IOException, RegistryExtException {

		RegistryExtAgent registryAgent = new RegistryExtAgent(registryEPR,
				isRegistrySelfSigned);

		String repoPath = "/a/b/workset.zip?user=gruan";
		registryAgent.deleteResource(repoPath);

	}

	public static void testRegistryExtResExist() throws HttpException,
			IOException, RegistryExtException {
		RegistryExtAgent registryAgent = new RegistryExtAgent(registryEPR,
				isRegistrySelfSigned);

		String repoPath = "/a/b/bar.xml?user=gruan";
		if (registryAgent.isResourceExist(repoPath)) {
			System.out.println(String.format("Resource %s exists", repoPath));
		} else {
			System.out.println(String.format("Resource %s doesn't exist",
					repoPath));
		}

		repoPath = "/c?user=gruan";
		if (registryAgent.isResourceExist(repoPath)) {
			System.out.println(String.format("Resource %s exists", repoPath));
		} else {
			System.out.println(String.format("Resource %s doesn't exist",
					repoPath));
		}
	}

	public static void testRegistryExtPutRes() throws HttpException,
			IOException, RegistryExtException {
		RegistryExtAgent registryAgent = new RegistryExtAgent(registryEPR,
				isRegistrySelfSigned);

		String testFilePath = "D:\\tmp\\jobs.zip";
		String repoPath = "/a/b/workset.zip?user=gruan";

		String destPath = registryAgent.postResource(repoPath,
				new ResourceISType(new FileInputStream(testFilePath),
						"workset.zip", "application/zip"));

		System.out.println("Resource has been posted to " + destPath);
	}

	public static void testRegistryExtPostMultiRes() throws IOException,
			RegistryExtException {
		RegistryExtAgent registryAgent = new RegistryExtAgent(registryEPR,
				isRegistrySelfSigned);

		final String localTestFilePath1 = "D:\\tmp\\token.tmp";
		final String localTestFilePath2 = "D:\\tmp\\niodev.jar";
		String repoPath = "/c?user=gruan";

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
			IOException, RegistryExtException {
		RegistryExtAgent registryAgent = new RegistryExtAgent(registryEPR,
				isRegistrySelfSigned);

		String repoPath = "/c/token.tmp?user=gruan";

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
	 */
	public static void main(String[] args) throws ClientProtocolException,
			IllegalStateException, RegistryExtException, IOException,
			JAXBException {
		// TODO Auto-generated method stub

		testRegistryExtListChildren();
		// testRegistryExtDeletion();
		// testRegistryExtResExist();
		// testRegistryExtPutRes();
		// testRegistryExtPostMultiRes();
		// testRegistryExtGetRes();
	}

}
