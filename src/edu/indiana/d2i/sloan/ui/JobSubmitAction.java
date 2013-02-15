package edu.indiana.d2i.sloan.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.SessionAware;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

import edu.indiana.d2i.sloan.AgentsRepoSingleton;
import edu.indiana.d2i.sloan.Constants;
import edu.indiana.d2i.sloan.exception.PathNotExistException;
import edu.indiana.d2i.sloan.schema.internal.InternalSchemaUtil;
import edu.indiana.d2i.sloan.schema.internal.JobDescriptionType;
import edu.indiana.d2i.sloan.schema.user.UserSchemaUtil;
import edu.indiana.d2i.wso2.JobProperty;
import edu.indiana.d2i.wso2.WSO2Agent;
import edu.indiana.sloan.schema.SchemaUtil;

public class JobSubmitAction extends ActionSupport implements SessionAware,
		LoginRequired, SessionTimeoutRequired {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger
			.getLogger(JobSubmitAction.class);

	private Map<String, Object> session;

	private String selectedJob; // it is the job title given by the user

	private File jobDesp;
	private String jobDespContentType;
	private String jobDespFileName;

	private File jobArchive;
	private String jobArchiveContentType;
	private String jobArchiveFileName;

	private List<WorksetMetaInfo> worksetInfoList;
	private boolean[] worksetCheckbox;

	public static class WorksetMetaInfo {
		private String UUID;
		private String fileName;
		private String worksetTitle;
		private String worksetDesp;

		public WorksetMetaInfo(String UUID, String fileName,
				String worksetTitle, String worksetDesp) {
			this.UUID = UUID;
			this.fileName = fileName;
			this.worksetTitle = worksetTitle;
			this.worksetDesp = worksetDesp;
		}

		public String getUUID() {
			return UUID;
		}

		public void setUUID(String UUID) {
			this.UUID = UUID;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getWorksetTitle() {
			return worksetTitle;
		}

		public void setWorksetTitle(String worksetTitle) {
			this.worksetTitle = worksetTitle;
		}

		public String getWorksetDesp() {
			return worksetDesp;
		}

		public void setWorksetDesp(String worksetDesp) {
			this.worksetDesp = worksetDesp;
		}

	}

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

			worksetInfoList = new ArrayList<WorksetMetaInfo>();

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
				worksetInfoList.add(worksetMetaInfo);
			}

			if (worksetInfoList.size() > 0) {
				worksetCheckbox = new boolean[worksetInfoList.size()];
				Arrays.fill(worksetCheckbox, false);
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

	public String jobSubmitForm() {
		return loadWorksetInfo();
	}

	public String execute() {

		if (logger.isDebugEnabled()) {

			StringBuilder selectedWorksets = new StringBuilder();
			for (int i = 0; i < worksetCheckbox.length; i++) {
				if (worksetCheckbox[i]) {
					selectedWorksets.append(worksetInfoList.get(i).getUUID()
							+ "\n");
				}
			}

			if (worksetCheckbox.length > 0) {
				logger.debug("selected worksets:\n"
						+ selectedWorksets.toString());
			} else {
				logger.debug("No worksets being selected");
			}
		}

		try {
			uploadJob();
		} catch (RegistryException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		} catch (JAXBException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		}

		return SUCCESS;
	}

	private void uploadJob() throws RegistryException, IOException,
			JAXBException {

		Map<String, Object> session = ActionContext.getContext().getSession();
		String username = (String) session.get(Constants.SESSION_USERNAME);

		AgentsRepoSingleton agentsRepo = AgentsRepoSingleton.getInstance();
		WSO2Agent wso2Agent = agentsRepo.getWSO2Agent();

		String repoPrefix = PortalConfiguration.getRegistryPrefix();

		// remove leading and trailing white-spaces
		selectedJob = selectedJob.trim();

		String internalJobId = UUID.randomUUID().toString();
		String jobPath = repoPrefix + username + WSO2Agent.separator
				+ internalJobId + WSO2Agent.separator;
		logger.info("Created job pathname = " + jobPath);

		/**
		 * check user uploaded job description xml file, make sure it conforms
		 * to user xsd
		 */

		edu.indiana.d2i.sloan.schema.user.JobDescriptionType userJobDesp = null;

		InputStream is = new ByteArrayInputStream(
				FileUtils.readFileToByteArray(jobDesp));
		userJobDesp = UserSchemaUtil.readConfigXML(is);
		if (logger.isDebugEnabled()) {
			logger.debug("Use uploaded job desp file:\n");
			logger.debug(UserSchemaUtil.toXMLString(userJobDesp));
		}

		List<WorksetMetaInfo> selectedWorksets = new ArrayList<WorksetMetaInfo>();
		for (int i = 0; i < worksetCheckbox.length; i++) {
			if (worksetCheckbox[i]) {
				selectedWorksets.add(worksetInfoList.get(i));
			}
		}

		JobDescriptionType internalJobDesp = SchemaUtil.user2internal(
				userJobDesp, username, internalJobId, jobArchiveFileName,
				selectedWorksets);

		if (logger.isDebugEnabled()) {
			logger.debug("Converted internal job description:\n");
			logger.debug(InternalSchemaUtil.toXMLString(internalJobDesp));
		}

		String outPath = null;

		// create job.properties file
		Properties jobProp = new Properties();
		jobProp.setProperty(JobProperty.JOB_TITLE, selectedJob);
		jobProp.setProperty(JobProperty.JOB_OWNER, username);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		jobProp.store(os, "");

		outPath = wso2Agent.postResource(jobPath
				+ Constants.WSO2_JOB_PROP_FNAME, os.toByteArray(), "text");
		logger.info(String.format("Job property file %s saved to %s",
				Constants.WSO2_JOB_PROP_FNAME, outPath));

		// post job description
		outPath = wso2Agent.postResource(jobPath
				+ Constants.WSO2_JOB_DESP_FNAME,
				InternalSchemaUtil.toXMLString(internalJobDesp),
				jobDespContentType);

		logger.info(String.format("Job description file %s saved to %s",
				Constants.WSO2_JOB_DESP_FNAME, outPath));

		// post archive
		outPath = wso2Agent.postResource(
				jobPath + PortalConfiguration.getRegistryArchiveFolder()
						+ WSO2Agent.separator + jobArchiveFileName,
				FileUtils.readFileToByteArray(jobArchive),
				jobArchiveContentType);

		logger.info(String.format("Job archive file %s saved to %s",
				jobArchiveFileName, outPath));

		// create tmpOutput folder
		String jobTmpOutputHome = PortalConfiguration
				.getRegistryTmpOutputPrefix()
				+ username
				+ WSO2Agent.separator
				+ internalJobId;
		wso2Agent.createDir(jobTmpOutputHome);
		logger.info(String.format("Job tmpOutput home dir %s created",
				jobTmpOutputHome));
	}

	public String getSelectedJob() {
		return selectedJob;
	}

	public void setSelectedJob(String selectedJob) {
		this.selectedJob = selectedJob;
	}

	public File getJobDesp() {
		return jobDesp;
	}

	public void setJobDesp(File jobDesp) {
		this.jobDesp = jobDesp;
	}

	public String getJobDespContentType() {
		return jobDespContentType;
	}

	public void setJobDespContentType(String jobDespContentType) {
		this.jobDespContentType = jobDespContentType;
	}

	public String getJobDespFileName() {
		return jobDespFileName;
	}

	public void setJobDespFileName(String jobDespFileName) {
		this.jobDespFileName = jobDespFileName;
	}

	public File getJobArchive() {
		return jobArchive;
	}

	public void setJobArchive(File jobArchive) {
		this.jobArchive = jobArchive;
	}

	public String getJobArchiveContentType() {
		return jobArchiveContentType;
	}

	public void setJobArchiveContentType(String jobArchiveContentType) {
		this.jobArchiveContentType = jobArchiveContentType;
	}

	public String getJobArchiveFileName() {
		return jobArchiveFileName;
	}

	public void setJobArchiveFileName(String jobArchiveFileName) {
		this.jobArchiveFileName = jobArchiveFileName;
	}

	public List<WorksetMetaInfo> getWorksetInfoList() {
		return worksetInfoList;
	}

	public void setWorksetInfoList(List<WorksetMetaInfo> worksetInfoList) {
		this.worksetInfoList = worksetInfoList;
	}

	public boolean[] getWorksetCheckbox() {
		return worksetCheckbox;
	}

	public void setWorksetCheckbox(boolean[] worksetCheckbox) {
		this.worksetCheckbox = worksetCheckbox;
	}

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}

}
