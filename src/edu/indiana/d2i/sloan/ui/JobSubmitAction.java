package edu.indiana.d2i.sloan.ui;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import com.opensymphony.xwork2.ActionSupport;

import edu.indiana.d2i.sigiri.FrameworkTypeEnum;
import edu.indiana.d2i.sigiri.ImageTypeEnum;
import edu.indiana.d2i.sigiri.JobDescriptionType;
import edu.indiana.d2i.sigiri.JobTypeEnumeration;
import edu.indiana.d2i.sigiri.NameValuePairType;
import edu.indiana.d2i.sigiri.SigiriAgent;
import edu.indiana.d2i.sigiri.SigiriConstants;
import edu.indiana.d2i.sloan.AgentsRepoSingleton;
import edu.indiana.d2i.sloan.Constants;
import edu.indiana.d2i.sloan.exception.ErrorType;
import edu.indiana.d2i.sloan.exception.InvalidJobName;
import edu.indiana.d2i.sloan.exception.JobAlreadyExistException;
import edu.indiana.d2i.sloan.exception.NullExecutableException;
import edu.indiana.d2i.sloan.exception.PathNotExistException;
import edu.indiana.d2i.wso2.JobProperty;
import edu.indiana.d2i.wso2.LibFileExt;
import edu.indiana.d2i.wso2.WSO2Agent;

@SuppressWarnings("serial")
public class JobSubmitAction extends ActionSupport {
	private static final Logger logger = Logger
			.getLogger(JobSubmitAction.class);
	private static ErrorType errorType = ErrorType.NOERROR;
	private String username;
	private String selectedJob; // it is the job title given by the user
	private File jobDesp;
	private String jobDespContentType;
	private String jobDespFileName;
	private JobDescriptionType jobDespForm;
	private File executable;
	private String executableContentType;
	private String executableFileName;
	private List<File> dependancy;
	private List<String> dependancyContentType;
	private List<String> dependancyFileName;
	private String imageTypeStr;
	private String frameworkTypeStr;
	private List<String> imageTypes;
	private List<String> frameworkTypes;
	private JobTypeEnumeration[] jobTypes;
	private List<String> pubExeRepo;
	private List<String> userExeRepo;
	private List<String> pubLibRepo;
	private List<String> userLibRepo;
	private String errMsg;

	private String selectedPubExe;
	private String selectedPubLib;

	private String selectedUsrExe;
	private String selectedUsrLib;

	private void initJobDespForm() {
		jobDespForm = new JobDescriptionType();
		List<NameValuePairType> pairList = jobDespForm.getEnvironment();

		// set job type, default single
		jobDespForm.setJobType(JobTypeEnumeration.SINGLE);

		NameValuePairType imgeType = new NameValuePairType();
		// set imgeType
		imgeType.setName(SigiriConstants.IMAGE_TYPE);
		imgeType.setValue("m1.small");
		pairList.add(imgeType);

		// set instance #
		NameValuePairType instanceNum = new NameValuePairType();
		instanceNum.setName(SigiriConstants.INSTANCE_NUMBER);
		instanceNum.setValue("1");
		pairList.add(instanceNum);

		// set framework
		NameValuePairType framework = new NameValuePairType();
		framework.setName(SigiriConstants.SOFTWARE_FRAMEWORK);
		framework.setValue("java");
		pairList.add(framework);

		// set output file
		NameValuePairType outFile = new NameValuePairType();
		outFile.setName(SigiriConstants.OUTPUT_FILE);
		outFile.setValue("");
		pairList.add(outFile);
	}

	public JobSubmitAction() {
		imageTypeStr = SigiriConstants.IMAGE_TYPE;
		frameworkTypeStr = SigiriConstants.SOFTWARE_FRAMEWORK;
		imageTypes = Arrays.asList(ImageTypeEnum.images);
		frameworkTypes = Arrays.asList(FrameworkTypeEnum.frameworks);
		jobTypes = JobTypeEnumeration.values();
	}

	public String getImageTypeStr() {
		return imageTypeStr;
	}

	private String loadResInfo() {
		initJobDespForm();
		try {
			AgentsRepoSingleton agentsRepo = AgentsRepoSingleton.getInstance();
			WSO2Agent wso2Agent = agentsRepo.getWSO2Agent();
			Properties props = agentsRepo.getProps();

			String exeRepPrefix = props
					.getProperty(Constants.PN_WSO2_RES_EXE_PREFIX);
			String libRepPrefix = props
					.getProperty(Constants.PN_WSO2_RES_LIB_PREFIX);

			pubExeRepo = wso2Agent.getAllChildren(exeRepPrefix + "public");
			userExeRepo = wso2Agent.getAllChildren(exeRepPrefix + username);

			pubLibRepo = wso2Agent.getAllChildren(libRepPrefix + "public");
			userLibRepo = wso2Agent.getAllChildren(libRepPrefix + username);

		} catch (RegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			errorType = ErrorType.SIGIRI_SERVICE_UNREACHABLE;
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
		} finally {
			switch (errorType) {
			case REGISTRY_SERVICE_UNREACHABLE:
				errMsg = "Sorry, error occurs when uploading the job to registry";
				break;
			case PATH_NOT_EXIST:
				errMsg = "Sorry, cannot find the job description xml file in registry...";
				break;
			case NOERROR:
				errMsg = "";
				break;
			}
		}

		return SUCCESS;
	}

	public String jobSubmit() {
		return loadResInfo();
	}

	private void showJosDespForm() {
		List<NameValuePairType> pairList = jobDespForm.getEnvironment();
		System.out.println("Command line : " + jobDespForm.getExecutable());
		System.out.println("Job type : " + jobDespForm.getJobType());
		for (NameValuePairType pair : pairList) {
			System.out.println(pair.getName() + " : " + pair.getValue());
		}

	}

	public String execute() {

		try {
			showJosDespForm();
			System.out.println("Selected pub exe = " + this.selectedPubExe);
			System.out.println("Selected usr exe = " + this.selectedUsrExe);
			System.out.println("Selected pub lib = " + this.selectedPubLib);
			System.out.println("Selected usr lib = " + this.selectedUsrLib);

			System.out.println(pubExeRepo);

			uploadJob();
		} catch (RegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			errorType = ErrorType.SIGIRI_SERVICE_UNREACHABLE;
			return ERROR;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			errorType = ErrorType.SIGIRI_SERVICE_UNREACHABLE;
			return ERROR;
		} catch (JobAlreadyExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			errorType = ErrorType.JOB_ALREADY_EXIST;
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
		} catch (InvalidJobName e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			errorType = ErrorType.JOB_NAME_INVALID;
			return ERROR;
		} catch (NullExecutableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			errorType = ErrorType.NULL_EXECUTABLE;
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
				errMsg = "Sorry, error occurs when uploading the job to registry";
				break;
			case PATH_NOT_EXIST:
				errMsg = "Sorry, cannot find the job description xml file in registry...";
				break;
			case JOB_ALREADY_EXIST:
				errMsg = "Job already exists, you can update this job if you like";
				break;
			case JOB_NAME_INVALID:
				errMsg = "Invalid job name, should not contain invalid characters (white space etc)";
				break;
			case NULL_EXECUTABLE:
				errMsg = "Must upload an executable";
				break;
			case NOERROR:
				errMsg = "";
				break;
			}
		}

		return SUCCESS;
	}

	private void uploadJob() throws RegistryException, IOException,
			JobAlreadyExistException, PathNotExistException, JAXBException,
			InvalidJobName, NullExecutableException {

		AgentsRepoSingleton agentsRepo = AgentsRepoSingleton.getInstance();
		WSO2Agent wso2Agent = agentsRepo.getWSO2Agent();
		Properties props = agentsRepo.getProps();

		String pathPrefix = props.getProperty(Constants.PN_WSO2_REPO_PREFIX);

		String despFilePath = props
				.getProperty(Constants.PN_WSO2_REPO_JOB_DESP);
		String exeFilePath = props.getProperty(Constants.PN_WSO2_REPO_JOB_EXE);
		String depFilePath = props.getProperty(Constants.PN_WSO2_REPO_JOB_DEP);

		// check jobId
		// remove leading and trailing whitespaces
		selectedJob = selectedJob.trim();

		// if (selectedJob.contains(" ")) {
		// throw new InvalidJobName("Job Name should not contain spaces");
		// }

		// /**
		// * Check whether the job already exists
		// */
		// String repoPath = pathPrefix + username;
		// List<String> currentJobs = wso2Agent.getAllChildren(repoPath);
		// if (currentJobs.contains(selectedJob)) {
		// logger.warn(String.format("Job %s already exits", selectedJob));
		// throw new JobAlreadyExistException(selectedJob + " already exits");
		// }

		String internalJobId = UUID.randomUUID().toString();
		String jobPath = pathPrefix + username + "/" + internalJobId + "/";
		logger.info("New job pathname = " + jobPath);

		/**
		 * check and modify the uploaded job description xml file, make sure it
		 * conforms to the xsd
		 */

		JobDescriptionType jobDescriptionType = null;

		/**
		 * If user uploads the job desp file, simply use it, otherwise generate
		 * the job desp from the submitted form
		 */
		if (jobDesp != null) {
			InputStream is = new ByteArrayInputStream(
					FileUtils.readFileToByteArray(jobDesp));
			jobDescriptionType = SigiriAgent.readConfigXML(is);
			if (logger.isDebugEnabled()) {
				logger.debug("Use uploaded job desp file\n");
			}
		} else {
			jobDescriptionType = jobDespForm;
			jobDespFileName = SigiriConstants.DEFAULT_JOB_DESP_NAME;
			jobDespContentType = SigiriConstants.DEFAULT_JOB_DESP_CONTENT_TYPE;
			if (logger.isDebugEnabled()) {
				logger.debug("Use submitted form to generate job desp file\n");
			}
		}

		List<NameValuePairType> pairList = jobDescriptionType.getEnvironment();

		// set username
		jobDescriptionType.setLocalUserId(username);

		NameValuePairType exePahtPair = new NameValuePairType();
		// set executable path
		exePahtPair.setName(SigiriConstants.EXECUTABLE_PATH);
		exePahtPair.setValue(jobPath + exeFilePath);
		pairList.add(exePahtPair);

		// set dependancy path
		NameValuePairType depPahtPair = new NameValuePairType();
		depPahtPair.setName(SigiriConstants.PROPERTY_PATH);
		depPahtPair.setValue(jobPath + depFilePath);
		pairList.add(depPahtPair);

		// set token path
		NameValuePairType tokenPahtPair = new NameValuePairType();
		tokenPahtPair.setName(SigiriConstants.TOKEN_PATH);
		tokenPahtPair.setValue(pathPrefix + username + "/"
				+ Constants.OAUTH2_TOKEN_FNAME);
		pairList.add(tokenPahtPair);

		if (logger.isDebugEnabled()) {
			logger.debug("Updated job description file\n");
			logger.debug(SigiriAgent.toXMLString(jobDescriptionType));
		}

		String outPath;

		// create an empty job.properties file
		outPath = wso2Agent.postResource(jobPath
				+ Constants.WSO2_JOB_PROP_FNAME, new byte[0], "text");
		logger.info(String.format("Empty %s saved to %s",
				Constants.WSO2_JOB_PROP_FNAME, outPath));

		// post job description
		outPath = wso2Agent.postResource(jobPath + despFilePath + "/"
				+ jobDespFileName, SigiriAgent.toXMLString(jobDescriptionType),
				jobDespContentType);

		logger.info("Job description file saved to : " + outPath);

		// post executable
		String exeRepPrefix = props
				.getProperty(Constants.PN_WSO2_RES_EXE_PREFIX);
		String[] tmp = null;
		String exeAssertMsg = "Can only select one executable!";
		if (selectedPubExe != null && !"".equals(selectedPubExe)) {
			// executable from public executable repo

			// Only one executable is allowed
			tmp = selectedPubExe.split(",");
			assert (tmp.length == 1) : exeAssertMsg;

			String symlink = jobPath + exeFilePath + "/" + tmp[0];
			String target = exeRepPrefix + "public" + "/" + tmp[0];
			wso2Agent.createSymLink(symlink, target);

			logger.info("Executable from public executable repo");
			logger.info("Create symbolic link");
			logger.info("symlik : " + symlink + " --> " + target);
		} else if (selectedUsrExe != null && !"".equals(selectedUsrExe)) {
			// executable from user's executable repo
			tmp = selectedUsrExe.split(",");
			assert (tmp.length == 1) : exeAssertMsg;

			String symlink = jobPath + exeFilePath + "/" + tmp[0];
			String target = exeRepPrefix + username + "/" + tmp[0];
			wso2Agent.createSymLink(symlink, target);

			logger.info("Executable from user's executable repo");
			logger.info("Create symbolic link");
			logger.info("symlik : " + symlink + " --> " + target);
		} else if (executable != null) {
			// user uploads the executable

			// Step one: upload this executable to user's executable repo
			String symlink = jobPath + exeFilePath + "/" + executableFileName;
			String target = exeRepPrefix + username + "/" + executableFileName;

			outPath = wso2Agent.postResource(target,
					FileUtils.readFileToByteArray(executable),
					executableContentType);
			logger.info("Executable from user upload");
			logger.info("Executable file saved to user's executable repo: "
					+ outPath);

			// Step two: create a symbolic link in the job's executable folder
			wso2Agent.createSymLink(symlink, target);
			logger.info("Create symbolic link");
			logger.info("symlik : " + symlink + " --> " + target);
		} else {
			throw new NullExecutableException("Executable can not be null");
		}

		// post dependancies
		// create the dependancy dir anyway
		wso2Agent.createDir(jobPath + depFilePath);
		String libRepPrefix = props
				.getProperty(Constants.PN_WSO2_RES_LIB_PREFIX);

		if (selectedPubLib != null && !"".equals(selectedPubLib)) {
			// dependancies from public lib repo
			/**
			 * Note that we have to trim the surrounding white spaces
			 */
			tmp = selectedPubLib.split(",");

			logger.info("Dependancies from public lib repo");
			for (String dep : tmp) {
				dep = dep.trim();
				String symlink = jobPath + depFilePath + "/" + dep;
				String target = libRepPrefix + "public" + "/" + dep;
				wso2Agent.createSymLink(symlink, target);

				logger.info("Create symbolic link");
				logger.info("symlik : " + symlink + " --> " + target);
			}
		}

		if (selectedUsrLib != null && !"".equals(selectedUsrLib)) {
			// dependancies from user's lib repo
			tmp = selectedUsrLib.split(",");

			logger.info("Dependancies from user's lib repo");
			for (String dep : tmp) {
				dep = dep.trim();
				String symlink = jobPath + depFilePath + "/" + dep;
				String target = libRepPrefix + username + "/" + dep;
				wso2Agent.createSymLink(symlink, target);

				logger.info("Create symbolic link");
				logger.info("symlik : " + symlink + " --> " + target);
			}
		}

		if (dependancy != null) {
			logger.info("Dependancies from user upload");
			List<String> libFileExt = Arrays.asList(LibFileExt.libFileExt);

			for (int i = 0; i < dependancy.size(); i++) {
				/**
				 * if the file extension is any one defined in class
				 * {@link LibFileExt}, then upload to the user's lib repo first,
				 * then create a symlink in job's dependancies dir, otherwise
				 * directly store the file under dependancies dir
				 */
				// post each uploaded dependancy
				int idx = dependancyFileName.get(i).lastIndexOf(".");
				String fileExt = null;
				if (idx != -1) {
					fileExt = dependancyFileName.get(i).substring(idx);
				}

				if (libFileExt.contains(fileExt)) {
					// file extension appears in the list

					// Step one: upload the file to user's lib repo
					String symlink = jobPath + depFilePath + "/"
							+ dependancyFileName.get(i);
					String target = libRepPrefix + username + "/"
							+ dependancyFileName.get(i);
					outPath = wso2Agent.postResource(target,
							FileUtils.readFileToByteArray(dependancy.get(i)),
							dependancyContentType.get(i));
					logger.info(String.format(
							"Dependancy file %s saved to %s ",
							dependancyFileName.get(i), outPath));

					// Step two: create a symbolic link in the job's
					// dependancies folder
					wso2Agent.createSymLink(symlink, target);
					logger.info("Create symbolic link");
					logger.info("symlik : " + symlink + " --> " + target);
				} else {
					// save file directly to the job's dependancies folder
					outPath = wso2Agent.postResource(jobPath + depFilePath
							+ "/" + dependancyFileName.get(i),
							FileUtils.readFileToByteArray(dependancy.get(i)),
							dependancyContentType.get(i));
					logger.info(String.format(
							"Dependancy file %s saved to %s ",
							dependancyFileName.get(i), outPath));
				}
			}
		}

		// set properties for the job
		Resource jobResource = wso2Agent.getRawResource(jobPath);
		jobResource.setProperty(JobProperty.JOB_TITLE, selectedJob);
		jobResource.setProperty(JobProperty.JOB_OWNER, username);
		wso2Agent.updateResource(jobPath, jobResource);
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

	public String getExecutableContentType() {
		return executableContentType;
	}

	public void setExecutableContentType(String executableContentType) {
		this.executableContentType = executableContentType;
	}

	public String getExecutableFileName() {
		return executableFileName;
	}

	public void setExecutableFileName(String executableFileName) {
		this.executableFileName = executableFileName;
	}

	public List<String> getDependancyContentType() {
		return dependancyContentType;
	}

	public void setDependancyContentType(List<String> dependancyContentType) {
		this.dependancyContentType = dependancyContentType;
	}

	public List<String> getDependancyFileName() {
		return dependancyFileName;
	}

	public void setDependancyFileName(List<String> dependancyFileName) {
		this.dependancyFileName = dependancyFileName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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

	public File getExecutable() {
		return executable;
	}

	public void setExecutable(File executable) {
		this.executable = executable;
	}

	public List<File> getDependancy() {
		return dependancy;
	}

	public void setDependancy(List<File> dependancy) {
		this.dependancy = dependancy;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public JobDescriptionType getJobDespForm() {
		return jobDespForm;
	}

	public void setJobDespForm(JobDescriptionType jobDespForm) {
		this.jobDespForm = jobDespForm;
	}

	public List<String> getImageTypes() {
		return imageTypes;
	}

	public void setImageTypes(List<String> imageTypes) {
		this.imageTypes = imageTypes;
	}

	public JobTypeEnumeration[] getJobTypes() {
		return jobTypes;
	}

	public List<String> getFrameworkTypes() {
		return frameworkTypes;
	}

	public String getFrameworkTypeStr() {
		return frameworkTypeStr;
	}

	public List<String> getPubExeRepo() {
		return pubExeRepo;
	}

	public void setPubExeRepo(List<String> pubExeRepo) {
		this.pubExeRepo = pubExeRepo;
	}

	public List<String> getUserExeRepo() {
		return userExeRepo;
	}

	public void setUserExeRepo(List<String> userExeRepo) {
		this.userExeRepo = userExeRepo;
	}

	public List<String> getPubLibRepo() {
		return pubLibRepo;
	}

	public void setPubLibRepo(List<String> pubLibRepo) {
		this.pubLibRepo = pubLibRepo;
	}

	public List<String> getUserLibRepo() {
		return userLibRepo;
	}

	public void setUserLibRepo(List<String> userLibRepo) {
		this.userLibRepo = userLibRepo;
	}

	public String getSelectedPubExe() {
		return selectedPubExe;
	}

	public void setSelectedPubExe(String selectedPubExe) {
		this.selectedPubExe = selectedPubExe;
	}

	public String getSelectedPubLib() {
		return selectedPubLib;
	}

	public void setSelectedPubLib(String selectedPubLib) {
		this.selectedPubLib = selectedPubLib;
	}

	public String getSelectedUsrExe() {
		return selectedUsrExe;
	}

	public void setSelectedUsrExe(String selectedUsrExe) {
		this.selectedUsrExe = selectedUsrExe;
	}

	public String getSelectedUsrLib() {
		return selectedUsrLib;
	}

	public void setSelectedUsrLib(String selectedUsrLib) {
		this.selectedUsrLib = selectedUsrLib;
	}
}
