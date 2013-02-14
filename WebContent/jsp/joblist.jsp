<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<!-- <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">  -->
<meta http-equiv="refresh" content="15">
<link rel="stylesheet" type="text/css" href="./css/results-style.css" />
<title>Job List</title>
<script type="text/javascript"
	src="http://ajax.googleapis.com/ajax/libs/jquery/1.8.0/jquery.min.js"></script>
	
<script language="javascript">
	$(document).ready(function(){
		$("#deletejobs").click(function() {
			$('#completedform').submit();
		});
		
		$("#canceljobs").click(function() {
			$('#activeform').submit();
		});
	});

	function abortOrDeletaJobs(op) {
		var checkboxes;
		if (op == 'cancel') {
			checkboxes = document.getElementsByName("abortJobIds");
		} else if (op == 'delete') {
			checkboxes = document.getElementsByName("deleteJobIds");
		}		
		
		var jobIds = new Array();
		var index = 0;
		for (var i = 0; i < checkboxes.length; i++) {
			if (checkboxes[i].checked) 
				jobIds[index++] = checkboxes[i].value;
		}		
		if (jobIds.length == 0) {
			alert("Please select at least one job!");
			return false;
		} else {
			var str = "";
			for (i = 0; i < jobIds.length-1; i++) str += jobIds[i] + ", ";
			str += jobIds[jobIds.length-1] + "?";
			return confirm("Do you want to " + op + " " + str);
		}
	}
</script>
</head>
<body>
	<s:include value="header.jsp"></s:include>
	<div id="joblist">
		<div id="activejobs">
			<s:form action="ControlJobsAction" method="post" namespace="/" 
				theme="simple" name="activeform" id="activeform"
				onsubmit="return abortOrDeletaJobs('cancel')">
				<fieldset>
					<legend><b>Active Jobs</b></legend>
					<div id="activejobsheader">
						<table width="98%">
							<tr>
								<th width="40%"><div id="jobtitle">Job Title</div></th>
								<th width="20%">Last Updated</th>
								<th width="20%">Status</th>
								<th width="20%">Cancel?</th>
							</tr>
						</table>	
					</div>
					<div id="activejobsbody">
						<table width="100%">
							<s:iterator value="jobsActiveDetails">
								<tr>
									<td width="40%">
										<div id="jobtitle">
											<s:property value="jobTitle" />
										</div>
									</td>
									<td width="20%"><s:property value="lastUpdatedDate" /></td>
									<td width="20%"><s:property value="jobStatus" /></td>
									<td width="20%"><s:checkbox name="abortJobIds" fieldValue="%{jobId}"/></td>
								</tr>
							</s:iterator>
						</table>
					</div>
					<!-- <s:submit id="cancelbutton" key="label.job.abort.button" name="Cancel" align="center" /> -->
					<a href="#" class="canceljobs" id="canceljobs">Cancel</a>
				</fieldset>
			</s:form>
		</div>
	
		<div id="completedjobs">
			<s:form action="ControlJobsAction" method="post" namespace="/" theme="simple"
				name="completedform" id="completedform"
			  	onsubmit="return abortOrDeletaJobs('delete')">
				<fieldset>
					<legend><b>Completed Jobs</b></legend>
					<div id="completedjobsheader">
						<table width="98%">
							<tr>
								<th width="40%"><div id="jobtitle">Job Title</div></th>
								<th width="20%">Last Updated</th>
								<th width="20%">Status</th>
								<th width="20%">Delete?</th>
							</tr>
						</table>
					</div>
					<div id="completedjobsbody">
						<table width="100%">
							<s:iterator value="jobsCompletedDetails">
								<tr>
									<td width="40%">
										<div id="jobtitle">
										<s:url action="ViewJobsDetailsAction.action" var="joburl" >
		    								<s:param name="jobId"><s:property value="jobId"/></s:param>
										</s:url>
										<s:a href="%{joburl}"><s:property value="jobTitle" /></s:a>
										</div>
									</td>
									<td width="20%"><s:property value="lastUpdatedDate" /></td>
									<td width="20%">
										<s:set name="status" value="jobStatus"/>
										<s:if test="%{#status == 'Crashed'}">
											<div style="color:#FF4500"><s:property value="jobStatus" /></div>
										</s:if>
										<s:else>
											<s:property value="jobStatus" />
										</s:else>
									</td>
									<td width="20%"><s:checkbox name="deleteJobIds" fieldValue="%{jobId}"/></td>
								</tr>
							</s:iterator>
						</table>						
					</div>					
					<!-- <s:submit id="deletebutton" method="execute" key="label.job.delete.button" align="center" /> -->
					<a href="#" class="deletejobs" id="deletejobs">Delete</a>
				</fieldset>
			</s:form>
		</div>		
	</div>
</body>
</html>