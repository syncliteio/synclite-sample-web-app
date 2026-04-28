# PR Description — synclite-sample-web-app

## Summary

This PR fixes critical functional bugs in STORE device-type routing, completes resource cleanup for STORE device types in `DeviceCloser`, corrects a device-type constant typo, updates JDBC driver dependencies, and rewrites the module README.

---

## Changes by File

### `web/pom.xml` — Dependency updates

| Dependency | Before | After |
|---|---|---|
| `org.xerial:sqlite-jdbc` | 3.45.3.0 | **3.53.0.0** |
| `org.duckdb:duckdb_jdbc` | 1.0.0 | **1.5.2.0** |

Both updates align this module with all other SyncLite submodules.

---

### `src/.../DeviceCreator.java` — Critical bug fix: STORE device types calling Appender init

**Bug description:**

All five STORE device types (`SQLITE_STORE`, `DUCKDB_STORE`, `DERBY_STORE`, `H2_STORE`, `HYPERSQL_STORE`) were incorrectly routed to the corresponding Appender initialisation methods:

```java
case "SQLITE_STORE":
    initSQLiteAppenderDevice(dbDir, deviceName, deviceConfig);  // WRONG
    break;
```

`SyncLiteAppender.initialize()` configures the device in Appender mode. Appender devices write INSERT/UPDATE/DELETE SQL statements to a log segment and replay them during consolidation. `SyncLiteStore.initialize()` configures the device in Store mode with structured CRUD operations. Calling the Appender initialiser for a STORE device silently produced a device that rejected all structured Store API calls at runtime.

**Fix:**

Five new methods added:

- `initSQLiteStoreDevice(Path dbDir, String deviceName, Path deviceConfig)` — loads `io.synclite.logger.SQLiteStore` via `Class.forName()` and calls `SQLiteStore.initialize(dbPath, deviceConfig)`.
- `initDuckDBStoreDevice(...)` — same pattern with `DuckDBStore`.
- `initDerbyStoreDevice(...)` — same pattern with `DerbyStore`.
- `initH2StoreDevice(...)` — same pattern with `H2Store`.
- `initHyperSQLStoreDevice(...)` — same pattern with `HyperSQLStore`.

Each case statement now routes to the correct Store-specific method.

**Typo fix:**

`"SQLOTE_STORE"` ? `"SQLITE_STORE"` in the switch condition. The typo caused the SQLite Store case to never match, so SQLite Store devices were silently skipped during creation.

---

### `src/.../DeviceCloser.java` — Added `closeAllDevices()` for STORE device types

`closeAllDevices()` was called for all Appender device types but was missing for every STORE device type. Devices left open without `closeAllDevices()` do not flush their final transaction to the log segment, meaning the last batch of changes could be lost at application shutdown.

Added `closeAllDevices()` calls for all five STORE types:

```java
SQLiteStore.closeAllDevices();
DuckDBStore.closeAllDevices();
DerbyStore.closeAllDevices();
H2Store.closeAllDevices();
HyperSQLStore.closeAllDevices();
```

Each call uses the same reflection-based class loading pattern already used for Appender types, so the SyncLite logger JAR does not need to be on the compile-time classpath.

---

### `src/.../WorkloadRunner.java` — Critical bug fix: STORE device types using Appender JDBC URLs

**Bug description:**

All five STORE device types were constructing JDBC connection URLs using the Appender scheme and loading Appender driver classes:

```java
case "SQLITE_STORE":
    String url = "jdbc:synclite_sqlite:" + dbPath;       // WRONG: Appender URL
    Class.forName("io.synclite.logger.SQLiteAppender");   // WRONG: Appender class
    runDMLsGeneric(url, ...);
    break;
```

`jdbc:synclite_sqlite:` is the URL scheme for the `SQLiteAppender` driver. The correct URL scheme for the `SQLiteStore` driver is `jdbc:synclite_sqlite_store:`. As a result, all DML operations executed against STORE devices were actually logged through the Appender driver, producing incorrect replicated data (INSERT/UPDATE/DELETE SQL text in the log rather than structured change records).

**Fix:**

Five new methods added, each using the correct JDBC URL and driver class:

| Method | JDBC URL | Driver class |
|---|---|---|
| `runDMLsSQLiteStore(...)` | `jdbc:synclite_sqlite_store:` | `io.synclite.logger.SQLiteStore` |
| `runDMLsDuckDBStore(...)` | `jdbc:synclite_duckdb_store:` | `io.synclite.logger.DuckDBStore` |
| `runDMLsDerbyStore(...)` | `jdbc:synclite_derby_store:` | `io.synclite.logger.DerbyStore` |
| `runDMLsH2Store(...)` | `jdbc:synclite_h2_store:` | `io.synclite.logger.H2Store` |
| `runDMLsHyperSQLStore(...)` | `jdbc:synclite_hypersql_store:` | `io.synclite.logger.HyperSQLStore` |

**Typo fix:**

Same `"SQLOTE_STORE"` ? `"SQLITE_STORE"` typo corrected in `WorkloadRunner`'s switch statement.

---

### `README.md` — Comprehensive rewrite

- Architecture overview updated: describes STORE vs. Appender device types and when to use each.
- Quick Start section: end-to-end example using `SQLiteStore` API via the sample web app.
- Device creation table: all 10 device types listed with correct display names.
- Build and run instructions verified against current Maven coordinates.
- Outbound links to synclite-logger-java README and synclite-consolidator README added.
