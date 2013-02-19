<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<link rel="stylesheet" type="text/css" href="./css/header-style.css" />
<div class="header">
	<div>
		<img alt="HATHI TRUST" src="./images/hathi.jpg">
		<div id="htrcheader">HathiTrust Research Center</div>
		<div id="subtitle">
			<s:property value="webPageTitle" />
		</div>
		<div id="floatclear"></div>
	</div>
	<div class="navbar">
		<ul>
			<li><a href="HomeAction"><span>Home</span></a></li>
			<li><a href="AboutAction"><span>About</span></a></li>
			<li><a href="PrepareJobInfoAction"><span>Search Job</span></a>
			</li>
			<li><a href="JobSubmitFormAction"><span>Submit Job</span></a></li>
			<li><a href="ManageWorkSetAction"><span>Workset</span></a></li>
			<li><a href="HelpAction"><span>Help</span></a></li>

			<li><s:if test="%{#session['session.token'] == null}">
					<a href="LogInAction"><span>Login</span></a>
				</s:if> <s:else>
					<a href="LogoutAction"><span>Logout</span></a>
				</s:else></li>
		</ul>
	</div>
</div>

