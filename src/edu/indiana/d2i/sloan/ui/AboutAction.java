package edu.indiana.d2i.sloan.ui;

import com.opensymphony.xwork2.ActionSupport;

public class AboutAction extends ActionSupport {
	private static final long serialVersionUID = 1L;

	private final String webPageTitle = "About";

	public String execute() {
		return SUCCESS;
	}

	public String getWebPageTitle() {
		return webPageTitle;
	}
}
