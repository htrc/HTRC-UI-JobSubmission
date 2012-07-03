package edu.indiana.d2i.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

import edu.indiana.d2i.AgentsRepoSingleton;
import edu.indiana.d2i.Constants;
import edu.indiana.d2i.exception.ErrorType;
import edu.indiana.d2i.exception.JobDespNotExistException;
import edu.indiana.d2i.exception.JobExeNotExistException;
import edu.indiana.d2i.exception.NullSigiriJobIdException;
import edu.indiana.d2i.exception.PathNotExistException;
import edu.indiana.d2i.oauth2.OAuth2Agent;
import edu.indiana.d2i.sigiri.JobDescriptionType;
import edu.indiana.d2i.sigiri.NameValuePairType;
import edu.indiana.d2i.sigiri.SigiriAgent;
import edu.indiana.d2i.sigiri.SigiriConstants;
import edu.indiana.d2i.wso2.JobProperty;
import edu.indiana.d2i.wso2.WSO2Agent;
import edu.indiana.extreme.sigiri.SigiriServiceStub.JobStatus;

@SuppressWarnings("serial")
public class SearchResult extends ActionSupport {
	private static final Logger logger = Logger.getLogger(SearchResult.class);
	private static ErrorType errorType = ErrorType.NOERROR;
	private String username;
	private String selectedJobTitle;
	private String errMsg;

	private List<RadioItem> jobInsList = new ArrayList<RadioItem>();
	private List<JobMetaInfo> jobInfoList = new ArrayList<JobMetaInfo>();

	private static final String RUN = "Run";
	private static final String QUERY = "Query Status";
	private static final String MANAGE = "Manage";

	private static final String EXPIRE_STATUS = "expire";

	private List<String> ops;
	private String selectedOp;

	private String sigiriJobId;
	private String selectedJob;

	private String selectedInsId;

	public static class RadioItem {
		private String key;
		private String label;

		public RadioItem(String key, String label) {
			this.key = key;
			this.label = label;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

	}

	public static class JobMetaInfo implements Comparable<JobMetaInfo> {
		private Date createdTime;
		private String createdTimeStr;
		private Date lastModifiedTime;
		private String lastModifiedTimeStr;
		private String owner;
		private String internalJobId;
		private String jobTitle;
		private String jobStatus;
		private String lastStatusUpdateTimeStr;

		public String getCreatedTimeStr() {
			return createdTimeStr;
		}

		public void setCreatedTimeStr(String createdTimeStr) {
			this.createdTimeStr = createdTimeStr;
		}

		public String getLastModifiedTimeStr() {
			return lastModifiedTimeStr;
		}

		public void setLastModifiedTimeStr(String lastModifiedTimeStr) {
			this.lastModifiedTimeStr = lastModifiedTimeStr;
		}

		public String getOwner() {
			return owner;
		}

		public void setOwner(String owner) {
			this.owner = owner;
		}

		public String getJobTitle() {
			return jobTitle;
		}

		public void setJobTitle(String jobTitle) {
			this.jobTitle = jobTitle;
		}

		public static String date2Str(Date date) {
			// return DateFormat.getDateInstance(DateFormat.LONG).format(date);
			return date.toString();
		}

		public void initDateStr() {
			if (createdTime != null)
				createdTimeStr = JobMetaInfo.date2Str(createdTime);

			if (lastModifiedTime != null)
				lastModifiedTimeStr = JobMetaInfo.date2Str(lastModifiedTime);
		}

		public String getInternalJobId() {
			return internalJobId;
		}

		public void setInternalJobId(String internalJobId) {
			this.internalJobId = internalJobId;
		}

		public Date getCreatedTime() {
			return createdTime;
		}

		public void setCreatedTime(Date createdTime) {
			this.createdTime = createdTime;
		}

		public Date getLastModifiedTime() {
			return lastModifiedTime;
		}

		public void setLastModifiedTime(Date lastModifiedTime) {
			this.lastModifiedTime = lastModifiedTime;
		}

		public String getJobStatus() {
			return jobStatus;
		}

		public void setJobStatus(String jobStatus) {
			this.jobStatus = jobStatus;
		}

		@Override
		public int compareTo(JobMetaInfo jobMetaInfo) {
			// TODO Auto-generated method stub
			int res = jobTitle.compareTo(jobMetaInfo.getJobTitle());
			if (res != 0)
				return res;

			res = createdTime.compareTo(jobMetaInfo.createdTime);
			if (res != 0)
				return res;
			else {
				return lastModifiedTime.compareTo(jobMetaInfo.lastModifiedTime);
			}
		}

		public String getLastStatusUpdateTimeStr() {
			return lastStatusUpdateTimeStr;
		}

		public void setLastStatusUpdateTimeStr(String lastStatusUpdateTimeStr) {
			this.lastStatusUpdateTimeStr = lastStatusUpdateTimeStr;
		}
	}

	public String getDefaultOpValue() {
		return SearchResult.RUN;
	}

	public String execute() {
		logger.debug(String.format("Operation %s selected", selectedOp));
		logger.debug(String.format("Job %s selected", selectedJob));
		logger.debug(String.format("Job Title is %s", selectedJobTitle));

		if (selectedJob == null || "".equals(selectedJob)) {
			errMsg = "Please select a job instance";
			return ERROR;
		}

		if (RUN.equals(selectedOp)) {
			try {
				/**
				 * check whether token has expired
				 */
				AgentsRepoSingleton agentsRepo = AgentsRepoSingleton
						.getInstance();
				OAuth2Agent oauth2Agent = agentsRepo.getOAuth2Agent();
				WSO2Agent wso2Agent = agentsRepo.getWSO2Agent();
				Properties props = agentsRepo.getProps();

				Map<String, Object> session = ActionContext.getContext()
						.getSession();
				String oauth2Token = (String) session.get("oauth2_token");

				if (logger.isDebugEnabled())
					logger.debug(String
							.format("Token %s retrieved from session, validity check against OAuth2 server",
									oauth2Token));

				if (oauth2Agent.isTokenExpire(oauth2Token)) {
					logger.info(String
							.format("Token %s has expired, redirect user %s to login page",
									oauth2Token, username));
					return EXPIRE_STATUS;
				}

				String pathPrefix = props
						.getProperty(Constants.PN_WSO2_REPO_PREFIX);
				String despFilePath = props
						.getProperty(Constants.PN_WSO2_REPO_JOB_DESP);
				String exeFilePath = props
						.getProperty(Constants.PN_WSO2_REPO_JOB_EXE);
				String depFilePath = props
						.getProperty(Constants.PN_WSO2_REPO_JOB_DEP);

				String jobPath = pathPrefix + username + "/" + selectedJob
						+ "/";

				logger.info(String.format("Job path is %s", jobPath));

				/**
				 * write token as a property file, all jobs share this token
				 * file, which is stored directly under user's home directory.
				 * Each (re)run will update this file
				 */
				StringBuilder sb = new StringBuilder();

				sb.append("token=" + oauth2Token + "\n");
				String outPath = wso2Agent.postResource(pathPrefix + username
						+ "/" + Constants.OAUTH2_TOKEN_FNAME, sb.toString(),
						"text");

				if (logger.isDebugEnabled())
					logger.debug(String.format("%s saved to %s",
							Constants.OAUTH2_TOKEN_FNAME, outPath));

				/**
				 * If the selected instances has been run previously, then we
				 * need to create a new job instance as we don't want to
				 * overwrite previous one,
				 */
				InputStream is = wso2Agent.getResource(jobPath + "/"
						+ Constants.WSO2_JOB_PROP_FNAME);
				Properties jobProp = new Properties();
				jobProp.load(is);

				String internalJobId = null;
				boolean isNewInstance = false;
				sigiriJobId = jobProp.getProperty(Constants.SIGIRI_JOB_ID);
				if (sigiriJobId != null && !"NoJobId".equals(sigiriJobId)) {
					// need to create a new job instance
					logger.info(String
							.format("Job instance %s has been executed before, going to create a new one",
									selectedJob));

					internalJobId = UUID.randomUUID().toString();
					isNewInstance = true;
					jobPath = pathPrefix + username + "/" + internalJobId + "/";
					logger.info("Internal job id allocated for the new job : "
							+ internalJobId);
					String existingJobPath = pathPrefix + username + "/"
							+ selectedJob + "/";

					/**
					 * create an empty job.properties file
					 */

					outPath = wso2Agent.postResource(jobPath
							+ Constants.WSO2_JOB_PROP_FNAME, new byte[0],
							"text");
					logger.info(String.format("Empty %s saved to %s",
							Constants.WSO2_JOB_PROP_FNAME, outPath));

					/**
					 * copy job description
					 */
					String despFileName = null;
					List<String> despList = wso2Agent
							.getAllChildren(existingJobPath + despFilePath);
					if (despList != null && despList.size() > 0) {
						despFileName = despList.get(0);
					} else {
						throw new JobDespNotExistException(
								"Cannot find job description file");
					}

					String src = existingJobPath + despFilePath + "/"
							+ despFileName;
					String jobDespContentType = wso2Agent.getRawResource(src)
							.getMediaType();
					String dest = jobPath + despFilePath + "/" + despFileName;

					/**
					 * modify job description file
					 */
					is = wso2Agent.getResource(src);
					JobDescriptionType jobDespForm = SigiriAgent
							.readConfigXML(is);

					List<NameValuePairType> pairs = jobDespForm
							.getEnvironment();

					for (NameValuePairType pair : pairs) {
						if (pair.getName().equals(
								SigiriConstants.EXECUTABLE_PATH)) {
							pair.setValue(jobPath + exeFilePath);
							continue;
						}

						if (pair.getName()
								.equals(SigiriConstants.PROPERTY_PATH))
							pair.setValue(jobPath + depFilePath);
					}

					outPath = wso2Agent.postResource(dest,
							SigiriAgent.toXMLString(jobDespForm),
							jobDespContentType);

					logger.info("new description file saved to " + outPath);

					/**
					 * copy executable
					 */
					// retrieve executable name
					String exeName = null;
					List<String> oldExe = wso2Agent
							.getAllChildren(existingJobPath + exeFilePath);
					if (oldExe != null && oldExe.size() > 0) {
						exeName = oldExe.get(0);
					} else {
						throw new JobExeNotExistException(
								"Cannot find job executable file");
					}

					src = existingJobPath + exeFilePath + "/" + exeName;
					dest = jobPath + exeFilePath + "/" + exeName;
					outPath = wso2Agent.copy(src, dest);
					logger.info("Copy executable file " + exeName + " from "
							+ src + " to " + outPath);

					/**
					 * copy dependencies
					 */
					List<String> depns = wso2Agent
							.getAllChildren(existingJobPath + depFilePath);

					if (depns != null && depns.size() > 0) {
						for (String dep : depns) {
							src = existingJobPath + depFilePath + "/" + dep;
							dest = jobPath + depFilePath + "/" + dep;
							outPath = wso2Agent.copy(src, dest);
							logger.info("Copy dependency file " + dep
									+ " from " + src + " to " + outPath);
						}
					}

					/**
					 * copy meta data
					 */
					logger.info("copy job meta data for the new job instance");
					Resource existJobResource = wso2Agent
							.getRawResource(existingJobPath);
					Resource jobResource = wso2Agent.getRawResource(jobPath);
					jobResource
							.setProperty(JobProperty.JOB_TITLE,
									existJobResource
											.getProperty(JobProperty.JOB_TITLE));
					jobResource
							.setProperty(JobProperty.JOB_OWNER,
									existJobResource
											.getProperty(JobProperty.JOB_OWNER));
					wso2Agent.updateResource(jobPath, jobResource);
				}

				// retrieve job description file

				// should only have one single desp file
				List<String> despList = wso2Agent.getAllChildren(jobPath
						+ despFilePath);
				is = wso2Agent.getResource(jobPath + despFilePath + "/"
						+ despList.get(0));

				JobDescriptionType jobDescriptionType = SigiriAgent
						.readConfigXML(is);
				String jobDescriptionXMLStr = SigiriAgent
						.toXMLString(jobDescriptionType);

				if (logger.isDebugEnabled()) {
					logger.debug("Job Desp XML String submitted to Sigiri:\n");
					logger.debug(jobDescriptionXMLStr);
				}

				// submit to sigiri service
				SigiriAgent sigiriAgent = agentsRepo.getSigiriAgent();
				JobStatus jobStatus = sigiriAgent
						.submitJob(jobDescriptionXMLStr);

				sigiriJobId = jobStatus.getJobId();

				if (sigiriJobId == null || "".equals(sigiriJobId))
					throw new NullSigiriJobIdException(
							"None sigiri job id returned");

				logger.info(String.format("Job id returned from sigiri = "
						+ sigiriJobId));

				/**
				 * write the job id to registry for future reference
				 */
				is = wso2Agent.getResource(jobPath + "/"
						+ Constants.WSO2_JOB_PROP_FNAME);
				jobProp = new Properties();
				jobProp.load(is);
				jobProp.setProperty(Constants.SIGIRI_JOB_ID, sigiriJobId);
				jobProp.setProperty(Constants.SIGIRI_JOB_STATUS,
						jobStatus.getStatus());
				jobProp.setProperty(Constants.SIGIRI_JOB_STATUS_UPDATE_TIME,
						new Date(System.currentTimeMillis()).toString());
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				jobProp.store(os, "");

				wso2Agent.postResource(jobPath + "/"
						+ Constants.WSO2_JOB_PROP_FNAME, os.toByteArray(),
						"text");

				if (isNewInstance) {
					selectedInsId = internalJobId;
				} else {
					selectedInsId = selectedJob;
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
				errorType = ErrorType.SIGIRI_SERVICE_UNREACHABLE;
				return ERROR;
			} catch (PathNotExistException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(e.getMessage());
				errorType = ErrorType.PATH_NOT_EXIST;
				return ERROR;
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(e.getMessage());
				errorType = ErrorType.JOB_DESP_SCHEMA_INVALID;
				return ERROR;
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(e.getMessage());
				errorType = ErrorType.XML_PARSE_ERROR;
				return ERROR;
			} catch (NullSigiriJobIdException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(e.getMessage());
				errorType = ErrorType.NULL_SIGIRI_JOB_ID;
				return ERROR;
			} catch (JobExeNotExistException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JobDespNotExistException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				switch (errorType) {
				case UNKNOWN:
					errMsg = "Sorry, error occurs when submit the job ...";
					break;
				case JOB_DESP_SCHEMA_INVALID:
					errMsg = "Job description file does't conform to the schema";
					break;
				case REGISTRY_SERVICE_UNREACHABLE:
					errMsg = "Registry service is unreachable now, please try later ...";
					break;
				case SIGIRI_SERVICE_UNREACHABLE:
					errMsg = "Sigiri service is unreachable now, please try later ...";
					break;
				case XML_PARSE_ERROR:
					errMsg = "Sorry, error occurs when parsing the job description xml file ...";
					break;
				case PATH_NOT_EXIST:
					errMsg = "Sorry, cannot find the job description xml file in registry...";
					break;
				case NULL_SIGIRI_JOB_ID:
					errMsg = "Error, none sigiri job id returned ...";
					break;
				case NOERROR:
					errMsg = "";
					break;
				}
			}

			logger.info("Going to run job " + selectedInsId);
			return RUN.toLowerCase();
		} else if (MANAGE.equals(selectedOp)) {
			logger.info("Going to manage job" + selectedJob);
			return MANAGE.toLowerCase();
		} else if (QUERY.equals(selectedOp)) {
			try {
				AgentsRepoSingleton agentsRepo = AgentsRepoSingleton
						.getInstance();
				WSO2Agent wso2Agent = agentsRepo.getWSO2Agent();
				Properties props = agentsRepo.getProps();
				String pathPrefix = props
						.getProperty(Constants.PN_WSO2_REPO_PREFIX);

				String jobPath = pathPrefix + username + "/" + selectedJob
						+ "/";

				// retrieve job id
				InputStream is = wso2Agent.getResource(jobPath + "/"
						+ Constants.WSO2_JOB_PROP_FNAME);
				Properties jobProp = new Properties();
				jobProp.load(is);
				sigiriJobId = jobProp.getProperty(Constants.SIGIRI_JOB_ID);

				/**
				 * null indicated that the job has never been submitted to
				 * sigiri for execution before
				 */
				if (sigiriJobId == null) {
					sigiriJobId = "";
					logger.info("No sigiri job id for job " + selectedJob);
				}

				/**
				 * Let the JobQueryAction take care of the job query, we just
				 * need to pass in the sigiriJobId
				 */
				selectedInsId = selectedJob;
				logger.info("Going to query job " + selectedInsId);
				return RUN.toLowerCase();

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
					errMsg = "Sorry, error occurs when submit the job ...";
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

		}

		// Actually this statement should never be reached
		return SUCCESS;
	}

	public String showSearchRes() {
		logger.debug("username = " + username);
		logger.debug("selectedJobTitle = " + selectedJobTitle);

		selectedJobTitle = selectedJobTitle.trim();

		ops = new ArrayList<String>();
		ops.add(RUN);
		ops.add(QUERY);
		ops.add(MANAGE);

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

			jobInfoList = new ArrayList<JobMetaInfo>();

			for (String jobId : internalJobIds) {

				String jobPath = pathPrefix + username + "/" + jobId;
				logger.debug("job path = " + jobPath);

				Resource jobRes = wso2Agent.getRawResource(jobPath);
				String jobTitle = jobRes.getProperty(JobProperty.JOB_TITLE);

				if (jobTitle != null && !"".equals(jobTitle)) {
					if (jobTitle.trim().equalsIgnoreCase(selectedJobTitle)) {

						JobMetaInfo jobMetaInfo = new JobMetaInfo();

						jobMetaInfo.setInternalJobId(jobId);
						jobMetaInfo.setJobTitle(jobTitle);
						jobMetaInfo.setOwner(jobRes
								.getProperty(JobProperty.JOB_OWNER));
						jobMetaInfo.setCreatedTime(jobRes.getCreatedTime());
						jobMetaInfo.setLastModifiedTime(jobRes
								.getLastModified());

						jobMetaInfo.initDateStr();

						/**
						 * Retrieve job Status info
						 */
						InputStream is = wso2Agent.getResource(jobPath + "/"
								+ Constants.WSO2_JOB_PROP_FNAME);
						Properties jobProp = new Properties();
						jobProp.load(is);
						String jobStatus = jobProp
								.getProperty(Constants.SIGIRI_JOB_STATUS);
						if (jobStatus == null || "".equals(jobStatus))
							jobMetaInfo.setJobStatus("Not started");
						else
							jobMetaInfo.setJobStatus(jobStatus);

						String updatedTime = jobProp
								.getProperty(Constants.SIGIRI_JOB_STATUS_UPDATE_TIME);
						if (updatedTime == null || "".equals(updatedTime))
							jobMetaInfo
									.setLastStatusUpdateTimeStr("Not Available");
						else
							jobMetaInfo.setLastStatusUpdateTimeStr(updatedTime);

						jobInfoList.add(jobMetaInfo);
					}
				} else {
					logger.error("No job title associated with job : " + jobId);
				}

			}

			// sort the list
			Collections.sort(jobInfoList);

			jobInsList = new ArrayList<RadioItem>();

			for (int i = 0; i < jobInfoList.size(); i++) {
				JobMetaInfo jobMetaInfo = jobInfoList.get(i);
				jobInsList.add(new RadioItem(jobMetaInfo.getInternalJobId(),
						String.valueOf(i + 1)));
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

		return SUCCESS;
	}

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

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public List<RadioItem> getJobInsList() {
		return jobInsList;
	}

	public void setJobInsList(List<RadioItem> jobInsList) {
		this.jobInsList = jobInsList;
	}

	public List<JobMetaInfo> getJobInfoList() {
		return jobInfoList;
	}

	public void setJobInfoList(List<JobMetaInfo> jobInfoList) {
		this.jobInfoList = jobInfoList;
	}

	public List<String> getOps() {
		return ops;
	}

	public void setOps(List<String> ops) {
		this.ops = ops;
	}

	public String getSelectedOp() {
		return selectedOp;
	}

	public void setSelectedOp(String selectedOp) {
		this.selectedOp = selectedOp;
	}

	public String getSelectedJob() {
		return selectedJob;
	}

	public void setSelectedJob(String selectedJob) {
		this.selectedJob = selectedJob;
	}

	public String getSigiriJobId() {
		return sigiriJobId;
	}

	public void setSigiriJobId(String sigiriJobId) {
		this.sigiriJobId = sigiriJobId;
	}

	public String getSelectedInsId() {
		return selectedInsId;
	}

	public void setSelectedInsId(String selectedInsId) {
		this.selectedInsId = selectedInsId;
	}
}
