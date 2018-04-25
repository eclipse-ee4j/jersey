/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.httppatch;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;

import org.glassfish.jersey.message.MessageBodyWorkers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;

/**
 * JAX-RS reader interceptor that implements server-side PATCH support.
 *
 * @author Gerard Davison (gerard.davison at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class PatchingInterceptor implements ReaderInterceptor {

    private final UriInfo uriInfo;
    private final MessageBodyWorkers workers;

    /**
     * {@code PatchingInterceptor} injection constructor.
     *
     * @param uriInfo {@code javax.ws.rs.core.UriInfo} proxy instance.
     * @param workers {@link org.glassfish.jersey.message.MessageBodyWorkers} message body workers.
     */
    public PatchingInterceptor(@Context UriInfo uriInfo, @Context MessageBodyWorkers workers) {
        this.uriInfo = uriInfo;
        this.workers = workers;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object aroundReadFrom(ReaderInterceptorContext readerInterceptorContext) throws IOException, WebApplicationException {
        // Get the resource we are being called on, and find the GET method
        Object resource = uriInfo.getMatchedResources().get(0);

        Method found = null;
        for (Method next : resource.getClass().getMethods()) {
            if (next.getAnnotation(GET.class) != null) {
                found = next;
                break;
            }
        }

        if (found == null) {
            throw new InternalServerErrorException("No matching GET method on resource");
        }

        // Invoke the get method to get the state we are trying to patch
        Object bean;
        try {
            bean = found.invoke(resource);
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }

        // Convert this object to a an array of bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MessageBodyWriter bodyWriter =
                workers.getMessageBodyWriter(bean.getClass(), bean.getClass(),
                        new Annotation[0], MediaType.APPLICATION_JSON_TYPE);

        bodyWriter.writeTo(bean, bean.getClass(), bean.getClass(),
                new Annotation[0], MediaType.APPLICATION_JSON_TYPE,
                new MultivaluedHashMap<String, Object>(), baos);


        // Use the Jackson 2.x classes to convert both the incoming patch
        // and the current state of the object into a JsonNode / JsonPatch
        ObjectMapper mapper = new ObjectMapper();
        JsonNode serverState = mapper.readValue(baos.toByteArray(), JsonNode.class);
        JsonNode patchAsNode = mapper.readValue(readerInterceptorContext.getInputStream(), JsonNode.class);
        JsonPatch patch = JsonPatch.fromJson(patchAsNode);

        try {
            // Apply the patch
            JsonNode result = patch.apply(serverState);

            // Stream the result & modify the stream on the readerInterceptor
            ByteArrayOutputStream resultAsByteArray = new ByteArrayOutputStream();
            mapper.writeValue(resultAsByteArray, result);
            readerInterceptorContext.setInputStream(new ByteArrayInputStream(resultAsByteArray.toByteArray()));

            // Pass control back to the Jersey code
            return readerInterceptorContext.proceed();
        } catch (JsonPatchException ex) {
            throw new InternalServerErrorException("Error applying patch.", ex);
        }
    }
}
