<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib prefix="s" uri="/struts-tags"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css" href="./css/staticpage-style.css" />
<title>Job Submission</title>
<script language="javascript">
	String.prototype.endsWith = function(suffix) {
		return this.indexOf(suffix, this.length - suffix.length) !== -1;
	};

	function validate() {
		try {
			var table = document.getElementById('job_title_table');

			if (table != null) {

				var row = table.rows[0];

				var title = table.rows[0].cells[1].childNodes[0];
				if (null == title || '' == title.value.trim()) {
					var errMsg = 'Job title cannot be empty';
					alert(errMsg);
					return false;
				}

			}

		} catch (e) {
			alert(e);
		}

		try {
			var table = document.getElementById('job_desp_table');

			if (table != null) {

				var row = table.rows[0];

				var desp = table.rows[0].cells[1].childNodes[0];
				if (null == desp || '' == desp.value) {
					var errMsg = 'Job description cannot be empty';
					alert(errMsg);
					return false;
				}

				if (!desp.value.endsWith('.xml')) {
					var errMsg = 'Job description must be an xml file';
					alert(errMsg);
					return false;
				}
			}

		} catch (e) {
			alert(e);
		}

		try {
			var table = document.getElementById('job_archive_table');

			if (table != null) {

				var row = table.rows[0];

				var archive = table.rows[0].cells[1].childNodes[0];
				if (null == archive || '' == archive.value) {
					var errMsg = 'Job archive cannot be empty';
					alert(errMsg);
					return false;
				}

				var validArchiveSuffix = new Array(".zip", ".tar");

				var isvalid = false;

				for (var i = 0; i < validArchiveSuffix.length; i++) {
					if (archive.value.endsWith(validArchiveSuffix[i])) {
						isvalid = true;
						break;
					}
				}
				
				if (!isvalid) {
					var errMsg = 'Job archive can only be .zip, .tar file';
					alert(errMsg);
					return false;
				}

			}

		} catch (e) {
			alert(e);
		}

		return true;
	}
</script>
</head>

<body>
	<s:include value="./jsp/header.jsp"></s:include>

	<s:if test="hasActionErrors()">
		<div id="jobsuberror" class="errors">
			<s:actionerror />
		</div>
	</s:if>

	<div id="submitjobpage">
		<s:form action="SubmitJobAction" method="post"
			enctype="multipart/form-data" namespace="/" theme="simple"
			onsubmit="return validate()">
			<fieldset>
				<legend>Job Title (*)</legend>
				<table id="job_title_table">
					<tr>
						<td>Job Title :</td>
						<td><s:textfield id="jobId" name="selectedJob"
								key="label.jobid" size="20" /></td>
					</tr>
				</table>
			</fieldset>
			<br />
			<br />
			<br />
			<fieldset>
				<legend>Job Description (*)</legend>
				<p>Upload job description file</p>
				<table id="job_desp_table">
					<tr>
						<td>Job Description :</td>
						<td><s:file id="jobDesp" name="jobDesp" key="label.jobdesp" /></td>
					</tr>
				</table>
			</fieldset>
			<br />
			<br />
			<br />
			<fieldset>
				<legend>Job Archive (*)</legend>
				<p>Upload job archive file</p>
				<table id="job_archive_table">
					<tr>
						<td>Job Archive :</td>
						<td><s:file id="jobArchive" name="jobArchive"
								key="label.jobarchive" /></td>
					</tr>
				</table>
			</fieldset>
			<br />
			<br />
			<br />

			<fieldset>
				<legend>Worksets</legend>

				<s:if test="worksetInfoList != null && worksetInfoList.size() > 0">
					<p>Select worksets for job</p>
					<table border="2" cellspacing="0" cellpadding="2">
						<tr>
							<th>Workset ID (UUID)</th>
							<th>File Name</th>
							<th>Workset Title</th>
							<th>Workset Description</th>
							<th>Select</th>
						</tr>
						<s:iterator value="worksetInfoList" status="envStatus">
							<tr>
								<td><s:label
										key="worksetInfoList[%{#envStatus.index}].UUID" /></td>
								<s:hidden
									name="worksetInfoList[%{#envStatus.index}].UUID" />
								<td><s:label
										key="worksetInfoList[%{#envStatus.index}].fileName" /></td>
								<s:hidden
									name="worksetInfoList[%{#envStatus.index}].fileName" />
								<td><s:label
										key="worksetInfoList[%{#envStatus.index}].worksetTitle" /></td>
								<s:hidden
									name="worksetInfoList[%{#envStatus.index}].worksetTitle" />
								<td><s:label
										key="worksetInfoList[%{#envStatus.index}].worksetDesp" /></td>
								<s:hidden
									name="worksetInfoList[%{#envStatus.index}].worksetDesp" />
								<td><s:checkbox name="worksetCheckbox"
										fieldValue="%{#envStatus.index}" /></td>
							</tr>
						</s:iterator>
					</table>
				</s:if>
				<s:else>
					<p>
						Currently no worksets have been uploaded, use <a
							id="worksetUploadLink" href="ManageWorkSetAction">workset
							management page</a> to upload.
					</p>
				</s:else>
			</fieldset>
			<br />
			<s:submit method="execute" key="label.upload" align="center" />
		</s:form>
	</div>
</body>
</html>