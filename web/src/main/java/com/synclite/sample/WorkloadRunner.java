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
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.synclite.logger.*;

/**
 * Servlet implementation class WorkloadRunner
 */
@WebServlet("/workloadRunner")
public class WorkloadRunner extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WorkloadRunner() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String workload = "";
		try {
			doGet(request, response);
			String basePath = "";
			if (request.getSession().getAttribute("basePath") != null) {
				basePath = request.getSession().getAttribute("basePath").toString().trim();
			}

			if (basePath.equals("")) {
				throw new ServletException("Devices not Created/Initialized yet");
			}

			Integer numDevices = 1;
			if (request.getSession().getAttribute("numDevices") != null) {
				numDevices = (Integer) request.getSession().getAttribute("numDevices");
			}

			String deviceType = "SQLITE";
			if (request.getSession().getAttribute("deviceType") != null) {
				deviceType = request.getSession().getAttribute("deviceType").toString();
			}

			if (request.getParameter("workload") != null) {
				workload =  request.getParameter("workload").trim();
			} 

			Integer startDeviceIdx = 1;
			if (request.getParameter("startDeviceIdx") != null) {
				startDeviceIdx =  Integer.valueOf(request.getParameter("startDeviceIdx"));
			} 

			Integer endDeviceIdx = 1;
			if (request.getParameter("endDeviceIdx") != null) {
				endDeviceIdx =  Integer.valueOf(request.getParameter("endDeviceIdx"));
			} 

			if (workload.equals("")) {
				throw new ServletException("Please specify workload for execution");
			}

	        ExecutorService fixedPoolExecutor = Executors.newFixedThreadPool(numDevices);
			List<Future<Void>> futureList = new ArrayList<>();

			long startTime = System.currentTimeMillis();
			if (deviceType.equals("TELEMETRY")) {
				Class.forName("io.synclite.logger.Telemetry");
				for (Integer i = startDeviceIdx; i <= endDeviceIdx; ++i) {
					final Integer deviceIdx = i;
					final String finalBasePath = basePath;
					final String finalWorkload = workload;
					Future<Void> future = fixedPoolExecutor.submit(() -> runDMLsTelemetry(deviceIdx, finalBasePath, finalWorkload));
					futureList.add(future);
				}
			} else if (deviceType.equals("STREAMING")) {
				Class.forName("io.synclite.logger.Streaming");
				for (Integer i = startDeviceIdx; i <= endDeviceIdx; ++i) {
					final Integer deviceIdx = i;
					final String finalBasePath = basePath;
					final String finalWorkload = workload;
					Future<Void> future = fixedPoolExecutor.submit(() -> runDMLsStreaming(deviceIdx, finalBasePath, finalWorkload));
					futureList.add(future);
				}
			} else if (deviceType.equals("SQLITE")){
				Class.forName("io.synclite.logger.SQLite");
				for (Integer i = startDeviceIdx; i <= endDeviceIdx ; ++i) {
					final Integer deviceIdx = i;
					final String finalBasePath = basePath;
					final String finalWorkload = workload;
					Future<Void> future = fixedPoolExecutor.submit(() -> runDMLsSQLite(deviceIdx, finalBasePath, finalWorkload));
					futureList.add(future);
				}
			} else if (deviceType.equals("DUCKDB")){
				Class.forName("io.synclite.logger.DuckDB");
				for (Integer i = startDeviceIdx; i <= endDeviceIdx ; ++i) {
					final Integer deviceIdx = i;
					final String finalBasePath = basePath;
					final String finalWorkload = workload;
					Future<Void> future = fixedPoolExecutor.submit(() -> runDMLsDuckDB(deviceIdx, finalBasePath, finalWorkload));
					futureList.add(future);
				}
			} else if (deviceType.equals("DERBY")){
				Class.forName("io.synclite.logger.Derby");
				for (Integer i = startDeviceIdx; i <= endDeviceIdx ; ++i) {
					final Integer deviceIdx = i;
					final String finalBasePath = basePath;
					final String finalWorkload = workload;
					Future<Void> future = fixedPoolExecutor.submit(() -> runDMLsDerby(deviceIdx, finalBasePath, finalWorkload));
					futureList.add(future);
				}
			} else if (deviceType.equals("H2")){
				Class.forName("io.synclite.logger.H2");
				for (Integer i = startDeviceIdx; i <= endDeviceIdx ; ++i) {
					final Integer deviceIdx = i;
					final String finalBasePath = basePath;
					final String finalWorkload = workload;
					Future<Void> future = fixedPoolExecutor.submit(() -> runDMLsH2(deviceIdx, finalBasePath, finalWorkload));
					futureList.add(future);
				}
			} else if (deviceType.equals("HYPERSQL")){
				Class.forName("io.synclite.logger.HyperSQL");
				for (Integer i = startDeviceIdx; i <= endDeviceIdx ; ++i) {
					final Integer deviceIdx = i;
					final String finalBasePath = basePath;
					final String finalWorkload = workload;
					Future<Void> future = fixedPoolExecutor.submit(() -> runDMLsHyperSQL(deviceIdx, finalBasePath, finalWorkload));
					futureList.add(future);
				}
			} else if (deviceType.equals("SQLITE_APPENDER")) {
				Class.forName("io.synclite.logger.SQLiteAppender");
				for (Integer i = startDeviceIdx; i <= endDeviceIdx; ++i) {
					final Integer deviceIdx = i;
					final String finalBasePath = basePath;
					final String finalWorkload = workload;
					Future<Void> future = fixedPoolExecutor.submit(() -> runDMLsSQLiteAppender(deviceIdx, finalBasePath, finalWorkload));
					futureList.add(future);
				}
			} else if (deviceType.equals("DUCKDB_APPENDER")) {
				Class.forName("io.synclite.logger.DuckDBAppender");
				for (Integer i = startDeviceIdx; i <= endDeviceIdx; ++i) {
					final Integer deviceIdx = i;
					final String finalBasePath = basePath;
					final String finalWorkload = workload;
					Future<Void> future = fixedPoolExecutor.submit(() -> runDMLsDuckDBAppender(deviceIdx, finalBasePath, finalWorkload));
					futureList.add(future);
				}
			} else if (deviceType.equals("DERBY_APPENDER")) {
				Class.forName("io.synclite.logger.DerbyAppender");
				for (Integer i = startDeviceIdx; i <= endDeviceIdx; ++i) {
					final Integer deviceIdx = i;
					final String finalBasePath = basePath;
					final String finalWorkload = workload;
					Future<Void> future = fixedPoolExecutor.submit(() -> runDMLsDerbyAppender(deviceIdx, finalBasePath, finalWorkload));
					futureList.add(future);
				}
			} else if (deviceType.equals("H2_APPENDER")) {
				Class.forName("io.synclite.logger.H2Appender");
				for (Integer i = startDeviceIdx; i <= endDeviceIdx; ++i) {
					final Integer deviceIdx = i;
					final String finalBasePath = basePath;
					final String finalWorkload = workload;
					Future<Void> future = fixedPoolExecutor.submit(() -> runDMLsH2Appender(deviceIdx, finalBasePath, finalWorkload));
					futureList.add(future);
				}
			} else if (deviceType.equals("HYPERSQL_APPENDER")) {
				Class.forName("io.synclite.logger.HyperSQLAppender");
				for (Integer i = startDeviceIdx; i <= endDeviceIdx; ++i) {
					final Integer deviceIdx = i;
					final String finalBasePath = basePath;
					final String finalWorkload = workload;
					Future<Void> future = fixedPoolExecutor.submit(() -> runDMLsHyperSQLAppender(deviceIdx, finalBasePath, finalWorkload));
					futureList.add(future);
				}
			}
			
			for (Future<Void> future : futureList) {
				future.get();
			}
		
			long finishTime = System.currentTimeMillis();

			long elapsedTime = finishTime - startTime;
			request.getRequestDispatcher("runDMLs.jsp?runStatus=SUCCESS&runStatusDetails=;&elapsedTime=" + elapsedTime).forward(request, response);

		} catch (Exception e) {
			String errorMsg = e.getMessage();
			request.getRequestDispatcher("runDMLs.jsp?runStatus=FAIL&runStatusDetails=" + errorMsg + ";" + "&workload=" + workload).forward(request, response);
		}
	}
	
	private Void runDMLsSQLite(Integer deviceIndex, String basePath, String workload) throws SQLException {
		Path devicePath = Path.of(basePath.toString(), String.valueOf(deviceIndex));
		String url = "jdbc:synclite_sqlite:" + devicePath;
		try (Connection conn = DriverManager.getConnection(url)) {
			try (Statement stmt = conn.createStatement()) {
				stmt.execute(workload);
			}	
		} 
		return null;
	}

	private Void runDMLsSQLiteAppender(Integer deviceIndex, String basePath, String workload) throws SQLException {
		Path devicePath = Path.of(basePath.toString(), String.valueOf(deviceIndex));
		String url = "jdbc:synclite_sqlite_appender:" + devicePath;
		try (Connection conn = DriverManager.getConnection(url)) {
			try (Statement stmt = conn.createStatement()) {
				stmt.execute(workload);
			}	
		} 
		return null;
	}

	private Void runDMLsDuckDB(Integer deviceIndex, String basePath, String workload) throws SQLException {
		Path devicePath = Path.of(basePath.toString(), String.valueOf(deviceIndex));
		String url = "jdbc:synclite_duckdb:" + devicePath;
		try (Connection conn = DriverManager.getConnection(url)) {
			try (Statement stmt = conn.createStatement()) {
				stmt.execute(workload);
			}	
		} 
		return null;
	}

	private Void runDMLsDuckDBAppender(Integer deviceIndex, String basePath, String workload) throws SQLException {
		Path devicePath = Path.of(basePath.toString(), String.valueOf(deviceIndex));
		String url = "jdbc:synclite_duckdb_appender:" + devicePath;
		try (Connection conn = DriverManager.getConnection(url)) {
			try (Statement stmt = conn.createStatement()) {
				stmt.execute(workload);
			}	
		} 
		return null;
	}

	private Void runDMLsDerby(Integer deviceIndex, String basePath, String workload) throws SQLException {
		Path devicePath = Path.of(basePath.toString(), String.valueOf(deviceIndex));
		String url = "jdbc:synclite_derby:" + devicePath;
		try (Connection conn = DriverManager.getConnection(url)) {
			try (Statement stmt = conn.createStatement()) {
				stmt.execute(workload);
			}	
		} 
		return null;
	}

	private Void runDMLsDerbyAppender(Integer deviceIndex, String basePath, String workload) throws SQLException {
		Path devicePath = Path.of(basePath.toString(), String.valueOf(deviceIndex));
		String url = "jdbc:synclite_derby_appender:" + devicePath;
		try (Connection conn = DriverManager.getConnection(url)) {
			try (Statement stmt = conn.createStatement()) {
				stmt.execute(workload);
			}	
		} 
		return null;
	}

	private Void runDMLsH2(Integer deviceIndex, String basePath, String workload) throws SQLException {
		Path devicePath = Path.of(basePath.toString(), String.valueOf(deviceIndex));
		String url = "jdbc:synclite_h2:" + devicePath;
		try (Connection conn = DriverManager.getConnection(url)) {
			try (Statement stmt = conn.createStatement()) {
				stmt.execute(workload);
			}	
		} 
		return null;
	}

	private Void runDMLsH2Appender(Integer deviceIndex, String basePath, String workload) throws SQLException {
		Path devicePath = Path.of(basePath.toString(), String.valueOf(deviceIndex));
		String url = "jdbc:synclite_h2_appender:" + devicePath;
		try (Connection conn = DriverManager.getConnection(url)) {
			try (Statement stmt = conn.createStatement()) {
				stmt.execute(workload);
			}	
		} 
		return null;
	}

	private Void runDMLsHyperSQL(Integer deviceIndex, String basePath, String workload) throws SQLException {
		Path devicePath = Path.of(basePath.toString(), String.valueOf(deviceIndex));
		String url = "jdbc:synclite_hsqldb:" + devicePath;
		try (Connection conn = DriverManager.getConnection(url)) {
			try (Statement stmt = conn.createStatement()) {
				stmt.execute(workload);
			}	
		} 
		return null;
	}

	private Void runDMLsHyperSQLAppender(Integer deviceIndex, String basePath, String workload) throws SQLException {
		Path devicePath = Path.of(basePath.toString(), String.valueOf(deviceIndex));
		String url = "jdbc:synclite_hsqldb_appender:" + devicePath;
		try (Connection conn = DriverManager.getConnection(url)) {
			try (Statement stmt = conn.createStatement()) {
				stmt.execute(workload);
			}	
		} 
		return null;
	}

	private Void runDMLsTelemetry(int deviceIndex, String basePath, String workload) throws SQLException {
		Path devicePath = Path.of(basePath.toString(), String.valueOf(deviceIndex));
		String url = "jdbc:synclite_telemetry:" + devicePath;
		try (Connection conn = DriverManager.getConnection(url)) {
			try (Statement stmt = conn.createStatement()) {
				stmt.execute(workload);
			}
		} 
		return null;
	}

	private Void runDMLsStreaming(int deviceIndex, String basePath, String workload) throws SQLException {
		Path devicePath = Path.of(basePath.toString(), String.valueOf(deviceIndex));
		String url = "jdbc:synclite_streaming:" + devicePath;
		try (Connection conn = DriverManager.getConnection(url)) {
			try (Statement stmt = conn.createStatement()) {
				stmt.execute(workload);
			}
		} 
		return null;
	}

}
