package edu.indiana.d2i.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import com.opensymphony.xwork2.ActionSupport;

import edu.indiana.d2i.AgentsRepoSingleton;
import edu.indiana.d2i.Constants;
import edu.indiana.d2i.exception.ErrorType;
import edu.indiana.d2i.sigiri.SigiriAgent;
import edu.indiana.d2i.wso2.WSO2Agent;
import edu.indiana.extreme.sigiri.SigiriServiceStub.JobId;
import edu.indiana.extreme.sigiri.SigiriServiceStub.JobStatus;

@SuppressWarnings("serial")
public class JobQueryAction extends ActionSupport {
	private static final Logger logger = Logger.getLogger(JobQueryAction.class);
	private static ErrorType errorType = ErrorType.NOERROR;
	private String username;
//	private String selectedJob;
	private String sigiriJobId;
	private String sigiriJobStatus;
	private String selectedJobTitle;
	private String errMsg;
	
	private String selectedInsId;

	public String execute() {
		try {
			logger.debug(String.format("sigiriJobId = %s", sigiriJobId));
			// logger.debug(String.format("Job %s selected", selectedJob));
			logger.debug(String.format("Job instance id %s selected", selectedInsId));
			logger.debug(String.format("Job Title is %s", selectedJobTitle));
			
			AgentsRepoSingleton agentsRepo = AgentsRepoSingleton.getInstance();
			SigiriAgent sigiriAgent = agentsRepo.getSigiriAgent();
			WSO2Agent wso2Agent = agentsRepo.getWSO2Agent();
			Properties props = agentsRepo.getProps();

			String pathPrefix = props
					.getProperty(Constants.PN_WSO2_REPO_PREFIX);

			String jobPath = pathPrefix + username + "/" + selectedInsId + "/";

			logger.info("Sigiri Job id = " + sigiriJobId);

			JobId jobId = new JobId();
			jobId.setJobId(sigiriJobId);
			JobStatus jobStatus = sigiriAgent.queryJobStatus(jobId);
			sigiriJobStatus = jobStatus.getStatus();
			logger.info(String.format("Sigiri Job id %s , status is %s",
					sigiriJobId, sigiriJobStatus));

			/**
			 * Also update the job status in job property file
			 */
			InputStream is = wso2Agent.getResource(jobPath + "/"
					+ Constants.WSO2_JOB_PROP_FNAME);
			Properties jobProp = new Properties();
			jobProp.load(is);
			jobProp.setProperty(Constants.SIGIRI_JOB_STATUS,
					jobStatus.getStatus());
			jobProp.setProperty(Constants.SIGIRI_JOB_STATUS_UPDATE_TIME,
					new Date(System.currentTimeMillis()).toString());
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			jobProp.store(os, "");

			wso2Agent.postResource(jobPath + "/"
					+ Constants.WSO2_JOB_PROP_FNAME, os.toByteArray(), "text");
		} catch (RegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			errorType = ErrorType.REGISTRY_SERVICE_UNREACHABLE;
			return ERROR;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			errorType = ErrorType.SIGIRI_SERVICE_UNREACHABLE;
			return ERROR;
		} finally {
			switch (errorType) {
			case UNKNOWN:
				errMsg = "Sorry, error occurs when querying the job ...";
				break;
			case REGISTRY_SERVICE_UNREACHABLE:
				errMsg = "Registry service is unreachable now, please try later ...";
				break;
			case SIGIRI_SERVICE_UNREACHABLE:
				errMsg = "Sigiri service is unreachable now, please try later ...";
				break;
			case NOERROR:
				errMsg = "";
				break;
			}
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

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

//	public String getSelectedJob() {
//		return selectedJob;
//	}
//
//	public void setSelectedJob(String selectedJob) {
//		this.selectedJob = selectedJob;
//	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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

}
