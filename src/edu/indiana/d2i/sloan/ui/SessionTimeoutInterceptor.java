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
# File:  SessionTimeoutInterceptor.java
# Description:  
#
# -----------------------------------------------------------------
# 
 */
package edu.indiana.d2i.sloan.ui;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import edu.indiana.d2i.sloan.Constants;

/**
 * Class which handles session timeout.
 * 
 * @author Guangchen
 * 
 */
public class SessionTimeoutInterceptor extends AbstractInterceptor {
	private static final long serialVersionUID = 1L;
	private static final Log logger = LogFactory
			.getLog(SessionTimeoutInterceptor.class);

	@Override
	public String intercept(final ActionInvocation invocation) throws Exception {
		Object action = invocation.getAction();
		if (!(action instanceof SessionTimeoutRequired)) {
			return invocation.invoke();
		}

		Map<String, Object> session = invocation.getInvocationContext()
				.getSession();
		if (session == null || session.isEmpty()) {
			HttpServletRequest httpRequest = ServletActionContext.getRequest();
			Cookie[] cookies = httpRequest.getCookies();
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().equals(Constants.SESSION_EXIST_BEFORE)
						&& (cookies[i].getValue().equals("true"))) {
					logger.info("Session expired.");
					session.put(Constants.SESSION_EXIST_BEFORE, new Boolean(
							true));
					return "sessionexpiredRedirect";
				}
			}

		}
		return invocation.invoke();
	}
}
