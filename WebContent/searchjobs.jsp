<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sx" uri="/struts-dojo-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css" href="./css/staticpage-style.css" />
<title>Job Search</title>
<sx:head />
</head>

<body>
	<s:include value="header.jsp"></s:include>
	<div id="searchjobpage">
		<h3>
			<a href="JobSubmitFormAction"><span>Create a new job</span></a>
		</h3>

		<s:form action="JobSearchAction" method="post" namespace="/"
			theme="simple">
			<s:if test="jobTitles != null && jobTitles.size() > 0">
				<sx:autocompleter label="Search a job" list="jobTitles"
					name="selectedJobTitle" />
				<s:submit method="execute" key="label.search" align="center" />
			</s:if>
			<s:else>
				<h3>No jobs posted, you can use above link to create a new job</h3>
			</s:else>

		</s:form>
	</div>
</body>
</html>