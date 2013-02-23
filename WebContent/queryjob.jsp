<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib prefix="s" uri="/struts-tags"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css" href="./css/staticpage-style.css" />
<title>Job Query</title>
</head>

<body>
	<s:include value="./jsp/header.jsp"></s:include>
	<div id="jobquery">

		<table border="2" cellspacing="0" cellpadding="2">
			<tr>
				<th>Jot Title</th>
				<td><s:property value="jobTitle" /></td>
			</tr>
			<tr>
				<th>Job Internal ID (UUID)</th>
				<td><s:property value="selectedInsId" /></td>
			</tr>
			<tr>
				<th>Job Status</th>
				<td><s:property value="sigiriJobStatus" /></td>
			</tr>
		</table>

		<br />
		<s:form action="JobQueryAction" method="post" namespace="/">
			<s:hidden name="selectedInsId" value="%{selectedInsId}" />
			<s:hidden name="jobTitle" value="%{jobTitle}" />
			<s:hidden name="sigiriJobId" value="%{sigiriJobId}" />
			<s:submit method="execute" key="label.update" align="center" />
		</s:form>

		<br />
		<p>
			<a id="searchlink" href="PrepareJobInfoAction">Back to job search
				page</a>
		</p>
	</div>
</body>
</html>