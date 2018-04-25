/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 * JAX-RS resource class backed by a request scoped CDI bean.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@RequestScoped
@Path("req")
public class RequestScopedResource {

    @Inject
    AppScopedResource appResource;

    @Inject
    RequestScopedBean bean;

    @GET
    @Path("app/counter")
    public int getCounter() {
        return appResource.getCount();
    }

    @GET
    @Path("myself")
    public String getMyself() {
        return this.toString();
    }

    @GET
    @Path("parameterized")
    @ResponseBodyFromCdiBean
    public String interceptedParameterized(@QueryParam("q") String q) {
        bean.setRequestId(q);
        return "does not matter";
    }

    @GET
    @Path("straight")
    public String parameterizedStraight(@QueryParam("q") String q) {
        return "straight: " + q;
    }

    private static final Executor executor = Executors.newCachedThreadPool();

    @GET
    @Path("parameterized-async")
    @ResponseBodyFromCdiBean
    public void interceptedParameterizedAsync(@QueryParam("q") final String q, @Suspended final AsyncResponse response) {
        bean.setRequestId(q);
        executor.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(RequestScopedResource.class.getName()).log(Level.SEVERE, null, ex);
                }
                response.resume("this will never make it to the client");
            }
        });
    }

    @Context
    UriInfo uriInfo;

    @Inject App.JaxRsApplication jaxRsApplication;

    @GET
    @Path("ui/jax-rs-field/{d}")
    public String getJaxRsInjectedUIUri() {

        if (uriInfo == jaxRsApplication.uInfo) {
            throw new IllegalStateException("UriInfo injected into req scoped cdi bean should not get proxied.");
        }

        return uriInfo.getRequestUri().toString();
    }

    @GET
    @Path("ui/jax-rs-app-field/{d}")
    public String getCdiInjectedJaxRsAppUri() {
        return jaxRsApplication.uInfo.getRequestUri().toString();
    }
}
