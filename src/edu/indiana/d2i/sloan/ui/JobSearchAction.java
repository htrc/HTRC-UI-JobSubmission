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
# File:  JobSearchAction.java
# Description:  
#
# -----------------------------------------------------------------
# 
 */
package edu.indiana.d2i.sloan.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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

/**
 * Job search action
 * 
 * @author Guangchen
 * 
 */
@SuppressWarnings("serial")
public class JobSearchAction extends ActionSupport implements SessionAware,
		LoginRequired, SessionTimeoutRequired {
	private static final Logger logger = Logger
			.getLogger(JobSearchAction.class);

	private Set<String> jobTitles;
	private String selectedJobTitle;

	private String errMsg = null;

	@SuppressWarnings("unused")
	private Map<String, Object> session;
	private final String webPageTitle = "Job Search";

	public String execute() {
		return SUCCESS;
	}

	/**
	 * prepare job information snapshot
	 * 
	 * @return
	 */
	public String prepareJobInfo() {
		/* error message passed from SearchResultAction (showSearchRes() method) */
		if (errMsg != null && !"".equals(errMsg)) {
			addActionError(errMsg);
		}

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

			jobTitles = new HashSet<String>();

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

				if (jobTitle != null && !"".equals(jobTitle)) {
					jobTitles.add(jobTitle.trim());
				} else {
					logger.error("No job title associated with job : " + jobId);
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		} catch (IllegalStateException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		} catch (RegistryExtException e) {
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

	// getters and setters

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

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}
}
