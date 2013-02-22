package edu.indiana.d2i.sloan.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.SessionAware;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

import edu.indiana.d2i.sloan.AgentsRepoSingleton;
import edu.indiana.d2i.sloan.Constants;
import edu.indiana.d2i.sloan.exception.PathNotExistException;
import edu.indiana.d2i.sloan.ui.JobSubmitAction.WorksetMetaInfo;
import edu.indiana.d2i.wso2.WSO2Agent;

public class ManageWorkSetAction extends ActionSupport implements SessionAware,
		LoginRequired, SessionTimeoutRequired {

	private static final Logger logger = Logger
			.getLogger(ManageWorkSetAction.class);

	private static final long serialVersionUID = 1L;
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

		Map<String, Object> session = ActionContext.getContext().getSession();
		String username = (String) session.get(Constants.SESSION_USERNAME);

		try {
			AgentsRepoSingleton agentsRepo = AgentsRepoSingleton.getInstance();
			WSO2Agent wso2Agent = agentsRepo.getWSO2Agent();

			String worksetRepoPrefix = PortalConfiguration
					.getRegistryWorksetPrefix();

			List<String> userWorksetRepo = wso2Agent
					.getAllChildren(worksetRepoPrefix + username);

			currentWorksetInfoList = new ArrayList<WorksetMetaInfo>();

			for (String worksetId : userWorksetRepo) {
				List<String> items = wso2Agent.getAllChildren(worksetRepoPrefix
						+ username + WSO2Agent.separator + worksetId);

				String fileName = null;
				for (String item : items) {
					if (!item.equals(Constants.WSO2_WORKSET_META_FNAME)) {
						fileName = item;
						break;
					}
				}

				InputStream is = wso2Agent.getResource(worksetRepoPrefix
						+ username + WSO2Agent.separator + worksetId
						+ WSO2Agent.separator
						+ Constants.WSO2_WORKSET_META_FNAME);
				Properties worksetMeta = new Properties();
				worksetMeta.load(is);

				WorksetMetaInfo worksetMetaInfo = new WorksetMetaInfo(
						worksetId, fileName, worksetMeta.getProperty(
								Constants.WSO2_WORKSET_METAFILE_SETNAME,
								"Unknown"), worksetMeta.getProperty(
								Constants.WSO2_WORKSET_METAFILE_SETDESP,
								"Unknown"));
				currentWorksetInfoList.add(worksetMetaInfo);
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

	public String execute() {
		String retValue = loadWorksetInfo();
		if (!SUCCESS.equals(retValue))
			return retValue;

		return SUCCESS;
	}

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

				logger.debug("Items to be deleted = " + deletionList.toString());
			}

			if (currentWorksetNewFile != null)
				logger.debug("new file length = "
						+ currentWorksetNewFile.length);

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
			WSO2Agent wso2Agent = agentsRepo.getWSO2Agent();

			String userWorksetRepoPrefix = PortalConfiguration
					.getRegistryWorksetPrefix()
					+ username
					+ WSO2Agent.separator;

			// update existing worksets

			int idx = 0;

			if (currentWorksetInfoList != null
					&& currentWorksetInfoList.size() > 0) {
				for (int i = 0; i < currentWorksetInfoList.size(); i++) {
					String worksetPath = userWorksetRepoPrefix
							+ currentWorksetInfoList.get(i).getUUID();

					if ((currentWorksetCheckbox != null)
							&& currentWorksetCheckbox.contains(String
									.valueOf(i))) {

						logger.info(String.format("User %s deleted workset %s",
								username, worksetPath));

						wso2Agent.deleteResource(worksetPath);
					} else {

						// user uploads a new workset archive file
						if (updateList.contains(i)) {

							logger.info(String.format(
									"User %s uploaded a new workset %s",
									username,
									currentWorksetNewFileFileName[idx]));

							// remove old workset archive file
							List<String> items = wso2Agent
									.getAllChildren(worksetPath);

							String archiveFileName = null;
							for (String item : items) {
								if (!item
										.equals(Constants.WSO2_WORKSET_META_FNAME)) {
									archiveFileName = item;
									break;
								}
							}

							archiveFileName = worksetPath + WSO2Agent.separator
									+ archiveFileName;
							wso2Agent.deleteResource(archiveFileName);
							logger.info("Deleted old workset file "
									+ archiveFileName);

							String path = worksetPath + WSO2Agent.separator
									+ currentWorksetNewFileFileName[idx];

							// upload archive file
							String outPath = wso2Agent
									.postResource(
											path,
											FileUtils
													.readFileToByteArray(currentWorksetNewFile[idx]),
											currentWorksetNewFileContentType[idx]);

							logger.info(String
									.format("New workset %s saved to %s",
											currentWorksetNewFileFileName[idx],
											outPath));

							// update index
							idx++;

						}

						// update metadata file anyway
						String metaFilePath = worksetPath + WSO2Agent.separator
								+ Constants.WSO2_WORKSET_META_FNAME;

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

						String outPath = wso2Agent.postResource(metaFilePath,
								os.toByteArray(), "text");
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
					String path = userWorksetRepoPrefix + worksetId
							+ WSO2Agent.separator + newWorksetFileName.get(i);

					// upload archive file
					String outPath = wso2Agent.postResource(path,
							FileUtils.readFileToByteArray(newWorkset.get(i)),
							newWorksetContentType.get(i));

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

					String metaFilePath = userWorksetRepoPrefix + worksetId
							+ WSO2Agent.separator
							+ Constants.WSO2_WORKSET_META_FNAME;
					outPath = wso2Agent.postResource(metaFilePath,
							os.toByteArray(), "text");
					logger.info(String.format("New metafile %s saved to %s",
							Constants.WSO2_WORKSET_META_FNAME, outPath));
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

}
