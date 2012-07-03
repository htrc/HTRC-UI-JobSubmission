<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Job Management Service</title>
<script language="javascript">
	function addEntry(tableId) {

		var table = document.getElementById(tableId);

		var rowCount = table.rows.length;
		var row = table.insertRow(rowCount);

		var cell1 = row.insertCell(0);
		var element1 = document.createElement("input");
		element1.type = "checkbox";
		element1.name = "depCheckbox";
		element1.setAttribute("onclick", "onUnCheck('" + tableId + "')");

		cell1.appendChild(element1);

		var cell2 = row.insertCell(1);
		var element2 = document.createElement("input");
		element2.type = "file";
		element2.name = "dependancy";
		element2.setAttribute("onclick",
				"markCheckbox(this.parentNode.parentNode)");
		cell2.appendChild(element2);
	}

	function markCheckbox(selectedRow) {
		try {
			var chkbox = selectedRow.cells[0].childNodes[0];
			if (chkbox != null)
				chkbox.checked = true;
		} catch (e) {
			alert(e);
		}

	}

	function onUnCheck(tableId) {
		try {
			var table = document.getElementById(tableId);
			var rowCount = table.rows.length;

			for ( var i = 0; i < rowCount; i++) {
				var row = table.rows[i];
				var chkbox = row.cells[0].childNodes[0];
				if (null != chkbox && false == chkbox.checked) {
					table.deleteRow(i);
					rowCount--;
					i--;
				}
			}
		} catch (e) {
			alert(e);
		}
	}

	function validateForm(tableId) {
		var jobId = document.getElementById("jobId");
		var jobDesp = document.getElementById("jobDesp");
		var executable = document.getElementById("executable");

		if (jobId.value == null || jobId.value == "") {
			alert("Please specify a job Id");
			return false;
		}

		/*
		if (jobDesp.value == null || jobDesp.value == "") {
			alert("Please upload a job description file");
			return false;
		}
		 */

		/*
		if (executable.value == null || executable.value == "") {
			alert("Please upload an executable");
			return false;
		}
		 */
		onUnCheck(tableId);
	}
</script>
<s:head />
</head>

<body>

	<h4>
		Welcome,
		<s:property value="username" />
	</h4>

	<s:form action="job_submit" method="post" enctype="multipart/form-data"
		namespace="/" theme="simple"
		onsubmit="return validateForm('dep_table')">
		<fieldset>
			<legend>Job Title</legend>
			<table>
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
			<legend>Job Description</legend>
			<p>Upload the job description file directly</p>
			<table>
				<tr>
					<td>Job Description :</td>
					<td><s:file id="jobDesp" name="jobDesp" key="label.jobdesp" /></td>
				</tr>
			</table>
			<p>Or, use the following form to compose one</p>
			<table>
				<tr>
					<td>Command Line :</td>
					<td><s:textfield name="jobDespForm.executable" /></td>
				</tr>
				<tr>
					<td>Job Type :</td>
					<td><s:select list="jobTypes" name="jobDespForm.jobType" /></td>
				</tr>
				<s:if
					test="jobDespForm.environment != null && jobDespForm.environment.size() > 0">
					<s:iterator value="jobDespForm.environment" status="envStatus">
						<tr>
							<s:if
								test="jobDespForm.environment[#envStatus.index].name == imageTypeStr">
								<td>Image Type :</td>
								<td><s:select list="imageTypes"
										name="jobDespForm.environment[%{#envStatus.index}].value" /></td>
								<s:hidden
									name="jobDespForm.environment[%{#envStatus.index}].name"
									value="%{jobDespForm.environment[#envStatus.index].name}" />
							</s:if>
							<s:elseif
								test="jobDespForm.environment[#envStatus.index].name == frameworkTypeStr">
								<td>Framework Type :</td>
								<td><s:select list="frameworkTypes"
										name="jobDespForm.environment[%{#envStatus.index}].value" /></td>
								<s:hidden
									name="jobDespForm.environment[%{#envStatus.index}].name"
									value="%{jobDespForm.environment[#envStatus.index].name}" />
							</s:elseif>
							<s:else>
								<td><s:label
										key="jobDespForm.environment[%{#envStatus.index}].name" /></td>
								<td><s:textfield
										name="jobDespForm.environment[%{#envStatus.index}].value" /></td>
								<s:hidden
									name="jobDespForm.environment[%{#envStatus.index}].name"
									value="%{jobDespForm.environment[#envStatus.index].name}" />
							</s:else>
						</tr>
					</s:iterator>
				</s:if>
			</table>

		</fieldset>
		<br />
		<br />
		<br />

		<fieldset>
			<legend>Executable</legend>
			<p>Select an executable from public repository</p>
			<s:optiontransferselect leftTitle="public repository"
				allowUpDownOnLeft="false" allowUpDownOnRight="false"
				allowSelectAll="false" allowAddAllToLeft="false"
				allowAddAllToRight="false" rightTitle="current job"
				name="pubExeList" list="pubExeRepo" headerKey="headerKey"
				headerValue="--- Please Select ---"
				doubleHeaderKey="doubleHeaderKey"
				doubleHeaderValue="--- Please Select ---"
				doubleName="selectedPubExe" doubleList="{}" />
			<p>Or, select one from your own repository</p>
			<s:optiontransferselect leftTitle="personal repository"
				allowUpDownOnLeft="false" allowUpDownOnRight="false"
				allowSelectAll="false" allowAddAllToLeft="false"
				allowAddAllToRight="false" rightTitle="current job"
				name="personExeList" list="userExeRepo" headerKey="headerKey"
				headerValue="--- Please Select ---" doubleHeaderKey="-1"
				doubleHeaderValue="--- Please Select ---"
				doubleName="selectedUsrExe" doubleList="{}" />
			<p>Or, upload one</p>
			<table>
				<tr>
					<td>Executable :</td>
					<td><s:file id="executable" name="executable" key="label.exe" /></td>
				</tr>
			</table>
		</fieldset>
		<br />
		<br />
		<br />

		<fieldset>
			<legend>Dependencies (Optional)</legend>
			<p>Select dependencies from public repository</p>
			<s:optiontransferselect leftTitle="public repository"
				allowUpDownOnLeft="false" allowUpDownOnRight="false"
				allowSelectAll="false" rightTitle="current job" name="pubDepList"
				list="pubLibRepo" headerKey="headerKey"
				headerValue="--- Please Select ---"
				doubleHeaderKey="doubleHeaderKey"
				doubleHeaderValue="--- Please Select ---"
				doubleName="selectedPubLib" doubleList="{}" multiple="true" />

			<p>And/Or, select them from your own repository</p>
			<s:optiontransferselect leftTitle="personal repository"
				allowUpDownOnLeft="false" allowUpDownOnRight="false"
				allowSelectAll="false" rightTitle="current job" name="personDepList"
				list="userLibRepo" headerKey="headerKey"
				headerValue="--- Please Select ---"
				doubleHeaderKey="doubleHeaderKey"
				doubleHeaderValue="--- Please Select ---"
				doubleName="selectedUsrLib" doubleList="{}" />
			<p>And/Or, upload them</p>
			<table id="dep_table">
				<tr>
					<td><input type="checkbox" name="depCheckbox"
						onclick="onUnCheck('dep_table')" /></td>
					<td><input type="file" name="dependancy"
						onclick="markCheckbox(this.parentNode.parentNode)" /></td>
				</tr>
			</table>
			<br /> <br />
			<button type="button" onclick="addEntry('dep_table')">Upload
				another file</button>
		</fieldset>
		<br />

		<s:hidden name="username" value="%{username}" />
		<s:submit method="execute" key="label.upload" align="center" />
	</s:form>

</body>
</html>