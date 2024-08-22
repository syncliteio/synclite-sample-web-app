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

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
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
 * Servlet implementation class InitDevices
 */
@WebServlet("/deviceCreator")
public class DeviceCreator extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DeviceCreator() {
        super();
        // TODO Auto-generated constructor stub
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
		try {
			String basePathVal = request.getSession().getAttribute("basePath").toString();
			Path basePath;
			if ((basePathVal== null) || basePathVal.trim().isEmpty()) {
				throw new ServletException("\"DB Base Path\" must be specified");
			} else {
				basePath = Path.of(basePathVal);				
				if (! Files.exists(basePath)) {
					Files.createDirectories(basePath);
				}

				if (! Files.exists(basePath)) {
					throw new ServletException("Specified \"DB Base Path\" : " + basePath + " does not exist, please specify a valid \"BasePath\"");
				}
			}

			String props = request.getParameter("props");
			if ((props == null) || props.trim().isEmpty()) {
				throw new ServletException("\"Device Configurations\" must be specified");
			}

			String deviceType = request.getParameter("deviceType");
			if ((deviceType == null)) {
				throw new ServletException("\"Device Type\" must be specified");
			}

			Integer numDevices = Integer.valueOf(request.getParameter("numDevices"));
			if ((numDevices == null)) {
				throw new ServletException("\"Num Devices\" must be specified");
			}

			//Save the contents of props into base_path/synclite.props file
			
			String propsPath = Path.of(basePath.toString(), "synclite_logger.conf").toString();

			FileWriter propsWriter = null;
			try {
				propsWriter = new FileWriter(propsPath);
				propsWriter.write(props);
				propsWriter.close();
			} catch (Exception e) {
				if (propsWriter != null) {
					propsWriter.close();
				}
				throw e;
			}

			//Class.forName("org.sqlite.JDBC");
				
	        ExecutorService fixedPoolExecutor = Executors.newFixedThreadPool(numDevices);
			List<Future<Void>> futureList = new ArrayList<>();

			if (deviceType.equals("TELEMETRY")) {
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initTelemetryDevice(deviceIdx, basePath, propsPath));
					futureList.add(future);
				}
			} if (deviceType.equals("STREAMING")) {
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initStreamingDevice(deviceIdx, basePath, propsPath));
					futureList.add(future);
				}
			} else if (deviceType.equals("SQLITE")){
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initSQLiteDevice(deviceIdx, basePath, propsPath));
					futureList.add(future);
				}				
			} else if (deviceType.equals("DUCKDB")){
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initDuckDBDevice(deviceIdx, basePath, propsPath));
					futureList.add(future);
				}
			} else if (deviceType.equals("DERBY")){
					for (int i = 1; i <= numDevices; ++i) {
						final int deviceIdx = i;
						Future<Void> future = fixedPoolExecutor.submit(() -> initDerbyDevice(deviceIdx, basePath, propsPath));
						futureList.add(future);
					}	
			} else if (deviceType.equals("H2")){
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initH2Device(deviceIdx, basePath, propsPath));
					futureList.add(future);
				}	
			} else if (deviceType.equals("HYPERSQL")){
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initHyperSQLDevice(deviceIdx, basePath, propsPath));
					futureList.add(future);
				}	
			} else if (deviceType.equals("SQLITE_APPENDER")) {
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initSQLiteAppenderDevice(deviceIdx, basePath, propsPath));
					futureList.add(future);
				}
			} else if (deviceType.equals("DUCKDB_APPENDER")) {
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initDuckDBAppenderDevice(deviceIdx, basePath, propsPath));
					futureList.add(future);
			 	}
			} else if (deviceType.equals("DERBY_APPENDER")) {
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initDerbyAppenderDevice(deviceIdx, basePath, propsPath));
					futureList.add(future);
				}
			} else if (deviceType.equals("H2_APPENDER")) {
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initH2AppenderDevice(deviceIdx, basePath, propsPath));
					futureList.add(future);
				}
			} else if (deviceType.equals("HYPERSQL_APPENDER")) {
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initHyperSQLAppenderDevice(deviceIdx, basePath, propsPath));
					futureList.add(future);
				}
			}
			
			for (Future<Void> future : futureList) {
				future.get();
			}
			
			request.getSession().setAttribute("deviceType", deviceType);
			request.getSession().setAttribute("numDevices", numDevices);
			
			request.getRequestDispatcher("createDevices.jsp?emulateStatus=SUCCESS&emulateStatusDetails=;").forward(request, response);
		} catch (Exception e) {
		//		request.setAttribute("saveStatus", "FAIL");
			System.out.println("exception : " + e);
			String errorMsg = e.getMessage();
			request.getRequestDispatcher("createDevices.jsp?emulateStatus=FAIL&emulateStatusDetails=" + errorMsg + ";").forward(request, response);
	}

	}

	private Void initTelemetryDevice(int i, Path basePath, String propsPath) throws Exception {
		try {
			Class.forName("io.synclite.logger.Telemetry");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			Telemetry.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			return null;
		} catch (Exception e) {
			throw e;
		}
	} 

	private Void initStreamingDevice(int i, Path basePath, String propsPath) throws Exception {
		try {
			Class.forName("io.synclite.logger.Streaming");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			Streaming.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			return null;
		} catch (Exception e) {
			throw e;
		}
	} 

	private Void initSQLiteDevice(int i, Path basePath, String propsPath) throws Exception {
		try {
			Class.forName("io.synclite.logger.SQLite");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			SQLite.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			return null;
		} catch (Exception e) {
			throw e;
		}
	} 

	private Void initSQLiteAppenderDevice(int i, Path basePath, String propsPath) throws Exception {
		try {
			Class.forName("io.synclite.logger.SQLiteAppender");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			SQLiteAppender.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			return null;
		} catch (Exception e) {
			throw e;
		}
	} 

	private Void initDuckDBDevice(int i, Path basePath, String propsPath) throws Exception {
		try {
			Class.forName("io.synclite.logger.DuckDB");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			DuckDB.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			return null;
		} catch (Exception e) {
			throw e;
		}
	}

	private Void initDuckDBAppenderDevice(int i, Path basePath, String propsPath) throws Exception {
		try {
			Class.forName("io.synclite.logger.DuckDBAppender");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			DuckDBAppender.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			return null;
		} catch (Exception e) {
			throw e;
		}
	}

	private Void initDerbyDevice(int i, Path basePath, String propsPath) throws Exception {
		try {
			Class.forName("io.synclite.logger.Derby");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			Derby.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			return null;
		} catch (Exception e) {
			throw e;
		}
	}

	private Void initDerbyAppenderDevice(int i, Path basePath, String propsPath) throws Exception {
		try {
			Class.forName("io.synclite.logger.DerbyAppender");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			DerbyAppender.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			return null;
		} catch (Exception e) {
			throw e;
		}
	}

	private Void initH2Device(int i, Path basePath, String propsPath) throws Exception {
		try {
			Class.forName("io.synclite.logger.H2");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			H2.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			return null;
		} catch (Exception e) {
			throw e;
		}
	}

	private Void initH2AppenderDevice(int i, Path basePath, String propsPath) throws Exception {
		try {
			Class.forName("io.synclite.logger.H2Appender");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			H2Appender.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			return null;
		} catch (Exception e) {
			throw e;
		}
	}

	private Void initHyperSQLDevice(int i, Path basePath, String propsPath) throws Exception {
		try {
			Class.forName("io.synclite.logger.HyperSQL");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			HyperSQL.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			return null;
		} catch (Exception e) {
			throw e;
		}
	}

	private Void initHyperSQLAppenderDevice(int i, Path basePath, String propsPath) throws Exception {
		try {
			Class.forName("io.synclite.logger.HyperSQLAppender");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			HyperSQLAppender.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			return null;
		} catch (Exception e) {
			throw e;
		}
	}
}

