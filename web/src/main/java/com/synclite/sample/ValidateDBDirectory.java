/*
 * Copyright (c) 2024 mahendra.chavan@synclite.io, all rights reserved.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.  See the License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package com.synclite.sample;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Servlet implementation class ValidateJobConfiguration
 */
@WebServlet("/validateDBDirectory")
public class ValidateDBDirectory extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(ValidateDBDirectory.class.getName());
	private static final int MAX_JOB_NAME_LENGTH = 16;
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
				jobName = jobName.trim();
				//Check if specified jobName is in correct format
				if (jobName.length() > MAX_JOB_NAME_LENGTH ) {
					throw new ServletException("Job name must be upto 16 characters in length");
				}
				if (!jobName.matches("[a-zA-Z0-9-_]+")) {
					throw new ServletException("Specified job name is invalid. Allowed characters are alphanumeric characters, hyphens or underscores.");
				}		
			} else {
				jobName = "job1";
			}

			String basePath = request.getParameter("basePath");
			if ((basePath== null) || basePath.trim().isEmpty()) {
				throw new ServletException("\"SyncLite Device Directory Path\" must be specified");
			}
			Path basePathDir = SecurityUtil.normalizePath(basePath);
			if (!Files.exists(basePathDir)) {
				Files.createDirectories(basePathDir);
			}
			if (!Files.isDirectory(basePathDir)) {
				throw new ServletException("Specified \"DB Base Path\" must be a directory");
			}
			
			if (! basePathDir.toFile().canRead()) {
				throw new ServletException("Specified \"DB Base Path\" does not have read permission");
			}

			if (! basePathDir.toFile().canWrite()) {
				throw new ServletException("Specified \"DB Base Path\" does not have write permission");
			}


			request.getSession().setAttribute("jobName", jobName);
			request.getSession().setAttribute("basePath", basePathDir.toString());
			request.changeSessionId();

			response.sendRedirect("createDevices.jsp");
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Failed to validate DB directory", e);
			String errorMsg = SecurityUtil.sanitizeErrorMessage(e);
			request.getRequestDispatcher("selectDBDirectory.jsp?errorMsg=" + SecurityUtil.encodeUrlParam(errorMsg)).forward(request, response);
		}
	}

}


