package edu.indiana.sloan.schema;

import java.io.IOException;
import java.util.List;

import edu.indiana.d2i.sloan.Constants;
import edu.indiana.d2i.sloan.schema.internal.JobDescriptionType;
import edu.indiana.d2i.sloan.schema.internal.JobTypeEnumeration;
import edu.indiana.d2i.sloan.schema.internal.TransferRequestType;
import edu.indiana.d2i.sloan.schema.internal.TransferType;
import edu.indiana.d2i.sloan.schema.internal.WorkSets;
import edu.indiana.d2i.sloan.ui.PortalConfiguration;
import edu.indiana.d2i.sloan.ui.JobSubmitAction.WorksetMetaInfo;
import edu.indiana.d2i.wso2.WSO2Agent;

public class SchemaUtil {

	/**
	 * transform user job description to internal job description
	 * 
	 * @param userJobDesp
	 * @param username
	 * @param jobInternalId
	 * @param archiveFileName
	 * @param worksetInfoList
	 * @return
	 * @throws IOException
	 */
	public static JobDescriptionType user2internal(
			edu.indiana.d2i.sloan.schema.user.JobDescriptionType userJobDesp,
			String username, String jobInternalId, String archiveFileName,
			List<WorksetMetaInfo> worksetInfoList) throws IOException {

		JobDescriptionType internalJobDesp = new JobDescriptionType();

		/* set user id */
		internalJobDesp.setLocalUserId(username);

		/* set command line */
		internalJobDesp.setCommandLine(userJobDesp.getCommandLine());

		/* set job type */
		switch (userJobDesp.getJobType()) {
		case PIG:
			internalJobDesp.setJobType(JobTypeEnumeration.PIG);
			break;
		case MAPREDUCE:
			internalJobDesp.setJobType(JobTypeEnumeration.MAPREDUCE);
			break;
		}

		/* set VM count */
		// if user VM count setting is null, then internal VM count setting is
		// null
		internalJobDesp.setVMCount(userJobDesp.getVMCount());

		/* set estimated execution time */
		// if user execution time setting is null, then internal execution time
		// setting is null
		internalJobDesp.setExecutionTime(userJobDesp.getExecutionTime());

		/* set files need staged out */
		List<TransferRequestType> fileStageOut = internalJobDesp
				.getFileStageOut();

		for (edu.indiana.d2i.sloan.schema.user.TransferRequestType userFileOut : userJobDesp
				.getFileStageOut()) {

			TransferRequestType internalFileOut = new TransferRequestType();

			/* set src path */
			internalFileOut.setSrcPath(userFileOut.getPath());
			/* set src type */
			switch (userFileOut.getType()) {
			case HEADNODE:
				internalFileOut.setSrcType(TransferType.HEADNODE);
				break;
			case HDFS:
				internalFileOut.setSrcType(TransferType.HDFS);
				break;
			}

			/* set dest path */
			internalFileOut.setDestPath(PortalConfiguration
					.getRegistryTmpOutputPrefix()
					+ username
					+ WSO2Agent.separator + jobInternalId);

			/* set dest type */
			internalFileOut.setDestType(TransferType.WSO_2_REGISTRY);

			fileStageOut.add(internalFileOut);
		}

		/* set token path */
		TransferRequestType tokenPath = new TransferRequestType();
		tokenPath.setSrcPath(PortalConfiguration.getRegistryPrefix() + username
				+ WSO2Agent.separator + Constants.OAUTH2_TOKEN_FNAME);
		tokenPath.setSrcType(TransferType.WSO_2_REGISTRY);
		tokenPath.setDestPath("");
		tokenPath.setDestType(TransferType.HEADNODE);

		internalJobDesp.setTokenPath(tokenPath);

		/* set archive path */
		TransferRequestType archive = new TransferRequestType();
		archive.setSrcPath(PortalConfiguration.getRegistryPrefix() + username
				+ WSO2Agent.separator + jobInternalId + WSO2Agent.separator
				+ PortalConfiguration.getRegistryArchiveFolder()
				+ WSO2Agent.separator + archiveFileName);
		archive.setSrcType(TransferType.WSO_2_REGISTRY);
		archive.setDestPath("");
		archive.setDestType(TransferType.HEADNODE);

		internalJobDesp.setArchive(archive);

		/* set worksets */
		WorkSets worksets = new WorkSets();
		List<TransferRequestType> jobInputSet = worksets.getWorkset();

		for (WorksetMetaInfo worksetMeta : worksetInfoList) {
			TransferRequestType workset = new TransferRequestType();
			workset.setSrcPath(PortalConfiguration.getRegistryWorksetPrefix()
					+ username + WSO2Agent.separator + worksetMeta.getUUID()
					+ WSO2Agent.separator + worksetMeta.getFileName());
			workset.setSrcType(TransferType.WSO_2_REGISTRY);
			workset.setDestPath("");
			workset.setDestType(TransferType.HEADNODE);

			jobInputSet.add(workset);
		}

		internalJobDesp.setWorksets(worksets);

		return internalJobDesp;
	}
}
