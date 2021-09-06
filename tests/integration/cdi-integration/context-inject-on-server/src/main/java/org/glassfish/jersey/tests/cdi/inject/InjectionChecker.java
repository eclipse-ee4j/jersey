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
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.WebConfig;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Providers;
import java.lang.annotation.Annotation;
import java.util.Iterator;

class InjectionChecker {
    static final String APPLICATION_PROPERTY = "ApplicationProperty";
    static final String HEADER = "HttpHeader";
    static final String ROOT = "resource";

    static boolean checkApplication(Application application, StringBuilder sb) {
        if (application == null) {
            sb.append("Application is null.");
            return false;
        }
        if (!application.getProperties().containsKey(APPLICATION_PROPERTY)) {
            sb.append("Application does not contain expected key.");
            return false;
        }
        if (!APPLICATION_PROPERTY.equals(application.getProperties().get(APPLICATION_PROPERTY))) {
            sb.append("Application does not contain expected value.");
            return false;
        }
        return true;
    }

    static boolean checkConfiguration(Configuration configuration, StringBuilder sb) {
        if (configuration == null) {
            sb.append("Configuration is null.");
            return false;
        }
        if (!configuration.getProperties().containsKey(APPLICATION_PROPERTY)) {
            sb.append("Configuration does not contain expected key.");
            return false;
        }
        if (!APPLICATION_PROPERTY.equals(configuration.getProperties().get(APPLICATION_PROPERTY))) {
            sb.append("Configuration does not contain expected value.");
            return false;
        }
        return true;
    }

    static boolean checkContainerRequestContext(ContainerRequestContext containerRequestContext, StringBuilder sb) {
        if (containerRequestContext == null) {
            sb.append("ContainerRequestContext is null.");
            return false;
        }

        return checkRequest(containerRequestContext.getRequest(), sb) && checkUriInfo(containerRequestContext.getUriInfo(), sb);
    }

    static boolean checkHttpHeaders(HttpHeaders headers, StringBuilder sb) {
        if (headers == null) {
            sb.append("HttpHeaders is null.");
            return false;
        }
        if (headers.getHeaderString(HEADER) == null) {
            sb.append("HttpHeaders does not contain expected header.");
            return false;
        }
        if (!HEADER.equals(headers.getHeaderString(HEADER))) {
            sb.append("HttpHeaders does not contain expected header value.");
            return false;
        }
        return true;
    }

    static boolean checkParamConverterProvider(ParamConverterProvider provider, StringBuilder sb) {
        if (provider == null) {
            sb.append("ParamConverterProvider is null.");
            return false;
        }
        return true;
    }

    static boolean checkPropertiesDelegate(PropertiesDelegate propertiesDelegate, StringBuilder sb) {
        if (propertiesDelegate == null) {
            sb.append("PropertiesDelegate is null.");
            return false;
        }
        if (null == propertiesDelegate.getProperty(APPLICATION_PROPERTY)) {
            sb.append("PropertiesDelegate does not contain expected key.");
            return false;
        }
        return true;
    }

    static boolean checkProviders(Providers providers, StringBuilder sb) {
        if (providers == null) {
            sb.append("Providers is null.");
            return false;
        }
        MessageBodyWriter<String> mbw =
                providers.getMessageBodyWriter(String.class, String.class, new Annotation[]{}, MediaType.TEXT_PLAIN_TYPE);
        if (mbw == null) {
            sb.append("String MessageBodyWriter is null.");
            return false;
        }
        return true;
    }

    static boolean checkRequest(Request request, StringBuilder sb) {
        if (request == null) {
            sb.append("Request is null.");
            return false;
        }
        final String method = request.getMethod();
        if (method == null) {
            sb.append("Request did not get a method.");
            return false;
        }
        if (!HttpMethod.GET.equals(method)) {
            sb.append("Request did not correct method, but ").append(method).append(" .");
            return false;
        }
        return true;
    }

    static boolean checkResourceContext(ResourceContext context, StringBuilder sb) {
        if (context == null) {
            sb.append("ResourceContext is null.");
            return false;
        }
        ScopedResource resource = context.getResource(ScopedResource.class);
        if (resource == null) {
            sb.append("ResourceContext did not get the resource.");
            return false;
        }
        return true;
    }

    static boolean checkResourceInfo(ResourceInfo info, StringBuilder sb) {
        if (info == null) {
            sb.append("ResourceInfo is null.");
            return false;
        }
        final Class<?> resourceClass = info.getResourceClass();
        if (resourceClass == null) {
            sb.append("ResourceInfo did not get the resource.");
            return false;
        }
        if (!resourceClass.getSimpleName().endsWith("ScopedResource")) {
            sb.append("ResourceInfo did not get the proper resource.");
            return false;
        }
        return true;
    }

    static boolean checkSecurityContext(SecurityContext context, StringBuilder sb) {
        if (context == null) {
            sb.append("SecurityContext is null.");
            return false;
        }
        if (context.isSecure()) {
            sb.append("SecurityContext returned unexpected security.");
            return false;
        }
        return true;
    }

    static boolean checkUriInfo(UriInfo info, StringBuilder sb) {
        if (info == null) {
            sb.append("UriInfo is null.");
            return false;
        }
        if (!info.getPath().startsWith(ROOT)) {
            sb.append("UriInfo does not start with expected ").append(ROOT)
                    .append(" but it is ").append(info.getPath()).append(".");
        }
        return true;
    }

    static boolean checkWebConfig(WebConfig config, StringBuilder sb) {
        if (config == null) {
            sb.append("WebConfig is null.");
            return false;
        }
        if (config.getServletContext() == null) {
            sb.append("WebConfig#getServletContext() is null.");
            return false;
        }
        if (!checkServletContext(config.getServletContext(), sb)) {
            return false;
        }
        if (!checkServletConfig(config.getServletConfig(), sb)) {
            return false;
        }
        return true;
    }

    static boolean checkServletContext(ServletContext context, StringBuilder sb) {
        if (context == null) {
            sb.append("ServletContext is null.");
            return false;
        }
        if (context.getServletRegistrations() == null) {
            sb.append("ServletContext#getServletRegistrations is null.");
            return false;
        }
        Iterator<String> it = context.getServletRegistrations().keySet().iterator();
        if (!it.hasNext()) {
            sb.append("ServletContext#getServletRegistrations is empty.");
            return false;
        }
        if (!ServletContainer.class.getName().equals(it.next())) {
            sb.append("ServletContext#getServletRegistrations does not contain ServletContainer registration.");
            return false;
        }
        return true;
    }

    static boolean checkServletConfig(ServletConfig config, StringBuilder sb) {
        if (config == null) {
            sb.append("ServletConfig is null.");
            return false;
        }
        if (!ServletContainer.class.getName().equals(config.getServletName())) {
            sb.append("ServletConfig has unexpected servlet name ").append(config.getServletName()).append(" .");
            return false;
        }
        return true;
    }

    static boolean checkHttpServletRequest(HttpServletRequest request, StringBuilder sb) {
        if (request == null) {
            sb.append("HttpServletRequest is null.");
            return false;
        }
        if (request.getHeaderNames() == null) {
            sb.append("HttpServletRequest header names is null.");
            return false;
        }
        if (request.getHeader(HEADER) == null) {
            sb.append("HttpServletRequest does not contain expected header.");
            return false;
        }
        if (!HEADER.equals(request.getHeader(HEADER))) {
            sb.append("HttpServletRequest does not contain expected header value.");
            return false;
        }
        return true;
    }

    static boolean checkHttpServletResponse(HttpServletResponse response, StringBuilder sb) {
        if (response == null) {
            sb.append("HttpServletResponse is null.");
            return false;
        }
        if (response.getStatus() != 200) {
            sb.append("HttpServletResponse has unexpectes status.");
            return false;
        }
        return true;
    }
}
