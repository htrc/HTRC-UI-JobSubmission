<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sx" uri="/struts-dojo-tags"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css" href="./css/staticpage-style.css" />
<title>Job Search</title>
<sx:head />
</head>
<body>
	<s:include value="./jsp/header.jsp"></s:include>
	<div id="searchjobpage">
		<h3>
			<a href="JobSubmitFormAction"><span>Create a new job</span></a>
		</h3>

		<s:if test="hasActionErrors()">
			<div id="jobsuberror" class="errors">
				<s:actionerror />
			</div>
		</s:if>

		<s:form action="JobSearchAction" method="post" namespace="/"
			theme="simple">
			<s:if test="jobTitles != null && jobTitles.size() > 0">
				<h4>Use * to search all jobs</h4>
				<sx:autocompleter label="Search a job" list="jobTitles"
					name="selectedJobTitle" />
				<s:submit method="execute" key="label.search" align="center" />
			</s:if>
			<s:else>
				<h3>Currently you have no jobs submitted, you can use above
					link to create a new job</h3>
			</s:else>

		</s:form>
	</div>
</body>
</html>