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
# File:  ArchiveFileExt.java
# Description:  
#
# -----------------------------------------------------------------
# 
 */

package edu.indiana.d2i.wso2;

import java.util.ArrayList;
import java.util.List;

/**
 * Class represents supported job archive file types
 * 
 * @author Guangchen
 * 
 */
public class ArchiveFileExt {
	public static final String MSG = "Only support .zip, .tar file";
	public static final List<String> archiveFileExt = new ArrayList<String>() {

		private static final long serialVersionUID = 1L;

		{
			add(".zip");
			add(".tar");
		}
	};

	/**
	 * check whether the given file name has valid file extension
	 * 
	 * @param fileName
	 *            filename to be checked
	 * @return
	 */
	public static boolean isValidExt(String fileName) {
		int idx = fileName.lastIndexOf(".");

		return archiveFileExt.contains(fileName.substring(idx));
	}
}
