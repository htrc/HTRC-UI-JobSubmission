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
# File:  ManageWorkSetAction.java
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

import edu.indiana.d2i.registryext.RegistryExtAgent;
import edu.indiana.d2i.registryext.RegistryExtAgent.GetResourceResponse;
import edu.indiana.d2i.registryext.RegistryExtAgent.ListResourceResponse;
import edu.indiana.d2i.registryext.RegistryExtAgent.ResourceISType;
import edu.indiana.d2i.registryext.schema.Entry;
import edu.indiana.d2i.sloan.AgentsRepoSingleton;
import edu.indiana.d2i.sloan.Constants;
import edu.indiana.d2i.sloan.exception.RegistryExtException;
import edu.indiana.d2i.sloan.ui.JobSubmitAction.WorksetMetaInfo;

/**
 * Action that allows user to manage workset. Workset addition, modification and
 * deletion are supported.
 * 
 * @author Guangchen
 * 
 */
public class ManageWorkSetAction extends ActionSupport implements SessionAware,
		LoginRequired, SessionTimeoutRequired {

	private static final Logger logger = Logger
			.getLogger(ManageWorkSetAction.class);

	private final String webPageTitle = "Workset";
	
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private Map<String, Object> session;

	private List<WorksetMetaInfo> currentWorksetInfoList;
	private List<String> currentWorksetCheckbox;
	private String curWorkSetUpdateList;

	private File[] currentWorksetNewFile;
	private String[] currentWorksetNewFileContentType;
	private String[] currentWorksetNewFileFileName;

	private List<String> newWorksetTitle;
	private List<String> newWorksetDesp;
	private List<File> newWorkset;
	private List<String> newWorksetContentType;
	private List<String> newWorksetFileName;

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

			currentWorksetInfoList = new ArrayList<WorksetMetaInfo>();

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
				currentWorksetInfoList.add(worksetMetaInfo);
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

	public String execute() {

		return loadWorksetInfo();
	}

	/**
	 * update workset
	 * @return
	 */
	public String updateWorkSet() {
		Map<String, Object> session = ActionContext.getContext().getSession();
		String username = (String) session.get(Constants.SESSION_USERNAME);

		// update list of current worksets
		List<Integer> updateList = new ArrayList<Integer>();
		if (curWorkSetUpdateList != null
				&& !"".equals(curWorkSetUpdateList.trim())) {
			curWorkSetUpdateList = curWorkSetUpdateList.trim();
			String[] items = curWorkSetUpdateList.split("\\s+");

			for (int i = 0; i < items.length; i++) {
				updateList.add(Integer.valueOf(items[i]));
			}
		}

		if (logger.isDebugEnabled()) {

			if (curWorkSetUpdateList != null) {
				logger.debug("curWorkSetUpdateList=" + curWorkSetUpdateList);
			}

			if (currentWorksetCheckbox != null) {
				StringBuilder deletionList = new StringBuilder();
				for (String idx : currentWorksetCheckbox)
					deletionList.append(idx + " ");

				logger.debug("Items to be deleted=" + deletionList.toString());
			}

			if (currentWorksetNewFile != null)
				logger.debug("new file length=" + currentWorksetNewFile.length);

			if (currentWorksetInfoList != null
					&& currentWorksetInfoList.size() > 0) {

				for (int i = 0; i < currentWorksetInfoList.size(); i++) {
					logger.debug(currentWorksetInfoList.get(i));
				}
			} else {
				logger.debug("Current worksets are null or empty");
			}
		}

		AgentsRepoSingleton agentsRepo;
		try {
			agentsRepo = AgentsRepoSingleton.getInstance();
			RegistryExtAgent registryExtAgent = agentsRepo
					.getRegistryExtAgent();

			// update existing worksets

			int idx = 0;

			if (currentWorksetInfoList != null
					&& currentWorksetInfoList.size() > 0) {
				for (int i = 0; i < currentWorksetInfoList.size(); i++) {
					String worksetPath = PortalConfiguration
							.getRegistryWorksetPrefix()
							+ currentWorksetInfoList.get(i).getUUID();

					if ((currentWorksetCheckbox != null)
							&& currentWorksetCheckbox.contains(String
									.valueOf(i))) {

						logger.info(String.format("User %s deleted workset %s",
								username, worksetPath));

						registryExtAgent.deleteResource(new StringBuilder()
								.append(worksetPath).toString());

					} else {

						// user uploads a new workset archive file
						if (updateList.contains(i)) {

							logger.info(String.format(
									"User %s uploaded a new workset %s",
									username,
									currentWorksetNewFileFileName[idx]));

							// remove old workset archive file

							ListResourceResponse resp = registryExtAgent
									.getAllChildren(new StringBuilder().append(
											worksetPath).toString());

							List<String> items = new ArrayList<String>();
							for (Entry entry : resp.getEntries().getEntry()) {
								items.add(entry.getName());
							}

							String archiveFileName = null;
							for (String item : items) {
								if (!item
										.equals(Constants.WSO2_WORKSET_META_FNAME)) {
									archiveFileName = item;
									break;
								}
							}

							registryExtAgent.deleteResource(new StringBuilder()
									.append(worksetPath)
									.append(RegistryExtAgent.separator)
									.append(archiveFileName).toString());

							logger.info("Deleted old workset file "
									+ archiveFileName);

							// upload archive file
							String outPath = registryExtAgent
									.postResource(
											new StringBuilder()
													.append(worksetPath)
													.append(RegistryExtAgent.separator)
													.append(currentWorksetNewFileFileName[idx])
													.toString(),
											new ResourceISType(
													new FileInputStream(
															currentWorksetNewFile[idx]),
													currentWorksetNewFileFileName[idx],
													currentWorksetNewFileContentType[idx]));

							logger.info(String
									.format("New workset %s saved to %s",
											currentWorksetNewFileFileName[idx],
											outPath));

							// update index
							idx++;

						}

						// update metadata file anyway
						Properties worksetMeta = new Properties();

						worksetMeta
								.setProperty(
										Constants.WSO2_WORKSET_METAFILE_SETNAME,
										currentWorksetInfoList.get(i)
												.getWorksetTitle());
						worksetMeta.setProperty(
								Constants.WSO2_WORKSET_METAFILE_SETDESP,
								currentWorksetInfoList.get(i).getWorksetDesp());

						ByteArrayOutputStream os = new ByteArrayOutputStream();
						worksetMeta.store(os, "");

						String outPath = registryExtAgent
								.postResource(
										new StringBuilder()
												.append(worksetPath)
												.append(RegistryExtAgent.separator)
												.append(Constants.WSO2_WORKSET_META_FNAME)
												.toString(),
										new ResourceISType(
												new ByteArrayInputStream(os
														.toByteArray()),
												Constants.WSO2_WORKSET_META_FNAME,
												"text/plain"));

						logger.info(String.format(
								"Updated metafile %s saved to %s",
								Constants.WSO2_WORKSET_META_FNAME, outPath));
					}
				}
			}

			// upload new submitted worksets
			if (newWorkset != null && newWorkset.size() > 0) {

				for (int i = 0; i < newWorkset.size(); i++) {
					String worksetId = UUID.randomUUID().toString();

					String worksetPath = PortalConfiguration
							.getRegistryWorksetPrefix() + worksetId;

					// upload archive file

					String outPath = registryExtAgent.postResource(
							new StringBuilder().append(worksetPath)
									.append(RegistryExtAgent.separator)
									.append(newWorksetFileName.get(i))
									.toString(), new ResourceISType(
									new FileInputStream(newWorkset.get(i)),
									newWorksetFileName.get(i),
									newWorksetContentType.get(i)));

					logger.info(String.format("New workset %s saved to %s",
							newWorksetFileName.get(i), outPath));

					// compose metadata file
					Properties worksetMeta = new Properties();

					worksetMeta.setProperty(
							Constants.WSO2_WORKSET_METAFILE_SETNAME,
							newWorksetTitle.get(i));
					worksetMeta.setProperty(
							Constants.WSO2_WORKSET_METAFILE_SETDESP,
							newWorksetDesp.get(i));

					ByteArrayOutputStream os = new ByteArrayOutputStream();
					worksetMeta.store(os, "");

					outPath = registryExtAgent.postResource(
							new StringBuilder().append(worksetPath)
									.append(RegistryExtAgent.separator)
									.append(Constants.WSO2_WORKSET_META_FNAME)
									.toString(), new ResourceISType(
									new ByteArrayInputStream(os.toByteArray()),
									Constants.WSO2_WORKSET_META_FNAME,
									"text/plain"));

					logger.info(String.format("New metafile %s saved to %s",
							Constants.WSO2_WORKSET_META_FNAME, outPath));
				}

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

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}

	public List<WorksetMetaInfo> getCurrentWorksetInfoList() {
		return currentWorksetInfoList;
	}

	public void setCurrentWorksetInfoList(
			List<WorksetMetaInfo> currentWorksetInfoList) {
		this.currentWorksetInfoList = currentWorksetInfoList;
	}

	public List<String> getNewWorksetTitle() {
		return newWorksetTitle;
	}

	public void setNewWorksetTitle(List<String> newWorksetTitle) {
		this.newWorksetTitle = newWorksetTitle;
	}

	public List<String> getNewWorksetDesp() {
		return newWorksetDesp;
	}

	public void setNewWorksetDesp(List<String> newWorksetDesp) {
		this.newWorksetDesp = newWorksetDesp;
	}

	public List<File> getNewWorkset() {
		return newWorkset;
	}

	public void setNewWorkset(List<File> newWorkset) {
		this.newWorkset = newWorkset;
	}

	public List<String> getNewWorksetContentType() {
		return newWorksetContentType;
	}

	public void setNewWorksetContentType(List<String> newWorksetContentType) {
		this.newWorksetContentType = newWorksetContentType;
	}

	public List<String> getNewWorksetFileName() {
		return newWorksetFileName;
	}

	public void setNewWorksetFileName(List<String> newWorksetFileName) {
		this.newWorksetFileName = newWorksetFileName;
	}

	public File[] getCurrentWorksetNewFile() {
		return currentWorksetNewFile;
	}

	public void setCurrentWorksetNewFile(File[] currentWorksetNewFile) {
		this.currentWorksetNewFile = currentWorksetNewFile;
	}

	public String[] getCurrentWorksetNewFileContentType() {
		return currentWorksetNewFileContentType;
	}

	public void setCurrentWorksetNewFileContentType(
			String[] currentWorksetNewFileContentType) {
		this.currentWorksetNewFileContentType = currentWorksetNewFileContentType;
	}

	public String[] getCurrentWorksetNewFileFileName() {
		return currentWorksetNewFileFileName;
	}

	public void setCurrentWorksetNewFileFileName(
			String[] currentWorksetNewFileFileName) {
		this.currentWorksetNewFileFileName = currentWorksetNewFileFileName;
	}

	public List<String> getCurrentWorksetCheckbox() {
		return currentWorksetCheckbox;
	}

	public void setCurrentWorksetCheckbox(List<String> currentWorksetCheckbox) {
		this.currentWorksetCheckbox = currentWorksetCheckbox;
	}

	public String getCurWorkSetUpdateList() {
		return curWorkSetUpdateList;
	}

	public void setCurWorkSetUpdateList(String curWorkSetUpdateList) {
		this.curWorkSetUpdateList = curWorkSetUpdateList;
	}

	public String getWebPageTitle() {
		return webPageTitle;
	}
}
