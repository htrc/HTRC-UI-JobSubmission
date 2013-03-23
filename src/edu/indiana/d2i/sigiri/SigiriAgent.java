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
# File:  SigiriAgent.java
# Description:  
#
# -----------------------------------------------------------------
# 
 */
package edu.indiana.d2i.sigiri;

import java.rmi.RemoteException;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;

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

	public JobStatus submitJob(String internalJobDescriptionXMLStr)
			throws XMLStreamException, RemoteException {
		JobSubmissionParameters parameters = new JobSubmissionParameters();
		parameters.setHpcResource(HpcResourceName.Quarry);
		OMElement omelement = AXIOMUtil
				.stringToOM(internalJobDescriptionXMLStr);
		XMLContent xmlContent = new XMLContent();
		xmlContent.setExtraElement(omelement);
		parameters.setJobDescriptionXML(xmlContent);
		parameters.setCallbackURL("www.google.com");
		
		return stub.submitJob(parameters);
	}

	public JobStatus queryJobStatus(JobId jobId) throws RemoteException {
		return stub.checkStatus(jobId);
	}

}
