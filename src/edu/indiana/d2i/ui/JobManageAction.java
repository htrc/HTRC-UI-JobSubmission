package edu.indiana.d2i.ui;

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

import edu.indiana.d2i.AgentsRepoSingleton;
import edu.indiana.d2i.Constants;
import edu.indiana.d2i.exception.ErrorType;
import edu.indiana.d2i.exception.JobDespNotExistException;
import edu.indiana.d2i.exception.JobExeNotExistException;
import edu.indiana.d2i.exception.PathNotExistException;
import edu.indiana.d2i.sigiri.FrameworkTypeEnum;
import edu.indiana.d2i.sigiri.ImageTypeEnum;
import edu.indiana.d2i.sigiri.JobDescriptionType;
import edu.indiana.d2i.sigiri.JobTypeEnumeration;
import edu.indiana.d2i.sigiri.NameValuePairType;
import edu.indiana.d2i.sigiri.SigiriAgent;
import edu.indiana.d2i.sigiri.SigiriConstants;
import edu.indiana.d2i.wso2.JobProperty;
import edu.indiana.d2i.wso2.LibFileExt;
import edu.indiana.d2i.wso2.WSO2Agent;

@SuppressWarnings("serial")
public class JobManageAction extends ActionSupport {
	private static final Logger logger = Logger
			.getLogger(JobManageAction.class);
	private static ErrorType errorType = ErrorType.NOERROR;
	private String username;
	private String selectedJob;
	// list of dependancies in the repository
	private List<String> dependancy;
	// list of dependancies kept, separated by comma
	private String keepDepnList;

	private File newJobDesp;
	private String newJobDespContentType;
	private String newJobDespFileName;

	private File newExecutable;
	private String newExecutableContentType;
	private String newExecutableFileName;

	private List<File> newDependancy;
	private List<String> newDependancyContentType;
	private List<String> newDependancyFileName;

	private String errMsg;

	private JobDescriptionType jobDespForm;

	private String selectedPubExe;
	private String selectedPubLib;

	private String selectedUsrExe;
	private String selectedUsrLib;

	private String imageTypeStr;
	private String frameworkTypeStr;
	private String instanceNumStr;
	private String outputFileStr;

	private List<String> imageTypes;
	private List<String> frameworkTypes;

	private JobTypeEnumeration[] jobTypes;

	private List<String> pubExeRepo;
	private List<String> userExeRepo;
	private List<String> pubLibRepo;
	private List<String> userLibRepo;

	private String defaultImageType;
	private String defaultFramework;

	private String jobDespFileName;
	private String jobDespContentType;

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public String getKeepDepnList() {
		return keepDepnList;
	}

	public void setKeepDepnList(String keepDepnList) {
		this.keepDepnList = keepDepnList;
	}

	public File getNewJobDesp() {
		return newJobDesp;
	}

	public void setNewJobDesp(File newJobDesp) {
		this.newJobDesp = newJobDesp;
	}

	public String getNewJobDespContentType() {
		return newJobDespContentType;
	}

	public void setNewJobDespContentType(String newJobDespContentType) {
		this.newJobDespContentType = newJobDespContentType;
	}

	public String getNewJobDespFileName() {
		return newJobDespFileName;
	}

	public void setNewJobDespFileName(String newJobDespFileName) {
		this.newJobDespFileName = newJobDespFileName;
	}

	public File getNewExecutable() {
		return newExecutable;
	}

	public void setNewExecutable(File newExecutable) {
		this.newExecutable = newExecutable;
	}

	public String getNewExecutableContentType() {
		return newExecutableContentType;
	}

	public void setNewExecutableContentType(String newExecutableContentType) {
		this.newExecutableContentType = newExecutableContentType;
	}

	public String getNewExecutableFileName() {
		return newExecutableFileName;
	}

	public void setNewExecutableFileName(String newExecutableFileName) {
		this.newExecutableFileName = newExecutableFileName;
	}

	public List<File> getNewDependancy() {
		return newDependancy;
	}

	public void setNewDependancy(List<File> newDependancy) {
		this.newDependancy = newDependancy;
	}

	public List<String> getNewDependancyContentType() {
		return newDependancyContentType;
	}

	public void setNewDependancyContentType(
			List<String> newDependancyContentType) {
		this.newDependancyContentType = newDependancyContentType;
	}

	public List<String> getNewDependancyFileName() {
		return newDependancyFileName;
	}

	public void setNewDependancyFileName(List<String> newDependancyFileName) {
		this.newDependancyFileName = newDependancyFileName;
	}

	public List<String> getDependancy() {
		return dependancy;
	}

	public void setDependancy(List<String> dependancy) {
		this.dependancy = dependancy;
	}

	public String[] getDefaultDepnList() {
		String[] defaults = new String[dependancy.size()];
		dependancy.toArray(defaults);
		return defaults;
	}

	public String execute() {
		try {
			logger.info("jobDespFileName = " + jobDespFileName);
			logger.info("jobDespContentType = " + jobDespContentType);

			System.out.println("Selected pub exe = " + this.selectedPubExe);
			System.out.println("Selected usr exe = " + this.selectedUsrExe);
			System.out.println("Selected pub lib = " + this.selectedPubLib);
			System.out.println("Selected usr lib = " + this.selectedUsrLib);

			AgentsRepoSingleton agentsRepo = AgentsRepoSingleton.getInstance();
			WSO2Agent wso2Agent = agentsRepo.getWSO2Agent();
			Properties props = agentsRepo.getProps();

			String pathPrefix = props
					.getProperty(Constants.PN_WSO2_REPO_PREFIX);
			String despFilePath = props
					.getProperty(Constants.PN_WSO2_REPO_JOB_DESP);
			String exeFilePath = props
					.getProperty(Constants.PN_WSO2_REPO_JOB_EXE);
			String depFilePath = props
					.getProperty(Constants.PN_WSO2_REPO_JOB_DEP);

			String jobPath = pathPrefix + username + "/" + selectedJob + "/";

			/**
			 * First check whether the selected job has been executed before, if
			 * no, simply update this job, otherwise create a new job instance.
			 * To do so, simply check whether the job id assigned by the sigiri
			 * service is null or not in the job.properties file.
			 */
			InputStream is = wso2Agent.getResource(jobPath
					+ Constants.WSO2_JOB_PROP_FNAME);
			Properties jobProp = new Properties();
			jobProp.load(is);

			boolean isNewInstance = true;
			String internalJobId = null;

			if (jobProp.getProperty(Constants.SIGIRI_JOB_ID) == null) {
				// update existing job
				internalJobId = selectedJob;
				isNewInstance = false;
				logger.info("job " + selectedJob
						+ " has NOT been executed before.");
				logger.info("Simply update this job");
			} else {
				// create a new job
				internalJobId = UUID.randomUUID().toString();
				isNewInstance = true;
				logger.info("job " + selectedJob + " has been executed before.");
				logger.info("Create a new job instance");
			}

			jobPath = pathPrefix + username + "/" + internalJobId + "/";

			String outPath;

			if (isNewInstance) {
				// create an empty job.properties file
				outPath = wso2Agent.postResource(jobPath
						+ Constants.WSO2_JOB_PROP_FNAME, new byte[0], "text");
				logger.info(String.format("Empty %s saved to %s",
						Constants.WSO2_JOB_PROP_FNAME, outPath));
			}

			/****************** Job desp file *******************/
			JobDescriptionType jobDescriptionType = null;

			if (this.newJobDesp != null) {
				/**
				 * check and modify the uploaded job description xml file, make
				 * sure it conforms to the xsd
				 */
				is = new ByteArrayInputStream(
						FileUtils.readFileToByteArray(newJobDesp));
				jobDescriptionType = SigiriAgent.readConfigXML(is);
				if (logger.isDebugEnabled()) {
					logger.debug("Use newly uploaded job desp file\n");
				}

				// add auto-generated name-value pair
				List<NameValuePairType> pairList = jobDescriptionType
						.getEnvironment();

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

			} else {
				// update the job desp using the form
				jobDescriptionType = jobDespForm;
				if (logger.isDebugEnabled()) {
					logger.debug("Use submitted form to update job desp file\n");
				}

				/**
				 * if new instance, then have to update the executable path and
				 * dependancy path
				 */
				if (isNewInstance) {
					List<NameValuePairType> pairs = jobDescriptionType
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
				}
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Updated job description file\n");
				logger.debug(SigiriAgent.toXMLString(jobDescriptionType));
			}

			// if updating, delete the old job desp
			if (!isNewInstance) {
				// delete old one
				List<String> oldDesp = wso2Agent.getAllChildren(jobPath
						+ despFilePath);
				if (oldDesp != null && oldDesp.size() > 0) {
					logger.info("Delete old desp file : " + oldDesp.get(0));
					wso2Agent.deleteResource(jobPath + despFilePath + "/"
							+ oldDesp.get(0));
				} else {
					throw new JobDespNotExistException(
							"Cannot find job description file");
				}
			}

			// post new job description
			if (this.newJobDesp != null) {
				logger.info("Upload new job description file "
						+ newJobDespFileName);
				outPath = wso2Agent.postResource(jobPath + despFilePath + "/"
						+ newJobDespFileName,
						SigiriAgent.toXMLString(jobDescriptionType),
						newJobDespContentType);
			} else {
				logger.info("Update existing description file "
						+ jobDespFileName);
				outPath = wso2Agent.postResource(jobPath + despFilePath + "/"
						+ jobDespFileName,
						SigiriAgent.toXMLString(jobDescriptionType),
						jobDespContentType);
			}

			logger.info("New job description file saved to " + outPath);

			/****************** executable *******************/

			// if updating and user updates the executable, delete old one
			if (!isNewInstance
					&& ((selectedPubExe != null && !"".equals(selectedPubExe))
							|| (selectedUsrExe != null && !""
									.equals(selectedUsrExe)) || (newExecutable != null))) {
				// delete old one
				List<String> oldExe = wso2Agent.getAllChildren(jobPath
						+ exeFilePath);
				if (oldExe != null && oldExe.size() > 0) {
					logger.info("Delete old exe file : " + oldExe.get(0));
					wso2Agent.deleteResource(jobPath + exeFilePath + "/"
							+ oldExe.get(0));
				} else {
					throw new JobExeNotExistException(
							"Cannot find job executable file");
				}
			}

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
			} else if (newExecutable != null) {
				// user uploads the executable

				// Step one: upload this executable to user's executable repo
				String symlink = jobPath + exeFilePath + "/"
						+ newExecutableFileName;
				String target = exeRepPrefix + username + "/"
						+ newExecutableFileName;

				outPath = wso2Agent.postResource(target,
						FileUtils.readFileToByteArray(newExecutable),
						newExecutableContentType);
				logger.info("Executable from user upload");
				logger.info("Executable file saved to user's executable repo: "
						+ outPath);

				// Step two: create a symbolic link in the job's executable
				// folder
				wso2Agent.createSymLink(symlink, target);
				logger.info("Create symbolic link");
				logger.info("symlik : " + symlink + " --> " + target);
			} else {
				/**
				 * if new job instance, we should copy the executable to the new
				 * job
				 */
				if (isNewInstance) {
					// retrieve executable name
					String existingJobPath = pathPrefix + username + "/"
							+ selectedJob + "/";
					String exeName = null;
					List<String> oldExe = wso2Agent
							.getAllChildren(existingJobPath + exeFilePath);
					if (oldExe != null && oldExe.size() > 0) {
						exeName = oldExe.get(0);
					} else {
						throw new JobExeNotExistException(
								"Cannot find job executable file");
					}

					String src = existingJobPath + exeFilePath + "/" + exeName;
					String dest = jobPath + exeFilePath + "/" + exeName;
					outPath = wso2Agent.copy(src, dest);
					logger.info("Copy executable file " + exeName + " to "
							+ outPath);
				}
			}

			/****************** dependancy *******************/
			if (isNewInstance)
				wso2Agent.createDir(jobPath + depFilePath);

			if (!isNewInstance) {
				/**
				 * keepDepnList == null indicates that no dependancies being
				 * uploaded previously
				 */
				if (keepDepnList != null) {
					logger.info("Dependancies to keep : " + keepDepnList);
					if ("".equals(keepDepnList)) {
						logger.info("Delete all previous dependancies");

						for (String dep : dependancy) {
							logger.info("Deleting dep " + dep);
							wso2Agent.deleteResource(jobPath + depFilePath
									+ "/" + dep);
						}
					} else {
						List<String> keepDepArray = Arrays.asList(keepDepnList
								.split(","));
						// remove surrounding white spaces
						for (int i = 0; i < keepDepArray.size(); i++)
							keepDepArray.set(i, keepDepArray.get(i).trim());

						logger.info("Keep dependancy list = " + keepDepArray);
						logger.info("All previous dependancy = " + dependancy);

						for (String dep : dependancy) {
							if (!keepDepArray.contains(dep)) {
								logger.info("Deleting dep " + dep);
								wso2Agent.deleteResource(jobPath + depFilePath
										+ "/" + dep);
							}
						}
					}
				}
			} else {
				if (keepDepnList != null) {
					if (!"".equals(keepDepnList)) {
						String existingJobPath = pathPrefix + username + "/"
								+ selectedJob + "/";
						List<String> keepDepArray = Arrays.asList(keepDepnList
								.split(","));
						// remove surrounding white spaces
						for (int i = 0; i < keepDepArray.size(); i++)
							keepDepArray.set(i, keepDepArray.get(i).trim());

						logger.info("Dependancies to keep for new job instance: "
								+ keepDepArray);

						for (String dep : keepDepArray) {
							logger.info("Copy dependancy " + dep
									+ " to new job instance");
							String src = existingJobPath + depFilePath + "/"
									+ dep;
							String dest = jobPath + depFilePath + "/" + dep;
							outPath = wso2Agent.copy(src, dest);
							logger.info("Copy dependacy file " + dep + " to "
									+ outPath);
						}
					}
				}
			}

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

			if (newDependancy != null) {
				logger.info("New dependancies from user upload");
				List<String> libFileExt = Arrays.asList(LibFileExt.libFileExt);

				for (int i = 0; i < newDependancy.size(); i++) {
					/**
					 * if the file extension is any one defined in class
					 * {@link LibFileExt}, then upload to the user's lib repo
					 * first, then create a symlink in job's dependancies dir,
					 * otherwise directly store the file under dependancies dir
					 */
					// post each uploaded dependancy
					int idx = newDependancyFileName.get(i).lastIndexOf(".");
					String fileExt = null;
					if (idx != -1) {
						fileExt = newDependancyFileName.get(i).substring(idx);
					}

					if (libFileExt.contains(fileExt)) {
						// file extension appears in the list

						// Step one: upload the file to user's lib repo
						String symlink = jobPath + depFilePath + "/"
								+ newDependancyFileName.get(i);
						String target = libRepPrefix + username + "/"
								+ newDependancyFileName.get(i);
						outPath = wso2Agent.postResource(target, FileUtils
								.readFileToByteArray(newDependancy.get(i)),
								newDependancyContentType.get(i));
						logger.info(String.format(
								"Dependancy file %s saved to %s ",
								newDependancyFileName.get(i), outPath));

						// Step two: create a symbolic link in the job's
						// dependancies folder
						wso2Agent.createSymLink(symlink, target);
						logger.info("Create symbolic link");
						logger.info("symlik : " + symlink + " --> " + target);
					} else {
						// save file directly to the job's dependancies folder
						outPath = wso2Agent.postResource(jobPath + depFilePath
								+ "/" + newDependancyFileName.get(i), FileUtils
								.readFileToByteArray(newDependancy.get(i)),
								newDependancyContentType.get(i));
						logger.info(String.format(
								"Dependancy file %s saved to %s ",
								newDependancyFileName.get(i), outPath));
					}
				}
			}

			/****************** job meta data *******************/
			if (isNewInstance) {
				logger.info("copy job meta data for the new job instance");
				String existingJobPath = pathPrefix + username + "/"
						+ selectedJob + "/";
				Resource existJobResource = wso2Agent
						.getRawResource(existingJobPath);
				Resource jobResource = wso2Agent.getRawResource(jobPath);
				jobResource.setProperty(JobProperty.JOB_TITLE,
						existJobResource.getProperty(JobProperty.JOB_TITLE));
				jobResource.setProperty(JobProperty.JOB_OWNER,
						existJobResource.getProperty(JobProperty.JOB_OWNER));
				wso2Agent.updateResource(jobPath, jobResource);
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
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			errorType = ErrorType.JOB_DESP_SCHEMA_INVALID;
			return ERROR;
		} catch (JobDespNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			errorType = ErrorType.JOB_DESP_NOT_EXIST;
		} catch (JobExeNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			errorType = ErrorType.JOB_EXE_NOT_EXIST;
		} finally {
			switch (errorType) {
			case UNKNOWN:
				errMsg = "Sorry, error occurs when updating the job "
						+ selectedJob;
				break;
			case JOB_DESP_SCHEMA_INVALID:
				errMsg = "Job description file does't conform to the schema";
				break;
			case REGISTRY_SERVICE_UNREACHABLE:
				errMsg = "Registry service is unreachable now, please try later ...";
				break;
			case PATH_NOT_EXIST:
				errMsg = "Sorry, cannot find the job description xml file in registry...";
				break;
			case JOB_DESP_NOT_EXIST:
				errMsg = "Cannot find and delete old job description file";
				break;
			case JOB_EXE_NOT_EXIST:
				errMsg = "Cannot find and delete old job executable file";
				break;
			case NOERROR:
				errMsg = "";
				break;
			}
		}

		return SUCCESS;
	}

	public String listJobInfo() {
		logger.info(String.format("List user %s 's job info of job %s",
				username, selectedJob));

		try {
			AgentsRepoSingleton agentsRepo = AgentsRepoSingleton.getInstance();
			WSO2Agent wso2Agent = agentsRepo.getWSO2Agent();

			Properties props = agentsRepo.getProps();
			String pathPrefix = props
					.getProperty(Constants.PN_WSO2_REPO_PREFIX);

			// get dependancy list
			String depnPath = pathPrefix + username + "/" + selectedJob + "/"
					+ props.getProperty(Constants.PN_WSO2_REPO_JOB_DEP);
			System.out.println(depnPath);

			dependancy = wso2Agent.getAllChildren(depnPath);

			logger.info("dependancy list = " + dependancy);

			// load job description info
			String despPath = pathPrefix + username + "/" + selectedJob + "/"
					+ props.getProperty(Constants.PN_WSO2_REPO_JOB_DESP);
			List<String> oldDesp = wso2Agent.getAllChildren(despPath);

			if (oldDesp != null && oldDesp.size() > 0) {
				logger.info("Retrieve desp file : " + oldDesp.get(0));

				this.jobDespFileName = oldDesp.get(0);
				this.jobDespContentType = wso2Agent.getRawResource(
						despPath + "/" + oldDesp.get(0)).getMediaType();

				logger.info("jobDespFileName = " + jobDespFileName);
				logger.info("jobDespContentType = " + jobDespContentType);

				logger.info(despPath);
				InputStream is = wso2Agent.getResource(despPath + "/"
						+ oldDesp.get(0));
				jobDespForm = SigiriAgent.readConfigXML(is);

				List<NameValuePairType> pairs = jobDespForm.getEnvironment();

				for (NameValuePairType pair : pairs) {
					if (pair.getName().equals(SigiriConstants.IMAGE_TYPE)) {
						defaultImageType = pair.getValue();
						continue;
					}

					if (pair.getName().equals(
							SigiriConstants.SOFTWARE_FRAMEWORK))
						defaultFramework = pair.getValue();
				}

			} else {
				throw new JobDespNotExistException(
						"Cannot find job description file");
			}

			// load fields info
			imageTypeStr = SigiriConstants.IMAGE_TYPE;
			frameworkTypeStr = SigiriConstants.SOFTWARE_FRAMEWORK;
			instanceNumStr = SigiriConstants.INSTANCE_NUMBER;
			outputFileStr = SigiriConstants.OUTPUT_FILE;

			imageTypes = Arrays.asList(ImageTypeEnum.images);
			frameworkTypes = Arrays.asList(FrameworkTypeEnum.frameworks);
			jobTypes = JobTypeEnumeration.values();

			// prepare public/personal executable repository
			// prepare public/personal dependancy repository
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
			// e.printStackTrace();
			logger.error(e.getMessage());
			errorType = ErrorType.PATH_NOT_EXIST;
			return ERROR;
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			errorType = ErrorType.XML_PARSE_ERROR;
		} catch (JobDespNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			errorType = ErrorType.JOB_DESP_NOT_EXIST;
		} finally {
			switch (errorType) {
			case UNKNOWN:
				errMsg = "Sorry, error occurs when retrieving job info for job "
						+ selectedJob;
				break;
			case REGISTRY_SERVICE_UNREACHABLE:
				errMsg = "Registry service is unreachable now, please try later ...";
				break;
			case PATH_NOT_EXIST:
				errMsg = "Sorry, cannot find the job description xml file in registry ...";
				break;
			case XML_PARSE_ERROR:
				errMsg = "Sorry, error occurs when loading job description xml file ...";
				break;
			case JOB_DESP_NOT_EXIST:
				errMsg = "Cannot find job description file";
				break;
			case NOERROR:
				errMsg = "";
				break;
			}
		}

		return NONE;
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

	public JobDescriptionType getJobDespForm() {
		return jobDespForm;
	}

	public void setJobDespForm(JobDescriptionType jobDespForm) {
		this.jobDespForm = jobDespForm;
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

	public String getImageTypeStr() {
		return imageTypeStr;
	}

	public void setImageTypeStr(String imageTypeStr) {
		this.imageTypeStr = imageTypeStr;
	}

	public String getFrameworkTypeStr() {
		return frameworkTypeStr;
	}

	public void setFrameworkTypeStr(String frameworkTypeStr) {
		this.frameworkTypeStr = frameworkTypeStr;
	}

	public List<String> getImageTypes() {
		return imageTypes;
	}

	public void setImageTypes(List<String> imageTypes) {
		this.imageTypes = imageTypes;
	}

	public List<String> getFrameworkTypes() {
		return frameworkTypes;
	}

	public void setFrameworkTypes(List<String> frameworkTypes) {
		this.frameworkTypes = frameworkTypes;
	}

	public JobTypeEnumeration[] getJobTypes() {
		return jobTypes;
	}

	public void setJobTypes(JobTypeEnumeration[] jobTypes) {
		this.jobTypes = jobTypes;
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

	public String getInstanceNumStr() {
		return instanceNumStr;
	}

	public void setInstanceNumStr(String instanceNumStr) {
		this.instanceNumStr = instanceNumStr;
	}

	public String getOutputFileStr() {
		return outputFileStr;
	}

	public void setOutputFileStr(String outputFileStr) {
		this.outputFileStr = outputFileStr;
	}

	public String getDefaultImageType() {
		return defaultImageType;
	}

	public String getDefaultFramework() {
		return defaultFramework;
	}

	public String getJobDespFileName() {
		return jobDespFileName;
	}

	public void setJobDespFileName(String jobDespFileName) {
		this.jobDespFileName = jobDespFileName;
	}

	public String getJobDespContentType() {
		return jobDespContentType;
	}

	public void setJobDespContentType(String jobDespContentType) {
		this.jobDespContentType = jobDespContentType;
	}
}
