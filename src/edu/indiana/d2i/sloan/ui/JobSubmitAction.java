package edu.indiana.d2i.sloan.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

import edu.indiana.d2i.registryext.RegistryExtAgent;
import edu.indiana.d2i.registryext.RegistryExtAgent.GetResourceResponse;
import edu.indiana.d2i.registryext.RegistryExtAgent.ListResourceResponse;
import edu.indiana.d2i.registryext.RegistryExtAgent.ResourceISType;
import edu.indiana.d2i.registryext.schema.Entry;
import edu.indiana.d2i.sigiri.SigiriAgent;
import edu.indiana.d2i.sloan.AgentsRepoSingleton;
import edu.indiana.d2i.sloan.Constants;
import edu.indiana.d2i.sloan.exception.NullSigiriJobIdException;
import edu.indiana.d2i.sloan.exception.RegistryExtException;
import edu.indiana.d2i.sloan.schema.internal.InternalSchemaUtil;
import edu.indiana.d2i.sloan.schema.internal.JobDescriptionType;
import edu.indiana.d2i.sloan.schema.user.UserSchemaUtil;
import edu.indiana.d2i.wso2.JobProperty;
import edu.indiana.extreme.sigiri.SigiriServiceStub.JobStatus;
import edu.indiana.sloan.schema.SchemaUtil;

public class JobSubmitAction extends ActionSupport implements SessionAware,
		LoginRequired, SessionTimeoutRequired {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger
			.getLogger(JobSubmitAction.class);

	@SuppressWarnings("unused")
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
	private List<String> worksetCheckbox;

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

	public boolean isValidForm() {
		/**
		 * Currently all basic validations are moved to front end (e.g.,
		 * javascript), Other complex validations can be put in this method as
		 * backend validation
		 */

		// if (!ArchiveFileExt.isValidExt(getJobArchiveFileName())) {
		// errMsg = "Job archive file: " + ArchiveFileExt.MSG;
		// logger.error(errMsg);
		// return false;
		// }

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
			RegistryExtAgent registryExtAgent = agentsRepo
					.getRegistryExtAgent();

			StringBuilder requestURL = new StringBuilder();
			ListResourceResponse response = registryExtAgent
					.getAllChildren(requestURL
							.append(PortalConfiguration
									.getRegistryWorksetPrefix())
							.append("?user=").append(username).toString());

			List<String> userWorksetRepo = new ArrayList<String>();

			if (response.getStatusCode() == 404) {
				logger.warn(String.format("Path %s doesn't exist",
						requestURL.toString()));
			} else if (response.getStatusCode() == 200) {
				for (Entry entry : response.getEntries().getEntry()) {
					userWorksetRepo.add(entry.getName());
				}
			}

			worksetInfoList = new ArrayList<WorksetMetaInfo>();

			for (String worksetId : userWorksetRepo) {

				StringBuilder url = new StringBuilder();
				ListResourceResponse resp = registryExtAgent.getAllChildren(url
						.append(PortalConfiguration.getRegistryWorksetPrefix())
						.append(worksetId).append("?user=").append(username)
						.toString());

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
						.append(Constants.WSO2_WORKSET_META_FNAME)
						.append("?user=").append(username).toString());

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
				worksetInfoList.add(worksetMetaInfo);
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

		if (!isValidForm())
			return INPUT;

		if (logger.isDebugEnabled()) {

			if (worksetCheckbox != null) {
				StringBuilder worksetList = new StringBuilder();
				for (String idx : worksetCheckbox)
					worksetList.append(idx + " ");

				logger.debug("Selected worksets=" + worksetList.toString());
			} else {
				logger.debug("No worksets being selected");
			}

		}

		try {
			uploadJob();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		} catch (JAXBException e) {
			logger.error(e.getMessage(), e);
			errMsg = "Invalid job description file";
			logger.error(errMsg);
			addActionError(e.getMessage());
			return INPUT;
		} catch (NullSigiriJobIdException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		} catch (XMLStreamException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		} catch (RegistryExtException e) {
			logger.error(e.getMessage(), e);
			addActionError(e.getMessage());
			return ERROR;
		}

		return SUCCESS;
	}

	private void uploadJob() throws IOException, JAXBException,
			NullSigiriJobIdException, XMLStreamException, RegistryExtException {

		Map<String, Object> session = ActionContext.getContext().getSession();
		String username = (String) session.get(Constants.SESSION_USERNAME);
		String accessToken = (String) session.get(Constants.SESSION_TOKEN);

		AgentsRepoSingleton agentsRepo = AgentsRepoSingleton.getInstance();
		RegistryExtAgent registryExtAgent = agentsRepo.getRegistryExtAgent();

		/**
		 * Update token: write token as a property file, all jobs share this
		 * token file, which is stored directly under user's home directory. (In
		 * registry extension, it is under /htrc/username/files/sloan/jobs/).
		 * Each job submission will update this file
		 */

		Properties tokenFile = new Properties();

		tokenFile.setProperty(Constants.OAUTH2_ACCESS_TOKEN, accessToken);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		tokenFile.store(os, "");

		String outPath = registryExtAgent.postResource(
				new StringBuilder()
						.append(PortalConfiguration.getRegistryJobPrefix())
						.append(Constants.OAUTH2_TOKEN_FNAME).append("?user=")
						.append(username).toString(), new ResourceISType(
						new ByteArrayInputStream(os.toByteArray()),
						Constants.OAUTH2_TOKEN_FNAME, "text/plain"));

		logger.info(String.format("Updated token file %s saved to %s",
				Constants.OAUTH2_TOKEN_FNAME, outPath));

		// remove leading and trailing white-spaces
		selectedJob = selectedJob.trim();

		/* generate registry internal job id */
		String internalJobId = UUID.randomUUID().toString();

		String jobPath = PortalConfiguration.getRegistryJobPrefix()
				+ internalJobId + RegistryExtAgent.separator;
		logger.info("Create new job home=" + jobPath);

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

		/**
		 * Pick user selected worksets
		 */
		List<WorksetMetaInfo> selectedWorksets = new ArrayList<WorksetMetaInfo>();

		if (worksetCheckbox != null) {
			for (String idx : worksetCheckbox) {
				selectedWorksets.add(worksetInfoList.get(Integer.valueOf(idx)));
			}
		}

		/**
		 * Convert user schema to internal schema used by sigiri daemon
		 */
		JobDescriptionType internalJobDesp = SchemaUtil.user2internal(
				userJobDesp, username, internalJobId, jobArchiveFileName,
				selectedWorksets);

		String internalJobDespStr = InternalSchemaUtil
				.toXMLString(internalJobDesp);

		if (logger.isDebugEnabled()) {
			logger.debug("Converted internal job description:\n");
			logger.debug(internalJobDespStr);
		}

		// create job.properties file
		Properties jobProp = new Properties();
		jobProp.setProperty(JobProperty.JOB_TITLE, selectedJob);
		jobProp.setProperty(JobProperty.JOB_OWNER, username);
		jobProp.setProperty(JobProperty.JOB_CREATION_TIME,
				new Date(System.currentTimeMillis()).toString());

		os = new ByteArrayOutputStream();
		jobProp.store(os, "");

		outPath = registryExtAgent.postResource(
				new StringBuilder().append(jobPath)
						.append(Constants.WSO2_JOB_PROP_FNAME).append("?user=")
						.append(username).toString(), new ResourceISType(
						new ByteArrayInputStream(os.toByteArray()),
						Constants.WSO2_JOB_PROP_FNAME, "text/plain"));

		logger.info(String.format("Job property file %s saved to %s",
				Constants.WSO2_JOB_PROP_FNAME, outPath));

		// post job description
		outPath = registryExtAgent.postResource(
				new StringBuilder().append(jobPath)
						.append(Constants.WSO2_JOB_DESP_FNAME).append("?user=")
						.append(username).toString(),
				new ResourceISType(new ByteArrayInputStream(internalJobDespStr
						.getBytes()), Constants.WSO2_JOB_DESP_FNAME,
						jobDespContentType));

		logger.info(String.format("Job description file %s saved to %s",
				Constants.WSO2_JOB_DESP_FNAME, outPath));

		// post archive
		outPath = registryExtAgent.postResource(
				new StringBuilder().append(jobPath)
						.append(PortalConfiguration.getRegistryArchiveFolder())
						.append(RegistryExtAgent.separator)
						.append(jobArchiveFileName).append("?user=")
						.append(username).toString(), new ResourceISType(
						new FileInputStream(jobArchive), jobArchiveFileName,
						jobArchiveContentType));

		logger.info(String.format("Job archive file %s saved to %s",
				jobArchiveFileName, outPath));

		// submit to sigiri service
		SigiriAgent sigiriAgent = agentsRepo.getSigiriAgent();
		JobStatus jobStatus = sigiriAgent.submitJob(internalJobDespStr);

		String sigiriJobId = jobStatus.getJobId();

		if (sigiriJobId == null || "".equals(sigiriJobId))
			throw new NullSigiriJobIdException("None sigiri job id returned");

		/**
		 * write the job id to registry for future reference
		 */
		jobProp.setProperty(JobProperty.SIGIRI_JOB_ID, sigiriJobId);
		jobProp.setProperty(JobProperty.SIGIRI_JOB_STATUS,
				jobStatus.getStatus());

		jobProp.setProperty(JobProperty.SIGIRI_JOB_STATUS_UPDATE_TIME,
				new Date(System.currentTimeMillis()).toString());
		os = new ByteArrayOutputStream();
		jobProp.store(os, "");

		outPath = registryExtAgent.postResource(
				new StringBuilder().append(jobPath)
						.append(Constants.WSO2_JOB_PROP_FNAME).append("?user=")
						.append(username).toString(), new ResourceISType(
						new ByteArrayInputStream(os.toByteArray()),
						Constants.WSO2_JOB_PROP_FNAME, "text/plain"));

		logger.info(String.format("Updated job property file %s saved to %s",
				Constants.WSO2_JOB_PROP_FNAME, outPath));
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

	public List<String> getWorksetCheckbox() {
		return worksetCheckbox;
	}

	public void setWorksetCheckbox(List<String> worksetCheckbox) {
		this.worksetCheckbox = worksetCheckbox;
	}

}
