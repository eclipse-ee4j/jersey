/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.validation;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Default validation error entity to be included in {@code Response}.
 *
 * @author Michal Gajdos
 */
@XmlRootElement
@SuppressWarnings("UnusedDeclaration")
public final class ValidationError extends ValidationErrorData {

    /**
     * Create a {@code ValidationError} instance. Constructor for JAXB providers.
     */
    public ValidationError() {
    }

    /**
     * Create a {@code ValidationError} instance.
     *
     * @param message interpolated error message.
     * @param messageTemplate non-interpolated error message.
     * @param path property path.
     * @param invalidValue value that failed to pass constraints.
     */
    public ValidationError(final String message, final String messageTemplate, final String path, final String invalidValue) {
        super(message, messageTemplate, path, invalidValue);
    }
}
