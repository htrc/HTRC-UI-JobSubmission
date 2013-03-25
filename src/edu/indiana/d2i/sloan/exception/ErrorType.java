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
# File:  ErrorType.java
# Description:  
#
# -----------------------------------------------------------------
# 
 */

package edu.indiana.d2i.sloan.exception;

/**
 * Enumeration of error types can be encountered in job submission process
 * 
 * @author Guangchen
 * 
 */
public enum ErrorType {
	NOERROR, UNKNOWN, PATH_NOT_EXIST, XML_PARSE_ERROR, JOB_DESP_SCHEMA_INVALID, JOB_ALREADY_EXIST, JOB_NAME_INVALID, SIGIRI_SERVICE_UNREACHABLE, REGISTRY_SERVICE_UNREACHABLE, OAUTH2_SERVICE_UNREACHABLE, NULL_EXECUTABLE, JOB_DESP_NOT_EXIST, JOB_EXE_NOT_EXIST, NULL_SIGIRI_JOB_ID;
}
