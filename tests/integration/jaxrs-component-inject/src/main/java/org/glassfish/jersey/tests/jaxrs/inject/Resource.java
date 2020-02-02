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

import java.util.Set;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

/**
 * Resource with {@link Context} injection points.
 */
@Path("resource")
public class Resource {

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
    Configuration configration;

    @POST
    @Path("echo")
    public String returnGivenString(String string) {
        return string;
    }

    @POST
    @Path("reader")
    public String reader(StringBean bean) {
        return bean.get();
    }

    @POST
    @Path("writer")
    public StringBean writer(String entity) {
        return new StringBean(entity);
    }

    @GET
    @Path("instance")
    public String instance() {
        return StringBeanEntityProviderWithInjectables.computeMask(application,
                info, request, headers, security, providers, resources,
                configration);
    }

    @GET
    @Path("method")
    public String method(@Context Application application,
            @Context UriInfo info, @Context Request request,
            @Context HttpHeaders headers, @Context SecurityContext security,
            @Context Providers providers, @Context ResourceContext resources) {
        return StringBeanEntityProviderWithInjectables.computeMask(application,
                info, request, headers, security, providers, resources,
                configration);
    }

    @GET
    @Path("application")
    public String application(@Context Application application) {
        Set<Object> singletons = application.getSingletons();
        SingletonWithInjectables singleton = (SingletonWithInjectables) singletons
                .iterator().next();
        return singleton.getInjectedContextValues();
    }

}
