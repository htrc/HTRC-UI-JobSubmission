package edu.indiana.d2i.sloan.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.interceptor.SessionAware;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

import edu.indiana.d2i.sigiri.SigiriAgent;
import edu.indiana.d2i.sloan.AgentsRepoSingleton;
import edu.indiana.d2i.sloan.Constants;
import edu.indiana.d2i.sloan.exception.NullSigiriJobIdException;
import edu.indiana.d2i.sloan.exception.PathNotExistException;
import edu.indiana.d2i.sloan.schema.internal.InternalSchemaUtil;
import edu.indiana.d2i.sloan.schema.internal.JobDescriptionType;
import edu.indiana.d2i.sloan.schema.user.UserSchemaUtil;
import edu.indiana.d2i.wso2.ArchiveFileExt;
import edu.indiana.d2i.wso2.JobProperty;
import edu.indiana.d2i.wso2.WSO2Agent;
import edu.indiana.extreme.sigiri.SigiriServiceStub.JobStatus;
import edu.indiana.sloan.schema.SchemaUtil;

public class JobSubmitAction extends ActionSupport implements SessionAware,
		LoginRequired, SessionTimeoutRequired {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger
			.getLogger(JobSubmitAction.class);

	private Map<String, Object> session;

	private String errMsg = null;

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

		public WorksetMetaInfo() {
		}

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

		@Override
		public String toString() {

			return String.format(
					"UUID=%s, title=%s, description=%s, filename=%s", UUID,
					worksetTitle, worksetDesp, fileName);
		}

	}

	public boolean isValidateForm() {

		if (getSelectedJob() == null || "".equals(getSelectedJob())) {
			errMsg = "Please specify a job title";
			logger.error(errMsg);
			return false;
		}

		if (getJobDesp() == null) {
			errMsg = "Please upload a job description file";
			logger.error(errMsg);
			return false;
		}

		if (!getJobDespFileName().endsWith(".xml")) {
			errMsg = "Job description must be an .xml file";
			logger.error(errMsg);
			return false;
		}

		if (getJobArchive() == null) {
			errMsg = "Please upload a job archive file";
			logger.error(errMsg);
			return false;
		}

		if (!ArchiveFileExt.isValidExt(getJobArchiveFileName())) {
			errMsg = "Job archive file: " + ArchiveFileExt.MSG;
			logger.error(errMsg);
			return false;
		}

		return true;
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

	public void checkError() {
		if (errMsg == null || "".equals(errMsg)) {
			return;
		}
		addActionError(errMsg);
	}

	public String jobSubmitForm() {
		checkError();
		return loadWorksetInfo();
	}

	public String execute() {

		if (!isValidateForm())
			return INPUT;

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
		} catch (NullSigiriJobIdException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		} catch (XMLStreamException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		}

		return SUCCESS;
	}

	private void uploadJob() throws RegistryException, IOException,
			JAXBException, NullSigiriJobIdException, XMLStreamException {

		Map<String, Object> session = ActionContext.getContext().getSession();
		String username = (String) session.get(Constants.SESSION_USERNAME);

		AgentsRepoSingleton agentsRepo = AgentsRepoSingleton.getInstance();
		WSO2Agent wso2Agent = agentsRepo.getWSO2Agent();

		// remove leading and trailing white-spaces
		selectedJob = selectedJob.trim();

		String internalJobId = UUID.randomUUID().toString();
		String jobPath = PortalConfiguration.getRegistryPrefix() + username
				+ WSO2Agent.separator + internalJobId + WSO2Agent.separator;
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

		String internalJobDespStr = InternalSchemaUtil
				.toXMLString(internalJobDesp);

		if (logger.isDebugEnabled()) {
			logger.debug("Converted internal job description:\n");
			logger.debug(internalJobDespStr);
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
				+ Constants.WSO2_JOB_DESP_FNAME, internalJobDespStr,
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

		// submit to sigiri service
		SigiriAgent sigiriAgent = agentsRepo.getSigiriAgent();
		JobStatus jobStatus = sigiriAgent.submitJob(internalJobDespStr);

		String sigiriJobId = jobStatus.getJobId();

		if (sigiriJobId == null || "".equals(sigiriJobId))
			throw new NullSigiriJobIdException("None sigiri job id returned");

		/**
		 * write the job id to registry for future reference
		 */
		is = wso2Agent.getResource(jobPath + Constants.WSO2_JOB_PROP_FNAME);
		jobProp = new Properties();
		jobProp.load(is);
		jobProp.setProperty(Constants.SIGIRI_JOB_ID, sigiriJobId);
		jobProp.setProperty(Constants.SIGIRI_JOB_STATUS, jobStatus.getStatus());
		jobProp.setProperty(Constants.SIGIRI_JOB_STATUS_UPDATE_TIME, new Date(
				System.currentTimeMillis()).toString());
		os = new ByteArrayOutputStream();
		jobProp.store(os, "");

		wso2Agent.postResource(jobPath + Constants.WSO2_JOB_PROP_FNAME,
				os.toByteArray(), "text");
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

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

}
