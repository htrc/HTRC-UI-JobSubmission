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
# File:  XMLValidationEventHandler.java
# Description:  
#
# -----------------------------------------------------------------
# 
 */

package edu.indiana.sloan.schema;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;

import org.apache.log4j.Logger;

/**
 * XML validation event handler
 * 
 * @author Guangchen
 * 
 */
public class XMLValidationEventHandler implements ValidationEventHandler {
	private static final Logger logger = Logger
			.getLogger(XMLValidationEventHandler.class);

	@Override
	public boolean handleEvent(ValidationEvent ve) {

		if (ve.getSeverity() == ValidationEvent.FATAL_ERROR
				|| ve.getSeverity() == ValidationEvent.ERROR) {
			ValidationEventLocator locator = ve.getLocator();
			// Print message from validation event
			logger.error("Invalid workload config file: " + locator.getURL());
			logger.error("Error: " + ve.getMessage());
			logger.error("Error at column " + locator.getColumnNumber()
					+ ", line " + locator.getLineNumber());
			return false;
		}
		return true;
	}
}
