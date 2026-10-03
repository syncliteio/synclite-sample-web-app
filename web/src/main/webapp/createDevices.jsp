<%-- 
    Copyright (c) 2024 mahendra.chavan@syncLite.io, all rights reserved.

    Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
    in compliance with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software distributed under the License
    is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
    or implied.  See the License for the specific language governing permissions and limitations
    under the License.
--%>

<%@page import="java.nio.file.Paths"%>
<%@page import="java.nio.file.Files"%>
<%@page import="java.time.Instant"%>
<%@page import="java.nio.file.Path"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.FileReader"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.io.FileWriter"%>
<%@page import="io.synclite.logger.*" %>
<%@page import="org.owasp.encoder.Encode"%>


<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.sql.*"%>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href=css/SyncLiteStyle.css>

<script type="text/javascript">
var MAX_DESTINATIONS = 16;

function getDestinationRows() {
	return Array.prototype.slice.call(
		document.querySelectorAll("#destinationList .destination-card"));
}

function getDestinationField(row, fieldName) {
	return row.querySelector('[data-field="' + fieldName + '"]');
}

function toggleDstFields(row) {
	var dstType = getDestinationField(row, "dst-type");
	var dstTypeVal = dstType ? dstType.value : "SQLITE";
	var dbField = row.querySelector(".dst-db-field");
	var schemaField = row.querySelector(".dst-schema-field");
	var database = getDestinationField(row, "dst-database");
	var schema = getDestinationField(row, "dst-schema");
	var showDb = (dstTypeVal === "DUCKDB" || dstTypeVal === "POSTGRES");
	var showSchema = (dstTypeVal === "POSTGRES");
	if (dbField) {
		dbField.style.display = showDb ? "" : "none";
	}
	if (schemaField) {
		schemaField.style.display = showSchema ? "" : "none";
	}
	if (database) {
		database.required = showDb;
	}
	if (schema) {
		schema.required = showSchema;
	}
}

function toggleEmbeddedFields() {
	var consolidatorType = document.getElementById("consolidatorType");
	var embedded = consolidatorType && consolidatorType.value === "EMBEDDED";
	var fields = document.getElementsByClassName("embedded-field");
	for (var i = 0; i < fields.length; i++) {
		fields[i].style.display = embedded ? "" : "none";
	}
	var destinationInputs = document.querySelectorAll("#destinationList [data-field]");
	for (var inputIndex = 0; inputIndex < destinationInputs.length; inputIndex++) {
		destinationInputs[inputIndex].disabled = !embedded;
	}
	document.getElementById("num-destinations").disabled = !embedded;
	if (embedded) {
		var rows = getDestinationRows();
		if (rows.length === 0) {
			addDestination();
		}
		rows = getDestinationRows();
		for (var j = 0; j < rows.length; j++) {
			toggleDstFields(rows[j]);
		}
	}
}

// Populate the connection string, database and schema fields with
// sensible per-destination-type defaults. When 'force' is true the
// current values are overwritten (used on destination-type change);
// otherwise only empty fields are filled (used on initial page load
// so user/session values are preserved).
function applyDstDefaults(row, force) {
	var dstType = getDestinationField(row, "dst-type");
	if (!dstType || !window.DST_DEFAULTS) {
		return;
	}
	var defs = window.DST_DEFAULTS[dstType.value];
	if (!defs) {
		return;
	}
	var conn = getDestinationField(row, "dst-connection-string");
	var db = getDestinationField(row, "dst-database");
	var schema = getDestinationField(row, "dst-schema");
	var destinationIndex = getDestinationRows().indexOf(row) + 1;
	var connectionString = defs.connectionString.replace("{DST_INDEX}", destinationIndex);
	if (conn && (force || conn.value === "")) {
		conn.value = connectionString;
	}
	if (db && (force || db.value === "")) {
		db.value = defs.database;
	}
	if (schema && (force || schema.value === "")) {
		schema.value = defs.schema;
	}
}

function onDstTypeChange(select) {
	var row = select.closest(".destination-card");
	applyDstDefaults(row, true);
	toggleDstFields(row);
}

function createDestinationCard() {
	var row = document.createElement("div");
	row.className = "destination-card";
	row.innerHTML =
		'<div class="destination-card-header">' +
			'<strong class="destination-title"></strong>' +
			'<button type="button" class="destination-remove">Remove</button>' +
		'</div>' +
		'<div class="destination-grid">' +
			'<label>Destination Type' +
				'<select data-field="dst-type" title="Destination backend for the embedded consolidator.">' +
					'<option value="SQLITE">SQLite</option>' +
					'<option value="DUCKDB">DuckDB</option>' +
					'<option value="POSTGRES">PostgreSQL</option>' +
				'</select>' +
			'</label>' +
			'<label class="destination-wide">Connection String' +
				'<input type="text" data-field="dst-connection-string" required maxlength="4096" title="Connection string for this destination." />' +
			'</label>' +
			'<label class="dst-db-field">Database' +
				'<input type="text" data-field="dst-database" maxlength="1024" title="Required for DuckDB and PostgreSQL; leave empty for SQLite." />' +
			'</label>' +
			'<label class="dst-schema-field">Schema' +
				'<input type="text" data-field="dst-schema" maxlength="1024" title="Required for PostgreSQL; leave empty for SQLite." />' +
			'</label>' +
			'<label>Sync Mode' +
				'<select data-field="dst-sync-mode" title="Consolidation merges device changes; replication mirrors each device.">' +
					'<option value="CONSOLIDATION">Consolidation</option>' +
					'<option value="REPLICATION">Replication</option>' +
				'</select>' +
			'</label>' +
		'</div>';
	row.querySelector(".destination-remove").addEventListener("click", function() {
		removeDestination(this);
	});
	getDestinationField(row, "dst-type").addEventListener("change", function() {
		onDstTypeChange(this);
	});
	return row;
}

function renumberDestinations() {
	var rows = getDestinationRows();
	for (var offset = 0; offset < rows.length; offset++) {
		var index = offset + 1;
		rows[offset].querySelector(".destination-title").textContent = "Destination " + index;
		var fields = rows[offset].querySelectorAll("[data-field]");
		for (var fieldIndex = 0; fieldIndex < fields.length; fieldIndex++) {
			var fieldName = fields[fieldIndex].getAttribute("data-field");
			fields[fieldIndex].name = fieldName + "-" + index;
			fields[fieldIndex].id = fieldName + "-" + index;
		}
		rows[offset].querySelector(".destination-remove").disabled = rows.length === 1;
	}
	document.getElementById("num-destinations").value = rows.length;
	document.getElementById("addDestinationButton").disabled = rows.length >= MAX_DESTINATIONS;
}

function addDestination(initialValue) {
	if (getDestinationRows().length >= MAX_DESTINATIONS) {
		return;
	}
	var row = createDestinationCard();
	document.getElementById("destinationList").appendChild(row);
	renumberDestinations();
	if (initialValue) {
		getDestinationField(row, "dst-type").value = initialValue["dst-type"];
		getDestinationField(row, "dst-connection-string").value = initialValue["dst-connection-string"];
		getDestinationField(row, "dst-database").value = initialValue["dst-database"];
		getDestinationField(row, "dst-schema").value = initialValue["dst-schema"];
		getDestinationField(row, "dst-sync-mode").value = initialValue["dst-sync-mode"];
	}
	applyDstDefaults(row, !initialValue);
	toggleDstFields(row);
}

function removeDestination(button) {
	var rows = getDestinationRows();
	if (rows.length <= 1) {
		return;
	}
	button.closest(".destination-card").remove();
	renumberDestinations();
}

function readInitialDestinations() {
	var values = [];
	var initialRows = document.querySelectorAll("#initialDestinationValues .initial-destination");
	for (var i = 0; i < initialRows.length; i++) {
		values.push({
			"dst-type": initialRows[i].getAttribute("data-dst-type"),
			"dst-connection-string": initialRows[i].getAttribute("data-dst-connection-string"),
			"dst-database": initialRows[i].getAttribute("data-dst-database"),
			"dst-schema": initialRows[i].getAttribute("data-dst-schema"),
			"dst-sync-mode": initialRows[i].getAttribute("data-dst-sync-mode")
		});
	}
	return values;
}

window.addEventListener("DOMContentLoaded", function() {
	document.getElementById("addDestinationButton").addEventListener("click", function() {
		addDestination();
	});
	var initialDestinations = readInitialDestinations();
	for (var i = 0; i < initialDestinations.length; i++) {
		addDestination(initialDestinations[i]);
	}
	if (getDestinationRows().length === 0) {
		addDestination();
	}
	renumberDestinations();
	toggleEmbeddedFields();
	var form = document.querySelector('form[action$="/deviceCreator"]');
	if (form) {
		form.addEventListener("submit", renumberDestinations);
	}
});
</script>
<title>SyncLite App - Create Databases</title>
</head>
<%!
private String escHtml(String value) {
	return value == null ? "" : Encode.forHtml(value);
}

private int destinationCount(Object value) {
	if (value == null) {
		return 1;
	}
	try {
		return Math.max(1, Math.min(16, Integer.parseInt(value.toString())));
	} catch (NumberFormatException e) {
		return 1;
	}
}

private String destinationValue(javax.servlet.http.HttpServletRequest request,
		javax.servlet.http.HttpSession session, boolean indexedRequest,
		String fieldName, String legacyFieldName, int index, String defaultValue) {
	if (indexedRequest) {
		String value = request.getParameter(fieldName + "-" + index);
		return value == null ? defaultValue : value;
	}
	if (index == 1 && request.getParameter(legacyFieldName) != null) {
		return request.getParameter(legacyFieldName);
	}
	Object indexedValue = session.getAttribute(fieldName + "-" + index);
	if (indexedValue != null) {
		return indexedValue.toString();
	}
	if (index == 1 && session.getAttribute(legacyFieldName) != null) {
		return session.getAttribute(legacyFieldName).toString();
	}
	return defaultValue;
}
%>
<%

String jobName = session.getAttribute("jobName") == null ? null : session.getAttribute("jobName").toString(); 
//Check if base path is set in session
if (jobName == null) {
	response.sendRedirect("selectDBDirectory.jsp");
	return;
}

String basePath = session.getAttribute("basePath") == null ? null : session.getAttribute("basePath").toString(); 
//Check if base path is set in session
if (basePath == null) {
	response.sendRedirect("selectDBDirectory.jsp");
	return;
}

String csrfToken = (String) session.getAttribute("csrfToken");


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

String consolidatorType = "STANDALONE";
if (request.getParameter("consolidatorType") != null) {
	consolidatorType = request.getParameter("consolidatorType");
} else if (session.getAttribute("consolidatorType") != null) {
	consolidatorType = session.getAttribute("consolidatorType").toString();
}

boolean indexedDestinationRequest = request.getParameter("num-destinations") != null;
int numDestinations;
if (indexedDestinationRequest) {
	numDestinations = destinationCount(request.getParameter("num-destinations"));
} else {
	Object sessionDestinationCount = session.getAttribute("num-destinations");
	numDestinations = destinationCount(sessionDestinationCount);
}

List<String> dstTypes = new ArrayList<>();
List<String> dstConnectionStrings = new ArrayList<>();
List<String> dstDatabases = new ArrayList<>();
List<String> dstSchemas = new ArrayList<>();
List<String> syncModes = new ArrayList<>();
for (int index = 1; index <= numDestinations; ++index) {
	dstTypes.add(destinationValue(request, session, indexedDestinationRequest,
			"dst-type", "dstType", index, "SQLITE"));
	dstConnectionStrings.add(destinationValue(request, session, indexedDestinationRequest,
			"dst-connection-string", "dstConnectionString", index, ""));
	dstDatabases.add(destinationValue(request, session, indexedDestinationRequest,
			"dst-database", "dstDatabase", index, ""));
	dstSchemas.add(destinationValue(request, session, indexedDestinationRequest,
			"dst-schema", "dstSchema", index, ""));
	syncModes.add(destinationValue(request, session, indexedDestinationRequest,
			"dst-sync-mode", "syncMode", index, "CONSOLIDATION"));
}
// Default destination connection strings per destination type, mirroring
// the SyncLite Consolidator UI. SQLite/DuckDB destination databases belong
// in the job work directory, not alongside the source devices in db.
// PostgreSQL uses a local server template. These feed the client-side
// auto-populate logic.
Path defaultWorkDir = Path.of(System.getProperty("user.home"), "synclite", jobName, "workDir");
String defaultConnStrSQLite = "jdbc:sqlite:" + defaultWorkDir.resolve("consolidated_db_{DST_INDEX}.sqlite") + "?journal_mode=WAL";
String defaultConnStrDuckDB = "jdbc:duckdb:" + defaultWorkDir.resolve("consolidated_db_{DST_INDEX}.duckdb");
String defaultConnStrPostgreSQL = "jdbc:postgresql://127.0.0.1:5432/synclitedb?user=synclite&password=CHANGE_ME";


String props = "";
if (request.getParameter("props") != null) {
	props =  request.getParameter("props");
} else if (Files.exists(Path.of(basePath, "synclite.conf"))) {
	props = Files.readString(Path.of(basePath, "synclite.conf"));
} else if (Files.exists(Path.of(basePath, "synclite_logger.conf"))) {
	props = Files.readString(Path.of(basePath, "synclite_logger.conf"));
} else {
	
	//Check if synclite.conf (or legacy synclite_logger.conf) exists in dbpath, if yes load it.
	
	Path confPath = Path.of(basePath.toString(), "synclite.conf");
	Path legacyConfPath = Path.of(basePath.toString(), "synclite_logger.conf");

	if (Files.exists(confPath)) {
		props = Files.readString(confPath);
	} else if (Files.exists(legacyConfPath)) {
		props = Files.readString(legacyConfPath);
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
		propsBuilder.append("device-stage-type=FS");
		propsBuilder.append(newLine);
		propsBuilder.append("#device-stage-type=<FS|MS_ONEDRIVE|GOOGLE_DRIVE|SFTP|MINIO|KAFKA|S3>");
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
	<script type="text/javascript">
	// Per-destination-type defaults consumed by applyDstDefaults().
	// database/schema defaults align with DestinationOptions validation:
	// SQLite forbids both; DuckDB requires database (schema optional);
	// PostgreSQL requires both database and schema.
	window.DST_DEFAULTS = {
		"SQLITE": { connectionString: "<%=Encode.forJavaScript(defaultConnStrSQLite)%>", database: "", schema: "" },
		"DUCKDB": { connectionString: "<%=Encode.forJavaScript(defaultConnStrDuckDB)%>", database: "main", schema: "" },
		"POSTGRES": { connectionString: "<%=Encode.forJavaScript(defaultConnStrPostgreSQL)%>", database: "synclitedb", schema: "syncliteschema" }
	};
	</script>
	<div id="initialDestinationValues" hidden>
		<%
		for (int offset = 0; offset < numDestinations; ++offset) {
		%>
		<div class="initial-destination"
			data-dst-type="<%=Encode.forHtmlAttribute(dstTypes.get(offset))%>"
			data-dst-connection-string="<%=Encode.forHtmlAttribute(dstConnectionStrings.get(offset))%>"
			data-dst-database="<%=Encode.forHtmlAttribute(dstDatabases.get(offset))%>"
			data-dst-schema="<%=Encode.forHtmlAttribute(dstSchemas.get(offset))%>"
			data-dst-sync-mode="<%=Encode.forHtmlAttribute(syncModes.get(offset))%>"></div>
		<%
		}
		%>
	</div>
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
				+ escHtml(emulateStatusDetails) + "</h4>");
			}
		}
		%>

		<form action="${pageContext.request.contextPath}/deviceCreator"
			method="post">
			<input type="hidden" name="csrfToken" value="<%=escHtml(csrfToken)%>"/>
			<table>
				<tbody>
					<tr>
						<td>Job Name</td>
						<td><input type="text" size = 30 id="job-name" name="job-name" value="<%=escHtml(jobName)%>" onchange="this.form.action='createDevices.jsp'; this.form.submit();" title="Specify a job name for sample application. Make sure that the job name specified here is same as the one specified in SyncLite Consolidator"/></td>
					</tr>
			
					<tr>
						<td>DB Base Path</td>
						<td><input type="text" size=30 id="basePath"
							name="basePath"
							value="<%=escHtml(basePath)%>"
							title="Specify a path to a directory which will hold all the created device/database files."/></td>
					</tr>
					
					<tr>
						<td>Number of Databases</td>
						<td><input type="text" id="numDevices"
							name="numDevices"
							value="<%=escHtml(String.valueOf(numDevices))%>"
							title="Specify number of devices to be created/initialized. Please note that a numeric index ( starting from 1 to numDevices) will be designated as a device name to each device. In your own real applications, you can specify your own device names through properties file or SyncLite API."/></td>
					</tr>
					
					<tr>
						<td>Database Type</td>
						<td><select id="deviceType" name="deviceType" title="Select database/device type. BASE types capture SQL DDL and DML. STORE types keep a local data copy in addition to staged logs.">
								<%
								if (deviceType.equals("SQLITE")) {
									out.println("<option value=\"SQLITE\" selected>SQLite</option>");
								} else {
									out.println("<option value=\"SQLITE\">SQLite</option>");
								}
								if (deviceType.equals("SQLITE_STORE")) {
									out.println("<option value=\"SQLITE_STORE\" selected>SQLite Store</option>");
								} else {
									out.println("<option value=\"SQLITE_STORE\">SQLite Store</option>");
								}
								if (deviceType.equals("DUCKDB")) {
									out.println("<option value=\"DUCKDB\" selected>DuckDB</option>");
								} else {
									out.println("<option value=\"DUCKDB\">DuckDB</option>");
								}
								if (deviceType.equals("DUCKDB_STORE")) {
									out.println("<option value=\"DUCKDB_STORE\" selected>DuckDB Store</option>");
								} else {
									out.println("<option value=\"DUCKDB_STORE\">DuckDB Store</option>");
								}
								if (deviceType.equals("DERBY")) {
									out.println("<option value=\"DERBY\" selected>Apache Derby</option>");
								} else {
									out.println("<option value=\"DERBY\">Apache Derby</option>");
								}
								if (deviceType.equals("DERBY_STORE")) {
									out.println("<option value=\"DERBY_STORE\" selected>Apache Derby Store</option>");
								} else {
									out.println("<option value=\"DERBY_STORE\">Apache Derby Store</option>");
								}
								if (deviceType.equals("H2")) {
									out.println("<option value=\"H2\" selected>H2</option>");
								} else {
									out.println("<option value=\"H2\">H2</option>");
								}
								if (deviceType.equals("H2_STORE")) {
									out.println("<option value=\"H2_STORE\" selected>H2 Store</option>");
								} else {
									out.println("<option value=\"H2_STORE\">H2 Store</option>");
								}
								if (deviceType.equals("HYPERSQL")) {
									out.println("<option value=\"HYPERSQL\" selected>HyperSQL</option>");
								} else {
									out.println("<option value=\"HYPERSQL\">HyperSQL</option>");
								}
								if (deviceType.equals("HYPERSQL_STORE")) {
									out.println("<option value=\"HYPERSQL_STORE\" selected>HyperSQL Store</option>");
								} else {
									out.println("<option value=\"HYPERSQL_STORE\">HyperSQL Store</option>");
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
						<td>Consolidator Type</td>
						<td><select id="consolidatorType" name="consolidatorType" onchange="toggleEmbeddedFields()" title="Standalone runs the SyncLite Consolidator as a separate process. Embedded runs the in-process consolidator; you must provide destination details below.">
								<%
								if (consolidatorType.equals("STANDALONE")) {
									out.println("<option value=\"STANDALONE\" selected>Standalone</option>");
								} else {
									out.println("<option value=\"STANDALONE\">Standalone</option>");
								}
								if (consolidatorType.equals("EMBEDDED")) {
									out.println("<option value=\"EMBEDDED\" selected>Embedded</option>");
								} else {
									out.println("<option value=\"EMBEDDED\">Embedded</option>");
								}
								%>
						</select></td>
					</tr>
					<tr class="embedded-field">
						<td>Destinations</td>
						<td>
							<input type="hidden" id="num-destinations" name="num-destinations" value="<%=numDestinations%>" />
							<div id="destinationList" aria-live="polite"></div>
							<div class="destination-actions">
								<button type="button" id="addDestinationButton">Add Destination</button>
								<span>Destinations are initialized in the order shown.</span>
							</div>
						</td>
					</tr>
					<tr>
						<td>Device Configuration Manager</td>
						<td><textarea name="props" id="props" rows="25" cols="100" title="Edit SyncLite device configuration. The content is saved as synclite.conf in the selected base path and used to initialize all created devices. Defaults include local stage and destination settings."><%=escHtml(props)%></textarea>
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
