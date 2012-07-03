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
</script>
</head>

<body>
	<h2>
		<s:property value="errMsg" />
	</h2>

	<h3>
		<s:url value="javascript: submitform('job_form')"
			var="updateJobPage" />
		<s:a href="%{updateJobPage}">Back to job update page</s:a>
	</h3>

	<s:form id="job_form" action="job_info" method="post" namespace="/">
		<s:hidden name="username" value="%{username}" />
		<s:hidden name="selectedJob" value="%{selectedJob}" />
	</s:form>
</body>
</html>