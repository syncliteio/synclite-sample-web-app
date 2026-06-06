# SyncLite Sample Web App — JSP/Servlet Demo Application

> Part of the [SyncLite Platform](https://github.com/syncliteio/SyncLite) — Build Anything, Sync Anywhere.

## What is this?

The **SyncLite Sample Web App** is a fully functional JSP/Servlet web application that demonstrates how to embed [SyncLite Logger](https://github.com/syncliteio/synclite-logger-java) into a Java web application. It is provided as a reference implementation and hands-on demo.

The sample app lets you:
- Create SyncLite devices (databases) of any supported type directly from a web form
- Execute SQL workloads (INSERT / UPDATE / DELETE batches) across multiple devices simultaneously
- Observe live data consolidation flowing into the destination database via the SyncLite Consolidator dashboard

It is deployed alongside SyncLite Consolidator on the same Tomcat instance and serves as the quickest way to see the full SyncLite pipeline in action — from edge write through sync log shipping to destination DB — without writing any code.

## Quick Start

```bash
# From the platform bin/ directory
./deploy.sh && ./start.sh
```

Then open: http://localhost:8080/synclite-sample-app

## What the app demonstrates

| Feature | Description |
|---|---|
| Device creation | Create one or many SyncLite devices (SQLite, DuckDB, Derby, H2, HyperSQL, Streaming) |
| SQL workload execution | Run configurable INSERT / UPDATE / DELETE workloads on N devices in parallel |
| Multi-device consolidation | Watch hundreds of devices consolidating into a single destination DB |
| Configuration | Shows how to pass a `synclite.conf` to `SyncLite.initialize()` |

## Architecture

```
Browser  --HTTP-->  SyncLite Sample Web App (Tomcat)
                         │  SyncLite Logger (embedded JDBC)
                         v
                   Edge Databases (SQLite / DuckDB / …)
                         │  sync log files
                         v
                   Local staging directory
                         │
                         v
                   SyncLite Consolidator  -->  Destination DB
```

## Source Code

The JSP/Servlet source is in `web/src/`. Key entry points:

- `web/src/main/webapp/` — JSP views (create device, run workload, dashboard)
- `web/src/main/java/` — Servlet handlers and SyncLite Logger integration code
- `web/src/main/resources/synclite.conf` — sample logger configuration

This source code is the best starting point if you want to see exactly how to initialize SyncLite Logger, manage connections, and execute SQL (with transactions) from a web application.

## Build

```bash
cd synclite-sample-web-app/web
mvn -Drevision=oss clean install
```

Built WAR: `web/target/synclite-sample-app-oss.war`

## Related Components

| Component | Role |
|---|---|
| [SyncLite Logger](https://github.com/syncliteio/synclite-logger-java) | Embedded JDBC driver used by this app |
| [SyncLite Consolidator](https://github.com/syncliteio/synclite-consolidator) | Consolidates the data written by this app |

## Documentation & Community

- Full documentation: https://github.com/syncliteio/SyncLite/blob/main/DOCUMENTATION.md
- Website: https://www.synclite.io
- Community: https://github.com/syncliteio/SyncLite/issues

---

← Back to the [SyncLite Platform README](https://github.com/syncliteio/SyncLite/blob/main/README.md)
