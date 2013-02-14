<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib prefix="s" uri="/struts-tags"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css" href="./css/staticpage-style.css" />
<title>HTRC Sloan Cloud Home</title>
</head>
<body>
	<s:include value="./jsp/header.jsp"></s:include>
	<div id="homepage">
		<h2>
			<br> Welcome to the HathiTrust Research Center Sloan Cloud!
		</h2>
		<p>
			<br> The HathiTrust Research Center (HTRC) is a collaborative
			research center launched jointly by Indiana University and the
			University of Illinois, along with the HathiTrust Digital Library, to
			help meet the technical challenges of dealing with massive amounts of
			digital text that researchers face by developing cutting-edge
			software tools and cyberinfrastructure to enable advanced
			computational access to the growing digital record of human
			knowledge.
		</p>
		<p>
			The HTRC Sloan Cloud provisions a cloud computation environment to
			run analysis and algorithm against HTRC private corpus on behalf of
			researchers. Currently Hadoop MapReduce job and Hadoop PigLatin data
			flow job are supported.
			<s:if test="%{#session['session.token']==null}">
				Click on the login link to begin. 
				<a id="loginlink" href="LogInAction">LOGIN</a>
			</s:if>
		</p>
	</div>
</body>
</html>