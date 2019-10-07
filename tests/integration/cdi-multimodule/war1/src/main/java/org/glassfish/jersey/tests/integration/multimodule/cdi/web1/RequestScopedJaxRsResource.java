/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.multimodule.cdi.web1;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.glassfish.jersey.tests.integration.multimodule.cdi.lib.JaxRsInjectedDependentBean;
import org.glassfish.jersey.tests.integration.multimodule.cdi.lib.JaxRsInjectedRequestScopedBean;

/**
 * JAX-RS resource backed by a request scoped CDI bean.
 *
 * @author Jakub Podlesak
  */
@Path("request-scoped")
@RequestScoped
public class RequestScopedJaxRsResource {

    @Inject
    JaxRsInjectedDependentBean dependentBean;

    @Inject
    JaxRsInjectedRequestScopedBean reqScopedBean;

    @Path("req/header")
    @GET
    public String getReqHeader() {
        return reqScopedBean.getTestHeader();
    }

    @Path("dependent/header")
    @GET
    public String getDependentHeader() {
        return dependentBean.getTestHeader();
    }

    @Path("req/uri/{p}")
    @GET
    public String getReqUri() {
        return reqScopedBean.getUriInfo().getRequestUri().toString();
    }

    @Path("dependent/uri/{p}")
    @GET
    public String getAppUri() {
        return dependentBean.getUriInfo().getRequestUri().toString();
    }
}
