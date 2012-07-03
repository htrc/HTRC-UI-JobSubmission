<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Job Management Service</title>
<script language="javascript">
	function submitform(form_id) {
		document.forms[form_id].submit();
	}
	function validateForm() {
		var job_list = document.getElementById('my_job_list');
		var selected_value = job_list.options[job_list.selectedIndex].value;
		
		if (selected_value == "-1") {
			alert("Please select a job!");
			return false;
		}
	}
</script>
</head>

<body>
	<table border="0" width="100%">
		<tr>
			<th align="left">
				<h4>
					Welcome,
					<s:property value="username" />
				</h4>
			</th>
			<th align="right">
				<h4>
					<s:url value="login.jsp" var="logout" />
					<s:a href="%{logout}">log out</s:a>
				</h4>
			</th>
		</tr>
	</table>

	<h3>
		<s:url value="javascript: submitform('upload_form')"
			var="uploadJobPage" />
		<s:a href="%{uploadJobPage}">Want to create a new job?</s:a>
	</h3>

	<s:form action="select_job" method="post" namespace="/"
		onsubmit="return validateForm()">
		<s:select id="my_job_list" label="Job List" headerKey="-1"
			headerValue="Select a Job" list="jobs" name="selectedJob" />

		<s:radio label="Operation" name="selectedOp" list="ops"
			value="defaultOpValue" />
		<br />
		<s:hidden name="username" value="%{username}" />
		<s:submit method="execute" key="label.submit" align="center" />
	</s:form>

	<s:form id="upload_form" action="job_sub_interface" method="post"
		namespace="/">
		<s:hidden name="username" value="%{username}" />
	</s:form>
</body>
</html>