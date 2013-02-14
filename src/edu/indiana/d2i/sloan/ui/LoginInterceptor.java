package edu.indiana.d2i.sloan.ui;

import java.util.Map;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import edu.indiana.d2i.sloan.Constants;

public class LoginInterceptor extends AbstractInterceptor {
	private static final long serialVersionUID = 1L;

	@Override
	public String intercept(final ActionInvocation invocation) throws Exception {
		Map<String, Object> session = ActionContext.getContext().getSession();

		// actions do not require login
		Object action = invocation.getAction();
		if (!(action instanceof LoginRequired)) {
			if (action instanceof LoginAction) {
				if (!session.containsKey(Constants.SESSION_LAST_ACTION))
					session.put(Constants.SESSION_LAST_ACTION, "HomeAction");
			}
			return invocation.invoke();
		}

		session.put(Constants.SESSION_LAST_ACTION, invocation
				.getInvocationContext().getName());

		// other login required actions
		String userName = (String) session.get(Constants.SESSION_USERNAME);
		if (userName != null) { // user has logged in before
			return invocation.invoke();
		} else { // user hasn't logged in
			return "loginRedirect";
		}
	}
}
