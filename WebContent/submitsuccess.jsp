<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib prefix="s" uri="/struts-tags"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css" href="./css/staticpage-style.css" />
<title>Job Submission Success</title>
</head>

<body>
	<s:include value="./jsp/header.jsp"></s:include>
	<div id="submitsuccesspage">
		<h3>
			Job "
			<s:property value="selectedJob" />
			" has been submitted to Sloan Cloud successfully
		</h3>
		<br />

		<h4>
			<a id="searchlink" href="PrepareJobInfoAction">Back to job search
				page</a>
		</h4>
	</div>
</body>
</html>