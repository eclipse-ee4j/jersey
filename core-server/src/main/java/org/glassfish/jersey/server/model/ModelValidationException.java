/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.List;

/**
 * Resource model validation exception.
 *
 * Indicates the issues with the model.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ModelValidationException extends RuntimeException {

    private static final long serialVersionUID = 4076015716487596210L;
    private final List<ResourceModelIssue> issues;

    /**
     * Creates new resource model validation exception with the list of validation issues and the message.
     * @param message message for the exception. The final message returned by {@link #getMessage()} will
     *               contains the {@code message} and other information about exception.
     * @param issues validation issues.
     */
    public ModelValidationException(String message, List<ResourceModelIssue> issues) {
        super(message);
        this.issues = issues;
    }

    /**
     * Get validation issues.
     *
     * @return validation issues.
     */
    public List<ResourceModelIssue> getIssues() {
        return issues;
    }

    @Override
    public String getMessage() {
        final String message = super.getMessage();
        return (message == null ? "" : message + '\n') + issues.toString();
    }
}
