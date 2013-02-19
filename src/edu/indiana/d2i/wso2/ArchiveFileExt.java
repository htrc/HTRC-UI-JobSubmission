package edu.indiana.d2i.wso2;

import java.util.ArrayList;
import java.util.List;

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
	 * @return
	 */
	public static boolean isValidExt(String fileName) {
		int idx = fileName.lastIndexOf(".");

		return archiveFileExt.contains(fileName.substring(idx));
	}
}
