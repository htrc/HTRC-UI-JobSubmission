package edu.indiana.d2i.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

import edu.indiana.d2i.AgentsRepoSingleton;
import edu.indiana.d2i.Constants;
import edu.indiana.d2i.exception.ErrorType;
import edu.indiana.d2i.exception.PathNotExistException;
import edu.indiana.d2i.oauth2.OAuth2Agent;
import edu.indiana.d2i.sigiri.JobDescriptionType;
import edu.indiana.d2i.sigiri.SigiriAgent;
import edu.indiana.d2i.wso2.WSO2Agent;
import edu.indiana.extreme.sigiri.SigiriServiceStub.JobStatus;

@SuppressWarnings("serial")
public class JobListAction extends ActionSupport {
	private static final Logger logger = Logger.getLogger(JobListAction.class);
	private static ErrorType errorType = ErrorType.NOERROR;
	private String username;
	private List<String> jobs;
	private String selectedJob;
	private List<String> ops;
	private String selectedOp;
	private String sigiriJobId;

	private static final String RUN = "Run";
	private static final String QUERY = "Query Status";
	private static final String MANAGE = "Manage";

	private static final String EXPIRE_STATUS = "expire";

	private String errMsg;

	public JobListAction() {
		ops = new ArrayList<String>();
		ops.add(RUN);
		ops.add(QUERY);
		ops.add(MANAGE);
	}

	public String getSelectedOp() {
		return selectedOp;
	}

	public void setSelectedOp(String selectedOp) {
		this.selectedOp = selectedOp;
	}

	public String getDefaultOpValue() {
		return JobListAction.RUN;
	}

	public String getSelectedJob() {
		return selectedJob;
	}

	public void setSelectedJob(String selectedJob) {
		this.selectedJob = selectedJob;
	}

	public String execute() {
		logger.info(String.format("Operation %s selected", selectedOp));

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

				// retrieve job description file

				// should only have one single desp file
				List<String> despList = wso2Agent.getAllChildren(jobPath
						+ despFilePath);
				InputStream is = wso2Agent.getResource(jobPath + despFilePath
						+ "/" + despList.get(0));

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
				logger.info(String.format("Job id returned from sigiri = ",
						sigiriJobId));

				/**
				 * write the job id to registry for future reference
				 */
				is = wso2Agent.getResource(jobPath
						+ Constants.WSO2_JOB_PROP_FNAME);
				Properties jobProp = new Properties();
				jobProp.load(is);
				jobProp.setProperty(Constants.SIGIRI_JOB_ID, sigiriJobId);
				jobProp.setProperty(Constants.SIGIRI_JOB_STATUS,
						jobStatus.getStatus());
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				jobProp.store(os, "");

				wso2Agent.postResource(jobPath + Constants.WSO2_JOB_PROP_FNAME,
						os.toByteArray(), "text");

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
				case NOERROR:
					errMsg = "";
					break;
				}
			}

			logger.info("Going to run job" + selectedJob);
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
				if (sigiriJobId == null)
					sigiriJobId = "";

				/**
				 * Let the JobQueryAction take care of the job query, we just
				 * need to pass in the sigiriJobId
				 */
				logger.info("Going to query job" + selectedJob);
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

	public String listJob() {
		try {
			AgentsRepoSingleton agentsRepo = AgentsRepoSingleton.getInstance();
			WSO2Agent wso2Agent = agentsRepo.getWSO2Agent();
			Properties props = agentsRepo.getProps();
			String pathPrefix = props
					.getProperty(Constants.PN_WSO2_REPO_PREFIX);

			String repoPath = pathPrefix + username;
			jobs = wso2Agent.getAllChildren(repoPath);

			if (logger.isDebugEnabled())
				logger.debug(String.format("User %s 's current jobs are %s",
						username, jobs));
		} catch (RegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			return ERROR;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			return ERROR;
		} catch (PathNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			return ERROR;
		} finally {
			errMsg = "Sorry, error occurs when listing existing jobs ...";
		}

		return NONE;
	}

	public List<String> getOps() {
		return ops;
	}

	public void setOps(List<String> ops) {
		this.ops = ops;
	}

	public List<String> getJobs() {
		return jobs;
	}

	public void setJobs(List<String> jobs) {
		this.jobs = jobs;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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
}
