<%@page import="java.nio.file.Paths"%>
<%@page import="java.nio.file.Files"%>
<%@page import="java.time.Instant"%>
<%@page import="java.nio.file.Path"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.FileReader"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.io.FileWriter"%>
<%@page import="javax.websocket.Session"%>
<%@page import="io.synclite.logger.*" %>


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
<title>SyncLite App - Create Databases</title>
</head>
<%

String jobName = session.getAttribute("jobName").toString(); 
//Check if base path is set in session
if (jobName == null) {
	response.sendRedirect("selectDBDirectory.jsp");
}

String basePath = session.getAttribute("basePath").toString(); 
//Check if base path is set in session
if (basePath == null) {
	response.sendRedirect("selectDBDirectory.jsp");
}


Integer numDevices = 1;
if (request.getParameter("numDevices") != null) {
	numDevices = Integer.valueOf(request.getParameter("numDevices"));
} else {
	//Check if numDevices is set in session
	if (session.getAttribute("numDevices") != null) {
		numDevices = Integer.valueOf(session.getAttribute("numDevices").toString());
	}	
}

String deviceType = "SQLITE"; 
if (request.getParameter("deviceType") != null) {
	deviceType = request.getParameter("deviceType");
} else {
	//Check if deviceType is set in session
	if (session.getAttribute("deviceType") != null) {
		deviceType = session.getAttribute("deviceType").toString();
	}
}


String props = "";
if (request.getParameter("props") != null) {
	props =  request.getParameter("props");
} else if (Files.exists(Path.of(basePath, "synclite_logger.conf"))) { 
	props = Files.readString(Path.of(basePath, "synclite_logger.conf"));
} else {
	
	//Check if synclite_logger.conf exists in dbpath, if yes load it.
	
	Path confPath = Path.of(basePath.toString(), "synclite_logger.conf");

	if (Files.exists(confPath)) {
		props = Files.readString(confPath);
	} else {
		StringBuilder propsBuilder = new StringBuilder();
		String newLine = System.getProperty("line.separator");
	
		String stageDir = Path.of(System.getProperty("user.home"), "synclite", jobName, "stageDir").toString();
		String commandDir = Path.of(System.getProperty("user.home"), "synclite", jobName, "commandDir").toString();

		propsBuilder.append("#==============Device Stage Properties==================");
		propsBuilder.append(newLine);
		propsBuilder.append("local-data-stage-directory=").append(stageDir);
		propsBuilder.append(newLine);
		propsBuilder.append("#local-data-stage-directory=<path/to/local/data/stage/directory>");
		propsBuilder.append(newLine);
		propsBuilder.append("local-command-stage-directory=").append(commandDir);
		propsBuilder.append(newLine);
		propsBuilder.append("#local-command-stage-directory=<path/to/local/command/stage/directory  #specify if device command handler is enabled>");
		propsBuilder.append(newLine);
		propsBuilder.append("destination-type=FS");
		propsBuilder.append(newLine);
		propsBuilder.append("#destination-type=<FS|MS_ONEDRIVE|GOOGLE_DRIVE|SFTP|MINIO|KAFKA|S3>");
		propsBuilder.append(newLine);
		propsBuilder.append(newLine);
		propsBuilder.append("#==============SFTP Configuration=================");
		propsBuilder.append(newLine);		
		propsBuilder.append("#sftp:host=<host name of SFTP server to receive shipped devices and device logs>");
		propsBuilder.append(newLine);
		propsBuilder.append("#sftp:port=<port number of SFTP server>");
		propsBuilder.append(newLine);
		propsBuilder.append("#sftp:user-name=<user name to connect to remote host>");
		propsBuilder.append(newLine);
		propsBuilder.append("#sftp:password=<password>");
		propsBuilder.append(newLine);
		propsBuilder.append("#sftp:remote-data-stage-directory=<remote data stage directory name that will stage device directories>");
		propsBuilder.append(newLine);
		propsBuilder.append("#sftp:remote-command-stage-directory=<remote command directory name which will hold command files sent by consolidator if device command handler is enabled>");
		propsBuilder.append(newLine);
		propsBuilder.append(newLine);	
		propsBuilder.append("#==============MinIO  Configuration=================");
		propsBuilder.append(newLine);
		propsBuilder.append("#minio:endpoint=<MinIO endpoint to upload devices>");
		propsBuilder.append(newLine);
		propsBuilder.append("#minio:access-key=<MinIO access key>");
		propsBuilder.append(newLine);
		propsBuilder.append("#minio:secret-key=<MinIO secret key>");
		propsBuilder.append(newLine);
		propsBuilder.append("#minio:data-stage-bucket-name=<MinIO data stage bucket name that will host device directories>");
		propsBuilder.append(newLine);
		propsBuilder.append("#minio:command-stage-bucket-name=<MinIO command stage bucket name that will hold command files sent by SyncLite Consolidator>");
		propsBuilder.append(newLine);
		propsBuilder.append(newLine);	
		propsBuilder.append("#==============S3 Configuration=====================");
		propsBuilder.append(newLine);
		propsBuilder.append("#s3:endpoint=https://s3-<region>.amazonaws.com");
		propsBuilder.append(newLine);
		propsBuilder.append("#s3:access-key=<S3 access key>");
		propsBuilder.append(newLine);
		propsBuilder.append("#s3:secret-key=<S3 secret key>");
		propsBuilder.append(newLine);
		propsBuilder.append("#s3:data-stage-bucket-name=<S3 data stage bucket name that will hold device directories>");
		propsBuilder.append(newLine);
		propsBuilder.append("#s3:command-stage-bucket-name=<S3 command stage bucket name that will hold command files sent by SyncLite Consolidator>");
		propsBuilder.append(newLine);
		propsBuilder.append(newLine);
		propsBuilder.append("#==============Kafka Configuration=================");
		propsBuilder.append(newLine);
		propsBuilder.append("#kafka-producer:bootstrap.servers=localhost:9092,localhost:9093,localhost:9094");
		propsBuilder.append(newLine);
		propsBuilder.append("#kafka-producer:<any_other_kafka_producer_property> = <kafka_producer_property_value>");
		propsBuilder.append(newLine);
		propsBuilder.append("#kafka-producer:<any_other_kafka_producer_property> = <kafka_producer_property_value>");
		propsBuilder.append(newLine);
		propsBuilder.append("#kafka-consumer:bootstrap.servers=localhost:9092,localhost:9093,localhost:9094");
		propsBuilder.append(newLine);
		propsBuilder.append("#kafka-consumer:<any_other_kafka_consumer_property> = <kafka_consumer_property_value>");
		propsBuilder.append(newLine);
		propsBuilder.append("#kafka-consumer:<any_other_kafka_consumer_property> = <kafka_consumer_property_value>");
		propsBuilder.append(newLine);
		propsBuilder.append(newLine);
		propsBuilder.append("#==============Table filtering Configuration=================");
		propsBuilder.append(newLine);
		propsBuilder.append("#include-tables=<comma separate table list>");
		propsBuilder.append(newLine);
		propsBuilder.append("#exclude-tables=<comma separate table list>");
		propsBuilder.append(newLine);
		propsBuilder.append(newLine);
		propsBuilder.append("#==============Logger Configuration==================");	
		propsBuilder.append(newLine);
		propsBuilder.append("#log-queue-size=2147483647");
		propsBuilder.append(newLine);
		propsBuilder.append("#log-segment-flush-batch-size=1000000");
		propsBuilder.append(newLine);
		propsBuilder.append("#log-segment-switch-log-count-threshold=1000000");
		propsBuilder.append(newLine);
		propsBuilder.append("#log-segment-switch-duration-threshold-ms=5000");
		propsBuilder.append(newLine);
		propsBuilder.append("#log-segment-shipping-frequency-ms=5000");
		propsBuilder.append(newLine);
		propsBuilder.append("#log-segment-page-size=4096");
		propsBuilder.append(newLine);
		propsBuilder.append("#log-max-inlined-arg-count=16");
		propsBuilder.append(newLine);
		propsBuilder.append("#use-precreated-data-backup=false");
		propsBuilder.append(newLine);
		propsBuilder.append("#vacuum-data-backup=true");
		propsBuilder.append(newLine);
		propsBuilder.append("#skip-restart-recovery=false");
		propsBuilder.append(newLine);
		propsBuilder.append(newLine);
		propsBuilder.append("#==============Command Handler Configuration==================");
		propsBuilder.append(newLine);
		propsBuilder.append("#enable-command-handler=false|true");
		propsBuilder.append(newLine);
		propsBuilder.append("#command-handler-type=INTERNAL|EXTERNAL");
		propsBuilder.append(newLine);
		propsBuilder.append("#external-command-handler=synclite_command_processor.bat <COMMAND> <COMMAND_FILE>");
		propsBuilder.append(newLine);
		propsBuilder.append("#external-command-handler=synclite_command_processor.sh <COMMAND> <COMMAND_FILE>");
		propsBuilder.append(newLine);
		propsBuilder.append("#command-handler-frequency-ms=10000");
		propsBuilder.append(newLine);
		propsBuilder.append(newLine);
		propsBuilder.append("#==============Device Configuration==================");
		propsBuilder.append(newLine);
		String deviceEncryptionKeyFile = Path.of(System.getProperty("user.home"), ".ssh", "synclite_public_key.der").toString();
		propsBuilder.append("#device-encryption-key-file=" + deviceEncryptionKeyFile);
		propsBuilder.append(newLine);
		propsBuilder.append("#device-name=");
		propsBuilder.append(newLine);	
	
		props = propsBuilder.toString();
	}
}

String emulateStatus = "";
if (request.getParameter("emulateStatus") != null) {
	emulateStatus= request.getParameter("emulateStatus");
}

String emulateStatusDetails = "";
if (request.getParameter("emulateStatusDetails") != null) {
	emulateStatusDetails = request.getParameter("emulateStatusDetails");
}


%>

<body>
	<%@include file="html/menu.html"%>	

	<div class="main">
		<h2>SyncLite Sample Application : Create/Initialize Databases</h2>
		<%
		//out.println("status : " + emulateStatus);
		if (emulateStatus != null) {
			if (emulateStatus.equals("SUCCESS")) {
				out.println("<h4 style=\"color: blue;\"> Successfully created  SyncLite devices </h4>");
			} else if (emulateStatus.equals("FAIL")) {
				out.println("<h4 style=\"color: red;\"> Device creation failed with error : "
				+ emulateStatusDetails.replace("<", "&lt;").replace(">", "&gt;") + "</h4>");
			}
		}
		%>

		<form action="${pageContext.request.contextPath}/deviceCreator"
			method="post">
			<table>
				<tbody>
					<tr>
						<td>Job Name</td>
						<td><input type="text" size = 30 id="job-name" name="job-name" value="<%=jobName%>" onchange="this.form.action='createDevices.jsp'; this.form.submit();" title="Specify a job name for sample application. Make sure that the job name specified here is same as the one specified in SyncLite Consolidator"/></td>
					</tr>
			
					<tr>
						<td>DB Base Path</td>
						<td><input type="text" size=30 id="basePath"
							name="basePath"
							value="<%=basePath%>"
							title="Specify a path to a directory which will hold all the created device/database files."/></td>
					</tr>
					
					<tr>
						<td>Number of Databases</td>
						<td><input type="text" id="numDevices"
							name="numDevices"
							value="<%=numDevices%>"
							title="Specify number of devices to be created/initialized. Please note that a numeric index ( starting from 1 to numDevices) will be designated as a device name to each device. In your own real applications, you can specify your own device names through properties file or SyncLite API."/></td>
					</tr>
					
					<tr>
						<td>Database Type</td>
						<td><select id="deviceType" name="deviceType" title="Select database/device type. A TELEMETRY device allows SQL operations CREATE/DROP/ALTER/RENAME/SELECT/INSERT and maintains only schema in the underlying database file, with all the inserts logged in eventlog files in the device stage directory. An APPENDER device is similar to a TELEMETRY device but in addition, it keeps a full copy of all the ingested data in the local database file. A SQLite/DuckDB device allows all SQL operations CREATE/DROP/ALTER/RENAME/SELECT/INSERT/UPDATE/DELETE under transactions, with all transactions captured in commandlog files in the device stage directory.">
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
						<td>Database Configurations</td>
						<td><textarea name="props" id="props" rows="25" cols="100" title="Specify database/device configuration. Specified device configurations are written into a .conf file and supplied to initialization of each database/device. Please note the defaults specified for local-stage-directory and destination-type."><%=props%></textarea>
						</td>
					</tr>
					
				</tbody>				
			</table>
			<center>
				<input type="submit" id="emulate" name="emulate" value="Create/Initialize">
			</center>			
		</form>
	</div>
</body>
</html>
