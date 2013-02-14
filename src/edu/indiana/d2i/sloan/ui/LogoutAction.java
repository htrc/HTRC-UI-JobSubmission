package edu.indiana.d2i.sloan.ui;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionSupport;

import edu.indiana.d2i.sloan.Constants;

public class LogoutAction extends ActionSupport implements SessionAware,
		LoginRequired, ServletResponseAware {
	private static final long serialVersionUID = 1L;
	private static final Log logger = LogFactory.getLog(LogoutAction.class);

	private Map<String, Object> session;
	private HttpServletResponse httpResponse = null;

	public String execute() throws Exception {
		String user = (String) session.get(Constants.SESSION_USERNAME);
		logger.info(user + " log out.");
		session.clear();
		httpResponse.addCookie(new Cookie(Constants.SESSION_EXIST_BEFORE,
				"false"));
		return SUCCESS;
	}

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}

	@Override
	public void setServletResponse(HttpServletResponse response) {
		this.httpResponse = response;
	}
}
