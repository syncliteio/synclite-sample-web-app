package com.synclite.sample;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet implementation class ValidateJobConfiguration
 */
@WebServlet("/validateDBDirectory")
public class ValidateDBDirectory extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	/**
	 * Default constructor. 
	 */
	public ValidateDBDirectory() {
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	/**	  
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String jobName = request.getParameter("jobName");

			if (jobName != null) {
				//Check if specified jobName is in correct format
				if (jobName.length() > 16 ) {
					throw new ServletException("Job name must be upto 16 characters in length");
				}
				if (!jobName.matches("[a-zA-Z0-9-_]+")) {
					throw new ServletException("Specified job name is invalid. Allowed characters are alphanumeric characters, hyphens or underscores.");
				}		
			} else {
				jobName = "job1";
			}

			String basePath = request.getParameter("basePath").toString();
			Path basePathDir;
			if ((basePath== null) || basePath.trim().isEmpty()) {
				throw new ServletException("\"SyncLite Device Directory Path\" must be specified");
			} else {
				basePathDir = Path.of(basePath);
				if (! Files.exists(basePathDir)) {
					try {
						Files.createDirectories(basePathDir);
					} catch (Exception e) {
						throw new ServletException("Failed to create database directory : " + basePath + " : " + e.getMessage(), e);
					}
				}
				if (! Files.exists(basePathDir)) {
					throw new ServletException("Specified \"DB Base Path\" : " + basePathDir + " does not exist, please specify a valid path.");
				}
			}
			
			if (! basePathDir.toFile().canRead()) {
				throw new ServletException("Specified \"DB Base Path\" does not have read permission");
			}

			if (! basePathDir.toFile().canWrite()) {
				throw new ServletException("Specified \"DB Base Path\" does not have write permission");
			}


			request.getSession().setAttribute("jobName", jobName);
			request.getSession().setAttribute("basePath", basePath);

			response.sendRedirect("createDevices.jsp");
		} catch (Exception e) {
			//System.out.println("exception : " + e);
			String errorMsg = e.getMessage();
			request.getRequestDispatcher("selectDBDirectory.jsp?errorMsg=" + errorMsg).forward(request, response);
			throw new ServletException(e);
		}
	}

}


