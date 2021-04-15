/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.cdi.inject;

import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.servlet.WebConfig;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Providers;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class ApplicationInjectParent extends Application {
    @Context
    protected HttpHeaders contextHttpHeaders;

    @Inject
    protected HttpHeaders injectHttpHeaders;

    @Context
    ParamConverterProvider contextParamConverterProvider;

    @Inject
    ParamConverterProvider injectParamConverterProvider;

    @Context
    protected Providers contextProviders;

    @Inject
    protected Providers injectProviders;

    @Context
    protected ResourceContext contextResourceContext;

    @Inject
    protected ResourceContext injectResourceContext;

    @Context
    protected Request contextRequest;

    @Inject
    protected Request injectRequest;

    @Context
    protected ResourceInfo contextResourceInfo;

    @Inject
    protected ResourceInfo injectResourceInfo;

    @Context
    protected SecurityContext contextSecurityContext;

    @Inject
    protected SecurityContext injectSecurityContext;

    @Context
    protected UriInfo contextUriInfo;

    @Inject
    protected UriInfo injectUriInfo;

    @Context
    protected HttpServletRequest contextHttpServletRequest;

    @Inject
    protected HttpServletRequest injectHttpServletRequest;

    @Context
    protected WebConfig contextWebConfig;

    @Inject
    protected WebConfig injectWebConfig;

    @Context
    protected HttpServletResponse contextHttpServletResponse;

    @Inject
    protected HttpServletResponse injectHttpServletResponse;

    @Context
    protected ServletConfig contextServletConfig;

    @Inject
    protected ServletConfig injectServletConfig;

    @Context
    protected ServletContext contextServletContext;

    @Inject
    protected ServletContext injectServletContext;

    static class InjectHolder extends ParentInject {

        @Override
        protected boolean checkApplication(Application application, StringBuilder stringBuilder) {
            return true;
        }

        @Override
        protected boolean checkConfiguration(Configuration configuration, StringBuilder stringBuilder) {
            return true;
        }

        @Override
        protected boolean checkPropertiesDelegate(PropertiesDelegate propertiesDelegate, StringBuilder stringBuilder) {
            return true;
        }
    };

    private InjectHolder injectHolder = new InjectHolder();

    @PostConstruct
    void postConstruct() {
        try {
            setInjectHolder("context");
            setInjectHolder("inject");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setInjectHolder(String prefix) throws NoSuchFieldException, IllegalAccessException {
        for (Field field : ApplicationInjectParent.class.getDeclaredFields()) {
            if (field.getType() != InjectHolder.class && field.getName().startsWith(prefix)) {
                Field holders = InjectHolder.class.getSuperclass().getDeclaredField(field.getName());
                holders.setAccessible(true);
                holders.set(injectHolder, field.get(this));
            }
        }
    }

    @Override
    public Set<Object> getSingletons() {
        final Set<Object> set = new LinkedHashSet<>();
        set.add(injectHolder);
        return set;
    }

    public static class ResourceParent {
        @Context
        Application contextApplication;

        @Inject
        Application injectApplication;

        @Context
        PropertiesDelegate propertiesDelegate;

        @GET
        @Path("context")
        public Response checkAppContexted() {
            return checkApp(true);
        }

        @GET
        @Path("inject")
        public Response checkAppInjected() {
            return checkApp(false);
        }

        private Response checkApp(boolean contexted) {
            StringBuilder sb = new StringBuilder();
            Iterator<Object> singletons = contextApplication.getSingletons().iterator();
            final InjectHolder injectHolder = (InjectHolder) singletons.next();
            final boolean injected = contexted ? injectHolder.checkContexted(sb) : injectHolder.checkInjected(sb);
            if (injected) {
                return Response.ok().entity("All injected").build();
            } else {
                propertiesDelegate.setProperty(ParentWriterInterceptor.STATUS, Response.Status.EXPECTATION_FAILED);
                return Response.status(Response.Status.EXPECTATION_FAILED).entity(sb.toString()).build();
            }
        }
    }

}
