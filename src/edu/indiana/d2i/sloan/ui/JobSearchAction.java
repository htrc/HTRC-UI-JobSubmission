package edu.indiana.d2i.sloan.ui;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

@SuppressWarnings("serial")
public class JobSearchAction extends ActionSupport implements SessionAware,
		LoginRequired, SessionTimeoutRequired {
	private static final Logger logger = Logger
			.getLogger(JobSearchAction.class);
	private Set<String> jobTitles;
	private String selectedJobTitle;

	private Map<String, Object> session;
	private final String webPageTitle = "Job Search";

	public String execute() {
		if (selectedJobTitle == null || "".equals(selectedJobTitle)) {
			String errMsg = "Cannot obtain job tile for search";
			logger.error(errMsg);
			addActionError(errMsg);
			return ERROR;
		}
		return SUCCESS;
	}

	public String prepareJobInfo() {

		try {
			AgentsRepoSingleton agentsRepo = AgentsRepoSingleton.getInstance();
			WSO2Agent wso2Agent = agentsRepo.getWSO2Agent();

			String repoPrefix = PortalConfiguration.getRegistryPrefix();

			Map<String, Object> session = ActionContext.getContext()
					.getSession();
			String username = (String) session.get(Constants.SESSION_USERNAME);

			String repoPath = repoPrefix + username;
			List<String> internalJobIds = wso2Agent.getAllChildren(repoPath);

			// remove the token file name from the list
			internalJobIds.remove(Constants.OAUTH2_TOKEN_FNAME);

			jobTitles = new HashSet<String>();

			for (String jobId : internalJobIds) {
				String jobPath = repoPrefix + username + WSO2Agent.separator
						+ jobId;
				Resource jobRes = wso2Agent.getRawResource(jobPath);
				String jobTitle = jobRes.getProperty(JobProperty.JOB_TITLE);

				if (jobTitle != null && !"".equals(jobTitle)) {
					jobTitles.add(jobTitle.trim());
				} else {
					logger.error("No job title associated with job : " + jobId);
				}
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

	public Set<String> getJobTitles() {
		return jobTitles;
	}

	public void setJobTitles(Set<String> jobTitles) {
		this.jobTitles = jobTitles;
	}

	public String getSelectedJobTitle() {
		return selectedJobTitle;
	}

	public void setSelectedJobTitle(String selectedJobTitle) {
		this.selectedJobTitle = selectedJobTitle;
	}

	public String getWebPageTitle() {
		return webPageTitle;
	}

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}
}
