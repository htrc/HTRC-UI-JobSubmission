<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css" href="./css/results-style.css" />
<title>Job Details</title>
<script type="text/javascript"
	src="http://ajax.googleapis.com/ajax/libs/jquery/1.8.0/jquery.min.js"></script>
<script type="text/javascript">
	$(document).ready(function(){
	    $("#resultlinks a").click(function(e) {
	        e.preventDefault();
	        $("#iframeresult").attr("src", $(this).attr("href"));
	    });
	});
</script>
</head>

<body>
	<s:include value="header.jsp"></s:include>	
	<div id="viewjob">
		<div id="jobdetails">
			<fieldset>
				<legend><b>Job Details</b></legend>
				<table style="table-layout: fixed; width: 100%">
					<s:iterator value="jobDetailBeans">
						<tr><td style="word-wrap: break-word"><b>Job Title: </b><s:property value="jobTitle"/></td></tr>
						<tr><td style="word-wrap: break-word"><b>Algorithm Name: </b><s:property value="algorithmName"/></td></tr>
						<tr><td><b>Last Updated: </b><s:property value="lastUpdatedDate"/></td></tr>
						<tr><td><b>Results: </b>
								<div id="resultlinks">
									<table>
										<s:iterator value="results">
											<tr>
												<td style="border:solid 2px #060"><a href="<s:url value="%{value}" />" onclick="showresult('<s:property value="value" />');"><s:property value="key" /></a></td>
											</tr>
										</s:iterator>
									</table>
								</div></td>
						</tr>
						<tr><td><b>Job Parameters: </b>
								<div id="parameters">
								<table width="100%">
									<s:iterator value="jobParams">
										<tr>
											<td style="border:solid 2px #060; word-wrap: break-word">
											<s:property value="key"/>: <s:property value="value"/>
											</td>
										</tr>
									</s:iterator>
								</table></div></td>
						</tr>
						<tr><td><b>Job Id: </b><br><s:property value="jobId"/></td></tr>
						<tr><td><b>Status: </b><br><s:property value="jobStatus"/></td></tr>
					</s:iterator>
				</table>
			</fieldset>
		</div>
	
		<div id="visibleresult">
			<fieldset>
				<legend><b>View Results</b></legend>
				<iframe id="iframeresult" src="">
	  				<p>Your browser does not support iframes.</p>
				</iframe>
			</fieldset>
		</div>
	</div>
</body>
</html>