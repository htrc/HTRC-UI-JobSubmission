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
# File:  UserSchemaUtil.java
# Description:  
#
# -----------------------------------------------------------------
# 
 */

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

import edu.indiana.sloan.schema.XMLValidationEventHandler;

/**
 * Utility class for user job description XML file, provisions methods to do
 * transformation between XML file and corresponding java class, and validation.
 * 
 * @author Guangchen
 * 
 */
public class UserSchemaUtil {

	/**
	 * read user job description XML file from an input stream and transform it
	 * to corresponding java class
	 * 
	 * @param is
	 *            input stream where XML file can be read
	 * @return java class represents job description XML file
	 * @throws JAXBException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static JobDescriptionType readConfigXML(InputStream is)
			throws JAXBException, IOException {

		JAXBContext jaxbContext = JAXBContext
				.newInstance("edu.indiana.d2i.sloan.schema.user");
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		unmarshaller.setEventHandler(new XMLValidationEventHandler());

		JAXBElement<JobDescriptionType> configElement = (JAXBElement<JobDescriptionType>) unmarshaller
				.unmarshal(is);

		JobDescriptionType jobDescriptionType = configElement.getValue();
		return jobDescriptionType;
	}

	/**
	 * transform user job description java class to string form of corresponding
	 * XML file
	 * 
	 * @param jobDescriptionType
	 *            user job description
	 * @return string form of corresponding XML file
	 * @throws JAXBException
	 */
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
