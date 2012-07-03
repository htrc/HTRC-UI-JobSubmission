package edu.indiana.d2i.ui;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import com.opensymphony.xwork2.ActionSupport;

import edu.indiana.d2i.AgentsRepoSingleton;
import edu.indiana.d2i.Constants;
import edu.indiana.d2i.exception.ErrorType;
import edu.indiana.d2i.exception.PathNotExistException;
import edu.indiana.d2i.wso2.JobProperty;
import edu.indiana.d2i.wso2.WSO2Agent;

@SuppressWarnings("serial")
public class JobSearchAction extends ActionSupport {
	private static final Logger logger = Logger
			.getLogger(JobSearchAction.class);
	private static ErrorType errorType = ErrorType.NOERROR;
	private String username;
	private Set<String> jobTitles;
	private String selectedJobTitle;

	private String errMsg = null;
	
	public String execute() {
		if (selectedJobTitle == null || "".equals(selectedJobTitle)) {
			errMsg = "Please select a job";
			return ERROR;
		}
		return NONE;
	}

	public String showJob() {
		try {
			AgentsRepoSingleton agentsRepo = AgentsRepoSingleton.getInstance();
			WSO2Agent wso2Agent = agentsRepo.getWSO2Agent();
			Properties props = agentsRepo.getProps();

			String pathPrefix = props
					.getProperty(Constants.PN_WSO2_REPO_PREFIX);

			String repoPath = pathPrefix + username;
			List<String> internalJobIds = wso2Agent.getAllChildren(repoPath);
			// remove the token file name from the list
			internalJobIds.remove(Constants.OAUTH2_TOKEN_FNAME);

			jobTitles = new HashSet<String>();

			for (String jobId : internalJobIds) {
				String jobPath = pathPrefix + username + "/" + jobId;
				Resource jobRes = wso2Agent.getRawResource(jobPath);
				String jobTitle = jobRes.getProperty(JobProperty.JOB_TITLE);

				if (jobTitle != null && !"".equals(jobTitle)) {
					jobTitles.add(jobTitle.trim());
				} else {
					logger.error("No job title associated with job : " + jobId);
				}
			}
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
			errorType = ErrorType.REGISTRY_SERVICE_UNREACHABLE;
			return ERROR;
		} catch (PathNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			errorType = ErrorType.PATH_NOT_EXIST;
			return ERROR;
		} finally {
			switch (errorType) {
			case REGISTRY_SERVICE_UNREACHABLE:
				errMsg = "Registry service is unreachable now, please try later ...";
				break;
			case PATH_NOT_EXIST:
				errMsg = "Sorry, cannot find the job path ...";
				break;
			case NOERROR:
				errMsg = "";
				break;
			}
		}

		return NONE;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Set<String> getJobTitles() {
		return jobTitles;
	}

	public void setJobTitles(Set<String> jobTitles) {
		this.jobTitles = jobTitles;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public String getSelectedJobTitle() {
		return selectedJobTitle;
	}

	public void setSelectedJobTitle(String selectedJobTitle) {
		this.selectedJobTitle = selectedJobTitle;
	}
}
