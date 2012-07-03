<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sx" uri="/struts-dojo-tags"%>
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
<sx:head />
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

	<s:form action="submit_search" method="post" namespace="/"
		theme="simple">
		<s:if test="jobTitles != null && jobTitles.size() > 0">
			<sx:autocompleter label="Search a job" list="jobTitles"
				name="selectedJobTitle" />
			<s:hidden name="username" value="%{username}" />
			<s:hidden name="errMsg" value="%{errMsg}" />
			<s:submit method="execute" key="label.submit" align="center" />
		</s:if>
		<s:else>
			<h3>No jobs posted, please use above link to create new job</h3>
		</s:else>

	</s:form>

	<s:form id="upload_form" action="job_sub_interface" method="post"
		namespace="/">
		<s:hidden name="username" value="%{username}" />
	</s:form>
</body>
</html>