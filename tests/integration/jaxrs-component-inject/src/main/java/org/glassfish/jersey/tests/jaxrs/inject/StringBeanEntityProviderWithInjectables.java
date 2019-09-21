/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.jaxrs.inject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

/**
 * Provider with {@link Context} injection points.
 */
@Provider
public class StringBeanEntityProviderWithInjectables extends StringBeanEntityProvider {

    @Context
    Application application;
    @Context
    UriInfo info;
    @Context
    Request request;
    @Context
    HttpHeaders headers;
    @Context
    SecurityContext security;
    @Context
    Providers providers;
    @Context
    ResourceContext resources;
    @Context
    Configuration configuration;

    /**
     * Chosen decimal as a representation to be more human readable
     */
    public static String computeMask(Application application, UriInfo info,
            Request request, HttpHeaders headers, SecurityContext security,
            Providers providers, ResourceContext resources,
            Configuration configuration) {
        int mask = 1;
        mask = 10 * mask + (application == null ? 0 : 1);
        mask = 10 * mask + (info == null ? 0 : 1);
        mask = 10 * mask + (request == null ? 0 : 1);
        mask = 10 * mask + (headers == null ? 0 : 1);
        mask = 10 * mask + (security == null ? 0 : 1);
        mask = 10 * mask + (providers == null ? 0 : 1);
        mask = 10 * mask + (resources == null ? 0 : 1);
        mask = 10 * mask + (configuration == null ? 0 : 1);
        return String.valueOf(mask);
    }

    /**
     * Here, the bitwise operation with mask variable would be more efficient,
     * but less human readable when sending over the link as a binary number.
     * Hence, sMask is supposed to be decimal number created by writeTo method.
     * <p>
     * If something has not been injected, and thus not written by writeTo, this
     * static method parses what it was not injected.
     */
    public static String notInjected(String sMask) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i != sMask.length(); i++) {
            sb.append(notInjected(sMask, i));
        }
        return sb.toString();
    }

    /**
     * Here, the bitwise operation with mask variable would be more efficient,
     * but less human readable when sending over the link as a binary number.
     * Hence, sMask is supposed to be decimal number created by writeTo method.
     * <p>
     * If something has not been injected, and thus not written by writeTo, this
     * static method parses what it was not injected.
     */
    public static final String notInjected(String sMask, int index) {
        String[] labels = {
                "Application,", "UriInfo,", "Request,",
                "HttpHeaders,", "SecurityContext,", "Providers,",
                "ResourceContext", "Configuration"
        };
        String label = "";
        if (sMask.charAt(index) == '0') {
            label = labels[index - 1];
        }
        return label;
    }

    @Override
    public long getSize(StringBean t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return 9;
    }

    @Override
    public void writeTo(StringBean t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException,
            WebApplicationException {
        entityStream.write(computeMask(application, info, request, headers,
                security, providers, resources, configuration).getBytes());
    }

    @Override
    public StringBean readFrom(Class<StringBean> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {
        return new StringBean(computeMask(application, info, request, headers,
                security, providers, resources, configuration));
    }

}
