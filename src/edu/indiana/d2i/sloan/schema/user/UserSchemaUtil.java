package edu.indiana.d2i.sloan.schema.user;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import edu.indiana.sloan.schema.JobDespTyValidationEventHandler;

public class UserSchemaUtil {

	@SuppressWarnings("unchecked")
	public static JobDescriptionType readConfigXML(InputStream is)
			throws JAXBException, IOException {

		JAXBContext jaxbContext = JAXBContext
				.newInstance("edu.indiana.d2i.sloan.schema.user");
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
				.newInstance("edu.indiana.d2i.sloan.schema.user");
		Marshaller marshaller = jaxbContext.createMarshaller();
		ObjectFactory factory = new ObjectFactory();
		JAXBElement<JobDescriptionType> createJobDescriptionType = factory
				.createJob(jobDescriptionType);
		OutputStream out = new ByteArrayOutputStream();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(createJobDescriptionType, out);
		return out.toString();
	}
}
