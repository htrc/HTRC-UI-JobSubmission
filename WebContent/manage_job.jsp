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
		element2.name = "newDependancy";
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
</script>
<s:head />
</head>

<body>
	<h4>
		Welcome,
		<s:property value="username" />
	</h4>
	<h2>
		Job
		<s:property value="selectedJob" />
	</h2>
	<s:form action="update_job" method="post" enctype="multipart/form-data"
		namespace="/" theme="simple" onsubmit="onUnCheck('dep_table')">
		<br />

		<fieldset>
			<legend>Job Description</legend>
			<p>Upload a new job description file</p>
			<table>
				<tr>
					<td>Job Description :</td>
					<td><s:file id="jobDesp" name="newJobDesp" key="label.jobdesp" /></td>
				</tr>
			</table>
			<p>Or, use the following form to update</p>
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
										name="jobDespForm.environment[%{#envStatus.index}].value"
										value="defaultImageType" /></td>
								<s:hidden
									name="jobDespForm.environment[%{#envStatus.index}].name"
									value="%{jobDespForm.environment[#envStatus.index].name}" />
							</s:if>
							<s:elseif
								test="jobDespForm.environment[#envStatus.index].name == frameworkTypeStr">
								<td>Framework Type :</td>
								<td><s:select list="frameworkTypes"
										name="jobDespForm.environment[%{#envStatus.index}].value"
										value="defaultFramework" /></td>
								<s:hidden
									name="jobDespForm.environment[%{#envStatus.index}].name"
									value="%{jobDespForm.environment[#envStatus.index].name}" />
							</s:elseif>
							<s:elseif
								test="jobDespForm.environment[#envStatus.index].name == instanceNumStr || jobDespForm.environment[#envStatus.index].name == outputFileStr">
								<td><s:label
										key="jobDespForm.environment[%{#envStatus.index}].name" /></td>
								<td><s:textfield
										name="jobDespForm.environment[%{#envStatus.index}].value" /></td>
								<s:hidden
									name="jobDespForm.environment[%{#envStatus.index}].name"
									value="%{jobDespForm.environment[#envStatus.index].name}" />
							</s:elseif>
							<!-- we don't display auto-generated fields -->
							<s:else>
								<s:hidden
									name="jobDespForm.environment[%{#envStatus.index}].name"
									value="%{jobDespForm.environment[#envStatus.index].name}" />
								<s:hidden
									name="jobDespForm.environment[%{#envStatus.index}].value"
									value="%{jobDespForm.environment[#envStatus.index].value}" />
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
			<p>Select a new executable from public repository</p>
			<s:optiontransferselect label="Select an executable"
				leftTitle="public repository" allowUpDownOnLeft="false"
				allowUpDownOnRight="false" allowSelectAll="false"
				allowAddAllToLeft="false" allowAddAllToRight="false"
				rightTitle="current job" name="pubExeList" list="pubExeRepo"
				headerKey="headerKey" headerValue="--- Please Select ---"
				doubleHeaderKey="doubleHeaderKey"
				doubleHeaderValue="--- Please Select ---"
				doubleName="selectedPubExe" doubleList="{}" />
			<p>Or, select one from your own repository</p>
			<s:optiontransferselect label="Select an executable"
				leftTitle="personal repository" allowUpDownOnLeft="false"
				allowUpDownOnRight="false" allowSelectAll="false"
				allowAddAllToLeft="false" allowAddAllToRight="false"
				rightTitle="current job" name="personExeList" list="userExeRepo"
				headerKey="headerKey" headerValue="--- Please Select ---"
				doubleHeaderKey="doubleHeaderKey"
				doubleHeaderValue="--- Please Select ---"
				doubleName="selectedUsrExe" doubleList="{}" />
			<p>Or, upload a new one</p>
			<table>
				<tr>
					<td>Executable :</td>
					<td><s:file id="executable" name="newExecutable"
							key="label.exe" /></td>
				</tr>
			</table>
		</fieldset>
		<br />
		<br />
		<br />

		<fieldset>
			<legend>Dependencies</legend>
			<s:if test="dependancy != null && dependancy.size() > 0">
				<p>Uncheck to remove existing dependancies</p>
				<s:label for="dep">Dependancies</s:label>
				<br />
				<s:checkboxlist id="dep" list="dependancy" name="keepDepnList"
					value="defaultDepnList" theme="htrc" />
			</s:if>

			<p>Select new dependencies from public repository</p>
			<s:optiontransferselect label="Select an executable"
				leftTitle="public repository" allowUpDownOnLeft="false"
				allowUpDownOnRight="false" allowSelectAll="false"
				rightTitle="current job" name="pubDepList" list="pubLibRepo"
				headerKey="headerKey" headerValue="--- Please Select ---"
				doubleHeaderKey="doubleHeaderKey"
				doubleHeaderValue="--- Please Select ---"
				doubleName="selectedPubLib" doubleList="{}" />
			<p>And/Or, select them from your own repository</p>
			<s:optiontransferselect label="Select an executable"
				leftTitle="personal repository" allowUpDownOnLeft="false"
				allowUpDownOnRight="false" allowSelectAll="false"
				rightTitle="current job" name="personDepList" list="userLibRepo"
				headerKey="headerKey" headerValue="--- Please Select ---"
				doubleHeaderKey="doubleHeaderKey"
				doubleHeaderValue="--- Please Select ---"
				doubleName="selectedUsrLib" doubleList="{}" />
			<p>And/Or, upload more dependancies</p>
			<table id="dep_table">
				<tr>
					<td><input type="checkbox" name="depCheckbox"
						onclick="onUnCheck('dep_table')" /></td>
					<td><input type="file" name="newDependancy"
						onclick="markCheckbox(this.parentNode.parentNode)" /></td>
				</tr>
			</table>
			<br /> <br />
			<button type="button" onclick="addEntry('dep_table')">Upload
				another file</button>
		</fieldset>

		<!--
		<s:if test="%{dependancy == null}">No dependacy uploaded</s:if>
		<s:elseif test="%{dependancy.size() == 0}">No dependacy uploaded</s:elseif>
		<s:else>
			<s:label for="dep">Dependancies (Uncheck to remove)</s:label>
			<br />
			<s:checkboxlist id="dep" list="dependancy" name="keepDepnList"
				value="defaultDepnList" theme="htrc" />
		</s:else>
		<br />
		<br />
		<fieldset>
			<legend>Update Job Description and Executable (Optional)</legend>
			<table>
				<tr>
					<td>Job Description :</td>
					<td><s:file name="newJobDesp" key="label.jobdesp" /></td>
				</tr>
				<tr>
					<td>Executable :</td>
					<td><s:file name="newExecutable" key="label.exe" /></td>
				</tr>
			</table>
		</fieldset>
		<br />
		<br />

		<fieldset>
			<legend>Upload More Dependencies (Optional)</legend>
			<table id="dep_table">
				<tr>
					<td><input type="checkbox" name="depCheckbox"
						onclick="onUnCheck('dep_table')" /></td>
					<td><input type="file" name="newDependancy"
						onclick="markCheckbox(this.parentNode.parentNode)" /></td>
				</tr>
			</table>
			<br />
			<button type="button" onclick="addEntry('dep_table')">Upload
				another file</button>
		</fieldset>
		-->

		<br />
		<s:hidden name="username" value="%{username}" />
		<s:hidden name="selectedJob" value="%{selectedJob}" />

		<s:iterator value="dependancy" status="depnStatus">
			<s:hidden name="dependancy[%{#depnStatus.index}]"
				value="%{dependancy[#depnStatus.index]}" />
		</s:iterator>

		<s:hidden name="jobDespFileName" value="%{jobDespFileName}" />
		<s:hidden name="jobDespContentType" value="%{jobDespContentType}" />
		<s:submit method="execute" key="label.update" align="center" />
	</s:form>
</body>
</html>