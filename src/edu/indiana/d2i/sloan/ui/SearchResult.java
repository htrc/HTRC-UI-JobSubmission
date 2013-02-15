package edu.indiana.d2i.sloan.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.SessionAware;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

import edu.indiana.d2i.sloan.AgentsRepoSingleton;
import edu.indiana.d2i.sloan.Constants;
import edu.indiana.d2i.sloan.exception.PathNotExistException;
import edu.indiana.d2i.wso2.JobProperty;
import edu.indiana.d2i.wso2.WSO2Agent;

public class SearchResult extends ActionSupport implements SessionAware,
		LoginRequired, SessionTimeoutRequired {
	private static final long serialVersionUID = 1L;
	private Map<String, Object> session;
	private static final Logger logger = Logger.getLogger(SearchResult.class);
	private String selectedJobTitle;

	private List<RadioItem> jobInsList = new ArrayList<RadioItem>();
	private List<JobMetaInfo> jobInfoList = new ArrayList<JobMetaInfo>();

	private String selectedOp;

	private String sigiriJobId;
	private String selectedJob;

	private String selectedInsId;

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
		private Date createdTime;
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

		public static String date2Str(Date date) {
			// return DateFormat.getDateInstance(DateFormat.LONG).format(date);
			return date.toString();
		}

		public void initDateStr() {
			if (createdTime != null)
				createdTimeStr = JobMetaInfo.date2Str(createdTime);
		}

		public String getInternalJobId() {
			return internalJobId;
		}

		public void setInternalJobId(String internalJobId) {
			this.internalJobId = internalJobId;
		}

		public Date getCreatedTime() {
			return createdTime;
		}

		public void setCreatedTime(Date createdTime) {
			this.createdTime = createdTime;
		}

		public String getJobStatus() {
			return jobStatus;
		}

		public void setJobStatus(String jobStatus) {
			this.jobStatus = jobStatus;
		}

		@Override
		public int compareTo(JobMetaInfo jobMetaInfo) {
			// TODO Auto-generated method stub
			int res = jobTitle.compareTo(jobMetaInfo.getJobTitle());

			return (res != 0) ? res : createdTime
					.compareTo(jobMetaInfo.createdTime);
		}

		public String getLastStatusUpdateTimeStr() {
			return lastStatusUpdateTimeStr;
		}

		public void setLastStatusUpdateTimeStr(String lastStatusUpdateTimeStr) {
			this.lastStatusUpdateTimeStr = lastStatusUpdateTimeStr;
		}
	}

	public String execute() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		String username = (String) session.get(Constants.SESSION_USERNAME);

		if (logger.isDebugEnabled()) {
			logger.debug("username=" + username);
			logger.debug("selectedJobTitle=" + selectedJobTitle);
			logger.debug(String.format("Operation %s selected", selectedOp));
			logger.debug(String.format("Job %s selected", selectedJob));
			logger.debug(String.format("Job Title is %s", selectedJobTitle));
		}

		if (selectedJob == null || "".equals(selectedJob)) {
			String errMsg = "Please select a job instance";
			logger.error(errMsg);
			addActionError(errMsg);
			return ERROR;
		}

		try {
			AgentsRepoSingleton agentsRepo = AgentsRepoSingleton.getInstance();
			WSO2Agent wso2Agent = agentsRepo.getWSO2Agent();
			String repoPrefix = PortalConfiguration.getRegistryPrefix();

			String jobPath = repoPrefix + username + WSO2Agent.separator
					+ selectedJob + WSO2Agent.separator;

			// retrieve job id
			InputStream is = wso2Agent.getResource(jobPath + "/"
					+ Constants.WSO2_JOB_PROP_FNAME);
			Properties jobProp = new Properties();
			jobProp.load(is);
			sigiriJobId = jobProp.getProperty(Constants.SIGIRI_JOB_ID);

			/**
			 * null indicated that the job has never been submitted to sigiri
			 * for execution before
			 */
			if (sigiriJobId == null) {
				sigiriJobId = "";
				logger.info("No sigiri job id for job " + selectedJob);
			}

			/**
			 * Let JobQueryAction take care of the job query, we just need to
			 * pass in the sigiriJobId
			 */
			selectedInsId = selectedJob;
			logger.info("Going to query job " + selectedInsId);

		} catch (RegistryException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		}

		return SUCCESS;
	}

	public String showSearchRes() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		String username = (String) session.get(Constants.SESSION_USERNAME);

		if (logger.isDebugEnabled()) {
			logger.debug("username=" + username);
			logger.debug("selectedJobTitle=" + selectedJobTitle);
		}

		selectedJobTitle = selectedJobTitle.trim();

		try {
			AgentsRepoSingleton agentsRepo = AgentsRepoSingleton.getInstance();
			WSO2Agent wso2Agent = agentsRepo.getWSO2Agent();

			String repoPrefix = PortalConfiguration.getRegistryPrefix();
			String repoPath = repoPrefix + username;
			List<String> internalJobIds = wso2Agent.getAllChildren(repoPath);
			// remove the token file name from the list
			internalJobIds.remove(Constants.OAUTH2_TOKEN_FNAME);

			jobInfoList = new ArrayList<JobMetaInfo>();

			for (String jobId : internalJobIds) {

				String jobPath = repoPrefix + username + WSO2Agent.separator
						+ jobId;

				if (logger.isDebugEnabled()) {
					logger.debug("job path = " + jobPath);
				}

				Resource jobRes = wso2Agent.getRawResource(jobPath);

				InputStream is = wso2Agent.getResource(jobPath
						+ WSO2Agent.separator + Constants.WSO2_JOB_PROP_FNAME);
				Properties jobProp = new Properties();
				jobProp.load(is);
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
						jobMetaInfo.setCreatedTime(jobRes.getCreatedTime());

						jobMetaInfo.initDateStr();

						/**
						 * Retrieve job Status info
						 */
						String jobStatus = jobProp
								.getProperty(Constants.SIGIRI_JOB_STATUS);
						if (jobStatus == null || "".equals(jobStatus))
							jobMetaInfo.setJobStatus("Not started");
						else
							jobMetaInfo.setJobStatus(jobStatus);

						String updatedTime = jobProp
								.getProperty(Constants.SIGIRI_JOB_STATUS_UPDATE_TIME);
						if (updatedTime == null || "".equals(updatedTime))
							jobMetaInfo
									.setLastStatusUpdateTimeStr("Not Available");
						else
							jobMetaInfo.setLastStatusUpdateTimeStr(updatedTime);

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

		} catch (RegistryException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		} catch (PathNotExistException e) {
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

	public String getSelectedOp() {
		return selectedOp;
	}

	public void setSelectedOp(String selectedOp) {
		this.selectedOp = selectedOp;
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
}
