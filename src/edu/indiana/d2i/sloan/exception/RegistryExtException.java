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
# File:  RegistryExtException.java
# Description:  
#
# -----------------------------------------------------------------
# 
 */

package edu.indiana.d2i.sloan.exception;

/**
 * Base class represents exception thrown by registry extension agent
 * 
 * @author Guangchen
 * 
 */
public class RegistryExtException extends Exception {

	private static final long serialVersionUID = 1L;

	public RegistryExtException(String message) {
		super(message);
	}
}