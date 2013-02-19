package edu.indiana.d2i.sloan.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.SessionAware;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

import edu.indiana.d2i.sigiri.SigiriAgent;
import edu.indiana.d2i.sloan.AgentsRepoSingleton;
import edu.indiana.d2i.sloan.Constants;
import edu.indiana.d2i.wso2.WSO2Agent;
import edu.indiana.extreme.sigiri.SigiriServiceStub.JobId;
import edu.indiana.extreme.sigiri.SigiriServiceStub.JobStatus;

public class JobQueryAction extends ActionSupport implements SessionAware,
		LoginRequired, SessionTimeoutRequired {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(JobQueryAction.class);
	private Map<String, Object> session;

	private String sigiriJobId;
	private String sigiriJobStatus;
	private String selectedJobTitle;

	private String selectedInsId;

	public String execute() {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Job instance id %s selected",
					selectedInsId));
			logger.debug(String.format("sigiriJobId = %s", sigiriJobId));
			logger.debug(String.format("Job Title is %s", selectedJobTitle));
		}

		Map<String, Object> session = ActionContext.getContext().getSession();
		String username = (String) session.get(Constants.SESSION_USERNAME);

		try {

			AgentsRepoSingleton agentsRepo = AgentsRepoSingleton.getInstance();
			SigiriAgent sigiriAgent = agentsRepo.getSigiriAgent();
			WSO2Agent wso2Agent = agentsRepo.getWSO2Agent();

			String jobPath = PortalConfiguration.getRegistryPrefix() + username
					+ WSO2Agent.separator + selectedInsId + WSO2Agent.separator;

			JobId jobId = new JobId();
			jobId.setJobId(sigiriJobId);
			JobStatus jobStatus = sigiriAgent.queryJobStatus(jobId);
			sigiriJobStatus = jobStatus.getStatus();
			logger.info(String.format("Sigiri Job id %s , status is %s",
					sigiriJobId, sigiriJobStatus));

			/**
			 * Also update the job status in job property file
			 */
			InputStream is = wso2Agent.getResource(jobPath
					+ Constants.WSO2_JOB_PROP_FNAME);
			Properties jobProp = new Properties();
			jobProp.load(is);
			jobProp.setProperty(Constants.SIGIRI_JOB_STATUS,
					jobStatus.getStatus());
			jobProp.setProperty(Constants.SIGIRI_JOB_STATUS_UPDATE_TIME,
					new Date(System.currentTimeMillis()).toString());
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			jobProp.store(os, "");

			wso2Agent.postResource(jobPath + Constants.WSO2_JOB_PROP_FNAME,
					os.toByteArray(), "text");
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

	public String getSelectedJobTitle() {
		return selectedJobTitle;
	}

	public void setSelectedJobTitle(String selectedJobTitle) {
		this.selectedJobTitle = selectedJobTitle;
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
