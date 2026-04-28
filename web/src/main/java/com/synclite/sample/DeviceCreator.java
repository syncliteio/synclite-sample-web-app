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

import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
 * Servlet implementation class InitDevices
 */
@WebServlet("/deviceCreator")
public class DeviceCreator extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(DeviceCreator.class.getName());
	private static final int MAX_DEVICES = 200;
       
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
		ExecutorService fixedPoolExecutor = null;
		try {
			Object basePathAttr = request.getSession().getAttribute("basePath");
			if (basePathAttr == null) {
				throw new ServletException("\"DB Base Path\" must be specified");
			}
			Path basePath = SecurityUtil.normalizePath(basePathAttr.toString());
			if (!Files.exists(basePath)) {
				Files.createDirectories(basePath);
			}
			if (!Files.isDirectory(basePath)) {
				throw new ServletException("Specified \"DB Base Path\" is invalid");
			}

			String props = SecurityUtil.getRequiredText(request, "props", 250_000);
			String deviceType = SecurityUtil.getValidatedDeviceType(request, "deviceType");
			int numDevices = SecurityUtil.getRequiredPositiveInt(request, "numDevices", MAX_DEVICES);

			//Save the contents of props into base_path/synclite.props file
			Path propsPath = basePath.resolve("synclite_logger.conf");
			Files.writeString(propsPath, props, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

			//Class.forName("org.sqlite.JDBC");
				
	        fixedPoolExecutor = Executors.newFixedThreadPool(numDevices);
			List<Future<Void>> futureList = new ArrayList<>();

			if (deviceType.equals("STREAMING")) {
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initStreamingDevice(deviceIdx, basePath, propsPath.toString()));
					futureList.add(future);
				}
			} else if (deviceType.equals("SQLITE")){
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initSQLiteDevice(deviceIdx, basePath, propsPath.toString()));
					futureList.add(future);
				}				
			} else if (deviceType.equals("DUCKDB")){
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initDuckDBDevice(deviceIdx, basePath, propsPath.toString()));
					futureList.add(future);
				}
			} else if (deviceType.equals("DERBY")){
					for (int i = 1; i <= numDevices; ++i) {
						final int deviceIdx = i;
						Future<Void> future = fixedPoolExecutor.submit(() -> initDerbyDevice(deviceIdx, basePath, propsPath.toString()));
						futureList.add(future);
					}	
			} else if (deviceType.equals("H2")){
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initH2Device(deviceIdx, basePath, propsPath.toString()));
					futureList.add(future);
				}	
			} else if (deviceType.equals("HYPERSQL")){
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initHyperSQLDevice(deviceIdx, basePath, propsPath.toString()));
					futureList.add(future);
				}	
			} else if (deviceType.equals("SQLITE_STORE")) {
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initSQLiteStoreDevice(deviceIdx, basePath, propsPath.toString()));
					futureList.add(future);
				}
			} else if (deviceType.equals("DUCKDB_STORE")) {
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initDuckDBStoreDevice(deviceIdx, basePath, propsPath.toString()));
					futureList.add(future);
			 	}
			} else if (deviceType.equals("DERBY_STORE")) {
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initDerbyStoreDevice(deviceIdx, basePath, propsPath.toString()));
					futureList.add(future);
				}
			} else if (deviceType.equals("H2_STORE")) {
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initH2StoreDevice(deviceIdx, basePath, propsPath.toString()));
					futureList.add(future);
				}
			} else if (deviceType.equals("HYPERSQL_STORE")) {
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initHyperSQLStoreDevice(deviceIdx, basePath, propsPath.toString()));
					futureList.add(future);
				}
			}
			
			for (Future<Void> future : futureList) {
				future.get();
			}
			
			request.getSession().setAttribute("deviceType", deviceType);
			request.getSession().setAttribute("numDevices", numDevices);
			
			request.getRequestDispatcher("createDevices.jsp?emulateStatus=SUCCESS&emulateStatusDetails=;").forward(request, response);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.log(Level.WARNING, "Device creation interrupted", e);
			request.getRequestDispatcher("createDevices.jsp?emulateStatus=FAIL&emulateStatusDetails="
					+ SecurityUtil.encodeUrlParam("Device creation interrupted") + ";").forward(request, response);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Failed to create devices", e);
			String errorMsg = SecurityUtil.sanitizeErrorMessage(e);
			request.getRequestDispatcher("createDevices.jsp?emulateStatus=FAIL&emulateStatusDetails="
					+ SecurityUtil.encodeUrlParam(errorMsg) + ";").forward(request, response);
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

	private Void initSQLiteStoreDevice(int i, Path basePath, String propsPath) throws Exception {
		try {
			Class.forName("io.synclite.logger.SQLiteStore");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			SQLiteStore.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
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

	private Void initDuckDBStoreDevice(int i, Path basePath, String propsPath) throws Exception {
		try {
			Class.forName("io.synclite.logger.DuckDBStore");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			DuckDBStore.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
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

	private Void initDerbyStoreDevice(int i, Path basePath, String propsPath) throws Exception {
		try {
			Class.forName("io.synclite.logger.DerbyStore");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			DerbyStore.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
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

	private Void initH2StoreDevice(int i, Path basePath, String propsPath) throws Exception {
		try {
			Class.forName("io.synclite.logger.H2Store");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			H2Store.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
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

	private Void initHyperSQLStoreDevice(int i, Path basePath, String propsPath) throws Exception {
		try {
			Class.forName("io.synclite.logger.HyperSQLStore");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			HyperSQLStore.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			return null;
		} catch (Exception e) {
			throw e;
		}
	}
}

