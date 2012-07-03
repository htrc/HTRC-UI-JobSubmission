package edu.indiana.d2i.exception;

@SuppressWarnings("serial")
public class InvalidJobName extends Exception {
	public InvalidJobName(String message) {
		super(message);
	}
}
