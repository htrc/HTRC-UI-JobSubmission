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
# File:  SigiriConstants.java
# Description:  
#
# -----------------------------------------------------------------
# 
 */
package edu.indiana.d2i.sigiri;

public class SigiriConstants {
	public static final String EXECUTABLE_PATH = "executable.path";
	public static final String PROPERTY_PATH = "property.path";
	public static final String PROPERTY_NAME = "property.name";
	public static final String SOFTWARE_FRAMEWORK = "software.framework";
	public static final String IMAGE_TYPE = "image.type";
	public static final String INSTANCE_NUMBER = "instance.number";
	public static final String OUTPUT_FILE = "output.file";
	public static final String TEMPORARY_FILE = "temporary.file";

	public static final String EXECUTABLE_TYPE_JAR = "Java";
	public static final String EXECUTABLE_TYPE_HADOOP = "Hadoop";

	// properties
	public static final String VM_EUCA2OOLS_HOME = "vmm.vm.euca2ools.credential";
	public static final String VM_AUTH_KEY = "vmm.vm.authkeyname";
	public static final String VM_USER_NAME = "vmm.vm.user";
	public static final String VM_USER_PWD = "vmm.vm.pwd";
	public static final String VM_WORK_DIR = "vmm.vm.workdir";
	public static final String VM_WARM_UP = "vmm.vm.connection.warmup";
	public static final String VM_JOB_MAXRUNTIME = "vmm.job.runtime.maximum";
	public static final String VM_POLICY = "vmm.vm.policy";
	public static final String VMM_LOCAL_WORKDIR = "vmm.local.workdir";
	public static final String VMM_HTTP_HOST = "vmm.http.server.host";
	public static final String VMM_HTTP_PORT = "vmm.http.server.port";

	// default job description file name used when user uses the form
	public static final String DEFAULT_JOB_DESP_NAME = "job-desp.xml";
	public static final String DEFAULT_JOB_DESP_CONTENT_TYPE = "text/xml";

	// token
	public static final String TOKEN_PATH = "token.path";
}
