/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.server.internal;

/**
 * Utility methods used by container implementations.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class ContainerUtils {
    private static final String[] TOKENS = {
            "{", "}", "\\", "^", "|", "`"
    };

    private static final String[] REPLACEMENTS = {
            "%7B", "%7D", "%5C", "%5E", "%7C", "%60"
    };

    /**
     * Encodes (predefined subset of) unsafe/unwise URI characters with the percent-encoding.
     *
     * <p>Replaces the predefined set of unsafe URI characters in the query string with its percent-encoded
     * counterparts. The reserved characters (as defined by the RFC) are automatically encoded by browsers, but some
     * characters are in the "gray zone" - are not explicitly forbidden, but not recommended and known to cause
     * issues.</p>
     *
     * @param originalQueryString URI query string (the part behind the question mark character).
     * @return the same string with unsafe characters percent encoded.
     */
    public static String encodeUnsafeCharacters(final String originalQueryString) {
        if (originalQueryString == null) {
            return null;
        }

        String result = originalQueryString;
        for (int i = 0; i < TOKENS.length; i++) {
            if (originalQueryString.contains(TOKENS[i])) {
                result = result.replace(TOKENS[i], REPLACEMENTS[i]);
            }
        }

        return result;
    }

    /**
     * Reduces the number of slashes before the path to only one slash.
     *
     * @param path path string
     * @return path string with reduced slashes to only one.
     */
    public static String reduceLeadingSlashes(final String path) {
        int length;
        if (path == null || (length = path.length()) == 0) {
            return path;
        }

        int start = 0;
        while (start != length && "/".indexOf(path.charAt(start)) != -1) {
            start++;
        }

        return path.substring(start > 0 ? start - 1 : 0);
    }

    /**
     * Splits URI address from query params and returns it.
     *
     * @param uri URI address in string format with query params
     * @return URI address in string format without query params
     */
    public static String getHandlerPath(String uri) {
        if (uri == null || uri.length() == 0 || !uri.contains("?")) {
            return uri;
        } else {
            return uri.substring(0, uri.indexOf("?"));
        }
    }
}
