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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import io.synclite.*;

/**
 * Servlet implementation class InitDevices
 */
@WebServlet("/deviceCreator")
public class DeviceCreator extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(DeviceCreator.class.getName());
	private static final int MAX_DEVICES = 200;
	private static final int MAX_DESTINATIONS = 16;
       
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

			// Consolidator type: STANDALONE (default) runs the consolidator as a
			// separate process; EMBEDDED runs the in-process consolidator and
			// requires one or more ordered DestinationOptions.
			String consolidatorType = SecurityUtil.getValidatedConsolidatorType(request, "consolidatorType");
			final List<DestinationOptions> destinations =
					buildDestinationOptions(request, consolidatorType);

			//Save the contents of props into base_path/synclite.props file
			Path propsPath = basePath.resolve("synclite.conf");
			Files.writeString(propsPath, props, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

			//Class.forName("org.sqlite.JDBC");
				
	        fixedPoolExecutor = Executors.newFixedThreadPool(numDevices);
			List<Future<Void>> futureList = new ArrayList<>();

			if (deviceType.equals("STREAMING")) {
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initStreamingDevice(deviceIdx, basePath, propsPath.toString(), destinations));
					futureList.add(future);
				}
			} else if (deviceType.equals("SQLITE")){
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initSQLiteDevice(deviceIdx, basePath, propsPath.toString(), destinations));
					futureList.add(future);
				}				
			} else if (deviceType.equals("DUCKDB")){
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initDuckDBDevice(deviceIdx, basePath, propsPath.toString(), destinations));
					futureList.add(future);
				}
			} else if (deviceType.equals("DERBY")){
					for (int i = 1; i <= numDevices; ++i) {
						final int deviceIdx = i;
						Future<Void> future = fixedPoolExecutor.submit(() -> initDerbyDevice(deviceIdx, basePath, propsPath.toString(), destinations));
						futureList.add(future);
					}	
			} else if (deviceType.equals("H2")){
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initH2Device(deviceIdx, basePath, propsPath.toString(), destinations));
					futureList.add(future);
				}	
			} else if (deviceType.equals("HYPERSQL")){
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initHyperSQLDevice(deviceIdx, basePath, propsPath.toString(), destinations));
					futureList.add(future);
				}	
			} else if (deviceType.equals("SQLITE_STORE")) {
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initSQLiteStoreDevice(deviceIdx, basePath, propsPath.toString(), destinations));
					futureList.add(future);
				}
			} else if (deviceType.equals("DUCKDB_STORE")) {
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initDuckDBStoreDevice(deviceIdx, basePath, propsPath.toString(), destinations));
					futureList.add(future);
			 	}
			} else if (deviceType.equals("DERBY_STORE")) {
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initDerbyStoreDevice(deviceIdx, basePath, propsPath.toString(), destinations));
					futureList.add(future);
				}
			} else if (deviceType.equals("H2_STORE")) {
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initH2StoreDevice(deviceIdx, basePath, propsPath.toString(), destinations));
					futureList.add(future);
				}
			} else if (deviceType.equals("HYPERSQL_STORE")) {
				for (int i = 1; i <= numDevices; ++i) {
					final int deviceIdx = i;
					Future<Void> future = fixedPoolExecutor.submit(() -> initHyperSQLStoreDevice(deviceIdx, basePath, propsPath.toString(), destinations));
					futureList.add(future);
				}
			}
			
			for (Future<Void> future : futureList) {
				future.get();
			}
			
			request.getSession().setAttribute("deviceType", deviceType);
			request.getSession().setAttribute("numDevices", numDevices);
			request.getSession().setAttribute("consolidatorType", consolidatorType);
			if (!destinations.isEmpty()) {
				persistDestinationsInSession(request, destinations);
			}
			
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

	private List<DestinationOptions> buildDestinationOptions(HttpServletRequest request, String consolidatorType)
			throws ServletException {
		if (!"EMBEDDED".equals(consolidatorType)) {
			return List.of();
		}

		boolean indexed = request.getParameter("num-destinations") != null;
		int destinationCount = indexed
				? SecurityUtil.getRequiredPositiveInt(request, "num-destinations", MAX_DESTINATIONS)
				: 1;
		List<DestinationOptions> destinations = new ArrayList<>(destinationCount);
		Map<List<String>, Integer> destinationTargets = new HashMap<>();
		for (int index = 1; index <= destinationCount; ++index) {
			String dstTypeParam = indexed ? "dst-type-" + index : "dstType";
			String connectionStringParam = indexed
					? "dst-connection-string-" + index : "dstConnectionString";
			String databaseParam = indexed ? "dst-database-" + index : "dstDatabase";
			String schemaParam = indexed ? "dst-schema-" + index : "dstSchema";
			String syncModeParam = indexed ? "dst-sync-mode-" + index : "syncMode";
			String dstType = SecurityUtil.getValidatedDstType(request, dstTypeParam);
			String dstConnectionString = SecurityUtil.getRequiredText(
					request, connectionStringParam, 4096);
			String dstDatabase = SecurityUtil.getOptionalText(
					request, databaseParam, 1024);
			String dstSchema = SecurityUtil.getOptionalText(
					request, schemaParam, 1024);
			String syncMode = SecurityUtil.getValidatedSyncMode(request, syncModeParam);

			DestinationOptions.Builder builder = DestinationOptions.builder()
					.dstType(DstType.valueOf(dstType))
					.connectionString(dstConnectionString)
					.syncMode(DstSyncMode.valueOf(syncMode));
			if (dstDatabase != null) {
				builder.database(dstDatabase);
			}
			if (dstSchema != null) {
				builder.schema(dstSchema);
			}
			try {
				DestinationOptions destination = builder.build();
				List<String> target = List.of(
						destination.dstType().name(),
						destination.connectionString(),
						destination.database().orElse(""),
						destination.schema().orElse(""));
				Integer duplicateIndex = destinationTargets.putIfAbsent(target, index);
				if (duplicateIndex != null) {
					throw new ServletException("Destination " + index
							+ " targets the same database as destination " + duplicateIndex);
				}
				destinations.add(destination);
			} catch (IllegalArgumentException e) {
				throw new ServletException("Destination " + index + ": " + e.getMessage());
			}
		}
		return List.copyOf(destinations);
	}

	private void persistDestinationsInSession(
			HttpServletRequest request, List<DestinationOptions> destinations) {
		Object previousCount = request.getSession().getAttribute("num-destinations");
		int oldCount = 0;
		if (previousCount != null) {
			try {
				oldCount = Integer.parseInt(previousCount.toString());
			} catch (NumberFormatException ignored) {
				oldCount = 0;
			}
		}
		request.getSession().setAttribute("num-destinations", destinations.size());
		for (int offset = 0; offset < destinations.size(); ++offset) {
			int index = offset + 1;
			DestinationOptions destination = destinations.get(offset);
			request.getSession().setAttribute("dst-type-" + index, destination.dstType().name());
			request.getSession().setAttribute(
					"dst-connection-string-" + index, destination.connectionString());
			request.getSession().setAttribute(
					"dst-database-" + index, destination.database().orElse(""));
			request.getSession().setAttribute(
					"dst-schema-" + index, destination.schema().orElse(""));
			request.getSession().setAttribute("dst-sync-mode-" + index, destination.syncMode().name());
		}
		for (int index = destinations.size() + 1; index <= oldCount; ++index) {
			request.getSession().removeAttribute("dst-type-" + index);
			request.getSession().removeAttribute("dst-connection-string-" + index);
			request.getSession().removeAttribute("dst-database-" + index);
			request.getSession().removeAttribute("dst-schema-" + index);
			request.getSession().removeAttribute("dst-sync-mode-" + index);
		}

		// Keep the original first-destination session keys for seamless
		// upgrades from the previous single-destination form.
		DestinationOptions first = destinations.get(0);
		request.getSession().setAttribute("dstType", first.dstType().name());
		request.getSession().setAttribute("dstConnectionString", first.connectionString());
		request.getSession().setAttribute("dstDatabase", first.database().orElse(""));
		request.getSession().setAttribute("dstSchema", first.schema().orElse(""));
		request.getSession().setAttribute("syncMode", first.syncMode().name());
	}

	private Void initStreamingDevice(int i, Path basePath, String propsPath, List<DestinationOptions> destinations) throws Exception {
		try {
			Class.forName("io.synclite.Streaming");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			if (!destinations.isEmpty()) {
				Streaming.initialize(devicePath, Path.of(propsPath), String.valueOf(i), destinations);
			} else {
				Streaming.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			}
			return null;
		} catch (Exception e) {
			throw e;
		}
	} 

	private Void initSQLiteDevice(int i, Path basePath, String propsPath, List<DestinationOptions> destinations) throws Exception {
		try {
			Class.forName("io.synclite.SQLite");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			if (!destinations.isEmpty()) {
				SQLite.initialize(devicePath, Path.of(propsPath), String.valueOf(i), destinations);
			} else {
				SQLite.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			}
			return null;
		} catch (Exception e) {
			throw e;
		}
	} 

	private Void initSQLiteAppenderDevice(int i, Path basePath, String propsPath) throws Exception {
		try {
			Class.forName("io.synclite.SQLiteAppender");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			SQLiteAppender.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			return null;
		} catch (Exception e) {
			throw e;
		}
	}

	private Void initSQLiteStoreDevice(int i, Path basePath, String propsPath, List<DestinationOptions> destinations) throws Exception {
		try {
			Class.forName("io.synclite.SQLiteStore");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			if (!destinations.isEmpty()) {
				SQLiteStore.initialize(devicePath, Path.of(propsPath), String.valueOf(i), destinations);
			} else {
				SQLiteStore.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			}
			return null;
		} catch (Exception e) {
			throw e;
		}
	} 

	private Void initDuckDBDevice(int i, Path basePath, String propsPath, List<DestinationOptions> destinations) throws Exception {
		try {
			Class.forName("io.synclite.DuckDB");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			if (!destinations.isEmpty()) {
				DuckDB.initialize(devicePath, Path.of(propsPath), String.valueOf(i), destinations);
			} else {
				DuckDB.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			}
			return null;
		} catch (Exception e) {
			throw e;
		}
	}

	private Void initDuckDBAppenderDevice(int i, Path basePath, String propsPath) throws Exception {
		try {
			Class.forName("io.synclite.DuckDBAppender");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			DuckDBAppender.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			return null;
		} catch (Exception e) {
			throw e;
		}
	}

	private Void initDuckDBStoreDevice(int i, Path basePath, String propsPath, List<DestinationOptions> destinations) throws Exception {
		try {
			Class.forName("io.synclite.DuckDBStore");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			if (!destinations.isEmpty()) {
				DuckDBStore.initialize(devicePath, Path.of(propsPath), String.valueOf(i), destinations);
			} else {
				DuckDBStore.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			}
			return null;
		} catch (Exception e) {
			throw e;
		}
	}

	private Void initDerbyDevice(int i, Path basePath, String propsPath, List<DestinationOptions> destinations) throws Exception {
		try {
			Class.forName("io.synclite.Derby");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			if (!destinations.isEmpty()) {
				Derby.initialize(devicePath, Path.of(propsPath), String.valueOf(i), destinations);
			} else {
				Derby.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			}
			return null;
		} catch (Exception e) {
			throw e;
		}
	}

	private Void initDerbyAppenderDevice(int i, Path basePath, String propsPath) throws Exception {
		try {
			Class.forName("io.synclite.DerbyAppender");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			DerbyAppender.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			return null;
		} catch (Exception e) {
			throw e;
		}
	}

	private Void initDerbyStoreDevice(int i, Path basePath, String propsPath, List<DestinationOptions> destinations) throws Exception {
		try {
			Class.forName("io.synclite.DerbyStore");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			if (!destinations.isEmpty()) {
				DerbyStore.initialize(devicePath, Path.of(propsPath), String.valueOf(i), destinations);
			} else {
				DerbyStore.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			}
			return null;
		} catch (Exception e) {
			throw e;
		}
	}

	private Void initH2Device(int i, Path basePath, String propsPath, List<DestinationOptions> destinations) throws Exception {
		try {
			Class.forName("io.synclite.H2");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			if (!destinations.isEmpty()) {
				H2.initialize(devicePath, Path.of(propsPath), String.valueOf(i), destinations);
			} else {
				H2.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			}
			return null;
		} catch (Exception e) {
			throw e;
		}
	}

	private Void initH2AppenderDevice(int i, Path basePath, String propsPath) throws Exception {
		try {
			Class.forName("io.synclite.H2Appender");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			H2Appender.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			return null;
		} catch (Exception e) {
			throw e;
		}
	}

	private Void initH2StoreDevice(int i, Path basePath, String propsPath, List<DestinationOptions> destinations) throws Exception {
		try {
			Class.forName("io.synclite.H2Store");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			if (!destinations.isEmpty()) {
				H2Store.initialize(devicePath, Path.of(propsPath), String.valueOf(i), destinations);
			} else {
				H2Store.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			}
			return null;
		} catch (Exception e) {
			throw e;
		}
	}

	private Void initHyperSQLDevice(int i, Path basePath, String propsPath, List<DestinationOptions> destinations) throws Exception {
		try {
			Class.forName("io.synclite.HyperSQL");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			if (!destinations.isEmpty()) {
				HyperSQL.initialize(devicePath, Path.of(propsPath), String.valueOf(i), destinations);
			} else {
				HyperSQL.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			}
			return null;
		} catch (Exception e) {
			throw e;
		}
	}

	private Void initHyperSQLAppenderDevice(int i, Path basePath, String propsPath) throws Exception {
		try {
			Class.forName("io.synclite.HyperSQLAppender");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			HyperSQLAppender.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			return null;
		} catch (Exception e) {
			throw e;
		}
	}

	private Void initHyperSQLStoreDevice(int i, Path basePath, String propsPath, List<DestinationOptions> destinations) throws Exception {
		try {
			Class.forName("io.synclite.HyperSQLStore");
			Path devicePath = Path.of(basePath.toString(), String.valueOf(i));
			if (!destinations.isEmpty()) {
				HyperSQLStore.initialize(devicePath, Path.of(propsPath), String.valueOf(i), destinations);
			} else {
				HyperSQLStore.initialize(devicePath, Path.of(propsPath), String.valueOf(i));
			}
			return null;
		} catch (Exception e) {
			throw e;
		}
	}
}

