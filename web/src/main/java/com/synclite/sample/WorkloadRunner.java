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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	private static final Logger LOGGER = Logger.getLogger(WorkloadRunner.class.getName());
	private static final int MAX_DEVICES = 200;
	private static final int MAX_WORKLOAD_LENGTH = 200_000;
       
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
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ExecutorService fixedPoolExecutor = null;
		String workload = "";
		try {
			String basePath = "";
			if (request.getSession().getAttribute("basePath") != null) {
				basePath = request.getSession().getAttribute("basePath").toString().trim();
			}

			if (basePath.equals("")) {
				throw new ServletException("Devices not Created/Initialized yet");
			}

			int numDevices = 1;
			if (request.getSession().getAttribute("numDevices") != null) {
				numDevices = (Integer) request.getSession().getAttribute("numDevices");
			}
			if (numDevices <= 0 || numDevices > MAX_DEVICES) {
				throw new ServletException("Invalid number of initialized devices in session");
			}

			String deviceType = "SQLITE";
			if (request.getSession().getAttribute("deviceType") != null) {
				deviceType = request.getSession().getAttribute("deviceType").toString();
			}
			if (deviceType == null || deviceType.trim().isEmpty()) {
				throw new ServletException("Device type not found in session");
			}

			if (request.getParameter("workload") != null) {
				workload =  request.getParameter("workload").trim();
			} 
			if (workload.length() > MAX_WORKLOAD_LENGTH) {
				throw new ServletException("Workload is too large");
			}

			int startDeviceIdx = 1;
			if (request.getParameter("startDeviceIdx") != null) {
				startDeviceIdx = SecurityUtil.getRequiredPositiveInt(request, "startDeviceIdx", numDevices);
			} 

			int endDeviceIdx = 1;
			if (request.getParameter("endDeviceIdx") != null) {
				endDeviceIdx = SecurityUtil.getRequiredPositiveInt(request, "endDeviceIdx", numDevices);
			} 
			if (startDeviceIdx > endDeviceIdx) {
				throw new ServletException("Start Database Index must be <= End Database Index");
			}

			if (workload.equals("")) {
				throw new ServletException("Please specify workload for execution");
			}

	        fixedPoolExecutor = Executors.newFixedThreadPool(numDevices);
			List<Future<Void>> futureList = new ArrayList<>();

			long startTime = System.currentTimeMillis();
			if (deviceType.equals("STREAMING")) {
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
			} else if (deviceType.equals("SQLITE_STORE") || deviceType.equals("SQLOTE_STORE")) {
				Class.forName("io.synclite.logger.SQLiteAppender");
				for (Integer i = startDeviceIdx; i <= endDeviceIdx; ++i) {
					final Integer deviceIdx = i;
					final String finalBasePath = basePath;
					final String finalWorkload = workload;
					Future<Void> future = fixedPoolExecutor.submit(() -> runDMLsSQLiteAppender(deviceIdx, finalBasePath, finalWorkload));
					futureList.add(future);
				}
			} else if (deviceType.equals("DUCKDB_STORE")) {
				Class.forName("io.synclite.logger.DuckDBAppender");
				for (Integer i = startDeviceIdx; i <= endDeviceIdx; ++i) {
					final Integer deviceIdx = i;
					final String finalBasePath = basePath;
					final String finalWorkload = workload;
					Future<Void> future = fixedPoolExecutor.submit(() -> runDMLsDuckDBAppender(deviceIdx, finalBasePath, finalWorkload));
					futureList.add(future);
				}
			} else if (deviceType.equals("DERBY_STORE")) {
				Class.forName("io.synclite.logger.DerbyAppender");
				for (Integer i = startDeviceIdx; i <= endDeviceIdx; ++i) {
					final Integer deviceIdx = i;
					final String finalBasePath = basePath;
					final String finalWorkload = workload;
					Future<Void> future = fixedPoolExecutor.submit(() -> runDMLsDerbyAppender(deviceIdx, finalBasePath, finalWorkload));
					futureList.add(future);
				}
			} else if (deviceType.equals("H2_STORE")) {
				Class.forName("io.synclite.logger.H2Appender");
				for (Integer i = startDeviceIdx; i <= endDeviceIdx; ++i) {
					final Integer deviceIdx = i;
					final String finalBasePath = basePath;
					final String finalWorkload = workload;
					Future<Void> future = fixedPoolExecutor.submit(() -> runDMLsH2Appender(deviceIdx, finalBasePath, finalWorkload));
					futureList.add(future);
				}
			} else if (deviceType.equals("HYPERSQL_STORE")) {
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

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.log(Level.WARNING, "Workload execution interrupted", e);
			request.getRequestDispatcher("runDMLs.jsp?runStatus=FAIL&runStatusDetails="
					+ SecurityUtil.encodeUrlParam("Workload execution interrupted")
					+ ";&workload=" + SecurityUtil.encodeUrlParam(workload)).forward(request, response);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Workload execution failed", e);
			String errorMsg = SecurityUtil.sanitizeErrorMessage(e);
			request.getRequestDispatcher("runDMLs.jsp?runStatus=FAIL&runStatusDetails="
					+ SecurityUtil.encodeUrlParam(errorMsg)
					+ ";&workload=" + SecurityUtil.encodeUrlParam(workload)).forward(request, response);
		} finally {
			if (fixedPoolExecutor != null) {
				fixedPoolExecutor.shutdown();
				try {
					if (!fixedPoolExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
						fixedPoolExecutor.shutdownNow();
					}
				} catch (InterruptedException e) {
					fixedPoolExecutor.shutdownNow();
					Thread.currentThread().interrupt();
				}
			}
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
