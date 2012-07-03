<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sx" uri="/struts-dojo-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Job Management Service</title>
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

	<h4>
		Job "
		<s:property value="selectedJobTitle" />
		" instances
	</h4>

	<s:form action="op_job" method="post" namespace="/" theme="simple">
		<s:if test="jobInfoList != null && jobInfoList.size() > 0">
			<table border="2" cellspacing="0" cellpadding="2">
				<tr>
					<th>#</th>
					<th>Job Title</th>
					<th>Internal Job ID (UUID)</th>
					<th>Owner</th>
					<th>Job Created Time</th>
					<th>Job Last Modified Time</th>
					<th>Status Last Updated Time</th>
					<th>Last updated Status</th>
				</tr>
				<s:iterator value="jobInsList" status="envStatus">
					<tr>

						<td><s:radio name="selectedJob" list="#{key:label}"
								theme="simple" /></td>

						<td><s:label key="jobInfoList[%{#envStatus.index}].jobTitle" /></td>
						<td><s:label
								key="jobInfoList[%{#envStatus.index}].internalJobId" /></td>
						<td><s:label key="jobInfoList[%{#envStatus.index}].owner" /></td>
						<td><s:label
								key="jobInfoList[%{#envStatus.index}].createdTimeStr" /></td>
						<td><s:label
								key="jobInfoList[%{#envStatus.index}].lastModifiedTimeStr" /></td>
						<td><s:label
								key="jobInfoList[%{#envStatus.index}].lastStatusUpdateTimeStr" /></td>
						<td><s:label key="jobInfoList[%{#envStatus.index}].jobStatus" /></td>
					</tr>
				</s:iterator>
			</table>
		</s:if>

		<br />
		<p>
			Operation
			<s:radio label="Operation" name="selectedOp" list="ops"
				value="defaultOpValue" />
		</p>
		<br />
		<s:hidden name="username" value="%{username}" />
		<s:hidden name="selectedJobTitle" value="%{selectedJobTitle}" />
		<s:submit key="label.submit" align="center" />
	</s:form>
</body>
</html>