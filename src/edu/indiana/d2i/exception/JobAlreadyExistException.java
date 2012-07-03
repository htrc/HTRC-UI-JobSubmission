package edu.indiana.d2i.exception;

@SuppressWarnings("serial")
public class JobAlreadyExistException extends Exception {
	public JobAlreadyExistException(String message) {
		super(message);
	}
}
