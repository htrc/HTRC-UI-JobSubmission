/*
#
# Copyright 2007 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: HTRC Sloan job submission web interface
# File:  FileSchemaUtil.java
# Description:  
#
# -----------------------------------------------------------------
# 
 */

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
