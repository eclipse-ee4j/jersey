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

package org.glassfish.jersey.client.oauth1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.uri.UriComponent;


/**
 * Utility class for processing client requests. This class somehow wants to be
 * more than just a utility class for this one filter.
 *
 * @author Paul C. Bryan <pbryan@sun.com>
 * @since 2.3
 */
final class RequestUtil {

    private RequestUtil() {
        throw new AssertionError("Instantiation not allowed.");
    }

    private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];

    /**
     * Returns the query parameters of a request as a multi-valued map.
     *
     * @param request the client request to retrieve query parameters from.
     * @return a {@link javax.ws.rs.core.MultivaluedMap} containing the entity query parameters.
     */
    public static MultivaluedMap<String, String> getQueryParameters(ClientRequestContext request) {
        URI uri = request.getUri();
        if (uri == null) {
            return null;
        }

        return UriComponent.decodeQuery(uri, true);
    }

    /**
     * Returns the form parameters from a request entity as a multi-valued map.
     * If the request does not have a POST method, or the media type is not
     * x-www-form-urlencoded, then null is returned.
     *
     * @param request the client request containing the entity to extract parameters from.
     * @return a {@link javax.ws.rs.core.MultivaluedMap} containing the entity form parameters.
     */
    @SuppressWarnings("unchecked")
    public static MultivaluedMap<String, String> getEntityParameters(ClientRequestContext request,
                                                                     MessageBodyWorkers messageBodyWorkers) {

        Object entity = request.getEntity();
        String method = request.getMethod();
        MediaType mediaType = request.getMediaType();

        // no entity, not a post or not x-www-form-urlencoded: return empty map
        if (entity == null || method == null || !HttpMethod.POST.equalsIgnoreCase(method)
                || mediaType == null || !mediaType.equals(MediaType.APPLICATION_FORM_URLENCODED_TYPE)) {
            return new MultivaluedHashMap<String, String>();
        }

        // it's ready to go if already expressed as a multi-valued map
        if (entity instanceof MultivaluedMap) {
            return (MultivaluedMap<String, String>) entity;
        }

        Type entityType = entity.getClass();

        // if the entity is generic, get specific type and class
        if (entity instanceof GenericEntity) {
            final GenericEntity generic = (GenericEntity) entity;
            entityType = generic.getType(); // overwrite
            entity = generic.getEntity();
        }

        final Class entityClass = entity.getClass();

        ByteArrayOutputStream out = new ByteArrayOutputStream();


        MessageBodyWriter writer = messageBodyWorkers.getMessageBodyWriter(entityClass,
                entityType, EMPTY_ANNOTATIONS, MediaType.APPLICATION_FORM_URLENCODED_TYPE);

        try {
            writer.writeTo(entity, entityClass, entityType,
                    EMPTY_ANNOTATIONS, MediaType.APPLICATION_FORM_URLENCODED_TYPE, null, out);
        } catch (WebApplicationException wae) {
            throw new IllegalStateException(wae);
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        MessageBodyReader reader = messageBodyWorkers.getMessageBodyReader(MultivaluedMap.class,
                MultivaluedMap.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_FORM_URLENCODED_TYPE);

        try {
            return (MultivaluedMap<String, String>) reader.readFrom(MultivaluedMap.class,
                    MultivaluedMap.class, EMPTY_ANNOTATIONS, MediaType.APPLICATION_FORM_URLENCODED_TYPE, null, in);
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }
}

