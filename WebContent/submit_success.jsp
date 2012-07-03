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
		Job
		<s:property value="selectedJob" />
		has been uploaded to registry successfully
	</h2>

	<h3>
		<s:url value="javascript: submitform('listjob_form')"
			var="listJobPage" />
		<s:a href="%{listJobPage}">Back to job search page</s:a>
	</h3>

	<s:form id="listjob_form" action="job_search" method="post" namespace="/">
		<s:hidden name="username" value="%{username}" />
	</s:form>
</body>
</html>