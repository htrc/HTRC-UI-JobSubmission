<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sx" uri="/struts-dojo-tags"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css" href="./css/staticpage-style.css" />
<title>Search Result</title>
<sx:head />
</head>

<body>
	<s:include value="./jsp/header.jsp"></s:include>
	<div id="searchjobpage">

		<s:form action="PrepareJobQueryAction" method="post" namespace="/"
			theme="simple">
			<s:if test="jobInfoList != null && jobInfoList.size() > 0">
				<h4>
					Jobs which match search string "
					<s:property value="selectedJobTitle" />
					"
				</h4>
				<table border="2" cellspacing="0" cellpadding="2">
					<tr>
						<th>#</th>
						<th>Job Title</th>
						<th>Internal Job ID (UUID)</th>
						<th>Owner</th>
						<th>Job Created Time</th>
						<th>Status Last Updated Time</th>
						<th>Last updated Status</th>
					</tr>
					<s:iterator value="jobInsList" status="envStatus">
						<tr>
							<td><s:radio id="selectedRadio" name="selectedJob"
									list="#{key:label}" theme="simple" value="defaultJob" /></td>

							<td><s:label key="jobInfoList[%{#envStatus.index}].jobTitle" /></td>
							<td><s:label
									key="jobInfoList[%{#envStatus.index}].internalJobId" /></td>
							<td><s:label key="jobInfoList[%{#envStatus.index}].owner" /></td>
							<td><s:label
									key="jobInfoList[%{#envStatus.index}].createdTimeStr" /></td>
							<td><s:label
									key="jobInfoList[%{#envStatus.index}].lastStatusUpdateTimeStr" /></td>
							<td><s:label
									key="jobInfoList[%{#envStatus.index}].jobStatus" /></td>
						</tr>
					</s:iterator>
				</table>
				<br />
				<s:hidden name="selectedJobTitle" value="%{selectedJobTitle}" />
				<s:submit key="label.query" align="center" />
			</s:if>
			<s:else>
				<h4>
					No jobs match search string "
					<s:property value="selectedJobTitle" />
					", <a id="searchlink" href="PrepareJobInfoAction">Back to job
						search page</a>
				</h4>
			</s:else>
		</s:form>
	</div>
</body>
</html>