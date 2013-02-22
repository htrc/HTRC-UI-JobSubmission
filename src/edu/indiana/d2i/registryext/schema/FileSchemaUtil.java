package edu.indiana.d2i.registryext.schema;

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

public class FileSchemaUtil {
	@SuppressWarnings("unchecked")
	public static Entry readConfigXML(InputStream is) throws JAXBException,
			IOException {

		JAXBContext jaxbContext = JAXBContext
				.newInstance("edu.indiana.d2i.registryext.schema");
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		unmarshaller.setEventHandler(new XMLValidationEventHandler());

		JAXBElement<Entry> configElement = (JAXBElement<Entry>) unmarshaller
				.unmarshal(is);

		Entry entry = configElement.getValue();
		return entry;
	}

	public static String toXMLString(Entry entry) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext
				.newInstance("edu.indiana.d2i.registryext.schema");
		Marshaller marshaller = jaxbContext.createMarshaller();
		ObjectFactory factory = new ObjectFactory();
		JAXBElement<Entry> createEntryType = factory.createEntry(entry);

		OutputStream out = new ByteArrayOutputStream();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(createEntryType, out);
		return out.toString();
	}

	public static void main(String[] args) throws FileNotFoundException,
			JAXBException, IOException {
		String filePath = "D:\\workspace\\java_workspace\\HTRC-UI-JobSubmission\\resources\\file-example.xml";

		Entry entry = readConfigXML(new FileInputStream(filePath));
		
		String xmlStr = toXMLString(entry);
		System.out.println(xmlStr);
	}
}
