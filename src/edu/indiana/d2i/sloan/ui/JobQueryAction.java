package edu.indiana.d2i.sloan.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

import edu.indiana.d2i.registryext.RegistryExtAgent;
import edu.indiana.d2i.registryext.RegistryExtAgent.GetResourceResponse;
import edu.indiana.d2i.registryext.RegistryExtAgent.ResourceISType;
import edu.indiana.d2i.sigiri.SigiriAgent;
import edu.indiana.d2i.sloan.AgentsRepoSingleton;
import edu.indiana.d2i.sloan.Constants;
import edu.indiana.d2i.sloan.exception.RegistryExtException;
import edu.indiana.d2i.wso2.JobProperty;
import edu.indiana.extreme.sigiri.SigiriServiceStub.JobId;
import edu.indiana.extreme.sigiri.SigiriServiceStub.JobStatus;

public class JobQueryAction extends ActionSupport implements SessionAware,
		LoginRequired, SessionTimeoutRequired {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(JobQueryAction.class);
	@SuppressWarnings("unused")
	private Map<String, Object> session;

	private String sigiriJobId;
	private String sigiriJobStatus;
	private String jobTitle;

	/* registry job id */
	private String selectedInsId;

	public String execute() {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Registry job id=%s", selectedInsId));
			logger.debug(String.format("Sigiri job id=%s", sigiriJobId));
			logger.debug(String.format("Job title =%s", jobTitle));
		}

		Map<String, Object> session = ActionContext.getContext().getSession();
		String accessToken = (String) session.get(Constants.SESSION_TOKEN);

		try {

			AgentsRepoSingleton agentsRepo = AgentsRepoSingleton.getInstance();
			SigiriAgent sigiriAgent = agentsRepo.getSigiriAgent();
			RegistryExtAgent registryExtAgent = agentsRepo
					.getRegistryExtAgent();

			JobId jobId = new JobId();
			jobId.setJobId(sigiriJobId);
			JobStatus jobStatus = sigiriAgent.queryJobStatus(jobId);
			sigiriJobStatus = jobStatus.getStatus();

			/**
			 * Also update the job status in job property file
			 */
			StringBuilder requestURL = new StringBuilder();

			GetResourceResponse response = registryExtAgent.getResource(
					requestURL
							.append(PortalConfiguration.getRegistryJobPrefix())
							.append(selectedInsId)
							.append(RegistryExtAgent.separator)
							.append(Constants.WSO2_JOB_PROP_FNAME).toString(),
					accessToken);

			Properties jobProp = new Properties();
			jobProp.load(response.getIs());

			// close connection
			registryExtAgent.closeConnection(response.getMethod());

			jobProp.setProperty(JobProperty.SIGIRI_JOB_STATUS, sigiriJobStatus);

			jobProp.setProperty(JobProperty.SIGIRI_JOB_STATUS_UPDATE_TIME,
					new Date(System.currentTimeMillis()).toString());
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			jobProp.store(os, "");

			registryExtAgent.postResource(
					requestURL.toString(),
					accessToken,
					new ResourceISType(new ByteArrayInputStream(os
							.toByteArray()), Constants.WSO2_JOB_PROP_FNAME,
							"text/plain"));

		} catch (RegistryExtException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		} catch (RemoteException e) {
			logger.error(e.getMessage(), e);
			addActionError("Sigiri service exception:" + e.getMessage());
			return ERROR;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		}

		return SUCCESS;
	}

	public String getSigiriJobStatus() {
		return sigiriJobStatus;
	}

	public void setSigiriJobStatus(String sigiriJobStatus) {
		this.sigiriJobStatus = sigiriJobStatus;
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

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

}
