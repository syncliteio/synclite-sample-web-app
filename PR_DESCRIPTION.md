# Title
Security hardening, device-type normalization, and dependency refresh for sample web app

# Summary
This PR strengthens request and session security across the web app, replaces unsafe request/response handling patterns, modernizes key Maven dependencies, and aligns device type naming to BASE and STORE variants.

The change set includes:
- New servlet filter for security headers and CSRF enforcement.
- New shared security utility for parameter validation, path normalization, URL-safe error forwarding, and allowed device-type validation.
- Servlet hardening in directory validation, device creation, device closure, and workload execution.
- JSP output encoding and CSRF hidden token integration.
- Device type option migration from APPENDER naming to STORE naming, with compatibility for SQLOTE_STORE where currently present.
- Maven dependency upgrades and cleanup.
- Web session configuration hardening in deployment descriptor.
- Eclipse project metadata updates for annotation processing and resource filtering.

# What Changed

## 1. Security filter and utility additions
Added:
- web/src/main/java/com/synclite/sample/SecurityHeadersAndCSRFFilter.java
- web/src/main/java/com/synclite/sample/SecurityUtil.java

Details:
- Adds response headers: X-Content-Type-Options, X-Frame-Options, Referrer-Policy, and no-cache headers.
- Generates and stores CSRF token in session if missing.
- Rejects POST requests with invalid or missing CSRF token.
- Centralizes input validation helpers (required text, positive integer range, device type whitelist).
- Centralizes error sanitization and URL encoding for safe forwarding.

## 2. Web descriptor security configuration
Updated:
- web/src/main/webapp/WEB-INF/web.xml

Details:
- Registers SecurityHeadersAndCSRFFilter on all routes.
- Changes session timeout from unlimited to 30 minutes.
- Enforces cookie-only session tracking.
- Enables HttpOnly and Secure session cookies.

## 3. Servlet hardening and behavior fixes
Updated:
- web/src/main/java/com/synclite/sample/ValidateDBDirectory.java
- web/src/main/java/com/synclite/sample/DeviceCreator.java
- web/src/main/java/com/synclite/sample/DeviceCloser.java
- web/src/main/java/com/synclite/sample/WorkloadRunner.java

Details:
- Replaces direct console printing with java.util.logging.
- Uses safer message handling and URL-encoded error forwarding.
- Validates/normalizes base directory path and ensures directory existence.
- Rotates session id after DB directory setup to reduce session fixation risk.
- Reworks doGet/doPost patterns to avoid recursive calls and side effects.
- Adds executor shutdown and interruption-safe cleanup in finally blocks.
- Adds bounds and size checks (max devices/workload length).
- Removes Telemetry-specific initialization and workload execution paths and switches closure path to DBLogger.
- Aligns device type handling to STORE naming in server-side logic.

## 4. JSP encoding, CSRF wiring, and UX text improvements
Updated:
- web/src/main/webapp/selectDBDirectory.jsp
- web/src/main/webapp/createDevices.jsp
- web/src/main/webapp/runDMLs.jsp
- web/src/main/webapp/queryDevice.jsp
- web/src/main/webapp/stopDevices.jsp

Details:
- Adds OWASP Encoder usage for HTML output escaping.
- Adds CSRF hidden input to POST forms.
- Escapes user-controllable values rendered in inputs/messages/tables.
- Updates device type labels/options from APPENDER to STORE naming.
- Removes Telemetry option exposure in relevant UI locations.
- Improves workload/query helper text.
- Adds read-only query guard in query page to block multi-statement and non-read-only SQL execution.

## 5. Maven dependency updates
Updated:
- web/pom.xml

Details:
- sqlite-jdbc: 3.43.0.0 -> 3.45.3.0
- javax.servlet-api: 3.0.1 -> 4.0.1
- JSP API artifact/version updated to javax.servlet.jsp-api 2.3.3
- commons-io: 2.11.0 -> 2.16.1
- Removes duplicate legacy sqlite-jdbc entry and adds org.owasp.encoder:encoder:1.3.1

## 6. Project metadata and tooling files
Updated:
- web/.classpath
- web/.project
- web/.settings/org.eclipse.jdt.core.prefs

Added:
- web/.settings/org.eclipse.jdt.apt.core.prefs

Details:
- Adds generated source/resource classpath entries and annotation processing metadata.
- Disables annotation processing in Eclipse preferences.
- Adds filtered resource patterns in Eclipse project metadata.

## 7. Minor formatting change
Updated:
- web/src/main/webapp/css/SyncLiteStyle.css

Details:
- Whitespace-only indentation adjustment.

# Compatibility and Migration Notes
- Device type values previously using APPENDER naming are now represented as STORE variants in UI and backend condition branches.
- Existing compatibility check for SQLOTE_STORE remains accepted where present.
- Telemetry device path has been removed from creation and workload execution flows.

# Risk Assessment
- Security posture is significantly improved; however, enabling Secure session cookies assumes HTTPS in target deployment.
- Device type rename can affect any external automation or bookmarked URLs that still use APPENDER values.
- Query page behavior is intentionally stricter due to read-only SQL enforcement.

# Test Plan
- Validate CSRF protection:
	- Submit each POST-backed form with valid token (expect success path).
	- Submit POST without token or with invalid token (expect HTTP 403).
- Validate output encoding:
	- Inject angle brackets and script-like text into user inputs and confirm escaped rendering.
- Validate device workflows:
	- Create devices for each supported type.
	- Run workload on a bounded index range.
	- Close devices and confirm success/error handling routes.
- Validate query workflow:
	- Run allowed read-only queries.
	- Confirm rejection of multi-statement or write statements.
- Validate session behavior:
	- Confirm session timeout and cookie flags in deployed environment over HTTPS.

# Reviewer Focus Areas
- SecurityHeadersAndCSRFFilter coverage and CSRF POST assumptions.
- Device type normalization and backward compatibility impacts.
- Error forwarding and message safety across servlet-to-JSP flows.
- Any remaining references expecting APPENDER or Telemetry semantics.

