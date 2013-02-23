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
# File:  Constants.java
# Description:  
#
# -----------------------------------------------------------------
# 
 */
package edu.indiana.d2i.sloan;

public class Constants {
	// OAuth2 related properties
	public static final String PN_OAUTH2_AUTH_EPR = "oauth2.auth.endpoint";
	public static final String PN_OAUTH2_TOKEN_EPR = "oauth2.token.endpoint";
	public static final String PN_OAUTH2_USERINFO_EPR = "oauth2.userinfo.endpoint";
	public static final String PN_OAUTH2_CLIENT_ID = "oauth2.client.id";
	public static final String PN_OAUTH2_CLIENT_PASS = "oauth2.client.secrete";
	public static final String PN_OAUTH2_GRANT_TYPE = "oauth2.grant";
	public static final String PN_OAUTH2_SCOPE = "oauth2.scope";

	public static String OAUTH2_REDIRECT_URL = "oauth2.redirect";
	public static String OAUTH2_ACCESS_TOKEN = "access_token";
	public static String OAUTH2_REFRESH_TOKEN = "refresh_token";
	public static String OAUTH2_EXPIRE = "expires_in";

	// WSO2 Registry related properties -- configuration
	public static final String PN_WSO2_SERVER_EPR = "wso2.registry.server.epr";
	public static final String PN_WSO2_SELF_SIGN = "wso2.registry.self.sign";

	// WSO2 Registry related properties -- internal directory structure
	public static final String PN_WSO2_WORKSET_PREFIX = "wso2.registry.workset.prefix";
	public static final String PN_WSO2_JOB_PREFIX = "wso2.registry.job.prefix";
	public static final String PN_WSO2_REPO_JOB_ARCHIVE = "wso2.registry.repo.job.archive";

	public static final String OAUTH2_TOKEN_FNAME = "token.tmp";
	public static final String WSO2_JOB_PROP_FNAME = "job.properties";
	public static final String WSO2_JOB_DESP_FNAME = "job-description.xml";
	public static final String WSO2_WORKSET_META_FNAME = "metadata.properties";
	public static final String WSO2_WORKSET_METAFILE_SETNAME = "workset.name";
	public static final String WSO2_WORKSET_METAFILE_SETDESP = "workset.description";

	// Sigiri related properties
	public static final String PN_SIGIRI_SERVER_EPR = "sigiri.server.epr";

	// Session related properties
	public static String SESSION_USERNAME = "session.username";
	public static String SESSION_TOKEN = "session.token";
	public static String SESSION_TOKEN_EXPIRE_SEC = "session.token.expire";
	public static String SESSION_REFRESH_TOKEN = "session.refresh.token";
	public static String SESSION_LAST_ACTION = "session.lastaction";
	public static String SESSION_EXIST_BEFORE = "session.timeout";

	// Mis
	public static String PROPERTY_FNAME = "sloan-job-submission.properties";
}
