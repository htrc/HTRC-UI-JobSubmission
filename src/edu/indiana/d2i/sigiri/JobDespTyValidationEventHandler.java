package edu.indiana.d2i.sigiri;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;

public class JobDespTyValidationEventHandler implements
		ValidationEventHandler {

	@Override
	public boolean handleEvent(ValidationEvent ve) {

		if (ve.getSeverity() == ValidationEvent.FATAL_ERROR
				|| ve.getSeverity() == ValidationEvent.ERROR) {
			ValidationEventLocator locator = ve.getLocator();
			// Print message from validation event
			System.err.println("Invalid workload config file: " + locator.getURL());
			System.err.println("Error: " + ve.getMessage());
			// Output line and column number
			System.err.println("Error at column " + locator.getColumnNumber()
					+ ", line " + locator.getLineNumber());
			return false;
		}
		return true;
	}
}
