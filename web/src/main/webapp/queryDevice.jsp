<%@page import="java.util.Properties"%>
<%@page import="java.nio.file.Files"%>
<%@page import="java.time.Instant"%>
<%@page import="java.nio.file.Path"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.FileReader"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.io.FileWriter"%>
<%@page import="javax.websocket.Session"%>
<%@page import="org.sqlite.*"%>
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
<title>Run Queries on SyncLite Device</title>
</head>
<%
String basePath = "";
if (session.getAttribute("basePath") != null) {
	basePath = session.getAttribute("basePath").toString();
}

String deviceType = "SQLITE"; 
if (session.getAttribute("deviceType") != null) {
	deviceType = session.getAttribute("deviceType").toString();
}

Integer deviceIdx = 1;
if (request.getParameter("deviceIdx") != null) {
	deviceIdx = Integer.valueOf(request.getParameter("deviceIdx"));
}

String workload = "";
if (request.getParameter("workload") != null) {
	workload = request.getParameter("workload");
}

String runStatus = "";
String runStatusDetails = "";

if (basePath.equals("")) {
	runStatus = "FAILED";
	runStatusDetails ="Please create/initialize the databases first";	
}

String sampleSQL = "SELECT * FROM t1";
%>
<body>
	<%@include file="html/menu.html"%>

	<div class="main">
		<h2>SyncLite Sample App : Query Device</h2>
		<%
		//out.println("status : " + runStatus);
		if (runStatus != null) {
			if (runStatus.equals("SUCCESS")) {
				out.println("<h4 style=\"color: blue;\"> Successfully executed query on specified database </h4>");
			} else if (runStatus.equals("FAILED")) {
				out.println("<h4 style=\"color: red;\"> Query execution failed with error : "
				+ runStatusDetails.replace("<", "&lt;").replace(">", "&gt;") + "</h4>");
			}
		}
		%>

		<form method="post">
			<table>
				<tbody>
					<tr></tr>
					<tr>
						<td>Database Path</td>
						<td><input type="text" id="basePath" name="basePath"
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
						<td>Database Index</td>
						<td><input type="text" id="deviceIdx"
							name="deviceIdx" value="<%=deviceIdx%>"
							title="Specify the index of the database/device to execute a query."/></td>
					</tr>

					<tr>
						<td>Query</td>
						<td><textarea name="workload" id="workload" rows="10" cols="100" placeholder="<%=sampleSQL%>" style="color:blue"  title="Specify SQL query to execute on the specified database/device."><%=workload%></textarea>
						</td>
					</tr>

					<tr>
						<td>Result</td>
						<td>
							<%
							long rowCnt = 0;
							if (!workload.trim().isEmpty()) {
								Path dbPath = Path.of(basePath, String.valueOf(deviceIdx));
								String url = "jdbc:sqlite:" + dbPath;
								Properties props = new Properties();
								if (deviceType.equals("DUCKDB") || deviceType.equals("DUCKDB_APPENDER")) {
									url = "jdbc:duckdb:" + dbPath;
									Class.forName("org.duckdb.DuckDBDriver");
									props.setProperty("duckdb.read_only", "true");
								} else if (deviceType.equals("DERBY") || deviceType.equals("DERBY_APPENDER")){
									url = "jdbc:derby:" + dbPath;
									Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
								} else if (deviceType.equals("H2") || deviceType.equals("H2_APPENDER")){
									url = "jdbc:h2:" + dbPath;
									Class.forName("org.h2.Driver");
								} else if (deviceType.equals("HYPERSQL") || deviceType.equals("HYPERSQL_APPENDER")){
									url = "jdbc:hsqldb:" + dbPath;
							        Class.forName("org.hsqldb.jdbc.JDBCDriver");
								} else {
									Class.forName("org.sqlite.JDBC");
								}
								try (Connection conn = DriverManager.getConnection(url, props)) {
									try (Statement stmt = conn.createStatement()) {
										try (ResultSet rs = stmt.executeQuery(workload)) {
											
											if (rs != null) {												

												ResultSetMetaData rsMetadata = rs.getMetaData();
												int colCount = rsMetadata.getColumnCount();
												out.println("<div class=\"container\">");

												out.println("<table>");
												out.println("<tbody>");
												  
												out.println("<tr>");
												for (int j = 1; j <= colCount; ++j) {
													String colDisplayName = rsMetadata.getColumnName(j);
													out.println("<th>");
													out.print(colDisplayName);
													out.println("</th>");
												}
												out.println("</tr>");

												while (rs.next()) {
													out.println("<tr>");
													for (int k = 1; k <= colCount; ++k) {
														out.println("<td>");
														out.println(rs.getString(k));
														out.println("</td>");
													}
													out.println("</tr>");
													++rowCnt;
												}
												out.println("</tbody>");
												out.println("</table>");
												out.println("</div>");
											}
										}
									} 
								} catch (Exception e) {
									runStatus = "FAILED";
									runStatusDetails = "Query execution failed with exception : " + e.getMessage();
								}							
							}
							%>
						</td>
					</tr>
					<%
						if (rowCnt > 0) {
							out.println("<tr>");
							out.println("<td></td>");							
							out.println("<td>");
							out.println(rowCnt + " rows");
							out.println("</td>");
							out.println("</tr>");
						}
						if (runStatus.equals("FAILED")) {
							out.println("<tr>");
							out.println("<td></td>");							
							out.println("<td>");
							out.println(runStatusDetails);
							out.println("</td>");
							out.println("</tr>");
							
						}
					%>
				</tbody>
			</table>
			<center>
				<input type="submit" id="run" name="run" value="Query">
			</center>
		</form>
	</div>
</body>
</html>