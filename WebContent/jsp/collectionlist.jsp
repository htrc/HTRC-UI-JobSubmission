<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css" href="./css/collection-style.css" />
<title>HTRC COLLECTIONS</title>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.8.0/jquery.min.js"></script>
<script language="javascript">
	$(document).ready(function(){	
		// turn it on for performance issue
		$.ajaxSetup ({
			//cache: false
			cache: true
		});
		
		$('#ajaxBusy').ajaxStart(function(){
		   	$(this).html("<img src='./images/loading.gif' />");
		});
		$("#ajaxBusy").ajaxStop(function(){
		   	$(this).html("");
		});
		$("#ajaxError").ajaxError(function(){
		   	$(this).html("Unable to retrieve data from agent. Try again.");
		   	$('#selectcollection').attr('disabled', false);		   	
		});
		
		$('#selectcollection').change(function() {
			//alert($('#selectcollection').val());
			$('#selectcollection').attr('disabled', true);
			
			// remove previous elements
			$('#collectiontable').empty(); 
			$('#ajaxBusy').empty();
			$('#ajaxError').empty();
			
			var collectionName = $('#selectcollection').val();
			$.get("ViewCollectionDetailsAction", 
				{collectionName: collectionName},
				function(response){
					//alert(response);
					//alert(response.collectionName);					
					$('#selectcollection').attr('disabled', false);
					$('#collectiontable').append("<table>");
					$('#collectiontable').append("<tr>" + "<td><b>Collection Name:</td><td>" + response.collectionName + "</td></tr>");
			    	$('#collectiontable').append("<tr>" + "<td><b>Collection Description:</td><td>" + response.attributes.description + "</td></tr>");
			    	$('#collectiontable').append("<tr>" + "<td><b>Collection Owner:</td><td>" + response.attributes.owner + "</td></tr>");
			    	$('#collectiontable').append("<tr>" + "<td><b>Collection Availability:</td><td>" + response.attributes.availability + "</td></tr>");
			    	$('#collectiontable').append("<tr>" + "<td><b>Collection Tags:</td><td>" + response.attributes.tags + "</td></tr>");
			    	$('#collectiontable').append("</table>");
				},
				"json");
		});
	});
	
	/*
	function select() {
		var select = document.getElementById("selectcollection");
		var collectionName = select.options[select.selectedIndex].value;
		
		var xmlhttp = new XMLHttpRequest();
		xmlhttp.onreadystatechange=function() {
			if (xmlhttp.readyState==4 && xmlhttp.status==200) {
		    	//document.getElementById("result").innerHTML=xmlhttp.responseText;
		    	$('#collectiontable').empty(); // remove previous elements
		    	var data = eval('(' + xmlhttp.responseText + ')');
		    	
		    	$('#collectiontable').append("<tr>" + "<td><b>Collection Name:</td><td>" + data.collectionName + "</td></tr>");
		    	$('#collectiontable').append("<tr>" + "<td><b>Collection Description:</td><td>" + data.attributes.description + "</td></tr>");
		    	$('#collectiontable').append("<tr>" + "<td><b>Collection Owner:</td><td>" + data.attributes.owner + "</td></tr>");
		    	$('#collectiontable').append("<tr>" + "<td><b>Collection Availability:</td><td>" + data.attributes.availability + "</td></tr>");
		    	$('#collectiontable').append("<tr>" + "<td><b>Collection Tags:</td><td>" + data.attributes.tags + "</td></tr>");
		  	}
		};
		
		xmlhttp.open("GET", "ViewCollectionDetailsAction?collectionName="+collectionName, true);
		xmlhttp.send();
	}
	*/	
</script>
</head>
<body>
	<s:include value="header.jsp"></s:include>
	<div id="collectionpage">
		<s:if test="hasActionErrors()">
			<div class="errors">
				<s:actionerror />
			</div>
		</s:if>
		
		<div><a href="http://htrc.mine.nu/blacklight" class="createcollection">Create a New Collection</a></div>
		
		<div id="collectionlist">
			<fieldset>
				<legend>Available Collections</legend>
				<div >
					<select id="selectcollection" name="selectcollection" 
						size="25" onchange="select();">
						<s:iterator value="allCollections" >
							<option><s:property value="key"/></option>
						</s:iterator>
					</select>
				</div>
			</fieldset>
		</div>	
			
		<div id="collectiondetails" >
			<fieldset>
				<legend>Collection Details</legend>
				<div id="ajaxBusy" >Please select a collection in the list on the left to display information.</div>
				<div id="ajaxError" ></div>
				<table id="collectiontable"></table>
			</fieldset>
		</div>
	</div>
</body>
</html>

