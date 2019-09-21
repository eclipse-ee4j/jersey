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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

/**
 * Application Configuration with {@link Context} injection points.
 */
public class TSAppConfig extends Application {

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

    public java.util.Set<java.lang.Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
        resources.add(Resource.class);
        resources.add(StringBeanEntityProviderWithInjectables.class);
        resources.add(PrintingErrorHandler.class);
        return resources;
    }

    @Override
    public Set<Object> getSingletons() {
        Object single = new SingletonWithInjectables(this);
        return Collections.singleton(single);
    }

    public String getInjectedContextValues() {
        return StringBeanEntityProviderWithInjectables.computeMask(
                /*
                 * Spec: 9.2.1 Application Note that this cannot be injected into the Application subclass itself since this would
                 * create a circular dependency.
                 * */
                this, info, request, headers, security, providers, resources,
                // Configuration injection N/A on Application
                ClientBuilder.newClient().getConfiguration());
    }
}
