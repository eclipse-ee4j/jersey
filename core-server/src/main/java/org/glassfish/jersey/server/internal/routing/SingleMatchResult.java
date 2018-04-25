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

package org.glassfish.jersey.server.internal.routing;

import java.util.regex.MatchResult;

/**
 * {@link MatchResult} implementation that returns the nested string as a
 * single matching result. This match result mimics matching of a single
 * matching group with group index 0 (the one containing the whole expression).
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
final class SingleMatchResult implements MatchResult {

    private final String path;

    /**
     * Construct a match result matching the whole supplied path.
     *
     * @param path matched path.
     */
    public SingleMatchResult(final String path) {
        this.path = stripMatrixParams(path);
    }

    /**
     * Strip the matrix parameters from a path.
     *
     * @return path stripped of matrix parameters.
     */
    private static String stripMatrixParams(final String path) {
        int e = path.indexOf(';');
        if (e == -1) {
            return path;
        }

        int s = 0;
        StringBuilder sb = new StringBuilder();
        do {
            // Append everything up to but not including the ';'
            sb.append(path, s, e);

            // Skip everything up to but not including the '/'
            s = path.indexOf('/', e + 1);
            if (s == -1) {
                break;
            }
            e = path.indexOf(';', s);
        } while (e != -1);

        if (s != -1) {
            // Append any remaining characters
            sb.append(path, s, path.length());
        }

        return sb.toString();
    }


    @Override
    public int start() {
        return 0;
    }

    @Override
    public int start(final int group) {
        if (group == 0) {
            return start();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int end() {
        return path.length();
    }

    @Override
    public int end(final int group) {
        if (group == 0) {
            return end();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public String group() {
        return path;
    }

    @Override
    public String group(final int group) {
        if (group == 0) {
            return group();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int groupCount() {
        return 0;
    }
}
