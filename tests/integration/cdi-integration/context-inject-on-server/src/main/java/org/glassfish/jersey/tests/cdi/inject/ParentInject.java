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

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Providers;

public class ParentInject implements ParentChecker {
    @Context
    protected Application contextApplication;

    @Inject
    protected Application injectApplication;

    @Context
    protected Configuration contextConfiguration;

    @Inject
    protected Configuration injectConfiguration;

    @Context
    protected HttpHeaders contextHttpHeaders;

    @Inject
    protected HttpHeaders injectHttpHeaders;

    @Context
    protected ParamConverterProvider contextParamConverterProvider;

    @Inject
    protected ParamConverterProvider injectParamConverterProvider;

    @Context
    protected PropertiesDelegate contextPropertiesDelegate;

    @Inject
    protected PropertiesDelegate injectPropertiesDelegate;

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

    @Override
    public boolean checkInjected(StringBuilder stringBuilder) {
        boolean injected = true;
        injected &= checkApplication(injectApplication, stringBuilder);
        injected &= checkConfiguration(injectConfiguration, stringBuilder);
        injected &= InjectionChecker.checkHttpHeaders(injectHttpHeaders, stringBuilder);
        injected &= checkPropertiesDelegate(injectPropertiesDelegate, stringBuilder);
        injected &= InjectionChecker.checkParamConverterProvider(injectParamConverterProvider, stringBuilder);
        injected &= InjectionChecker.checkProviders(injectProviders, stringBuilder);
        injected &= InjectionChecker.checkRequest(injectRequest, stringBuilder);
        injected &= InjectionChecker.checkResourceContext(injectResourceContext, stringBuilder);
        injected &= InjectionChecker.checkResourceInfo(injectResourceInfo, stringBuilder);
        injected &= InjectionChecker.checkSecurityContext(injectSecurityContext, stringBuilder);
        injected &= InjectionChecker.checkUriInfo(injectUriInfo, stringBuilder);

        injected &= InjectionChecker.checkHttpServletRequest(injectHttpServletRequest, stringBuilder);
        injected &= InjectionChecker.checkHttpServletResponse(injectHttpServletResponse, stringBuilder);
        injected &= InjectionChecker.checkWebConfig(injectWebConfig, stringBuilder);
        injected &= InjectionChecker.checkServletConfig(injectServletConfig, stringBuilder);
        injected &= InjectionChecker.checkServletContext(injectServletContext, stringBuilder);

        return injected;
    }

    @Override
    public boolean checkContexted(StringBuilder stringBuilder) {
        boolean injected = true;
        injected &= checkApplication(contextApplication, stringBuilder);
        injected &= checkConfiguration(contextConfiguration, stringBuilder);
        injected &= InjectionChecker.checkHttpHeaders(contextHttpHeaders, stringBuilder);
        injected &= InjectionChecker.checkParamConverterProvider(contextParamConverterProvider, stringBuilder);
        injected &= checkPropertiesDelegate(contextPropertiesDelegate, stringBuilder);
        injected &= InjectionChecker.checkProviders(contextProviders, stringBuilder);
        injected &= InjectionChecker.checkRequest(contextRequest, stringBuilder);
        injected &= InjectionChecker.checkResourceContext(contextResourceContext, stringBuilder);
        injected &= InjectionChecker.checkResourceInfo(contextResourceInfo, stringBuilder);
        injected &= InjectionChecker.checkSecurityContext(contextSecurityContext, stringBuilder);
        injected &= InjectionChecker.checkUriInfo(contextUriInfo, stringBuilder);

        injected &= InjectionChecker.checkHttpServletRequest(contextHttpServletRequest, stringBuilder);
        injected &= InjectionChecker.checkHttpServletResponse(contextHttpServletResponse, stringBuilder);
        injected &= InjectionChecker.checkWebConfig(contextWebConfig, stringBuilder);
        injected &= InjectionChecker.checkServletConfig(contextServletConfig, stringBuilder);
        injected &= InjectionChecker.checkServletContext(contextServletContext, stringBuilder);

        return injected;
    }

    protected boolean checkApplication(Application application, StringBuilder stringBuilder) {
        return InjectionChecker.checkApplication(contextApplication, stringBuilder);
    }

    protected boolean checkConfiguration(Configuration configuration, StringBuilder stringBuilder) {
        return InjectionChecker.checkConfiguration(configuration, stringBuilder);
    }

    protected boolean checkPropertiesDelegate(PropertiesDelegate propertiesDelegate, StringBuilder stringBuilder) {
        return InjectionChecker.checkPropertiesDelegate(propertiesDelegate, stringBuilder);
    }
}
