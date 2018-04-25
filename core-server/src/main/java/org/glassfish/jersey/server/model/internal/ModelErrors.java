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

package org.glassfish.jersey.server.model.internal;

import java.util.List;
import java.util.stream.Collectors;

import org.glassfish.jersey.internal.Errors;
import org.glassfish.jersey.server.model.ResourceModelIssue;

/**
 * Utility to transform {@link Errors.ErrorMessage error messages} to {@link ResourceModelIssue}s.
 *
 * @author Michal Gajdos
 */
public class ModelErrors {

    /**
     * Get all filed error messages as {@link ResourceModelIssue}s.
     *
     * @return non-null list of resource model issues.
     */
    public static List<ResourceModelIssue> getErrorsAsResourceModelIssues() {
        return getErrorsAsResourceModelIssues(false);
    }

    /**
     * Get error messages filed after {@link Errors#mark()} flag was set as {@link ResourceModelIssue}s.
     *
     * @return non-null list of resource model issues.
     */
    public static List<ResourceModelIssue> getErrorsAsResourceModelIssues(final boolean afterMark) {
        return Errors.getErrorMessages(afterMark).stream()
                     .map(input -> new ResourceModelIssue(input.getSource(), input.getMessage(), input.getSeverity()))
                     .collect(Collectors.toList());
    }

}
