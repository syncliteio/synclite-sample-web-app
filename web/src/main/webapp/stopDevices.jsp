<%@page import="io.synclite.logger.*"%>

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
<title>Stop SyncLite Devices</title>
</head>
<body>
	<%@include file="html/menu.html"%>	
	<div class="main">
		<%
		String basePath = "";
		if (session.getAttribute("basePath") != null) {
			basePath = session.getAttribute("basePath").toString();
		}

		String closeStatus = "";
		if (request.getParameter("closeStatus") != null) {
			closeStatus= request.getParameter("closeStatus");
		}

		String closeStatusDetails = "";
		if (request.getParameter("closeStatusDetails") != null) {
			closeStatusDetails = request.getParameter("closeStatusDetails");
		}

		if (closeStatus != null) {
			if (closeStatus.equals("SUCCESS")) {
				out.println("<h4 style=\"color: blue;\"> Successfully closed all SyncLite devices </h4>");
			} else if (closeStatus.equals("FAILED")) {
				out.println("<h4 style=\"color: red;\"> Device close failed with error : "
				+ closeStatusDetails.replace("<", "&lt;").replace(">", "&gt;") + "</h4>");
			}
		}		

		%>		
		<form action="${pageContext.request.contextPath}/deviceCloser"
				method="post">
				<%
				if (basePath.equals("")) {		
					out.println("<h4 style=\"color: red;\"> Devices not initialized yet</h4>");		
				} else {
					out.println("<center>");
					out.println("<input type=\"submit\" id=\"close\" name=\"close\" value=\"Stop All Databases\">");
					out.println("</center>");
				}
				%>
			
		</form>
	</div>
</body>	

</html>