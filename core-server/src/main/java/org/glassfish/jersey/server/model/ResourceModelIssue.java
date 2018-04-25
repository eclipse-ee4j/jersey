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

package org.glassfish.jersey.server.model;

import org.glassfish.jersey.Severity;

/**
 * Resource model validity issue.
 * <p/>
 * Covers various model issues, such as duplicate URI templates, duplicate
 * HTTP method annotations, etc.
 * <p/>
 * The model issues can be either fatal warnings or hings (see {@link #getSeverity()}).
 * While the non-fatal issues are merely reported as warnings in the log, the
 * fatal issues prevent the successful application deployment.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public final class ResourceModelIssue {

    private final Object source;
    private final String message;
    private final Severity severity;

    /**
     * Create a new resource model warning.
     *
     * @param source  issue source.
     * @param message human-readable issue description.
     */
    public ResourceModelIssue(final Object source, final String message) {
        this(source, message, Severity.WARNING);
    }

    /**
     * Create a new resource model issue.
     *
     * @param source   issue source.
     * @param message  human-readable issue description.
     * @param severity indicates severity of added error.
     */
    public ResourceModelIssue(final Object source, final String message, final Severity severity) {
        this.source = source;
        this.message = message;
        this.severity = severity;
    }

    /**
     * Human-readable description of the issue.
     *
     * @return message describing the issue.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get {@link org.glassfish.jersey.Severity}.
     *
     * @return severity of current {@link ResourceModelIssue}.
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * The issue source.
     * <p/>
     * Identifies the object where the issue was found.
     *
     * @return source of the issue.
     */
    public Object getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "[" + severity + "] " + message + "; source='" + source + '\'';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ResourceModelIssue that = (ResourceModelIssue) o;

        if (message != null ? !message.equals(that.message) : that.message != null) {
            return false;
        }
        if (severity != that.severity) {
            return false;
        }
        if (source != null ? !source.equals(that.source) : that.source != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = source != null ? source.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (severity != null ? severity.hashCode() : 0);
        return result;
    }
}
