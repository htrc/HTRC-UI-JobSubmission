<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib prefix="s" uri="/struts-tags"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css" href="../css/login-style.css" />
<title>HTRC LOGIN</title>
<script type="text/javascript"
	src="http://ajax.googleapis.com/ajax/libs/jquery/1.8.0/jquery.min.js"></script>
<script language="javascript">
	$(document).ready(function(){			
		$('#loginform').submit(function() {					
			if ($('#username').val() == '' || $('#password').val() == null) {
				alert("Please check your input.");
				return false;
			} else {
				$('#submitbutton').attr('disabled', true);
				return true;
			}
		});		
	});

	function clearInput() {
		document.getElementsByName("username").value = '';
		document.getElementsByName("password").value = '';
		return true;
	}
</script>
</head>
<body>
	<div class="login">
		<img alt="HATHI TRUST" src="../images/hathi.jpg" /> 
		<div>
			HathiTrust Research Center Login
		</div>
		<div class="input">
			<s:form id="loginform" action="LogInAction" method="post" >
				<s:if test="hasActionErrors()">
					<div class="errors">
						<s:actionerror />
					</div>
				</s:if>
				<s:textfield id="username" name="username" key="label.username" value=""></s:textfield>
				<s:password id="password" name="password" key="label.password" value=""></s:password>
				<s:submit id="submitbutton" method="execute" key="label.login" align="center" />
			</s:form>
		</div>
	</div>
</body>
</html>