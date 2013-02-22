package edu.indiana.d2i.sloan.schema.internal;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import edu.indiana.sloan.schema.XMLValidationEventHandler;

public class InternalSchemaUtil {

	@SuppressWarnings("unchecked")
	public static JobDescriptionType readConfigXML(InputStream is)
			throws JAXBException, IOException {

		JAXBContext jaxbContext = JAXBContext
				.newInstance("edu.indiana.d2i.sloan.schema.internal");
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		unmarshaller.setEventHandler(new XMLValidationEventHandler());

		JAXBElement<JobDescriptionType> configElement = (JAXBElement<JobDescriptionType>) unmarshaller
				.unmarshal(is);

		JobDescriptionType jobDescriptionType = configElement.getValue();
		return jobDescriptionType;
	}

	public static String toXMLString(JobDescriptionType jobDescriptionType)
			throws JAXBException {
		JAXBContext jaxbContext = JAXBContext
				.newInstance("edu.indiana.d2i.sloan.schema.internal");
		Marshaller marshaller = jaxbContext.createMarshaller();
		ObjectFactory factory = new ObjectFactory();
		JAXBElement<JobDescriptionType> createJobDescriptionType = factory
				.createJob(jobDescriptionType);
		OutputStream out = new ByteArrayOutputStream();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(createJobDescriptionType, out);
		return out.toString();
	}

	public static void main(String[] args) throws JAXBException,
			FileNotFoundException, IOException {
		String filePath = "D:\\workspace\\java_workspace\\HTRC-UI-JobSubmission\\resources\\job-internal-example.xml";

		JobDescriptionType userJobDesp = readConfigXML(new FileInputStream(
				filePath));

		String xmlStr = toXMLString(userJobDesp);
		System.out.println(xmlStr);
	}
}
