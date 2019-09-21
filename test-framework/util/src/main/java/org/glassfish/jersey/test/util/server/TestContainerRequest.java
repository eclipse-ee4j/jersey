/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.util.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.WriterInterceptor;

import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.server.ContainerRequest;

/**
 * {@link org.glassfish.jersey.server.ContainerRequest Container request context} used for testing/benchmarking purposes.
 *
 * @author Michal Gajdos
 * @since 2.17
 */
final class TestContainerRequest extends ContainerRequest {

    private static final Logger LOGGER = Logger.getLogger(TestContainerRequest.class.getName());

    TestContainerRequest(final URI baseUri,
                         final URI requestUri,
                         final String method,
                         final SecurityContext securityContext,
                         final PropertiesDelegate propertiesDelegate) {
        super(baseUri, requestUri, method, securityContext, propertiesDelegate);
    }

    void setEntity(final InputStream stream) {
        setEntityStream(stream);
    }

    void setEntity(final Object requestEntity, final MessageBodyWorkers workers) {
        final Object entity;
        final GenericType entityType;

        if (requestEntity instanceof GenericEntity) {
            entity = ((GenericEntity) requestEntity).getEntity();
            entityType = new GenericType(((GenericEntity) requestEntity).getType());
        } else {
            entity = requestEntity;
            entityType = new GenericType(requestEntity.getClass());
        }

        final byte[] entityBytes;

        if (entity != null) {
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            OutputStream stream = null;

            try {
                stream = workers.writeTo(entity, entity.getClass(),
                        entityType.getType(),
                        new Annotation[0],
                        getMediaType(),
                        new MultivaluedHashMap<String, Object>(getHeaders()),
                        getPropertiesDelegate(),
                        output,
                        Collections.<WriterInterceptor>emptyList());
            } catch (final IOException | WebApplicationException ex) {
                LOGGER.log(Level.SEVERE, "Transforming entity to input stream failed.", ex);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (final IOException e) {
                        // ignore
                    }
                }
            }

            entityBytes = output.toByteArray();
        } else {
            entityBytes = new byte[0];
        }

        setEntity(new ByteArrayInputStream(entityBytes));
    }
}
