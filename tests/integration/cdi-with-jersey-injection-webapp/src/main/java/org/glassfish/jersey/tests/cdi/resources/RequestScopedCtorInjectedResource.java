/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.cdi.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Provider;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.monitoring.MonitoringStatistics;
import org.glassfish.jersey.spi.ExceptionMappers;

/**
 * CDI backed, request scoped, JAX-RS resource to be injected
 * via it's constructor from both CDI and Jersey HK2.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@RequestScoped
@Path("request-ctor-injected")
public class RequestScopedCtorInjectedResource {

    // CDI injected
    EchoService echoService;

    // Jersey injected
    ContainerRequest request;
    ExceptionMappers mappers;
    Provider<MonitoringStatistics> stats;

    // Jersey/HK2 custom injected
    MyApplication.MyInjection customInjected;

    // to make weld happy
    public RequestScopedCtorInjectedResource() {
    }

    @Inject
    public RequestScopedCtorInjectedResource(@RequestSpecific EchoService echoService,
                                             ContainerRequest request, ExceptionMappers mappers,
                                             Provider<MonitoringStatistics> stats, MyApplication.MyInjection customInjected) {

        this.echoService = echoService;
        this.mappers = mappers;
        this.request = request;
        this.stats = stats;
        this.customInjected = customInjected;
    }

    @GET
    public String echo(@QueryParam("s") String s) {
        return echoService.echo(s);
    }

    @GET
    @Path("path/{param}")
    public String getPath() {
        return request.getPath(true);
    }

    @GET
    @Path("mappers")
    public String getMappers() {
        return mappers.toString();
    }

    @GET
    @Path("requestCount")
    public String getStatisticsProperty() {
        return String.valueOf(stats.get().snapshot().getRequestStatistics().getTimeWindowStatistics().get(0L).getRequestCount());
    }

    @GET
    @Path("custom")
    public String getCustom() {
        return customInjected.getName();
    }
}
