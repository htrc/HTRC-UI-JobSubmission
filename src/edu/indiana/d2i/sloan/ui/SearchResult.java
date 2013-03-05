package edu.indiana.d2i.sloan.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionSupport;

import edu.indiana.d2i.registryext.RegistryExtAgent;
import edu.indiana.d2i.registryext.RegistryExtAgent.GetResourceResponse;
import edu.indiana.d2i.registryext.RegistryExtAgent.ListResourceResponse;
import edu.indiana.d2i.registryext.schema.Entry;
import edu.indiana.d2i.sloan.AgentsRepoSingleton;
import edu.indiana.d2i.sloan.Constants;
import edu.indiana.d2i.sloan.exception.RegistryExtException;
import edu.indiana.d2i.wso2.JobProperty;

public class SearchResult extends ActionSupport implements SessionAware,
		LoginRequired, SessionTimeoutRequired {
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private Map<String, Object> session;
	private static final Logger logger = Logger.getLogger(SearchResult.class);

	/* job query string */
	private String selectedJobTitle;

	private List<RadioItem> jobInsList = new ArrayList<RadioItem>();
	private List<JobMetaInfo> jobInfoList = new ArrayList<JobMetaInfo>();

	private String errMsg = null;

	/* sigiri job id */
	private String sigiriJobId;

	/* registry job id */
	private String selectedJob;

	/* registry job id, same as 'selectedJob', used by JobQueryAction */
	private String selectedInsId;

	private String defaultJob;

	private String jobTitle;

	public static class RadioItem {
		private String key;
		private String label;

		public RadioItem(String key, String label) {
			this.key = key;
			this.label = label;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

	}

	public static class JobMetaInfo implements Comparable<JobMetaInfo> {
		private String createdTimeStr;
		private String owner;
		private String internalJobId;
		private String jobTitle;
		private String jobStatus;
		private String lastStatusUpdateTimeStr;

		public String getCreatedTimeStr() {
			return createdTimeStr;
		}

		public void setCreatedTimeStr(String createdTimeStr) {
			this.createdTimeStr = createdTimeStr;
		}

		public String getOwner() {
			return owner;
		}

		public void setOwner(String owner) {
			this.owner = owner;
		}

		public String getJobTitle() {
			return jobTitle;
		}

		public void setJobTitle(String jobTitle) {
			this.jobTitle = jobTitle;
		}

		public String getInternalJobId() {
			return internalJobId;
		}

		public void setInternalJobId(String internalJobId) {
			this.internalJobId = internalJobId;
		}

		public String getJobStatus() {
			return jobStatus;
		}

		public void setJobStatus(String jobStatus) {
			this.jobStatus = jobStatus;
		}

		public String getLastStatusUpdateTimeStr() {
			return lastStatusUpdateTimeStr;
		}

		public void setLastStatusUpdateTimeStr(String lastStatusUpdateTimeStr) {
			this.lastStatusUpdateTimeStr = lastStatusUpdateTimeStr;
		}

		@Override
		public int compareTo(JobMetaInfo jobMetaInfo) {
			// TODO Auto-generated method stub
			return jobTitle.compareTo(jobMetaInfo.getJobTitle());

		}
	}

	public String execute() {

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Job %s is selected", selectedJob));
			logger.debug(String.format("Job title =%s", selectedJobTitle));
		}

		if (selectedJob == null || "".equals(selectedJob)) {
			String errMsg = "Please select a job instance";
			logger.error(errMsg);
			addActionError(errMsg);
			return ERROR;
		}

		try {
			AgentsRepoSingleton agentsRepo = AgentsRepoSingleton.getInstance();
			RegistryExtAgent registryExtAgent = agentsRepo
					.getRegistryExtAgent();

			StringBuilder requestURL = new StringBuilder();

			GetResourceResponse response = registryExtAgent
					.getResource(requestURL
							.append(PortalConfiguration.getRegistryJobPrefix())
							.append(selectedJob)
							.append(RegistryExtAgent.separator)
							.append(Constants.WSO2_JOB_PROP_FNAME).toString());

			Properties jobProp = new Properties();
			jobProp.load(response.getIs());

			// close connection
			registryExtAgent.closeConnection(response.getMethod());

			sigiriJobId = jobProp.getProperty(JobProperty.SIGIRI_JOB_ID,
					"Not available");

			jobTitle = jobProp.getProperty(JobProperty.JOB_TITLE);

			/**
			 * Let JobQueryAction take care of the job query, we just need to
			 * pass in the sigiriJobId
			 */
			selectedInsId = selectedJob;

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		} catch (RegistryExtException e) {
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

	public String showSearchRes() {

		if (logger.isDebugEnabled()) {
			logger.debug("Job title query string =" + selectedJobTitle);
		}

		if (null == selectedJobTitle || "".equals(selectedJobTitle.trim())) {
			errMsg = "Query string cannot be empty";
			return INPUT;
		}

		selectedJobTitle = selectedJobTitle.trim();

		try {
			AgentsRepoSingleton agentsRepo = AgentsRepoSingleton.getInstance();
			RegistryExtAgent registryExtAgent = agentsRepo
					.getRegistryExtAgent();

			StringBuilder requestURL = new StringBuilder();
			ListResourceResponse response = registryExtAgent
					.getAllChildren(requestURL.append(
							PortalConfiguration.getRegistryJobPrefix())
							.toString());

			List<String> internalJobIds = new ArrayList<String>();

			if (response.getStatusCode() == 404) {
				logger.warn(String.format("Path %s doesn't exist",
						requestURL.toString()));
			} else if (response.getStatusCode() == 200) {
				for (Entry entry : response.getEntries().getEntry()) {
					internalJobIds.add(entry.getName());
				}
			}

			// remove the token file name from the list
			internalJobIds.remove(Constants.OAUTH2_TOKEN_FNAME);

			jobInfoList = new ArrayList<JobMetaInfo>();

			for (String jobId : internalJobIds) {

				StringBuilder url = new StringBuilder();
				GetResourceResponse resp = registryExtAgent.getResource(url
						.append(PortalConfiguration.getRegistryJobPrefix())
						.append(jobId).append(RegistryExtAgent.separator)
						.append(Constants.WSO2_JOB_PROP_FNAME).toString());

				Properties jobProp = new Properties();
				jobProp.load(resp.getIs());

				// close connection
				registryExtAgent.closeConnection(resp.getMethod());

				String jobTitle = jobProp.getProperty(JobProperty.JOB_TITLE);

				if ("*".equals(selectedJobTitle) || jobTitle != null
						&& !"".equals(jobTitle)) {
					if ("*".equals(selectedJobTitle)
							|| jobTitle.trim().toLowerCase()
									.contains(selectedJobTitle.toLowerCase())
							|| selectedJobTitle.toLowerCase().contains(
									jobTitle.trim().toLowerCase())) {

						JobMetaInfo jobMetaInfo = new JobMetaInfo();

						jobMetaInfo.setInternalJobId(jobId);
						jobMetaInfo.setJobTitle(jobTitle);
						jobMetaInfo.setOwner(jobProp
								.getProperty(JobProperty.JOB_OWNER));
						jobMetaInfo.setCreatedTimeStr(jobProp
								.getProperty(JobProperty.JOB_CREATION_TIME));

						/**
						 * Retrieve job Status info
						 */

						jobMetaInfo
								.setJobStatus(jobProp.getProperty(
										JobProperty.SIGIRI_JOB_STATUS,
										"Not available"));

						jobMetaInfo
								.setLastStatusUpdateTimeStr(jobProp
										.getProperty(
												JobProperty.SIGIRI_JOB_STATUS_UPDATE_TIME,
												"Not available"));

						jobInfoList.add(jobMetaInfo);
					}
				} else {
					logger.error("No job title associated with job : " + jobId);
				}

			}

			// sort the list
			Collections.sort(jobInfoList);

			jobInsList = new ArrayList<RadioItem>();

			for (int i = 0; i < jobInfoList.size(); i++) {
				JobMetaInfo jobMetaInfo = jobInfoList.get(i);
				jobInsList.add(new RadioItem(jobMetaInfo.getInternalJobId(),
						String.valueOf(i + 1)));
			}

			if (jobInfoList.size() > 0)
				defaultJob = jobInfoList.get(0).getInternalJobId();

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

	public String getSelectedJobTitle() {
		return selectedJobTitle;
	}

	public void setSelectedJobTitle(String selectedJobTitle) {
		this.selectedJobTitle = selectedJobTitle;
	}

	public List<RadioItem> getJobInsList() {
		return jobInsList;
	}

	public void setJobInsList(List<RadioItem> jobInsList) {
		this.jobInsList = jobInsList;
	}

	public List<JobMetaInfo> getJobInfoList() {
		return jobInfoList;
	}

	public void setJobInfoList(List<JobMetaInfo> jobInfoList) {
		this.jobInfoList = jobInfoList;
	}

	public String getSelectedJob() {
		return selectedJob;
	}

	public void setSelectedJob(String selectedJob) {
		this.selectedJob = selectedJob;
	}

	public String getSigiriJobId() {
		return sigiriJobId;
	}

	public void setSigiriJobId(String sigiriJobId) {
		this.sigiriJobId = sigiriJobId;
	}

	public String getSelectedInsId() {
		return selectedInsId;
	}

	public void setSelectedInsId(String selectedInsId) {
		this.selectedInsId = selectedInsId;
	}

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}

	public String getDefaultJob() {
		return defaultJob;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}
}
