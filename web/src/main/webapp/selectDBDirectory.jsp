<%@page import="java.nio.file.Files"%>
<%@page import="java.nio.file.Path"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href=css/SyncLiteStyle.css>
<title>Configure SyncLite Sample Application</title>
</head>

<% 
	String jobName = request.getParameter("jobName");
	String errorMsg = request.getParameter("errorMsg");
	
	String basePath = request.getParameter("basePath");
	
	if (jobName != null) {
		//Check if specified jobName is in correct format
		
		if (jobName.length() > 16 ) {
			errorMsg = "Job name must be upto 16 characters in length";
		}
		if (!jobName.matches("[a-zA-Z0-9-_]+")) {
			errorMsg = "Specified job name is invalid. Allowed characters are alphanumeric, hyphen or underscrore characters.";
		}		
	} else {
		jobName = "job1";
	}

	//Path rootDir = Path.of(getServletContext().getRealPath("/")).getRoot();
	//Path defaultDataRoot = Path.of(rootDir.toString(), "synclite", "workDir");
	Path defaultBasePath = Path.of(System.getProperty("user.home"), "synclite", jobName, "db");
	basePath = defaultBasePath.toString();
%>

<body>
	<%@include file="html/menu.html"%>
	<div class="main">
		<h2>Configure SyncLite Sample Application</h2>
		<%	
		if (errorMsg != null) {
			out.println("<h4 style=\"color: red;\">" + errorMsg + "</h4>");
		}
		%>
	
		<form method="post" action="validateDBDirectory">
			<table>
				<tbody>
					<tr>
						<td>Job Name</td>
						<td><input type="text" size = 30 id="jobName" name="jobName" value="<%=jobName%>" onchange="this.form.action='selectDBDirectory.jsp'; this.form.submit();" title="Specify SyncLite consolidator job name. Make sure that the job name specified here is same as the one specified in SyncLite Consolidator"/></td>
					</tr>
					<tr>
						<td>DB Base Path</td>
						<td><input type="text" size = 50 id="basePath" name="basePath" value="<%=basePath%>" title="Specify a work directory for SyncLite dbreader to store SyncLite devices holding extracted data frpom source database."/></td>
					</tr>
				</tbody>
			</table>
			<center>
				<button type="submit" name="next">Next</button>
			</center>			
			
		</form>
	</div>
</body>
</html>