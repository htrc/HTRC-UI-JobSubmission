package edu.indiana.sloan.schema;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;

import org.apache.log4j.Logger;

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
