<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib prefix="s" uri="/struts-tags"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css" href="./css/algorithm-style.css" />
<title>HTRC ALGORITHMS</title>
<script type="text/javascript"
	src="http://ajax.googleapis.com/ajax/libs/jquery/1.8.0/jquery.min.js"></script>
<script type="text/javascript">
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
		   	$('#selectalgorithm').attr('disabled', false);		   	
		});
		
		$('#jobsubmitform').submit( function() {
			//var str = $('#collection').val();
			//alert(str);
			
			if ($('#jobName').val() == '') { // more checks are needed
				alert("Please check your input.");
				return false;
			} else {
				$('#submitbutton').attr('disabled', true);
				return true;
			}
	    });
		
		$('#selectalgorithm').change(function() {			
			// disable user's input
			$('#selectalgorithm').attr('disabled', true);
			
			// remove previous elements
			$('#algorithmtable').empty(); 
			$('#ajaxBusy').empty();
			$('#ajaxError').empty();
			
			var algorithmName = $('#selectalgorithm').val();
			$.get("ViewAlgorithmDetailsAction", 
				{algorithmName: algorithmName},
				function(response){
					//alert(response);
					//alert(response.algorithmName);
					$('#selectalgorithm').attr('disabled', false);
										
					// algorithm info table
					$('#algorithmtable').append("<table>");
					$('#algorithmtable').append("<tr>" + "<td><b>Algorithm Name: </td><td>" + response.algorithmName + "</td></tr>");
			    	$('#algorithmtable').append("<tr>" + "<td><b>Algorithm Description: </td><td>" + response.algorithmDetail.description + "</td></tr>");
			    	$('#algorithmtable').append("<tr>" + "<td><b>Version: </td><td>" + response.algorithmDetail.version + "</td></tr>");			    	
			    	var authorsStr = "";
			    	$.each(response.algorithmDetail.authors, function(k, v){
			    		authorsStr += v + ";";
			    	}); 
			    	$('#algorithmtable').append("<tr>" + "<td><b>Algorithm Author: </td><td>" + authorsStr + "</td></tr>");
			    	$('#algorithmtable').append("</table>");
			    	$('#algorithmtable').append("<br>");
			    	
			    	// user input
			    	$('#algorithmtable').append("<div id=\"param\"><label>Please Input Job Name: </label> (required)<br>"+ 
			    			"<input type=\"text\" name=\"jobName\" id=\"jobName\"/></div>");
			    	
			    	var index = 0;
			    	$.each(response.algorithmDetail.parameters, function(k, v){
			    		var col1 = v.label;
			    		var col2 = ""; 
			    		$('#algorithmtable').append("<input type=\"hidden\" name=\"parameters[" + index + "].paramName\" value=\"" + v.name + "\">");
		    			$('#algorithmtable').append("<input type=\"hidden\" name=\"parameters[" + index + "].paramType\" value=\"" + v.type + "\">");
			    		if (v.type == 'collection') { // other type??
			    			col2 = "<select name=\"parameters[" + index + "].paramValue\">";
			    			$.each(response.collectionNames, function(k1, v1){
			    				col2 += "<option>" + v1 + "</option>";
			    			}); 
			    			col2 += "</select>";
			    		} else if (v.type == 'boolean') {
			    			col2 = "<select name=\"parameters[" + index + "].paramValue\">";
			    			col2 += "<option>True</option><option>False</option></select>";
			    		} else {
			    			col2 = "<input id=\"collection\" type=\"text\" name=\"parameters[" + index + "].paramValue\" />";
			    			if (v.required == false) col2 += " (optional)";
			    			else col2 += " (required)";
			    			//alert("else " + v.name);
			    			//col2 = "<input type=\"text\" name=\"" + "aa" + ""\" id=\"jobname\"/>";
			    		}
			    		$('#algorithmtable').append("<div id=\"param\"><label>" + col1 + ": <label><br>" + col2 + "</div>");
			    		index++;
			    	});
			    	
			    	// add some hidden fields for submit
			    	$('#algorithmtable').append("<input type=\"hidden\" name=\"algorithmName\" value=\"" + response.algorithmName + "\">");
			    	
			    	// submit button
			    	//$('#algorithmtable').append("<input id=\"submitbutton\" type=\"submit\" value=\"Submit\" />");
			    	$('#algorithmtable').append("<a href=\"#\" id=\"submitbutton\" class=\"submitbutton\">Submit</a>");
			    	
			    	$("#submitbutton").click(function(){
			    		$('#jobsubmitform').submit();
					});
				},
				"json");
		});		
		
		
	});
</script>
</head>
<body>
	<s:include value="header.jsp"></s:include>
	<div id="algorithmpage">
		<div id="algorithmlist">
			<fieldset>
				<legend>Available Algorithms</legend>
				<div style="overflow: auto;">
				<select id="selectalgorithm" size="25" style="overflow: auto;">
					<s:iterator id="p" value="algorithms" status="indice">
						<option>
							<s:property value="%{algorithms[#indice.index]}" />
						</option>
					</s:iterator>
				</select>
				</div>
			</fieldset>
		</div>
		<div id="algorithmdetails">
			<s:form action="SubmitJobAction" method="post" id="jobsubmitform">
				<fieldset id="algorithmfieldset">
					<legend>Algorithm Parameters</legend>
					<div id="ajaxBusy" >Please select an algorithm in the list to display information.</div>
					<div id="ajaxError" ></div>
					<div id="algorithmtable"></div>
				</fieldset>
			</s:form>
		</div>
	</div>
</body>
</html>
