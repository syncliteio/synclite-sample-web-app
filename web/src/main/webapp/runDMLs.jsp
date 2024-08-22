<%@page import="java.nio.file.Files"%>
<%@page import="java.time.Instant"%>
<%@page import="java.nio.file.Path"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.FileReader"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.io.FileWriter"%>
<%@page import="javax.websocket.Session"%>
<%@page import="io.synclite.logger.*" %>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>



<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.sql.*"%>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href=css/SyncLiteStyle.css>

<script type="text/javascript">
</script>
<title>Run Workload on SyncLite Devices</title>
</head>
<%
String basePath = "";
if (session.getAttribute("basePath") != null) {
	basePath = request.getSession().getAttribute("basePath").toString().trim();
}

Integer numDevices = 1;
if (session.getAttribute("numDevices") != null) {
	numDevices = (Integer) request.getSession().getAttribute("numDevices");
}

String deviceType = "SQLITE";
if (session.getAttribute("deviceType") != null) {
	deviceType = request.getSession().getAttribute("deviceType").toString();
}

String workload = "";
if (request.getParameter("workload") != null) {
	workload = 	request.getParameter("workload");
}

Integer startDeviceIdx = 1;
if (request.getParameter("startDeviceIdx") != null) {
	startDeviceIdx =  Integer.valueOf(request.getParameter("startDeviceIdx"));
} 

Integer endDeviceIdx = 1;
if (request.getParameter("endDeviceIdx") != null) {
	endDeviceIdx =  Integer.valueOf(request.getParameter("endDeviceIdx"));
} 

Long elapsedTime = 0L;
if (request.getParameter("elapsedTime") != null) {
	elapsedTime =  Long.valueOf(request.getParameter("elapsedTime"));
} 

String runStatus = "";
if (request.getParameter("runStatus") != null) {
	runStatus= request.getParameter("runStatus");
}

String runStatusDetails = "";
if (request.getParameter("runStatusDetails") != null) {
	runStatusDetails = request.getParameter("runStatusDetails");
}

if (basePath.equals("")) {
	runStatus = "FAILED";
	runStatusDetails ="Please create/initialize the databases first";	
}

String sampleSQL = "--SQL DML/DDL Statements--\n\nCREATE TABLE t1(a INT);\nINSERT INTO t1 values(100);"; 
%>
<body>
	<%@include file="html/menu.html"%>	

	<div class="main">
		<h2>SyncLite Sample App : Run Workload</h2>
		<%
		//out.println("status : " + runStatus);
		if (runStatus != null) {
			if (runStatus.equals("SUCCESS")) {				
				out.println("<h4 style=\"color: blue;\"> Status : SUCCESS. Elapsed Time : " + elapsedTime + " ms </h4>");
			} else if (runStatus.equals("FAIL")) {
				out.println("<h4 style=\"color: red;\"> Workload execution failed with error : "
				+ runStatusDetails.replace("<", "&lt;").replace(">", "&gt;") + "</h4>");
			}
		}
		%>
		
		<form action="${pageContext.request.contextPath}/workloadRunner"
			method="post">		
			<table>
				<tbody>
					<tr>
						<td>Database Path</td>
						<td><input type="text" id="basePath"
							name="basePath"
							value="<%=basePath%>" disabled/></td>
					</tr>

					<tr>
						<td>Database Type</td>
						<td><select id="deviceType" name="deviceType" disabled>
								<%
								if (deviceType.equals("SQLITE")) {
									out.println("<option value=\"SQLITE\" selected>SQLite</option>");
								} else {
									out.println("<option value=\"SQLITE\">SQLite</option>");
								}
								if (deviceType.equals("SQLITE_APPENDER")) {
									out.println("<option value=\"SQLITE_APPENDER\" selected>SQLite Appender</option>");
								} else {
									out.println("<option value=\"SQLITE_APPENDER\">SQLite Appender</option>");
								}
								if (deviceType.equals("DUCKDB")) {
									out.println("<option value=\"DUCKDB\" selected>DuckDB</option>");
								} else {
									out.println("<option value=\"DUCKDB\">DuckDB</option>");
								}
								if (deviceType.equals("DUCKDB_APPENDER")) {
									out.println("<option value=\"DUCKDB_APPENDER\" selected>DuckDB Appender</option>");
								} else {
									out.println("<option value=\"DUCKDB_APPENDER\">DuckDB Appender</option>");
								}
								if (deviceType.equals("DERBY")) {
									out.println("<option value=\"DERBY\" selected>Apache Derby</option>");
								} else {
									out.println("<option value=\"DERBY\">Apache Derby</option>");
								}
								if (deviceType.equals("DERBY_APPENDER")) {
									out.println("<option value=\"DERBY_APPENDER\" selected>Apache Derby Appender</option>");
								} else {
									out.println("<option value=\"DERBY_APPENDER\">Apache Derby Appender</option>");
								}
								if (deviceType.equals("H2")) {
									out.println("<option value=\"H2\" selected>H2</option>");
								} else {
									out.println("<option value=\"H2\">H2</option>");
								}
								if (deviceType.equals("H2_APPENDER")) {
									out.println("<option value=\"H2_APPENDER\" selected>H2 Appender</option>");
								} else {
									out.println("<option value=\"H2_APPENDER\">H2 Appender</option>");
								}
								if (deviceType.equals("HYPERSQL_APPENDER")) {
									out.println("<option value=\"HYPERSQL_APPENDER\" selected>HyperSQL Appender</option>");
								} else {
									out.println("<option value=\"HYPERSQL_APPENDER\">HyperSQL Appender</option>");
								}
								if (deviceType.equals("TELEMETRY")) {
									out.println("<option value=\"TELEMETRY\" selected>SyncLite Telemetry</option>");
								} else {
									out.println("<option value=\"TELEMETRY\">SyncLite Telemetry</option>");
								}
								if (deviceType.equals("STREAMING")) {
									out.println("<option value=\"STREAMING\" selected>SyncLite Streaming</option>");
								} else {
									out.println("<option value=\"STREAMING\">SyncLite Streaming</option>");
								}
								%>
						</select></td>
					</tr>

					<tr>
						<td>Start Database Index</td>
						<td><input type="Integer" id="startDeviceIdx"
							name="startDeviceIdx"
							value="<%=startDeviceIdx%>"
							title="Specify the starting index of the database/device to execute SQL workload."/></td>
					</tr>

					<tr>
						<td>End Database Index</td>
						<td><input type="Integer" id="endDeviceIdx"
							name="endDeviceIdx"
							value="<%=endDeviceIdx%>"
							title="Specify the ending index of the database/device to execute SQL workload."/></td>
					</tr>

					<tr>
						<td>SQL Workload (DDL/DML)</td>
						<td><textarea name="workload" id="workload" rows="25" cols="100" placeholder="<%=sampleSQL%>" style="color:blue" title="Specify SQL workload to execute on the selected set of database/devices"><%=workload%></textarea>
						</td>
					</tr>
				</tbody>				
			</table>
			<center>
				<input type="submit" id="run" name="run" value="Run">
			</center>			
		</form>
	</div>
</body>
</html>