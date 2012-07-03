package edu.indiana.d2i.sigiri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import edu.indiana.d2i.AgentsRepoSingleton;
import edu.indiana.extreme.sigiri.SigiriServiceStub;
import edu.indiana.extreme.sigiri.SigiriServiceStub.HpcResourceName;
import edu.indiana.extreme.sigiri.SigiriServiceStub.JobId;
import edu.indiana.extreme.sigiri.SigiriServiceStub.JobStatus;
import edu.indiana.extreme.sigiri.SigiriServiceStub.JobSubmissionParameters;
import edu.indiana.extreme.sigiri.SigiriServiceStub.XMLContent;

public class SigiriAgent {
	private final SigiriServiceStub stub;

	public SigiriAgent(String epr) throws AxisFault {
		stub = new SigiriServiceStub(epr);
	}

	public JobStatus submitJob(String jobDescriptionXMLStr)
			throws XMLStreamException, RemoteException {
		JobSubmissionParameters parameters = new JobSubmissionParameters();
		parameters.setHpcResource(HpcResourceName.Quarry);
		OMElement omelement = AXIOMUtil.stringToOM(jobDescriptionXMLStr);
		XMLContent xmlContent = new XMLContent();
		xmlContent.setExtraElement(omelement);
		parameters.setJobDescriptionXML(xmlContent);
		parameters.setCallbackURL("www.google.com");

		return stub.submitJob(parameters);
	}

	public JobStatus queryJobStatus(JobId jobId) throws RemoteException {
		return stub.checkStatus(jobId);
	}

	@SuppressWarnings("unchecked")
	public static JobDescriptionType readConfigXML(InputStream is)
			throws JAXBException, IOException {

		JAXBContext jaxbContext = JAXBContext
				.newInstance("edu.indiana.d2i.sigiri");
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		unmarshaller.setEventHandler(new JobDespTyValidationEventHandler());

		JAXBElement<JobDescriptionType> configElement = (JAXBElement<JobDescriptionType>) unmarshaller
				.unmarshal(is);

		JobDescriptionType jobDescriptionType = configElement.getValue();
		return jobDescriptionType;
	}

	public static String toXMLString(JobDescriptionType jobDescriptionType)
			throws JAXBException {
		JAXBContext jaxbContext = JAXBContext
				.newInstance("edu.indiana.d2i.sigiri");
		Marshaller marshaller = jaxbContext.createMarshaller();
		ObjectFactory factory = new ObjectFactory();
		JAXBElement<JobDescriptionType> createJobDescriptionType = factory
				.createJob(jobDescriptionType);
		OutputStream out = new ByteArrayOutputStream();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(createJobDescriptionType, out);
		// marshaller.marshal(createJobDescriptionType, System.out);
		return out.toString();
	}
	
	/**
	 * just for test purpose
	 * @return
	 * @throws JAXBException 
	 */
	private static String genFakeDespXMLStr() throws JAXBException {
		JobDescriptionType jobDesp = new JobDescriptionType();
		// jobDesp.setExecutable("java -jar htrc-app-0.2.jar dataapitest.properties token.tmp");
		jobDesp.setExecutable("dos2unix hello.sh;./hello.sh");
		jobDesp.setJobType(JobTypeEnumeration.SINGLE);
		// jobDesp.setLocalUserId("yim");
		
		List<NameValuePairType> pairList = jobDesp.getEnvironment();

		NameValuePairType imageTypePair = new NameValuePairType();
		// set executable path
		imageTypePair.setName(SigiriConstants.IMAGE_TYPE);
		imageTypePair.setValue("m1.small");
		pairList.add(imageTypePair);
		
		NameValuePairType imageNumPair = new NameValuePairType();
		// set executable path
		imageNumPair.setName(SigiriConstants.INSTANCE_NUMBER);
		imageNumPair.setValue("1");
		pairList.add(imageNumPair);
	
		NameValuePairType softWareFk = new NameValuePairType();
		// set executable path
		softWareFk.setName(SigiriConstants.SOFTWARE_FRAMEWORK);
		// softWareFk.setValue("JAVA");
		softWareFk.setValue("Script");
		pairList.add(softWareFk);
		
		NameValuePairType outFilePath = new NameValuePairType();
		// set executable path
		outFilePath.setName(SigiriConstants.OUTPUT_FILE);
		outFilePath.setValue("result.txt");
		pairList.add(outFilePath);	
		
//		NameValuePairType exePahtPair = new NameValuePairType();
//		// set executable path
//		exePahtPair.setName(SigiriConstants.EXECUTABLE_PATH);
//		exePahtPair.setValue("/htrc/repo/yim/htrcjob/executable");
//		pairList.add(exePahtPair);
//
//		// set dependancy path
//		NameValuePairType depPahtPair = new NameValuePairType();
//		depPahtPair.setName(SigiriConstants.PROPERTY_PATH);
//		depPahtPair.setValue("/htrc/repo/yim/htrcjob/dependancy");
//		pairList.add(depPahtPair);
		
		return SigiriAgent.toXMLString(jobDesp);
	}
	
	public static void main(String[] args) {
		try {
			System.out.println(genFakeDespXMLStr());
			AgentsRepoSingleton agentsRepo = AgentsRepoSingleton
					.getInstance();
			
			SigiriAgent sigiriAgent = agentsRepo.getSigiriAgent();
			JobId jobId = new JobId();
			// jobId.setJobId("None exist job id");
			// jobId.setJobId(null);
			jobId.setJobId("");
			JobStatus jobStatus = sigiriAgent.queryJobStatus(jobId);
			System.out.println("Job Id = " + jobStatus.getStatus());
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RegistryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
