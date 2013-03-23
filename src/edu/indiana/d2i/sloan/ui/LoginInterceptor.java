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
# File:  LoginInterceptor.java
# Description:  
#
# -----------------------------------------------------------------
# 
 */
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
