<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib prefix="s" uri="/struts-tags"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css" href="./css/staticpage-style.css" />
<title>Manage Workset</title>
<script language="javascript">
	String.prototype.endsWith = function(suffix) {
		return this.indexOf(suffix, this.length - suffix.length) !== -1;
	};

	function addEntry(tableId) {

		var table = document.getElementById(tableId);

		var rowCount = table.rows.length;
		var row = table.insertRow(rowCount);

		var cell0 = row.insertCell(0);
		var element0 = document.createElement("input");
		element0.type = "text";
		element0.name = "newWorksetTitle";
		element0.setAttribute("size", "20");
		cell0.appendChild(element0);

		var cell1 = row.insertCell(1);
		var element1 = document.createElement("input");
		element1.type = "text";
		element1.name = "newWorksetDesp";
		element1.setAttribute("size", "50");
		cell1.appendChild(element1);

		var cell2 = row.insertCell(2);
		var element2 = document.createElement("input");
		element2.type = "file";
		element2.name = "newWorkset";
		element2.setAttribute("onclick",
				"markCheckbox(this.parentNode.parentNode)");
		cell2.appendChild(element2);

		var cell3 = row.insertCell(3);
		var element3 = document.createElement("input");
		element3.type = "checkbox";
		element3.name = "newWorksetCheckbox";
		element3.setAttribute("onclick", "onUnCheck('" + tableId + "')");
		cell3.appendChild(element3);

	}

	function markCheckbox(selectedRow) {
		try {
			var chkbox = selectedRow.cells[3].childNodes[0];
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

			// first row is table header

			for ( var i = 1; i < rowCount; i++) {
				var row = table.rows[i];
				var chkbox = row.cells[3].childNodes[0];
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

	function validate() {
		var validArchiveSuffix = new Array(".zip", ".tar");

		var current_wset_table_empty = true;
		var upload_wset_table_empty = true;

		try {
			var table = document.getElementById('current_wset_table');

			if (table != null) {
				var update_file_list = '';

				var rowCount = table.rows.length;

				if (rowCount > 1)
					current_wset_table_empty = false;

				// first row is table header
				for ( var i = 1; i < rowCount; i++) {
					var row = table.rows[i];

					var textbox_title = row.cells[2].childNodes[0];
					if (null == textbox_title
							|| '' == textbox_title.value.trim()) {
						var errMsg = 'In management form, line # ' + i
								+ ': workset title cannot be empty';
						alert(errMsg);
						return false;
					}

					var textbox_desp = row.cells[3].childNodes[0];
					if (null == textbox_desp || '' == textbox_desp.value.trim()) {
						var errMsg = 'In management form, line # ' + i
								+ ': workset description cannot be empty';
						alert(errMsg);
						return false;
					}

					var filebox = row.cells[4].childNodes[0];
					if (null != filebox && '' != filebox.value) {

						var isvalid = false;

						for ( var j = 0; j < validArchiveSuffix.length; j++) {
							if (filebox.value.endsWith(validArchiveSuffix[j])) {
								isvalid = true;
								break;
							}
						}

						if (!isvalid) {
							var errMsg = 'In management form, line # ' + i
									+ ': workset can only be .zip, .tar file';
							alert(errMsg);
							return false;
						}

						update_file_list = update_file_list + ' ' + (i - 1);
					}
				}

				var updatelist = document.getElementById('update_list');
				updatelist.value = update_file_list;

				// alert(updatelist.value);
			}

		} catch (e) {
			alert(e);
		}

		onUnCheck('workset_table');

		try {
			var table = document.getElementById('workset_table');

			if (table != null) {

				var rowCount = table.rows.length;

				if (rowCount > 1)
					upload_wset_table_empty = false;

				// first row is table header
				for ( var i = 1; i < rowCount; i++) {
					var row = table.rows[i];

					var textbox_title = row.cells[0].childNodes[0];
					if (null == textbox_title
							|| '' == textbox_title.value.trim()) {
						var errMsg = 'In upload form, line # ' + i
								+ ': workset title cannot be empty';
						alert(errMsg);
						return false;
					}

					var textbox_desp = row.cells[1].childNodes[0];
					if (null == textbox_desp || '' == textbox_desp.value.trim()) {
						var errMsg = 'In upload form, line # ' + i
								+ ': workset description cannot be empty';
						alert(errMsg);
						return false;
					}

					var filebox = row.cells[2].childNodes[0];
					if (null == filebox || '' == filebox.value) {
						var errMsg = 'In upload form, line # ' + i
								+ ': workset archive cannot be empty';
						alert(errMsg);
						return false;
					}

					var isvalid = false;

					for ( var j = 0; j < validArchiveSuffix.length; j++) {
						if (filebox.value.endsWith(validArchiveSuffix[j])) {
							isvalid = true;
							break;
						}
					}

					if (!isvalid) {
						var errMsg = 'In upload form, line # ' + i
								+ ': workset can only be .zip, .tar file';
						alert(errMsg);
						return false;
					}
				}

			}

		} catch (e) {
			alert(e);
		}

		if (current_wset_table_empty && upload_wset_table_empty) {
			var errMsg = 'Nothing to update';
			alert(errMsg);
			return false;
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

	<div id="manageworksetpage">
		<s:form action="UpdateWorkSetAction" method="post"
			enctype="multipart/form-data" namespace="/" theme="simple"
			onsubmit="return validate()">

			<fieldset>
				<legend>Manage Existing Worksets</legend>

				<s:if
					test="currentWorksetInfoList != null && currentWorksetInfoList.size() > 0">
					<p>Current worksets</p>
					<table id="current_wset_table" border="2" cellspacing="0"
						cellpadding="2">
						<tr>
							<th>Workset ID (UUID)</th>
							<th>File Name</th>
							<th>Workset Title</th>
							<th>Workset Description</th>
							<th>Upload New Archive</th>
							<th>Deletion</th>
						</tr>
						<s:iterator value="currentWorksetInfoList" status="envStatus">
							<tr>
								<td><s:label
										key="currentWorksetInfoList[%{#envStatus.index}].UUID" /></td>
								<s:hidden
									name="currentWorksetInfoList[%{#envStatus.index}].UUID" />
								<td><s:label
										key="currentWorksetInfoList[%{#envStatus.index}].fileName" /></td>
								<s:hidden
									name="currentWorksetInfoList[%{#envStatus.index}].fileName" />
								<td><s:textfield
										name="currentWorksetInfoList[%{#envStatus.index}].worksetTitle"
										label="" size="20" /></td>
								<td><s:textfield
										name="currentWorksetInfoList[%{#envStatus.index}].worksetDesp"
										label="" size="50" /></td>
								<td><input type="file" name="currentWorksetNewFile" /></td>
								<td><s:checkbox name="currentWorksetCheckbox"
										fieldValue="%{#envStatus.index}" /></td>
							</tr>
						</s:iterator>
					</table>
					<input type="hidden" id="update_list" name="curWorkSetUpdateList" />
				</s:if>
				<s:else>
					<p>Currently no worksets have been uploaded, use following form
						to upload.</p>
				</s:else>
			</fieldset>
			<br />
			<br />
			<br />
			<fieldset>
				<legend>Upload new Worksets</legend>
				<table id="workset_table" border="2" cellspacing="0" cellpadding="2">
					<tr>
						<th>Workset Title</th>
						<th>Workset Description</th>
						<th>Upload Archive</th>
						<th>Select</th>
					</tr>
					<tr>
						<td><input type="text" name="newWorksetTitle" size="20" /></td>
						<td><input type="text" name="newWorksetDesp" size="50" /></td>
						<td><input type="file" name="newWorkset"
							onclick="markCheckbox(this.parentNode.parentNode)" /></td>
						<td><input type="checkbox" name="newWorksetCheckbox"
							onclick="onUnCheck('workset_table')" /></td>
					</tr>
				</table>
				<br />
				<button type="button" onclick="addEntry('workset_table')">Upload
					another workset</button>
			</fieldset>
			<br />
			<s:submit method="execute" key="label.update" align="center" />
		</s:form>
	</div>

</body>
</html>