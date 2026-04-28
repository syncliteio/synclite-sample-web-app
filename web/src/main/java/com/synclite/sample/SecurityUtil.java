/*
 * Copyright (c) 2024 mahendra.chavan@synclite.io, all rights reserved.
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
 */

package com.synclite.sample;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

final class SecurityUtil {

    static final String CSRF_TOKEN_PARAM = "csrfToken";
    static final String CSRF_TOKEN_SESSION_KEY = "csrfToken";

    private static final Set<String> ALLOWED_DEVICE_TYPES = new HashSet<>(Arrays.asList(
        "STREAMING", "SQLITE", "DUCKDB", "DERBY", "H2", "HYPERSQL",
        "SQLITE_STORE", "DUCKDB_STORE", "DERBY_STORE", "H2_STORE", "HYPERSQL_STORE"
    ));

    private SecurityUtil() {
    }

    static String sanitizeErrorMessage(Exception e) {
        if (e == null || e.getMessage() == null || e.getMessage().trim().isEmpty()) {
            return "Request failed";
        }
        return e.getMessage();
    }

    static String encodeUrlParam(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    static void forwardWithError(HttpServletRequest request, HttpServletResponse response, String jsp, Exception e)
            throws ServletException {
        String errorMsg = sanitizeErrorMessage(e);
        try {
            String target = jsp + "?errorMsg=" + encodeUrlParam(errorMsg);
            request.getRequestDispatcher(target).forward(request, response);
        } catch (Exception forwardEx) {
            throw new ServletException("Failed while forwarding to error page", forwardEx);
        }
    }

    static int getRequiredPositiveInt(HttpServletRequest request, String paramName, int max)
            throws ServletException {
        String val = request.getParameter(paramName);
        if (val == null || val.trim().isEmpty()) {
            throw new ServletException("\"" + paramName + "\" must be specified");
        }
        try {
            int parsed = Integer.parseInt(val.trim());
            if (parsed <= 0 || parsed > max) {
                throw new ServletException("\"" + paramName + "\" must be between 1 and " + max);
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new ServletException("\"" + paramName + "\" must be a valid integer");
        }
    }

    static String getRequiredText(HttpServletRequest request, String paramName, int maxLen) throws ServletException {
        String val = request.getParameter(paramName);
        if (val == null || val.trim().isEmpty()) {
            throw new ServletException("\"" + paramName + "\" must be specified");
        }
        String trimmed = val.trim();
        if (trimmed.length() > maxLen) {
            throw new ServletException("\"" + paramName + "\" must be <= " + maxLen + " characters");
        }
        return trimmed;
    }

    static String getValidatedDeviceType(HttpServletRequest request, String paramName) throws ServletException {
        String val = getRequiredText(request, paramName, 64);
        if (!ALLOWED_DEVICE_TYPES.contains(val)) {
            throw new ServletException("Invalid device type specified");
        }
        return val;
    }

    static Path normalizePath(String rawPath) throws ServletException {
        if (rawPath == null || rawPath.trim().isEmpty()) {
            throw new ServletException("\"DB Base Path\" must be specified");
        }
        return Path.of(rawPath.trim()).normalize();
    }
}
