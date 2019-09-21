/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.server.validation;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;

import javax.validation.constraints.NotNull;

import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.process.Inflector;

/**
 * @author Michal Gajdos
 */
public class ValidationInflector implements Inflector<ContainerRequestContext, String> {

    @NotNull
    @Override
    public String apply(final ContainerRequestContext requestContext) {
        return get(requestContext);
    }

    @NotNull
    public String get(@NotNull final ContainerRequestContext requestContext) {
        try {
            final String entity = ReaderWriter.readFromAsString(
                    requestContext.getEntityStream(),
                    requestContext.getMediaType());

            return entity.isEmpty() ? null : entity;
        } catch (IOException e) {
            return "error";
        }
    }
}
