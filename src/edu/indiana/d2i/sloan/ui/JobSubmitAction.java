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
# File:  JobSubmitAction.java
# Description:  
#
# -----------------------------------------------------------------
# 
 */
package edu.indiana.d2i.sloan.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

import edu.indiana.d2i.registryext.RegistryExtAgent;
import edu.indiana.d2i.registryext.RegistryExtAgent.GetResourceResponse;
import edu.indiana.d2i.registryext.RegistryExtAgent.ListResourceResponse;
import edu.indiana.d2i.registryext.RegistryExtAgent.ResourceISType;
import edu.indiana.d2i.registryext.schema.Entry;
import edu.indiana.d2i.sigiri.SigiriAgent;
import edu.indiana.d2i.sloan.AgentsRepoSingleton;
import edu.indiana.d2i.sloan.Constants;
import edu.indiana.d2i.sloan.exception.NullSigiriJobIdException;
import edu.indiana.d2i.sloan.exception.RegistryExtException;
import edu.indiana.d2i.sloan.schema.internal.InternalSchemaUtil;
import edu.indiana.d2i.sloan.schema.internal.JobDescriptionType;
import edu.indiana.d2i.sloan.schema.user.UserSchemaUtil;
import edu.indiana.d2i.wso2.JobProperty;
import edu.indiana.extreme.sigiri.SigiriServiceStub.JobStatus;
import edu.indiana.sloan.schema.SchemaUtil;

/**
 * Job submission action
 * 
 * @author Guangchen
 * 
 */
public class JobSubmitAction extends ActionSupport implements SessionAware,
		LoginRequired, SessionTimeoutRequired {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger
			.getLogger(JobSubmitAction.class);

	@SuppressWarnings("unused")
	private Map<String, Object> session;

	private String errMsg = null;

	private String selectedJob; // it is the job title given by the user

	private File jobDesp;
	private String jobDespContentType;
	private String jobDespFileName;

	private File jobArchive;
	private String jobArchiveContentType;
	private String jobArchiveFileName;

	private List<WorksetMetaInfo> worksetInfoList;
	private List<String> worksetCheckbox;

	private String danglingJobId = null;

	/**
	 * Class represents workset meta info
	 * 
	 * @author Guangchen
	 * 
	 */
	public static class WorksetMetaInfo {
		private String UUID;
		private String fileName;
		private String worksetTitle;
		private String worksetDesp;

		public WorksetMetaInfo() {
		}

		public WorksetMetaInfo(String UUID, String fileName,
				String worksetTitle, String worksetDesp) {
			this.UUID = UUID;
			this.fileName = fileName;
			this.worksetTitle = worksetTitle;
			this.worksetDesp = worksetDesp;
		}

		public String getUUID() {
			return UUID;
		}

		public void setUUID(String UUID) {
			this.UUID = UUID;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getWorksetTitle() {
			return worksetTitle;
		}

		public void setWorksetTitle(String worksetTitle) {
			this.worksetTitle = worksetTitle;
		}

		public String getWorksetDesp() {
			return worksetDesp;
		}

		public void setWorksetDesp(String worksetDesp) {
			this.worksetDesp = worksetDesp;
		}

		@Override
		public String toString() {

			return String.format(
					"UUID=%s, title=%s, description=%s, filename=%s", UUID,
					worksetTitle, worksetDesp, fileName);
		}

	}

	/**
	 * Do backend validation. Currently all basic validations are moved to front
	 * end (e.g., javascript), Other complex validations can be put in this
	 * method as backend validation
	 * 
	 * @return
	 */
	public boolean isValidForm() {
		/* add backend validation here if needed */
		return true;
	}

	/**
	 * load workset info
	 * 
	 * @return
	 */
	private String loadWorksetInfo() {

		try {
			AgentsRepoSingleton agentsRepo = AgentsRepoSingleton.getInstance();
			RegistryExtAgent registryExtAgent = agentsRepo
					.getRegistryExtAgent();

			StringBuilder requestURL = new StringBuilder();
			ListResourceResponse response = registryExtAgent
					.getAllChildren(requestURL.append(
							PortalConfiguration.getRegistryWorksetPrefix())
							.toString());

			List<String> userWorksetRepo = new ArrayList<String>();

			if (response.getStatusCode() == 404) {
				logger.warn(String.format("Path %s doesn't exist",
						requestURL.toString()));
			} else if (response.getStatusCode() == 200) {
				for (Entry entry : response.getEntries().getEntry()) {
					userWorksetRepo.add(entry.getName());
				}
			}

			worksetInfoList = new ArrayList<WorksetMetaInfo>();

			for (String worksetId : userWorksetRepo) {

				StringBuilder url = new StringBuilder();
				ListResourceResponse resp = registryExtAgent.getAllChildren(url
						.append(PortalConfiguration.getRegistryWorksetPrefix())
						.append(worksetId).toString());

				List<String> items = new ArrayList<String>();
				for (Entry entry : resp.getEntries().getEntry()) {
					items.add(entry.getName());
				}

				String fileName = null;
				for (String item : items) {
					if (!item.equals(Constants.WSO2_WORKSET_META_FNAME)) {
						fileName = item;
						break;
					}
				}

				url.setLength(0);
				GetResourceResponse getResp = registryExtAgent.getResource(url
						.append(PortalConfiguration.getRegistryWorksetPrefix())
						.append(worksetId).append(RegistryExtAgent.separator)
						.append(Constants.WSO2_WORKSET_META_FNAME).toString());

				Properties worksetMeta = new Properties();
				worksetMeta.load(getResp.getIs());

				// close connection
				registryExtAgent.closeConnection(getResp.getMethod());

				WorksetMetaInfo worksetMetaInfo = new WorksetMetaInfo(
						worksetId, fileName, worksetMeta.getProperty(
								Constants.WSO2_WORKSET_METAFILE_SETNAME,
								"Unknown"), worksetMeta.getProperty(
								Constants.WSO2_WORKSET_METAFILE_SETDESP,
								"Unknown"));
				worksetInfoList.add(worksetMetaInfo);
			}

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		} catch (RegistryExtException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		} catch (IllegalStateException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		} catch (JAXBException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		} catch (OAuthSystemException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		} catch (OAuthProblemException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		}

		return SUCCESS;
	}

	public void checkError() {
		if (errMsg == null || "".equals(errMsg)) {
			return;
		}
		addActionError(errMsg);
	}

	public String jobSubmitForm() {
		checkError();
		return loadWorksetInfo();
	}

	/**
	 * upload job to registry and submit it to Sigiri web services
	 */
	public String execute() {

		if (!isValidForm())
			return INPUT;

		if (logger.isDebugEnabled()) {

			if (worksetCheckbox != null) {
				StringBuilder worksetList = new StringBuilder();
				for (String idx : worksetCheckbox)
					worksetList.append(idx + " ");

				logger.debug("Selected worksets=" + worksetList.toString());
			} else {
				logger.debug("No worksets being selected");
			}

		}

		try {
			uploadJob();
		} catch (JAXBException e) {
			logger.error(e.getMessage(), e);
			errMsg = "Invalid job description file";
			logger.error(errMsg);
			addActionError(e.getMessage());
			return INPUT;
		} catch (RemoteException e) {
			logger.error(e.getMessage(), e);
			addActionError("Sigiri service exception:" + e.getMessage());
			return ERROR;
		} catch (NullSigiriJobIdException e) {
			logger.error(e.getMessage(), e);
			addActionError("Sigiri service exception:" + e.getMessage());
			return ERROR;
		} catch (XMLStreamException e) {
			logger.error(e.getMessage(), e);
			addActionError("Sigiri service exception:" + e.getMessage());
			return ERROR;
		} catch (RegistryExtException e) {
			logger.error(e.getMessage(), e);
			addActionError("Registry service exception" + e.getMessage());
			return ERROR;
		} catch (HttpException e) {
			logger.error(e.getMessage(), e);
			addActionError("Registry service exception" + e.getMessage());
			return ERROR;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		} catch (OAuthSystemException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		} catch (OAuthProblemException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		} finally {
			String errString = "Exception occurred when cleaning up dangling job:";
			try {
				removeDanglingJob();
			} catch (HttpException e) {
				logger.error(errString + e.getMessage(), e);
			} catch (IOException e) {
				logger.error(errString + e.getMessage(), e);
			} catch (RegistryExtException e) {
				logger.error(errString + e.getMessage(), e);
			} catch (OAuthSystemException e) {
				logger.error(errString + e.getMessage(), e);
			} catch (OAuthProblemException e) {
				logger.error(errString + e.getMessage(), e);
			}
		}

		return SUCCESS;
	}

	/**
	 * Remove dangling job when job submission failed
	 * 
	 * @throws RegistryExtException
	 * @throws IOException
	 * @throws HttpException
	 * @throws OAuthProblemException
	 * @throws OAuthSystemException
	 */
	private void removeDanglingJob() throws HttpException, IOException,
			RegistryExtException, OAuthSystemException, OAuthProblemException {
		if (danglingJobId != null) {

			AgentsRepoSingleton agentsRepo = AgentsRepoSingleton.getInstance();
			RegistryExtAgent registryExtAgent = agentsRepo
					.getRegistryExtAgent();

			/**
			 * check if the job exists in registry
			 */

			StringBuilder requestURL = new StringBuilder();
			requestURL.append(PortalConfiguration.getRegistryJobPrefix())
					.append(danglingJobId);

			if (registryExtAgent.isResourceExist(requestURL.toString())) {
				logger.info(String.format(
						"Going to clean up dangling job %s in registry",
						requestURL.toString()));

				registryExtAgent.deleteResource(requestURL.toString());

				logger.info(String.format(
						"Removed dangling job %s in registry",
						requestURL.toString()));
			} else {
				logger.info(String.format(
						"Job %s doesn't exist in registry, nothing to cleanup",
						requestURL.toString()));
			}
		}
	}

	/**
	 * upload job
	 * 
	 * @throws RemoteException
	 * @throws JAXBException
	 * @throws NullSigiriJobIdException
	 * @throws XMLStreamException
	 * @throws RegistryExtException
	 * @throws IOException
	 * @throws OAuthSystemException
	 * @throws OAuthProblemException
	 */
	private void uploadJob() throws RemoteException, JAXBException,
			NullSigiriJobIdException, XMLStreamException, RegistryExtException,
			IOException, OAuthSystemException, OAuthProblemException {

		Map<String, Object> session = ActionContext.getContext().getSession();
		String username = (String) session.get(Constants.SESSION_USERNAME);
		String accessToken = (String) session.get(Constants.SESSION_TOKEN);
		String refreshToken = (String) session
				.get(Constants.SESSION_REFRESH_TOKEN);

		AgentsRepoSingleton agentsRepo = AgentsRepoSingleton.getInstance();
		RegistryExtAgent registryExtAgent = agentsRepo.getRegistryExtAgent();

		/**
		 * Update token: write token as a property file, all jobs share this
		 * token file, which is stored directly under user's home directory. (In
		 * registry extension, it is under /htrc/username/files/sloan/jobs/).
		 * Each job submission will update this file
		 */

		Properties tokenFile = new Properties();

		tokenFile.setProperty(Constants.OAUTH2_ACCESS_TOKEN, accessToken);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		tokenFile.store(os, "");

		String outPath = registryExtAgent.postResource(
				new StringBuilder()
						.append(PortalConfiguration.getRegistryJobPrefix())
						.append(Constants.OAUTH2_TOKEN_FNAME).toString(),
				new ResourceISType(new ByteArrayInputStream(os.toByteArray()),
						Constants.OAUTH2_TOKEN_FNAME, "text/plain"));

		logger.info(String.format("Updated token file %s saved to %s",
				Constants.OAUTH2_TOKEN_FNAME, outPath));

		// remove leading and trailing white-spaces
		selectedJob = selectedJob.trim();

		/* generate registry internal job id */
		String internalJobId = UUID.randomUUID().toString();

		danglingJobId = internalJobId;

		String jobPath = PortalConfiguration.getRegistryJobPrefix()
				+ internalJobId + RegistryExtAgent.separator;
		logger.info("Create new job home=" + jobPath);

		/**
		 * check user uploaded job description xml file, make sure it conforms
		 * to user xsd
		 */

		edu.indiana.d2i.sloan.schema.user.JobDescriptionType userJobDesp = null;

		InputStream is = new ByteArrayInputStream(
				FileUtils.readFileToByteArray(jobDesp));
		userJobDesp = UserSchemaUtil.readConfigXML(is);
		if (logger.isDebugEnabled()) {
			logger.debug("Use uploaded job desp file:\n");
			logger.debug(UserSchemaUtil.toXMLString(userJobDesp));
		}

		/**
		 * Pick user selected worksets
		 */
		List<WorksetMetaInfo> selectedWorksets = new ArrayList<WorksetMetaInfo>();

		if (worksetCheckbox != null) {
			for (String idx : worksetCheckbox) {
				/*
				 * This happens only when there is only one workset and user
				 * doesn't select it when composing the job
				 */
				if ("false".equals(idx))
					continue;

				selectedWorksets.add(worksetInfoList.get(Integer.valueOf(idx)));
			}
		}

		/**
		 * Convert user schema to internal schema used by sigiri daemon
		 */
		JobDescriptionType internalJobDesp = SchemaUtil.user2internal(
				userJobDesp, accessToken, refreshToken, username,
				internalJobId, jobArchiveFileName, selectedWorksets);

		String internalJobDespStr = InternalSchemaUtil
				.toXMLString(internalJobDesp);

		if (logger.isDebugEnabled()) {
			logger.debug("Converted internal job description:\n");
			logger.debug(internalJobDespStr);
		}

		// create job.properties file
		Properties jobProp = new Properties();
		jobProp.setProperty(JobProperty.JOB_TITLE, selectedJob);
		jobProp.setProperty(JobProperty.JOB_OWNER, username);
		jobProp.setProperty(JobProperty.JOB_CREATION_TIME,
				new Date(System.currentTimeMillis()).toString());

		os = new ByteArrayOutputStream();
		jobProp.store(os, "");

		outPath = registryExtAgent.postResource(
				new StringBuilder().append(jobPath)
						.append(Constants.WSO2_JOB_PROP_FNAME).toString(),
				new ResourceISType(new ByteArrayInputStream(os.toByteArray()),
						Constants.WSO2_JOB_PROP_FNAME, "text/plain"));

		logger.info(String.format("Job property file %s saved to %s",
				Constants.WSO2_JOB_PROP_FNAME, outPath));

		// post job description
		outPath = registryExtAgent.postResource(
				new StringBuilder().append(jobPath)
						.append(Constants.WSO2_JOB_DESP_FNAME).toString(),
				new ResourceISType(new ByteArrayInputStream(internalJobDespStr
						.getBytes()), Constants.WSO2_JOB_DESP_FNAME,
						jobDespContentType));

		logger.info(String.format("Job description file %s saved to %s",
				Constants.WSO2_JOB_DESP_FNAME, outPath));

		// post archive
		outPath = registryExtAgent.postResource(
				new StringBuilder().append(jobPath)
						.append(PortalConfiguration.getRegistryArchiveFolder())
						.append(RegistryExtAgent.separator)
						.append(jobArchiveFileName).toString(),
				new ResourceISType(new FileInputStream(jobArchive),
						jobArchiveFileName, jobArchiveContentType));

		logger.info(String.format("Job archive file %s saved to %s",
				jobArchiveFileName, outPath));

		// submit to sigiri service
		SigiriAgent sigiriAgent = agentsRepo.getSigiriAgent();
		JobStatus jobStatus = sigiriAgent.submitJob(internalJobDespStr);

		String sigiriJobId = jobStatus.getJobId();

		if (sigiriJobId == null || "".equals(sigiriJobId)
				|| "NoJobId".equals(sigiriJobId))
			throw new NullSigiriJobIdException("None sigiri job id returned");

		if (logger.isDebugEnabled()) {
			logger.debug(String.format(
					"Job %s submitted to Sigiri, sigiri job id returned is %s",
					internalJobId, sigiriJobId));
		}

		/**
		 * write the job id to registry for future reference
		 */
		jobProp.setProperty(JobProperty.SIGIRI_JOB_ID, sigiriJobId);
		jobProp.setProperty(JobProperty.SIGIRI_JOB_STATUS,
				jobStatus.getStatus());

		jobProp.setProperty(JobProperty.SIGIRI_JOB_STATUS_UPDATE_TIME,
				new Date(System.currentTimeMillis()).toString());
		os = new ByteArrayOutputStream();
		jobProp.store(os, "");

		outPath = registryExtAgent.postResource(
				new StringBuilder().append(jobPath)
						.append(Constants.WSO2_JOB_PROP_FNAME).toString(),
				new ResourceISType(new ByteArrayInputStream(os.toByteArray()),
						Constants.WSO2_JOB_PROP_FNAME, "text/plain"));

		logger.info(String.format("Updated job property file %s saved to %s",
				Constants.WSO2_JOB_PROP_FNAME, outPath));

		logger.info(String.format(
				"User %s's job %s has been submitted successfully", username,
				jobPath));

		/**
		 * set danglingJobId to null to indicate that job has been submitted
		 * successfully (no exceptions occurred)
		 */
		danglingJobId = null;
	}

	// getters and setters

	public String getSelectedJob() {
		return selectedJob;
	}

	public void setSelectedJob(String selectedJob) {
		this.selectedJob = selectedJob;
	}

	public File getJobDesp() {
		return jobDesp;
	}

	public void setJobDesp(File jobDesp) {
		this.jobDesp = jobDesp;
	}

	public String getJobDespContentType() {
		return jobDespContentType;
	}

	public void setJobDespContentType(String jobDespContentType) {
		this.jobDespContentType = jobDespContentType;
	}

	public String getJobDespFileName() {
		return jobDespFileName;
	}

	public void setJobDespFileName(String jobDespFileName) {
		this.jobDespFileName = jobDespFileName;
	}

	public File getJobArchive() {
		return jobArchive;
	}

	public void setJobArchive(File jobArchive) {
		this.jobArchive = jobArchive;
	}

	public String getJobArchiveContentType() {
		return jobArchiveContentType;
	}

	public void setJobArchiveContentType(String jobArchiveContentType) {
		this.jobArchiveContentType = jobArchiveContentType;
	}

	public String getJobArchiveFileName() {
		return jobArchiveFileName;
	}

	public void setJobArchiveFileName(String jobArchiveFileName) {
		this.jobArchiveFileName = jobArchiveFileName;
	}

	public List<WorksetMetaInfo> getWorksetInfoList() {
		return worksetInfoList;
	}

	public void setWorksetInfoList(List<WorksetMetaInfo> worksetInfoList) {
		this.worksetInfoList = worksetInfoList;
	}

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public List<String> getWorksetCheckbox() {
		return worksetCheckbox;
	}

	public void setWorksetCheckbox(List<String> worksetCheckbox) {
		this.worksetCheckbox = worksetCheckbox;
	}

}
